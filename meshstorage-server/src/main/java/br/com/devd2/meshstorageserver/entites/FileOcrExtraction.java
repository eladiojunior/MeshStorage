package br.com.devd2.meshstorageserver.entites;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity @Table(name = "TB_FILE_OCR_EXTRACTION")
public class FileOcrExtraction {

    /**
     * Identificador único da extração OCR.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_OCR_EXTRACTION")
    private Long id;

    /**
     * Conteúdo texto extraído do arquivo, após processamento OCR.
     */
    @Column(name = "TX_CONTENT_FILE_OCR")
    private String textFileOcr;

    /**
     * Hash do conteúdo do arquivo, após procesamento do ORC, para conferir duplicidade.
     */
    @Column(name = "TX_HASH_CONTENT_FILE_OCR")
    private String hashContentFile;

    /**
     * Tipo do documento identificado (automaticamente).
     */
    @Column(name = "NM_DOCUMENT_TYPE_FILE_OCR")
    private String documentType = "Documento não identificado";

    /**
     * Percentual do Grau de Confiança do Tipo do documento identificado (automaticamente).
     * Grau vai de 0.0 - 0.1, onde mais de 0.8 (80%) é um excelente grau de confiança.
     */
    @Column(name = "VL_DEGREE_CONFIDENCE_DOCUMENT_TYPE")
    private double degreeConfidenceDocumentType = 0.0;

    /**
     * Status do processo de extração de texto do arquivos, via OCR.
     * {@link ExtractionTextByOcrStatusEnum}
     */
    @Column(name = "CD_STATUS_EXTRACTION_TEXT_ORC_FILE")
    private Integer extractionTextByOrcStatusCode = ExtractionTextByOcrStatusEnum.NOT_EXTRACTIO.getCode();

    /**
     * Data e hora do inicio do processo de extração de texto, via OCR.
     */
    @Column(name = "DH_START_EXTRACTION_OCR")
    private LocalDateTime dateTimeStartExtraction;

    /**
     * Data e hora do final do processo de extração de texto, via OCR.
     */
    @Column(name = "DH_END_EXTRACTION_OCR")
    private LocalDateTime dateTimeEndExtraction;

    /**
     * Lista de campos (chave, valor) da extração do conteúdo texto, após processamento OCR.
     */
    @OneToMany
    private List<FileOcrExtractionFields> fieldsOcrExtraction;

}