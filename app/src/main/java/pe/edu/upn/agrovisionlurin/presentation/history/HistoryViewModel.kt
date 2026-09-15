package pe.edu.upn.agrovisionlurin.presentation.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upn.agrovisionlurin.data.local.AppDatabase
import pe.edu.upn.agrovisionlurin.data.local.InspectionDao
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity

enum class HistoryFilter { ALL, PESTS, WATER_STRESS, HEALTHY }

data class HistoryUiState(
    val inspections: List<InspectionEntity> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val totalLocalCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val pestCount: Int = 0,
    val waterStressCount: Int = 0,
    val healthyCount: Int = 0,
    val isLoading: Boolean = false
)

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao: InspectionDao = AppDatabase.getDatabase(application).inspectionDao()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        dao.getAllInspections(),
        _selectedFilter,
        dao.getPendingSyncCount()
    ) { allInspections, filter, pendingCount ->
        val pestCount = allInspections.count { it.diagnosisClass == 1 }
        val waterStressCount = allInspections.count { it.diagnosisClass == 2 }
        val healthyCount = allInspections.count { it.diagnosisClass == 0 }

        val filteredList = when (filter) {
            HistoryFilter.ALL -> allInspections
            HistoryFilter.PESTS -> allInspections.filter { it.diagnosisClass == 1 }
            HistoryFilter.WATER_STRESS -> allInspections.filter { it.diagnosisClass == 2 }
            HistoryFilter.HEALTHY -> allInspections.filter { it.diagnosisClass == 0 }
        }

        HistoryUiState(
            inspections = filteredList,
            selectedFilter = filter,
            totalLocalCount = allInspections.size,
            pendingSyncCount = pendingCount,
            pestCount = pestCount,
            waterStressCount = waterStressCount,
            healthyCount = healthyCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun syncPending() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val pending = dao.getPendingSyncInspections()
                if (pending.isNotEmpty()) {
                    delay(800) // Simulación breve de transferencia de datos
                    dao.markAllAsSynced(pending.map { it.id })
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
