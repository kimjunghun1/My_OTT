package com.example.myott.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// TMDB API 서버와 통신하기 위한 Retrofit 인스턴스 생성
// baseUrl은 모든 API 요청의 기본 주소 (반드시 '/'로 끝나야 함)
private val retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/")
    .addConverterFactory(GsonConverterFactory.create()) // 서버에서 받은 JSON 응답을 Kotlin 데이터 클래스(Movie)로 자동 변환
    .build()

val tmdbService = retrofit.create(ApiService::class.java)

// TMDB API 요청 주소에 넣을 값들을 정의하는 인터페이스
// 최종 요청 URL 예시: https://api.themoviedb.org/3/movie/640146?api_key=YOUR_KEY&language=ko-KR
interface ApiService{
    @GET("movie/{id}") // 특정 영화 ID에 해당하는 데이터를 조회하는 GET 요청(id = 특정 영화 고유 id)
    suspend fun getMovie(
        @Path("id") movieId: Int, // 영화 고유 id
        @Query("api_key") apiKey : String, // API 키
        @Query("language") language: String = "ko-KR" // 언어 설정
    ) : Movie

    // 콘텐츠 상세 화면용: 기본 정보 + 출연진/감독(credits) + OTT(watch/providers) + 관람 등급(release_dates)
    // 요청 URL 예시: movie/640146?api_key=KEY&language=ko-KR&append_to_response=credits,watch/providers,release_dates
    @GET("movie/{id}")
    suspend fun getMovieDetail(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR",
        @Query("append_to_response") append: String = "credits,watch/providers,release_dates"
    ) : MovieDetail

    @GET("tv/{id}")
    suspend fun getTvDetail(
        @Path("id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR",
        @Query("append_to_response") append: String = "credits,watch/providers,content_ratings"
    ) : MovieDetail

    //주간 인기 프로그램을 받아옴 (영화, Tv 프로그램)
    //mediaType에 "movie"를 넣으면 주간 인기 영화, "tv"를 넣으면 주간 인기 TV 프로그램
    @GET("trending/{mediaType}/week")
    suspend fun getWeeklyTrending(
        @Path("mediaType") mediaType: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR"
    ): MovieResponse

    // 검색어 없이 카테고리로 검색합니다. mediaType은 "movie" 또는 "tv"입니다.
    @GET("discover/{mediaType}")
    suspend fun discoverContents(
        @Path("mediaType") mediaType: String,
        @Query("api_key") apiKey: String,
        @Query("with_genres") genres: String?,
        @Query("page") page: Int,
        @Query("language") language: String = "ko-KR"
    ): MovieResponse

    // 사용자가 입력한 제목으로 영화 또는 TV 프로그램을 검색합니다.
    @GET("search/{mediaType}")
    suspend fun searchByTitle(
        @Path("mediaType") mediaType: String,
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("language") language: String = "ko-KR"
    ): MovieResponse

    // 추가 인물별 영화 작품 조회
    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR"
    ): PersonMovieCreditsResponse

    // 추가 인물별 TV 작품 조회
    @GET("person/{person_id}/tv_credits")
    suspend fun getPersonTvCredits(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR"
    ): PersonTvCreditsResponse

    // ApiService.kt 파일 하단 또는 데이터 클래스 모음에 추가
    data class GenreListResponse(
        val genres: List<TmdbGenre>
    )

    // ApiService 인터페이스 내부에 추가
    @GET("genre/{mediaType}/list")
    suspend fun getGenreList(
        @Path("mediaType") mediaType: String, // "movie" 또는 "tv"
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ko-KR"
    ): GenreListResponse
}
