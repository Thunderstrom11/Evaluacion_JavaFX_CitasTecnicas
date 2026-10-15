-- ============================================================
-- Evaluacion JavaFX - Citas Tecnicas
-- Base de datos: gestion_citas   |   Motor: PostgreSQL
-- Ejecutar una sola vez con F5 en pgAdmin, sobre la base gestion_citas
-- ============================================================

DROP TABLE IF EXISTS cita_tecnica;
DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id        SERIAL       PRIMARY KEY,
    nombres   VARCHAR(80)  NOT NULL,
    apellidos VARCHAR(80)  NOT NULL,
    telefono  VARCHAR(20)  NOT NULL,
    correo    VARCHAR(120) NOT NULL UNIQUE,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE cita_tecnica (
    id            SERIAL       PRIMARY KEY,
    cliente_id    INTEGER      NOT NULL,
    fecha         DATE         NOT NULL,
    hora          TIME         NOT NULL,
    tipo_servicio VARCHAR(80)  NOT NULL,
    descripcion   VARCHAR(255) NOT NULL,
    estado        VARCHAR(20)  NOT NULL,

    CONSTRAINT fk_cita_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id),

    CONSTRAINT chk_cita_estado
        CHECK (estado IN ('Pendiente', 'En proceso', 'Finalizada', 'Cancelada'))
);

CREATE INDEX idx_cita_cliente ON cita_tecnica (cliente_id);
CREATE INDEX idx_cita_fecha   ON cita_tecnica (fecha);

-- ---------------------- Datos de prueba ----------------------
INSERT INTO cliente (nombres, apellidos, telefono, correo, activo) VALUES
 ('Maria',  'Lopez Ruiz',   '88881111', 'maria.lopez@correo.com',    TRUE),
 ('Carlos', 'Mendoza Diaz', '88882222', 'carlos.mendoza@correo.com', TRUE),
 ('Ana',    'Gutierrez',    '88883333', 'ana.gutierrez@correo.com',  FALSE);

INSERT INTO cita_tecnica (cliente_id, fecha, hora, tipo_servicio, descripcion, estado) VALUES
 (1, '2026-10-16', '09:00', 'Mantenimiento', 'Limpieza y cambio de pasta termica',  'Pendiente'),
 (1, '2026-10-17', '14:30', 'Reparacion',    'No enciende, revision de la fuente',  'En proceso'),
 (2, '2026-10-18', '10:15', 'Mantenimiento', 'Actualizacion del sistema operativo', 'Finalizada');
