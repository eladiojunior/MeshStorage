package br.com.devd2.meshstorageserver.services.cache;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.entites.ServerStorageMetrics;
import br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum;
import br.com.devd2.meshstorageserver.repositories.ServerStorageRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServerStorageCache {
    private final ServerStorageRepository serverStorageRepository;
    private final Map<String, ServerStorage> mapServerStorageCache = new ConcurrentHashMap<>();

    @Value("${mesh.file.weight-free-space:0}")
    private double weight_free_space;
    @Value("${mesh.file.weight-response-time:0}")
    private double weight_response_time;
    @Value("${mesh.file.weight-request-last-minute:0}")
    private double weight_request_last_minute;
    @Value("${mesh.file.weight-errors-last-request:0}")
    private double weight_errors_last_request;

    /**
     * Verifica se a lista de Server Storage está vazia e atualiza com as informações do banco dados.
     */
    public void ifEmptyRefreshListAllServerStorage() {
        if (mapServerStorageCache.isEmpty()) {
            var listAll = serverStorageRepository.findAll();
            listAll.forEach(this::addOrUpdateServerStorage);
        }
    }

    //Chave de cache para recuperar o Score, se existir.
    public record MetricKeyScore(String id,
            long free, long total, long resp, int req, int err) {}
    private Cache<MetricKeyScore, Double> cacheScore = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5)) //5 Minutos no cache
            .maximumSize(10_000)
            .build();

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
     * Recupera todos os Server Storages que não tenham sido REMOVIDOS logicamente, primeiro no cache se não encontrar, vai no banco.
     * @return Lista de Server Storage não removidos.
     */
    public List<ServerStorage> listByNotStatusRemoved() {
        return mapServerStorageCache.values().stream()
                .sorted( (s1, s2) -> {
                    return s1.getServerStorageStatusCode().compareTo(s2.getServerStorageStatusCode());
                }).filter(f ->
                        f.getServerStorageStatusCode() != ServerStorageStatusEnum.REMOVED.getCode()).toList();
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

        var metrics = storage.getMetrics();

        var keyMetrics = new MetricKeyScore(storage.getIdServerStorageClient(),
                metrics.getFreeSpace(), metrics.getTotalSpace(),
                metrics.getResponseTime(), metrics.getRequestLastMinute(), metrics.getErrosLastRequest());

        Double scoreDouble;
        scoreDouble = cacheScore.get(keyMetrics, k ->
                calcScore(storage.getMetrics(), maxResponseTime, maxRequestLastMinute));

        if (log.isDebugEnabled())
            log.debug("ServerStorage: {} => Score: {}", storage.getIdServerStorageClient(), scoreDouble);

        return scoreDouble == null ? 0 : scoreDouble;

    }

    /**
     * Responsável por calcular o Score
     * @param metrics - Métricas do Server Storage
     * @param maxRespTime - Tempo máximo de requisição de todos os Server Storages ativos.
     * @param maxReqsMinute - Quantidade máxima de requisições nos últimos 10 minutos.
     * @return Score calculado.
     */
    private double calcScore(ServerStorageMetrics metrics, long maxRespTime, int maxReqsMinute) {

        int maxErrsMinute = metrics.getErrosLastRequest();
        /* Normaliza cada métrica para 0..1 (quanto maior, melhor) */
        double free   = ratio(metrics.getFreeSpace(), metrics.getTotalSpace());     // ↑ bom
        double rtGood = 1.0 - ratio(metrics.getResponseTime(),      maxRespTime);   // ↓ bom
        double load   = 1.0 - ratio(metrics.getRequestLastMinute(), maxReqsMinute); // ↓ bom
        double errs   = 1.0 - ratio(metrics.getErrosLastRequest(),  maxErrsMinute); // ↓ bom

        /* pesos (soma = 1.0) */
        double score =  weight_free_space           * free +
                        weight_response_time        * rtGood +
                        weight_request_last_minute  * load +
                        weight_errors_last_request  * errs;

        /* Garante dentro de 0..1, caso algum valor extrapole */
        return Math.max(0, Math.min(1, score));

    }

    /** calcula razão protegendo divisão por zero  */
    private static double ratio(long part, long total) {
        return total == 0 ? 0.0 : (double) part / total; // 0‥1
    }

}