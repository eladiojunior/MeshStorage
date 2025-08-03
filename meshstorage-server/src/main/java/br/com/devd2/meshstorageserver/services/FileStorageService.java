package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
import br.com.devd2.meshstorage.enums.FileContentTypesEnum;
import br.com.devd2.meshstorage.enums.FileStorageStatusEnum;
import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.FileUtil;
import br.com.devd2.meshstorage.helper.OcrUtil;
import br.com.devd2.meshstorage.models.FileStorageClientDownload;
import br.com.devd2.meshstorage.models.FileStorageClientStatus;
import br.com.devd2.meshstorage.models.messages.FileDeleteMessage;
import br.com.devd2.meshstorage.models.messages.FileDownloadMessage;
import br.com.devd2.meshstorage.models.messages.FileRegisterMessage;
import br.com.devd2.meshstorageserver.config.WebSocketMessaging;
import br.com.devd2.meshstorageserver.entites.*;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperFormat;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.helper.HelperServer;
import br.com.devd2.meshstorageserver.models.response.FileContentTypesResponse;
import br.com.devd2.meshstorageserver.models.response.FileStatusCodeResponse;
import br.com.devd2.meshstorageserver.models.response.ListFileStorageResponse;
import br.com.devd2.meshstorageserver.models.response.QrCodeFileResponse;
import br.com.devd2.meshstorageserver.repositories.FileStorageAccessTokenRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageLogAccessRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.persistence.criteria.Predicate;

@Slf4j
@Service
public class FileStorageService {
    private final FileStorageRepository fileStorageRepository;
    private final FileStorageLogAccessRepository fileStorageLogAccessRepository;
    private final FileStorageAccessTokenRepository fileStorageAccessTokenRepository;
    private final ServerStorageService serverStorageService;
    private final ApplicationService applicationService;
    private final WebSocketMessaging webSocketMessaging;
    private final QrCodeService qrCodeService;

    @Value("${acesso-file-url:\"\"}")
    private String url_file_acess;

    @Value("${quality-compressed-webp:0.85f}")
    private float quality_compressed_webp;

    public FileStorageService(ApplicationService applicationService, FileStorageRepository fileStorageRepository,
                              ServerStorageService serverStorageService, WebSocketMessaging webSocketMessaging,
                              FileStorageLogAccessRepository fileStorageLogAccessRepository, QrCodeService qrCodeService,
                              FileStorageAccessTokenRepository fileStorageAccessTokenRepository) {
        this.applicationService = applicationService;
        this.fileStorageRepository = fileStorageRepository;
        this.fileStorageLogAccessRepository = fileStorageLogAccessRepository;
        this.serverStorageService = serverStorageService;
        this.webSocketMessaging = webSocketMessaging;
        this.qrCodeService = qrCodeService;
        this.fileStorageAccessTokenRepository = fileStorageAccessTokenRepository;
    }

