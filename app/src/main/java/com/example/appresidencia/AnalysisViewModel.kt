package com.example.appresidencia

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    object Loading : AnalysisUiState()
    data class Success(val data: AnalysisResponseDto) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

class AnalysisViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState

    /**
     * Análisis "base". Aquí va tu llamada real a la API.
     * Lo ejecutamos en Dispatchers.IO para no bloquear la UI.
     */
    private fun runDemoAnalysisInternal() {
        _uiState.value = AnalysisUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = RetrofitClient.apiService.getDemoAnalysis()
                _uiState.value = AnalysisUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /** Llamado cuando viene una foto de la cámara */
    fun runDemoAnalysisFromBitmap(bitmap: Bitmap) {
        // Si después quieres mandar la imagen a tu API, este es el lugar.
        runDemoAnalysisInternal()
    }

    /** Llamado cuando viene una imagen de la galería */
    fun runDemoAnalysisFromUri(uri: Uri) {
        // Aquí podrías leer el archivo y subirlo a tu API.
        runDemoAnalysisInternal()
    }

    /** Por si quieres invocar el demo directamente */
    fun runDemoAnalysis() {
        runDemoAnalysisInternal()
    }

    fun reset() {
        _uiState.value = AnalysisUiState.Idle
    }
}
