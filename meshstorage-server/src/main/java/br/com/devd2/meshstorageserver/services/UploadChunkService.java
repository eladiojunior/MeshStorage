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
                .maximumSize(10000) // ajuste conforme necessidade
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
        Path staging = stagingDir.resolve(uploadId + ".part");
        // Cria arquivo estágio (vazio). Pré-alocar é opcional (pode usar setLength).
        try (RandomAccessFile raf = new RandomAccessFile(staging.toFile(), "rw")) {
            raf.setLength(request.fileSize());
        }
        UploadSessionModel sessionModel = new UploadSessionModel(
                uploadId, request.applicationCode(), request.fileName(), request.contentType(), request.fileSize(),
                chunkSize, chunkTotal, staging, request.checksumSha256()
        );
        sessions.put(uploadId, sessionModel);
        return new InitUploadResponse(uploadId, chunkSize, chunkTotal);
    }

    /**
     * Realizar o recebimento dos blocos de arquivo para armazenamento no Server Storage, conforme regras da aplicação.
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

        gate.acquire();
        try (RandomAccessFile raf = new RandomAccessFile(sessionModel.getStagingFile().toFile(), "rw");
             FileChannel ch = raf.getChannel()) {
            raf.seek(offset);
            byte[] buf = new byte[64 * 1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                raf.write(buf, 0, read);
            }
        } finally {
            gate.release();
        }
        sessionModel.getReceived().set(index);
        sessionModel.setLastTouch(java.time.Instant.now());
    }

    /**
     * Realiza o processo de finalização do upload em blocos do arquivo.
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
                Files.deleteIfExists(sessionModel.getStagingFile());
                sessions.invalidate(uploadId);
                throw new ApiBusinessException("Checksum do arquivo upload não confere");
            }
        }

        // Enviar realmente para o Server Storage disponível...
        FileUploadModel fileUploadModel = new  FileUploadModel(sessionModel.getFileName(),
                sessionModel.getContentType(), new byte[0]);
        try (var in = Files.newInputStream(sessionModel.getStagingFile())) {
            fileUploadModel.setBytes(in.readAllBytes());
        }
        var fileStorage = fileStorageService.registerFile(sessionModel.getApplicationCode(), fileUploadModel);
        // Remover arquivo do temporário...
        if (Files.deleteIfExists(sessionModel.getStagingFile())) {
            log.info("Arquivo temporário [{}] deletado com sucesso.",
                    sessionModel.getStagingFile().toFile().getPath());
        }
        // Retirar o identificador do upload da sessão...
        sessions.invalidate(uploadId);
        return fileStorage;

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
        if (Files.deleteIfExists(sessionModel.getStagingFile())) {
            log.info("Arquivo temporário [{}] deletado com sucesso.",
                    sessionModel.getStagingFile().toFile().getPath());
        }
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