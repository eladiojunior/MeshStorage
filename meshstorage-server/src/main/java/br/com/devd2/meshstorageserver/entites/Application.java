package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String applicationName;
    private String applicationDescription;
    private Long maximumFileSize;      // em MB
    private String allowedFileTypes;   // contentTypes permitidos, separado por ponto-virgula
    private LocalDateTime dateTimeApplication;
}