package vn.asiantech.way.ui.arrived

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.hypertrack.lib.HyperTrack
import com.hypertrack.lib.HyperTrackMapFragment
import com.hypertrack.lib.MapFragmentCallback
import com.hypertrack.lib.callbacks.HyperTrackCallback
import com.hypertrack.lib.models.ErrorResponse
import com.hypertrack.lib.models.HyperTrackLocation
import com.hypertrack.lib.models.SuccessResponse
import kotlinx.android.synthetic.main.activity_arrived.*
import kotlinx.android.synthetic.main.detail_arrived.*
import kotlinx.android.synthetic.main.show_detail_arrived.*
import vn.asiantech.way.R
import vn.asiantech.way.extension.makeAverageSpeed
import vn.asiantech.way.extension.makeDistance
import vn.asiantech.way.extension.makeDuration
import vn.asiantech.way.extension.toast
import vn.asiantech.way.models.Arrived
import vn.asiantech.way.ui.base.BaseActivity

/**
 *  Copyright © 2017 AsianTech inc.
 *  Created by at-hoavo on 26/09/2017.
 */
internal class ArrivedActivity : BaseActivity() {
    companion object {
        private val TAG = ArrivedActivity::class.toString()
        private const val TYPE_PROGRESS_MAX = 100
        private const val TYPE_CAMERA_ZOOM = 14f
        private const val TYPE_MAP_ZOOM = 16.9f
        private const val TYPE_POLYLINE_WIDTH = 5f
        private const val TYPE_PADDING_BOTTOM = 500
        private const val TYPE_PADDING_TOP = 0
        private const val TYPE_PADDING_RIGHT = 0
        private const val TYPE_PADDING_LEFT = 0
        private const val TYPE_ANCHOR = 0.5f
        private const val TYPE_ORIGIN_DEFAULT_LATITUDE = 16.0751387
        private const val TYPE_ORIGIN_DEFAULT_LONGITUDE = 108.1538494
    }

