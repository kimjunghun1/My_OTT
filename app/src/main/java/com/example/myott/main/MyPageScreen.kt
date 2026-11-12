package com.example.myott.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// 색상 (메인 화면과 같은 다크 + 보라 계열)
private val MyPageBackground = Color(0xFF121212)
private val MyPageCard = Color(0xFF202020)
private val MyPagePurple = Color(0xFF9363EB)
private val MyPageText = Color.White
private val MyPageSubText = Color(0xFFAAAAAA)

// 원형 그래프 조각 하나: 장르 이름 + 비율(0~100) + 색
// TODO: '보러가기' 클릭 기록 기능이 생기면, 장르별 클릭 횟수로 비율을 계산해서 이 형태로 넘겨주면 됨
data class GenreRatio(
    val genre: String,
    val percent: Float,
    val color: Color,
)

// 그래프 조각에 쓸 색 목록 (장르 순서대로 하나씩 사용)
val GenreChartColors = listOf(
    Color(0xFF9363EB), // 보라
    Color(0xFF4FC3F7), // 하늘
    Color(0xFFFFB74D), // 주황
    Color(0xFF81C784), // 초록
    Color(0xFFF06292), // 분홍
    Color(0xFF90A4AE), // 회색 (기타)
)

// 마이페이지 화면 (하단 탭 '마이페이지')
// 1. 상단 멘트  2. 장르별 '보러가기' 비율 원형 그래프  3. 사용자 정보 초기화 버튼
// 지금은 UI만 구현 — 기록이 없으므로 그래프는 빈 상태로 표시된다.
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    genreRatios: List<GenreRatio> = emptyList(), // TODO: 실제 '보러가기' 기록으로 계산한 값 연결
    onResetClick: () -> Unit = {},               // TODO: 선호 장르 초기화 + 장르 선택 화면 이동 연결
) {
    // 비율이 가장 높은 장르 (기록이 없으면 null)
    val topGenre = genreRatios.maxByOrNull { it.percent }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyPageBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "마이페이지",
            color = MyPagePurple,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. 상단 멘트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MyPageCard)
                .padding(20.dp)
        ) {
            Text(
                // TODO: 장르별 멘트 정하기 (예: 애니메이션 → "십덕이시네요!")
                text = if (topGenre == null) "아직 시청 기록이 없어요" else "${topGenre.genre} 마니아시네요!",
                color = MyPageText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (topGenre == null) "콘텐츠의 '보러가기'를 누르면 취향을 분석해 드릴게요"
                else "가장 많이 본 장르는 ${topGenre.genre}예요",
                color = MyPageSubText,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. 원형 그래프
        Text(
            text = "장르별 시청 비율",
            color = MyPageText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "'보러가기'를 누른 횟수 기준",
            color = MyPageSubText,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        GenrePieChart(
            ratios = genreRatios,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 그래프 범례 (색 · 장르 · 비율)
        genreRatios.forEach { ratio ->
            LegendRow(ratio)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. 사용자 정보 초기화 버튼
        Button(
            onClick = onResetClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MyPagePurple,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "선호 장르 다시 선택하기", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "선택한 선호 장르와 시청 기록이 초기화돼요",
            color = MyPageSubText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 원형 그래프 — 비율만큼 조각을 그린다. 기록이 없으면 회색 원 + 안내 문구
@Composable
private fun GenrePieChart(ratios: List<GenreRatio>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (ratios.isEmpty()) {
                drawCircle(color = MyPageCard)
            } else {
                var startAngle = -90f // 12시 방향부터 시계 방향으로
                ratios.forEach { ratio ->
                    val sweepAngle = 360f * ratio.percent / 100f
                    drawArc(
                        color = ratio.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
        }
        if (ratios.isEmpty()) {
            Text(text = "기록 없음", color = MyPageSubText, fontSize = 14.sp)
        }
    }
}

// 범례 한 줄: ● 장르 이름 ........ 00%
@Composable
private fun LegendRow(ratio: GenreRatio) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ratio.color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = ratio.genre, color = MyPageText, fontSize = 14.sp)
        }
        Text(
            text = "${ratio.percent.roundToInt()}%",
            color = MyPageSubText,
            fontSize = 14.sp
        )
    }
}

// ---------------- Android Studio 미리보기용 (앱 실행과는 무관) ----------------

// 실제 앱과 같은 상태: 기록 없음
@Preview(name = "기록 없음", showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun MyPageScreenEmptyPreview() {
    MyPageScreen()
}

// 그래프 모양 확인용 예시 (미리보기 창에서만 보이고 앱에는 들어가지 않음)
@Preview(name = "그래프 예시", showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun MyPageScreenSamplePreview() {
    MyPageScreen(
        genreRatios = listOf(
            GenreRatio("애니메이션", 45f, GenreChartColors[0]),
            GenreRatio("로맨스", 25f, GenreChartColors[1]),
            GenreRatio("SF", 20f, GenreChartColors[2]),
            GenreRatio("기타", 10f, GenreChartColors[5]),
        )
    )
}
