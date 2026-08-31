package com.example.stockflow.customer;

import com.example.stockflow.customer.dto.CustomerRequest;
import com.example.stockflow.customer.dto.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Expone mediante HTTP los casos de uso disponibles para clientes.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Crea el controlador con el servicio que contiene las reglas de negocio.
     *
     * @param customerService servicio de clientes
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Crea un cliente a partir de un cuerpo JSON validado.
     *
     * @param request datos de creación
     * @return respuesta 201, cliente creado y cabecera Location
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse createdCustomer = customerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCustomer.id())
                .toUri();
        return ResponseEntity.created(location).body(createdCustomer);
    }

    /**
     * Lista clientes activos e inactivos ordenados por apellido y nombre.
     *
     * @return respuesta 200 con todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    /**
     * Obtiene un cliente por su identificador.
     *
     * @param id identificador solicitado
     * @return respuesta 200 con el cliente encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    /**
     * Reemplaza los datos editables de un cliente existente.
     *
     * @param id identificador que se actualizará
     * @param request nuevos datos validados
     * @return respuesta 200 con el cliente actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    /**
     * Desactiva un cliente sin borrar su fila de PostgreSQL.
     *
     * @param id identificador que se desactivará
     * @return respuesta 204 sin cuerpo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
