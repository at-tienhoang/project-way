package vn.asiantech.way.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hypertrack.lib.models.User
import org.jetbrains.anko.AnkoContext
import vn.asiantech.way.extension.observeOnUiThread
import vn.asiantech.way.ui.base.BaseFragment

/**
 * Fragment create group
 * Created by haingoq on 28/11/2017.
 */
class CreateGroupFragment : BaseFragment() {
    companion object {
        const val KEY_USER = "user"

        /**
         * Get instance with given user.
         */
        fun getInstance(user: User): CreateGroupFragment {
            val instance = CreateGroupFragment()
            val bundle = Bundle()
            bundle.putSerializable(KEY_USER, user)
            instance.arguments = bundle
            return instance
        }
    }

    private lateinit var createGroupViewModel: CreateGroupViewModel
    private lateinit var ui: CreateGroupFragmentUI

    private var user: User? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?) = CreateGroupFragmentUI()
            .createView(AnkoContext.create(context, this))

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments.getSerializable(KEY_USER) as? User
        ui = CreateGroupFragmentUI()
        createGroupViewModel = CreateGroupViewModel(activity)
    }

    override fun onBindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Create Group
     */
    internal fun createGroup() {
        addDisposables(createGroupViewModel.createGroup(user?.id, ui.edtGroupName.text.toString())
                .observeOnUiThread()
                .subscribe(this::createGroupSuccess, this::createGroupFail))
    }

    private fun createGroupSuccess(boolean: Boolean) {
        TODO("Will update code later")
    }

    private fun createGroupFail(throwable: Throwable) {
        TODO("Will update code later")
    }
}
