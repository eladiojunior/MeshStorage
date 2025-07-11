package br.com.devd2.meshstorage.enums;

import lombok.Getter;

public enum ExtractionTextByOcrStatusEnum {
    NOT_EXTRACTIO(0, "Sem extração OCR configurado"),
    IN_PROCESSING(1, "Em processamento"),
    PROCESSED(2, "Processado"),
    EXTRACTION_ERROR(3, "Erro extração");
    @Getter
    private final int code;
    @Getter
    private final String description;

    ExtractionTextByOcrStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
