# 🎭 Sistema de Gestión de Eventos

**Proyecto Grupal - Programación con Gestión de Datos**  
**Equipo:** Ale / LuisM / Fran  
**Versión:** 1.0.0  
**Fecha:** 4 de noviembre de 2025

---

## 📋 Descripción del Proyecto

Sistema completo de gestión de eventos desarrollado en **Java 17** utilizando **Hibernate/JPA**, **JavaFX**, **XML/JSON** y códigos QR. El proyecto permite administrar eventos, vender entradas online, validar el acceso mediante códigos QR, y generar informes completos.

### 🎯 Características Principales

#### Para Usuarios
- ✅ Registro y autenticación segura (BCrypt)
- 🔍 Búsqueda y filtrado de eventos
- 🎟️ Compra de entradas online
- 📱 Recepción de códigos QR para acceso
- 📜 Historial completo de compras
- 📧 Confirmaciones en formato JSON

#### Para Administradores
- 📅 Gestión completa de eventos (CRUD)
- 🏢 Administración de sedes y equipamiento
- 💰 Control de tipos de entrada y precios
- 📊 Generación de informes y estadísticas
- 📤 Exportación/Importación de datos en XML
- 👥 Gestión de usuarios y roles

#### Para Empleados
- 🔍 Validación de entradas mediante QR
- ✅ Registro de ingresos a eventos
- 📋 Control de acceso en tiempo real

---

## 🛠️ Tecnologías Utilizadas

### Core
- **Java** 17 (LTS)
- **Maven** 3.9+ - Gestión de dependencias
- **Hibernate** 6.3.1 - ORM / JPA 3.1

### Persistencia
- **MySQL** 8.0+ - Base de datos (XAMPP)
- **HikariCP** - Pool de conexiones

### Interfaz de Usuario
- **JavaFX** 17+ - Framework de UI moderno

### XML/JSON
- **JAXB** (Jakarta XML Binding) - Exportación/Importación XML
- **Jackson** 2.15+ - Procesamiento JSON
- **XSD** - Validación de esquemas XML

### Características Adicionales
- **ZXing** - Generación y lectura de códigos QR
- **BCrypt** - Hash seguro de contraseñas
- **Log4j2** - Sistema de logging
- **JUnit 5** + **Mockito** - Testing

---

## 📐 Arquitectura del Sistema

El proyecto sigue una **arquitectura en capas** (Layered Architecture):

```
┌─────────────────────────────────────────┐
│     CAPA DE PRESENTACIÓN (Views)        │
│           JavaFX / Swing                │
└─────────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│    CAPA DE CONTROLADORES                │
│  Usuario, Evento, Compra, Admin...      │
└─────────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│       CAPA DE SERVICIOS                 │
│  Lógica de negocio y validaciones       │
└─────────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│    CAPA DE PERSISTENCIA (Repositories)  │
│         Acceso a datos (JPA)            │
└─────────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│         BASE DE DATOS (H2/MySQL)        │
└─────────────────────────────────────────┘
```

---

## 🗂️ Estructura del Proyecto

```
gestion-eventos/
├── src/
│   ├── main/
│   │   ├── java/com/eventos/
│   │   │   ├── config/              # Configuración (Hibernate, Config)
│   │   │   ├── models/              # Entidades JPA (10 entidades)
│   │   │   ├── repositories/        # Capa de acceso a datos
│   │   │   ├── services/            # Lógica de negocio
│   │   │   ├── controllers/         # Controladores
│   │   │   ├── views/               # Interfaces de usuario
│   │   │   ├── dto/                 # DTOs para XML/JSON
│   │   │   ├── utils/               # Utilidades
│   │   │   ├── exceptions/          # Excepciones personalizadas
│   │   │   └── Main.java            # Punto de entrada
│   │   └── resources/
│   │       ├── META-INF/persistence.xml
│   │       ├── config.properties
│   │       ├── log4j2.xml
│   │       └── schema/              # Esquemas XSD
│   └── test/                        # Tests unitarios e integración
├── exports/                         # Archivos XML/JSON exportados
├── imports/                         # Archivos para importar
├── qr-codes/                        # Códigos QR generados
├── docs/                            # Documentación
└── pom.xml
```

