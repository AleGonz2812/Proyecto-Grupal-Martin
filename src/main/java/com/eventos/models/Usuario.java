package com.eventos.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Usuario en el sistema
 */
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password; // Hash BCrypt
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 100)
    private String apellidos;
    
    @Column(length = 20)
    private String telefono;

    @Column(unique = true, length = 12)
    private String dni;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;
    
    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    // Constructores
    public Usuario() {
    }
    
    public Usuario(String email, String password, String nombre, Rol rol) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.rol = rol;
        this.fechaAlta = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    
    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
    
    public LocalDateTime getUltimaConexion() {
        return ultimaConexion;
    }
    
    public void setUltimaConexion(LocalDateTime ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }
    
    public List<Compra> getCompras() {
        return compras;
    }
    
    public void setCompras(List<Compra> compras) {
        this.compras = compras;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return apellidos != null ? nombre + " " + apellidos : nombre;
    }
    
    public boolean esAdmin() {
        return rol != null && "ADMIN".equals(rol.getNombre());
    }
    
    public boolean esEmpleado() {
        return rol != null && "EMPLEADO".equals(rol.getNombre());
    }
    
    public boolean tienePermiso(String permiso) {
        return rol != null && rol.tienePermiso(permiso);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", rol=" + (rol != null ? rol.getNombre() : "null") +
                ", activo=" + activo +
                '}';
    }
}
