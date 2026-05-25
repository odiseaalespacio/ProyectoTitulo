package com.example.cloty_administrador.data.api

import com.example.cloty_administrador.BuildConfig
import com.example.cloty_administrador.data.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

  private var tokenStore: TokenStore? = null

  fun init(store: TokenStore) {
    tokenStore = store
  }

  private val authInterceptor = Interceptor { chain ->
    val requestBuilder = chain.request().newBuilder()
    val token = runBlocking { tokenStore?.tokenFlow?.first() }
    if (!token.isNullOrBlank()) {
      requestBuilder.addHeader("Authorization", "Bearer $token")
    }
    chain.proceed(requestBuilder.build())
  }

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .addInterceptor(authInterceptor)
      .addInterceptor(
        HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        }
      )
      .build()
  }

  val api: ClotyApi by lazy {
    Retrofit.Builder()
      .baseUrl(BuildConfig.API_BASE_URL)
      .client(httpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ClotyApi::class.java)
  }
}
