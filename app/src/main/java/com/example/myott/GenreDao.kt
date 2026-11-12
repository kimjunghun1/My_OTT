package com.example.genreapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    // 수정 : 선택된 장르만 불러오기
    @Query("SELECT * FROM preferred_genres WHERE isSelected = 1")
    fun getSelectedGenres(): Flow<List<PreferredGenre>>

    // 전체 장르 목록 불러오기
    @Query("SELECT * FROM preferred_genres ORDER BY genreName ASC")
    fun getAllGenres(): Flow<List<PreferredGenre>>

    // 여러 장르 한 번에 저장
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultGenres( genres: List<PreferredGenre> )

    // 특정 장르의 선택 상태 변경
    @Query(" UPDATE preferred_genres SET isSelected = :selected WHERE genreId = :genreId")
    suspend fun updateGenreSelection(
        genreId: String,
        selected: Boolean
    )

}