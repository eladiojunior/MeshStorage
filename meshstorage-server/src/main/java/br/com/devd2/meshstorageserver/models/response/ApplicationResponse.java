package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String applicationName;
    private String applicationDescription;
    private long maximumFileSize;
    private String[] allowedFileTypes;
    private boolean compressFileContent; //Realizar a compressão dos arquivos antes de armazenar;
    private boolean applyOcrFileContent; //Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
    private boolean allowDuplicateFile; //Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeApplication;
}