package com.example.myott

import com.example.genreapp.data.GenreDao
import com.example.genreapp.data.PreferredGenre
import kotlinx.coroutines.flow.Flow

// 앱에서 회원이 등록한 정보를 어떻게 가져오고 저장할지 관리(Dao에서 만들어놓은 함수들을 사용한다.)
class GenreRepository(private val genreDao: GenreDao) { // Repository는 UI와 Dao 사이에 있기 때문에 GenreDao를 외부에서 전달받음
    val selectedGenres: Flow<List<PreferredGenre>> = genreDao.getSelectedGenres()
    // genreDao의 getSelectedGenres 함수를 호출해 선택된 장르들의 목록을 지속적으로 관찰

    // 전체 장르 목록
    val allGenres: Flow<List<PreferredGenre>> =
        genreDao.getAllGenres()

    // 기본 장르 12개 저장
    suspend fun saveDefaultGenres() {

        val defaultGenres = listOf(
            PreferredGenre("SF", "SF", false),
            PreferredGenre("Noir", "느와르", false),
            PreferredGenre("ACTION", "액션", false),
            PreferredGenre("HORROR", "공포", false),
            PreferredGenre("COMEDY", "코미디", false),
            PreferredGenre("FANTASY", "판타지", false),
            PreferredGenre("ROMANCE", "로맨스", false),
            PreferredGenre("HISTORY", "역사", false),
            PreferredGenre("MUSICAL", "뮤지컬", false),
            PreferredGenre("MYSTERY", "미스터리", false),
            PreferredGenre("ANIMATION", "애니메이션", false),
            PreferredGenre("DOCUMENTARY", "다큐멘터리", false)
        )

        genreDao.insertDefaultGenres(defaultGenres)
    }

    // 사용자가 선택한 장르의 상태 변경
    suspend fun updateGenreSelection(
        genreId: String,
        selected: Boolean
    ) {
        genreDao.updateGenreSelection(genreId, selected)
    }
}