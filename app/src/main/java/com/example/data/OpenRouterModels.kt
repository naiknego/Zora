package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenRouterMessage>,
    @Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class ResponseMessage(
    @Json(name = "role") val role: String?,
    @Json(name = "content") val content: String?
)

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "message") val message: ResponseMessage?
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    @Json(name = "id") val id: String?,
    @Json(name = "choices") val choices: List<Choice>?
)