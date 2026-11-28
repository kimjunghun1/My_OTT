package com.example.myott

import com.example.myott.API.PersonMovieCredit
import com.example.myott.API.PersonTvCredit
import com.example.myott.API.tmdbService
import com.example.myott.BuildConfig
import kotlinx.coroutines.flow.first

// 추천 화면에 표시할 통합 작품 정보
// 영화와 TV 프로그램을 하나의 데이터 형태로 관리
data class PersonRecommendedWork(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val releaseDate: String?,
    val overview: String?,
    val voteAverage: Double?,
    val genreIds: List<Int>?,
    val mediaType: String
)

// 인물별 추천 작품 목록
// 누구의 작품인지 구분하기 위해 인물 정보도 함께 저장
data class PersonRecommendation(
    val person: PersonRecord,
    val works: List<PersonRecommendedWork>
)

// 배우·감독 추천 작품 조회 Repository
class PersonRecommendationRepository(
    private val personRepository: PersonRepository
) {
    // 감독 추천 작품 가져오기
    suspend fun getDirectorRecommendations(): List<PersonRecommendation> {

        // Room에 저장된 감독 목록 가져오기
        val directors = personRepository.directors.first()

        // 감독별 추천 작품을 저장할 리스트
        val recommendationList = mutableListOf<PersonRecommendation>()

        // 등록된 감독마다 작품 조회
        for (director in directors) {

            // 해당 감독의 영화 및 TV 작품 조회
            val works = getPersonWorks(
                personId = director.personId,
                personType = "DIRECTOR"
            )

            // 감독과 작품 목록을 함께 저장
            recommendationList.add(
                PersonRecommendation(
                    person = director,
                    works = works
                )
            )
        }

        return recommendationList
    }

    // 배우 추천 작품 가져오기
    suspend fun getActorRecommendations(): List<PersonRecommendation> {

        // Room에 저장된 배우 목록 가져오기
        val actors = personRepository.actors.first()

        // 배우별 추천 작품을 저장할 리스트
        val recommendationList = mutableListOf<PersonRecommendation>()

        // 등록된 배우마다 작품 조회
        for (actor in actors) {

            // 해당 배우의 영화 및 TV 작품 조회
            val works = getPersonWorks(
                personId = actor.personId,
                personType = "ACTOR"
            )

            // 배우와 작품 목록을 함께 저장
            recommendationList.add(
                PersonRecommendation(
                    person = actor,
                    works = works
                )
            )
        }

        return recommendationList
    }

    // 특정 인물의 영화 + TV 작품 조회
    private suspend fun getPersonWorks(
        personId: Int,
        personType: String
    ): List<PersonRecommendedWork> {

        // TMDB API 키 확인
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return emptyList()
        }

        // 영화 작품 목록
        val movieWorks: List<PersonRecommendedWork>

        // TV 작품 목록
        val tvWorks: List<PersonRecommendedWork>

        try {

            // TMDB에서 영화 크레딧 조회
            val movieCredits = tmdbService.getPersonMovieCredits(
                personId = personId,
                apiKey = BuildConfig.TMDB_API_KEY
            )

            // TMDB에서 TV 크레딧 조회
            val tvCredits = tmdbService.getPersonTvCredits(
                personId = personId,
                apiKey = BuildConfig.TMDB_API_KEY
            )

            // --------------------------------------
            // 인물 유형에 따라 작품 선택
            // --------------------------------------

            if (personType == "DIRECTOR") {

                // 감독: crew에서 Director 작품만 선택
                movieWorks = movieCredits.crew
                    .orEmpty()
                    .filter { it.job == "Director" }
                    .map { convertMovie(it) }

                tvWorks = tvCredits.crew
                    .orEmpty()
                    .filter { it.job == "Director" }
                    .map { convertTv(it) }

            } else {

                // 배우: cast에 있는 출연 작품 선택
                movieWorks = movieCredits.cast
                    .orEmpty()
                    .map { convertMovie(it) }

                tvWorks = tvCredits.cast
                    .orEmpty()
                    .map { convertTv(it) }
            }

        } catch (e: Exception) {

            // API 조회 실패 시 해당 인물의 작품은 빈 목록 반환
            return emptyList()
        }

        // 영화와 TV 작품 합치기
        return (movieWorks + tvWorks)

            // 제목이 없는 작품 제외
            .filter { it.title.isNotBlank() }

            // 같은 콘텐츠 중복 제거
            .distinctBy { "${it.mediaType}_${it.id}" }

            // 최신 작품 순 정렬
            .sortedByDescending { it.releaseDate.orEmpty() }
    }

    // 영화 DTO → 통합 작품 정보로 변환
    private fun convertMovie(
        movie: PersonMovieCredit
    ): PersonRecommendedWork {

        return PersonRecommendedWork(
            id = movie.id,
            title = movie.title.orEmpty(),
            posterPath = movie.poster_path,
            releaseDate = movie.release_date,
            overview = movie.overview,
            voteAverage = movie.vote_average,
            genreIds = movie.genre_ids,
            mediaType = "movie"
        )
    }

    // TV DTO → 통합 작품 정보로 변환
    private fun convertTv(
        tv: PersonTvCredit
    ): PersonRecommendedWork {

        return PersonRecommendedWork(
            id = tv.id,
            title = tv.name.orEmpty(),
            posterPath = tv.poster_path,
            releaseDate = tv.first_air_date,
            overview = tv.overview,
            voteAverage = tv.vote_average,
            genreIds = tv.genre_ids,
            mediaType = "tv"
        )
    }
}