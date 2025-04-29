package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorage.models.FileStorageClientStatus;
import br.com.devd2.meshstorage.models.StorageClientStatus;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final ServerStorageService serverStorageService;

    public WebSocketController(ServerStorageService serverStorageService) {
        this.serverStorageService = serverStorageService;
    }

    @MessageMapping("/status-update-client")
    public void receiveStatusUpdate(@Payload StorageClientStatus clientStatus) {
        if (clientStatus == null)
            return;
        try {
            //Atualzar o status
            serverStorageService.updateServerStorageStatus(clientStatus.getServerName(),
                    clientStatus.getStorageName(), clientStatus.getFreeSpaceMB(), true);
        } catch (Exception erro) {
            logger.error("Erro ao receber status do cliente.", erro);
        }
    }

    @MessageMapping("/status-upload-file")
    public void receiverStatusUpdateFile(@Payload FileStorageClientStatus clientStatus) {

        System.out.println("Status file: " + clientStatus);

    }

}
