package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ChatRepository(
    private val chatDao: ChatDao,
    private val settingsDao: SettingsDao
) {
    // OkHttp & Retrofit instance
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openrouter.ai/api/v1/") // default base URL
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(OpenRouterService::class.java)

    // Room database operations
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun getSessionById(sessionId: String): ChatSession? = withContext(Dispatchers.IO) {
        chatDao.getSessionById(sessionId)
    }

    suspend fun createSession(session: ChatSession) = withContext(Dispatchers.IO) {
        chatDao.insertSession(session)
    }

    suspend fun updateSession(session: ChatSession) = withContext(Dispatchers.IO) {
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun saveMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    // Settings helpers
    suspend fun getSetting(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        settingsDao.getSettingByKey(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingsDao.insertSetting(AppSetting(key, value))
    }

    fun getAllSettingsFlow(): Flow<List<AppSetting>> = settingsDao.getAllSettingsFlow()

    // Remote chat execution
    suspend fun fetchChatCompletion(
        endpointUrl: String,
        apiKey: String,
        model: String,
        messages: List<OpenRouterMessage>,
        temperature: Double? = 0.7
    ): OpenRouterChatResponse = withContext(Dispatchers.IO) {
        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        apiService.getChatCompletions(
            url = endpointUrl,
            authorization = authHeader,
            request = OpenRouterChatRequest(
                model = model,
                messages = messages,
                temperature = temperature
            )
        )
    }
}