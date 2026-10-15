package com.stagepass.live.adapter.out.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.stagepass.live.application.port.out.QRPort;
import com.stagepass.live.domain.model.BoletoDigital;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Adaptador de salida REAL de generacion de codigos QR, usando la libreria
 * ZXing (no simulado). Codifica el identificador de redencion y la firma del
 * boleto, y retorna la imagen PNG como data URI base64 lista para incrustar
 * en una respuesta JSON o en una pagina HTML.
 */
@Component
public class QRAdapter implements QRPort {

    private static final int TAMANO_PIXELES = 300;

    @Override
    public String generar(BoletoDigital boleto) {
        String contenido = "STAGEPASS|" + boleto.getId() + "|" + boleto.getCodigoRedencion() + "|" + boleto.getFirmaHash();
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, TAMANO_PIXELES, TAMANO_PIXELES);

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", salida);

            String base64 = Base64.getEncoder().encodeToString(salida.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("No fue posible generar el codigo QR del boleto " + boleto.getId(), ex);
        }
    }
}
