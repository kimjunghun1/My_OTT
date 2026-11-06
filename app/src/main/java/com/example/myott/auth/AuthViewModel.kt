package com.example.myott.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.genreapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 로그인 화면에 보여줄 상태 (처음엔 모든 입력칸이 비어 있음)
data class LoginUiState(
    val userId: String = "",
    val password: String = "",
    val errorMessage: String? = null, // null이 아니면 화면에 오류 메시지 표시
    val isLoading: Boolean = false,
)

// 로그인 / 로그아웃 상태를 관리하는 ViewModel
// 값은 사용자가 입력칸에 직접 입력하고 버튼을 눌렀을 때만 바뀐다.
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 현재 로그인한 회원 (null이면 로그아웃 상태)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // 아이디 입력칸에 글자를 입력할 때마다 호출
    fun onUserIdChange(value: String) {
        _uiState.update { it.copy(userId = value, errorMessage = null) }
    }

    // 비밀번호 입력칸에 글자를 입력할 때마다 호출
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    // 3번: '로그인' 버튼을 눌렀을 때 호출
    fun login() {
        val state = _uiState.value
        val userId = state.userId.trim()

        // 4번: 입력이 비어 있으면 오류 메시지
        when {
            userId.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "아이디를 입력해 주세요.") }
                return
            }
            state.password.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "비밀번호를 입력해 주세요.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.login(userId, state.password)) {
                is LoginResult.Success -> {
                    _currentUser.value = result.user
                    _uiState.value = LoginUiState() // 입력칸 비우기
                }
                is LoginResult.Failure -> {
                    // 4번: 로그인 실패 시 오류 메시지 표시 (비밀번호 칸은 비움)
                    _uiState.update {
                        it.copy(isLoading = false, password = "", errorMessage = result.message)
                    }
                }
            }
        }
    }

    // 5번: '로그아웃' 버튼을 눌렀을 때 호출
    // 앱이 들고 있던 로그인 회원 정보(아이디, 비밀번호, 닉네임, 생년월일, 이메일)를 지운다.
    // (DB에 저장된 회원 정보는 지우지 않으므로 다시 로그인할 수 있다.)
    fun logout() {
        _currentUser.value = null
        _uiState.value = LoginUiState()
    }

    companion object {
        // AuthViewModel은 생성자로 Repository를 받기 때문에 Factory가 필요하다.
        fun factory(repository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(repository) }
        }
    }
}
