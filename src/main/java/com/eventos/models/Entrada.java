package com.eventos.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Entrada a un evento
 */
@Entity
@Table(name = "entradas")
public class Entrada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_entrada_id", nullable = false)
    private TipoEntrada tipoEntrada;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numeroEntrada;
    
    @Column(nullable = false)
    private Boolean validada = false;
    
    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;
    
    @Column(name = "codigo_qr", unique = true, length = 500)
    private String codigoQR;
    
    // Constructores
    public Entrada() {
    }
    
    @PrePersist
    protected void onCreate() {
        if (numeroEntrada == null) {
            numeroEntrada = generarNumeroEntrada();
        }
    }
    
    private String generarNumeroEntrada() {
        return "ENT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }
    
    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }
    
    public Evento getEvento() {
        return evento;
    }
    
    public void setEvento(Evento evento) {
        this.evento = evento;
    }
    
    public Compra getCompra() {
        return compra;
    }
    
    public void setCompra(Compra compra) {
        this.compra = compra;
    }
    
    public String getNumeroEntrada() {
        return numeroEntrada;
    }
    
    public void setNumeroEntrada(String numeroEntrada) {
        this.numeroEntrada = numeroEntrada;
    }
    
    public Boolean getValidada() {
        return validada;
    }
    
    public void setValidada(Boolean validada) {
        this.validada = validada;
    }
    
    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }
    
    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }
    
    public String getCodigoQR() {
        return codigoQR;
    }
    
    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }
}
