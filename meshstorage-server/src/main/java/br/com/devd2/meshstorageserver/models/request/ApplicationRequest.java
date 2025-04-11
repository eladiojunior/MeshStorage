package br.com.devd2.meshstorageserver.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Nome da aplicação é obrigatório.")
    private String applicationName;

    private String applicationDescription;

    @NotBlank(message = "Tipos de arquivos (ContentTypes) permitidos é obrigatório.")
    private String[] allowedFileTypes;

    @NotNull(message = "Tamanho (em MegaByte) máximo dos aquivos deve ser informado.")
    @Min(value = 1, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser maior que 0 (zero).")
    private Long maximumFileSize;

}