package com.stagepass.live.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Boleto emitido para un asiento especifico de una reserva aprobada.
 * La firma (firmaHash) se calcula FUERA del dominio (FirmaPort, en infraestructura)
 * a partir de datosParaFirma(), y se adjunta una sola vez mediante firmar(...).
 */
public class BoletoDigital {

    private final String id;
    private final String reservaId;
    private final String asientoId;
    private final String codigoRedencion;
    private final LocalDateTime fechaEmision;
    private String firmaHash;

    public BoletoDigital(String id, String reservaId, String asientoId, String codigoRedencion,
                          LocalDateTime fechaEmision, String firmaHash) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.reservaId = Objects.requireNonNull(reservaId, "reservaId es obligatorio");
        this.asientoId = Objects.requireNonNull(asientoId, "asientoId es obligatorio");
        this.codigoRedencion = Objects.requireNonNull(codigoRedencion, "codigoRedencion es obligatorio");
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "fechaEmision es obligatoria");
        this.firmaHash = firmaHash;
    }

    public static BoletoDigital sinFirma(String id, String reservaId, String asientoId,
                                          String codigoRedencion, LocalDateTime fechaEmision) {
        return new BoletoDigital(id, reservaId, asientoId, codigoRedencion, fechaEmision, null);
    }

    /**
     * Cadena inmutable y determinista que se firma con HMAC-SHA256. Contiene
     * unicamente datos que no cambian una vez emitido el boleto.
     */
    public String datosParaFirma() {
        return String.join("|",
                id,
                reservaId,
                asientoId,
                codigoRedencion,
                fechaEmision.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public void firmar(String firmaHash) {
        if (this.firmaHash != null) {
            throw new IllegalStateException("El boleto " + id + " ya tiene una firma asignada");
        }
        this.firmaHash = Objects.requireNonNull(firmaHash, "firmaHash es obligatorio");
    }

    public boolean tieneFirma() {
        return firmaHash != null;
    }

    public String getId() {
        return id;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getAsientoId() {
        return asientoId;
    }

    public String getCodigoRedencion() {
        return codigoRedencion;
    }

    public String getFirmaHash() {
        return firmaHash;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoletoDigital that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
