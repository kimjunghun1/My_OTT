package com.example.myott

import com.example.myott.API.Movie
import com.example.myott.API.tmdbService
import com.example.myott.detail.ContentScoreRepository
import kotlinx.coroutines.flow.first

class RecommendationRepository(
    private val genreRepository: GenreRepository,
    private val scoreRepository: ContentScoreRepository,
) {
    suspend fun getRecommendedContents(): List<Movie> {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) return emptyList()

        // * 관심 없음을 누른 콘텐츠 ID 목록 (추천 제외 대상)
        val excludedIds = scoreRepository.notInterestedIds.first().toSet()

        // 온보딩에서 선택한 선호 장르 ID 목록
        val preferredIds = genreRepository.selectedGenres.first().map { it.genreId }

        // DB에 저장된 전체 TMDB 장르 ID 목록 (3단계 탐색용)
        val allTmdbGenreIds = genreRepository.allGenres.first().map { it.genreId }

        // 2단계에서 누적된 장르 점수 목록 (점수 내림차순)
        val genreScores = scoreRepository.genreScores.first().filter { it.score > 0 }

        // =====================================================================
        // [1단계] 행동 점수가 아직 없는 경우 -> 선호 장르 7개 + 미선택 장르 3개 (총 10개)
        // =====================================================================
        if (genreScores.isEmpty()) {
            val mainCandidates = if (preferredIds.isNotEmpty()) {
                preferredIds.flatMap { genreId ->
                    fetchMoviesAndTv(apiKey, genreId.toString(), page = (1..2).random())
                }
            } else {
                fetchTrending(apiKey)
            }

            val first7 = mainCandidates
                .distinctBy { it.mediaType to it.id }
                .filterNot { it.id in excludedIds }
                .shuffled()
                .take(7)

            val pickedKeys = first7.map { it.mediaType to it.id }.toSet()

            // 선택하지 않은(점수가 0점인) 나머지 장르 중 2개를 뽑아 3편 추천
            val unselectedGenreIds = allTmdbGenreIds
                .filterNot { it in preferredIds }
                .shuffled()
                .take(2)

            val last3 = unselectedGenreIds.flatMap { genreId ->
                fetchMoviesAndTv(apiKey, genreId.toString(), page = 1)
            }
                .distinctBy { it.mediaType to it.id }
                .filterNot { it.id in excludedIds }
                .filterNot { (it.mediaType to it.id) in pickedKeys }
                .shuffled()
                .take(3)

            return first7 + last3
        }

        // =====================================================================
        // [2단계] 상위 3개 장르 점수 합산 기준 추천 -> 앞의 7개
        // =====================================================================
        val top3Genres = genreScores.take(3)
        val top3ScoreMap = top3Genres.associate { it.genreId to it.score }

        val queryGenreIds = (top3ScoreMap.keys + preferredIds).distinct()
        val mainCandidates = queryGenreIds.flatMap { genreId ->
            fetchMoviesAndTv(apiKey, genreId.toString(), page = 1)
        }
            .distinctBy { it.mediaType to it.id }
            .filterNot { it.id in excludedIds }

        val rankedMainList = mainCandidates
            .map { movie ->
                val totalScore = movie.genreIds.orEmpty()
                    .filter { it in top3ScoreMap }
                    .sumOf { top3ScoreMap[it] ?: 0 }
                movie to totalScore
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(7) // 👈 앞의 7개

        val pickedKeys = rankedMainList.map { it.mediaType to it.id }.toSet()

        // =====================================================================
        // [3단계] 점수가 낮거나 0점인 장르의 콘텐츠 -> 뒤의 3개
        // =====================================================================
        val scoreMapAll = genreScores.associate { it.genreId to it.score }
        val lowScoreGenreIds = allTmdbGenreIds
            .sortedBy { scoreMapAll[it] ?: 0 }
            .take(3)
            .shuffled()
            .take(2)

        val explorationList = lowScoreGenreIds.flatMap { genreId ->
            fetchMoviesAndTv(apiKey, genreId.toString(), page = 1)
        }
            .distinctBy { it.mediaType to it.id }
            .filterNot { it.id in excludedIds }
            .filterNot { (it.mediaType to it.id) in pickedKeys }
            .shuffled()
            .take(3) // 👈 뒤의 3개

        // 총 10개 반환 (사용자 정보 기반 7개 + 저점수 장르 탐색 3개)
        return rankedMainList + explorationList
    }

    private suspend fun fetchMoviesAndTv(apiKey: String, genreParam: String, page: Int): List<Movie> {
        return try {
            val movies = tmdbService.discoverContents("movie", apiKey, genreParam, page)
                .results.map { it.copy(mediaType = "movie") }
            val tv = tmdbService.discoverContents("tv", apiKey, genreParam, page)
                .results.map { it.copy(mediaType = "tv") }
            movies + tv
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchTrending(apiKey: String): List<Movie> {
        return try {
            val movies = tmdbService.getWeeklyTrending("movie", apiKey)
                .results.map { it.copy(mediaType = "movie") }
            val tv = tmdbService.getWeeklyTrending("tv", apiKey)
                .results.map { it.copy(mediaType = "tv") }
            movies + tv
        } catch (e: Exception) {
            emptyList()
        }
    }
}