package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.BoletoConsultaPort;
import com.stagepass.live.application.port.out.BoletoRepository;
import com.stagepass.live.application.port.out.QRPort;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.model.BoletoDigital;

import java.util.List;
import java.util.Objects;

public class BoletoConsultaService implements BoletoConsultaPort {

    private final BoletoRepository boletoRepository;
    private final QRPort qrPort;

    public BoletoConsultaService(BoletoRepository boletoRepository, QRPort qrPort) {
        this.boletoRepository = Objects.requireNonNull(boletoRepository);
        this.qrPort = Objects.requireNonNull(qrPort);
    }

    @Override
    public List<BoletoDigital> porReserva(String reservaId) {
        return boletoRepository.porReserva(reservaId);
    }

    @Override
    public String generarQr(String boletoId) {
        BoletoDigital boleto = boletoRepository.buscarPorId(boletoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleto no encontrado: " + boletoId));
        return qrPort.generar(boleto);
    }
}
