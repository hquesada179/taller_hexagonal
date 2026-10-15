package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia el catalogo (recintos, eventos, asientos).
 * Implementado realmente por MySQLCatalogoAdapter.
 */
public interface CatalogoRepository {

    Optional<Recinto> recinto(String id);

    Optional<Evento> evento(String id);

    List<Evento> eventos();

    List<Asiento> asientos(String eventoId);
}
