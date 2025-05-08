package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idFile; //Chave de identificação externa do arquivo.
    private String idClientStorage; //Identificador do Client Storage que está armazenado.
    private String applicationStorageFolder; //Estrutura de pasta da aplicação, início do armazenamento.

    private String fileLogicName;
    private String fileFisicalName;
    private String fileType;
    private int fileLength;
    @Transient
    private byte[] fileContent;
    private boolean compressFileContent; //Arquivo armazenado em ZIP compressão;
    private String textOcrFileContent;
    private String hashFileContent;
    private boolean hasFileSentForPurge; //Sinaliza que o arquivo foi enviado para expurgo.
    private LocalDateTime dateTimeFileStorage;
    private Integer fileStatusCode;

    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

}