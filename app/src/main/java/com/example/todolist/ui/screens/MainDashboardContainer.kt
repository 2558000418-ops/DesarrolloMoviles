package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.CornerStyleMode
import com.example.todolist.model.FontScaleMode
import com.example.todolist.model.LayoutDensityMode
import com.example.todolist.model.ProStatus
import com.example.todolist.model.QuickTaskTemplates
import com.example.todolist.model.SubtaskItem
import com.example.todolist.model.SubscriptionPlan
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TaskRecurrence
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.model.serializeSubtasks
import com.example.todolist.ui.components.ProPaywallSheet
import com.example.todolist.ui.components.TaskColorSelector
import com.example.todolist.ui.components.ThemePreviewCard
import com.example.todolist.ui.components.UserAvatarComponent
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.AvailableThemes
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.TaskColorChoices
import com.example.todolist.ui.theme.appTextFieldColors
import com.example.todolist.ui.theme.priorityColor
import com.example.todolist.util.LocalizedStrings
import com.example.todolist.util.NotificationHelper
import com.example.todolist.util.ParsedSmartTask
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.formatTaskDate
import com.example.todolist.util.formatTaskTime
import com.example.todolist.util.parseTaskTime
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardContainer(
    theme: AppThemePalette,
    user: UserAccount,
    tasks: List<TodoTask>,
    strings: LocalizedStrings,
    isDarkMode: Boolean,
    notificationsEnabled: Boolean,
    selectedLanguageCode: String,
    colorblindMode: ColorblindMode = ColorblindMode.NONE,
    fontScaleMode: FontScaleMode = FontScaleMode.NORMAL,
    highContrastEnabled: Boolean = false,
    dyslexiaFriendlyEnabled: Boolean = false,
    leftHandedMode: Boolean = false,
    reduceMotion: Boolean = false,
    largeTouchTargets: Boolean = false,
    zenFocusMode: Boolean = false,
    borderStyleMode: BorderStyleMode = BorderStyleMode.SUBTLE,
    cornerStyleMode: CornerStyleMode = CornerStyleMode.ROUNDED,
    layoutDensityMode: LayoutDensityMode = LayoutDensityMode.COMFORTABLE,
    hapticFeedbackEnabled: Boolean = true,
    privacyBlurMode: Boolean = false,
    streakDays: Int = 1,
    proStatus: ProStatus = ProStatus(),
    onUnlockRewarded24h: () -> Unit = {},
    onSelectSubscriptionPlan: (SubscriptionPlan) -> Unit = {},
    onUpdateUserPin: (String?) -> Unit = {},
    onExportJsonUri: (Uri) -> Unit = {},
    onImportJsonUri: (Uri) -> Unit = {},
    onQuickCreateSmartTask: (ParsedSmartTask) -> Unit = {},
    onMoveTaskKanban: (TodoTask, Int) -> Unit = { _, _ -> },
    initialTab: Int,
    onTabChanged: (Int) -> Unit,
    onPickProfilePhoto: () -> Unit,
    onRemoveProfilePhoto: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTheme: (Int) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectColorblindMode: (ColorblindMode) -> Unit = {},
    onSelectFontScaleMode: (FontScaleMode) -> Unit = {},
    onToggleHighContrast: (Boolean) -> Unit = {},
    onToggleDyslexiaFriendly: (Boolean) -> Unit = {},
    onToggleLeftHandedMode: (Boolean) -> Unit = {},
    onToggleReduceMotion: (Boolean) -> Unit = {},
    onToggleLargeTouchTargets: (Boolean) -> Unit = {},
    onToggleZenFocusMode: (Boolean) -> Unit = {},
    onSelectBorderStyleMode: (BorderStyleMode) -> Unit = {},
    onSelectCornerStyleMode: (CornerStyleMode) -> Unit = {},
    onSelectLayoutDensityMode: (LayoutDensityMode) -> Unit = {},
    onToggleHapticFeedback: (Boolean) -> Unit = {},
    onTogglePrivacyBlur: (Boolean) -> Unit = {},
    onRescheduleOverdueTasks: () -> Int = { 0 },
    onImportBackupTasks: (String) -> Int = { 0 },
    onResetDefaultPreferences: () -> Unit,
    onToggleTask: (TodoTask) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onTogglePin: (TodoTask) -> Unit = {},
    onMoveTaskUp: (TodoTask) -> Unit = {},
    onMoveTaskDown: (TodoTask) -> Unit = {},
    onRestoreTask: (Int) -> Unit = {},
    onDeletePermanently: (Int) -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    onAddTask: (
        title: String,
        desc: String,
        category: String,
        time: String,
        dueDate: String,
        colorHex: String,
        priority: Int,
        recurrence: String,
        subtasksJson: String,
        isPinned: Boolean,
        reminderEnabled: Boolean
    ) -> Unit,
    onChangeTaskColor: (Int, String) -> Unit,
    onAssignDateToTask: (TodoTask, LocalDate) -> Unit = { _, _ -> },
    onOpenDetail: (TodoTask) -> Unit,
    onSwitchUserOrLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    fun openTab(tab: Int) {
        selectedTab = tab
        onTabChanged(tab)
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var showUserSettingsSheet by remember { mutableStateOf(false) }
    var paywallFeatureName by remember { mutableStateOf<String?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Universidad") }
    var newDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var newHour by remember { mutableIntStateOf(LocalTime.now().hour) }
    var newMinute by remember { mutableIntStateOf(LocalTime.now().minute) }
    var newColor by remember { mutableStateOf(TaskColorChoices.first().hex) }
    var newPriority by remember { mutableIntStateOf(TaskPriority.MEDIUM.level) }
    var newRecurrence by remember { mutableStateOf(TaskRecurrence.NONE.code) }
    var newPinned by remember { mutableStateOf(false) }
    var newReminder by remember { mutableStateOf(false) }
    val newSubtasks = remember { mutableStateListOf<SubtaskItem>() }
    var draftSubtaskText by remember { mutableStateOf("") }
    var addTaskErrorMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    val sheetSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val parsed = VoiceAndShareHelper.parseSmartTaskInput(spoken)
                newTitle = parsed.cleanTitle
                newCategory = parsed.category
                newPriority = parsed.priority
                newDate = parsed.dueDate
                val t = parseTaskTime(parsed.time)
                newHour = t.hour
                newMinute = t.minute
                addTaskErrorMsg = ""
                Toast.makeText(
                    context,
                    "🎤 Voz detectada: ${parsed.cleanTitle} (${parsed.category})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        containerColor = theme.bgColor,
        floatingActionButtonPosition = if (leftHandedMode) FabPosition.Start else FabPosition.End,
        floatingActionButton = {
            if (selectedTab != 2 && selectedTab != 3) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = theme.primaryAccent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = if (largeTouchTargets) Modifier.size(64.dp) else Modifier
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Nueva Tarea",
                        modifier = Modifier.size(if (largeTouchTargets) 34.dp else 28.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = theme.cardColor,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { openTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.navSummary) },
                    label = { Text(strings.navSummary) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primaryAccent,
                        selectedTextColor = theme.primaryAccent,
                        unselectedIconColor = theme.textSecondary,
                        unselectedTextColor = theme.textSecondary,
                        indicatorColor = theme.secondaryAccent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { openTab(1) },
                    icon = { Icon(Icons.Default.List, contentDescription = strings.navMyTasks) },
                    label = { Text(strings.navMyTasks) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primaryAccent,
                        selectedTextColor = theme.primaryAccent,
                        unselectedIconColor = theme.textSecondary,
                        unselectedTextColor = theme.textSecondary,
                        indicatorColor = theme.secondaryAccent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { openTab(2) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = strings.navCalendar) },
                    label = { Text(strings.navCalendar) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primaryAccent,
                        selectedTextColor = theme.primaryAccent,
                        unselectedIconColor = theme.textSecondary,
                        unselectedTextColor = theme.textSecondary,
                        indicatorColor = theme.secondaryAccent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { openTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.navSettings) },
                    label = { Text(strings.navSettings) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primaryAccent,
                        selectedTextColor = theme.primaryAccent,
                        unselectedIconColor = theme.textSecondary,
                        unselectedTextColor = theme.textSecondary,
                        indicatorColor = theme.secondaryAccent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeSummaryTab(
                    theme = theme,
                    user = user,
                    tasks = tasks,
                    strings = strings,
                    notificationsEnabled = notificationsEnabled,
                    colorblindMode = colorblindMode,
                    highContrast = highContrastEnabled,
                    dyslexiaFriendly = dyslexiaFriendlyEnabled,
                    reduceMotion = reduceMotion,
                    largeTouchTargets = largeTouchTargets,
                    zenFocusMode = zenFocusMode,
                    streakDays = streakDays,
                    privacyBlurMode = privacyBlurMode,
                    onToggleZenMode = onToggleZenFocusMode,
                    onRescheduleOverdueTasks = onRescheduleOverdueTasks,
                    onQuickCreateSmartTask = onQuickCreateSmartTask,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask,
                    onTogglePin = onTogglePin,
                    onMoveTaskUp = onMoveTaskUp,
                    onMoveTaskDown = onMoveTaskDown,
                    onOpenDetail = onOpenDetail,
                    onOpenTopUserConfig = { showUserSettingsSheet = true }
                )

                1 -> AllTasksSearchTab(
                    theme = theme,
                    user = user,
                    tasks = tasks,
                    strings = strings,
                    colorblindMode = colorblindMode,
                    highContrast = highContrastEnabled,
                    dyslexiaFriendly = dyslexiaFriendlyEnabled,
                    largeTouchTargets = largeTouchTargets,
                    hasProAccess = proStatus.hasProAccess,
                    onRequestProPaywall = { paywallFeatureName = it },
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask,
                    onTogglePin = onTogglePin,
                    onMoveTaskUp = onMoveTaskUp,
                    onMoveTaskDown = onMoveTaskDown,
                    onMoveTaskKanban = onMoveTaskKanban,
                    onRestoreTask = onRestoreTask,
                    onDeletePermanently = onDeletePermanently,
                    onEmptyTrash = onEmptyTrash,
                    onOpenDetail = onOpenDetail,
                    onOpenTopUserConfig = { showUserSettingsSheet = true }
                )

                2 -> CalendarTasksTab(
                    theme = theme,
                    tasks = tasks,
                    strings = strings,
                    colorblindMode = colorblindMode,
                    highContrast = highContrastEnabled,
                    dyslexiaFriendly = dyslexiaFriendlyEnabled,
                    largeTouchTargets = largeTouchTargets,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask,
                    onTogglePin = onTogglePin,
                    onOpenDetail = onOpenDetail,
                    onChangeTaskColor = onChangeTaskColor,
                    onAssignDateToTask = onAssignDateToTask,
                    onCreateTaskForDate = { selectedDate ->
                        newDate = selectedDate.toString()
                        showAddSheet = true
                    }
                )

                3 -> SettingsAndPreferencesTab(
                    theme = theme,
                    user = user,
                    tasks = tasks,
                    strings = strings,
                    isDarkMode = isDarkMode,
                    notificationsEnabled = notificationsEnabled,
                    selectedLanguageCode = selectedLanguageCode,
                    colorblindMode = colorblindMode,
                    fontScaleMode = fontScaleMode,
                    highContrastEnabled = highContrastEnabled,
                    dyslexiaFriendlyEnabled = dyslexiaFriendlyEnabled,
                    leftHandedMode = leftHandedMode,
                    reduceMotion = reduceMotion,
                    largeTouchTargets = largeTouchTargets,
                    zenFocusMode = zenFocusMode,
                    borderStyleMode = borderStyleMode,
                    cornerStyleMode = cornerStyleMode,
                    layoutDensityMode = layoutDensityMode,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    privacyBlurMode = privacyBlurMode,
                    proStatus = proStatus,
                    onOpenProPaywall = { paywallFeatureName = it },
                    onUpdateUserPin = onUpdateUserPin,
                    onExportJsonUri = onExportJsonUri,
                    onImportJsonUri = onImportJsonUri,
                    onPickProfilePhoto = onPickProfilePhoto,
                    onRemoveProfilePhoto = onRemoveProfilePhoto,
                    onToggleDarkMode = onToggleDarkMode,
                    onSelectTheme = onSelectTheme,
                    onToggleNotifications = onToggleNotifications,
                    onSelectLanguage = onSelectLanguage,
                    onSelectColorblindMode = onSelectColorblindMode,
                    onSelectFontScaleMode = onSelectFontScaleMode,
                    onToggleHighContrast = onToggleHighContrast,
                    onToggleDyslexiaFriendly = onToggleDyslexiaFriendly,
                    onToggleLeftHandedMode = onToggleLeftHandedMode,
                    onToggleReduceMotion = onToggleReduceMotion,
                    onToggleLargeTouchTargets = onToggleLargeTouchTargets,
                    onToggleZenFocusMode = onToggleZenFocusMode,
                    onSelectBorderStyleMode = onSelectBorderStyleMode,
                    onSelectCornerStyleMode = onSelectCornerStyleMode,
                    onSelectLayoutDensityMode = onSelectLayoutDensityMode,
                    onToggleHapticFeedback = onToggleHapticFeedback,
                    onTogglePrivacyBlur = onTogglePrivacyBlur,
                    onImportBackupTasks = onImportBackupTasks,
                    onResetDefaultPreferences = onResetDefaultPreferences,
                    onLogout = onSwitchUserOrLogout
                )
            }
        }
    }

    // ==================== MODAL DE MONETIZACIÓN ÉTICA (PASE 24H GRATIS O PRO) ====================
    if (paywallFeatureName != null) {
        ProPaywallSheet(
            theme = theme,
            featureName = paywallFeatureName ?: "Funciones Pro",
            onDismiss = { paywallFeatureName = null },
            onUnlockRewarded24h = {
                onUnlockRewarded24h()
                paywallFeatureName = null
            },
            onSelectPlan = { plan ->
                onSelectSubscriptionPlan(plan)
                paywallFeatureName = null
            }
        )
    }

    // ==================== MODAL SUPERIOR: ACCESO RÁPIDO A PERFIL Y 10 TEMAS ====================
    if (showUserSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUserSettingsSheet = false },
            containerColor = theme.cardColor,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            val active = tasks.filter { !it.isDeleted }
            val completed = active.count { it.isCompleted }
            val pending = active.size - completed

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserAvatarComponent(
                        name = user.name,
                        imagePath = user.profileImagePath,
                        backgroundColor = theme.primaryAccent,
                        size = 64.dp,
                        fontSize = 26.sp,
                        showEditBadge = true,
                        onClick = onPickProfilePhoto
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = theme.textSecondary
                        )
                        Text(
                            text = strings.pendingAndCompletedCount(pending, completed),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = onPickProfilePhoto,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = theme.secondaryAccent,
                            contentColor = theme.primaryAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            strings.changePhoto,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!user.profileImagePath.isNullOrBlank()) {
                        TextButton(
                            onClick = onRemoveProfilePhoto,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(strings.removePhoto, fontSize = 12.sp, color = DangerRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = theme.surfaceBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${strings.darkModeTitle} (10 Paletas)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    text = strings.dataStoreSectionSubtitle,
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AvailableThemes, key = { it.id }) { itemTheme ->
                        ThemePreviewCard(
                            itemTheme = itemTheme,
                            isSelected = itemTheme.id == theme.id,
                            onClick = { onSelectTheme(itemTheme.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showUserSettingsSheet = false
                            onSwitchUserOrLogout()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, theme.primaryAccent)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoTranslatedText("Cambiar Cuenta",
                            color = theme.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            showUserSettingsSheet = false
                            openTab(3)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                    ) {
                        Text(
                            strings.navSettings,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // ==================== MODAL INFERIOR: AGREGAR NUEVA TAREA COMPLETA ====================
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                addTaskErrorMsg = ""
                showAddSheet = false
            },
            containerColor = theme.cardColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nueva Tarea para ${user.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.textPrimary
                        )
                        AutoTranslatedText(text = "Los campos con (*) son obligatorios",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    IconButton(onClick = { newPinned = !newPinned }) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Fijar",
                            tint = if (newPinned) theme.primaryAccent else theme.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Plantillas Rápidas de 1 Toque (Autocompletado Inteligente)
                AutoTranslatedText(text = "⚡ Plantillas Rápidas (1 toque):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QuickTaskTemplates) { tpl ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.secondaryAccent,
                            border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                newTitle = tpl.defaultTitle
                                newDesc = tpl.defaultDesc
                                newCategory = tpl.category
                                newPriority = tpl.priority
                                newRecurrence = tpl.recurrence
                                newColor = tpl.colorHex
                                addTaskErrorMsg = ""
                                newSubtasks.clear()
                                tpl.subtasks.forEachIndexed { idx, sTitle ->
                                    newSubtasks.add(
                                        SubtaskItem(
                                            id = "${System.currentTimeMillis()}_$idx",
                                            title = sTitle,
                                            isCompleted = false
                                        )
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Plantilla '${tpl.name}' cargada con ${tpl.subtasks.size} pasos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text(
                                text = "${tpl.iconEmoji} ${tpl.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.primaryAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = {
                            newTitle = it
                            if (it.isNotBlank()) addTaskErrorMsg = ""
                        },
                        label = {
                            AutoTranslatedText("Título de la tarea *",
                                color = if (addTaskErrorMsg.isNotEmpty()) DangerRed else theme.textSecondary
                            )
                        },
                        isError = addTaskErrorMsg.isNotEmpty(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = appTextFieldColors(
                            theme = theme,
                            accent = theme.primaryAccent,
                            isError = addTaskErrorMsg.isNotEmpty(),
                            containerColor = theme.bgColor
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.secondaryAccent,
                        border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                runCatching {
                                    sheetSpeechLauncher.launch(
                                        VoiceAndShareHelper.createSpeechRecognizerIntent(selectedLanguageCode)
                                    )
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Dictado por voz no disponible en este dispositivo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Dictar por voz",
                                tint = theme.primaryAccent
                            )
                        }
                    }
                }

                if (addTaskErrorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, DangerRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = addTaskErrorMsg,
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { newDesc = it },
                    label = { AutoTranslatedText("Notas o descripción", color = theme.textSecondary) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = appTextFieldColors(
                        theme = theme,
                        accent = theme.primaryAccent,
                        containerColor = theme.bgColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nivel de Prioridad
                AutoTranslatedText("Nivel de Prioridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { prio ->
                        val selected = newPriority == prio.level
                        val pColor = priorityColor(prio.level, colorblindMode)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) pColor else theme.surfaceBorder,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { newPriority = prio.level }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${prio.badgeSymbol} ${prio.labelEs}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else theme.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Categoría
                AutoTranslatedText("Categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val categories = listOf("Universidad", "Proyecto", "Personal", "Trabajo", "Salud", "Urgente")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = newCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) theme.primaryAccent else theme.surfaceBorder,
                            modifier = Modifier.clickable { newCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else theme.textPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Repetición (Recurrencia)
                AutoTranslatedText("Repetición (Hábito / Rutina)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskRecurrence.entries.forEach { rec ->
                        val isSelected = newRecurrence == rec.code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) theme.primaryAccent else theme.surfaceBorder,
                            modifier = Modifier.clickable { newRecurrence = rec.code }
                        ) {
                            Text(
                                text = rec.labelEs,
                                color = if (isSelected) Color.White else theme.textPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtareas iniciales opcionales
                Text(
                    "Subtareas / Pasos (${newSubtasks.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                newSubtasks.forEachIndexed { idx, sub ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${sub.title}", fontSize = 12.sp, color = theme.textPrimary)
                        IconButton(
                            onClick = { newSubtasks.removeAt(idx) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftSubtaskText,
                        onValueChange = { draftSubtaskText = it },
                        placeholder = { AutoTranslatedText("Agregar subtarea...", fontSize = 12.sp, color = theme.textSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = appTextFieldColors(
                            theme = theme,
                            accent = theme.primaryAccent,
                            containerColor = theme.bgColor
                        )
                    )
                    FilledTonalButton(
                        onClick = {
                            if (draftSubtaskText.isNotBlank()) {
                                newSubtasks.add(
                                    SubtaskItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = draftSubtaskText.trim(),
                                        isCompleted = false
                                    )
                                )
                                draftSubtaskText = ""
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = theme.secondaryAccent,
                            contentColor = theme.primaryAccent
                        )
                    ) {
                        AutoTranslatedText("+ Paso", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primaryAccent)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fecha y Hora + Recordatorio Local
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            val date = LocalDate.parse(newDate)
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    newDate = LocalDate.of(year, month + 1, day).toString()
                                },
                                date.year,
                                date.monthValue - 1,
                                date.dayOfMonth
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AutoTranslatedText("Fecha", fontSize = 11.sp, color = theme.textSecondary)
                            Text(
                                formatTaskDate(newDate, selectedLanguageCode),
                                fontSize = 12.sp,
                                color = theme.primaryAccent
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    newHour = hour
                                    newMinute = minute
                                },
                                newHour,
                                newMinute,
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AutoTranslatedText("Hora", fontSize = 11.sp, color = theme.textSecondary)
                            Text(
                                formatTaskTime(newHour, newMinute),
                                fontSize = 12.sp,
                                color = theme.primaryAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoTranslatedText(text = "Programar alarma y recordatorio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                    }
                    Switch(
                        checked = newReminder,
                        onCheckedChange = { newReminder = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                AutoTranslatedText("Color de la tarea", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                TaskColorSelector(
                    theme = theme,
                    selectedHex = newColor,
                    onSelectColor = { newColor = it },
                    colorblindMode = colorblindMode
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (newTitle.isBlank()) {
                            addTaskErrorMsg = "⚠️ Debes escribir como mínimo el título de la tarea para poder guardarla."
                            Toast.makeText(
                                context,
                                "Por favor escribe al menos el título de la tarea",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val timeFormatted = formatTaskTime(newHour, newMinute)
                            onAddTask(
                                newTitle,
                                if (newDesc.isBlank()) "Sin notas adicionales." else newDesc,
                                newCategory,
                                timeFormatted,
                                newDate,
                                newColor,
                                newPriority,
                                newRecurrence,
                                serializeSubtasks(newSubtasks),
                                newPinned,
                                newReminder
                            )
                            if (newReminder && notificationsEnabled) {
                                NotificationHelper.sendTaskNotification(
                                    context = context,
                                    notificationId = (System.currentTimeMillis() % 100000).toInt(),
                                    title = newTitle,
                                    category = newCategory,
                                    time = timeFormatted,
                                    priorityLevel = newPriority
                                )
                            }
                            newTitle = ""
                            newDesc = ""
                            addTaskErrorMsg = ""
                            newDate = LocalDate.now().toString()
                            newHour = LocalTime.now().hour
                            newMinute = LocalTime.now().minute
                            newColor = TaskColorChoices.first().hex
                            newPriority = TaskPriority.MEDIUM.level
                            newRecurrence = TaskRecurrence.NONE.code
                            newPinned = false
                            newReminder = false
                            newSubtasks.clear()
                            draftSubtaskText = ""
                            showAddSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                ) {
                    AutoTranslatedText("Guardar Tarea",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
