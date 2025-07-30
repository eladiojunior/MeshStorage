package br.com.devd2.meshstorageserver.models.enums;

import lombok.Getter;

public enum ServerStorageStatusEnum {
    /** Storage Client ativo e disponivel para receber arquivos. */
    ACTIVE(1, "Ativo"),
    /** Storage Client inativo NÃO disponivel para receber arquivos. */
    INACTIVE(2, "Inativo"),
    /** Storage Client removido logicamente, não deve ter arquivos vinculados e não disponível para receber arquivos. */
    REMOVED(3, "Removido"),
    /** Storage Client pausado temporariamente e NÃO disponivel para receber arquivos, evento manual pelo usuario. */
    PAUSED(4, "Pausado");

    @Getter
    private final int code;
    @Getter
    private final String description;

    ServerStorageStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Recupera o value (description) do enum pelo code.
     * @param code - código do enum para recuperar a descrição.
     * @return Descrição do enum ou nulo;
     */
    public static String getValue(Integer code) {
        for (ServerStorageStatusEnum _enum : ServerStorageStatusEnum.values()) {
            if (_enum.code == code)
                return _enum.description;
        }
        return null;
    }

}
