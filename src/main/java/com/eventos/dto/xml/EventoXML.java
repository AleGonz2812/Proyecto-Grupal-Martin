package com.eventos.dto.xml;

import jakarta.xml.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * DTO para exportar/importar Eventos en formato XML
 * Usa JAXB para la conversión automática
 */
@XmlRootElement(name = "evento")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventoXML {
    
    @XmlElement(required = true)
    private Long id;
    
    @XmlElement(required = true)
    private String nombre;
    
    @XmlElement
    private String descripcion;
    
    @XmlElement(required = true)
    private String fechaInicio;
    
    @XmlElement(required = true)
    private String fechaFin;
    
    @XmlElement(required = true)
    private Integer aforoMaximo;
    
    @XmlElement
    private Integer aforoActual;
    
    @XmlElement(required = true)
    private String estado;
    
    @XmlElement
    private Double precioBase;
    
    @XmlElement
    private String sede;
    
    @XmlElement
    private String tipoEvento;
    
    // Constructores
    public EventoXML() {
    }
    
    public EventoXML(Long id, String nombre, String descripcion, String fechaInicio, 
                     String fechaFin, Integer aforoMaximo, Integer aforoActual, 
                     String estado, Double precioBase, String sede, String tipoEvento) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.aforoMaximo = aforoMaximo;
        this.aforoActual = aforoActual;
        this.estado = estado;
        this.precioBase = precioBase;
        this.sede = sede;
        this.tipoEvento = tipoEvento;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    
    public Integer getAforoMaximo() { return aforoMaximo; }
    public void setAforoMaximo(Integer aforoMaximo) { this.aforoMaximo = aforoMaximo; }
    
    public Integer getAforoActual() { return aforoActual; }
    public void setAforoActual(Integer aforoActual) { this.aforoActual = aforoActual; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }
    
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
}
