package com.example.myott.ui.auth

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myott.formatBirth

internal object BirthDateVisualTransformation : VisualTransformation {
    // 입력 상태에는 숫자를 유지하고 화면에 표시할 문자열에만 하이픈을 넣는다.
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = formatBirth(text.text)
        val offsets = object : OffsetMapping {
            // 원본 커서 앞에 추가된 하이픈 수만큼 화면 커서를 이동한다.
            override fun originalToTransformed(offset: Int): Int = when {
                offset > 6 -> offset + 2
                offset > 4 -> offset + 1
                else -> offset
            }

            // 화면 커서 앞의 숫자를 세어 원본 입력에서의 위치를 구한다.
            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset).count { it != '-' }
        }
        return TransformedText(AnnotatedString(formatted), offsets)
    }
}
