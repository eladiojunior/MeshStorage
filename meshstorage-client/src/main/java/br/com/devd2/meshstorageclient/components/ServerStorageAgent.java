package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerStorageAgent {
    private static final Logger logger = LoggerFactory.getLogger(ServerStorageAgent.class);
    private final StorageConfig storageConfig;

    public ServerStorageAgent(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Scheduled(fixedRate = 5000) // A cada 5 segundos
    public void sendStatus() {
        try {

            if (storageConfig.notConnectServer())
                return; //Não connectado...

            var storagePath = storageConfig.getClient().getStoragePath();
            storageConfig.getClient().setTotalSpaceMB(UtilClient.getTotalSpaceStorage(storagePath));
            storageConfig.getClient().setFreeSpaceMB(UtilClient.getFreeSpaceStorage(storagePath));

            var session = storageConfig.getSession();
            if (session != null && session.isConnected()) {
                var jsonClient = JsonUtil.toJson(storageConfig.getClient());
                session.send("/server/status-update-client", jsonClient);
            }

        } catch (Exception error) {
            logger.error("Error ao enviar status para o servidor.", error);
        }
    }

}