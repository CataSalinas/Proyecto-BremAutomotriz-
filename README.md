# BREM Automotriz · App móvil de Órdenes de Trabajo

Aplicación Android nativa para gestionar las órdenes de trabajo (OT) del taller BREM Automotriz (Maipú).
Proyecto de la asignatura DSY1105 · Desarrollo de Aplicaciones Móviles · Duoc UC.

La app guarda toda la información en el teléfono (Room/SQLite) y funciona sin internet ni servidor.
Todos los datos de prueba son ficticios.

## Integrantes

- Yuliana Calvucura
- Catalina Salinas

## Funcionalidades

- Login local con roles (Administrador y Jefe de taller)
- Búsqueda por patente, cliente o N° de OT
- Apertura de OT con formularios validados
- Cálculo automático de neto, IVA (19 %) y total
- Presupuestos y conversión a OT
- Registro de pagos y entrega
- Evidencia fotográfica con la cámara (máximo 15 archivos por OT)
- Detalle de la OT en PDF para compartir

Estado: en desarrollo.

## Tecnologías

Kotlin · Android Studio · Jetpack Compose · Material Design 3 · MVVM · Navigation Compose · Room · Git/GitHub · Trello

## Cómo ejecutar

1. Clonar el repositorio: `git clone https://github.com/CataSalinas/Proyecto-BremAutomotriz-.git`
2. Abrir la carpeta en Android Studio (File → Open).
3. Esperar a que termine la sincronización de Gradle.
4. Ejecutar la app en un emulador o en un celular Android (API 26 o superior).

## Estructura

- `data`: base de datos Room, entidades y repositorios
- `domain`: reglas de negocio y cálculos
- `ui`: pantallas, navegación y tema

## Flujo de trabajo

Una rama por tarea (`feat/...`, `fix/...`, `docs/...`), Pull Request revisado por la otra persona y merge a `main`.
Las tareas se gestionan en Trello.