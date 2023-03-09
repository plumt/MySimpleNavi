package com.yun.mysimplenavi.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yun.mysimplenavi.base.ListLiveData
import com.yun.mysimplenavi.data.model.KeywordSearchModel
import com.yun.mysimplenavi.data.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class KeywordSearchViewModel @Inject constructor(
    @Named("kakao") private val api: ApiRepository
) : ViewModel() {

    val locationKeyword = MutableLiveData("건대")
    val keywordSearchResults = ListLiveData<KeywordSearchModel.RS.Documents>()

    fun callApi() {
        api.searchKeyword(locationKeyword.value!!).observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val temp = arrayListOf<KeywordSearchModel.RS.Documents>()
                it.documents!!.forEachIndexed { index, documents ->
                    temp.add(keywordSearchModelDocumentItem(index, documents))
                }
                keywordSearchResults.value = temp
            }.subscribe({
                Log.d("lys", "success")
            }, {
                Log.e("lys", "fail : ${it.message}")
            })
    }

    private fun keywordSearchModelDocumentItem(index: Int, param: KeywordSearchModel.RS.Documents) =
        KeywordSearchModel.RS.Documents(
            index,
            param.address_name,
            param.road_address_name,
            param.place_name,
            param.lon,
            param.lat
        )
}