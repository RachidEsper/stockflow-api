package com.example.stockflow.product;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el recorrido completo desde HTTP hasta PostgreSQL utilizando los
 * componentes reales de la funcionalidad de productos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Deja una vista temporal vacía de la tabla para aislar el escenario. La
     * transacción restaura cualquier dato previo al finalizar la prueba.
     */
    @BeforeEach
    void clearProducts() {
        productRepository.deleteAllInBatch();
    }

    /**
     * Crea un producto mediante POST, recupera el identificador generado y lo
     * consulta con GET, comprobando además que existe una sola fila durante la
     * transacción. Todos los cambios se revierten al terminar.
     *
     * @throws Exception cuando MockMvc no puede ejecutar alguna petición
     */
    @Test
    void createsAndRetrievesProductThroughAllLayers() throws Exception {
        String requestBody = """
                {
                  "name": "Mechanical Keyboard",
                  "sku": "key-001",
                  "price": 129.90,
                  "stock": 10
                }
                """;

        MvcResult creationResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("KEY-001"))
                .andReturn();

        Number id = JsonPath.read(
                creationResult.getResponse().getContentAsString(),
                "$.id"
        );

        mockMvc.perform(get("/api/products/{id}", id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.active").value(true));

        assertEquals(1L, productRepository.count());
    }
}
