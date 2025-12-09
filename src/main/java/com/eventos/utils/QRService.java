package com.eventos.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Servicio utilitario para generar códigos QR en Base64 (PNG).
 */
public class QRService {

    /**
     * Genera un PNG en Base64 a partir de un texto.
     * @param texto contenido a codificar
     * @param size tamaño del QR (px)
     * @return cadena Base64 (sin prefijo data:)
     */
    public String generarQRBase64(String texto, int size) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(texto.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el código QR", e);
        }
    }
}
