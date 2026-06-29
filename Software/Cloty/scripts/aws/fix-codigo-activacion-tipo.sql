-- Corrige columna demasiado corta para RECUPERACION_CONTRASENA (22 caracteres).
-- Ejecutar en MariaDB del servidor AWS si los correos de recuperación fallan con DATA_INTEGRITY.
ALTER TABLE codigo_activacion MODIFY COLUMN tipo VARCHAR(32) NOT NULL;
