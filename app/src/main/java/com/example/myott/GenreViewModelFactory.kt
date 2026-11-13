package com.example.myott

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenreViewModelFactory(
    private val repository: GenreRepository
) : ViewModelProvider.Factory {

    // ViewModel을 생성하는 함수
    // modelClass에는 생성하려는 ViewModel의 클래스 정보가 전달됨
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        // 생성하려는 ViewModel이 GenreViewModel인지 확인
        if (modelClass.isAssignableFrom(GenreViewModel::class.java)) {
            // GenreViewModel을 생성하고 Repository를 전달
            // 실제 반환 타입 T와 GenreViewModel의 타입이 다를 수 있어 형변환 필요
            @Suppress("UNCHECKED_CAST")
            return GenreViewModel(repository) as T
        }
        // 지원하지 않는 ViewModel을 생성하려고 하면 오류 발생
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}