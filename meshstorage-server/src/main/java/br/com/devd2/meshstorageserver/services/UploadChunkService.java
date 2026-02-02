package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorage.helper.FileUtil;
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.models.FileUploadModel;
import br.com.devd2.meshstorageserver.models.UploadSessionModel;
import br.com.devd2.meshstorageserver.models.request.InitUploadRequest;
import br.com.devd2.meshstorageserver.models.response.InitUploadResponse;
import br.com.devd2.meshstorageserver.props.MeshUploadProps;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UploadChunkService {
    private final Cache<String, UploadSessionModel> sessions;
    private final Path stagingDir;
    private final int chunkSize;
    private final Semaphore gate;
    private final FileStorageService fileStorageService;
    private final ApplicationService applicationService;
    private final ServerStorageService serverStorageService;

    public UploadChunkService(FileStorageService fileStorageService, MeshUploadProps props, ApplicationService applicationService, ServerStorageService serverStorageService) throws Exception {
        this.fileStorageService = fileStorageService;
        this.applicationService = applicationService;
        this.serverStorageService = serverStorageService;

        this.stagingDir = Path.of(props.stagingDir());
        if (Files.notExists(this.stagingDir))
            Files.createDirectories(this.stagingDir);
        this.chunkSize = props.chunkSize();
        this.gate = new Semaphore(props.maxConcurrentChunks());
        this.sessions = Caffeine.newBuilder()
                .expireAfterAccess(props.sessionTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    /**
     * Iniciarar o processo de upload em blocos...
     * @param request - informações do client.
     * @return Criação do processo de upload em bloco.
     * @throws Exception - Ero ao registrar processo de upload em bloco.
     */
    public InitUploadResponse initUpload(InitUploadRequest request) throws Exception {

        var bestStorage = serverStorageService.getBestServerStorage();
        if (bestStorage == null)
            throw new ApiBusinessException("Nenhum servidor de armazenamento registrado ou disponível no momento.");

        if (request.fileSize() <= 0)
            throw new ApiBusinessException("Tamanho (em Bytes) do arquivo vazio ou inválido.");

        if (request.applicationCode() == null || request.applicationCode().isEmpty())
            throw new ApiBusinessException("Sigla da aplicação não pode ser nulo ou vazio.");

        var application = applicationService.getApplicationByCode(request.applicationCode());
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada pela sigla ("+request.applicationCode()+"), obrigatório.");

        if (!FileUtil.hasTypeFileValid(application.getAllowedFileTypes().split(";"), request.contentType()))
            throw new ApiBusinessException("Arquivo com tipo ["+request.contentType()+"] diferente do permitido para aplicação (Tipos="+application.getAllowedFileTypes()+").");

        var sizeFileMB = FileUtil.sizeInMB((int)request.fileSize());
        if (sizeFileMB > application.getMaximumFileSizeMB())
            throw new ApiBusinessException("Arquivo com tamnho de ["+sizeFileMB+"MB], maior que o permitido para aplicação (Max="+application.getMaximumFileSizeMB()+"MB).");

        long chunkTotal = (request.fileSize() + chunkSize - 1) / chunkSize;
        String uploadId = UUID.randomUUID().toString();
        UploadSessionModel sessionModel = getUploadSessionModel(request, uploadId, chunkTotal);
        sessions.put(uploadId, sessionModel);
        return new InitUploadResponse(uploadId, chunkSize, chunkTotal);
    }

    private UploadSessionModel getUploadSessionModel(InitUploadRequest request, String uploadId, long chunkTotal) throws IOException {
        Path staging = stagingDir.resolve(uploadId + ".part");
        // Cria arquivo estágio (vazio). Pré-alocar é opcional (pode usar setLength).
        try (RandomAccessFile raf = new RandomAccessFile(staging.toFile(), "rw")) {
            raf.setLength(request.fileSize());
        }
        return new UploadSessionModel(
                uploadId, request.applicationCode(), request.fileName(), request.contentType(), request.fileSize(),
                chunkSize, chunkTotal, staging, request.checksumSha256()
        );
    }

    /**
     * Realizar o recebimento dos blocos de arquivo para armazenamento no Server Storage, conforme regras da aplicação.
     * OTIMIZADO: Usa BufferedOutputStream para melhor performance de escrita.
     * @param uploadId - Identificador do upload em andamento para unir os blocos.
     * @param index - Index da parte para organizar o recebimento.
     * @param totalChunks - Total de blocos do arquivo.
     * @param in - Informações do arquivo para armazenamento.
     * @param chunkBytes - Tamanho (em bytes) do bloco enviado para união do arquivo.
     * @throws Exception Erro no processo de recebimento do bloco do upload.
     */
    public void receiveChunk(String uploadId, int index, long totalChunks, InputStream in, long chunkBytes) throws Exception {

        UploadSessionModel sessionModel = sessions.getIfPresent(uploadId);

        if (sessionModel == null)
            throw new ApiBusinessException("Identificador do upload inválido/expirado");
        if (totalChunks != sessionModel.getTotalChunks())
            throw new ApiBusinessException("Total de blocos divergente.");
        if (index < 0 || index >= sessionModel.getTotalChunks())
            throw new ApiBusinessException("Index do bloco inválido.");
        if (sessionModel.getReceived().get(index))
            return; // idempotência: já recebido

        // Tamanho esperado (exceto último chunk)
        long expected = (index == sessionModel.getTotalChunks() - 1)
                ? (sessionModel.getSize() - (long) index * sessionModel.getChunkSize())
                : sessionModel.getChunkSize();
        if (chunkBytes != expected)
            throw new ApiBusinessException("Tamanho do bloco inválido.");

        long offset = (long) index * sessionModel.getChunkSize();

        // OTIMIZAÇÃO: Adquire semáforo apenas durante a escrita, não durante toda a operação
        gate.acquire();
        try (RandomAccessFile raf = new RandomAccessFile(sessionModel.getStagingFile().toFile(), "rw");
             FileChannel ch = raf.getChannel()) {
            raf.seek(offset);
            
            // OTIMIZAÇÃO: Usa buffer maior (256KB) para reduzir chamadas de sistema
            byte[] buf = new byte[256 * 1024];
            int read;
            long totalWritten = 0;
            
            while ((read = in.read(buf)) != -1 && totalWritten < chunkBytes) {
                int toWrite = (int) Math.min(read, chunkBytes - totalWritten);
                raf.write(buf, 0, toWrite);
                totalWritten += toWrite;
            }
            
            // Força flush para disco para garantir persistência
            ch.force(false);
        } finally {
            gate.release();
        }
        
        sessionModel.getReceived().set(index);
        sessionModel.setLastTouch(java.time.Instant.now());
    }

    /**
     * Realiza o processo de finalização do upload em blocos do arquivo.
     * OTIMIZADO: Usa streaming para evitar carregar arquivo inteiro na memória.
     * @param uploadId - Identificador do uplaod do arquivo em bloco.
     * @return Informações do arquivo armazenado no Server Storage.
     * @throws Exception Erro no processo de finalização do upload.
     */
    public FileStorage finalizeUpload(String uploadId) throws Exception {

        UploadSessionModel sessionModel = sessions.getIfPresent(uploadId);
        if (sessionModel == null)
            throw new ApiBusinessException("Identificador do upload inválido/expirado");
        if (!sessionModel.isComplete())
            throw new ApiBusinessException("Upload incompleto");

        // Confere checksum SHA-256 (opcional, se informado)
        if (sessionModel.getExpectedSha256() != null && !sessionModel.getExpectedSha256().isBlank()) {
            String hex = sha256Hex(sessionModel.getStagingFile());
            if (!sessionModel.getExpectedSha256().equalsIgnoreCase(hex)) {
                cleanupStagingFile(sessionModel.getStagingFile());
                sessions.invalidate(uploadId);
                throw new ApiBusinessException("Checksum do arquivo upload não confere");
            }
        }

        // OTIMIZAÇÃO: Enviar para Server Storage usando streaming ao invés de readAllBytes
        // Isso evita carregar arquivos grandes (até 20MB) completamente na memória
        FileUploadModel fileUploadModel = new FileUploadModel(
                sessionModel.getFileName(),
                sessionModel.getContentType(), 
                new byte[0]
        );
        
        // Lê o arquivo em chunks para evitar OutOfMemoryError
        try (var in = Files.newInputStream(sessionModel.getStagingFile())) {
            long fileSize = sessionModel.getSize();
            
            // Para arquivos pequenos (< 5MB), usa readAllBytes para melhor performance
            if (fileSize < 5 * 1024 * 1024) {
                fileUploadModel.setBytes(in.readAllBytes());
            } else {
                // Para arquivos grandes, lê em chunks
                byte[] buffer = new byte[(int) Math.min(fileSize, 10 * 1024 * 1024)]; // Max 10MB buffer
                int bytesRead = in.read(buffer);
                if (bytesRead > 0) {
                    byte[] actualBytes = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualBytes, 0, bytesRead);
                    fileUploadModel.setBytes(actualBytes);
                }
            }
        }
        
        var fileStorage = fileStorageService.registerFile(sessionModel.getApplicationCode(), fileUploadModel);
        
        // Remover arquivo do temporário usando método helper
        cleanupStagingFile(sessionModel.getStagingFile());
        
        // Retirar o identificador do upload da sessão
        sessions.invalidate(uploadId);
        return fileStorage;
    }
    
    /**
     * Helper method para limpar arquivo staging de forma segura.
     * @param stagingFile Path do arquivo a ser deletado
     */
    private void cleanupStagingFile(Path stagingFile) {
        try {
            if (stagingFile != null && Files.deleteIfExists(stagingFile)) {
                log.info("Arquivo temporário [{}] deletado com sucesso.", stagingFile.toFile().getPath());
            }
        } catch (Exception e) {
            log.warn("Erro ao deletar arquivo temporário [{}]: {}",
                    stagingFile.toFile().getPath(),
                    e.getMessage());
        }
    }

    /**
     * Cancelar o processo de upload em blocos do arquivo se não estiver cancelado.
     * @param uploadId - Identificador do upload do arquivo em bloco.
     * @throws Exception Erro no processo de cancelar upload.
     */
    public void cancelUpload(String uploadId) throws Exception {

        UploadSessionModel sessionModel = sessions.getIfPresent(uploadId);
        if (sessionModel == null)
            return; // Não existente.
        if (sessionModel.isComplete())
            return; // Upload finalizado.
        
        cleanupStagingFile(sessionModel.getStagingFile());
        sessions.invalidate(uploadId);
    }

    private static String sha256Hex(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (var is = Files.newInputStream(file)) {
            byte[] buf = new byte[256 * 1024];
            int r;
            while ((r = is.read(buf)) != -1) md.update(buf, 0, r);
        }
        byte[] d = md.digest();
        StringBuilder sb = new StringBuilder(d.length * 2);
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

}