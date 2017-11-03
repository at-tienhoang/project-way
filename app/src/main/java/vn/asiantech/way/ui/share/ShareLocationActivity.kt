package vn.asiantech.way.ui.share

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.hypertrack.lib.HyperTrack
import com.hypertrack.lib.HyperTrackUtils
import com.hypertrack.lib.callbacks.HyperTrackCallback
import com.hypertrack.lib.internal.common.models.VehicleType
import com.hypertrack.lib.internal.consumer.view.MarkerAnimation
import com.hypertrack.lib.models.ErrorResponse
import com.hypertrack.lib.models.HyperTrackLocation
import com.hypertrack.lib.models.SuccessResponse
import kotlinx.android.synthetic.main.activity_share_location.*
import kotlinx.android.synthetic.main.bottom_button_card_view.*
import kotlinx.android.synthetic.main.bottom_button_card_view.view.*
import vn.asiantech.way.R
import vn.asiantech.way.data.model.search.MyLocation
import vn.asiantech.way.ui.base.BaseActivity
import vn.asiantech.way.ui.confirm.LocationNameAsyncTask
import vn.asiantech.way.ui.custom.BottomButtonCard
import vn.asiantech.way.ui.custom.RadiusAnimation
import vn.asiantech.way.ui.search.SearchLocationActivity
import vn.asiantech.way.utils.AppConstants
import vn.asiantech.way.utils.LocationUtil
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Copyright © AsianTech Co., Ltd
 * Created by toan on 27/09/2017.
 */
class ShareLocationActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        private const val INTERVAL = 500L
        private const val ZOOM_SIZE = 16f
        private const val TIME_CONVERT = 3.6
        private const val ONE_THOUSAND = 1000L
        private const val RADIUS = 360.0
        private const val STEP_ETA = 3
    }

    private var mCurrentMarker: Marker? = null
    private var mIsStopTracking = false
    private var mLocationUpdates: MutableList<LatLng>? = null
    private var mLocations: MutableList<vn.asiantech.way.data.model.Location>? = null
    private var mCurrentLocation: HyperTrackLocation? = null
    private lateinit var mHandlerTracking: Handler
    private var mRunnable: Runnable? = null
    private var mCountTimer = ONE_THOUSAND
    private lateinit var mDestinationLatLng: LatLng
    private var mDistanceTravel = 0f
    private var mAverageSpeed = 0f
    private var mEtaUpdate = 0.0f
    private var mEtaMaximum = 0.0f
    private var mEtaSpeed = 0.0f
    private var mCount = 0

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private var mDestinationName: String? = null
    private var mLatLng: LatLng? = null
    private var mMyLocation: MyLocation? = null
    private var mAction: String? = null
    private var mIsConfirm: Boolean = false
    private var mMarker: Marker? = null
    private var mGroundOverlay: GroundOverlay? = null
    private lateinit var mLocationAsyncTask: AsyncTask<LatLng, Void, String>

    private val mCurrentBatteryReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(p0: Context?, p1: Intent?) {
            val level = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            tvBattery.text = "${level.toString()}%"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_location)
        if (intent.extras != null) {
            mMyLocation = intent.getParcelableExtra(AppConstants.KEY_LOCATION)
            mAction = intent.getStringExtra(AppConstants.KEY_CONFIRM)
        }
        registerReceiver(mCurrentBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        mLocationUpdates = ArrayList()
        mLocations = ArrayList()
        initMap()
        initializeUIViews()
        initGoogleApiClient()
        initLocationRequest()
        // handler tracking progress here
        handlerProgressTracking()
        onClickButtonSearchLocation()
        initBottomButtonCard(true, mAction)
    }


    private fun initMap() {
        mMapFragment = supportFragmentManager.findFragmentById(R.id.fgMap) as SupportMapFragment
        mMapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map != null) {
            mGoogleMap = map
            mGoogleMap.setOnCameraIdleListener(this)
        }
        HyperTrack.getCurrentLocation(object : HyperTrackCallback() {
            override fun onSuccess(p0: SuccessResponse) {
                mCurrentLocation = HyperTrackLocation((p0.responseObject) as Location?)
                if (mCurrentLocation != null) {
                    updateUI(mCurrentLocation!!)
                }
            }

            override fun onError(p0: ErrorResponse) {
            }
        })
        val currentLocation = LocationUtil(this).getCurrentLocation()
        if (currentLocation != null) {
            drawCurrentMaker(currentLocation)
        }
        mGoogleMap.setOnCameraIdleListener(this)
        val lat = mMyLocation?.geometry?.location?.lat
        val lng = mMyLocation?.geometry?.location?.lng
        if (lat != null && lng != null) {
            mLatLng = LatLng(lat, lng)
            addDestinationMarker(mLatLng)
        }
        if (mAction == AppConstants.KEY_CURRENT_LOCATION) {
            mLatLng = currentLocation?.latitude?.let { LatLng(it, currentLocation.longitude) }
            addDestinationMarker(mLatLng)
            getLocationName(mLatLng)
            mIsConfirm = true
        }
        mDestinationLatLng = LatLng(16.09175, 108.23747)
    }

    override fun onLocationChanged(location: Location) {
        if (mMarker != null && mGroundOverlay != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            mMarker?.position = latLng
            mGroundOverlay?.position = latLng
        }
    }

    override fun onCameraIdle() {
        mLatLng = mGoogleMap.cameraPosition?.target
        if (!mIsConfirm) {
            if (mMyLocation?.geometry?.location == null) {
                mLocationAsyncTask = LocationNameAsyncTask(WeakReference(this)).execute(mLatLng)
            } else {
                val lat = mMyLocation?.geometry?.location?.lat
                val lng = mMyLocation?.geometry?.location?.lng
                if (lat != null && lng != null) {
                    mLatLng = LatLng(lat, lng)
                    mLocationAsyncTask = LocationNameAsyncTask(WeakReference(this)).execute(mLatLng)
                }
            }
        }
    }

    private fun initializeUIViews() {
        bottomButtonCard?.buttonListener = object : BottomButtonCard.ButtonListener {
            override fun onStopButtonClick() {
                if (rippleTrackingToggle.tag == "stop") {
                    mIsStopTracking = true
                    mHandlerTracking.removeCallbacks(mRunnable)
                } else if (rippleTrackingToggle.tag == "summary") {
                }
            }

            override fun onShareButtonClick() {
                rlBottomCard.visibility = View.VISIBLE
            }

            override fun onCallButtonClick() {
                // No-op
            }

            override fun onCloseButtonClick() {
                rlBottomCard.visibility = View.GONE
            }

            override fun onActionButtonClick() {
                when (mAction) {
                    AppConstants.KEY_CONFIRM -> {
                        mAction = AppConstants.KEY_SHARING
                        initBottomButtonCard(true, mAction)
                        addDestinationMarker(mLatLng)
                        mIsConfirm = true
                    }
                    AppConstants.KEY_SHARING, AppConstants.KEY_CURRENT_LOCATION -> {
                        mAction = AppConstants.KEY_START_SHARING
                        initBottomButtonCard(true, mAction)
                    }
                    else -> shareLocation()
                }
            }

            override fun onCopyButtonClick() {
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                        ClipData.newPlainText("tracking_url", bottomButtonCard.tvURL.text)
            }

        }
    }

    private fun initBottomButtonCard(show: Boolean, action: String?) {
        when (action) {
            AppConstants.KEY_CONFIRM -> {
                bottomButtonCard?.hideCloseButton()
                bottomButtonCard?.hideTvTitle()
                bottomButtonCard?.setDescriptionText(getString(R.string.confirm_move_map))
                bottomButtonCard?.setShareButtonText(getString(R.string.confirm_location))
                bottomButtonCard?.showActionButton()
            }
            AppConstants.KEY_SHARING, AppConstants.KEY_CURRENT_LOCATION -> {
                bottomButtonCard?.hideCloseButton()
                bottomButtonCard?.hideTvDescription()
                bottomButtonCard?.setTitleText(getString(R.string.share_textview_text_look_good))
                bottomButtonCard?.setShareButtonText(getString(R.string.share_textview_text_start_sharing))
                bottomButtonCard?.showActionButton()
            }
            else -> {
                bottomButtonCard?.showCloseButton()
                bottomButtonCard?.actionType = BottomButtonCard.ActionType.SHARE_TRACKING_URL
                bottomButtonCard?.showTrackingURLLayout()
                bottomButtonCard?.setTitleText(getString(R.string.bottom_button_card_title_text))
                bottomButtonCard?.setDescriptionText(getString(R.string.bottom_button_card_description_text))
                bottomButtonCard?.setShareButtonText(getString(R.string.share_textview_text_start_sharing))
                bottomButtonCard?.showActionButton()
                bottomButtonCard?.showTitle()
            }
        }
        if (show) {
            bottomButtonCard?.showBottomCardLayout()
        }
    }

    private fun shareLocation() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        val message = "My Location is ${bottomButtonCard.tvURL.text}"
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message)
        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), 200)
    }

    /**
     * Set destination name
     */
    fun setDestinationName(name: String) {
        mDestinationName = name
    }

    private fun onClickButtonSearchLocation() {
        rlSearchLocation.setOnClickListener {
            startActivity(Intent(this, SearchLocationActivity::class.java))
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun addDestinationMarker(latLng: LatLng?) {
        if (latLng != null) {
            mGoogleMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_expected_place_marker))
                    .title(mDestinationName)
                    .anchor(AppConstants.KEY_DEFAULT_ANCHOR, AppConstants.KEY_DEFAULT_ANCHOR))
                    ?.showInfoWindow()
            imgPickLocation.visibility = View.INVISIBLE
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    private fun drawCurrentMaker(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        mGoogleMap.clear()
        mMarker = mGoogleMap.addMarker(MarkerOptions()
                .position(currentLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_point))
                .title(getString(R.string.current_location))
                .anchor(0.5f, 0.5f))
        addPulseRing(currentLocation)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
    }

    private fun addPulseRing(latLng: LatLng) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setSize(AppConstants.KEY_DRAWABLE_SIZE, AppConstants.KEY_DRAWABLE_SIZE)
        drawable.setColor(ContextCompat.getColor(this, R.color.pulse_color))

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val groundOverlay = mGoogleMap.addGroundOverlay(GroundOverlayOptions()
                .position(latLng, AppConstants.KEY_GROUND_OVERLAY_POSITION)
                .image(BitmapDescriptorFactory.fromBitmap(bitmap)))
        val groundAnimation = RadiusAnimation(groundOverlay)
        groundAnimation.repeatCount = Animation.INFINITE
        groundAnimation.duration = AppConstants.KEY_GR_ANIMATION_DUR
        mMapFragment.view?.startAnimation(groundAnimation)
    }

    private fun getLocationName(latLng: LatLng?) {
        if (latLng != null) {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address> = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                tvLocation.text = address.getAddressLine(0)
                mDestinationName = if (!address.subThoroughfare.isNullOrEmpty()) {
                    address.subThoroughfare.plus(" ").plus(address.thoroughfare)
                } else {
                    address.thoroughfare
                }
            } else {
                tvLocation.text = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mGoogleApiClient.connect()
    }

    override fun onPause() {
        super.onPause()
        mGoogleApiClient.disconnect()
        if (mAction == AppConstants.KEY_CONFIRM && !mLocationAsyncTask.isCancelled) {
            mLocationAsyncTask.cancel(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mAction == AppConstants.KEY_CONFIRM && !mLocationAsyncTask.isCancelled) {
            mLocationAsyncTask.cancel(true)
        }
    }

    private fun initGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun initLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = ShareLocationActivity.INTERVAL
        mLocationRequest.fastestInterval = ShareLocationActivity.INTERVAL
    }

    override fun onConnected(p0: Bundle?) {
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this)
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this)
        }

        if (HyperTrackUtils.isInternetConnected(this)) {
            if (HyperTrackUtils.isLocationEnabled(this)) {
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        //No op
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, SearchLocationActivity::class.java))
        this.finish()
    }

    private fun handlerProgressTracking() {
        mHandlerTracking = Handler()
        mRunnable = Runnable {
            if (mLocationUpdates != null) {
                Log.d("zxc", "" + (mLocationUpdates == null))
                if (mLocationUpdates!!.size > 0
                        && (mLocationUpdates!![mLocationUpdates!!.size - 1] == mDestinationLatLng)) {
                    mEtaUpdate = 0f
                    mAverageSpeed = 0f
                    mCurrentMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location))
                    mCurrentMarker?.setAnchor(0.5f, 0.5f)
                    updateCurrentTimeView()
                    Toast.makeText(this, "DONE!", Toast.LENGTH_SHORT).show()
                    return@Runnable
                }
                requestLocation()
                requestEta()
                mCountTimer += ONE_THOUSAND
                handlerProgressTracking()
            }
        }
        mHandlerTracking.postDelayed(mRunnable, ONE_THOUSAND)
    }

    private fun requestLocation() {
        HyperTrack.getCurrentLocation(object : HyperTrackCallback() {
            override fun onSuccess(p0: SuccessResponse) {
                val hyperTrackLocation = HyperTrackLocation((p0.responseObject) as Location?)
                updateUI(hyperTrackLocation)
            }

            override fun onError(p0: ErrorResponse) {
                Log.d("at-dinhvo", "ErrorResponse: " + p0.errorMessage)
            }
        })
    }

    private fun requestEta() {
        HyperTrack.getETA(mDestinationLatLng, VehicleType.MOTORCYCLE, object : HyperTrackCallback() {
            override fun onSuccess(p0: SuccessResponse) {
                mEtaUpdate = (p0.responseObject as Double?)!!.toFloat()
                if (mCount < 1) {
                    mEtaMaximum = mEtaUpdate
                }
                mCount++
            }

            override fun onError(p0: ErrorResponse) {
                Log.d("at-dinhvo", "ETA: ErrorResponse " + p0.errorMessage)
            }
        })
    }

