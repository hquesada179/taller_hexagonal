package com.stagepass.live.adapter.out.firma;

import com.stagepass.live.application.port.out.FirmaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Adaptador de salida REAL de firma criptografica: HMAC-SHA256 autentico
 * (no simulado). La clave maestra vive en configuracion/infraestructura
 * (application.yml / variable de entorno STAGEPASS_HMAC_MASTER_SECRET) y
 * NUNCA se referencia desde el dominio.
 *
 * Para cada evento se deriva una clave propia aplicando HMAC-SHA256 sobre el
 * eventoId usando la clave maestra como clave: esto satisface el requisito
 * de "clave secreta asociada al evento" sin necesidad de una tabla adicional
 * de secretos por evento.
 */
@Component
public class HmacAdapter implements FirmaPort {

    private static final String ALGORITMO = "HmacSHA256";

    private final String claveMaestra;

    public HmacAdapter(@Value("${stagepass.hmac.master-secret}") String claveMaestra) {
        this.claveMaestra = Objects.requireNonNull(claveMaestra, "claveMaestra es obligatoria");
    }

    @Override
    public String firmar(String datos, String eventoId) {
        byte[] claveEvento = derivarClaveDeEvento(eventoId);
        byte[] firma = hmac(claveEvento, datos.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(firma);
    }

    @Override
    public boolean verificar(String datos, String firma, String eventoId) {
        String firmaEsperada = firmar(datos, eventoId);
        return MessageDigest.isEqual(
                firmaEsperada.getBytes(StandardCharsets.UTF_8),
                firma.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] derivarClaveDeEvento(String eventoId) {
        return hmac(claveMaestra.getBytes(StandardCharsets.UTF_8), eventoId.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmac(byte[] clave, byte[] datos) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(clave, ALGORITMO));
            return mac.doFinal(datos);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("No fue posible calcular el HMAC-SHA256", ex);
        }
    }
}
