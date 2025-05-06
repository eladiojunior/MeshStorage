package br.com.devd2.meshstorage.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static String hashContent(String textContent) {
        return null;
    }

    /**
     * Aplicar um padrão HASH em um conteúdo de array de byte[]
     * @param data - Array de bytes para aplicar o HASH
     * @return String do HASH aplicado.
     */
    public static String hashConteudo(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException erro) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", erro);
        }
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
        var prefixName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefixName + "_" + UUID.randomUUID().toString().toUpperCase() + extension.toLowerCase();
    }

    /**
     * Realiza o processo de compressão do conteúdo do arquivo.
     * @param bytesFile - Bytes do arquivo para realizar a compressão em ZIP;
     * @return Bytes do arquivo comprimido em formato ZIP.
     */
    public static byte[] compressZipFileContent(String entryName, byte[] bytesFile) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            ZipEntry entry = new ZipEntry(entryName);
            entry.setSize(bytesFile.length);
            zos.putNextEntry(entry);
            zos.write(bytesFile);
        }
        return bos.toByteArray();
    }

    /**
     * Realiza o processo de DEScompressão do conteúdo do arquivo.
     * @param bytesFile - Bytes do arquivo em ZIP para realizar a DEScompressão;
     * @return Bytes do arquivo DEScomprimido.
     */
    public static byte[] descompressZipFileContent(byte[] bytesFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytesFile))) {
            zis.getNextEntry();
            return zis.readAllBytes();
        }
    }

    /**
     * Responsável por mudar a extensão do nome do arquivo para a informada.
     * @param originalName - Nome do arquivo com a extensão (XXXX.jpeg);
     * @param newExtension - Nova extensão, exemplo: .zip;
     * @return Nome do arquivo com a nova extensão.
     */
    public static String changeFileNameExtension(String originalName, String newExtension) {

        if (originalName == null || originalName.isBlank())
            throw new IllegalArgumentException("Nome de arquivo vazio.");

        // Normaliza a nova extensão (garante o ponto e remove espaços)
        String ext = newExtension.trim();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        // Posição do último ponto (não conta caminhos)
        int lastDot = originalName.lastIndexOf('.');
        int lastSep = Math.max(originalName.lastIndexOf('/'),
                originalName.lastIndexOf('\\'));

        // Se não há ponto depois da última barra, considera “sem extensão”
        String baseName = (lastDot > lastSep) ? originalName.substring(0, lastDot)
                : originalName;

        return baseName + ext;
    }

    /** Extensões que indicam arquivo já comprimido ou “não‑compressível”. */
    private static final Set<String> COMPRESSED_EXT = Set.of(
            "zip","gz","bz2","xz","7z","rar","tar",
            "png","jpg","jpeg","gif","webp","avif",
            "mp4","mkv","mov","pdf","ogg","mp3",
            "jpeg2000","woff","woff2"
    );

    /**
     * Verifica se o arquivo já está comprimido, simplesmente pelas extensões.
     * @param originalName - Nome do arquivo para verificar se é um ZIP;
     * @return true = se a extensão for COMPRESSED, caso contrário = false
     */
    public static boolean hasFileNameCompress(String originalName) {

        int dot = originalName.lastIndexOf('.');
        String ext = (dot >= 0) ? originalName.substring(dot + 1) : "";
        ext = ext.toLowerCase();
        return COMPRESSED_EXT.contains(ext);

    }
}
