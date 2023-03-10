package com.yun.mysimplenavi.ui.map.find

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.common.constants.GpsConstants
import com.yun.mysimplenavi.common.constants.GpsConstants.Companion.WAY_OFF_ROUTE_MAX_COUNT
import com.yun.mysimplenavi.common.constants.GpsConstants.Distance.WAY_OFF_DISTANCE
import com.yun.mysimplenavi.common.constants.GpsConstants.Distance.WAY_POINT_DISTANCE_CAR
import com.yun.mysimplenavi.common.constants.GpsConstants.Distance.WAY_POINT_DISTANCE_FOOT
import com.yun.mysimplenavi.databinding.FragmentFindMapBinding
import com.yun.mysimplenavi.ui.dialog.ButtonPopup
import com.yun.mysimplenavi.util.PolyUtil.isLocationOnEdge
import com.yun.mysimplenavi.util.PolyUtil.isLocationOnPath
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.*
import java.text.SimpleDateFormat

@AndroidEntryPoint
class FindMapFragment : BaseFragment<FragmentFindMapBinding, FindMapViewModel>() {
    override val viewModel: FindMapViewModel by viewModels()
    override fun getResourceId(): Int = R.layout.fragment_find_map
    override fun setVariable(): Int = BR.find
    override fun isOnBackEvent(): Boolean = true
    override fun onBackEvent() {
        endRouteGuidance(false)
    }

    /**
     * kakao mapview
     */
    private var mMapView: MapView? = null

