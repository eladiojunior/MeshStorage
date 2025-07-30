package br.com.devd2.meshstorageserver.entites;

import br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

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
     * Score do Storage para classificação do melhor Storage para armazenar os arquivos.
     */
    @Transient
    private double scoreStorage;

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
     * Código da situação do ServerStorage, por Client:
     * {@link br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum}
     */
    @Column(name = "CD_STATUS_SERVER_STORAGE", nullable = false)
    private Integer serverStorageStatusCode;

    /**
     * Data e hora de registro do Server Storage, no envio da informação do Client e registro do Server Storage.
     */
    @Column(name = "DH_REGISTERED_SERVER_STORAGE")
    private LocalDateTime dateTimeRegisteredServerStorage;

    /**
     * Data e hora de remoção lógica do Server Storage, quando código da situação igual a 3=REMOVED.
     */
    @Column(name = "DH_REMOVED_SERVER_STORAGE")
    private LocalDateTime dateTimeRemovedServerStorage;

    /**
     * Relacionamento com a Métrica do Server Storages
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private ServerStorageMetrics metrics;

    /**
     * Verifica o código do status do Storage para identificar se ele está ativo.
     * @return true Ativo, false Inativo.
     */
    @Transient
    public boolean isAtive() {
        return serverStorageStatusCode != null &&
                Objects.equals(serverStorageStatusCode, ServerStorageStatusEnum.ACTIVE.getCode());
    }
}