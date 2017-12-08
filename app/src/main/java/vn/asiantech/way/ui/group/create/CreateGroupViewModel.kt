package vn.asiantech.way.ui.group.create

import io.reactivex.Observable
import vn.asiantech.way.data.source.GroupRepository

/**
 * CreateGroupViewModel.
 *
 * @author at-ToanNguyen
 */
class CreateGroupViewModel {
    private val groupRepository = GroupRepository()

    internal fun createGroup(name: String, userId: String): Observable<Boolean> {
        return groupRepository.createGroup(name, userId).toObservable()
    }
}
