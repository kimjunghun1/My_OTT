package com.example.myott.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myott.API.MovieDetail
import com.example.myott.API.Provider
import com.example.myott.API.tmdbService
import com.example.myott.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 상세 화면에 보여줄 상태
data class DetailUiState(
    val loading: Boolean = true,
    val detail: MovieDetail? = null,
    val error: String? = null,
)

// 콘텐츠 상세 화면 ViewModel
// - TMDB에서 상세 정보(포스터, 제목, 개요, 장르, 배우, 감독, OTT)를 가져온다.
// - 관심 있음 / 관심 없음 / OTT 보러가기를 누르면 점수를 기록한다.
class ContentDetailViewModel(
    private val movieId: Int,
    private val mediaType: String = "movie",
    private val repository: ContentScoreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // 이 콘텐츠에 대한 내 기록 (관심 있음/없음 버튼 상태)
    val record: StateFlow<ContentRecord?> = repository.observeRecord(movieId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadDetail()
    }

    // TMDB에서 상세 정보 불러오기 ('다시 시도' 버튼에서도 사용)
    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState(loading = true)
            if (BuildConfig.TMDB_API_KEY.isBlank()) {
                _uiState.value = DetailUiState(
                    loading = false,
                    error = "TMDB API 키가 없어요.\nlocal.properties에 TMDB_API_KEY를 추가해 주세요."
                )
                return@launch
            }
            try {
                val detail = if (mediaType == "tv") {
                    tmdbService.getTvDetail(
                        tvId = movieId,
                        apiKey = BuildConfig.TMDB_API_KEY
                    )
                } else {
                    tmdbService.getMovieDetail(
                        movieId = movieId,
                        apiKey = BuildConfig.TMDB_API_KEY
                    )
                }
                _uiState.value = DetailUiState(loading = false, detail = detail)
                repository.recordDetailView(detail.genres.orEmpty())
            } catch (e: Exception) {
                _uiState.value = DetailUiState(
                    loading = false,
                    error = "정보를 불러오지 못했어요.\n${e.message}"
                )
            }
        }
    }

    // '관심 있어요' 버튼
    fun onInterestClick() {
        val info = contentInfo() ?: return
        val now = record.value?.isInterested == true
        viewModelScope.launch { repository.setInterested(info, !now) }
    }

    // '관심 없어요' 버튼
    fun onNotInterestedClick() {
        val info = contentInfo() ?: return
        val now = record.value?.isNotInterested == true
        viewModelScope.launch { repository.setNotInterested(info, !now) }
    }

    // OTT '보러가기' — 링크 열기 전에 기록
    fun onOttClick(@Suppress("UNUSED_PARAMETER") provider: Provider) {
        val info = contentInfo() ?: return
        viewModelScope.launch { repository.recordOttClick(info) }
    }

    private fun contentInfo(): ContentInfo? {
        val detail = _uiState.value.detail ?: return null
        return ContentInfo(
            movieId = detail.id,
            mediaType = mediaType,
            title = detail.title ?: detail.original_title.orEmpty(),
            posterPath = detail.poster_path,
            genres = detail.genres.orEmpty()
        )
    }

    companion object {
        fun factory(
            movieId: Int,
            mediaType: String = "movie",
            repository: ContentScoreRepository
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ContentDetailViewModel(movieId, mediaType, repository) }
            }
    }
}
