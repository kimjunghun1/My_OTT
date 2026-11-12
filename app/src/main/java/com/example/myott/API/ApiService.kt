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
}