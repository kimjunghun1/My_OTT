package com.example.myott.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

// 필터에 표시할 작품 유형을 화면 순서대로 정의한다.
private val titleTypes = listOf("드라마", "애니메이션", "영화")

// 장르 칩의 표시 순서와 선택지를 정의한다.
private val titleGenres = listOf("로맨스", "SF", "공포", "액션", "스릴러", "코미디", "다큐멘터리")

/** [modifier]로 화면 여백을 적용하고 제목 입력란과 선택한 필터를 표시한다. */
@Composable
internal fun SearchScreen(modifier: Modifier = Modifier) {
    // 사용자가 입력 중인 제목을 보관한다. 빈 값이어도 필터를 열 수 있다.
    var input by rememberSaveable { mutableStateOf("") }
    // 선택창에서 확정한 작품 유형을 보관한다.
    var types by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    // 선택창에서 확정한 장르를 보관한다.
    var genres by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    // 필터 선택창의 열림 상태를 보관한다.
    var showFilters by rememberSaveable { mutableStateOf(false) }
    // 필터를 열거나 제목 입력을 마치면 입력란의 포커스를 해제한다.
    val focus = LocalFocusManager.current

    Column(
        modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
            Spacer(Modifier.height(16.dp))
            Text("작품 검색", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("작품 제목") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    trailingIcon = {
                        // 돋보기를 누르면 키보드의 검색 키와 같이 제목 입력란의 포커스를 해제한다.
                        IconButton(onClick = { focus.clearFocus() }) {
                            Icon(Icons.Filled.Search, contentDescription = "제목 검색")
                        }
                    }
                )
                FilledTonalButton(onClick = {
                    focus.clearFocus()
                    showFilters = true
                }) { Text("필터") }
            }
            if (types.isNotEmpty() || genres.isNotEmpty()) {
                Text(
                    "선택한 조건  " + (types + genres).joinToString(" · "),
                    Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showFilters) {
        FilterSheet(types, genres, onDismiss = { showFilters = false }) { selectedTypes, selectedGenres ->
            types = selectedTypes
            genres = selectedGenres
            showFilters = false
        }
    }
}

/**
 * [types]와 [genres]를 초기 선택값으로 사용한다.
 * 검색 버튼은 [onApply]로 선택을 확정하고, 뒤로 가기는 [onDismiss]로 변경을 취소한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    types: List<String>,
    genres: List<String>,
    onDismiss: () -> Unit,
    onApply: (ArrayList<String>, ArrayList<String>) -> Unit
) {
    // 선택창 안에서 편집 중인 작품 유형이다.
    var draftTypes by rememberSaveable { mutableStateOf(ArrayList(types)) }
    // 선택창 안에서 편집 중인 장르다.
    var draftGenres by rememberSaveable { mutableStateOf(ArrayList(genres)) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("검색 필터", style = MaterialTheme.typography.headlineSmall)
            FilterGroup("작품 유형", titleTypes, draftTypes) { draftTypes = it }
            FilterGroup("장르", titleGenres, draftGenres) { draftGenres = it }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    draftTypes = arrayListOf()
                    draftGenres = arrayListOf()
                }) { Text("초기화") }
                Button(onClick = { onApply(draftTypes, draftGenres) }, modifier = Modifier.weight(1f)) {
                    Text("검색")
                }
            }
        }
    }
}

/** [label] 아래에 [options]를 칩으로 표시하고 [selected]가 바뀌면 [onChange]로 전달한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(
    label: String,
    options: List<String>,
    selected: List<String>,
    onChange: (ArrayList<String>) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                // 현재 항목의 선택 표시와 다음 클릭 동작을 결정한다.
                val checked = option in selected
                FilterChip(
                    selected = checked,
                    onClick = {
                        // 누른 항목을 추가하거나 해제한 새 선택 목록이다.
                        val next = if (checked) selected - option else selected + option
                        onChange(ArrayList(next))
                    },
                    label = { Text(option) },
                    leadingIcon = if (checked) { { Text("✓") } } else null
                )
            }
        }
    }
}
