package com.yun.mysimplenavi.ui.map.find

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.Api
import com.yun.mysimplenavi.data.model.OpenStreetMapModel
import com.yun.mysimplenavi.data.repository.ApiRepository
import com.yun.mysimplenavi.util.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FindMapViewModel @Inject constructor(
    @Named("navi") private val api: ApiRepository
) : ViewModel() {

    val isLoading = MutableLiveData(false)

    val openStreetRoutes = MutableLiveData<OpenStreetMapModel.RS>()

    fun openStreetMapNavigation(endLat: Double, endLon: Double) {
        isLoading.value = true
        val start = "127.065782,37.547348;"
        val end = "$endLon,$endLat"
        val path = start + end
        Log.d("lys", "path : $path")
        api.openStreetMapNavi(isCar = true, path).observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                Log.d("lys", "result : $it")
                openStreetRoutes.value = it
            }.subscribe({
                isLoading.value = false
                Log.d("lys", "success")
            }, {
                isLoading.value = false
                Log.e("lys", "fail : ${it.message}")
            })
    }
}