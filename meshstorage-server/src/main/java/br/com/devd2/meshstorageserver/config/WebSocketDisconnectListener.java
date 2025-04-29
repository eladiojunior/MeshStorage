package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketDisconnectListener.class);

    private final ServerStorageService serverStorageService;

    public WebSocketDisconnectListener(ServerStorageService serverStorageService) {
        this.serverStorageService = serverStorageService;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {

        var sessionId = event.getSessionId();
        var idClient = HelperSessionClients.get().getIdClient(sessionId);
        if (idClient == null)
            return;

        try {

            //Verificar se o Client existe no banco...
            var storage = serverStorageService.findByIdClient(idClient);
            if (storage != null)
            {//Existe atualizar para Desativado...
                serverStorageService.updateServerStorageStatus(storage.getServerName(), storage.getStorageName(), storage.getFreeSpace(), false);
            }

            HelperSessionClients.get().removeSessionToClient(sessionId);
            logger.info("Cliente DESCONECTADO: SessionId={} => IdClient={}", sessionId, idClient);

        } catch (Exception erro) {
            logger.error("Erro ao desativar Storage.", erro);
        }

    }

}