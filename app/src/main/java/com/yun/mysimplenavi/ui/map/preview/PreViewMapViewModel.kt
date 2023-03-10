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

    /**
     * 통신 중 로딩 프로그레스바 노출
     */
    val isLoading = MutableLiveData(true)

    /**
     * openstreetmap result data
     */
    val openStreetRoutes = MutableLiveData<OpenStreetMapModel.RS>()

    /**
     * 총 소요 시간
     */
    val duration = MutableLiveData("")

    /**
     * 총 이동 거리
     */
    val distance = MutableLiveData("")

    /**
     * 유저 현재 위치 좌표
     */
    var userLatLon = DoubleArray(2) { 0.0 }

    /**
     * 길찾기 차 / 걸음
     */
    val isCar = MutableLiveData(true)

    var lon: String? = null
    var lat: String? = null
    var name: String? = null

    var distanceCheck = 0.0

    /**
     * openstreetmap api 호출 함수
     * @param startLat 시작지점 위도 Double
     * @param startLon 시작지점 경도 Double
     * @param endLat 도착지점 위도 Double
     * @param endLon 도착지점 경도 Double
     */
    fun openStreetMapNavigation(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ) {
        isLoading.value = true
        val start = "${startLon},${startLat};"
        val end = "$endLon,$endLat"
        val path = start + end
        Log.d("lys", "path : $path")
        api.openStreetMapNavi(isCar = isCar.value!!, path).observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                Log.d("lys", "result : $it")
                openStreetRoutes.value = it
                distanceCheck = it.routes?.get(0)?.distance ?: 0.0
                distance.value = Util.formatDistance(it.routes?.get(0)?.distance ?: 0.0)
                val convertDuration =
                    Util.convertSeconds((it.routes?.get(0)?.duration?.toInt() ?: 0))
                duration.value = durationConvert(
                    convertDuration.first,
                    convertDuration.second,
                    convertDuration.third
                )
            }.subscribe({
                isLoading.value = false
                Log.d("lys", "success")
            }, {
                isLoading.value = false
                Log.e("lys", "fail : ${it.message}")
            })
    }

    /**
     * duration(초) > 시 분 초 형태로 변환
     * @param hour 시간 Int
     * @param minute 분 Int
     * @param second 초 Int
     */
    private fun durationConvert(hour: Int, minute: Int, second: Int): String {
        var result = ""
        if (hour != 0) result += "${hour}시"
        if (minute != 0) result += "${minute}분"
        if (second != 0) result += "${second}초"
        return result
    }
}

