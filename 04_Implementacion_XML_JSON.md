# Guía de Implementación XML/JSON - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Versión:** 1.0  
**Fecha:** 4 de noviembre de 2025

---

## 📋 Casos de Uso XML y JSON

### **Cumplimiento del Enunciado**

El proyecto debe trabajar con **XML** y **JSON**. A continuación se detallan los usos prácticos:

| Formato | Uso Principal | Ejemplo Práctico |
|---------|---------------|------------------|
| **XML** | Exportación/Importación de eventos | Generar catálogo de eventos para distribución |
| **XML** | Configuración de sedes y equipamiento | Backup y restauración de configuraciones |
| **XML** | Informes estructurados | Reportes de ventas y estadísticas |
| **JSON** | Confirmaciones de compra | Email con detalles de la compra |
| **JSON** | API de comunicación | Intercambio de datos entre módulos |
| **JSON** | Datos de códigos QR | Información embebida en el QR |

---

## 🔧 Implementación XML con JAXB

### **1. Exportación de Eventos a XML**

#### **Clase EventoXML (DTO para exportación)**

```java
package com.eventos.dto.xml;

import jakarta.xml.bind.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@XmlRootElement(name = "evento")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventoXML {
    
    @XmlAttribute
    private Long id;
    
    @XmlElement(required = true)
    private String nombre;
    
    @XmlElement
    private String descripcion;
    
    @XmlElement(name = "tipo")
    private String tipoEvento;
    
    @XmlElement(name = "sede")
    private SedeXML sede;
    
    @XmlElement(name = "fecha_inicio")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime fechaInicio;
    
    @XmlElement(name = "fecha_fin")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime fechaFin;
    
    @XmlElement(name = "aforo_maximo")
    private Integer aforoMaximo;
    
    @XmlElement(name = "aforo_actual")
    private Integer aforoActual;
    
    @XmlElement
    private String estado;
    
    @XmlElement(name = "precio_base")
    private BigDecimal precioBase;
    
    @XmlElementWrapper(name = "tipos_entrada")
    @XmlElement(name = "tipo_entrada")
    private List<TipoEntradaXML> tiposEntrada;
}
```

#### **Adaptador para LocalDateTime**

```java
package com.eventos.dto.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public LocalDateTime unmarshal(String v) {
        return v != null ? LocalDateTime.parse(v, FORMATTER) : null;
    }
    
    @Override
    public String marshal(LocalDateTime v) {
        return v != null ? v.format(FORMATTER) : null;
    }
}
```

#### **Servicio de Exportación XML**

```java
package com.eventos.services;

import com.eventos.dto.xml.EventoXML;
import com.eventos.dto.xml.CatalogoEventosXML;
import com.eventos.models.Evento;
import jakarta.xml.bind.*;
import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class ExportacionXMLService {
    
    private final JAXBContext jaxbContext;
    
    public ExportacionXMLService() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(
            CatalogoEventosXML.class, 
            EventoXML.class
        );
    }
    
    /**
     * Exporta una lista de eventos a un archivo XML
     */
    public void exportarEventos(List<Evento> eventos, String rutaArchivo) 
            throws JAXBException {
        
        // Convertir entidades a DTOs
        List<EventoXML> eventosXML = eventos.stream()
            .map(this::convertirAEventoXML)
            .collect(Collectors.toList());
        
        CatalogoEventosXML catalogo = new CatalogoEventosXML();
        catalogo.setEventos(eventosXML);
        catalogo.setFechaGeneracion(LocalDateTime.now());
        catalogo.setTotal(eventosXML.size());
        
        // Marshalling: Objeto Java -> XML
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        File archivoXML = new File(rutaArchivo);
        marshaller.marshal(catalogo, archivoXML);
        
        System.out.println("✅ Eventos exportados a: " + rutaArchivo);
    }
    
    /**
     * Exporta un solo evento a String XML
     */
    public String exportarEventoAString(Evento evento) throws JAXBException {
        EventoXML eventoXML = convertirAEventoXML(evento);
        
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(eventoXML, writer);
        
        return writer.toString();
    }
    
    /**
     * Convierte entidad Evento a EventoXML
     */
    private EventoXML convertirAEventoXML(Evento evento) {
        EventoXML xml = new EventoXML();
        xml.setId(evento.getId());
        xml.setNombre(evento.getNombre());
        xml.setDescripcion(evento.getDescripcion());
        xml.setTipoEvento(evento.getTipoEvento().getNombre());
        xml.setFechaInicio(evento.getFechaInicio());
        xml.setFechaFin(evento.getFechaFin());
        xml.setAforoMaximo(evento.getAforoMaximo());
        xml.setAforoActual(evento.getAforoActual());
        xml.setEstado(evento.getEstado().toString());
        xml.setPrecioBase(evento.getPrecioBase());
        
        // Convertir sede
        SedeXML sedeXML = new SedeXML();
        sedeXML.setNombre(evento.getSede().getNombre());
        sedeXML.setDireccion(evento.getSede().getDireccion());
        sedeXML.setCiudad(evento.getSede().getCiudad());
        xml.setSede(sedeXML);
        
        return xml;
    }
}
```

