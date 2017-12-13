package vn.asiantech.way.register

import com.hypertrack.lib.models.User
import com.hypertrack.lib.models.UserParams
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import vn.asiantech.way.TestUtil
import vn.asiantech.way.data.source.LocalRepository
import vn.asiantech.way.data.source.WayRepository
import vn.asiantech.way.data.source.remote.response.ResponseStatus
import vn.asiantech.way.ui.register.RegisterViewModel

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suppress("IllegalIdentifier")
class RegisterViewModelTest {
    @Mock
    private lateinit var wayRepository: WayRepository
    @Mock
    private lateinit var assetDataRepository: LocalRepository

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun initTests() {
        MockitoAnnotations.initMocks(this)
        viewModel = RegisterViewModel(wayRepository, assetDataRepository, true)
    }

    @Test
    fun `Given progress bar - When call update user - Then progress bar should show then hide`() {
        /* Given */
        val test = TestObserver<Boolean>()
        val param = UserParams()
        `when`(wayRepository.updateUser(TestUtil.any())).thenReturn(Observable.just(ResponseStatus(true)))

        /* When */
        viewModel.progressBarStatus.subscribe(test)
        viewModel.updateUser(param).subscribe()

        /* Then */
        test.assertValues(true, false)
    }

    @Test
    fun `Given an user - When get current user - Then return right user`() {
        /* Given */
        val user = User()
        val getUserTest = TestObserver<User>()
        `when`(wayRepository.getUser()).thenReturn(SingleSubject.just(user))

        /* When */
        viewModel.getUser().subscribe(getUserTest)

        /* Then */
        getUserTest.assertValue { it == user }
    }
}
