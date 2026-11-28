package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myott.GenreViewModel

val BackgroundColor = Color(0xFF121212)
val CardColor = Color(0xFF202020)
val PurpleColor = Color(0xFF9363EB)
val TextColor = Color.White
val SubTextColor = Color(0xFFAAAAAA)

// TMDB 장르 ID에 맞는 아이콘 반환
private fun iconForTmdbGenre(genreId: Int): String = when (genreId) {
    28, 10759 -> "⚔"      // 액션 / 액션·모험
    12 -> "🧭"            // 모험
    16 -> "◎"             // 애니메이션
    35 -> "☻"             // 코미디
    80 -> "♧"             // 범죄
    99 -> "▧"             // 다큐멘터리
    18 -> "🎭"            // 드라마
    10751 -> "🏠"         // 가족
    14, 10765 -> "♢"      // 판타지 / SF·판타지
    36 -> "♜"             // 역사
    27 -> "👻"            // 공포
    10402 -> "♬"          // 음악
    9648 -> "♠"           // 미스터리
    10749 -> "♡"          // 로맨스
    878 -> "✧"            // SF
    53 -> "🔪"            // 스릴러
    10752, 10768 -> "🛡"  // 전쟁 / 전쟁·정치
    37 -> "🤠"            // 서부
    else -> "🎬"          // 기타 TMDB 장르 (뉴스, 리얼리티, 토크쇼 등)
}

@Composable
fun GenreSelectionScreen(
    genreViewModel: GenreViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val genres by genreViewModel.allGenres.collectAsState()
    val selectedCount = genres.count { it.isSelected }
    var showSkipDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(20.dp)
    ) {
        // 상단 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color = TextColor,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBack() }
            )
            Text(
                text = "취향 설정",
                color = TextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "좋아하는 장르를 선택해 주세요.",
            color = TextColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "관심 있는 장르 3개를 선택하면 콘텐츠를 추천해 드려요.",
            color = SubTextColor,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "3개의 장르를 선택해 주세요",
                color = SubTextColor,
                fontSize = 11.sp
            )
            Text(
                text = "${selectedCount}/3개 선택",
                color = PurpleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TMDB에서 불러온 전체 장르를 2열 그리드로 표시 (스크롤 가능)
        if (genres.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleColor)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(genres, key = { it.genreId }) { genre ->
                    GenreCard(
                        genre = "${iconForTmdbGenre(genre.genreId)}  ${genre.genreName}",
                        isSelected = genre.isSelected,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (genre.isSelected) {
                            genreViewModel.updateGenreSelection(genre.genreId, false)
                        } else if (selectedCount < 3) {
                            genreViewModel.updateGenreSelection(genre.genreId, true)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "취향은 나중에 언제든지 변경할 수 있어요.",
            color = SubTextColor,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onComplete() },
            enabled = selectedCount == 3,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor)
        ) {
            Text(
                text = "다음",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GenreCard(
    genre: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF302344) else CardColor,
        border = if (isSelected) {
            BorderStroke(1.5.dp, PurpleColor)
        } else {
            BorderStroke(1.dp, Color(0xFF353535))
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = genre,
                color = if (isSelected) Color.White else SubTextColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-3).dp)
                        .background(PurpleColor, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}