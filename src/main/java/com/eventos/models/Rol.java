package com.eventos.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Rol en el sistema
 */
@Entity
@Table(name = "roles")
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // ADMIN, USUARIO, EMPLEADO
    
    @Column(length = 255)
    private String descripcion;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permisos_rol", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso")
    private List<String> permisos = new ArrayList<>();
    
    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios = new ArrayList<>();
    
    // Constructores
    public Rol() {
    }
    
    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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
    
    public List<String> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }
    
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
    
    // Métodos de utilidad
    public void addPermiso(String permiso) {
        if (!this.permisos.contains(permiso)) {
            this.permisos.add(permiso);
        }
    }
    
    public boolean tienePermiso(String permiso) {
        return this.permisos.contains(permiso);
    }
    
    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
