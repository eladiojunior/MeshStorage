package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "TB_FILE_STORAGE_ACCESS_TOKEN")
public class FileAccessToken {

    /**
     * Identificador único do Access Token em banco de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_STORAGE_ACCESS_TOKEN")
    private Long id;

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    @Column(name = "CD_FILE_STORAGE_CLIENT")
    private String idFile; //Chave de identificação externa do arquivo.

    /**
     * Token de acesso ao arquivo.
     */
    @Column(name = "CD_ACCESS_TOKEN_FILE")
    private String accessToken;

    /**
     * Tempo de expiração do token de acesso, em minutos, se 0 (zero) nunca expira.
     */
    @Column(name = "QT_TIME_EXPIRATION_TOKEN")
    private long tokenExpirationTime;

    /**
     * Quantidade de acesso máxima ao arquivo com o token, se 0 (zero) não tem limite.
     */
    @Column(name = "QT_MAXIMUM_ACCESSES_TOKEN")
    private int maximumAccessesToken;

    /**
     * Data e hora do registro do token de acesso ao arquivo.
     */
    @Column(name = "DH_REGISTERED_TOKEN")
    private LocalDateTime dateTimeRegistered;

    /**
     * Data e hora do último acesso por token ao arquivo.
     */
    @Column(name = "DH_LAST_ACCESS_TOKEN")
    private LocalDateTime dateTimeLastAccess;

    /**
     * Relacionamento com um arquivo do armazenamento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FILE_STORAGE")
    private FileStorage fileStorage;

}