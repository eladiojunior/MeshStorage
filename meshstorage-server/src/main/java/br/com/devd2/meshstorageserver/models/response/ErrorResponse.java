package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class ErrorResponse {
    private int codeError;
    private String messageError;
    public ErrorResponse(int code, String message) {
        this.codeError = code;
        this.messageError = message;
    }
}