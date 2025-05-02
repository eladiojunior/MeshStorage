package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorage.enums.FileStorageStatusEnum;
import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.FileUtil;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.helper.OcrUtil;
import br.com.devd2.meshstorage.models.messages.FileDeleteMessage;
import br.com.devd2.meshstorage.models.messages.FileRegisterMessage;
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import br.com.devd2.meshstorageserver.repositories.ApplicationRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final ApplicationRepository applicationRepository;
    private final FileStorageRepository fileStorageRepository;
    private final ServerStorageService serverStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    public FileStorageService(ApplicationRepository applicationRepository, FileStorageRepository fileStorageRepository,
                              SimpMessagingTemplate messagingTemplate,
                              ServerStorageService serverStorageService) {
        this.applicationRepository = applicationRepository;
        this.fileStorageRepository = fileStorageRepository;
        this.messagingTemplate = messagingTemplate;
        this.serverStorageService = serverStorageService;
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

        return null;

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

            var fileStorageEntity = new FileStorage();
            fileStorageEntity.setApplication(application);
            fileStorageEntity.setIdFile(UUID.randomUUID().toString());
            fileStorageEntity.setFileLogicName(file.getOriginalFilename());
            fileStorageEntity.setFileFisicalName(FileUtil.generatePisicalName(Objects.requireNonNull(file.getOriginalFilename())));
            fileStorageEntity.setFileLength(bytesFile.length);
            fileStorageEntity.setFileContent(bytesFile);
            fileStorageEntity.setFileType(file.getContentType());
            fileStorageEntity.setTextOcrFileContent(textOcrFileContent);
            fileStorageEntity.setHashFileContent(hashFileContent);
            fileStorageEntity.setDateTimeFileStorage(LocalDateTime.now());
            fileStorageEntity.setFileStatusCode(FileStorageStatusEnum.SENT_TO_STORAGE.getCode());

            //Gravar
            fileStorageRepository.save(fileStorageEntity);

            //Enviar para armazenar fisicamente...
            var fileStorage = new FileRegisterMessage();
            fileStorage.setIdFile(fileStorageEntity.getIdFile());
            fileStorage.setFileName(fileStorageEntity.getFileFisicalName());
            fileStorage.setDataBase64(FileBase64Util.fileToBase64(bytesFile));

            String jsonFileStorage = JsonUtil.toJson(fileStorage);

            var idClient = bestStorage.getIdClient();
            var sessionClient = HelperSessionClients.get().getSessionClient(idClient);
            if (sessionClient == null || sessionClient.isEmpty())
                throw new ApiBusinessException("Não foi possível identificar uma sessão de Storage para enviar o arquivo.");

            messagingTemplate.convertAndSend("/client/private", jsonFileStorage);

            return fileStorageEntity;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Solicita a remoção de um arquivo do Server Storage.
     * @param idFile - Identificador do arquivo para remover.
     */
    public void deleteFile(String idFile) throws ApiBusinessException {

        if (idFile == null || idFile.isEmpty())
            throw new ApiBusinessException("Identificador do arquivo não pode ser nulo ou vazio.");

        var file = fileStorageRepository.findByIdFile(idFile).orElse(null);
        if (file == null)
            throw new ApiBusinessException("Arquivo não identificado pelo seu ID ("+idFile+"), obrigatório.");

        //Enviar comando de DELETE para o Storage...
        var fileStorage = new FileDeleteMessage();
        fileStorage.setIdFile(file.getIdFile());
        fileStorage.setFileName(file.getFileFisicalName());

        String jsonFileStorage = JsonUtil.toJson(fileStorage);
        var idClient = file.getIdClientStorage();
        var sessionClient = HelperSessionClients.get().getSessionClient(idClient);
        if (sessionClient == null || sessionClient.isEmpty())
            throw new ApiBusinessException("Não foi possível identificar uma sessão de Storage para remover o arquivo.");

        messagingTemplate.convertAndSend("/client/private", jsonFileStorage);

    }
}