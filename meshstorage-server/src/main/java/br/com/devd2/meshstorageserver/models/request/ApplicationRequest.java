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
    @Min(value = 1, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser MAIOR que 0 (zero).")
    @Min(value = 20, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser MENOR que 20 (vinte).")
    private Long maximumFileSizeMB;

    /**
     * Realizar a compressão dos arquivos antes de armazenar;
     */
    private boolean compressFileContent = false;

    /**
     * Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
     */
    private boolean applyOcrFileContent = false;

    /**
     * Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
     */
    private boolean allowDuplicateFile = true;

}