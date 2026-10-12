package pe.edu.upn.agrovisionlurin

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpScreen
import pe.edu.upn.agrovisionlurin.presentation.theme.AgroVisionLurinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroVisionLurinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocalMvpScreen()
                }
            }
        }
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        return try {
            super.dispatchGenericMotionEvent(ev)
        } catch (e: IllegalStateException) {
            // Mitigación para el bug de AndroidComposeView (ACTION_HOVER_EXIT) en screen mirroring / stylus
            Log.w("MainActivity", "Interceptado bug de Compose hover event: ${e.message}")
            true
        }
    }
}
