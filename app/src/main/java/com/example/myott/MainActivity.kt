package com.example.myott

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genreapp.data.AppDatabase
import com.example.myott.auth.AuthRepository
import com.example.myott.auth.AuthRoute
import com.example.myott.auth.AuthViewModel
import com.example.myott.ui.theme.MyOTTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DB → Repository 준비
        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepository(database.userDao())

        setContent {
            MyOTTTheme {
                val authViewModel: AuthViewModel =
                    viewModel(factory = AuthViewModel.factory(authRepository))

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthRoute(
                        authViewModel = authViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
