package com.example.todolist.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.todolist.data.TodoDatabaseHelper
import com.example.todolist.model.FirstDayOfWeekMode
import com.example.todolist.model.TodoTask
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

fun localeForLanguageCode(languageCode: String): Locale = when (languageCode.lowercase()) {
    "en" -> Locale.US
    "fr" -> Locale.FRANCE
    "pt" -> Locale.forLanguageTag("pt-BR")
    "de" -> Locale.GERMANY
    "it" -> Locale.ITALY
    "ja" -> Locale.JAPAN
    "zh-cn", "zh" -> Locale.SIMPLIFIED_CHINESE
    "zh-tw" -> Locale.TRADITIONAL_CHINESE
    "ko" -> Locale.KOREA
    "ru" -> Locale.forLanguageTag("ru-RU")
    "ar" -> Locale.forLanguageTag("ar-SA")
    "hi" -> Locale.forLanguageTag("hi-IN")
    "tr" -> Locale.forLanguageTag("tr-TR")
    "nl" -> Locale.forLanguageTag("nl-NL")
    "pl" -> Locale.forLanguageTag("pl-PL")
    "sv" -> Locale.forLanguageTag("sv-SE")
    "el" -> Locale.forLanguageTag("el-GR")
    "he" -> Locale.forLanguageTag("he-IL")
    "vi" -> Locale.forLanguageTag("vi-VN")
    "th" -> Locale.forLanguageTag("th-TH")
    "id" -> Locale.forLanguageTag("id-ID")
    "uk" -> Locale.forLanguageTag("uk-UA")
    "ca" -> Locale.forLanguageTag("ca-ES")
    else -> Locale.forLanguageTag("es-MX")
}

fun formatTaskDate(isoDate: String, languageCode: String = "es"): String = try {
    LocalDate.parse(isoDate)
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", localeForLanguageCode(languageCode)))
} catch (_: Exception) {
    when (languageCode.lowercase()) {
        "en" -> "No date"
        "fr" -> "Sans date"
        "pt" -> "Sem data"
        "de" -> "Kein Datum"
        "it" -> "Senza data"
        "ja" -> "日付なし"
        "zh-cn", "zh", "zh-tw" -> "无日期"
        "ko" -> "날짜 없음"
        "ru" -> "Без даты"
        "ar" -> "بدون تاريخ"
        else -> "Sin fecha"
    }
}

fun formatTaskTime(hour: Int, minute: Int): String =
    String.format(
        Locale.US,
        "%02d:%02d %s",
        if (hour % 12 == 0) 12 else hour % 12,
        minute,
        if (hour < 12) "AM" else "PM"
    )

fun parseTaskTime(value: String): LocalTime =
    runCatching { LocalTime.parse(value, DateTimeFormatter.ofPattern("hh:mm a", Locale.US)) }
        .getOrElse { runCatching { LocalTime.parse(value) }.getOrDefault(LocalTime.now()) }

fun calculateNextRecurrenceDate(currentIsoDate: String, recurrenceCode: String): String {
    val baseDate = runCatching { LocalDate.parse(currentIsoDate) }.getOrDefault(LocalDate.now())
    val nextDate = when (recurrenceCode.uppercase()) {
        "DAILY" -> baseDate.plusDays(1)
        "WEEKDAYS" -> {
            var candidate = baseDate.plusDays(1)
            while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY) {
                candidate = candidate.plusDays(1)
            }
            candidate
        }
        "WEEKLY" -> baseDate.plusWeeks(1)
        "MONTHLY" -> baseDate.plusMonths(1)
        else -> baseDate
    }
    return nextDate.toString()
}

fun saveImageFromUriToInternalStorage(context: Context, uri: Uri, userId: Int): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 300, 300, true)
        val fileName = "avatar_user_${userId}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Guarda una imagen o captura adjunta a una tarea comprimiéndola a un máximo de 800px.
 */
