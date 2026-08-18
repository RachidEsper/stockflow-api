package com.example.stockflow.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Proporciona acceso persistente a los productos mediante Spring Data JPA.
 *
 * <p>Los métodos CRUD básicos se heredan de {@link JpaRepository}. Spring Data
 * genera las consultas de los métodos adicionales a partir de sus nombres.</p>
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca un producto por SKU sin distinguir entre mayúsculas y minúsculas.
     *
     * @param sku código interno del producto
     * @return el producto encontrado o un valor vacío si no existe
     */
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Comprueba si existe un producto con el SKU indicado sin distinguir
     * entre mayúsculas y minúsculas.
     *
     * @param sku código interno que se desea comprobar
     * @return {@code true} cuando el SKU ya está registrado
     */
    boolean existsBySkuIgnoreCase(String sku);

    /**
     * Recupera todos los productos ordenados alfabéticamente por nombre.
     *
     * @return productos activos e inactivos ordenados por nombre ascendente
     */
    List<Product> findAllByOrderByNameAsc();
}
