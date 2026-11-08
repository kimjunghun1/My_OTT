package com.example.myott

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 화면과 저장소가 검증에 사용하는 회원정보 입력값이다. */
data class RegistrationRequest(
    val id: String, val email: String, val birth: String,
    val password: String, val confirmation: String, val agreed: Boolean
) {
    // 저장소에서 사용하며, 입력값이 유효하면 저장 동의 여부까지 확인한다.
    fun issue(): String? = inputIssue() ?: if (agreed) null else "계정 정보 저장에 동의해 주세요."

    // 화면에 표시할 첫 번째 오류를 반환한다. 모든 항목이 유효하면 null을 반환한다.
    fun inputIssue(): String? {
        if (id.isBlank()) return "아이디를 입력해 주세요."
        if (!email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) return "이메일 형식을 확인해 주세요."
        // 2월 30일 같은 날짜가 다음 달 날짜로 자동 보정되는 것을 막는다.
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val date = runCatching { parser.parse(birth) }.getOrNull()
        if (!birth.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) || date == null || date.after(Date()))
            return "생년월일을 YYYY-MM-DD 형식의 실제 날짜로 입력해 주세요."
        if (password.length < 8 || password.none(Char::isLetter) || password.none(Char::isDigit))
            return "비밀번호는 문자와 숫자를 포함해 8자 이상이어야 해요."
        if (password != confirmation) return "비밀번호 확인이 일치하지 않아요."

        return null
    }
}

// 월과 일의 첫 숫자가 입력되면 그 앞에 하이픈을 붙인다.
internal fun formatBirth(digits: String): String = buildString {
    digits.forEachIndexed { index, digit ->
        if (index == 4 || index == 6) append('-')
        append(digit)
    }
}
