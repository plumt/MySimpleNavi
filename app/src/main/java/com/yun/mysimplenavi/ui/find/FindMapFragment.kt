package com.yun.mysimplenavi.ui.find

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentFindMapBinding
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class FindMapFragment : BaseFragment<FragmentFindMapBinding, FindMapViewModel>() {
    override val viewModel: FindMapViewModel by viewModels()
    override fun getResourceId(): Int = R.layout.fragment_find_map
    override fun setVariable(): Int = BR.findMap

    private var mMapView: MapView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = MapView(requireActivity())

        binding.mapView.addView(mMapView)


        val lon = arguments?.getString("lon")
        val lat = arguments?.getString("lat")
        val name = arguments?.getString("name")

        if(lat != null && lon != null && name != null){
            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat.toDouble(),lon.toDouble()),0,true)
            addMarker(lat.toDouble(), lon.toDouble(), name)
        }

    }

    private fun addMarker(lat: Double, lon: Double, name: String){
        val uNowPosition = MapPoint.mapPointWithGeoCoord(lat, lon)
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = uNowPosition
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mMapView!!.addPOIItem(marker)
    }
}