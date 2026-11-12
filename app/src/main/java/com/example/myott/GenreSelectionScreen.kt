
package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myott.GenreViewModel



// 색상 리스트 수정 편하라고 미리 만들어놈

// 배경색
val BackgroundColor = Color(0xFF121212)
// 카드 배경색
val CardColor = Color(0xFF202020)
// 장르 선택 시
val PurpleColor = Color(0xFF9363EB)
// 기본 글자
val TextColor = Color.White
// 부가 설명, 선택되지 않은 장르 글자 색상
val SubTextColor = Color(0xFFAAAAAA)


// 장르 선택 화면

@Composable
fun GenreSelectionScreen(
    // 변경: ViewModel을 전달받음
    genreViewModel: GenreViewModel,
    modifier: Modifier = Modifier
) {

    // 변경 : ViewModel에서 전체 장르 목록을 가져옴
    // collectAsState()를 사용하여 DB 데이터가 변경되면 화면도 자동으로 갱신
    val genres by genreViewModel.allGenres.collectAsState()

    // 추가: 현재 선택된 장르 개수 계산
    val selectedCount = genres.count { it.isSelected }

    // 추가: 기존 화면의 장르 배치 순서 유지
    // DB에 저장된 genreId와 비교하여 장르의 순서를 정함
    val genreOrder = listOf(
        "ACTION", "HORROR",
        "SF", "HISTORY",
        "Noir", "MUSICAL",
        "ROMANCE", "COMEDY",
        "FANTASY", "MYSTERY",
        "ANIMATION", "DOCUMENTARY"
    )

    // 추가: 장르 이름과 아이콘을 저장하는 리스트
    val genreNames = listOf(
        "⚔  액션", "♧  공포",
        "✧  SF", "♜  역사",
        "♧  느와르", "♬ 뮤지컬",
        "♡  로맨스", "☻  코미디",
        "♢  판타지", "♠  미스터리",
        "◎  애니메이션", "▧  다큐멘터리"
    )

    // 추가: DB에서 가져온 장르를 기존 화면 순서로 정렬
    val displayGenres = genres.sortedBy {
        genreOrder.indexOf(it.genreId)
    }

    // Column : 화면의 요소들을 세로 방향으로 배치
    Column(
        modifier = modifier
            .fillMaxSize() // 화면 전체 크기 사용
            .background(BackgroundColor) // 배경색 설정
            .padding(20.dp) // 화면 가장자리 여백 설정
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 뒤로 가기 버튼
            Text(
                text = "←",
                color = TextColor,
                fontSize = 30.sp,
                modifier = Modifier.clickable {
                    // 이전 화면 미구현 이슈
                }
            )

            // 화면 제목
            Text(
                text = "취향 설정",
                color = TextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // 장르 선택을 건너뛰는 버튼
            Text(
                text = "건너뛰기",
                color = SubTextColor,
                fontSize = 13.sp,

                modifier = Modifier.clickable {
                    // 다음 화면 미구현 이슈
                }
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        // 화면 제목 및 설명
        Text(
            text = "좋아하는 장르를 선택해 주세요.",
            color = TextColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "관심 있는 장르를 선택하면 콘텐츠를 추천해 드려요.",
            color = SubTextColor,
            fontSize = 12.sp
        )

        // 설명과 선택 개수 영역 사이의 간격
        Spacer(modifier = Modifier.height(25.dp))


        // 장르 선택 개수 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "여러 개를 선택할 수 있어요",
                color = SubTextColor,
                fontSize = 11.sp
            )

            // 현재 선택한 장르 개수 표시
            // 변경 : selectedCount : DB에서 가져온 선택된 장르의 개수
            // 최대 선택 가능 개수는 3개
            Text(
                text = "${selectedCount}/3개 선택",
                color = PurpleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 장르 카드 2열 배치
        for (i in 0 until 6) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 한 줄에 장르 카드 2개 배치
                for (j in 0 until 2) {

                    val index = i * 2 + j

                    // DB에서 장르 데이터를 가져왔을 때만 카드 표시
                    if (index < displayGenres.size) {

                        // 정렬된 장르 목록에서 현재 위치의 장르 가져오기
                        val genre = displayGenres[index]

                        // 장르 카드 생성
                        GenreCard(
                            // 장르 ID에 맞는 아이콘과 장르 이름 표시
                            genre = genreNames[genreOrder.indexOf(genre.genreId)],
                            // DB에 저장된 장르의 선택 상태 전달
                            isSelected = genre.isSelected,
                            modifier = Modifier.weight(1f)
                        ) {
                            // 이미 선택한 장르라면 선택 해제
                            // DB에 해당 장르의 선택 상태를 false로 변경
                            if (genre.isSelected) {
                                genreViewModel.updateGenreSelection(
                                    genre.genreId,
                                    false
                                )
                            }
                            // 선택 개수가 3개 미만이면 장르 선택
                            // DB에 해당 장르의 선택 상태를 true로 변경
                            else if (selectedCount < 3) {
                                genreViewModel.updateGenreSelection(
                                    genre.genreId,
                                    true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
        // 남은 공간을 차지하여 하단 버튼을 화면 아래쪽에 배치
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "취향은 나중에 언제든지 변경할 수 있어요.",
            color = SubTextColor,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // 다음 화면 이동 버튼
        Button(
            // 다음화면이 없어 아직 미구현, 기능 추가
            onClick = {  },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleColor
            )
        ) {
            Text(
                text = "다음",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



// 장르 카드 UI
@Composable
fun GenreCard(
    // 장르 이름
    genre: String,

    //장르의 선택 여부
    isSelected: Boolean,
    modifier: Modifier = Modifier,

    // 카드 클릭 시 실행할 함수
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp) // 카드 높이 설정
            // 카드를 클릭하면 전달받은 onClick 함수 실행
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),

        // 장르 선택 여부에 따라 배경색 변경
        color = if (isSelected) {
            // 선택된 경우 보라색 계열 배경
            Color(0xFF302344)

        } else {
            // 선택되지 않은 경우 기본 카드 배경
            CardColor
        },

        // 장르 선택 여부에 따라 테두리 변경
        border = if (isSelected) {

            // 선택시 보라색 테두리 표시
            BorderStroke(1.5.dp, PurpleColor)

        } else {

            // 일반적으로는 회색 테두리 표시
            BorderStroke(1.dp, Color(0xFF353535))
        }

    ) {

        //카드 내부 배치
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // 장르 이름 표시
            Text(
                text = genre,

                // 선택 시 글자 색상 변경
                color = if (isSelected) {
                    Color.White
                } else {
                    SubTextColor
                },
                fontSize = 13.sp,

                // 선택시 굵게 표시
                fontWeight = if (isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )



            // 오른쪽 위 체크 원 표시
            if (isSelected) {

                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-3).dp)
                        .background(
                            PurpleColor,
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
