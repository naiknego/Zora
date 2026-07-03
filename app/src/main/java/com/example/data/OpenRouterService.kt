package com.example.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface OpenRouterService {
    @POST
    suspend fun getChatCompletions(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/aistudio",
        @Header("X-Title") title: String = "RikkaHub AI",
        @Body request: OpenRouterChatRequest
    ): OpenRouterChatResponse
}