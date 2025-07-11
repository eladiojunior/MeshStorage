package br.com.devd2.meshstorageserver.services;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class QrCodeService {
    private static final int QR_SIZE    = 320;
    private static final int PADDING_Y  = 20;   // espaço para legenda abaixo
    private static final Font FONT      = new Font("SansSerif", Font.PLAIN, 14);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * @param link         URL completo para o download (com token, etc.)
     * @param fileName     "foto.webp"
     * @param sizeInBytes  tamanho para exibição
     * @param uploadedAt   data de upload (LocalDateTime ou null)
     */
    public byte[] createQrImage(String link,
                                String fileName,
                                long   sizeInBytes,
                                java.time.LocalDateTime uploadedAt) throws Exception {

        /* 1) Gera QR (BitMatrix) */
        var hints = java.util.Map.of(
                EncodeHintType.MARGIN, 1,
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        BitMatrix matrix = new MultiFormatWriter()
                .encode(link, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        /* 2) Converte para imagem e cria “canvas” maior para a legenda */
        BufferedImage qr = MatrixToImageWriter.toBufferedImage(matrix);
        int width  = qr.getWidth();
        int height = qr.getHeight() + PADDING_Y * 3;

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(qr, 0, 0, null);

        /* 3) Escreve texto */
        g.setColor(Color.BLACK);
        g.setFont(FONT);
        int y = QR_SIZE + PADDING_Y;
        g.drawString(fileName, 10, y);

        String sizeHuman = humanSize(sizeInBytes);
        g.drawString(sizeHuman, 10, y + PADDING_Y);

        if (uploadedAt != null) {
            g.drawString(DATE_FMT.format(uploadedAt), 10, y + PADDING_Y * 2);
        }
        g.dispose();

        /* 4) Converte para PNG in-memory */
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(canvas, "png", baos);
            return baos.toByteArray();
        }
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

}
