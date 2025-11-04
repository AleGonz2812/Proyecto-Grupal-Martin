# Modelo de Datos - Sistema de Gestión de Eventos

**Proyecto:** Sistema de Gestión de Empresa de Eventos  
**Versión:** 1.0  
**Fecha:** 4 de noviembre de 2025

---

## 📊 Diagrama Entidad-Relación

```
┌─────────────┐         ┌─────────────┐
│     ROL     │────┐    │   USUARIO   │
├─────────────┤    │    ├─────────────┤
│ id (PK)     │    │    │ id (PK)     │
│ nombre      │    └────│ rol_id (FK) │
│ descripcion │         │ email       │
│ permisos    │         │ password    │
└─────────────┘         │ nombre      │
                        │ telefono    │
                        │ activo      │
                        │ fechaAlta   │
                        └──────┬──────┘
                               │ 1
                               │
                               │ N
                        ┌──────┴──────┐
                        │   COMPRA    │
                        ├─────────────┤
                        │ id (PK)     │
                        │ usuario_id  │
                        │ fecha       │
                        │ total       │
                        │ estado      │
                        │ codigoQR    │
                        │ jsonConfirm │
                        └──────┬──────┘
                               │ 1
                               │
                               │ N
┌─────────────┐         ┌──────┴──────┐         ┌─────────────┐
│TIPO_ENTRADA │────N────│   ENTRADA   │────N────│   EVENTO    │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │         │ id (PK)     │
│ nombre      │         │ tipoEnt_id  │         │ tipoEv_id   │
│ descripcion │         │ evento_id   │         │ sede_id (FK)│
│ precio      │         │ compra_id   │         │ nombre      │
│ activo      │         │ numero      │         │ descripcion │
└─────────────┘         │ validada    │         │ fechaInicio │
                        │ fechaValid  │         │ fechaFin    │
                        └─────────────┘         │ aforoMax    │
                                                │ aforoActual │
                                                │ estado      │
                                                │ precio      │
                                                └──────┬──────┘
                                                       │ N
                                                       │
                                                       │ 1
┌─────────────┐         ┌─────────────┐         ┌──────┴──────┐
│TIPO_EVENTO  │────N────│    SEDE     │────N────│EQUIPAMIENTO │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │         │ id (PK)     │
│ nombre      │         │ tipoEv_id   │         │ sede_id (FK)│
│ descripcion │         │ nombre      │         │ nombre      │
│ categoria   │         │ direccion   │         │ descripcion │
└─────────────┘         │ ciudad      │         │ cantidad    │
                        │ capacidad   │         │ estado      │
                        │ activa      │         └─────────────┘
                        └─────────────┘

                        ┌─────────────────┐
                        │ REGISTRO_ENTRADA│
                        ├─────────────────┤
                        │ id (PK)         │
                        │ entrada_id (FK) │
                        │ evento_id (FK)  │
                        │ fechaHora       │
                        │ empleado        │
                        └─────────────────┘
```

---

## 🗂️ Entidades JPA Detalladas

### **1. Usuario**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password; // Hash BCrypt
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 100)
    private String apellidos;
    
    @Column(length = 20)
    private String telefono;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;
    
    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        fechaAlta = LocalDateTime.now();
    }
}
```

**Campos clave:**
- `password`: Hash BCrypt (nunca almacenar en texto plano)
- `activo`: Para desactivar usuarios sin eliminarlos
- `compras`: Relación 1:N con historial de compras

---

### **2. Rol**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // ADMIN, USUARIO, EMPLEADO
    
    @Column(length = 255)
    private String descripcion;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permisos_rol", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso")
    private List<String> permisos = new ArrayList<>();
    
    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios = new ArrayList<>();
}
```

**Permisos típicos:**
- ADMIN: `CREAR_EVENTO`, `ELIMINAR_EVENTO`, `VER_INFORMES`, `GESTIONAR_USUARIOS`
- USUARIO: `COMPRAR_ENTRADA`, `VER_HISTORIAL`
- EMPLEADO: `VALIDAR_ENTRADA`, `VER_EVENTOS`

---

