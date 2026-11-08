# 회원가입 UI에 아이디 조회와 DB 저장 연결하기

기본 회원가입 화면은 **입력 확인**을 누르면 폼의 유효성을 검사합니다. 팀 앱에서 아이디 중복 확인만 사용하려면 아래 조회 함수를 연결하면 됩니다. 실제 계정을 생성하려면 가입 요청을 처리하는 단계도 추가해야 합니다.

## 1. 아이디 중복 조회 연결

`RegistrationScreen`을 호출할 때 선택 인자 `isIdAvailable`에 조회 함수를 전달하면 **아이디 중복 확인** 버튼이 표시됩니다. 인자를 생략하면 버튼이 숨겨집니다. 함수는 앞뒤 공백을 제거한 아이디를 받아 **사용 가능하면 `true`, 이미 사용 중이면 `false`**를 반환합니다. 조회 오류는 예외로 전달하면 화면이 재시도 안내를 표시합니다.

이 프로젝트의 기기 내 Room DB를 사용하려면 `MainActivity.kt`의 `onCreate()`에서 `setContent` 호출 전에 저장소를 준비합니다. 아래 예시에 사용하는 코루틴 함수는 `kotlinx.coroutines.Dispatchers`와 `kotlinx.coroutines.withContext`를 import합니다.

```kotlin
val repository = RegistrationRepository(UserDatabase.get(applicationContext).users())
```

이어서 기존 `Scaffold` 안의 화면 호출에 조회 함수를 전달합니다. `available()`은 동기식 DAO를 호출하므로 IO 스레드에서 실행합니다.

```kotlin
RegistrationScreen(
    model = registration,
    onCancel = { finish() },
    modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
    isIdAvailable = { id ->
        withContext(Dispatchers.IO) { repository.available(id) }
    }
)
```

UI 파일만 가져간 팀 앱은 같은 인자에 자체 저장소나 서버 API의 조회 함수를 전달할 수 있습니다. 위 Room 예시를 그대로 사용하려면 팀 앱에 `RegistrationRepository.kt`, `UserDatabase.kt`와 프로젝트의 Room·KSP 설정도 포함하세요.

## 2. 회원정보 저장 연결 방향

현재 `RegistrationModel.submit()`은 `inputIssue()`로 입력값만 검사합니다. 저장 콜백은 아직 없습니다. Room에 실제 가입 정보를 저장하려면 다음 순서로 확장하세요.

1. **저장 동의를 입력받습니다.** 현재 폼은 `agreed = false`로 요청을 만듭니다. 가입 버튼을 연결할 때는 화면에 저장 동의 항목을 추가하고 사용자가 선택한 값을 `RegistrationRequest.agreed`에 전달합니다.
2. **가입 요청을 검증합니다.** 제출 시점의 입력값으로 `RegistrationRequest`를 만들고 `request.issue()`를 호출합니다. 오류 문구가 있으면 화면에 표시하고, 결과가 `null`이면 저장을 진행합니다. 이 함수는 입력 형식과 저장 동의를 함께 확인합니다.
3. **앱에서 저장 함수를 전달합니다.** 조회와 같은 방식으로 `suspend (RegistrationRequest) -> Boolean` 타입의 저장 콜백을 추가합니다. Room을 쓰는 앱에서는 콜백 안에서 `withContext(Dispatchers.IO) { repository.register(request) }`를 호출합니다. 기존 저장소가 아이디와 이메일을 정리하고, 비밀번호를 해시한 뒤 삽입합니다.
4. **최종 삽입 결과로 가입 여부를 표시합니다.** 저장 중에는 중복 제출을 막고, `register()`가 `true`를 반환하면 비밀번호 입력값을 비우고 가입 완료를 알립니다. `false`는 아이디 또는 이메일 중복이므로 입력을 수정하도록 안내합니다. DB 접근 예외가 발생하면 재시도를 안내합니다.

중복 조회 결과는 조회 시점에만 유효합니다. 조회와 가입 사이에 같은 아이디가 등록될 수 있으므로, 최종 중복 판정은 DB의 아이디 기본 키와 이메일 고유 제약에 맡깁니다.

## 3. 여러 기기가 계정을 공유하는 경우

위 Room 예시는 각 기기의 앱 내부 `users.db`에 적용됩니다. 여러 기기에서 같은 계정으로 로그인해야 한다면 조회와 가입을 같은 서버 API에 연결하세요. Android 앱은 API를 호출하고, 서버가 공유 DB의 중복 검사, 비밀번호 해시, 회원 레코드 삽입을 담당합니다. 이 구조에서도 화면에 전달하는 `isIdAvailable`의 반환 규칙은 같습니다.
