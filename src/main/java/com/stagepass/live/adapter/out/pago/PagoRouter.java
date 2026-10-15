package com.stagepass.live.adapter.out.pago;

import com.stagepass.live.application.port.out.PagoGateway;
import com.stagepass.live.domain.enums.ProveedorPago;
import com.stagepass.live.domain.exception.PagoInvalidoException;
import com.stagepass.live.domain.model.Pago;
import com.stagepass.live.domain.model.Reserva;
import com.stagepass.live.domain.vo.ConfirmacionPago;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Router que selecciona la pasarela de pago concreta (PayPal, Wompi o PSE)
 * segun el ProveedorPago de la reserva. Es el UNICO bean de tipo PagoGateway
 * inyectado en los casos de uso (CompraService/ConfirmacionService); las
 * pasarelas individuales son implementaciones intercambiables que el router
 * usa internamente, nunca referenciadas directamente desde la capa de
 * aplicacion.
 */
@Component
@Primary
public class PagoRouter implements PagoGateway {

    private final Map<ProveedorPago, PagoGateway> pasarelas = new EnumMap<>(ProveedorPago.class);

    public PagoRouter(PayPalAdapter payPalAdapter, WompiAdapter wompiAdapter, PSEAdapter pseAdapter) {
        pasarelas.put(ProveedorPago.PAYPAL, payPalAdapter);
        pasarelas.put(ProveedorPago.WOMPI, wompiAdapter);
        pasarelas.put(ProveedorPago.PSE, pseAdapter);
    }

    @Override
    public Pago registrar(Reserva reserva, ProveedorPago proveedor) {
        return resolver(proveedor).registrar(reserva, proveedor);
    }

    @Override
    public Pago verificar(ConfirmacionPago datos) {
        return resolver(datos.getProveedor()).verificar(datos);
    }

    @Override
    public Optional<Pago> consultar(String reservaId) {
        return pasarelas.values().stream()
                .map(pasarela -> pasarela.consultar(reservaId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private PagoGateway resolver(ProveedorPago proveedor) {
        PagoGateway pasarela = pasarelas.get(proveedor);
        if (pasarela == null) {
            throw new PagoInvalidoException("Proveedor de pago no soportado: " + proveedor);
        }
        return pasarela;
    }
}