#### **Ejemplo de XML Generado**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<catalogo_eventos fecha_generacion="2025-11-04T10:30:00" total="2">
    <eventos>
        <evento id="1">
            <nombre>Concierto de Rock en Vivo</nombre>
            <descripcion>Gran concierto con las mejores bandas de rock</descripcion>
            <tipo>Concierto de Rock</tipo>
            <sede>
                <nombre>Auditorio Nacional</nombre>
                <direccion>Paseo de la Castellana 99</direccion>
                <ciudad>Madrid</ciudad>
                <capacidad>5000</capacidad>
            </sede>
            <fecha_inicio>2025-12-15T20:00:00</fecha_inicio>
            <fecha_fin>2025-12-15T23:30:00</fecha_fin>
            <aforo_maximo>5000</aforo_maximo>
            <aforo_actual>1250</aforo_actual>
            <estado>ACTIVO</estado>
            <precio_base>45.00</precio_base>
            <tipos_entrada>
                <tipo_entrada>
                    <nombre>General</nombre>
                    <precio>45.00</precio>
                    <disponibilidad>3500</disponibilidad>
                </tipo_entrada>
                <tipo_entrada>
                    <nombre>VIP</nombre>
                    <precio>120.00</precio>
                    <disponibilidad>250</disponibilidad>
                </tipo_entrada>
            </tipos_entrada>
        </evento>
        <evento id="2">
            <nombre>Obra de Teatro - El Quijote</nombre>
            <descripcion>Adaptación moderna del clásico de Cervantes</descripcion>
            <tipo>Teatro</tipo>
            <sede>
                <nombre>Palacio de Congresos</nombre>
                <direccion>Avenida Capital de España</direccion>
                <ciudad>Barcelona</ciudad>
                <capacidad>3000</capacidad>
            </sede>
            <fecha_inicio>2025-11-20T19:00:00</fecha_inicio>
            <fecha_fin>2025-11-20T21:30:00</fecha_fin>
            <aforo_maximo>3000</aforo_maximo>
            <aforo_actual>800</aforo_actual>
            <estado>PLANIFICADO</estado>
            <precio_base>30.00</precio_base>
        </evento>
    </eventos>
</catalogo_eventos>
```

---

### **2. Importación de Eventos desde XML**

#### **Servicio de Importación**

```java
package com.eventos.services;

import com.eventos.dto.xml.CatalogoEventosXML;
import com.eventos.dto.xml.EventoXML;
import com.eventos.models.*;
import com.eventos.repositories.*;
import jakarta.xml.bind.*;
import java.io.File;
import java.util.List;

public class ImportacionXMLService {
    
    private final JAXBContext jaxbContext;
    private final EventoRepository eventoRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final SedeRepository sedeRepository;
    
