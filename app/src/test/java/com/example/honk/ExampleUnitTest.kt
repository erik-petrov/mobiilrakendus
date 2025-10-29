package com.example.honk

import com.example.honk.repository.JokeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun api_works(){
        val jk = JokeRepository()
        var joke = ""
        runBlocking { joke = jk.fetchShortJoke() }

        assert(joke != "")
    }
}