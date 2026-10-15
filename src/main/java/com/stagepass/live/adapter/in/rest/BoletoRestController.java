package com.stagepass.live.adapter.in.rest;

import com.stagepass.live.adapter.in.rest.dto.BoletoResponse;
import com.stagepass.live.adapter.in.rest.dto.QrResponse;
import com.stagepass.live.application.port.in.BoletoConsultaPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada REST para consultar los boletos emitidos de una
 * reserva y su codigo QR.
 */
@RestController
@RequestMapping("/api/boletos")
public class BoletoRestController {

    private final BoletoConsultaPort boletoConsultaPort;

    public BoletoRestController(BoletoConsultaPort boletoConsultaPort) {
        this.boletoConsultaPort = boletoConsultaPort;
    }

    @GetMapping("/reserva/{reservaId}")
    public List<BoletoResponse> porReserva(@PathVariable String reservaId) {
        return boletoConsultaPort.porReserva(reservaId).stream().map(BoletoResponse::desde).toList();
    }

    @GetMapping("/{boletoId}/qr")
    public QrResponse qr(@PathVariable String boletoId) {
        return new QrResponse(boletoId, boletoConsultaPort.generarQr(boletoId));
    }
}
