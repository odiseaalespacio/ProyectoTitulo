-- =============================================================================
-- Cloty — esquema MySQL 8 / MariaDB 10.5+ (utf8mb4)
-- Referencia / reinicio total. En desarrollo la API también puede crear la BD,
-- tablas (Hibernate ddl-auto=update) y el super usuario inicial al arrancar.
-- Usar este script solo para borrar todo y empezar de cero:
--   mysql -u root -p < cloty-mysql.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS cloty
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
<

USE cloty;

SET NAMES utf8mb4;

-- Limpieza (orden inverso a las dependencias)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notificacion;
DROP TABLE IF EXISTS evento;
DROP TABLE IF EXISTS tarjeta;
DROP TABLE IF EXISTS alumno;
DROP TABLE IF EXISTS colegio_apoderado;
DROP TABLE IF EXISTS curso;
DROP TABLE IF EXISTS apoderado;
DROP TABLE IF EXISTS colegio;
DROP TABLE IF EXISTS codigo_activacion;
DROP TABLE IF EXISTS super_usuario;
DROP TABLE IF EXISTS administrador;
DROP TABLE IF EXISTS usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- usuario
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
  id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
  username       VARCHAR(50)  NOT NULL,
  rut            VARCHAR(12)  NULL,
  password_hash  VARCHAR(255) NOT NULL,
  rol            VARCHAR(20)  NOT NULL,
  estado         TINYINT(1)   NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_usuario_username (username),
  UNIQUE KEY uq_usuario_rut (rut)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- super_usuario (perfil del rol SUPER_USUARIO; mismo modelo que administrador)
