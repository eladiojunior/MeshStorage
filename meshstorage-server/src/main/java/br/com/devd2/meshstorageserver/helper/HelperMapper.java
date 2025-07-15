package br.com.devd2.meshstorageserver.helper;

import br.com.devd2.meshstorageserver.entites.Application;
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.models.StatusMeshStorageModel;
import br.com.devd2.meshstorageserver.models.response.*;

import java.util.List;
import java.util.stream.Collectors;

public class HelperMapper {

    /**
     * Converte um objeto Entity (ServerStorage) para Response (ServerStorageResponse).
     *
     * @param serverStorage - Objeto a ser convertido em Response
     * @return Instancia de Respose
     */
    public static ServerStorageResponse ConvertToResponse(ServerStorage serverStorage) {
        if (serverStorage == null) {
            return null;
        }
        ServerStorageResponse response = new ServerStorageResponse();
        response.setId(serverStorage.getId());
        response.setIdClient(serverStorage.getIdClient());
        response.setServerName(serverStorage.getServerName());
        response.setStorageName(serverStorage.getStorageName());
        response.setIpServer(serverStorage.getIpServer());
        response.setOsServer(serverStorage.getOsServer());
        response.setFreeSpace(serverStorage.getFreeSpace());
        response.setTotalSpace(serverStorage.getTotalSpace());
        response.setAvailable(serverStorage.isAvailable());
        response.setDateTimeAvailable(serverStorage.getDateTimeAvailable());
        return response;
    }

    /**
     * Converte um objeto Entity (FileStorage) para Response (FileStorageResponse).
     *
     * @param fileStorage - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static FileStorageResponse ConvertToResponse(FileStorage fileStorage) {
        if (fileStorage == null) {
            return null;
        }
        FileStorageResponse response = new FileStorageResponse();
        response.setIdFile(fileStorage.getIdFile());
        response.setFileLogicName(fileStorage.getFileLogicName());
        response.setFileFisicalName(fileStorage.getFileFisicalName());
        response.setFileContentType(fileStorage.getFileContentType());
        response.setFileLength(fileStorage.getFileLength());
        response.setHashFileBytes(fileStorage.getHashFileBytes());
        response.setExtractionTextByOrcFormFile(fileStorage.isExtractionTextByOrcFormFile());
        response.setExtractionTextByOrcFormFileStatus(fileStorage.getExtractionTextByOrcFormFileStatus());
        response.setHashFileContentByOcr(fileStorage.getHashFileContentByOcr());
        response.setCompressedFileContent(fileStorage.isCompressedFileContent());
        response.setCompressedFileLength(fileStorage.getCompressedFileLength());
        response.setFileCompressionInformation(fileStorage.getFileCompressionInformation());
        response.setDateTimeRegisteredFileStorage(fileStorage.getDateTimeRegisteredFileStorage());
        response.setDateTimeRemovedFileStorage(fileStorage.getDateTimeRemovedFileStorage());
        response.setDateTimeBackupFileStorage(fileStorage.getDateTimeBackupFileStorage());
        response.setFileStatusCode(fileStorage.getFileStatusCode());
        return response;
    }

    /**
     * Converte lista de objeto Entity (ServerStorage) para lista de Response (ServerStorageResponse).
     * @param listServerStorage - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<ServerStorageResponse> ConvertToResponseListServerStorage(List<ServerStorage> listServerStorage) {
        return listServerStorage.stream().map(HelperMapper::ConvertToResponse).toList();
    }

    /**
     * Converte um objeto Entity (Application) para Response (ApplicationResponse).
     *
     * @param application - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static ApplicationResponse ConvertToResponse(Application application) {
        if (application == null)
            return null;
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setApplicationName(application.getApplicationName());
        response.setApplicationDescription(application.getApplicationDescription());
        response.setMaximumFileSize(application.getMaximumFileSizeMB());
        response.setAllowedFileTypes(application.getAllowedFileTypes().split(";"));
        response.setAllowDuplicateFile(application.isAllowDuplicateFile());
        response.setCompressedFileContentToZip(application.isCompressedFileContentToZip());
        response.setConvertImageFileToWebp(application.isConvertImageFileToWebp());
        response.setApplyOcrFileContent(application.isApplyOcrFileContent());
        response.setTotalFiles(application.getTotalFiles());
        response.setDateTimeApplication(application.getDateTimeRegisteredApplication());
        return response;
    }

    /**
     * Converte lista de objeto Entity (Application) para lista de Response (ApplicationResponse).
     * @param list - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<ApplicationResponse> ConvertToResponseListApplication(List<Application> list) {
        return list.stream().map(HelperMapper::ConvertToResponse).collect(Collectors.toList());
    }

    /**
     * Converte lista de objeto Entity (FileStorage) para lista de Response (FileStorageResponse).
     * @param list - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<FileStorageResponse> ConvertToResponseListFileStorage(List<FileStorage> list) {
        return list.stream().map(HelperMapper::ConvertToResponse).collect(Collectors.toList());
    }

    /**
     * Converte uma Model em Response (StatusMeshStorageResponse) para retornar na API;
     * @param model - Informações do status.
     * @return Objeto de Response carregad.
     */
    public static StatusMeshStorageResponse ConvertToResponseStatusMeshStorage(StatusMeshStorageModel model) {
        if (model == null)
            return null;
        StatusMeshStorageResponse response = new StatusMeshStorageResponse();
        response.setSystemHealth(model.getSystemHealth());
        response.setMessageStatus(model.getMessageStatus());
        response.setTotalSpaceStorages(model.getTotalSpaceStorages());
        response.setTotalFreeStorages(model.getTotalFreeStorages());
        response.setTotalClientsConnected(model.getTotalClientsConnected());
        response.setTotalFilesStorages(model.getTotalFilesStorages());
        response.setDateTimeAvailable(model.getDateTimeAvailable());
        return response;
    }
}