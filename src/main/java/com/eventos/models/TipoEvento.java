package com.eventos.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Tipo de Evento
 */
@Entity
@Table(name = "tipos_evento")
public class TipoEvento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 255)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategoriaEvento categoria;
    
    @OneToMany(mappedBy = "tipoEvento")
    private List<Evento> eventos = new ArrayList<>();
    
    // Constructores
    public TipoEvento() {
    }
    
    public TipoEvento(String nombre, String descripcion, CategoriaEvento categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public CategoriaEvento getCategoria() {
        return categoria;
    }
    
    public void setCategoria(CategoriaEvento categoria) {
        this.categoria = categoria;
    }
    
    public List<Evento> getEventos() {
        return eventos;
    }
    
    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
}

/**
 * Categorías de eventos
 */
enum CategoriaEvento {
    CULTURAL,
    DEPORTIVO,
    CORPORATIVO,
    ENTRETENIMIENTO
}
