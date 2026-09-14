package pe.edu.upn.agrovisionlurin.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import pe.edu.upn.agrovisionlurin.domain.usecase.GetRecommendationsUseCase

class ResultViewModel : ViewModel() {
    private val getRecommendationsUseCase = GetRecommendationsUseCase()

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadResult(resultId: String) {
        // En una app real, recuperarías el DiagnosticResult guardado de la BD usando resultId.
        // Aquí pasamos un resultado mockeado como prueba.
        viewModelScope.launch {
            val result = pe.edu.upn.agrovisionlurin.domain.model.ResultCache.lastResult
            if (result != null) {
                val recommendations = getRecommendationsUseCase(result.classId, result.confidence)
                _uiState.value = ResultUiState.Success(result, recommendations)
            } else {
                _uiState.value = ResultUiState.Error("No se encontró resultado en caché")
            }
        }
    }

    fun saveToLocalRegistry(onSaved: () -> Unit) {
        // En un caso real inyectarías InspectionDao y harías un insert
        // dao.insertInspection(...)
        onSaved()
    }
}

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(
        val diagnostic: DiagnosticResult,
        val recommendations: List<pe.edu.upn.agrovisionlurin.domain.model.Recommendation>
    ) : ResultUiState()
    data class Error(val message: String) : ResultUiState()
}
