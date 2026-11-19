package com.example.todolist.ui.screens

import com.example.todolist.ui.components.AutoTranslatedText

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.AppLanguage
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.CornerStyleMode
import com.example.todolist.model.FontScaleMode
import com.example.todolist.model.LayoutDensityMode
import com.example.todolist.model.ProStatus
import com.example.todolist.model.SubscriptionPlan
import com.example.todolist.model.TodoTask
import com.example.todolist.model.UserAccount
import com.example.todolist.ui.components.ThemePreviewCard
import com.example.todolist.ui.components.UserAvatarComponent
import com.example.todolist.ui.theme.AccentGreen
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.AvailableThemes
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.TaskColorChoices
import com.example.todolist.ui.theme.adaptColorForColorblindMode
import com.example.todolist.ui.theme.appTextFieldColors
import com.example.todolist.util.LocalizedStrings
import com.example.todolist.util.NotificationHelper
import com.example.todolist.util.exportTasksBackupString

@Composable
fun SettingsAndPreferencesTab(
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
    proStatus: ProStatus = ProStatus(),
    onOpenProPaywall: (String) -> Unit = {},
    onUpdateUserPin: (String?) -> Unit = {},
    onExportJsonUri: (Uri) -> Unit = {},
    onImportJsonUri: (Uri) -> Unit = {},
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
    onImportBackupTasks: (String) -> Int = { 0 },
    onResetDefaultPreferences: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activeTasks = tasks.filter { !it.isDeleted }
    val completed = activeTasks.count { it.isCompleted }
    val pending = activeTasks.size - completed

    var showLanguageDropdownList by remember { mutableStateOf(false) }
    var languageSearchQuery by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var draftPinCode by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf("") }

    val currentLanguageObj: AppLanguage = remember(selectedLanguageCode) {
        AppLanguage.fromCode(selectedLanguageCode)
    }
    val copy: SettingsTranslations = remember<SettingsTranslations>(currentLanguageObj) {
        settingsTranslations(currentLanguageObj)
    }
    val filteredLanguages: List<AppLanguage> = remember(languageSearchQuery) {
        if (languageSearchQuery.isBlank()) AppLanguage.entries
        else AppLanguage.entries.filter { lang: AppLanguage ->
            lang.displayName.contains(languageSearchQuery, ignoreCase = true) ||
                lang.code.contains(languageSearchQuery, ignoreCase = true)
        }
    }

    val sectionBorder: BorderStroke? = if (theme.borderStyle == BorderStyleMode.NONE && !highContrastEnabled) {
        null
    } else {
        BorderStroke(
            width = theme.borderStyle.widthDp.coerceAtLeast(0.8f).dp,
            color = theme.surfaceBorder
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.sendTestNotification(context, user.name)
            Toast.makeText(context, "Notificación de prueba enviada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportJsonUri(uri)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportJsonUri(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(theme.spacingDp.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 95.dp)
    ) {
        // Encabezado
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = strings.settingsHeaderTitle,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
                Text(
                    text = strings.settingsHeaderSubtitle,
                    fontSize = 13.sp,
                    color = theme.textSecondary
                )
            }
        }

        // ==================== TARJETA TODOLIST PRO / PASE RECOMPENSADO 24H ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = BorderStroke(1.5.dp, theme.primaryAccent),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .clip(CircleShape)
                                .background(theme.primaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (proStatus.hasProAccess) "⭐ ToDoList Pro Activo" else "ToDoList Plan Gratuito",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.textPrimary
                            )
                            val statusDesc = when {
                                proStatus.plan != SubscriptionPlan.FREE ->
                                    "${proStatus.plan.labelEs} (${proStatus.plan.priceTag})"
                                proStatus.isRewardedActive -> {
                                    val minsLeft = ((proStatus.rewardedUnlockUntilMillis - System.currentTimeMillis()) / 60_000L).coerceAtLeast(1L)
                                    val hoursLeft = minsLeft / 60
                                    "🎁 Pase 24h activo (${hoursLeft}h ${minsLeft % 60}m restantes)"
                                }
                                else -> "Desbloquea Kanban, PIN, .JSON y Fotos gratis por 24h o con Pro"
                            }
                            Text(
                                text = statusDesc,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (proStatus.hasProAccess) AccentGreen else theme.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onOpenProPaywall("Suscripción ToDoList Pro / Pase 24h") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (proStatus.hasProAccess) "Ver Plan" else "Mejorar ⭐",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Tarjeta de Perfil, Foto, Bóveda PIN y Respaldo Rápido (.JSON y Portapapeles)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = sectionBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatarComponent(
                        name = user.name,
                        imagePath = user.profileImagePath,
                        backgroundColor = theme.primaryAccent,
                        size = 86.dp,
                        fontSize = 34.sp,
                        showEditBadge = true,
                        onClick = onPickProfilePhoto
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                    Text(text = user.email, fontSize = 13.sp, color = theme.textSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onPickProfilePhoto,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.primaryAccent)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                strings.changePhoto,
                                color = theme.primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!user.profileImagePath.isNullOrBlank()) {
                            TextButton(onClick = onRemoveProfilePhoto) {
                                Text(strings.removePhoto, color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bóveda de Seguridad con PIN de 4 dígitos
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.bgColor,
                        border = sectionBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (!user.pinCode.isNullOrBlank()) AccentGreen else theme.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    AutoTranslatedText(text = "Bóveda PIN de 4 Dígitos",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = if (!user.pinCode.isNullOrBlank())
                                            "🔒 Perfil protegido con PIN al cambiar cuenta"
                                        else
                                            "Sin PIN configurado",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!user.pinCode.isNullOrBlank()) {
                                    TextButton(
                                        onClick = {
                                            onUpdateUserPin(null)
                                            Toast.makeText(
                                                context,
                                                "PIN eliminado del perfil",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ) {
                                        AutoTranslatedText("Quitar", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        if (!proStatus.hasProAccess) {
                                            onOpenProPaywall("Bóveda PIN de 4 Dígitos")
                                        } else {
                                            draftPinCode = user.pinCode.orEmpty()
                                            pinDialogError = ""
                                            showPinDialog = true
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (!user.pinCode.isNullOrBlank()) "Cambiar PIN" else "Activar PIN ⭐",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape((theme.cornerRadiusDp - 4).coerceAtLeast(6).dp),
                            color = theme.bgColor,
                            border = sectionBorder
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$completed",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentGreen
                                )
                                Text(
                                    text = strings.completedLabel,
                                    fontSize = 12.sp,
                                    color = theme.textSecondary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape((theme.cornerRadiusDp - 4).coerceAtLeast(6).dp),
                            color = theme.bgColor,
                            border = sectionBorder
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$pending",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.primaryAccent
                                )
                                Text(
                                    text = strings.pendingLabel,
                                    fontSize = 12.sp,
                                    color = theme.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val backupData = exportTasksBackupString(tasks)
                                clipboard?.setPrimaryClip(ClipData.newPlainText("ToDoListBackup", backupData))
                                Toast.makeText(
                                    context,
                                    "Respaldo de ${activeTasks.size} tareas copiado al portapapeles",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoTranslatedText("Copiar Respaldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val raw = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                val count = onImportBackupTasks(raw)
                                if (count > 0) {
                                    Toast.makeText(
                                        context,
                                        "¡Se importaron $count tareas desde el portapapeles!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No se encontró un respaldo válido en el portapapeles",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoTranslatedText("Pegar Respaldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Exportar e Importar en Archivo Físico .JSON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!proStatus.hasProAccess) {
                                    onOpenProPaywall("Exportación en Archivo Físico .JSON")
                                } else {
                                    exportJsonLauncher.launch("respaldo_todolist_${user.name.lowercase().replace(" ", "_")}.json")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoTranslatedText("Guardar .JSON ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!proStatus.hasProAccess) {
                                    onOpenProPaywall("Importación desde Archivo Físico .JSON")
                                } else {
                                    importJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.primaryAccent)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoTranslatedText("Abrir .JSON ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryAccent)
                        }
                    }
                }
            }
        }

        // ==================== BLOQUE 1: PREFERENCIAS DE ANDROID DATASTORE ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = sectionBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = strings.dataStoreSectionTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryAccent
                        )
                        Text(
                            text = strings.dataStoreSectionSubtitle,
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 1. Estado de Sesión Persistente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.sessionPersistenceTitle} (ID #${user.id})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = strings.sessionPersistenceActiveDesc,
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 2. Selector e Interruptor de Modo Claro y Oscuro
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(theme.secondaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.DarkMode
                                        else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.darkModeTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = strings.darkModeSubtitle(isDarkMode),
                                        fontSize = 12.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = theme.primaryAccent
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.bgColor)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isDarkMode) theme.primaryAccent else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleDarkMode(false) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (!isDarkMode) Color.White else theme.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.lightModeOption,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isDarkMode) Color.White else theme.textSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) theme.primaryAccent else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleDarkMode(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color.White else theme.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.darkModeOption,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color.White else theme.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 3. Activar / Desactivar Notificaciones + Botón de Prueba Local
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(theme.secondaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (notificationsEnabled)
                                            Icons.Default.NotificationsActive
                                        else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.notificationsTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = strings.notificationsSubtitle(notificationsEnabled),
                                        fontSize = 12.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { onToggleNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = theme.primaryAccent
                                )
                            )
                        }

                        if (notificationsEnabled) {
                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val sent = NotificationHelper.sendTestNotification(context, user.name)
                                        if (!sent) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            Toast.makeText(context, "¡Notificación enviada a la barra de estado!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        NotificationHelper.sendTestNotification(context, user.name)
                                        Toast.makeText(context, "¡Notificación enviada a la barra de estado!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                        Text(copy.testNotification, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 4. SELECTOR DE IDIOMA COMPACTO Y DESPLEGABLE (24 IDIOMAS SIN ESTORBAR)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = theme.bgColor,
                            border = sectionBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageDropdownList = !showLanguageDropdownList }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(theme.secondaryAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = theme.primaryAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = strings.appLanguageTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary
                                        )
                                        Text(
                                            text = "Actual: ${currentLanguageObj.flagEmoji} ${currentLanguageObj.displayName} (${currentLanguageObj.code.uppercase()})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.primaryAccent
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.secondaryAccent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (showLanguageDropdownList) "Ocultar" else "Lista (24)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.primaryAccent
                                        )
                                        Icon(
                                            imageVector = if (showLanguageDropdownList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = theme.primaryAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Carrusel horizontal compacto de 1 sola línea para cambio rápido sin ocupar espacio vertical
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AppLanguage.entries) { lang ->
                                val selected = currentLanguageObj == lang
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) theme.primaryAccent else theme.bgColor,
                                    border = if (selected) null else sectionBorder,
                                    modifier = Modifier.clickable { onSelectLanguage(lang.code) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = lang.flagEmoji, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = lang.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else theme.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Lista desplegable compacta con scroll propio cuando el usuario toca "Lista (24)"
                        if (showLanguageDropdownList) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.bgColor,
                                border = BorderStroke(1.dp, theme.primaryAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    OutlinedTextField(
                                        value = languageSearchQuery,
                                        onValueChange = { languageSearchQuery = it },
                                        placeholder = {
                                            AutoTranslatedText("Buscar idioma...",
                                                fontSize = 12.sp,
                                                color = theme.textSecondary
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                tint = theme.primaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = appTextFieldColors(
                                            theme = theme,
                                            accent = theme.primaryAccent,
                                            containerColor = theme.cardColor
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredLanguages, key = { it.code }) { lang ->
                                            val selected = currentLanguageObj == lang
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (selected) theme.primaryAccent else theme.cardColor,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onSelectLanguage(lang.code)
                                                        showLanguageDropdownList = false
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = lang.flagEmoji, fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = lang.displayName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (selected) Color.White else theme.textPrimary
                                                        )
                                                    }
                                                    Text(
                                                        text = if (lang.isRtl) "${lang.code.uppercase()} • RTL" else lang.code.uppercase(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (selected) Color.White.copy(alpha = 0.9f) else theme.textSecondary
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

        // ==================== BLOQUE 2: PERSONALIZACIÓN DEL ENTORNO Y BORDES ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = sectionBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = copy.environmentTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryAccent
                        )
                        Text(
                            text = copy.environmentSubtitle,
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // A. Intensidad de los Bordes / Marcos (Nulo, Suave, Medio, Fuerte)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.BorderStyle,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = copy.borderTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BorderStyleMode.entries.forEach { mode ->
                                val selected = borderStyleMode == mode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) theme.primaryAccent else theme.bgColor,
                                    border = if (mode == BorderStyleMode.NONE && !selected) null else BorderStroke(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) theme.primaryAccent else theme.surfaceBorder.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectBorderStyleMode(mode) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = copy.borderLabels.getValue(mode),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (selected) Color.White else theme.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (mode == BorderStyleMode.NONE) "0 dp" else "${mode.widthDp} dp",
                                            fontSize = 9.sp,
                                            color = if (selected) Color.White.copy(alpha = 0.85f) else theme.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = copy.borderDescriptions.getValue(borderStyleMode),
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // B. Forma de Esquinas de Tarjetas (Recto Minimalista, Redondeado, Burbuja)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.RoundedCorner,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = copy.cornerTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CornerStyleMode.entries.forEach { corner ->
                                val selected = cornerStyleMode == corner
                                Surface(
                                    shape = RoundedCornerShape(corner.radiusDp.dp),
                                    color = if (selected) theme.primaryAccent else theme.bgColor,
                                    border = sectionBorder,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectCornerStyleMode(corner) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = copy.cornerLabels.getValue(corner),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else theme.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // C. Densidad y Espaciado del Entorno (Compacto, Cómodo, Amplio)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ViewAgenda,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = copy.densityTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LayoutDensityMode.entries.forEach { density ->
                                val selected = layoutDensityMode == density
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) theme.primaryAccent else theme.bgColor,
                                    border = sectionBorder,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectLayoutDensityMode(density) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = copy.densityLabels.getValue(density),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else theme.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== BLOQUE 3: CENTRO DE ACCESIBILIDAD EXPANDIDO (11 HERRAMIENTAS) ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                border = sectionBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(theme.secondaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = copy.accessibilityTitle,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.primaryAccent
                            )
                            Text(
                                text = copy.accessibilitySubtitle,
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 1 y 2. Modo para Daltónicos + Doble Codificación Visual (Compacto en Carrusel Horizontal)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = copy.colorVisionTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.bgColor)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TaskColorChoices.forEach { choice ->
                                val previewColor = adaptColorForColorblindMode(
                                    choice.color,
                                    colorblindMode
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(previewColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = choice.symbol,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = choice.name.take(3),
                                        fontSize = 9.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ColorblindMode.entries) { mode ->
                                val isSelected = colorblindMode == mode
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) theme.primaryAccent else theme.bgColor,
                                    border = sectionBorder,
                                    modifier = Modifier
                                        .width(175.dp)
                                        .clickable { onSelectColorblindMode(mode) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = mode.labelEs,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else theme.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mode.descriptionEs,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else theme.textSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 3. 5 Niveles de Escalado Tipográfico (90% a 145% Máximo Baja Visión)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                        text = copy.typographyTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(FontScaleMode.entries) { scaleOption ->
                                val selected = fontScaleMode == scaleOption
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) theme.primaryAccent else theme.bgColor,
                                    border = sectionBorder,
                                    modifier = Modifier.clickable { onSelectFontScaleMode(scaleOption) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = scaleOption.labelEs,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else theme.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // 4. Modo de Alto Contraste
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.Contrast,
                        title = copy.accessibilityItems[0].first,
                        subtitle = copy.accessibilityItems[0].second,
                        checked = highContrastEnabled,
                        onCheckedChange = onToggleHighContrast
                    )

                    // 5. Modo Lectura Fácil / Dislexia
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.FormatSize,
                        title = copy.accessibilityItems[1].first,
                        subtitle = copy.accessibilityItems[1].second,
                        checked = dyslexiaFriendlyEnabled,
                        onCheckedChange = onToggleDyslexiaFriendly
                    )

                    // 6. Modo Zurdo / Una Sola Mano
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.PanTool,
                        title = copy.accessibilityItems[2].first,
                        subtitle = copy.accessibilityItems[2].second,
                        checked = leftHandedMode,
                        onCheckedChange = onToggleLeftHandedMode
                    )

                    // 7. Reducción de Movimiento / Fotosensibilidad
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.MotionPhotosOff,
                        title = copy.accessibilityItems[3].first,
                        subtitle = copy.accessibilityItems[3].second,
                        checked = reduceMotion,
                        onCheckedChange = onToggleReduceMotion
                    )

                    // 8. Objetivos Táctiles Ampliados / Accesibilidad Motora
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.TouchApp,
                        title = copy.accessibilityItems[4].first,
                        subtitle = copy.accessibilityItems[4].second,
                        checked = largeTouchTargets,
                        onCheckedChange = onToggleLargeTouchTargets
                    )

                    // 9. Modo Enfoque Zen / TDAH
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.SelfImprovement,
                        title = copy.accessibilityItems[5].first,
                        subtitle = copy.accessibilityItems[5].second,
                        checked = zenFocusMode,
                        onCheckedChange = onToggleZenFocusMode
                    )

                    // 10. Respuesta Háptica Táctil (Vibración Suave)
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.Vibration,
                        title = copy.accessibilityItems[6].first,
                        subtitle = copy.accessibilityItems[6].second,
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = onToggleHapticFeedback
                    )

                    // 11. Modo Privacidad / Ocultar Títulos Sensibles
                    AccessibilitySwitchItem(
                        theme = theme,
                        icon = Icons.Default.VisibilityOff,
                        title = copy.accessibilityItems[7].first,
                        subtitle = copy.accessibilityItems[7].second,
                        checked = privacyBlurMode,
                        onCheckedChange = onTogglePrivacyBlur
                    )

                    HorizontalDivider(color = theme.surfaceBorder.copy(alpha = 0.3f))

                    // Botón para Restablecer la Configuración Predeterminada
                    OutlinedButton(
                        onClick = {
                            onResetDefaultPreferences()
                            Toast.makeText(
                                context,
                                strings.resetDefaultsDoneToast,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, DangerRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.resetDefaultsButton,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 5 Combinaciones con Blanco (Modo Claro)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${strings.lightPalettesTitle} (5 Paletas)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AvailableThemes.filter { !it.isDark }, key = { it.id }) { itemTheme ->
                        ThemePreviewCard(
                            itemTheme = itemTheme,
                            isSelected = itemTheme.id == theme.id,
                            onClick = { onSelectTheme(itemTheme.id) }
                        )
                    }
                }
            }
        }

        // 5 Combinaciones con Negro AMOLED (Modo Oscuro)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${strings.darkPalettesTitle} (5 Paletas AMOLED)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AvailableThemes.filter { it.isDark }, key = { it.id }) { itemTheme ->
                        ThemePreviewCard(
                            itemTheme = itemTheme,
                            isSelected = itemTheme.id == theme.id,
                            onClick = { onSelectTheme(itemTheme.id) }
                        )
                    }
                }
            }
        }

        // Botón para Cerrar Sesión / Cambiar Cuenta
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(theme.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryAccent)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.switchOrLogoutButton,
                    color = theme.primaryAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Modal para configurar el PIN de 4 dígitos
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = theme.cardColor,
            title = {
                AutoTranslatedText(text = "🔒 Configurar PIN de 4 Dígitos",
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Este PIN se solicitará antes de abrir el perfil de ${user.name} desde la pantalla de selección de cuentas.",
                        fontSize = 13.sp,
                        color = theme.textSecondary
                    )
                    OutlinedTextField(
                        value = draftPinCode,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                draftPinCode = it
                                pinDialogError = ""
                            }
                        },
                        label = { AutoTranslatedText("PIN de 4 dígitos", color = theme.textSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinDialogError.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(
                            theme = theme,
                            accent = theme.primaryAccent,
                            isError = pinDialogError.isNotEmpty(),
                            containerColor = theme.bgColor
                        )
                    )
                    if (pinDialogError.isNotEmpty()) {
                        Text(
                            text = pinDialogError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (draftPinCode.length == 4) {
                            onUpdateUserPin(draftPinCode)
                            showPinDialog = false
                            Toast.makeText(
                                context,
                                "🔒 PIN de 4 dígitos guardado en el perfil",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pinDialogError = "⚠️ El PIN debe tener exactamente 4 números."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                ) {
                    AutoTranslatedText("Guardar PIN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    AutoTranslatedText("Cancelar", color = theme.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun AccessibilitySwitchItem(
    theme: AppThemePalette,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = theme.textSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = theme.primaryAccent
            )
        )
    }
}

internal data class SettingsTranslations(
    val testNotification: String,
    val environmentTitle: String,
    val environmentSubtitle: String,
    val borderTitle: String,
    val borderLabels: Map<BorderStyleMode, String>,
    val borderDescriptions: Map<BorderStyleMode, String>,
    val cornerTitle: String,
    val cornerLabels: Map<CornerStyleMode, String>,
    val densityTitle: String,
    val densityLabels: Map<LayoutDensityMode, String>,
    val accessibilityTitle: String,
    val accessibilitySubtitle: String,
    val colorVisionTitle: String,
    val typographyTitle: String,
    val accessibilityItems: List<Pair<String, String>>
)

internal fun settingsTranslations(language: AppLanguage): SettingsTranslations = when (language) {
    AppLanguage.ZH_CN -> SettingsTranslations(
        testNotification = "发送测试通知",
        environmentTitle = "界面与边框自定义",
        environmentSubtitle = "调整边框强度、卡片形状和界面密度，打造专属体验",
        borderTitle = "边框样式与强度",
        borderLabels = mapOf(
            BorderStyleMode.NONE to "无",
            BorderStyleMode.SUBTLE to "轻柔",
            BorderStyleMode.MEDIUM to "中等",
            BorderStyleMode.STRONG to "强烈"
        ),
        borderDescriptions = mapOf(
            BorderStyleMode.NONE to "卡片不显示外框",
            BorderStyleMode.SUBTLE to "细腻柔和的边框，不会造成视觉干扰",
            BorderStyleMode.MEDIUM to "边框清晰，强度均衡",
            BorderStyleMode.STRONG to "粗边框，最高对比度"
        ),
        cornerTitle = "卡片与容器形状",
        cornerLabels = mapOf(
            CornerStyleMode.SHARP to "简约直角",
            CornerStyleMode.ROUNDED to "经典圆角",
            CornerStyleMode.PILL to "超圆润气泡"
        ),
        densityTitle = "界面布局密度",
        densityLabels = mapOf(
            LayoutDensityMode.COMPACT to "紧凑",
            LayoutDensityMode.COMFORTABLE to "舒适",
            LayoutDensityMode.SPACIOUS to "宽松"
        ),
        accessibilityTitle = "视觉、认知与行动无障碍中心",
        accessibilitySubtitle = "永久免费 • 无障碍工具已保存至 DataStore",
        colorVisionTitle = "1–2. 色觉辅助与几何符号",
        typographyTitle = "3. 字体缩放（5 个级别，最高 145%）",
        accessibilityItems = listOf(
            "4. 文字与边框高对比" to "将次要文字调整为清晰易读",
            "5. 易读 / 阅读障碍模式" to "增加字母间距、行高和字重",
            "6. 左手 / 单手模式" to "将浮动按钮（+）移到屏幕左侧",
            "7. 减少动态效果（光敏）" to "关闭滑动和动画，减少眩晕不适",
            "8. 加大触控区域（52dp+）" to "放大按钮、复选框和间距，提升操作精度",
            "9. 禅意专注 / ADHD 模式" to "隐藏次要内容，仅显示最紧急的 3 项",
            "10. 触觉反馈（振动）" to "完成任务或更改设置时提供触觉确认",
            "11. 公共视图隐私模式" to "在概览屏幕隐藏任务标题"
        )
    )
    AppLanguage.ZH_TW -> SettingsTranslations(
        testNotification = "傳送測試通知",
        environmentTitle = "介面與邊框自訂",
        environmentSubtitle = "調整邊框強度、卡片形狀與介面密度，打造專屬體驗",
        borderTitle = "邊框樣式與強度",
        borderLabels = mapOf(BorderStyleMode.NONE to "無", BorderStyleMode.SUBTLE to "柔和", BorderStyleMode.MEDIUM to "中等", BorderStyleMode.STRONG to "強烈"),
        borderDescriptions = mapOf(BorderStyleMode.NONE to "卡片不顯示外框", BorderStyleMode.SUBTLE to "細緻柔和的邊框", BorderStyleMode.MEDIUM to "邊框清晰且強度均衡", BorderStyleMode.STRONG to "粗邊框，最高對比度"),
        cornerTitle = "卡片與容器形狀",
        cornerLabels = mapOf(CornerStyleMode.SHARP to "簡約直角", CornerStyleMode.ROUNDED to "經典圓角", CornerStyleMode.PILL to "超圓潤氣泡"),
        densityTitle = "介面版面密度",
        densityLabels = mapOf(LayoutDensityMode.COMPACT to "緊湊", LayoutDensityMode.COMFORTABLE to "舒適", LayoutDensityMode.SPACIOUS to "寬敞"),
        accessibilityTitle = "視覺、認知與行動無障礙中心",
        accessibilitySubtitle = "永久免费 • 無障礙工具已儲存至 DataStore",
        colorVisionTitle = "1–2. 色覺輔助與幾何符號",
        typographyTitle = "3. 字體縮放（5 個級別，最高 145%）",
        accessibilityItems = listOf(
            "4. 文字與邊框高對比" to "讓次要文字清晰易讀",
            "5. 易讀 / 閱讀障礙模式" to "增加字母間距、行高與字重",
            "6. 左手 / 單手模式" to "將浮動按鈕（+）移至螢幕左側",
            "7. 減少動態效果（光敏）" to "關閉滑動與動畫，減少暈眩不適",
            "8. 加大觸控區域（52dp+）" to "放大按鈕、核取方塊與間距，提升操作精確度",
            "9. 禪意專注 / ADHD 模式" to "隱藏次要內容，只顯示最緊急的 3 項",
            "10. 觸覺回饋（震動）" to "完成任務或變更設定時提供觸覺確認",
            "11. 公開檢視隱私模式" to "在總覽畫面隱藏任務標題"
        )
    )
    AppLanguage.EN -> SettingsTranslations(
        testNotification = "Send test notification",
        environmentTitle = "Interface and Border Customization",
        environmentSubtitle = "Adjust border strength, card shapes, and layout density to make the app your own",
        borderTitle = "Border Style and Strength",
        borderLabels = mapOf(BorderStyleMode.NONE to "None", BorderStyleMode.SUBTLE to "Subtle", BorderStyleMode.MEDIUM to "Medium", BorderStyleMode.STRONG to "Strong"),
        borderDescriptions = mapOf(BorderStyleMode.NONE to "Cards without outer borders", BorderStyleMode.SUBTLE to "Thin, soft borders that stay out of the way", BorderStyleMode.MEDIUM to "Clear borders with balanced emphasis", BorderStyleMode.STRONG to "Thick, high-contrast borders"),
        cornerTitle = "Card and Container Shapes",
        cornerLabels = mapOf(CornerStyleMode.SHARP to "Minimal square", CornerStyleMode.ROUNDED to "Classic rounded", CornerStyleMode.PILL to "Extra round / Bubble"),
        densityTitle = "Interface Layout Density",
        densityLabels = mapOf(LayoutDensityMode.COMPACT to "Compact", LayoutDensityMode.COMFORTABLE to "Comfortable", LayoutDensityMode.SPACIOUS to "Spacious"),
        accessibilityTitle = "Visual, Cognitive, and Motor Accessibility",
        accessibilitySubtitle = "Always free • Inclusive tools saved in DataStore",
        colorVisionTitle = "1–2. Color vision support and geometric symbols",
        typographyTitle = "3. Font scaling (5 levels up to 145%)",
        accessibilityItems = listOf(
            "4. High contrast text and borders" to "Make secondary text fully clear",
            "5. Easy reading / dyslexia mode" to "Increase letter spacing, line height, and weight",
            "6. Left-handed / one-handed mode" to "Move the floating (+) button to the left side",
            "7. Reduce motion (photosensitivity)" to "Disable swipes and animations to reduce discomfort",
            "8. Larger touch targets (52dp+)" to "Enlarge buttons, checkboxes, and spacing for accuracy",
            "9. Zen focus / ADHD mode" to "Hide secondary items and show only the top 3 urgent tasks",
            "10. Haptic feedback (vibration)" to "Confirm task completion and setting changes with a vibration",
            "11. Privacy mode for public view" to "Hide task titles on the Summary screen"
        )
    )
    else -> SettingsTranslations(
        testNotification = "Enviar notificación de prueba",
        environmentTitle = "Personalización de Entorno y Marcos",
        environmentSubtitle = "Ajusta la intensidad de los bordes, la forma de las tarjetas y la densidad visual para que tu entorno sea único",
        borderTitle = "Estilo e Intensidad de Bordes",
        borderLabels = mapOf(BorderStyleMode.NONE to "Nulo", BorderStyleMode.SUBTLE to "Suave", BorderStyleMode.MEDIUM to "Medio", BorderStyleMode.STRONG to "Fuerte"),
        borderDescriptions = BorderStyleMode.entries.associateWith { it.descriptionEs },
        cornerTitle = "Geometría de Tarjetas y Contenedores",
        cornerLabels = CornerStyleMode.entries.associateWith { it.labelEs },
        densityTitle = "Densidad Visual del Entorno",
        densityLabels = LayoutDensityMode.entries.associateWith { it.labelEs },
        accessibilityTitle = "Centro de Accesibilidad Visual, Cognitiva y Motora",
        accessibilitySubtitle = "100% Gratuito siempre • Herramientas inclusivas persistidas en DataStore",
        colorVisionTitle = "1–2. Modo para Daltónicos y Símbolos Geométricos",
        typographyTitle = "3. Escalado Tipográfico (5 Niveles hasta 145%)",
        accessibilityItems = listOf(
            "4. Contraste Máximo de Textos y Marcos" to "Fuerza texto secundario al 100% de nitidez",
            "5. Modo Lectura Fácil / Dislexia" to "Incrementa espaciado de letras, altura de línea y grosor",
            "6. Modo Zurdo / Una Sola Mano" to "Mueve el botón flotante (+) al lado izquierdo de la pantalla",
            "7. Reducción de Movimiento (Fotosensibilidad)" to "Desactiva barridos y animaciones para evitar fatiga vestibular",
            "8. Objetivos Táctiles Ampliados (52dp+)" to "Agranda botones, checkboxes y separación para precisión motora",
            "9. Modo Enfoque Zen / TDAH" to "Oculta elementos secundarios y muestra solo el Top 3 urgente",
            "10. Respuesta Háptica Táctil (Vibración)" to "Confirmación física al completar tareas y cambiar ajustes",
            "11. Modo Privacidad en Vista Pública" to "Enmascara los títulos de tus tareas en la pantalla de Resumen"
        )
    )
}
