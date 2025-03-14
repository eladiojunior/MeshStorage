package br.com.devd2.meshstorageserver.helper;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.models.response.FileStorageResponse;
import br.com.devd2.meshstorageserver.models.response.ServerStorageResponse;

public class HelperMapper {

    /**
     * Converte um objeto Entity (ServerStorage) para Response (ServerStorageResponse).
     * @param serverStorage - Objeto a ser convertido em Response
     * @return
     */
    public static ServerStorageResponse ConvertToResponse(ServerStorage serverStorage) {
        if (serverStorage == null) {
            return null;
        }
        ServerStorageResponse response = new ServerStorageResponse();
        response.setId(serverStorage.getId());
        response.setServerName(serverStorage.getServerName());
        response.setStorageName(serverStorage.getStorageName());
        response.setFreeSpace(serverStorage.getFreeSpace());
        response.setTotalSpace(serverStorage.getTotalSpace());
        return response;
    }


    /**
     * Converte um objeto Entity (FileStorage) para Response (FileStorageResponse).
     * @param fileStorage - Objeto a ser convertido em Response
     * @return
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
        return response;
    }

}
