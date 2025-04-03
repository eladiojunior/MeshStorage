package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketConnectListener implements
        ApplicationListener<SessionConnectedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectListener.class);

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {

        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        StompHeaderAccessor connectHeaders = StompHeaderAccessor.wrap(sha.getMessageHeaders().get("simpConnectMessage", Message.class));

        var sessionId = connectHeaders.getSessionId();

        String idClient = connectHeaders.getFirstNativeHeader("id-client");
        String serverName = connectHeaders.getFirstNativeHeader("server-name");
        String storageName = connectHeaders.getFirstNativeHeader("storage-name");

        if (HelperSessionClients.get().hasSessionToIdClient(idClient)) {
            logger.info("Cliente já conectado: IdClient={}", idClient);
            return;
        }
        HelperSessionClients.get().addSessionToClient(sessionId, idClient);

        logger.info("Cliente CONECTADO: SessionId={} => IdClient={}, ServerName={}, StorageName={}", sessionId, idClient, serverName, storageName);

    }

}
