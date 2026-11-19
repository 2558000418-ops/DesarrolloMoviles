package com.example.todolist.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TodoTask
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

data class ParsedSmartTask(
    val cleanTitle: String,
    val category: String,
    val priority: Int,
    val dueDate: String,
    val time: String,
    val assignee: String = "Solo a mí"
)

object VoiceAndShareHelper {

    private var ttsEngine: TextToSpeech? = null
    private var isTtsReady = false

    fun speakText(context: Context, text: String, languageCode: String = "es") {
        val targetLocale = localeForLanguageCode(languageCode)
        if (ttsEngine == null) {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    ttsEngine?.language = targetLocale
                    ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "todolist_tts_utt")
                }
            }
        } else if (isTtsReady) {
            ttsEngine?.language = targetLocale
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "todolist_tts_utt")
        }
    }

    fun stopSpeaking() {
        runCatching { ttsEngine?.stop() }
    }

    /**
     * Crea un Intent nativo para reconocimiento de voz (`SpeechToText`).
     */
    fun createSpeechRecognizerIntent(languageCode: String = "es"): Intent {
        val locale = localeForLanguageCode(languageCode)
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Dicta tu tarea (ej: Entregar reporte mañana 16:00 #Universidad !critica)"
            )
        }
    }

    /**
     * Parser NLP Local en 1 línea:
     * Detecta #Categoría, !Prioridad (!baja, !media, !alta, !critica/!urgente),
     * fechas relativas (hoy, mañana, pasado mañana) y horas (16:00, 4pm, 8:30am).
     */
    fun parseSmartTaskInput(rawText: String): ParsedSmartTask {
        var working = rawText.trim()
        var detectedCategory = "Universidad"
        var detectedPriority = TaskPriority.MEDIUM.level
        var detectedDate = LocalDate.now()
        var detectedTime = formatTaskTime(LocalTime.now().hour, LocalTime.now().minute)
        var detectedAssignee = "Solo a mí"

        // 0. Detectar @Responsable (ej. @Equipo, @Claudia, @ProyectoUTNA)
        val assigneeRegex = Regex("@([\\p{L}0-9_]+)")
        val assigneeMatch = assigneeRegex.find(working)
        if (assigneeMatch != null) {
            val rawName = assigneeMatch.groupValues[1].replace("_", " ").trim()
            if (rawName.isNotEmpty()) {
                detectedAssignee = rawName.replaceFirstChar { it.uppercase() }
            }
            working = working.removeRange(assigneeMatch.range)
        }

        // 1. Detectar #Categoría
        val categoryMap = mapOf(
            "#universidad" to "Universidad",
            "#uni" to "Universidad",
            "#proyecto" to "Proyecto",
            "#personal" to "Personal",
            "#trabajo" to "Trabajo",
            "#salud" to "Salud",
            "#urgente" to "Urgente"
        )
        categoryMap.forEach { (token, catName) ->
            val regex = Regex("(?i)\\b$token\\b|$token")
            if (regex.containsMatchIn(working)) {
                detectedCategory = catName
                working = working.replace(regex, " ")
            }
        }

        // 2. Detectar !Prioridad
        val priorityTokens = listOf(
            Regex("(?i)!(critica|crítica|urgente)") to TaskPriority.CRITICAL.level,
            Regex("(?i)!alta") to TaskPriority.HIGH.level,
            Regex("(?i)!media") to TaskPriority.MEDIUM.level,
            Regex("(?i)!baja") to TaskPriority.LOW.level
        )
        priorityTokens.forEach { (regex, level) ->
            if (regex.containsMatchIn(working)) {
                detectedPriority = level
                working = working.replace(regex, " ")
            }
        }

        // 3. Detectar fechas relativas (pasado mañana, mañana, hoy)
        val pasadoMananaRegex = Regex("(?i)\\bpasado\\s+mañana\\b|\\bpasado\\s+manana\\b")
        val mananaRegex = Regex("(?i)\\bmañana\\b|\\bmanana\\b|\\btomorrow\\b")
        val hoyRegex = Regex("(?i)\\bhoy\\b|\\btoday\\b")

        when {
            pasadoMananaRegex.containsMatchIn(working) -> {
                detectedDate = LocalDate.now().plusDays(2)
                working = working.replace(pasadoMananaRegex, " ")
            }
            mananaRegex.containsMatchIn(working) -> {
                detectedDate = LocalDate.now().plusDays(1)
                working = working.replace(mananaRegex, " ")
            }
            hoyRegex.containsMatchIn(working) -> {
                detectedDate = LocalDate.now()
                working = working.replace(hoyRegex, " ")
            }
        }

        // 4. Detectar hora (ej. "16:30", "4pm", "4:15pm", "08:00", "9am")
        val timeRegex = Regex("(?i)\\b([01]?\\d|2[0-3])(?::([0-5]\\d))?\\s*(am|pm)?\\b")
        val match = timeRegex.findAll(working).firstOrNull { m ->
            val hasColon = m.groups[2] != null
            val hasAmPm = m.groups[3] != null
            hasColon || hasAmPm
        }
        if (match != null) {
            val rawHour = match.groupValues[1].toIntOrNull() ?: 10
            val rawMin = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3].lowercase()
            val hour24 = when {
                amPm == "pm" && rawHour < 12 -> rawHour + 12
                amPm == "am" && rawHour == 12 -> 0
                else -> rawHour.coerceIn(0, 23)
            }
            detectedTime = formatTaskTime(hour24, rawMin.coerceIn(0, 59))
            working = working.removeRange(match.range)
        }

        val cleanTitle = working.replace(Regex("\\s+"), " ").trim()
        return ParsedSmartTask(
            cleanTitle = cleanTitle.ifEmpty { rawText.trim() },
            category = detectedCategory,
            priority = detectedPriority,
            dueDate = detectedDate.toString(),
            time = detectedTime,
            assignee = detectedAssignee
        )
    }

    fun triggerHapticFeedback(context: Context, enabled: Boolean) {
        if (!enabled) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib?.vibrate(VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vib?.vibrate(35L)
                }
            }
        }
    }

    fun shareTaskViaIntent(context: Context, task: TodoTask) {
        val prio = TaskPriority.fromLevel(task.priority)
        val subtasksText = if (task.subtasks.isEmpty()) {
            ""
        } else {
            "\n📋 *Subtareas / Pasos:*\n" + task.subtasks.joinToString("\n") { sub ->
                "${if (sub.isCompleted) "✅" else "⬜"} ${sub.title}"
            }
        }
        val statusText = if (task.isCompleted) "✅ Completada" else "⏳ Pendiente"
        val message = buildString {
            appendLine("📌 *Tarea:* ${task.title}")
            appendLine("👤 *Responsable:* ${task.assignee}")
            appendLine("🏷️ *Categoría:* ${task.category} (${prio.badgeSymbol} Prioridad ${prio.labelEs})")
            appendLine("📅 *Fecha y Hora:* ${task.dueDate.ifBlank { "Sin fecha" }} • ${task.time}")
            appendLine("📊 *Estado:* $statusText")
            if (task.focusMinutes > 0) {
                appendLine("⏱️ *Tiempo de Enfoque Pomodoro:* ${task.focusMinutes} min")
            }
            appendLine("📝 *Notas:* ${task.description}")
            if (subtasksText.isNotEmpty()) {
                append(subtasksText)
            }
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Tarea: ${task.title}")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir tarea vía...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(chooser) }
    }

    fun shareDayAgendaViaIntent(context: Context, dateFormatted: String, dayTasks: List<TodoTask>) {
        val message = buildString {
            appendLine("📅 *Agenda del día: $dateFormatted*")
            if (dayTasks.isEmpty()) {
                appendLine("Sin tareas programadas.")
            } else {
                dayTasks.forEach { t ->
                    val check = if (t.isCompleted) "✅" else "⏳"
                    appendLine("$check [${t.time}] *${t.title}* (${t.category})")
                }
            }
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Agenda $dateFormatted")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir agenda del día...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(chooser) }
    }

    fun hashSha256(input: String): String {
        return runCatching {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(Locale.US, it) }
        }.getOrDefault(input)
    }
}
