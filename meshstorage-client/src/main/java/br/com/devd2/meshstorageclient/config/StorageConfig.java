package br.com.devd2.meshstorageclient.config;

import br.com.devd2.meshstorageclient.components.ClientsHandler;
import br.com.devd2.meshstorageclient.models.StorageClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Component
public class StorageConfig {
    private StorageClient client;

    private StompSession session;
    private boolean isConnectedServer;

    @Value("${url-websocket-server}")
    private String urlServer;

    private StorageConfig() {
        client = new StorageClient();
    }

    private void openConnectServerWs() {

        isConnectedServer = false;

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
            isConnectedServer = true;

            // Inscrever-se para receber comandos de armazenamento
            this.session.subscribe("/client/store-command", new ClientsHandler());

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Erro: " + e.getMessage());
        }

    }

    public boolean testConnectServer() {
        if (!isConnectedServer || (session == null || !session.isConnected()))
            openConnectServerWs();
        return isConnectedServer;
    }

}