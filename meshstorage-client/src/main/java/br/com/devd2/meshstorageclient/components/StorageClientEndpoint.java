package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
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
        subscribeChannel("/client/private");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("==> Mensagem recebida do servidor: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("==> Conexão fechada: " + reason.getReasonPhrase());
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
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:"+client.getIdClient()+"\n" +
                "destination:" + destination + "\n\n" +
                "\u0000";
        sendMessage(subscribeFrame);
    }

}