package vn.asiantech.way.ui.register

import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import vn.asiantech.way.R

/**
 * Anko layout for RegisterActivity
 * Created by haingoq on 27/11/2017.
 */
class RegisterActivityUI(private val countryAdapter: CountryAdapter) : AnkoComponent<RegisterActivity> {
    companion object {
        private const val ID_FR_AVATAR = 1001
        private const val ID_TV_DESCRIPTION = 1002
        private const val ID_RL_INFORMATION = 1003
        private const val ID_BTN_SAVE = 1004
        private const val ID_TV_SKIP = 1005
        private const val ID_EDT_NAME = 1006
        private const val ID_VIEW_LINE = 1007
    }

    internal lateinit var dialogInterface: DialogInterface
    internal lateinit var frAvatar: FrameLayout
    internal lateinit var progressBarAvatar: ProgressBar
    internal lateinit var imgAvatar: ImageView
    internal lateinit var edtName: EditText
    internal lateinit var imgFlag: ImageView
    internal lateinit var tvTel: TextView
    internal lateinit var edtPhone: EditText
    internal lateinit var btnRegister: Button
    internal lateinit var tvSkip: TextView
    internal lateinit var tvCancel: TextView
    internal lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<RegisterActivity>) = with(ui) {
        relativeLayout {
            lparams(matchParent, matchParent)
            frAvatar = frameLayout {
                id = ID_FR_AVATAR

                circleImageView {
                    backgroundResource = R.drawable.ic_default_avatar
                    lparams(dimen(R.dimen.register_screen_avatar_size),
                            dimen(R.dimen.register_screen_avatar_size))
                    borderColor = ContextCompat.getColor(context, R.color.white)
                    borderWidth = dimen(R.dimen.border)
                }

                progressBarAvatar = progressBar {
                    visibility = View.GONE
                }.lparams {
                    gravity = Gravity.CENTER
                }

                imgAvatar = circleImageView {
                    backgroundResource = R.drawable.ic_profile_camera
                    borderColor = ContextCompat.getColor(context, R.color.white)
                    borderWidth = dip(dimen(R.dimen.border))
                }.lparams {
                    rightMargin = dimen(R.dimen.register_screen_avatar_margin)
                    gravity = Gravity.END
                }
            }.lparams {
                topMargin = dimen(R.dimen.margin_huge)
                centerHorizontally()
            }

            textView(R.string.register_description) {
                id = ID_TV_DESCRIPTION
                gravity = Gravity.CENTER
                textSize = px2dip(dimen(R.dimen.register_screen_name_text_size))
            }.lparams(matchParent, wrapContent) {
                below(ID_FR_AVATAR)
                val margin = resources.getDimension(R.dimen.margin_xxhigh).toInt()
                topMargin = margin
                leftMargin = margin
                rightMargin = margin
            }

            relativeLayout {
                id = ID_RL_INFORMATION
                backgroundResource = R.drawable.custom_layout_phone

                edtName = editText {
                    id = ID_EDT_NAME
                    backgroundColor = ContextCompat.getColor(context, android.R.color.transparent)
                    hint = resources.getString(R.string.register_hint_name)
                    inputType = InputType.TYPE_CLASS_TEXT
                    textSize = px2dip(dimen(R.dimen.register_screen_name_text_size))
                    gravity = Gravity.CENTER
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                }.lparams(matchParent, dimen(R.dimen.register_screen_edit_text_height))

                view {
                    id = ID_VIEW_LINE
                    backgroundColor = ContextCompat.getColor(context, R.color.grayLight)
                }.lparams(matchParent, dimen(R.dimen.border)) {
                    below(ID_EDT_NAME)
                }

                linearLayout {
                    val padding = dip(dimen(R.dimen.register_screen_ll_phone_padding))
                    leftPadding = padding
                    rightPadding = padding
                    imgFlag = imageView().lparams {
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    imageView(R.drawable.ic_arrow_drop_down) {
                        onClick {
                            dialogInterface = alert {
                                customView {
                                    recyclerView {
                                        layoutManager = LinearLayoutManager(context)
                                        adapter = countryAdapter
                                        countryAdapter.onItemClick = { country ->
                                            // TODO Set image to imgFlag and tel to tvTel
                                            dialogInterface.dismiss()
                                        }
                                    }
                                }
                            }.show()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    tvTel = textView(R.string.register_tel) {
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        textSize = px2dip(dimen(R.dimen.register_screen_phone_text_size))
                    }.lparams(dip(dimen(R.dimen.register_screen_tv_tel_width)), matchParent)

                    edtPhone = editText {
                        backgroundColor = ContextCompat.getColor(context, android.R.color.transparent)
                        hint = resources.getString(R.string.register_hint_phone)
                        inputType = InputType.TYPE_CLASS_PHONE
                        textSize = px2dip(dimen(R.dimen.register_screen_phone_text_size))
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        imeOptions = EditorInfo.IME_ACTION_DONE
                    }.lparams(matchParent, matchParent)
                }.lparams(matchParent, dimen(R.dimen.register_screen_edit_text_height)) {
                    below(ID_VIEW_LINE)
                }
            }.lparams(matchParent, wrapContent) {
                below(ID_TV_DESCRIPTION)
                val margin = dimen(R.dimen.margin_high)
                bottomMargin = dimen(R.dimen.margin_huge)
                leftMargin = margin
                rightMargin = margin
                topMargin = margin
            }

            btnRegister = button(R.string.register_button_save_text) {
                id = ID_BTN_SAVE
                backgroundResource = R.drawable.custom_button_save
                setAllCaps(false)
                textColor = ContextCompat.getColor(context, R.color.white)
                textSize = px2dip(dimen(R.dimen.register_screen_save_button_text_size))
                isEnabled = false
            }.lparams(matchParent, dimen(R.dimen.register_screen_save_button_height)) {
                val margin = dimen(R.dimen.register_screen_btn_register_margin)
                below(ID_RL_INFORMATION)
                leftMargin = margin
                topMargin = margin
                rightMargin = margin
            }

            tvSkip = textView {
                text = resources.getString(R.string.register_skip)
                id = ID_TV_SKIP
                textSize = px2dip(dimen(R.dimen.register_screen_phone_text_size))
                gravity = Gravity.CENTER
            }.lparams(matchParent, wrapContent) {
                below(ID_BTN_SAVE)
                topMargin = dimen(R.dimen.register_screen_tv_skip_margin)
            }

            tvCancel = textView(R.string.register_cancel) {
                textSize = px2dip(dimen(R.dimen.register_screen_phone_text_size))
                gravity = Gravity.CENTER
                visibility = View.GONE
            }.lparams(matchParent, wrapContent) {
                below(ID_TV_SKIP)
                topMargin = dimen(R.dimen.register_screen_tv_skip_margin)
            }

            progressBar = progressBar {
                visibility = View.GONE
            }.lparams {
                centerInParent()
            }
        }
    }

    /*
     * Add circleImageView library
     */
    private inline fun ViewManager.circleImageView(theme: Int = 0, init: CircleImageView.() -> Unit):
            CircleImageView {
        return ankoView({ CircleImageView(it) }, theme, init)
    }
}