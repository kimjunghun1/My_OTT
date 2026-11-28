package com.example.myott.API

// 인물의 영화 작품 조회 응답
data class PersonMovieCreditsResponse(
    val cast: List<PersonMovieCredit>?,
    val crew: List<PersonMovieCredit>?
)

// 인물의 TV 작품 조회 응답
data class PersonTvCreditsResponse(
    val cast: List<PersonTvCredit>?,
    val crew: List<PersonTvCredit>?
)

// 영화 작품 정보
data class PersonMovieCredit(
    val id: Int,
    val title: String?,
    val poster_path: String?,
    val release_date: String?,
    val overview: String?,
    val vote_average: Double?,
    val genre_ids: List<Int>?,

    // 배우가 맡은 역할
    val character: String? = null,

    // 감독 및 제작진 직책
    val job: String? = null
)

// TV 작품 정보
data class PersonTvCredit(
    val id: Int,
    val name: String?,
    val poster_path: String?,
    val first_air_date: String?,
    val overview: String?,
    val vote_average: Double?,
    val genre_ids: List<Int>?,

    // 배우가 맡은 역할
    val character: String? = null,

    // 감독 및 제작진 직책
    val job: String? = null
)