    /**
     * Recupera um arquivo pelo seu Identificador externo.
     * @param idFile - Identificador para recuperação do arquivo no Storage.
     * @return Arquivo recuperado ou nulo se não existir.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage getFile(String idFile) throws ApiBusinessException {

        if (idFile == null || idFile.isEmpty())
            throw new ApiBusinessException("Id File (chave do arquivo) não pode ser nulo ou vazio.");

        var fileStorage = fileStorageRepository.findByIdFile(idFile).orElse(null);
        if (fileStorage == null)
            throw new ApiBusinessException("Arquivo não identificado pelo seu ID ("+idFile+"), obrigatório.");

        String pathFileStorage =  Path.of(fileStorage.getApplicationStorageFolder(), fileStorage.getFileFisicalName()).toString();
        if (fileStorage.getFileStatusCode()==FileStorageStatusEnum.ARCHIVED_SUCESSFULLY.getCode() &&
                fileStorage.getDateTimeBackupFileStorage() != null)
            throw new ApiBusinessException("Arquivo enviado para armazenamento de longo prazo (backup) em [" +
                    fileStorage.getDateTimeBackupFileStorage().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                    "]. Solicite a recuperação do arquivo no backup ["+ pathFileStorage +"].");

        if (fileStorage.getFileStatusCode()==FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode() &&
                fileStorage.getDateTimeRemovedFileStorage() != null)
            throw new ApiBusinessException("Arquivo removido do armazenamento em [" +
                    fileStorage.getDateTimeRemovedFileStorage().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                    "]. Solicite recuperação do arquivo no backup ["+ pathFileStorage +"].");

        //Enviar comando de DOWNLOAD para o Storage...
        var fileStorageMessage = new FileDownloadMessage();
        fileStorageMessage.setIdFile(fileStorage.getIdFile());
        fileStorageMessage.setFileName(fileStorage.getFileFisicalName());
        if (fileStorage.isCompressedFileContent() && fileStorage.getFileCompressed() != null)
            fileStorageMessage.setFileName(fileStorage.getFileCompressed().getCompressedFileFisicalName());
        fileStorageMessage.setApplicationStorageFolder(fileStorage.getApplicationStorageFolder());
        try {

            //TODO implementar para retornar em qual ServerStorage o arquivo foi recurado... ;)
            var fileStorageClientDownload = downloadFileStorageClient(fileStorage.getListFileStorageClient(), fileStorageMessage);

            //Carregar os dados do arquivo.
            byte[] bytesFile = FileBase64Util.base64ToBytes(fileStorageClientDownload.getDataBase64());
            if (fileStorage.isCompressedFileContent() && fileStorage.getFileCompressed() != null
                    && !FileContentTypesEnum.WEBP.getContentType().equals(fileStorage.getFileCompressed()
                    .getCompressedFileContentType())) {
                bytesFile = FileUtil.descompressZipFileContent(bytesFile);
            }

            //Verificar se o hash do arquivo de download está conforme o armazenado no upload ;)
            var hashFileDownload = FileUtil.hashConteudoBytes(bytesFile);
            var hashFileEntity = fileStorage.getHashFileBytes();
            if (fileStorage.isCompressedFileContent() && fileStorage.getFileCompressed()!=null
                    && FileContentTypesEnum.WEBP.getContentType().equals(fileStorage.getFileCompressed()
                    .getCompressedFileContentType())) {
                hashFileEntity = fileStorage.getFileCompressed().getCompressedHashFileBytes();
            }
            if (!hashFileEntity.equals(hashFileDownload))
                log.warn("Arquivo [{}] com HASH diferente, esse arquivo pode ter sido manipulado no Storage!",
                        pathFileStorage);
            //TODO adicionar na mensagem o Id ServerStorage onde o arquivo está ;)

            fileStorage.setFileContent(bytesFile);

            return fileStorage;

        } catch (ApiBusinessException error) {
            throw error;
        }
        catch (Exception error) {
            throw new ApiBusinessException("Erro não esperado: " + error.getMessage());
        }

    }

    /**
     * Registra um arquivo em um Storage disponível e guarda as informações em banco.
     * @param applicationName - Nome da aplicação que está utilizando.
     * @param file - Informações do arquivo.
     * @return Arquivo armazenado conforme solicitação.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage registerFile(String applicationName, MultipartFile file) throws ApiBusinessException {

        if (applicationName == null || applicationName.isEmpty())
            throw new ApiBusinessException("Nome da aplicação não pode ser nulo ou vazio.");

        var application = applicationService.getApplicationByName(applicationName);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada pelo seu nome ("+applicationName+"), obrigatório.");

        if (file == null || file.isEmpty())
            throw new ApiBusinessException("Arquivo físico não pode ser nulo ou vazio.");

        if (!FileUtil.hasTypeFileValid(application.getAllowedFileTypes().split(";"), file.getContentType()))
            throw new ApiBusinessException("Arquivo com tipo ["+file.getContentType()+"] diferente do permitido para aplicação (Tipos="+application.getAllowedFileTypes()+").");

        try {

            var hashFileBytes = "";
            var bytesFile = file.getBytes();
            var sizeFileMB = FileUtil.sizeInMB(bytesFile.length);
            if (sizeFileMB > application.getMaximumFileSizeMB())
                throw new ApiBusinessException("Arquivo com tamnho de ["+sizeFileMB+"MB], maior que o permitido para aplicação (Max="+application.getMaximumFileSizeMB()+"MB).");

            //Criar nome FISICO do arquivo.
            String nomeFisicoArquivo = FileUtil.generatePhisicalName(Objects.requireNonNull(file.getOriginalFilename()));

            //Aplicar HASH nos bytes do arquivo, não no conteúdo.
            hashFileBytes = FileUtil.hashConteudoBytes(bytesFile);
            if (application.isAllowDuplicateFile())
            {//Verificar duplicicade de HASH
                checkFileDuplicationByHash(application.getId(), hashFileBytes);
            }

            //Verificar qual o ServerStorageClient será utilizado...
            List<FileStorageClient> listFileStorageClient = new  ArrayList<>();

            var bestStorage = serverStorageService.getBestServerStorage();
            listFileStorageClient.add(new FileStorageClient(bestStorage.getIdServerStorageClient()));
            if (application.isRequiresFileReplication())
            {//Recuperar mais um ServerStorage para Replicação, se existir.
                var bestStorageReplica = serverStorageService.getBestServerStorage(bestStorage.getIdServerStorageClient());
                if (bestStorageReplica!=null)
                    listFileStorageClient.add(new FileStorageClient(bestStorageReplica.getIdServerStorageClient()));
            }

            boolean isFileCompressedContent = false;
            int lengthBytes = bytesFile.length;
            int lengthBytesCompressed = 0;
            String fileCompressionInformation = "";
            String contentTypeFile = file.getContentType();
            String nameFileCompressed = "";
            String hashFileBytesCompressed = "";
            String contentTypeFileCompressed = "";
            double percentualFileCompressed = 0;

            //Verificar se está configurado para executar a extração de OCR do arquivo.
            boolean isExtractionTextByOrcFormFile = application.isApplyOcrFileContent() &&
                    OcrUtil.isAllowedTypeForOcr(contentTypeFile);

            //Guardar os bytes original do arquivo para pocessamento do OCR.
            var bytesFileOcr = new byte[]{};
            if (isExtractionTextByOrcFormFile)
                bytesFileOcr = bytesFile.clone();

            //Verificar se a aplicação está configurada para compressão de arquivo em ZIP
            // e se o tipo do arquivo é compatível com compressão.
            if (application.isCompressedFileContentToZip() && FileUtil.hasFileTypeNameCompressedZip(contentTypeFile))
            {//Compactar arquivo antes de armazenar
                try {
                    bytesFile = FileUtil.compressZipFileContent(nomeFisicoArquivo, bytesFile);
                    lengthBytesCompressed = bytesFile.length;
                    nameFileCompressed = FileUtil.changeFileNameExtension(nomeFisicoArquivo, FileContentTypesEnum.ZIP.getExtension());
                    percentualFileCompressed = 100.0 - ((double) lengthBytesCompressed / lengthBytes * 100);
                    fileCompressionInformation = contentTypeFile + " >> " + FileContentTypesEnum.ZIP.getContentType() +
                            " [compressão de: " + HelperFormat.formatPercent(percentualFileCompressed) + "]";
                    isFileCompressedContent = true;
                    //Como a conversão foi bem sucedida precisamos gerar o HASH dos bytes do arquivo comprimido.
                    hashFileBytesCompressed = FileUtil.hashConteudoBytes(bytesFile);
                    contentTypeFileCompressed = contentTypeFile; //Como o arquivo é descompactado, manter o memso.
                } catch (Exception error) {
                    log.error("Erro ao realizar compressão do arquivo.", error);
                }
            }

            //Verificar se a aplicação está configurada para conversão de imagem em WebP
            // e se o tipo do arquivo é compatível para realizar essa conversão.
            if (application.isConvertImageFileToWebp() && FileUtil.hasFileTypeCompressedWebP(file.getContentType()))
            {//Converter imagem em um formato mais leve (WebP).
                try {
                    byte[] bytesFileWebp = FileUtil.convertImagemToWebp(bytesFile, quality_compressed_webp);
                    if (bytesFileWebp.length != 0 && bytesFileWebp.length < lengthBytes) {
                        lengthBytesCompressed = bytesFileWebp.length;
                        nameFileCompressed = FileUtil.changeFileNameExtension(nomeFisicoArquivo,
                                FileContentTypesEnum.WEBP.getExtension());
                        nomeFisicoArquivo = nameFileCompressed; //Manter o nome.
                        percentualFileCompressed = 100.0 - ((double) bytesFileWebp.length / lengthBytes * 100);
                        fileCompressionInformation = contentTypeFile + " >> " + FileContentTypesEnum.
                                WEBP.getContentType() + " [quality: " + quality_compressed_webp + ", compressão de: " +
                                HelperFormat.formatPercent(percentualFileCompressed) + "]";
                        isFileCompressedContent = true;
                        bytesFile = bytesFileWebp.clone();
                        //Como a conversão foi bem sucedida precisamos gerar o HASH dos bytes do arquivo comprimido.
                        hashFileBytesCompressed = FileUtil.hashConteudoBytes(bytesFile);
                        contentTypeFileCompressed = FileContentTypesEnum.WEBP.getContentType();
                    }
                } catch (Exception error) {
                    log.error("Erro ao realizar conversão da imagem para WebP.", error);
                }
            }

            var fileStorageEntity = new FileStorage();
            fileStorageEntity.setApplication(application);
            fileStorageEntity.setIdFile(UUID.randomUUID().toString());
            fileStorageEntity.setApplicationStorageFolder(application.getApplicationName());
            fileStorageEntity.setFileLogicName(file.getOriginalFilename());
            fileStorageEntity.setFileFisicalName(nomeFisicoArquivo);
            fileStorageEntity.setFileLength(lengthBytes);
            fileStorageEntity.setFileContent(bytesFile);
            fileStorageEntity.setFileContentType(contentTypeFile);
            fileStorageEntity.setHashFileBytes(hashFileBytes);
            fileStorageEntity.setCompressedFileContent(isFileCompressedContent);
            //Informações da compressão do arquivo...
            if (isFileCompressedContent) {
                var fileStorageCompressedEntity = new FileStorageCompressed();
                fileStorageCompressedEntity.setCompressedFileFisicalName(nameFileCompressed);
                fileStorageCompressedEntity.setCompressedFileLength(lengthBytesCompressed);
                fileStorageCompressedEntity.setCompressedFileContentType(contentTypeFileCompressed);
                fileStorageCompressedEntity.setCompressionFileInformation(fileCompressionInformation);
                fileStorageCompressedEntity.setCompressedHashFileBytes(hashFileBytesCompressed);
                fileStorageCompressedEntity.setPercentualCompressedFileContent(percentualFileCompressed);
                fileStorageEntity.setFileCompressed(fileStorageCompressedEntity);
            }
            //Informações da extração OCR do conteúdo do arquivo...
            fileStorageEntity.setExtractionTextFileByOcr(isExtractionTextByOrcFormFile);
            if (isExtractionTextByOrcFormFile) {
                var fileOcrExtractionEntity = new FileOcrExtraction();
                fileOcrExtractionEntity.setExtractionTextByOrcStatusCode(ExtractionTextByOcrStatusEnum.IN_PROCESSING.getCode());
                fileStorageEntity.setFileExtractionByOcr(fileOcrExtractionEntity);
            }
            //Informações de registro e status do arquivo...
            fileStorageEntity.setFileStatusCode(FileStorageStatusEnum.SENT_TO_STORAGE.getCode());
            fileStorageEntity.setDateTimeRegisteredFileStorage(LocalDateTime.now());

            //Enviar para armazenar fisicamente...
            var fileRegisterMessage = new FileRegisterMessage();
            fileRegisterMessage.setIdFile(fileStorageEntity.getIdFile());
            fileRegisterMessage.setFileName(fileStorageEntity.getFileFisicalName());
            fileRegisterMessage.setApplicationStorageFolder(fileStorageEntity.getApplicationStorageFolder());
            fileRegisterMessage.setDataBase64(FileBase64Util.fileToBase64(bytesFile));

            FileStorageClientStatus fileStorageClientStatus = uploadFileStorageClient(listFileStorageClient, fileRegisterMessage);
            for (FileStorageClient fileStorageClient : listFileStorageClient)
                fileStorageEntity.addFileStorageClient(fileStorageClient);

            //Atualizar o status do arquivo...
            fileStorageEntity.setFileStatusCode(fileStorageClientStatus.getFileStatusCode());

            //Gravar status...
            fileStorageRepository.save(fileStorageEntity);

            //Registrar mais um arquivo requistrado no Server Storage e Aplicação...
            if (fileStorageClientStatus.getFileStatusCode() == FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode()) {

                //Atualizar totalizador...
                serverStorageService.updateServerStorageTotalFile(bestStorage.getId(), true);
                applicationService.updateApplicationTotalFile(application.getId(), true);

                if (isExtractionTextByOrcFormFile)
                {//Colocar na fila para processamento do OCR do arquivo...
                    OcrUtil.sendExtractionTextFormFile(fileStorageEntity.getId(), hashFileBytes, bytesFileOcr);
                }

            }

            return fileStorageEntity;

        } catch (ApiBusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new ApiBusinessException(error.getMessage());
        }

    }

    /**
     * Verifica se o arquivo já existe, seu armazenamento confirmado, para uma aplicação (ID)
     * com base no HASH dos bytes do arquivo.
     * @param applicationId - Identificador da aplicação que está armazenando o arquivo.
     * @param hashFileBytes - HASH dos bytes do arquivos para verificação da duplicidade.
     * @throws ApiBusinessException Caso exista duplicidade será retornado um exception de negócio.
     */
    private void checkFileDuplicationByHash(Long applicationId, String hashFileBytes) throws ApiBusinessException {
        var fileStorage = fileStorageRepository.findByApplicationIdAndHashFileBytes(
                        applicationId, hashFileBytes).stream().filter(f ->
                        f.getFileStatusCode()==FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode())
                .findFirst().orElse(null);
        if (fileStorage != null)
            throw new ApiBusinessException("Arquivo já existe na aplicação e armazenado confirmado, duplicidade não é permitido.");
    }

