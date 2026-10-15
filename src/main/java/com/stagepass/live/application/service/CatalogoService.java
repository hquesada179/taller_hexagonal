package com.stagepass.live.application.service;

import com.stagepass.live.application.port.in.CatalogoPort;
import com.stagepass.live.application.port.out.CatalogoRepository;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;

import java.util.List;
import java.util.Objects;

public class CatalogoService implements CatalogoPort {

    private final CatalogoRepository catalogoRepository;

    public CatalogoService(CatalogoRepository catalogoRepository) {
        this.catalogoRepository = Objects.requireNonNull(catalogoRepository);
    }

    @Override
    public List<Evento> listarEventos() {
        return catalogoRepository.eventos();
    }

    @Override
    public Evento obtenerEvento(String eventoId) {
        return catalogoRepository.evento(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado: " + eventoId));
    }

    @Override
    public Recinto obtenerRecinto(String recintoId) {
        return catalogoRepository.recinto(recintoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recinto no encontrado: " + recintoId));
    }

    @Override
    public List<Asiento> listarAsientos(String eventoId) {
        obtenerEvento(eventoId);
        return catalogoRepository.asientos(eventoId);
    }
}
