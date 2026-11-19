package com.example.myott

import android.app.Application
import com.example.genreapp.data.AppDatabase
import com.example.myott.detail.ContentScoreRepository

class AppContainer : Application() {

    // Room 데이터베이스 생성
    val database by lazy {
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
}
