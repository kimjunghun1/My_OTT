package com.example.myott.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myott.API.TmdbGenre
import com.example.myott.AppContainer
import com.example.myott.detail.ContentInfo
import com.example.myott.detail.ContentRecord
import kotlinx.coroutines.launch

private val MainBackground = Color(0xFF121212)
private val MainCard = Color(0xFF202020)
private val MainPurple = Color(0xFF9363EB)
private val MainText = Color.White
private val MainSubText = Color(0xFFAAAAAA)

@Composable
fun FavoriteScreen(
    onMovieClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as AppContainer).contentScoreRepository }
    val scope = rememberCoroutineScope()

    val favoriteList by repository.interestedContents.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .padding(20.dp)
    ) {
        Text(
            text = "찜한 작품",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MainPurple
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "내가 좋아한 작품들 (${favoriteList.size}편)",
            style = MaterialTheme.typography.bodyMedium,
            color = MainText
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (favoriteList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "아직 찜한 작품이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainSubText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = favoriteList,
                    key = { "${it.mediaType}_${it.movieId}" } // 👈 영화/TV ID 중복 방지 키
                ) { record ->
                    FavoriteWorkRow(
                        record = record,
                        onItemClick = { onMovieClick(record.movieId, record.mediaType) }, // 👈 mediaType 함께 전달
                        onRemove = {
                            scope.launch {
                                repository.setInterested(record.toContentInfo(), false)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteWorkRow(
    record: ContentRecord,
    onItemClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onItemClick), // 👈 포스터나 제목 영역 클릭 시 상세 페이지 이동
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 1. 왼쪽 실제 포스터 이미지
        Box(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(MainCard)
        ) {
            if (record.posterPath != null) {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w342${record.posterPath}"),
                    contentDescription = record.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. 가운데 제목 및 장르 정보
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = record.title,
                color = MainText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ContentRecord에 저장된 장르 이름 표시 (예: "액션,SF" -> "액션 · SF")
            val genresText = record.genreNames
                .split(",")
                .filter { it.isNotBlank() }
                .joinToString(" · ")

            if (genresText.isNotEmpty()) {
                Text(
                    text = genresText,
                    color = MainSubText,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. 오른쪽 하트 버튼 (누르면 DB에서 관심 해제되어 목록에서 즉시 사라짐)
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "찜 해제",
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ContentRecord(DB 저장 형태)를 setInterested()가 요구하는 ContentInfo로 변환하는 확장 함수
private fun ContentRecord.toContentInfo(): ContentInfo {
    val ids = genreIds.split(",").mapNotNull { it.toIntOrNull() }
    val names = genreNames.split(",")
    val genres = ids.mapIndexed { index, id ->
        TmdbGenre(id = id, name = names.getOrNull(index))
    }
    return ContentInfo(
        movieId = movieId,
        mediaType = mediaType,
        title = title,
        posterPath = posterPath,
        genres = genres
    )
}