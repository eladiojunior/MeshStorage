package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class SuccessResponse {
    private String messageSuccess;
    public SuccessResponse(String message) {
        this.messageSuccess = message;
    }
}