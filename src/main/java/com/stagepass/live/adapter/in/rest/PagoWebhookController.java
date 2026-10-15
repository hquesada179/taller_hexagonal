package com.stagepass.live.adapter.in.rest;

import com.stagepass.live.adapter.in.rest.dto.WebhookPagoRequest;
import com.stagepass.live.application.port.in.ConfirmacionPort;
import com.stagepass.live.domain.vo.ConfirmacionPago;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST que recibe la confirmacion de pago de la
 * pasarela (webhook).
 *
 * ADVERTENCIA — ESTO ES UNA SIMULACION, NO UNA VERIFICACION DE PAGO SEGURA:
 * este endpoint NO valida ninguna firma criptografica del PROVEEDOR sobre la
 * peticion HTTP entrante (a diferencia de un webhook real de PayPal/Wompi/PSE,
 * que firma su payload con un secreto compartido que el receptor debe
 * verificar antes de confiar en el mensaje). El campo "exitoso" del cuerpo de
 * la peticion es, en este taller, una simple afirmacion del llamador: la
 * autenticidad del pago se demuestra unicamente a traves de la cotejacion de
 * referencia/monto contra el registro interno de la pasarela simulada
 * (PasarelaPagoSimulada), no mediante criptografia de transporte. Por lo
 * tanto, este endpoint representa el PUNTO donde un webhook real se
 * conectaria, pero su implementacion actual JAMAS debe exponerse tal cual a
 * internet en un entorno de produccion sin agregar verificacion de firma del
 * proveedor (ver detalle en el README, seccion "Limitaciones tecnicas").
 */
@RestController
@RequestMapping("/api/webhooks/pagos")
public class PagoWebhookController {

    private final ConfirmacionPort confirmacionPort;

    public PagoWebhookController(ConfirmacionPort confirmacionPort) {
        this.confirmacionPort = confirmacionPort;
    }

    @PostMapping
    public ResponseEntity<Void> recibir(@Valid @RequestBody WebhookPagoRequest request) {
        ConfirmacionPago datos = new ConfirmacionPago(
                request.reservaId(), request.referencia(), request.proveedor(),
                request.monto(), request.exitoso(), request.momentoConfirmacionProveedor());
        confirmacionPort.confirmar(datos);
        return ResponseEntity.ok().build();
    }
}
