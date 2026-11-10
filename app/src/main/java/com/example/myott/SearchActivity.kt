package com.example.myott

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myott.ui.search.SearchScreen
import com.example.myott.ui.theme.MyOTTTheme

/** 앱을 실행할 때 작품 검색 화면을 연다. */
class SearchActivity : ComponentActivity() {
    /** [savedInstanceState]를 상위 액티비티에 전달하고 앱 테마로 검색 UI를 구성한다. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyOTTTheme {
                Scaffold { insets ->
                    // insets는 상태 표시줄과 내비게이션 바를 피하기 위한 여백이다.
                    SearchScreen(Modifier.padding(insets).consumeWindowInsets(insets))
                }
            }
        }
    }
}
