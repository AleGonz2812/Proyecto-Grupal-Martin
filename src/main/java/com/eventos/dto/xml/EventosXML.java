package com.eventos.dto.xml;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper para exportar múltiples eventos en un solo archivo XML
 */
@XmlRootElement(name = "eventos")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventosXML {
    
    @XmlElement(name = "evento")
    private List<EventoXML> eventos = new ArrayList<>();
    
    // Constructores
    public EventosXML() {
    }
    
    public EventosXML(List<EventoXML> eventos) {
        this.eventos = eventos;
    }
    
    // Getters y Setters
    public List<EventoXML> getEventos() {
        return eventos;
    }
    
    public void setEventos(List<EventoXML> eventos) {
        this.eventos = eventos;
    }
}
