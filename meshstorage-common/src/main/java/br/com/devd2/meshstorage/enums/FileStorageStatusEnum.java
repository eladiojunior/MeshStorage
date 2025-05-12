package br.com.devd2.meshstorage.enums;

import lombok.Getter;

public enum FileStorageStatusEnum {
    SENT_TO_STORAGE(1, "Arquivo foi enviado para armazenamento"),
    STORED_SUCCESSFULLY(2, "Arquivo armazenado com sucesso"),
    SENT_TO_ARCHIVED(3, "Arquivo enviado para backup (armazenamento de longo prazo)"),
    ARCHIVED_SUCESSFULLY(4, "Arquivo em backup com sucesso"),
    DELETED_SUCCESSFULLY(5, "Arquivo removido com sucesso"),
    STORAGE_FAILED(9, "Falha no processamento do arquivo");

    @Getter
    private final int code;
    @Getter
    private final String description;

    FileStorageStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
