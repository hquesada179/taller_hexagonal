package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.application.port.out.CatalogoRepository;
import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida REAL hacia MySQL para el catalogo (recintos, eventos,
 * asientos). Solo traduce entre JPA y dominio; no contiene reglas de negocio.
 */
@Repository
public class MySQLCatalogoAdapter implements CatalogoRepository {

    private final RecintoJpaRepository recintoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final AsientoJpaRepository asientoJpaRepository;

    public MySQLCatalogoAdapter(RecintoJpaRepository recintoJpaRepository,
                                 EventoJpaRepository eventoJpaRepository,
                                 AsientoJpaRepository asientoJpaRepository) {
        this.recintoJpaRepository = recintoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.asientoJpaRepository = asientoJpaRepository;
    }

    @Override
    public Optional<Recinto> recinto(String id) {
        return recintoJpaRepository.findById(id).map(CatalogoMapper::aDominio);
    }

    @Override
    public Optional<Evento> evento(String id) {
        return eventoJpaRepository.findById(id).map(CatalogoMapper::aDominio);
    }

    @Override
    public List<Evento> eventos() {
        return eventoJpaRepository.findAll().stream().map(CatalogoMapper::aDominio).toList();
    }

    @Override
    public List<Asiento> asientos(String eventoId) {
        return eventoJpaRepository.findById(eventoId)
                .map(evento -> asientoJpaRepository.findByRecintoId(evento.getRecintoId()).stream()
                        .map(CatalogoMapper::aDominio)
                        .toList())
                .orElseGet(List::of);
    }
}
