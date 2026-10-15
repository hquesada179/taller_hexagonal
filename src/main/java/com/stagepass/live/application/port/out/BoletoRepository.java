package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.BoletoDigital;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia de boletos digitales.
 * Implementado realmente por MongoBoletoAdapter, con restriccion unica
 * (reservaId, asientoId) y (codigoRedencion) para evitar emisiones duplicadas.
 */
public interface BoletoRepository {

    void guardarSinDuplicar(List<BoletoDigital> boletos);

    List<BoletoDigital> porReserva(String reservaId);

    Optional<BoletoDigital> buscarPorId(String id);

    int contarEmitidos(String clienteId, String eventoId);
}
