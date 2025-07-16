package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileStorageLogAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageLogAccessRepository extends JpaRepository<FileStorageLogAccess, Long> {

    @Async
    default void saveAsync(FileStorageLogAccess entity) {
        save(entity);
    }

    int countByUserName(String userName);
}
