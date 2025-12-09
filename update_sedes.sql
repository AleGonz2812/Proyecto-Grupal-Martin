-- Agregar columnas de coordenadas si no existen
ALTER TABLE sedes 
ADD COLUMN IF NOT EXISTS latitud DECIMAL(10, 7),
ADD COLUMN IF NOT EXISTS longitud DECIMAL(10, 7);

-- Actualizar coordenadas de las sedes existentes
UPDATE sedes SET latitud = 40.4381311, longitud = -3.6879691 WHERE nombre = 'Auditorio Nacional';
UPDATE sedes SET latitud = 41.3947688, longitud = 2.1249837 WHERE nombre = 'Palacio de Congresos';
UPDATE sedes SET latitud = 39.4699075, longitud = -0.3762881 WHERE nombre = 'Teatro Principal';
UPDATE sedes SET latitud = 37.3890924, longitud = -5.9844589 WHERE nombre = 'Estadio Municipal';
UPDATE sedes SET latitud = 41.6488226, longitud = -0.8890853 WHERE nombre = 'Centro de Convenciones';