    /**
     * Solicita a remoção de um arquivo do Server Storage.
     * @param idFile - Identificador do arquivo para remover.
     */
    public FileStorage deleteFile(String idFile) throws ApiBusinessException {

        if (idFile == null || idFile.isEmpty())
            throw new ApiBusinessException("Identificador do arquivo não pode ser nulo ou vazio.");

        var fileStorage = fileStorageRepository.findByIdFile(idFile).orElse(null);
        if (fileStorage == null)
            throw new ApiBusinessException("Arquivo não identificado pelo seu ID ("+idFile+"), obrigatório.");

        if (fileStorage.getFileStatusCode()==FileStorageStatusEnum.ARCHIVED_SUCESSFULLY.getCode() &&
                fileStorage.getDateTimeBackupFileStorage() != null)
            throw new ApiBusinessException("Arquivo enviado para armazenamento de longo prazo (backup) em [" +
                    HelperFormat.formatDateTime(fileStorage.getDateTimeBackupFileStorage(), "dd/MM/yyyy HH:mm:ss") +
                    "]. Não será possível remove-lo.");

        if (fileStorage.getFileStatusCode()==FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode() &&
                fileStorage.getDateTimeRemovedFileStorage() != null)
            throw new ApiBusinessException("Arquivo removido do armazenamento em [" +
                    HelperFormat.formatDateTime(fileStorage.getDateTimeRemovedFileStorage(), "dd/MM/yyyy HH:mm:ss") +
                    "]. Não é possível remove-lo novamente.");

        //Enviar comando de DELETE para o Storage...
        var fileDeleteMessage = new FileDeleteMessage();
        fileDeleteMessage.setIdFile(fileStorage.getIdFile());
        fileDeleteMessage.setApplicationStorageFolder(fileStorage.getApplicationStorageFolder());
        fileDeleteMessage.setFileName(fileStorage.getFileFisicalName());

        try {

            var listFileStorageClient = fileStorage.getListFileStorageClient();
            var fileStorageClientStatus = deleteFileStorageClient(listFileStorageClient, fileDeleteMessage);

            //Atualiza a quantidade de arquivo no Server Storage e Aplicação...
            if (fileStorageClientStatus.getFileStatusCode() == FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode()) {

                //Gravar exclusão logica...
                fileStorage.setDateTimeRemovedFileStorage(LocalDateTime.now());
                fileStorage.setFileStatusCode(fileStorageClientStatus.getFileStatusCode());
                fileStorageRepository.save(fileStorage);

                for (FileStorageClient client : listFileStorageClient) {
                    var serverStorage = serverStorageService.getByIdServerStorageClient(client.getIdServerStorageClient());
                    if (serverStorage != null)
                        serverStorageService.updateServerStorageTotalFile(serverStorage.getId(), false);
                }
                if (fileStorage.getApplication() != null)
                    applicationService.updateApplicationTotalFile(fileStorage.getApplication().getId(), false);

            }

            return fileStorage;

        } catch (ApiBusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new ApiBusinessException(error.getMessage());
        }

    }

