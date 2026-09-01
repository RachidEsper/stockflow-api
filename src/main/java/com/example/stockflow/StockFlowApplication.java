package com.example.stockflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API StockFlow.
 */
@SpringBootApplication
public class StockFlowApplication {

    /**
     * Crea la configuración principal que Spring Boot utiliza para iniciar el
     * contexto de la aplicación.
     */
    public StockFlowApplication() {
    }

    /**
     * Inicia Spring Boot y el servidor HTTP embebido.
     *
     * @param args argumentos recibidos desde la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(StockFlowApplication.class, args);
    }

}
