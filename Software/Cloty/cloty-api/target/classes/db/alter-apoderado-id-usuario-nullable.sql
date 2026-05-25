-- Ejecutar en MySQL sobre la base cloty (una sola vez).
-- Permite que el admin cree apoderados sin cuenta; el apoderado activa después vía API.

ALTER TABLE apoderado
	MODIFY COLUMN id_usuario INT NULL;
