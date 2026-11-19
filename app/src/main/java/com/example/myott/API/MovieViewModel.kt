package com.example.myott.API

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.myott.BuildConfig
import kotlinx.coroutines.launch

// TMDB API로부터 영화 데이터를 조회하고 화면 상태를 관리하는 ViewModel
class MovieViewModel: ViewModel() {
    private val _movieState = mutableStateOf(MovieState()) // 내부에서만 상태를 수정하기 위한 _movieState
    val movieState: State<MovieState> = _movieState // 외부에는 읽기 전용 movieState

    init{
        fetchMovie()
    }

    // Retrofit을 이용해 특정 영화 상세 정보를 가져오는 함수
    private fun fetchMovie(){
        viewModelScope.launch{
            try{
                val response = tmdbService.getMovie( // 특정 영화를 가져와서 response에 저장
                    movieId = 76600, // 영화 id
                    apiKey = BuildConfig.TMDB_API_KEY // API 키는 local.properties의 TMDB_API_KEY에서 읽음 (깃허브에 안 올라감)
                )
                _movieState.value = _movieState.value.copy( // API 호출 성공 시
                    loading = false, // 로딩 종료
                    movie = response, // 받아온 영화 데이터 반영
                    error = null // 에러 초기화
                )
            } catch(e: Exception){
                _movieState.value = _movieState.value.copy(
                    loading = false,
                    error = "Error fetching users ${e.message}"
                )
            }
        }
    }

    // 영화 상세 화면의 UI 상태를 하나로 묶어 관리하는 데이터 클래스
    data class MovieState(
        val loading: Boolean = true, // 데이터 로딩 여부
        val movie: Movie? = null, // 영화 데이터 객체
        val error: String? = null // 에러 메시지 저장 객체
    )
}
