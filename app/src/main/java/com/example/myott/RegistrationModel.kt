package com.example.myott

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegistrationModel : ViewModel() {
    // Compose가 관찰하는 상태이므로 값이 바뀌면 해당 입력란이 갱신된다.
    var id by mutableStateOf("")
    var email by mutableStateOf("")
    var birthDigits by mutableStateOf("")
        private set
    var password by mutableStateOf("")
    var confirm by mutableStateOf("")

    var message by mutableStateOf("")
    var busy by mutableStateOf(false)
        private set

    // 붙여넣기를 포함해 생년월일 입력을 숫자 최대 8자리로 제한한다.
    fun editBirth(value: String) {
        birthDigits = value.filter { it in '0'..'9' }.take(8)
        message = ""
    }

    // 호출한 화면이 제공한 조회 함수에 공백을 제거한 아이디를 전달한다.
    fun checkId(isIdAvailable: suspend (String) -> Boolean) {
        if (busy) return
        val checkedId = id.trim()
        if (checkedId.isEmpty()) {
            message = "아이디를 입력해 주세요."
            return
        }
        busy = true
        message = ""
        viewModelScope.launch {
            try {
                message = if (isIdAvailable(checkedId)) "사용할 수 있는 아이디예요." else "이미 사용 중인 아이디예요."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message = "아이디 중복 확인에 실패했어요. 다시 시도해 주세요."
            } finally {
                busy = false
            }
        }
    }

    // 입력값만 검증해 결과를 표시한다. 이 경로에서는 DB에 저장하지 않는다.
    fun submit() {
        if (busy) return
        val request = RegistrationRequest(id, email, formatBirth(birthDigits), password, confirm, agreed = false)
        message = request.inputIssue() ?: "입력 확인을 마쳤어요. 회원정보는 저장되지 않았어요."
    }
}