    public ImportacionXMLService(
            EventoRepository eventoRepository,
            TipoEventoRepository tipoEventoRepository,
            SedeRepository sedeRepository) throws JAXBException {
        
        this.jaxbContext = JAXBContext.newInstance(CatalogoEventosXML.class);
        this.eventoRepository = eventoRepository;
        this.tipoEventoRepository = tipoEventoRepository;
        this.sedeRepository = sedeRepository;
    }
    
    /**
     * Importa eventos desde un archivo XML
     */
    public void importarEventos(String rutaArchivo) throws JAXBException {
        File archivoXML = new File(rutaArchivo);
        
        // Unmarshalling: XML -> Objeto Java
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        CatalogoEventosXML catalogo = 
            (CatalogoEventosXML) unmarshaller.unmarshal(archivoXML);
        
        List<EventoXML> eventosXML = catalogo.getEventos();
        
        for (EventoXML eventoXML : eventosXML) {
            Evento evento = convertirAEvento(eventoXML);
            eventoRepository.save(evento);
            System.out.println("✅ Importado: " + evento.getNombre());
        }
        
        System.out.println("✅ Total eventos importados: " + eventosXML.size());
    }
    
    /**
     * Convierte EventoXML a entidad Evento
     */
    private Evento convertirAEvento(EventoXML xml) {
        // Buscar o crear TipoEvento
        TipoEvento tipoEvento = tipoEventoRepository
            .findByNombre(xml.getTipoEvento())
            .orElseGet(() -> {
                TipoEvento nuevo = new TipoEvento();
                nuevo.setNombre(xml.getTipoEvento());
                return tipoEventoRepository.save(nuevo);
            });
        
        // Buscar o crear Sede
        Sede sede = sedeRepository
            .findByNombre(xml.getSede().getNombre())
            .orElseGet(() -> {
                Sede nueva = new Sede();
                nueva.setNombre(xml.getSede().getNombre());
                nueva.setDireccion(xml.getSede().getDireccion());
                nueva.setCiudad(xml.getSede().getCiudad());
                nueva.setCapacidad(xml.getSede().getCapacidad());
                return sedeRepository.save(nueva);
            });
        
        // Crear evento
        Evento evento = new Evento();
        evento.setNombre(xml.getNombre());
        evento.setDescripcion(xml.getDescripcion());
        evento.setTipoEvento(tipoEvento);
        evento.setSede(sede);
        evento.setFechaInicio(xml.getFechaInicio());
        evento.setFechaFin(xml.getFechaFin());
        evento.setAforoMaximo(xml.getAforoMaximo());
        evento.setAforoActual(xml.getAforoActual());
        evento.setEstado(EstadoEvento.valueOf(xml.getEstado()));
        evento.setPrecioBase(xml.getPrecioBase());
        
        return evento;
    }
}
```

---

### **3. Validación XML con XSD**

#### **Esquema XSD para Eventos**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           elementFormDefault="qualified">

    <xs:element name="catalogo_eventos">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="eventos">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="evento" type="EventoType" 
                                        maxOccurs="unbounded"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:sequence>
            <xs:attribute name="fecha_generacion" type="xs:dateTime" use="required"/>
            <xs:attribute name="total" type="xs:int" use="required"/>
        </xs:complexType>
    </xs:element>

    <xs:complexType name="EventoType">
        <xs:sequence>
            <xs:element name="nombre" type="xs:string"/>
            <xs:element name="descripcion" type="xs:string" minOccurs="0"/>
            <xs:element name="tipo" type="xs:string"/>
            <xs:element name="sede" type="SedeType"/>
            <xs:element name="fecha_inicio" type="xs:dateTime"/>
            <xs:element name="fecha_fin" type="xs:dateTime"/>
            <xs:element name="aforo_maximo" type="xs:int"/>
            <xs:element name="aforo_actual" type="xs:int"/>
            <xs:element name="estado" type="EstadoType"/>
            <xs:element name="precio_base" type="xs:decimal"/>
        </xs:sequence>
        <xs:attribute name="id" type="xs:long"/>
    </xs:complexType>

    <xs:complexType name="SedeType">
        <xs:sequence>
            <xs:element name="nombre" type="xs:string"/>
            <xs:element name="direccion" type="xs:string"/>
            <xs:element name="ciudad" type="xs:string"/>
            <xs:element name="capacidad" type="xs:int"/>
        </xs:sequence>
    </xs:complexType>

    <xs:simpleType name="EstadoType">
        <xs:restriction base="xs:string">
            <xs:enumeration value="PLANIFICADO"/>
            <xs:enumeration value="ACTIVO"/>
            <xs:enumeration value="CANCELADO"/>
            <xs:enumeration value="FINALIZADO"/>
        </xs:restriction>
    </xs:simpleType>

</xs:schema>
```

