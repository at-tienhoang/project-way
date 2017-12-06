package vn.asiantech.way.ui.group.invite

import android.app.Activity
import android.graphics.Color
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

/**
 * Invite UI
 * @author NgocTTN
 */

class InviteFragmentUI(val activity: Activity, private val users: MutableList<User>) : AnkoComponent<InviteFragment> {

    private var userId = ""
    private var groupId = ""
    private var groupName = ""
    private var ownerId = ""
    internal lateinit var edtUserName: EditText
    internal val userListAdapter = UserListAdapter(users)

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
                            activity.onBackPressed()
                        }
                    }.lparams(wrapContent, wrapContent) {
                        topMargin = dimen(R.dimen.invite_screen_padding)
                        leftMargin = dimen(R.dimen.invite_screen_padding)
                        rightMargin = dimen(R.dimen.invite_screen_padding)
                    }
                    // Edit text : User name
                    edtUserName = editText {
                        backgroundColor = ActivityCompat.getColor(context, R.color.colorEdtSearchBackground)
                        hint = resources.getString(R.string.item_user_text_hint)
                        singleLine = true
                        padding = dimen(R.dimen.invite_screen_padding)
                        textSize = 18f
                        gravity = Gravity.CENTER_VERTICAL

                        addTextChangedListener(object : InviteFragmentUI.TextChangeListener {
                            override fun afterTextChanged(editable: Editable) {
                                owner.searchUserList(editable.toString().trim())
                            }
                        })
                    }.lparams(matchParent, matchParent) {
                        topMargin = dimen(R.dimen.invite_screen_padding)
                        rightMargin = dimen(R.dimen.invite_screen_padding)
                    }
                }

                // RecycleView: list user
                recyclerView {
                    id = R.id.invite_recycle_view
                    clipToPadding = false
                    background = resources.getDrawable(R.drawable.bg_user_list)
                    userListAdapter.onItemClick = {
                        owner.onItemClick(it)
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
