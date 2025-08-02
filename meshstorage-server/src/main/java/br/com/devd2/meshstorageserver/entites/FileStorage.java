package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity @Table(name = "TB_FILE_STORAGE")
public class FileStorage {

    /**
     * Identificador único do arquivo no banco de dados.
     * Gerado automaticamente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_STORAGE")
    private Long id;

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    @Column(name = "CD_FILE_STORAGE_CLIENT")
    private String idFile; //Chave de identificação externa do arquivo.

    /**
     * Nome da estrutura de armazenamento do arquivo no Storage
     */
    @Column(name = "DS_APPLICATION_STORAGE_FOLDER")
    private String applicationStorageFolder; //Estrutura de pasta da aplicação, início do armazenamento.

    /**
     * Nome lógico do arquivo, exatamente como a aplicação recebe do usuário.
     */
    @Column(name = "NM_FILE_LOGIC")
    private String fileLogicName;

    /**
     * O nome físico do arquivo será utilizando no Storage para separar em pastas para controle.
     * -----
     * Exemplo: 20250813_8ebbea50-434a-4dbb-8456-aebd461e0ecc.png
     * {20250813} = Será utilizado para o nome da pasta no Storage;
     * {8ebbea50-434a-4dbb-8456-aebd461e0ecc} = UUID.randomUUID().toString().toUpperCase();
     * {.png} = Extensão do arquivo, recuperado do nome lógico e aplicado o toLowerCase();
     * Formando:
     * {20250813_8ebbea50-434a-4dbb-8456-aebd461e0ecc.png} = Nome do arquivo fisico para armazenamento;
     */
    @Column(name = "NM_FILE_FISICAL")
    private String fileFisicalName;

    /**
     * Tipo do arquivo (content Type), exemplo: application/pdf
     */
    @Column(name = "DS_FILE_CONTENT_TYPE")
    private String fileContentType;

    /**
     * Tamnho em bytes do arquivo.
     */
    @Column(name = "QT_FILE_LENGTH")
    private int fileLength;

    /**
     * Dados (bytes) do arquivo quando recuperado do armazenamento.
     * Essa informação não será mantida em banco de dados.
     */
    @Transient
    private byte[] fileContent;

    /**
     * Hash dos bytes do arquivo.
     */
    @Column(name = "TX_HASH_FILE_BYTES")
    private String hashFileBytes;

    /**
     * Indicador que o arquivo passou por uma compressão em ZIP ou WEBP antes do armazenamento;
     */
    @Column(name = "IS_COMPRESSED_FILE_CONTENT")
    private boolean compressedFileContent;

    /**
     * Caso seja aplicação esteja com a extração do OCR dos arquivos ativo, será setado como 'true';
     */
    @Column(name = "IS_EXTRACTION_TEXT_OCR_FILE")
    private boolean extractionTextFileByOcr;

    /**
     * Código da situação do arquivo:
     * {@link br.com.devd2.meshstorage.enums.FileStorageStatusEnum}
     */
    @Column(name = "CD_STATUS_FILE_STORAGE")
    private Integer fileStatusCode;

    /**
     * Data e hora do registro do arquivo no armazenamento.
     */
    @Column(name = "DH_REGISTERED_FILE_STORAGE")
    private LocalDateTime dateTimeRegisteredFileStorage;

    /**
     * Data e hora da remoção do arquivo para do armazenamento.
     */
    @Column(name = "DH_REMOVED_FILE_STORAGE")
    private LocalDateTime dateTimeRemovedFileStorage;

    /**
     * Data e hora do envio do arquivo para o backup, armazenamento não online.
     */
    @Column(name = "DH_BACKUP_FILE_STORAGE")
    private LocalDateTime dateTimeBackupFileStorage;

    /**
     * Relacionamento com uma aplicação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_APPLICATION")
    private Application application;

    /**
     * Relacionamento com os Server Storages
     */
    @OneToMany(mappedBy = "fileStorage", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<FileStorageClient> listFileStorageClient = new ArrayList<>();
    public void addFileStorageClient(FileStorageClient client) {
        listFileStorageClient.add(client);
        client.setFileStorage(this);
    }

    /**
     * Relacionamento com a estrutura de compressão do arquivo, se houver.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_FILE_STORAGE_COMPRESSED")
    private FileStorageCompressed fileCompressed;

    /**
     * Relacionamento com a estrutura de extração, via OCR, se houver.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_FILE_OCR_EXTRACTION")
    private FileOcrExtraction fileExtractionByOcr;


}