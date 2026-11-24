

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

BEGIN TRANSACTION;
CREATE TABLE catalogo_diagnosticos (
    id_diagnostico    INTEGER PRIMARY KEY,
    codigo            TEXT UNIQUE NOT NULL,
    nombre_comun      TEXT NOT NULL,
    nombre_cientifico TEXT,
    tipo_afectacion   TEXT NOT NULL CHECK(tipo_afectacion IN ('Plaga entomologica','Estres Abiotico','Fisiologico Optimo')),
    descripcion       TEXT NOT NULL,
    umbral_alerta     REAL NOT NULL DEFAULT 0.75
);
INSERT INTO "catalogo_diagnosticos" VALUES(0,'SANO','Cultivo Sano','Hojas asintomaticas','Fisiologico Optimo','Foliolo vigoroso con coloracion verde uniforme, sin lesiones fitopatogenas.',0.8);
INSERT INTO "catalogo_diagnosticos" VALUES(1,'TRIPS','Trips tabaci','Thrips tabaci Lindeman','Plaga entomologica','Genera plateado foliar, clorosis puntiforme y deformacion de brotes en Lurin.',0.7);
INSERT INTO "catalogo_diagnosticos" VALUES(2,'ESTRES_HIDRICO','Estres Hidrico','Deficit de humedad/turgencia','Estres Abiotico','Perdida de turgencia celular foliar por desbalance evapotranspirativo.',0.75);
CREATE TABLE inspeccion_recomendaciones (
    id_detalle        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inspeccion     TEXT    NOT NULL,
    id_recomendacion  INTEGER NOT NULL,
    aplicada          INTEGER NOT NULL DEFAULT 0,
    fecha_aplicacion  DATETIME,
    observaciones     TEXT,
    FOREIGN KEY (id_inspeccion)    REFERENCES inspecciones_campo(id_inspeccion)  ON DELETE CASCADE,
    FOREIGN KEY (id_recomendacion) REFERENCES recomendaciones_catalogo(id_recomendacion) ON DELETE RESTRICT
);
INSERT INTO "inspeccion_recomendaciones" VALUES(1,'insp-01',3,1,NULL,'Trampas colocadas en surco evaluado');
INSERT INTO "inspeccion_recomendaciones" VALUES(2,'insp-01',4,0,NULL,'Pendiente aplicacion vespertina');
INSERT INTO "inspeccion_recomendaciones" VALUES(3,'insp-02',6,0,NULL,'Reportado a encargado de fertirriego');
INSERT INTO "inspeccion_recomendaciones" VALUES(4,'insp-03',1,1,NULL,'Sin novedad fitopatologica');
INSERT INTO "inspeccion_recomendaciones" VALUES(5,'insp-04',3,0,NULL,'Pendiente revision');
INSERT INTO "inspeccion_recomendaciones" VALUES(6,'insp-05',6,1,NULL,'Ajuste de riego realizado');
INSERT INTO "inspeccion_recomendaciones" VALUES(7,'insp-05',7,1,NULL,'Tensiometros calibrados');
CREATE TABLE inspecciones_campo (
    id_inspeccion             TEXT    PRIMARY KEY,
    id_usuario                INTEGER,
    id_parcela                TEXT    NOT NULL,
    sub_parcela_surco         TEXT    NOT NULL,
    ruta_imagen_local         TEXT    NOT NULL,
    id_diagnostico            INTEGER NOT NULL,
    confianza_ia              REAL    NOT NULL,
    latencia_inferencia_ms    INTEGER NOT NULL,
    nivel_severidad           TEXT    NOT NULL CHECK(nivel_severidad IN ('Bajo','Medio','Critico')),
    estado_sincronizacion     INTEGER NOT NULL DEFAULT 0 CHECK(estado_sincronizacion IN (0,1)),
    creado_en_timestamp       INTEGER NOT NULL,
    sincronizado_en_timestamp INTEGER,
    FOREIGN KEY (id_usuario)    REFERENCES usuarios(id_usuario)   ON DELETE SET NULL,
    FOREIGN KEY (id_parcela)    REFERENCES parcelas(id_parcela)   ON DELETE RESTRICT,
    FOREIGN KEY (id_diagnostico) REFERENCES catalogo_diagnosticos(id_diagnostico) ON DELETE RESTRICT
);
INSERT INTO "inspecciones_campo" VALUES('insp-01',2,'PAR-LUR-02','Surco 04 - Sector Norte','inspections/trips_01.jpg',1,0.942,248,'Critico',1,1791072230586,1791075830586);
INSERT INTO "inspecciones_campo" VALUES('insp-02',3,'PAR-LUR-01','Hilera 12 - Surco Evaluado','inspections/water_02.jpg',2,0.887,262,'Medio',0,1791158630586,NULL);
INSERT INTO "inspecciones_campo" VALUES('insp-03',2,'PAR-LUR-03','Lote C - Hilera Central','inspections/sano_03.jpg',0,0.978,215,'Bajo',1,1791227030586,1791230630586);
INSERT INTO "inspecciones_campo" VALUES('insp-04',3,'PAR-LUR-01','Surco 08 - Extremo Sur','inspections/trips_04.jpg',1,0.835,255,'Medio',0,1791243230586,NULL);
INSERT INTO "inspecciones_campo" VALUES('insp-05',1,'PAR-LUR-02','Lote Fresa - Zona A','inspections/water_05.jpg',2,0.761,271,'Bajo',1,1790985830586,1790989430586);
CREATE TABLE inspections (
    id                 TEXT    PRIMARY KEY NOT NULL,
    plotId             TEXT    NOT NULL,
    subPlot            TEXT    NOT NULL,
    imagePath          TEXT    NOT NULL,
    diagnosisClass     INTEGER NOT NULL,
    confidence         REAL    NOT NULL,
    latencyMs          INTEGER NOT NULL,
    severityLevel      TEXT    NOT NULL,
    recommendationsJson TEXT    NOT NULL,
    syncStatus         INTEGER NOT NULL,
    createdAt          INTEGER NOT NULL
);
INSERT INTO "inspections" VALUES('insp-01','PAR-LUR-02','Surco 04 - Sector Norte','inspections/trips_01.jpg',1,0.942,248,'Critico','[{"title": "Ver Catalogo", "description": "Revisar recomendaciones del sistema"}]',1,1791072230586);
INSERT INTO "inspections" VALUES('insp-02','PAR-LUR-01','Hilera 12 - Surco Evaluado','inspections/water_02.jpg',2,0.887,262,'Medio','[{"title": "Ver Catalogo", "description": "Revisar recomendaciones del sistema"}]',0,1791158630586);
INSERT INTO "inspections" VALUES('insp-03','PAR-LUR-03','Lote C - Hilera Central','inspections/sano_03.jpg',0,0.978,215,'Bajo','[{"title": "Ver Catalogo", "description": "Revisar recomendaciones del sistema"}]',1,1791227030586);
INSERT INTO "inspections" VALUES('insp-04','PAR-LUR-01','Surco 08 - Extremo Sur','inspections/trips_04.jpg',1,0.835,255,'Medio','[{"title": "Ver Catalogo", "description": "Revisar recomendaciones del sistema"}]',0,1791243230586);
INSERT INTO "inspections" VALUES('insp-05','PAR-LUR-02','Lote Fresa - Zona A','inspections/water_05.jpg',2,0.761,271,'Bajo','[{"title": "Ver Catalogo", "description": "Revisar recomendaciones del sistema"}]',1,1790985830586);
CREATE TABLE parcelas (
    id_parcela         TEXT PRIMARY KEY,
    nombre_fundo       TEXT NOT NULL,
    sector             TEXT NOT NULL,
    cultivo_principal  TEXT NOT NULL,
    area_hectareas     REAL NOT NULL,
    coordenadas_gps    TEXT,
    estado             TEXT NOT NULL DEFAULT 'Activo',
    creado_en          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO "parcelas" VALUES('PAR-LUR-01','Fundo San Pedro','Valle Bajo - Lurin','Hortalizas (Aji / Paprika)',4.5,'-12.27415, -76.86822','Activo','2026-10-06 00:03:50');
INSERT INTO "parcelas" VALUES('PAR-LUR-02','Fundo Mamacona','Sector Litoral Lurin','Fresa / Frutales Menores',3.2,'-12.26189, -76.89240','Activo','2026-10-06 00:03:50');
INSERT INTO "parcelas" VALUES('PAR-LUR-03','Sector Manchay Bajo','Valle Medio Lurin','Palto / Citricos',6.0,'-12.18950, -76.83410','Activo','2026-10-06 00:03:50');
CREATE TABLE recomendaciones_catalogo (
    id_recomendacion  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_diagnostico    INTEGER NOT NULL,
    titulo            TEXT NOT NULL,
    descripcion       TEXT NOT NULL,
    tipo_accion       TEXT NOT NULL CHECK(tipo_accion IN ('Monitoreo','Biologico','Quimico','Riego','Cultural')),
    prioridad         TEXT NOT NULL CHECK(prioridad IN ('Baja','Media','Alta','Inmediata')),
    FOREIGN KEY (id_diagnostico) REFERENCES catalogo_diagnosticos(id_diagnostico) ON DELETE CASCADE
);
INSERT INTO "recomendaciones_catalogo" VALUES(1,0,'Monitoreo Preventivo Quincenal','Continuar fertirriego y monitoreo programado de trampas.','Monitoreo','Baja');
INSERT INTO "recomendaciones_catalogo" VALUES(2,0,'Registro Fotografico de Lote','Mantener registro fotografico de evolucion vegetativa.','Monitoreo','Baja');
INSERT INTO "recomendaciones_catalogo" VALUES(3,1,'Inspeccion de Enves y Trampas Azules','Revisar enves foliar, instalar trampas cromaticas azules a 30cm del dosel.','Monitoreo','Alta');
INSERT INTO "recomendaciones_catalogo" VALUES(4,1,'Aplicacion de Jabon Potasico / Neem','Pulverizacion focalizada al atardecer para deshidratar ninfas.','Biologico','Inmediata');
INSERT INTO "recomendaciones_catalogo" VALUES(5,1,'Control Biologico Chrysoperla / Orius','Liberacion de insectos depredadores autorizados en valles costeros.','Biologico','Media');
INSERT INTO "recomendaciones_catalogo" VALUES(6,2,'Ajuste de Turno y Frecuencia de Riego','Incrementar frecuencia de pulso de riego por goteo en 25%.','Riego','Inmediata');
INSERT INTO "recomendaciones_catalogo" VALUES(7,2,'Calibracion de Tensiometros','Verificar tension hidrica a 20-40cm de profundidad radicular.','Riego','Alta');
INSERT INTO "recomendaciones_catalogo" VALUES(8,2,'Inspeccion de Goteros y Presion de Red','Comprobar obstrucciones en lineas de goteo del surco.','Cultural','Media');
CREATE TABLE sincronizaciones_azure_log (
    id_sync             INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_sync          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad_registros  INTEGER  NOT NULL,
    endpoint_azure      TEXT     NOT NULL,
    http_status         INTEGER  NOT NULL,
    estado              TEXT     NOT NULL CHECK(estado IN ('PENDING','SUCCESS','FAILED')),
    mensaje_respuesta   TEXT
);
INSERT INTO "sincronizaciones_azure_log" VALUES(1,'2026-10-03 19:03:50',1,'https://agrovision-backend-app.azurewebsites.net/api/inspections/sync',200,'SUCCESS','{"success":true,"syncedIds":["insp-01"]}');
INSERT INTO "sincronizaciones_azure_log" VALUES(2,'2026-10-05 14:03:50',2,'https://agrovision-backend-app.azurewebsites.net/api/inspections/sync',200,'SUCCESS','{"success":true,"syncedIds":["insp-03","insp-05"]}');
CREATE TABLE usuarios (
    id_usuario       INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT    UNIQUE NOT NULL,
    password_hash    TEXT    NOT NULL,
    nombre_completo  TEXT    NOT NULL,
    rol              TEXT    NOT NULL CHECK(rol IN ('Administrador del Lote','Ing. Agronomo','Operario de Campo')),
    email            TEXT,
    activo           INTEGER NOT NULL DEFAULT 1,
    creado_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO "usuarios" VALUES(1,'admin','hash:admin123','Administrador General','Administrador del Lote','admin@agrovision-lurin.pe',1,'2026-10-06 00:03:50');
INSERT INTO "usuarios" VALUES(2,'agronomo','hash:agro2026','Ing. Carlos Mendoza R.','Ing. Agronomo','carlos.mendoza@agrovision-lurin.pe',1,'2026-10-06 00:03:50');
INSERT INTO "usuarios" VALUES(3,'operario','hash:operario1','Juan Quispe Morales','Operario de Campo','juan.quispe@agrovision-lurin.pe',1,'2026-10-06 00:03:50');
CREATE INDEX idx_insp_sync    ON inspecciones_campo(estado_sincronizacion);
CREATE INDEX idx_insp_fecha   ON inspecciones_campo(creado_en_timestamp DESC);
CREATE INDEX idx_insp_parcela ON inspecciones_campo(id_parcela);
CREATE INDEX index_inspections_syncStatus ON inspections(syncStatus);
CREATE INDEX index_inspections_createdAt  ON inspections(createdAt);
CREATE VIEW v_resumen_fitosanitario AS
SELECT
    p.nombre_fundo              AS fundo,
    p.sector                    AS sector_lurin,
    cd.nombre_comun             AS diagnostico,
    cd.tipo_afectacion,
    COUNT(i.id_inspeccion)      AS total_evaluaciones,
    ROUND(AVG(i.confianza_ia)*100,2) AS confianza_promedio_pct,
    ROUND(AVG(i.latencia_inferencia_ms),1) AS latencia_promedio_ms,
    SUM(CASE WHEN i.nivel_severidad = 'Critico' THEN 1 ELSE 0 END) AS casos_criticos,
    SUM(CASE WHEN i.estado_sincronizacion = 0 THEN 1 ELSE 0 END)   AS pendientes_sync
FROM inspecciones_campo i
JOIN parcelas              p  ON i.id_parcela    = p.id_parcela
JOIN catalogo_diagnosticos cd ON i.id_diagnostico = cd.id_diagnostico
GROUP BY p.nombre_fundo, p.sector, cd.nombre_comun, cd.tipo_afectacion;
DELETE FROM "sqlite_sequence";
INSERT INTO "sqlite_sequence" VALUES('usuarios',3);
INSERT INTO "sqlite_sequence" VALUES('recomendaciones_catalogo',8);
INSERT INTO "sqlite_sequence" VALUES('inspeccion_recomendaciones',7);
INSERT INTO "sqlite_sequence" VALUES('sincronizaciones_azure_log',2);
COMMIT;
