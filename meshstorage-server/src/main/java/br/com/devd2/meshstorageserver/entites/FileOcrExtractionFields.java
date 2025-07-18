package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity @Table(name = "TB_FILE_OCR_EXTRACTION_FIELDS")
public class FileOcrExtractionFields {

    /**
     * Identificador único dos campos da extração do OCR.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FILE_OCR_EXTRACTION_FIELDS")
    private Long id;

    /**
     * Chave, nome do campo da extração do OCR do arquivo.
     */
    @Column(name = "NM_FIELD_EXTRACTION")
    private String keyField;

    /**
     * Conteúdo do campo da extração do OCR do arquivo.
     */
    @Column(name = "TX_CONTENT_FIELD_EXTRACTION")
    private String valueField;

    /**
     * Relacionamento com a extração OCR do arquivo.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_FILE_OCR_EXTRACTION")
    private FileOcrExtraction fileOcrExtraction;
}
