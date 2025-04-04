package br.com.devd2.meshstorageclient.components;

import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClient;
import br.com.devd2.meshstorageclient.services.StorageService;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

public class ClientCommandPrivateHandler implements StompFrameHandler {
    private final StorageService storageService = new StorageService();

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {

        if (!(payload instanceof String))
            return;

        var fileStorage = JsonUtil.fromJson((String)payload, FileStorageClient.class);

        try {
            var pathFileStorage = storageService.writeFileStorage(fileStorage);
            fileStorage.setPathStorage(pathFileStorage);
            fileStorage.setErrorWrite(false);
            fileStorage.setMessageErrorWrite("");
        } catch (Exception err) {
            fileStorage.setErrorWrite(true);
            fileStorage.setMessageErrorWrite(err.getMessage());
            System.out.println("Error: " + err.getMessage());
        }

        storageService.statusServerStorage(fileStorage);

    }

}