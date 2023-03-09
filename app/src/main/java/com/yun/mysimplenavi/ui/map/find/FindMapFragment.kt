package com.yun.mysimplenavi.ui.map.find

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.LatLng
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentFindMapBinding
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

    /**
     * kakao mapview
     */
    private var mMapView: MapView? = null

    /**
     * 현재 위치 라이브러리
     */
    private var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = MapView(requireActivity())

        binding.mapView.addView(mMapView)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            locationManager!!.getBestProvider(Criteria(), false)!!, 2000, 1.0f
        ) {
            onLocationChanged(it)
        }

        val lon = arguments?.getString("lon")
        val lat = arguments?.getString("lat")
        val name = arguments?.getString("name")

        if (lat != null && lon != null && name != null) {
            viewModel.openStreetMapNavigation(lat.toDouble(), lon.toDouble())
            addMarker(lat.toDouble(), lon.toDouble(), name)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            sharedViewModel.isLoading.value = it
        }

        viewModel.openStreetRoutes.observe(viewLifecycleOwner) {
            if ((it.routes?.size ?: -1) > 0) {
                addPolyLine()
            }
        }

    }

    /**
     * gps cahnged listener
     */
    @SuppressLint("MissingPermission")
    fun onLocationChanged(p0: Location) {

//        if(viewModel.latitude == 0.0){
//            viewModel.callMapAPi(p0.latitude,p0.longitude)
//        }
        viewModel.userLatLon[0] = p0.latitude
        viewModel.userLatLon[1] = p0.longitude
        if (viewModel.arrayLatLngRoute.size > 0) {
            locationOnPathCheck()
            locationWayPoint()
        }
//        Log.d(
//            "lys",
//            "location : ${p0.latitude} ${p0.longitude} ${
//                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(p0.time)
//            }"
//        )
    }

    /**
     * polyLine 그리는 함수
     */
    private fun addPolyLine() {
        mMapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading

        mMapView!!.setZoomLevel(0, true)
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
                    if(it.maneuver.type == "depart") "안내를 시작합니다"
                    else if(it.maneuver.type == "arrive") "목적지 주변입니다"
                    else if(it.maneuver.modifier.contains("right")) "잠시 후 우회전입니다"
                    else if(it.maneuver.modifier.contains("left")) "잠시 후 좌회전입니다"
                    else if(it.maneuver.modifier.contains("uturn")) "잠시 후 유턴입니다"
                    else ""
                )
                addMarker(it.maneuver.location[1],it.maneuver.location[0],
                if(it.maneuver.type == "depart" || it.maneuver.type == "arrive") it.maneuver.type else it.maneuver.modifier)
            }

            mMapView!!.addPolyline(this)
            mPolyline.addPoints(this.mapPoints)
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
     * 경로 이탈 체크 함수
     */
    private fun locationOnPathCheck() {
        val isRoute = isLocationOnPath(
            LatLng(viewModel.userLatLon[0], viewModel.userLatLon[1]),
            viewModel.arrayLatLngRoute,
            true,
            50.0
        )
        if (!isRoute) {
        Log.d("lys", "경로이탈 --> ${isRoute}")
            mapNotify("경로를 이탈하셨습니다")
//            speakTTS("경로를 이탈하셨습니다")
//            binding.clear.performClick()
//            viewModel.callMapAPi()
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
            30.0
        )
        if (isWayPoint) {
            Log.d("lys", "way : ${viewModel.arrayLatLngWayPoint[viewModel.arrayLatLngIndex]} >> ${viewModel.arrayLatLngWPTitle[viewModel.arrayLatLngIndex]}")
//            speakTTS(viewModel.arrayLatLngRouteTitle[viewModel.arrayLatLngIndex])
            mapNotify(viewModel.arrayLatLngWPTitle[viewModel.arrayLatLngIndex])
            viewModel.arrayLatLngIndex++
        }
    }

    /**
     * 지도 위 알림 텍스트 노출
     */
    private fun mapNotify(text: String){
        if(viewModel.notifyComment.value == ""){
            viewModel.notifyComment.value = text
            Handler().postDelayed({
                viewModel.notifyComment.value = ""
            }, 2000)
        }
    }

    /**
     * onStop
     * 프래그먼트 종료시 맵뷰 지워야 함 > 에러방지
     */
    override fun onStop() {
        binding.mapView.removeView(mMapView)
        super.onStop()
    }
}