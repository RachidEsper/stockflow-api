package com.example.stockflow.customer;

import com.example.stockflow.common.error.GlobalExceptionHandler;
import com.example.stockflow.customer.dto.CustomerRequest;
import com.example.stockflow.customer.dto.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato HTTP de {@link CustomerController} con MockMvc y un
 * servicio simulado, sin iniciar PostgreSQL.
 */
@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void createsCustomer() throws Exception {
        when(customerService.create(any(CustomerRequest.class))).thenReturn(customerResponse());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listsCustomers() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(customerResponse()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[0].lastName").value("Lovelace"));
    }

    @Test
    void returnsCustomerById() throws Exception {
        when(customerService.findById(1L)).thenReturn(customerResponse());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void returnsNotFoundForUnknownCustomer() throws Exception {
        when(customerService.findById(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("No se encontró el cliente con id 99"))
                .andExpect(jsonPath("$.path").value("/api/customers/99"));
    }

    @Test
    void rejectsInvalidCustomer() throws Exception {
        String invalidJson = """
                {
                  "firstName": " ",
                  "lastName": "",
                  "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid-json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El cuerpo de la petición no contiene un JSON válido"));
    }

    @Test
    void returnsConflictForDuplicateEmail() throws Exception {
        when(customerService.create(any(CustomerRequest.class)))
                .thenThrow(new DuplicateEmailException("ada@example.com"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un cliente con el email ada@example.com"))
                .andExpect(jsonPath("$.path").value("/api/customers"));
    }

    @Test
    void updatesCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse(
                1L,
                "Augusta Ada",
                "Lovelace",
                "ada.lovelace@example.com",
                true
        );
        when(customerService.update(eq(1L), any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Augusta Ada",
                                  "lastName": "Lovelace",
                                  "email": "ada.lovelace@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Augusta Ada"))
                .andExpect(jsonPath("$.email").value("ada.lovelace@example.com"));
    }

    @Test
    void deactivatesCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deactivate(1L);
    }

    private String validCustomerJson() {
        return """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ADA@Example.COM"
                }
                """;
    }

    private CustomerResponse customerResponse() {
        return new CustomerResponse(
                1L,
                "Ada",
                "Lovelace",
                "ada@example.com",
                true
        );
    }
}