    /**
     * 현재 위치
     */
    private var locationManager: LocationManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            mMapView = MapView(requireActivity())
            binding.mapView.addView(mMapView)
        } catch (e: Exception) {
            findNavController().popBackStack()
        }


        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        startRouteGuidance()

        /**
         * 로딩 다이알로그 show / hide
         */
        viewModel.isLoading.observe(viewLifecycleOwner) {
            sharedViewModel.isLoading.value = it
        }

        /**
         * 길찾기 데이터로 polyLine 추가
         */
        viewModel.openStreetRoutes.observe(viewLifecycleOwner) {
            if ((it.routes?.size ?: -1) > 0) addPolyLine()
        }
    }

    /**
     * gps cahnged listener
     */
    @SuppressLint("MissingPermission")
    private val locationListener = object : LocationListener {

        override fun onLocationChanged(p0: Location) {

            viewModel.userLatLon[0] = p0.latitude
            viewModel.userLatLon[1] = p0.longitude
            if (viewModel.arrayLatLngRoute.size > 0) {
                locationOnPathCheck()
                locationWayPoint()
            }
//            addMarker(p0.latitude,p0.longitude,"내 위치")
//            Log.d("lys", "${p0.provider})location : ${p0.latitude} ${p0.longitude} ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(p0.time)}")
        }
    }

    /**
     * polyLine 그리는 함수
     */
    private fun addPolyLine() {
        mMapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading

        mMapView!!.setZoomLevel(0, true)
        try {
            val mPolyline = MapPolyline()
            MapPolyline().apply {
                lineColor = Color.argb(255, 255, 51, 0)
                viewModel.openStreetRoutes.value!!.routes!![0].legs!![0].steps!!.forEach {
                    it.intersections?.forEach { its ->
                        addPoint(
                            MapPoint.mapPointWithGeoCoord(
                                its.location[1],
                                its.location[0]
                            )
                        )
                        viewModel.arrayLatLngRoute.add(
                            LatLng(
                                its.location[1],
                                its.location[0]
                            )
                        )
                    }
                    viewModel.arrayLatLngWayPoint.add(
                        LatLng(
                            it.maneuver!!.location[1],
                            it.maneuver.location[0]
                        )
                    )
                    viewModel.arrayLatLngWPTitle.add(
                        wpEventTitle(
                            it.maneuver.type,
                            it.maneuver.modifier
                        )
                    )
                    addMarker(
                        it.maneuver.location[1], it.maneuver.location[0],
                        if (wpEventCheck(it.maneuver.type)) it.maneuver.type else it.maneuver.modifier
                    )
                }

                mMapView!!.addPolyline(this)
                mPolyline.addPoints(this.mapPoints)

            }
        } catch (e: Exception) {
            Log.e("lys", "PreViewMapFragment addPolyLine error : ${e.message}")
            Toast.makeText(requireActivity(), "서버와의 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    /**
     * marker 추가 함수
     */
    private fun addMarker(lat: Double, lon: Double, name: String) {
        val uNowPosition = MapPoint.mapPointWithGeoCoord(lat, lon)
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = uNowPosition
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mMapView!!.addPOIItem(marker)
    }

    /**
     * 맵뷰 객체 초기화
     */
    private fun clearMap() {
        mMapView!!.removeAllPolylines()
        mMapView!!.removeAllPOIItems()
        mMapView!!.removeAllCircles()
    }

    /**
     * 시작 지점, 도착 지점 좌표 데이터 체크
     */
    private fun gpsDataSettingCheck() =
        viewModel.run {
            endLon != null && endLat != null && endName != null && userLatLon[0] != 0.0 && userLatLon[1] != 0.0
        }


    /**
     * 경로 이탈 체크 함수
     */
    private fun locationOnPathCheck() {
        val isRoute = isLocationOnPath(
            LatLng(viewModel.userLatLon[0], viewModel.userLatLon[1]),
            viewModel.arrayLatLngRoute,
            true,
            WAY_OFF_DISTANCE
        )
        if (!isRoute && viewModel.offRoute != -1) {
            mapNotify("경로를 이탈하셨습니다")
            if (WAY_OFF_ROUTE_MAX_COUNT <= viewModel.offRoute) {
                clearMap()
                Log.d("lys", "경로이탈 --> ${isRoute}")
//            speakTTS("경로를 이탈하셨습니다")
//            binding.clear.performClick()
//            viewModel.callMapAPi()
                viewModel.offRoute = -1
                viewModel.openStreetMapNavigation(viewModel.endLat!!, viewModel.endLon!!)
                addMarker(viewModel.endLat!!, viewModel.endLon!!, viewModel.endName!!)
            } else {
                viewModel.offRoute++
            }
        } else {
            viewModel.offRoute = 0
        }
    }

    /**
     * wp 접근 체크 함수
     */
    private fun locationWayPoint() {
        val arrayLatLngVia = ArrayList<LatLng>()
        if (viewModel.arrayLatLngIndex >= viewModel.arrayLatLngWayPoint.size) return
        arrayLatLngVia.add(
            LatLng(
                viewModel.arrayLatLngWayPoint[viewModel.arrayLatLngIndex].latitude,
                viewModel.arrayLatLngWayPoint[viewModel.arrayLatLngIndex].longitude
            )
        )
        val isWayPoint = isLocationOnEdge(
            LatLng(viewModel.userLatLon[0], viewModel.userLatLon[1]),
            arrayLatLngVia,
            true,
            if (viewModel.isCar.value!!) WAY_POINT_DISTANCE_CAR else WAY_POINT_DISTANCE_FOOT
        )
        if (isWayPoint) {
            Log.d(
                "lys",
                "way : ${viewModel.arrayLatLngWayPoint[viewModel.arrayLatLngIndex]} >> ${viewModel.arrayLatLngWPTitle[viewModel.arrayLatLngIndex]}"
            )
//            speakTTS(viewModel.arrayLatLngRouteTitle[viewModel.arrayLatLngIndex])
            mapNotify(viewModel.arrayLatLngWPTitle[viewModel.arrayLatLngIndex])
            viewModel.arrayLatLngIndex++

            if (viewModel.arrayLatLngIndex >= viewModel.arrayLatLngWayPoint.size) {
                endRouteGuidance(true)
            }
        }
    }

    /**
     * 경로 안내 시작
     */
    @SuppressLint("MissingPermission")
    private fun startRouteGuidance() {

        locationManager!!.requestLocationUpdates(
            locationManager!!.getBestProvider(Criteria(), true)!!, 2000, 0.0f, locationListener
        )
//        locationManager!!.requestLocationUpdates(
//            LocationManager.GPS_PROVIDER, 2000, 0.0f, locationListener
//        )
//        locationManager!!.requestLocationUpdates(
//            LocationManager.NETWORK_PROVIDER, 2000, 0.0f, locationListener
//        )
        viewModel.run {
            endName = arguments?.getString("name")
            endLon = arguments?.getString("endLon")?.toDouble()
            endLat = arguments?.getString("endLat")?.toDouble()
            userLatLon[0] = arguments?.getString("startLat")?.toDouble() ?: 0.0
            userLatLon[1] = arguments?.getString("startLon")?.toDouble() ?: 0.0
            isCar.value = arguments?.getBoolean("isCar")

            /**
             * 도착 지점 마커 추가
             * 길찾기 api 호출
             */
            if (gpsDataSettingCheck()) {
                openStreetMapNavigation(endLat!!, endLon!!)
                addMarker(endLat!!, endLon!!, endName!!)
            }
        }
    }

    /**
     * 경로 안내 종료 및 뒤로가기 버튼 이벤트
     */
    private fun endRouteGuidance(isComplete: Boolean) {
        Log.d("lys", "endRouteGuidance")
        clearMap()
        mMapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        if (isComplete) {
            locationManager!!.removeUpdates(locationListener)
            showButtonPopup("안내를 종료합니다.", false)
        } else {
            showButtonPopup("안내를 종료하시겠습니까?", true)
        }

    }

    private fun showButtonPopup(message: String, isTwoButton: Boolean = false) {
        ButtonPopup().apply {
            showPopup(requireActivity(), "알림", message, isTwoButton)
            setDialogListener(object : ButtonPopup.DialogListener {
                override fun onResultClicked(result: Boolean) {
                    if (result) {
                        // 뒤로가기 및 안내 완료
                        findNavController().popBackStack()
                    } else {
                        viewModel.offRoute = -1
                        viewModel.openStreetMapNavigation(viewModel.endLat!!, viewModel.endLon!!)
                        addMarker(viewModel.endLat!!, viewModel.endLon!!, viewModel.endName!!)
                    }
                }
            })
        }
    }

    /**
     * wp 이벤트 텍스트
     */
    private fun wpEventTitle(type: String, modifier: String): String {
        return when (type) {
            GpsConstants.WayPoint.DEPART -> {
                "경로안내를 시작합니다."
            }
            GpsConstants.WayPoint.ARRIVE -> {
                "목적지 주변입니다. 경로 안내를 종료합니다."
            }
            else -> {
                when (modifier) {
                    GpsConstants.WayPoint.LEFT -> {
                        "잠시 후 좌회전입니다."
                    }
                    GpsConstants.WayPoint.SLIGHT_LEFT -> {
                        "잠시 후 10시 방향 좌회전입니다."
                    }
                    GpsConstants.WayPoint.RIGHT -> {
                        "잠시 후 우회전입니다."
                    }
                    GpsConstants.WayPoint.SLIGHT_RIGHT -> {
                        "잠시 후 2시 방향 우회전입니다."
                    }
                    GpsConstants.WayPoint.STRAIGHT -> {
                        "이어서 직진입니다."
                    }
                    GpsConstants.WayPoint.U_TURN -> {
                        "잠시 후 유턴입니다."
                    }
                    else -> ""
                }
            }
        }
    }

    /**
     * wp 이벤트 체크
     */
    private fun wpEventCheck(type: String): Boolean =
        type == GpsConstants.WayPoint.ARRIVE || type == GpsConstants.WayPoint.DEPART

    /**
     * 지도 위 알림 텍스트 노출
     */
    private fun mapNotify(text: String) {
        if (viewModel.notifyComment.value == "") {
            viewModel.notifyComment.value = text
            Handler().postDelayed({
                viewModel.notifyComment.value = ""
            }, 2000)
        }
    }

    /**
     * onDestroy
     * 프래그먼트 종료시 맵뷰 지워야 함 > 에러방지
     */
    override fun onDestroy() {
        Log.d("lys", "FindMapFragment onDestroy")
        binding.mapView.removeView(mMapView)
        locationManager!!.removeUpdates(locationListener)
        super.onDestroy()
    }
}