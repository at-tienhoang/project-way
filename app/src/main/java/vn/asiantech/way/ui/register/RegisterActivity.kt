package vn.asiantech.way.ui.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hypertrack.lib.HyperTrack
import com.hypertrack.lib.callbacks.HyperTrackCallback
import com.hypertrack.lib.models.ErrorResponse
import com.hypertrack.lib.models.SuccessResponse
import com.hypertrack.lib.models.User
import com.hypertrack.lib.models.UserParams
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_register.*
import vn.asiantech.way.R
import vn.asiantech.way.data.models.Country
import vn.asiantech.way.extension.hideKeyboard
import vn.asiantech.way.extension.toast
import vn.asiantech.way.ui.base.BaseActivity
import java.io.ByteArrayOutputStream

/**
 * Activity register user
 * Created by haibt on 9/26/17.
 */
class RegisterActivity : BaseActivity(), TextView.OnEditorActionListener
        , View.OnClickListener, TextWatcher {
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1001
        private const val REQUEST_CODE_GALLERY = 500
        private const val IMAGE_SIZE_LIMIT = 1048576
    }

    var mBitmap: Bitmap? = null
    var mByteArray: ByteArray? = null
    var mCountries: List<Country> = ArrayList()
    var mPreviousName: String? = null
    var mPreviousPhone: String? = null
    var mIsoCode: String? = null
    var mTel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initListener()
        mCountries = getCountries(readJsonFromDirectory())
        mIsoCode = getString(R.string.register_iso_code_default)
        initCountrySpinner()
        setUserInformation()
        frAvatar.setOnClickListener {
            checkPermissionGallery()
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
        when (v?.id) {
            R.id.edtName -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    edtPhoneNumber.requestFocus()
                    return true
                }
            }
            R.id.edtPhoneNumber -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    edtPhoneNumber.hideKeyboard(this)
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSave -> {
                val name: String = edtName.text.toString().trim()
                val phoneNumber: String = edtPhoneNumber.text.toString().trim()
                if (name.isBlank()) {
                    edtName.setText(R.string.register_user_name_default)
                }
                if (phoneNumber.isBlank()) {
                    edtPhoneNumber.text = null
                }
                createUser(name, phoneNumber)
                /**
                 * TODO
                 * intent to home activity
                 */
            }
            R.id.tvCancel -> {
                /**
                 * TODO
                 * intent to home activity
                 */
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        // No-op
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // No-op
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        val name: String = edtName.text.toString().trim()
        val phone: String = edtPhoneNumber.text.toString().trim()
        val tel: String = tvTel.text.toString().removeRange(0, 1)
        if ((name.isBlank() && phone.isBlank())
                || (mPreviousName?.trim() == name
                && mPreviousPhone?.removeRange(0, 3) == phone
                && mTel == tel)) {
            tvCancel.text = getString(R.string.register_skip)
            btnSave.isEnabled = false
        } else {
            tvCancel.text = getString(R.string.register_cancel)
            btnSave.isEnabled = true
        }
    }

    private fun createUser(name: String, phoneNumber: String) {
        val userParam: UserParams = UserParams()
                .setName(name)
                .setPhoto(encodeImage())
                .setPhone(mIsoCode?.plus("/")?.plus(phoneNumber))
                .setLookupId(phoneNumber)

        // Create new user
        if (mPreviousPhone != phoneNumber) {
            HyperTrack.getOrCreateUser(userParam, object : HyperTrackCallback() {
                override fun onSuccess(p0: SuccessResponse) {
                    HyperTrack.startTracking()
                    RegisterActivity().toast(getString(R.string.register_create_user))
                }

                override fun onError(p0: ErrorResponse) {
                    // No-op
                }
            })
        } else {
            // Update user information
            HyperTrack.updateUser(userParam, object : HyperTrackCallback() {
                override fun onSuccess(p0: SuccessResponse) {
                    HyperTrack.startTracking()
                    RegisterActivity().toast(getString(R.string.register_update_user))
                }

                override fun onError(p0: ErrorResponse) {
                    // No-op
                }
            })
        }
    }

    private fun setUserInformation() {
        visibleProgressBar(progressBar)
        HyperTrack.getUser(object : HyperTrackCallback() {
            override fun onSuccess(response: SuccessResponse) {
                val user: User? = response.responseObject as User
                updateView(user)
                invisibleProgressBar(progressBar)
            }

            override fun onError(p0: ErrorResponse) {
                invisibleProgressBar(progressBar)
            }
        })
    }

    private fun updateView(user: User?) {
        val target: Target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                visibleProgressBar(progressBarAvatar)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                // No-op
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                imgAvatar.setImageBitmap(bitmap)
                invisibleProgressBar(progressBarAvatar)
                mBitmap = bitmap
            }
        }
        Picasso.with(this)
                .load(user?.photo)
                .resize(100, 100)
                .into(target)
        imgAvatar.tag = target
        edtName.setText(user?.name)
        val basePhone: List<String>? = user?.phone?.split("/")
        if (basePhone!!.size > 1) {
            // Set isoCode
            mIsoCode = basePhone[0]
            for (i in 0 until mCountries.size) {
                if (mIsoCode == mCountries[i].iso) {
                    spinnerNation.setSelection(i)
                    val tel = mCountries[i].tel
                    tvTel.text = tel
                    mTel = tel
                    break
                }
            }
            edtPhoneNumber.setText(basePhone[1])
        } else {
            edtPhoneNumber.setText(basePhone[0])
        }
        btnSave.isEnabled = checkUser(user.name, user.phone)
        mPreviousName = user.name
        mPreviousPhone = user.phone
    }

    private fun checkUser(name: String, phone: String): Boolean {
        if (mPreviousName != name.trim() && mPreviousPhone != phone.trim()) {
            return true
        }
        return false
    }

    private fun intentGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun checkPermissionGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_GALLERY)
            }
        } else {
            intentGallery()
        }
    }

    private fun visibleProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun invisibleProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun initListener() {
        edtName.setOnEditorActionListener(this)
        edtName.addTextChangedListener(this)
        edtPhoneNumber.setOnEditorActionListener(this)
        edtPhoneNumber.addTextChangedListener(this)
        tvTel.addTextChangedListener(this)
        btnSave.setOnClickListener(this)
    }

    private fun initCountrySpinner() {
        spinnerNation.adapter = CountrySpinnerAdapter(this, R.layout.item_list_country, mCountries)
        spinnerNation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // No-op
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                tvTel.text = getString(R.string.register_plus).plus(mCountries[position].tel)
                mIsoCode = mCountries[position].iso
            }
        }
    }

    /**
     * Read file json from raw directory
     */
    private fun readJsonFromDirectory(): String {
        val iStream = resources.openRawResource(R.raw.countries)
        val byteStream = ByteArrayOutputStream()
        val buffer = ByteArray(iStream.available())
        iStream.read(buffer)
        byteStream.write(buffer)
        byteStream.close()
        iStream.close()
        return byteStream.toString()
    }

    private fun getCountries(json: String): List<Country> {
        return Gson().fromJson(json, object : TypeToken<List<Country>>() {}.type)
    }

    private fun encodeImage(): String {
        return Base64.encodeToString(mByteArray, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            val uri: Uri? = data?.data
            mBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val baoStream = ByteArrayOutputStream()
            mBitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baoStream)
            mByteArray = baoStream.toByteArray()
            val imageSize: Int = mByteArray!!.size
            if (imageSize > IMAGE_SIZE_LIMIT) {
                this.toast(getString(R.string.toast_image_size))
            } else {
                imgAvatar.setImageBitmap(mBitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intentGallery()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}