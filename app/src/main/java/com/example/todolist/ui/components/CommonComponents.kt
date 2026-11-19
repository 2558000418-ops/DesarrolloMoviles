package com.example.todolist.ui.components

import com.example.todolist.ui.components.AutoTranslatedText

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.DefaultCollaborators
import com.example.todolist.model.DueDateFilter
import com.example.todolist.model.GroupByMode
import com.example.todolist.model.SortByMode
import com.example.todolist.model.SubscriptionPlan
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TaskRecurrence
import com.example.todolist.model.TaskViewFormat
import com.example.todolist.model.TodoTask
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.CrimsonHighlight
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.TaskColorChoices
import com.example.todolist.ui.theme.adaptColorForColorblindMode
import com.example.todolist.ui.theme.priorityColor
import com.example.todolist.ui.theme.taskAccent
import com.example.todolist.ui.theme.taskColorName
import com.example.todolist.ui.theme.taskColorSymbol
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.formatTaskDate
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun UserAvatarComponent(
    name: String,
    imagePath: String?,
    backgroundColor: Color,
    size: Dp,
    fontSize: TextUnit,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bitmap = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) {
            val file = File(imagePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } else null
    }

    Box(
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de perfil de $name",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = name.take(1).uppercase().ifEmpty { "U" },
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar foto",
                    tint = backgroundColor,
                    modifier = Modifier.size(size * 0.20f)
                )
            }
        }
    }
}

