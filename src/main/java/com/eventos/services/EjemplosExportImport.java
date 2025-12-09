package com.eventos.services;

import com.eventos.dto.json.EventoJSON;
import com.eventos.dto.xml.EventoXML;

import java.util.List;

/**
 * EJEMPLOS DE USO - Servicios XML y JSON
 * 
 * Copiar el código que necesites para tu aplicación
 */
public class EjemplosExportImport {
    
    public static void main(String[] args) {
        
        // ============================================
        // EJEMPLO 1: EXPORTAR EVENTOS A XML
        // ============================================
        EventoXMLService xmlService = new EventoXMLService();
        xmlService.exportarEventos("exports/xml/eventos.xml");
        
        // ============================================
        // EJEMPLO 2: IMPORTAR EVENTOS DESDE XML
        // ============================================
        List<EventoXML> eventosXML = xmlService.importarEventos("imports/xml/eventos.xml");
        System.out.println("Eventos importados de XML: " + eventosXML.size());
        
        // Ver datos de cada evento
        for (EventoXML evento : eventosXML) {
            System.out.println("- " + evento.getNombre() + " (" + evento.getFechaInicio() + ")");
        }
        
        // ============================================
        // EJEMPLO 3: EXPORTAR EVENTOS A JSON
        // ============================================
        EventoJSONService jsonService = new EventoJSONService();
        jsonService.exportarEventos("exports/json/eventos.json");
        
        // ============================================
        // EJEMPLO 4: IMPORTAR EVENTOS DESDE JSON
        // ============================================
        List<EventoJSON> eventosJSON = jsonService.importarEventos("imports/json/eventos.json");
        System.out.println("Eventos importados de JSON: " + eventosJSON.size());
        
        // Ver datos de cada evento
        for (EventoJSON evento : eventosJSON) {
            System.out.println("- " + evento.getNombre() + " (" + evento.getFechaInicio() + ")");
        }
        
        // ============================================
        // EJEMPLO 5: USO EN UN BOTÓN DE EXPORTACIÓN
        // ============================================
        /*
        btnExportarXML.setOnAction(e -> {
            EventoXMLService service = new EventoXMLService();
            boolean exito = service.exportarEventos("exports/xml/eventos_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xml");
            
            if (exito) {
                mostrarAlerta("Exportación exitosa", "Eventos exportados a XML");
            }
        });
        */
        
        // ============================================
        // EJEMPLO 6: USO EN UN BOTÓN DE IMPORTACIÓN
        // ============================================
        /*
        btnImportarJSON.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo JSON");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            
            File archivo = fileChooser.showOpenDialog(stage);
            if (archivo != null) {
                EventoJSONService service = new EventoJSONService();
                List<EventoJSON> eventos = service.importarEventos(archivo.getAbsolutePath());
                
                // Aquí procesarías los eventos importados
                System.out.println("Se importaron " + eventos.size() + " eventos");
            }
        });
        */
    }
}
