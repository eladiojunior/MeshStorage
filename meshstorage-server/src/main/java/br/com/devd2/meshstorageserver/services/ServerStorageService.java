package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorageMetrics;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperServer;
import br.com.devd2.meshstorageserver.models.MetricsStorageModel;
import br.com.devd2.meshstorageserver.models.ServerStorageModel;
import br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum;
import br.com.devd2.meshstorageserver.repositories.ServerStorageRepository;
import br.com.devd2.meshstorageserver.services.cache.ServerStorageCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ServerStorageService {
    private final ServerStorageCache cacheServerStorage;
    private final ServerStorageRepository serverStorageRepository;

    public ServerStorageService(ServerStorageCache cacheServerStorage, ServerStorageRepository serverStorageRepository) {
        this.cacheServerStorage = cacheServerStorage;
        this.serverStorageRepository = serverStorageRepository;
    }

    /**
     * Recupera um ServerStorage pelo idServerStorageClient.
     * @param idServerStorageClient - Identificador único do Client de Storage;
     * @return ServerStorage ou nulo se não existir;
     */
    public ServerStorage getByIdServerStorageClient(String idServerStorageClient) {
        return cacheServerStorage.getByIdServerStorageClient(idServerStorageClient);
    }

    /**
     * Responsável por recuperar o melhor storage disponível no momento
     * para armazenamento das informações.
     * @return ServerStoreage ou erro.
     * @throws ApiBusinessException Erro de negócio.
     */
    public ServerStorage getBestServerStorage() throws ApiBusinessException {
        return cacheServerStorage.listByStatusActive().stream()
                .max(Comparator.comparingDouble(ServerStorage::getScoreStorage))
                .orElseThrow(() -> new ApiBusinessException("Nenhum servidor de armazenamento registrado ou disponível no momento."));
    }

    /**
     * Responsável por recuperar o melhor storage disponível no momento
     * para armazenamento das informações, que seja diferente do ID informado.
     * @return ServerStoreage ou erro.
     * @throws ApiBusinessException Erro de negócio.
     */
    public ServerStorage getBestServerStorage(String idServerStorageUse) throws ApiBusinessException {
        return cacheServerStorage.listByStatusActive().stream()
                .filter(s -> !Objects.equals(s.getIdServerStorageClient(), idServerStorageUse))
                .max(Comparator.comparingDouble(ServerStorage::getScoreStorage))
                .orElse(null);
    }

    /**
     * Recupera a lista de Storages (armazenamento) de um server pelo nome do servidor.
     * @param serverName - Nome do server (ServerStorage), pode ser o nome do servidor ou codenome;
     * @return Lista de Storages do Server
     */
    public List<ServerStorage> findByServerName(String serverName) {
        return cacheServerStorage.listByServerName(serverName);
    }

    /**
     * Recupera um Server/Storage pelos nomes registrado na base.
     * @param serverName - Nome do server (ServerName), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do armazenamento (StorageName), pode ser o nome do local de armazenamento ou codenome;
     * @return Instância de um ServerStorage ou null
     */
    public ServerStorage findByServerNameAndStorageName(String serverName, String storageName) {
        return cacheServerStorage.getByServerNameAndStorageName(serverName, storageName);
    }

    /**
     * Registra um ServerStorage na estrutura para utilização.
     * @param model - Informações do Server Storage para utilização.
     * @return Informações do Server Storage registrado.
     * @throws ApiBusinessException Erro de negócio
     */
    public ServerStorage registerServerStorage(ServerStorageModel model) throws ApiBusinessException {

        if (model == null)
            throw new ApiBusinessException("Informações do ServerStorage não pode ser nulo.");
        if (model.getIdClient() == null || model.getIdClient().isEmpty())
            throw new ApiBusinessException("ID do Client (identificador do Cliente) não pode ser nulo ou vazio.");
        if (model.getServeName() == null || model.getServeName().isEmpty())
            throw new ApiBusinessException("Server name (nome do servidor) não pode ser nulo ou vazio.");
        if (model.getIpServer() == null || model.getIpServer().isEmpty())
            throw new ApiBusinessException("IP do Server (endereço IP do servidor) não pode ser nulo ou vazio.");
        if (!HelperServer.IsValidIp(model.getIpServer()))
            throw new ApiBusinessException("IP do Server (endereço IP do servidor) inválido.");
        if (model.getStorageName() == null || model.getStorageName().isEmpty())
            throw new ApiBusinessException("Storage name (nome do local de armazenamento) não pode ser nulo ou vazio.");

        //Verificar se existe um server/storage registrado.
        ServerStorage server = serverStorageRepository
                .findByServerNameAndStorageName(model.getServeName(), model.getStorageName()).orElse(null);
        if (server != null)
            throw new ApiBusinessException(String.format("Existem um Server [%1s] e Storage [%2s] registrado.", model.getServeName(), model.getStorageName()));

        server = new ServerStorage();
        server.setIdServerStorageClient(model.getIdClient());
        server.setServerName(model.getServeName());
        server.setIpServer(model.getIpServer());
        server.setOsServer(model.getOsServer());
        server.setStorageName(model.getStorageName());
        server.setDateTimeRegisteredServerStorage(LocalDateTime.now());
        server.setServerStorageStatusCode(ServerStorageStatusEnum.ACTIVE.getCode());
        //Metricas do Storage
        var metrics = new ServerStorageMetrics();
        metrics.setTotalSpace(model.getTotalSpace());
        metrics.setFreeSpace(model.getFreeSpace());
        metrics.setTotalFiles(0L);
        metrics.setResponseTime(0L);
        metrics.setRequestLastMinute(0);
        metrics.setErrosLastRequest(0);
        metrics.setDateTimeLastAvailable(LocalDateTime.now());
        server.setMetrics(metrics);

        var serverStorage = serverStorageRepository.save(server);

        //Adicionar novo Storage no cache...
        cacheServerStorage.clearCache();
        cacheServerStorage.addOrUpdateServerStorage(serverStorage);
        return serverStorage;

    }

    /**
     * Atualiza as métricas do Server Storage.
     *
     * @param serverName  - Nome do server (FileServer), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do storage que está sendo utilizando no server (FileServer), local físico de armazenamento;
     * @param metricsStorage - Metricas de espaço livre em disco (MB), disponibilidade, erros, requisições e tempo de resposta do Storage;
     * @throws ApiBusinessException - Erro de negócio
     */
    public void updateServerStorageMetrics(String serverName, String storageName, MetricsStorageModel metricsStorage) throws ApiBusinessException {
        //TODO implementar lógica de atualização das métricas.
    }

    /**
     * Atualiza o status do Server Storage quanto ao espaço em disco (MB) e se ativo para receber arquivos.
     *
     * @param serverName  - Nome do server (FileServer), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do storage que está sendo utilizando no server (FileServer), local físico de armazenamento;
     * @param freeSpace   - Espaço livre em disco (MB), com referência do storage do server (FileServer);
     * @param available   - Flag que indica a disponibilidade do server/storage para receber informações.
     * @throws ApiBusinessException - Erro de negócio
     */
    public void updateServerStorageStatus(String serverName, String storageName, long freeSpace, boolean available) throws ApiBusinessException {

        if (serverName == null || serverName.isEmpty())
            throw new ApiBusinessException("Server name (nome do servidor) não pode ser nulo ou vazio.");
        if (storageName == null || storageName.isEmpty())
            throw new ApiBusinessException("Storage name (nome do local de armazenamento) não pode ser nulo ou vazio.");

        //Verificar Server Storage existente para atualização.
        ServerStorage server = cacheServerStorage.getByServerNameAndStorageName(serverName, storageName);
        if (server == null)
            throw new ApiBusinessException("Server Storage não identificado para atualização do seu status.");

        if (available)
            server.setServerStorageStatusCode(ServerStorageStatusEnum.ACTIVE.getCode());
        else
            server.setServerStorageStatusCode(ServerStorageStatusEnum.INACTIVE.getCode());

        server.getMetrics().setFreeSpace(freeSpace);
        server.getMetrics().setDateTimeLastAvailable(LocalDateTime.now());

        serverStorageRepository.save(server);

        //Atualizar as métricas do Storage no cache.
        cacheServerStorage.refreshMetrics(server);

    }

    /**
     * Atualizar a quantidade de arquivo registrado no Server Storage, upload.
     * @param idServerStorage - Identificador do ServerStorage para atualizar a quantidade;
     * @param hasAdicionar - flag para que indica se será para adicionar (true) ou subtrair (false) da quantidade.
     * @throws ApiBusinessException - Erro de negócio
     */
    public void updateServerStorageTotalFile(Long idServerStorage, boolean hasAdicionar) throws ApiBusinessException {

        if (idServerStorage == null || idServerStorage == 0)
            throw new ApiBusinessException("Identificador do ServerStorage não pode ser nulo ou zero.");

        //Verificar Server Storage existente para atualização.
        ServerStorage server = serverStorageRepository.findById(idServerStorage).orElse(null);
        if (server == null)
            throw new ApiBusinessException("Server Storage não identificado para atualização da quantidade de arquivos.");

        long totalFiles = server.getMetrics().getTotalFiles() == null ? 0 : server.getMetrics().getTotalFiles();
        totalFiles = hasAdicionar ? totalFiles + 1 : totalFiles - 1;
        if (totalFiles < 0) totalFiles = 0; //Evitar informação negativa;
        server.getMetrics().setTotalFiles(totalFiles);
        server.getMetrics().setDateTimeLastAvailable(LocalDateTime.now());

        serverStorageRepository.save(server);

        //Atualizar Storage no cache...
        cacheServerStorage.addOrUpdateServerStorage(server);

    }

    /**
     * Atualiza o IdClient do Server Storage caso ele mude.
     *
     * @param id       - Identificador do Server/Storage no banco.
     * @param idClient - Identificador do Cliente Server/Storage no servidor;
     * @throws ApiBusinessException - Erro de negócio
     */
    public void updateIdClientServerStorage(Long id, String idClient) throws ApiBusinessException {

        if (id == null || id == 0)
            throw new ApiBusinessException("Id do Server Storage não pode ser nulo ou zero.");
        if (idClient == null || idClient.isEmpty())
            throw new ApiBusinessException("Id Client (identificador do storage cliente) não pode ser nulo ou vazio.");

        //Verificar Server Storage existente para atualização.
        ServerStorage server = serverStorageRepository.findById(id).orElse(null);
        if (server == null)
            throw new ApiBusinessException("Server Storage não identificado para atualização do seu status.");

        server.setIdServerStorageClient(idClient);

        serverStorageRepository.save(server);

        //Atualizar Storage no cache...
        cacheServerStorage.addOrUpdateServerStorage(server);

    }

    /**
     * Lista de Server Storages registrados para utilização, seja os disponíveis ou não.
     * @return Lista de Server Storages encontrados.
     */
    public List<ServerStorage> getListServerStorage(boolean hasAvailable) throws ApiBusinessException {
        if (hasAvailable)
            return cacheServerStorage.listByStatusActive();
        else
            return serverStorageRepository.findAll();
    }

    /**
     * Realiza atualização nas metricas do Servidor de Armazenamento de ResponseTime e quantidade de requisições.
     * @param idServerStorageClient - Identificador do ServerStorageClient para atualização
     * @param responseTime - Tempo de resposta a requisição para o Servidor de Armanzenamento.
     */
    public void updateMetricsResposeTimeAndRequestCount(String idServerStorageClient, long responseTime) {
        //TODO Implementar
        log.info("Atualizar ServerStorageClient: {} - Tempo Resposta: {} - +1 Request", idServerStorageClient, responseTime);
    }

    /**
     * Realiza atualização nas métricas do Servidor de Armazenamento quantidade de Erros.
     * @param idServerStorageClientErrors - List[String] de Ids de Servidores que geraram erro de cominucação.
     */
    public void updateMetricsErrors(List<String> idServerStorageClientErrors) {
        //TODO Implementar
        for (String idServerStorageClient : idServerStorageClientErrors) {
            log.info("Atualizar ServerStorageClient: {} - +1 Erro", idServerStorageClient);
        }
    }
}