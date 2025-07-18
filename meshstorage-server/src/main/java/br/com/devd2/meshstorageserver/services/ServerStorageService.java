package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperServer;
import br.com.devd2.meshstorageserver.models.ServerStorageModel;
import br.com.devd2.meshstorageserver.repositories.ServerStorageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServerStorageService {
    private final ServerStorageRepository serverStorageRepository;

    public ServerStorageService(ServerStorageRepository fileServerRepository) {
        this.serverStorageRepository = fileServerRepository;
    }

    /**
     * Recupera um ServerStorage pelo idClient.
     * @param idClient - Identificador único do Client de Storage;
     * @return ServerStorage ou nulo se não existir;
     */
    public ServerStorage findByIdClient(String idClient) {
        Optional<ServerStorage> serverStorage = serverStorageRepository.findByIdClient(idClient);
        return serverStorage.orElse(null);
    }

    /**
     * Responsável por recuperar o melhor storage disponível no momento
     * para armazenamento das informações.
     * @return ServerStoreage ou erro.
     * @throws ApiBusinessException Erro de negócio.
     */
    public ServerStorage getBestServerStorage() throws ApiBusinessException {
        List<ServerStorage> listServerStorage = serverStorageRepository.findByAvailableTrueOrderByFreeSpaceDesc();
        var server = listServerStorage.isEmpty() ? null : listServerStorage.get(0);
        if (server == null)
            throw new ApiBusinessException("Nenhum servidor de armazenamento registrado ou disponível.");
        return server;
    }

    /**
     * Recupera a lista de Storages (armazenamento) de um server pelo nome do servidor.
     * @param serverName - Nome do server (ServerStorage), pode ser o nome do servidor ou codenome;
     * @return Lista de Storages do Server
     */
    public List<ServerStorage> findByServerName(String serverName) {
        return serverStorageRepository.findByServerName(serverName);
    }

    /**
     * Recupera um Server/Storage pelos nomes registrado na base.
     * @param serverName - Nome do server (ServerName), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do armazenamento (StorageName), pode ser o nome do local de armazenamento ou codenome;
     * @return Instância de um ServerStorage ou null
     */
    public ServerStorage findByServerNameAndStorageName(String serverName, String storageName) {
        Optional<ServerStorage> serverStorage = serverStorageRepository.findByServerNameAndStorageName(serverName, storageName);
        return serverStorage.orElse(null);
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
                .findByServerNameAndStorageName(model.getServeName(), model.getStorageName())
                .orElse(null);
        if (server != null)
            throw new ApiBusinessException(String.format("Existem um Server [%1s] e Storage [%2s] registrado.", model.getServeName(), model.getStorageName()));

        server = new ServerStorage();
        server.setIdServerStorageClient(model.getIdClient());
        server.setServerName(model.getServeName());
        server.setIpServer(model.getIpServer());
        server.setOsServer(model.getOsServer());
        server.setStorageName(model.getStorageName());
        server.setTotalSpace(model.getTotalSpace());
        server.setFreeSpace(model.getTotalSpace());
        server.setTotalFiles(0L);
        server.setAvailable(true);
        server.setDateTimeAvailable(LocalDateTime.now());
        server.setDateTimeRegisteredServerStorage(LocalDateTime.now());

        return serverStorageRepository.save(server);

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
        ServerStorage server = serverStorageRepository
                .findByServerNameAndStorageName(serverName, storageName)
                .orElse(null);

        if (server == null)
            throw new ApiBusinessException("Server Storage não identificado para atualização do seu status.");

        server.setFreeSpace(freeSpace);
        server.setAvailable(available);
        server.setDateTimeAvailable(LocalDateTime.now());

        serverStorageRepository.save(server);

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

        long totalFiles = server.getTotalFiles() == null ? 0 : server.getTotalFiles();
        totalFiles = hasAdicionar ? totalFiles + 1 : totalFiles - 1;
        if (totalFiles < 0) totalFiles = 0; //Evitar informação negativa;
        server.setTotalFiles(totalFiles);
        server.setDateTimeAvailable(LocalDateTime.now());

        serverStorageRepository.save(server);

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

    }

    /**
     * Lista de Server Storages registrados para utilização, seja os disponíveis ou não.
     * @return Lista de Server Storages encontrados.
     */
    public List<ServerStorage> getListServerStorage(boolean hasAvailable) throws ApiBusinessException {
        if (hasAvailable)
            return serverStorageRepository.findByAvailableTrue();
        else
            return serverStorageRepository.findAll();
    }

}