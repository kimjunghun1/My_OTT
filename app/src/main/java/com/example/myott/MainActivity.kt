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
import com.example.myott.ui.auth.RegistrationScreen
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
                    RegistrationScreen(
                        model = registration,
                        onCancel = { finish() },
                        modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
                    )
                }
            }
        }
    }

    companion object {
        // 다른 화면에서 회원가입 액티비티를 열 때 사용할 인텐트를 만든다.
        fun registrationIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }
}
