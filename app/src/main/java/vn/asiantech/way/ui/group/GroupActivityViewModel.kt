package vn.asiantech.way.ui.group

import io.reactivex.Observable
import vn.asiantech.way.data.model.Group
import vn.asiantech.way.data.model.Invite
import vn.asiantech.way.data.source.GroupRepository
import vn.asiantech.way.extension.observeOnUiThread

/**
 * Copyright © 2017 Asian Tech Co., Ltd.
 * Created by cuongcaov on 04/12/2017
 */
class GroupActivityViewModel {

    private val groupDataSource = GroupRepository()

    internal fun getGroupInfo(groupId: String): Observable<Group> {
        return groupDataSource.getGroupInfo(groupId)
                .observeOnUiThread()
    }

    internal fun getGroupId(userId: String): Observable<String> {
        return groupDataSource.getGroupId(userId)
                .observeOnUiThread()
    }

    internal fun getInviteList(userId: String): Observable<Invite> {
        return groupDataSource.getInvite(userId)
                .observeOnUiThread()
    }
}