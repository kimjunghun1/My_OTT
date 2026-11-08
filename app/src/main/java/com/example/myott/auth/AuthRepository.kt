package com.example.myott.auth

import com.example.genreapp.data.User
import com.example.genreapp.data.UserDao

// 로그인 결과: 성공이면 회원 정보, 실패면 화면에 띄울 오류 메시지
sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    data class Failure(val message: String) : LoginResult
}

// UI(ViewModel)와 Dao 사이에서 회원 정보를 가져오고 확인하는 역할
class AuthRepository(private val userDao: UserDao) {

    // 3번 로그인 / 4번 로그인 실패 시 오류 메시지
    suspend fun login(userId: String, password: String): LoginResult {
        val user = userDao.findByUserId(userId)
            ?: return LoginResult.Failure("존재하지 않는 아이디입니다.")

        return if (user.password == password) {
            LoginResult.Success(user)
        } else {
            LoginResult.Failure("비밀번호가 일치하지 않습니다.")
        }
    }
}
