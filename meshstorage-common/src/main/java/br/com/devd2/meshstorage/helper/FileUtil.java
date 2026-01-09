package br.com.devd2.meshstorage.helper;

import br.com.devd2.meshstorage.enums.FileContentTypesEnum;
import com.luciad.imageio.webp.WebPWriteParam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    /**
     * Aplicar um padrão HASH em um conteúdo de array de byte[]
     * @param data - Array de bytes para aplicar o HASH
     * @return String do HASH aplicado.
     */
    public static String hashConteudoBytes(byte[] data) {
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
    public static String generatePhisicalName(String originalFilename) {
        var extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return generatePhisicalNameByExtension(extension);
    }

    /**
     * Gerar nome físico de arquivo para armazenamento em disco.
     * @param extension - Extensão (com ponto, ex: .txt)do nome do arquivo fisico.
     * @return Nome fisico gerado.
     */
    public static String generatePhisicalNameByExtension(String extension) {
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

    /**
     * Extensões que indicam arquivo já comprimido ou “não‑compressível”.
     **/
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
    public static boolean hasFileNameCompressedZip(String originalName) {
        int dot = originalName.lastIndexOf('.');
        String ext = (dot >= 0) ? originalName.substring(dot + 1) : "";
        ext = ext.toLowerCase();
        return COMPRESSED_EXT.contains(ext);
    }

    /**
     * Tipos aceitos para conversão. Tudo em minúsculas para comparação \"case-insensitive\".
     **/
    private static final Set<String> CONVERTIBLE_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/pjpeg",
            "image/gif", "image/bmp", "image/x-windows-bmp", "image/tiff"
    );

    /**
     * Tipos de arquivos de compressão/contêiner. Tudo em minúsculas para comparação \"case-insensitive\".
     **/
    private static final Set<String> CONVERTIBLE_ZIP_TYPES = Set.of(
            "application/zip", "application/x-zip-compressed", "application/gzip",
            "application/x-7z-compressed", "application/x-rar-compressed", "application/x-bzip2",
            "application/x-xz", "application/x-tar" );

    /**
     * Verifica se o arquivo é compactado, conforme seu ContentType.
     * @param contentType - ContentType do arquivo para verificar se é um ZIP;
     * @return true = se a extensão for COMPRESSED, caso contrário = false
     */
    public static boolean hasContentTypeCompressedZip(String contentType) {
        return CONVERTIBLE_ZIP_TYPES.contains(contentType.toLowerCase());
    }
    /**
     * Verifica se o <i>Content-Type</i> recebido representa
     * uma imagem que podemos converter para WEBP.
     *
     * @param contentType ex.: <code>"image/png"</code>
     * @return {@code true} se for convertível, {@code false} caso contrário
     */
    public static boolean hasFileTypeCompressedWebP(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase(Locale.ROOT).trim();

        // Ignora imagens que já estão em WebP ou SVG (vetorial)
        if (ct.startsWith("image/webp") || ct.startsWith("image/svg"))
            return false;
        // Remove charset se vier "image/png; charset=binary"
        int semi = ct.indexOf(';');
        if (semi > 0) {
            ct = ct.substring(0, semi).trim();
        }
        return CONVERTIBLE_IMAGE_TYPES.contains(ct);
    }

    /**
     * Binários já comprimidos ou que pouco se beneficiam de ZIP.
     **/
    private static final Set<String> ALREADY_COMPRESSED = Set.of(
        // Imagens
        "image/jpeg", "image/jpg", "image/png", "image/webp",
        "image/gif",  "image/bmp", "image/heic",
        // Mídia
        "audio/", "video/",
        // Arquivos de compressão/contêiner
        "application/zip", "application/x-zip-compressed", "application/gzip",
        "application/x-7z-compressed", "application/x-rar-compressed", "application/x-bzip2",
        "application/x-xz", "application/x-tar",
        // PDF costuma conter compressão interna
        "application/pdf"
    );

    /**
     * Tipos que quase sempre se beneficiam de ZIP mesmo já sendo textuais.
     **/
    private static final Set<String> EXPLICIT_COMPRESSIBLE = Set.of(
        FileContentTypesEnum.JSON.getContentType(), FileContentTypesEnum.XML_APPLICATION.getContentType(),
        FileContentTypesEnum.JSONX.getContentType(), FileContentTypesEnum.JS_APPLICATION.getContentType(),
        FileContentTypesEnum.JSX_APPLICATION.getContentType(), FileContentTypesEnum.SQL_APPLICATION.getContentType(),
        FileContentTypesEnum.GRAPHQL_APPLICATION.getContentType(), FileContentTypesEnum.CSV.getContentType()
    );

    /**
     * Verifica se o <i>Content-Type</i> recebido representa
     * tipo de arquivo que vale a pena comprimir.
     *
     * @param contentType ex.: {@code "image/png"} ou {@code "application/json; charset=utf-8"}
     * @return {@code true} se vale comprimir em ZIP.
     */
    public static boolean hasFileTypeNameCompressedZip(String contentType) {
        if (contentType == null) return true;               // desconhecido → tenta.
        String ct = contentType.toLowerCase(Locale.ROOT).trim();
        // remove charset etc.
        int semi = ct.indexOf(';');
        if (semi > 0) ct = ct.substring(0, semi).trim();
        /* Imagem/áudio/vídeo ou outros já comprimidos: pula */
        if (isAlreadyCompressed(ct)) return false;
        /* Qualquer coisa text/* (html, css, csv, svg...) costuma reduzir 60-80 % */
        if (ct.startsWith("text/")) return true;
        /* JSON, XML, etc. */
        if (EXPLICIT_COMPRESSIBLE.contains(ct)) return true;
        /* Caso não reconheça, ainda vale tentar: pior cenário = ganho nulo */
        return true;
    }

    private static boolean isAlreadyCompressed(String ct) {
        if (ALREADY_COMPRESSED.contains(ct))
            return true;
        return ct.startsWith("image/") || ct.startsWith("audio/") || ct.startsWith("video/");
    }

    /**
     * Converte uma imagem [PNG/JPG/etc] em formato WebP para compressão e redução de espaço.
     * @param original - Bytes da imagem a ser convertida em WebP.
     * @param quality 0.0 – 1.0 (1 = sem perda, 0.80 +- boa para fotos)
     * @return Imagem convertida em WebP.
     */
    public static byte[] convertImagemToWebp(byte[] original, float quality) throws IOException {

        /* 0) Garante que os plugins de ImageIO sejam visíveis */
        ImageIO.scanForPlugins();

        /* 1) Decodifica */
        BufferedImage input;
        try (ByteArrayInputStream in = new ByteArrayInputStream(original)) {
            input = ImageIO.read(in);
        }
        if (input == null) throw new IOException("Imagem inválida ou formato não suportado.");

        /* 2) Obtém um writer real de WEBP */
        ImageWriter writer = ImageIO.getImageWritersByMIMEType(FileContentTypesEnum.WEBP.getContentType()).next();

        /* 3) Usa o WebPWriteParam específico */
        WebPWriteParam param = new WebPWriteParam(writer.getLocale());
        param.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
        param.setCompressionType("Lossy");          // ou "Lossless"
        param.setCompressionQuality(quality);       // 0–1

        /* 4) Escreve em memória */
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(input, null, null), param);
            ios.flush();          // força descarregar no BAOS
            return baos.toByteArray(); // agora tem dados
        } finally {
            writer.dispose();
        }

    }

    /**
     * Valida um ContentType informado.
     * @param contentType - Content Type para validar.
     * @return true, valido ou false, inválido.
     */
    public static boolean hasValidContentType(String contentType) {
        if (contentType == null || contentType.isEmpty())
            return false;
        return Arrays.stream(FileContentTypesEnum.values())
                .anyMatch( f -> f.getContentType().equals(contentType.toLowerCase()));
    }

}