package com.stagepass.live.adapter.out.pago;

import org.springframework.stereotype.Component;

/**
 * ADAPTADOR SIMULADO de PSE. No realiza ninguna llamada real al API de PSE;
 * existe unicamente para demostrar el flujo completo de compra.
 */
@Component
public class PSEAdapter extends PasarelaPagoSimulada {

    @Override
    protected String prefijoReferencia() {
        return "PSE";
    }
}
