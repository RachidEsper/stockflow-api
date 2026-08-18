package com.example.stockflow.product;

import com.example.stockflow.common.error.GlobalExceptionHandler;
import com.example.stockflow.product.dto.ProductRequest;
import com.example.stockflow.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato HTTP de {@link ProductController} con MockMvc y un
 * servicio simulado, sin iniciar PostgreSQL.
 */
@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    /**
     * Comprueba que un JSON válido produzca 201, la cabecera Location y el
     * producto creado.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void createsProduct() throws Exception {
        ProductResponse response = productResponse();
        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("KEY-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    /**
     * Comprueba que el listado responda 200 y conserve el contrato JSON.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void listsProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mechanical Keyboard"));
    }

    /**
     * Comprueba que la consulta por identificador responda con el producto.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void returnsProductById() throws Exception {
        when(productService.findById(1L)).thenReturn(productResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("KEY-001"));
    }

    /**
     * Comprueba que un producto inexistente se traduzca a 404 y al formato de
     * error uniforme.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("No se encontró el producto con id 99"))
                .andExpect(jsonPath("$.path").value("/api/products/99"));
    }

    /**
     * Comprueba que un cuerpo con campos inválidos se rechace con 400 antes de
     * invocar el servicio.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void rejectsInvalidProduct() throws Exception {
        String invalidJson = """
                {
                  "name": " ",
                  "sku": "",
                  "price": 0,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.sku").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stock").exists());
    }

    /**
     * Comprueba que un JSON mal formado se traduzca al formato uniforme con
     * estado 400.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid-json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El cuerpo de la petición no contiene un JSON válido"));
    }

    /**
     * Comprueba que un SKU duplicado informado por el servicio produzca 409.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void returnsConflictForDuplicateSku() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new DuplicateSkuException("KEY-001"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un producto con el SKU KEY-001"));
    }

    /**
     * Comprueba que una actualización válida responda con el producto editado.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void updatesProduct() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "Updated Keyboard",
                "KEY-001",
                new BigDecimal("149.90"),
                12,
                true
        );
        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Keyboard",
                                  "sku": "KEY-001",
                                  "price": 149.90,
                                  "stock": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Keyboard"))
                .andExpect(jsonPath("$.stock").value(12));
    }

    /**
     * Comprueba que DELETE delegue la desactivación y responda 204 sin cuerpo.
     *
     * @throws Exception cuando MockMvc no puede ejecutar la petición
     */
    @Test
    void deactivatesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deactivate(1L);
    }

    @Test
    void increasesStock() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "Mechanical Keyboard", "KEY-001", new BigDecimal("129.90"), 15, true);
        when(productService.increaseStock(1L, 5)).thenReturn(response);

        mockMvc.perform(patch("/api/products/1/stock/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    void rejectsNonPositiveStockMovement() throws Exception {
        mockMvc.perform(patch("/api/products/1/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void returnsConflictWhenStockIsInsufficient() throws Exception {
        when(productService.decreaseStock(1L, 11))
                .thenThrow(new InsufficientStockException(1L, 10, 11));

        mockMvc.perform(patch("/api/products/1/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":11}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Stock insuficiente para el producto con id 1: disponible 10, solicitado 11"));
    }

    /**
     * Devuelve un JSON válido reutilizado por los escenarios de creación.
     *
     * @return cuerpo JSON de ejemplo
     */
    private String validProductJson() {
        return """
                {
                  "name": "Mechanical Keyboard",
                  "sku": "key-001",
                  "price": 129.90,
                  "stock": 10
                }
                """;
    }

    /**
     * Construye la respuesta simulada reutilizada por los escenarios HTTP.
     *
     * @return producto de ejemplo
     */
    private ProductResponse productResponse() {
        return new ProductResponse(
                1L,
                "Mechanical Keyboard",
                "KEY-001",
                new BigDecimal("129.90"),
                10,
                true
        );
    }
}
