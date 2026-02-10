package com.diovanes.multitenant.entity;

import java.io.Serializable;

/**
 * Entity class representing a Cliente (Client) record from the database.
 * 
 * This class maps to the 'clientes' table in the PostgreSQL database
 * with columns: id, nome, and email.
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String email;

    /**
     * Default constructor.
     */
    public Cliente() {
    }

    /**
     * Constructor with all fields.
     *
     * @param id    the unique identifier
     * @param nome  the client's name
     * @param email the client's email address
     */
    public Cliente(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the client's name.
     *
     * @return the nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Sets the client's name.
     *
     * @param nome the nome to set
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Gets the client's email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the client's email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
