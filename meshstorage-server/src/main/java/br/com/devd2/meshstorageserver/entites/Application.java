package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TB_APPLICATION")
public class Application {

    /**
     * Identificador único da aplicação no banco de dados.
     * Gerado automaticamente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APPLICATION", nullable = false)
    private Long id;

    /**
     * Sigla da aplicação sem espaços, máximo 10 caracteres
     */
    @Column(name = "CD_APPLICATION", length = 10, nullable = false)
    private String applicationCode;

    /**
     * Nome da aplicação, máximo 50 caracteres
     */
    @Column(name = "NM_APPLICATION", length = 50, nullable = false)
    private String applicationName;

    /**
     * Descrição da aplicação.
     */
    @Column(name = "DS_APPLICATION", length = 500, nullable = false)
    private String applicationDescription;
    /**
     * Tipos de arquivos (content types) permitidos na aplicaçãp, conteúdo separado por ";"
     */
    @Column(name = "TX_ALLOWED_FILE_TYPES", nullable = false)
    private String allowedFileTypes;
    /**
     * Tamanho máximo em MB (Megabytes) do arquivo para na aplicação.
     */
    @Column(name = "QT_MAXIMUM_FILE_SIZE_MB", nullable = false)
    private Long maximumFileSizeMB;
    /**
     * Realizar a compressão dos arquivos antes de armazenar para ZIP os passíveis;
     */
    @Column(name = "IS_COMPRESSED_FILE_CONTENT_TOZIP", nullable = false)
    private boolean compressedFileContentToZip;
    /**
     * Realizar a compressão de arquivos de imagens (PNG, JPGE, GIF ou BMP) para o formato WebP;
     */
    @Column(name = "IS_CONVERT_IMAGE_FILE_TOWEBP", nullable = false)
    private boolean convertImageFileToWebp;
    /**
     * Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
     */
    @Column(name = "IS_APPLY_OCR_FILE_CONTENT", nullable = false)
    private boolean applyOcrFileContent;
    /**
     * Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
     */
    @Column(name = "IS_ALLOW_DUPLICATE_FILE", nullable = false)
    private boolean allowDuplicateFile;
    /**
     * Verificar se aplicação requer replicação do arquivo em outro servidor de arquivos;
     * Para replicar será necessário ter mais de um ServerStorage (Client) ativo no sistema.
     */
    @Column(name = "IS_REQUIRES_FILE_REPLICATION", nullable = false)
    private boolean requiresFileReplication;
    /**
     * Quantidade total de arquivos armazenados na aplicação.
     */
    @Column(name = "QT_TOTAL_FILES_APPLICATION", nullable = false)
    private Long totalFiles;
    /**
     * Código da situação do aplicação:
     * {@link br.com.devd2.meshstorageserver.models.enums.ApplicationStatusEnum}
     */
    @Column(name = "CD_STATUS_APPLICATION", nullable = false)
    private Integer applicationStatusCode;
    /**
     * Data e hora de registro da aplicação.
     */
    @Column(name = "DH_REGISTERED_APPLICATION", nullable = false)
    private LocalDateTime dateTimeRegisteredApplication;
    /**
     * Data e hora da remoção da aplicação (logicamente).
     */
    @Column(name = "DH_REMOVED_APPLICATION")
    private LocalDateTime dateTimeRemovedApplication;
}