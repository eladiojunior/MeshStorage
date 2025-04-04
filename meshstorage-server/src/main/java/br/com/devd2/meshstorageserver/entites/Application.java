package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String applicationName; //Nome simples sem espaços;
    private String applicationDescription; //Descrição da aplicação;
    private String allowedFileTypes; //Separar os tipos permitidos por ";"
    private long maximumFileSizeMB; //Em MegaBytes
    private boolean compressFileContent; //Realizar a compressão dos arquivos antes de armazenar;
    private boolean applyOcrFileContent; //Aplicar OCR em arquivos de Imagem/PDF para indexação de conteúdo e HASH;
    private boolean allowDuplicateFile; //Verificar se permite duplicidade de conteúdo ou hash em bytes do arquivo;
    private LocalDateTime dateTimeApplication;
}