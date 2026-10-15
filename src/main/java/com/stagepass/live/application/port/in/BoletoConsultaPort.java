package com.stagepass.live.application.port.in;

import com.stagepass.live.domain.model.BoletoDigital;

import java.util.List;

/**
 * Puerto de entrada para consultar los boletos emitidos de una reserva.
 * No existe en el UML original como puerto independiente (los boletos se
 * modelan como entidad de dominio, no como caso de uso); se agrega aqui
 * para satisfacer el requisito del taller de "consultar los boletos
 * emitidos" via API sin mezclar responsabilidades con ConfirmacionPort.
 */
public interface BoletoConsultaPort {

    List<BoletoDigital> porReserva(String reservaId);

    String generarQr(String boletoId);
}