-- ---------------------------------------------------------------------------
CREATE TABLE super_usuario (
  id_super_usuario INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario       INT          NOT NULL,
  rut              VARCHAR(12)  NOT NULL,
  nombres          VARCHAR(100) NOT NULL,
  apellidos        VARCHAR(100) NOT NULL,
  email            VARCHAR(150) NOT NULL,
  telefono         VARCHAR(20)  NULL,
  fecha_creacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_super_usuario_id_usuario (id_usuario),
  UNIQUE KEY uq_super_usuario_rut (rut),
  CONSTRAINT fk_super_usuario_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- administrador
-- ---------------------------------------------------------------------------
CREATE TABLE administrador (
  id_administrador INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario       INT          NOT NULL,
  rut              VARCHAR(12)  NOT NULL,
  nombres          VARCHAR(100) NOT NULL,
  apellidos        VARCHAR(100) NOT NULL,
  email            VARCHAR(150) NOT NULL,
  telefono         VARCHAR(20)  NULL,
  fecha_creacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_administrador_id_usuario (id_usuario),
  UNIQUE KEY uq_administrador_rut (rut),
  CONSTRAINT fk_administrador_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- colegio (puede existir sin cuenta de acceso; email para activación)
-- ---------------------------------------------------------------------------
CREATE TABLE colegio (
  id_colegio     INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario     INT          NULL,
  rut            VARCHAR(12)  NULL,
  nombre         VARCHAR(150) NOT NULL,
  email          VARCHAR(150) NULL,
  telefono       VARCHAR(20)  NULL,
  direccion      VARCHAR(255) NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_colegio_id_usuario (id_usuario),
  UNIQUE KEY uq_colegio_rut (rut),
  CONSTRAINT fk_colegio_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- apoderado
-- ---------------------------------------------------------------------------
CREATE TABLE apoderado (
  id_apoderado   INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario     INT          NULL,
  rut            VARCHAR(12)  NOT NULL,
  nombres        VARCHAR(100) NOT NULL,
  apellidos      VARCHAR(100) NOT NULL,
  email          VARCHAR(150) NULL,
  telefono       VARCHAR(20)  NULL,
  direccion      VARCHAR(255) NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_apoderado_id_usuario (id_usuario),
  UNIQUE KEY uq_apoderado_rut (rut),
  CONSTRAINT fk_apoderado_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- curso (se crean desde CSV o manualmente; sin cursos por defecto)
-- ---------------------------------------------------------------------------
CREATE TABLE curso (
  id_curso       INT AUTO_INCREMENT PRIMARY KEY,
  id_colegio     INT         NOT NULL,
  nombre         VARCHAR(50) NOT NULL,
  nivel          VARCHAR(50) NULL,
  estado         TINYINT(1)  NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_curso_colegio (id_colegio, nombre),
  CONSTRAINT fk_curso_colegio
    FOREIGN KEY (id_colegio) REFERENCES colegio (id_colegio)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- colegio_apoderado
-- ---------------------------------------------------------------------------
CREATE TABLE colegio_apoderado (
  id_colegio_apoderado INT AUTO_INCREMENT PRIMARY KEY,
  id_colegio           INT NOT NULL,
  id_apoderado         INT NOT NULL,
  fecha_asociacion     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_colegio_apoderado (id_colegio, id_apoderado),
  CONSTRAINT fk_ca_colegio
    FOREIGN KEY (id_colegio) REFERENCES colegio (id_colegio)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ca_apoderado
    FOREIGN KEY (id_apoderado) REFERENCES apoderado (id_apoderado)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- alumno
-- ---------------------------------------------------------------------------
CREATE TABLE alumno (
  id_alumno      INT AUTO_INCREMENT PRIMARY KEY,
  id_colegio     INT          NOT NULL,
  id_apoderado   INT          NOT NULL,
  id_curso       INT          NOT NULL,
  rut            VARCHAR(12)  NOT NULL,
  nombres        VARCHAR(100) NOT NULL,
  apellidos      VARCHAR(100) NOT NULL,
  estado         TINYINT(1)   NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_alumno_rut (rut),
  CONSTRAINT fk_alumno_colegio
    FOREIGN KEY (id_colegio) REFERENCES colegio (id_colegio)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_alumno_apoderado
    FOREIGN KEY (id_apoderado) REFERENCES apoderado (id_apoderado)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_alumno_curso
    FOREIGN KEY (id_curso) REFERENCES curso (id_curso)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- tarjeta
-- ---------------------------------------------------------------------------
CREATE TABLE tarjeta (
  id_tarjeta       INT AUTO_INCREMENT PRIMARY KEY,
  id_alumno        INT          NOT NULL,
  uid_nfc          VARCHAR(100) NOT NULL,
  codigo_visual    VARCHAR(100) NULL,
  tipo_prenda      VARCHAR(100) NULL,
  estado           VARCHAR(20)  NULL,
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tarjeta_uid_nfc (uid_nfc),
  CONSTRAINT fk_tarjeta_alumno
    FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- evento
-- ---------------------------------------------------------------------------
CREATE TABLE evento (
  id_evento      INT AUTO_INCREMENT PRIMARY KEY,
  id_tarjeta     INT          NOT NULL,
  tipo_evento    VARCHAR(40)  NOT NULL,
  descripcion    VARCHAR(500) NULL,
  ubicacion      VARCHAR(255) NULL,
  fecha_evento   TIMESTAMP    NULL,
  registrado_por INT          NULL,
  CONSTRAINT fk_evento_tarjeta
    FOREIGN KEY (id_tarjeta) REFERENCES tarjeta (id_tarjeta)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- notificacion
-- ---------------------------------------------------------------------------
CREATE TABLE notificacion (
  id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
  id_evento       INT          NOT NULL,
  id_apoderado    INT          NOT NULL,
  titulo          VARCHAR(200) NOT NULL,
  mensaje         VARCHAR(500) NOT NULL,
  estado          VARCHAR(20)  NULL,
  leida           TINYINT(1)   NULL,
  fecha_envio     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notificacion_evento
    FOREIGN KEY (id_evento) REFERENCES evento (id_evento)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_notificacion_apoderado
    FOREIGN KEY (id_apoderado) REFERENCES apoderado (id_apoderado)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- codigo_activacion (activación de cuentas y recuperación de contraseña)
-- id_entidad = id_apoderado | id_colegio | id_usuario según tipo
-- ---------------------------------------------------------------------------
CREATE TABLE codigo_activacion (
  id_codigo_activacion INT AUTO_INCREMENT PRIMARY KEY,
  tipo                 VARCHAR(20)  NOT NULL,
  id_entidad           INT          NOT NULL,
  codigo_hash          VARCHAR(255) NOT NULL,
  expira_en            TIMESTAMP    NOT NULL,
  usado                TINYINT(1)   NOT NULL DEFAULT 0,
  fecha_creacion       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_codigo_tipo_entidad (tipo, id_entidad)
) ENGINE=InnoDB;

-- Valores enum (referencia; la app usa VARCHAR):
-- usuario.rol: SUPER_USUARIO | ADMINISTRADOR | COLEGIO | APODERADO
-- tarjeta.estado: ACTIVA | PERDIDA | DESACTIVADA
-- notificacion.estado: PENDIENTE | ENVIADA | ERROR
-- evento.tipo_evento: PRENDA_ENCONTRADA | PRENDA_RECUPERADA | NOTIFICACION_ENVIADA | TARJETA_DESACTIVADA
-- codigo_activacion.tipo: APODERADO | COLEGIO | RECUPERACION_CONTRASENA

-- ---------------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------------

-- Super usuario (panel admin). Usuario: superadmin | Contraseña: super123
-- Cambiar password_hash en producción.
INSERT INTO usuario (username, rut, password_hash, rol, estado)
VALUES (
  'superadmin',
  '00000000-0',
  '$2a$10$js51SAu/N4sQGVPKwqK4nulST86adcFFPIjXPIpBlDmPQxVQvCwR6',
  'SUPER_USUARIO',
  1
);

INSERT INTO super_usuario (id_usuario, rut, nombres, apellidos, email)
VALUES (
  1,
  '00000000-0',
  'Super',
  'Administrador',
  'superadmin@cloty.local'
);
