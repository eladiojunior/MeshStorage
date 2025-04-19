package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private ServerStorageService serverStorageService;

    @MessageMapping("/status-update-client")
    public void receiveStatusUpdate(String payload) {

        var storageClient = JsonUtil.fromJson(payload, StorageClient.class);
        if (storageClient == null)
            return;
        try {
            //Atualzar o status
            serverStorageService.updateServerStorageStatus(storageClient.getServerName(),
                    storageClient.getStorageName(), storageClient.getFreeSpaceMB(), true);
        } catch (Exception erro) {
            logger.error("Erro ao receber status do cliente.", erro);
        }

    }

    // Comando para um file server armazenar um arquivo
    @MessageMapping("/status-upload-file")
    public void receiverStatusUpdateFile(String fileInfo) {

        System.out.println("Status file: " + fileInfo);

    }
}
