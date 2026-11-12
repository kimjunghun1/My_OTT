package com.example.myott.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

// 마이페이지 화면 (하단 탭 '마이페이지'를 누르면 표시)
// 로그인 기능이 빠져서 로그인 정보는 받지 않는다.
// TODO: 선호 장르·연령대·성별 확인/수정 등 마이페이지 내용 추가
@Composable
fun MyPageScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "마이페이지", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "준비 중", color = Color(0xFFAAAAAA), fontSize = 14.sp)
    }
}

// Android Studio 미리보기용 (앱 실행과는 무관)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MyPageScreenPreview() {
    MyPageScreen()
}
