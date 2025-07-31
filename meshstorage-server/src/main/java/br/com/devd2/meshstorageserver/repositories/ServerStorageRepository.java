package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerStorageRepository extends JpaRepository<ServerStorage, Long> {
    Optional<ServerStorage> findByServerNameAndStorageName(String serverName, String storageName);
    List<ServerStorage> findByServerName(String serverName);
    Optional<ServerStorage> findByIdServerStorageClient(String idServerStorageClient);
    List<ServerStorage> findByServerStorageStatusCode(Integer serverStorageStatusCode);
}
