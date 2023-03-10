package com.yun.mysimplenavi.ui.map.preview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentPreviewMapBinding
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.*

@AndroidEntryPoint
class PreViewMapFragment : BaseFragment<FragmentPreviewMapBinding, PreViewMapViewModel>() {
    override val viewModel: PreViewMapViewModel by viewModels()
    override fun getResourceId(): Int = R.layout.fragment_preview_map
    override fun setVariable(): Int = BR.preview
    override fun isOnBackEvent(): Boolean = false
    override fun onBackEvent() { }

    private var mMapView: MapView? = null

    /**
     * 현재 위치
     */
    private var locationManager: LocationManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{
            mMapView = MapView(requireActivity())
            mMapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
            mMapView!!.setShowCurrentLocationMarker(false)
            binding.mapView.addView(mMapView)
        } catch (e: Exception){
            findNavController().popBackStack()
        }


        viewModel.lon = arguments?.getString("lon")
        viewModel.lat = arguments?.getString("lat")
        viewModel.name = arguments?.getString("name")

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager



        binding.btnFind.setOnClickListener {
            binding.mapView.removeView(mMapView)
            navigate(R.id.action_preViewMapFragment_to_findMapFragment, Bundle().apply {
                putString("endLon", viewModel.lon)
                putString("endLat", viewModel.lat)
                putString("startLon",viewModel.userLatLon[1].toString())
                putString("startLat",viewModel.userLatLon[0].toString())
                putString("name", viewModel.name)
                putBoolean("isCar", viewModel.isCar.value!!)
            }, NavOptions.Builder().setPopUpTo(R.id.preViewMapFragment, true).build())
        }

        binding.btnCar.setOnClickListener {
            viewModel.isCar.value = true
        }

        binding.btnFoot.setOnClickListener {
            viewModel.isCar.value = false
        }



        viewModel.isLoading.observe(viewLifecycleOwner) {
            sharedViewModel.isLoading.value = it
        }

        viewModel.openStreetRoutes.observe(viewLifecycleOwner) {
            if ((it.routes?.size ?: -1) > 0) {
                addPolyLine()
            }
        }

//        locationManager!!.getLastKnownLocation(
//            locationManager!!.getBestProvider(
//                Criteria(),
//                false
//            )!!
//        )?.run {
//            if (lat != null && lon != null && name != null) {
//                viewModel.userLatLon[0] = latitude
//                viewModel.userLatLon[1] = longitude
//                viewModel.openStreetMapNavigation(
//                    latitude,
//                    longitude,
//                    lat.toDouble(),
//                    lon.toDouble()
//                )
////            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat.toDouble(),lon.toDouble()),0,true)
//                addMarker(latitude, longitude, "start")
//                addMarker(lat.toDouble(), lon.toDouble(), name)
//            }
//        }
        viewModel.isCar.observe(viewLifecycleOwner){
            callApi()
        }
    }

    @SuppressLint("MissingPermission")
    private fun callApi(){
        clearMap()
//        locationManager!!.requestLocationUpdates(
//            locationManager!!.getBestProvider(Criteria(), false)!!, 2000, 1.0f, locationListener
//        )

        locationManager!!.getLastKnownLocation(
            locationManager!!.getBestProvider(
                Criteria(),
                false
            )!!
        )?.run {
            if (viewModel.lat != null && viewModel.lon != null && viewModel.name != null) {
                viewModel.userLatLon[0] = latitude
                viewModel.userLatLon[1] = longitude
                viewModel.openStreetMapNavigation(
                    latitude,
                    longitude,
                    viewModel.lat!!.toDouble(),
                    viewModel.lon!!.toDouble()
                )
//            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat.toDouble(),lon.toDouble()),0,true)
                addMarker(latitude, longitude, "start")
                addMarker(viewModel.lat!!.toDouble(), viewModel.lon!!.toDouble(), viewModel.name!!)
            }
        }
    }

    /**
     * gps cahnged listener
     */
//    @SuppressLint("MissingPermission")
//    private val locationListener = object : LocationListener {
//
//        override fun onLocationChanged(p0: Location) {}
//    }

    /**
     * polyLine 그리는 함수
     */
    private fun addPolyLine() {
        val mPolyline = MapPolyline()
        MapPolyline().apply {
            lineColor = Color.argb(255, 255, 51, 0)
//            viewModel.openStreetRoutes.value!!.routes!![0].legs!![0].steps!!.forEach {
//                addPoint(
//                    MapPoint.mapPointWithGeoCoord(
//                        it.maneuver!!.location[1],
//                        it.maneuver!!.location[0]
//                    )
//                )
//            }
            viewModel.openStreetRoutes.value!!.routes!![0].legs!![0].steps!!.forEach {
                it.intersections?.forEach { its ->
                    addPoint(
                        MapPoint.mapPointWithGeoCoord(
                            its.location[1],
                            its.location[0]
                        )
                    )
                }
            }

            mMapView!!.addPolyline(this)
            mPolyline.addPoints(this.mapPoints)
        }
        mMapView!!.moveCamera(
            CameraUpdateFactory.newMapPointBounds(
                MapPointBounds(mPolyline.mapPoints),
                100
            )
        )
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
     * onDestroy
     * 프래그먼트 종료시 맵뷰 지워야 함 > 에러방지
     */
    override fun onDestroy() {
        Log.d("lys", "PreViewMapFragment onDestroy")
        binding.mapView.removeView(mMapView)
        super.onDestroy()
    }
}