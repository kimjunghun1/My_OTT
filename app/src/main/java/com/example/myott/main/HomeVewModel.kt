package com.example.myott.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myott.API.Movie
import com.example.myott.RecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 홈 화면에 표시할 추천 영화 목록을 가져오고 상태를 관리하는 클래스
class HomeViewModel(
    private val recommendationRepository: RecommendationRepository // 추천 영화 목록 데이터를 실제로 가져오는 RecommendationRepository를 외부에서 전달받음
) : ViewModel() { // ViewModel 상속->화면 회전이나 설정 변경 시에도 UI 데이터를 유지

    private val _recommendedList = MutableStateFlow<List<Movie>>(emptyList()) // 쓰기 전용 리스트
    val recommendedList: StateFlow<List<Movie>> = _recommendedList.asStateFlow() // 읽기 전용 리스트

    init { // HomeViewModel이 처음 생성될 때 init 호출
        refreshRecommendations()
    }

    fun refreshRecommendations() { // 새로고침
        viewModelScope.launch { // 작업을 메인 스레드가 아닌 백그라운드에서 비동기로 처리
            _recommendedList.value = recommendationRepository.getRecommendedContents() // 반환한 영화 목록을 _recommendedList.value에 대입
        }
    }

    companion object { // HomeViewModel 객체가 만들어지기 전에도, 클래스 이름만으로 바로 HomeViewModel.factory(repo)처럼 함수를 호출할 수 있게 해줌
        fun factory(repo: RecommendationRepository): ViewModelProvider.Factory = viewModelFactory { // 외부에서 repo를 받아서 ViewModelProvider.Factory를 반환하는 함수
            initializer { HomeViewModel(repo) } // 실제 ViewModel이 생성되는 시점에 실행됨
        }
    }
}