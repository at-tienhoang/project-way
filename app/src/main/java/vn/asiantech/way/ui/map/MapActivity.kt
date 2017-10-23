package vn.asiantech.way.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import vn.asiantech.way.R
import vn.asiantech.way.ui.base.BaseActivity

internal class MapActivity : BaseActivity() {
    private companion object {
        private const val REQUEST_CODE_PERMISSION = 200
        private const val VERSION_SDK = 23
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        replaceCreatePOStepFragment(MapFragment(), false)
        askPermissionsAccessLocation()
    }

    private fun replaceCreatePOStepFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            transaction.addToBackStack(fragment.tag)
        }
        transaction.replace(R.id.frContainer, fragment)
        transaction.commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
    }

    private fun askPermissionsAccessLocation() {
        // Ask for permission with API >= 23
        if (Build.VERSION.SDK_INT >= VERSION_SDK) {
            val accessFineLocationPermission = ContextCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                // Permissions
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                // Dialog
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION)
            } else {
                // No-op
            }
        }
    }
}
