package com.stagepass.live.adapter.in.rest;

import com.stagepass.live.adapter.in.rest.dto.EventoResponse;
import com.stagepass.live.adapter.in.rest.dto.IniciarCompraRequest;
import com.stagepass.live.adapter.in.rest.dto.ReservaResponse;
import com.stagepass.live.application.port.in.CatalogoPort;
import com.stagepass.live.application.port.in.CompraPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada REST para el caso de uso de compra. Replica
 * literalmente los dos metodos de CompraRestController en el UML
 * (iniciar/catalogo); las consultas de detalle de catalogo (asientos,
 * recinto) se exponen ademas en CatalogoRestController por comodidad.
 */
@RestController
@RequestMapping("/api/compras")
public class CompraRestController {

    private final CompraPort compraPort;
    private final CatalogoPort catalogoPort;

    public CompraRestController(CompraPort compraPort, CatalogoPort catalogoPort) {
        this.compraPort = compraPort;
        this.catalogoPort = catalogoPort;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> iniciar(@Valid @RequestBody IniciarCompraRequest request) {
        var reserva = compraPort.iniciar(request.clienteId(), request.eventoId(), request.asientoIds(), request.proveedor());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponse.desde(reserva));
    }

    @GetMapping("/catalogo")
    public List<EventoResponse> catalogo() {
        return catalogoPort.listarEventos().stream().map(EventoResponse::desde).toList();
    }

    @GetMapping("/{reservaId}")
    public ReservaResponse consultar(@PathVariable String reservaId) {
        return ReservaResponse.desde(compraPort.consultar(reservaId));
    }
}
