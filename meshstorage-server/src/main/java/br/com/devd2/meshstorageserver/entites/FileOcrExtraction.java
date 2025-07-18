package br.com.devd2.meshstorageserver.entites;

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
     * Data e hora do registro de extração de texto, via OCR.
     */
    @Column(name = "DH_REGISTERED_EXTRACTION_OCR")
    private LocalDateTime dateTimeRegisteredExtraction;

    /**
     * Lista de campos (chave, valor) da extração do conteúdo texto, após processamento OCR.
     */
    @OneToMany
    private List<FileOcrExtractionFields> fieldsOcrExtraction;

    /**
     * Relacionamento do arquivo ao processo de extração.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FILE_STORAGE")
    private FileStorage fileStorage;

}