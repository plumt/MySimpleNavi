package com.yun.mysimplenavi.data.repository

import com.yun.mysimplenavi.data.Api
import javax.inject.Inject


class ApiRepository @Inject constructor(private val api: Api) {

    fun searchKeyword(keyword: String, x: String, y: String, page: Int) =
        api.searchKeywordToCode(keyword, x, y, page)

    fun searchCode(x: String, y: String) = api.searchCodeToKeyword(x, y)

    fun openStreetMapNavi(isCar: Boolean, path: String) =
        api.openStreetMapNavi(if (isCar) "car" else "foot", path)
}