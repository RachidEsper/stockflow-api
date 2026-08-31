package com.example.stockflow.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa un cliente administrado por StockFlow y define su mapeo hacia
 * la tabla {@code customers}.
 */
@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String firstName;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Constructor requerido por JPA para reconstruir entidades desde la base.
     */
    protected Customer() {
    }

    /**
     * Crea un cliente nuevo y activo.
     *
     * @param firstName nombre del cliente
     * @param lastName apellido del cliente
     * @param email correo electrónico único
     */
    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Desactiva el cliente sin eliminar su registro persistente.
     */
    public void deactivate() {
        this.active = false;
    }
}
