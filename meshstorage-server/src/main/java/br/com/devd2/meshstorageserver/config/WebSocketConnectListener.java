package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import br.com.devd2.meshstorageserver.models.ServerStorageModel;
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

        try {

            String idClient = connectHeaders.getFirstNativeHeader("id-client");

            //Verificar se existe um Session para o Client...
            if (!HelperSessionClients.get().hasSessionToIdClient(idClient)) {
                var sessionId = connectHeaders.getSessionId();
                HelperSessionClients.get().addSessionToClient(sessionId, idClient);
            }

            //Verificar se o Client existe no banco...
            var storage = serverStorageService.findByIdClient(idClient);
            if (storage != null) {


                if (storage.isAvailable())
                {//Existe e está ativo!
                    logger.info("Cliente já conectado: IdClient={} e ATIVO.", idClient);
                    return;
                }

                //Existe mas está inativo por algum motivo. Reativar!!!
                String stringFreeSpace = connectHeaders.getFirstNativeHeader("storage-free-space");
                var freeSpace = stringFreeSpace == null ? 0 : Long.parseLong(stringFreeSpace);
                serverStorageService.updateServerStorageStatus(storage.getServerName(), storage.getStorageName(), freeSpace, true);
                logger.info("Cliente CONECTADO: IdClient={} e REATIVO.", idClient);
                return;

            }

            //Não existe, novo client... registrar!
            String serverName = connectHeaders.getFirstNativeHeader("server-name");
            String ipServer = connectHeaders.getFirstNativeHeader("ip-server");
            String storageName = connectHeaders.getFirstNativeHeader("storage-name");
            String stringTotalSpace = connectHeaders.getFirstNativeHeader("storage-total-space");
            var totalSpace =  stringTotalSpace == null ? 0 : Long.parseLong(stringTotalSpace);
            String stringFreeSpace = connectHeaders.getFirstNativeHeader("storage-free-space");
            var freeSpace = stringFreeSpace == null ? 0 : Long.parseLong(stringFreeSpace);

            var model = new ServerStorageModel();
            model.setIdClient(idClient);
            model.setServeName(serverName);
            model.setIpServer(ipServer);
            model.setStorageName(storageName);
            model.setTotalSpace(totalSpace);
            model.setFreeSpace(freeSpace);

            serverStorageService.registerServerStorage(model);

            logger.info("Cliente CONECTADO: IdClient={}, ServerName={}, StorageName={}", idClient, serverName, storageName);

        } catch (Exception erro) {
            logger.error("Erro ao registrar ou atualizar o Storage.", erro);
        }

    }

}