package com.eventos.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Compra de entradas
 */
@Entity
@Table(name = "compras")
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado;
    
    @Column(name = "codigo_confirmacion", unique = true, length = 100)
    private String codigoConfirmacion;
    
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;
    
    @Column(name = "confirmacion_json", columnDefinition = "TEXT")
    private String confirmacionJSON;
    
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entrada> entradas = new ArrayList<>();
    
    // Constructores
    public Compra() {
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCompra.PENDIENTE;
        }
        if (codigoConfirmacion == null) {
            codigoConfirmacion = generarCodigoConfirmacion();
        }
    }
    
    private String generarCodigoConfirmacion() {
        return "COMP-" + System.currentTimeMillis();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }
    
    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public EstadoCompra getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoCompra estado) {
        this.estado = estado;
    }
    
    public String getCodigoConfirmacion() {
        return codigoConfirmacion;
    }
    
    public void setCodigoConfirmacion(String codigoConfirmacion) {
        this.codigoConfirmacion = codigoConfirmacion;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public String getConfirmacionJSON() {
        return confirmacionJSON;
    }
    
    public void setConfirmacionJSON(String confirmacionJSON) {
        this.confirmacionJSON = confirmacionJSON;
    }
    
    public List<Entrada> getEntradas() {
        return entradas;
    }
    
    public void setEntradas(List<Entrada> entradas) {
        this.entradas = entradas;
    }
    
    // Métodos de utilidad
    public void addEntrada(Entrada entrada) {
        entradas.add(entrada);
        entrada.setCompra(this);
    }
    
    public void calcularTotal() {
        this.total = entradas.stream()
            .map(e -> e.getTipoEntrada().getPrecio())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

/**
 * Estados posibles de una compra
 */
enum EstadoCompra {
    PENDIENTE,
    COMPLETADA,
    CANCELADA,
    REEMBOLSADA
}