    /**
     * Recupera a lista de arquivos de uma aplicação (nome) de forma painada.
     * @param applicationName - Nome da aplicação para recuperação dos arquivos.
     * @param pageNumber - Número da página da paginação
     * @param recordsPerPage - Número de registros por página.
     * @param isFilesSentForBackup - indicador de filtro dos arquivos enviados para o backup, armazenamento de longo prazo.
     * @param isFilesRemoved - indicado de filtro dos arquivos removidos do armazenamento.
     * @return Instancia com a lista de arquivos da aplicação.
     */
    public ListFileStorageResponse listFilesByApplicationName(String applicationName, int pageNumber, int recordsPerPage, boolean isFilesSentForBackup, boolean isFilesRemoved) throws ApiBusinessException {

        if (applicationName == null || applicationName.isEmpty())
            throw new ApiBusinessException("Nome da aplicação não pode ser nulo ou vazio.");

        var application = applicationService.getApplicationByName(applicationName);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada pelo seu nome ("+applicationName+"), obrigatório.");

        if (pageNumber == 0) pageNumber = 1;
        if (recordsPerPage == 0) recordsPerPage = 15;

        Specification<FileStorage> specification = (root, q, cb) -> {
            List<Integer> fileStatusCodes = new ArrayList<>();
            fileStatusCodes.add(FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode());
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("application").get("id"), application.getId()));
            if (isFilesSentForBackup)
                fileStatusCodes.add(FileStorageStatusEnum.ARCHIVED_SUCESSFULLY.getCode());
            if (isFilesRemoved)
                fileStatusCodes.add(FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode());
            predicates.add(root.get("fileStatusCode").in(fileStatusCodes));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        ListFileStorageResponse result = new ListFileStorageResponse();
        var totalRecords = fileStorageRepository.count(specification);
        result.setTotalRecords(totalRecords);
        Pageable pageable = PageRequest.of(pageNumber-1, recordsPerPage);
        var listFileStorage = fileStorageRepository.findAll(specification, pageable);
        var listFileResponse = HelperMapper.ConvertToResponseListFileStorage(listFileStorage.stream().toList());
        result.setFiles(listFileResponse);
        return result;

    }

    /**
     * Lista a tabela de domínio dos Status dos arquivos no Server Storage.
     * @return Lista de status dos arquivos.
     */
    public List<FileStatusCodeResponse> listStatusCodeFiles() {
        List<FileStatusCodeResponse> result = new ArrayList<>();
        for (var item_enum : FileStorageStatusEnum.values()) {
            FileStatusCodeResponse item = new FileStatusCodeResponse();
            item.setCode(item_enum.getCode());
            item.setNameEnum(item_enum.name());
            item.setDescription(item_enum.getDescription());
            result.add(item);
        }
        return result;
    }

    /**
     * Lista a tabela de domínio dos ContentType dos arquivos no Server Storage.
     * @return Lista de ContentTypes dos arquivos.
     */
    public List<FileContentTypesResponse> listContentTypesFiles() {
        List<FileContentTypesResponse> result = new ArrayList<>();
        for (var item_enum : FileContentTypesEnum.values()) {
            FileContentTypesResponse item = new FileContentTypesResponse();
            item.setCode(item_enum.getCode());
            item.setNameEnum(item_enum.name());
            item.setExtension(item_enum.getExtension());
            item.setDescription(item_enum.getDescription());
            item.setContentType(item_enum.getContentType());
            result.add(item);
        }
        return result;
    }

    /**
     * Gerar um QR Code de uma arquivo específico para acesso do arquivo diretamente.
     * @param idFile - Identificador do arquivo.
     * @param tokenExpirationTime - Tempo de expiração do token de acesso, em minutos, se 0 (zero) nunca expira.
     * @param maximumAccessestoken - Quantidade de acesso máxima ao arquivo com o token, se 0 (zero) não tem limite.
     * @return Bytes da imagem do QR Code
     */
    public QrCodeFileResponse generateQrCode(String idFile, long tokenExpirationTime, int maximumAccessestoken) throws Exception {

        var file = fileStorageRepository.findByIdFile(idFile).orElse(null);
        if (file == null)
            throw new ApiBusinessException("Arquivo com o ID invalido ou não existente.");

        if (url_file_acess == null || url_file_acess.isEmpty())
            throw new ApiBusinessException("URL de acesso ao arquivo do repositório diretamente não configurada.");

        QrCodeFileResponse response = new QrCodeFileResponse();
        String tokenPublic = generateTokenAccess(file, tokenExpirationTime, maximumAccessestoken);
        String link = String.format(url_file_acess, tokenPublic);
        byte[] imagemQrCode = qrCodeService.createQrImage(link, file.getFileFisicalName());

        response.setIdFile(idFile);
        response.setLinkAcessFile(link);
        response.setImageQrCodeAcessFile(imagemQrCode);
        response.setDateTimeRegisteredFileStorage(LocalDateTime.now());
        return response;

    }

    /**
     * Gerar um token de acesso de uma arquivo específico para acesso ao arquivo de forma pública e diretamente.
     * @param fileStorage - Instancia do arquivo.
     * @param tokenExpirationTime - Tempo de expiração do token de acesso, em minutos, se 0 (zero) nunca expira.
     * @param maximumAccessestoken - Quantidade de acesso máxima ao arquivo com o token, se 0 (zero) não tem limite.
     * @return Token de acesso regado.
     **/
    private String generateTokenAccess(FileStorage fileStorage, long tokenExpirationTime, int maximumAccessestoken) {

        String accessToken = UUID.randomUUID().toString();

        FileAccessToken tokenAccess = new FileAccessToken();
        tokenAccess.setIdFile(fileStorage.getIdFile());
        tokenAccess.setFileStorage(fileStorage);
        tokenAccess.setAccessToken(accessToken);
        tokenAccess.setTokenExpirationTime(tokenExpirationTime);
        tokenAccess.setMaximumAccessesToken(maximumAccessestoken);
        tokenAccess.setDateTimeRegistered(LocalDateTime.now());

        fileStorageAccessTokenRepository.saveAsync(tokenAccess);

        return accessToken;
    }

    /**
     * Recupera um arquivo da estrutura de armazenamento pelo ID apos a verificação do token de acesso.
     * @param token - Token de acesso ao arquivo.
     * @return Arquivo recuperado ou nulo se não existir.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage getFileByToken(String token) throws ApiBusinessException {

        if (token == null ||  token.isEmpty())
            throw new ApiBusinessException("Token de acesso inválido ou não informado.");

        FileAccessToken accessToken = fileStorageAccessTokenRepository.findByAccessToken(token).orElse(null);
        if (accessToken == null)
            throw new ApiBusinessException("Token de acesso inválido ou não existente.");

        if (accessToken.getTokenExpirationTime() != 0)
        {//Verificar se o token está expirado...

            LocalDateTime dateTimeExpiration = accessToken.getDateTimeRegistered()
                    .plusMinutes(accessToken.getMaximumAccessesToken());

            if (dateTimeExpiration.isAfter(LocalDateTime.now()))
                throw new ApiBusinessException(String.format("Token de acesso expirado em [%s].",
                        HelperFormat.formatDateTime(dateTimeExpiration, "dd/MM/yyyy HH:mm:ss")));

        }

        if (accessToken.getMaximumAccessesToken() != 0)
        {//Verificar se o token já chegou a quantidade máxima de acessos.

            int numberAccessestoken = fileStorageLogAccessRepository.countByUserName(accessToken.getAccessToken());
            if (numberAccessestoken >= accessToken.getMaximumAccessesToken())
                throw new ApiBusinessException(
                        String.format("Token de acesso com limite de acesso máximo [%s].", numberAccessestoken));
        }

        return getFile(accessToken.getIdFile());

    }


    /**
     * Responsável por recupera um arquivo do Storage em caso de replica acessar o que tiver o arquivo.
     * @param listFileStorageClient - Lista de Storages que o arquivo está.
     * @param fileStorageMessage - Informações do arquivo a ser recuperado no Storage.
     * @return instância do download do arquivo.
     */
    private FileStorageClientDownload downloadFileStorageClient(List<FileStorageClient> listFileStorageClient,
                                                                FileDownloadMessage fileStorageMessage) throws ApiBusinessException {

        boolean hasReplica = listFileStorageClient.size() > 1;
        ApiBusinessException lastError = null;
        String idServerStorageClient;
        List<String> idServerStorageClientErrors = new ArrayList<>();

        LocalDateTime timestampStart = LocalDateTime.now();

        for (FileStorageClient client : listFileStorageClient) {

            idServerStorageClient = client.getIdServerStorageClient();

            try {

                FileStorageClientDownload result =
                        webSocketMessaging.startFileDownloadClient(idServerStorageClient, fileStorageMessage);

                //Verificar se teve sucesso, e não precisa acionar as replicas, se existir.
                if (!result.isError()) {
                    //Atualizar a metrics de erro no Server Storage... atualizar em backgroud...
                    if (!idServerStorageClientErrors.isEmpty())
                        serverStorageService.updateMetricsErrors(idServerStorageClientErrors);
                    return result;
                }

                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.

                //Guarda para lançar depois, mas só se não houver mais tentativas.
                log.warn("Erro no downlod do arquivo {} no ServerStorageClient {}, tentar no próximo Storage.",
                        fileStorageMessage.getFileName(), client.getIdServerStorageClient());
                lastError = new ApiBusinessException(result.getMessageError());

                //Verificar se existe uma replica para recperar.
                if (!hasReplica)
                    break;

            } catch (ApiBusinessException error_negocio) {
                lastError = error_negocio;
                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.
            } catch (InterruptedException | ExecutionException error) {
                lastError = new ApiBusinessException(error.getMessage());
                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.
            } catch (TimeoutException error) {
                lastError = new ApiBusinessException("Erro de timeout do Storage [" + idServerStorageClient + "] no download do arquivo.");
                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.
            } finally { //Calcular o tempo de resposta do Upload...
                var responseTime = HelperServer.elapsedMillis(timestampStart, LocalDateTime.now());
                serverStorageService.updateMetricsResponseTimeAndRequestCount(idServerStorageClient, responseTime);
            }

        }

        //Atualizar a metrics de erro no Server Storage... atualizar em backgroud...
        serverStorageService.updateMetricsErrors(idServerStorageClientErrors);
        throw lastError != null ? lastError :
                new ApiBusinessException("Falha em todas tentativas de realizar o download do arquivo.");

    }

    /**
     * Responsável por realizar o upload do arquivo em todas as replicas de Storage,
     * objetivo é armazenar em pelo menos um Storage.
     * @param listFileStorageClient - Lista de Storage para upload do arquivo.
     * @param fileRegisterMessage - Informações do arquivo para upload nos Storages.
     * @return Status do Upload do aquivo nos Storages.
     */
    private FileStorageClientStatus uploadFileStorageClient(List<FileStorageClient> listFileStorageClient,
                                                            FileRegisterMessage fileRegisterMessage) throws ApiBusinessException {

        boolean hasReplica = listFileStorageClient.size() > 1;
        ApiBusinessException lastError = null;
        String idServerStorageClient;
        List<String> idServerStorageClientErrors = new ArrayList<>();
        FileStorageClientStatus resultSuccess = null;

        LocalDateTime timestampStart = LocalDateTime.now();

        for (FileStorageClient client : listFileStorageClient) {

            idServerStorageClient = client.getIdServerStorageClient();

            try {

                FileStorageClientStatus lastResult = webSocketMessaging.startFileRegisterClient(idServerStorageClient, fileRegisterMessage);
                if (lastResult.isError()) {
                    idServerStorageClientErrors.add(idServerStorageClient);
                    lastError = new ApiBusinessException(lastResult.getMessageError());
                    if (hasReplica) {
                        log.warn("Erro no upload no arquivo {} no ServerStorageClient {}, tentar no próximo Storage.",
                                fileRegisterMessage.getFileName(), client.getIdServerStorageClient());
                    }
                } else
                    resultSuccess = lastResult;

            } catch (ApiBusinessException error_negocio) {
                lastError = error_negocio;
                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.
            } catch (InterruptedException | ExecutionException error) {
                lastError = new ApiBusinessException(error.getMessage());
                idServerStorageClientErrors.add(idServerStorageClient);
            }
            catch (TimeoutException error) {
                lastError = new ApiBusinessException("Erro de timeout do Storage [" + idServerStorageClient +
                        "] no upload do arquivo.");
                idServerStorageClientErrors.add(idServerStorageClient);
            } finally { //Calcular o tempo de resposta do Upload...
                var responseTime = HelperServer.elapsedMillis(timestampStart, LocalDateTime.now());
                serverStorageService.updateMetricsResponseTimeAndRequestCount(idServerStorageClient, responseTime);
            }

        }

        if (!idServerStorageClientErrors.isEmpty()) {
            //Atualizar a metrics de erro no Server Storage... atualizar em backgroud...
            serverStorageService.updateMetricsErrors(idServerStorageClientErrors);
            if (idServerStorageClientErrors.size() == listFileStorageClient.size()) //Todos as tentativas de uploads falharam...
                throw lastError;
            //Remover os ServerStorageClient que deram erro...
            listFileStorageClient.removeIf(f -> idServerStorageClientErrors.contains(f.getIdServerStorageClient()));
        }

        return resultSuccess;

    }

    /**
     * Responsável por remover o arquivo de todos os Storages, mestre e suas replicas.
     * @param listFileStorageClient - Lista de Storage para remoção do arquivo.
     * @param fileDeleteMessage - Informções do arquivo para realizar a remoção dos Storages.
     * @return Status da Remoção do aquivo dos Storages.
     */
    private FileStorageClientStatus deleteFileStorageClient(List<FileStorageClient> listFileStorageClient,
                                                            FileDeleteMessage fileDeleteMessage) throws ApiBusinessException {

        ApiBusinessException lastError = null;
        String idServerStorageClient;
        List<String> idServerStorageClientErrors = new ArrayList<>();
        FileStorageClientStatus resultSuccess = null;

        LocalDateTime timestampStart = LocalDateTime.now();

        for (FileStorageClient client : listFileStorageClient) {

            idServerStorageClient = client.getIdServerStorageClient();

            try {

                FileStorageClientStatus lastResult = webSocketMessaging.startFileDeleteClient(idServerStorageClient, fileDeleteMessage);
                if (lastResult.isError()) {
                    idServerStorageClientErrors.add(idServerStorageClient);
                    lastError = new ApiBusinessException(lastResult.getMessageError());
                } else
                    resultSuccess = lastResult;

            } catch (ApiBusinessException error_negocio) {
                lastError = error_negocio;
                idServerStorageClientErrors.add(idServerStorageClient); //Registrar ID com erro.
            } catch (InterruptedException | ExecutionException error) {
                lastError = new ApiBusinessException(error.getMessage());
                idServerStorageClientErrors.add(idServerStorageClient);
            } catch (TimeoutException error) {
                lastError = new ApiBusinessException("Erro de timeout do Storage [" + idServerStorageClient +
                        "] na remoção do arquivo.");
                idServerStorageClientErrors.add(idServerStorageClient);
            } finally { //Calcular o tempo de resposta do Upload...
                var responseTime = HelperServer.elapsedMillis(timestampStart, LocalDateTime.now());
                serverStorageService.updateMetricsResponseTimeAndRequestCount(idServerStorageClient, responseTime);
            }

        }

        if (!idServerStorageClientErrors.isEmpty()) {
            serverStorageService.updateMetricsErrors(idServerStorageClientErrors);
            if (idServerStorageClientErrors.size() == listFileStorageClient.size()) //Todos as tentativas de delete falharam...
                throw lastError;
            //Remover os ServerStorageClient que deram erro...
            listFileStorageClient.removeIf(f -> idServerStorageClientErrors
                    .contains(f.getIdServerStorageClient()));
            for (String idServerStorageClientError : idServerStorageClientErrors) {
                log.warn("Erro na remoção do arquivo {} do ServerStorageClient {}, verifique o Storage.",
                        fileDeleteMessage.getFileName(), idServerStorageClientError);
            }
        }

        return resultSuccess;

    }

}