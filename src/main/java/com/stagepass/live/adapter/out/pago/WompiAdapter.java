package com.stagepass.live.adapter.out.pago;

import org.springframework.stereotype.Component;

/**
 * ADAPTADOR SIMULADO de Wompi. No realiza ninguna llamada real al API de
 * Wompi; existe unicamente para demostrar el flujo completo de compra.
 */
@Component
public class WompiAdapter extends PasarelaPagoSimulada {

    @Override
    protected String prefijoReferencia() {
        return "WOMPI";
    }
}
