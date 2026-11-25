package pe.edu.upn.agrovisionlurin.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
    session: LocalMvpViewModel
) {
    val form by viewModel.uiState.collectAsStateWithLifecycle()
    val busy by session.busy.collectAsStateWithLifecycle()
    val message by session.message.collectAsStateWithLifecycle()
    val setup by session.setup.collectAsStateWithLifecycle()
    val user by session.user.collectAsStateWithLifecycle()
    val uiState=form.copy(isLoading=busy,errorMessage=message.takeIf { it.isNotBlank() },loginSuccess=user!=null)
    var displayName by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val submit: () -> Unit = { focusManager.clearFocus();session.login(form.username,form.password,displayName) }

    // Navegar cuando el login es exitoso
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            viewModel.onPasswordChange("")
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF1B5E20),
                        0.26f to Color(0xFF2E7D32),
                        0.46f to Color(0xFF388E3C),
                        0.58f to Color(0xFFF8F9FA),
                        1.0f to Color(0xFFF8F9FA)
                    )
                )
            )
    ) {
        // Decoración de fondo: círculos orgánicos
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            // ── Logo y Título ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono app
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "AgroVision",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Cooperativa Agraria LIMONORT-PERU, Sullana",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                    Text(
                        text = "Sistema de diagnóstico fitosanitario",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.60f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Card de Login ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if(setup) "Configura tu cuenta" else "Iniciar sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = if(setup) "Crea el primer administrador para empezar." else "Ingresa tus credenciales de acceso al sistema.",
                        fontSize = 13.sp,
                        color = Color(0xFF5E636E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if(setup) OutlinedTextField(
                        value=displayName,onValueChange={displayName=it},label={Text("Nombre completo")},
                        leadingIcon={Icon(Icons.Rounded.Badge,contentDescription=null,tint=PrimaryGreen)},
                        singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),
                        colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=PrimaryGreen,focusedLabelColor=PrimaryGreen,cursorColor=PrimaryGreen)
                    )

                    // Campo: Usuario
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Usuario") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = if (uiState.username.isNotBlank()) PrimaryGreen
                                       else Color(0xFF5E636E)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        isError = uiState.errorMessage != null
                    )

                    // Campo: Contraseña
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (uiState.password.isNotBlank()) PrimaryGreen
                                       else Color(0xFF5E636E)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible)
                                        Icons.Rounded.VisibilityOff
                                    else
                                        Icons.Rounded.Visibility,
                                    contentDescription = if (uiState.isPasswordVisible)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña",
                                    tint = Color(0xFF5E636E)
                                )
                            }
                        },
                        visualTransformation = if (uiState.isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                submit()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        isError = uiState.errorMessage != null
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    fontSize = 13.sp,
                                    color = Color(0xFFBA1A1A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón de ingreso
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submit()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Login,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if(setup) "Crear cuenta e ingresar" else "Ingresar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Información de acceso con contraste sobre el fondo claro.
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Key,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Acceso local SQLite",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                    Text(
                        text = if(setup) "La cuenta se guarda en este dispositivo" else "Usa una cuenta creada por tu administrador",
                        fontSize = 11.sp,
                        color = Color(0xFF5E636E),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "No hay credenciales predefinidas",
                        fontSize = 11.sp,
                        color = Color(0xFF5E636E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pie de página
            Text(
                text = "AgroVision Sullana · UPN Capstone 2026",
                fontSize = 11.sp,
                color = Color(0xFF5E636E)
            )
            }
        }
    }
}
