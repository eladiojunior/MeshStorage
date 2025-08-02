package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity @Table(name = "TB_FILE_STORAGE_COMPRESSED")
public class FileStorageCompressed {

    /**
     * Identificador único do arquivo comprimido no banco de dados.
     * Gerado automaticamente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_STORAGE_COMPRESSED")
    private Long id;

    /**
     * Tamanho em bytes do arquivo apos a compressão (ZIP ou WEBP).
     */
    @Column(name = "QT_COMPRESSED_FILE_LENGTH")
    private int compressedFileLength;

    /**
     * Tipo do arquivo (content Type) após a compressão (ZIP ou WEBP), exemplo: image/webp
     */
    @Column(name = "DS_COMPRESSED_FILE_CONTENT_TYPE")
    private String compressedFileContentType;

    /**
     * Resultado da compressão do arquivo Content Type >> (ZIP ou WEBP);
     */
    @Column(name = "DS_COMPRESSED_FILE_INFORMATION")
    private String compressionFileInformation;

    /**
     * Hash dos bytes do arquivo quando comprimido.
     */
    @Column(name = "TX_COMPRESSED_HASH_FILE_BYTES")
    private String compressedHashFileBytes;

    /**
     * Percentual de compressão do arquivo.
     */
    @Column(name = "VL_COMPRESSED_FILE_CONTENT")
    private double percentualCompressedFileContent;

}