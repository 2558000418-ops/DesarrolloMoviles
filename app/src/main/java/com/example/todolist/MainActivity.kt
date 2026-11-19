package com.example.todolist

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.todolist.data.TodoDatabaseHelper
import com.example.todolist.data.UserPreferencesRepository
import com.example.todolist.model.AppLanguage
import com.example.todolist.model.AppScreen
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.CornerStyleMode
import com.example.todolist.model.FontScaleMode
import com.example.todolist.model.LayoutDensityMode
import com.example.todolist.model.ProStatus
import com.example.todolist.model.SubscriptionPlan
import com.example.todolist.model.TaskRecurrence
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.model.UserPreferencesState
import com.example.todolist.ui.components.ProPaywallSheet
import com.example.todolist.ui.screens.AuthAndUserPickerScreen
import com.example.todolist.ui.screens.MainDashboardContainer
import com.example.todolist.ui.screens.TaskDetailView
import com.example.todolist.ui.theme.AvailableThemes
import com.example.todolist.ui.theme.ProvideDynamicAppTheme
import com.example.todolist.ui.components.LocalSelectedLanguage
import com.example.todolist.ui.theme.TaskColorChoices
import com.example.todolist.ui.theme.applyAccessibilityToTheme
import com.example.todolist.ui.theme.resolveThemeIdForMode
import com.example.todolist.util.NotificationHelper
import com.example.todolist.util.VoiceAndShareHelper
import com.example.todolist.util.exportTasksToJsonString
import com.example.todolist.util.importTasksFromJsonString
import com.example.todolist.util.saveImageFromUriToInternalStorage
import com.example.todolist.util.stringsForLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannelCreated(this)
        setContent {
            MaterialTheme {
                TodoAppRoot()
            }
        }
    }
}

