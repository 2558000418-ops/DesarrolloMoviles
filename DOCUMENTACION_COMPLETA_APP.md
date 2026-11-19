# Documento Maestro de la Aplicación: ToDoList (Edición Completa y Avanzada)

Este documento describe de principio a fin la arquitectura, módulos, base de datos, sistema de preferencias con **Android DataStore**, funciones de productividad, seguridad, idiomas y accesibilidad que integran la aplicación **ToDoList** (`com.example.todolist`).

---

## 1. Ficha Técnica y Configuración del Proyecto

* **Paquete Principal:** `com.example.todolist`
* **Lenguaje y Entorno:** Kotlin (`JVM Target 11`) en Android Studio.
* **Compatibilidad de Sistema Operativo:** `minSdk = 24` (Android 7.0 Nougat) hasta `targetSdk = 37` (compatible con Android 11 API 30, Android 12, 13, 14 y 15+).
* **UI Framework:** 100% declarativo con **Jetpack Compose** (`compose-bom:2026.09.00`) y **Material Design 3**.
* **Arquitectura de Persistencia Híbrida (Doble Motor):**
  1. **Android Jetpack DataStore (`Preferences DataStore`)**: Persistencia asíncrona, transaccional y reactiva (`Flow`) para la sesión iniciada, tema visual, modo claro/oscuro, notificaciones, idioma (24 idiomas), racha de productividad y todas las opciones de accesibilidad y personalización del entorno.
  2. **SQLite Relacional Multiusuario (`SQLiteOpenHelper` - Versión 4)**: Almacenamiento estructurado y aislado por usuario (`WHERE user_id = ?`) para cuentas (`users`) y tareas (`tasks`) con soporte para prioridades, recurrencia, subtareas, fijado, papelera de reciclaje y tiempo Pomodoro.
* **Permisos Declarados (`AndroidManifest.xml`):**
  * `android.permission.POST_NOTIFICATIONS` (Notificaciones locales nativas en la barra de estado).
  * `android.permission.VIBRATE` (Respuesta háptica táctil y vibración de recordatorios).

---

## 2. Estructura Modular de Archivos (17 Archivos en 6 Paquetes)

1. **`app/build.gradle.kts`**: Configuración de `minSdk = 24`, Compose y `androidx.datastore:datastore-preferences:1.0.0`.
2. **`MainActivity.kt`**: Controlador raíz (`TodoAppRoot`), estado optimista instantáneo (`0ms` de latencia), proveedor de densidad tipográfica (`LocalDensity`), soporte `RTL` (`LocalLayoutDirection`) y restauración automática de sesión.
3. **Paquete `data/`**:
   * **`UserPreferencesRepository.kt`**: Repositorio de **Preferences DataStore** (`todo_user_preferences.preferences_pb`) con lectura por `Flow` y escritura transaccional `edit {}`.
   * **`TodoDatabaseHelper.kt`**: Base de datos **SQLite v4** (`todo_multiuser_v3.db`) con migración segura e idempotente (`ensureSchemaColumns`), tablas `users` y `tasks`, CRUD completo, papelera y reprogramación de vencidas.
4. **Paquete `model/`**:
   * **`AppModels.kt`**: Define `UserAccount`, `TodoTask`, `SubtaskItem`, `TaskPriority`, `TaskRecurrence`, `AppLanguage` (24 idiomas), `ColorblindMode`, `FontScaleMode`, `BorderStyleMode`, `CornerStyleMode`, `LayoutDensityMode` y `QuickTaskTemplates`.
5. **Paquete `ui/theme/`**:
   * **`AppTheme.kt`**: Define `ProvideDynamicAppTheme`, `appTextFieldColors`, las 10 paletas de color (5 claras con Blanco Puro y 5 oscuras con Negro AMOLED), los 7 colores de tarea con símbolos geométricos y las matrices de transformación para daltonismo.
