package com.nequi.franchises.adapter.in.web;

import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidation(
            WebExchangeBindException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene campos inválidos."
        );

        problem.setTitle("Error de validación");

        List<FieldValidationError> errors =
                exception.getFieldErrors().stream()
                        .map(error -> new FieldValidationError(
                                error.getField(),
                                error.getDefaultMessage() != null
                                        ? error.getDefaultMessage()
                                        : "El valor no es válido"
                        ))
                        .toList();

        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ProblemDetail handleInvalidInput(
            ServerWebInputException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud o los parámetros "
                        + "no tienen el formato esperado. "
                        + "Los identificadores deben ser UUID válidos "
                        + "y el stock debe ser un entero dentro del rango permitido."
        );

        problem.setTitle("Solicitud inválida");

        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(
            ResourceNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Recurso no encontrado");

        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrentModification(
            OptimisticLockingFailureException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "La franquicia fue modificada por otra solicitud. "
                        + "Intenta nuevamente la operación."
        );

        problem.setTitle("Modificación concurrente");

        return problem;
    }

    @ExceptionHandler({
            MongoTimeoutException.class,
            MongoSocketException.class,
            DataAccessResourceFailureException.class
    })
    public ProblemDetail handlePersistenceUnavailable(
            Exception exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible acceder a la persistencia. "
                        + "Intenta nuevamente más tarde."
        );

        problem.setTitle("Persistencia no disponible");

        return problem;
    }

    public record FieldValidationError(
            String field,
            String message
    ) {
    }
}