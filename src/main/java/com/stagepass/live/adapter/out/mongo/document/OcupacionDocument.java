package com.stagepass.live.adapter.out.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento de ocupacion de un asiento para una funcion (evento) concreta.
 *
 * El "_id" se construye deterministicamente como "eventoId::asientoId", de
 * forma que el indice unico propio de "_id" en MongoDB es, por si mismo, la
 * garantia tecnica de que nunca pueden coexistir dos documentos para el
 * mismo asiento de la misma funcion: es fisicamente imposible insertar dos
 * veces el mismo "_id".
 */
@Document(collection = "disponibilidad_asientos")
public class OcupacionDocument {

    @Id
    private String id;

    @Indexed
    private String eventoId;

    private String asientoId;
    private String estado;
    private String reservaId;
    private String clienteId;
    private LocalDateTime bloqueadoHasta;

    protected OcupacionDocument() {
    }

    public OcupacionDocument(String id, String eventoId, String asientoId, String estado,
                              String reservaId, String clienteId, LocalDateTime bloqueadoHasta) {
        this.id = id;
        this.eventoId = eventoId;
        this.asientoId = asientoId;
        this.estado = estado;
        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public static String construirId(String eventoId, String asientoId) {
        return eventoId + "::" + asientoId;
    }

    public String getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getAsientoId() {
        return asientoId;
    }

    public String getEstado() {
        return estado;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
