# Recomendaciones de Desarrollo - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Versión:** 1.0  
**Fecha:** 4 de noviembre de 2025

---

## 🎯 Mejores Prácticas de Desarrollo

### **1. Organización del Código**

#### **Principios SOLID**
- **S**ingle Responsibility: Cada clase tiene una única responsabilidad
- **O**pen/Closed: Abierto para extensión, cerrado para modificación
- **L**iskov Substitution: Interfaces consistentes
- **I**nterface Segregation: Interfaces específicas
- **D**ependency Inversion: Depender de abstracciones

#### **Ejemplo - Repository Pattern**
```java
// ✅ BIEN - Interfaz genérica
public interface IRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(T entity);
}

// ✅ BIEN - Implementación específica
public class EventoRepository implements IRepository<Evento, Long> {
    private EntityManager em;
    
    // Métodos específicos para eventos
    public List<Evento> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        // Implementación
    }
}

// ❌ MAL - Todo en una clase God Object
public class EventoManager {
    public void crearEvento() { }
    public void validarEntrada() { }
    public void generarInforme() { }
    public void enviarEmail() { }
    // Demasiadas responsabilidades
}
```

---

### **2. Gestión de Transacciones**

```java
public class CompraService {
    
    private EntityManager em;
    
    @Transactional
    public Compra procesarCompra(CompraDTO dto) {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // 1. Crear compra
            Compra compra = new Compra();
            // ... configurar compra
            
            // 2. Crear entradas
            for (DetalleEntrada detalle : dto.getDetalles()) {
                Entrada entrada = new Entrada();
                // ... configurar entrada
                compra.addEntrada(entrada);
            }
            
            // 3. Actualizar aforo
            Evento evento = em.find(Evento.class, dto.getEventoId());
            evento.incrementarAforo(dto.getCantidad());
            
            // 4. Persistir todo
            em.persist(compra);
            em.merge(evento);
            
            tx.commit();
            return compra;
            
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CompraException("Error al procesar compra", e);
        }
    }
}
```

---

### **3. Manejo de Excepciones**

```java
// Jerarquía de excepciones personalizadas
public class EventosException extends RuntimeException {
    public EventosException(String message) {
        super(message);
    }
    
    public EventosException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class CompraException extends EventosException {
    public CompraException(String message) {
        super(message);
    }
}

public class AutenticacionException extends EventosException {
    public AutenticacionException(String message) {
        super(message);
    }
}

// Uso en controladores
public class EventoController {
    
    public void crearEvento(EventoDTO dto) {
        try {
            eventoService.crear(dto);
            mostrarMensaje("Evento creado exitosamente");
            
        } catch (ValidationException e) {
            mostrarError("Datos inválidos: " + e.getMessage());
            
        } catch (EventosException e) {
            mostrarError("Error al crear evento: " + e.getMessage());
            logger.error("Error creando evento", e);
            
        } catch (Exception e) {
            mostrarError("Error inesperado");
            logger.error("Error inesperado", e);
        }
    }
}
```

---

### **4. Validación de Datos**

```java
public class Validator {
    
    public static void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es obligatorio");
        }
        
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(regex)) {
            throw new ValidationException("Email inválido");
        }
    }
    
    public static void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Contraseña debe tener al menos 8 caracteres");
        }
        
        boolean tieneNumero = password.matches(".*\\d.*");
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        
        if (!tieneNumero || !tieneMayuscula) {
            throw new ValidationException(
                "Contraseña debe tener números y mayúsculas"
            );
        }
    }
    
    public static void validarFechaEvento(LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime ahora = LocalDateTime.now();
        
        if (inicio.isBefore(ahora)) {
            throw new ValidationException("La fecha de inicio no puede ser en el pasado");
        }
        
        if (fin.isBefore(inicio)) {
            throw new ValidationException("La fecha de fin debe ser posterior al inicio");
        }
    }
}
```

---

### **5. Logging Efectivo**

```java
// Configuración log4j2.xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        
        <File name="FileLogger" fileName="logs/eventos.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileLogger"/>
        </Root>
        
        <Logger name="com.eventos" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileLogger"/>
        </Logger>
    </Loggers>
</Configuration>
```

