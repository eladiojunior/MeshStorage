package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QrCodeFileResponse {
    private String idFile;
    /**
     * URL com link de acesso ao arquivo (mesmo do QR Code).
     */
    private String linkAcessFile;
    /**
     * Bytes da imagem do QR Code de acesso ao arquivo.
     */
    private byte[] imageQrCodeAcessFile;
    /**
     * Data e hora do registro do QR Code de acesso ao arquivo.
     */
    private LocalDateTime dateTimeRegisteredFileStorage;
}