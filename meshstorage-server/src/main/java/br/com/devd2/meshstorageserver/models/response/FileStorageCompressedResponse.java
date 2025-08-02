package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileStorageCompressedResponse {

    /**
     * Tamnho em bytes do arquivo apos a compressão (ZIP ou WEBP).
     */
    private int compressedFileLength;

    /**
     * Tipo do arquivo (content Type), exemplo: image/webp
     */
    private String compressedFileContentType;

    /**
     * Resultado da compressão do arquivo Content Type >> (ZIP ou WEBP);
     */
    private String compressedFileInformation;

    /**
     * Hash dos bytes do arquivo comprimido.
     */
    private String compressedHashFileBytes;

    /**
     * Percentual de compressão do arquivo.
     */
    private double percentualCompressedFile;


}