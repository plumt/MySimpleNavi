package com.yun.mysimplenavi.ui.map.preview

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
class PreViewMapViewModel @Inject constructor(
    @Named("navi") private val api: ApiRepository
) : ViewModel() {

    val isLoading = MutableLiveData(false)
    val openStreetRoutes = MutableLiveData<OpenStreetMapModel.RS>()

    val duration = MutableLiveData("")
    val distance = MutableLiveData("")

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
                distance.value = Util.formatDistance(it.routes?.get(0)?.distance ?: 0.0)
                val convertDuration =
                    Util.convertSeconds((it.routes?.get(0)?.duration?.toInt() ?: 0))
                duration.value = durationConvert(convertDuration.first, convertDuration.second, convertDuration.third)
            }.subscribe({
                isLoading.value = false
                Log.d("lys", "success")
            }, {
                isLoading.value = false
                Log.e("lys", "fail : ${it.message}")
            })
    }

    private fun durationConvert(hour: Int, minute: Int, second: Int): String {
        var result = ""
        if (hour != 0) result += "${hour}시"
        if (minute != 0) result += "${minute}분"
        if (second != 0) result += "${second}초"
        return result
    }
}