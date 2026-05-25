-- Ejecutar en MySQL sobre la base cloty (ajustes para RUT en usuario/colegio y cuentas pendientes).

-- Apoderado sin usuario / email opcional (si aún no aplicó el parche anterior)
ALTER TABLE apoderado MODIFY COLUMN id_usuario INT NULL;
ALTER TABLE apoderado MODIFY COLUMN email VARCHAR(150) NULL;

-- Usuario: RUT del titular (administrador, colegio o apoderado)
ALTER TABLE usuario ADD COLUMN rut VARCHAR(12) NULL;
CREATE UNIQUE INDEX uq_usuario_rut ON usuario (rut);

-- Colegio: RUT institucional o de contacto; puede existir sin cuenta hasta activación
ALTER TABLE colegio ADD COLUMN rut VARCHAR(12) NULL;
ALTER TABLE colegio MODIFY COLUMN id_usuario INT NULL;
CREATE UNIQUE INDEX uq_colegio_rut ON colegio (rut);

-- Correo del colegio: lo ingresa el establecimiento al activar cuenta (puede ser NULL antes)
ALTER TABLE colegio MODIFY COLUMN email VARCHAR(150) NULL;
