package com.eventos.models;

import jakarta.persistence.*;

/**
 * Entidad que representa el Equipamiento de una sede
 */
@Entity
@Table(name = "equipamiento")
public class Equipamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEquipamiento estado;
    
    @Column(length = 100)
    private String categoria;
    
    // Constructores
    public Equipamiento() {
    }
    
    public Equipamiento(String nombre, Integer cantidad, EstadoEquipamiento estado) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.estado = estado;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Sede getSede() {
        return sede;
    }
    
    public void setSede(Sede sede) {
        this.sede = sede;
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
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public EstadoEquipamiento getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoEquipamiento estado) {
        this.estado = estado;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}

/**
 * Estados posibles del equipamiento
 */
enum EstadoEquipamiento {
    DISPONIBLE,
    EN_USO,
    MANTENIMIENTO,
    DAÑADO
}
