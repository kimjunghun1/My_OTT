package com.example.myott

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
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

/** 회원가입 폼을 여는 액티비티다. */
class MainActivity : ComponentActivity() {
    // 화면 회전으로 액티비티가 재생성되어도 입력 중인 값을 유지한다.
    private val registration: RegistrationModel by viewModels()

    // 앱 테마를 적용해 폼을 표시하고, 돌아가기 요청이 오면 이 액티비티를 닫는다.
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
