package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.TodoTask
import com.example.todolist.ui.components.TaskCardComponent
import com.example.todolist.ui.components.TaskColorSelector
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.taskAccent
import com.example.todolist.ui.theme.taskColorSymbol
import com.example.todolist.util.LocalizedStrings
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.formatTaskDate
import com.example.todolist.util.localeForLanguageCode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarTasksTab(
    theme: AppThemePalette,
    tasks: List<TodoTask>,
    strings: LocalizedStrings,
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    highContrast: Boolean = false,
    dyslexiaFriendly: Boolean = false,
    largeTouchTargets: Boolean = false,
    onToggleTask: (TodoTask) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onTogglePin: (TodoTask) -> Unit = {},
    onOpenDetail: (TodoTask) -> Unit,
    onChangeTaskColor: (Int, String) -> Unit,
    onAssignDateToTask: (TodoTask, LocalDate) -> Unit = { _, _ -> },
    onCreateTaskForDate: (LocalDate) -> Unit
) {
    val activeTasks = tasks.filter { !it.isDeleted }
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var showUnscheduled by remember { mutableStateOf(false) }

    val firstDayOffset = visibleMonth.atDay(1).dayOfWeek.value - 1
    val cellsNeeded = firstDayOffset + visibleMonth.lengthOfMonth()
    val weeks = (0 until ((cellsNeeded + 6) / 7) * 7)
        .map { it - firstDayOffset + 1 }
        .chunked(7)

    val selectedIso = selectedDate.toString()
    val pendingForSelectedDay = activeTasks.filter { it.dueDate == selectedIso && !it.isCompleted }
    val completedForSelectedDay = activeTasks.filter { it.dueDate == selectedIso && it.isCompleted }
    val unscheduled = activeTasks.filter { it.dueDate.isBlank() }

    val monthPrefix = visibleMonth.toString()
    val monthTasksCount = activeTasks.count { it.dueDate.startsWith(monthPrefix) && !it.isCompleted }
    val monthCompletedCount = activeTasks.count { it.dueDate.startsWith(monthPrefix) && it.isCompleted }

    val locale = localeForLanguageCode(strings.language.code)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.calendarTitle,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = strings.calendarSubtitle,
                        fontSize = 13.sp,
                        color = theme.textSecondary
                    )
                }

                FilledTonalButton(
                    onClick = {
                        selectedDate = today
                        visibleMonth = YearMonth.from(today)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = theme.secondaryAccent,
                        contentColor = theme.primaryAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.goToToday,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tarjeta Principal del Calendario Mensual (Estilo Google Calendar)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(
                    if (highContrast) 2.dp else 1.dp,
                    theme.surfaceBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = theme.bgColor,
                            border = BorderStroke(1.dp, theme.surfaceBorder),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    visibleMonth = visibleMonth.minusMonths(1)
                                    selectedDate = visibleMonth.atDay(1)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Mes anterior",
                                    tint = theme.primaryAccent
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = visibleMonth.atDay(1)
                                    .format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
                                    .replaceFirstChar { it.titlecase(locale) },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "$monthTasksCount pendientes · $monthCompletedCount completadas este mes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = theme.bgColor,
                            border = BorderStroke(1.dp, theme.surfaceBorder),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    visibleMonth = visibleMonth.plusMonths(1)
                                    selectedDate = visibleMonth.atDay(1)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Mes siguiente",
                                    tint = theme.primaryAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.bgColor)
                            .padding(vertical = 8.dp)
                    ) {
                        listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM").forEach { label ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.forEach { dayNumber ->
                                if (dayNumber !in 1..visibleMonth.lengthOfMonth()) {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(74.dp)
                                    )
                                } else {
                                    val date = visibleMonth.atDay(dayNumber)
                                    val dateIso = date.toString()
                                    val isSelected = date == selectedDate
                                    val isToday = date == today

                                    val dayTasks = activeTasks.filter { it.dueDate == dateIso }
                                    val dayPending = dayTasks.filter { !it.isCompleted }
                                    val dayCompleted = dayTasks.filter { it.isCompleted }

                                    val cellBorderColor = when {
                                        isSelected -> theme.primaryAccent
                                        isToday -> theme.primaryAccent.copy(alpha = 0.6f)
                                        dayPending.isNotEmpty() -> taskAccent(
                                            dayPending.first().colorHex,
                                            colorblindMode
                                        ).copy(alpha = 0.45f)
                                        else -> theme.surfaceBorder.copy(alpha = 0.6f)
                                    }

                                    val cellBgColor = when {
                                        isSelected -> theme.primaryAccent.copy(alpha = 0.14f)
                                        isToday -> theme.secondaryAccent.copy(alpha = 0.65f)
                                        else -> theme.bgColor.copy(alpha = 0.55f)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = cellBgColor,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.2.dp else 1.dp,
                                            color = cellBorderColor
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 2.dp)
                                            .height(74.dp)
                                            .clickable { selectedDate = date }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 3.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> theme.primaryAccent
                                                            isToday -> theme.primaryAccent.copy(alpha = 0.22f)
                                                            else -> Color.Transparent
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || isToday || dayTasks.isNotEmpty())
                                                        FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> theme.primaryAccent
                                                        else -> theme.textPrimary
                                                    }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            if (dayPending.isNotEmpty()) {
                                                val firstTask = dayPending.first()
                                                val firstColor = taskAccent(
                                                    firstTask.colorHex,
                                                    colorblindMode
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(15.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(firstColor)
                                                        .padding(horizontal = 3.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = if (colorblindMode != ColorblindMode.NONE)
                                                            "${taskColorSymbol(firstTask.colorHex)}${firstTask.title}"
                                                        else
                                                            firstTask.title,
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Clip
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(3.dp))

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    dayPending.take(4).forEach { t ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    taskAccent(
                                                                        t.colorHex,
                                                                        colorblindMode
                                                                    )
                                                                )
                                                        )
                                                    }
                                                    if (dayPending.size > 4) {
                                                        Text(
                                                            text = "+${dayPending.size - 4}",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = theme.primaryAccent
                                                        )
                                                    }
                                                }
                                            } else if (dayCompleted.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(14.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(AccentGreen.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "✓ ${dayCompleted.size}",
                                                        color = AccentGreen,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== AGENDA DEL DÍA SELECCIONADO ====================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.5.dp, theme.primaryAccent.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.primaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedDate.dayOfMonth.toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formatTaskDate(selectedIso, strings.language.code),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = strings.pendingAndCompletedCount(
                                    pendingForSelectedDay.size,
                                    completedForSelectedDay.size
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.secondaryAccent,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    VoiceAndShareHelper.shareDayAgendaViaIntent(
                                        context = context,
                                        dateFormatted = formatTaskDate(selectedIso, strings.language.code),
                                        dayTasks = pendingForSelectedDay + completedForSelectedDay
                                    )
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Compartir agenda del día",
                                    tint = theme.primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { onCreateTaskForDate(selectedDate) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.addTaskShort,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (pendingForSelectedDay.isEmpty() && completedForSelectedDay.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = BorderStroke(1.dp, theme.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.noTasksForSelectedDay,
                            color = theme.textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(pendingForSelectedDay, key = { "pending_${it.id}" }) { task ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.dp, theme.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskCardComponent(
                        theme = theme,
                        task = task,
                        languageCode = strings.language.code,
                        colorblindMode = colorblindMode,
                        highContrast = highContrast,
                        dyslexiaFriendly = dyslexiaFriendly,
                        largeTouchTargets = largeTouchTargets,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onTogglePin = { onTogglePin(task) },
                        onClick = { onOpenDetail(task) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AutoTranslatedText(text = "Color:",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            TaskColorSelector(
                                theme = theme,
                                selectedHex = task.colorHex,
                                onSelectColor = { hex -> onChangeTaskColor(task.id, hex) },
                                compact = true,
                                colorblindMode = colorblindMode
                            )
                        }
                        TextButton(onClick = { onOpenDetail(task) }) {
                            AutoTranslatedText(text = "Editar tarea →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryAccent
                            )
                        }
                    }
                }
            }
        }

        if (completedForSelectedDay.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.completedHeader} (${completedForSelectedDay.size})",
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            items(completedForSelectedDay, key = { "completed_${it.id}" }) { task ->
                TaskCardComponent(
                    theme = theme,
                    task = task,
                    languageCode = strings.language.code,
                    colorblindMode = colorblindMode,
                    highContrast = highContrast,
                    dyslexiaFriendly = dyslexiaFriendly,
                    largeTouchTargets = largeTouchTargets,
                    onToggle = { onToggleTask(task) },
                    onDelete = { onDeleteTask(task.id) },
                    onTogglePin = { onTogglePin(task) },
                    onClick = { onOpenDetail(task) }
                )
            }
        }

        if (unscheduled.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = BorderStroke(1.dp, theme.surfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showUnscheduled = !showUnscheduled },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = theme.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tareas sin fecha asignada (${unscheduled.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = theme.textPrimary
                                )
                            }
                            Text(
                                text = if (showUnscheduled) "Ocultar" else "Mostrar",
                                color = theme.primaryAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (showUnscheduled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            unscheduled.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${task.title}",
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    FilledTonalButton(
                                        onClick = { onAssignDateToTask(task, selectedDate) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.EventAvailable,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Asignar al día ${selectedDate.dayOfMonth}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