#### **Validador XSD**

```java
package com.eventos.utils;

import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.File;
import java.io.IOException;

public class XMLValidator {
    
    /**
     * Valida un archivo XML contra un esquema XSD
     */
    public static boolean validarXML(String rutaXML, String rutaXSD) {
        try {
            SchemaFactory factory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = factory.newSchema(new File(rutaXSD));
            Validator validator = schema.newValidator();
            
            validator.validate(new StreamSource(new File(rutaXML)));
            
            System.out.println("✅ XML válido según el esquema XSD");
            return true;
            
        } catch (SAXException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("❌ Error al leer archivos: " + e.getMessage());
            return false;
        }
    }
}
```

---

## 🔧 Implementación JSON con Jackson

### **1. Confirmación de Compra en JSON**

#### **DTO de Confirmación**

```java
package com.eventos.dto.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class ConfirmacionCompraJSON {
    
    @JsonProperty("codigo_confirmacion")
    private String codigoConfirmacion;
    
    @JsonProperty("fecha_compra")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCompra;
    
    @JsonProperty("cliente")
    private ClienteInfo cliente;
    
    @JsonProperty("detalles_compra")
    private List<DetalleEntrada> entradas;
    
    @JsonProperty("total")
    private BigDecimal total;
    
    @JsonProperty("metodo_pago")
    private String metodoPago;
    
    @JsonProperty("estado")
    private String estado;
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ClienteInfo {
        private String nombre;
        private String email;
        private String telefono;
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DetalleEntrada {
        
        @JsonProperty("numero_entrada")
        private String numeroEntrada;
        
        @JsonProperty("evento")
        private String nombreEvento;
        
        @JsonProperty("fecha_evento")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime fechaEvento;
        
        @JsonProperty("sede")
        private String sede;
        
        @JsonProperty("tipo_entrada")
        private String tipoEntrada;
        
        @JsonProperty("precio")
        private BigDecimal precio;
        
        @JsonProperty("codigo_qr")
        private String codigoQR;
    }
}
```

#### **Servicio de Generación JSON**

