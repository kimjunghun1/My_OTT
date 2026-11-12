package com.example.myott.API

// TMDB 목록 조회 API의 응답을 매핑하는 데이터 클래스
data class MovieResponse(
    val results: List<Movie> // 아래 Movie 데이터 클래스의 List를 저장하는 results 변수
)

data class Movie(
    val id: Int, // 영화 고유 id
    val poster_path: String?, // 영화 포스터 이미지 경로
    val title: String, // 영화 제목
    val overview: String // 영화 줄거리
)