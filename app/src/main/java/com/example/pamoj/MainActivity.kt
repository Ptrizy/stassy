package com.example.pamoj

import com.example.pamoj.ui.navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pamoj.data.repository.AuthRepository
import com.example.pamoj.ui.theme.PamojTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.initializeApp(this)
            setContent {
                PamojTheme {
                    AppNavigation(authRepository)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}