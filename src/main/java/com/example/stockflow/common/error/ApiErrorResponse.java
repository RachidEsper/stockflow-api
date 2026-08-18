package com.example.stockflow.common.error;

import java.time.Instant;
import java.util.Map;

/**
 * Estructura uniforme utilizada por la API para comunicar errores conocidos.
 *
 * @param timestamp instante en que se produjo el error
 * @param status código de estado HTTP
 * @param error descripción estándar del estado HTTP
 * @param message explicación legible del problema
 * @param path ruta que recibió la petición
 * @param fieldErrors errores de validación asociados a campos concretos
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
