package vn.asiantech.way

import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import vn.asiantech.way.data.model.TrackingInformation
import vn.asiantech.way.data.source.LocalRepository
import vn.asiantech.way.ui.home.HomeViewModel

/**
 * HomeViewModelTest.
 *
 * @author at-ToanNguyen
 */
@Suppress("IllegalIdentifier")
class HomeViewModelTest {
    @Mock
    private lateinit var assetDataRepository: LocalRepository

    private lateinit var viewModel: HomeViewModel

    @Before
    fun initTest() {
        MockitoAnnotations.initMocks(this)
        viewModel = HomeViewModel(assetDataRepository)
    }

    @Test
    fun `Given tracking history - When get tracking history - Then return right list tracking history`() {
        val trackingHistory = mutableListOf<TrackingInformation>()
        val test = TestObserver<MutableList<TrackingInformation>>()
        `when`(assetDataRepository.getTrackingHistory()).thenReturn(trackingHistory)
        viewModel.getTrackingHistory().subscribe(test)
        test.assertValue { it == trackingHistory }
    }
}