package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FileStorageLogAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do usuário que está acessando o arquivo.
     * Caso seja um acesso de token será o chave do token;
     */
    private String userName;

    /**
     * Identificador da máquina que está acessando o arquivo.
     */
    private String ipUser;

    /**
     * Informações do agente de usuário que está acessando o arquivo.
     */
    private String userAgent;

    /**
     * Data e hora do registro de acesso ao arquivo no armazenamento.
     */
    private LocalDateTime dateTimeRegisteredAccess;

    /**
     * Canal de acesso do usuário, Site, Mobile, Chat.
     */
    private String accessChanel;

    /**
     * Relacionamento com um arquivo do armazenamento.
     */
    @ManyToOne
    @JoinColumn(name = "fileStorageId")
    private FileStorage fileStorage;

}