package com.example.myott.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/** 키보드 종류와 표시 형식을 설정하고 입력 변경을 호출자에게 전달하는 공통 입력란이다. */
@Composable
internal fun AuthTextField(
    label: String,
    value: String,
    enabled: Boolean,
    type: KeyboardType = KeyboardType.Text,
    // 기본값은 비밀번호를 가리며, 생년월일처럼 별도 표시 형식을 지정할 수도 있다.
    transformation: VisualTransformation = if (type == KeyboardType.Password) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    },
    change: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = change,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        visualTransformation = transformation
    )
}
