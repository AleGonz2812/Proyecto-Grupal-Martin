USE eventos_db;

-- Primero insertar las sedes si no existen
INSERT IGNORE INTO sedes (nombre, direccion, ciudad, provincia, codigoPostal, capacidad, activa, telefono, descripcion, latitud, longitud) VALUES
('Auditorio Nacional', 'Paseo de la Castellana 99', 'Madrid', 'Madrid', '28046', 5000, TRUE, '912345678', 'Gran auditorio con excelente acústica', 40.4381311, -3.6879691),
('Palacio de Congresos', 'Avenida Diagonal 661', 'Barcelona', 'Barcelona', '08028', 3000, TRUE, '934567890', 'Espacio versátil para eventos corporativos y culturales', 41.3947688, 2.1249837),
('Teatro Principal', 'Calle Mayor 10', 'Valencia', 'Valencia', '46001', 800, TRUE, '963456789', 'Teatro histórico restaurado', 39.4699075, -0.3762881),
('Estadio Municipal', 'Avenida del Estadio 1', 'Sevilla', 'Sevilla', '41005', 15000, TRUE, '954567890', 'Estadio deportivo multiusos', 37.3890924, -5.9844589),
('Centro de Convenciones', 'Plaza España 5', 'Zaragoza', 'Zaragoza', '50001', 2000, TRUE, '976234567', 'Moderno centro de convenciones', 41.6488226, -0.8890853);

-- Insertar 10 eventos de prueba con variedad de tipos, sedes y estados

-- Evento 1: Concierto de Rock en Auditorio Nacional
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (1, 1, 'Rock Fest Madrid 2025', 'Gran festival de rock con bandas nacionales e internacionales', '2025-12-20 20:00:00', '2025-12-21 02:00:00', 5000, 1250, 'ACTIVO', 45.00, 'https://example.com/rock-fest.jpg', NOW());

-- Evento 2: Conferencia Tecnológica en Palacio de Congresos
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (7, 2, 'Tech Summit Barcelona 2025', 'Conferencia sobre inteligencia artificial y blockchain', '2025-12-15 09:00:00', '2025-12-15 18:00:00', 3000, 2100, 'ACTIVO', 120.00, 'https://example.com/tech-summit.jpg', NOW());

-- Evento 3: Obra de Teatro en Teatro Principal
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (3, 3, 'Hamlet - Versión Moderna', 'Adaptación contemporánea de la obra clásica de Shakespeare', '2025-12-18 19:30:00', '2025-12-18 22:00:00', 800, 650, 'ACTIVO', 35.00, 'https://example.com/hamlet.jpg', NOW());

-- Evento 4: Partido de Fútbol en Estadio Municipal
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (5, 4, 'Sevilla FC vs Real Betis', 'Derbi sevillano - Liga española de fútbol', '2025-12-22 21:00:00', '2025-12-22 23:00:00', 15000, 14500, 'ACTIVO', 50.00, 'https://example.com/derbi.jpg', NOW());

-- Evento 5: Seminario Empresarial en Centro de Convenciones
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (8, 5, 'Liderazgo y Gestión de Equipos', 'Seminario intensivo sobre management y recursos humanos', '2025-12-16 10:00:00', '2025-12-16 17:00:00', 2000, 850, 'ACTIVO', 95.00, 'https://example.com/seminario.jpg', NOW());

-- Evento 6: Concierto Pop en Auditorio Nacional
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (2, 1, 'Noche de Pop Español', 'Los mejores artistas pop de la década', '2025-12-25 21:00:00', '2025-12-26 01:00:00', 5000, 3200, 'ACTIVO', 55.00, 'https://example.com/pop-night.jpg', NOW());

-- Evento 7: Comedia Stand-Up en Teatro Principal
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (4, 3, 'Risas Sin Parar - Tour 2025', 'Show de comedia con los mejores humoristas españoles', '2025-12-19 20:00:00', '2025-12-19 22:30:00', 800, 450, 'ACTIVO', 28.00, 'https://example.com/comedia.jpg', NOW());

-- Evento 8: Partido de Baloncesto en Estadio Municipal
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (6, 4, 'Valencia Basket vs Barcelona', 'Liga ACB - Cuartos de final', '2025-12-28 18:00:00', '2025-12-28 20:00:00', 15000, 8900, 'ACTIVO', 42.00, 'https://example.com/basket.jpg', NOW());

-- Evento 9: Conferencia en Palacio de Congresos - PLANIFICADO
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (7, 2, 'Innovación Digital 2026', 'Próxima conferencia sobre transformación digital', '2026-01-15 09:00:00', '2026-01-15 19:00:00', 3000, 0, 'PLANIFICADO', 150.00, 'https://example.com/innovacion.jpg', NOW());

-- Evento 10: Festival de Teatro en Teatro Principal - CANCELADO
INSERT INTO eventos (tipo_evento_id, sede_id, nombre, descripcion, fecha_inicio, fecha_fin, aforo_maximo, aforo_actual, estado, precioBase, imagenUrl, fecha_creacion) 
VALUES (3, 3, 'Festival Internacional de Teatro', 'Festival cancelado por causas técnicas', '2025-12-10 18:00:00', '2025-12-12 23:00:00', 800, 0, 'CANCELADO', 40.00, 'https://example.com/festival-teatro.jpg', NOW());

SELECT 'Se han insertado 10 eventos de prueba correctamente' as resultado;
