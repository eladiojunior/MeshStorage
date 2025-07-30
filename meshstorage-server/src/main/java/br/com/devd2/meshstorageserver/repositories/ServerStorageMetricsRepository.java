package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.ServerStorageMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerStorageMetricsRepository extends JpaRepository<ServerStorageMetrics, Long> {
    Optional<ServerStorageMetrics> findByIdServerStorage(Long idServerStorage);
}