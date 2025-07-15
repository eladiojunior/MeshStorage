package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OcrFileExtractionFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chave, nome do campo.
     */
    private String keyField;

    /**
     * Valor, conteúdo do campo.
     */
    private String valueField;

}
