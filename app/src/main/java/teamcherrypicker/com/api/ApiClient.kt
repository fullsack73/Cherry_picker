package teamcherrypicker.com.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import teamcherrypicker.com.BuildConfig

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"

    private val resolvedBaseUrl: String by lazy {
        val raw = BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
        if (raw.endsWith('/')) raw else "$raw/"
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(resolvedBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
