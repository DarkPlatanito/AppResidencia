package com.example.appresidencia

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // Endpoint que usa AnalysisViewModel
    @GET("demo-analysis")
    suspend fun getDemoAnalysis(): AnalysisResponseDto

    // Endpoint que usa JarvisViewModel
    @POST("chat")
    suspend fun sendMessage(
        @Body request: JarvisRequestDto
    ): JarvisResponseDto
}
