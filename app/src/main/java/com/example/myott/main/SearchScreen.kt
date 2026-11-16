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

// OTT 유형 제한을 해제할 때 사용하는 선택지
private const val ALL_TYPES = "전체"

// 카테고리 창에 표시할 OTT 유형. 목록에 적힌 순서대로 표시
private val ottTypes = listOf(ALL_TYPES, "드라마", "영화", "애니메이션")

// 카테고리 창에서 여러 개 선택할 수 있는 장르 목록
private val ottGenres = listOf(
    "SF", "느와르", "액션", "공포", "코미디", "판타지",
    "로맨스", "역사", "뮤지컬", "미스터리", "애니메이션", "다큐멘터리"
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

    fun submitSearch() {
        focusManager.clearFocus()
        onSearch(
            query.trim(),
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
                    // TMDB 연동 전에는 배치를 확인할 수 있도록 결과 세 건을 표시합니다.
                    items(count = 10) {
                        SearchResultPlaceholder()
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

// type과 genres는 사용자가 '적용'을 눌러 확정한 유형과 장르입니다.
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
                Text("OTT 유형", style = MaterialTheme.typography.titleMedium)
                CategoryChips(
                    options = ottTypes,
                    selected = listOf(draftType),
                    onClick = { draftType = it }
                )

                Spacer(Modifier.height(4.dp))

                Text("장르 (복수 선택)", style = MaterialTheme.typography.titleMedium)
                CategoryChips(
                    options = ottGenres,
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

// 검색 결과 한 건의 포스터, 제목, 줄거리 자리를 회색으로 표시합니다.
@Composable
private fun SearchResultPlaceholder() {
    val placeholderColor = MainCard
    val textShape = RoundedCornerShape(4.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp), // 포스터와 오른쪽 내용이 공유하는 높이
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 왼쪽 포스터 자리. 가로 90dp, 세로 135dp로 2:3 비율입니다.
        Box(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .background(placeholderColor, RoundedCornerShape(8.dp))
        )

        // 오른쪽은 포스터 옆에 남은 너비만큼 사용합니다.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // 제목 자리. 포스터 상단에 맞추고 오른쪽 너비의 75%를 채웁니다.
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(22.dp)
                    .background(placeholderColor, textShape)
            )

            // 제목과 줄거리 사이의 간격입니다.
            Spacer(modifier = Modifier.height(12.dp))

            // 줄거리 자리. 제목 아래부터 포스터 하단까지 남은 높이를 채웁니다.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(placeholderColor, textShape)
            )
        }
    }
}

@Preview(name = "OTT 검색", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SearchScreenPreview() {
    SearchScreen()
}