6. **Paquete `util/`**:
   * **`AppStrings.kt`**: Diccionario reactivo completo para los **24 idiomas** soportados.
   * **`DateTimeAndImageUtils.kt`**: Formateo de fechas localizado en 24 idiomas, cálculo de siguiente fecha recurrente, compresión de foto de perfil `300x300 px` y serialización de respaldos.
   * **`NotificationHelper.kt`**: Creación de `NotificationChannel` y envío de notificaciones locales de tareas y de prueba.
   * **`VoiceAndShareHelper.kt`**: Motor de lectura por voz (`TextToSpeech`), vibración háptica (`Vibrator`), compartir por WhatsApp/Correo (`Intent.ACTION_SEND`) y cifrado `SHA-256`.
7. **Paquete `ui/components/`**:
   * **`CommonComponents.kt`**: `UserAvatarComponent`, `TaskColorSelector`, `ThemePreviewCard` y `TaskCardComponent` (con insignias de prioridad, recurrencia, Pomodoro, barra de subtareas, fijado y bordes adaptables).
8. **Paquete `ui/screens/`**:
   * **`AuthAndUserPickerScreen.kt`**: Inicio de sesión, selector de sesiones guardadas en 1 toque y registro de usuarios con cifrado SHA-256.
   * **`MainDashboardContainer.kt`**: Contenedor con barra inferior de 4 pestañas, FAB adaptable a Modo Zurdo, modal de perfil rápido y modal de nueva tarea con plantillas rápidas y validación obligatoria de título.
   * **`HomeSummaryTab.kt`**: Pestaña 1 (*Resumen*): lectura por voz TTS, detector de tareas vencidas, Modo Enfoque Zen, racha y analítica por categoría, tira semanal, anillo de progreso y lista de tareas.
   * **`AllTasksSearchTab.kt`**: Pestaña 2 (*Mis Tareas*): buscador, filtros por categoría y prioridad, ordenamiento múltiple, **Matriz de Eisenhower 2x2** y **Papelera de Reciclaje**.
   * **`CalendarTasksTab.kt`**: Pestaña 3 (*Calendario*): cuadrícula mensual estilo **Google Calendar** con barras de color y puntos bajo cada día, agenda del día, compartir agenda y asignación rápida de fechas.
   * **`SettingsAndPreferencesTab.kt`**: Pestaña 4 (*Configuración*): Administrador de Preferencias DataStore, selector compacto de 24 idiomas, personalización de bordes/esquinas/densidad, 11 herramientas de accesibilidad, 10 temas y respaldo al portapapeles.
   * **`TaskDetailView.kt`**: Estudio completo de edición de tarea: lectura por voz TTS, compartir tarea por WhatsApp, temporizador Pomodoro integrado, checklist de subtareas y edición de todos los campos.

---

## 3. Catálogo Completo de Funcionalidades

### 3.1. Sistema Multiusuario, Sesión Persistente y Seguridad
* **Sesión Persistente con DataStore**: Conserva el usuario activo (`logged_in_user_id`) incluso si se cierra la aplicación desde multitarea o se reinicia el teléfono.
* **Cifrado Criptográfico SHA-256**: Las contraseñas de usuario se transforman mediante hash `SHA-256` antes de almacenarse en SQLite, manteniendo compatibilidad con el usuario semilla (`Erick Rosales` / `erick@utna.edu.mx` / `1234`).
* **Fotos de Perfil Personalizadas**: Selección desde la galería del dispositivo (`ActivityResultContracts.GetContent()`), redimensionada a `300x300 px` en almacenamiento interno privado, con opción para restaurar el avatar con inicial.
* **Modo Privacidad en Vista Pública**: Permite ocultar/enmascarar los títulos de las tareas en el Resumen (`🔒 Tarea Privada #ID`) cuando muestras la pantalla a otras personas.

