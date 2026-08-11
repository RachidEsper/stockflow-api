package com.example.stockflow.common.error;

import com.example.stockflow.product.DuplicateSkuException;
import com.example.stockflow.product.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce excepciones conocidas a respuestas HTTP consistentes para todos los
 * controladores de la aplicación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Crea el manejador que centraliza los errores de los controladores REST.
     */
    public GlobalExceptionHandler() {
    }

    /**
     * Convierte la ausencia de un producto en una respuesta 404.
     *
     * @param exception excepción producida por el servicio
     * @param request petición HTTP original
     * @return detalle uniforme del error
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Convierte un conflicto de SKU en una respuesta 409.
     *
     * @param exception excepción producida por el servicio
     * @param request petición HTTP original
     * @return detalle uniforme del conflicto
     */
    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSku(
            DuplicateSkuException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Convierte las infracciones de Bean Validation en una respuesta 400 que
     * identifica cada campo inválido.
     *
     * @param exception validaciones rechazadas por Spring MVC
     * @param request petición HTTP original
     * @return detalle uniforme con errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (firstMessage, ignoredMessage) -> firstMessage,
                        LinkedHashMap::new
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "La petición contiene valores inválidos",
                request.getRequestURI(),
                fieldErrors
        );
    }

    /**
     * Convierte un cuerpo JSON ausente o mal formado en una respuesta 400.
     *
     * @param exception error producido al interpretar el cuerpo
     * @param request petición HTTP original
     * @return detalle uniforme del error de lectura
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no contiene un JSON válido",
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Construye la representación común y su estado HTTP.
     *
     * @param status estado HTTP que se devolverá
     * @param message explicación del error
     * @param path ruta solicitada
     * @param fieldErrors errores asociados a campos, si existen
     * @return respuesta HTTP lista para enviarse
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}
