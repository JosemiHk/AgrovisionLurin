package pe.edu.upn.agrovisionlurin.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.agrovisionlurin.data.local.AppDatabase
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).inspectionDao()

    val pendingSyncCount: StateFlow<Int> = dao.getPendingSyncCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val recentInspections: StateFlow<List<InspectionEntity>> = dao.getAllInspections()
        .map { it.take(5) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalScansCount: StateFlow<Int> = dao.getAllInspections()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