```java
// Uso en clases
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompraService {
    private static final Logger logger = LoggerFactory.getLogger(CompraService.class);
    
    public Compra procesarCompra(CompraDTO dto) {
        logger.info("Procesando compra para usuario: {}", dto.getUsuarioId());
        
        try {
            Compra compra = crearCompra(dto);
            logger.info("Compra creada exitosamente. ID: {}", compra.getId());
            return compra;
            
        } catch (Exception e) {
            logger.error("Error procesando compra para usuario: {}", dto.getUsuarioId(), e);
            throw new CompraException("Error al procesar compra", e);
        }
    }
}
```

---

### **6. Testing**

#### **Pruebas Unitarias con JUnit 5**

```java
package com.eventos.services;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacionServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private AutenticacionService autenticacionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("Login exitoso con credenciales correctas")
    void testLoginExitoso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("test@email.com");
        usuario.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        
        when(usuarioRepository.findByEmail("test@email.com"))
            .thenReturn(Optional.of(usuario));
        
        // Act
        Usuario resultado = autenticacionService.login("test@email.com", "password123");
        
        // Assert
        assertNotNull(resultado);
        assertEquals("test@email.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail("test@email.com");
    }
    
    @Test
    @DisplayName("Login falla con contraseña incorrecta")
    void testLoginFallaPasswordIncorrecto() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("test@email.com");
        usuario.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        
        when(usuarioRepository.findByEmail("test@email.com"))
            .thenReturn(Optional.of(usuario));
        
        // Act & Assert
        assertThrows(AutenticacionException.class, () -> {
            autenticacionService.login("test@email.com", "wrongpassword");
        });
    }
    
    @Test
    @DisplayName("Login falla con usuario no existente")
    void testLoginFallaUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findByEmail("noexiste@email.com"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(AutenticacionException.class, () -> {
            autenticacionService.login("noexiste@email.com", "password123");
        });
    }
}
```

#### **Pruebas de Integración**

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegracionCompraTest {
    
    private static EntityManagerFactory emf;
    private EntityManager em;
    private CompraService compraService;
    
    @BeforeAll
    static void setupDatabase() {
        emf = Persistence.createEntityManagerFactory("EventosTestPU");
    }
    
    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        compraService = new CompraService(em);
    }
    
    @Test
    @Order(1)
    @DisplayName("Flujo completo: Crear evento, comprar entrada, validar")
    void testFlujoCompleto() {
        // 1. Crear evento
        Evento evento = crearEventoTest();
        em.getTransaction().begin();
        em.persist(evento);
        em.getTransaction().commit();
        
        // 2. Comprar entrada
        CompraDTO dto = new CompraDTO();
        dto.setEventoId(evento.getId());
        dto.setCantidad(2);
        
        Compra compra = compraService.procesarCompra(dto);
        
        // 3. Verificar
        assertNotNull(compra.getId());
        assertEquals(2, compra.getEntradas().size());
        assertNotNull(compra.getCodigoConfirmacion());
        
        // 4. Verificar aforo actualizado
        Evento eventoActualizado = em.find(Evento.class, evento.getId());
        assertEquals(2, eventoActualizado.getAforoActual());
    }
    
    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    @AfterAll
    static void closeDatabase() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

---

### **7. Seguridad**

#### **Hash de Contraseñas con BCrypt**

```java
public class PasswordUtil {
    
    /**
     * Genera hash BCrypt de una contraseña
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    /**
     * Verifica si una contraseña coincide con su hash
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

// Uso
public class UsuarioService {
    
    public Usuario registrar(UsuarioDTO dto) {
        // Validar datos
        Validator.validarEmail(dto.getEmail());
        Validator.validarPassword(dto.getPassword());
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(PasswordUtil.hashPassword(dto.getPassword())); // ✅ Hash
        usuario.setNombre(dto.getNombre());
        
        return usuarioRepository.save(usuario);
    }
}
```

---

### **8. Optimización de Consultas**

