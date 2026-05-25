-- Ejecutar en MySQL sobre la base cloty (una sola vez).
-- 1) Apoderado sin usuario hasta que active la cuenta.
-- 2) Correo opcional (CSV puede venir sin email o el apoderado usar otro al registrarse).

ALTER TABLE apoderado
	MODIFY COLUMN id_usuario INT NULL;

ALTER TABLE apoderado
	MODIFY COLUMN email VARCHAR(150) NULL;
