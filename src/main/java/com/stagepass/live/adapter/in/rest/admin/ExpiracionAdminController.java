package com.stagepass.live.adapter.in.rest.admin;

import com.stagepass.live.adapter.in.rest.dto.ResultadoExpiracionResponse;
import com.stagepass.live.application.port.in.ExpiracionPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST administrativo para ejecutar y consultar el
 * proceso de expiracion de reservas. Protegido por AdminSecurityInterceptor
 * (requiere el encabezado X-Admin-Key); no debe exponerse sin esa proteccion.
 */
@RestController
@RequestMapping("/api/admin/expiracion")
public class ExpiracionAdminController {

    private final ExpiracionPort expiracionPort;

    public ExpiracionAdminController(ExpiracionPort expiracionPort) {
        this.expiracionPort = expiracionPort;
    }

    @PostMapping("/ejecutar")
    public ResultadoExpiracionResponse ejecutar() {
        expiracionPort.expirarVencidas();
        return ResultadoExpiracionResponse.desde(expiracionPort.consultarUltimaEjecucion());
    }

    @GetMapping("/estado")
    public ResultadoExpiracionResponse estado() {
        return ResultadoExpiracionResponse.desde(expiracionPort.consultarUltimaEjecucion());
    }
}