```java
public class EventoRepository {
    
    // ❌ MAL - N+1 queries problem
    public List<Evento> findAll() {
        return em.createQuery("SELECT e FROM Evento e", Evento.class)
                 .getResultList();
        // Cada acceso a e.getSede() o e.getTipoEvento() genera una query adicional
    }
    
    // ✅ BIEN - Usar JOIN FETCH para eager loading
    public List<Evento> findAllWithDetails() {
        return em.createQuery(
            "SELECT DISTINCT e FROM Evento e " +
            "LEFT JOIN FETCH e.sede " +
            "LEFT JOIN FETCH e.tipoEvento", 
            Evento.class
        ).getResultList();
    }
    
    // ✅ BIEN - Usar paginación para grandes conjuntos de datos
    public List<Evento> findAllPaginated(int page, int size) {
        return em.createQuery("SELECT e FROM Evento e", Evento.class)
                 .setFirstResult(page * size)
                 .setMaxResults(size)
                 .getResultList();
    }
    
    // ✅ BIEN - Usar DTO projection para evitar cargar entidades completas
    public List<EventoDTO> findEventosResumen() {
        return em.createQuery(
            "SELECT new com.eventos.dto.EventoDTO(" +
            "e.id, e.nombre, e.fechaInicio, e.aforoActual, e.aforoMaximo) " +
            "FROM Evento e",
            EventoDTO.class
        ).getResultList();
    }
}
```

---

### **9. Configuración de Entorno**

#### **Properties externas**

```java
// config.properties
db.url=jdbc:mysql://localhost:3306/eventos_db
db.username=root
db.password=admin123
app.export.path=./exports
app.qr.path=./qr-codes
email.smtp.host=smtp.gmail.com
email.smtp.port=587
```

```java
public class ConfigManager {
    private static Properties properties;
    
    static {
        properties = new Properties();
        try (InputStream input = ConfigManager.class
                .getResourceAsStream("/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración", e);
        }
    }
    
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    public static String getExportPath() {
        return get("app.export.path");
    }
}
```

---

### **10. Documentación de Código**

```java
/**
 * Servicio para gestión de eventos.
 * 
 * <p>Proporciona operaciones CRUD para eventos y funcionalidades
 * relacionadas como búsqueda, filtrado y exportación.</p>
 * 
 * @author Equipo Eventos
 * @version 1.0
 * @since 2025-11-04
 */
public class EventoService {
    
    /**
     * Crea un nuevo evento en el sistema.
     * 
     * @param dto Datos del evento a crear
     * @return El evento creado con ID asignado
     * @throws ValidationException Si los datos del evento son inválidos
     * @throws EventosException Si ocurre un error al crear el evento
     * 
     * @see EventoDTO
     */
    public Evento crear(EventoDTO dto) {
        // Implementación
    }
    
    /**
     * Busca eventos por rango de fechas.
     * 
     * @param inicio Fecha de inicio del rango (inclusive)
     * @param fin Fecha de fin del rango (inclusive)
     * @return Lista de eventos en el rango especificado, puede estar vacía
     * @throws IllegalArgumentException Si fin es anterior a inicio
     */
    public List<Evento> buscarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        // Implementación
    }
}
```

---

## 📝 Checklist de Desarrollo por Sprint

### **Sprint 1: Configuración Base**
- [ ] Crear proyecto Maven/Gradle
- [ ] Configurar dependencias (Hibernate, JAXB, Jackson, etc.)
- [ ] Configurar persistence.xml
- [ ] Crear entidades Usuario y Rol
- [ ] Implementar repositories básicos
- [ ] Crear servicio de autenticación
- [ ] Implementar hash de contraseñas
- [ ] Pruebas unitarias de autenticación

### **Sprint 2: Gestión de Eventos**
- [ ] Crear entidades Evento, TipoEvento
- [ ] Implementar EventoRepository
- [ ] Crear EventoService con CRUD
- [ ] Implementar búsqueda y filtros
- [ ] Crear interfaces de usuario para eventos
- [ ] Pruebas unitarias de eventos

### **Sprint 3: XML/JSON**
- [ ] Configurar JAXB para XML
- [ ] Crear DTOs para exportación XML
- [ ] Implementar ExportacionXMLService
- [ ] Crear esquema XSD
- [ ] Implementar ImportacionXMLService
- [ ] Implementar validación XSD
- [ ] Configurar Jackson para JSON
- [ ] Pruebas de exportación/importación

