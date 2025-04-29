package br.com.devd2.meshstorageclient.services;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.components.StorageClientEndpoint;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StorageService {

    private final List<StorageClient> storages = new CopyOnWriteArrayList<>();

    /**
     * Responsável por gravar o arquivo no local de armazenamento.
     * @param fileStorageClient - Informações do arquivo.
     * @return
     */
    public String writeFileStorage(FileStorageClient fileStorageClient) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.montarPathStorage(fileStorageClient.getFileName())).toString();
            byte[] fileBytes = FileBase64Util.base64ToBytes(fileStorageClient.getDataBase64());
            try (FileOutputStream fos = new FileOutputStream(pathFileStorage)) {
                fos.write(fileBytes);
            }
            return pathFileStorage;
        } catch (Exception err) {
            throw new Exception("Erro ao gravar o arquivo em disco ["+pathFileStorage+"].", err);
        }
    }

    /**
     * Responsável por notitificar o ServerStorage que o status do arquivo armazenado.
     * @param fileStorageClient - Informações do arquivo armazenado.
     */
    public void statusServerStorage(FileStorageClient fileStorageClient) {

        StorageClientEndpoint session = StorageConfig.get().getSession();
        if (session != null && session.isConnected()) {
            String jsonClient = JsonUtil.toJson(fileStorageClient);
            session.sendMessage("/server/status-upload-file", jsonClient);
        }

    }

}