package com.example.myott.API
import com.google.gson.annotations.SerializedName

// TMDB 목록 조회 API의 응답을 매핑하는 데이터 클래스
data class MovieResponse(
    val results: List<Movie>, // 아래 Movie 데이터 클래스의 List를 저장하는 results 변수

    // 첫 페이지에서 새로운 작품 10편을 못 채우면 다음 페이지를 조회할 때 사용합니다.
    @SerializedName("total_pages")
    val totalPages: Int = 0
)

data class Movie(
    val id: Int, // 영화 고유 id
    val poster_path: String?, // 영화 포스터 이미지 경로

    // 영화는 title, TV 프로그램은 name으로 내려오는 제목을 하나로 읽습니다.
    @SerializedName(value = "title", alternate = ["name"])
    val title: String,  //영화 제목 및 TV 프로그램 제목

    val overview: String, // 영화 줄거리

    // 제목 검색 결과에도 선택한 장르 조건을 적용할 때 사용합니다.
    @SerializedName("genre_ids")
    val genreIds: List<Int>? = null,

    // 결과를 합칠 때 앱에서 "movie" 또는 "tv"를 기록합니다.
    // 영화와 TV는 같은 숫자 ID를 가질 수 있어 유형도 함께 보관합니다.
    val mediaType: String = ""
)