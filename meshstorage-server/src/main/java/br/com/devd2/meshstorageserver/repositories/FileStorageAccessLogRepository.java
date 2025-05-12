package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileStorageAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageAccessLogRepository extends JpaRepository<FileStorageAccessLog, Long> {

    @Async
    default void saveAsync(FileStorageAccessLog entity) {
        save(entity);
    }

}
