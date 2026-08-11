package com.example.stockflow.product;

import com.example.stockflow.product.dto.ProductRequest;
import com.example.stockflow.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica las reglas de negocio de {@link ProductService} de forma aislada,
 * sustituyendo el repositorio real por un mock de Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    /**
     * Comprueba que el servicio transforme en respuestas el listado ordenado
     * proporcionado por el repositorio.
     */
    @Test
    void listsProductsOrderedByName() {
        Product keyboard = persistedProduct(
                1L,
                "Mechanical Keyboard",
                "KEY-001",
                "129.90",
                10
        );
        Product mouse = persistedProduct(
                2L,
                "Wireless Mouse",
                "MOU-001",
                "59.90",
                5
        );
        when(productRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(keyboard, mouse));

        List<ProductResponse> result = productService.findAll();

        assertEquals(List.of("Mechanical Keyboard", "Wireless Mouse"),
                result.stream().map(ProductResponse::name).toList());
    }

    /**
     * Comprueba que un producto existente pueda obtenerse por identificador y
     * se convierta al DTO público.
     */
    @Test
    void returnsProductById() {
        Product product = persistedProduct(
                1L,
                "Mechanical Keyboard",
                "KEY-001",
                "129.90",
                10
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertAll(
                () -> assertEquals(1L, result.id()),
                () -> assertEquals("Mechanical Keyboard", result.name()),
                () -> assertEquals("KEY-001", result.sku()),
                () -> assertTrue(result.active())
        );
    }

    /**
     * Comprueba que una búsqueda inexistente produzca la excepción de dominio.
     */
    @Test
    void throwsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(99L)
        );

        assertEquals("No se encontró el producto con id 99", exception.getMessage());
    }

    /**
     * Comprueba que la creación recorte el nombre, normalice el SKU y persista
     * un producto activo.
     */
    @Test
    void createsProductWithNormalizedData() {
        ProductRequest request = new ProductRequest(
                "  Mechanical Keyboard  ",
                " key-001 ",
                new BigDecimal("129.90"),
                10
        );
        when(productRepository.existsBySkuIgnoreCase("KEY-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        ProductResponse result = productService.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertAll(
                () -> assertEquals(1L, result.id()),
                () -> assertEquals("Mechanical Keyboard", saved.getName()),
                () -> assertEquals("KEY-001", saved.getSku()),
                () -> assertTrue(saved.isActive())
        );
    }

    /**
     * Comprueba que la creación rechace un SKU existente antes de intentar
     * persistir una entidad.
     */
    @Test
    void rejectsDuplicateSkuWhenCreating() {
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "key-001",
                new BigDecimal("129.90"),
                10
        );
        when(productRepository.existsBySkuIgnoreCase("KEY-001")).thenReturn(true);

        DuplicateSkuException exception = assertThrows(
                DuplicateSkuException.class,
                () -> productService.create(request)
        );

        assertEquals("Ya existe un producto con el SKU KEY-001", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Comprueba que una actualización modifique los datos editables y permita
     * conservar el SKU del mismo producto.
     */
    @Test
    void updatesExistingProduct() {
        Product product = persistedProduct(
                1L,
                "Old Keyboard",
                "KEY-001",
                "99.90",
                3
        );
        ProductRequest request = new ProductRequest(
                "  Mechanical Keyboard  ",
                " key-001 ",
                new BigDecimal("129.90"),
                10
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuIgnoreCase("KEY-001"))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductResponse result = productService.update(1L, request);

        assertAll(
                () -> assertEquals("Mechanical Keyboard", result.name()),
                () -> assertEquals("KEY-001", result.sku()),
                () -> assertEquals(new BigDecimal("129.90"), result.price()),
                () -> assertEquals(10, result.stock())
        );
    }

    /**
     * Comprueba que una actualización no pueda utilizar el SKU perteneciente a
     * otro producto.
     */
    @Test
    void rejectsAnotherProductsSkuWhenUpdating() {
        Product product = persistedProduct(
                1L,
                "Mechanical Keyboard",
                "KEY-001",
                "129.90",
                10
        );
        Product conflictingProduct = persistedProduct(
                2L,
                "Wireless Mouse",
                "MOU-001",
                "59.90",
                5
        );
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "mou-001",
                new BigDecimal("129.90"),
                10
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuIgnoreCase("MOU-001"))
                .thenReturn(Optional.of(conflictingProduct));

        assertThrows(DuplicateSkuException.class,
                () -> productService.update(1L, request));
        verify(productRepository, never()).save(product);
    }

    /**
     * Comprueba que eliminar desde la API desactive la entidad en lugar de
     * borrarla físicamente.
     */
    @Test
    void deactivatesProductWithoutDeletingIt() {
        Product product = persistedProduct(
                1L,
                "Mechanical Keyboard",
                "KEY-001",
                "129.90",
                10
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.deactivate(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
        verify(productRepository, never()).delete(any(Product.class));
    }

    /**
     * Construye una entidad que simula haber sido persistida asignándole un ID
     * mediante la utilidad de reflexión de Spring para tests.
     *
     * @param id identificador simulado
     * @param name nombre del producto
     * @param sku código interno
     * @param price precio expresado como texto decimal
     * @param stock cantidad disponible
     * @return producto con identificador asignado
     */
    private Product persistedProduct(
            Long id,
            String name,
            String sku,
            String price,
            Integer stock
    ) {
        Product product = new Product(name, sku, new BigDecimal(price), stock);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
