package com.example.myott.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myott.main.MainScreen

// 로그인 상태에 따라 화면 선택
// - 로그아웃 상태(currentUser == null) → 로그인 화면
// - 로그인 상태 → 메인 화면
// (팀에서 navigation-compose를 도입하면 이 부분을 NavHost로 바꾸면 된다.)
@Composable
fun AuthRoute(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onSignUpClick: () -> Unit = {},       // TODO: 회원가입 화면 연결
    onFindIdClick: () -> Unit = {},       // TODO: 아이디 찾기 화면 연결
    onFindPasswordClick: () -> Unit = {}, // TODO: 비밀번호 찾기 화면 연결
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val user = currentUser
    if (user == null) {
        LoginScreen(
            uiState = uiState,
            onUserIdChange = authViewModel::onUserIdChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onLoginClick = authViewModel::login,
            onSignUpClick = onSignUpClick,
            onFindIdClick = onFindIdClick,
            onFindPasswordClick = onFindPasswordClick,
            modifier = modifier
        )
    } else {
        // 로그인 후 → 메인 화면 (기존 회원 정보 + 로그아웃 화면은 마이페이지 탭으로 이동)
        MainScreen(
            myPageContent = {
                LoggedInScreen(
                    user = user,
                    onLogoutClick = authViewModel::logout
                )
            },
            modifier = modifier
        )
    }
}
