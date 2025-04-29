package br.com.devd2.meshstorageserver.models.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Nome da aplicação é obrigatório.")
    private String applicationName;

    private String applicationDescription;

    private String[] allowedFileTypes = new String[] {"application/pdf", "image/jpeg", "image/gif"};

    @NotNull(message = "Tamanho (em MegaByte) máximo dos aquivos deve ser informado.")
    @Min(value = 1, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser maior que 0 (zero).")
    private Long maximumFileSizeMB;

    private boolean compressFileContent = false; //Realizar a compressão dos arquivos antes de armazenar;

    private boolean applyOcrFileContent = false; //Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;

    private boolean allowDuplicateFile = true; //Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;

}