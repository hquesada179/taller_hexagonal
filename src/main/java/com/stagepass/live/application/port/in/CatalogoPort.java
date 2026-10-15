package com.stagepass.live.application.port.in;

import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;

import java.util.List;

/**
 * Puerto de entrada para consultar el catalogo de eventos, recintos y asientos.
 *
 * "obtenerEvento" y "obtenerRecinto" son extensiones respecto del UML original
 * (que solo definia listarEventos/listarAsientos); se agregan para satisfacer
 * el requisito del taller de "consultar recintos y asientos" de forma directa.
 */
public interface CatalogoPort {

    List<Evento> listarEventos();

    Evento obtenerEvento(String eventoId);

    Recinto obtenerRecinto(String recintoId);

    List<Asiento> listarAsientos(String eventoId);
}
