package com.yun.mysimplenavi.ui.map.find

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentFindMapBinding
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.*

@AndroidEntryPoint
class FindMapFragment : BaseFragment<FragmentFindMapBinding, FindMapViewModel>() {
    override val viewModel: FindMapViewModel by viewModels()
    override fun getResourceId(): Int = R.layout.fragment_find_map
    override fun setVariable(): Int = BR.find

    private var mMapView: MapView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = MapView(requireActivity())

        binding.mapView.addView(mMapView)

        val lon = arguments?.getString("lon")
        val lat = arguments?.getString("lat")
        val name = arguments?.getString("name")

        if (lat != null && lon != null && name != null) {
            viewModel.openStreetMapNavigation(lat.toDouble(), lon.toDouble())
//            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat.toDouble(),lon.toDouble()),0,true)
//            addMarker(37.5473480673073, 127.065782814398, "start")
            addMarker(lat.toDouble(), lon.toDouble(), name)
        }

        viewModel.isLoading.observe(viewLifecycleOwner){
            sharedViewModel.isLoading.value = it
        }

        viewModel.openStreetRoutes.observe(viewLifecycleOwner) {
            if ((it.routes?.size ?: -1) > 0) {
                addPolyLine()
            }
        }

    }

    private fun addPolyLine() {
//        mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(viewModel.latitude,viewModel.longitude),-100,true)
        mMapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
        mMapView!!.setZoomLevel(-50,true)
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
//        mMapView!!.moveCamera(
//            CameraUpdateFactory.newMapPointBounds(
//                MapPointBounds(mPolyline.mapPoints),
//                100
//            )
//        )
    }

    private fun addMarker(lat: Double, lon: Double, name: String) {
        val uNowPosition = MapPoint.mapPointWithGeoCoord(lat, lon)
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = uNowPosition
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mMapView!!.addPOIItem(marker)
    }

    override fun onStop() {
        binding.mapView.removeView(mMapView)
        super.onStop()
    }

}