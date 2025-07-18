package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileStorageAccessTokenRepository extends JpaRepository<FileAccessToken, Long> {

    @Async
    default void saveAsync(FileAccessToken entity) {
        save(entity);
    }
    Optional<FileAccessToken> findByAccessToken(String accessToken);
}
