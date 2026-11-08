package com.example.myott

import com.example.genreapp.data.GenreDao
import com.example.genreapp.data.PreferredGenre
import kotlinx.coroutines.flow.Flow

// 앱에서 회원이 등록한 정보를 어떻게 가져오고 저장할지 관리(Dao에서 만들어놓은 함수들을 사용한다.)
class GenreRepository(private val genreDao: GenreDao) { // Repository는 UI와 Dao 사이에 있기 때문에 GenreDao를 외부에서 전달받음
    val selectedGenres: Flow<List<PreferredGenre>> = genreDao.getSelectedGenres()
    // genreDao의 getSelectedGenres 함수를 호출해 선택된 장르들의 목록을 지속적으로 관찰

    suspend fun saveDefaultGenres(
        genres: List<PreferredGenre>
    ) { // genreDao의 insertAll 함수를 호출해 선호 장르 저장
        genreDao.insertAll(genres)
    }
}