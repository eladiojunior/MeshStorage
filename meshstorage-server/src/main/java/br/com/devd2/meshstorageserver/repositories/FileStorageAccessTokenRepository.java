package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileStorageAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileStorageAccessTokenRepository extends JpaRepository<FileStorageAccessToken, Long> {

    @Async
    default void saveAsync(FileStorageAccessToken entity) {
        save(entity);
    }
    Optional<FileStorageAccessToken> findByAccessToken(String accessToken);
}
