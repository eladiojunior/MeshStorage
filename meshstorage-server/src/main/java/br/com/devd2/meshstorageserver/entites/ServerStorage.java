package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "TB_SERVER_STORAGE")
public class ServerStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVER_STORAGE")
    private Long id;

    /**
     * Idenficiador único do Client no Server Storage.
     */
    @Column(name = "ID_SERVER_STORAGE_CLIENT")
    private String idServerStorageClient;

    /**
     * Nome do Server Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "NM_SERVER_STORAGE")
    private String serverName;

    /**
     * Nome do Storage Client dentro do Server Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "NM_STORAGE_CLIENT")
    private String storageName;

    /**
     * IP da máquina do Server Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "CD_IP_SERVER_STORAGE")
    private String ipServer;

    /**
     * Descrição do Sistema Operacional da máquina do Server Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "DS_OS_SERVER_STORAGE")
    private String osServer;

    /**
     * Tamanho total do espaço de armazenamento do Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "QT_TOTAL_SPACE_STORAGE_CLIENT")
    private Long totalSpace;  // em MB

    /**
     * Espaço disponível de armazenamento do Storage enviado na configuração, execução do agent no Cliente.
     * Atualizado sempre por um processo no Client, enviando a informação para o servidor.
     */
    @Column(name = "QT_FREE_SPACE_STORAGE_CLIENT")
    private Long freeSpace;   // em MB

    /**
     * Total de arquivos no Storage, sempre que um arquivo é enviado e confirmado esse quantitativo é atualizado.
     */
    @Column(name = "QT_TOTAL_FILES_STORAGE_CLIENT")
    private Long totalFiles;  // Quantidade

    /**
     * Indicador de disponibilidade do Storage para receber aquivos.
     * Atualizado sempre por um processo no Client, enviando a informação para o servidor.
     */
    @Column(name = "IS_AVAILABLE_STORAGE_CLIENT")
    private boolean available;

    /**
     * Data e hora da última atualização da disponibilidade do Storage para receber arquivos.
     * Atualizado sempre por um processo no Client, enviando a informação para o servidor.
     */
    @Column(name = "DH_AVAILABLE_STORAGE_CLIENT")
    private LocalDateTime dateTimeAvailable;

    /**
     * Data e hora de registro do Server Storage, no envio da informação do Client e registro do Server Storage.
     */
    @Column(name = "DH_REGISTERED_SERVER_STORAGE")
    private LocalDateTime dateTimeRegisteredServerStorage;

}