### **3. Evento**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Evento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_evento_id", nullable = false)
    private TipoEvento tipoEvento;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;
    
    @Column(name = "aforo_maximo", nullable = false)
    private Integer aforoMaximo;
    
    @Column(name = "aforo_actual")
    private Integer aforoActual = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvento estado; // PLANIFICADO, ACTIVO, CANCELADO, FINALIZADO
    
    @Column(precision = 10, scale = 2)
    private BigDecimal precioBase;
    
    @Column(length = 500)
    private String imagenUrl;
    
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<Entrada> entradas = new ArrayList<>();
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoEvento.PLANIFICADO;
        }
    }
    
    public boolean hayDisponibilidad() {
        return aforoActual < aforoMaximo;
    }
    
    public void incrementarAforo(int cantidad) {
        this.aforoActual += cantidad;
    }
}
```

**Estados del evento:**
```java
public enum EstadoEvento {
    PLANIFICADO,
    ACTIVO,
    CANCELADO,
    FINALIZADO
}
```

---

### **4. TipoEvento**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipos_evento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class TipoEvento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String nombre; // Concierto, Teatro, Deportivo, Conferencia, etc.
    
    @Column(length = 255)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategoriaEvento categoria; // CULTURAL, DEPORTIVO, CORPORATIVO, ENTRETENIMIENTO
    
    @OneToMany(mappedBy = "tipoEvento")
    private List<Evento> eventos = new ArrayList<>();
}
```

---

### **5. Sede**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sedes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Sede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(nullable = false, length = 255)
    private String direccion;
    
    @Column(nullable = false, length = 100)
    private String ciudad;
    
    @Column(length = 100)
    private String provincia;
    
    @Column(length = 10)
    private String codigoPostal;
    
    @Column(nullable = false)
    private Integer capacidad;
    
    @Column(nullable = false)
    private Boolean activa = true;
    
    @Column(length = 20)
    private String telefono;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @OneToMany(mappedBy = "sede", cascade = CascadeType.ALL)
    private List<Evento> eventos = new ArrayList<>();
    
    @OneToMany(mappedBy = "sede", cascade = CascadeType.ALL)
    private List<Equipamiento> equipamientos = new ArrayList<>();
}
```

---

### **6. Equipamiento**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipamiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Equipamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;
    
    @Column(nullable = false, length = 100)
    private String nombre; // Proyector, Micrófono, Sillas, etc.
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEquipamiento estado; // DISPONIBLE, EN_USO, MANTENIMIENTO, DAÑADO
    
    @Column(length = 100)
    private String categoria; // Audio, Video, Mobiliario, etc.
}
```

---

### **7. TipoEntrada**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tipos_entrada")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class TipoEntrada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre; // VIP, General, Preferente, etc.
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(length = 100)
    private String beneficios; // "Asiento reservado, bebida incluida"
}
```

---

### **8. Entrada**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entradas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Entrada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_entrada_id", nullable = false)
    private TipoEntrada tipoEntrada;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numeroEntrada; // Código único de la entrada
    
    @Column(nullable = false)
    private Boolean validada = false;
    
    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;
    
    @Column(name = "codigo_qr", unique = true, length = 500)
    private String codigoQR; // Ruta al archivo QR o código base64
    
    @PrePersist
    protected void onCreate() {
        if (numeroEntrada == null) {
            numeroEntrada = generarNumeroEntrada();
        }
    }
    
    private String generarNumeroEntrada() {
        return "ENT-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 10000);
    }
}
```

---