    private var mGoogleMap: GoogleMap? = null
    private lateinit var mDestinationPosition: LatLng
    private var mArrived = Arrived()
    private var mPoints: MutableList<LatLng> = mutableListOf()
    private var mHypertrackMapFragment: HyperTrackMapFragment? = null
    private var mCurrentLocation: HyperTrackLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrived)
        setArrivedDetail()
        configFirst()
        checkForLocationSetting()
        mHypertrackMapFragment = fragmentHypertrackMap as? HyperTrackMapFragment
        mHypertrackMapFragment?.setMapFragmentCallback(object : MapFragmentCallback() {
            override fun onMapReadyCallback(hyperTrackMapFragment: HyperTrackMapFragment?,
                                            map: GoogleMap?) {
                mGoogleMap = map
                setOnMapReady()
            }

            override fun onMapLoadedCallback(hyperTrackMapFragment: HyperTrackMapFragment?,
                                             map: GoogleMap?) {
                getCurrentLocation()
            }
        })

        btnShowSummary.setOnClickListener {
            showDialog()
        }

        imgArrowRight.setOnClickListener {
            showDetailTracking()
        }

        imgArrowDown.setOnClickListener {
            imgArrowDown.visibility = View.GONE
            imgArrowRight.visibility = View.VISIBLE
            cardViewDetailArrived.visibility = View.GONE
        }

        imgBtnArrowBack.setOnClickListener {
            //TODO("Back to previous screen")
        }

        imgBtnResetPosition.setOnClickListener {
            mCurrentLocation?.latLng?.let {
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, TYPE_CAMERA_ZOOM))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForLocationSetting()

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                toast(resources.getString(R.string.arrived_turn_location_permission))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode != Activity.RESULT_OK) {
                toast(resources.getString(R.string.arrived_turn_location_service))
            }
            checkForLocationSetting()
        }
    }

    private fun setArrivedDetail() {
        //TODO("Set for mArrived")
//        mArrived.time = 0
//        mArrived.distance = 0.0
//        mArrived.averageSpeed = 0.0
    }

    private fun configFirst() {
        //TODO("Set for mPoint base on mArrived")
//        mArrived.segments.forEach {
//            mPoints.add(LatLng(it.startLocation.latitude, it.startLocation.longitude))
//            mPoints.add(LatLng(it.endLocation.latitude, it.endLocation.longitude))
//        }
        mPoints.add(LatLng(TYPE_ORIGIN_DEFAULT_LATITUDE, TYPE_ORIGIN_DEFAULT_LONGITUDE))
    }

    private fun setOnMapReady() {
        mGoogleMap?.setPadding(TYPE_PADDING_LEFT, TYPE_PADDING_TOP, TYPE_PADDING_RIGHT,
                TYPE_PADDING_BOTTOM)
        mGoogleMap?.setMaxZoomPreference(TYPE_MAP_ZOOM)
        mGoogleMap?.uiSettings?.isMapToolbarEnabled = false
        mGoogleMap?.uiSettings?.isCompassEnabled = false
        mGoogleMap?.addMarker(setMarkerOption(R.drawable.ic_ht_source_place_marker, mPoints[0]))
    }

    private fun getCurrentLocation() {
        HyperTrack.getCurrentLocation(object : HyperTrackCallback() {
            override fun onSuccess(response: SuccessResponse) {
                Log.d(TAG, "onSuccess: Current Location Received")
                mCurrentLocation = HyperTrackLocation(response.responseObject as? Location)
                mCurrentLocation?.latLng?.let {
                    mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it,
                            TYPE_CAMERA_ZOOM))
                    mGoogleMap?.addMarker(setMarkerOption(R.drawable.ic_ht_expected_place_marker,
                            it))
                    mPoints.add(it)
                    mDestinationPosition = it
                    if (checkDestination()) {
                        arrivedFinish()
                    }
                }
            }

            override fun onError(errorResponse: ErrorResponse) {
                Log.d(TAG, "onError: Current Location Receiving error")
                Log.d(TAG, "onError: " + errorResponse.errorMessage)
            }
        })
    }

    private fun setMarkerOption(resource: Int, position: LatLng): MarkerOptions =
            MarkerOptions().position(position).
                    icon(BitmapDescriptorFactory.fromResource(resource))
                    .anchor(TYPE_ANCHOR, TYPE_ANCHOR)

    private fun checkDestination(): Boolean = mCurrentLocation?.latLng?.latitude ==
            mDestinationPosition.latitude
            && mCurrentLocation?.latLng?.longitude == mDestinationPosition.longitude

    private fun arrivedFinish() {
        btnShowSummary.visibility = View.VISIBLE
        progressBarCircular.progress = TYPE_PROGRESS_MAX
        tvTimeTotalArrived.text = mArrived.time.makeDuration(this)
        tvDistanceArrived.text = mArrived.distance.makeDistance(this)
        tvAverageSpeed.text = mArrived.averageSpeed.makeAverageSpeed(this)
        drawLine()
    }

    private fun drawLine() {
        val polyPointOption = PolylineOptions()
        polyPointOption.addAll(mPoints)
        polyPointOption.color(Color.BLACK)
        polyPointOption.width(TYPE_POLYLINE_WIDTH)
        mGoogleMap?.addPolyline(polyPointOption)
    }

    private fun showDialog() {
        val dialog = DialogShowArrivedInformation.newInstance(mArrived.time, mArrived.distance,
                mArrived.averageSpeed)
        val fragmentManager = supportFragmentManager as? FragmentManager
        dialog.show(fragmentManager, resources.getString(R.string.arrived_dialog_tag))
    }

    private fun showDetailTracking() {
        imgArrowRight.visibility = View.GONE
        imgArrowDown.visibility = View.VISIBLE
        cardViewDetailArrived.visibility = View.VISIBLE
        tvAverage.visibility = View.GONE
        tvTraveled.visibility = View.GONE
        tvElapsed.visibility = View.GONE
        tvTimeTotal.text = mArrived.time.makeDuration(this)
        tvDistance.text = mArrived.distance.makeDistance(this)
        tvAverageSpeed.text = mArrived.averageSpeed.makeAverageSpeed(this)
    }

    private fun checkForLocationSetting() {
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this)
            return
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this)
        }
    }
}
