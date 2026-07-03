package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.ChatViewModel

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun RikkaHubApp(
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val sessions by chatViewModel.sessions.collectAsState()
    val currentMessages by chatViewModel.currentMessages.collectAsState()
    val currentSessionId by chatViewModel.currentSessionId.collectAsState()
    val activeMode by chatViewModel.activeMode.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()

    // Query if onboarding has been completed
    val onboardingCompleted = remember {
        derivedStateOf {
            chatViewModel.getSettingValue("onboarding_completed", "false") == "true"
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(durationMillis = 350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(animationSpec = tween(durationMillis = 350))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(durationMillis = 350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(animationSpec = tween(durationMillis = 350))
        }
    ) {
        // 1. Splash Route
        composable(NavRoutes.SPLASH) {
            SplashScreen(onTimeout = {
                if (onboardingCompleted.value) {
                    navController.navigate(NavRoutes.CHAT_LIST) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.ONBOARDING) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            })
        }

        // 2. Onboarding Route
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(onFinished = {
                chatViewModel.saveSettingValue("onboarding_completed", "true")
                navController.navigate(NavRoutes.CHAT_LIST) {
                    popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                }
            })
        }

        // 3. Chat List Route (Home)
        composable(NavRoutes.CHAT_LIST) {
            ChatListScreen(
                sessions = sessions,
                onSessionSelected = { sessionId ->
                    chatViewModel.selectSession(sessionId)
                    navController.navigate("${NavRoutes.CHAT_DETAIL}/$sessionId")
                },
                onNewChatClicked = {
                    chatViewModel.startNewSession { newSessionId ->
                        navController.navigate("${NavRoutes.CHAT_DETAIL}/$newSessionId")
                    }
                },
                onSettingsClicked = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onDeleteSession = { sessionId ->
                    chatViewModel.deleteSession(sessionId)
                }
            )
        }

        // 4. Chat Detail Route
        composable(
            route = "${NavRoutes.CHAT_DETAIL}/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val currentSession = remember(sessions, sessionId) {
                sessions.find { it.id == sessionId }
            }
            val title = currentSession?.title ?: "Chat"

            // Set session active in viewModel when returning/entering
            LaunchedEffect(sessionId) {
                chatViewModel.selectSession(sessionId)
            }

            ChatScreen(
                sessionTitle = title,
                messages = currentMessages,
                activeMode = activeMode,
                isTyping = isTyping,
                errorMessage = errorMessage,
                onBackClicked = {
                    navController.popBackStack()
                },
                onModeChanged = { mode ->
                    chatViewModel.setMode(mode)
                },
                onSendMessage = { prompt ->
                    chatViewModel.sendMessage(prompt)
                }
            )
        }

        // 5. Settings Route
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = chatViewModel,
                onBackClicked = {
                    navController.popBackStack()
                },
                onAboutClicked = {
                    navController.navigate(NavRoutes.ABOUT)
                }
            )
        }

        // 6. About Route
        composable(NavRoutes.ABOUT) {
            AboutScreen(onBackClicked = {
                navController.popBackStack()
            })
        }
    }
}