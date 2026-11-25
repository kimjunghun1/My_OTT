package com.example.myott

import com.example.genreapp.data.GenreDao
import com.example.genreapp.data.PreferredGenre
import com.example.myott.API.tmdbService
import kotlinx.coroutines.flow.Flow

class GenreRepository(private val genreDao: GenreDao) {
    val selectedGenres: Flow<List<PreferredGenre>> = genreDao.getSelectedGenres()
    val allGenres: Flow<List<PreferredGenre>> = genreDao.getAllGenres()

    // TMDB에서 한국어 번역이 누락되어 영어로 내려오는 장르 ID 보정 맵
    private val koreanGenreNameMap = mapOf(
        10759 to "액션·모험",   // Action & Adventure
        10762 to "어린이",      // Kids
        10763 to "뉴스",        // News
        10764 to "리얼리티",    // Reality
        10765 to "SF·판타지",   // Sci-Fi & Fantasy
        10766 to "소프 오페라", // Soap
        10767 to "토크쇼",      // Talk
        10768 to "전쟁·정치"    // War & Politics
    )

    // TMDB API에서 영화와 TV 장르 목록을 불러와 한국어로 보정 후 DB에 저장
    suspend fun fetchAndSaveTmdbGenres() {
        val apiKey = BuildConfig.TMDB_API_KEY
        val fetchedGenres = try {
            val movieGenres = tmdbService.getGenreList("movie", apiKey).genres
            val tvGenres = tmdbService.getGenreList("tv", apiKey).genres

            (movieGenres + tvGenres)
                .distinctBy { it.id }
                .filter { !it.name.isNullOrBlank() }
                .map { tmdbGenre ->
                    // 보정 맵에 있으면 한국어 이름을 사용하고, 없으면 서버에서 받은 이름 사용
                    val localizedName = koreanGenreNameMap[tmdbGenre.id] ?: tmdbGenre.name!!
                    PreferredGenre(
                        genreId = tmdbGenre.id,
                        genreName = localizedName,
                        isSelected = false
                    )
                }
        } catch (e: Exception) {
            listOf(
                PreferredGenre(28, "액션", false),
                PreferredGenre(16, "애니메이션", false),
                PreferredGenre(35, "코미디", false),
                PreferredGenre(80, "범죄", false),
                PreferredGenre(99, "다큐멘터리", false),
                PreferredGenre(18, "드라마", false),
                PreferredGenre(14, "판타지", false),
                PreferredGenre(27, "공포", false),
                PreferredGenre(9648, "미스터리", false),
                PreferredGenre(10749, "로맨스", false),
                PreferredGenre(878, "SF", false),
                PreferredGenre(53, "스릴러", false)
            )
        }

        genreDao.insertDefaultGenres(fetchedGenres)
    }

    suspend fun updateGenreSelection(
        genreId: Int,
        selected: Boolean
    ) {
        genreDao.updateGenreSelection(genreId, selected)
    }
}