```java
package com.eventos.services;

import com.eventos.dto.json.ConfirmacionCompraJSON;
import com.eventos.models.Compra;
import com.eventos.models.Entrada;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class JSONService {
    
    private final ObjectMapper objectMapper;
    
    public JSONService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Genera confirmación de compra en formato JSON
     */
    public String generarConfirmacionJSON(Compra compra) throws IOException {
        ConfirmacionCompraJSON confirmacion = convertirAConfirmacion(compra);
        return objectMapper.writeValueAsString(confirmacion);
    }
    
    /**
     * Guarda confirmación en archivo JSON
     */
    public void guardarConfirmacionJSON(Compra compra, String rutaArchivo) 
            throws IOException {
        
        ConfirmacionCompraJSON confirmacion = convertirAConfirmacion(compra);
        objectMapper.writeValue(new File(rutaArchivo), confirmacion);
        
        System.out.println("✅ Confirmación guardada en: " + rutaArchivo);
    }
    
    /**
     * Lee confirmación desde archivo JSON
     */
    public ConfirmacionCompraJSON leerConfirmacion(String rutaArchivo) 
            throws IOException {
        
        return objectMapper.readValue(
            new File(rutaArchivo), 
            ConfirmacionCompraJSON.class
        );
    }
    
    /**
     * Convierte entidad Compra a ConfirmacionCompraJSON
     */
    private ConfirmacionCompraJSON convertirAConfirmacion(Compra compra) {
        // Cliente
        ConfirmacionCompraJSON.ClienteInfo cliente = 
            new ConfirmacionCompraJSON.ClienteInfo(
                compra.getUsuario().getNombre(),
                compra.getUsuario().getEmail(),
                compra.getUsuario().getTelefono()
            );
        
        // Detalles de entradas
        List<ConfirmacionCompraJSON.DetalleEntrada> detalles = 
            compra.getEntradas().stream()
                .map(this::convertirADetalleEntrada)
                .collect(Collectors.toList());
        
        // Confirmación completa
        return ConfirmacionCompraJSON.builder()
            .codigoConfirmacion(compra.getCodigoConfirmacion())
            .fechaCompra(compra.getFechaCompra())
            .cliente(cliente)
            .entradas(detalles)
            .total(compra.getTotal())
            .metodoPago(compra.getMetodoPago())
            .estado(compra.getEstado().toString())
            .build();
    }
    
    private ConfirmacionCompraJSON.DetalleEntrada convertirADetalleEntrada(
            Entrada entrada) {
        
        return new ConfirmacionCompraJSON.DetalleEntrada(
            entrada.getNumeroEntrada(),
            entrada.getEvento().getNombre(),
            entrada.getEvento().getFechaInicio(),
            entrada.getEvento().getSede().getNombre(),
            entrada.getTipoEntrada().getNombre(),
            entrada.getTipoEntrada().getPrecio(),
            entrada.getCodigoQR()
        );
    }
}
```

#### **Ejemplo de JSON Generado**

```json
{
  "codigo_confirmacion": "COMP-1730716200000",
  "fecha_compra": "2025-11-04 15:30:00",
  "cliente": {
    "nombre": "Juan Pérez",
    "email": "juan.perez@email.com",
    "telefono": "+34 600123456"
  },
  "detalles_compra": [
    {
      "numero_entrada": "ENT-1730716201234-5678",
      "evento": "Concierto de Rock en Vivo",
      "fecha_evento": "2025-12-15 20:00:00",
      "sede": "Auditorio Nacional",
      "tipo_entrada": "VIP",
      "precio": 120.00,
      "codigo_qr": "qr-codes/ENT-1730716201234-5678.png"
    },
    {
      "numero_entrada": "ENT-1730716201234-5679",
      "evento": "Concierto de Rock en Vivo",
      "fecha_evento": "2025-12-15 20:00:00",
      "sede": "Auditorio Nacional",
      "tipo_entrada": "General",
      "precio": 45.00,
      "codigo_qr": "qr-codes/ENT-1730716201234-5679.png"
    }
  ],
  "total": 165.00,
  "metodo_pago": "TARJETA",
  "estado": "COMPLETADA"
}
```

---

### **2. Datos para Código QR en JSON**

```java
package com.eventos.dto.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class DatosQRJSON {
    
    @JsonProperty("numero_entrada")
    private String numeroEntrada;
    
    @JsonProperty("evento_id")
    private Long eventoId;
    
    @JsonProperty("evento_nombre")
    private String eventoNombre;
    
    @JsonProperty("titular")
    private String titular;
    
    @JsonProperty("tipo_entrada")
    private String tipoEntrada;
    
    @JsonProperty("fecha_compra")
    private String fechaCompra;
    
    @JsonProperty("validada")
    private boolean validada;
}
```

---

## 📱 Generación de Códigos QR

### **Servicio QR con ZXing**

