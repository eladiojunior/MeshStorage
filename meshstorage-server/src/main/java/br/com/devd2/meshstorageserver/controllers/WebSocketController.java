package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorage.helper.FileBase64Util;
import br.com.devd2.meshstorage.models.*;
import br.com.devd2.meshstorageserver.config.WebSocketMessaging;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.io.Console;
import java.util.*;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final ServerStorageService serverStorageService;
    private final WebSocketMessaging webSocketMessaging;
    private final Map<String, List<PartFileStorageModel>> partFileStorage = new HashMap<>();

    public WebSocketController(ServerStorageService serverStorageService, WebSocketMessaging webSocketMessaging) {
        this.serverStorageService = serverStorageService;
        this.webSocketMessaging = webSocketMessaging;
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

    @MessageMapping("/status-file-storage")
    public void receiverStatusFileStorage(@Payload FileStorageClientStatus clientStatus) {
        webSocketMessaging.notifyFileStatusClient(clientStatus);

    }

    @MessageMapping("/download-file-storage")
    public void receiverDownloadFileStorage(@Payload FileStorageClientDownload clientDownload) {
        webSocketMessaging.notifyFileDownloadClient(clientDownload);
    }

    @MessageMapping("/download-part-file-storage")
    protected void receiverDownloadPartFileStorage(@Payload FileStoragePartClientDownload message) {

        List<PartFileStorageModel> listPartFile;
        if (partFileStorage.containsKey(message.getIdFile()))
            listPartFile = partFileStorage.get(message.getIdFile());
        else {
            listPartFile = new ArrayList<>();
            partFileStorage.put(message.getIdFile(), listPartFile);
        }

        listPartFile.add(new PartFileStorageModel(message.getPartFile(), message.getDataBase64()));
        if (message.isLastPartFile()) {
            FileStorageClientDownload clientDownload = new FileStorageClientDownload();
            clientDownload.setIdFile(message.getIdFile());
            clientDownload.setFileName(message.getFileName());
            clientDownload.setApplicationName(message.getApplicationName());
            clientDownload.setError(message.isError());
            clientDownload.setMessageError(message.getMessageError());
            clientDownload.setDataBase64(FileBase64Util.unionDataBase64FileStorage(listPartFile));
            partFileStorage.remove(message.getIdFile());
            webSocketMessaging.notifyFileDownloadClient(clientDownload);
        }

    }

}
