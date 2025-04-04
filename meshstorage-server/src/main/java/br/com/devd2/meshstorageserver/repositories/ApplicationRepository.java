package br.com.devd2.meshstorageserver.repositories;

import br.com.devd2.meshstorageserver.entites.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByApplicationName(String applicationName);
}