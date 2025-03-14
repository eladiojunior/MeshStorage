package br.com.devd2.meshstorageclient.components;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class ServerStorageAgent {

    private StompSession session;
    private final String serverName = "FileServer1";
    private final String storageName = "/mnt/storage";
    private final String urlServer = "ws://localhost:8080/server";

    public ServerStorageAgent() {

        try {

            // Criar WebSocket Client
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new StringMessageConverter());

            // Conectar ao servidor WebSocket de forma assíncrona
            CompletableFuture<StompSession> futureSession =
                    stompClient.connectAsync(urlServer, new StompSessionHandlerAdapter() {});

            // Aguarda a conexão ser estabelecida antes de continuar
            this.session = futureSession.get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Scheduled(fixedRate = 5000) // A cada 5 segundos
    public void sendStatus() {
        try {
            long freeSpace = new File(storageName).getFreeSpace() / (1024 * 1024); // MB
            if (session != null && session.isConnected()) {
                session.send("/status-update", serverName + "|" + storageName + "|" + freeSpace);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
