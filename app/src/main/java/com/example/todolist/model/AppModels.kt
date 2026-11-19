package com.example.todolist.model

enum class AppScreen {
    LOGIN,
    MAIN_DASHBOARD,
    TASK_DETAIL
}

/**
 * Planes de Monetización Ética (Suscripción Pro Mensual/Anual + Pase Recompensado de 24h).
 */
enum class SubscriptionPlan(val code: String, val labelEs: String, val priceTag: String) {
    FREE("FREE", "Plan Gratuito", "$0 MXN"),
    MONTHLY_PRO("MONTHLY_PRO", "Pro Mensual", "$39 MXN / mes"),
    ANNUAL_PRO("ANNUAL_PRO", "Pro Anual", "$299 MXN / año");

    companion object {
        fun fromCode(code: String): SubscriptionPlan =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FREE
    }
}

data class ProStatus(
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val rewardedUnlockUntilMillis: Long = 0L
) {
    val hasProAccess: Boolean
        get() = plan != SubscriptionPlan.FREE || System.currentTimeMillis() < rewardedUnlockUntilMillis

    val isRewardedActive: Boolean
        get() = plan == SubscriptionPlan.FREE && System.currentTimeMillis() < rewardedUnlockUntilMillis
}

/**
 * Formatos del Panel Desplegable "Mostrar" (Todoist / TickTick superado con Matriz 2x2).
 */
enum class TaskViewFormat(val code: String, val labelEs: String) {
    LIST("LIST", "Lista"),
    BOARD("BOARD", "Panel"),
    CALENDAR("CALENDAR", "Calendario"),
    MATRIX("MATRIX", "Matriz");

    companion object {
        fun fromCode(code: String): TaskViewFormat =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: LIST
    }
}

/**
 * Opciones de Agrupación para el Panel "Mostrar".
 */
enum class GroupByMode(val code: String, val labelEs: String) {
    NONE("NONE", "Ninguno"),
    CATEGORY("CATEGORY", "Proyecto / Categoría"),
    PRIORITY("PRIORITY", "Prioridad"),
    DUE_DATE("DUE_DATE", "Fecha límite"),
    ASSIGNEE("ASSIGNEE", "Responsable");

    companion object {
        fun fromCode(code: String): GroupByMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NONE
    }
}

/**
 * Modos de Ordenamiento (incluyendo Ordenamiento Inteligente por Relevancia).
 */
enum class SortByMode(val code: String, val labelEs: String) {
    SMART("SMART", "Inteligente"),
    DUE_DATE("DUE_DATE", "Fecha límite"),
    PRIORITY("PRIORITY", "Prioridad"),
    ALPHABETICAL("ALPHABETICAL", "Alfabético"),
    MANUAL("MANUAL", "Manual");

    companion object {
        fun fromCode(code: String): SortByMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SMART
    }
}

/**
 * Filtros rápidos de Fecha límite en el Panel "Mostrar".
 */
enum class DueDateFilter(val code: String, val labelEs: String) {
    ANY("ANY", "Cualquier fecha"),
    TODAY("TODAY", "Hoy"),
    NEXT_7_DAYS("NEXT_7_DAYS", "Próximos 7 días"),
    OVERDUE("OVERDUE", "Vencidas"),
    NO_DATE("NO_DATE", "Sin fecha");

    companion object {
        fun fromCode(code: String): DueDateFilter =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ANY
    }
}

/**
 * Primer día de la semana para el Calendario Mensual y la Tira Semanal.
 */
enum class FirstDayOfWeekMode(val code: String, val labelEs: String, val startIsoDay: Int) {
    SYSTEM("SYSTEM", "Sistema por defecto", 1),
    MONDAY("MONDAY", "Lunes", 1),
    SUNDAY("SUNDAY", "Domingo", 7),
    SATURDAY("SATURDAY", "Sábado", 6);

