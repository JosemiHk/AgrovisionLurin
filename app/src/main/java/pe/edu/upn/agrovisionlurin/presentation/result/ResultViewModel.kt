package pe.edu.upn.agrovisionlurin.presentation.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.upn.agrovisionlurin.data.local.AppDatabase
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import pe.edu.upn.agrovisionlurin.domain.model.Recommendation
import pe.edu.upn.agrovisionlurin.domain.model.ResultCache
import pe.edu.upn.agrovisionlurin.domain.usecase.GetRecommendationsUseCase

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val inspectionDao = AppDatabase.getDatabase(application).inspectionDao()
    private val getRecommendationsUseCase = GetRecommendationsUseCase()

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadResult(resultId: String) {
        viewModelScope.launch {
            val cached = ResultCache.lastCachedDiagnostic
            if (cached != null) {
                val recommendations = getRecommendationsUseCase(cached.result.classId, cached.result.confidence)
                _uiState.value = ResultUiState.Success(cached.result, recommendations)
                return@launch
            }

            // Resiliencia a Process Death: Carga desde Room SQLite
            val entity = withContext(Dispatchers.IO) {
                inspectionDao.getInspectionById(resultId)
            }
            if (entity != null) {
                val result = DiagnosticResult(
                    classId = entity.diagnosisClass,
                    className = when (entity.diagnosisClass) {
                        0 -> "Cultivo Sano"
                        1 -> "Trips tabaci"
                        2 -> "Estrés Hídrico"
                        else -> "Desconocido"
                    },
                    confidence = entity.confidence,
                    latencyMs = entity.latencyMs,
                    severityLevel = entity.severityLevel
                )
                val recommendations = getRecommendationsUseCase(result.classId, result.confidence)
                _uiState.value = ResultUiState.Success(result, recommendations)
            } else {
                _uiState.value = ResultUiState.Error("No se encontró el registro en almacenamiento local")
            }
        }
    }

    fun saveToLocalRegistry(onSaved: () -> Unit) {
        val cached = ResultCache.lastCachedDiagnostic ?: run {
            onSaved()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val recommendations = getRecommendationsUseCase(cached.result.classId, cached.result.confidence)
            val jsonArray = JSONArray()
            recommendations.forEach { rec ->
                val obj = JSONObject().apply {
                    put("title", rec.title)
                    put("description", rec.description)
                }
                jsonArray.put(obj)
            }

            val entity = InspectionEntity(
                id = cached.inspectionId,
                plotId = "Valle Bajo - Lurín",
                subPlot = "Surco Evaluado",
                imagePath = cached.imagePath,
                diagnosisClass = cached.result.classId,
                confidence = cached.result.confidence,
                latencyMs = cached.result.latencyMs,
                severityLevel = cached.result.severityLevel,
                recommendationsJson = jsonArray.toString(),
                syncStatus = 0, // 0 = Modo Local / Pendiente de Sincronización
                createdAt = System.currentTimeMillis()
            )

            // Persistencia transaccional garantizada
            inspectionDao.insertInspection(entity)

            withContext(Dispatchers.Main) {
                onSaved()
            }
        }
    }
}

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(
        val diagnostic: DiagnosticResult,
        val recommendations: List<Recommendation>
    ) : ResultUiState()
    data class Error(val message: String) : ResultUiState()
}
