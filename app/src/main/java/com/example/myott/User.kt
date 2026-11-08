package com.example.genreapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 회원 정보 테이블 (6번 '회원가입 후 아이디, 비밀번호, 닉네임, 생년월일, 이메일 저장'에서 사용)
// 회원가입 담당 팀원도 이 User 클래스를 그대로 사용하면 된다.
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String,     // 아이디 (중복 불가 → 기본키)
    val password: String,   // 비밀번호
    val nickname: String,   // 닉네임
    val birthDate: String,  // 생년월일 (예: "2003-05-17")
    val email: String,      // 이메일
)
