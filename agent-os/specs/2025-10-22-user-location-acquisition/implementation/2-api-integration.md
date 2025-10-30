# Task 2: API Integration

## Overview
**Task Reference:** Task #2 from `agent-os/specs/2025-10-22-user-location-acquisition/tasks.md`
**Implemented By:** api-engineer
**Date:** 2025-10-27
**Status:** ✅ Complete

### Task Description
This task involved integrating an API client into the Android application to send the user's location data to a backend server.

## Implementation Summary
To implement the API integration, I chose Retrofit, a type-safe HTTP client for Android and Java. It's a widely-used and robust library that simplifies the process of making network requests.

First, I added the necessary dependencies for Retrofit, Gson (for JSON serialization), and Kotlin Coroutines to the project's `build.gradle.kts` and `libs.versions.toml` files. I also added the `INTERNET` permission to the `AndroidManifest.xml` to allow the application to make network requests.

I then created a `data` package to hold the `UserLocation` data class, which models the JSON payload for the API request. In a new `api` package, I defined an `ApiService` interface using Retrofit annotations to specify the `POST` request to the `/user-location` endpoint. I also created an `ApiClient` singleton object to configure and provide a single instance of the Retrofit service.

Finally, I updated `MainActivity` to use this new API client. A new `sendLocationToBackend` function was created, which is called after successfully retrieving the user's location. This function uses Kotlin Coroutines to perform the network request on a background thread, ensuring the UI remains responsive. Basic error handling was added using `Toast` messages to notify the user of network or server errors.

To verify the implementation, I wrote a unit test for the `ApiClient` using `MockWebServer`. This test confirms that the client correctly constructs and sends the `POST` request to the specified endpoint with the correct JSON body, without needing a live backend.

## Files Changed/Created

### New Files
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/data/UserLocation.kt` - A data class to model the location data sent to the API.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/api/ApiService.kt` - A Retrofit interface defining the API endpoints.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/api/ApiClient.kt` - A singleton object to provide a configured Retrofit client.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/test/java/teamcherrypicker/com/api/ApiClientTest.kt` - Unit tests for the `ApiClient` using `MockWebServer`.

### Modified Files
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/gradle/libs.versions.toml` - Added versions and libraries for Retrofit, Coroutines, and MockWebServer.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/build.gradle.kts` - Added the new dependencies for networking and testing.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/AndroidManifest.xml` - Added the `INTERNET` permission.
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/MainActivity.kt` - Integrated the `ApiClient` to send location data after retrieval.

### Deleted Files
- None.

## Key Implementation Details

### Retrofit API Client
**Location:** `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/api/`

I created a standard Retrofit setup with an `ApiService` interface and an `ApiClient` singleton. The `ApiService` defines the API endpoints, and the `ApiClient` provides a configured Retrofit instance. This is a common and effective pattern for managing network requests in Android.

```kotlin
// ApiService.kt
interface ApiService {
    @POST("/user-location")
    suspend fun sendLocation(@Body location: UserLocation): Response<Unit>
}

// ApiClient.kt
object ApiClient {
    private const val BASE_URL = "https://your-backend-api.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

**Rationale:** This approach decouples the networking logic from the UI layer (`MainActivity`), making the code more modular, testable, and easier to maintain.

### Asynchronous Network Call
**Location:** `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/main/java/teamcherrypicker/com/MainActivity.kt`

I used `lifecycleScope.launch` to call the `sendLocation` suspend function from the `ApiClient`. This ensures the network request is made on a background thread and doesn't block the main UI thread.

```kotlin
private fun sendLocationToBackend(latitude: Double, longitude: Double) {
    lifecycleScope.launch {
        try {
            val response = ApiClient.apiService.sendLocation(UserLocation(latitude, longitude))
            // ... handle response
        } catch (e: Exception) {
            // ... handle error
        }
    }
}
```

**Rationale:** Using coroutines is the modern standard for handling asynchronous operations in Android. It simplifies the code and avoids common issues like memory leaks that can occur with other approaches.

## Dependencies

### New Dependencies Added
- `com.squareup.retrofit2:retrofit:2.9.0` - For making HTTP requests.
- `com.squareup.retrofit2:converter-gson:2.9.0` - For JSON serialization/deserialization.
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3` - For managing background threads.
- `com.squareup.okhttp3:mockwebserver:4.12.0` - For testing the API client.

## Testing

### Test Files Created/Updated
- `/Users/anarchytoast/AndroidStudioProjects/Cherry_picker/app/src/test/java/teamcherrypicker/com/api/ApiClientTest.kt` - Contains unit tests for the `ApiClient`.

### Test Coverage
- Unit tests: ✅ Complete
- Integration tests: ❌ None
- Edge cases covered:
  - Correct endpoint and HTTP method.
  - Correct request body serialization.

### Manual Testing Performed
Not applicable for this task.

## User Standards & Preferences Compliance

### agent-os/standards/backend/api.md
**File Reference:** `agent-os/standards/backend/api.md`

**How Your Implementation Complies:**
The implementation follows RESTful principles by using a `POST` request to a resource-based URL (`/user-location`). The data is sent in the request body as JSON.

**Deviations (if any):**
None.

### agent-os/standards/global/coding-style.md
**File Reference:** `agent-os/standards/global/coding-style.md`

**How Your Implementation Complies:**
The new code adheres to Kotlin conventions. I created new packages (`api`, `data`) to organize the code logically, following the principle of a consistent project structure.

**Deviations (if any):**
None.

## Integration Points

### APIs/Endpoints
- `POST /user-location` - Sends the user's latitude and longitude to the backend.
  - Request format: `{"latitude": Double, "longitude": Double}`
  - Response format: Empty body with a 200 status code on success.

## Known Issues & Limitations

### Limitations
1. **Placeholder Base URL**
   - Description: The `ApiClient` uses a placeholder URL (`https://your-backend-api.com/`) for the base URL.
   - Reason: The actual backend URL was not provided in the spec.
   - Future Consideration: This URL will need to be updated with the real backend address when it becomes available, likely managed through build configurations or an external properties file.
