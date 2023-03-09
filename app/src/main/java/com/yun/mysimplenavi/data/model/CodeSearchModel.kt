package com.yun.mysimplenavi.data.model

class CodeSearchModel {
    data class RS(
        val documents: ArrayList<Documents>?
    ){
        data class Documents(
            val address_name: String?
        )
    }
}