package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class OcrFileExtraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash dos bytes do aquivo para conferir duplicidade.
     */
    private String hashFileBytes;

    /**
     * Conteúdo texto extraído do arquivo, após processamento OCR.
     */
    private String textFileOcr;

    /**
     * Lista de campos (chave, valor) da extração do conteúdo texto, após processamento OCR.
     */
    @OneToMany
    private List<OcrFileExtractionFields> contentFieldsOcr;

    /**
     * Tipo do documento identificado (automaticamente).
     */
    private String documentType = "Documento não identificado";

    /**
     * Percentual do Grau de Confiança do Tipo do documento identificado (automaticamente).
     * Grau vai de 0.0 - 0.1, onde mais de 0.8 (80%) é um excelente grau de confiança.
     */
    private double degreeConfidenceDocumentType = 0.0;

    /**
     * Data e hora do registro de extração de texto, via OCR.
     */
    private LocalDateTime dateTimeRegisteredExtraction;

    /**
     * Relacionamento com um arquivo do armazenamento.
     */
    @ManyToOne
    @JoinColumn(name = "fileStorageId")
    private FileStorage fileStorage;

}