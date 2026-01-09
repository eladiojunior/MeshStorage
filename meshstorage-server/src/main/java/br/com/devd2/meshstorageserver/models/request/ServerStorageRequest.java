package br.com.devd2.meshstorageserver.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ServerStorageRequest {

    @NotBlank(message = "Nome do servidor é obrigatório.")
    @Schema(description = "Nome do servidor de armazenamento", example = "NomeServer")
    private String serveName;

    @NotBlank(message = "Endereço IP é obrigatório.")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$",
            message = "Endereço IP inválido.")
    @Schema(description = "Endereço IP do servidor de armazenamento", example = "0.0.0.0")
    private String ipServer;

    @NotBlank(message = "Nome do armazenamento é obrigatório.")
    @Schema(description = "Nome do local de armazenamento no servidor de file server", example = "NomeStorage")
    private String storageName;

    @NotNull(message = "Espaço total do armazenamento deve ser informado.")
    @Min(value = 1, message = "Espaço total do armazenamento deve ser maior que 0 (zero).")
    @Schema(description = "Total do espaço de armazenamento em bytes", example = "0")
    private Long totalSpace;

}