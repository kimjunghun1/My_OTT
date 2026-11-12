package com.example.myott

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genreapp.data.PreferredGenre
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel: 화면(UI)에 필요한 데이터를 관리하고, 사용자의 요청을 Repository에 전달하는 중간 관리자
class GenreViewModel(
    private val repository: GenreRepository // Repository 객체를 외부에서 전달받음
) : ViewModel() { // ViewModel 클래스 상속 -> 화면이 회전하거나 재구성되는 등의 상황에서도 화면에 필요한 데이터 유지

    // 전체 장르 목록을 StateFlow로 관리
    val allGenres: StateFlow<List<PreferredGenre>> =
        repository.allGenres
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Flow를 StateFlow로 변환하여 UI에서 안전하게 관찰
    val selectedGenres: StateFlow<List<PreferredGenre>> = repository.selectedGenres
        // StateFlow를 사용해 사용자 정보의 현재 상태를 보관하면서 전달
        .stateIn( // Flow를 StateFlow로 변환해주는 함수
            scope = viewModelScope, // viewModelScope는 ViewModel에 연결된 코루틴 실행 공간
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // ViewModel이 생성될 때 백그라운드 코루틴(viewModelScope)에서 초기 데이터 저장
        saveDefaultGenres() // 아래 함수를 호출해 앱을 처음 실행했을 때 장르 목록 보여줌
    }

    // 기본 장르 저장
    private fun saveDefaultGenres() {
        viewModelScope.launch {
            repository.saveDefaultGenres()
        }
    }

    // 사용자가 선택한 장르의 상태 변경
    fun updateGenreSelection(
        genreId: String,
        selected: Boolean
    ) {
        viewModelScope.launch {
            repository.updateGenreSelection(
                genreId,
                selected
            )
        }
    }

}