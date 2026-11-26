package pe.edu.upn.agrovisionlurin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FunctionalLoginTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFirstTimeSetupAndNavigation() {
        // 1. Verificar que estamos en la pantalla de "Configura tu cuenta" (Modo Setup)
        // o "Iniciar sesión" si ya existe un usuario.
        val isSetup = try {
            composeTestRule.onNodeWithText("Configura tu cuenta").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }

        if (isSetup) {
            // 2. Completar flujo de registro inicial
            composeTestRule.onNodeWithText("Nombre completo")
                .performTextInput("Administrador de Prueba")
            composeTestRule.onNodeWithText("Usuario").performTextInput("admin_test")
            composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")

            // Hacer clic en el botón de creación
            composeTestRule.onNodeWithText("Crear cuenta e ingresar").performClick()
        } else {
            // Flujo de login normal
            composeTestRule.onNodeWithText("Usuario").performTextInput("admin_test")
            composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
            composeTestRule.onNodeWithText("Ingresar").performClick()
        }

        // 3. Verificar que navegamos al Dashboard (ajusta el texto según tu DashboardScreen)
        // Esperamos a que el CircularProgressIndicator desaparezca si lo hay
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("AgroVision").fetchSemanticsNodes().isNotEmpty()
        }

        // Verificar un elemento único del Dashboard
        composeTestRule.onNodeWithText("Resumen de Actividad").assertIsDisplayed()
    }

    @Test
    fun testLoginErrorShowsMessage() {
        // Forzar un error de login (usuario vacío o incorrecto)
        composeTestRule.onNodeWithText("Usuario").performTextInput("usuario_inexistente")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("wrong_pass")
        composeTestRule.onNodeWithText("Ingresar").performClick()

        // Verificar que aparece un mensaje de error (según la lógica de tu LoginScreen)
        composeTestRule.onNodeWithTag("Error").assertExists() // Si usas testTag
        // O por texto si conoces el mensaje de error de tu repositorio:
        // composeTestRule.onNodeWithText("Credenciales incorrectas").assertIsDisplayed()
    }
}