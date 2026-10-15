package com.stagepass.live.adapter.out.pago;

import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.domain.enums.EstadoPago;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.exception.PagoInvalidoException;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.vo.ConfirmacionPago;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTADOR SIMULADO (base comun): cada pasarela de pago real (PayPal, Wompi,
 * PSE) se representa aqui como un servicio externo independiente que
 * mantiene su propio "libro" de transacciones en memoria, exactamente como
 * lo haria el proveedor real. Ninguna de estas clases es una integracion
 * real: no se realiza ninguna llamada de red a un proveedor externo.
 */
public abstract class PasarelaPagoSimulada implements PagoGateway {

    private final Map<String, Pago> pagosPorReserva = new ConcurrentHashMap<>();

    protected abstract String prefijoReferencia();

    @Override
    public Pago registrar(Reserva reserva, ProveedorPago proveedor) {
        Pago pago = new Pago(
                UUID.randomUUID().toString(),
                reserva.getId(),
                prefijoReferencia() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                reserva.getTotal(),
                EstadoPago.PENDIENTE,
                proveedor);
        pagosPorReserva.put(reserva.getId(), pago);
        return pago;
    }

    /**
     * Cotejamos referencia y monto contra el registro interno (en memoria) de
     * esta pasarela simulada. Esto NO equivale a la verificacion de firma
     * criptografica que haria un receptor de webhooks reales: aqui no se
     * valida ningun secreto compartido ni cabecera de firma del proveedor.
     */
    @Override
    public Pago verificar(ConfirmacionPago datos) {
        Pago pago = pagosPorReserva.get(datos.getReservaId());
        if (pago == null) {
            throw new PagoInvalidoException(
                    "No existe un pago registrado en " + prefijoReferencia() + " para la reserva " + datos.getReservaId());
        }
        if (!pago.getReferenciaExterna().equals(datos.getReferencia())) {
            throw new PagoInvalidoException(
                    "La referencia informada no corresponde al pago registrado para la reserva " + datos.getReservaId());
        }
        if (pago.getMonto().compareTo(datos.getMonto()) != 0) {
            throw new PagoInvalidoException(
                    "El monto informado (" + datos.getMonto() + ") no coincide con el monto registrado (" + pago.getMonto() + ")");
        }

        if (datos.isExitoso()) {
            if (!pago.estaVerificado()) {
                pago.aprobar();
            }
        } else {
            pago.rechazar();
        }
        return pago;
    }

    @Override
    public Optional<Pago> consultar(String reservaId) {
        return Optional.ofNullable(pagosPorReserva.get(reservaId));
    }
}
