package vn.asiantech.way.ui.search

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_search_location.*
import org.json.JSONArray
import vn.asiantech.way.R
import vn.asiantech.way.data.model.search.MyLocation
import vn.asiantech.way.ui.base.BaseActivity

/**
 * Copyright © 2017 Asian Tech Co., Ltd.
 * Created by cuongcaov. on 25/09/2017.
 */
class SearchLocationActivity : BaseActivity() {

    companion object {
        private const val SHARED_PREFERENCES = "shared"
        private const val KEY_HISTORY = "history"
        private const val HISTORY_MAX_SIZE = 10
    }

    private var mTask: SearchLocationAsyncTask? = null
    private var mAdapter: LocationsAdapter? = null
    private var mMyLocations: MutableList<MyLocation> = mutableListOf()
    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
        initAdapter()
        locationSearch()
        onClick()
    }

    private fun onClick() {
        imgBtnBack.setOnClickListener {
            this.finish()
        }

        rlYourLocation.setOnClickListener {
            // TODO: Call to ShareLocationActivity - atToanNguyen
        }

        rlChooseOnMap.setOnClickListener {
            // TODO: Call to ShareLocationActivity - atToanNguyen
        }

    }

    private fun locationSearch() {
        edtLocation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                mMyLocations.clear()
                mAdapter?.notifyDataSetChanged()
                if (p0.toString().isNotEmpty()) {
                    if (mTask != null) {
                        mTask = null
                    }
                    mTask = SearchLocationAsyncTask(object : SearchLocationAsyncTask.SearchLocationListener {
                        override fun onCompleted(myLocations: List<MyLocation>?) {
                            val thread = Thread({
                                runOnUiThread({
                                    myLocations?.forEach {
                                        mMyLocations.add(it)
                                    }
                                    mAdapter?.notifyDataSetChanged()
                                })
                            })
                            thread.start()
                        }

                    })
                    mTask?.execute(p0.toString())
                } else {
                    val history = getSearchHistory()
                    if (history != null) {
                        mMyLocations.addAll(history)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        })
    }

    private fun initAdapter() {
        val history = getSearchHistory()
        if (history != null) {
            mMyLocations.addAll(history)
        }
        mAdapter = LocationsAdapter(mMyLocations, object : LocationsAdapter.RecyclerViewOnItemClickListener {
            override fun onItemClick(myLocation: MyLocation) {
                saveSearchHistory(myLocation)
                // TODO: Call to ShareLocationActivity - atToanNguyen
            }

        })
        recyclerViewLocations.layoutManager = LinearLayoutManager(this)
        recyclerViewLocations.adapter = mAdapter
    }

    private fun getSearchHistory(): MutableList<MyLocation>? {
        val gson = Gson()
        val result = mutableListOf<MyLocation>()
        return try {
            val history = mSharedPreferences?.getString(KEY_HISTORY, "[]")
            val jsonArray = JSONArray(history)
            (0 until jsonArray.length())
                    .mapTo(result) { gson.fromJson(jsonArray.getJSONObject(it).toString(), MyLocation::class.java) }
            result
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun saveSearchHistory(myLocation: MyLocation) {
        val gson = Gson()
        val editor = mSharedPreferences?.edit()
        var history = getSearchHistory()
        if (history == null) {
            history = mutableListOf()
        }
        history.forEach {
            if (it.id == myLocation.id) {
                return
            }
        }
        if (history.size > HISTORY_MAX_SIZE) {
            history.removeAt(0)
        }
        history.add(myLocation)
        editor?.putString(KEY_HISTORY, gson.toJson(history))
        editor?.commit()
    }
}