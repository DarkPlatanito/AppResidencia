package com.example.appresidencia

// Lo que mandas al backend de JarvisLite
data class JarvisRequestDto(
    val prompt: String
)

// Lo que te regresa el backend de JarvisLite
data class JarvisResponseDto(
    val texto: String
)

data class HealthResponseDto(
    val status: String
)