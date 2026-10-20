# BREM Automotriz

App Android para llevar las órdenes de trabajo (OT) del taller BREM Automotriz, en Maipú.
Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC.

Todo se guarda en el teléfono con Room, no usa internet ni servidor. Los datos de prueba son inventados.

## Integrantes

- Yuliana Calvucura
- Catalina Salinas

Sección: DESARROLLO DE APLICACIONES MOVILES_012D

## Qué hace la app (primera entrega)

- Inicio de sesión con validaciones y mensajes de error
- Pantalla de inicio según el rol (Administrador, Jefe de taller o Developer)
- Búsqueda de órdenes por patente, cliente o número de OT
- Creación de órdenes de trabajo con formulario validado
- Detalle de la OT: trabajos y repuestos, neto, IVA (19 %) y total
- Cambio de estado de la OT (Pendiente, En proceso, Listo, Entregado); solo el Administrador puede marcarla como Lista
- Historial de otras órdenes del mismo vehículo
- Fotos del vehículo con la cámara
- Compartir el resumen de la OT con otras apps
- Animaciones al navegar y al entrar al inicio

Queda para la próxima entrega: administración de usuarios, presupuestos, pagos y entrega, anulación de órdenes y exportar a PDF.

## Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin1@brem.cl | admin123 | Administrador |
| admin2@brem.cl | admin123 | Administrador |
| jefe@brem.cl | jefe123 | Jefe de taller |
| dev@brem.cl | dev1234 | Developer |

Patentes de ejemplo para crear órdenes: ABCD12, WXYZ34 y BCDF56.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, MVVM, Navigation Compose, Room, Git y GitHub, Trello.

## Cómo ejecutarla

1. Clonar el repositorio: `git clone https://github.com/CataSalinas/Proyecto-BremAutomotriz-.git`
2. Abrir la carpeta en Android Studio (File > Open).
3. Esperar que termine la sincronización de Gradle (la primera vez descarga dependencias).
4. Ejecutar en un emulador o en un celular Android 8.0 o superior (API 26).

## Estructura

- `data`: base de datos Room, entidades y acceso a datos
- `domain`: validaciones y reglas de negocio (por ejemplo el cálculo de montos)
- `model`: estado de cada pantalla
- `ui`: pantallas, navegación, tema y ViewModels

## Trabajo en equipo

Una rama por tarea, Pull Request revisado por la otra persona y merge a `develop`. Las tareas están en Trello.