### **Sprint 4: Sedes y Equipamiento**
- [ ] Crear entidades Sede y Equipamiento
- [ ] Implementar repositories
- [ ] Crear servicios de gestión
- [ ] Implementar relaciones con Evento
- [ ] Crear interfaces de administración
- [ ] Pruebas de integridad referencial

### **Sprint 5: Sistema de Entradas**
- [ ] Crear entidades TipoEntrada y Entrada
- [ ] Implementar gestión de precios
- [ ] Implementar control de disponibilidad
- [ ] Crear servicios de gestión
- [ ] Pruebas de concurrencia (aforo)

### **Sprint 6: Proceso de Compra**
- [ ] Crear entidad Compra
- [ ] Implementar CompraService
- [ ] Integrar generación JSON
- [ ] Configurar ZXing para QR
- [ ] Implementar QRService
- [ ] Crear interfaz de compra
- [ ] Sistema de confirmaciones
- [ ] Pruebas de flujo completo

### **Sprint 7: Validación y Historial**
- [ ] Crear entidad RegistroEntrada
- [ ] Implementar validación de QR
- [ ] Crear interfaz de escaneo
- [ ] Implementar historial de compras
- [ ] Pruebas de validación

### **Sprint 8: Informes**
- [ ] Implementar InformeService
- [ ] Crear generadores de estadísticas
- [ ] Implementar exportación de informes
- [ ] Crear interfaces de visualización
- [ ] Pruebas de informes

### **Sprint 9: Testing e Integración**
- [ ] Pruebas de integración completas
- [ ] Pruebas de carga
- [ ] Optimizar consultas SQL
- [ ] Implementar caché (opcional)
- [ ] Refactorización de código

### **Sprint 10: Documentación y Entrega**
- [ ] Generar JavaDoc
- [ ] Documentar API
- [ ] Crear manual de usuario
- [ ] Preparar presentación
- [ ] Crear README.md
- [ ] Verificar cumplimiento de requisitos

---

## ⚡ Consejos Finales

### **1. División del Trabajo (3 personas)**

**Persona 1: Backend Specialist**
- Persistencia (Hibernate/JPA)
- Servicios de negocio
- Repositories
- Testing unitario

**Persona 2: Frontend/UI Developer**
- Interfaces Swing/JavaFX
- Controladores
- Validación de formularios
- Experiencia de usuario

**Persona 3: Integration/Data Specialist**
- XML/JSON (JAXB, Jackson)
- Códigos QR (ZXing)
- Exportación/Importación
- Informes y estadísticas

### **2. Herramientas Recomendadas**

- **Control de versiones:** Git + GitHub
- **IDE:** IntelliJ IDEA (Community)
- **Base de datos:** MySQL Workbench
- **Testing:** JUnit 5 + Mockito
- **Documentación:** PlantUML para diagramas
- **Gestión:** Jira (que ya estás usando)

### **3. Evitar Errores Comunes**

❌ **No hacer:**
- Mezclar lógica de negocio en vistas
- Almacenar contraseñas en texto plano
- Ignorar validación de datos
- Commit de código sin probar
- Dejar TODOs sin resolver

✅ **Sí hacer:**
- Separar responsabilidades (capas)
- Validar todos los inputs
- Escribir tests desde el inicio
- Documentar mientras programas
- Hacer commits frecuentes con mensajes claros

### **4. Métricas de Calidad**

- **Cobertura de tests:** Objetivo > 70%
- **Complejidad ciclomática:** Métodos < 10
- **Líneas por método:** < 50 líneas
- **Clases por paquete:** 5-15 clases
- **Acoplamiento:** Bajo (alta cohesión)

---

## 🎓 Criterios de Evaluación Estimados

| Criterio | Peso | Observaciones |
|----------|------|---------------|
| **Uso de Java** | 20% | Sintaxis, POO, buenas prácticas |
| **Persistencia (Hibernate)** | 25% | Correcto uso de JPA, relaciones |
| **XML/JSON** | 25% | JAXB, Jackson, validación XSD |
| **Funcionalidad** | 15% | Cumple requisitos, casos de uso |
| **Testing** | 10% | Pruebas unitarias e integración |
| **Documentación** | 5% | JavaDoc, manual, diagramas |

---

¡Éxito con tu proyecto! 🚀
