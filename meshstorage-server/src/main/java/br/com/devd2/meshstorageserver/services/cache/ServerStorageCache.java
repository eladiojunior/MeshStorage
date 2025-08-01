package br.com.devd2.meshstorageserver.services.cache;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorageMetrics;
import br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum;
import br.com.devd2.meshstorageserver.repositories.ServerStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServerStorageCache {
    private final ServerStorageRepository serverStorageRepository;
    private final Map<String, ServerStorage> mapServerStorageCache = new ConcurrentHashMap<>();

    @Value("${weight-free-space:0}")
    private double weight_free_space;
    @Value("${weight-response-time:0}")
    private double weight_response_time;
    @Value("${weight-request-last-minute:0}")
    private double weight_request_last_minute;
    @Value("${weight-errors-last-request:0}")
    private double weight_errors_last_request;

    public ServerStorageCache(ServerStorageRepository serverStorageRepository) {
        this.serverStorageRepository = serverStorageRepository;
    }

    /**
     * Atualizar as metricas do servidor de armazenamento se estiver em cache.
     * @param storage Servidor de armazenamento para atualização do cache.
     */
    public void refreshMetrics(ServerStorage storage) {
        try {
            if (storage == null || storage.getMetrics() == null)
                return;
            mapServerStorageCache.computeIfPresent(storage.getIdServerStorageClient(), (k, ss) -> {
                ss.getMetrics().setTotalSpace(storage.getMetrics().getTotalSpace());
                ss.getMetrics().setFreeSpace(storage.getMetrics().getFreeSpace());
                ss.getMetrics().setTotalFiles(storage.getMetrics().getTotalFiles());
                ss.getMetrics().setResponseTime(storage.getMetrics().getTotalSpace());
                ss.getMetrics().setRequestLastMinute(storage.getMetrics().getRequestLastMinute());
                ss.getMetrics().setErrosLastRequest(storage.getMetrics().getErrosLastRequest());
                ss.getMetrics().setDateTimeLastAvailable(storage.getMetrics().getDateTimeLastAvailable());
                //Calcular Score...
                ss.setScoreStorage(score(ss));
                return ss;
            });
        } catch (Exception error) {
            log.error("Erro ao atualizar as métricas, cache será limpo.", error);
            clearCache();
        }
    }

    /**
     * Limpa o cache e força a recuperação em banco de dados.
     */
    public void clearCache() {
        mapServerStorageCache.clear();
    }

    /**
     * Recupera uma Server Storage pelo ID, mas verifica se está no cache antes de ir ao banco de dados.
     * @param idServerStorageClient - Identificador únido do Server Storage no Client para recuperação.
     * @return Instância do Server Storage do cache ou indo ao banco, nulo, se não encontrar.
     */
    public ServerStorage getByIdServerStorageClient(String idServerStorageClient) {
        return mapServerStorageCache.computeIfAbsent(idServerStorageClient, this::loadFromDb);
    }

    /**
     * Recupera a lista de Storages (armazenamento) de um server pelo nome do servidor,
     * mas verifica se está no cache antes de ir ao banco de dados.
     * @param serverName - Nome do servidor para recuperação da lista de Storages.
     * @return Lista de ServerStorage com o serverName igual.
     */
    public List<ServerStorage> listByServerName(String serverName) {
        List<ServerStorage> listServerStorageCache = mapServerStorageCache.values()
                .stream().filter(ss -> Objects.equals(ss.getServerName(), serverName))
                .collect(Collectors.toList());
        if (listServerStorageCache.isEmpty())
        {//Verificar se existe no banco de dados...
            List<ServerStorage> listFromDb = serverStorageRepository.findByServerName(serverName);
            listFromDb.forEach(s -> {
                s.setScoreStorage(score(s)); //Calcular o Score.
                mapServerStorageCache.put(s.getIdServerStorageClient(), s);
            });
            return listFromDb;
        }
        return listServerStorageCache;
    }

    /**
     * Recupera todos os Server Storages ativos, primeiro no cache se não encontrar, vai no banco.
     * @return Lista de Server Storage ativo.
     */
    public List<ServerStorage> listByStatusActive() {
        return mapServerStorageCache.values().stream()
                .filter(f ->
                        f.getServerStorageStatusCode() == ServerStorageStatusEnum.ACTIVE.getCode()).toList();
    }

    /**
     * Recupera um Server/Storage pelos nomes registrado na base, mas verifica se está no cache antes
     * de ir ao banco de dados.
     * @param serverName - Nome do server (ServerName), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do armazenamento (StorageName), pode ser o nome do local de armazenamento ou codenome;
     * @return Instância de um ServerStorage ou null
     */
    public ServerStorage getByServerNameAndStorageName(String serverName, String storageName) {
        ServerStorage serverStorageCache = mapServerStorageCache.values()
                .stream().filter(ss -> Objects.equals(ss.getServerName(), serverName) &&
                        Objects.equals(ss.getStorageName(), storageName)).findFirst().orElse(null);
        if (serverStorageCache == null)
        {//Verificar se existe no banco de dados...
            ServerStorage itemFromDb = serverStorageRepository
                    .findByServerNameAndStorageName(serverName, storageName).orElse(null);
            if (itemFromDb!=null) {
                itemFromDb.setScoreStorage(score(itemFromDb));
                mapServerStorageCache.put(itemFromDb.getIdServerStorageClient(), itemFromDb);
            }
            return itemFromDb;
        }
        return serverStorageCache;
    }

    /**
     * Adiciona ou atualizar um Server Storage no controle de cache.
     * @param serverStorage - Instância do Server Storage para ser colocado no cache.
     */
    public void addOrUpdateServerStorage(ServerStorage serverStorage) {
        if (serverStorage == null) return;
        serverStorage.setScoreStorage(score(serverStorage));
        mapServerStorageCache.put(serverStorage.getIdServerStorageClient(), serverStorage);
    }

    /**
     * Recupera um ServerStorage pelo ID no banco de dados.
     * @param idServerStorageClient - Identificador únido do Server Storage no Client para recuperação.
     * @return Instância do ServerStorage ou nulo, se não encontrar.
     */
    private ServerStorage loadFromDb(String idServerStorageClient) {
        var serverStorage = serverStorageRepository
                .findByIdServerStorageClient(idServerStorageClient).orElse(null);
        if (serverStorage != null) // Calcular Score.
            serverStorage.setScoreStorage(score(serverStorage));
        return serverStorage;
    }

    /**
     * Calcular o Score conforme os pesos definidos para ranquear os melhores storages para armazenamento.
     * @param storage - Server Storage para calculo do Score.
     * @return Score do Storage, valor entre 0 e 1;
     */
    private double score(ServerStorage storage) {

        var listStoragesAvaliable = listByStatusActive();
        if (listStoragesAvaliable.size() <= 1)
            return 1; //Como só existe um Storage ativo não gastar com cálculo de Score.

        //Calular metrics dos storages ativos para definir a máxima tempo de resposta e quantidade de requisições.
        var listMetricsAvaliableStorages = listStoragesAvaliable.stream()
                .map(ServerStorage::getMetrics).toList();
        long maxResponseTime = listMetricsAvaliableStorages.stream()
                .mapToLong(ServerStorageMetrics::getResponseTime).max().orElse(0);
        int maxRequestLastMinute = listMetricsAvaliableStorages.stream()
                .mapToInt(ServerStorageMetrics::getRequestLastMinute).max().orElse(0);

        var score = calcScore(storage.getMetrics(), maxResponseTime, maxRequestLastMinute);
        log.info("ServerStorage: {} => Score: {}", storage.getIdServerStorageClient(), score);
        return score;

    }

    /**
     * Responsável por calcular o Score
     * @param storageMetrics - Métricas do Server Storage
     * @param maxResponseTime - Tempo máximo de requisição de todos os Server Storages ativos.
     * @param maxRequestLastMinute - Quantidade máxima de requisições nos últimos 10 minutos.
     * @return Score calculado.
     */
    private double calcScore(ServerStorageMetrics storageMetrics, long maxResponseTime, int maxRequestLastMinute) {
        double freeSpace  = storageMetrics.getTotalSpace() !=0 ?
                ((double) storageMetrics.getFreeSpace() / storageMetrics.getTotalSpace()) : 0; // 0‑1
        double respTime   = maxResponseTime != 0 ?
                ((double) storageMetrics.getResponseTime() / maxResponseTime) : 0;             // 0‑1 (quanto MAIOR = pior)
        double reqsMinute = maxRequestLastMinute != 0 ?
                ((double) storageMetrics.getRequestLastMinute() / maxRequestLastMinute) : 0;   // 0‑1
        double errsMinute = storageMetrics.getErrosLastRequest();                              // 0‑1
        return  ((weight_free_space * freeSpace) - (weight_response_time * respTime) -
                (weight_request_last_minute * reqsMinute) - (weight_errors_last_request * errsMinute));
    }

}