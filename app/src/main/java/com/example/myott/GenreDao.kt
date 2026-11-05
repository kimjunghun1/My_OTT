package com.example.genreapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    // 선호 장르 목록 조회 (Flow를 통해 실시간 데이터 변경 감지)
    @Query("SELECT * FROM preferred_genres WHERE isSelected = 1 ORDER BY genreName ASC")
    fun getSelectedGenres(): Flow<List<PreferredGenre>>

    // 여러 장르 한 번에 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(genres: List<PreferredGenre>)

}