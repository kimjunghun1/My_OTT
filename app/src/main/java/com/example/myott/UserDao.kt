package com.example.genreapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    // 아이디로 회원 한 명 조회 (로그인, 아이디 중복 확인에 사용) — 없으면 null
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun findByUserId(userId: String): User?

    // 회원 저장 (회원가입 담당 팀원이 사용) — 같은 아이디가 있으면 저장하지 않고 예외 발생
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)
}
