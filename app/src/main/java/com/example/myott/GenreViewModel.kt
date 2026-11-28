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
    private val repository: GenreRepository
) : ViewModel() {

    val allGenres: StateFlow<List<PreferredGenre>> =
        repository.allGenres
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val selectedGenres: StateFlow<List<PreferredGenre>> =
        repository.selectedGenres
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadTmdbGenres()
    }

    private fun loadTmdbGenres() {
        viewModelScope.launch {
            repository.fetchAndSaveTmdbGenres()
        }
    }

    fun updateGenreSelection(
        genreId: Int, // 👈 Int로 변경
        selected: Boolean
    ) {
        viewModelScope.launch {
            repository.updateGenreSelection(genreId, selected)
        }
    }
}