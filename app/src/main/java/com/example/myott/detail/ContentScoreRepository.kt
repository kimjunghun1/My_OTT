package com.example.myott.detail

import com.example.myott.API.TmdbGenre
import kotlinx.coroutines.flow.Flow

// 사용자 행동별 장르 점수 (알고리즘: 관심 있음 > OTT 보러가기 > 검색 결과 선택)
// 숫자만 바꾸면 가중치 조정 가능
object ScoreWeights {
    const val INTEREST = 3      // 관심 있음
    const val OTT_CLICK = 2     // OTT 보러가기
    const val SEARCH_SELECT = 1 // 검색 결과에서 콘텐츠 선택
}

// 점수 계산에 필요한 콘텐츠 기본 정보
data class ContentInfo(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val genres: List<TmdbGenre>,
)

// 상세 화면(및 검색 화면)의 사용자 행동을 받아 기록하고 장르 점수를 바꾸는 곳
class ContentScoreRepository(private val dao: ContentScoreDao) {

    fun observeRecord(movieId: Int): Flow<ContentRecord?> = dao.observeRecord(movieId)

    // 추천 담당 팀원이 사용할 데이터
    val genreScores: Flow<List<GenreScore>> = dao.observeGenreScores()          // 2단계: 장르 점수
    val notInterestedIds: Flow<List<Int>> = dao.observeNotInterestedIds()       // 관심 없음 → 제외
    val ottClickedIds: Flow<List<Int>> = dao.observeOttClickedIds()             // 4단계: 후순위
    val interestedContents: Flow<List<ContentRecord>> = dao.observeInterested() // 관심 목록
    val ottClickedRecords: Flow<List<ContentRecord>> = dao.observeOttClickedRecords() // 마이페이지 그래프

    // 관심 있음 켜기 / 끄기
    suspend fun setInterested(info: ContentInfo, interested: Boolean) {
        val record = getOrCreate(info)
        if (record.isInterested == interested) return

        // 켜면 장르 점수 +, 끄면 다시 -
        addGenreScore(info.genres, if (interested) ScoreWeights.INTEREST else -ScoreWeights.INTEREST)
        dao.saveRecord(
            record.copy(
                isInterested = interested,
                // 관심 있음을 누르면 관심 없음은 해제
                isNotInterested = if (interested) false else record.isNotInterested,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // 관심 없음 켜기 / 끄기 — 장르 점수는 건드리지 않고, 이 콘텐츠만 추천에서 빠지게 한다.
    suspend fun setNotInterested(info: ContentInfo, notInterested: Boolean) {
        val record = getOrCreate(info)
        if (record.isNotInterested == notInterested) return

        // 관심 있음 상태였다면 해제하면서 점수도 되돌림
        if (notInterested && record.isInterested) {
            addGenreScore(info.genres, -ScoreWeights.INTEREST)
        }
        dao.saveRecord(
            record.copy(
                isNotInterested = notInterested,
                isInterested = if (notInterested) false else record.isInterested,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // OTT 보러가기를 누를 때마다 기록 + 장르 점수
    suspend fun recordOttClick(info: ContentInfo) {
        val record = getOrCreate(info)
        addGenreScore(info.genres, ScoreWeights.OTT_CLICK)
        dao.saveRecord(
            record.copy(
                ottClickCount = record.ottClickCount + 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // 검색 결과에서 콘텐츠를 선택했을 때 (검색 화면 담당 팀원이 호출)
    suspend fun recordSearchSelect(info: ContentInfo) {
        val record = getOrCreate(info)
        addGenreScore(info.genres, ScoreWeights.SEARCH_SELECT)
        dao.saveRecord(
            record.copy(
                searchSelectCount = record.searchSelectCount + 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // 기록이 없으면 새로 만든다 (저장은 호출한 쪽에서)
    private suspend fun getOrCreate(info: ContentInfo): ContentRecord {
        return dao.getRecord(info.movieId) ?: ContentRecord(
            movieId = info.movieId,
            title = info.title,
            posterPath = info.posterPath,
            genreIds = info.genres.joinToString(",") { it.id.toString() },
            genreNames = info.genres.joinToString(",") { it.name.orEmpty() },
        )
    }

    // 콘텐츠의 모든 장르에 delta만큼 점수를 더한다 (0 아래로는 내려가지 않음)
    private suspend fun addGenreScore(genres: List<TmdbGenre>, delta: Int) {
        genres.forEach { genre ->
            val current = dao.getGenreScore(genre.id)
            val newScore = ((current?.score ?: 0) + delta).coerceAtLeast(0)
            dao.saveGenreScore(
                GenreScore(
                    genreId = genre.id,
                    genreName = genre.name ?: current?.genreName.orEmpty(),
                    score = newScore
                )
            )
        }
    }
}
