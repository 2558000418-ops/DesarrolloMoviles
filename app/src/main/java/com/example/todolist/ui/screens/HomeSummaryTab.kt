package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import android.app.Activity
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.ui.components.TaskCardComponent
import com.example.todolist.ui.components.UserAvatarComponent
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AccentOrange
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.appTextFieldColors
import com.example.todolist.util.LocalizedStrings
import com.example.todolist.util.ParsedSmartTask
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.localeForLanguageCode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeSummaryTab(
    theme: AppThemePalette,
    user: UserAccount,
    tasks: List<TodoTask>,
    strings: LocalizedStrings,
    notificationsEnabled: Boolean,
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    highContrast: Boolean = false,
    dyslexiaFriendly: Boolean = false,
    reduceMotion: Boolean = false,
    largeTouchTargets: Boolean = false,
    zenFocusMode: Boolean = false,
    streakDays: Int = 1,
    privacyBlurMode: Boolean = false,
    hasProAccess: Boolean = false,
    onRequestProPaywall: (String) -> Unit = {},
    onQuickCreateSmartTask: (ParsedSmartTask) -> Unit = {},
    onToggleZenMode: (Boolean) -> Unit = {},
    onRescheduleOverdueTasks: () -> Int = { 0 },
    onToggleTask: (TodoTask) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onTogglePin: (TodoTask) -> Unit = {},
    onMoveTaskUp: (TodoTask) -> Unit = {},
    onMoveTaskDown: (TodoTask) -> Unit = {},
    onOpenDetail: (TodoTask) -> Unit,
    onOpenTopUserConfig: () -> Unit
) {
    val context = LocalContext.current
    val activeTasks = tasks.filter { !it.isDeleted }
    val today = remember { LocalDate.now() }
    val todayIso = remember { today.toString() }
    var selectedDayIndex by remember { mutableIntStateOf(today.dayOfWeek.value - 1) }
    var showAllDays by remember { mutableStateOf(true) }
    var showReorderControls by remember { mutableStateOf(false) }
    var showAnalyticsPanel by remember { mutableStateOf(false) }
    var smartInputText by remember { mutableStateOf("") }

    // Launcher para Dictado por Voz Nativo (SpeechToText)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                smartInputText = spoken
            }
        }
    }

    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val weekDates = (0..6).map { monday.plusDays(it.toLong()) }
    val locale = localeForLanguageCode(strings.language.code)
    val days = weekDates.map { day ->
        day.format(DateTimeFormatter.ofPattern("EEE", locale)) to
            day.dayOfMonth.toString()
    }

    val overdueTasks = remember(activeTasks, todayIso) {
        activeTasks.filter { !it.isCompleted && it.dueDate.isNotBlank() && it.dueDate < todayIso }
    }

    val visibleTasks = remember(activeTasks, showAllDays, selectedDayIndex, zenFocusMode) {
        val base = if (showAllDays) activeTasks else
            activeTasks.filter { it.dueDate == weekDates[selectedDayIndex].toString() }

        if (zenFocusMode) {
            base.filter { !it.isCompleted }
                .sortedWith(
                    compareByDescending<TodoTask> { it.isPinned }
                        .thenByDescending { it.priority }
                        .thenBy { it.sortOrder }
                )
                .take(3)
        } else {
            base
        }
    }

    val totalCount = activeTasks.size
    val completedCount = activeTasks.count { it.isCompleted }
    val totalFocusMinutes = remember(activeTasks) { activeTasks.sumOf { it.focusMinutes } }
    val xpPoints = completedCount * 25 + totalFocusMinutes * 2
    val userLevelTitle = when {
        xpPoints >= 300 -> "Nivel 4 • Maestro del Tiempo 🏆"
        xpPoints >= 150 -> "Nivel 3 • Estratega Pro ⚡"
        xpPoints >= 50 -> "Nivel 2 • Organizador Activo 🌟"
        else -> "Nivel 1 • Iniciador Productivo 🌱"
    }

    val rawProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 600),
        label = "ProgressAnimation"
    )
    val displayProgress = if (reduceMotion) rawProgress else animatedProgress
    val percentageInt = (rawProgress * 100).toInt()

    val cardBorder: BorderStroke? = if (theme.borderStyle == BorderStyleMode.NONE && !highContrast) {
        null
    } else {
        BorderStroke(theme.borderStyle.widthDp.coerceAtLeast(0.8f).dp, theme.surfaceBorder)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(if (largeTouchTargets) 18.dp else theme.spacingDp.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 95.dp)
    ) {
        // Barra Superior + Botón de Lectura por Voz (TTS) + Perfil
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.helloUser}, ${user.name} • ${strings.language.flagEmoji} ${strings.language.displayName}",
                        fontSize = 13.sp,
                        color = theme.primaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.todaySummary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = theme.secondaryAccent,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                val pendingCount = totalCount - completedCount
                                val speech = "Hola ${user.name}. Tienes $pendingCount tareas pendientes y $completedCount completadas. Tu avance es del $percentageInt por ciento y llevas $streakDays días de racha."
                                VoiceAndShareHelper.speakText(context, speech, strings.language.code)
                                Toast.makeText(context, "🔊 Leyendo resumen en voz alta...", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Escuchar resumen del día",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = theme.cardColor,
                        border = cardBorder,
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onOpenTopUserConfig() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive
                                else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (notificationsEnabled) theme.primaryAccent else theme.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            UserAvatarComponent(
                                name = user.name,
                                imagePath = user.profileImagePath,
                                backgroundColor = theme.primaryAccent,
                                size = 34.dp,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Usuario, Foto y Preferencias",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== BARRA DE CREACIÓN RÁPIDA INTELIGENTE (NLP + DICTADO POR VOZ) ====================
        item {
            Card(
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = cardBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoTranslatedText(text = "Creación Inteligente NLP o por Voz (#Categoría !prioridad mañana 16:00)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = smartInputText,
                            onValueChange = { smartInputText = it },
                            placeholder = {
                                AutoTranslatedText("Ej: Entregar reporte mañana 16:00 #Universidad !critica",
                                    fontSize = 11.sp,
                                    color = theme.textSecondary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = appTextFieldColors(
                                theme = theme,
                                accent = theme.primaryAccent,
                                containerColor = theme.bgColor
                            )
                        )

                        // Botón de Micrófono (SpeechToText)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.secondaryAccent,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    runCatching {
                                        speechRecognizerLauncher.launch(
                                            VoiceAndShareHelper.createSpeechRecognizerIntent(strings.language.code)
                                        )
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Reconocimiento de voz no disponible en este dispositivo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Dictar tarea por voz",
                                    tint = theme.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Botón Crear con Parser NLP
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.primaryAccent,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    if (smartInputText.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "⚠️ Escribe o dicta al menos el título de la tarea",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val parsed = VoiceAndShareHelper.parseSmartTaskInput(smartInputText)
                                        onQuickCreateSmartTask(parsed)
                                        smartInputText = ""
                                        Toast.makeText(
                                            context,
                                            "✨ Creada: '${parsed.cleanTitle}' [${parsed.category} • ${parsed.dueDate} ${parsed.time}]",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Crear tarea inteligente",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Detector Inteligente de Tareas Vencidas (Overdue)
        if (overdueTasks.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.5.dp, DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ Tienes ${overdueTasks.size} tarea(s) de días pasados sin completar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DangerRed
                            )
                            AutoTranslatedText(text = "Muévelas a la agenda de Hoy con un solo toque",
                                fontSize = 11.sp,
                                color = theme.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val moved = onRescheduleOverdueTasks()
                                Toast.makeText(
                                    context,
                                    "¡$moved tareas reprogramadas para Hoy!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.EventRepeat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoTranslatedText("Mover a Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Banner de Modo Enfoque Zen / TDAH + Botón de Estadísticas y Mapa de Calor 90 Días
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (zenFocusMode) theme.primaryAccent else theme.cardColor,
                    border = if (zenFocusMode) null else cardBorder,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleZenMode(!zenFocusMode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = if (zenFocusMode) Color.White else theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (zenFocusMode) "Modo Zen Activo" else "Modo Enfoque Zen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (zenFocusMode) Color.White else theme.textPrimary
                            )
                            Text(
                                text = if (zenFocusMode) "Top 3 urgentes" else "Sin distracciones",
                                fontSize = 10.sp,
                                color = if (zenFocusMode) Color.White.copy(alpha = 0.85f) else theme.textSecondary
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (showAnalyticsPanel) theme.primaryAccent else theme.cardColor,
                    border = if (showAnalyticsPanel) null else cardBorder,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showAnalyticsPanel = !showAnalyticsPanel }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (showAnalyticsPanel) Color.White else AccentOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🔥 Racha: $streakDays día(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (showAnalyticsPanel) Color.White else theme.textPrimary
                            )
                            Text(
                                text = "$xpPoints XP • Mapa 90d",
                                fontSize = 10.sp,
                                color = if (showAnalyticsPanel) Color.White.copy(alpha = 0.85f) else theme.primaryAccent
                            )
                        }
                    }
                }
            }
        }

        // ==================== PANEL DE ANALÍTICA Y MAPA DE CALOR DE 90 DÍAS ====================
        if (showAnalyticsPanel) {
            item {
                Card(
                    shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = cardBorder,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = userLevelTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "$xpPoints Puntos XP • ⏱️ $totalFocusMinutes min de enfoque Pomodoro",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = theme.primaryAccent
                            )
                        }

                        HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                        // Mapa de Calor de Constancia de 90 Días (13 semanas x 7 días estilo GitHub)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AutoTranslatedText(text = "🔥 Mapa de Calor de Constancia (Últimos 90 Días)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.textPrimary
                                )
                                if (!hasProAccess) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.secondaryAccent,
                                        modifier = Modifier.clickable {
                                            onRequestProPaywall("Mapa de Calor de 90 Días")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(11.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            AutoTranslatedText("PRO / 24h Gratis", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryAccent)
                                        }
                                    }
                                }
                            }

                            if (!hasProAccess) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = theme.bgColor,
                                    border = cardBorder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onRequestProPaywall("Mapa de Calor de 90 Días") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AutoTranslatedText(text = "Desbloquea gratis por 24h o con ToDoList Pro tu cuadrícula de constancia de 90 días.",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AutoTranslatedText(text = "Desbloquear →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = theme.primaryAccent
                                        )
                                    }
                                }
                            } else {
                                // Cuadrícula de 13 semanas x 7 días (91 días)
                                val completedCountsByDate = remember(activeTasks) {
                                    val map = mutableMapOf<String, Int>()
                                    activeTasks.filter { it.isCompleted }.forEach { t ->
                                        val key = t.completedDate?.ifBlank { t.dueDate } ?: t.dueDate
                                        if (key.isNotBlank()) {
                                            map[key] = (map[key] ?: 0) + 1
                                        }
                                    }
                                    map
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (weekOffset in 12 downTo 0) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            for (dayInWeek in 0..6) {
                                                val daysAgo = weekOffset * 7 + (6 - dayInWeek)
                                                val cellDate = today.minusDays(daysAgo.toLong()).toString()
                                                val count = completedCountsByDate[cellDate] ?: 0
                                                val cellColor = when {
                                                    count >= 4 -> theme.primaryAccent
                                                    count == 3 -> theme.primaryAccent.copy(alpha = 0.78f)
                                                    count == 2 -> theme.primaryAccent.copy(alpha = 0.52f)
                                                    count == 1 -> theme.primaryAccent.copy(alpha = 0.30f)
                                                    else -> theme.surfaceBorder.copy(alpha = 0.18f)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(cellColor)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AutoTranslatedText("Menos ", fontSize = 9.sp, color = theme.textSecondary)
                                    listOf(0.18f, 0.30f, 0.52f, 0.78f, 1.0f).forEach { alpha ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(theme.primaryAccent.copy(alpha = alpha))
                                        )
                                    }
                                    AutoTranslatedText(" Más", fontSize = 9.sp, color = theme.textSecondary)
                                }
                            }
                        }

                        HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                        AutoTranslatedText(text = "Distribución por Categoría",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )

                        val categoriesGroup = activeTasks.groupBy { it.category }
                        if (categoriesGroup.isEmpty()) {
                            AutoTranslatedText(text = "Agrega tareas para visualizar tus métricas.",
                                fontSize = 11.sp,
                                color = theme.textSecondary
                            )
                        } else {
                            categoriesGroup.forEach { (catName, catTasks) ->
                                val catDone = catTasks.count { it.isCompleted }
                                val catRatio = if (totalCount > 0) catTasks.size.toFloat() / totalCount.toFloat() else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$catName (${catTasks.size} tareas • $catDone listas)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary
                                        )
                                        Text(
                                            text = "${(catRatio * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.primaryAccent
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { catRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = theme.primaryAccent,
                                        trackColor = theme.surfaceBorder.copy(alpha = 0.25f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!zenFocusMode) {
            // Calendario Horizontal Semanal
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days.size) { index ->
                        val (dayName, dayNumber) = days[index]
                        val isSelected = selectedDayIndex == index && !showAllDays
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) theme.primaryAccent else theme.cardColor
                            ),
                            border = if (!isSelected) cardBorder else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 1.dp),
                            modifier = Modifier
                                .width(62.dp)
                                .clickable {
                                    selectedDayIndex = index
                                    showAllDays = false
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else theme.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayNumber,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else theme.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta de Progreso Circular
            item {
                Card(
                    shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = cardBorder,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.progressOf} ${user.name}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.completedOutOf(completedCount, totalCount),
                                fontSize = 14.sp,
                                color = theme.textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = theme.secondaryAccent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (percentageInt == 100 && totalCount > 0)
                                        strings.allCompletedBadge
                                    else
                                        userLevelTitle,
                                    color = theme.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { displayProgress },
                                modifier = Modifier.size(110.dp),
                                color = theme.primaryAccent,
                                strokeWidth = 10.dp,
                                trackColor = theme.surfaceBorder.copy(alpha = 0.25f),
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$percentageInt%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Cabecera de Lista + Botones de Ver Todas y Reordenar Manualmente
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        zenFocusMode -> "Top 3 Tareas Prioritarias"
                        showAllDays -> strings.yourPrivateTasks
                        else -> strings.tasksOfTheDay
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showReorderControls = !showReorderControls }) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Reordenar tareas",
                            tint = if (showReorderControls) theme.primaryAccent else theme.textSecondary
                        )
                    }
                    if (!zenFocusMode) {
                        TextButton(onClick = { showAllDays = true }) {
                            Text(
                                text = if (showAllDays) strings.registeredCount(activeTasks.size) else strings.viewAll,
                                fontSize = 12.sp,
                                color = theme.primaryAccent
                            )
                        }
                    }
                }
            }
        }

        if (visibleTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (showAllDays) strings.noTasksRegistered else strings.noTasksForDay,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            strings.pressPlusToAdd,
                            fontSize = 13.sp,
                            color = theme.textSecondary
                        )
                    }
                }
            }
        } else {
            items(visibleTasks, key = { it.id }) { task ->
                val maskedTask = if (privacyBlurMode) {
                    task.copy(title = "🔒 Tarea Privada #${task.id}")
                } else task

                val blockerTitle = remember(task.blockedByTaskId, activeTasks) {
                    task.blockedByTaskId?.let { bId ->
                        activeTasks.firstOrNull { it.id == bId && !it.isCompleted }?.title
                    }
                }

                TaskCardComponent(
                    theme = theme,
                    task = maskedTask,
                    languageCode = strings.language.code,
                    colorblindMode = colorblindMode,
                    highContrast = highContrast,
                    dyslexiaFriendly = dyslexiaFriendly,
                    largeTouchTargets = largeTouchTargets,
                    showOrderingControls = showReorderControls,
                    blockerTaskTitle = blockerTitle,
                    onToggle = { onToggleTask(task) },
                    onDelete = { onDeleteTask(task.id) },
                    onTogglePin = { onTogglePin(task) },
                    onMoveUp = { onMoveTaskUp(task) },
                    onMoveDown = { onMoveTaskDown(task) },
                    onClick = { onOpenDetail(task) }
                )
            }
        }
    }
}
