package com.example.myott.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.GenreSelectionScreen
import com.example.myott.API.Movie
import com.example.myott.API.MovieViewModel
import com.example.myott.AppContainer
import com.example.myott.GenreViewModel
import com.example.myott.WelcomeScreen
import com.example.myott.detail.ContentDetailScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.myott.WelcomeScreen
import com.example.myapplication.GenreSelectionScreen
import com.example.myott.PersonRecommendedWork
import com.example.myott.main.PersonRecommendationViewModel

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
// - 최초 실행 시: WelcomeScreen -> GenreSelectionScreen -> MainScreen
// - 하단 탭: 홈 / 검색 / 관심 / 마이페이지
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    // 현재 몇 번째 하단 탭이 선택되었는지 숫자로 기억(숫자 전용 상태 상자에 저장) 홈 화면의 인덱스를 가져옴
    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.HOME.ordinal) }
    // 상세 페이지로 넘어간 콘텐츠 id (null이 아니면 상세 페이지 들어간 상태, null이면 상세 페이지 닫힘)
    var detailMovieId by rememberSaveable { mutableStateOf<Int?>(null) }
    // 상세 페이지로 넘어간 콘텐츠 유형이 "movie"인지 "tv"인지 구분하는 변수(movie와 tv콘텐츠의 id가 같은 경우가 있기 때문)
    var detailMediaType by rememberSaveable { mutableStateOf("movie") } // 기본값은 movie

    // 상세 페이지로 넘어갈 때 detailMovieId와 detailMediaType을 변경하기 위한 함수
    fun openDetail(id: Int, mediaType: String = "movie") {
        detailMovieId = id
        detailMediaType = mediaType
    }

    // SharedPreferences로 최초 실행 여부 확인
    val context = LocalContext.current // 현재 화면의 context(지금 실행 중인 내 앱의 신분증)
    val prefs = context.getSharedPreferences(
        "myott_preferences",
        android.content.Context.MODE_PRIVATE
    )

    // 최초 실행 여부에 따라 첫 화면 결정
    var currentScreen by rememberSaveable {
        mutableStateOf(
            if (prefs.getBoolean("first_launch_completed", false)) {
                "MAIN"
            } else {
                "WELCOME"
            }
        )
    }

    // 장르 선택 화면에서 사용할 ViewModel
    val appContainer = context.applicationContext as AppContainer
    val genreViewModel: GenreViewModel = viewModel(
        factory = appContainer.genreViewModelFactory
    )

    // 최초 실행 화면 분기
    if (currentScreen == "WELCOME") {
        // 웰컴 화면
        WelcomeScreen(
            onStartClick = {
                currentScreen = "GENRE"
            },
            modifier = modifier
        )
    } else if (currentScreen == "GENRE") {
        // 장르 선택 화면
        GenreSelectionScreen(
            genreViewModel = genreViewModel,
            modifier = modifier,
            // 뒤로가기: 웰컴 화면으로 이동
            onBack = {
                currentScreen = "WELCOME"
            },
            // 장르 선택 완료: 메인 화면으로 이동
            onComplete = {
                prefs.edit()
                    .putBoolean("first_launch_completed", true)
                    .apply()

                currentScreen = "MAIN"
            },
            // 건너뛰기 확인: 메인 화면으로 이동
            onSkip = {
                prefs.edit()
                    .putBoolean("first_launch_completed", true)
                    .apply()

                currentScreen = "MAIN"
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MainBackground)
        ) {
            // 탭 내용
            Box(modifier = Modifier.weight(1f)) {
                val movieId = detailMovieId
                if (movieId != null) {
                    // 콘텐츠 상세 화면 (휴대폰 뒤로가기 / 화면의 뒤로가기 버튼으로 닫힘)
                    BackHandler { detailMovieId = null }
                    ContentDetailScreen(
                        movieId = movieId,
                        mediaType = detailMediaType,
                        onBack = { detailMovieId = null }
                    )
                } else when (MainTab.entries[selectedTab]) {
                    // id와 type을 모두 넘겨주는 이유는 id는 같은데 type이 다른 경우가 있기 때문
                    MainTab.HOME -> HomeTab(onMovieClick = { id, type -> openDetail(id, type) }) // id와 type을 openDetail에 넘겨줘 detailMovieId와 detailMediaType을 변경한다.
                    MainTab.SEARCH -> SearchScreen(onMovieClick = { id, type -> openDetail(id, type) })
                    MainTab.INTEREST -> FavoriteScreen(onMovieClick = { id, type -> openDetail(id, type) })
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
                        onClick = {
                            selectedTab = tab.ordinal
                            detailMovieId = null // 탭을 누르면 상세 화면 닫기
                        },
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
}

// 홈 탭 — 섹션 제목 + 빈 포스터 자리
@Composable
private fun HomeTab(onMovieClick: (Int, String) -> Unit) {
    val context = LocalContext.current // 현재 화면의 안드로이드 시스템 정보
    val appContainer = context.applicationContext as AppContainer // 앱의 최상위 본부(applicationContext)를 불러와서 AppContainer 형태로 열어봄
    val homeViewModel: HomeViewModel = viewModel( // 화면의 데이터를 책임지는 HomeViewModel 생성
        factory = HomeViewModel.factory(appContainer.recommendationRepository) // appContainer.recommendationRepository 전달
    )
    val recommendedMovies by homeViewModel.recommendedList.collectAsState() // HomeViewModel의 recommendedList를 화면과 실시간으로 연결

    // 추가: 배우 및 감독 추천 ViewModel
    val personRecommendationViewModel: PersonRecommendationViewModel =
        viewModel(
            factory = appContainer.personRecommendationViewModelFactory
        )

    // 추가: 감독 추천 작품 목록
    val directorWorks by personRecommendationViewModel.directorWorks.collectAsState()

    // 추가: 배우 추천 작품 목록
    val actorWorks by personRecommendationViewModel.actorWorks.collectAsState()

    // 추가: 추천 작품 로딩 상태
    val personRecommendationLoading by personRecommendationViewModel.isLoading.collectAsState()

    // 홈 탭에 돌아올 때마다 최신 점수/관심없음 상태를 반영해 추천 목록 갱신
    androidx.compose.runtime.LaunchedEffect(Unit) { // LaunchedEffect는 괄호 안의 값이 바뀔 때만 중괄호 안 코드 실행->Unit이므로 홈 화면에 처음 진입할 때 1번만 실행
        homeViewModel.refreshRecommendations() // 화면 새로고침
        personRecommendationViewModel.loadRecommendations() // 추가: 배우 및 감독 추천 새로고침
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
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

        // 1~3단계 알고리즘이 반영된 메인 추천 섹션
        item {
            RecommendedSection(
                title = "지금 당신을 위한 추천",
                subtitle = "선호 장르와 취향 점수를 반영한 맞춤 추천",
                movies = recommendedMovies,
                onMovieClick = onMovieClick
            )
        }

        // 2. 배우 추천 작품
        item {
            PersonRecommendedSection(
                title = "좋아하는 배우와 함께하는 작품",
                subtitle = "관심 작품에 나온 배우의 다른 작품",
                works = actorWorks,
                isLoading = personRecommendationLoading,
                onMovieClick = onMovieClick
            )
        }

        // 3. 감독 추천 작품
        item {
            PersonRecommendedSection(
                title = "선호 감독의 추천 작품",
                subtitle = "관심 작품 감독의 다른 작품",
                works = directorWorks,
                isLoading = personRecommendationLoading,
                onMovieClick = onMovieClick
            )
        }
    }
}

@Composable
private fun RecommendedSection( // 콘텐츠 추천 슬라이드를 그려줌
    title: String,
    subtitle: String,
    movies: List<Movie>, // 화면에 뿌려줄 콘텐츠 목록 데이터
    onMovieClick: (Int, String) -> Unit // 포스터를 눌렀을 때 실행됨
) {
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (movies.isEmpty()) { // 데이터가 아직 없을 때 ex) 로딩 중 화면
                items(5) { // 이 안에 적은 UI 디자인을 5번 반복해서 그려줌
                    Column {
                        Box(
                            modifier = Modifier
                                .size(width = 120.dp, height = 180.dp)
                                .clip(RoundedCornerShape(10.dp)) // 사각형의 네 귀퉁이를 둥글게 잘라냄
                                .background(MainCard) // 사각형 안쪽을 MainCard 색상으로 채움
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 90.dp, height = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MainCard)
                        )
                    }
                }
            } else { // 데이터가 도착했을 때
                items(movies.size) { index -> // movies에 들어있는 작품 개수만큼 반복해서 그려줌. index는 현재 순번을 알려주는 변수
                    val movie = movies[index] // movies 목록에서 현재 index에 해당하는 콘텐츠 데이터 1개를 movie에 저장
                    Column(
                        modifier = Modifier.clickable { // 포스터나 제목을 클릭하면 아래 코드가 실행됨
                            onMovieClick(movie.id, movie.mediaType) // 클릭한 콘텐츠의 id와 type이 openDetail()에게 전달되고 상세 페이지로 이동
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 120.dp, height = 180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MainCard)
                        ) {
                            if (movie.poster_path != null) {
                                Image( // 포스터를 가져와서 그림
                                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"), // rememberAsyncImagePainter를 통해 포스터 이미지를 가져옴
                                    contentDescription = movie.title, // 화면 읽기 기능을 켰을 때 콘텐츠 제목을 읽어줌
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop, // 사진을 틀 크기에 맞춤
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 2, // 최대 줄 수 제한
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, // 제목이 너무 길 때 ...처리
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(120.dp) // 제목 텍스트의 가로 폭 제한
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PersonRecommendedSection(
    title: String,
    subtitle: String,
    works: List<PersonRecommendedWork>,
    isLoading: Boolean,
    onMovieClick: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 24.dp)
    ) {

        // 섹션 제목
        Text(
            text = title,
            color = MainText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 섹션 설명
        Text(
            text = subtitle,
            color = MainSubText,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 포스터 가로 스크롤
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 로딩 중이거나 아직 데이터가 없을 때
            if (isLoading && works.isEmpty()) {
                items(5) {
                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MainCard)
                    )
                }
            }

            // 추천 작품이 없을 때
            else if (works.isEmpty()) {
                item {
                    Text(
                        text = "추천 작품이 없습니다.",
                        color = MainSubText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(
                            vertical = 20.dp
                        )
                    )
                }
            }

            // 실제 추천 작품 표시
            else {
                items(works.size) { index ->

                    val work = works[index]

                    Column(
                        modifier = Modifier.clickable {
                            // 영화인지 TV 시리즈인지 구분해서 상세 화면으로 이동
                            onMovieClick(
                                work.id,
                                work.mediaType
                            )
                        }
                    ) {

                        // 포스터
                        Box(
                            modifier = Modifier
                                .size(
                                    width = 120.dp,
                                    height = 180.dp
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(MainCard)
                        ) {
                            if (work.posterPath != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        "https://image.tmdb.org/t/p/w500${work.posterPath}"
                                    ),
                                    contentDescription = work.title,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 작품 제목
                        Text(
                            text = work.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
        }
    }
}


/*
// 섹션 하나: 제목 + 부제 + 빈 포스터 자리
@Composable
private fun ContentSection(title: String, subtitle: String, onMovieClick: (Int) -> Unit) {
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
        PosterPlaceholderRow(movie = viewState.movie, onMovieClick = onMovieClick)
    }
}

// 포스터가 들어갈 빈 자리 (가로 스크롤)
@Composable
private fun PosterPlaceholderRow(movie: Movie?, onMovieClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            // 포스터를 누르면 상세 화면으로 이동
            Column(
                modifier = Modifier.clickable(enabled = movie != null) {
                    movie?.let { onMovieClick(it.id) }
                }
            ) {
                // 포스터 자리
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MainCard)
                ) {
                    if (movie != null) {
                        Image(
                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // 제목 자리
                if (movie != null) {
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(120.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MainCard)
                    )
                }
            }
        }
    }
}

//// 아직 담당이 정해지지 않았거나 다른 팀원이 만들 탭의 빈 자리
//@Composable
//private fun PlaceholderTab(title: String, description: String) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = title, color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(text = description, color = MainSubText, fontSize = 14.sp)
//    }
//}
*/