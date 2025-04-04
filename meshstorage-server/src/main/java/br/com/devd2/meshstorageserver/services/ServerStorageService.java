package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.models.request.ServerStorageRequest;
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
     * Registra um ServerStorage na estrutura para utilização.
     * @param request - Informações do Server Storage para utilização.
     * @return Informações do Server Storage registrado.
     * @throws ApiBusinessException
     */
    public ServerStorage registerServerStorage(ServerStorageRequest request) throws ApiBusinessException {

        if (request == null)
            throw new ApiBusinessException("Informações do ServerStorage não pode ser nulo.");
        if (request.getServeName() == null || request.getServeName().isEmpty())
            throw new ApiBusinessException("Server name (nome do servidor) não pode ser nulo ou vazio.");
        if (request.getStorageName() == null || request.getStorageName().isEmpty())
            throw new ApiBusinessException("Storage name (nome do local de armazenamento) não pode ser nulo ou vazio.");

        //Verificar se existe um server/storage registrado.
        ServerStorage server = serverStorageRepository
                .findByServerNameAndStorageName(request.getServeName(), request.getStorageName())
                .orElse(null);
        if (server != null)
            throw new ApiBusinessException(String.format("Existem um Server [%1s] e Storage [%2s] registrado.", request.getServeName(), request.getStorageName()));

        server = new ServerStorage();
        server.setServerName(request.getServeName());
        server.setIpServer(request.getIpServer());
        server.setStorageName(request.getStorageName());
        server.setTotalSpace(request.getTotalSpace());
        server.setFreeSpace(request.getTotalSpace());
        server.setAvailable(true);
        server.setDateTimeAvailable(LocalDateTime.now());
        server.setDateTimeServerStorage(LocalDateTime.now());

        return serverStorageRepository.save(server);

    }

    /**
     * Atualiza o status do Server Storage quanto ao espaço em disco (MB) e se ativo para receber arquivos.
     * @param serverName - Nome do server (FileServer), pode ser o nome do servidor ou codenome;
     * @param storageName - Nome do storage que está sendo utilizando no server (FileServer), local físico de armazenamento;
     * @param freeSpace - Espaço livre em disco (MB), com referência do storage do server (FileServer);
     * @param available - Flag que indica a disponibilidade do server/storage para receber informações.
     * @throws ApiBusinessException - Erro de negócio
     * @return ServerStorage atualizado.
     */
    public ServerStorage updateServerStorageStatus(String serverName, String storageName, long freeSpace, boolean available) throws ApiBusinessException {

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

        return serverStorageRepository.save(server);

    }

    /**
     * LIsta de Server Storages registrados para utilização, seja os disponíveis ou não.
     * @return Lista de Server Storages encontrados.
     */
    public List<ServerStorage> getListServerStorage(boolean hasAvailable) throws ApiBusinessException {
        if (hasAvailable)
            return serverStorageRepository.findByAvailableTrue();
        else
            return serverStorageRepository.findAll();
    }

}