---

## 📊 Modelo de Datos

### Entidades Principales (10)

1. **Usuario** - Usuarios del sistema con autenticación
2. **Rol** - Roles y permisos (Admin, Usuario, Empleado)
3. **Evento** - Eventos disponibles
4. **TipoEvento** - Categorización de eventos
5. **Sede** - Ubicaciones donde se realizan eventos
6. **Equipamiento** - Equipamiento disponible en sedes
7. **TipoEntrada** - Tipos de entrada (VIP, General, etc.)
8. **Entrada** - Entradas individuales vendidas
9. **Compra** - Transacciones de compra
10. **RegistroEntrada** - Registro de acceso a eventos

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven 3.9+
- XAMPP (con MySQL activo)

### Pasos

1. **Iniciar MySQL en XAMPP**
   - Abrir el panel de control de XAMPP
   - Iniciar el servicio Apache y MySQL
   - La base de datos `eventos_db` se creará automáticamente

2. **Clonar el repositorio**
```bash
git clone https://github.com/AleGonz2812/Proyecto-Grupal-Martin.git
cd Proyecto-Grupal-Martin
```

3. **Compilar el proyecto**
```bash
mvn clean install
```

4. **Ejecutar la aplicación**
```bash
mvn exec:java -Dexec.mainClass="com.eventos.Main"
```

O con JavaFX:
```bash
mvn javafx:run
```

5. **Ejecutar tests**
```bash
mvn test
```

---

## 📅 Planificación del Proyecto

El desarrollo está organizado en **10 sprints** siguiendo metodología ágil:

### Sprint 1 - Configuración y Autenticación Base ✅
- PQDM-6: Configurar proyecto Java con Maven
- PQDM-33: Diseñar modelo de datos usuarios y roles
- PQDM-34: Implementar persistencia con Hibernate

### Sprints 2-10
Ver documentación completa en `01_Analisis_Sprints_Jira.md`

---

## 📚 Documentación Adicional

- **00_INDICE.md** - Índice completo de documentación
- **01_Analisis_Sprints_Jira.md** - Planificación detallada de sprints
- **02_Arquitectura_Tecnica.md** - Arquitectura y stack tecnológico
- **03_Modelo_Datos_Entidades.md** - Modelo de datos completo
- **04_Implementacion_XML_JSON.md** - Guía de XML/JSON
- **05_Casos_Uso_Flujos.md** - Casos de uso y mockups
- **06_Recomendaciones_Desarrollo.md** - Mejores prácticas

---

## 👥 Equipo de Desarrollo

- **Ale** - Backend Developer (Persistencia, Servicios, APIs)
- **LuisM** - Frontend Developer (Interfaces, UX)
- **Fran** - Integration Specialist (XML/JSON, QR, Testing)

---

## ✨ Estado del Proyecto

🚧 **En Desarrollo**

- [x] **Sprint 1.1** - PQDM-6: Configurar proyecto Maven ✅
- [x] **Sprint 1.2** - PQDM-33: Modelo de datos Usuario/Rol ✅
- [x] **Sprint 1.3** - PQDM-34: Persistencia con Hibernate ✅
- [x] Estructura completa de directorios creada
- [x] 10 entidades JPA implementadas
- [x] Sistema de configuración (HibernateUtil, ConfigManager)
- [x] Utilidades (PasswordUtil, Validator)
- [x] Excepciones personalizadas
- [ ] Sprint 2: Interfaces de usuario y eventos
- [ ] Sprint 3: Exportación/Importación XML/JSON
- [ ] Sprints 4-10: Funcionalidades restantes

---

**Última Actualización:** 4 de noviembre de 2025
