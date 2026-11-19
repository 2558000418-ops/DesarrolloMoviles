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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.model.UserAccount
import com.example.todolist.ui.components.UserAvatarComponent
import com.example.todolist.ui.theme.AppThemePalette
import com.example.todolist.ui.theme.AvailableThemes
import com.example.todolist.ui.theme.DangerRed
import com.example.todolist.ui.theme.appTextFieldColors

@Composable
fun AuthAndUserPickerScreen(
    theme: AppThemePalette,
    savedUsers: List<UserAccount>,
    onSelectSavedUser: (UserAccount) -> Unit,
    onDeleteSavedUser: (UserAccount) -> Unit,
    onLoginAttempt: (String, String) -> Boolean,
    onRegisterAttempt: (String, String, String) -> Boolean
) {
    var showSavedAccountsList by remember(savedUsers.size) {
        mutableStateOf(savedUsers.isNotEmpty())
    }
    var isLoginMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    var pendingPinUser by remember { mutableStateOf<UserAccount?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinErrorMsg by remember { mutableStateOf("") }

    if (pendingPinUser != null) {
        val targetUser = pendingPinUser!!
        AlertDialog(
            onDismissRequest = {
                pendingPinUser = null
                enteredPin = ""
                pinErrorMsg = ""
            },
            containerColor = theme.cardColor,
            title = {
                Text(
                    text = "🔒 Bóveda PIN: ${targetUser.name}",
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutoTranslatedText(text = "Ingresa el PIN de 4 dígitos para acceder a este perfil:",
                        fontSize = 13.sp,
                        color = theme.textSecondary
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                enteredPin = it
                                pinErrorMsg = ""
                            }
                        },
                        label = { AutoTranslatedText("PIN de 4 dígitos", color = theme.textSecondary) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors(theme = theme, containerColor = theme.bgColor)
                    )
                    if (pinErrorMsg.isNotEmpty()) {
                        Text(
                            text = pinErrorMsg,
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == targetUser.pinCode) {
                            pendingPinUser = null
                            enteredPin = ""
                            pinErrorMsg = ""
                            onSelectSavedUser(targetUser)
                        } else {
                            pinErrorMsg = "PIN incorrecto. Inténtalo de nuevo."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
                ) {
                    AutoTranslatedText("Desbloquear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingPinUser = null
                        enteredPin = ""
                        pinErrorMsg = ""
                    }
                ) {
                    AutoTranslatedText("Cancelar", color = theme.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bgColor)
            .statusBarsPadding()
            .padding(horizontal = 26.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(theme.primaryAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Usuarios",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (showSavedAccountsList && savedUsers.isNotEmpty()) {
            AutoTranslatedText(text = "Usuarios",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            AutoTranslatedText(text = "Selecciona tu perfil para iniciar sesión persistente en DataStore",
                fontSize = 13.sp,
                color = theme.textSecondary
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "SESIONES GUARDADAS (${savedUsers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(savedUsers, key = { it.id }) { account ->
                    val userPalette =
                        AvailableThemes.find { it.id == account.themeId } ?: AvailableThemes[0]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!account.pinCode.isNullOrBlank()) {
                                    pendingPinUser = account
                                    enteredPin = ""
                                    pinErrorMsg = ""
                                } else {
                                    onSelectSavedUser(account)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatarComponent(
                                name = account.name,
                                imagePath = account.profileImagePath,
                                backgroundColor = userPalette.primaryAccent,
                                size = 48.dp,
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!account.pinCode.isNullOrBlank()) "${account.name} 🔒" else account.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "${account.email} • ${userPalette.name}",
                                    fontSize = 12.sp,
                                    color = theme.textSecondary
                                )
                            }

                            IconButton(onClick = { onDeleteSavedUser(account) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar sesión",
                                    tint = theme.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    isLoginMode = false
                    name = ""
                    email = ""
                    password = ""
                    errorMsg = ""
                    showSavedAccountsList = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, theme.primaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                AutoTranslatedText(text = "Agregar otro usuario",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = if (isLoginMode) "Iniciar Sesión" else "Agregar Nuevo Usuario",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isLoginMode) "Ingresa con tu correo y contraseña"
                else "La sesión y preferencias se guardarán en Android DataStore",
                fontSize = 14.sp,
                color = theme.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceBorder)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isLoginMode) theme.cardColor else Color.Transparent)
                        .clickable {
                            isLoginMode = false
                            errorMsg = ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AutoTranslatedText(text = "Nuevo Usuario",
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isLoginMode) theme.primaryAccent else theme.textSecondary,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLoginMode) theme.cardColor else Color.Transparent)
                        .clickable {
                            isLoginMode = true
                            errorMsg = ""
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AutoTranslatedText(text = "Ya tengo cuenta",
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLoginMode) theme.primaryAccent else theme.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { AutoTranslatedText("Nombre del usuario", color = theme.textSecondary) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.primaryAccent
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(theme = theme, containerColor = theme.cardColor)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { AutoTranslatedText("Correo electrónico", color = theme.textSecondary) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = theme.primaryAccent
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(theme = theme, containerColor = theme.cardColor)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { AutoTranslatedText("Contraseña", color = theme.textSecondary) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = theme.primaryAccent
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(theme = theme, containerColor = theme.cardColor)
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMsg,
                    color = DangerRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        if (email.isBlank() || password.isBlank()) {
                            errorMsg = "Por favor ingresa tu correo y contraseña."
                        } else {
                            val ok = onLoginAttempt(email, password)
                            if (!ok) errorMsg = "Credenciales incorrectas o usuario no encontrado."
                        }
                    } else {
                        if (name.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMsg = "Completa todos los campos para registrar al usuario."
                        } else {
                            val ok = onRegisterAttempt(name, email, password)
                            if (!ok) errorMsg =
                                "Ese correo ya está registrado. Usa 'Ya tengo cuenta'."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent)
            ) {
                Text(
                    text = if (isLoginMode) "Entrar a mi Cuenta" else "Guardar Usuario y Entrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (savedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = {
                    errorMsg = ""
                    showSavedAccountsList = true
                }) {
                    AutoTranslatedText(text = "← Volver a la lista de usuarios guardados",
                        color = theme.primaryAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
