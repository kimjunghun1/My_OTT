package com.example.myott.main

import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myott.ui.theme.MyOTTTheme
import kotlinx.coroutines.launch

private val MainBackground = Color(0xFF121212)
private val MainCard = Color(0xFF202020)
private val MainPurple = Color(0xFF9363EB)
private val MainText = Color.White
private val MainSubText = Color(0xFFAAAAAA)

// TMDB 연동 전, 세 작품을 구분하기 위한 임시 ID입니다.
private val sampleWorkIds = listOf(1, 2, 3)



@Composable
fun FavoriteScreen(modifier: Modifier = Modifier) {
    // 현재 찜한 작품의 ID입니다. 처음에는 세 작품 모두 찜한 상태입니다.
    var favoriteIds by rememberSaveable {
        mutableStateOf(ArrayList(sampleWorkIds))
    }

    // 찜을 해제한 직후, 회색 하트를 잠시 보여주는 작품의 ID입니다.
    val removingIds = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()

    // 찜한 작품과 회색 하트를 보여주는 중인 작품을 원래 순서로 표시합니다.
    val visibleIds = sampleWorkIds.filter { it in favoriteIds || it in removingIds }

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
            text = "내가 좋아한 작품들",
            style = MaterialTheme.typography.bodyMedium,
            color = MainText
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (visibleIds.isEmpty()) {
            Text(
                text = "아직 찜한 작품이 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MainText
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 작품 ID를 기준으로 구분해, 누른 작품만 목록에서 제거합니다.
                items(items = visibleIds, key = { it }) { workId ->
                    FavoriteWorkRow(
                        isFavorite = workId in favoriteIds,
                        onRemove = {
                            removingIds.add(workId)
                            favoriteIds = ArrayList(favoriteIds - workId)

                            scope.launch {
                                removingIds.remove(workId)
                            }
                        }
                    )
                }
            }
        }
    }
}

// 찜한 작품 한 줄을 그립니다. 포스터와 글 영역은 나중에 실제 데이터로 교체합니다.
@Composable
private fun FavoriteWorkRow(
    isFavorite: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 왼쪽 포스터 자리. 검색 화면과 같은 90 × 135dp 크기입니다.
        Box(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .background(MainCard, RoundedCornerShape(8.dp))
        )

        // 가운데 영역은 포스터와 하트 버튼 사이에 남은 너비를 사용합니다.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // 작품 제목 자리
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(22.dp)
                    .background(MainCard, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 줄거리 자리. 아래쪽을 포스터 하단에 맞춥니다.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MainCard, RoundedCornerShape(4.dp))
            )
        }

        // 맨 오른쪽 하트 버튼. 찜을 해제하면 회색으로 바뀌고 중복 클릭을 막습니다.
        IconButton(
            onClick = onRemove,
            enabled = isFavorite
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = if (isFavorite) "찜 해제" else "찜 해제됨",
                tint = if (isFavorite) Color(0xFFEF5350) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(name = "찜한 작품", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun FavoriteScreenPreview() {
    MyOTTTheme(darkTheme = true, dynamicColor = false) {
        FavoriteScreen()
    }
}