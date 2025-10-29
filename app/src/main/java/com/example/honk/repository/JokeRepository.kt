package com.example.honk.repository

import com.example.honk.data.remote.JokeApiService
import com.example.honk.data.remote.NetworkModule
import kotlinx.coroutines.runBlocking

class JokeRepository(
    private val api: JokeApiService =
        NetworkModule.retrofit.create(JokeApiService::class.java)
) {
    /**
     * Fetch a short joke.
     * @param maxLen max allowed characters (default 115)
     * @param blacklist flags to exclude
     */
    suspend fun fetchShortJoke(
        maxLen: Int = 115,
        blacklist: String = "religious,political,racist,sexist",
        lang: String = "en"
    ): String {
        // 1st attempt: batch
        api.getRandomJokes(
            type = "single",
            amount = 10,
            blacklistFlags = blacklist,
            lang = lang
        ).let { resp ->
            if (!resp.error) {
                val candidate = resp.jokes
                    .asSequence()
                    .filter { it.type == "single" && !it.joke.isNullOrBlank() }
                    .map { it.joke!!.trim() }
                    .firstOrNull { it.length <= maxLen }
                if (candidate != null) return candidate

                // if none <= maxLen, keep shortest
                val shortest = resp.jokes
                    .asSequence()
                    .filter { it.type == "single" && !it.joke.isNullOrBlank() }
                    .map { it.joke!!.trim() }
                    .minByOrNull { it.length }

                if (shortest != null && shortest.length <= maxLen + 20) {
                    return shortest.take(maxLen - 1) + "…"
                }
            }
        }

        // 2nd attempt: try another batch (in case previous set was unlucky)
        api.getRandomJokes(
            type = "single",
            amount = 10,
            blacklistFlags = blacklist,
            lang = lang
        ).let { resp ->
            if (!resp.error) {
                val candidate = resp.jokes
                    .asSequence()
                    .filter { it.type == "single" && !it.joke.isNullOrBlank() }
                    .map { it.joke!!.trim() }
                    .firstOrNull { it.length <= maxLen }
                if (candidate != null) return candidate
            }
        }

        // Fallback: single joke (may be long)
        val single = api.getRandomJoke(
            type = "single",
            blacklistFlags = blacklist,
            lang = lang
        )
        val text = single.joke?.trim().orEmpty()
        return if (text.isBlank()) "No joke this time :(" else {
            if (text.length <= maxLen) text else text.take(maxLen - 1) + "…"
        }
    }
}