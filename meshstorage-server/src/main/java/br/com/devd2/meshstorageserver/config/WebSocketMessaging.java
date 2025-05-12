package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorage.helper.JsonUtil;
import br.com.devd2.meshstorage.models.FileStorageClientDownload;
import br.com.devd2.meshstorage.models.FileStorageClientStatus;
import br.com.devd2.meshstorage.models.messages.FileDeleteMessage;
import br.com.devd2.meshstorage.models.messages.FileDownloadMessage;
import br.com.devd2.meshstorage.models.messages.FileRegisterMessage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperSessionClients;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class WebSocketMessaging {
    private final Map<String, CompletableFuture<FileStorageClientDownload>> pendingsFileDownload = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<FileStorageClientStatus>> pendingsFileStatus = new ConcurrentHashMap<>();

    private final Duration timeout_status = Duration.ofSeconds(5); //segundos...
    private final Duration timeout_download = Duration.ofSeconds(10); //segundos...
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessaging(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Inicia o processo de download de um arquivo do Storage, aguardando a resposta do Servidor.
     * @param idClientStorage - Identificação do Client Storage para recuperar o arquivo.
     * @param fileDownloadMessage - Informações para o dowanload.
     * @return Dados do arquivo baixado ou informações do erro no Server.
     */
    public FileStorageClientDownload startFileDownloadClient(String idClientStorage, FileDownloadMessage fileDownloadMessage) throws ApiBusinessException, ExecutionException, InterruptedException, TimeoutException {

        verificarSessionClient(idClientStorage);

        CompletableFuture<FileStorageClientDownload> future = new CompletableFuture<>();
        pendingsFileDownload.put(fileDownloadMessage.getIdFile(), future);
        String jsonRequest = JsonUtil.toJson(fileDownloadMessage);
        messagingTemplate.convertAndSend("/client/"+idClientStorage, jsonRequest);
        try {
            return future.get(timeout_download.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            pendingsFileDownload.remove(fileDownloadMessage.getIdFile());
        }

    }

    /**
     * Notifica o retorno do servidor.
     * @param response - Informações do servidor.
     */
    public void notifyFileDownloadClient(FileStorageClientDownload response) {
        CompletableFuture<FileStorageClientDownload> f = pendingsFileDownload.get(response.getIdFile());
        if (f != null) f.complete(response);
    }

    /**
     * Inicia o processo de registro de um arquivo no Storage, aguardando a resposta do Servidor.
     * @param idClientStorage - Identificação do Client Storage para recuperar o arquivo.
     * @param fileRegisterMessage - Informações para o registro do arquivo.
     * @return Status do registro do arquivo no Server.
     */
    public FileStorageClientStatus startFileRegisterClient(String idClientStorage, FileRegisterMessage fileRegisterMessage) throws ApiBusinessException, ExecutionException, InterruptedException, TimeoutException {

        verificarSessionClient(idClientStorage);

        CompletableFuture<FileStorageClientStatus> future = new CompletableFuture<>();
        pendingsFileStatus.put(fileRegisterMessage.getIdFile(), future);
        String jsonRequest = JsonUtil.toJson(fileRegisterMessage);
        messagingTemplate.convertAndSend("/client/"+idClientStorage, jsonRequest);
        try {
            return future.get(timeout_status.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            pendingsFileStatus.remove(fileRegisterMessage.getIdFile());
        }

    }

    private static void verificarSessionClient(String idClientStorage) throws ApiBusinessException {
        var sessionClient = HelperSessionClients.get().getSessionClient(idClientStorage);
        if (sessionClient == null || sessionClient.isEmpty())
            throw new ApiBusinessException("Não foi possível identificar uma sessão de Storage.");
    }

    /**
     * Inicia o processo de remoção de um arquivo no Storage, aguardando a resposta do Servidor.
     * @param idClientStorage - Identificação do Client Storage para recuperar o arquivo.
     * @param fileDeleteMessage - Informações para o remoção do arquivo.
     * @return Status do registro do arquivo no Server.
     */
    public FileStorageClientStatus startFileDeleteClient(String idClientStorage, FileDeleteMessage fileDeleteMessage) throws ApiBusinessException, ExecutionException, InterruptedException, TimeoutException {

        verificarSessionClient(idClientStorage);

        CompletableFuture<FileStorageClientStatus> future = new CompletableFuture<>();
        pendingsFileStatus.put(fileDeleteMessage.getIdFile(), future);
        String jsonRequest = JsonUtil.toJson(fileDeleteMessage);
        messagingTemplate.convertAndSend("/client/"+idClientStorage, jsonRequest);
        try {
            // espera resposta ou timeout
            return future.get(timeout_status.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            pendingsFileStatus.remove(fileDeleteMessage.getIdFile());
        }

    }

    /**
     * Notifica o retorno do servidor.
     * @param response - Informações do servidor.
     */
    public void notifyFileStatusClient(FileStorageClientStatus response) {
        CompletableFuture<FileStorageClientStatus> f = pendingsFileStatus.get(response.getIdFile());
        if (f != null) f.complete(response);
    }

}
