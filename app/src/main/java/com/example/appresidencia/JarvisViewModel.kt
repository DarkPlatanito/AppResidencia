package com.example.appresidencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JarvisViewModel : ViewModel() {

    private val _reply = MutableStateFlow("")
    val reply: StateFlow<String> = _reply

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val request = JarvisRequestDto(prompt = text)

                val response = RetrofitClient.apiService.sendMessage(request)

                _reply.value = response.texto
            } catch (e: Exception) {
                _reply.value = "Error al conectar con JarvisLite: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
