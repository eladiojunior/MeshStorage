package br.com.devd2.meshstorageclient.services;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorage.models.FileStorageClientStatus;
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
    private static StorageService instance;

    private StorageService() {
    }

    public static StorageService get() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    /**
     * Responsável por gravar o arquivo no local de armazenamento.
     * @param fileStorageClient - Informações do arquivo.
     * @return Caminho do arquivo registrado no storage.
     */
    public String writeFileStorage(FileStorageClient fileStorageClient) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.mountPathStorage(fileStorageClient.getFileName())).toString();
            UtilClient.checkAndCreatePathStorage(pathFileStorage);
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
     * Responsável por notitificar o ServerStorage que o status do arquivo processado.
     * @param fileStorageClientStatus - Informações do arquivo processado.
     */
    public void statusFileStorage(FileStorageClientStatus fileStorageClientStatus) {

        StorageClientEndpoint session = StorageConfig.get().getSession();
        if (session != null && session.isConnected()) {
            String jsonClient = JsonUtil.toJson(fileStorageClientStatus);
            session.sendMessage("/server/status-file-storage", jsonClient);
        }

    }

    /**
     * Realiza a remoção do arquivo fisicamente do Storage.
     * @param fileName - Nome do arquivo com a estrutura de pasta no padrão.
     * @return true = arquivo removido, false = arquivo não encontrado.
     * @throws Exception - Erro no processo de remover o arquivo do storage.
     */
    public boolean removerFileStorage(String fileName) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.mountPathStorage(fileName)).toString();
            File file = new File(pathFileStorage);
            if (!file.exists())
                return false;
            return file.delete();
        } catch (Exception err) {
            throw new Exception("Erro ao remover o arquivo em disco ["+pathFileStorage+"].", err);
        }
    }

}