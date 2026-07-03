package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ChatRepository
import com.example.ui.RikkaHubApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room Database, DAOs, and repository
    val database = AppDatabase.getDatabase(this)
    val chatDao = database.chatDao()
    val settingsDao = database.settingsDao()
    val repository = ChatRepository(chatDao, settingsDao)
    
    // Build the ViewModel Factory
    val viewModelFactory = ChatViewModel.Factory(application, repository)
    val chatViewModel: ChatViewModel by viewModels { viewModelFactory }
    
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          RikkaHubApp(chatViewModel = chatViewModel)
        }
      }
    }
  }
}