```java
package com.eventos.services;

import com.eventos.dto.json.DatosQRJSON;
import com.eventos.models.Entrada;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRService {
    
    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;
    private final ObjectMapper objectMapper;
    
    public QRService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Genera código QR para una entrada
     */
    public String generarQREntrada(Entrada entrada, String rutaDirectorio) 
            throws WriterException, IOException {
        
        // Crear datos JSON para el QR
        DatosQRJSON datosQR = DatosQRJSON.builder()
            .numeroEntrada(entrada.getNumeroEntrada())
            .eventoId(entrada.getEvento().getId())
            .eventoNombre(entrada.getEvento().getNombre())
            .titular(entrada.getCompra().getUsuario().getNombre())
            .tipoEntrada(entrada.getTipoEntrada().getNombre())
            .fechaCompra(entrada.getCompra().getFechaCompra().toString())
            .validada(entrada.getValidada())
            .build();
        
        // Convertir a JSON String
        String datosJSON = objectMapper.writeValueAsString(datosQR);
        
        // Generar nombre de archivo
        String nombreArchivo = "QR_" + entrada.getNumeroEntrada() + ".png";
        String rutaCompleta = rutaDirectorio + "/" + nombreArchivo;
        
        // Generar código QR
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            datosJSON, 
            BarcodeFormat.QR_CODE, 
            QR_WIDTH, 
            QR_HEIGHT
        );
        
        Path path = FileSystems.getDefault().getPath(rutaCompleta);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        
        System.out.println("✅ QR generado: " + rutaCompleta);
        
        return rutaCompleta;
    }
    
    /**
     * Lee y decodifica un código QR
     */
    public DatosQRJSON leerQR(String rutaImagen) throws Exception {
        // Implementación de lectura de QR
        // Retorna los datos JSON embebidos
        // (Requiere implementación adicional con BufferedImage)
        return null;
    }
}
```

---

## 🎯 Flujo Completo de Compra

### **Diagrama de Secuencia**

```
Usuario                 Sistema                 BD              Servicios Externos
  │                       │                      │                       │
  │──Seleccionar evento──>│                      │                       │
  │                       │──Consultar aforo────>│                       │
  │                       │<─────Aforo OK────────│                       │
  │<──Mostrar disponib.───│                      │                       │
  │                       │                      │                       │
  │──Añadir entradas─────>│                      │                       │
  │──Confirmar compra────>│                      │                       │
  │                       │──Crear Compra───────>│                       │
  │                       │──Crear Entradas─────>│                       │
  │                       │                      │                       │
  │                       │──Generar JSON────────────────────────────────>│
  │                       │<─────JSON OK─────────────────────────────────│
  │                       │                      │                       │
  │                       │──Generar QR──────────────────────────────────>│
  │                       │<──────QR PNG─────────────────────────────────│
  │                       │                      │                       │
  │                       │──Guardar QR path────>│                       │
  │                       │──Actualizar aforo───>│                       │
  │                       │                      │                       │
  │<──Mostrar confirm.────│                      │                       │
  │<──Enviar email JSON───│──────────────────────────────────────────────>│
```

### **Código del Controller**

```java
package com.eventos.controllers;

import com.eventos.dto.CompraDTO;
import com.eventos.models.*;
import com.eventos.services.*;
import java.util.List;

public class CompraController {
    
    private final CompraService compraService;
    private final JSONService jsonService;
    private final QRService qrService;
    private final EventoService eventoService;
    
    /**
     * Procesa una compra completa
     */
    public Compra procesarCompra(CompraDTO dto) {
        try {
            // 1. Validar disponibilidad
            Evento evento = eventoService.findById(dto.getEventoId());
            if (!evento.hayDisponibilidad()) {
                throw new CompraException("Evento sin disponibilidad");
            }
            
            // 2. Crear compra
            Compra compra = compraService.crearCompra(dto);
            
            // 3. Generar JSON de confirmación
            String confirmacionJSON = jsonService.generarConfirmacionJSON(compra);
            compra.setConfirmacionJSON(confirmacionJSON);
            
            // 4. Generar códigos QR para cada entrada
            for (Entrada entrada : compra.getEntradas()) {
                String rutaQR = qrService.generarQREntrada(
                    entrada, 
                    "qr-codes"
                );
                entrada.setCodigoQR(rutaQR);
            }
            
            // 5. Guardar cambios
            compraService.update(compra);
            
            // 6. Enviar confirmación por email
            enviarConfirmacionEmail(compra, confirmacionJSON);
            
            return compra;
            
        } catch (Exception e) {
            throw new CompraException("Error al procesar compra", e);
        }
    }
}
```

---

**Siguiente documento:** Casos de Uso y Manual de Usuario