### 3.2. Gestión Avanzada de Tareas y Productividad
* **Validación Obligatoria de Datos**: Al crear o editar una tarea, si el usuario no escribe el título, el campo se marca en rojo (`isError = true`), muestra una alerta visual y lanza un aviso emergente (`Toast`).
* **Plantillas Rápidas de 1 Toque**: Chips en el modal de creación (*`📚 Entrega UTNA`*, *`⏱️ Sesión Pomodoro`*, *`🚀 Sprint Proyecto`*, *`💪 Rutina Salud`*) que rellenan automáticamente título, notas, categoría, prioridad y 3 subtareas preconfiguradas.
* **4 Niveles de Prioridad Visual**: *Baja (`▽`)*, *Media (`◇`)*, *Alta (`▲`)* y *Crítica (`⚡`)* con resaltado en Rojo Carmín (`#BE123C`).
* **Sistema de Subtareas (Checklists Jerárquicos)**: Permite agregar, marcar y eliminar pasos individuales dentro de cada tarea, mostrando una barra de progreso (`✓ X/Y subtareas`) en la tarjeta y opción de auto-completar la tarea principal al terminar todos los pasos.
* **Tareas Recurrentes (Hábitos y Rutinas)**: Modos *No repetir*, *Diariamente*, *Lunes a Viernes*, *Semanalmente* y *Mensualmente*. Al completar una tarea recurrente, genera automáticamente la siguiente fecha en SQLite.
* **Temporizador Pomodoro / Modo Enfoque Integrado**: Cronómetro interactivo dentro de cada tarea (*Pomodoro 25m*, *Descanso 5m*, *Enfoque 45m*) que suma automáticamente los minutos trabajados al registro `focus_minutes` de la tarea.
* **Matriz de Eisenhower (Vista 2x2)**: Clasifica las tareas pendientes en 4 cuadrantes (*1. Hacer Ahora*, *2. Planificar*, *3. Resolver Rápido*, *4. Algún Día*).
* **Detector de Tareas Vencidas (*Overdue*)**: Detecta tareas de días pasados sin terminar y permite moverlas todas a la fecha de **Hoy** con un solo toque.
* **Fijado (`Pin`), Reordenamiento Manual (`↑` / `↓`) y Duplicación**: Fija tareas importantes arriba, cambia su orden manualmente o duplica cualquier actividad en un toque.
* **Papelera de Reciclaje (*Soft Delete*)**: Las tareas borradas se envían a la Papelera (`is_deleted = 1`), desde donde pueden restaurarse o eliminarse definitivamente.

### 3.3. Calendario Mensual Estilo Google Calendar
* **Etiquetas de Color y Puntos Bajo Cada Día**: Debajo de cada número de día (ej. día **13**), dibuja una píldora con el color seleccionado y el título de la tarea, más los puntos de colores de todas las tareas pendientes de esa fecha (e indicador `+N`).
* **Agenda Interactiva del Día**: Al tocar cualquier fecha, muestra abajo sus tareas pendientes y completadas, selector rápido de color, botón `+ Nueva tarea` preconfigurado en ese día y botón para asignar fecha a tareas antiguas sin programar.

### 3.4. Gamificación, Estadísticas, Notificaciones y Compartir
* **Racha de Productividad (`🔥`) y Nivel XP**: Calcula los puntos de experiencia (`XP`) según tareas completadas y minutos Pomodoro, asignando rangos desde *Nivel 1: Iniciador Productivo 🌱* hasta *Nivel 4: Maestro del Tiempo 🏆*, y guarda la racha en DataStore.
* **Panel Desplegable de Analítica por Categoría**: Gráficas de barras proporcionales que muestran el porcentaje y avance en *Universidad*, *Proyecto*, *Personal*, *Trabajo*, *Salud* y *Urgente*.
* **Notificaciones Locales Nativas**: Canal `NotificationChannel` para recordatorios de tareas y botón de prueba en Configuración.
* **Compartir por WhatsApp / Correo (`Intent.ACTION_SEND`)**: Envía fichas formateadas de tareas individuales (con subtareas `✅`/`⬜`) o la agenda completa del día hacia WhatsApp, Telegram o correo.
* **Exportar e Importar Respaldo Rápido**: Copia y restaura todas las tareas del usuario mediante el portapapeles (`ClipboardManager`).

