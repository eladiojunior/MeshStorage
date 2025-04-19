package br.com.devd2.meshstorageclient.config;

import br.com.devd2.meshstorageclient.components.ClientCommandPrivateHandler;
import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class StorageConfig {
    private static StorageClient client;

    @Getter
    private StompSession session;
    private boolean isConnectedServer;

    public boolean isExistendClient() {
        if (client == null)
            client = carregarStorageServer();
        return (client != null);
    }

    public StorageClient getClient() {
        if (client == null) {
            client = new StorageClient();
        }
        return client;
    }

    private void openConnectServerWs() {

        isConnectedServer = false;

        try {

            // Criar WebSocket Client
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new StringMessageConverter());

            var storagePath = getClient().getStoragePath();
            getClient().setTotalSpaceMB(UtilClient.getTotalSpaceStorage(storagePath));
            getClient().setFreeSpaceMB(UtilClient.getFreeSpaceStorage(storagePath));

            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("id-client", getClient().getIdClient());
            connectHeaders.add("server-name", getClient().getServerName());
            connectHeaders.add("ip-server", getClient().getIpServer());
            connectHeaders.add("storage-name", getClient().getStorageName());
            connectHeaders.add("storage-total-space", String.valueOf(getClient().getTotalSpaceMB()));
            connectHeaders.add("storage-free-space", String.valueOf(getClient().getFreeSpaceMB()));

            // Conectar ao servidor WebSocket de forma assíncrona
            CompletableFuture<StompSession> futureSession =
                    stompClient.connectAsync(getClient().getUrlWebsocketServer(), new WebSocketHttpHeaders(),
                            connectHeaders, new StompSessionHandlerAdapter() {});

            // Aguarda a conexão ser estabelecida antes de continuar
            this.session = futureSession.get();
            isConnectedServer = true;

            // Inscrever-se para receber comandos de armazenamento
            this.session.subscribe("/user/client/private", new ClientCommandPrivateHandler());

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Erro: " + e.getMessage());
        }

    }

    public boolean notConnectServer() {
        if (!isConnectedServer || (session == null || !session.isConnected()))
            openConnectServerWs();
        return !isConnectedServer;
    }

    /**
     * Recupera as informações do Storage já registrado.
     * @return
     */
    private StorageClient carregarStorageServer() {
        StorageClient storageClient = null;
        try {
            File fileStorage = new File("storage.json");
            if (!fileStorage.exists())
                return storageClient;
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(fileStorage));
            storageClient = gson.fromJson(reader, StorageClient.class);
            reader.close();
        } catch (Exception erro) {
            erro.printStackTrace();
        }
        return storageClient;
    }

    /**
     * Gravar as informações do Storage
     */
    public void gravarStorageServer() {
        try {
            File fileStorage = new File("storage.json");
            Gson gson = new Gson();
            JsonWriter writer = new JsonWriter(new FileWriter(fileStorage));
            gson.toJson(client, StorageClient.class, writer);
            writer.close();
        } catch (Exception erro) {
            erro.printStackTrace();
        }
    }

}