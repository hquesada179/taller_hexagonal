package com.stagepass.live.adapter.out.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento de un boleto emitido.
 *
 * El "_id" se construye deterministicamente como "reservaId::asientoId": es
 * la garantia tecnica (indice unico de "_id") de que nunca se puede emitir
 * mas de un boleto para el mismo asiento dentro de la misma reserva, incluso
 * si el webhook de confirmacion de pago llega duplicado. "boletoId" es el
 * identificador de negocio (UUID) de la entidad BoletoDigital, independiente
 * de la clave tecnica de deduplicacion.
 */
@Document(collection = "boletos")
public class BoletoDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String boletoId;

    @Indexed
    private String reservaId;

    private String asientoId;

    @Indexed
    private String clienteId;

    @Indexed
    private String eventoId;

    @Indexed(unique = true)
    private String codigoRedencion;

    private String firmaHash;
    private LocalDateTime fechaEmision;

    protected BoletoDocument() {
    }

    public BoletoDocument(String id, String boletoId, String reservaId, String asientoId, String clienteId,
                           String eventoId, String codigoRedencion, String firmaHash, LocalDateTime fechaEmision) {
        this.id = id;
        this.boletoId = boletoId;
        this.reservaId = reservaId;
        this.asientoId = asientoId;
        this.clienteId = clienteId;
        this.eventoId = eventoId;
        this.codigoRedencion = codigoRedencion;
        this.firmaHash = firmaHash;
        this.fechaEmision = fechaEmision;
    }

    public static String construirId(String reservaId, String asientoId) {
        return reservaId + "::" + asientoId;
    }

    public String getId() {
        return id;
    }

    public String getBoletoId() {
        return boletoId;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getAsientoId() {
        return asientoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getEventoId() {
        return eventoId;
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
}
