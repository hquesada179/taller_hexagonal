package com.stagepass.live.adapter.in.rest;

import com.stagepass.live.adapter.in.rest.dto.AsientoResponse;
import com.stagepass.live.adapter.in.rest.dto.EventoResponse;
import com.stagepass.live.adapter.in.rest.dto.RecintoResponse;
import com.stagepass.live.application.port.in.CatalogoPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada REST para consultar el catalogo: eventos, recintos y
 * asientos. (Extension practica sobre el UML: expone cada consulta en su
 * propio recurso, ademas del metodo catalogo() agregado en CompraRestController.)
 */
@RestController
@RequestMapping("/api")
public class CatalogoRestController {

    private final CatalogoPort catalogoPort;

    public CatalogoRestController(CatalogoPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping("/eventos")
    public List<EventoResponse> listarEventos() {
        return catalogoPort.listarEventos().stream().map(EventoResponse::desde).toList();
    }

    @GetMapping("/eventos/{eventoId}")
    public EventoResponse obtenerEvento(@PathVariable String eventoId) {
        return EventoResponse.desde(catalogoPort.obtenerEvento(eventoId));
    }

    @GetMapping("/eventos/{eventoId}/asientos")
    public List<AsientoResponse> listarAsientos(@PathVariable String eventoId) {
        return catalogoPort.listarAsientos(eventoId).stream().map(AsientoResponse::desde).toList();
    }

    @GetMapping("/recintos/{recintoId}")
    public RecintoResponse obtenerRecinto(@PathVariable String recintoId) {
        return RecintoResponse.desde(catalogoPort.obtenerRecinto(recintoId));
    }
}
