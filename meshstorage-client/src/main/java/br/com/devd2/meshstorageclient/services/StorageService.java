package br.com.devd2.meshstorageclient.services;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Paths;

@Service
public class StorageService {

    @Autowired
    private StorageConfig storageConfig;

    /**
     * Responsável por gravar o arquivo no local de armazenamento.
     * @param fileStorageClient - Informações do arquivo.
     * @return
     */
    public String writeFileStorage(FileStorageClient fileStorageClient) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(storageConfig.getClient().getStoragePath(),
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
     * Responsável por noitificar o ServerStorage que o status do arquivo armazenado.
     * @param fileStorageClient - Informações do arquivo armazenado.
     */
    public void statusServerStorage(FileStorageClient fileStorageClient) {

        var session = storageConfig.getSession();
        if (session != null && session.isConnected()) {
            var jsonClient = JsonUtil.toJson(fileStorageClient);
            session.send("/server/status-upload-file", jsonClient);
        }

    }
}