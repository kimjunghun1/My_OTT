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

    private fun saveDefaultGenres() {
        viewModelScope.launch { // saveDefaultGenres()는 suspend 함수이기 때문에 코루틴을 실행하는 launch 사용
            val defaultGenres = listOf(
                PreferredGenre("SF", "SF", isSelected = true),
                PreferredGenre("Noir", "느와르", isSelected = true),
                PreferredGenre("ACTION", "액션", isSelected = true),
                PreferredGenre("HORROR", "공포", isSelected = true),
                PreferredGenre("COMEDY", "코미디", isSelected = true),
                PreferredGenre("FANTASY", "판타지", isSelected = true),
                PreferredGenre("ROMANCE", "로맨스", isSelected = true),
                PreferredGenre("HISTORY", "역사", isSelected = true),
                PreferredGenre("MUSICAL", "뮤지컬", isSelected = true),
                PreferredGenre("MYSTERY", "미스터리", isSelected = true),
                PreferredGenre("ANIMATION", "애니메이션", isSelected = true),
                PreferredGenre("DOCUMENTARY", "다큐멘터리", isSelected = true)
            )
            repository.saveDefaultGenres(defaultGenres)
        }
    }
}