fun saveTaskAttachmentImage(context: Context, uri: Uri, taskId: Int): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        val maxSide = max(originalBitmap.width, originalBitmap.height).coerceAtLeast(1)
        val scale = if (maxSide > 800) 800f / maxSide.toFloat() else 1f
        val targetW = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
        val targetH = (originalBitmap.height * scale).toInt().coerceAtLeast(1)

        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
        val fileName = "task_attachment_${taskId}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun exportTasksBackupString(tasks: List<TodoTask>): String {
    val active = tasks.filter { !it.isDeleted }
    if (active.isEmpty()) return "TODOLIST_BACKUP_V4\n"
    val lines = active.joinToString("\n") { t ->
        listOf(
            t.title.replace("\n", " ").replace("||", " "),
            t.description.replace("\n", " ").replace("||", " "),
            t.category,
            t.dueDate,
            t.time,
            t.colorHex,
            t.priority.toString(),
            t.recurrence,
            t.subtasksJson,
            if (t.isCompleted) "1" else "0",
            if (t.isPinned) "1" else "0",
            t.focusMinutes.toString(),
            t.assignee
        ).joinToString("||")
    }
    return "TODOLIST_BACKUP_V6\n$lines"
}

/**
 * Devuelve la lista de 7 días de la semana (`DayOfWeek`) ordenados según `FirstDayOfWeekMode`.
 */
fun orderedDaysOfWeek(mode: FirstDayOfWeekMode): List<DayOfWeek> {
    val startIso = when (mode) {
        FirstDayOfWeekMode.SUNDAY -> 7
        FirstDayOfWeekMode.SATURDAY -> 6
        FirstDayOfWeekMode.MONDAY, FirstDayOfWeekMode.SYSTEM -> 1
    }
    return (0..6).map { offset ->
        val iso = ((startIso - 1 + offset) % 7) + 1
        DayOfWeek.of(iso)
    }
}

/**
 * Serializa todas las tareas del usuario en un documento JSON estructurado para guardar como archivo `.json` o Google Drive.
 */
fun exportTasksToJsonString(tasks: List<TodoTask>): String {
    val root = JSONObject()
    root.put("version", 6)
    root.put("exportedAt", LocalDate.now().toString())
    val array = JSONArray()
    tasks.filter { !it.isDeleted }.forEach { t ->
        val obj = JSONObject().apply {
            put("title", t.title)
            put("description", t.description)
            put("category", t.category)
            put("dueDate", t.dueDate)
            put("time", t.time)
            put("colorHex", t.colorHex)
            put("priority", t.priority)
            put("recurrence", t.recurrence)
            put("subtasksJson", t.subtasksJson)
            put("isCompleted", t.isCompleted)
            put("isPinned", t.isPinned)
            put("focusMinutes", t.focusMinutes)
            put("assignee", t.assignee)
            put("kanbanStatus", t.kanbanStatus)
        }
        array.put(obj)
    }
    root.put("tasks", array)
    return root.toString(2)
}

/**
 * Importa tareas desde el contenido de un archivo `.json` o respaldo de Google Drive hacia SQLite.
 */
fun importTasksFromJsonString(
    dbHelper: TodoDatabaseHelper,
    userId: Int,
    jsonContent: String
): Int {
    return try {
        val root = JSONObject(jsonContent)
        val array = root.optJSONArray("tasks") ?: return 0
        var imported = 0
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val title = obj.optString("title", "").trim()
            if (title.isNotEmpty()) {
                val isCompleted = obj.optBoolean("isCompleted", false)
                val kanban = obj.optInt("kanbanStatus", if (isCompleted) 2 else 0)
                val newId = dbHelper.insertTask(
                    userId = userId,
                    title = title,
                    description = obj.optString("description", "Sin notas adicionales."),
                    category = obj.optString("category", "Universidad"),
                    dueDate = obj.optString("dueDate", LocalDate.now().toString()),
                    time = obj.optString("time", "10:00 AM"),
                    dayIndex = 1,
                    colorHex = obj.optString("colorHex", "#7C3AED"),
                    priority = obj.optInt("priority", 1),
                    recurrence = obj.optString("recurrence", "NONE"),
                    subtasksJson = obj.optString("subtasksJson", ""),
                    isPinned = obj.optBoolean("isPinned", false),
                    focusMinutes = obj.optInt("focusMinutes", 0),
                    assignee = obj.optString("assignee", "Solo a mí"),
                    kanbanStatus = kanban
                )
                if (newId != -1L) imported++
            }
        }
        imported
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}
