package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    Optional<FileStorage> findByApplicationIdAndHashFileContent(Long applicationId, String hashFileContent);
    Optional<FileStorage> findByIdFile(String idFile);
    Optional<Long> countByApplicationId(Long idApplication);
    Page<FileStorage> findByApplicationId(Long applicationId, Pageable pageable);
}