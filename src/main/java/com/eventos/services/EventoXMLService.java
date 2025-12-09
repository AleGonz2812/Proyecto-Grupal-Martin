package com.eventos.services;

import com.eventos.dto.xml.EventoXML;
import com.eventos.dto.xml.EventosXML;
import com.eventos.models.Evento;
import com.eventos.repositories.EventoRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para exportar e importar eventos en formato XML
 * Usa JAXB para la serialización
 */
public class EventoXMLService {
    
    private final EventoRepository eventoRepo;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public EventoXMLService() {
        this.eventoRepo = new EventoRepository();
    }
    
    /**
     * Exporta todos los eventos a un archivo XML
     * @param rutaArchivo Ruta donde guardar el XML
     * @return true si se exportó correctamente
     */
    public boolean exportarEventos(String rutaArchivo) {
        try {
            // Obtener todos los eventos de la BD
            List<Evento> eventos = eventoRepo.findAll();
            
            // Convertir entidades a DTOs
            List<EventoXML> eventosXML = new ArrayList<>();
            for (Evento evento : eventos) {
                EventoXML eventoXML = convertirAEventoXML(evento);
                eventosXML.add(eventoXML);
            }
            
            // Crear wrapper con la lista
            EventosXML eventosWrapper = new EventosXML(eventosXML);
            
            // Crear contexto JAXB y marshaller
            JAXBContext context = JAXBContext.newInstance(EventosXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            // Exportar a archivo
            File archivo = new File(rutaArchivo);
            marshaller.marshal(eventosWrapper, archivo);
            
            System.out.println("✓ Eventos exportados correctamente a: " + rutaArchivo);
            return true;
            
        } catch (JAXBException e) {
            System.err.println("✗ Error al exportar eventos a XML: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Importa eventos desde un archivo XML
     * @param rutaArchivo Ruta del archivo XML
     * @return Lista de eventos importados
     */
    public List<EventoXML> importarEventos(String rutaArchivo) {
        try {
            // Crear contexto JAXB y unmarshaller
            JAXBContext context = JAXBContext.newInstance(EventosXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            // Leer archivo XML
            File archivo = new File(rutaArchivo);
            EventosXML eventosWrapper = (EventosXML) unmarshaller.unmarshal(archivo);
            
            System.out.println("✓ Eventos importados correctamente desde: " + rutaArchivo);
            return eventosWrapper.getEventos();
            
        } catch (JAXBException e) {
            System.err.println("✗ Error al importar eventos desde XML: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Convierte una entidad Evento a EventoXML
     * Nota: El estado se exporta como texto descriptivo
     */
    private EventoXML convertirAEventoXML(Evento evento) {
        return new EventoXML(
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
