package com.example.honk.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JokeResponse(
    val error: Boolean,
    val category: String? = null,
    val type: String,          // "single"
    val joke: String? = null,  // we enforce type=single
    val flags: JokeFlags? = null,
    val id: Int? = null,
    val safe: Boolean? = null,
    val lang: String? = null
)

@JsonClass(generateAdapter = true)
data class JokeFlags(
    val nsfw: Boolean,
    val religious: Boolean,
    val political: Boolean,
    val racist: Boolean,
    val sexist: Boolean,
    val explicit: Boolean
)

@JsonClass(generateAdapter = true)
data class JokeMultiResponse(
    val error: Boolean,
    val amount: Int,
    val jokes: List<JokeResponse>
)