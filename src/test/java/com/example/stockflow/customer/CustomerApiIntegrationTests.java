package com.example.stockflow.customer;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el recorrido completo desde HTTP hasta PostgreSQL utilizando los
 * componentes reales de la funcionalidad de clientes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void clearCustomers() {
        customerRepository.deleteAllInBatch();
    }

    @Test
    void createsAndRetrievesCustomerThroughAllLayers() throws Exception {
        String requestBody = """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ADA@Example.COM"
                }
                """;

        MvcResult creationResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andReturn();

        Number id = JsonPath.read(
                creationResult.getResponse().getContentAsString(),
                "$.id"
        );

        mockMvc.perform(get("/api/customers/{id}", id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.active").value(true));

        assertEquals(1L, customerRepository.count());
    }

    @Test
    void deleteDeactivatesCustomerWithoutRemovingIt() throws Exception {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Ada", "Lovelace", "ada@example.com")
        );

        mockMvc.perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        Customer persistedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertFalse(persistedCustomer.isActive());
        assertEquals(1L, customerRepository.count());
    }
}
