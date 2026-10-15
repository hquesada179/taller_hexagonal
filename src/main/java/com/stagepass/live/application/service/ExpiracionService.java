package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.ExpiracionPort;
import com.stagepass.live.application.port.in.ResultadoExpiracion;
import com.stagepass.live.application.port.out.OcupacionRepository;
import com.stagepass.live.application.port.out.RelojPort;
import com.stagepass.live.application.port.out.ReservaRepository;
import com.stagepass.live.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Caso de uso que libera los asientos y marca como EXPIRADA toda reserva
 * PENDIENTE_PAGO cuyo bloqueo de 10 minutos ya vencio. Se invoca tanto desde
 * el scheduler automatico (ReservaScheduler) como manualmente desde un
 * endpoint administrativo/demo.
 */
public class ExpiracionService implements ExpiracionPort {

    private final ReservaRepository reservaRepository;
    private final OcupacionRepository ocupacionRepository;
    private final RelojPort relojPort;
    private final AtomicReference<ResultadoExpiracion> ultimoResultado =
            new AtomicReference<>(ResultadoExpiracion.ningunaEjecucion());

    public ExpiracionService(ReservaRepository reservaRepository,
                              OcupacionRepository ocupacionRepository,
                              RelojPort relojPort) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository);
        this.ocupacionRepository = Objects.requireNonNull(ocupacionRepository);
        this.relojPort = Objects.requireNonNull(relojPort);
    }

    @Override
    public int expirarVencidas() {
        LocalDateTime ahora = relojPort.ahora();
        List<Reserva> vencidas = reservaRepository.vencidas(ahora);

        int expiradas = 0;
        for (Reserva reserva : vencidas) {
            boolean marcada = reservaRepository.expirarSiPendiente(reserva.getId(), ahora);
            if (marcada) {
                ocupacionRepository.liberar(reserva.getId());
                expiradas++;
            }
        }

        ultimoResultado.set(new ResultadoExpiracion(expiradas, ahora));
        return expiradas;
    }

    @Override
    public ResultadoExpiracion consultarUltimaEjecucion() {
        return ultimoResultado.get();
    }
}
