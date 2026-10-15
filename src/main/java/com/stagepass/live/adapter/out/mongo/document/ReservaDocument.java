package com.stagepass.live.adapter.out.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Documento de infraestructura (MongoDB). No contiene reglas de negocio,
 * solo representa el estado persistido de una Reserva de dominio.
 */
@Document(collection = "reservas")
public class ReservaDocument {

    @Id
    private String id;

    @Indexed
    private String eventoId;

    @Indexed
    private String clienteId;

    private List<String> asientoIds;
    private BigDecimal subtotal;
    private BigDecimal tarifaServicio;
    private BigDecimal descuento;
    private BigDecimal total;
    private LocalDateTime expiraEn;
    private String estado;

    protected ReservaDocument() {
    }

    public ReservaDocument(String id, String eventoId, String clienteId, List<String> asientoIds,
                            BigDecimal subtotal, BigDecimal tarifaServicio, BigDecimal descuento,
                            BigDecimal total, LocalDateTime expiraEn, String estado) {
        this.id = id;
        this.eventoId = eventoId;
        this.clienteId = clienteId;
        this.asientoIds = asientoIds;
        this.subtotal = subtotal;
        this.tarifaServicio = tarifaServicio;
        this.descuento = descuento;
        this.total = total;
        this.expiraEn = expiraEn;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<String> getAsientoIds() {
        return asientoIds;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTarifaServicio() {
        return tarifaServicio;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public String getEstado() {
        return estado;
    }
}
