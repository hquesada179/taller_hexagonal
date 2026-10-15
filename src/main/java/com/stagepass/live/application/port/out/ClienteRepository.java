package com.stagepass.live.application.port.out;

import com.stagepass.live.domain.model.Cliente;

import java.util.Optional;

/**
 * Puerto de salida hacia la fuente de datos de clientes.
 * Implementado por un adaptador mock (MockClienteAdapter) en este taller.
 */
public interface ClienteRepository {

    Optional<Cliente> buscar(String id);
}
