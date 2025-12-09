# 🎭 Sistema de Gestión de Eventos

Sistema completo para la gestión de eventos con **Java 17**, **Hibernate**, **JavaFX**, **MySQL** y soporte para **XML/JSON**.

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-6.3-green)
![Maven](https://img.shields.io/badge/Maven-3.8+-red)

---

## 📋 Tabla de Contenidos

- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración de Base de Datos](#-configuración-de-base-de-datos)
- [Ejecución](#-ejecución)
- [Diagrama Entidad-Relación](#-diagrama-entidad-relación)
- [Diagrama de Clases](#-diagrama-de-clases)
- [Diagrama de Casos de Uso](#-diagrama-de-casos-de-uso)
- [Requisitos Funcionales y No Funcionales](#-requisitos-funcionales-y-no-funcionales)

---

## 🔧 Requisitos Previos

- **Java JDK 17** o superior
- **XAMPP** (con MySQL 8.0+) o MySQL Server independiente
- **Git** para clonar el repositorio
- Conexión a Internet (para dependencias Maven y mapa Leaflet)

---

## 📥 Instalación

### 1. Clonar el repositorio (rama main)

```bash
git clone -b main https://github.com/tu-usuario/Proyecto-Grupal-Martin-fran-dev.git
cd Proyecto-Grupal-Martin-fran-dev
```

> ⚠️ **Importante**: Asegúrate de clonar la rama `main` que contiene la versión estable del proyecto.

### 2. Verificar Java

```bash
java -version
```

Asegúrate de tener Java 17 o superior instalado.

---

## 🗄️ Configuración de Base de Datos

### 1. Iniciar MySQL (XAMPP)

1. Abre **XAMPP Control Panel**
2. Inicia el servicio **MySQL**
3. Opcionalmente inicia **Apache** para usar phpMyAdmin

### 2. Crear la base de datos

Abre phpMyAdmin (`http://localhost/phpmyadmin`) o usa la consola MySQL y ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS eventos_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 3. Ejecutar el script de esquema

El archivo `src/main/resources/sql/schema.sql` contiene todas las tablas necesarias. Puedes ejecutarlo desde phpMyAdmin o desde consola:

```bash
mysql -u root -p eventos_db < src/main/resources/sql/schema.sql
```

### 4. Cargar datos de prueba (opcional)

```bash
mysql -u root -p eventos_db < src/main/resources/sql/data.sql
```

---

## ▶️ Ejecución

### Opción 1: Usando el script BAT (Windows)

```bash
EJECUTAR.bat
```

### Opción 2: Usando Maven

```bash
# Compilar
.\mvnw.cmd compile

# Ejecutar
.\mvnw.cmd javafx:run
```


## 📊 Diagrama Entidad-Relación

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│      ROLES      │       │    USUARIOS     │       │  TIPOS_EVENTO   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ PK id           │──┐    │ PK id           │       │ PK id           │
│    nombre       │  │    │ FK rol_id       │───────│    nombre       │
│    descripcion  │  └────│    email        │       │    descripcion  │
└─────────────────┘       │    password     │       │    categoria    │
                          │    nombre       │       └────────┬────────┘
┌─────────────────┐       │    apellidos    │                │
│  PERMISOS_ROL   │       │    telefono     │                │
├─────────────────┤       │    activo       │                │
│ FK rol_id       │       │    fecha_alta   │                │
│    permiso      │       └────────┬────────┘                │
└─────────────────┘                │                         │
                                   │                         │
                          ┌────────┴────────┐       ┌────────┴────────┐
                          │     COMPRAS     │       │     EVENTOS     │
                          ├─────────────────┤       ├─────────────────┤
                          │ PK id           │       │ PK id           │
                          │ FK usuario_id   │       │ FK tipo_evento_id│
                          │    fecha_compra │       │ FK sede_id      │───┐
                          │    total        │       │    nombre       │   │
                          │    estado       │       │    descripcion  │   │
                          │    codigo_conf  │       │    fecha_inicio │   │
                          │    metodo_pago  │       │    fecha_fin    │   │
                          └────────┬────────┘       │    aforo_maximo │   │
                                   │                │    estado       │   │
                                   │                │    precio_base  │   │
                                   │                └────────┬────────┘   │
                                   │                         │            │
┌─────────────────┐       ┌────────┴─────────────────────────┴───┐       │
│  TIPOS_ENTRADA  │       │              ENTRADAS               │       │
├─────────────────┤       ├─────────────────────────────────────┤       │
│ PK id           │───────│ PK id                               │       │
│    nombre       │       │ FK tipo_entrada_id                  │       │
│    descripcion  │       │ FK evento_id                        │       │
│    precio       │       │ FK compra_id                        │       │
│    activo       │       │    numero_entrada                   │       │
│    beneficios   │       │    validada                         │       │
└─────────────────┘       │    codigo_qr                        │       │
                          └────────┬────────────────────────────┘       │
                                   │                                     │
                          ┌────────┴────────┐                           │
                          │REGISTROS_ENTRADA│       ┌───────────────────┴─┐
                          ├─────────────────┤       │        SEDES        │
                          │ PK id           │       ├─────────────────────┤
                          │ FK entrada_id   │       │ PK id               │
                          │ FK evento_id    │       │    nombre           │
                          │    fecha_hora   │       │    direccion        │
                          │    empleado     │       │    ciudad           │
                          │    observaciones│       │    capacidad        │
                          └─────────────────┘       │    latitud/longitud │
                                                    │    activa           │
                          ┌─────────────────┐       └─────────┬───────────┘
                          │   EQUIPAMIENTO  │                 │
                          ├─────────────────┤                 │
                          │ PK id           │                 │
                          │ FK sede_id      │─────────────────┘
                          │    nombre       │
                          │    cantidad     │
                          │    estado       │
                          └─────────────────┘
```

### Relaciones principales:

| Relación | Tipo | Descripción |
|----------|------|-------------|
| Usuario → Rol | N:1 | Cada usuario tiene un rol |
| Evento → TipoEvento | N:1 | Cada evento tiene un tipo |
| Evento → Sede | N:1 | Cada evento se celebra en una sede |
| Compra → Usuario | N:1 | Cada compra pertenece a un usuario |
| Entrada → Compra | N:1 | Cada entrada pertenece a una compra |
| Entrada → Evento | N:1 | Cada entrada es para un evento |
| Entrada → TipoEntrada | N:1 | Cada entrada tiene un tipo |
| Equipamiento → Sede | N:1 | Cada equipamiento pertenece a una sede |
| RegistroEntrada → Entrada | N:1 | Cada registro valida una entrada |

---

## 📐 Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CAPA DE MODELOS                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│     Usuario      │    │      Evento      │    │       Sede       │
├──────────────────┤    ├──────────────────┤    ├──────────────────┤
│ - id: Long       │    │ - id: Long       │    │ - id: Long       │
│ - email: String  │    │ - nombre: String │    │ - nombre: String │
│ - password: String│   │ - descripcion    │    │ - direccion      │
│ - nombre: String │    │ - fechaInicio    │    │ - ciudad: String │
│ - apellidos      │    │ - fechaFin       │    │ - capacidad: int │
│ - telefono       │    │ - aforoMaximo    │    │ - latitud: Double│
│ - activo: boolean│    │ - estado: Enum   │    │ - longitud: Double│
│ - rol: Rol       │    │ - tipoEvento     │    │ - activa: boolean│
├──────────────────┤    │ - sede: Sede     │    ├──────────────────┤
│ + getters/setters│    ├──────────────────┤    │ + getters/setters│
└────────┬─────────┘    │ + getters/setters│    └────────┬─────────┘
         │              └────────┬─────────┘             │
         │                       │                       │
         ▼                       ▼                       ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│       Rol        │    │    TipoEvento    │    │   Equipamiento   │
├──────────────────┤    ├──────────────────┤    ├──────────────────┤
│ - id: Long       │    │ - id: Long       │    │ - id: Long       │
│ - nombre: String │    │ - nombre: String │    │ - nombre: String │
│ - descripcion    │    │ - descripcion    │    │ - cantidad: int  │
│ - permisos: List │    │ - categoria      │    │ - estado: String │
└──────────────────┘    └──────────────────┘    │ - sede: Sede     │
                                                 └──────────────────┘

┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│     Compra       │    │     Entrada      │    │   TipoEntrada    │
├──────────────────┤    ├──────────────────┤    ├──────────────────┤
│ - id: Long       │    │ - id: Long       │    │ - id: Long       │
│ - fechaCompra    │    │ - numeroEntrada  │    │ - nombre: String │
│ - total: BigDec  │    │ - validada: bool │    │ - precio: BigDec │
│ - estado: Enum   │    │ - codigoQr       │    │ - activo: boolean│
│ - codigoConf     │    │ - tipoEntrada    │    │ - beneficios     │
│ - metodoPago     │    │ - evento: Evento │    └──────────────────┘
│ - usuario        │    │ - compra: Compra │
│ - entradas: List │    └──────────────────┘
└──────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE SERVICIOS                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│AutenticacionService│  │   EventoService    │  │   CompraService    │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│+ login()           │  │+ listarTodos()     │  │+ crear()           │
│+ logout()          │  │+ buscarPorId()     │  │+ procesarPago()    │
│+ getUsuarioActual()│  │+ crear()           │  │+ cancelar()        │
│+ verificarPermisos │  │+ actualizar()      │  │+ obtenerHistorial()│
└────────────────────┘  │+ eliminar()        │  └────────────────────┘
                        │+ filtrar()         │
                        └────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE REPOSITORIOS                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│UsuarioRepository   │  │  EventoRepository  │  │  CompraRepository  │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│+ findById()        │  │+ findById()        │  │+ findById()        │
│+ findByEmail()     │  │+ findAll()         │  │+ findByUsuario()   │
│+ save()            │  │+ findByEstado()    │  │+ save()            │
│+ delete()          │  │+ findByFecha()     │  │+ delete()          │
└────────────────────┘  │+ save()            │  └────────────────────┘
                        └────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE CONTROLADORES                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│  LoginController    │  │ DashboardController │  │EventosAdminController│
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│- emailField         │  │- statsLabels        │  │- eventosTable       │
│- passwordField      │  │- chartsPane         │  │- mapaWebView        │
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│+ handleLogin()      │  │+ initialize()       │  │+ handleCrearEvento()│
│+ handleRegistro()   │  │+ cargarEstadisticas │  │+ handleModificar()  │
└─────────────────────┘  └─────────────────────┘  │+ handleEliminar()   │
                                                  └─────────────────────┘
```

### Patrones de diseño utilizados:

- **Repository Pattern**: Abstracción de la capa de datos
- **Service Layer**: Lógica de negocio centralizada
- **MVC (Model-View-Controller)**: Separación de responsabilidades
- **Singleton**: Para servicios como `AutenticacionService`
- **DTO (Data Transfer Object)**: Para exportación XML/JSON

---

## 🎯 Diagrama de Casos de Uso

```
                           ┌─────────────────────────────────────┐
                           │     SISTEMA DE GESTIÓN DE EVENTOS   │
                           └─────────────────────────────────────┘

    ┌─────────┐                                                      ┌─────────┐
    │         │                                                      │         │
    │ Usuario │                                                      │  Admin  │
    │         │                                                      │         │
    └────┬────┘                                                      └────┬────┘
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         ├────┤  🔐 AUTENTICACIÓN                                    ├───┤
         │    │  ○ Iniciar sesión                                    │   │
         │    │  ○ Cerrar sesión                                     │   │
         │    │  ○ Registrarse (solo Usuario)                        │   │
         │    └──────────────────────────────────────────────────────┘   │
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         ├────┤  🎭 GESTIÓN DE EVENTOS                               ├───┤
         │    │  ○ Ver catálogo de eventos                           │   │
         │    │  ○ Buscar eventos por nombre/tipo/fecha              │   │
         │    │  ○ Filtrar eventos por estado                        │   │
         │    │  ○ Ver detalles de evento                            │   │
         │    │  ○ Ver ubicación en mapa                             │   │
         │    │  ● Crear evento (solo Admin)                         │   │
         │    │  ● Modificar evento (solo Admin)                     │   │
         │    │  ● Eliminar evento (solo Admin)                      │   │
         │    └──────────────────────────────────────────────────────┘   │
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         ├────┤  🎟️ GESTIÓN DE ENTRADAS                              │   │
         │    │  ○ Comprar entradas                                  │   │
         │    │  ○ Seleccionar tipo de entrada                       │   │
         │    │  ○ Ver mis entradas                                  │   │
         │    │  ○ Descargar código QR                               │   │
         │    │  ○ Cancelar compra                                   │   │
         │    └──────────────────────────────────────────────────────┘   │
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         │    │  🏢 GESTIÓN DE SEDES (solo Admin)                    ├───┤
         │    │  ● Ver listado de sedes                              │   │
         │    │  ● Crear sede                                        │   │
         │    │  ● Modificar sede                                    │   │
         │    │  ● Eliminar sede                                     │   │
         │    │  ● Gestionar equipamiento                            │   │
         │    └──────────────────────────────────────────────────────┘   │
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         │    │  📊 REPORTES Y EXPORTACIÓN (solo Admin)              ├───┤
         │    │  ● Exportar eventos a XML                            │   │
         │    │  ● Exportar eventos a JSON                           │   │
         │    │  ● Importar eventos desde XML                        │   │
         │    │  ● Importar eventos desde JSON                       │   │
         │    │  ● Ver estadísticas del dashboard                    │   │
         │    └──────────────────────────────────────────────────────┘   │
         │                                                                │
         │    ┌──────────────────────────────────────────────────────┐   │
         │    │                                                      │   │
         └────┤  👤 GESTIÓN DE PERFIL                                │   │
              │  ○ Ver mi perfil                                     │   │
              │  ○ Modificar mis datos                               │   │
              │  ○ Ver historial de compras                          │   │
              └──────────────────────────────────────────────────────┘

    Leyenda:
    ○ Caso de uso disponible para el actor
    ● Caso de uso exclusivo del actor
```

---

## 📋 Requisitos Funcionales y No Funcionales

### ✅ Requisitos Funcionales

#### RF01 - Autenticación y Autorización
| ID | Requisito | Prioridad |
|----|-----------|-----------|
| RF01.1 | El sistema debe permitir el registro de nuevos usuarios | Alta |
| RF01.2 | El sistema debe permitir iniciar sesión con email y contraseña | Alta |
| RF01.3 | El sistema debe diferenciar entre roles (Admin/Usuario) | Alta |
| RF01.4 | Las contraseñas deben almacenarse encriptadas (BCrypt) | Alta |
| RF01.5 | El sistema debe permitir cerrar sesión | Media |

#### RF02 - Gestión de Eventos
| ID | Requisito | Prioridad |
|----|-----------|-----------|
| RF02.1 | El administrador debe poder crear nuevos eventos | Alta |
| RF02.2 | El administrador debe poder modificar eventos existentes | Alta |
| RF02.3 | El administrador debe poder eliminar eventos | Alta |
| RF02.4 | Los usuarios deben poder ver el catálogo de eventos | Alta |
| RF02.5 | El sistema debe permitir filtrar eventos por tipo y estado | Media |
| RF02.6 | El sistema debe mostrar eventos en un mapa interactivo | Media |
| RF02.7 | El sistema debe controlar el aforo de los eventos | Alta |

#### RF03 - Gestión de Sedes
| ID | Requisito | Prioridad |
|----|-----------|-----------|
| RF03.1 | El administrador debe poder crear, modificar y eliminar sedes | Alta |
| RF03.2 | Cada sede debe tener ubicación geográfica (latitud/longitud) | Media |
| RF03.3 | El sistema debe mostrar sedes en el mapa | Media |
| RF03.4 | El administrador debe poder gestionar el equipamiento de sedes | Baja |

#### RF04 - Compra de Entradas
| ID | Requisito | Prioridad |
|----|-----------|-----------|
| RF04.1 | Los usuarios deben poder comprar entradas para eventos | Alta |
| RF04.2 | El sistema debe generar códigos QR para las entradas | Alta |
| RF04.3 | Los usuarios deben poder ver su historial de compras | Media |
| RF04.4 | El sistema debe permitir cancelar compras | Media |
| RF04.5 | El sistema debe soportar diferentes tipos de entrada | Media |

#### RF05 - Exportación/Importación de Datos
| ID | Requisito | Prioridad |
|----|-----------|-----------|
| RF05.1 | El sistema debe permitir exportar eventos a formato XML | Alta |
| RF05.2 | El sistema debe permitir exportar eventos a formato JSON | Alta |
| RF05.3 | El sistema debe permitir importar eventos desde XML | Media |
| RF05.4 | El sistema debe permitir importar eventos desde JSON | Media |

---

### 🔒 Requisitos No Funcionales

#### RNF01 - Rendimiento
| ID | Requisito | Métrica |
|----|-----------|---------|
| RNF01.1 | Tiempo de carga de la aplicación | < 5 segundos |
| RNF01.2 | Tiempo de respuesta para consultas | < 2 segundos |
| RNF01.3 | Soporte de usuarios concurrentes | Mínimo 10 |

#### RNF02 - Seguridad
| ID | Requisito |
|----|-----------|
| RNF02.1 | Las contraseñas deben estar encriptadas con BCrypt |
| RNF02.2 | Las sesiones deben expirar tras inactividad |
| RNF02.3 | Los datos sensibles no deben mostrarse en logs |
| RNF02.4 | Validación de entrada para prevenir SQL Injection |

#### RNF03 - Usabilidad
| ID | Requisito |
|----|-----------|
| RNF03.1 | Interfaz gráfica intuitiva con JavaFX |
| RNF03.2 | Mensajes de error claros y descriptivos |
| RNF03.3 | Confirmación antes de acciones destructivas |
| RNF03.4 | Diseño responsive y moderno |

#### RNF04 - Mantenibilidad
| ID | Requisito |
|----|-----------|
| RNF04.1 | Código estructurado en capas (MVC) |
| RNF04.2 | Uso de patrones de diseño (Repository, Service) |
| RNF04.3 | Código documentado con JavaDoc |
| RNF04.4 | Configuración externalizada en archivos properties |

#### RNF05 - Compatibilidad
| ID | Requisito |
|----|-----------|
| RNF05.1 | Compatible con Java 17+ |
| RNF05.2 | Compatible con MySQL 8.0+ |
| RNF05.3 | Funciona en Windows, macOS y Linux |

#### RNF06 - Disponibilidad
| ID | Requisito |
|----|-----------|
| RNF06.1 | Pool de conexiones con HikariCP |
| RNF06.2 | Manejo de errores con recuperación graceful |
| RNF06.3 | Logs de errores con Log4j2 |

---

## 📁 Estructura del Proyecto

```
├── src/
│   ├── main/
│   │   ├── java/com/eventos/
│   │   │   ├── config/          # Configuración (Hibernate, etc.)
│   │   │   ├── controllers/     # Controladores JavaFX
│   │   │   ├── dto/             # DTOs para XML/JSON
│   │   │   ├── exceptions/      # Excepciones personalizadas
│   │   │   ├── models/          # Entidades JPA
│   │   │   ├── repositories/    # Capa de acceso a datos
│   │   │   ├── services/        # Lógica de negocio
│   │   │   ├── utils/           # Utilidades
│   │   │   └── Main.java        # Punto de entrada
│   │   └── resources/
│   │       ├── css/             # Estilos
│   │       ├── fxml/            # Vistas JavaFX
│   │       ├── html/            # Mapa Leaflet
│   │       └── sql/             # Scripts SQL
│   └── test/                    # Tests unitarios
├── exports/                     # Archivos exportados
├── imports/                     # Archivos para importar
├── logs/                        # Logs de la aplicación
├── qr-codes/                    # Códigos QR generados
├── pom.xml                      # Configuración Maven
└── README.md                    # Este archivo
```

---
