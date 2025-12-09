USE eventos_db;

-- Actualizar contraseña del administrador
-- Password: admin123 (hash BCrypt válido con 12 rounds)
UPDATE usuarios 
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5XAOKhRjqOQKa'
WHERE email = 'admin@eventos.com';

-- Verificar actualización
SELECT email, 'Password actualizado correctamente' as estado 
FROM usuarios 
WHERE email = 'admin@eventos.com';
