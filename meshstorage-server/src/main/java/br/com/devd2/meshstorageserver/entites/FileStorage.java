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

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    private String idFile; //Chave de identificação externa do arquivo.

    /**
     * Identificador do Storage para recuperação do arquivo.
     */
    private String idClientStorage; //Identificador do Client Storage que está armazenado.
    /**
     * Nome da estrutura de armazenamento do arquivo no Storage
     */
    private String applicationStorageFolder; //Estrutura de pasta da aplicação, início do armazenamento.

    /**
     * Nome lógico do arquivo, exatamente como a aplicação recebe do usuário.
     */
    private String fileLogicName;

    /**
     * O nome físico do arquivo será utilizando no Storage para separar em pastas para controle.
     * -----
     * Exemplo: 20250813_8ebbea50-434a-4dbb-8456-aebd461e0ecc.png
     * {20250813} = Será utilizado para o nome da pasta no Storage;
     * {8ebbea50-434a-4dbb-8456-aebd461e0ecc} = UUID.randomUUID().toString().toUpperCase();
     * {.png} = Extensão do arquivo, recuperado do nome lógico e aplicado o toLowerCase();
     * Formando:
     * {20250813_8ebbea50-434a-4dbb-8456-aebd461e0ecc.png} = Nome do arquivo fisico para armazenamento;
     */
    private String fileFisicalName;

    /**
     * Tipo do arquivo (content Type), exemplo: application/pdf
     */
    private String fileContentType;

    /**
     * Tamnho em bytes do arquivo.
     */
    private int fileLength;

    /**
     * Dados (bytes) do arquivo quando recuperado do armazenamento.
     * Essa informação não será mantida em banco de dados.
     */
    @Transient
    private byte[] fileContent;

    /**
     * Indicador que o arquivo passou por uma compressão em ZIP antes do armazenamento;
     */
    private boolean compressedFileContent;

    /**
     * Tamnho em bytes do arquivo apos a compressão (ZIP ou WEBP).
     */
    private int compressedFileLength;

    /**
     * Resultado da compressão do arquivo Content Type >> (ZIP ou WEBP);
     */
    private String fileCompressionInformation;

    /**
     * Caso seja aplicado o ORC no arquivo será as informações do resultado do OCR
     * para indexação do conteúdo do arquivo.
     */
    private String textOcrFileContent;

    /**
     * Hash do conteúdo do arquivo, bytes, para comparação de duplicidade.
     */
    private String hashFileContent;

    /**
     * Data e hora do registro do arquivo no armazenamento.
     */
    private LocalDateTime dateTimeRegisteredFileStorage;

    /**
     * Data e hora da remoção do arquivo para do armazenamento.
     */
    private LocalDateTime dateTimeRemovedFileStorage;

    /**
     * Data e hora do envio do arquivo para o backup, armazenamento não online.
     */
    private LocalDateTime dateTimeBackupFileStorage;

    /**
     * Código da situação do arquivo:
     * {@link br.com.devd2.meshstorage.enums.FileStorageStatusEnum}
     */
    private Integer fileStatusCode;

    /**
     * Relacionamento com uma aplicação para controle.
     */
    @ManyToOne
    @JoinColumn(name = "applicationId")
    private Application application;

}