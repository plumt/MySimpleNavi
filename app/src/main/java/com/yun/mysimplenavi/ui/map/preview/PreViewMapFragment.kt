package com.yun.mysimplenavi.ui.map.preview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
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

    private var mMapView: MapView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = MapView(requireActivity())

        binding.btnFind.setOnClickListener {
            navigate(R.id.action_preViewMapFragment_to_findMapFragment)
        }

        binding.mapView.addView(mMapView)

        viewModel.openStreetRoutes.observe(viewLifecycleOwner){
            if((it.routes?.size ?: -1) > 0){
                addPolyLine()
            }
        }


        val lon = arguments?.getString("lon")
        val lat = arguments?.getString("lat")
        val name = arguments?.getString("name")

        if (lat != null && lon != null && name != null) {
            viewModel.openStreetMapNavigation(lat.toDouble(), lon.toDouble())
//            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat.toDouble(),lon.toDouble()),0,true)
            addMarker(37.5473480673073,127.065782814398,"start")
            addMarker(lat.toDouble(), lon.toDouble(), name)
        }

    }

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

    private fun addMarker(lat: Double, lon: Double, name: String) {
        val uNowPosition = MapPoint.mapPointWithGeoCoord(lat, lon)
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = uNowPosition
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mMapView!!.addPOIItem(marker)
    }
}