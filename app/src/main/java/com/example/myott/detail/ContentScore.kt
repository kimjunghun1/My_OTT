package com.example.myott.detail

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------
// 추천 알고리즘 2단계에서 쓰는 점수 저장용 테이블
// ---------------------------------------------------------------

// 장르별 점수 (TMDB 장르 id 기준)
// 관심 있음 / OTT 보러가기 / 검색 결과 선택을 할 때마다 그 콘텐츠의 장르들에 점수가 더해진다.
@Entity(tableName = "genre_scores")
data class GenreScore(
    @PrimaryKey
    val genreId: Int,      // TMDB 장르 id (예: 28 = 액션)
    val genreName: String, // 장르 이름 (예: "액션")
    val score: Int,        // 현재 점수 (사용자 행동에 따라 계속 바뀜)
)

// 콘텐츠별 사용자 행동 기록
// - isInterested: 관심 있음 → 관심 목록, 장르 점수
// - isNotInterested: 관심 없음 → 추천에서 제외할 목록
// - ottClickCount: OTT 보러가기 누른 횟수 → 4단계(후순위), 마이페이지 그래프
@Entity(tableName = "content_records")
data class ContentRecord(
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val genreIds: String,           // 장르 id들을 쉼표로 저장 (예: "28,878")
    val genreNames: String,         // 장르 이름들을 쉼표로 저장 (예: "액션,SF")
    val isInterested: Boolean = false,
    val isNotInterested: Boolean = false,
    val ottClickCount: Int = 0,
    val searchSelectCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface ContentScoreDao {

    // ----- 콘텐츠 기록 -----

    // 콘텐츠 하나의 기록을 실시간으로 관찰 (상세 화면 버튼 상태 표시용)
    @Query("SELECT * FROM content_records WHERE movieId = :movieId")
    fun observeRecord(movieId: Int): Flow<ContentRecord?>

    @Query("SELECT * FROM content_records WHERE movieId = :movieId")
    suspend fun getRecord(movieId: Int): ContentRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(record: ContentRecord)

    // 관심 있음 목록 (관심 탭에서 사용 가능)
    @Query("SELECT * FROM content_records WHERE isInterested = 1 ORDER BY updatedAt DESC")
    fun observeInterested(): Flow<List<ContentRecord>>

    // 관심 없음 목록 → 추천에서 제외
    @Query("SELECT movieId FROM content_records WHERE isNotInterested = 1")
    fun observeNotInterestedIds(): Flow<List<Int>>

    // OTT 보러가기를 누른 콘텐츠 → 추천 후순위
    @Query("SELECT movieId FROM content_records WHERE ottClickCount > 0")
    fun observeOttClickedIds(): Flow<List<Int>>

    // OTT 보러가기 기록 전체 (마이페이지 장르 비율 그래프용)
    @Query("SELECT * FROM content_records WHERE ottClickCount > 0")
    fun observeOttClickedRecords(): Flow<List<ContentRecord>>

    // ----- 장르 점수 -----

    @Query("SELECT * FROM genre_scores WHERE genreId = :genreId")
    suspend fun getGenreScore(genreId: Int): GenreScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGenreScore(genreScore: GenreScore)

    // 점수 높은 순 장르 목록 (추천 담당: 상위 3개 장르만 사용)
    @Query("SELECT * FROM genre_scores ORDER BY score DESC")
    fun observeGenreScores(): Flow<List<GenreScore>>
}
