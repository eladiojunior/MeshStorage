package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketDisconnectListener.class);

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {

        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        var sessionId = event.getSessionId();
        var idClient = HelperSessionClients.get().getIdClient(sessionId);
        if (idClient == null)
            return;
        HelperSessionClients.get().removeSessionToClient(sessionId);

        logger.info("Cliente DESCONECTADO: SessionId={} => IdClient={}", sessionId, idClient);

    }

}