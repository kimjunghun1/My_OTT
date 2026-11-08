package com.example.myott.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myott.RegistrationModel

/** 모델의 입력값과 검증 결과를 표시하고 돌아가기 동작은 호출한 화면에 맡긴다. */
@Composable
fun RegistrationScreen(
    model: RegistrationModel,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isIdAvailable: (suspend (String) -> Boolean)? = null
) {
    BackHandler { if (!model.busy) onCancel() }
    Surface(modifier.fillMaxSize()) {
        Column(
            Modifier.imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("MyOTT", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                Text("회원가입", style = MaterialTheme.typography.headlineLarge)
                // 아이디 중복 조회 중에는 입력값 변경과 재요청을 막는다.
                val enabled = !model.busy
                AuthTextField("아이디", model.id, enabled) { model.id = it; model.message = "" }
                // 호스트가 조회 함수를 연결한 경우에만 중복 확인을 제공한다.
                if (isIdAvailable != null) {
                    TextButton(onClick = { model.checkId(isIdAvailable) }, enabled = enabled) { Text("아이디 중복 확인") }
                }
                AuthTextField("이메일", model.email, enabled, KeyboardType.Email) { model.email = it; model.message = "" }
                AuthTextField("생년월일 (YYYY-MM-DD)", model.birthDigits, enabled, KeyboardType.Number,
                    transformation = BirthDateVisualTransformation, change = model::editBirth)
                AuthTextField("비밀번호", model.password, enabled, KeyboardType.Password) { model.password = it; model.message = "" }
                Text("문자와 숫자를 포함해 8자 이상 입력해 주세요.", style = MaterialTheme.typography.bodySmall)
                AuthTextField("비밀번호 확인", model.confirm, enabled, KeyboardType.Password) { model.confirm = it; model.message = "" }
                // 안내 문구가 바뀌면 화면 읽기 도구가 현재 발화를 마친 뒤 읽도록 한다.
                if (model.message.isNotBlank()) Text(model.message, Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                Button(model::submit, Modifier.fillMaxWidth(), enabled = enabled) {
                    Text("입력 확인")
                }
                TextButton(onCancel, enabled = !model.busy) { Text("로그인으로 돌아가기") }
            }
        }
    }
}
