
package com.example.myott.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myott.PersonRecommendationRepository
import com.example.myott.PersonRecommendedWork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 배우 및 감독 추천 작품을 관리하는 ViewModel
class PersonRecommendationViewModel(
    private val repository: PersonRecommendationRepository
) : ViewModel() {

    // 감독 추천 작품
    // 감독 추천 작품을 저장하는 변수
    private val _directorWorks =
        MutableStateFlow<List<PersonRecommendedWork>>(emptyList())

    // 화면에서 읽을 수 있는 감독 추천 작품 목록
    val directorWorks: StateFlow<List<PersonRecommendedWork>> =
        _directorWorks.asStateFlow()

    // 배우 추천 작품
    // 배우 추천 작품을 저장하는 변수
    private val _actorWorks =
        MutableStateFlow<List<PersonRecommendedWork>>(emptyList())

    // 화면에서 읽을 수 있는 배우 추천 작품 목록
    val actorWorks: StateFlow<List<PersonRecommendedWork>> =
        _actorWorks.asStateFlow()

    // 로딩 상태
    // 추천 작품을 불러오는 중인지 확인
    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ViewModel 생성 시 추천 작품 자동 조회
    init {
        loadRecommendations()
    }

    // 추천 작품 불러오기
    fun loadRecommendations() {

        // 이미 데이터를 불러오는 중이면 중복 실행 방지
        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true

            try {

                // 감독 추천 작품 조회
                val directorRecommendations =
                    repository.getDirectorRecommendations()

                // 감독별 작품을 하나의 리스트로 통합
                val allDirectorWorks =
                    directorRecommendations.flatMap { it.works }

                // 중복 제거, 최신순 정렬, 최대 10개 저장
                _directorWorks.value =
                    allDirectorWorks
                        .distinctBy { "${it.mediaType}_${it.id}" }
                        .sortedByDescending { it.releaseDate.orEmpty() }
                        .take(10)


                // 배우 추천 작품 조회
                val actorRecommendations =
                    repository.getActorRecommendations()

                // 배우별 작품을 하나의 리스트로 통합
                val allActorWorks =
                    actorRecommendations.flatMap { it.works }

                // 중복 제거, 최신순 정렬, 최대 15개 저장
                _actorWorks.value =
                    allActorWorks
                        .distinctBy { "${it.mediaType}_${it.id}" }
                        .sortedByDescending { it.releaseDate.orEmpty() }
                        .take(15)

            } finally {

                // 작업이 끝나면 로딩 상태 해제
                _isLoading.value = false
            }
        }
    }

    // ViewModel Factory
    companion object {

        // Repository를 전달받아 ViewModel을 생성하는 Factory
        fun factory(
            repository: PersonRecommendationRepository
        ): ViewModelProvider.Factory = viewModelFactory {

            initializer {
                PersonRecommendationViewModel(repository)
            }
        }
    }
}
