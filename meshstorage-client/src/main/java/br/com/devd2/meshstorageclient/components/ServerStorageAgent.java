package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.JsonUtil;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerStorageAgent {

    private final StorageConfig storageConfig;

    public ServerStorageAgent(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Scheduled(fixedRate = 5000) // A cada 5 segundos
    public void sendStatus() {
        try {

            if (!storageConfig.isConnectedServer())
                return; //Não connectado...

            var storageName = storageConfig.getClient().getStorageName();
            storageConfig.getClient().setTotalSpaceMB(UtilClient.getTotalSpaceStorage(storageName));
            storageConfig.getClient().setFreeSpaceMB(UtilClient.getFreeSpaceStorage(storageName));

            var session = storageConfig.getSession();
            if (session != null && session.isConnected()) {
                var jsonClient = JsonUtil.toJson(storageConfig.getClient());
                session.send("/app/status-update", jsonClient);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}