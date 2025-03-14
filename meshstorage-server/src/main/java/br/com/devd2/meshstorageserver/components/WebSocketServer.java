package br.com.devd2.meshstorageserver.components;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketServer extends TextWebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String clientId = session.getId();
        clients.put(clientId, session);
        System.out.println("Novo File Server conectado: " + clientId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Mensagem recebida de " + session.getId() + ": " + message.getPayload());

        // Processa a mensagem do file server (ex: espaço disponível)
        String[] data = message.getPayload().split("|");
        String serverName = data[0];
        String storageName = data[1];
        long freeSpace = Long.parseLong(data[2]);

        System.out.println("File Server " + serverName + "/" + storageName + " tem " + freeSpace + "MB livres.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clients.remove(session.getId());
        System.out.println("File Server desconectado: " + session.getId());
    }

    public void sendToClient(String clientId, String message) throws Exception {
        WebSocketSession session = clients.get(clientId);
        if (session != null) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
