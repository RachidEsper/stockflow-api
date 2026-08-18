package com.example.stockflow.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Representa un producto administrado por StockFlow y define su mapeo hacia
 * la tabla {@code products}.
 */
@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String sku;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Constructor requerido por JPA para reconstruir entidades desde la base.
     */
    protected Product() {
    }

    /**
     * Crea un producto nuevo y activo. El identificador será asignado por
     * PostgreSQL cuando la entidad se persista.
     *
     * @param name nombre visible del producto
     * @param sku código interno único
     * @param price precio unitario
     * @param stock cantidad disponible
     */
    public Product(String name, String sku, BigDecimal price, Integer stock) {
        this(name, null, sku, price, stock);
    }

    public Product(String name, String description, String sku, BigDecimal price, Integer stock) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Devuelve el identificador persistente del producto.
     *
     * @return identificador generado o {@code null} antes de persistir
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el nombre del producto.
     *
     * @return nombre visible
     */
    public String getName() {
        return name;
    }

    /**
     * Actualiza el nombre del producto.
     *
     * @param name nuevo nombre visible
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Devuelve el SKU del producto.
     *
     * @return código interno único
     */
    public String getSku() {
        return sku;
    }

    /**
     * Actualiza el SKU del producto.
     *
     * @param sku nuevo código interno
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Devuelve el precio unitario.
     *
     * @return precio del producto
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Actualiza el precio unitario.
     *
     * @param price nuevo precio positivo
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Devuelve la cantidad disponible.
     *
     * @return unidades en stock
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Actualiza la cantidad disponible.
     *
     * @param stock nueva cantidad, igual o mayor que cero
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * Indica si el producto puede utilizarse actualmente.
     *
     * @return {@code true} cuando el producto está activo
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Desactiva el producto sin eliminar su registro histórico.
     */
    public void deactivate() {
        this.active = false;
    }
}
