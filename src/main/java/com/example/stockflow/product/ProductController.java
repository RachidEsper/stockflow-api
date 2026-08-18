package com.example.stockflow.product;

import com.example.stockflow.product.dto.ProductRequest;
import com.example.stockflow.product.dto.ProductResponse;
import com.example.stockflow.product.dto.StockMovementRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Expone mediante HTTP los casos de uso disponibles para productos.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Crea el controlador con el servicio que contiene las reglas de negocio.
     *
     * @param productService servicio de productos
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Crea un producto a partir de un cuerpo JSON validado.
     *
     * @param request datos de creación
     * @return respuesta 201, producto creado y cabecera Location
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse createdProduct = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.id())
                .toUri();
        return ResponseEntity.created(location).body(createdProduct);
    }

    /**
     * Lista productos activos e inactivos ordenados por nombre.
     *
     * @return respuesta 200 con todos los productos
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador solicitado
     * @return respuesta 200 con el producto encontrado
     * @throws ProductNotFoundException cuando el producto no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    /**
     * Reemplaza los datos editables de un producto existente.
     *
     * @param id identificador que se actualizará
     * @param request nuevos datos validados
     * @return respuesta 200 con el producto actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * Desactiva un producto sin borrar su fila de PostgreSQL.
     *
     * @param id identificador que se desactivará
     * @return respuesta 204 sin cuerpo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<ProductResponse> increaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request
    ) {
        return ResponseEntity.ok(productService.increaseStock(id, request.quantity()));
    }

    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<ProductResponse> decreaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request
    ) {
        return ResponseEntity.ok(productService.decreaseStock(id, request.quantity()));
    }
}
