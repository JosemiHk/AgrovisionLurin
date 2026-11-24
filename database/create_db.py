"""
Generador de Base de Datos SQLite - AgroVision Lurín
Proyecto de Tesis: Diagnóstico Fitosanitario Móvil Edge-AI
Autor: Script generado por Antigravity IDE
"""
import sqlite3, os, time, json, shutil

DB_PATH    = r"e:\TESIS\AGROVISION-LURIN-1\database\agrovision_lurin.sqlite"
SQL_PATH   = r"e:\TESIS\AGROVISION-LURIN-1\database\agrovision_lurin_schema.sql"
DB_COPY    = r"e:\TESIS\AGROVISION-LURIN-1\agrovision_database.sqlite"

for p in (DB_PATH, DB_COPY, SQL_PATH):
    if os.path.exists(p):
        os.remove(p)

conn   = sqlite3.connect(DB_PATH)
cursor = conn.cursor()
cursor.execute("PRAGMA foreign_keys = ON;")
cursor.execute("PRAGMA journal_mode = WAL;")

# ─────────────────────────────────────────────
# DDL: TABLAS
# ─────────────────────────────────────────────

cursor.execute("""
CREATE TABLE usuarios (
    id_usuario       INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT    UNIQUE NOT NULL,
    password_hash    TEXT    NOT NULL,
    nombre_completo  TEXT    NOT NULL,
    rol              TEXT    NOT NULL CHECK(rol IN ('Administrador del Lote','Ing. Agronomo','Operario de Campo')),
    email            TEXT,
    activo           INTEGER NOT NULL DEFAULT 1,
    creado_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);""")

cursor.execute("""
CREATE TABLE parcelas (
    id_parcela         TEXT PRIMARY KEY,
    nombre_fundo       TEXT NOT NULL,
    sector             TEXT NOT NULL,
    cultivo_principal  TEXT NOT NULL,
    area_hectareas     REAL NOT NULL,
    coordenadas_gps    TEXT,
    estado             TEXT NOT NULL DEFAULT 'Activo',
    creado_en          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);""")

cursor.execute("""
CREATE TABLE catalogo_diagnosticos (
    id_diagnostico    INTEGER PRIMARY KEY,
    codigo            TEXT UNIQUE NOT NULL,
    nombre_comun      TEXT NOT NULL,
    nombre_cientifico TEXT,
    tipo_afectacion   TEXT NOT NULL CHECK(tipo_afectacion IN ('Plaga entomologica','Estres Abiotico','Fisiologico Optimo')),
    descripcion       TEXT NOT NULL,
    umbral_alerta     REAL NOT NULL DEFAULT 0.75
);""")

cursor.execute("""
CREATE TABLE recomendaciones_catalogo (
    id_recomendacion  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_diagnostico    INTEGER NOT NULL,
    titulo            TEXT NOT NULL,
    descripcion       TEXT NOT NULL,
    tipo_accion       TEXT NOT NULL CHECK(tipo_accion IN ('Monitoreo','Biologico','Quimico','Riego','Cultural')),
    prioridad         TEXT NOT NULL CHECK(prioridad IN ('Baja','Media','Alta','Inmediata')),
    FOREIGN KEY (id_diagnostico) REFERENCES catalogo_diagnosticos(id_diagnostico) ON DELETE CASCADE
);""")

cursor.execute("""
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
);""")

cursor.execute("CREATE INDEX idx_insp_sync    ON inspecciones_campo(estado_sincronizacion);")
cursor.execute("CREATE INDEX idx_insp_fecha   ON inspecciones_campo(creado_en_timestamp DESC);")
cursor.execute("CREATE INDEX idx_insp_parcela ON inspecciones_campo(id_parcela);")

cursor.execute("""
CREATE TABLE inspeccion_recomendaciones (
    id_detalle        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inspeccion     TEXT    NOT NULL,
    id_recomendacion  INTEGER NOT NULL,
    aplicada          INTEGER NOT NULL DEFAULT 0,
    fecha_aplicacion  DATETIME,
    observaciones     TEXT,
    FOREIGN KEY (id_inspeccion)    REFERENCES inspecciones_campo(id_inspeccion)  ON DELETE CASCADE,
    FOREIGN KEY (id_recomendacion) REFERENCES recomendaciones_catalogo(id_recomendacion) ON DELETE RESTRICT
);""")

cursor.execute("""
CREATE TABLE sincronizaciones_azure_log (
    id_sync             INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_sync          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad_registros  INTEGER  NOT NULL,
    endpoint_azure      TEXT     NOT NULL,
    http_status         INTEGER  NOT NULL,
    estado              TEXT     NOT NULL CHECK(estado IN ('PENDING','SUCCESS','FAILED')),
    mensaje_respuesta   TEXT
);""")

# ── Tabla IDÉNTICA a la Room Entity de Android ──────────────────────────────
cursor.execute("""
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
);""")
cursor.execute("CREATE INDEX index_inspections_syncStatus ON inspections(syncStatus);")
cursor.execute("CREATE INDEX index_inspections_createdAt  ON inspections(createdAt);")

