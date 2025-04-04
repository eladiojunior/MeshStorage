package br.com.devd2.meshstorage.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

public class FileUtil {

    public static String hashContent(String textContent) {
        return null;
    }

    public static String hashConteudo(byte[] bytesFile) {
        return null;
    }

    /**
     * Retorna o tamanho em MegaByte (MB)
     * @param lengthBytes - tamanho em bytes
     * @return tamanho convertido em MegaBytes.
     */
    public static int sizeInMB(int lengthBytes) {
        return lengthBytes / (1024 * 1024);
    }

    /**
     * Verificd se o tipo enviado é válido entre os tipos envia na lista.
     * @param contentTypesValid - Tipos válidos de arquivo.
     * @param contentType - Tipo para ser verifica
     * @return tipo válido (true) inválido (false), se a lista de tipo estivar vazia será retornado valido (true).
     */
    public static boolean hasTypeFileValid(String[] contentTypesValid, String contentType) {
        if (contentTypesValid == null || contentTypesValid.length == 0)
            return true;
        return Arrays.asList(contentTypesValid).contains(contentType);
    }

    /**
     * Gerar nome físico de arquivo para armazenamento em disco.
     * @param originalFilename - Nome original do arquivo.
     * @return Nome fisico gerado.
     */
    public static String generatePisicalName(String originalFilename) {
        var extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        var prefixName = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefixName + "_" + UUID.randomUUID().toString() + extension;
    }
}
