package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorage.enums.FileStorageStatusEnum;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorage.models.FileStorageClientStatus;
import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorage.models.messages.FileDeleteMessage;
import br.com.devd2.meshstorage.models.messages.FileDownloadMessage;
import br.com.devd2.meshstorage.models.messages.FileRegisterMessage;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import br.com.devd2.meshstorageclient.services.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.Getter;

@ClientEndpoint
public class StorageClientEndpoint {

    @Getter
    private Session session;

    @OnOpen
    public void onOpen(Session session) {

        this.session = session;

        // Conectar ao broker STOMP após WebSocket aberto
        sendStompConnect();

        // Se inscrever no canal desejado após conexão
        String idClientStorage = StorageConfig.get().getClient().getIdClient();
        subscribeChannel("/client/"+idClientStorage);

        System.out.println("==> Conexão aberta com o Servidor.");

    }

    public static String extractJsonPayload(String stompMessage) {
        String[] parts = stompMessage.split("\\r?\\n\\r?\\n", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Mensagem STOMP inválida, corpo não encontrado.");
        }
        return parts[1].trim(); // O JSON está após a quebra de linha dupla
    }

    @OnMessage
    public void onMessage(String message) {
        try {

            message = extractJsonPayload(message);
            if (message.isEmpty())
                return;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message);
            String type = node.get("type").asText();

            switch (type) {
                case "FILE_REGISTER":
                    registreFileStorage(mapper, message);
                    break;
                case "FILE_DELETE":
                    deleteFileStorage(mapper, message);
                    break;
                case "FILE_DOWNLOAD":
                    downloadFileStorage(mapper, message);
                    break;
                default:
                    System.err.println("!Tipo de mensagem desconhecido: " + type);
            }
        } catch (Exception error) {
            System.err.println("!Erro ao processar mensagem => " + error.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("==> Conexão fechada com o Servidor.");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("==> Erro na conexão: " + throwable.getMessage());
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public void sendMessage(String brocke, String messageJson) {
        String message ="SEND\n" +
                "destination:"+brocke+"\n" +
                "content-type:application/json\n\n" +
                messageJson + "\u0000";
        session.getAsyncRemote().sendText(message);
    }

    public boolean isConnected() {
        return (session!=null && session.isOpen());
    }

    private void sendStompConnect() {

        StorageClient client = StorageConfig.get().getClient();
        String storagePath = client.getStoragePath();
        client.setTotalSpaceMB(UtilClient.getTotalSpaceStorage(storagePath));
        client.setFreeSpaceMB(UtilClient.getFreeSpaceStorage(storagePath));

        String connectFrame = "CONNECT\n" +
                "accept-version:1.2\n" +
                "server-name:"+client.getServerName()+"\n" +
                "ip-server:"+client.getIpServer()+"\n" +
                "storage-name:"+client.getStorageName()+"\n" +
                "storage-total-space:"+client.getTotalSpaceMB()+"\n" +
                "storage-free-space:"+client.getFreeSpaceMB()+"\n" +
                "id-client:"+client.getIdClient()+"\n\n" +
                "\u0000";

        sendMessage(connectFrame);

    }

    public void subscribeChannel(String destination) {
        StorageClient client = StorageConfig.get().getClient();
        String subscribeFrame = "SUBSCRIBE\nid:"+client.getIdClient()+"\ndestination:" + destination + "\n\n\u0000";
        sendMessage(subscribeFrame);
    }

    private void deleteFileStorage(ObjectMapper mapper, String message) throws Exception {

        FileDeleteMessage fileDeleteMessage = mapper.readValue(message, FileDeleteMessage.class);

        //Informar o status para o Server.
        FileStorageClientStatus fileStorageClientStatus = new FileStorageClientStatus();
        fileStorageClientStatus.setIdFile(fileDeleteMessage.getIdFile());

        boolean isError = false;
        String messageError = null;
        int fileStatusCode = FileStorageStatusEnum.DELETED_SUCCESSFULLY.getCode();
        try {
            StorageService.get().removerFileStorage(fileDeleteMessage.getApplicationStorageFolder(), fileDeleteMessage.getFileName());
        } catch (Exception error) {
            isError = true;
            messageError = error.getMessage();
            fileStatusCode = FileStorageStatusEnum.STORAGE_FAILED.getCode();
        }
        fileStorageClientStatus.setFileStatusCode(fileStatusCode);
        fileStorageClientStatus.setError(isError);
        fileStorageClientStatus.setMessageError(messageError);

        StorageService.get().notifyServerStatusFileStorage(fileStorageClientStatus);

    }

    private void registreFileStorage(ObjectMapper mapper, String message) throws Exception {

        FileRegisterMessage fileRegisterMessage = mapper.readValue(message, FileRegisterMessage.class);
        FileStorageClient fileStorageClient = new FileStorageClient();
        fileStorageClient.setIdFile(fileRegisterMessage.getIdFile());
        fileStorageClient.setApplicationName(fileRegisterMessage.getApplicationStorageFolder());
        fileStorageClient.setFileName(fileRegisterMessage.getFileName());
        fileStorageClient.setDataBase64(fileRegisterMessage.getDataBase64());

        //Informar o status para o Server.
        FileStorageClientStatus fileStorageClientStatus = new FileStorageClientStatus();
        fileStorageClientStatus.setIdFile(fileRegisterMessage.getIdFile());

        boolean isError = false;
        String messageError = null;
        int fileStatusCode = FileStorageStatusEnum.STORED_SUCCESSFULLY.getCode();
        try {
            StorageService.get().writeFileStorage(fileStorageClient);
        } catch (Exception error) {
            isError = true;
            messageError = error.getMessage();
            fileStatusCode = FileStorageStatusEnum.STORAGE_FAILED.getCode();
        }
        fileStorageClientStatus.setFileStatusCode(fileStatusCode);
        fileStorageClientStatus.setError(isError);
        fileStorageClientStatus.setMessageError(messageError);

        StorageService.get().notifyServerStatusFileStorage(fileStorageClientStatus);

    }

    private void downloadFileStorage(ObjectMapper mapper, String message) throws Exception {

        FileDownloadMessage fileDownloadMessage = mapper.readValue(message, FileDownloadMessage.class);
        StorageService.get().downloadFileStorage(fileDownloadMessage.getIdFile(),
                fileDownloadMessage.getApplicationStorageFolder(),
                fileDownloadMessage.getFileName());

    }

}