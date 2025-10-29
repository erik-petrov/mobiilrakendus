# Step 3 Report: API Integration

### **Chosen API: Joke API**
- **API Provider**: JokeAPI (v2.jokeapi.dev)
- **Selection Rationale**:
  - Aligns perfectly with our app's lighthearted and engaging theme;
  - Provides simple, well-structured JSON responses ideal for demonstration;
  - Offers free access with generous rate limits;
  - Includes content filtering capabilities for family-friendly content.
 
    
### **API Endpoint**
**Base Configuration:**
- **Base URL**: `https://v2.jokeapi.dev/`.
- **HTTP Client**: OkHttpClient with logging interceptor.
- **JSON Parser**: Moshi with Kotlin reflection adapter.
- **Architecture**: Repository pattern with Retrofit.

**Primary Endpoints:**

`app/src/main/java/com/example/honk/data/remote/JokeApiService.kt`
1. **Single Joke Endpoint:**
   ```kotlin
   @GET("joke/Any?safe-mode")
    suspend fun getRandomJoke(
        @Query("type") type: String = "single",
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("lang") lang: String = "en"
    ): JokeResponse
   ```

2. **Multiple Jokes Endpoint:**
   ```kotlin
   @GET("joke/Any?safe-mode")
    suspend fun getRandomJokes(
        @Query("type") type: String = "single",
        @Query("amount") amount: Int = 10,
        @Query("blacklistFlags") blacklistFlags: String? = null,
        @Query("lang") lang: String = "en"
    ): JokeMultiResponse
   ```

**Request Parameters:**
- type: Enforced as "single" for consistent response format;
- safe-mode: Always enabled for content filtering;
- blacklistFlags: Excludes religious, political, racist, sexist content;
- amount: Batch size for joke selection (up to 10);
- lang: Language code (default: "en").

**Example Request URL:**

https://v2.jokeapi.dev/joke/Any?safe-mode&type=single&amount=10&blacklistFlags=religious,political,racist,sexist&lang=en

**Data Model (Kotlin Data Class)**

`app/src/main/java/com/example/honk/data/remote/JokeResponse.kt`

```kotlin
@JsonClass(generateAdapter = true)
data class JokeResponse(
    val error: Boolean,
    val category: String? = null,
    val type: String,          // "single" or "twopart"
    val joke: String? = null,  // content for single-type jokes
    val flags: JokeFlags? = null,
    val id: Int? = null,
    val safe: Boolean? = null,
    val lang: String? = null
)

@JsonClass(generateAdapter = true)
data class JokeMultiResponse(
    val error: Boolean,
    val amount: Int,
    val jokes: List<JokeResponse>
)
```

**Repository Pattern Implementation**

`app/src/main/java/com/example/honk/repository/JokeRepository.kt`

```kotlin
class JokeRepository(
    private val api: JokeApiService =
        NetworkModule.retrofit.create(JokeApiService::class.java)
) {
    suspend fun fetchShortJoke(
        maxLen: Int = 115,
        blacklist: String = "religious,political,racist,sexist",
        lang: String = "en"
    ): String {
        // Smart joke selection with length constraints and fallbacks
    }
}
```
### **Error Handling Strategy**

**1. API Response Validation**
  - Checks error boolean field in every API response;
  - Validates joke content for nullability and blank strings;
  - Ensures response type consistency.
    
**2. Retry Logic**
  - First Attempt: Fetch 10 jokes, filter by length (≤115 characters);
  - Second Attempt: If first batch fails, fetch another 10 jokes;
  - Final Attempt: Single joke fallback with intelligent truncation.
    
**3. Content validation** `app/src/main/java/com/example/honk/repository/JokeRepository.kt`
```kotlin
val candidate = resp.jokes
    .asSequence()
    .filter { 
        !it.error && 
        it.type == "single" && 
        !it.joke.isNullOrBlank() 
    }
    .map { it.joke!!.trim() }
    .firstOrNull { it.length <= maxLen }
```
**4. Technical protection**
   - Length Management: Auto-truncate long jokes with ellipsis (…);
   - Content Fallback: Default message "No joke this time :(" when no jokes available;
   - Batch Processing: Multiple attempts significantly increase success probability.

**Fallback Scenarios:**
- API Returns Error Flag: Use alternative joke batch;
- No Suitable Jokes Found: Try secondary batch request;
- All Batches Fail: Return single joke with length adjustment;
- Complete Failure: Show user-friendly fallback message.

### **Example API Response**

**Successful Response** 
```json
{
  "error": false,
  "category": "Programming",
  "type": "single",
  "joke": "Why do Java developers wear glasses? Because they can't C#.",
  "flags": {
    "nsfw": false,
    "religious": false,
    "political": false,
    "racist": false,
    "sexist": false,
    "explicit": false
  },
  "id": 1,
  "safe": true,
  "lang": "en"
}
```
**Multi-Joke Response**
```json
{
  "error": false,
  "amount": 3,
  "jokes": [
    {
      "error": false,
      "category": "Pun",
      "type": "single",
      "joke": "I'm reading a book about anti-gravity. It's impossible to put down!",
      "flags": {...},
      "id": 2,
      "safe": true,
      "lang": "en"
    }
  ]
}
```
