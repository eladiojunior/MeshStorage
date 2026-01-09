package br.com.devd2.meshstorageserver.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Sigla da aplicação obrigatória.")
    @Schema(description = "Cógigo/Sigla da aplicação", example = "APP01")
    private String applicationCode;

    @NotBlank(message = "Nome da aplicação obrigatório.")
    @Schema(description = "Nome da aplicação", example = "Nome da Aplicação")
    private String applicationName;

    @NotBlank(message = "Descrição da aplicação obrigatória.")
    @Schema(description = "Descrição da aplicação", example = "Descrição")
    private String applicationDescription;

    @Schema(description = "Tipo de arquivo aceitos pela aplicação", example = "application/pdf")
    private String[] allowedFileTypes = new String[] {"application/pdf", "image/jpeg", "image/gif"};

    @NotNull(message = "Tamanho (em MegaByte) máximo dos aquivos deve ser informado.")
    @Min(value = 1, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser MAIOR que 0 (zero).")
    @Max(value = 20, message = "Tamanho (em MegaByte) máximo dos aquivos deve ser MENOR ou igual a 20MB.")
    @Schema(description = "Tamanho máximo dos arquivos (em MegaBytes) aceitos pela aplicação", example = "5")
    private Long maximumFileSizeMB;

    /**
     * Realizar a compressão dos arquivos antes de armazenar;
     */
    @Schema(description = "Indicador para realizar a compressão dos arquivos antes de armazenar", example = "false")
    private boolean compressedFileContentToZip = false;

    /**
     * Realizar a compressão de arquivos de imagens (PNG, JPGE, GIF ou BMP) para o formato WebP;
     * Fonte: <a href="https://developers.google.com/speed/webp?hl=pt-br">webp</a>
     */
    @Schema(description = "Indicador para realizar a compressão de arquivos de imagens (PNG, JPGE, GIF ou BMP) para o formato WebP", example = "true")
    private boolean convertImageFileToWebp = true;

    /**
     * Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
     */
    @Schema(description = "Indicador para aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH", example = "false")
    private boolean applyOcrFileContent = false;

    /**
     * Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
     */
    @Schema(description = "Indicador para verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo", example = "true")
    private boolean allowDuplicateFile = true;

    /**
     * Verificar se aplicação requer replicação do arquivo em outro servidor de arquivos;
     * Para replicar será necessário ter mais de um ServerStorage (Client) ativo no sistrma.
     */
    @Schema(description = "Indicador para aplicar replicação do arquivo em outro servidor de arquivos", example = "false")
    private boolean requiresFileReplication = false;

}