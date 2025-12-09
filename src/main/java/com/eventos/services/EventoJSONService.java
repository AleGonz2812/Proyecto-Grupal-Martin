package com.eventos.services;

import com.eventos.dto.json.EventoJSON;
import com.eventos.models.Evento;
import com.eventos.repositories.EventoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para exportar e importar eventos en formato JSON
 * Usa Jackson para la serialización
 */
public class EventoJSONService {
    
    private final EventoRepository eventoRepo;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public EventoJSONService() {
        this.eventoRepo = new EventoRepository();
        this.objectMapper = new ObjectMapper();
        // Configurar formato legible
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Exporta todos los eventos a un archivo JSON
     * @param rutaArchivo Ruta donde guardar el JSON
     * @return true si se exportó correctamente
     */
    public boolean exportarEventos(String rutaArchivo) {
        try {
            // Obtener todos los eventos de la BD
            List<Evento> eventos = eventoRepo.findAll();
            
            // Convertir entidades a DTOs
            List<EventoJSON> eventosJSON = new ArrayList<>();
            for (Evento evento : eventos) {
                EventoJSON eventoJSON = convertirAEventoJSON(evento);
                eventosJSON.add(eventoJSON);
            }
            
            // Exportar a archivo
            File archivo = new File(rutaArchivo);
            objectMapper.writeValue(archivo, eventosJSON);
            
            System.out.println("✓ Eventos exportados correctamente a: " + rutaArchivo);
            return true;
            
        } catch (IOException e) {
            System.err.println("✗ Error al exportar eventos a JSON: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Importa eventos desde un archivo JSON
     * @param rutaArchivo Ruta del archivo JSON
     * @return Lista de eventos importados
     */
    public List<EventoJSON> importarEventos(String rutaArchivo) {
        try {
            // Leer archivo JSON
            File archivo = new File(rutaArchivo);
            EventoJSON[] eventosArray = objectMapper.readValue(archivo, EventoJSON[].class);
            
            // Convertir array a lista
            List<EventoJSON> eventos = new ArrayList<>();
            for (EventoJSON evento : eventosArray) {
                eventos.add(evento);
            }
            
            System.out.println("✓ Eventos importados correctamente desde: " + rutaArchivo);
            return eventos;
            
        } catch (IOException e) {
            System.err.println("✗ Error al importar eventos desde JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Exporta un solo evento a JSON (útil para confirmaciones)
     * @param evento Evento a exportar
     * @param rutaArchivo Ruta del archivo
     * @return true si se exportó correctamente
     */
    public boolean exportarEvento(Evento evento, String rutaArchivo) {
        try {
            EventoJSON eventoJSON = convertirAEventoJSON(evento);
            File archivo = new File(rutaArchivo);
            objectMapper.writeValue(archivo, eventoJSON);
            
            System.out.println("✓ Evento exportado correctamente a: " + rutaArchivo);
            return true;
            
        } catch (IOException e) {
            System.err.println("✗ Error al exportar evento a JSON: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convierte una entidad Evento a EventoJSON
     * Nota: El estado se exporta como texto descriptivo
     */
    private EventoJSON convertirAEventoJSON(Evento evento) {
        return new EventoJSON(
            evento.getId(),
            evento.getNombre(),
            evento.getDescripcion(),
            evento.getFechaInicio().format(FORMATTER),
            evento.getFechaFin().format(FORMATTER),
            evento.getAforoMaximo(),
            evento.getAforoActual(),
            "PROGRAMADO", // Estado simplificado para exportación
            evento.getPrecioBase() != null ? evento.getPrecioBase().doubleValue() : null,
            evento.getSede() != null ? evento.getSede().getNombre() : null,
            evento.getTipoEvento() != null ? evento.getTipoEvento().getNombre() : null
        );
    }
}
