package com.example.myott

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myott.main.MainScreen
import com.example.myott.ui.theme.MyOTTTheme

/** 회원가입 폼을 여는 액티비티다. */
class MainActivity : ComponentActivity() {
    // 화면 회전으로 액티비티가 재생성되어도 입력 중인 값을 유지한다.
    private val registration: RegistrationModel by viewModels()

    // 앱 테마를 적용해 폼을 표시하고, 돌아가기 요청이 오면 이 액티비티를 닫는다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyOTTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 로그인 기능 제외 → 앱을 켜면 바로 메인 화면
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