# ── Vista analítica para el Dashboard ────────────────────────────────────────
cursor.execute("""
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
""")

# ─────────────────────────────────────────────
# DML: DATOS SEMILLA REALISTAS
# ─────────────────────────────────────────────

# 1. Usuarios (demo Sprint 1 — LoginViewModel.kt)
cursor.executemany(
    "INSERT INTO usuarios(username,password_hash,nombre_completo,rol,email) VALUES(?,?,?,?,?)",
    [
        ("admin",    "hash:admin123",  "Administrador General",    "Administrador del Lote", "admin@agrovision-lurin.pe"),
        ("agronomo", "hash:agro2026",  "Ing. Carlos Mendoza R.",  "Ing. Agronomo",           "carlos.mendoza@agrovision-lurin.pe"),
        ("operario", "hash:operario1", "Juan Quispe Morales",     "Operario de Campo",       "juan.quispe@agrovision-lurin.pe"),
    ]
)

# 2. Parcelas del Valle de Lurín
cursor.executemany(
    "INSERT INTO parcelas(id_parcela,nombre_fundo,sector,cultivo_principal,area_hectareas,coordenadas_gps) VALUES(?,?,?,?,?,?)",
    [
        ("PAR-LUR-01","Fundo San Pedro",      "Valle Bajo - Lurin",    "Hortalizas (Aji / Paprika)",  4.5, "-12.27415, -76.86822"),
        ("PAR-LUR-02","Fundo Mamacona",        "Sector Litoral Lurin",  "Fresa / Frutales Menores",    3.2, "-12.26189, -76.89240"),
        ("PAR-LUR-03","Sector Manchay Bajo",   "Valle Medio Lurin",     "Palto / Citricos",            6.0, "-12.18950, -76.83410"),
    ]
)

# 3. Catálogo de diagnósticos (clases del modelo TFLite)
cursor.executemany(
    "INSERT INTO catalogo_diagnosticos(id_diagnostico,codigo,nombre_comun,nombre_cientifico,tipo_afectacion,descripcion,umbral_alerta) VALUES(?,?,?,?,?,?,?)",
    [
        (0,"SANO",         "Cultivo Sano",    "Hojas asintomaticas",         "Fisiologico Optimo",   "Foliolo vigoroso con coloracion verde uniforme, sin lesiones fitopatogenas.", 0.80),
        (1,"TRIPS",        "Trips tabaci",    "Thrips tabaci Lindeman",       "Plaga entomologica",   "Genera plateado foliar, clorosis puntiforme y deformacion de brotes en Lurin.", 0.70),
        (2,"ESTRES_HIDRICO","Estres Hidrico", "Deficit de humedad/turgencia", "Estres Abiotico",      "Perdida de turgencia celular foliar por desbalance evapotranspirativo.", 0.75),
    ]
)

# 4. Recomendaciones agronómicas por diagnóstico
cursor.executemany(
    "INSERT INTO recomendaciones_catalogo(id_diagnostico,titulo,descripcion,tipo_accion,prioridad) VALUES(?,?,?,?,?)",
    [
        # --- Sano ---
        (0,"Monitoreo Preventivo Quincenal",    "Continuar fertirriego y monitoreo programado de trampas.",   "Monitoreo","Baja"),
        (0,"Registro Fotografico de Lote",      "Mantener registro fotografico de evolucion vegetativa.",     "Monitoreo","Baja"),
        # --- Trips ---
        (1,"Inspeccion de Enves y Trampas Azules",  "Revisar enves foliar, instalar trampas cromaticas azules a 30cm del dosel.", "Monitoreo","Alta"),
        (1,"Aplicacion de Jabon Potasico / Neem",   "Pulverizacion focalizada al atardecer para deshidratar ninfas.",            "Biologico","Inmediata"),
        (1,"Control Biologico Chrysoperla / Orius",  "Liberacion de insectos depredadores autorizados en valles costeros.",      "Biologico","Media"),
        # --- Estres Hidrico ---
        (2,"Ajuste de Turno y Frecuencia de Riego", "Incrementar frecuencia de pulso de riego por goteo en 25%.",               "Riego",    "Inmediata"),
        (2,"Calibracion de Tensiometros",           "Verificar tension hidrica a 20-40cm de profundidad radicular.",            "Riego",    "Alta"),
        (2,"Inspeccion de Goteros y Presion de Red","Comprobar obstrucciones en lineas de goteo del surco.",                    "Cultural", "Media"),
    ]
)

# 5. Inspecciones campo
now_ms  = int(time.time() * 1000)
day_ms  = 86_400_000
hour_ms = 3_600_000

