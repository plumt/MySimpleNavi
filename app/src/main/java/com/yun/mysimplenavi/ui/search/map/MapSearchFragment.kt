package com.yun.mysimplenavi.ui.search.map

import android.annotation.SuppressLint
import android.content.Context
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
import com.yun.mysimplenavi.databinding.FragmentMapSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.text.SimpleDateFormat

@AndroidEntryPoint
class MapSearchFragment : BaseFragment<FragmentMapSearchBinding, MapSearchViewModel>() {
    override val viewModel: MapSearchViewModel by viewModels()
    override fun getResourceId(): Int = R.layout.fragment_map_search
    override fun setVariable(): Int = BR.map
    override fun isOnBackEvent(): Boolean = false
    override fun onBackEvent() { }

    private var mMapView: MapView? = null

    /**
     * 현재 위치
     */
    private var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = MapView(requireActivity())
        mMapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        mMapView!!.setShowCurrentLocationMarker(false)
//        mMapView!!.setZoomLevel(2, true)

        binding.mapView.addView(mMapView)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager!!.requestLocationUpdates(
            locationManager!!.getBestProvider(Criteria(), false)!!, 2000, 1.0f, locationListener
        )

        locationManager!!.getLastKnownLocation(locationManager!!.getBestProvider(Criteria(), false)!!)?.run {
            mMapView!!.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 2, true)
        }

        binding.btnFind.setOnClickListener {
            val centerLatLon = mMapView!!.mapCenterPoint
            viewModel.centerLongitude = String.format("%.6f",centerLatLon.mapPointGeoCoord.longitude)
            viewModel.centerLatitude = String.format("%.6f",centerLatLon.mapPointGeoCoord.latitude)
            viewModel.callApi(viewModel.centerLatitude,viewModel.centerLongitude)
        }

        viewModel.addressName.observe(viewLifecycleOwner){
            if(it == "null"){
                findNavController().popBackStack()
            } else if(it != ""){
                if(viewModel.centerLatitude != "" && viewModel.centerLongitude != ""){
                    binding.mapView.removeView(mMapView)
                    navigate(R.id.action_mapSearchFragment_to_preViewMapFragment, Bundle().apply {
                        putString("lon",viewModel.centerLongitude)
                        putString("lat",viewModel.centerLatitude)
                        putString("name",it)
                    }, NavOptions.Builder().setPopUpTo(R.id.mapSearchFragment, true).build())
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    /**
     * gps cahnged listener
     */
    @SuppressLint("MissingPermission")
    private val locationListener = object : LocationListener {

        override fun onLocationChanged(p0: Location) { }
    }

    override fun onDestroy() {
        Log.d("lys","MapFragment onDestroy")
        binding.mapView.removeView(mMapView)
        locationManager!!.removeUpdates(locationListener)
        super.onDestroy()
    }
}