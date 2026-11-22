package com.example.myott.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myott.ui.theme.MyOTTTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.myott.API.Movie
import com.example.myott.API.tmdbService
import com.example.myott.BuildConfig
import kotlinx.coroutines.CancellationException

// OTT 유형 제한을 해제할 때 사용하는 선택지
private const val ALL_TYPES = "전체"

// 카테고리 창에 표시할 콘텐츠 유형. 목록에 적힌 순서대로 표시
private val ContentsTypes = listOf(ALL_TYPES, "영화","TV 프로그램")

// 영화와 TV에서 같은 ID를 사용하는 장르입니다.
// 콘텐츠 유형이 '전체'이면 이 장르들만 표시합니다.
private val commonGenres = mapOf(
    "애니메이션" to 16,
    "코미디" to 35,
    "범죄" to 80,
    "다큐멘터리" to 99,
    "드라마" to 18,
    "가족" to 10751,
    "미스터리" to 9648,
    "서부" to 37
)

// 영화에서는 SF와 판타지가 각각 별도의 장르입니다.
private val movieGenres = commonGenres + mapOf(
    "액션" to 28,
    "모험" to 12,
    "판타지" to 14,
    "역사" to 36,
    "공포" to 27,
    "음악" to 10402,
    "로맨스" to 10749,
    "SF" to 878,
    "TV 영화" to 10770,
    "스릴러" to 53,
    "전쟁" to 10752
)

// TV에서는 액션·모험, SF·판타지가 각각 하나의 장르입니다.
private val tvGenres = commonGenres + mapOf(
    "액션·모험" to 10759,
    "어린이" to 10762,
    "뉴스" to 10763,
    "리얼리티" to 10764,
    "SF·판타지" to 10765,
    "소프 오페라" to 10766,
    "토크쇼" to 10767,
    "전쟁·정치" to 10768
)

// 카테고리 창과 검색 버튼이 동일한 장르 대응표를 사용합니다.
private fun genresForType(type: String): Map<String, Int> = when (type) {
    "영화" -> movieGenres
    "TV 프로그램" -> tvGenres
    else -> commonGenres
}

//검색 버튼을 누른 시점의 조건, 장르를 고른 순서가 달라도 같은 조건으로 취급
private data class ContentQuery(
    val title: String,
    val type: String,
    val genreIds: Set<Int>
)

