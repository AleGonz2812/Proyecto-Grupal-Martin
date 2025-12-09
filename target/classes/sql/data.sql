-- ============================================
-- Datos iniciales para el Sistema de Eventos
-- Para MySQL 8.0+ (XAMPP)
-- ============================================

USE eventos_db;

-- ============================================
-- Insertar Roles
-- ============================================
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema con todos los permisos'),
('USUARIO', 'Usuario normal que puede comprar entradas'),
('EMPLEADO', 'Empleado que valida entradas en eventos');

-- ============================================
-- Insertar Permisos para cada Rol
-- ============================================

-- Permisos ADMIN
INSERT INTO permisos_rol (rol_id, permiso) VALUES
(1, 'CREAR_EVENTO'),
(1, 'EDITAR_EVENTO'),
(1, 'ELIMINAR_EVENTO'),
(1, 'VER_INFORMES'),
(1, 'GESTIONAR_USUARIOS'),
(1, 'GESTIONAR_SEDES'),
(1, 'GESTIONAR_EQUIPAMIENTO'),
(1, 'EXPORTAR_DATOS'),
(1, 'IMPORTAR_DATOS');

-- Permisos USUARIO
INSERT INTO permisos_rol (rol_id, permiso) VALUES
(2, 'COMPRAR_ENTRADA'),
(2, 'VER_HISTORIAL'),
(2, 'VER_EVENTOS');

-- Permisos EMPLEADO
INSERT INTO permisos_rol (rol_id, permiso) VALUES
(3, 'VALIDAR_ENTRADA'),
(3, 'VER_EVENTOS'),
(3, 'REGISTRAR_ACCESO');

-- ============================================
-- Insertar Usuario Administrador
-- Password: admin123 (hash BCrypt)
-- ============================================
INSERT INTO usuarios (rol_id, email, password, nombre, apellidos, activo, fecha_alta) VALUES
(1, 'admin@eventos.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5XAOKhRjqOQKa', 'Administrador', 'Sistema', TRUE, NOW());

-- ============================================
-- Insertar Tipos de Evento
-- ============================================
INSERT INTO tipos_evento (nombre, descripcion, categoria) VALUES
('Concierto de Rock', 'Eventos musicales de rock y metal', 'CULTURAL'),
('Concierto Pop', 'Eventos musicales de música pop', 'CULTURAL'),
('Teatro', 'Obras teatrales y representaciones', 'CULTURAL'),
('Comedia Stand-Up', 'Shows de comedia en vivo', 'ENTRETENIMIENTO'),
('Partido de Fútbol', 'Eventos deportivos de fútbol', 'DEPORTIVO'),
('Partido de Baloncesto', 'Eventos deportivos de baloncesto', 'DEPORTIVO'),
('Conferencia Tecnológica', 'Conferencias sobre tecnología', 'CORPORATIVO'),
('Seminario Empresarial', 'Seminarios y talleres empresariales', 'CORPORATIVO');

-- ============================================
-- Insertar Sedes
-- ============================================
INSERT INTO sedes (nombre, direccion, ciudad, provincia, codigo_postal, capacidad, activa, telefono, descripcion, latitud, longitud) VALUES
('Auditorio Nacional', 'Paseo de la Castellana 99', 'Madrid', 'Madrid', '28046', 5000, TRUE, '912345678', 'Gran auditorio con excelente acústica', 40.4381311, -3.6879691),
('Palacio de Congresos', 'Avenida Diagonal 661', 'Barcelona', 'Barcelona', '08028', 3000, TRUE, '934567890', 'Espacio versátil para eventos corporativos y culturales', 41.3947688, 2.1249837),
('Teatro Principal', 'Calle Mayor 10', 'Valencia', 'Valencia', '46001', 800, TRUE, '963456789', 'Teatro histórico restaurado', 39.4699075, -0.3762881),
('Estadio Municipal', 'Avenida del Estadio 1', 'Sevilla', 'Sevilla', '41005', 15000, TRUE, '954567890', 'Estadio deportivo multiusos', 37.3890924, -5.9844589),
('Centro de Convenciones', 'Plaza España 5', 'Zaragoza', 'Zaragoza', '50001', 2000, TRUE, '976234567', 'Moderno centro de convenciones', 41.6488226, -0.8890853);

-- ============================================
-- Insertar Equipamiento
-- ============================================
INSERT INTO equipamiento (sede_id, nombre, descripcion, cantidad, estado, categoria) VALUES
-- Auditorio Nacional
(1, 'Sistema de Sonido Profesional', 'Equipo de audio de alta gama', 1, 'DISPONIBLE', 'Audio'),
(1, 'Proyector 4K', 'Proyector de alta resolución', 2, 'DISPONIBLE', 'Video'),
(1, 'Micrófonos inalámbricos', 'Micrófonos profesionales', 20, 'DISPONIBLE', 'Audio'),
(1, 'Iluminación LED', 'Sistema de iluminación completo', 1, 'DISPONIBLE', 'Iluminación'),

-- Palacio de Congresos
(2, 'Pantallas LED gigantes', 'Pantallas de alta definición', 4, 'DISPONIBLE', 'Video'),
(2, 'Sistema de traducción simultánea', 'Equipos de traducción', 10, 'DISPONIBLE', 'Audio'),
(2, 'Sillas plegables', 'Sillas para eventos', 3000, 'DISPONIBLE', 'Mobiliario'),

-- Teatro Principal
(3, 'Sistema de iluminación teatral', 'Focos y luces especializadas', 1, 'DISPONIBLE', 'Iluminación'),
(3, 'Telón motorizado', 'Telón de escenario automático', 1, 'DISPONIBLE', 'Escenografía'),

-- Estadio Municipal
(4, 'Marcador electrónico', 'Marcador digital grande', 2, 'DISPONIBLE', 'Deportivo'),
(4, 'Sistema de megafonía', 'Altavoces de estadio', 1, 'DISPONIBLE', 'Audio');

-- ============================================
-- Insertar Tipos de Entrada
-- ============================================
INSERT INTO tipos_entrada (nombre, descripcion, precio, activo, beneficios) VALUES
('General', 'Entrada general sin asiento reservado', 25.00, TRUE, 'Acceso general al evento'),
('Preferente', 'Entrada con asiento reservado en zona preferente', 50.00, TRUE, 'Asiento reservado en zona preferente'),
('VIP', 'Entrada VIP con beneficios exclusivos', 100.00, TRUE, 'Asiento VIP, acceso a zona privada, bebida incluida'),
('Palco', 'Acceso a palco privado', 200.00, TRUE, 'Palco privado para 4 personas con servicio de catering');

-- ============================================
-- Mensaje de confirmación
-- ============================================
SELECT 'Base de datos inicializada correctamente' AS mensaje;
SELECT COUNT(*) AS total_roles FROM roles;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
SELECT COUNT(*) AS total_tipos_evento FROM tipos_evento;
SELECT COUNT(*) AS total_sedes FROM sedes;
SELECT COUNT(*) AS total_equipamiento FROM equipamiento;
SELECT COUNT(*) AS total_tipos_entrada FROM tipos_entrada;
