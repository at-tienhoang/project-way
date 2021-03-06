package vn.asiantech.way.ui.search

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.util.DiffUtil.DiffResult
import org.jetbrains.anko.setContentView
import vn.asiantech.way.R
import vn.asiantech.way.data.model.WayLocation
import vn.asiantech.way.extension.observeOnUiThread
import vn.asiantech.way.extension.toast
import vn.asiantech.way.ui.base.BaseActivity
import vn.asiantech.way.ui.share.ShareActivity

/**
 * Copyright © 2017 Asian Tech Co., Ltd.
 * Created by cuongcaov on 27/11/2017
 */
class SearchActivity : BaseActivity() {

    private lateinit var ui: SearchActivityUI
    private lateinit var viewModel: SearchViewModel
    private lateinit var startShareActivityIntent: Intent
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = SearchViewModel(this)
        ui = SearchActivityUI(viewModel.locations)
        ui.setContentView(this)
        initViews()
        viewModel.searchLocations()
    }

    override fun onBindViewModel() {
        addDisposables(
                //Update progress bar status
                viewModel.progressBarStatus
                        .observeOnUiThread()
                        .subscribe(this::handleProgressBarStatus),
                //Update search list view
                viewModel.updateAutocompleteList
                        .observeOnUiThread()
                        .subscribe(this::handleUpdateSearchAutocompleteList))
    }

    /**
     * Search location by name.
     */
    internal fun eventSearchTextChanged(query: String) {
        viewModel.searchLocations(query)
    }

    /**
     * Get current location.
     */
    internal fun eventItemGetCurrentLocationClicked() {
        startShareActivityIntent.action = ShareActivity.ACTION_CURRENT_LOCATION
        startActivity(startShareActivityIntent)
    }

    /**
     * Choose location on map.
     */
    internal fun eventItemChooseLocationOnMapClicked() {
        startShareActivityIntent.action = ShareActivity.ACTION_CHOOSE_ON_MAP
        startActivity(startShareActivityIntent)
    }

    /**
     * On item of  RecyclerView click.
     */
    internal fun eventSearchItemClicked(location: WayLocation) {
        if (location.isHistory != null && location.isHistory == true) {
            startSharedActivity(location)
            return
        }
        val placeId = location.placeId
        if (placeId != null) {
            viewModel.getLocationDetail(placeId)
                    .observeOnUiThread()
                    .subscribe(this::startSharedActivity, {
                        it.printStackTrace()
                        toast(getString(R.string.error_message))
                    })
        }
    }

    /**
     * Show search location data or load data from history
     */
    private fun handleUpdateSearchAutocompleteList(diff: DiffResult) {
        diff.dispatchUpdatesTo(ui.locationAdapter)
    }

    private fun handleProgressBarStatus(isShow: Boolean) {
        if (isShow) {
            progressDialog.show()
        } else {
            progressDialog.dismiss()
        }
    }

    private fun initViews() {
        startShareActivityIntent = Intent(this, ShareActivity::class.java)
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.processing))
    }

    private fun startSharedActivity(location: WayLocation) {
        viewModel.saveSearchHistories(location)
        val bundle = Bundle()
        bundle.putParcelable(ShareActivity.KEY_LOCATION, location)
        startShareActivityIntent.action = ShareActivity.ACTION_SEND_WAY_LOCATION
        startShareActivityIntent.putExtras(bundle)
        startActivity(startShareActivityIntent)
    }
}
