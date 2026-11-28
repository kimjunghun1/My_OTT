package com.example.myott

import android.app.Application
import com.example.genreapp.data.AppDatabase
import com.example.myott.detail.ContentScoreRepository

// 앱 전체에서 공통으로 사용하는 핵심 객체들을 한 곳에 모아서 생성하고 관리함
class AppContainer : Application() { // Application 클래스: 가장 먼저 생성되어 앱이 완전히 종료될 때까지 살아있는 최상위 객체

    // Room 데이터베이스 생성
    val database by lazy { // by lazy: 변수가 처음 호출되어 사용되는 순간에 딱 한 번만 생성하게 함
        AppDatabase.getDatabase(applicationContext)
    }

    // Repository 생성
    val genreRepository by lazy {
        GenreRepository(database.genreDao())
    }

    // 콘텐츠 점수/기록 Repository (상세 화면, 추천, 검색에서 사용)
    val contentScoreRepository by lazy {
        ContentScoreRepository(database.contentScoreDao())
    }

    // ViewModel Factory 생성
    val genreViewModelFactory by lazy {
        GenreViewModelFactory(genreRepository)
    }

    val recommendationRepository by lazy {
        RecommendationRepository(genreRepository, contentScoreRepository) // RecommendationRepository 부름
    }
}
