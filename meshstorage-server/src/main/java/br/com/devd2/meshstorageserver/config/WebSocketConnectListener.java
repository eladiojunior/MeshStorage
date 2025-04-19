package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import br.com.devd2.meshstorageserver.models.request.ServerStorageRequest;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ServerStorageService serverStorageService;

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {

        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        StompHeaderAccessor connectHeaders = StompHeaderAccessor.wrap(sha.getMessageHeaders().get("simpConnectMessage", Message.class));

        String idClient = connectHeaders.getFirstNativeHeader("id-client");
        var storage = serverStorageService.findByIdClient(idClient);
        if (storage != null && storage.isAvailable()) {
            logger.info("Cliente já conectado: IdClient={} e ATIVO.", idClient);
            return;
        }

        String serverName = connectHeaders.getFirstNativeHeader("server-name");
        String storageName = connectHeaders.getFirstNativeHeader("storage-name");
        String stringFreeSpace = connectHeaders.getFirstNativeHeader("storage-free-space");
        String stringTotalSpace = connectHeaders.getFirstNativeHeader("storage-total-space");
        var freeSpace =  stringFreeSpace == null ? 0 : Long.parseLong(stringFreeSpace);
        var totalSpace =  stringTotalSpace == null ? 0 : Long.parseLong(stringTotalSpace);

        /*
        if (HelperSessionClients.get().hasSessionToIdClient(idClient)) {
            logger.info("Cliente já conectado: IdClient={} e ATIVO.", idClient);
            return;
        }
        HelperSessionClients.get().addSessionToClient(sessionId, idClient);
        */

        logger.info("Cliente CONECTADO: IdClient={}, ServerName={}, StorageName={}", idClient, serverName, storageName);
        if (storage == null)
        {//Registrar um storage inicial...
            var request = new ServerStorageRequest();
            request.setIdClient(idClient);
            request.setServeName(serverName);
            request.setStorageName(storageName);
            request.setFreeSpace(freeSpace);
            request.setTotalSpace(totalSpace);
            serverStorageService.registerServerStorage(request);
        } else {
            serverStorageService.updateServerStorageStatus(serverName, storageName, freeSpace, true);
        }

    }


}
