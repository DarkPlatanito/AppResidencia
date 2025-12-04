package com.example.appresidencia

import retrofit2.http.GET

/**
 * Interfaz de Retrofit (la dejamos lista por si después conectas backend).
 * OJO: Aquí ya NO se declara AnalysisResponseDto, solo se usa.
 */
interface ApiService {

    @GET("demo-analysis")
    suspend fun getDemoAnalysis(): AnalysisResponseDto
}
