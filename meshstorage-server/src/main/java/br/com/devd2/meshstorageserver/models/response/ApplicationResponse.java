package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String applicationName;
    private String applicationCode;
    private String applicationDescription;
    private long maximumFileSize;
    private String[] allowedFileTypes;
    private boolean compressedFileContentToZip;     //Realizar a compressão dos arquivos antes de armazenar para ZIP os passíveis;
    private boolean convertImageFileToWebp;         //Realizar a compressão de arquivos de imagens (PNG, JPGE, GIF ou BMP) para o formato WebP;
    private boolean applyOcrFileContent;            //Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
    private boolean allowDuplicateFile;             //Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
    /**
     * Verificar se aplicação requer replicação do arquivo em outro servidor de arquivos;
     * Para replicar será necessário ter mais de um ServerStorage (Client) ativo no sistema.
     */
    private boolean requiresFileReplication;
    private Long totalFiles;                        //Quantidade de arquivos armazenados da aplicação.
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeApplication;
}