@Composable
fun TodoAppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dbHelper = remember { TodoDatabaseHelper(context) }
    val preferencesRepository = remember { UserPreferencesRepository(context) }

    // Lectura desde Android DataStore
    val prefsState by preferencesRepository.preferencesFlow.collectAsState(
        initial = UserPreferencesState(isLoaded = false)
    )

    // ==================== ESTADO INSTANTÁNEO EN MEMORIA (0ms DELAY EN UI) ====================
    var instantThemeId by remember { mutableIntStateOf(0) }
    var instantDarkMode by remember { mutableStateOf(false) }
    var instantNotifications by remember { mutableStateOf(true) }
    var instantLanguageCode by remember { mutableStateOf(AppLanguage.ES.code) }
    var instantColorblindMode by remember { mutableStateOf(ColorblindMode.NONE) }
    var instantFontScale by remember { mutableStateOf(FontScaleMode.NORMAL) }
    var instantHighContrast by remember { mutableStateOf(false) }
    var instantDyslexia by remember { mutableStateOf(false) }
    var instantLeftHanded by remember { mutableStateOf(false) }
    var instantReduceMotion by remember { mutableStateOf(false) }
    var instantLargeTouch by remember { mutableStateOf(false) }
    var instantZenFocus by remember { mutableStateOf(false) }
    var instantBorderStyle by remember { mutableStateOf(BorderStyleMode.SUBTLE) }
    var instantCornerStyle by remember { mutableStateOf(CornerStyleMode.ROUNDED) }
    var instantLayoutDensity by remember { mutableStateOf(LayoutDensityMode.COMFORTABLE) }
    var instantHaptic by remember { mutableStateOf(true) }
    var instantPrivacyBlur by remember { mutableStateOf(false) }
    var instantStreakDays by remember { mutableIntStateOf(1) }
    var instantSubscriptionPlan by remember { mutableStateOf(SubscriptionPlan.FREE) }
    var instantRewardedUntil by remember { mutableLongStateOf(0L) }
    var globalPaywallFeature by remember { mutableStateOf<String?>(null) }

    val currentProStatus = remember(instantSubscriptionPlan, instantRewardedUntil) {
        ProStatus(
            plan = instantSubscriptionPlan,
            rewardedUnlockUntilMillis = instantRewardedUntil
        )
    }

    // Sincroniza los estados instantáneos cuando DataStore carga o emite cambios externos
    LaunchedEffect(prefsState) {
        if (prefsState.isLoaded) {
            instantThemeId = prefsState.themeId
            instantDarkMode = prefsState.isDarkMode
            instantNotifications = prefsState.notificationsEnabled
            instantLanguageCode = prefsState.languageCode
            instantColorblindMode = prefsState.colorblindMode
            instantFontScale = prefsState.fontScaleMode
            instantHighContrast = prefsState.highContrastEnabled
            instantDyslexia = prefsState.dyslexiaFriendlyEnabled
            instantLeftHanded = prefsState.leftHandedMode
            instantReduceMotion = prefsState.reduceMotion
            instantLargeTouch = prefsState.largeTouchTargets
            instantZenFocus = prefsState.zenFocusMode
            instantBorderStyle = prefsState.borderStyleMode
            instantCornerStyle = prefsState.cornerStyleMode
            instantLayoutDensity = prefsState.layoutDensityMode
            instantHaptic = prefsState.hapticFeedbackEnabled
            instantPrivacyBlur = prefsState.privacyBlurMode
            instantStreakDays = prefsState.streakDays
            instantSubscriptionPlan = prefsState.subscriptionPlan
            instantRewardedUntil = prefsState.rewardedUnlockUntilMillis
        }
    }

    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }
    var selectedTask by remember { mutableStateOf<TodoTask?>(null) }
    var lastDashboardTab by remember { mutableIntStateOf(0) }
    var sessionRestoredOnce by remember { mutableStateOf(false) }

    val activeTheme = remember(
        instantThemeId,
        instantColorblindMode,
        instantHighContrast,
        instantBorderStyle,
        instantCornerStyle,
        instantLayoutDensity
    ) {
        val base = AvailableThemes.find { it.id == instantThemeId } ?: AvailableThemes[0]
        applyAccessibilityToTheme(
            base = base,
            colorblindMode = instantColorblindMode,
            highContrast = instantHighContrast,
            borderStyleMode = instantBorderStyle,
            cornerStyleMode = instantCornerStyle,
            layoutDensityMode = instantLayoutDensity
        )
    }

    val localizedStrings = remember(instantLanguageCode) {
        stringsForLanguage(instantLanguageCode)
    }

    val savedUsers = remember { mutableStateListOf<UserAccount>() }
    val userTasks = remember { mutableStateListOf<TodoTask>() }

    fun refreshUsers() {
        savedUsers.clear()
        savedUsers.addAll(dbHelper.getAllSavedUsers())
    }

    fun refreshCurrentUserTasks() {
        currentUser?.let { user ->
            val fresh = dbHelper.getTasksForUser(user.id)
            userTasks.clear()
            userTasks.addAll(fresh)
        }
    }

    // Selector de fotos de perfil desde la galería del dispositivo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val active = currentUser
        if (uri != null && active != null) {
            val savedPath = saveImageFromUriToInternalStorage(context, uri, active.id)
            if (savedPath != null) {
                dbHelper.updateUserProfileImage(active.id, savedPath)
                currentUser = active.copy(profileImagePath = savedPath)
                refreshUsers()
            }
        }
    }

    // Inicialización de base de datos y usuario semilla
    LaunchedEffect(Unit) {
        val existing = dbHelper.getAllSavedUsers()
        if (existing.isEmpty()) {
            dbHelper.registerUser("Erick Rosales", "erick@utna.edu.mx", "1234", defaultThemeId = 0)
        }
        refreshUsers()
    }

    // Restaurar automáticamente la sesión iniciada desde DataStore al abrir la app
    LaunchedEffect(prefsState.isLoaded, prefsState.loggedInUserId) {
        if (prefsState.isLoaded && !sessionRestoredOnce) {
            sessionRestoredOnce = true
            if (prefsState.loggedInUserId != UserPreferencesRepository.NO_USER_LOGGED_IN) {
                val restoredUser = dbHelper.getUserById(prefsState.loggedInUserId)
                if (restoredUser != null) {
                    instantThemeId = prefsState.themeId
                    instantDarkMode = prefsState.isDarkMode
                    currentUser = restoredUser.copy(themeId = prefsState.themeId)
                    userTasks.clear()
                    userTasks.addAll(dbHelper.getTasksForUser(restoredUser.id))
                    currentScreen = AppScreen.MAIN_DASHBOARD
                }
            }
        }
    }

    val baseDensity = LocalDensity.current
    val customDensity = remember(baseDensity, instantFontScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * instantFontScale.scale
        )
    }

    val layoutDirection = if (localizedStrings.language.isRtl) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalLayoutDirection provides layoutDirection,
        LocalSelectedLanguage provides instantLanguageCode
    ) {
        ProvideDynamicAppTheme(theme = activeTheme) {
            Surface(modifier = Modifier.fillMaxSize(), color = activeTheme.bgColor) {
                if (!prefsState.isLoaded) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = activeTheme.primaryAccent)
                    }
                    return@Surface
                }

                when (currentScreen) {
                    AppScreen.LOGIN -> {
                        AuthAndUserPickerScreen(
                            theme = activeTheme,
                            savedUsers = savedUsers,
                            onSelectSavedUser = { user ->
                                val pal = AvailableThemes.find { it.id == user.themeId } ?: AvailableThemes[0]
                                instantThemeId = pal.id
                                instantDarkMode = pal.isDark
                                currentUser = user
                                refreshCurrentUserTasks()
                                currentScreen = AppScreen.MAIN_DASHBOARD
                                coroutineScope.launch(Dispatchers.IO) {
                                    preferencesRepository.saveUserSession(user.id, user.themeId)
                                }
                            },
                            onDeleteSavedUser = { user ->
                                dbHelper.deleteUserAccount(user.id)
                                if (prefsState.loggedInUserId == user.id) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.clearUserSession()
                                    }
                                }
                                refreshUsers()
                            },
                            onLoginAttempt = { email, password ->
                                val logged = dbHelper.loginUser(email, password)
                                if (logged != null) {
                                    val pal = AvailableThemes.find { it.id == logged.themeId } ?: AvailableThemes[0]
                                    instantThemeId = pal.id
                                    instantDarkMode = pal.isDark
                                    currentUser = logged
                                    refreshCurrentUserTasks()
                                    currentScreen = AppScreen.MAIN_DASHBOARD
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.saveUserSession(logged.id, logged.themeId)
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
                            onRegisterAttempt = { name, email, password ->
                                val registered = dbHelper.registerUser(
                                    name,
                                    email,
                                    password,
                                    instantThemeId
                                )
                                if (registered != null) {
                                    refreshUsers()
                                    currentUser = registered
                                    refreshCurrentUserTasks()
                                    currentScreen = AppScreen.MAIN_DASHBOARD
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.saveUserSession(
                                            registered.id,
                                            registered.themeId
                                        )
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                    }

                    AppScreen.MAIN_DASHBOARD -> {
                        val activeUser = currentUser
                        if (activeUser != null) {
                            MainDashboardContainer(
                                theme = activeTheme,
                                user = activeUser,
                                tasks = userTasks,
                                strings = localizedStrings,
                                isDarkMode = instantDarkMode,
                                notificationsEnabled = instantNotifications,
                                selectedLanguageCode = instantLanguageCode,
                                colorblindMode = instantColorblindMode,
                                fontScaleMode = instantFontScale,
                                highContrastEnabled = instantHighContrast,
                                dyslexiaFriendlyEnabled = instantDyslexia,
                                leftHandedMode = instantLeftHanded,
                                reduceMotion = instantReduceMotion,
                                largeTouchTargets = instantLargeTouch,
                                zenFocusMode = instantZenFocus,
                                borderStyleMode = instantBorderStyle,
                                cornerStyleMode = instantCornerStyle,
                                layoutDensityMode = instantLayoutDensity,
                                hapticFeedbackEnabled = instantHaptic,
                                privacyBlurMode = instantPrivacyBlur,
                                streakDays = instantStreakDays,
                                proStatus = currentProStatus,
                                onUnlockRewarded24h = {
                                    val until = System.currentTimeMillis() + 86_400_000L
                                    instantRewardedUntil = until
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.grantRewardedProAccess24h()
                                    }
                                    Toast.makeText(
                                        context,
                                        "🎁 ¡Pase Pro de 24 horas desbloqueado gratis!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onSelectSubscriptionPlan = { plan ->
                                    instantSubscriptionPlan = plan
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setSubscriptionPlan(plan)
                                    }
                                    Toast.makeText(
                                        context,
                                        "⭐ ¡Plan ${plan.labelEs} activado correctamente!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onUpdateUserPin = { newPin ->
                                    currentUser = activeUser.copy(pinCode = newPin)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateUserPinCode(activeUser.id, newPin)
                                    }
                                    refreshUsers()
                                },
                                onExportJsonUri = { uri ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        runCatching {
                                            val json = exportTasksToJsonString(userTasks)
                                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                                out.write(json.toByteArray(Charsets.UTF_8))
                                            }
                                        }.onSuccess {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "💾 Archivo .JSON guardado correctamente",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onImportJsonUri = { uri ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val imported = runCatching {
                                            val rawJson = context.contentResolver
                                                .openInputStream(uri)
                                                ?.bufferedReader(Charsets.UTF_8)
                                                ?.use { it.readText() } ?: ""
                                            importTasksFromJsonString(dbHelper, activeUser.id, rawJson)
                                        }.getOrDefault(0)
                                        launch(Dispatchers.Main) {
                                            if (imported > 0) {
                                                refreshCurrentUserTasks()
                                                Toast.makeText(
                                                    context,
                                                    "📂 ¡Se importaron $imported tareas desde el archivo .JSON!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No se encontraron tareas válidas en el archivo .JSON",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onQuickCreateSmartTask = { parsed ->
                                    dbHelper.insertTask(
                                        userId = activeUser.id,
                                        title = parsed.cleanTitle,
                                        description = "Creada con Entrada Inteligente NLP",
                                        category = parsed.category,
                                        time = parsed.time,
                                        dayIndex = 1,
                                        dueDate = parsed.dueDate,
                                        colorHex = TaskColorChoices.first().hex,
                                        priority = parsed.priority,
                                        recurrence = TaskRecurrence.NONE.code,
                                        subtasksJson = "",
                                        isPinned = false,
                                        reminderEnabled = false
                                    )
                                    refreshCurrentUserTasks()
                                    Toast.makeText(
                                        context,
                                        "✨ Creada: ${parsed.cleanTitle} (${parsed.category} • ${parsed.dueDate})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onMoveTaskKanban = { task, newStage ->
                                    val isDone = newStage == 2
                                    val completedIso = if (isDone) LocalDate.now().toString() else null
                                    val idx = userTasks.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(
                                            kanbanStatus = newStage,
                                            isCompleted = isDone,
                                            completedDate = completedIso
                                        )
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskKanbanStatus(task.id, newStage)
                                    }
                                },
                                initialTab = lastDashboardTab,
                                onTabChanged = { lastDashboardTab = it },
                                onPickProfilePhoto = {
                                    photoPickerLauncher.launch("image/*")
                                },
                                onRemoveProfilePhoto = {
                                    currentUser = activeUser.copy(profileImagePath = null)
                                    dbHelper.updateUserProfileImage(activeUser.id, null)
                                    refreshUsers()
                                },
                                onToggleDarkMode = { isDark ->
                                    // Actualización instantánea en 0ms
                                    val newThemeId = resolveThemeIdForMode(instantThemeId, isDark)
                                    instantDarkMode = isDark
                                    instantThemeId = newThemeId
                                    currentUser = activeUser.copy(themeId = newThemeId)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateUserTheme(activeUser.id, newThemeId)
                                        preferencesRepository.setDarkMode(isDark)
                                    }
                                },
                                onSelectTheme = { newThemeId ->
                                    // Actualización instantánea en 0ms
                                    val pal = AvailableThemes.find { it.id == newThemeId } ?: AvailableThemes[0]
                                    instantThemeId = pal.id
                                    instantDarkMode = pal.isDark
                                    currentUser = activeUser.copy(themeId = pal.id)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateUserTheme(activeUser.id, pal.id)
                                        preferencesRepository.setThemePalette(pal.id)
                                    }
                                },
                                onToggleNotifications = { enabled ->
                                    instantNotifications = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setNotificationsEnabled(enabled)
                                    }
                                },
                                onSelectLanguage = { langCode ->
                                    instantLanguageCode = langCode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setLanguage(langCode)
                                    }
                                },
                                onSelectColorblindMode = { mode ->
                                    instantColorblindMode = mode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setColorblindMode(mode)
                                    }
                                },
                                onSelectFontScaleMode = { scaleMode ->
                                    instantFontScale = scaleMode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setFontScaleMode(scaleMode)
                                    }
                                },
                                onToggleHighContrast = { enabled ->
                                    instantHighContrast = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setHighContrastEnabled(enabled)
                                    }
                                },
                                onToggleDyslexiaFriendly = { enabled ->
                                    instantDyslexia = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setDyslexiaFriendlyEnabled(enabled)
                                    }
                                },
                                onToggleLeftHandedMode = { enabled ->
                                    instantLeftHanded = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setLeftHandedMode(enabled)
                                    }
                                },
                                onToggleReduceMotion = { enabled ->
                                    instantReduceMotion = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setReduceMotion(enabled)
                                    }
                                },
                                onToggleLargeTouchTargets = { enabled ->
                                    instantLargeTouch = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setLargeTouchTargets(enabled)
                                    }
                                },
                                onToggleZenFocusMode = { enabled ->
                                    instantZenFocus = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setZenFocusMode(enabled)
                                    }
                                },
                                onSelectBorderStyleMode = { mode ->
                                    instantBorderStyle = mode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setBorderStyleMode(mode)
                                    }
                                },
                                onSelectCornerStyleMode = { mode ->
                                    instantCornerStyle = mode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setCornerStyleMode(mode)
                                    }
                                },
                                onSelectLayoutDensityMode = { mode ->
                                    instantLayoutDensity = mode
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setLayoutDensityMode(mode)
                                    }
                                },
                                onToggleHapticFeedback = { enabled ->
                                    instantHaptic = enabled
                                    VoiceAndShareHelper.triggerHapticFeedback(context, enabled)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setHapticFeedbackEnabled(enabled)
                                    }
                                },
                                onTogglePrivacyBlur = { enabled ->
                                    instantPrivacyBlur = enabled
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.setPrivacyBlurMode(enabled)
                                    }
                                },
                                onRescheduleOverdueTasks = {
                                    val moved = dbHelper.rescheduleOverdueTasksToToday(activeUser.id)
                                    if (moved > 0) refreshCurrentUserTasks()
                                    moved
                                },
                                onImportBackupTasks = { rawBackup ->
                                    val count = dbHelper.importTasksFromBackupString(
                                        activeUser.id,
                                        rawBackup
                                    )
                                    if (count > 0) refreshCurrentUserTasks()
                                    count
                                },
                                onResetDefaultPreferences = {
                                    // Restauración instantánea en 0ms
                                    instantThemeId = UserPreferencesRepository.DEFAULT_THEME_ID
                                    instantDarkMode = UserPreferencesRepository.DEFAULT_DARK_MODE
                                    instantNotifications = UserPreferencesRepository.DEFAULT_NOTIFICATIONS
                                    instantLanguageCode = UserPreferencesRepository.DEFAULT_LANGUAGE
                                    instantColorblindMode = ColorblindMode.NONE
                                    instantFontScale = FontScaleMode.NORMAL
                                    instantHighContrast = false
                                    instantDyslexia = false
                                    instantLeftHanded = false
                                    instantReduceMotion = false
                                    instantLargeTouch = false
                                    instantZenFocus = false
                                    instantBorderStyle = BorderStyleMode.SUBTLE
                                    instantCornerStyle = CornerStyleMode.ROUNDED
                                    instantLayoutDensity = LayoutDensityMode.COMFORTABLE
                                    currentUser = activeUser.copy(
                                        themeId = UserPreferencesRepository.DEFAULT_THEME_ID
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateUserTheme(
                                            activeUser.id,
                                            UserPreferencesRepository.DEFAULT_THEME_ID
                                        )
                                        preferencesRepository.resetToDefaultPreferences()
                                    }
                                },
                                onToggleTask = { task ->
                                    // Actualización optimista inmediata en lista + Respuesta Háptica + Racha
                                    val becomingCompleted = !task.isCompleted
                                    val todayStr = LocalDate.now().toString()
                                    VoiceAndShareHelper.triggerHapticFeedback(context, instantHaptic)
                                    val idx = userTasks.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        userTasks[idx] = task.copy(
                                            isCompleted = becomingCompleted,
                                            kanbanStatus = if (becomingCompleted) 2 else 0,
                                            completedDate = if (becomingCompleted) todayStr else null
                                        )
                                    }
                                    if (becomingCompleted) {
                                        NotificationHelper.cancelTaskReminder(context, task.id)
                                        instantStreakDays += 1
                                        coroutineScope.launch(Dispatchers.IO) {
                                            preferencesRepository.incrementStreakDays()
                                        }
                                    }
                                    val nextDate = dbHelper.updateTaskCompletionWithRecurrence(
                                        task,
                                        becomingCompleted
                                    )
                                    if (nextDate != null) {
                                        refreshCurrentUserTasks()
                                        Toast.makeText(
                                            context,
                                            "🔄 Tarea recurrente: siguiente ocurrencia creada para $nextDate",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                onDeleteTask = { taskId ->
                                    NotificationHelper.cancelTaskReminder(context, taskId)
                                    val idx = userTasks.indexOfFirst { it.id == taskId }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(isDeleted = true)
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.softDeleteTask(taskId)
                                    }
                                    Toast.makeText(
                                        context,
                                        "Enviada a la Papelera de Reciclaje",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onTogglePin = { task ->
                                    dbHelper.updateTaskPin(task.id, !task.isPinned)
                                    refreshCurrentUserTasks()
                                },
                                onMoveTaskUp = { task ->
                                    dbHelper.moveTaskOrder(activeUser.id, task.id, moveUp = true)
                                    refreshCurrentUserTasks()
                                },
                                onMoveTaskDown = { task ->
                                    dbHelper.moveTaskOrder(activeUser.id, task.id, moveUp = false)
                                    refreshCurrentUserTasks()
                                },
                                onRestoreTask = { taskId ->
                                    val idx = userTasks.indexOfFirst { it.id == taskId }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(isDeleted = false)
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.restoreTaskFromTrash(taskId)
                                    }
                                },
                                onDeletePermanently = { taskId ->
                                    NotificationHelper.cancelTaskReminder(context, taskId)
                                    userTasks.removeAll { it.id == taskId }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.deleteTaskPermanently(taskId)
                                    }
                                },
                                onEmptyTrash = {
                                    userTasks.removeAll { it.isDeleted }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.emptyTrashForUser(activeUser.id)
                                    }
                                },
                                onAddTask = { title, desc, category, time, dueDate, colorHex, priority, recurrence, subtasksJson, isPinned, reminderEnabled ->
                                    dbHelper.insertTask(
                                        userId = activeUser.id,
                                        title = title,
                                        description = desc,
                                        category = category,
                                        time = time,
                                        dayIndex = 1,
                                        dueDate = dueDate,
                                        colorHex = colorHex,
                                        priority = priority,
                                        recurrence = recurrence,
                                        subtasksJson = subtasksJson,
                                        isPinned = isPinned,
                                        reminderEnabled = reminderEnabled
                                    )
                                    refreshCurrentUserTasks()
                                    if (reminderEnabled && instantNotifications) {
                                        userTasks.maxByOrNull { it.id }?.let { newest ->
                                            NotificationHelper.scheduleTaskReminder(context, newest)
                                        }
                                    }
                                },
                                onChangeTaskColor = { taskId, hex ->
                                    // Cambio de color instantáneo en 0ms en memoria + guardado en segundo plano
                                    val idx = userTasks.indexOfFirst { it.id == taskId }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(colorHex = hex)
                                    }
                                    if (selectedTask?.id == taskId) {
                                        selectedTask = selectedTask?.copy(colorHex = hex)
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskColor(taskId, hex)
                                    }
                                },
                                onAssignDateToTask = { task, targetDate ->
                                    val newIso = targetDate.toString()
                                    val idx = userTasks.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(dueDate = newIso)
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskSchedule(
                                            task.id,
                                            newIso,
                                            task.time
                                        )
                                    }
                                },
                                onOpenDetail = { task ->
                                    selectedTask = task
                                    currentScreen = AppScreen.TASK_DETAIL
                                },
                                onSwitchUserOrLogout = {
                                    lastDashboardTab = 0
                                    currentUser = null
                                    userTasks.clear()
                                    refreshUsers()
                                    currentScreen = AppScreen.LOGIN
                                    coroutineScope.launch(Dispatchers.IO) {
                                        preferencesRepository.clearUserSession()
                                    }
                                }
                            )
                        }
                    }

                    AppScreen.TASK_DETAIL -> {
                        val activeUser = currentUser
                        val currentTask = userTasks.find { it.id == selectedTask?.id } ?: selectedTask
                        if (currentTask != null) {
                            TaskDetailView(
                                theme = activeTheme,
                                task = currentTask,
                                allUserTasks = userTasks,
                                hasProAccess = currentProStatus.hasProAccess,
                                languageCode = instantLanguageCode,
                                colorblindMode = instantColorblindMode,
                                notificationsEnabled = instantNotifications,
                                onRequestProPaywall = { globalPaywallFeature = it },
                                onBack = { currentScreen = AppScreen.MAIN_DASHBOARD },
                                onToggleComplete = {
                                    val nextDate = dbHelper.updateTaskCompletionWithRecurrence(
                                        currentTask,
                                        !currentTask.isCompleted
                                    )
                                    refreshCurrentUserTasks()
                                    selectedTask = userTasks.find { it.id == currentTask.id }
                                    if (nextDate != null) {
                                        Toast.makeText(
                                            context,
                                            "🔄 Tarea recurrente: siguiente ocurrencia creada para $nextDate",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                onDelete = {
                                    NotificationHelper.cancelTaskReminder(context, currentTask.id)
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(isDeleted = true)
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.softDeleteTask(currentTask.id)
                                    }
                                    currentScreen = AppScreen.MAIN_DASHBOARD
                                },
                                onChangeColor = { hex ->
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(colorHex = hex)
                                    }
                                    selectedTask = currentTask.copy(colorHex = hex)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskColor(currentTask.id, hex)
                                    }
                                },
                                onUpdateSchedule = { date, time ->
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(dueDate = date, time = time)
                                    }
                                    selectedTask = currentTask.copy(dueDate = date, time = time)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskSchedule(currentTask.id, date, time)
                                    }
                                },
                                onUpdateSubtasksLive = { subtasksJson, autoCompleteFlag ->
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        val updated = userTasks[idx].copy(
                                            subtasksJson = subtasksJson,
                                            isCompleted = autoCompleteFlag ?: userTasks[idx].isCompleted
                                        )
                                        userTasks[idx] = updated
                                        selectedTask = updated
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskSubtasks(
                                            currentTask.id,
                                            subtasksJson,
                                            autoCompleteFlag
                                        )
                                    }
                                },
                                onAddFocusMinutes = { addedMins ->
                                    val newTotal = (currentTask.focusMinutes + addedMins).coerceAtLeast(0)
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(focusMinutes = newTotal)
                                    }
                                    selectedTask = currentTask.copy(focusMinutes = newTotal)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.addFocusMinutes(
                                            currentTask.id,
                                            currentTask.focusMinutes,
                                            addedMins
                                        )
                                    }
                                },
                                onUpdateAttachment = { path ->
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(attachmentImagePath = path)
                                    }
                                    selectedTask = currentTask.copy(attachmentImagePath = path)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskAttachment(currentTask.id, path)
                                    }
                                },
                                onUpdateBlocker = { blockerId ->
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        userTasks[idx] = userTasks[idx].copy(blockedByTaskId = blockerId)
                                    }
                                    selectedTask = currentTask.copy(blockedByTaskId = blockerId)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskBlocker(currentTask.id, blockerId)
                                    }
                                },
                                onUpdateKanbanStatus = { newStage ->
                                    val isDone = newStage == 2
                                    val completedIso = if (isDone) LocalDate.now().toString() else null
                                    val idx = userTasks.indexOfFirst { it.id == currentTask.id }
                                    if (idx != -1) {
                                        val updated = userTasks[idx].copy(
                                            kanbanStatus = newStage,
                                            isCompleted = isDone,
                                            completedDate = completedIso
                                        )
                                        userTasks[idx] = updated
                                        selectedTask = updated
                                    }
                                    coroutineScope.launch(Dispatchers.IO) {
                                        dbHelper.updateTaskKanbanStatus(currentTask.id, newStage)
                                    }
                                },
                                onSaveFullChanges = { title, desc, category, dueDate, time, colorHex, priority, recurrence, subtasksJson, isPinned, reminderEnabled ->
                                    dbHelper.updateTaskFull(
                                        taskId = currentTask.id,
                                        title = title,
                                        description = desc,
                                        category = category,
                                        dueDate = dueDate,
                                        time = time,
                                        colorHex = colorHex,
                                        priority = priority,
                                        recurrence = recurrence,
                                        subtasksJson = subtasksJson,
                                        isPinned = isPinned,
                                        reminderEnabled = reminderEnabled
                                    )
                                    refreshCurrentUserTasks()
                                    selectedTask = userTasks.find { it.id == currentTask.id }
                                },
                                onDuplicateTask = { copyTask ->
                                    if (activeUser != null) {
                                        dbHelper.insertTask(
                                            userId = activeUser.id,
                                            title = copyTask.title,
                                            description = copyTask.description,
                                            category = copyTask.category,
                                            time = copyTask.time,
                                            dayIndex = 1,
                                            dueDate = copyTask.dueDate,
                                            colorHex = copyTask.colorHex,
                                            priority = copyTask.priority,
                                            recurrence = copyTask.recurrence,
                                            subtasksJson = copyTask.subtasksJson,
                                            isPinned = copyTask.isPinned,
                                            reminderEnabled = copyTask.reminderEnabled,
                                            kanbanStatus = copyTask.kanbanStatus,
                                            attachmentImagePath = copyTask.attachmentImagePath,
                                            blockedByTaskId = copyTask.blockedByTaskId
                                        )
                                        refreshCurrentUserTasks()
                                    }
                                }
                            )
                        }
                    }
                }

                if (globalPaywallFeature != null) {
                    ProPaywallSheet(
                        theme = activeTheme,
                        featureName = globalPaywallFeature ?: "Funciones Pro",
                        onDismiss = { globalPaywallFeature = null },
                        onUnlockRewarded24h = {
                            val until = System.currentTimeMillis() + 86_400_000L
                            instantRewardedUntil = until
                            coroutineScope.launch(Dispatchers.IO) {
                                preferencesRepository.grantRewardedProAccess24h()
                            }
                            globalPaywallFeature = null
                            Toast.makeText(
                                context,
                                "🎁 ¡Pase Pro de 24 horas desbloqueado gratis!",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onSelectPlan = { plan ->
                            instantSubscriptionPlan = plan
                            coroutineScope.launch(Dispatchers.IO) {
                                preferencesRepository.setSubscriptionPlan(plan)
                            }
                            globalPaywallFeature = null
                            Toast.makeText(
                                context,
                                "⭐ ¡Plan ${plan.labelEs} activado correctamente!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
