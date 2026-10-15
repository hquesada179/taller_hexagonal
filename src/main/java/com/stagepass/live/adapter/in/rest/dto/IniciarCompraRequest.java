package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.enums.ProveedorPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IniciarCompraRequest(
        @NotBlank(message = "clienteId es obligatorio") String clienteId,
        @NotBlank(message = "eventoId es obligatorio") String eventoId,
        @NotEmpty(message = "Debe seleccionar al menos un asiento") List<String> asientoIds,
        @NotNull(message = "proveedor es obligatorio") ProveedorPago proveedor) {
}
