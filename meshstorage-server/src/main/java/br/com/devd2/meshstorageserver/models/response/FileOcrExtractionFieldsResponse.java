package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class FileOcrExtractionFieldsResponse {

    /**
     * Chave, nome do campo da extração do OCR do arquivo.
     */
    private String keyField;

    /**
     * Conteúdo do campo da extração do OCR do arquivo.
     */
    private String valueField;
}