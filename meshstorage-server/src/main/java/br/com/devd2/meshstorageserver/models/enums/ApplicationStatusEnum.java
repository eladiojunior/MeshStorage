package br.com.devd2.meshstorageserver.models.enums;

import lombok.Getter;

public enum ApplicationStatusEnum {
    /** Aplicação operacional – uploads e downloads permitidos. */
    ACTIVE(1, "Active"),

    /** Aplicação desativada (freeze). Mantém histórico, mas não aceita novos uploads. */
    INACTIVE(2, "Inactive"),

    /** Remoção lógica – permanece no banco apenas para integridade referencial. */
    REMOVED(3, "Removed");

    @Getter
    private final int code;
    @Getter
    private final String description;

    ApplicationStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** Útil em regras de negócio: */
    public boolean isActive()   { return this == ACTIVE; }
    public boolean isRemoved()  { return this == REMOVED; }
    public boolean isInactive()  { return this == INACTIVE; }
}
