package br.com.devd2.meshstorageserver.helper;

import br.com.devd2.meshstorageserver.entites.Application;
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.models.response.ApplicationResponse;
import br.com.devd2.meshstorageserver.models.response.FileStorageResponse;
import br.com.devd2.meshstorageserver.models.response.ServerStorageResponse;

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
        response.setIdClient(serverStorage.getIdClient());
        response.setServerName(serverStorage.getServerName());
        response.setStorageName(serverStorage.getStorageName());
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
        response.setFileType(fileStorage.getFileType());
        response.setFileLength(fileStorage.getFileLength());
        response.setHashFileContent(fileStorage.getHashFileContent());
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
        response.setCompressFileContent(application.isCompressFileContent());
        response.setApplyOcrFileContent(application.isApplyOcrFileContent());
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
}