### 3.5. Personalización de Entorno, 10 Temas y 24 Idiomas (DataStore)
* **Actualización Instantánea (`0ms`)**: Todo cambio de color, tema, modo oscuro, borde o idioma se aplica en el mismo milisegundo en memoria mientras DataStore y SQLite persisten en segundo plano (`Dispatchers.IO`).
* **10 Paletas de Color**:
  * **5 Claras con Blanco Puro (`#FFFFFF`) y Marcos Oscuros**: *Blanco + Morado Oscuro*, *Blanco + Azul Marino*, *Blanco + Esmeralda Oscuro*, *Blanco + Terracota Oscuro*, *Blanco + Carmín Oscuro*.
  * **5 Oscuras con Negro Profundo AMOLED (`#050507`)**: *Negro + Carmín Rojo*, *Negro + Morado Profundo*, *Negro + Azul Noche*, *Negro + Verde Bosque*, *Negro + Bronce Dorado*.
* **Personalización del Entorno Visual**:
  * **Estilo e Intensidad de Bordes (`BorderStyleMode`)**: *Nulo (`0 dp` sin bordes)*, *Suave (`0.8 dp` discreto)*, *Medio (`1.4 dp` remarcado)* y *Fuerte (`2.4 dp` alto contraste)*.
  * **Geometría de Tarjetas (`CornerStyleMode`)**: *Recto Minimalista (`6 dp`)*, *Redondeado Clásico (`18 dp`)* y *Súper Curvo / Burbuja (`28 dp`)*.
  * **Densidad Visual (`LayoutDensityMode`)**: *Compacto*, *Cómodo* y *Amplio / Aireado*.
* **Selector Compacto de 24 Idiomas con Soporte RTL**:
  * Incluye barra compacta con lista desplegable de `200 dp` y buscador para: Español (🇲🇽), English (🇺🇸), Français (🇫🇷), Português (🇧🇷), Deutsch (🇩🇪), Italiano (🇮🇹), 日本語 (🇯🇵), 简体中文 (🇨🇳), 繁體中文 (🇹🇼), 한국어 (🇰🇷), Русский (🇷🇺), العربية (🇸🇦 **RTL**), हिन्दी (🇮🇳), Türkçe (🇹🇷), Nederlands (🇳🇱), Polski (🇵🇱), Svenska (🇸🇪), Ελληνικά (🇬🇷), עברית (🇮🇱 **RTL**), Tiếng Việt (🇻🇳), ไทย (🇹🇭), Bahasa Indonesia (🇮🇩), Українська (🇺🇦) y Català (🇪🇸).

### 3.6. Centro de Accesibilidad Visual, Cognitiva, Auditiva y Motora (11 Herramientas)
1. **5 Modos de Color para Daltónicos**: *Estándar*, *Protanopía (Rojo-Verde)*, *Deuteranopía (Verde-Rojo)*, *Tritanopía (Azul-Amarillo)* y *Acromatopsia (Monocromático)*.
2. **Doble Codificación Visual**: Símbolos geométricos únicos (`●`, `▲`, `■`, `◆`, `★`, `⬢`, `◉`) y nombre explícito del color.
3. **5 Niveles de Escalado Tipográfico**: *Compacto (90%)*, *Normal (100%)*, *Grande (115%)*, *Extra Grande (130%)* y *Máximo Baja Visión (145%)*.
4. **Contraste Máximo de Textos y Marcos**: Eleva la nitidez de textos secundarios y bordes al 100%.
5. **Modo Lectura Fácil / Dislexia**: Amplía el espaciado entre letras (`letterSpacing`) y la altura de línea.
6. **Modo Zurdo / Una Sola Mano**: Invierte el botón flotante (`+`) al lado izquierdo de la pantalla.
7. **Reducción de Movimiento (Fotosensibilidad)**: Desactiva animaciones de giro y barrido.
8. **Objetivos Táctiles Ampliados (`52dp+`)**: Agranda botones y checkboxes para accesibilidad motora.
9. **Modo Enfoque Zen / TDAH**: Oculta elementos distractores y muestra únicamente las 3 tareas más urgentes.
10. **Lectura por Voz Nativa (`Text-to-Speech`)**: Lee en voz alta el resumen diario y los pasos de cualquier tarea.
11. **Respuesta Háptica Táctil**: Confirmación mediante vibración suave al completar actividades.
