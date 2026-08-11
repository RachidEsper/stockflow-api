package com.example.stockflow.product;

import com.example.stockflow.product.dto.ProductRequest;
import com.example.stockflow.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementa los casos de uso y reglas de negocio de los productos.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Crea el servicio con el repositorio que administra la persistencia.
     *
     * @param productRepository repositorio de productos
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lista todos los productos, incluidos los inactivos, ordenados por nombre.
     *
     * @return representaciones públicas de los productos
     */
    public List<ProductResponse> findAll() {
        return productRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador persistente
     * @return producto encontrado
     * @throws ProductNotFoundException cuando el identificador no existe
     */
    public ProductResponse findById(Long id) {
        return toResponse(requireProduct(id));
    }

    /**
     * Crea un producto activo después de normalizar y comprobar su SKU.
     *
     * @param request datos validados del producto
     * @return producto persistido
     * @throws DuplicateSkuException cuando el SKU ya está registrado
     */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String normalizedSku = normalizeSku(request.sku());
        ensureSkuIsAvailableForCreate(normalizedSku);

        Product product = new Product(
                normalizeName(request.name()),
                normalizedSku,
                request.price(),
                request.stock()
        );

        return toResponse(productRepository.save(product));
    }

    /**
     * Reemplaza los datos editables de un producto existente, manteniendo su
     * identificador y estado actuales.
     *
     * @param id identificador del producto que se actualizará
     * @param request nuevos datos validados
     * @return producto actualizado
     * @throws ProductNotFoundException cuando el producto no existe
     * @throws DuplicateSkuException cuando el nuevo SKU pertenece a otro producto
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = requireProduct(id);
        String normalizedSku = normalizeSku(request.sku());
        ensureSkuIsAvailableForUpdate(normalizedSku, id);

        product.setName(normalizeName(request.name()));
        product.setSku(normalizedSku);
        product.setPrice(request.price());
        product.setStock(request.stock());

        return toResponse(productRepository.save(product));
    }

    /**
     * Desactiva lógicamente un producto y conserva su fila para mantener el
     * historial del inventario.
     *
     * @param id identificador del producto que se desactivará
     * @throws ProductNotFoundException cuando el producto no existe
     */
    @Transactional
    public void deactivate(Long id) {
        Product product = requireProduct(id);
        product.deactivate();
        productRepository.save(product);
    }

    /**
     * Recupera una entidad o produce la excepción de dominio correspondiente.
     *
     * @param id identificador solicitado
     * @return entidad persistente
     * @throws ProductNotFoundException cuando no existe
     */
    private Product requireProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Evita crear dos productos con el mismo SKU lógico.
     *
     * @param sku SKU normalizado que se desea crear
     * @throws DuplicateSkuException cuando el SKU ya existe
     */
    private void ensureSkuIsAvailableForCreate(String sku) {
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateSkuException(sku);
        }
    }

    /**
     * Evita asignar a un producto el SKU de otro registro.
     *
     * @param sku SKU normalizado que se desea asignar
     * @param currentProductId identificador del producto que se actualiza
     * @throws DuplicateSkuException cuando otro producto ya utiliza el SKU
     */
    private void ensureSkuIsAvailableForUpdate(String sku, Long currentProductId) {
        productRepository.findBySkuIgnoreCase(sku)
                .filter(existing -> !Objects.equals(existing.getId(), currentProductId))
                .ifPresent(existing -> {
                    throw new DuplicateSkuException(sku);
                });
    }

    /**
     * Elimina espacios externos del nombre.
     *
     * @param name nombre recibido
     * @return nombre normalizado
     */
    private String normalizeName(String name) {
        return name.trim();
    }

    /**
     * Elimina espacios externos y convierte el SKU a mayúsculas de forma
     * independiente del idioma del sistema.
     *
     * @param sku SKU recibido
     * @return SKU normalizado
     */
    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Convierte la entidad interna en el contrato público de respuesta.
     *
     * @param product entidad que se desea representar
     * @return DTO de respuesta
     */
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getStock(),
                product.isActive()
        );
    }
}
