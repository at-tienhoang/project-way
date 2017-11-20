package vn.asiantech.way.ui.update

import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_home.*
import vn.asiantech.way.R
import vn.asiantech.way.data.model.Location
import vn.asiantech.way.extension.toast
import vn.asiantech.way.ui.base.BaseActivity
import vn.asiantech.way.ui.custom.FloatingButtonHorizontal
import vn.asiantech.way.ui.home.HomeAdapter
import vn.asiantech.way.ui.register.RegisterActivity
import vn.asiantech.way.ui.search.SearchLocationActivity
import vn.asiantech.way.ui.share.ShareLocationActivity
import vn.asiantech.way.utils.AppConstants
import vn.asiantech.way.utils.LocationUtil
import vn.asiantech.way.utils.Preference

/**
 * Copyright © 2017 Asian Tech Co., Ltd.
 * Created by at-hoavo on 25/10/2017.
 */
internal class UpdateMap : BaseActivity(), OnMapReadyCallback,
        FloatingButtonHorizontal.OnMenuClickListener {

    companion object {
        private const val PADDING_LEFT = 0
        private const val PADDING_TOP = 0
        private const val PADDING_RIGHT = 0
        private const val ZOOM = 16f
        private const val TYPE_PROGRESS_MAX = 100
        private const val TYPE_POLYLINE_WIDTH = 5f
        private const val TYPE_ANCHOR = 0.5f
        private const val TYPE_TIME_DELAY = 3000L
        private const val UNIT_PADDING_BOTTOM = 3
        const val BEGIN_LAT = 16.0721115
        const val BEGIN_LONG = 108.2302225
        const val DESTINATION_LAT = 16.0712047
        const val DESTINATION_LONG = 108.2193197

    }

    private var mPosition = -1
    private lateinit var mHomeAdapter: HomeAdapter
    private var mGoogleMap: GoogleMap? = null
    private var isExit = false
    private var mIsExpand = false
    private lateinit var mDestination: LatLng
    private lateinit var mBegin: LatLng
    private var mLocations: MutableList<Location> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Preference.init(this)
        initMap()
        initViews()
        fabMenuGroup.setOnMenuItemClickListener(this)
        frOverlay.setOnClickListener {
            if (mIsExpand) {
                fabMenuGroup.collapseMenu()
                mIsExpand = false
                setGoneOverLay()
            }
        }
        initData()
        setDataForRecyclerView()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap
        setPaddingGoogleLogo()
        //================================
        val location = LocationUtil(this).getCurrentLocation()
        if (location != null) {
            drawCurrentMaker(location)
        } else {
            toast(resources.getString(R.string.not_update_current_location))
        }
    }

    override fun onMenuClick(isShowMenu: Boolean) {
        frOverlay.visibility = if (isShowMenu) View.VISIBLE else View.GONE
        mIsExpand = isShowMenu
    }

    override fun onShareClick() {
        startActivity(Intent(this, ShareLocationActivity::class.java))
        setGoneOverLay()
    }

    override fun onProfileClick() {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra(RegisterActivity.INTENT_REGISTER, RegisterActivity.INTENT_CODE_HOME)
        startActivity(intent)
        setGoneOverLay()
    }

    override fun onCalendarClick() {
        setGoneOverLay()
        // TODO after completed calendar feature
    }

    override fun onSearchClick() {
        val actionType = Preference().getActionType()
        if (actionType == AppConstants.KEY_START_TRACKING) {
            startActivity(Intent(this, ShareLocationActivity::class.java))
        } else {
            startActivity(Intent(this, SearchLocationActivity::class.java))
        }
        setGoneOverLay()
    }

    private fun initViews() {
        setStatusBarTranslucent(true)
    }

    private fun initMap() {
        val supportMapFragment = supportFragmentManager.
                findFragmentById(R.id.fragmentMap) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)
    }

    private fun setPaddingGoogleLogo() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mGoogleMap?.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT,
                size.y / UNIT_PADDING_BOTTOM)
    }

    private fun setGoneOverLay() {
        frOverlay.visibility = View.GONE
    }

    private fun drawCurrentMaker(location: android.location.Location) {
        if (mGoogleMap != null) {
            mGoogleMap?.clear()
            val currentLocation = LatLng(location.latitude, location.longitude)
            addMarker(MarkerOptions()
                    .position(currentLocation)
                    .draggable(true)
                    .title(resources.getString(R.string.current_location)).icon(BitmapDescriptorFactory.
                    fromResource(R.drawable.ic_current_point)), currentLocation)
        } else {
            toast(resources.getString(R.string.toast_text_google_map_null))
        }
    }

    private fun addMarker(markerOptions: MarkerOptions, position: LatLng) {
        mGoogleMap?.addMarker(markerOptions)
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM))
    }

    private fun setMarkerOption(resource: Int, position: LatLng): MarkerOptions =
            MarkerOptions().position(position).
                    icon(BitmapDescriptorFactory.fromResource(resource))
                    .anchor(TYPE_ANCHOR, TYPE_ANCHOR)

    private fun drawLine(points: List<LatLng>) {
        val polyPointOption = PolylineOptions()
        points.let {
            polyPointOption.addAll(it)
        }
        polyPointOption.color(Color.BLACK)
        polyPointOption.width(TYPE_POLYLINE_WIDTH)
        mGoogleMap?.addPolyline(polyPointOption)
    }

    private fun updateMap(points: List<LatLng>) {
        mGoogleMap?.clear()
        addMarker(setMarkerOption(R.drawable.ic_ht_source_place_marker, mBegin), mBegin)
        if (points.size > 1) {
            if (points[points.size - 1] != mDestination) {
                addMarker(setMarkerOption(R.drawable.ic_rectangle, points[points.size - 1]),
                        points[points.size - 1])
            } else {
                addMarker(setMarkerOption(R.drawable.ic_ht_expected_place_marker,
                        points[points.size - 1]), points[points.size - 1])
            }
            drawLine(points)
        }
    }

    private fun setDataForRecyclerView() {
        val positions: MutableList<Int> = mutableListOf()
        mHomeAdapter = HomeAdapter(mLocations) {
            if (mPosition >= 0) {
                mLocations[mPosition].isChoose = false
                mHomeAdapter.notifyItemChanged(mPosition)
            }
            positions.add(it)
            if (positions.size > 1) {
                if (it > positions[positions.size - 2]) {
                    recycleViewLocation.scrollToPosition(it + 1)
                } else {
                    recycleViewLocation.scrollToPosition(it - 1)
                }
            }
            mLocations[it].isChoose = true
            mHomeAdapter.notifyItemChanged(it)
            mPosition = it
            updateMap(setListToPosition(it))
        }
        recycleViewLocation.layoutManager = LinearLayoutManager(this)
        recycleViewLocation.adapter = mHomeAdapter
    }

    private fun setListToPosition(position: Int): List<LatLng> =
            (0..position).map { mLocations[it].point }

    private fun initData() {
        Preference().getTrackingHistory()?.let { mLocations.addAll(it) }
        mBegin = LatLng(BEGIN_LAT, BEGIN_LONG)
        mDestination = LatLng(DESTINATION_LAT, DESTINATION_LONG)
    }

    private fun setStatusBarTranslucent(makeTranslucent: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (makeTranslucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    override fun onBackPressed() {
        if (isExit) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            toast(resources.getString(R.string.register_double_click_to_exit))
            isExit = true
            Handler().postDelayed({ isExit = false }, TYPE_TIME_DELAY)
        }
    }
}
