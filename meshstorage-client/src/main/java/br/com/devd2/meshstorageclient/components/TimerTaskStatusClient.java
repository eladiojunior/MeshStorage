package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;

public class TimerTaskStatusClient extends TimerTask {
    private static final Logger logger = LogManager.getLogger(TimerTaskStatusClient.class);

    @Override
    public void run() {

        try {

            StorageConfig storageConfig = StorageConfig.get();
            if (storageConfig.notConnectServer())
                return; //Não connectado...

            String storagePath = storageConfig.getClient().getStoragePath();
            storageConfig.getClient().setTotalSpaceMB(UtilClient.getTotalSpaceStorage(storagePath));
            storageConfig.getClient().setFreeSpaceMB(UtilClient.getFreeSpaceStorage(storagePath));

            StorageClientEndpoint session = storageConfig.getSession();
            if (session != null && session.isConnected()) {
                String jsonClient = JsonUtil.toJson(storageConfig.getClient());
                session.sendMessage("/server/status-update-client", jsonClient);
            }

        } catch (Exception error) {
            logger.error("Error ao enviar status para o servidor.", error);
        }
    }

}
