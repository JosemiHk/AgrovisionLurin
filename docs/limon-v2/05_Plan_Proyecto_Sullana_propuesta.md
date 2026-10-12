Plan de gestión del proyecto

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Objetivo y responsabilidades

AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.

José Miguel administra la ejecución y revisión de cambios. No se presupone capacidad adicional de otro integrante. El docente revisa alcance y criterios; el experto valida etiquetas; agricultores participan si existe autorización y consentimiento.



Control de trabajo

Capacidad: lunes a jueves 1,5 h por día, viernes 2 h, sábado 4 h y domingo hasta 4 h. Comprometer 12 h semanales y reservar 4 h. Cada tarea incluye revisión del trabajo de agentes y pruebas. No asignar dos tareas simultáneas al mismo responsable.

El cronograma calcula CPM por precedencias fin a inicio y presenta calendario con disponibilidad. La ruta lógica y el calendario con recursos limitados se identifican separadamente. Si una tarea consume reserva, recortar alcance postergable antes que validez o pruebas. Congelar funciones el 8 de noviembre.



Gobierno de cambios

La acta registra decisión, requisitos afectados, costo y aceptación académica pendiente. Cada sprint termina con incremento demostrado y RTM actualizado. No marcar aprobación docente ni ejecución de campo sin evidencia. Conservar la línea base histórica de agosto y septiembre sin reescribir sus fechas.

Sprint | Fechas | Horas | Foco | Entregable

S1 | 12–18 oct. | 12 | Alcance y honestidad | Acta y SRS; fallback y sync falso retirados

S2 | 19–25 oct. | 12 | Room login y CRUD | Migración, usuarios y parcelas; protocolo de datos

S3 | 26 oct.–1 nov. | 12 | Datos y modelo real | Manifiesto agrupado, baseline y contrato FLOAT32

S4 | 2–8 nov. | 12 | Integración y RC | APK offline, historial por ID y CSV

S5 | 9–15 nov. | 12 | Piloto y campo | Etiquetas expertas y fichas de tareas sin inventar resultados

S6 | 16–22 nov. | 12 | Evaluación y resultados | Métricas reales, defectos y discusión

S7 | 23–29 nov. | 12 | Cierre y sustentación | Documentos coherentes, APK y ensayo

Riesgo | Señal | Respuesta

Referencia experta ausente | Etiquetas sin confirmación al 25/10 | Gestionar técnico externo; no reemplazar por IA

Integración atrasada | Sin modelo Android al 01/11 | Usar reserva; mantener baseline único

Métrica insuficiente | Test por debajo de meta | Informar resultados; no ajustar sobre test

Campo sin etapa inicial | No hay casos iniciales confirmados | Limitar título y conclusión

CRUD aumenta carga | Reserva de S2 consumida | Reducir adornos y mantener integridad/login