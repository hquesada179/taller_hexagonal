package com.stagepass.live.adapter.out.cliente;

import com.stagepass.live.application.port.out.ClienteRepository;
import com.stagepass.live.domain.model.Cliente;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTADOR SIMULADO: no hay un sistema real de gestion de clientes en este
 * taller. Mantiene unos clientes de demostracion en memoria para poder
 * ejercitar el flujo completo de compra sin una base de datos adicional.
 */
@Repository
public class MockClienteAdapter implements ClienteRepository {

    private final Map<String, Cliente> clientes = new ConcurrentHashMap<>();

    public MockClienteAdapter() {
        registrar(new Cliente("cliente-demo-1", "Ana Maria Gomez", "ana.gomez@example.com", "+57 300 111 2222", "Colombiana"));
        registrar(new Cliente("cliente-demo-2", "Carlos Perez", "carlos.perez@example.com", "+57 300 333 4444", "Colombiana"));
        registrar(new Cliente("cliente-demo-3", "Laura Martinez", "laura.martinez@example.com", "+57 300 555 6666", "Colombiana"));
    }

    private void registrar(Cliente cliente) {
        clientes.put(cliente.getId(), cliente);
    }

    @Override
    public Optional<Cliente> buscar(String id) {
        return Optional.ofNullable(clientes.get(id));
    }
}
