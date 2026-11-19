package com.example.todolist.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.todolist.model.SubtaskItem
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TaskRecurrence
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.model.serializeSubtasks
import com.example.todolist.ui.theme.TaskColorChoices
import com.example.todolist.ui.theme.hexToColorTag
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.calculateNextRecurrenceDate
import java.time.LocalDate

/**
 * Base de Datos Multiusuario SQLite (Versión 6) con migración idempotente en `users` y `tasks`.
 * Incluye soporte para Colaboración (`assignee`), Tablero Kanban, Adjuntos, Dependencias y PIN.
 */
class TodoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "todo_multiuser_v3.db", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                theme_id INTEGER NOT NULL DEFAULT 0,
                profile_image_path TEXT,
                pin_code TEXT DEFAULT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL DEFAULT '',
                due_date TEXT NOT NULL DEFAULT '',
                time TEXT NOT NULL,
                day_index INTEGER NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                priority INTEGER NOT NULL DEFAULT 1,
                recurrence TEXT NOT NULL DEFAULT 'NONE',
                subtasks_json TEXT NOT NULL DEFAULT '',
                is_pinned INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                reminder_enabled INTEGER NOT NULL DEFAULT 0,
                focus_minutes INTEGER NOT NULL DEFAULT 0,
                color_tag INTEGER NOT NULL DEFAULT 0,
                color_hex TEXT NOT NULL DEFAULT '#7C3AED',
                assignee TEXT NOT NULL DEFAULT 'Solo a mí',
                kanban_status INTEGER NOT NULL DEFAULT 0,
                attachment_image_path TEXT DEFAULT NULL,
                blocked_by_task_id INTEGER DEFAULT NULL,
                completed_date TEXT DEFAULT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        ensureSchemaColumns(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            ensureSchemaColumns(db)
        }
    }

    private fun ensureSchemaColumns(db: SQLiteDatabase) {
        // 1. Verificar columnas de tabla `users`
        val existingUserColumns = mutableSetOf<String>()
        val userCursor = db.rawQuery("PRAGMA table_info(users)", null)
        userCursor.use {
            val nameIdx = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIdx >= 0) existingUserColumns.add(it.getString(nameIdx).lowercase())
            }
        }
        if (!existingUserColumns.contains("pin_code")) {
            runCatching {
                db.execSQL("ALTER TABLE users ADD COLUMN pin_code TEXT DEFAULT NULL")
            }
        }

        // 2. Verificar columnas de tabla `tasks`
        val existingTaskColumns = mutableSetOf<String>()
        val taskCursor = db.rawQuery("PRAGMA table_info(tasks)", null)
        taskCursor.use {
            val nameIdx = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIdx >= 0) existingTaskColumns.add(it.getString(nameIdx).lowercase())
            }
        }

        fun addTaskColumnIfMissing(columnName: String, definition: String) {
            if (!existingTaskColumns.contains(columnName.lowercase())) {
                runCatching {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN $columnName $definition")
                }
            }
        }

        addTaskColumnIfMissing("date", "TEXT NOT NULL DEFAULT ''")
        addTaskColumnIfMissing("due_date", "TEXT NOT NULL DEFAULT ''")
        addTaskColumnIfMissing("priority", "INTEGER NOT NULL DEFAULT 1")
        addTaskColumnIfMissing("recurrence", "TEXT NOT NULL DEFAULT 'NONE'")
        addTaskColumnIfMissing("subtasks_json", "TEXT NOT NULL DEFAULT ''")
        addTaskColumnIfMissing("is_pinned", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("sort_order", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("is_deleted", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("reminder_enabled", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("focus_minutes", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("color_tag", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("color_hex", "TEXT NOT NULL DEFAULT '#7C3AED'")
        addTaskColumnIfMissing("assignee", "TEXT NOT NULL DEFAULT 'Solo a mí'")
        addTaskColumnIfMissing("kanban_status", "INTEGER NOT NULL DEFAULT 0")
        addTaskColumnIfMissing("attachment_image_path", "TEXT DEFAULT NULL")
        addTaskColumnIfMissing("blocked_by_task_id", "INTEGER DEFAULT NULL")
        addTaskColumnIfMissing("completed_date", "TEXT DEFAULT NULL")
    }

    fun getAllSavedUsers(): List<UserAccount> {
        val list = mutableListOf<UserAccount>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, email, password, theme_id, profile_image_path, pin_code FROM users ORDER BY id DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    UserAccount(
                        id = it.getInt(0),
                        name = it.getString(1),
                        email = it.getString(2),
                        password = it.getString(3),
                        themeId = it.getInt(4),
                        profileImagePath = if (it.isNull(5)) null else it.getString(5),
                        pinCode = if (it.isNull(6)) null else it.getString(6)
                    )
                )
            }
        }
        return list
    }

    fun getUserById(userId: Int): UserAccount? {
        if (userId <= 0) return null
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, email, password, theme_id, profile_image_path, pin_code FROM users WHERE id = ?",
            arrayOf(userId.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                return UserAccount(
                    id = it.getInt(0),
                    name = it.getString(1),
                    email = it.getString(2),
                    password = it.getString(3),
                    themeId = it.getInt(4),
                    profileImagePath = if (it.isNull(5)) null else it.getString(5),
                    pinCode = if (it.isNull(6)) null else it.getString(6)
                )
            }
        }
        return null
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        defaultThemeId: Int = 0
    ): UserAccount? {
        val db = writableDatabase
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val hashedPassword = VoiceAndShareHelper.hashSha256(password)
        val values = ContentValues().apply {
            put("name", cleanName)
            put("email", cleanEmail)
            put("password", hashedPassword)
            put("theme_id", defaultThemeId)
        }
        val newId = db.insert("users", null, values)
        if (newId == -1L) return null

        val userIdInt = newId.toInt()
        val sampleSubtasks = serializeSubtasks(
            listOf(
                SubtaskItem("1", "Probar Entrada Inteligente NLP con Voz (@Equipo)", true),
                SubtaskItem("2", "Abrir Panel 'Mostrar' (Lista, Panel, Calendario, Matriz)", false),
                SubtaskItem("3", "Sincronizar respaldo con Google Drive", false)
            )
        )
        insertTask(
            userId = userIdInt,
            title = "Explorar Panel 'Mostrar', Kanban y NLP",
            description = "Usa la barra superior de Creación Rápida con #Universidad !critica @Equipo o abre el panel 'Mostrar'.",
            category = "Universidad",
            time = "10:00 AM",
            dayIndex = 1,
            dueDate = LocalDate.now().toString(),
            colorHex = "#E11D48",
            priority = TaskPriority.CRITICAL.level,
            recurrence = TaskRecurrence.DAILY.code,
            subtasksJson = sampleSubtasks,
            isPinned = true,
            reminderEnabled = true,
            assignee = "Equipo"
        )
        insertTask(
            userId = userIdInt,
            title = "Revisar el Calendario Mensual interactivo",
            description = "Toca cualquier día marcado con barras de color en el Calendario para ver o editar sus tareas programadas.",
            category = "Proyecto",
            time = "05:00 PM",
            dayIndex = 1,
            dueDate = LocalDate.now().toString(),
            colorHex = "#059669",
            priority = TaskPriority.HIGH.level,
            recurrence = TaskRecurrence.NONE.code,
            assignee = "Solo a mí",
            kanbanStatus = 1
        )

        return UserAccount(userIdInt, cleanName, cleanEmail, hashedPassword, defaultThemeId, null, null)
    }

    fun loginUser(email: String, password: String): UserAccount? {
        val db = readableDatabase
        val cleanEmail = email.trim().lowercase()
        val hashed = VoiceAndShareHelper.hashSha256(password)
        val cursor = db.rawQuery(
            "SELECT id, name, email, password, theme_id, profile_image_path, pin_code FROM users WHERE email = ? AND (password = ? OR password = ?)",
            arrayOf(cleanEmail, hashed, password)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return UserAccount(
                    id = it.getInt(0),
                    name = it.getString(1),
                    email = it.getString(2),
                    password = it.getString(3),
                    themeId = it.getInt(4),
                    profileImagePath = if (it.isNull(5)) null else it.getString(5),
                    pinCode = if (it.isNull(6)) null else it.getString(6)
                )
            }
        }
        return null
    }

    fun updateUserTheme(userId: Int, themeId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("theme_id", themeId)
        }
        db.update("users", values, "id = ?", arrayOf(userId.toString()))
    }

    fun updateUserProfileImage(userId: Int, imagePath: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (imagePath == null) putNull("profile_image_path")
            else put("profile_image_path", imagePath)
        }
        db.update("users", values, "id = ?", arrayOf(userId.toString()))
    }

    fun updateUserPinCode(userId: Int, pinCode: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (pinCode.isNullOrBlank()) putNull("pin_code")
            else put("pin_code", pinCode.trim())
        }
        db.update("users", values, "id = ?", arrayOf(userId.toString()))
    }

    fun deleteUserAccount(userId: Int) {
        val db = writableDatabase
        db.delete("tasks", "user_id = ?", arrayOf(userId.toString()))
        db.delete("users", "id = ?", arrayOf(userId.toString()))
    }

    fun rescheduleOverdueTasksToToday(userId: Int): Int {
        val todayIso = LocalDate.now().toString()
        val db = writableDatabase
        val values = ContentValues().apply {
            put("date", todayIso)
            put("due_date", todayIso)
        }
        return db.update(
            "tasks",
            values,
            "user_id = ? AND is_deleted = 0 AND is_completed = 0 AND due_date != '' AND due_date < ?",
            arrayOf(userId.toString(), todayIso)
        )
    }

    fun getTasksForUser(userId: Int): List<TodoTask> {
        val list = mutableListOf<TodoTask>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT id, user_id, title, description, category, time, day_index, is_completed,
                   due_date, date, color_hex, priority, recurrence, subtasks_json,
                   is_pinned, sort_order, is_deleted, reminder_enabled, focus_minutes, color_tag,
                   assignee, kanban_status, attachment_image_path, blocked_by_task_id, completed_date
            FROM tasks
            WHERE user_id = ?
            ORDER BY is_pinned DESC, sort_order ASC, priority DESC, id DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                val due = it.getString(8)?.ifBlank { it.getString(9) ?: "" } ?: ""
                val hex = it.getString(10)?.ifBlank { "#7C3AED" } ?: "#7C3AED"
                val completed = it.getInt(7) == 1
                val assigneeVal = it.getString(20)?.ifBlank { "Solo a mí" } ?: "Solo a mí"
                val rawKanban = it.getInt(21)
                val syncedKanban = if (completed) 2 else rawKanban.coerceIn(0, 1)
                list.add(
                    TodoTask(
                        id = it.getInt(0),
                        userId = it.getInt(1),
                        title = it.getString(2),
                        description = it.getString(3),
                        category = it.getString(4),
                        time = it.getString(5),
                        dayIndex = it.getInt(6),
                        isCompleted = completed,
                        dueDate = due,
                        colorHex = hex,
                        priority = it.getInt(11),
                        recurrence = it.getString(12) ?: "NONE",
                        subtasksJson = it.getString(13) ?: "",
                        isPinned = it.getInt(14) == 1,
                        sortOrder = it.getInt(15),
                        isDeleted = it.getInt(16) == 1,
                        reminderEnabled = it.getInt(17) == 1,
                        focusMinutes = it.getInt(18),
                        colorTag = it.getInt(19),
                        assignee = assigneeVal,
                        kanbanStatus = syncedKanban,
                        attachmentImagePath = if (it.isNull(22)) null else it.getString(22),
                        blockedByTaskId = if (it.isNull(23)) null else it.getInt(23),
                        completedDate = if (it.isNull(24)) null else it.getString(24)
                    )
                )
            }
        }
        return list
    }

    fun insertTask(
        userId: Int,
        title: String,
        description: String,
        category: String,
        time: String,
        dayIndex: Int,
        dueDate: String = LocalDate.now().toString(),
        colorHex: String = "#7C3AED",
        priority: Int = 1,
        recurrence: String = "NONE",
        subtasksJson: String = "",
        isPinned: Boolean = false,
        reminderEnabled: Boolean = false,
        focusMinutes: Int = 0,
        assignee: String = "Solo a mí",
        kanbanStatus: Int = 0,
        attachmentImagePath: String? = null,
        blockedByTaskId: Int? = null
    ): Long {
        val db = writableDatabase
        val tag = hexToColorTag(colorHex)
        val cleanAssignee = assignee.trim().ifBlank { "Solo a mí" }
        val values = ContentValues().apply {
            put("user_id", userId)
            put("title", title.trim())
            put("description", description.trim().ifEmpty { "Sin notas adicionales." })
            put("category", category)
            put("time", time)
            put("day_index", dayIndex)
            put("date", dueDate)
            put("due_date", dueDate)
            put("color_hex", colorHex)
            put("color_tag", tag)
            put("priority", priority)
            put("recurrence", recurrence)
            put("subtasks_json", subtasksJson)
            put("is_pinned", if (isPinned) 1 else 0)
            put("sort_order", (System.currentTimeMillis() / 1000).toInt())
            put("is_deleted", 0)
            put("reminder_enabled", if (reminderEnabled) 1 else 0)
            put("focus_minutes", focusMinutes)
            put("assignee", cleanAssignee)
            put("is_completed", if (kanbanStatus == 2) 1 else 0)
            put("kanban_status", kanbanStatus.coerceIn(0, 2))
            if (attachmentImagePath != null) put("attachment_image_path", attachmentImagePath)
            if (blockedByTaskId != null) put("blocked_by_task_id", blockedByTaskId)
            if (kanbanStatus == 2) put("completed_date", LocalDate.now().toString())
        }
        return db.insert("tasks", null, values)
    }

    fun updateTaskCompletionWithRecurrence(task: TodoTask, isCompleted: Boolean): String? {
        val db = writableDatabase
        val todayIso = LocalDate.now().toString()
        val values = ContentValues().apply {
            put("is_completed", if (isCompleted) 1 else 0)
            put("kanban_status", if (isCompleted) 2 else 0)
            if (isCompleted) put("completed_date", todayIso) else putNull("completed_date")
        }
        db.update("tasks", values, "id = ?", arrayOf(task.id.toString()))

        if (isCompleted && task.recurrence != "NONE" && !task.isCompleted) {
            val baseDate = task.dueDate.ifBlank { todayIso }
            val nextDate = calculateNextRecurrenceDate(baseDate, task.recurrence)
            val resetSubtasks = serializeSubtasks(
                task.subtasks.map { it.copy(isCompleted = false) }
            )
            insertTask(
                userId = task.userId,
                title = task.title,
                description = task.description,
                category = task.category,
                time = task.time,
                dayIndex = task.dayIndex,
                dueDate = nextDate,
                colorHex = task.colorHex,
                priority = task.priority,
                recurrence = task.recurrence,
                subtasksJson = resetSubtasks,
                isPinned = task.isPinned,
                reminderEnabled = task.reminderEnabled,
                focusMinutes = 0,
                assignee = task.assignee,
                kanbanStatus = 0
            )
            return nextDate
        }
        return null
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_completed", if (isCompleted) 1 else 0)
            put("kanban_status", if (isCompleted) 2 else 0)
            if (isCompleted) put("completed_date", LocalDate.now().toString())
            else putNull("completed_date")
        }
        db.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskKanbanStatus(taskId: Int, newStatus: Int) {
        val clamped = newStatus.coerceIn(0, 2)
        val isComp = clamped == 2
        val values = ContentValues().apply {
            put("kanban_status", clamped)
            put("is_completed", if (isComp) 1 else 0)
            if (isComp) put("completed_date", LocalDate.now().toString())
            else putNull("completed_date")
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskAssignee(taskId: Int, assignee: String) {
        val clean = assignee.trim().ifBlank { "Solo a mí" }
        val values = ContentValues().apply {
            put("assignee", clean)
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskAttachment(taskId: Int, imagePath: String?) {
        val values = ContentValues().apply {
            if (imagePath.isNullOrBlank()) putNull("attachment_image_path")
            else put("attachment_image_path", imagePath)
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskBlocker(taskId: Int, blockedByTaskId: Int?) {
        val values = ContentValues().apply {
            if (blockedByTaskId == null || blockedByTaskId <= 0) putNull("blocked_by_task_id")
            else put("blocked_by_task_id", blockedByTaskId)
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskSubtasks(taskId: Int, subtasksJson: String, autoCompleteTask: Boolean? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("subtasks_json", subtasksJson)
            if (autoCompleteTask != null) {
                put("is_completed", if (autoCompleteTask) 1 else 0)
                put("kanban_status", if (autoCompleteTask) 2 else 0)
                if (autoCompleteTask) put("completed_date", LocalDate.now().toString())
            }
        }
        db.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskPin(taskId: Int, isPinned: Boolean) {
        writableDatabase.update(
            "tasks",
            ContentValues().apply { put("is_pinned", if (isPinned) 1 else 0) },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun moveTaskOrder(userId: Int, taskId: Int, moveUp: Boolean) {
        val activeTasks = getTasksForUser(userId).filter { !it.isDeleted }
        val index = activeTasks.indexOfFirst { it.id == taskId }
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex !in activeTasks.indices) return

        val reordered = activeTasks.toMutableList()
        val item = reordered.removeAt(index)
        reordered.add(targetIndex, item)

        val db = writableDatabase
        reordered.forEachIndexed { idx, t ->
            db.update(
                "tasks",
                ContentValues().apply { put("sort_order", idx + 1) },
                "id = ?",
                arrayOf(t.id.toString())
            )
        }
    }

    fun addFocusMinutes(taskId: Int, currentMinutes: Int, addedMinutes: Int) {
        val total = (currentMinutes + addedMinutes).coerceAtLeast(0)
        writableDatabase.update(
            "tasks",
            ContentValues().apply { put("focus_minutes", total) },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun updateTaskColor(taskId: Int, colorHex: String) {
        if (TaskColorChoices.none { it.hex.equals(colorHex, ignoreCase = true) }) return
        val tag = hexToColorTag(colorHex)
        writableDatabase.update(
            "tasks",
            ContentValues().apply {
                put("color_hex", colorHex)
                put("color_tag", tag)
            },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun updateTaskSchedule(taskId: Int, dueDate: String, time: String) {
        writableDatabase.update(
            "tasks",
            ContentValues().apply {
                put("date", dueDate)
                put("due_date", dueDate)
                put("time", time)
            },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun updateTaskFull(
        taskId: Int,
        title: String,
        description: String,
        category: String,
        dueDate: String,
        time: String,
        colorHex: String,
        priority: Int = 1,
        recurrence: String = "NONE",
        subtasksJson: String = "",
        isPinned: Boolean = false,
        reminderEnabled: Boolean = false,
        assignee: String = "Solo a mí",
        kanbanStatus: Int? = null,
        attachmentImagePath: String? = null,
        blockedByTaskId: Int? = null
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val tag = hexToColorTag(colorHex)
        val cleanAssignee = assignee.trim().ifBlank { "Solo a mí" }
        writableDatabase.update(
            "tasks",
            ContentValues().apply {
                put("title", cleanTitle)
                put("description", description.trim().ifEmpty { "Sin notas adicionales." })
                put("category", category)
                put("date", dueDate)
                put("due_date", dueDate)
                put("time", time)
                put("color_hex", colorHex)
                put("color_tag", tag)
                put("priority", priority)
                put("recurrence", recurrence)
                put("subtasks_json", subtasksJson)
                put("is_pinned", if (isPinned) 1 else 0)
                put("reminder_enabled", if (reminderEnabled) 1 else 0)
                put("assignee", cleanAssignee)
                if (kanbanStatus != null) {
                    put("kanban_status", kanbanStatus.coerceIn(0, 2))
                    put("is_completed", if (kanbanStatus == 2) 1 else 0)
                }
                if (attachmentImagePath == null) putNull("attachment_image_path")
                else put("attachment_image_path", attachmentImagePath)
                if (blockedByTaskId == null || blockedByTaskId <= 0) putNull("blocked_by_task_id")
                else put("blocked_by_task_id", blockedByTaskId)
            },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun softDeleteTask(taskId: Int) {
        writableDatabase.update(
            "tasks",
            ContentValues().apply { put("is_deleted", 1) },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun restoreTaskFromTrash(taskId: Int) {
        writableDatabase.update(
            "tasks",
            ContentValues().apply { put("is_deleted", 0) },
            "id = ?",
            arrayOf(taskId.toString())
        )
    }

    fun deleteTaskPermanently(taskId: Int) {
        val db = writableDatabase
        db.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }

    fun emptyTrashForUser(userId: Int) {
        writableDatabase.delete(
            "tasks",
            "user_id = ? AND is_deleted = 1",
            arrayOf(userId.toString())
        )
    }

    fun importTasksFromBackupString(userId: Int, rawBackup: String): Int {
        val lines = rawBackup.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty() || !lines.first().startsWith("TODOLIST_BACKUP")) return 0
        var importedCount = 0
        lines.drop(1).forEach { line ->
            val parts = line.split("||")
            if (parts.size >= 6 && parts[0].isNotBlank()) {
                val id = insertTask(
                    userId = userId,
                    title = parts[0],
                    description = parts[1],
                    category = parts[2].ifBlank { "Personal" },
                    dueDate = parts[3].ifBlank { LocalDate.now().toString() },
                    time = parts[4].ifBlank { "10:00 AM" },
                    dayIndex = 1,
                    colorHex = parts[5].ifBlank { "#7C3AED" },
                    priority = parts.getOrNull(6)?.toIntOrNull() ?: 1,
                    recurrence = parts.getOrNull(7) ?: "NONE",
                    subtasksJson = parts.getOrNull(8) ?: "",
                    isPinned = parts.getOrNull(10) == "1",
                    focusMinutes = parts.getOrNull(11)?.toIntOrNull() ?: 0,
                    assignee = parts.getOrNull(12)?.ifBlank { "Solo a mí" } ?: "Solo a mí"
                )
                if (id != -1L && parts.getOrNull(9) == "1") {
                    updateTaskCompletion(id.toInt(), true)
                }
                if (id != -1L) importedCount++
            }
        }
        return importedCount
    }
}
