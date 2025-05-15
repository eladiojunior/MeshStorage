package br.com.devd2.meshstorageclient.services;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.*;
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
     */
    public void writeFileStorage(FileStorageClient fileStorageClient) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.mountPathStorage(fileStorageClient.getApplicationName(), fileStorageClient.getFileName())).toString();
            UtilClient.checkAndCreatePathStorage(pathFileStorage);
            byte[] fileBytes = FileBase64Util.base64ToBytes(fileStorageClient.getDataBase64());
            try (FileOutputStream fos = new FileOutputStream(pathFileStorage)) {
                fos.write(fileBytes);
            }
            System.out.println("==> File registred: " + pathFileStorage);
        } catch (Exception err) {
            throw new Exception("Erro ao gravar o arquivo em disco ["+pathFileStorage+"].", err);
        }
    }

    /**
     * Responsável por notitificar o ServerStorage que o status do arquivo processado.
     * @param fileStorageClientStatus - Informações do arquivo processado.
     */
    public void notifyServerStatusFileStorage(FileStorageClientStatus fileStorageClientStatus) {

        StorageClientEndpoint session = StorageConfig.get().getSession();
        if (session != null && session.isConnected()) {
            String jsonClient = JsonUtil.toJson(fileStorageClientStatus);
            session.sendMessage("/server/status-file-storage", jsonClient);
        }

    }

    /**
     * Realiza a remoção do arquivo fisicamente do Storage.
     * @param applicationName - Nome da aplicação para compor a estrutura de pasta de armazenamento.
     * @param fileName - Nome do arquivo com a estrutura de pasta no padrão.
     * @throws Exception - Erro no processo de remover o arquivo do storage.
     */
    public void removerFileStorage(String applicationName, String fileName) throws Exception {
        String pathFileStorage = "";
        try {
            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.mountPathStorage(applicationName, fileName)).toString();
            File file = new File(pathFileStorage);

            boolean result = false;
            if (file.exists()) {
                result = file.delete();
            }

            if (result)
                System.out.println("==> File deleted: " + fileName);
            else
                System.out.println("==> File does not exist: " + fileName);

        } catch (Exception err) {
            throw new Exception("Erro ao remover o arquivo em disco ["+pathFileStorage+"].", err);
        }
    }

    /**
     * Realiza o downlod de um arquivo fisicamente do Storage e envia para o Server.
     * @param idFile - Identificador do arquivo.
     * @param fileName - Nome do arquivo para recuperar no Storage.
     */
    public FileStorageClientDownload downloadFileStorage(String idFile, String applicationName, String fileName) {
        String pathFileStorage = "";

        FileStorageClientDownload fileStorageClientDownload = new FileStorageClientDownload();
        fileStorageClientDownload.setIdFile(idFile);
        fileStorageClientDownload.setFileName(fileName);
        fileStorageClientDownload.setApplicationName(applicationName);

        try {

            pathFileStorage = Paths.get(StorageConfig.get().getClient().getStoragePath(),
                    UtilClient.mountPathStorage(applicationName, fileName)).toString();
            File fileDownload = new File(pathFileStorage);
            if (!fileDownload.exists()) {
                fileStorageClientDownload.setError(true);
                fileStorageClientDownload.setMessageError("Arquivo ["+fileName+"] não encontrado.");
            }
            else
            {//Recuperar o arquivo do disco e enviar em base64 para o servidor.
                System.out.println("==> File download: " + fileName);
                fileStorageClientDownload.setDataBase64(FileBase64Util.fileToBase64(fileDownload.getAbsolutePath()));
                fileStorageClientDownload.setError(false);
                fileStorageClientDownload.setMessageError(null);
            }
        } catch (Exception err) {
            fileStorageClientDownload.setError(true);
            fileStorageClientDownload.setMessageError(err.getMessage());
        }

        return fileStorageClientDownload;

    }

    /**
     * Responsável por notitificar o ServerStorage que o download do arquivo.
     * @param fileStorageClientDownload - Informações do arquivo processado.
     */
    public void notifyServerDownloadFileStorage(FileStorageClientDownload fileStorageClientDownload) {
        StorageClientEndpoint session = StorageConfig.get().getSession();
        if (session != null && session.isConnected()) {

            if (fileStorageClientDownload.isError()) {
                fileStorageClientDownload.setDataBase64(""); //Limpar dados arquivo...
                String jsonClientDownload = JsonUtil.toJson(fileStorageClientDownload);
                session.sendMessage("/server/download-file-storage", jsonClientDownload);
                return;
            }

            //Enviar a mensagem de forma fragmentada...
            String dataBase64 = fileStorageClientDownload.getDataBase64();
            int CHUNK = 15 * 1024;
            int part = 0;

            for (int off = 0; off < dataBase64.length(); off += CHUNK) {

                boolean last = off + CHUNK >= dataBase64.length();
                FileStoragePartClientDownload cloneFileStorageClientDownload = new FileStoragePartClientDownload();
                cloneFileStorageClientDownload.setIdFile(fileStorageClientDownload.getIdFile());
                cloneFileStorageClientDownload.setFileName(fileStorageClientDownload.getFileName());
                cloneFileStorageClientDownload.setApplicationName(fileStorageClientDownload.getApplicationName());
                String part_dataBase64 = dataBase64.substring(off, Math.min(dataBase64.length(), off + CHUNK));
                cloneFileStorageClientDownload.setDataBase64(part_dataBase64);
                cloneFileStorageClientDownload.setPartFile(part+=1);
                cloneFileStorageClientDownload.setLastPartFile(last);

                String jsonClientDownload = JsonUtil.toJson(cloneFileStorageClientDownload);
                session.sendMessage("/server/download-part-file-storage", jsonClientDownload);

            }

        }

    }
}