-- ============================================
-- Script de creación de Base de Datos
-- Sistema de Gestión de Eventos
-- Para MySQL 8.0+ (XAMPP)
-- ============================================

-- Crear base de datos (si no existe)
CREATE DATABASE IF NOT EXISTS eventos_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE eventos_db;

-- ============================================
-- Tabla: roles
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    INDEX idx_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: permisos_rol
-- ============================================
CREATE TABLE IF NOT EXISTS permisos_rol (
    rol_id BIGINT NOT NULL,
    permiso VARCHAR(100) NOT NULL,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_rol_id (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: usuarios
-- ============================================
CREATE TABLE IF NOT EXISTS usuarios (
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
    FOREIGN KEY (rol_id) REFERENCES roles(id),
    INDEX idx_email (email),
    INDEX idx_rol_id (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: tipos_evento
-- ============================================
CREATE TABLE IF NOT EXISTS tipos_evento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    categoria VARCHAR(50),
    INDEX idx_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: sedes
-- ============================================
CREATE TABLE IF NOT EXISTS sedes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    provincia VARCHAR(100),
    codigo_postal VARCHAR(10),
    capacidad INT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    telefono VARCHAR(20),
    descripcion TEXT,
    latitud DECIMAL(10,6),
    longitud DECIMAL(10,6),
    INDEX idx_ciudad (ciudad),
    INDEX idx_activa (activa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: eventos
-- ============================================
CREATE TABLE IF NOT EXISTS eventos (
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
    FOREIGN KEY (sede_id) REFERENCES sedes(id),
    INDEX idx_fecha_inicio (fecha_inicio),
    INDEX idx_estado (estado),
    INDEX idx_tipo_evento (tipo_evento_id),
    INDEX idx_sede (sede_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: equipamiento
-- ============================================
CREATE TABLE IF NOT EXISTS equipamiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sede_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    cantidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    categoria VARCHAR(100),
    FOREIGN KEY (sede_id) REFERENCES sedes(id) ON DELETE CASCADE,
    INDEX idx_sede_id (sede_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: tipos_entrada
-- ============================================
CREATE TABLE IF NOT EXISTS tipos_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    beneficios VARCHAR(100),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: compras
-- ============================================
CREATE TABLE IF NOT EXISTS compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_compra DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    codigo_confirmacion VARCHAR(100) UNIQUE,
    metodo_pago VARCHAR(50),
    confirmacion_json TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_fecha_compra (fecha_compra),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: entradas
-- ============================================
CREATE TABLE IF NOT EXISTS entradas (
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
    FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE,
    INDEX idx_numero_entrada (numero_entrada),
    INDEX idx_validada (validada),
    INDEX idx_evento_id (evento_id),
    INDEX idx_compra_id (compra_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla: registros_entrada
-- ============================================
CREATE TABLE IF NOT EXISTS registros_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entrada_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    fecha_hora_entrada DATETIME NOT NULL,
    empleado_validador VARCHAR(100),
    observaciones VARCHAR(255),
    FOREIGN KEY (entrada_id) REFERENCES entradas(id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id),
    INDEX idx_entrada_id (entrada_id),
    INDEX idx_evento_id (evento_id),
    INDEX idx_fecha_hora (fecha_hora_entrada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
