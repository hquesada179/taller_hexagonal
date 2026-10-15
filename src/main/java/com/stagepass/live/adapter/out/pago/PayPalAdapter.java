package com.stagepass.live.adapter.out.pago;

import org.springframework.stereotype.Component;

/**
 * ADAPTADOR SIMULADO de PayPal. No realiza ninguna llamada real al API de
 * PayPal; existe unicamente para demostrar el flujo completo de compra.
 */
@Component
public class PayPalAdapter extends PasarelaPagoSimulada {

    @Override
    protected String prefijoReferencia() {
        return "PAYPAL";
    }
}
