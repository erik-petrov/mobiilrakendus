package com.example.honk.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface JokeApiService {
    // single joke
    @GET("joke/Any?safe-mode")
    suspend fun getRandomJoke(
        @Query("type") type: String = "single",
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("lang") lang: String = "en"
    ): JokeResponse

    // list of jokes
    @GET("joke/Any?safe-mode")
    suspend fun getRandomJokes(
        @Query("type") type: String = "single",
        @Query("amount") amount: Int = 10,
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("lang") lang: String = "en"
    ): JokeMultiResponse
}