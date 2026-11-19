package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.SubtaskItem
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TaskRecurrence
import com.example.todolist.model.TodoTask
import com.example.todolist.model.deserializeSubtasks
import com.example.todolist.model.serializeSubtasks
import com.example.todolist.ui.components.TaskColorSelector
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AccentOrange
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.appTextFieldColors
import com.example.todolist.ui.theme.priorityColor
import com.example.todolist.ui.theme.taskAccent
import com.example.todolist.ui.theme.taskColorName
import com.example.todolist.ui.theme.taskColorSymbol
import com.example.todolist.util.NotificationHelper
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.formatTaskDate
import com.example.todolist.util.formatTaskTime
import com.example.todolist.util.parseTaskTime
import com.example.todolist.util.saveTaskAttachmentImage
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate

enum class PomodoroPreset(val label: String, val minutes: Int) {
    POMODORO_25("Pomodoro 25m", 25),
    SHORT_BREAK_5("Descanso 5m", 5),
    DEEP_FOCUS_45("Enfoque 45m", 45)
}

@Composable
fun TaskDetailView(
    theme: AppThemePalette,
    task: TodoTask,
    allUserTasks: List<TodoTask> = emptyList(),
    hasProAccess: Boolean = false,
    languageCode: String = "es",
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    notificationsEnabled: Boolean = true,
    onRequestProPaywall: (String) -> Unit = {},
    onBack: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onChangeColor: (String) -> Unit,
    onUpdateSchedule: (String, String) -> Unit,
    onUpdateSubtasksLive: (String, Boolean?) -> Unit = { _, _ -> },
    onAddFocusMinutes: (Int) -> Unit = {},
    onUpdateAttachment: (String?) -> Unit = {},
    onUpdateBlocker: (Int?) -> Unit = {},
    onUpdateKanbanStatus: (Int) -> Unit = {},
    onSaveFullChanges: (
        title: String,
        description: String,
        category: String,
        dueDate: String,
        time: String,
        colorHex: String,
        priority: Int,
        recurrence: String,
        subtasksJson: String,
        isPinned: Boolean,
        reminderEnabled: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onDuplicateTask: (TodoTask) -> Unit = {}
) {
    val context = LocalContext.current

    var editedTitle by remember(task.id) { mutableStateOf(task.title) }
    var editedDescription by remember(task.id) { mutableStateOf(task.description) }
    var editedCategory by remember(task.id) { mutableStateOf(task.category) }
    var editedDate by remember(task.id, task.dueDate) {
        mutableStateOf(task.dueDate.ifBlank { LocalDate.now().toString() })
    }
    var editedTime by remember(task.id, task.time) { mutableStateOf(task.time) }
    var editedColorHex by remember(task.id, task.colorHex) { mutableStateOf(task.colorHex) }
    var editedPriority by remember(task.id, task.priority) { mutableIntStateOf(task.priority) }
    var editedRecurrence by remember(task.id, task.recurrence) { mutableStateOf(task.recurrence) }
    var editedPinned by remember(task.id, task.isPinned) { mutableStateOf(task.isPinned) }
    var editedReminder by remember(task.id, task.reminderEnabled) { mutableStateOf(task.reminderEnabled) }
    var editedKanbanStatus by remember(task.id, task.kanbanStatus, task.isCompleted) {
        mutableIntStateOf(if (task.isCompleted) 2 else task.kanbanStatus)
    }
    var editedAttachmentPath by remember(task.id, task.attachmentImagePath) {
        mutableStateOf(task.attachmentImagePath)
    }
    var editedBlockerId by remember(task.id, task.blockedByTaskId) {
        mutableStateOf(task.blockedByTaskId)
    }
    var showBlockerDialog by remember { mutableStateOf(false) }

    val attachmentBitmap = remember(editedAttachmentPath) {
        val path = editedAttachmentPath
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
        } else null
    }

    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveTaskAttachmentImage(context, uri, task.id)
            if (savedPath != null) {
                editedAttachmentPath = savedPath
                onUpdateAttachment(savedPath)
                Toast.makeText(
                    context,
                    "📷 Foto adjuntada a la tarea correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val subtasksList = remember(task.id, task.subtasksJson) {
        mutableStateListOf<SubtaskItem>().apply {
            addAll(deserializeSubtasks(task.subtasksJson))
        }
    }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var autoCompleteWhenSubtasksDone by remember { mutableStateOf(true) }
    var titleErrorMsg by remember(task.id) { mutableStateOf("") }

    // Estado del Temporizador Pomodoro
    var selectedPreset by remember { mutableStateOf(PomodoroPreset.POMODORO_25) }
    var remainingSeconds by remember { mutableIntStateOf(PomodoroPreset.POMODORO_25.minutes * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedSessionSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            elapsedSessionSeconds++
            if (remainingSeconds == 0) {
                isTimerRunning = false
                val earnedMins = selectedPreset.minutes
                onAddFocusMinutes(earnedMins)
                elapsedSessionSeconds = 0
                Toast.makeText(
                    context,
                    "¡Sesión completada! Se sumaron +$earnedMins min de enfoque.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val activeAccent = taskAccent(editedColorHex, colorblindMode)
    val colorSymbol = taskColorSymbol(editedColorHex)
    val colorName = taskColorName(editedColorHex)
    val priorityEnum = TaskPriority.fromLevel(editedPriority)

    val allCategories = listOf(
        "Universidad",
        "Proyecto",
        "Personal",
        "Trabajo",
        "Salud",
        "Urgente"
    )

    fun syncSubtasksToDb() {
        val serialized = serializeSubtasks(subtasksList)
        val allDone = subtasksList.isNotEmpty() && subtasksList.all { it.isCompleted }
        val autoCompleteFlag = if (autoCompleteWhenSubtasksDone && allDone && !task.isCompleted) {
            true
        } else null
        onUpdateSubtasksLive(serialized, autoCompleteFlag)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bgColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==================== BARRA SUPERIOR DE ACCIONES ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = theme.cardColor,
                border = BorderStroke(1.dp, theme.surfaceBorder),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = theme.textPrimary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoTranslatedText(text = "Estudio de Tarea",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
                Text(
                    text = "ID #${task.id} • Enfoque: ${task.focusMinutes} min",
                    fontSize = 11.sp,
                    color = activeAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Botón de Lectura por Voz (TTS) de la tarea y sus pasos
                Surface(
                    shape = CircleShape,
                    color = theme.secondaryAccent,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            val stepsSpeech = if (subtasksList.isEmpty()) "" else
                                ". Pasos: " + subtasksList.joinToString(", ") { it.title }
                            val speech = "Tarea: $editedTitle. Categoría: $editedCategory. Notas: $editedDescription$stepsSpeech"
                            VoiceAndShareHelper.speakText(context, speech, languageCode)
                            Toast.makeText(context, "🔊 Leyendo tarea en voz alta...", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = "Leer en voz alta",
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Botón de Compartir por WhatsApp / Correo (Intent.ACTION_SEND)
                Surface(
                    shape = CircleShape,
                    color = theme.secondaryAccent,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            VoiceAndShareHelper.shareTaskViaIntent(
                                context,
                                task.copy(
                                    title = editedTitle,
                                    description = editedDescription,
                                    category = editedCategory,
                                    dueDate = editedDate,
                                    time = editedTime,
                                    priority = editedPriority,
                                    subtasksJson = serializeSubtasks(subtasksList)
                                )
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir tarea",
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (editedPinned) theme.primaryAccent else theme.cardColor,
                    border = BorderStroke(1.dp, theme.surfaceBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { editedPinned = !editedPinned }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Fijar arriba",
                            tint = if (editedPinned) Color.White else theme.textSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = theme.secondaryAccent,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            onDuplicateTask(
                                task.copy(
                                    title = "$editedTitle (Copia)",
                                    description = editedDescription,
                                    category = editedCategory,
                                    dueDate = editedDate,
                                    time = editedTime,
                                    colorHex = editedColorHex,
                                    priority = editedPriority,
                                    recurrence = editedRecurrence,
                                    subtasksJson = serializeSubtasks(subtasksList),
                                    isPinned = editedPinned
                                )
                            )
                            Toast.makeText(
                                context,
                                "Tarea duplicada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicar tarea",
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = DangerRed.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            Toast.makeText(
                                context,
                                "Tarea enviada a la Papelera de Reciclaje",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDelete()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Enviar a Papelera",
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ==================== TARJETA DE VISTA PREVIA EN VIVO ====================
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(2.dp, activeAccent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(activeAccent)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$colorSymbol $editedCategory",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${priorityEnum.badgeSymbol} ${priorityEnum.labelEs}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            color = if (task.isCompleted) AccentGreen else AccentOrange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (task.isCompleted) "✓ Completada" else "⏳ Pendiente",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = editedTitle.ifBlank { "Sin título" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatTaskDate(editedDate, languageCode)} • $editedTime • Color: $colorName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeAccent
                    )
                }
            }
        }

        // ==================== ETAPA KANBAN + DEPENDENCIAS ENTRE TAREAS ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ViewKanban,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoTranslatedText(text = "Etapa Kanban y Dependencias",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                val kanbanOptions = listOf(
                    0 to "📋 Por Hacer",
                    1 to "⚡ En Curso",
                    2 to "✅ Terminada"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    kanbanOptions.forEach { (statusCode, statusLabel) ->
                        val selected = editedKanbanStatus == statusCode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) activeAccent else theme.bgColor,
                            border = BorderStroke(
                                1.dp,
                                if (selected) activeAccent else theme.surfaceBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    editedKanbanStatus = statusCode
                                    onUpdateKanbanStatus(statusCode)
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else theme.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                val currentBlockerTask = remember(editedBlockerId, allUserTasks) {
                    editedBlockerId?.let { bId -> allUserTasks.find { it.id == bId } }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (currentBlockerTask != null && !currentBlockerTask.isCompleted)
                                Icons.Default.Lock
                            else
                                Icons.Default.Link,
                            contentDescription = null,
                            tint = if (currentBlockerTask != null && !currentBlockerTask.isCompleted)
                                DangerRed
                            else
                                activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(text = "Dependencia Secuencial",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = if (currentBlockerTask != null) {
                                    if (currentBlockerTask.isCompleted)
                                        "✓ Requisito cumplido: ${currentBlockerTask.title}"
                                    else
                                        "🔒 Bloqueada hasta completar: ${currentBlockerTask.title}"
                                } else {
                                    "Sin bloqueos (puede completarse en cualquier momento)"
                                },
                                fontSize = 11.sp,
                                color = if (currentBlockerTask != null && !currentBlockerTask.isCompleted)
                                    DangerRed
                                else
                                    theme.textSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (editedBlockerId != null) {
                            TextButton(
                                onClick = {
                                    editedBlockerId = null
                                    onUpdateBlocker(null)
                                }
                            ) {
                                AutoTranslatedText("Quitar", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                if (!hasProAccess) {
                                    onRequestProPaywall("Dependencias Secuenciales entre Tareas")
                                } else {
                                    showBlockerDialog = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasProAccess) "Elegir requisito" else "Requisito ⭐",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ==================== EVIDENCIA / FOTO ADJUNTA POR TAREA ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val tasksWithPhotosCount = remember(allUserTasks, task.id) {
                    allUserTasks.count {
                        !it.isDeleted && !it.attachmentImagePath.isNullOrBlank() && it.id != task.id
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(text = "Evidencia o Foto Adjunta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = if (hasProAccess)
                                    "Adjuntos fotográficos ilimitados (Pro activo)"
                                else
                                    "Plan Gratis: ${tasksWithPhotosCount.coerceAtMost(2)}/2 tareas con foto adjunta",
                                fontSize = 11.sp,
                                color = theme.textSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!editedAttachmentPath.isNullOrBlank()) {
                            TextButton(
                                onClick = {
                                    editedAttachmentPath = null
                                    onUpdateAttachment(null)
                                }
                            ) {
                                AutoTranslatedText("Quitar", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                if (!hasProAccess && tasksWithPhotosCount >= 2 && editedAttachmentPath.isNullOrBlank()) {
                                    onRequestProPaywall("Adjuntos Fotográficos Ilimitados")
                                } else {
                                    attachmentPickerLauncher.launch("image/*")
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (editedAttachmentPath.isNullOrBlank()) "Adjuntar Foto" else "Cambiar Foto",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (attachmentBitmap != null) {
                    Image(
                        bitmap = attachmentBitmap,
                        contentDescription = "Evidencia adjunta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }
        }

        // ==================== TEMPORIZADOR POMODORO / MODO ENFOQUE INTEGRADO ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(text = "Temporizador Pomodoro / Enfoque",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Acumulado en esta tarea: ${task.focusMinutes} minutos",
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PomodoroPreset.entries.forEach { preset ->
                        val selected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) activeAccent else theme.bgColor,
                            border = BorderStroke(1.dp, if (selected) activeAccent else theme.surfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isTimerRunning = false
                                    selectedPreset = preset
                                    remainingSeconds = preset.minutes * 60
                                    elapsedSessionSeconds = 0
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else theme.textPrimary
                                )
                            }
                        }
                    }
                }

                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                Text(
                    text = "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeAccent
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isTimerRunning = !isTimerRunning },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                    ) {
                        Icon(
                            if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerRunning) "Pausar" else "Iniciar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isTimerRunning = false
                            remainingSeconds = selectedPreset.minutes * 60
                            elapsedSessionSeconds = 0
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        AutoTranslatedText("Reiniciar", fontSize = 12.sp)
                    }

                    if (elapsedSessionSeconds >= 60) {
                        val minsToSave = (elapsedSessionSeconds / 60).coerceAtLeast(1)
                        FilledTonalButton(
                            onClick = {
                                isTimerRunning = false
                                onAddFocusMinutes(minsToSave)
                                elapsedSessionSeconds = 0
                                Toast.makeText(
                                    context,
                                    "Se registraron +$minsToSave min en esta tarea",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+$minsToSave min", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==================== SISTEMA DE SUBTAREAS (CHECKLIST JERÁRQUICO) ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val doneCount = subtasksList.count { it.isCompleted }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Subtareas ($doneCount/${subtasksList.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.textPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AutoTranslatedText(text = "Auto-completar",
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = autoCompleteWhenSubtasksDone,
                            onCheckedChange = { autoCompleteWhenSubtasksDone = it }
                        )
                    }
                }

                if (subtasksList.isNotEmpty()) {
                    val subProgress = doneCount.toFloat() / subtasksList.size.toFloat()
                    LinearProgressIndicator(
                        progress = { subProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = activeAccent,
                        trackColor = theme.surfaceBorder,
                        strokeCap = StrokeCap.Round
                    )
                }

                subtasksList.forEachIndexed { idx, item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.bgColor,
                        border = BorderStroke(1.dp, theme.surfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { checked ->
                                    subtasksList[idx] = item.copy(isCompleted = checked)
                                    syncSubtasksToDb()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = activeAccent)
                            )
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (item.isCompleted) theme.textSecondary else theme.textPrimary,
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    subtasksList.removeAt(idx)
                                    syncSubtasksToDb()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar subtarea",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        placeholder = {
                            AutoTranslatedText("Agregar paso o subtarea...",
                                fontSize = 13.sp,
                                color = theme.textSecondary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = appTextFieldColors(
                            theme = theme,
                            accent = activeAccent,
                            containerColor = theme.bgColor
                        )
                    )
                    Button(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                subtasksList.add(
                                    SubtaskItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = newSubtaskTitle.trim(),
                                        isCompleted = false
                                    )
                                )
                                newSubtaskTitle = ""
                                syncSubtasksToDb()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // ==================== SECCIÓN 1: EDITAR TÍTULO, NOTAS Y CATEGORÍA ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoTranslatedText(text = "Modificar Información de la Tarea",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = {
                        editedTitle = it
                        if (it.isNotBlank()) titleErrorMsg = ""
                    },
                    label = {
                        AutoTranslatedText("Título de la actividad *",
                            color = if (titleErrorMsg.isNotEmpty()) DangerRed else theme.textSecondary
                        )
                    },
                    isError = titleErrorMsg.isNotEmpty(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(
                        theme = theme,
                        accent = activeAccent,
                        isError = titleErrorMsg.isNotEmpty(),
                        containerColor = theme.bgColor
                    )
                )

                if (titleErrorMsg.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, DangerRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = titleErrorMsg,
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { AutoTranslatedText("Notas, pasos o descripción detallada", color = theme.textSecondary) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    colors = appTextFieldColors(
                        theme = theme,
                        accent = activeAccent,
                        containerColor = theme.bgColor
                    )
                )

                AutoTranslatedText(text = "Categoría de la tarea",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allCategories.forEach { cat ->
                        val selected = editedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) activeAccent else theme.bgColor,
                            border = BorderStroke(
                                1.dp,
                                if (selected) activeAccent else theme.surfaceBorder
                            ),
                            modifier = Modifier.clickable { editedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (selected) Color.White else theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== SECCIÓN: PRIORIDAD, RECURRENCIA Y RECORDATORIO ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoTranslatedText(text = "Prioridad y Repetición Automática",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { prio ->
                        val selected = editedPriority == prio.level
                        val pColor = priorityColor(prio.level, colorblindMode)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) pColor else theme.bgColor,
                            border = BorderStroke(1.dp, if (selected) pColor else theme.surfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { editedPriority = prio.level }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = prio.badgeSymbol,
                                    fontSize = 14.sp,
                                    color = if (selected) Color.White else pColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = prio.labelEs,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else theme.textPrimary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoTranslatedText(text = "Frecuencia de repetición (Hábito / Rutina)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskRecurrence.entries.forEach { rec ->
                        val selected = editedRecurrence == rec.code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) activeAccent else theme.bgColor,
                            border = BorderStroke(1.dp, if (selected) activeAccent else theme.surfaceBorder),
                            modifier = Modifier.clickable { editedRecurrence = rec.code }
                        ) {
                            Text(
                                text = rec.labelEs,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else theme.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(text = "Alarma y Recordatorio en Segundo Plano",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            AutoTranslatedText(text = "Programa AlarmManager para la fecha y hora elegidas",
                                fontSize = 11.sp,
                                color = theme.textSecondary
                            )
                        }
                    }
                    Switch(
                        checked = editedReminder,
                        onCheckedChange = {
                            editedReminder = it
                            if (it && notificationsEnabled) {
                                val updatedTask = task.copy(
                                    title = editedTitle.ifBlank { task.title },
                                    category = editedCategory,
                                    dueDate = editedDate,
                                    time = editedTime,
                                    priority = editedPriority,
                                    reminderEnabled = true
                                )
                                NotificationHelper.scheduleTaskReminder(context, updatedTask)
                                NotificationHelper.sendTaskNotification(
                                    context = context,
                                    notificationId = task.id,
                                    title = updatedTask.title,
                                    category = editedCategory,
                                    time = editedTime,
                                    priorityLevel = editedPriority
                                )
                            } else if (!it) {
                                NotificationHelper.cancelTaskReminder(context, task.id)
                            }
                        }
                    )
                }
            }
        }

        // ==================== SECCIÓN 2: FECHA, HORA Y ATAJOS RÁPIDOS ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoTranslatedText(text = "Programación de Fecha y Hora",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = theme.bgColor,
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parsed = runCatching { LocalDate.parse(editedDate) }
                                    .getOrDefault(LocalDate.now())
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val newIso = LocalDate.of(year, month + 1, day).toString()
                                        editedDate = newIso
                                        onUpdateSchedule(newIso, editedTime)
                                    },
                                    parsed.year,
                                    parsed.monthValue - 1,
                                    parsed.dayOfMonth
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                AutoTranslatedText(text = "Fecha",
                                    fontSize = 11.sp,
                                    color = theme.textSecondary
                                )
                                Text(
                                    text = formatTaskDate(editedDate, languageCode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.textPrimary
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = theme.bgColor,
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parsedTime = parseTaskTime(editedTime)
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        val formatted = formatTaskTime(hour, minute)
                                        editedTime = formatted
                                        onUpdateSchedule(editedDate, formatted)
                                    },
                                    parsedTime.hour,
                                    parsedTime.minute,
                                    false
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                AutoTranslatedText(text = "Hora",
                                    fontSize = 11.sp,
                                    color = theme.textSecondary
                                )
                                Text(
                                    text = editedTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.textPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickOptions = listOf(
                        "Hoy" to LocalDate.now(),
                        "Mañana" to LocalDate.now().plusDays(1),
                        "Próxima semana" to LocalDate.now().plusWeeks(1)
                    )
                    quickOptions.forEach { (label, targetDate) ->
                        val isCurrent = editedDate == targetDate.toString()
                        OutlinedButton(
                            onClick = {
                                editedDate = targetDate.toString()
                                onUpdateSchedule(editedDate, editedTime)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) activeAccent else theme.surfaceBorder
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) activeAccent else theme.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // ==================== SECCIÓN 3: PERSONALIZACIÓN DE COLOR Y DISEÑO ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
            border = BorderStroke(1.dp, theme.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoTranslatedText(text = "Diseño y Color en el Calendario",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                Text(
                    text = "Este color e icono ($colorSymbol $colorName) identificará tu tarea en la lista y debajo del día en el Calendario.",
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )

                TaskColorSelector(
                    theme = theme,
                    selectedHex = editedColorHex,
                    onSelectColor = { newHex ->
                        editedColorHex = newHex
                        onChangeColor(newHex)
                    },
                    colorblindMode = colorblindMode
                )
            }
        }

        // ==================== BOTONES PRINCIPALES DE GUARDADO Y ESTADO ====================
        Button(
            onClick = {
                if (editedTitle.isNotBlank()) {
                    onSaveFullChanges(
                        editedTitle,
                        editedDescription,
                        editedCategory,
                        editedDate,
                        editedTime,
                        editedColorHex,
                        editedPriority,
                        editedRecurrence,
                        serializeSubtasks(subtasksList),
                        editedPinned,
                        editedReminder
                    )
                    if (editedReminder && notificationsEnabled) {
                        val updatedTask = task.copy(
                            title = editedTitle,
                            category = editedCategory,
                            dueDate = editedDate,
                            time = editedTime,
                            priority = editedPriority,
                            reminderEnabled = true
                        )
                        NotificationHelper.scheduleTaskReminder(context, updatedTask)
                        NotificationHelper.sendTaskNotification(
                            context = context,
                            notificationId = task.id,
                            title = editedTitle,
                            category = editedCategory,
                            time = editedTime,
                            priorityLevel = editedPriority
                        )
                    } else if (!editedReminder) {
                        NotificationHelper.cancelTaskReminder(context, task.id)
                    }
                    Toast.makeText(
                        context,
                        "¡Cambios guardados en la tarea!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBack()
                } else {
                    titleErrorMsg = "⚠️ Debes escribir como mínimo el título de la tarea para guardar los cambios."
                    Toast.makeText(
                        context,
                        "El título de la tarea es obligatorio",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            AutoTranslatedText(text = "Guardar Todos los Cambios",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = {
                val activeBlocker = editedBlockerId?.let { bId ->
                    allUserTasks.find { it.id == bId && !it.isCompleted && !it.isDeleted }
                }
                if (!task.isCompleted && activeBlocker != null) {
                    Toast.makeText(
                        context,
                        "🔒 Completa primero '${activeBlocker.title}' antes de terminar esta tarea.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    onToggleComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                1.5.dp,
                if (task.isCompleted) AccentGreen else theme.primaryAccent
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (task.isCompleted) AccentGreen else theme.primaryAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (task.isCompleted)
                    "Tarea Completada (Toca para reabrir como Pendiente)"
                else
                    "Marcar Tarea como Completada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) AccentGreen else theme.primaryAccent
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal para seleccionar la tarea requisito (Dependencia Secuencial)
    if (showBlockerDialog) {
        val candidateTasks = remember(allUserTasks, task.id) {
            allUserTasks.filter { !it.isDeleted && it.id != task.id }
        }
        AlertDialog(
            onDismissRequest = { showBlockerDialog = false },
            containerColor = theme.cardColor,
            title = {
                AutoTranslatedText(text = "🔗 Seleccionar Tarea Requisito",
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Elige qué tarea debe completarse primero antes de poder finalizar '${editedTitle}':",
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                    if (candidateTasks.isEmpty()) {
                        AutoTranslatedText(text = "No hay otras tareas activas disponibles.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            candidateTasks.forEach { candidate ->
                                val isChosen = editedBlockerId == candidate.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChosen) activeAccent else theme.bgColor,
                                    border = BorderStroke(1.dp, theme.surfaceBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            editedBlockerId = candidate.id
                                            onUpdateBlocker(candidate.id)
                                            showBlockerDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = candidate.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) Color.White else theme.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = if (candidate.isCompleted) "✓ Lista" else "⏳ Pendiente",
                                            fontSize = 10.sp,
                                            color = if (isChosen) Color.White else theme.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockerDialog = false }) {
                    AutoTranslatedText("Cerrar", color = activeAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
