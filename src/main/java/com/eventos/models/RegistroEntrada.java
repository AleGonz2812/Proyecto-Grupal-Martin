package com.eventos.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa el Registro de entrada a un evento
 */
@Entity
@Table(name = "registros_entrada")
public class RegistroEntrada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entrada_id", nullable = false)
    private Entrada entrada;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    
    @Column(name = "fecha_hora_entrada", nullable = false)
    private LocalDateTime fechaHoraEntrada;
    
    @Column(length = 100)
    private String empleadoValidador;
    
    @Column(length = 255)
    private String observaciones;
    
    // Constructores
    public RegistroEntrada() {
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaHoraEntrada == null) {
            fechaHoraEntrada = LocalDateTime.now();
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Entrada getEntrada() {
        return entrada;
    }
    
    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }
    
    public Evento getEvento() {
        return evento;
    }
    
    public void setEvento(Evento evento) {
        this.evento = evento;
    }
    
    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }
    
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }
    
    public String getEmpleadoValidador() {
        return empleadoValidador;
    }
    
    public void setEmpleadoValidador(String empleadoValidador) {
        this.empleadoValidador = empleadoValidador;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
