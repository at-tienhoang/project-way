package vn.asiantech.way.ui.group.showinvite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.toast
import vn.asiantech.way.R
import vn.asiantech.way.data.model.Invite
import vn.asiantech.way.extension.observeOnUiThread
import vn.asiantech.way.ui.base.BaseFragment

/**
 *  Copyright © 2017 AsianTech inc.
 *  Created by hoavot on 05/12/2017.
 */
class ViewInviteFragment : BaseFragment() {

    companion object {

        private const val KEY_USER_ID = "key_user_id"

        /**
         * Get instance of ViewInviteFragment with a given user.
         */
        fun getInstance(userId: String): ViewInviteFragment {
            val instance = ViewInviteFragment()
            val bundle = Bundle()
            bundle.putString(KEY_USER_ID, userId)
            instance.arguments = bundle
            return instance
        }
    }

    private lateinit var ui: ViewInviteFragmentUI
    private lateinit var adapter: InviteListAdapter

    private var invites = mutableListOf<Invite>()
    private lateinit var viewModel: ViewInviteViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val userId = arguments.getString(KEY_USER_ID)
        viewModel = ViewInviteViewModel(userId)
        adapter = InviteListAdapter(context, invites)
        adapter.onOkClick = {
            eventOnButtonOkClick(it)
        }
        adapter.onCancelClick = {
            eventOnButtonCancelClick(it)
        }
        ui = ViewInviteFragmentUI(adapter)
        return ui.createView(AnkoContext.create(context, this))
    }

    override fun onBindViewModel() {
        addDisposables(
                viewModel
                        .getInvitesOfUser()
                        .observeOnUiThread()
                        .subscribe(
                                this::handleGetInviteSuccess,
                                this::handleGetInviteError),

                viewModel
                        .progressDialogObservable
                        .observeOnUiThread()
                        .subscribe(this::updateProgressDialog)
        )
    }

    private fun handleGetInviteSuccess(invite: Invite) {
        invites.add(invite)
        adapter.notifyDataSetChanged()
    }

    private fun handleGetInviteError(error: Throwable) {
        toast(error.message.toString())
    }

    private fun updateProgressDialog(show: Boolean) {
        if (show) {
            showProgressDialog()
        } else {
            hideProgressDialog()
        }
    }

    private fun eventOnButtonOkClick(invite: Invite) {
        addDisposables(
                viewModel
                        .acceptInvite(invite)
                        .observeOnUiThread()
                        .subscribe(
                                this::handleAcceptInviteSuccess,
                                this::handleAcceptInviteError
                        )
        )
    }

    private fun eventOnButtonCancelClick(invite: Invite) {
        addDisposables(viewModel
                .removeInviteUserFromGroup(invite)
                .observeOnUiThread()
                .subscribe(
                        this::handleRemoveInviteSuccess,
                        this::handleRemoveInviteError
                )
        )
    }

    private fun handleAcceptInviteSuccess(isOwner: Boolean) {
        toast(R.string.notify_success)
    }

    private fun handleAcceptInviteError(error: Throwable) {
        toast(error.message.toString())
    }

    private fun handleRemoveInviteSuccess(isSuccess: Boolean) {
        toast(R.string.notify_success)
    }

    private fun handleRemoveInviteError(error: Throwable) {
        toast(error.message.toString())
    }
}
