package teamcherrypicker.com.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import teamcherrypicker.com.BuildConfig
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"
    private const val TIMEOUT_SECONDS = 60L

    private val resolvedBaseUrl: String by lazy {
        val raw = BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
        if (raw.endsWith('/')) raw else "$raw/"
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(resolvedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
