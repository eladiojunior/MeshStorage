package br.com.devd2.meshstorageserver.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FileStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idFile; //Chave de identificação externa do arquivo.
    private String fileLogicName;
    private String fileFisicalName;
    private String fileType;
    private String serverStorage;
    private byte[] fileData;
    private Long fileLength;
    private String hashFileData;
    private LocalDateTime dateTimeFileStorage;
}
