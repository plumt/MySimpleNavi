package com.yun.mysimplenavi.data

import com.yun.mysimplenavi.data.model.KeywordSearchModel
import com.yun.mysimplenavi.data.model.OpenStreetMapModel
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface Api {

    @GET("/v2/local/search/keyword.json")
    fun searchKeyword(
        @Query("query") keyword: String,
        @Header("Authorization") Authorization: String = "KakaoAK 8ee9bf4ad1ec5983e0f32a8c55fc2a95"
    ): Observable<KeywordSearchModel.RS>

    @GET("/routed-{routed}/route/v1/driving/{path}")
//    @GET("/routed-car/route/v1/driving/{path}")
//    @GET("/routed-foot/route/v1/driving/{path}")
    fun openStreetMapNavi(
        @Path("routed") routed: String,
        @Path("path") path: String,
        @Query("overview") overview: Boolean = false,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("steps") steps: Boolean = true
) : Observable<OpenStreetMapModel.RS>
}