package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FileStorageAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    private String idFile; //Chave de identificação externa do arquivo.

    /**
     * Token de acesso ao arquivo.
     */
    private String accessToken;

    /**
     * Tempo de expiração do token de acesso, em minutos, se 0 (zero) nunca expira.
     */
    private long tokenExpirationTime;

    /**
     * Quantidade de acesso máxima ao arquivo com o token, se 0 (zero) não tem limite.
     */
    private int maximumAccessesToken;

    /**
     * Data e hora do registro do token de acesso ao arquivo.
     */
    private LocalDateTime dateTimeRegistered;

    /**
     * Data e hora do último acesso por token ao arquivo.
     */
    private LocalDateTime dateTimeLastAccess;

    /**
     * Relacionamento com um arquivo do armazenamento.
     */
    @ManyToOne
    @JoinColumn(name = "fileStorageId")
    private FileStorage fileStorage;

}