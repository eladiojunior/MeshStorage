package br.com.devd2.meshstorageserver.models.response;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
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
     * Hash do conteúdo do arquivo extraido por OCR, se configurado.
     */
    private String hashFileContentByOcr;

    /**
     * Caso seja aplicação esteja com a extração do OCR dos arquivos ativo, será setado como 'true';
     */
    private boolean extractionTextByOrcFormFile;

    /**
     * Status do processo de extração de texto do arquivos, via OCR.
     * {@link ExtractionTextByOcrStatusEnum}
     */
    private Integer extractionTextByOrcFormFileStatus = ExtractionTextByOcrStatusEnum.IN_PROCESSING.getCode();

    /**
     * Indicador que o arquivo passou por uma compressão em ZIP antes do armazenamento;
     */
    private boolean compressedFileContent;

    /**
     * Tamnho em bytes do arquivo apos a compressão (ZIP ou WEBP).
     */
    private int compressedFileLength;

    /**
     * Resultado da compressão do arquivo Content Type >> (ZIP ou WEBP);
     */
    private String fileCompressionInformation;

    /**
     * Data e hora do registro do arquivo no armazenamento.
     */
    private LocalDateTime dateTimeRegisteredFileStorage;

    /**
     * Data e hora da remoção do arquivo para do armazenamento.
     */
    private LocalDateTime dateTimeRemovedFileStorage;

    /**
     * Data e hora do envio do arquivo para o backup, armazenamento não online.
     */
    private LocalDateTime dateTimeBackupFileStorage;

    /**
     * Código da situação do arquivo:
     * {@link br.com.devd2.meshstorage.enums.FileStorageStatusEnum}
     */
    private Integer fileStatusCode;

}