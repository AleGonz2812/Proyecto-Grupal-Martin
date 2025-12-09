USE eventos_db;

-- Agregar columna dni si no existe
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS dni VARCHAR(20) NULL AFTER apellidos;

SELECT 'Columna dni verificada/agregada correctamente' as resultado;
