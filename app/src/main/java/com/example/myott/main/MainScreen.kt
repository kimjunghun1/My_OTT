package com.example.myott.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myott.API.Movie
import com.example.myott.API.MovieViewModel

// 색상 (장르 선택 화면과 같은 다크 + 보라 계열)
private val MainBackground = Color(0xFF121212)
private val MainCard = Color(0xFF202020)
private val MainPurple = Color(0xFF9363EB)
private val MainText = Color.White
private val MainSubText = Color(0xFFAAAAAA)

// 하단 탭 목록
private enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("홈", Icons.Filled.Home),
    SEARCH("검색", Icons.Filled.Search),
    INTEREST("관심", Icons.Filled.Favorite),
    MY_PAGE("마이페이지", Icons.Filled.Person),
}

// 메인 화면 (앱 실행 시 첫 화면)
// - 하단 탭: 홈 / 검색 / 관심 / 마이페이지
// - 홈 탭은 섹션 틀만 있고, 콘텐츠는 아직 띄우지 않는다. (TMDB 연동 후 채울 예정)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    // 현재 선택된 탭 (처음엔 홈). 사용자가 하단 탭을 눌러야만 바뀐다.
    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.HOME.ordinal) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
    ) {
        // 탭 내용
        Box(modifier = Modifier.weight(1f)) {
            when (MainTab.entries[selectedTab]) {
                MainTab.HOME -> HomeTab()
                MainTab.SEARCH -> SearchScreen() // TODO: 검색 화면 연결
                MainTab.INTEREST -> FavoriteScreen()// TODO: 관심 화면 연결
                MainTab.MY_PAGE -> MyPageScreen()
            }
        }

        // 하단 탭 바
        NavigationBar(
            containerColor = MainCard,
            windowInsets = WindowInsets(0, 0, 0, 0) // MainActivity의 Scaffold가 이미 여백을 처리함
        ) {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab.ordinal,
                    onClick = { selectedTab = tab.ordinal },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MainText,
                        selectedTextColor = MainPurple,
                        indicatorColor = MainPurple,
                        unselectedIconColor = MainSubText,
                        unselectedTextColor = MainSubText
                    )
                )
            }
        }
    }
}

// 홈 탭 — 섹션 제목 + 빈 포스터 자리
@Composable
private fun HomeTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 상단 바: 앱 이름
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My OTT",
                    color = MainPurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // TODO: TMDB 연동 후 각 섹션의 PosterPlaceholderRow를 실제 콘텐츠 목록으로 교체
        item { ContentSection("지금 당신을 위한 추천", "선호 장르와 관심 기록을 반영한 추천") }
        item { ContentSection("계절에 어울리는 작품", "지금 계절에 많이 보는 장르") }
        item { ContentSection("좋아하는 배우와 함께하는 작품", "관심 작품에 나온 배우의 다른 작품") }
        item { ContentSection("선호 감독의 추천 작품", "관심 작품 감독의 다른 작품") }

    }
}

// 섹션 하나: 제목 + 부제 + 빈 포스터 자리
@Composable
private fun ContentSection(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            color = MainText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = MainSubText,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        val movieViewModel: MovieViewModel = viewModel()
        val viewState by movieViewModel.movieState
        PosterPlaceholderRow(movie = viewState.movie)
    }
}

// 포스터가 들어갈 빈 자리 (가로 스크롤)
@Composable
private fun PosterPlaceholderRow(movie: Movie?) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Column {
                // 포스터 자리
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(com.example.myott.main.MainCard)
                ){
                    if(movie != null){
                        Image(
                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop, // 카드 비율에 맞게 꽉 채움
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // 제목 자리
                if(movie != null){
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(120.dp)
                    )
                } else{
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(com.example.myott.main.MainCard)
                    )
                }
            }
        }
    }
}

// 아직 담당이 정해지지 않았거나 다른 팀원이 만들 탭의 빈 자리
@Composable
private fun PlaceholderTab(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = MainSubText, fontSize = 14.sp)
    }
}

// Android Studio 미리보기용 (앱 실행과는 무관)
@Preview(showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun MainScreenPreview() {
    MainScreen()
}
