package vn.asiantech.way.ui.confirm

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_confirm_location.*
import kotlinx.android.synthetic.main.view_confirm_location_layout.*
import kotlinx.android.synthetic.main.view_confirm_location_layout.view.*
import vn.asiantech.way.R
import vn.asiantech.way.ui.base.BaseFragment
import vn.asiantech.way.ui.custom.RadiusAnimation
import vn.asiantech.way.utils.LocationUtil
import java.util.*

/**
 * Fragment confirm location
 * Created by haingoq on 10/10/2017.
 */
class ConfirmLocationFragment : BaseFragment(), OnMapReadyCallback,
        LocationListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {
    private var mGoogleMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mLatLng: LatLng? = null
    private var mDestinationName: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_confirm_location, container, false)
        initMap()
        initListener(view)
        return view
    }

    override fun onMapReady(map: GoogleMap?) {
        mGoogleMap = map
        //Get current location
        val currentLocation: Location? = LocationUtil(context).getCurrentLocation()
        if (currentLocation != null) {
            drawCurrentMaker(currentLocation)
        }
        mGoogleMap?.setOnCameraIdleListener(this)
    }

    override fun onLocationChanged(location: Location?) {
        drawCurrentMaker(location!!)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        // No op
    }

    override fun onProviderEnabled(p0: String?) {
        // No op
    }

    override fun onProviderDisabled(p0: String?) {
        // No op
    }

    override fun onCameraIdle() {
        mLatLng = mGoogleMap?.cameraPosition?.target
        getLocationName(mLatLng)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgEdit -> {
                //TODO intent to search
            }
            R.id.btnConfirm -> {
                addDestinationMarker(mLatLng)
                //TODO handle share location
            }
        }
    }

    private fun initListener(view: View?) {
        view?.imgEdit?.setOnClickListener(this)
        view?.btnConfirm?.setOnClickListener(this)
    }

    private fun initMap() {
        mMapFragment = childFragmentManager.findFragmentById(R.id.fragmentConfirmMap) as SupportMapFragment?
        mMapFragment?.getMapAsync(this)
    }

    private fun getLocationName(latLng: LatLng?) {
        val geoCoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geoCoder.getFromLocation(latLng!!.latitude, latLng.longitude, 1)
        if (addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            tvLocation.text = address.getAddressLine(0)
            if (!address.subThoroughfare.isNullOrEmpty()) {
                mDestinationName = address.subThoroughfare.plus(" ").plus(address.thoroughfare)
            } else {
                mDestinationName = address.thoroughfare
            }
        } else {
            tvLocation.text = null
        }
    }

    private fun addDestinationMarker(latLng: LatLng?) {
        mGoogleMap?.addMarker(MarkerOptions()
                .position(latLng!!)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_expected_place_marker))
                .title(mDestinationName)
                .anchor(0.5f, 0.5f))
                ?.showInfoWindow()
        imgPickLocation.visibility = View.INVISIBLE
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun drawCurrentMaker(location: Location) {
        if (mGoogleMap != null) {
            mGoogleMap?.clear()
            val currentLocation = LatLng(location.latitude, location.longitude)
            mGoogleMap?.addMarker(MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_point))
                    .title(getString(R.string.current_location))
                    .anchor(0.5f, 0.5f))
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            addPulseRing(currentLocation)
        }
    }

    private fun addPulseRing(latLng: LatLng) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setSize(500, 500)
        drawable.setColor(ContextCompat.getColor(context, R.color.pulse_color))

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val groundOverlay = mGoogleMap?.addGroundOverlay(GroundOverlayOptions()
                .position(latLng, 500f)
                .image(BitmapDescriptorFactory.fromBitmap(bitmap)))
        val groundAnimation = RadiusAnimation(groundOverlay)
        groundAnimation.repeatCount = Animation.INFINITE
        groundAnimation.duration = 2000
        mMapFragment?.view?.startAnimation(groundAnimation)
    }
}
