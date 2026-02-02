package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileStorageRepository extends JpaRepository<FileStorage, Long>, JpaSpecificationExecutor<FileStorage> {

    Optional<FileStorage> findByIdFile(String idFile);

    /**
     * OTIMIZADO: Query específica para verificação de duplicidade por hash.
     * Retorna apenas se existe um arquivo com o hash especificado e status STORED_SUCCESSFULLY.
     * Usa índice idx_file_storage_hash para performance.
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
           "FROM FileStorage f " +
           "WHERE f.application.id = :applicationId " +
           "AND f.hashFileBytes = :hash " +
           "AND f.fileStatusCode = 2") //2 = FileStorageStatusEnum.STORED_SUCCESSFULLY
    boolean existsByApplicationIdAndHashAndStatusStored(
            @Param("applicationId") Long applicationId, 
            @Param("hash") String hash);
    
    /**
     * OTIMIZADO: Query com JOIN FETCH para evitar N+1 problem.
     * Carrega FileStorage com suas relações em uma única query.
     */
    @Query("SELECT DISTINCT f FROM FileStorage f " +
           "LEFT JOIN FETCH f.application " +
           "LEFT JOIN FETCH f.listFileStorageClient " +
           "WHERE f.application.id = :applicationId " +
           "AND f.fileStatusCode IN :statusCodes")
    Page<FileStorage> findByApplicationIdWithRelations(
            @Param("applicationId") Long applicationId,
            @Param("statusCodes") List<Integer> statusCodes,
            Pageable pageable);

    @Query("SELECT COUNT(f) FROM FileStorage f " +
            "LEFT JOIN f.listFileStorageClient s " +
            "WHERE s.idServerStorageClient = :serverStorageClientId " +
            "AND f.fileStatusCode = 2") //2 = FileStorageStatusEnum.STORED_SUCCESSFULLY
    long countByIdServerStorageClient(
            @Param("serverStorageClientId") String serverStorageClientId);

    @Query("SELECT COUNT(f) FROM FileStorage f " +
            "LEFT JOIN f.application " +
            "WHERE f.application.id = :applicationId " +
            "AND f.fileStatusCode = 2") //2 = FileStorageStatusEnum.STORED_SUCCESSFULLY
    long countByIdApplication(
            @Param("applicationId") Long applicationId);
}