private val MainBackground = Color(0xFF121212)
private val MainCard = Color(0xFF202020)
private val MainPurple = Color(0xFF9363EB)
private val MainText = Color.White
private val MainSubText = Color(0xFFAAAAAA)

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 검색어 입력창, 카테고리 버튼, 검색 버튼을 표시하는 화면입니다.
 *   query는 앞뒤 공백을 제거한 검색어입니다.
 *   type은 선택한 OTT 유형이며, '전체'이면 null입니다.
 *   genres는 선택한 장르 목록이며, 빈 목록이면 장르 제한이 없습니다.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onSearch: (query: String, type: String?, genres: List<String>) -> Unit = { _, _, _ -> }
) {
    //입력창에 표시할 검색어, remeeberSaveable을 사용해 화면 회전 후에도 입력값을 복원함
    var query by rememberSaveable { mutableStateOf("") }
    //선택한 OTT유형
    var selectedType by rememberSaveable { mutableStateOf(ALL_TYPES) }
    //선택한 장르
    var selectedGenres by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    //카테고리 선택창 표시
    var showCategories by rememberSaveable { mutableStateOf(false) }
    // 검색을 실행하거나 카테고리 창을 열 때 입력창의 포커스를 해제하는 데 사용합니다.
    val focusManager = LocalFocusManager.current
    // 입력창에 포커스가 오면 테두리 색도 함께 바뀌도록 상태를 공유합니다.
    val interactionSource = remember { MutableInteractionSource() }

    // 화면에 표시할 작품 목록과 로딩·오류 상태를 보관합니다.
    var trendingItems by remember { mutableStateOf(emptyList<Movie>()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    // 현재 검색 조건과 아직 보여주지 않은 후보를 보관합니다.
    var activeSearch by remember { mutableStateOf<ContentSearch?>(null) }
    // 같은 조건으로 다시 눌러도 검색을 한 번 더 실행하기 위한 번호입니다.
    var searchNumber by remember { mutableIntStateOf(0) }

    LaunchedEffect(searchNumber) {
        isLoading = true
        loadError = null

        try {
            val search = activeSearch

            if (search == null) {
                // 첫 진입에는 현재 구현한 주간 인기 영화·TV 10편을 표시합니다.
                val apiKey = BuildConfig.TMDB_API_KEY
                val movies = tmdbService.getWeeklyTrending("movie", apiKey)
                    .results.distinctBy { it.id }.shuffled()
                val tvShows = tmdbService.getWeeklyTrending("tv", apiKey)
                    .results.distinctBy { it.id }.shuffled()

                val firstPicks = movies.take(1) + tvShows.take(1)
                val remaining = (movies.drop(1) + tvShows.drop(1)).shuffled()
                trendingItems = (firstPicks + remaining.take(10 - firstPicks.size)).shuffled()
            } else {
                // 검색 버튼을 누른 뒤에는 제출한 조건에 맞는 다음 작품들을 표시합니다.
                trendingItems = search.nextBatch()
            }
            isLoading = false
        } catch (e: CancellationException) {
            // 새 검색이 시작되거나 탭을 떠나면 진행 중인 요청을 취소합니다.
            throw e
        } catch (e: Exception) {
            loadError = "콘텐츠를 불러오지 못했어요. 다시 검색해 주세요."
            isLoading = false
        }
    }

    fun submitSearch() {
        focusManager.clearFocus()

        val availableGenres = genresForType(selectedType)
        val conditions = ContentQuery(
            title = query.trim(),
            type = selectedType,
            genreIds = selectedGenres.mapNotNull { availableGenres[it] }.toSet()
        )

        // 조건이 바뀌면 첫 페이지부터, 같으면 이전 검색의 남은 후보부터 시작합니다.
        if (activeSearch?.query != conditions) {
            activeSearch = ContentSearch(conditions)
        }
        searchNumber++

        // 기존 SearchScreen의 콜백도 유지합니다.
        onSearch(
            conditions.title,
            selectedType.takeUnless { it == ALL_TYPES },
            selectedGenres.toList()
        )
    }

    MyOTTTheme(darkTheme = true, dynamicColor = false) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MainBackground
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OTT 검색",
                    modifier = Modifier.padding(bottom = 12.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MainPurple,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        singleLine = true,
                        interactionSource = interactionSource,
                        // 입력한 글자에 테마의 본문 스타일과 글자색을 적용합니다.
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),

                        // 키보드에 검색 키를 표시하고, 누르면 검색을 실행합니다.
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { submitSearch() }),

                        // 실제 입력 영역(innerTextField) 주위에 테두리, 안내 문구, 돋보기를 배치합니다.
                        decorationBox = { innerTextField ->
                            OutlinedTextFieldDefaults.DecorationBox(
                                value = query,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = true,
                                visualTransformation = VisualTransformation.None,
                                interactionSource = interactionSource,

                                // 위아래 여백은 없애고, 글자가 왼쪽 테두리에 붙지 않도록 합니다.
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),

                                placeholder = { Text("검색어", maxLines = 1) },
                                trailingIcon = {
                                    IconButton(onClick = { submitSearch() }) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = "검색"
                                        )
                                    }
                                }
                            )
                        }
                    )
                    // 햄버거 아이콘을 누르면 카테고리 선택 창을 엽니다.
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showCategories = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "카테고리 선택",
                            modifier = Modifier.size(80.dp),
                            tint = MainPurple
                        )
                    }
                }

                // 적용된 조건을 검색창 아래에 표시합니다.
                val filters = listOfNotNull(selectedType.takeUnless { it == ALL_TYPES }) +
                        selectedGenres
                if (filters.isNotEmpty()) {
                    Text(
                        text = filters.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                // 검색창 아래에 남은 공간을 사용하고, 결과 목록만 세로로 스크롤합니다.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 요청 상태에 따라 로딩 표시, 오류 메시지, 빈 목록 안내 또는 작품 목록을 보여줍니다.
                    when {
                        isLoading -> item {
                            CircularProgressIndicator()
                        }
                        loadError != null -> item {
                            Text(text = loadError.orEmpty(), color = MainSubText)
                        }
                        trendingItems.isEmpty() -> item {
                            val message = if (activeSearch == null) {
                                "표시할 인기 콘텐츠가 없어요."
                            } else {
                                "이 조건으로 표시할 새 작품이 없어요."
                            }
                            Text(text = message, color = MainSubText)
                        }
                        else -> items(trendingItems) { content ->
                            SearchResultRow(content)
                        }
                    }
                }
            }
        }


        if (showCategories) {
            CategoryDialog(
                initialType = selectedType,
                initialGenres = selectedGenres,
                // 취소, 뒤로 가기, 창 바깥 터치 시 창만 닫습니다.
                onDismiss = { showCategories = false },
                // type과 genres는 사용자가 '적용'을 눌러 확정한 유형과 장르.
                onApply = { type, genres ->
                    selectedType = type
                    selectedGenres = ArrayList(genres)
                    showCategories = false
                }
            )
        }
    }
}

// 같은 조건으로 검색하는 동안 남은 후보와 다음 페이지를 기억
private class ContentSearch(val query: ContentQuery) {

    private val types = when (query.type) {
        "영화" -> listOf("movie")
        "TV 프로그램" -> listOf("tv")
        else -> listOf("movie", "tv")
    }

    // 유형별 다음 페이지. 마지막 페이지까지 읽은 유형은 여기서 제거.
    private val nextPages = types.associateWith { 1 }.toMutableMap()
    private val remaining = mutableListOf<Movie>()

    // 영화와 TV에 같은 숫자 ID가 있을 수 있으므로 유형도 함께 기록.
    private val collected = mutableSetOf<Pair<String, Int>>()

