package com.example.appresidencia

// Un paso de instrucciones para el componente analizado
data class AnalysisStepDto(
    val stepNumber: Int,
    val title: String,
    val description: String
)

// Respuesta "tipo IA" que usará toda la app
data class AnalysisResponseDto(
    val componentName: String,
    val steps: List<AnalysisStepDto>
)
