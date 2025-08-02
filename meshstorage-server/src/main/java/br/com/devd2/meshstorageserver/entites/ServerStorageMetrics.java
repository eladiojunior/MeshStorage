package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "TB_SERVER_STORAGE_METRICS")
public class ServerStorageMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVER_STORAGE_METRICS")
    private Long id;

    /**
     * Tamanho total do espaço de armazenamento do Storage enviado na configuração, execução do agent no Cliente.
     */
    @Column(name = "QT_TOTAL_SPACE_STORAGE")
    private Long totalSpace;  // em MB

    /**
     * Espaço disponível de armazenamento do Storage enviado na configuração, execução do agent no Cliente.
     * Atualizado sempre por um processo no Client, enviando a informação para o servidor.
     */
    @Column(name = "QT_FREE_SPACE_STORAGE")
    private Long freeSpace;   // em MB (Megabytes)

    /**
     * Total de arquivos no Storage, sempre que um arquivo é enviado e confirmado esse quantitativo é atualizado.
     */
    @Column(name = "QT_TOTAL_FILES_STORAGE")
    private Long totalFiles;  // Quantidade de arquivos

    /**
     * Valor do tempo de resposta (em miléssimo de segundo) no envio do arquivos para Storage.
     */
    @Column(name = "VL_RESPONSE_TIME_STORAGE")
    private Long responseTime;  // em Ms (Miléssimo de segundo)

    /**
     * Quantidade de requisições no último minuto para de envio de arquivos para Storage.
     */
    @Column(name = "QT_REQUEST_LAST_MINUTE_STORAGE")
    private Integer requestLastMinute;

    /**
     * Data e hora da última requisição enviada ao Server Storage Client.
     */
    @Column(name = "DH_LAST_REQUEST_STORAGE")
    private LocalDateTime dateTimeLastRequest;

    /**
     * Quantidade de requisições no último minuto para de envio de arquivos para Storage.
     */
    @Column(name = "QT_ERRORS_LAST_REQUEST_STORAGE")
    private Integer errosLastRequest;

    /**
     * Data e hora da última atualização das métricas do Storage Cliente.
     */
    @Column(name = "DH_LAST_AVAILABLE_STORAGE")
    private LocalDateTime dateTimeLastAvailable;

}