inspections = [
    dict(id="insp-01", uid=2, plot="PAR-LUR-02", sub="Surco 04 - Sector Norte",    img="inspections/trips_01.jpg",   diag=1, conf=0.942, lat=248, sev="Critico", sync=1, ts=now_ms-2*day_ms,  synced_ts=now_ms-2*day_ms+hour_ms),
    dict(id="insp-02", uid=3, plot="PAR-LUR-01", sub="Hilera 12 - Surco Evaluado", img="inspections/water_02.jpg",   diag=2, conf=0.887, lat=262, sev="Medio",   sync=0, ts=now_ms-day_ms,    synced_ts=None),
    dict(id="insp-03", uid=2, plot="PAR-LUR-03", sub="Lote C - Hilera Central",    img="inspections/sano_03.jpg",    diag=0, conf=0.978, lat=215, sev="Bajo",    sync=1, ts=now_ms-5*hour_ms, synced_ts=now_ms-4*hour_ms),
    dict(id="insp-04", uid=3, plot="PAR-LUR-01", sub="Surco 08 - Extremo Sur",     img="inspections/trips_04.jpg",   diag=1, conf=0.835, lat=255, sev="Medio",   sync=0, ts=now_ms-30*60*1000, synced_ts=None),
    dict(id="insp-05", uid=1, plot="PAR-LUR-02", sub="Lote Fresa - Zona A",        img="inspections/water_05.jpg",   diag=2, conf=0.761, lat=271, sev="Bajo",    sync=1, ts=now_ms-3*day_ms,  synced_ts=now_ms-3*day_ms+hour_ms),
]

for r in inspections:
    # Tabla normalizada (relacional)
    cursor.execute("""
        INSERT INTO inspecciones_campo
            (id_inspeccion,id_usuario,id_parcela,sub_parcela_surco,ruta_imagen_local,
             id_diagnostico,confianza_ia,latencia_inferencia_ms,nivel_severidad,
             estado_sincronizacion,creado_en_timestamp,sincronizado_en_timestamp)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?)""",
        (r["id"],r["uid"],r["plot"],r["sub"],r["img"],
         r["diag"],r["conf"],r["lat"],r["sev"],
         r["sync"],r["ts"],r["synced_ts"])
    )
    # Tabla Room Android (inspections — idéntica a InspectionEntity.kt)
    recs_json = json.dumps([{"title":"Ver Catalogo","description":"Revisar recomendaciones del sistema"}])
    cursor.execute("""
        INSERT INTO inspections
            (id,plotId,subPlot,imagePath,diagnosisClass,confidence,latencyMs,
             severityLevel,recommendationsJson,syncStatus,createdAt)
        VALUES(?,?,?,?,?,?,?,?,?,?,?)""",
        (r["id"],r["plot"],r["sub"],r["img"],
         r["diag"],r["conf"],r["lat"],r["sev"],
         recs_json,r["sync"],r["ts"])
    )

# 6. Detalles recomendaciones N:M
detalles = [
    ("insp-01", 3, 1, "Trampas colocadas en surco evaluado"),
    ("insp-01", 4, 0, "Pendiente aplicacion vespertina"),
    ("insp-02", 6, 0, "Reportado a encargado de fertirriego"),
    ("insp-03", 1, 1, "Sin novedad fitopatologica"),
    ("insp-04", 3, 0, "Pendiente revision"),
    ("insp-05", 6, 1, "Ajuste de riego realizado"),
    ("insp-05", 7, 1, "Tensiometros calibrados"),
]
cursor.executemany(
    "INSERT INTO inspeccion_recomendaciones(id_inspeccion,id_recomendacion,aplicada,observaciones) VALUES(?,?,?,?)",
    detalles
)

# 7. Log de sincronizaciones con Azure
cursor.executemany(
    "INSERT INTO sincronizaciones_azure_log(fecha_sync,cantidad_registros,endpoint_azure,http_status,estado,mensaje_respuesta) VALUES(?,?,?,?,?,?)",
    [
        (time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()-2*86400)),
         1,"https://agrovision-backend-app.azurewebsites.net/api/inspections/sync",200,"SUCCESS",'{"success":true,"syncedIds":["insp-01"]}'),
        (time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()-5*3600)),
         2,"https://agrovision-backend-app.azurewebsites.net/api/inspections/sync",200,"SUCCESS",'{"success":true,"syncedIds":["insp-03","insp-05"]}'),
    ]
)

conn.commit()

# ── Exportar dump SQL ─────────────────────────────────────────────────────────
header = """\
-- =============================================================================
-- SCRIPT DDL + DML  •  Base de Datos AgroVision Lurín
-- PROYECTO DE TESIS: Diagnóstico Fitosanitario Móvil Edge-AI en el Valle Lurín
-- Motor compatible : SQLite 3 / Android Room / DB Browser for SQLite
-- Generado el      : {date}
-- =============================================================================

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

""".format(date=time.strftime('%Y-%m-%d %H:%M'))

with open(SQL_PATH, 'w', encoding='utf-8') as f:
    f.write(header)
    for line in conn.iterdump():
        f.write(f"{line}\n")

conn.close()
shutil.copyfile(DB_PATH, DB_COPY)

print("✓ Base de datos SQLite generada:")
print(f"  → {DB_PATH}")
print(f"  → {DB_COPY}  (copia de conveniencia)")
print(f"✓ Script SQL exportado:")
print(f"  → {SQL_PATH}")