### **9. Compra**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado; // PENDIENTE, COMPLETADA, CANCELADA
    
    @Column(name = "codigo_confirmacion", unique = true, length = 100)
    private String codigoConfirmacion;
    
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // TARJETA, PAYPAL, TRANSFERENCIA
    
    @Column(name = "confirmacion_json", columnDefinition = "TEXT")
    private String confirmacionJSON; // Confirmación serializada en JSON
    
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entrada> entradas = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        fechaCompra = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoCompra.PENDIENTE;
        }
        if (codigoConfirmacion == null) {
            codigoConfirmacion = generarCodigoConfirmacion();
        }
    }
    
    private String generarCodigoConfirmacion() {
        return "COMP-" + System.currentTimeMillis();
    }
    
    public void calcularTotal() {
        this.total = entradas.stream()
            .map(e -> e.getTipoEntrada().getPrecio())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

**Estados de compra:**
```java
public enum EstadoCompra {
    PENDIENTE,
    COMPLETADA,
    CANCELADA,
    REEMBOLSADA
}
```

---

### **10. RegistroEntrada**

```java
package com.eventos.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_entrada")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
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
    private String empleadoValidador; // Nombre del empleado que validó
    
    @Column(length = 255)
    private String observaciones;
    
    @PrePersist
    protected void onCreate() {
        fechaHoraEntrada = LocalDateTime.now();
    }
}
```

---

## 📈 Relaciones entre Entidades

| Entidad 1 | Relación | Entidad 2 | Descripción |
|-----------|----------|-----------|-------------|
| Usuario | N:1 | Rol | Un usuario tiene un rol |
| Usuario | 1:N | Compra | Un usuario puede tener muchas compras |
| Compra | 1:N | Entrada | Una compra contiene múltiples entradas |
| Evento | N:1 | TipoEvento | Un evento pertenece a un tipo |
| Evento | N:1 | Sede | Un evento se realiza en una sede |
| Evento | 1:N | Entrada | Un evento tiene múltiples entradas |
| Sede | 1:N | Equipamiento | Una sede tiene múltiples equipamientos |
| Entrada | N:1 | TipoEntrada | Una entrada es de un tipo específico |
| Entrada | 1:1 | RegistroEntrada | Una entrada validada genera un registro |

---

## 🗄️ Script SQL de Creación (schema.sql)

```sql
-- Creación de base de datos
CREATE DATABASE IF NOT EXISTS eventos_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE eventos_db;

-- Tabla roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

-- Tabla usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_id BIGINT NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATETIME NOT NULL,
    ultima_conexion DATETIME,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Tabla tipos_evento
CREATE TABLE tipos_evento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    categoria VARCHAR(50)
);

-- Tabla sedes
CREATE TABLE sedes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    provincia VARCHAR(100),
    codigo_postal VARCHAR(10),
    capacidad INT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    telefono VARCHAR(20),
    descripcion TEXT
);

-- Tabla eventos
CREATE TABLE eventos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento_id BIGINT NOT NULL,
    sede_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    aforo_maximo INT NOT NULL,
    aforo_actual INT DEFAULT 0,
    estado VARCHAR(20) NOT NULL,
    precio_base DECIMAL(10,2),
    imagen_url VARCHAR(500),
    fecha_creacion DATETIME,
    FOREIGN KEY (tipo_evento_id) REFERENCES tipos_evento(id),
    FOREIGN KEY (sede_id) REFERENCES sedes(id)
);

-- Tabla equipamiento
CREATE TABLE equipamiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sede_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    cantidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    categoria VARCHAR(100),
    FOREIGN KEY (sede_id) REFERENCES sedes(id)
);

-- Tabla tipos_entrada
CREATE TABLE tipos_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    beneficios VARCHAR(100)
);

-- Tabla compras
CREATE TABLE compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_compra DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    codigo_confirmacion VARCHAR(100) UNIQUE,
    metodo_pago VARCHAR(50),
    confirmacion_json TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla entradas
CREATE TABLE entradas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_entrada_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    compra_id BIGINT NOT NULL,
    numero_entrada VARCHAR(50) UNIQUE NOT NULL,
    validada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_validacion DATETIME,
    codigo_qr VARCHAR(500) UNIQUE,
    FOREIGN KEY (tipo_entrada_id) REFERENCES tipos_entrada(id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id),
    FOREIGN KEY (compra_id) REFERENCES compras(id)
);

-- Tabla registros_entrada
CREATE TABLE registros_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entrada_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    fecha_hora_entrada DATETIME NOT NULL,
    empleado_validador VARCHAR(100),
    observaciones VARCHAR(255),
    FOREIGN KEY (entrada_id) REFERENCES entradas(id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_evento_fecha ON eventos(fecha_inicio);
CREATE INDEX idx_entrada_validada ON entradas(validada);
CREATE INDEX idx_compra_usuario ON compras(usuario_id);
```

---

## 🎲 Datos Iniciales (data.sql)

```sql
-- Insertar roles
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema con todos los permisos'),
('USUARIO', 'Usuario normal que puede comprar entradas'),
('EMPLEADO', 'Empleado que valida entradas en eventos');

-- Insertar usuario administrador (password: admin123)
INSERT INTO usuarios (rol_id, email, password, nombre, activo, fecha_alta) VALUES
(1, 'admin@eventos.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', TRUE, NOW());

-- Insertar tipos de evento
INSERT INTO tipos_evento (nombre, descripcion, categoria) VALUES
('Concierto de Rock', 'Eventos musicales de rock y metal', 'CULTURAL'),
('Teatro', 'Obras teatrales y representaciones', 'CULTURAL'),
('Partido de Fútbol', 'Eventos deportivos de fútbol', 'DEPORTIVO'),
('Conferencia Tecnológica', 'Conferencias sobre tecnología', 'CORPORATIVO');

-- Insertar sedes
INSERT INTO sedes (nombre, direccion, ciudad, capacidad, activa) VALUES
('Auditorio Nacional', 'Paseo de la Castellana 99', 'Madrid', 5000, TRUE),
('Palacio de Congresos', 'Avenida Capital de España', 'Barcelona', 3000, TRUE);

-- Insertar tipos de entrada
INSERT INTO tipos_entrada (nombre, descripcion, precio, activo) VALUES
('General', 'Entrada general sin asiento reservado', 25.00, TRUE),
('Preferente', 'Entrada con asiento reservado', 50.00, TRUE),
('VIP', 'Entrada VIP con beneficios exclusivos', 100.00, TRUE);
```

---

**Siguiente documento:** Guía de Implementación y Ejemplos XML/JSON
