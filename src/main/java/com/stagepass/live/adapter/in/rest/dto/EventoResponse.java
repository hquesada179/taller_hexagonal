package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.enums.EstadoEvento;
import com.stagepass.live.domain.enums.TipoEvento;
import com.stagepass.live.domain.model.Evento;

import java.time.LocalDateTime;

public record EventoResponse(String id, String nombre, TipoEvento tipo, String recintoId,
                              LocalDateTime fechaHora, EstadoEvento estado) {

    public static EventoResponse desde(Evento evento) {
        return new EventoResponse(evento.getId(), evento.getNombre(), evento.getTipo(),
                evento.getRecintoId(), evento.getFechaHora(), evento.getEstado());
    }
}
