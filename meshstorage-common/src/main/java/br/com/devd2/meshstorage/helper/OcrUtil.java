package br.com.devd2.meshstorage.helper;

import java.util.Set;

public class OcrUtil {

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/tiff", "image/bmp", "image/webp", "application/pdf"
    );

    /**
     * Verifica o contenty type do arquivo para verificar se é possível extrair OCR do conteúdo.
     * @param mimeType - Tipo de arquivo passível de extração OCR.
     * @return true - pode extrair conteúdo texto, via OCR.
     */
    public static boolean isAllowedTypeForOcr(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Envio o conteúdo do arquivo para processamento de extração de conteúdo, via OCR.
     * @param idFileStorage - Identificador do arquivo armazenado para controle do status.
     * @param hashFileBytes - Hash dos bytes do arquivo, para verificação e evitar processamento OCR desnecessário.
     * @param bytesFileOcr - Conteúdo, em bytes, do arquivo para processamento do OCR.
     */
    public static void sendExtractionTextFormFile(Long idFileStorage, String hashFileBytes, byte[] bytesFileOcr) {

    }

}
