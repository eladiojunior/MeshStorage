package br.com.devd2.meshstorage.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileStorageClient {

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    @JsonProperty("idFile")
    private String idFile;

    /**
     * Nome da aplicação para montrar a estrutura de armazenamento do arquivo.
     */
    @JsonProperty("applicationName")
    private String applicationName;

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
    @JsonProperty("fileName")
    private String fileName;

    /**
     * Conteúdo do arquivo em base64.
     */
    @JsonProperty("dataBase64")
    private String dataBase64;

}
