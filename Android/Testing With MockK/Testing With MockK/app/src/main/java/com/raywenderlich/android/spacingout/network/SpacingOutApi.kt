/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.spacingout.network

import com.raywenderlich.android.spacingout.models.ApodImage
import com.raywenderlich.android.spacingout.models.EarthImage
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SpacingOutApi {
  companion object {
    fun create(): SpacingOutApi {
      val client = OkHttpClient.Builder().addInterceptor { chain ->
        val url = chain.request().url().newBuilder().addQueryParameter("api_key", "IaZbUHgLeD5G3evuRL0ZDlaAXP9bIVPU919Gt9CF").build()
        val request = chain.request().newBuilder().url(url).build()
        chain.proceed(request)
      }.build()
      return Retrofit.Builder()
          .addConverterFactory(MoshiConverterFactory.create())
          .baseUrl("https://api.nasa.gov")
          .client(client)
          .build()
          .create(SpacingOutApi::class.java)
    }
  }

  @GET("/planetary/apod")
  fun getImage(@Query("date") date: String): Call<ApodImage>

  @GET("/planetary/earth/imagery/")
  suspend fun getEarthImagery(@Query("lon") longitude: Float, @Query("lat") latitude: Float): EarthImage
}