//    private fun getLocationName(latLng: LatLng?) {
//        val geoCoder = Geocoder(context, Locale.getDefault())
//        val addresses: List<Address> = geoCoder.getFromLocation(latLng!!.latitude, latLng.longitude, 1)
//        if (addresses.isNotEmpty()) {
//            val address: Address = addresses[0]
//            tvDestination.text = address.getAddressLine(0)
//        } else {
//            tvDestination.text = null
//        }
//    }

    private fun updateCurrentTimeView() {
        tvSpeed.text = String.format("%.2f", mAverageSpeed).plus(" km/h")
        tvTime.text = resources.getString(R.string.eta).plus(getEtaTime(mEtaUpdate))
        circleProgressBar.progress = 100
    }

    private fun updateUI(hyperTrackLocation: HyperTrackLocation) {
        val latLng = hyperTrackLocation.geoJSONLocation.latLng
        if (mLocationUpdates != null) {
            if (latLng != null) {
                mLocationUpdates?.add(latLng)
            }
            if (mLocationUpdates!!.size > 1) {
                mDistanceTravel += getDistancePerSecond(mLocationUpdates!![mLocationUpdates!!.size - 2]
                        , mLocationUpdates!![mLocationUpdates!!.size - 1])
            }
            updateView(hyperTrackLocation)
            updateMapView(latLng)
        }
    }

    private fun updateView(hyperTrackLocation: HyperTrackLocation) {
        if (!mIsStopTracking) {
            tvActionStatus.text = resources.getString(R.string.leaving)
        }
        tvTime.text = resources.getString(R.string.eta).plus(getEtaTime(mEtaUpdate))
        if (hyperTrackLocation.speed != null) {
            tvDistance.text = resources.getString(R.string.open_parentheses)
                    .plus(String.format(" %.2f", (mEtaMaximum * hyperTrackLocation.speed) / ONE_THOUSAND))
                    .plus(resources.getString(R.string.close_parentheses_distance))
        }
        tvSpeed.text = String.format("%.2f", mAverageSpeed).plus(" km/h")
        tvElapsedTime.text = formatInterval(mCountTimer)
        tvDistanceTravelled.text = String.format("%.2f", mDistanceTravel).plus(" km")
        if (mEtaMaximum - mEtaUpdate > STEP_ETA) {
            circleProgressBar.progress = (mEtaMaximum - (mEtaUpdate / mEtaMaximum) * 100).toInt()
        }
    }

    private fun getEtaTime(eta: Float): String = when {
        eta >= 3600 -> String.format(" %02d", (eta / 3600).toInt()).plus(" hour")
        eta < 60 -> String.format(" %02d", eta.toInt()).plus(" sec")
        else -> String.format(" %02d", (eta / 60).toInt()).plus(" min")
    }

    private fun formatInterval(millis: Long): String = String.format("%02d:%02d:%02d"
            , TimeUnit.MILLISECONDS.toHours(millis)
            , TimeUnit.MILLISECONDS.toMinutes(millis)
            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
            , TimeUnit.MILLISECONDS.toSeconds(millis)
            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))

    private fun radians(n: Double) = n * (Math.PI / (RADIUS / 2))

    private fun degrees(n: Double) = n * ((RADIUS / 2) / Math.PI)

    private fun getAngleMarker(startLatLong: LatLng, endLatLong: LatLng): Double {
        val startLat = radians(startLatLong.latitude)
        val startLong = radians(startLatLong.longitude)
        val endLat = radians(endLatLong.latitude)
        val endLong = radians(endLatLong.longitude)
        var deltaLong = endLong - startLong
        val deltaPhi = Math.log(Math.tan(endLat / 2.0 + Math.PI / 4.0) / Math.tan(startLat / 2.0 + Math.PI / 4.0))
        if (Math.abs(deltaLong) > Math.PI) {
            deltaLong = if (deltaLong > 0.0) {
                -(2.0 * Math.PI - deltaLong)
            } else {
                (2.0 * Math.PI + deltaLong)
            }
        }
        return (degrees(Math.atan2(deltaLong, deltaPhi)) + RADIUS) % RADIUS
    }

    private fun getDistancePerSecond(source: LatLng, destination: LatLng): Float {
        val des = Location("Point")
        des.latitude = source.latitude
        des.longitude = source.longitude
        val src = Location("Point")
        src.latitude = destination.latitude
        src.longitude = destination.longitude
        mAverageSpeed = (src.distanceTo(des) * TIME_CONVERT).toFloat()
        return src.distanceTo(des) / ONE_THOUSAND
    }

    private fun drawLine() {
        if (mLocationUpdates != null) {
            if (mLocationUpdates!!.size > 1) {
                mGoogleMap.addPolyline(PolylineOptions()
                        .add(mLocationUpdates!![mLocationUpdates!!.size - 2], mLocationUpdates!![mLocationUpdates!!.size - 1])
                        .width(ZOOM_SIZE / 2)
                        .color(Color.BLACK))
            }
        }
    }

    private fun updateMapView(latLng: LatLng) {
        var angle = 0.0f
        if (mLocationUpdates!!.size > 1) {
            angle = getAngleMarker(mLocationUpdates!![mLocationUpdates!!.size - 2], mLocationUpdates!![mLocationUpdates!!.size - 1]).toFloat()
        }
        if (mCurrentMarker == null) {
            mCurrentMarker = mGoogleMap.addMarker(MarkerOptions().
                    position(latLng).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_hero_marker))
                    .anchor(0.5f, 0.5f)
                    .flat(true))
        } else {
            mCurrentMarker!!.position = latLng
            mCurrentMarker!!.rotation = angle
        }
        drawLine()
        MarkerAnimation.animateMarker(mCurrentMarker, latLng)
    }
}
