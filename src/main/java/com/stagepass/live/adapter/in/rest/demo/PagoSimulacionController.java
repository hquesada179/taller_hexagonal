package com.stagepass.live.adapter.in.rest.demo;

import com.stagepass.live.adapter.in.rest.dto.PagoResponse;
import com.stagepass.live.adapter.in.rest.dto.ReservaResponse;
import com.stagepass.live.adapter.in.rest.dto.SimularPagoRequest;
import com.stagepass.live.application.port.in.CompraPort;
import com.stagepass.live.application.port.in.ConfirmacionPort;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.vo.ConfirmacionPago;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Adaptador de entrada REST SOLO para demostracion (perfil "local"): permite
 * simular el resultado de un pago (aprobado o rechazado) sin conocer la
 * referencia externa generada por la pasarela simulada. Internamente
 * construye el mismo ConfirmacionPago que recibiria el webhook real y lo
 * entrega al mismo caso de uso (ConfirmacionPort), para que el recorrido de
 * negocio sea identico al de un pago real. Adicionalmente protegido por
 * AdminSecurityInterceptor (encabezado X-Admin-Key).
 */
@RestController
@RequestMapping("/api/demo/pagos")
@Profile("local")
public class PagoSimulacionController {

    private final ConfirmacionPort confirmacionPort;
    private final CompraPort compraPort;

    public PagoSimulacionController(ConfirmacionPort confirmacionPort, CompraPort compraPort) {
        this.confirmacionPort = confirmacionPort;
        this.compraPort = compraPort;
    }

    @GetMapping("/{reservaId}")
    public PagoResponse consultar(@PathVariable String reservaId) {
        return confirmacionPort.consultarPago(reservaId)
                .map(PagoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay pago registrado para la reserva " + reservaId));
    }

    @PostMapping("/{reservaId}/simular")
    public ReservaResponse simular(@PathVariable String reservaId, @Valid @RequestBody SimularPagoRequest request) {
        Pago pago = confirmacionPort.consultarPago(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay pago registrado para la reserva " + reservaId));

        ConfirmacionPago datos = new ConfirmacionPago(
                reservaId, pago.getReferenciaExterna(), pago.getProveedor(), pago.getMonto(),
                request.exitoso(), LocalDateTime.now());

        confirmacionPort.confirmar(datos);
        return ReservaResponse.desde(compraPort.consultar(reservaId));
    }
}
