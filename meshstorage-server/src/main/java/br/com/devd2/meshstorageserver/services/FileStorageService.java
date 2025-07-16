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
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.entites.FileStorageAccessToken;
import br.com.devd2.meshstorageserver.entites.FileStorageLogAccess;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperFormat;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.UserAccessModel;
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
     * @param user - Informações do usuário (opcional)
     * @return Arquivo recuperado ou nulo se não existir.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage getFile(String idFile, UserAccessModel user) throws ApiBusinessException {

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
        fileStorageMessage.setApplicationStorageFolder(fileStorage.getApplicationStorageFolder());
        try {

            FileStorageClientDownload fileStorageClientDownload =
                    webSocketMessaging.startFileDownloadClient(fileStorage.getIdClientStorage(), fileStorageMessage);
            if (fileStorageClientDownload.isError())
                throw new ApiBusinessException(fileStorageClientDownload.getMessageError());

            //Carregar os dados do arquivo.
            byte[] bytesFile = FileBase64Util.base64ToBytes(fileStorageClientDownload.getDataBase64());
            if (fileStorage.isCompressedFileContent()) {
                bytesFile = FileUtil.descompressZipFileContent(bytesFile);
            }

            var hashFileDownload = FileUtil.hashConteudo(bytesFile);
            if (!fileStorage.getHashFileBytes().equals(hashFileDownload))
                log.warn("Arquivo [{}] com HASH diferente, esse arquivo pode ter sido manipulado no Storage!",
                        pathFileStorage);

            fileStorage.setFileContent(bytesFile);

            //Registrar histórico de acesso ao arquivo...
            RegisterFileAccessHistory(fileStorage, user);

            return fileStorage;

        } catch (ApiBusinessException error) {
            throw error;
        }
        catch (InterruptedException | ExecutionException error) {
            throw new ApiBusinessException(error.getMessage());
        }
        catch (TimeoutException error) {
            throw new ApiBusinessException("Erro de timeout do Storage ["+fileStorage.getIdClientStorage()+"] no download do arquivo.");
        }
        catch (Exception error) {
            throw new ApiBusinessException("Erro não esperado: " + error.getMessage());
        }

    }

    /**
     * Resposável por registrar as informações de acesso ao arquivo no histórico.
     * Não teve afetar o processamento de recuperação do arquivo, caso ocorra erro deve registrar e
     * seguir com o processamento.
     * @param fileStorage - Informações do arquivo acessado.
     * @param user - Informações do usuário que está acessando o arquivo.
     */
    private void RegisterFileAccessHistory(FileStorage fileStorage, UserAccessModel user) {

        try {

            FileStorageLogAccess fileStorageAccessLog = new FileStorageLogAccess();
            fileStorageAccessLog.setFileStorage(fileStorage);
            if (user != null) {
                fileStorageAccessLog.setUserName(user.getUserName());
                fileStorageAccessLog.setIpUser(user.getIpUser());
                fileStorageAccessLog.setUserAgent(user.getUserAgent());
                fileStorageAccessLog.setAccessChanel(user.getAccessChanel());
            }
            fileStorageAccessLog.setDateTimeRegisteredAccess(LocalDateTime.now());

            fileStorageLogAccessRepository.saveAsync(fileStorageAccessLog);

        } catch (Exception error) {
            log.error("Erro ao registrar acesso ao arquivo.", error);
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
            String nomeFisicoArquivo = FileUtil.generatePisicalName(Objects.requireNonNull(file.getOriginalFilename()));

            //Aplicar HASH nos bytes do arquivo, não no conteúdo.
            hashFileBytes = FileUtil.hashConteudo(bytesFile);

            if (application.isAllowDuplicateFile())
            {//Verificar duplicicade de HASH
                var fileStorage = fileStorageRepository.findByApplicationIdAndHashFileBytes(
                        application.getId(), hashFileBytes).orElse(null);
                if (fileStorage != null && fileStorage.getFileStatusCode()==FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode())
                    throw new ApiBusinessException("Arquivo já existe na aplicação e armazenado confirmado, duplicidade não é permitido.");
            }

            boolean fileCompressedContent = false;
            int lengthBytes = bytesFile.length;
            int lengthBytesCompressed = 0;
            String fileCompressionInformation = "";
            String contentTypeFile = file.getContentType();

            //Verificar se está configurado para executar a extração de OCR do arquivo.
            boolean hasExtractionTextByOrcFormFile = application.isApplyOcrFileContent() &&
                    OcrUtil.isAllowedTypeForOcr(contentTypeFile);

            //Guardar os bytes original do arquivo para pocessamento do OCR.
            var bytesFileOcr = new byte[]{};
            if (hasExtractionTextByOrcFormFile)
                bytesFileOcr = bytesFile.clone();

            if (application.isCompressedFileContentToZip() && FileUtil.hasFileTypeNameCompressedZip(contentTypeFile))
            {//Compactar arquivo antes de armazenar
                try {
                    bytesFile = FileUtil.compressZipFileContent(nomeFisicoArquivo, bytesFile);
                    lengthBytesCompressed = bytesFile.length;
                    nomeFisicoArquivo = FileUtil.changeFileNameExtension(nomeFisicoArquivo, FileContentTypesEnum.ZIP.getExtension());
                    double percentual_compressed = 100.0 - ((double) lengthBytesCompressed / lengthBytes * 100);
                    fileCompressionInformation = contentTypeFile + " >> " + FileContentTypesEnum.ZIP.getContentType() +
                            " [compressão de: " + HelperFormat.formatPercent(percentual_compressed) + "]";
                    fileCompressedContent = true;
                } catch (Exception error) {
                    log.error("Erro ao realizar compressão do arquivo.", error);
                }
            }

            if (application.isConvertImageFileToWebp() && FileUtil.hasFileTypeCompressedWebP(file.getContentType()))
            {//Converter imagem em um formato mais leve.
                try {
                    byte[] bytesFileWebp = FileUtil.convertImagemToWebp(bytesFile, quality_compressed_webp);
                    if (bytesFileWebp.length != 0 && bytesFileWebp.length < lengthBytes) {
                        lengthBytesCompressed = bytesFileWebp.length;
                        nomeFisicoArquivo = FileUtil.changeFileNameExtension(nomeFisicoArquivo, FileContentTypesEnum.WEBP.getExtension());
                        double percentual_compressed = 100.0 - ((double) bytesFileWebp.length / lengthBytes * 100);
                        fileCompressionInformation = contentTypeFile + " >> " + FileContentTypesEnum.WEBP.getContentType() +
                                " [quality: " + quality_compressed_webp + ", compressão de: " + HelperFormat.formatPercent(percentual_compressed) + "]";
                        fileCompressedContent = true;
                        contentTypeFile = FileContentTypesEnum.WEBP.getContentType();
                        bytesFile = bytesFileWebp.clone();
                    }
                } catch (Exception error) {
                    log.error("Erro ao realizar conversão da imagem para WebP.", error);
                }
            }

            //Verificar qual o ClientStorage será utilizado...
            var bestStorage = serverStorageService.getBestServerStorage();
            var idClientStorage = bestStorage.getIdClient();

            var fileStorageEntity = new FileStorage();
            fileStorageEntity.setApplication(application);
            fileStorageEntity.setIdFile(UUID.randomUUID().toString());
            fileStorageEntity.setIdClientStorage(idClientStorage);
            fileStorageEntity.setApplicationStorageFolder(application.getApplicationName());
            fileStorageEntity.setFileLogicName(file.getOriginalFilename());
            fileStorageEntity.setFileFisicalName(nomeFisicoArquivo);
            fileStorageEntity.setFileLength(lengthBytes);
            fileStorageEntity.setFileContent(bytesFile);
            fileStorageEntity.setCompressedFileLength(lengthBytesCompressed);
            fileStorageEntity.setCompressedFileContent(fileCompressedContent);
            fileStorageEntity.setFileCompressionInformation(fileCompressionInformation);
            fileStorageEntity.setFileContentType(contentTypeFile);
            fileStorageEntity.setHashFileBytes(hashFileBytes);
            fileStorageEntity.setExtractionTextByOrcFormFile(hasExtractionTextByOrcFormFile);
            if (hasExtractionTextByOrcFormFile)
                fileStorageEntity.setExtractionTextByOrcFormFileStatus(ExtractionTextByOcrStatusEnum.IN_PROCESSING.getCode());
            fileStorageEntity.setDateTimeRegisteredFileStorage(LocalDateTime.now());
            fileStorageEntity.setFileStatusCode(FileStorageStatusEnum.SENT_TO_STORAGE.getCode());

            //Enviar para armazenar fisicamente...
            var fileRegisterMessage = new FileRegisterMessage();
            fileRegisterMessage.setIdFile(fileStorageEntity.getIdFile());
            fileRegisterMessage.setFileName(fileStorageEntity.getFileFisicalName());
            fileRegisterMessage.setApplicationStorageFolder(fileStorageEntity.getApplicationStorageFolder());
            fileRegisterMessage.setDataBase64(FileBase64Util.fileToBase64(bytesFile));

            FileStorageClientStatus fileStorageClientStatus =
                    webSocketMessaging.startFileRegisterClient(idClientStorage, fileRegisterMessage);
            if (fileStorageClientStatus.isError())
                throw new ApiBusinessException(fileStorageClientStatus.getMessageError());

            fileStorageEntity.setFileStatusCode(fileStorageClientStatus.getFileStatusCode());

            //Gravar status...
            fileStorageRepository.save(fileStorageEntity);

            //Registrar mais um arquivo requistrado no Server Storage e Aplicação...
            if (fileStorageClientStatus.getFileStatusCode() == FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode()) {

                //Atualizar totalizador...
                serverStorageService.updateServerStorageTotalFile(bestStorage.getId(), true);
                applicationService.updateApplicationTotalFile(application.getId(), true);

                if (hasExtractionTextByOrcFormFile)
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

            FileStorageClientStatus fileStorageClientStatus =
                    webSocketMessaging.startFileDeleteClient(fileStorage.getIdClientStorage(), fileDeleteMessage);
            if (fileStorageClientStatus.isError())
                throw new ApiBusinessException(fileStorageClientStatus.getMessageError());

            fileStorage.setDateTimeRemovedFileStorage(LocalDateTime.now());
            fileStorage.setFileStatusCode(fileStorageClientStatus.getFileStatusCode());

            //Gravar exclusão logica...
            fileStorageRepository.save(fileStorage);

            //Atualiza a quantidade de arquivo no Server Storage e Aplicação...
            if (fileStorageClientStatus.getFileStatusCode() == FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode()) {
                var serverStorage = serverStorageService.findByIdClient(fileStorage.getIdClientStorage());
                if (serverStorage != null)
                    serverStorageService.updateServerStorageTotalFile(serverStorage.getId(), false);
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

        if (pageNumber == 0)
            pageNumber = 1;

        if (recordsPerPage == 0)
            recordsPerPage = 15;

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
            throw new ApiBusinessException("URL de acesso ao arquivo do repositório diretamente.");

        QrCodeFileResponse response = new QrCodeFileResponse();
        String tokenPublic = generateTokenAccess(file, tokenExpirationTime, maximumAccessestoken);
        String link = String.format(url_file_acess, idFile, tokenPublic);
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

        FileStorageAccessToken tokenAccess = new FileStorageAccessToken();
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
     * @param user - Informações do usuário (opcional)
     * @return Arquivo recuperado ou nulo se não existir.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage getFileByToken(String token, UserAccessModel user) throws ApiBusinessException {

        if (token == null ||  token.isEmpty())
            throw new ApiBusinessException("Token de acesso inválido ou não informado.");

        FileStorageAccessToken accessToken = fileStorageAccessTokenRepository.findByAccessToken(token).orElse(null);;
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

        if (user == null)
            user = new UserAccessModel();
        user.setUserName(accessToken.getAccessToken());

        return getFile(accessToken.getIdFile(), user);

    }
}