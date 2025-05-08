package br.com.devd2.meshstorageserver.services;

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
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.response.ListFileStorageResponse;
import br.com.devd2.meshstorageserver.repositories.ApplicationRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final ApplicationRepository applicationRepository;
    private final FileStorageRepository fileStorageRepository;
    private final ServerStorageService serverStorageService;
    private final WebSocketMessaging webSocketMessaging;

    public FileStorageService(ApplicationRepository applicationRepository, FileStorageRepository fileStorageRepository,
                              ServerStorageService serverStorageService, WebSocketMessaging webSocketMessaging) {
        this.applicationRepository = applicationRepository;
        this.fileStorageRepository = fileStorageRepository;
        this.serverStorageService = serverStorageService;
        this.webSocketMessaging = webSocketMessaging;
    }

    /**
     * Recupera um arquivo pelo seu Identificador externo.
     * @param idFile - Identificador para recuperação do arquivo no Storage.
     * @return Arquivo recuperado ou nulo se não existir.
     * @throws ApiBusinessException - Erro de negócio.
     */
    public FileStorage getFile(String idFile)throws ApiBusinessException {

        if (idFile == null || idFile.isEmpty())
            throw new ApiBusinessException("Id File (chave do arquivo) não pode ser nulo ou vazio.");

        var fileStorage = fileStorageRepository.findByIdFile(idFile).orElse(null);
        if (fileStorage == null)
            throw new ApiBusinessException("Arquivo não identificado pelo seu ID ("+idFile+"), obrigatório.");

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
            if (fileStorage.isCompressFileContent()) {
                bytesFile = FileUtil.descompressZipFileContent(bytesFile);
            }
            fileStorage.setFileContent(bytesFile);

            return fileStorage;

        } catch (ApiBusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new ApiBusinessException(error.getMessage());
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

        var application = applicationRepository.findByApplicationName(applicationName).orElse(null);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada pelo seu nome ("+applicationName+"), obrigatório.");

        if (file == null || file.isEmpty())
            throw new ApiBusinessException("Arquivo físico não pode ser nulo ou vazio.");

        if (!FileUtil.hasTypeFileValid(application.getAllowedFileTypes().split(";"), file.getContentType()))
            throw new ApiBusinessException("Arquivo com tipo ["+file.getContentType()+"] diferente do permitido para aplicação (Tipos="+application.getAllowedFileTypes()+").");

        try {

            var hashFileContent = "";
            var bytesFile = file.getBytes();
            var sizeFileMB = FileUtil.sizeInMB(bytesFile.length);
            if (sizeFileMB > application.getMaximumFileSizeMB())
                throw new ApiBusinessException("Arquivo com tamnho de ["+sizeFileMB+"MB], maior que o permitido para aplicação (Max="+application.getMaximumFileSizeMB()+"MB).");

            //Verificar qual o ClientStorage será utilizado...
            var bestStorage = serverStorageService.getBestServerStorage();

            var textOcrFileContent = "";
            if (application.isApplyOcrFileContent() && OcrUtil.isAllowedTypeForOcr(file.getContentType())) {
                textOcrFileContent = OcrUtil.extractTextFormFile(file.getInputStream());
                hashFileContent = FileUtil.hashContent(textOcrFileContent);
            }
            if (hashFileContent == null || hashFileContent.isEmpty())
            {//Aplicar HASH nos bytes do arquivo, não no conteúdo.
                hashFileContent = FileUtil.hashConteudo(bytesFile);
            }

            if (application.isAllowDuplicateFile())
            {//Verificar duplicicade de HASH
                var fileStorage = fileStorageRepository.findByApplicationIdAndHashFileContent(
                        application.getId(), hashFileContent).orElse(null);
                if (fileStorage != null)
                    throw new ApiBusinessException("Identificado que esse arquivo já existe na aplicação, não é permitido.");
            }

            boolean fileCompressZip = false;
            String nomeFisicoArquivo = FileUtil.generatePisicalName(Objects.requireNonNull(file.getOriginalFilename()));
            if (application.isCompressFileContent() && !FileUtil.hasFileNameCompress(nomeFisicoArquivo))
            {
                bytesFile = FileUtil.compressZipFileContent(nomeFisicoArquivo, bytesFile);
                nomeFisicoArquivo = FileUtil.changeFileNameExtension(nomeFisicoArquivo, ".zip");
                fileCompressZip = true;
            }

            var idClientStorage = bestStorage.getIdClient();

            var fileStorageEntity = new FileStorage();
            fileStorageEntity.setApplication(application);
            fileStorageEntity.setIdFile(UUID.randomUUID().toString());
            fileStorageEntity.setIdClientStorage(idClientStorage);
            fileStorageEntity.setApplicationStorageFolder(application.getApplicationName());
            fileStorageEntity.setFileLogicName(file.getOriginalFilename());
            fileStorageEntity.setFileFisicalName(nomeFisicoArquivo);
            fileStorageEntity.setFileLength(bytesFile.length);
            fileStorageEntity.setFileContent(bytesFile);
            fileStorageEntity.setCompressFileContent(fileCompressZip);
            fileStorageEntity.setFileType(file.getContentType());
            fileStorageEntity.setTextOcrFileContent(textOcrFileContent);
            fileStorageEntity.setHashFileContent(hashFileContent);
            fileStorageEntity.setDateTimeFileStorage(LocalDateTime.now());
            fileStorageEntity.setFileStatusCode(FileStorageStatusEnum.SENT_TO_STORAGE.getCode());

            //Gravar
            fileStorageRepository.save(fileStorageEntity);

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

            return fileStorageEntity;

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

            fileStorage.setFileStatusCode(fileStorageClientStatus.getFileStatusCode());

            //Gravar exclusão logica...
            fileStorageRepository.save(fileStorage);

            return fileStorage;

        } catch (Exception error) {
            throw new ApiBusinessException(error.getMessage());
        }

    }

    /**
     * Recupera a lista de arquivos de uma aplicação (nome) de forma painada.
     * @param applicationName - Nome da aplicação para recuperação dos arquivos.
     * @param pageNumber - Número da página da paginação
     * @param recordsPerPage - Número de registros por página.
     * @return Instancia com a lista de arquivos da aplicação.
     */
    public ListFileStorageResponse listFilesByApplicationName(String applicationName, int pageNumber, int recordsPerPage) throws ApiBusinessException {

        if (applicationName == null || applicationName.isEmpty())
            throw new ApiBusinessException("Nome da aplicação não pode ser nulo ou vazio.");

        var application = applicationRepository.findByApplicationName(applicationName).orElse(null);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada pelo seu nome ("+applicationName+"), obrigatório.");

        if (pageNumber == 0)
            pageNumber = 1;

        if (recordsPerPage == 0)
            recordsPerPage = 15;

        ListFileStorageResponse result = new ListFileStorageResponse();
        var totalRecords = fileStorageRepository.countByApplicationId(application.getId()).orElse(0L);
        result.setTotalRecords(totalRecords);
        Pageable pageable = PageRequest.of(pageNumber-1, recordsPerPage);
        var listFileStorage = fileStorageRepository.findByApplicationId(application.getId(), pageable);
        var listFileResponse = HelperMapper.ConvertToResponseListFileStorage(listFileStorage.stream().toList());
        result.setFiles(listFileResponse);
        return result;

    }
}