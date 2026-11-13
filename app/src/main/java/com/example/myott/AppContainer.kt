package com.example.myott

import android.app.Application
import com.example.genreapp.data.AppDatabase

class AppContainer : Application() {

    // Room 데이터베이스 생성
    val database by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    // Repository 생성
    val genreRepository by lazy {
        GenreRepository(database.genreDao())
    }

    // ViewModel Factory 생성
    val genreViewModelFactory by lazy {
        GenreViewModelFactory(genreRepository)
    }
}
