package com.example.myott.API

import com.google.gson.annotations.SerializedName

// TMDB 영화 상세 조회 응답 (movie/{id}?append_to_response=credits,watch/providers,release_dates)
// Gson은 값이 없으면 null을 넣기 때문에 모두 nullable로 둔다.
data class MovieDetail(
    val id: Int,

    @SerializedName(value = "title", alternate = ["name"])
    val title: String?,                  // 한국어 제목

    @SerializedName(value = "original_title", alternate = ["original_name"])
    val original_title: String?,         // 원제

    val overview: String?,               // 줄거리(개요)
    val poster_path: String?,            // 포스터 이미지 경로
    val backdrop_path: String?,          // 가로 배경 이미지 경로

    @SerializedName(value = "release_date", alternate = ["first_air_date"])
    val release_date: String?,           // 개봉일 (예: "2024-05-01")
    val runtime: Int?,                   // 상영 시간(분)
    val vote_average: Double?,           // 평점 (10점 만점)
    val genres: List<TmdbGenre>?,        // 장르 목록
    val credits: Credits?,               // 출연진 · 제작진
    @SerializedName("watch/providers")
    val watchProviders: WatchProvidersResponse?, // 볼 수 있는 OTT
    val release_dates: ReleaseDatesResponse?,    // 나라별 관람 등급
)

data class TmdbGenre(
    val id: Int,        // TMDB 장르 id (예: 28 = 액션)
    val name: String?,  // 장르 이름 (ko-KR이면 한국어)
)

data class Credits(
    val cast: List<CastMember>?,
    val crew: List<CrewMember>?,
)

data class CastMember(
    val id: Int,               // 추가: TMDB 배우 ID
    val name: String?,
    val character: String?,
    val order: Int?,
)

data class CrewMember(
    val id: Int,          // 추가: TMDB 인물 고유 ID
    val name: String?,
    val job: String?, // "Director"면 감독
)

data class WatchProvidersResponse(
    val results: Map<String, CountryProviders>?, // 나라 코드("KR") → OTT 목록
)

data class CountryProviders(
    val link: String?,              // TMDB의 시청 정보 페이지
    val flatrate: List<Provider>?,  // 구독으로 보기 (넷플릭스 등)
    val rent: List<Provider>?,      // 대여
    val buy: List<Provider>?,       // 구매
)

data class Provider(
    val provider_id: Int,
    val provider_name: String?,
    val logo_path: String?,
)

data class ReleaseDatesResponse(
    val results: List<CountryReleaseDates>?,
)

data class CountryReleaseDates(
    val iso_3166_1: String?, // 나라 코드
    val release_dates: List<ReleaseDate>?,
)

data class ReleaseDate(
    val certification: String?, // 관람 등급 (예: "15", "12", "All")
)

data class ContentRatingsResponse(
    val results: List<CountryContentRating>?,
)

data class CountryContentRating(
    val iso_3166_1: String? // 나라 코드 (예: "KR")
)