    companion object {
        fun fromCode(code: String): FirstDayOfWeekMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}

/**
 * Algoritmo de Ordenamiento Inteligente (`calculateSmartScore`):
 * Suma puntos por vencimiento (+100), vencimiento hoy (+60), prioridad Crítica (+50) / Alta (+30),
 * fijada (+80) o si bloquea otra tarea activa (+40).
 */
fun calculateSmartScore(task: TodoTask, allTasks: List<TodoTask>, todayIso: String): Int {
    var score = 0
    if (task.isCompleted) score -= 500
    if (task.isPinned) score += 80
    if (!task.isCompleted && task.dueDate.isNotBlank()) {
        when {
            task.dueDate < todayIso -> score += 100
            task.dueDate == todayIso -> score += 60
        }
    }
    score += when (task.priority) {
        3 -> 50
        2 -> 30
        1 -> 10
        else -> 0
    }
    val blocksAnotherTask = allTasks.any {
        !it.isDeleted && !it.isCompleted && it.blockedByTaskId == task.id
    }
    if (blocksAnotherTask) score += 40
    return score
}

/**
 * 24 Idiomas soportados en tiempo real con bandera, nombre nativo y soporte RTL automático.
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val isRtl: Boolean = false
) {
    ES("es", "Español", "🇲🇽", false),
    EN("en", "English", "🇺🇸", false),
    FR("fr", "Français", "🇫🇷", false),
    PT("pt", "Português", "🇧🇷", false),
    DE("de", "Deutsch", "🇩🇪", false),
    IT("it", "Italiano", "🇮🇹", false),
    JA("ja", "日本語", "🇯🇵", false),
    ZH_CN("zh-CN", "简体中文", "🇨🇳", false),
    ZH_TW("zh-TW", "繁體中文", "🇹🇼", false),
    KO("ko", "한국어", "🇰🇷", false),
    RU("ru", "Русский", "🇷🇺", false),
    AR("ar", "العربية", "🇸🇦", true),
    HI("hi", "हिन्दी", "🇮🇳", false),
    TR("tr", "Türkçe", "🇹🇷", false),
    NL("nl", "Nederlands", "🇳🇱", false),
    PL("pl", "Polski", "🇵🇱", false),
    SV("sv", "Svenska", "🇸🇪", false),
    EL("el", "Ελληνικά", "🇬🇷", false),
    HE("he", "עברית", "🇮🇱", true),
    VI("vi", "Tiếng Việt", "🇻🇳", false),
    TH("th", "ไทย", "🇹🇭", false),
    ID("id", "Bahasa Indonesia", "🇮🇩", false),
    UK("uk", "Українська", "🇺🇦", false),
    CA("ca", "Català", "🇪🇸", false);

    companion object {
        fun fromCode(code: String): AppLanguage {
            val normalized = code.trim()
            if (normalized.equals("zh", ignoreCase = true)) return ZH_CN
            return entries.firstOrNull { it.code.equals(normalized, ignoreCase = true) } ?: ES
        }
    }
}

enum class TaskPriority(val level: Int, val labelEs: String, val badgeSymbol: String) {
    LOW(0, "Baja", "▽"),
    MEDIUM(1, "Media", "◇"),
    HIGH(2, "Alta", "▲"),
    CRITICAL(3, "Crítica", "⚡");

    companion object {
        fun fromLevel(level: Int): TaskPriority =
            entries.firstOrNull { it.level == level } ?: MEDIUM
    }
}

enum class TaskRecurrence(val code: String, val labelEs: String) {
    NONE("NONE", "No repetir"),
    DAILY("DAILY", "Diariamente"),
    WEEKDAYS("WEEKDAYS", "Lunes a Viernes"),
    WEEKLY("WEEKLY", "Semanalmente"),
    MONTHLY("MONTHLY", "Mensualmente");

    companion object {
        fun fromCode(code: String): TaskRecurrence =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NONE
    }
}

data class SubtaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

fun serializeSubtasks(list: List<SubtaskItem>): String {
    if (list.isEmpty()) return ""
    return list.joinToString(";;") { item ->
        val safeTitle = item.title.replace("|", "/").replace(";;", ";")
        "${item.id}|$safeTitle|${if (item.isCompleted) "1" else "0"}"
    }
}

fun deserializeSubtasks(raw: String): List<SubtaskItem> {
    if (raw.isBlank()) return emptyList()
    return raw.split(";;").mapNotNull { token ->
        val parts = token.split("|")
        if (parts.size >= 3 && parts[1].isNotBlank()) {
            SubtaskItem(
                id = parts[0],
                title = parts[1],
                isCompleted = parts[2] == "1" || parts[2].equals("true", ignoreCase = true)
            )
        } else null
    }
}

enum class ColorblindMode(val code: String, val labelEs: String, val descriptionEs: String) {
    NONE("none", "Estándar (Sin filtro)", "Colores originales de la paleta"),
    PROTANOPIA("protanopia", "Protanopía (Rojo-Verde)", "Ajusta tonos rojos hacia azul/ámbar de alta distinción"),
    DEUTERANOPIA("deuteranopia", "Deuteranopía (Verde-Rojo)", "Sustituye verdes confusos por índigo, cian y dorado"),
    TRITANOPIA("tritanopia", "Tritanopía (Azul-Amarillo)", "Optimiza contraste con carmesí, turquesa y grafito"),
    ACHROMATOPSIA("achromatopsia", "Acromatopsia (Monocromo)", "Escala de grises de alto contraste con símbolos");

    companion object {
        fun fromCode(code: String): ColorblindMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NONE
    }
}

enum class FontScaleMode(val code: String, val scale: Float, val labelEs: String) {
    SMALL("small", 0.90f, "Compacto (90%)"),
    NORMAL("normal", 1.00f, "Normal (100%)"),
    LARGE("large", 1.15f, "Grande (115%)"),
    EXTRA_LARGE("xlarge", 1.30f, "Extra Grande (130%)"),
    MAX_LOW_VISION("max", 1.45f, "Máximo Baja Visión (145%)");

    companion object {
        fun fromCode(code: String): FontScaleMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NORMAL
    }
}

enum class BorderStyleMode(
    val code: String,
    val labelEs: String,
    val descriptionEs: String,
    val widthDp: Float,
    val alphaFactor: Float
) {
    NONE("none", "Nulo (Sin bordes)", "Tarjetas limpias sin marco exterior", 0f, 0f),
    SUBTLE("subtle", "Suave (Discreto)", "Bordes finos y suaves que no saturan la vista", 0.8f, 0.22f),
    MEDIUM("medium", "Medio (Remarcado)", "Marcos definidos con intensidad equilibrada", 1.4f, 0.55f),
    STRONG("strong", "Fuerte (Alto Contraste)", "Bordes gruesos y remarcados al 100%", 2.4f, 1.0f);

    companion object {
        fun fromCode(code: String): BorderStyleMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SUBTLE
    }
}

enum class CornerStyleMode(val code: String, val labelEs: String, val radiusDp: Int) {
    SHARP("sharp", "Recto Minimalista", 6),
    ROUNDED("rounded", "Redondeado Clásico", 18),
    PILL("pill", "Súper Curvo / Burbuja", 28);

    companion object {
        fun fromCode(code: String): CornerStyleMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ROUNDED
    }
}

enum class LayoutDensityMode(val code: String, val labelEs: String, val spacingDp: Int) {
    COMPACT("compact", "Compacto", 8),
    COMFORTABLE("comfortable", "Cómodo", 14),
    SPACIOUS("spacious", "Amplio / Aireado", 20);

    companion object {
        fun fromCode(code: String): LayoutDensityMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: COMFORTABLE
    }
}

/**
 * Estado reactivo de las preferencias almacenadas en Android DataStore.
 */
data class UserPreferencesState(
    val isLoaded: Boolean = false,
    val loggedInUserId: Int = -1,
    val isDarkMode: Boolean = false,
    val followSystemTheme: Boolean = false,
    val themeId: Int = 0,
    val notificationsEnabled: Boolean = true,
    val languageCode: String = AppLanguage.ES.code,
    val colorblindMode: ColorblindMode = ColorblindMode.NONE,
    val fontScaleMode: FontScaleMode = FontScaleMode.NORMAL,
    val highContrastEnabled: Boolean = false,
    val dyslexiaFriendlyEnabled: Boolean = false,
    val leftHandedMode: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeTouchTargets: Boolean = false,
    val zenFocusMode: Boolean = false,
    val borderStyleMode: BorderStyleMode = BorderStyleMode.SUBTLE,
    val cornerStyleMode: CornerStyleMode = CornerStyleMode.ROUNDED,
    val layoutDensityMode: LayoutDensityMode = LayoutDensityMode.COMFORTABLE,
    val hapticFeedbackEnabled: Boolean = true,
    val privacyBlurMode: Boolean = false,
    val streakDays: Int = 1,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val rewardedUnlockUntilMillis: Long = 0L,
    val firstDayOfWeekMode: FirstDayOfWeekMode = FirstDayOfWeekMode.SYSTEM,
    val lastDriveSyncMillis: Long = 0L,
    val driveAutoBackupEnabled: Boolean = true,
    val defaultReminderTime: String = "09:00 AM"
) {
    val proStatus: ProStatus
        get() = ProStatus(subscriptionPlan, rewardedUnlockUntilMillis)

    val hasProAccess: Boolean
        get() = proStatus.hasProAccess
}

data class TaskQuickTemplate(
    val name: String,
    val iconEmoji: String,
    val defaultTitle: String,
    val defaultDesc: String,
    val category: String,
    val priority: Int,
    val recurrence: String,
    val colorHex: String,
    val subtasks: List<String>
)

val QuickTaskTemplates = listOf(
    TaskQuickTemplate(
        name = "Entrega UTNA",
        iconEmoji = "📚",
        defaultTitle = "Entregar Actividad / Reporte de Materia",
        defaultDesc = "Revisar rúbrica, capturas de pantalla de la app y exportar documento en formato PDF.",
        category = "Universidad",
        priority = TaskPriority.CRITICAL.level,
        recurrence = TaskRecurrence.NONE.code,
        colorHex = "#7C3AED",
        subtasks = listOf(
            "Redactar respuestas técnicas con mis propias palabras",
            "Tomar capturas de pantalla de la aplicación",
            "Integrar portada oficial y exportar a PDF"
        )
    ),
    TaskQuickTemplate(
        name = "Sesión Pomodoro",
        iconEmoji = "⏱️",
        defaultTitle = "Bloque de Estudio Profundo (Pomodoro)",
        defaultDesc = "Sesión enfocada de 25 minutos sin distracciones seguida de 5 minutos de descanso.",
        category = "Universidad",
        priority = TaskPriority.HIGH.level,
        recurrence = TaskRecurrence.DAILY.code,
        colorHex = "#E11D48",
        subtasks = listOf(
            "Preparar apuntes y activar Modo Enfoque Zen",
            "Completar primer bloque Pomodoro de 25 min",
            "Repasar conceptos clave al terminar"
        )
    ),
    TaskQuickTemplate(
        name = "Sprint Proyecto",
        iconEmoji = "🚀",
        defaultTitle = "Avance de Proyecto en Android Studio",
        defaultDesc = "Implementar módulo, probar en dispositivo/emulador y verificar compilación sin errores.",
        category = "Proyecto",
        priority = TaskPriority.HIGH.level,
        recurrence = TaskRecurrence.WEEKDAYS.code,
        colorHex = "#2563EB",
        subtasks = listOf(
            "Actualizar modelos y capa de datos",
            "Conectar pantallas de Jetpack Compose",
            "Ejecutar assembleDebug y validar en emulador"
        )
    ),
    TaskQuickTemplate(
        name = "Rutina Salud",
        iconEmoji = "💪",
        defaultTitle = "Hábito Diario de Salud y Bienestar",
        defaultDesc = "Mantener hidratación, descanso visual y actividad física del día.",
        category = "Salud",
        priority = TaskPriority.MEDIUM.level,
        recurrence = TaskRecurrence.DAILY.code,
        colorHex = "#059669",
        subtasks = listOf(
            "Tomar 2 litros de agua durante el día",
            "30 minutos de ejercicio o caminata",
            "Pausa visual 20-20-20 frente a la pantalla"
        )
    )
)

val DefaultCollaborators = listOf(
    "Solo a mí",
    "Equipo",
    "Claudia",
    "Proyecto UTNA",
    "Laboratorio"
)

data class UserAccount(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val themeId: Int = 0,
    val profileImagePath: String? = null,
    val pinCode: String? = null
)

data class TodoTask(
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String,
    val category: String,
    val time: String,
    val dayIndex: Int,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val colorHex: String = "#7C3AED",
    val priority: Int = 1,
    val recurrence: String = "NONE",
    val subtasksJson: String = "",
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    val focusMinutes: Int = 0,
    val colorTag: Int = 0,
    val assignee: String = "Solo a mí",
    val kanbanStatus: Int = if (isCompleted) 2 else 0,
    val attachmentImagePath: String? = null,
    val blockedByTaskId: Int? = null,
    val completedDate: String? = null
) {
    val subtasks: List<SubtaskItem>
        get() = deserializeSubtasks(subtasksJson)
}
