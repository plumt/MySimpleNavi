package com.yun.mysimplenavi.ui.map.find

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.yun.mysimplenavi.data.model.OpenStreetMapModel
import com.yun.mysimplenavi.data.repository.ApiRepository
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

    /**
     * 통신 중 로딩 프로그레스바 노출
     */
    val isLoading = MutableLiveData(true)

    /**
     * openstreetmap result data
     */
    val openStreetRoutes = MutableLiveData<OpenStreetMapModel.RS>()

    /**
     * wp list
     */
    val arrayLatLngWayPoint = arrayListOf<LatLng>()

    /**
     * wp title
     */
    val arrayLatLngWPTitle = arrayListOf<String>()

    /**
     * polyline list
     */
    val arrayLatLngRoute = arrayListOf<LatLng>()

    /**
     * wp point index
     */
    var arrayLatLngIndex = 0

    /**
     * 경로 이탈 카운트
     */
    var offRoute = 0

    /**
     * 경로 이탈 및 wp 접근 알림
     */
    val notifyComment = MutableLiveData<String>("")

    /**
     * 유저 현재 위치 좌표
     */
    var userLatLon = DoubleArray(2) { 0.0 }

    var endLon: Double? = null
    var endLat: Double? = null
    var endName: String? = null

    /**
     * 길찾기 차 / 걸음
     */
    val isCar = MutableLiveData(true)

    /**
     * openstreetmap api 호출 함수
     * @param endLat 도착지점 위도 Double
     * @param endLon 도착지점 경도 Double
     */
    fun openStreetMapNavigation(endLat: Double, endLon: Double) {
        isLoading.value = true
        val start = "${userLatLon[1]},${userLatLon[0]};"
        val end = "$endLon,$endLat"
        val path = start + end
        Log.d("lys", "path : $path")
        api.openStreetMapNavi(isCar = isCar.value!!, path).observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .flatMap { Observable.just(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                clear()
                Log.d("lys", "result : $it")
                openStreetRoutes.value = it
                offRoute = 0
            }.subscribe({
                isLoading.value = false
                Log.d("lys", "success")
            }, {
                clear()
                isLoading.value = false
                Log.e("lys", "fail : ${it.message}")
            })
    }

    /**
     * 길안내 관련 변수 초기화
     */
    private fun clear(){
        arrayLatLngWayPoint.clear()
        arrayLatLngWPTitle.clear()
        arrayLatLngRoute.clear()
        arrayLatLngIndex = 0
        offRoute = 0
    }
}