package com.stagepass.live.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege los endpoints administrativos y de demostracion con una clave
 * simple por encabezado (X-Admin-Key). No es un mecanismo de autenticacion
 * completo: es la proteccion minima exigida por el taller para no exponer
 * endpoints de simulacion/administracion sin ningun control.
 */
@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {

    private static final String ENCABEZADO = "X-Admin-Key";

    private final String claveEsperada;

    public AdminSecurityInterceptor(@Value("${stagepass.admin.api-key}") String claveEsperada) {
        this.claveEsperada = claveEsperada;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws java.io.IOException {
        String recibida = request.getHeader(ENCABEZADO);
        if (recibida == null || !recibida.equals(claveEsperada)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"No autorizado. Se requiere el encabezado " + ENCABEZADO + "\"}");
            return false;
        }
        return true;
    }
}
