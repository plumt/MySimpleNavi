package com.yun.mysimplenavi.ui.search.map

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yun.mysimplenavi.data.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MapSearchViewModel @Inject constructor(
    @Named("kakao") private val api: ApiRepository
) : ViewModel() {

    val addressName = MutableLiveData("")
    var centerLatitude = ""
    var centerLongitude = ""

    fun callApi(lat: String, lon: String) {
        api.searchCode(lon, lat).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                Log.d("lys", "result : ${it.documents?.get(0)?.address_name?:"null"}")
                if ((it.documents?.size ?: -1) > 0) {
                    addressName.value = it.documents?.get(0)?.address_name?:"null"
                }
            }.subscribe({
                Log.d("lys", "success")
            }, {
                Log.e("lys", "fail > ${it.message}")
            })
    }
}