package br.com.devd2.meshstorage.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class FileBase64Util {

    /**
     * Responsável por converter arquivo físico em base64.
     * @param filePath - Caminho do arquivo para leitura.
     * @return String do conteúdo do arquivo em Base64
     * @throws IOException - Erro na leitura do arquivo.
     */
    public static String fileToBase64(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] fileBytes = Files.readAllBytes(path);
        return fileToBase64(fileBytes);
    }

    /**
     * Responsável por converter array de bytes em base64.
     * @param arrayBytes - Array de byte para conversão.
     * @return String do conteúdo do arquivo em Base64
     */
    public static String fileToBase64(byte[] arrayBytes) {
        return Base64.getEncoder().encodeToString(arrayBytes);
    }


    /**
     * Responsavel por converter de Base64 para arrey de bytes.
     * @param base64 - conteúdo de arquivo em base64
     * @return Array de Bytes
     */
    public static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }

}