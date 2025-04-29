package br.com.devd2.meshstorageclient.config;

import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.components.StorageClientEndpoint;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;

public class StorageConfig {
    private static final Logger logger = LogManager.getLogger(StorageConfig.class);
    private static StorageConfig instancia;
    private StorageClient client;
    private StorageClientEndpoint clientEndpoint;
    private WebSocketContainer container;

    private StorageConfig() {
        container = ContainerProvider.getWebSocketContainer();
    }

    public static StorageConfig get() {
        if (instancia == null)
            instancia = new StorageConfig();
        return instancia;
    }

    public boolean isExistendClient() {
        if (client == null)
            client = carregarStorageServer();
        return (client != null);
    }

    public StorageClient getClient() {
        if (client == null) {
            client = new StorageClient();
        }
        return client;
    }

    private void openConnectServerWs() {
        try {
            // Conecta ao servidor
            clientEndpoint = new StorageClientEndpoint();
            container.connectToServer(clientEndpoint, URI.create(getClient().getUrlWebsocketServer()));
        } catch (Exception error) {
            logger.error("Erro ao conectar com o Server", error);
        }
    }

    public boolean notConnectServer() {
        if (clientEndpoint == null || !clientEndpoint.isConnected())
            openConnectServerWs();
        return false;
    }

    /**
     * Recupera as informações do Storage já registrado.
     * @return
     */
    private StorageClient carregarStorageServer() {
        StorageClient storageClient = null;
        try {
            File fileStorage = new File("storage.json");
            if (!fileStorage.exists())
                return storageClient;
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(fileStorage));
            storageClient = gson.fromJson(reader, StorageClient.class);
            reader.close();
        } catch (Exception erro) {
            logger.error("Erro ao carregar o arquivo de StorageServer.", erro);
        }
        return storageClient;
    }

    /**
     * Gravar as informações do Storage
     */
    public void gravarStorageServer() {
        try {
            File fileStorage = new File("storage.json");
            Gson gson = new Gson();
            JsonWriter writer = new JsonWriter(new FileWriter(fileStorage));
            gson.toJson(client, StorageClient.class, writer);
            writer.close();
        } catch (Exception erro) {
            logger.error("Erro ao gravar o arquivo de StorageServer.", erro);
        }
    }

    public StorageClientEndpoint getSession() {
        return clientEndpoint;
    }

}