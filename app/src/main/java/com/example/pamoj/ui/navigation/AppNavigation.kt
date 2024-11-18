package com.example.pamoj.ui.navigation

import HomeScreen
import SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pamoj.data.model.Post
import com.example.pamoj.data.repository.AuthRepository
import com.example.pamoj.data.repository.FirestoreRepository
import com.example.pamoj.ui.screens.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object Splash : Screen("splash")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val firestoreRepository = remember {
        FirestoreRepository(
            storage = FirebaseStorage.getInstance(),
            firestore = FirebaseFirestore.getInstance(),
            authRepository = authRepository
        )
    }

    // state utk posts
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    val fetchPosts = {
        scope.launch {
            when (val result = firestoreRepository.getPosts()) {
                is FirestoreRepository.Result.Success -> {
                    posts = result.data
                }
                is FirestoreRepository.Result.Error -> {

                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchPosts()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(3000)
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authRepository = authRepository,
                onAuthSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                posts = posts,
                authRepository = authRepository,
                firestoreRepository = firestoreRepository,
                navController = navController,
                onPostsUpdated = { fetchPosts() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                authRepository = authRepository,
                firestoreRepository = firestoreRepository
            )
        }
    }
}