package vn.asiantech.way.ui.search

import android.os.Bundle
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import vn.asiantech.way.R
import vn.asiantech.way.data.model.MyLocation
import vn.asiantech.way.ui.base.BaseActivity

/**
 * Copyright © 2017 Asian Tech Co., Ltd.
 * Created by cuongcaov on 27/11/2017
 */
class SearchActivity : BaseActivity() {

    private lateinit var searchActivityUI: SearchActivityUI
    private lateinit var adapter: LocationAdapter
    private var locations = mutableListOf<MyLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = LocationAdapter(locations)
        adapter.onItemClick = {
            // TODO: 28/11/2017
            // Dummy data
            toast(R.string.coming_soon)
        }
        searchActivityUI = SearchActivityUI(adapter)
        searchActivityUI.setContentView(this)
    }

    /**
     * Search location by query name.
     */
    internal fun searchLocations(query: String) {
        // TODO: 28/11/2017
        // Dummy data
        locations.clear()
        adapter.notifyDataSetChanged()
        if (query == searchActivityUI.edtLocation.text.toString().trim()) {
            locations.add(MyLocation("aaa1", "sss1", "Binh Dao",
                    "Binh Dao Thang Binh"))
            locations.add(MyLocation("aaa2", "sss2", "Binh Dao",
                    "Binh Dao Thang Binh"))
            locations.add(MyLocation("aaa3", "sss3", "Binh Dao",
                    "Binh Dao Thang Binh"))
            locations.add(MyLocation("aaa4", "sss4", "Binh Dao",
                    "Binh Dao Thang Binh"))
            locations.add(MyLocation("aaa5", "sss5", "Binh Dao",
                    "Binh Dao Thang Binh"))
            locations.add(MyLocation("aaa6", "sss6", "Binh Dao",
                    "Binh Dao Thang Binh"))
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Get current location.
     */
    internal fun getCurrentLocation() {
        // Init later.
        toast(R.string.coming_soon)
    }

    /**
     * Choose location on map.
     */
    internal fun chooseOnMap() {
        // Init later.
        toast(R.string.coming_soon)
    }
}
