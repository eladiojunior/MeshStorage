package br.com.devd2.meshstorageserver.models;

import lombok.Data;

@Data
public class UserAccessModel {

    public UserAccessModel(String userName, String ipUser, String userAgent, String accessChanel) {
        this.userName = userName;
        this.ipUser = ipUser;
        this.userAgent = userAgent;
        this.accessChanel = accessChanel;
    }

    /**
     * Nome do usuário que está acessando.
     */
    private String userName;

    /**
     * Identificador da máquina que está acessando.
     */
    private String ipUser;

    /**
     * Informações do agente de usuário que está acessando.
     */
    private String userAgent;

    /**
     * Canal de acesso do usuário, Site, Mobile, Chat.
     */
    private String accessChanel;
}