package pe.edu.upn.agrovisionlurin.presentation.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.agrovisionlurin.data.local.AppDatabase
import pe.edu.upn.agrovisionlurin.data.local.UserPreferencesManager
import pe.edu.upn.agrovisionlurin.data.repository.LocalMvpRepository

data class LoginUiState(
    val username:String="", val password:String="", val isLoading:Boolean=false,
    val isPasswordVisible:Boolean=false, val errorMessage:String?=null, val loginSuccess:Boolean=false
)
/** Compatibility for the old, unrouted screen. All credentials are verified against SQLite. */
class LoginViewModel(application:Application):AndroidViewModel(application) {
    private val repo=LocalMvpRepository(AppDatabase.getDatabase(application))
    private val prefs=UserPreferencesManager.getInstance(application)
    private val _uiState=MutableStateFlow(LoginUiState());val uiState=_uiState.asStateFlow()
    val hasActiveSession:Boolean get()=false
    fun onUsernameChange(value:String) { _uiState.update { it.copy(username=value.trim(),errorMessage=null) } }
    fun onPasswordChange(value:String) { _uiState.update { it.copy(password=value,errorMessage=null) } }
    fun onTogglePasswordVisibility() { _uiState.update { it.copy(isPasswordVisible=!it.isPasswordVisible) } }
    fun onLoginClick() {
        val form=_uiState.value
        _uiState.update { it.copy(isLoading=true,errorMessage=null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                check(!repo.needsSetup()) { "Configura el administrador desde la pantalla principal." }
                val user=repo.login(form.username,form.password,"")
                prefs.saveSession(user.username,user.displayName,user.role)
                _uiState.update { it.copy(isLoading=false,password="",loginSuccess=true) }
            } catch(cause:CancellationException) { throw cause }
            catch(cause:Exception) { _uiState.update { it.copy(isLoading=false,password="",errorMessage=cause.message) } }
        }
    }
    fun logout() { prefs.clearSession();_uiState.value=LoginUiState() }
}

