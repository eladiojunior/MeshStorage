package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QrCodeFileResponse {
    /**
     * Identificador do arquivo.
     */
    private String idFile;
    /**
     * Chave de acesso do arquivo diretamente pelo link.
     */
    private String tokenAccessFile;
    /**
     * URL com link de acesso ao arquivo (mesmo do QR Code).
     */
    private String linkAccessFile;
    /**
     * Bytes da imagem do QR Code de acesso ao arquivo.
     */
    private byte[] imageQrCodeAccessFile;
    /**
     * Data e hora do registro do QR Code de acesso ao arquivo.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeRegisteredAccessFile;
}