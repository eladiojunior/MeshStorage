package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "TB_FILE_STORAGE_LOG_ACCESS")
public class FileLogAccess {

    /**
     * Identificar único do log de acesso ao arquivo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_STORAGE_LOG_ACCESS")
    private Long id;

    /**
     * Nome do usuário que está acessando o arquivo.
     * Caso seja um acesso de token será o chave do token;
     */
    @Column(name = "NM_USER_LOG_ACCESS")
    private String userName;

    /**
     * Identificador da máquina que está acessando o arquivo.
     */
    @Column(name = "CD_IP_USER_LOG_ACCESS")
    private String ipUser;

    /**
     * Informações do agente de usuário que está acessando o arquivo.
     */
    @Column(name = "DS_USER_AGENT_LOG_ACCESS")
    private String userAgent;

    /**
     * Canal de acesso do usuário, Site, Mobile, Chat.
     */
    @Column(name = "DS_CHANEL_USER_LOG_ACCESS")
    private String accessChanel;

    /**
     * Data e hora do registro de acesso ao arquivo no armazenamento.
     */
    @Column(name = "DH_REGISTERED_LOG_ACCESS")
    private LocalDateTime dateTimeRegisteredAccess;

    /**
     * Relacionamento com um arquivo do armazenamento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FILE_STORAGE")
    private FileStorage fileStorage;

}