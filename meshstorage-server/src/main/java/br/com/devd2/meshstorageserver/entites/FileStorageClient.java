package br.com.devd2.meshstorageserver.entites;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "TB_FILE_STORAGE_CLIENT")
public class FileStorageClient {

    /**
     * Identificador único do arquivo no banco de dados.
     * Gerado automaticamente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_STORAGE_CLIENT")
    private Long id;

    /**
     * Identificador do Storage para recuperação do arquivo.
     */
    @Column(name = "ID_SERVER_STORAGE_CLIENT")
    private String idServerStorageClient; //Identificador do Client Storage que está armazenado.

    /**
     * Relacionamento com uma aplicação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FILE_STORAGE")
    private FileStorage fileStorage;

}