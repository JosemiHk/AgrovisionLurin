package pe.edu.upn.agrovisionlurin.presentation.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {
    private val _pendingSyncCount = MutableStateFlow(3) // Mocked
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()
    
    // Additional view model logic
}
