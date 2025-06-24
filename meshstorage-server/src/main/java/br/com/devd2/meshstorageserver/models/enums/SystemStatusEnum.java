package br.com.devd2.meshstorageserver.models.enums;

import lombok.Getter;

@Getter
public enum SystemStatusEnum {
    //'healthy' ? 'success' : systemHealth === 'warning' ? 'warning' : 'danger'}
    HEALTHY("healthy", "Saudável"),
    WARNING("warning", "Atenção"),
    ERROR("error", "Erro");

    private final String status;
    private final String description;

    // Construtor para inicializar o valor do status
    SystemStatusEnum(String status, String description) {
        this.status = status;
        this.description = description;
    }

}
