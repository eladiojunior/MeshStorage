package br.com.devd2.meshstorage.enums;

import lombok.Getter;

public enum FileStorageStatusEnum {
    SENT_TO_STORAGE(1, "Arquivo foi enviado para armazenamento"),
    STORED_SUCCESSFULLY(2, "Arquivo armazenado com sucesso"),
    SENT_TO_ARCHIVED(3, "Arquivo movido para armazenamento de longo prazo"),
    STORAGE_FAILED(9, "Falha no armazenamento do arquivo");

    @Getter
    private final int code;
    @Getter
    private final String description;

    FileStorageStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
