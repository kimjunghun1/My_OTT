
package com.example.myott

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// 감독과 배우를 저장하는 테이블
@Entity(
    tableName = "person_records",
    primaryKeys = ["personId", "personType"]
)
data class PersonRecord(
    val personId: Int,           // TMDB 인물 ID
    val personName: String,      // 인물 이름
    val personType: String,     // DIRECTOR 또는 ACTOR
    val registeredAt: Long = System.currentTimeMillis() // 등록 시간
)

// 감독과 배우 데이터 저장 및 조회
@Dao
interface PersonDao {

    // 등록된 감독 목록 (오래된 순)
    @Query("""
        SELECT * FROM person_records
        WHERE personType = 'DIRECTOR'
        ORDER BY registeredAt ASC
    """)
    fun observeDirectors(): Flow<List<PersonRecord>>

    // 등록된 배우 목록 (오래된 순)
    @Query("""
        SELECT * FROM person_records
        WHERE personType = 'ACTOR'
        ORDER BY registeredAt ASC
    """)
    fun observeActors(): Flow<List<PersonRecord>>

    // 특정 인물이 이미 등록되어 있는지 확인
    @Query("""
        SELECT * FROM person_records
        WHERE personId = :personId AND personType = :personType
        LIMIT 1
    """)
    suspend fun getPerson(
        personId: Int,
        personType: String
    ): PersonRecord?

    // 유형별 등록 인원 확인
    @Query("""
        SELECT COUNT(*) FROM person_records
        WHERE personType = :personType
    """)
    suspend fun getPersonCount(personType: String): Int

    // 인물 저장 (이미 존재하면 무시)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPerson(person: PersonRecord)

    // 가장 오래된 인물 1명 삭제
    @Query("""
        DELETE FROM person_records
        WHERE personId = (
            SELECT personId FROM person_records
            WHERE personType = :personType
            ORDER BY registeredAt ASC
            LIMIT 1
        )
        AND personType = :personType
    """)
    suspend fun deleteOldestPerson(personType: String)

    // 특정 인물 삭제
    @Query("""
        DELETE FROM person_records
        WHERE personId = :personId AND personType = :personType
    """)
    suspend fun deletePerson(
        personId: Int,
        personType: String
    )
}
