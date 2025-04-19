package br.com.devd2.meshstorageclient.services;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StorageService {

    @Autowired
    private StorageConfig storageConfig;

    private final List<StorageClient> storages = new CopyOnWriteArrayList<>();

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
     * Responsável por notitificar o ServerStorage que o status do arquivo armazenado.
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