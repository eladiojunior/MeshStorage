package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class FileStorageClientDownload {

    /**
     * Identificador do arquivo em banco como chave: UUID.randomUUID().toString()
     */
    private String idFile;

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
    private String fileName;

    /**
     * Conteúdo do arquivo em base64.
     */
    private String dataBase64;

    /**
     * Indicador de erro no download.
     */
    private boolean isError;

    /**
     * Mensagem de erro caso ocorra.
     */
    private String messageError;

}
