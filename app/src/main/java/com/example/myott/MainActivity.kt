package com.example.myott

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genreapp.data.AppDatabase
import com.example.myapplication.GenreSelectionScreen
import com.example.myott.main.MainScreen
import com.example.myott.ui.theme.MyOTTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // Room 데이터베이스 생성
        val database = AppDatabase.getDatabase(applicationContext)

        // Repository 생성
        val repository = GenreRepository(database.genreDao())

        // ViewModel Factory 생성
        val factory = GenreViewModelFactory(repository)

        setContent {
            MyOTTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// ViewModel Factory
class GenreViewModelFactory(
    private val repository: GenreRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(GenreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenreViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}