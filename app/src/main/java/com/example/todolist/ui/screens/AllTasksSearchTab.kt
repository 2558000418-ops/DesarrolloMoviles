package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.DefaultCollaborators
import com.example.todolist.model.DueDateFilter
import com.example.todolist.model.GroupByMode
import com.example.todolist.model.SortByMode
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TaskViewFormat
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.model.calculateSmartScore
import com.example.todolist.ui.components.TaskCardComponent
import com.example.todolist.ui.components.TaskDisplayOptionsSheet
import com.example.todolist.ui.components.UserAvatarComponent
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AccentOrange
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.CrimsonHighlight
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.appTextFieldColors
import com.example.todolist.ui.theme.priorityColor
import com.example.todolist.util.LocalizedStrings
import java.time.LocalDate

@Composable
fun AllTasksSearchTab(
    theme: AppThemePalette,
    user: UserAccount,
    tasks: List<TodoTask>,
    strings: LocalizedStrings,
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    highContrast: Boolean = false,
    dyslexiaFriendly: Boolean = false,
    largeTouchTargets: Boolean = false,
    hasProAccess: Boolean = false,
    onRequestProPaywall: (String) -> Unit = {},
    onJumpToCalendarTab: () -> Unit = {},
    onToggleTask: (TodoTask) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onTogglePin: (TodoTask) -> Unit = {},
    onMoveTaskUp: (TodoTask) -> Unit = {},
    onMoveTaskDown: (TodoTask) -> Unit = {},
    onMoveTaskKanban: (TodoTask, Int) -> Unit = { _, _ -> },
    onRestoreTask: (Int) -> Unit = {},
    onDeletePermanently: (Int) -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    onOpenDetail: (TodoTask) -> Unit,
    onOpenTopUserConfig: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedPriorityFilter by remember { mutableIntStateOf(-1) }
    var showTrashBin by remember { mutableStateOf(false) }

    // Estados del Panel Desplegable "Mostrar"
    var showDisplaySheet by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(TaskViewFormat.LIST) }
    var selectedGroupBy by remember { mutableStateOf(GroupByMode.NONE) }
    var selectedSortBy by remember { mutableStateOf(SortByMode.SMART) }
    var selectedAssigneeFilter by remember { mutableStateOf("Todos") }
    var selectedDueDateFilter by remember { mutableStateOf(DueDateFilter.ANY) }
    val collapsedGroups = remember { mutableStateMapOf<String, Boolean>() }
    val trashCopy = remember(strings.language) {
        when (strings.language) {
            com.example.todolist.model.AppLanguage.EN -> TrashCopy(
                "Trash", "Recycle Bin", "Restore tasks deleted by mistake or permanently remove them.",
                "Empty", "The trash is empty", "No date", "Restore", "Delete permanently"
            )
            com.example.todolist.model.AppLanguage.ZH_CN -> TrashCopy(
                "回收站", "回收站", "恢复误删的任务，或将其永久删除。",
                "清空", "回收站为空", "无日期", "恢复", "永久删除"
            )
            com.example.todolist.model.AppLanguage.ZH_TW -> TrashCopy(
                "垃圾桶", "垃圾桶", "還原誤刪的任務，或將其永久刪除。",
                "清空", "垃圾桶是空的", "無日期", "還原", "永久刪除"
            )
            else -> TrashCopy(
                "Papelera", "Papelera de Reciclaje", "Restaura tareas borradas por error o elimínalas definitivamente.",
                "Vaciar", "La papelera está vacía", "Sin fecha", "Restaurar", "Eliminar definitivamente"
            )
        }
    }

    val categories = listOf("Todas", "Universidad", "Proyecto", "Personal", "Trabajo", "Salud", "Urgente")
    val deletedTasks = tasks.filter { it.isDeleted }
    val activeTasks = tasks.filter { !it.isDeleted }
    val today = remember { LocalDate.now() }
    val todayIso = remember { today.toString() }
    val next7DaysIso = remember { today.plusDays(7).toString() }

    val availableAssignees = remember(activeTasks) {
        (DefaultCollaborators + activeTasks.map { it.assignee }.filter { it.isNotBlank() }).distinct()
    }

    val cardBorder: BorderStroke? = if (theme.borderStyle == BorderStyleMode.NONE && !highContrast) {
        null
    } else {
        BorderStroke(theme.borderStyle.widthDp.coerceAtLeast(0.8f).dp, theme.surfaceBorder)
    }

    val filteredTasks = remember(
        activeTasks,
        searchQuery,
        selectedCategory,
        selectedPriorityFilter,
        selectedSortBy,
        selectedAssigneeFilter,
        selectedDueDateFilter,
        todayIso,
        next7DaysIso
    ) {
        val matches = activeTasks.filter { task ->
            val matchesCategory = selectedCategory == "Todas" || task.category == selectedCategory
            val matchesPriority = selectedPriorityFilter == -1 || task.priority == selectedPriorityFilter
            val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true) ||
                task.assignee.contains(searchQuery, ignoreCase = true)
            val matchesAssignee = selectedAssigneeFilter == "Todos" ||
                task.assignee.equals(selectedAssigneeFilter, ignoreCase = true)
            val matchesDueDate = when (selectedDueDateFilter) {
                DueDateFilter.ANY -> true
                DueDateFilter.TODAY -> task.dueDate == todayIso
                DueDateFilter.NEXT_7_DAYS -> task.dueDate.isNotBlank() &&
                    task.dueDate >= todayIso && task.dueDate <= next7DaysIso
                DueDateFilter.OVERDUE -> !task.isCompleted &&
                    task.dueDate.isNotBlank() && task.dueDate < todayIso
                DueDateFilter.NO_DATE -> task.dueDate.isBlank()
            }
            matchesCategory && matchesPriority && matchesSearch && matchesAssignee && matchesDueDate
        }

        when (selectedSortBy) {
            SortByMode.SMART -> matches.sortedByDescending {
                calculateSmartScore(it, activeTasks, todayIso)
            }
            SortByMode.PRIORITY -> matches.sortedWith(
                compareByDescending<TodoTask> { it.isPinned }
                    .thenByDescending { it.priority }
                    .thenBy { it.isCompleted }
            )
            SortByMode.DUE_DATE -> matches.sortedWith(
                compareByDescending<TodoTask> { it.isPinned }
                    .thenBy { it.dueDate.ifBlank { "9999-99-99" } }
            )
            SortByMode.ALPHABETICAL -> matches.sortedWith(
                compareByDescending<TodoTask> { it.isPinned }
                    .thenBy { it.title.lowercase() }
            )
            SortByMode.MANUAL -> matches.sortedWith(
                compareByDescending<TodoTask> { it.isPinned }
                    .thenBy { it.sortOrder }
            )
        }
    }

    val groupedTasks = remember(filteredTasks, selectedGroupBy, todayIso) {
        when (selectedGroupBy) {
            GroupByMode.NONE -> emptyMap()
            GroupByMode.CATEGORY -> filteredTasks.groupBy { it.category }
            GroupByMode.PRIORITY -> filteredTasks.groupBy {
                val p = TaskPriority.fromLevel(it.priority)
                "${p.badgeSymbol} Prioridad ${p.labelEs}"
            }
            GroupByMode.DUE_DATE -> filteredTasks.groupBy { task ->
                when {
                    task.dueDate.isBlank() -> "📅 Sin fecha límite"
                    task.dueDate < todayIso -> "⚠️ Vencidas (${task.dueDate})"
                    task.dueDate == todayIso -> "🌟 Para Hoy ($todayIso)"
                    else -> "📆 ${task.dueDate}"
                }
            }
            GroupByMode.ASSIGNEE -> filteredTasks.groupBy { "👤 ${it.assignee}" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Barra Superior con botón de opciones de vista ("Mostrar") y Papelera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    showTrashBin -> "${trashCopy.title} (${deletedTasks.size})"
                    selectedFormat == TaskViewFormat.BOARD -> "Panel Kanban"
                    selectedFormat == TaskViewFormat.MATRIX -> "Matriz Eisenhower"
                    else -> strings.exploreTasks
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón que abre el Panel Desplegable "Mostrar" (Formato, Orden Inteligente y Filtros)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.primaryAccent,
                    modifier = Modifier.clickable {
                        showTrashBin = false
                        showDisplaySheet = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Mostrar opciones de vista",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${strings.displaySheetTitle}: ${selectedFormat.labelEs}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Botón de Papelera
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showTrashBin) DangerRed else theme.cardColor,
                    border = if (showTrashBin) null else cardBorder,
                    modifier = Modifier.clickable {
                        showTrashBin = !showTrashBin
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Papelera",
                            tint = if (showTrashBin) Color.White else DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${deletedTasks.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (showTrashBin) Color.White else DangerRed
                        )
                    }
                }

                UserAvatarComponent(
                    name = user.name,
                    imagePath = user.profileImagePath,
                    backgroundColor = theme.primaryAccent,
                    size = 36.dp,
                    fontSize = 15.sp,
                    onClick = onOpenTopUserConfig
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chips de estado activo de Orden / Agrupación / Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.secondaryAccent,
                modifier = Modifier.clickable { showDisplaySheet = true }
            ) {
                Text(
                    text = if (selectedSortBy == SortByMode.SMART)
                        "✨ Orden: Inteligente"
                    else
                        "Orden: ${selectedSortBy.labelEs}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (selectedGroupBy != GroupByMode.NONE) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.secondaryAccent,
                    modifier = Modifier.clickable { showDisplaySheet = true }
                ) {
                    Text(
                        text = "📂 Grupo: ${selectedGroupBy.labelEs}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (selectedAssigneeFilter != "Todos") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.primaryAccent,
                    modifier = Modifier.clickable { selectedAssigneeFilter = "Todos" }
                ) {
                    Text(
                        text = "👤 $selectedAssigneeFilter ✕",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (selectedDueDateFilter != DueDateFilter.ANY) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.primaryAccent,
                    modifier = Modifier.clickable { selectedDueDateFilter = DueDateFilter.ANY }
                ) {
                    Text(
                        text = "📅 ${selectedDueDateFilter.labelEs} ✕",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (showTrashBin) {
            // ==================== VISTA DE PAPELERA DE RECICLAJE ====================
            Card(
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trashCopy.heading,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = theme.textPrimary
                        )
                        Text(
                            text = trashCopy.description,
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }
                    if (deletedTasks.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onEmptyTrash,
                            border = BorderStroke(1.dp, DangerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(trashCopy.empty, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (deletedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trashCopy.emptyState,
                        color = theme.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 85.dp)
                ) {
                    items(deletedTasks, key = { "trash_${it.id}" }) { task ->
                        Card(
                            shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                            border = cardBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "${task.category} • ${task.dueDate.ifBlank { trashCopy.noDate }}",
                                        fontSize = 12.sp,
                                        color = theme.textSecondary
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalButton(
                                        onClick = { onRestoreTask(task.id) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.RestoreFromTrash,
                                            contentDescription = trashCopy.restore,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(trashCopy.restore, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { onDeletePermanently(task.id) }) {
                                        Icon(
                                            Icons.Default.DeleteForever,
                                            contentDescription = trashCopy.delete,
                                            tint = DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedFormat == TaskViewFormat.BOARD) {
            // ==================== VISTA PANEL KANBAN DE 3 COLUMNAS ====================
            val todoColumn = filteredTasks.filter { !it.isCompleted && it.kanbanStatus == 0 }
            val inProgressColumn = filteredTasks.filter { !it.isCompleted && it.kanbanStatus == 1 }
            val doneColumn = filteredTasks.filter { it.isCompleted || it.kanbanStatus == 2 }

            AutoTranslatedText(text = "Desliza horizontalmente y mueve tus tareas entre etapas con las flechas (← / →):",
                fontSize = 12.sp,
                color = theme.textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KanbanColumnCard(
                    theme = theme,
                    title = "📋 Por Hacer",
                    accentColor = AccentOrange,
                    tasks = todoColumn,
                    columnStatus = 0,
                    colorblindMode = colorblindMode,
                    onMoveKanban = onMoveTaskKanban,
                    onOpenDetail = onOpenDetail
                )
                KanbanColumnCard(
                    theme = theme,
                    title = "⚡ En Curso",
                    accentColor = theme.primaryAccent,
                    tasks = inProgressColumn,
                    columnStatus = 1,
                    colorblindMode = colorblindMode,
                    onMoveKanban = onMoveTaskKanban,
                    onOpenDetail = onOpenDetail
                )
                KanbanColumnCard(
                    theme = theme,
                    title = "✅ Terminado",
                    accentColor = AccentGreen,
                    tasks = doneColumn,
                    columnStatus = 2,
                    colorblindMode = colorblindMode,
                    onMoveKanban = onMoveTaskKanban,
                    onOpenDetail = onOpenDetail
                )
            }
        } else if (selectedFormat == TaskViewFormat.MATRIX) {
            // ==================== VISTA MATRIZ DE EISENHOWER (4 CUADRANTES 2x2) ====================
            val pendingOnly = filteredTasks.filter { !it.isCompleted }
            val q1DoFirst = pendingOnly.filter { it.priority >= 2 && (it.dueDate <= todayIso || it.dueDate.isBlank()) }
            val q2Schedule = pendingOnly.filter { it.priority >= 2 && it.dueDate > todayIso }
            val q3Delegate = pendingOnly.filter { it.priority < 2 && it.dueDate.isNotBlank() && it.dueDate <= todayIso }
            val q4Eliminate = pendingOnly.filter { it.priority < 2 && (it.dueDate.isBlank() || it.dueDate > todayIso) }

            AutoTranslatedText(text = "Clasificación estratégica de tareas pendientes según Urgencia e Importancia (toca una tarea para editarla):",
                fontSize = 12.sp,
                color = theme.textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EisenhowerQuadrantCard(
                            modifier = Modifier.weight(1f),
                            theme = theme,
                            title = "⚡ 1. Hacer Ahora",
                            subtitle = "Urgente + Importante",
                            accentColor = CrimsonHighlight,
                            tasks = q1DoFirst,
                            onOpenDetail = onOpenDetail
                        )
                        EisenhowerQuadrantCard(
                            modifier = Modifier.weight(1f),
                            theme = theme,
                            title = "📅 2. Planificar",
                            subtitle = "No Urgente + Importante",
                            accentColor = theme.primaryAccent,
                            tasks = q2Schedule,
                            onOpenDetail = onOpenDetail
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EisenhowerQuadrantCard(
                            modifier = Modifier.weight(1f),
                            theme = theme,
                            title = "🚀 3. Resolver Rápido",
                            subtitle = "Para Hoy + Media/Baja",
                            accentColor = AccentOrange,
                            tasks = q3Delegate,
                            onOpenDetail = onOpenDetail
                        )
                        EisenhowerQuadrantCard(
                            modifier = Modifier.weight(1f),
                            theme = theme,
                            title = "🌱 4. Algún Día",
                            subtitle = "Baja Prioridad / Futuro",
                            accentColor = AccentGreen,
                            tasks = q4Eliminate,
                            onOpenDetail = onOpenDetail
                        )
                    }
                }
            }
        } else {
            // ==================== VISTA LISTA CLÁSICA O AGRUPADA ====================
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        strings.searchPlaceholder(user.name),
                        color = theme.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = theme.primaryAccent
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(theme = theme, containerColor = theme.cardColor)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtro por Categorías
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val label = if (category == "Todas") strings.categoryAll else category
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) theme.primaryAccent else theme.cardColor,
                        border = if (!isSelected) cardBorder else null,
                        modifier = Modifier.clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else theme.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtro rápido por Prioridad
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(TaskPriority.entries.reversed()) { prio ->
                    val chosen = selectedPriorityFilter == prio.level
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (chosen) theme.primaryAccent else theme.cardColor,
                        border = if (chosen) null else cardBorder,
                        modifier = Modifier.clickable {
                            selectedPriorityFilter = if (chosen) -1 else prio.level
                        }
                    ) {
                        Text(
                            text = "${prio.badgeSymbol} ${prio.labelEs}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (chosen) Color.White else theme.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(if (largeTouchTargets) 16.dp else theme.spacingDp.dp),
                contentPadding = PaddingValues(bottom = 85.dp)
            ) {
                if (selectedGroupBy == GroupByMode.NONE) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val blockerTitle = remember(task.blockedByTaskId, activeTasks) {
                            task.blockedByTaskId?.let { bId ->
                                activeTasks.find { it.id == bId && !it.isCompleted }?.title
                            }
                        }
                        TaskCardComponent(
                            theme = theme,
                            task = task,
                            blockerTaskTitle = blockerTitle,
                            languageCode = strings.language.code,
                            colorblindMode = colorblindMode,
                            highContrast = highContrast,
                            dyslexiaFriendly = dyslexiaFriendly,
                            largeTouchTargets = largeTouchTargets,
                            showOrderingControls = selectedSortBy == SortByMode.MANUAL,
                            onToggle = { onToggleTask(task) },
                            onDelete = { onDeleteTask(task.id) },
                            onTogglePin = { onTogglePin(task) },
                            onMoveUp = { onMoveTaskUp(task) },
                            onMoveDown = { onMoveTaskDown(task) },
                            onClick = { onOpenDetail(task) }
                        )
                    }
                } else {
                    groupedTasks.forEach { (groupHeader, groupItems) ->
                        val isCollapsed = collapsedGroups[groupHeader] == true
                        item(key = "header_$groupHeader") {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.cardColor,
                                border = cardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        collapsedGroups[groupHeader] = !isCollapsed
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$groupHeader (${groupItems.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = theme.primaryAccent
                                    )
                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = null,
                                        tint = theme.primaryAccent
                                    )
                                }
                            }
                        }

                        if (!isCollapsed) {
                            items(groupItems, key = { "group_${groupHeader}_${it.id}" }) { task ->
                                val blockerTitle = remember(task.blockedByTaskId, activeTasks) {
                                    task.blockedByTaskId?.let { bId ->
                                        activeTasks.find { it.id == bId && !it.isCompleted }?.title
                                    }
                                }
                                TaskCardComponent(
                                    theme = theme,
                                    task = task,
                                    blockerTaskTitle = blockerTitle,
                                    languageCode = strings.language.code,
                                    colorblindMode = colorblindMode,
                                    highContrast = highContrast,
                                    dyslexiaFriendly = dyslexiaFriendly,
                                    largeTouchTargets = largeTouchTargets,
                                    showOrderingControls = selectedSortBy == SortByMode.MANUAL,
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
            }
        }
    }

    if (showDisplaySheet) {
        TaskDisplayOptionsSheet(
            theme = theme,
            selectedFormat = selectedFormat,
            selectedGroupBy = selectedGroupBy,
            selectedSortBy = selectedSortBy,
            selectedAssigneeFilter = selectedAssigneeFilter,
            selectedDueDateFilter = selectedDueDateFilter,
            availableAssignees = availableAssignees,
            onSelectFormat = { newFormat ->
                when (newFormat) {
                    TaskViewFormat.BOARD -> {
                        if (!hasProAccess) {
                            showDisplaySheet = false
                            onRequestProPaywall("Tablero Kanban de 3 Columnas")
                        } else {
                            selectedFormat = newFormat
                        }
                    }
                    TaskViewFormat.CALENDAR -> {
                        showDisplaySheet = false
                        onJumpToCalendarTab()
                    }
                    else -> {
                        selectedFormat = newFormat
                    }
                }
            },
            onSelectGroupBy = { selectedGroupBy = it },
            onSelectSortBy = { selectedSortBy = it },
            onSelectAssigneeFilter = { selectedAssigneeFilter = it },
            onSelectDueDateFilter = { selectedDueDateFilter = it },
            onDismiss = { showDisplaySheet = false }
        )
    }
}

private data class TrashCopy(
    val title: String,
    val heading: String,
    val description: String,
    val empty: String,
    val emptyState: String,
    val noDate: String,
    val restore: String,
    val delete: String
)

@Composable
private fun KanbanColumnCard(
    theme: AppThemePalette,
    title: String,
    accentColor: Color,
    tasks: List<TodoTask>,
    columnStatus: Int,
    colorblindMode: ColorblindMode,
    onMoveKanban: (TodoTask, Int) -> Unit,
    onOpenDetail: (TodoTask) -> Unit
) {
    Card(
        modifier = Modifier
            .width(268.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor
                    ) {
                        Text(
                            text = "${tasks.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AutoTranslatedText(text = "Sin tareas en esta etapa",
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(tasks, key = { "kanban_${it.id}" }) { t ->
                        val pEnum = TaskPriority.fromLevel(t.priority)
                        val pColor = priorityColor(t.priority, colorblindMode)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.bgColor,
                            border = BorderStroke(1.dp, theme.surfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDetail(t) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${pEnum.badgeSymbol} ${t.category}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = pColor
                                    )
                                    if (!t.assignee.equals("Solo a mí", ignoreCase = true)) {
                                        Text(
                                            text = "👤 ${t.assignee}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.primaryAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = t.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = t.dueDate.ifBlank { "Sin fecha" },
                                        fontSize = 10.sp,
                                        color = theme.textSecondary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (columnStatus > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = theme.secondaryAccent,
                                                modifier = Modifier.clickable {
                                                    onMoveKanban(t, columnStatus - 1)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowBack,
                                                    contentDescription = "Mover a la izquierda",
                                                    tint = theme.primaryAccent,
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(14.dp)
                                                )
                                            }
                                        }
                                        if (columnStatus < 2) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = theme.primaryAccent,
                                                modifier = Modifier.clickable {
                                                    onMoveKanban(t, columnStatus + 1)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowForward,
                                                    contentDescription = "Mover a la derecha",
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(14.dp)
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
}

@Composable
private fun EisenhowerQuadrantCard(
    modifier: Modifier,
    theme: AppThemePalette,
    title: String,
    subtitle: String,
    accentColor: Color,
    tasks: List<TodoTask>,
    onOpenDetail: (TodoTask) -> Unit
) {
    Card(
        modifier = modifier.heightIn(min = 185.dp),
        shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Text(
                        text = "$subtitle (${tasks.size})",
                        fontSize = 10.sp,
                        color = theme.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (tasks.isEmpty()) {
                AutoTranslatedText(text = "Sin tareas en este cuadrante",
                    fontSize = 11.sp,
                    color = theme.textSecondary
                )
            } else {
                tasks.take(5).forEach { t ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.bgColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onOpenDetail(t) }
                    ) {
                        Text(
                            text = "• ${t.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
                if (tasks.size > 5) {
                    Text(
                        text = "+${tasks.size - 5} más...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
