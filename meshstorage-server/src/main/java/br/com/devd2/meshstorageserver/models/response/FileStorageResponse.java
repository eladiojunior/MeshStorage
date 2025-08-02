package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileStorageResponse {
    /**
     * Identificador do arquivo externo, utilizado para recupera ou remover quando necessário.
     */
    private String idFile;
    /**
     * Nome lógico do arquivo, exatamente como o usuáiro informou.
     */
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
    private String fileFisicalName;

    /**
     * Tipo do arquivo (content Type), exemplo: application/pdf
     */
    private String fileContentType;

    /**
     * Tamnho em bytes do arquivo.
     */
    private long fileLength;

    /**
     * Hash dos bytes do arquivo.
     */
    private String hashFileBytes;

    /**
     * Indicador que o arquivo passou ou está passando pelo processo de extração de texto, via OCR;
     */
    private boolean extractionTextFileByOcr;

    /**
     * Indicador que o arquivo passou por uma compressão em ZIP antes do armazenamento;
     */
    private boolean compressedFileContent;

    /**
     * Data e hora do registro do arquivo no armazenamento.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeRegisteredFileStorage;

    /**
     * Data e hora da remoção do arquivo para do armazenamento.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeRemovedFileStorage;

    /**
     * Data e hora do envio do arquivo para o backup, armazenamento não online.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeBackupFileStorage;

    /**
     * Código da situação do arquivo:
     * {@link br.com.devd2.meshstorage.enums.FileStorageStatusEnum}
     */
    private Integer fileStatusCode;

    /**
     * Descrição da situação do arquivo:
     * {@link br.com.devd2.meshstorage.enums.FileStorageStatusEnum}
     */
    private String fileStatusDescription;

    /**
     * Informações do processo de extração de conteúdo texto do arquivo, via OCR, quando existir.
     */
    private FileOcrExtractionResponse fileExtractionByOcr;

    /**
     * Informações da compressão do arquivo, quando existir.
     */
    private FileStorageCompressedResponse fileCompressed;

}