@Composable
fun TaskColorSelector(
    theme: AppThemePalette,
    selectedHex: String,
    onSelectColor: (String) -> Unit,
    compact: Boolean = false,
    colorblindMode: ColorblindMode = ColorblindMode.NONE
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskColorChoices.forEach { option ->
            val chosen = selectedHex.equals(option.hex, ignoreCase = true)
            val displayColor = adaptColorForColorblindMode(option.color, colorblindMode)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelectColor(option.hex) }
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 26.dp else 36.dp)
                        .clip(CircleShape)
                        .background(displayColor)
                        .border(
                            width = if (chosen) 2.5.dp else 1.dp,
                            color = if (chosen) theme.textPrimary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (chosen) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = option.name,
                            tint = Color.White,
                            modifier = Modifier.size(if (compact) 14.dp else 18.dp)
                        )
                    } else if (colorblindMode != ColorblindMode.NONE) {
                        Text(
                            text = option.symbol,
                            color = Color.White,
                            fontSize = if (compact) 10.sp else 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                if (!compact) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (colorblindMode != ColorblindMode.NONE)
                            "${option.symbol} ${option.name}"
                        else
                            option.name,
                        fontSize = 10.sp,
                        fontWeight = if (chosen) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (chosen) displayColor else theme.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ThemePreviewCard(
    itemTheme: AppThemePalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) itemTheme.primaryAccent else itemTheme.surfaceBorder
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = itemTheme.cardColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        modifier = Modifier
            .width(126.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(itemTheme.bgColor)
                    .border(1.dp, itemTheme.surfaceBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(itemTheme.primaryAccent)
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(itemTheme.secondaryAccent)
                            .border(1.dp, itemTheme.primaryAccent, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = itemTheme.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = itemTheme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (itemTheme.isDark) "Oscuro AMOLED" else "Claro",
                fontSize = 10.sp,
                color = itemTheme.textSecondary
            )
        }
    }
}

@Composable
fun TaskCardComponent(
    theme: AppThemePalette,
    task: TodoTask,
    languageCode: String = "es",
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    highContrast: Boolean = false,
    dyslexiaFriendly: Boolean = false,
    largeTouchTargets: Boolean = false,
    showOrderingControls: Boolean = false,
    blockerTaskTitle: String? = null,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val accent = taskAccent(task.colorHex, colorblindMode)
    val priorityEnum = TaskPriority.fromLevel(task.priority)
    val prioColor = priorityColor(task.priority, colorblindMode)
    val isHighOrCritical = task.priority >= TaskPriority.HIGH.level
    val borderAccent = if (isHighOrCritical && !task.isCompleted) {
        if (theme.isDark && task.priority == TaskPriority.CRITICAL.level)
            adaptColorForColorblindMode(CrimsonHighlight, colorblindMode)
        else prioColor
    } else accent

    val symbol = taskColorSymbol(task.colorHex)
    val colorName = taskColorName(task.colorHex)
    val letterSpace = if (dyslexiaFriendly) 0.8.sp else 0.sp
    val lineH = if (dyslexiaFriendly) 22.sp else 18.sp
    val actionTargetSize = if (largeTouchTargets) 52.dp else 36.dp

    val subtasks = task.subtasks
    val completedSubtasks = subtasks.count { it.isCompleted }

    val cardBorder: BorderStroke? = when {
        highContrast || theme.borderStyle == BorderStyleMode.STRONG -> BorderStroke(
            width = 2.4.dp,
            color = borderAccent
        )
        theme.borderStyle == BorderStyleMode.NONE -> null
        theme.borderStyle == BorderStyleMode.SUBTLE -> BorderStroke(
            width = 0.8.dp,
            color = borderAccent.copy(alpha = if (isHighOrCritical && !task.isCompleted) 0.45f else 0.22f)
        )
        else -> BorderStroke(
            width = 1.4.dp,
            color = borderAccent.copy(alpha = if (isHighOrCritical && !task.isCompleted) 0.85f else 0.55f)
        )
    }

    Card(
        shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isPinned) 5.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (largeTouchTargets) 16.dp else 14.dp,
                    vertical = if (largeTouchTargets) 16.dp else 12.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Barra lateral con símbolo geométrico
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .width(if (colorblindMode != ColorblindMode.NONE) 18.dp else 6.dp)
                        .height(if (largeTouchTargets) 56.dp else 46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accent)
                ) {
                    if (colorblindMode != ColorblindMode.NONE) {
                        Text(
                            text = symbol,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier.size(actionTargetSize),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = {
                            if (blockerTaskTitle != null && !task.isCompleted) {
                                Toast.makeText(
                                    context,
                                    "⛓️ Tarea bloqueada: completa primero '$blockerTaskTitle'",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                onToggle()
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accent,
                            uncheckedColor = if (blockerTaskTitle != null) DangerRed else accent,
                            checkmarkColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (task.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Fijada",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = task.title,
                            fontSize = if (largeTouchTargets) 17.sp else 16.sp,
                            letterSpacing = letterSpace,
                            lineHeight = lineH,
                            fontWeight = if (dyslexiaFriendly || isHighOrCritical) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (task.isCompleted) theme.textSecondary else theme.textPrimary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Fila de Insignias: Categoría, Prioridad, Colaborador ("Todos Juntos"), Recurrencia, Pomodoro y Foto
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // Insignia de Categoría + Símbolo/Color
                        Surface(
                            color = accent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = if (highContrast) BorderStroke(1.dp, accent) else null
                        ) {
                            Text(
                                text = if (colorblindMode != ColorblindMode.NONE)
                                    "$symbol ${task.category} ($colorName)"
                                else
                                    "$symbol ${task.category}",
                                color = accent,
                                fontSize = 10.sp,
                                letterSpacing = letterSpace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Insignia de Prioridad
                        Surface(
                            color = prioColor.copy(alpha = if (isHighOrCritical) 0.22f else 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = if (isHighOrCritical || highContrast) BorderStroke(1.dp, prioColor) else null
                        ) {
                            Text(
                                text = "${priorityEnum.badgeSymbol} ${priorityEnum.labelEs}",
                                color = prioColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Micro-chip de Colaboración ("Todos Juntos"): Responsable asignado + Compartir por WhatsApp en 1 toque
                        if (task.assignee.isNotBlank() && !task.assignee.equals("Solo a mí", ignoreCase = true)) {
                            Surface(
                                color = theme.secondaryAccent,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.45f)),
                                modifier = Modifier.clickable {
                                    VoiceAndShareHelper.shareTaskViaIntent(context, task)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = task.assignee,
                                        color = theme.primaryAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Enviar por WhatsApp al responsable",
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        // Insignia de Recurrencia
                        if (task.recurrence != "NONE") {
                            val rec = TaskRecurrence.fromCode(task.recurrence)
                            Surface(
                                color = theme.secondaryAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = rec.labelEs,
                                        color = theme.primaryAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Insignia de Minutos de Enfoque Pomodoro
                        if (task.focusMinutes > 0) {
                            Surface(
                                color = theme.secondaryAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${task.focusMinutes}m",
                                        color = theme.primaryAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Insignia de Foto Adjunta
                        if (!task.attachmentImagePath.isNullOrBlank()) {
                            Surface(
                                color = theme.secondaryAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    AutoTranslatedText(text = "Foto",
                                        color = theme.primaryAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Insignia de Dependencia / Bloqueo Secuencial
                        if (blockerTaskTitle != null && !task.isCompleted) {
                            Surface(
                                color = DangerRed.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, DangerRed)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "⛓️ Bloqueada por: $blockerTaskTitle",
                                        color = DangerRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${formatTaskDate(task.dueDate, languageCode)} · ${task.time}",
                        fontSize = 11.sp,
                        letterSpacing = letterSpace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = theme.textSecondary
                    )
                }

                // Acciones rápidas: Fijar y Eliminar (Soft Delete a Papelera)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onTogglePin != null) {
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(actionTargetSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Fijar tarea",
                                tint = if (task.isPinned) theme.primaryAccent else theme.textSecondary.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(actionTargetSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Enviar a Papelera",
                            tint = theme.textSecondary
                        )
                    }
                }
            }

            // Mini barra de progreso de Subtareas (si tiene subtareas)
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = completedSubtasks.toFloat() / subtasks.size.toFloat()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accent,
                        trackColor = theme.surfaceBorder,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "✓ $completedSubtasks/${subtasks.size} subtareas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            // Controles de Reordenamiento Manual (↑ / ↓) si está activo el modo organizar
            if (showOrderingControls && (onMoveUp != null || onMoveDown != null)) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AutoTranslatedText(text = "Posición manual:",
                        fontSize = 11.sp,
                        color = theme.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { onMoveUp?.invoke() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Subir", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        AutoTranslatedText("Subir", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { onMoveDown?.invoke() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        AutoTranslatedText("Bajar", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

/**
 * Panel Desplegable "Mostrar" (Superando a Todoist y TickTick/To-Do):
 * 1. FORMATO: Tarjetas verticales con vista previa esquemática dibujada en Compose + RadioButton (Lista, Panel, Calendario, Matriz).
 * 2. ORDEN: Agrupar (`GroupByMode`) y Ordenar (`SortByMode` con modo Inteligente por defecto).
 * 3. FILTRO: Responsable (`assignee`) y Fecha límite (`DueDateFilter`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDisplayOptionsSheet(
    theme: AppThemePalette,
    selectedFormat: TaskViewFormat,
    selectedGroupBy: GroupByMode,
    selectedSortBy: SortByMode,
    selectedAssigneeFilter: String,
    selectedDueDateFilter: DueDateFilter,
    availableAssignees: List<String> = DefaultCollaborators,
    onSelectFormat: (TaskViewFormat) -> Unit,
    onSelectGroupBy: (GroupByMode) -> Unit,
    onSelectSortBy: (SortByMode) -> Unit,
    onSelectAssigneeFilter: (String) -> Unit,
    onSelectDueDateFilter: (DueDateFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var expandGroupMenu by remember { mutableStateOf(false) }
    var expandSortMenu by remember { mutableStateOf(false) }
    var expandAssigneeMenu by remember { mutableStateOf(false) }
    var expandDueDateMenu by remember { mutableStateOf(false) }

    val allAssigneeOptions = remember(availableAssignees) {
        (listOf("Todos", "Solo a mí") + availableAssignees).distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.cardColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título centrado "Mostrar"
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AutoTranslatedText(text = "Mostrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
            }

            // ==================== 1. SECCIÓN FORMATO ====================
            AutoTranslatedText(text = "FORMATO",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textSecondary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TaskViewFormat.entries.forEach { format ->
                    val chosen = selectedFormat == format
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (chosen) theme.primaryAccent.copy(alpha = 0.14f) else theme.bgColor,
                        border = BorderStroke(
                            width = if (chosen) 2.dp else 1.dp,
                            color = if (chosen) theme.primaryAccent else theme.surfaceBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectFormat(format) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Vista previa esquemática dibujada en Compose Canvas
                            FormatSchematicPreview(
                                format = format,
                                accentColor = if (chosen) theme.primaryAccent else theme.textSecondary,
                                secondaryColor = theme.surfaceBorder
                            )

                            Text(
                                text = format.labelEs,
                                fontSize = 12.sp,
                                fontWeight = if (chosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (chosen) theme.primaryAccent else theme.textPrimary,
                                maxLines = 1
                            )

                            RadioButton(
                                selected = chosen,
                                onClick = { onSelectFormat(format) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = theme.primaryAccent,
                                    unselectedColor = theme.textSecondary
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ==================== 2. SECCIÓN ORDEN ====================
            AutoTranslatedText(text = "ORDEN",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textSecondary,
                letterSpacing = 1.sp
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.bgColor,
                border = BorderStroke(1.dp, theme.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Fila Agrupar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandGroupMenu = !expandGroupMenu }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoTranslatedText(text = "Agrupar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedGroupBy.labelEs,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expandGroupMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.textSecondary
                            )
                        }
                    }

                    if (expandGroupMenu) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GroupByMode.entries.forEach { mode ->
                                val active = selectedGroupBy == mode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) theme.primaryAccent else theme.cardColor,
                                    border = BorderStroke(1.dp, if (active) theme.primaryAccent else theme.surfaceBorder),
                                    modifier = Modifier.clickable {
                                        onSelectGroupBy(mode)
                                        expandGroupMenu = false
                                    }
                                ) {
                                    Text(
                                        text = mode.labelEs,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else theme.textPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.35f))

                    // Fila Ordenar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandSortMenu = !expandSortMenu }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoTranslatedText(text = "Ordenar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedSortBy == SortByMode.SMART)
                                    "✨ ${selectedSortBy.labelEs}"
                                else
                                    selectedSortBy.labelEs,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expandSortMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.textSecondary
                            )
                        }
                    }

                    if (expandSortMenu) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SortByMode.entries.forEach { mode ->
                                val active = selectedSortBy == mode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) theme.primaryAccent else theme.cardColor,
                                    border = BorderStroke(1.dp, if (active) theme.primaryAccent else theme.surfaceBorder),
                                    modifier = Modifier.clickable {
                                        onSelectSortBy(mode)
                                        expandSortMenu = false
                                    }
                                ) {
                                    Text(
                                        text = if (mode == SortByMode.SMART) "✨ ${mode.labelEs}" else mode.labelEs,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else theme.textPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==================== 3. SECCIÓN FILTRO ====================
            AutoTranslatedText(text = "FILTRO",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textSecondary,
                letterSpacing = 1.sp
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.bgColor,
                border = BorderStroke(1.dp, theme.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Fila Responsable ("Todos Juntos")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandAssigneeMenu = !expandAssigneeMenu }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoTranslatedText(text = "Responsable",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedAssigneeFilter,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expandAssigneeMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.textSecondary
                            )
                        }
                    }

                    if (expandAssigneeMenu) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allAssigneeOptions.forEach { person ->
                                val active = selectedAssigneeFilter.equals(person, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) theme.primaryAccent else theme.cardColor,
                                    border = BorderStroke(1.dp, if (active) theme.primaryAccent else theme.surfaceBorder),
                                    modifier = Modifier.clickable {
                                        onSelectAssigneeFilter(person)
                                        expandAssigneeMenu = false
                                    }
                                ) {
                                    Text(
                                        text = "👤 $person",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else theme.textPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.35f))

                    // Fila Fecha límite
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandDueDateMenu = !expandDueDateMenu }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoTranslatedText(text = "Fecha límite",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedDueDateFilter.labelEs,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expandDueDateMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.textSecondary
                            )
                        }
                    }

                    if (expandDueDateMenu) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DueDateFilter.entries.forEach { filter ->
                                val active = selectedDueDateFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) theme.primaryAccent else theme.cardColor,
                                    border = BorderStroke(1.dp, if (active) theme.primaryAccent else theme.surfaceBorder),
                                    modifier = Modifier.clickable {
                                        onSelectDueDateFilter(filter)
                                        expandDueDateMenu = false
                                    }
                                ) {
                                    Text(
                                        text = filter.labelEs,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else theme.textPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

/**
 * Dibuja las vistas previas esquemáticas para Lista, Panel Kanban, Calendario y Matriz 2x2.
 */
@Composable
private fun FormatSchematicPreview(
    format: TaskViewFormat,
    accentColor: Color,
    secondaryColor: Color
) {
    Canvas(
        modifier = Modifier
            .width(56.dp)
            .height(42.dp)
    ) {
        val w = size.width
        val h = size.height
        when (format) {
            TaskViewFormat.LIST -> {
                for (row in 0..2) {
                    val y = 6f + row * 12f
                    drawCircle(
                        color = accentColor,
                        radius = 3.5f,
                        center = Offset(7f, y + 3.5f)
                    )
                    drawRoundRect(
                        color = if (row == 0) accentColor else secondaryColor,
                        topLeft = Offset(15f, y),
                        size = Size(w - 19f, 7f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }
            TaskViewFormat.BOARD -> {
                val colW = (w - 12f) / 3f
                for (col in 0..2) {
                    val x = col * (colW + 6f)
                    val colH = when (col) {
                        0 -> h - 4f
                        1 -> h * 0.72f
                        else -> h * 0.88f
                    }
                    drawRoundRect(
                        color = if (col == 0) accentColor else secondaryColor,
                        topLeft = Offset(x, 2f),
                        size = Size(colW, colH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            TaskViewFormat.CALENDAR -> {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(2f, 2f),
                    size = Size(w - 4f, 8f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                val cellW = (w - 12f) / 3f
                val cellH = (h - 18f) / 2f
                for (r in 0..1) {
                    for (c in 0..2) {
                        drawRoundRect(
                            color = if (r == 0 && c == 1) accentColor else secondaryColor,
                            topLeft = Offset(2f + c * (cellW + 4f), 14f + r * (cellH + 4f)),
                            size = Size(cellW, cellH),
                            cornerRadius = CornerRadius(2.5f, 2.5f)
                        )
                    }
                }
            }
            TaskViewFormat.MATRIX -> {
                val boxW = (w - 8f) / 2f
                val boxH = (h - 8f) / 2f
                for (r in 0..1) {
                    for (c in 0..1) {
                        drawRoundRect(
                            color = if (r == 0 && c == 0) accentColor else secondaryColor,
                            topLeft = Offset(c * (boxW + 6f), r * (boxH + 6f)),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Icono vectorial multicolor estilo Google Drive (triángulo verde, amarillo y azul) dibujado con Canvas.
 */
@Composable
fun GoogleDriveTriangleIcon(modifier: Modifier = Modifier.size(34.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Segmento Superior Izquierdo (Verde Google Drive #0F9D58)
        val greenPath = Path().apply {
            moveTo(w * 0.34f, h * 0.10f)
            lineTo(w * 0.66f, h * 0.10f)
            lineTo(w * 0.36f, h * 0.64f)
            lineTo(w * 0.06f, h * 0.64f)
            close()
        }
        drawPath(path = greenPath, color = Color(0xFF0F9D58))

        // Segmento Superior Derecho (Amarillo Google Drive #F4B400)
        val yellowPath = Path().apply {
            moveTo(w * 0.38f, h * 0.10f)
            lineTo(w * 0.66f, h * 0.10f)
            lineTo(w * 0.94f, h * 0.64f)
            lineTo(w * 0.64f, h * 0.64f)
            close()
        }
        drawPath(path = yellowPath, color = Color(0xFFF4B400))

        // Segmento Inferior Horizontal (Azul Google Drive #4285F4)
        val bluePath = Path().apply {
            moveTo(w * 0.22f, h * 0.64f)
            lineTo(w * 0.94f, h * 0.64f)
            lineTo(w * 0.78f, h * 0.90f)
            lineTo(w * 0.06f, h * 0.90f)
            close()
        }
        drawPath(path = bluePath, color = Color(0xFF4285F4))
    }
}

/**
 * Modal de Monetización Ética y No Invasiva (Suscripción Mensual / Anual Pro + Pase Gratis 24h).
 * Respeta estrictamente la regla ética: las 11 herramientas de accesibilidad, los 24 idiomas y
 * el CRUD de tareas son 100% gratuitos siempre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallSheet(
    theme: AppThemePalette,
    featureName: String = "Funciones Avanzadas Pro",
    onDismiss: () -> Unit,
    onUnlockRewarded24h: () -> Unit,
    onSelectPlan: (SubscriptionPlan) -> Unit
) {
    val context = LocalContext.current
    var isWatchingAd by remember { mutableStateOf(false) }
    var adProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isWatchingAd) {
        if (isWatchingAd) {
            for (step in 1..30) {
                delay(100L)
                adProgress = step / 30f
            }
            isWatchingAd = false
            onUnlockRewarded24h()
            Toast.makeText(
                context,
                "🎉 ¡Pase Pro de 24 horas activado gratis en DataStore!",
                Toast.LENGTH_LONG
            ).show()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.cardColor,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(theme.primaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = "ToDoList Pro • Desbloquea $featureName",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textPrimary
            )

            AutoTranslatedText(text = "Ética Primero: Las 11 herramientas de accesibilidad, los 24 idiomas y tus tareas son 100% gratis siempre. Elige cómo desbloquear las herramientas de poder:",
                fontSize = 12.sp,
                color = theme.textSecondary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = theme.bgColor,
                border = BorderStroke(1.dp, theme.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val benefits = listOf(
                        "📋 Tablero Kanban de 3 Columnas + ⛓️ Dependencias entre Tareas",
                        "✨ Ordenamiento Inteligente Avanzado + Colaboración (@Responsable)",
                        "🔒 Bóveda de Seguridad con PIN de 4 dígitos por Perfil",
                        "📷 Adjuntos Fotográficos Ilimitados en tus Actividades",
                        "🔥 Mapa de Calor de Constancia de 90 Días + Respaldo Google Drive"
                    )
                    benefits.forEach { item ->
                        Text(
                            text = item,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                    }
                }
            }

            // Opción A: Gratuita y No Invasiva (Pase Recompensado 24h)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.secondaryAccent),
                border = BorderStroke(1.5.dp, theme.primaryAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = theme.primaryAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(text = "Opción Gratuita (Sin pagar nada)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.primaryAccent
                            )
                            AutoTranslatedText(text = "Mira un anuncio corto de 3 segundos y desbloquea TODO por 24 horas",
                                fontSize = 11.sp,
                                color = theme.textSecondary
                            )
                        }
                    }

                    if (isWatchingAd) {
                        LinearProgressIndicator(
                            progress = { adProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = theme.primaryAccent
                        )
                        Text(
                            text = "Desbloqueando acceso completo por 24h... ${(adProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryAccent
                        )
                    } else {
                        Button(
                            onClick = { isWatchingAd = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                        ) {
                            AutoTranslatedText(text = "🎬 Ver anuncio corto → Desbloquear 24h Gratis",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Opción B: Suscripción Mensual y Anual Pro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onSelectPlan(SubscriptionPlan.MONTHLY_PRO)
                        Toast.makeText(
                            context,
                            "⭐ ¡Suscripción Pro Mensual activada en DataStore!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, theme.primaryAccent)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AutoTranslatedText(text = "Pro Mensual",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryAccent
                        )
                        Text(
                            text = "$39 MXN / mes",
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        onSelectPlan(SubscriptionPlan.ANNUAL_PRO)
                        Toast.makeText(
                            context,
                            "🏆 ¡Plan Pro Anual activado en DataStore!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoTranslatedText(text = "Pro Anual",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "$299 MXN / año",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
