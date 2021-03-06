package vn.asiantech.way.ui.group.showinvite

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import vn.asiantech.way.R
import vn.asiantech.way.data.model.Invite

/**
 *  Copyright © 2017 AsianTech inc.
 *  Created by hoavot on 05/12/2017.
 */
class InviteListAdapter(private val context: Context, private val invites: MutableList<Invite>)
    : RecyclerView.Adapter<InviteListAdapter.InviteViewHolder>() {
    internal var onOkClick: (Invite) -> Unit = {}
    internal var onCancelClick: (Invite) -> Unit = {}

    override fun onBindViewHolder(holder: InviteViewHolder?, position: Int) {
        holder?.onBind()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = InviteListAdapterUI()
            .createView(AnkoContext.create(context, parent, false))
            .tag as? InviteViewHolder

    override fun getItemCount() = invites.size

    /**
     * Custom item of invites list.
     */
    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOk: TextView = itemView.find(R.id.group_show_invite_adapter_tv_ok)
        private val tvCancel: TextView = itemView.find(R.id.group_show_invite_adapter_tv_cancel)
        private val tvNameGroup: TextView = itemView.find(R.id.group_show_invite_adapter_tv_name_group)

        init {
            tvOk.onClick {
                onOkClick(invites[adapterPosition])
                invites.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }

            tvCancel.onClick {
                onCancelClick(invites[adapterPosition])
                invites.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
        }

        /**
         * Bind data to InviteViewHolder.
         */
        fun onBind() {
            tvNameGroup.text = invites[adapterPosition].groupName
        }
    }

    /**
     * Class to bind ViewHolder with Anko layout
     */
    inner class InviteListAdapterUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View {
            val itemView = ui.apply {
                relativeLayout {
                    lparams(matchParent, dimen(R.dimen.group_screen_adapter_item_width))
                    backgroundResource = R.color.colorWhite

                    textView {
                        id = R.id.group_show_invite_adapter_tv_name_group
                        textSize = px2dip(dimen(R.dimen.text_size_normal))
                    }

                    textView(R.string.ok) {
                        padding = dimen(R.dimen.invite_list_adapter_padding)
                        id = R.id.group_show_invite_adapter_tv_ok
                        backgroundResource = R.color.colorPinkLight
                        textSize = px2dip(dimen(R.dimen.group_text_size_normal))
                    }.lparams(dimen(R.dimen.group_screen_tv_ok_width), wrapContent) {
                        below(R.id.group_show_invite_adapter_tv_name_group)
                    }

                    textView(R.string.cancel) {
                        padding = dimen(R.dimen.invite_list_adapter_padding)
                        id = R.id.group_show_invite_adapter_tv_cancel
                        backgroundResource = R.color.colorGrayLight
                        textSize = px2dip(dimen(R.dimen.group_text_size_normal))
                    }.lparams(dimen(R.dimen.group_screen_tv_ok_width), wrapContent) {
                        rightOf(R.id.group_show_invite_adapter_tv_ok)
                        below(R.id.group_show_invite_adapter_tv_name_group)
                    }

                    view {
                        backgroundResource = R.color.grayLight
                    }.lparams(matchParent, dip(1)) {
                        below(R.id.group_show_invite_adapter_tv_ok)
                        topMargin = dimen(R.dimen.invite_list_adapter_padding)
                    }
                }.applyRecursively {
                    if (it.layoutParams is RelativeLayout.LayoutParams) {
                        (it.layoutParams as? RelativeLayout.LayoutParams)?.leftMargin = dimen(R.dimen.invite_list_adapter_margin_tv_name_group)
                    }

                    when (it) {
                        is TextView -> with(it) {
                            textColor = ContextCompat.getColor(context, R.color.colorBlack)
                            gravity = Gravity.CENTER
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                }
            }.view

            itemView.tag = InviteViewHolder(itemView)
            return itemView
        }
    }
}
