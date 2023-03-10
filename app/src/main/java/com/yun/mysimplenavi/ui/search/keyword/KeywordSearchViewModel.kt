package com.yun.mysimplenavi.ui.search.keyword

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yun.mysimplenavi.base.ListLiveData
import com.yun.mysimplenavi.data.model.KeywordSearchModel
import com.yun.mysimplenavi.data.repository.ApiRepository
import com.yun.mysimplenavi.util.Util
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

    val locationKeyword = MutableLiveData("")
    val keywordSearchResults = ListLiveData<KeywordSearchModel.RS.Documents>()
    val isFocus = MutableLiveData(false)

    var page = 1

    /**
     * 통신 중 로딩 프로그레스바 노출
     */
    val isLoading = MutableLiveData(false)

    fun callApi(lat: Double, lon: Double) {
        isLoading.value = true
        api.searchKeyword(locationKeyword.value!!, lon.toString(), lat.toString(), page)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                if (it.meta?.is_end != false) page = -1
                else page++
                it.documents!!.forEach { documents ->
                    keywordSearchResults.add(addItem(keywordSearchResults.value!!.size, documents))
                }
            }.subscribe({
                isLoading.value = false
                Log.d("lys", "success")
            }, {
                isLoading.value = false
                page = 0
                keywordSearchResults.value = arrayListOf()
                locationKeyword.value = ""
                Log.e("lys", "fail : ${it.message}")
            })
    }

    private fun addItem(index: Int, param: KeywordSearchModel.RS.Documents) =
        KeywordSearchModel.RS.Documents(
            index,
            param.address_name,
            param.road_address_name,
            param.place_name,
            param.lon,
            param.lat,
            Util.formatDistance(param.distance.toDouble())//param.distance
        )
}