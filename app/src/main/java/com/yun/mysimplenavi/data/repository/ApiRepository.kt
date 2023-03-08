package com.yun.mysimplenavi.data.repository

import com.yun.mysimplenavi.data.Api
import javax.inject.Inject
import javax.inject.Named


class ApiRepository @Inject constructor(private val api: Api) {

    fun searchKeyword(keyword: String) = api.searchKeyword(keyword)
}