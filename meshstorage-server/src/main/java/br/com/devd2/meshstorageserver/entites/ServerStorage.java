package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ServerStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idClient; //Idenificador do Client HASH (ServerName + StorageName)
    private String serverName;
    private String storageName;
    private String ipServer;
    private Long totalSpace;  // em MB
    private Long freeSpace;   // em MB
    private boolean available;
    private LocalDateTime dateTimeAvailable;
    private LocalDateTime dateTimeServerStorage;
}