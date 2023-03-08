package com.yun.mysimplenavi.data

import com.yun.mysimplenavi.data.model.KeywordSearchModel
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface Api {

    @GET("/v2/local/search/keyword.json")
    fun searchKeyword(
        @Query("query") keyword: String,
        @Header("Authorization") Authorization: String = "KakaoAK 8ee9bf4ad1ec5983e0f32a8c55fc2a95"
    ): Observable<KeywordSearchModel.RS>
}