    suspend fun nextBatch(): List<Movie> {
        val apiKey = BuildConfig.TMDB_API_KEY
        val genres = query.genreIds.takeIf { it.isNotEmpty() }?.joinToString(",")

        while (remaining.size < 10 && nextPages.isNotEmpty()) {
            // '전체'이면 영화와 TV에서 한 페이지씩 읽은 후 후보를 섞습니다.
            for (type in types) {
                val page = nextPages[type] ?: continue

                val response = if (query.title.isBlank()) {
                    // 제목이 비어 있으면 TMDB가 선택한 장르에 맞는 작품을 찾습니다.
                    tmdbService.discoverContents(type, apiKey, genres, page)
                } else {
                    // 제목이 있으면 해당 유형의 제목 검색 API를 사용합니다.
                    tmdbService.searchByTitle(type, apiKey, query.title, page)
                }

                for (content in response.results) {
                    if (query.title.isNotBlank()) {
                        // 화면에 표시될 제목에 검색어가 포함된 작품만 남깁니다.
                        if (!content.title.contains(query.title, ignoreCase = true)) continue

                        // 제목 검색 결과에도 선택한 장르를 모두 적용합니다.
                        if (!content.genreIds.orEmpty().containsAll(query.genreIds)) continue
                    }

                    if (collected.add(type to content.id)) {
                        remaining.add(content.copy(mediaType = type))
                    }
                }

                // 이 유형에 다음 페이지가 있을 때만 이어서 조회합니다.
                if (page < response.totalPages.coerceAtMost(500)) {
                    nextPages[type] = page + 1
                } else {
                    nextPages.remove(type)
                }
            }
        }

        // 현재 확보한 후보에서 최대 10편을 뽑고, 표시한 작품은 후보에서 꺼냅니다.
        remaining.shuffle()
        val count = minOf(10, remaining.size)
        val picked = remaining.take(count)
        remaining.subList(0, count).clear()
        return picked
    }
}

// type과 genres는 사용자가 '적용'을 눌러 확정한 유형과 장르.
@Composable
private fun CategoryDialog(
    initialType: String,
    initialGenres: List<String>,
    onDismiss: () -> Unit,
    onApply: (String, List<String>) -> Unit
) {
    // 창 안에서 편집하는 값입니다. '적용'을 눌렀을 때만 부모 화면에 전달합니다.
    var draftType by rememberSaveable { mutableStateOf(initialType) }
    var draftGenres by rememberSaveable { mutableStateOf(ArrayList(initialGenres)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("카테고리 선택") },
        text = {
            Column(
                // 작은 화면에서도 아래쪽 장르까지 스크롤해 선택할 수 있습니다.
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("콘텐츠 유형", style = MaterialTheme.typography.titleMedium)
                CategoryChips(
                    options = ContentsTypes,
                    selected = listOf(draftType),
                    onClick = { newType ->
                        draftType = newType

                        // 유형을 바꿔도 공통 장르는 유지하고, 새 유형에 없는 장르는 해제합니다.
                        val availableGenres = genresForType(newType)
                        draftGenres = ArrayList(draftGenres.filter { it in availableGenres })
                    }
                )

                Spacer(Modifier.height(4.dp))

                Text("장르 (복수 선택)", style = MaterialTheme.typography.titleMedium)
                CategoryChips(
                    options = genresForType(draftType).keys.toList(),
                    selected = draftGenres,
                    onClick = { genre ->
                        // genre는 방금 누른 장르입니다. 이미 선택했다면 빼고,
                        // 아직 선택하지 않았다면 추가한 새 목록을 상태에 대입합니다.
                        draftGenres = ArrayList(
                            if (genre in draftGenres) draftGenres - genre
                            else draftGenres + genre
                        )
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
        confirmButton = {
            Button(onClick = { onApply(draftType, draftGenres) }) { Text("적용") }
        }
    )
}

/**
 * 유형이나 장르 목록을 누를 수 있는 칩으로 표시합니다.
 *
 * @param options 화면에 표시할 전체 항목입니다.
 * @param selected 선택된 모양으로 표시할 항목입니다.
 * @param onClick 사용자가 누른 항목의 이름을 전달합니다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    options: List<String>,
    selected: List<String>,
    onClick: (String) -> Unit
) {
    // 한 줄이 가득 차면 다음 칩을 자동으로 아래 줄에 배치합니다.
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // option은 현재 그리는 칩 하나의 이름입니다
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onClick(option) },
                label = { Text(option) }
            )
        }
    }
}

// 작품 한 편의 포스터를 왼쪽에, 제목과 줄거리를 오른쪽에 표시합니다.
@Composable
private fun SearchResultRow(content: Movie) {
    Row(
        modifier = Modifier.fillMaxWidth().height(135.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(MainCard),
            contentAlignment = Alignment.Center
        ) {
            val path = content.poster_path
            if (path.isNullOrBlank()) {
                Text("포스터 없음", color = MainSubText)
            } else {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500$path",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(
                text = content.title,
                color = MainText,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = content.overview.ifBlank { "등록된 줄거리가 없어요." },
                color = MainSubText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "OTT 검색", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SearchScreenPreview() {
    SearchScreen()
}