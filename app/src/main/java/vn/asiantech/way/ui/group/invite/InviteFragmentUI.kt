package vn.asiantech.way.ui.group.invite

import android.graphics.Color
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.hypertrack.lib.models.User
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import vn.asiantech.way.R
import vn.asiantech.way.utils.AppConstants

/**
 * Invite UI
 * @author NgocTTN
 */

class InviteFragmentUI(users: MutableList<User>) : AnkoComponent<InviteFragment> {

    internal lateinit var edtUserName: EditText
    internal val userListAdapter = InviteUserListAdapter(users)

    override fun createView(ui: AnkoContext<InviteFragment>): View = ui.apply {
        verticalLayout {
            lparams(matchParent, matchParent)
            backgroundColor = ActivityCompat.getColor(context, R.color.colorSearchScreenBackground)

            verticalLayout {
                lparams(matchParent, wrapContent)
                gravity = Gravity.CENTER_VERTICAL

                linearLayout {
                    lparams(matchParent, wrapContent)
                    gravity = Gravity.CENTER_VERTICAL

                    // Image Button: back
                    imageButton(R.drawable.ic_back_icon_button) {
                        backgroundColor = Color.TRANSPARENT
                        contentDescription = null
                        padding = dimen(R.dimen.item_user_bottom_or_top_padding)
                        onClick {
                            owner.onBackPressed()
                        }
                    }.lparams(wrapContent, wrapContent) {
                        topMargin = dimen(R.dimen.invite_screen_padding)
                        leftMargin = dimen(R.dimen.invite_screen_padding)
                        rightMargin = dimen(R.dimen.invite_screen_padding)
                    }

                    // Edit text : enter user name
                    edtUserName = editText {
                        backgroundColor = ActivityCompat.getColor(context, R.color.colorEdtSearchBackground)
                        hint = resources.getString(R.string.item_user_text_hint)
                        singleLine = true
                        padding = dimen(R.dimen.invite_screen_padding)
                        textSize = AppConstants.KEY_EDT_USER_NAME_TEXT_SIZE
                        gravity = Gravity.CENTER_VERTICAL

                        addTextChangedListener(object : InviteFragmentUI.TextChangeListener {
                            override fun afterTextChanged(editable: Editable) {
                                Handler().postDelayed({
                                    owner.onGetListUserInvite(editable.toString().trim())
                                },AppConstants.DELAY_TIME)
                            }
                        })
                    }.lparams(matchParent, matchParent) {
                        topMargin = dimen(R.dimen.invite_screen_padding)
                        rightMargin = dimen(R.dimen.invite_screen_padding)
                    }
                }

                // RecycleView: list user invite
                recyclerView {
                    id = R.id.invite_recycle_view
                    clipToPadding = false
                    backgroundColor = ActivityCompat.getColor(context, R.color.colorWhite)
                    userListAdapter.onItemInviteClick = {
                        owner.onItemInviteClick(it)
                    }
                    layoutManager = LinearLayoutManager(context)
                    adapter = userListAdapter
                }.lparams(matchParent, matchParent) {
                    topMargin = dimen(R.dimen.invite_screen_top_margin)
                    bottomMargin = dimen(R.dimen.invite_screen_padding)
                    leftMargin = dimen(R.dimen.invite_screen_padding)
                    rightMargin = dimen(R.dimen.invite_screen_padding)
                }
            }
        }
    }.view

    /**
     * This interface use to handle Text change event of edit text.
     */
    interface TextChangeListener : TextWatcher {
        override fun beforeTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int) = Unit

        override fun onTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int) = Unit

        override fun afterTextChanged(editable: Editable)
    }
}
