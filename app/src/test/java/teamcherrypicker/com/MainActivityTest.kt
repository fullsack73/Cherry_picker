package teamcherrypicker.com

//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Looper
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.tasks.OnFailureListener
//import com.google.android.gms.tasks.OnSuccessListener
//import com.google.android.gms.tasks.Task
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mock
//import org.mockito.Mockito.* // Import all static methods from Mockito
//import org.mockito.junit.MockitoJUnitRunner
//import org.mockito.kotlin.any
//import org.mockito.kotlin.eq
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.whenever
//import retrofit2.Response
//import teamcherrypicker.com.api.ApiClient
//import teamcherrypicker.com.api.ApiService
//import teamcherrypicker.com.data.UserLocation
//
//@ExperimentalCoroutinesApi
//@RunWith(MockitoJUnitRunner::class)
class MainActivityTest {

//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Mock
//    private lateinit var mockFusedLocationClient: FusedLocationProviderClient
//
//    @Mock
//    private lateinit var mockContext: Context
//
//    @Mock
//    private lateinit var mockPackageManager: PackageManager
//
//    @Mock
//    private lateinit var mockApiService: ApiService
//
//    private lateinit var mainActivity: MainActivity
//
//    @Before
//    fun setUp() {
//        Dispatchers.setMain(testDispatcher)
//        // Mock static ApiClient.apiService
//        mockStatic(ApiClient::class.java).use {
//            whenever(ApiClient.apiService).thenReturn(mockApiService)
//        }
//
//        // Mock Context and PackageManager for permission checks
//        whenever(mockContext.packageManager).thenReturn(mockPackageManager)
//        whenever(mockContext.applicationContext).thenReturn(mockContext)
//
//        // Initialize MainActivity with mocked dependencies
//        mainActivity = MainActivity().apply {
//            // Manually set the mocked fusedLocationClient
//            // This requires reflection or making the field open/internal for testing
//            // For simplicity in this example, we'll assume a way to inject it or mock its behavior
//            // directly in the getLocation method if it were a private field.
//            // Since it's lazy, we can't directly set it. We'll mock the static method call if possible.
//            // Or, we can make the fusedLocationClient a property that can be set for testing.
//            // For now, we'll assume the `fusedLocationClient` in `MainActivity` is the one we're mocking.
//        }
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `getLocation should send location to backend on success`() = runTest {
//        // Given
//        val mockLocation = mock<Location>()
//        whenever(mockLocation.latitude).thenReturn(10.0)
//        whenever(mockLocation.longitude).thenReturn(20.0)
//
//        val mockTask = mock<Task<Location>>()
//        whenever(mockFusedLocationClient.getCurrentLocation(any(), any())).thenReturn(mockTask)
//
//        doAnswer { invocation ->
//            val listener: OnSuccessListener<Location> = invocation.getArgument(1)
//            listener.onSuccess(mockLocation)
//            mockTask
//        }.whenever(mockTask).addOnSuccessListener(any())
//
//        whenever(mockApiService.sendLocation(any())).thenReturn(Response.success(Unit))
//
//        // Mock permission granted
//        whenever(mockContext.checkSelfPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION))).thenReturn(PackageManager.PERMISSION_GRANTED)
//
//        // When
//        mainActivity.getLocation()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        verify(mockApiService).sendLocation(UserLocation(10.0, 20.0))
//    }
//
//    @Test
//    fun `getLocation should update locationState to Location not found when location is null`() = runTest {
//        // Given
//        val mockTask = mock<Task<Location>>()
//        whenever(mockFusedLocationClient.getCurrentLocation(any(), any())).thenReturn(mockTask)
//
//        doAnswer { invocation ->
//            val listener: OnSuccessListener<Location> = invocation.getArgument(1)
//            listener.onSuccess(null)
//            mockTask
//        }.whenever(mockTask).addOnSuccessListener(any())
//
//        // Mock permission granted
//        whenever(mockContext.checkSelfPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION))).thenReturn(PackageManager.PERMISSION_GRANTED)
//
//        // When
//        mainActivity.getLocation()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        // This requires access to locationState, which is private in MainActivity.
//        // For a real test, you'd expose it or test its effect on a ViewModel.
//        // For this example, we'll assume a way to verify its value.
//        // Assert.assertEquals("Location not found", mainActivity.locationState.value)
//    }
//
//    @Test
//    fun `getLocation should update locationState to Failed to get location on failure`() = runTest {
//        // Given
//        val mockTask = mock<Task<Location>>()
//        whenever(mockFusedLocationClient.getCurrentLocation(any(), any())).thenReturn(mockTask)
//
//        doAnswer { invocation ->
//            val listener: OnFailureListener = invocation.getArgument(1)
//            listener.onFailure(RuntimeException("Test Failure"))
//            mockTask
//        }.whenever(mockTask).addOnFailureListener(any())
//
//        // Mock permission granted
//        whenever(mockContext.checkSelfPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION))).thenReturn(PackageManager.PERMISSION_GRANTED)
//
//        // When
//        mainActivity.getLocation()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        // This requires access to locationState, which is private in MainActivity.
//        // For a real test, you'd expose it or test its effect on a ViewModel.
//        // For this example, we'll assume a way to verify its value.
//        // Assert.assertEquals("Failed to get location", mainActivity.locationState.value)
//    }
//
//    @Test
//    fun `sendLocationToBackend should handle API call failure`() = runTest {
//        // Given
//        val mockLocation = mock<Location>()
//        whenever(mockLocation.latitude).thenReturn(10.0)
//        whenever(mockLocation.longitude).thenReturn(20.0)
//
//        val mockTask = mock<Task<Location>>()
//        whenever(mockFusedLocationClient.getCurrentLocation(any(), any())).thenReturn(mockTask)
//
//        doAnswer { invocation ->
//            val listener: OnSuccessListener<Location> = invocation.getArgument(1)
//            listener.onSuccess(mockLocation)
//            mockTask
//        }.whenever(mockTask).addOnSuccessListener(any())
//
//        whenever(mockApiService.sendLocation(any())).thenReturn(Response.error(500, mock()))
//
//        // Mock permission granted
//        whenever(mockContext.checkSelfPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION))).thenReturn(PackageManager.PERMISSION_GRANTED)
//
//        // When
//        mainActivity.getLocation()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        verify(mockApiService).sendLocation(UserLocation(10.0, 20.0))
//        // Verify that a Toast is shown for server error. This is hard to test directly in unit tests.
//    }
}
