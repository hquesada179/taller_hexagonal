package com.stagepass.live.adapter.in.rest;

import com.stagepass.live.adapter.in.rest.dto.ErrorResponse;
import com.stagepass.live.domain.exception.AsientoNoDisponibleException;
import com.stagepass.live.domain.exception.EventoNoDisponibleException;
import com.stagepass.live.domain.exception.LimiteBoletosExcedidoException;
import com.stagepass.live.domain.exception.PagoInvalidoException;
import com.stagepass.live.domain.exception.RecursoNoEncontradoException;
import com.stagepass.live.domain.exception.ReglaNegocioException;
import com.stagepass.live.domain.exception.ReservaNoVigenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce las excepciones del nucleo (dominio/aplicacion) a codigos HTTP.
 * Es el UNICO lugar donde una regla de negocio se asocia a un protocolo:
 * el dominio nunca conoce HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PagoInvalidoException.class)
    public ResponseEntity<ErrorResponse> pagoInvalido(PagoInvalidoException ex) {
        return construir(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler({EventoNoDisponibleException.class, AsientoNoDisponibleException.class,
            LimiteBoletosExcedidoException.class, ReservaNoVigenteException.class})
    public ResponseEntity<ErrorResponse> conflictoDeNegocio(ReglaNegocioException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> solicitudInvalida(IllegalArgumentException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ErrorResponse cuerpo = ErrorResponse.de(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La solicitud contiene datos invalidos", detalles);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado: " + ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje));
    }
}
