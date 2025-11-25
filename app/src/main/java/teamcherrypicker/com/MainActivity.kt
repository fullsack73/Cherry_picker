package teamcherrypicker.com

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.data.UserLocation
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState
import teamcherrypicker.com.ui.main.AddCardFormScreen
import teamcherrypicker.com.ui.main.MainScreen
import teamcherrypicker.com.ui.main.ManageCardsScreen
import teamcherrypicker.com.ui.theme.Cherry_pickerTheme

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddCardScreen : Screen("add_card_screen")
    object AddCardFormScreen : Screen("add_card_form_screen")
    object SettingsScreen : Screen("settings_screen")
}

class MainActivity : ComponentActivity() {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val locationUiState = MutableStateFlow(LocationUiState())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                updatePermissionStatus(LocationPermissionStatus.Granted)
                getLocation()
            } else {
                updatePermissionStatus(LocationPermissionStatus.Denied)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            Cherry_pickerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
                    composable(Screen.MainScreen.route) {
                        MainScreen(
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            locationUiStateFlow = locationUiState
                        )
                    }
                    composable(Screen.AddCardScreen.route) {
                        ManageCardsScreen(navController)
                    }
                    composable(Screen.AddCardFormScreen.route) {
                        AddCardFormScreen(navController)
                    }
                    composable(Screen.SettingsScreen.route) {
                        // Placeholder for SettingsScreen content
                        Text("Settings Screen")
                    }
                }
            }
        }
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                updatePermissionStatus(LocationPermissionStatus.Granted)
                getLocation()
            }
            else -> {
                updatePermissionStatus(LocationPermissionStatus.Unknown)
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setLoading(true)
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        locationUiState.update {
                            it.copy(
                                isLoading = false,
                                lastKnownLocation = latLng,
                                lastUpdatedMillis = System.currentTimeMillis(),
                                permissionStatus = LocationPermissionStatus.Granted
                            )
                        }
                        sendLocationToBackend(location.latitude, location.longitude)
                    } else {
                        locationUiState.update { current ->
                            current.copy(isLoading = false)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    locationUiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to get location"
                        )
                    }
                }
        }
    }

    private fun sendLocationToBackend(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.sendLocation(UserLocation(latitude, longitude))
                if (response.isSuccessful) {
                    // Location sent successfully
                    Toast.makeText(this@MainActivity, "Location sent to server", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle server error
                    Toast.makeText(this@MainActivity, "Server error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network error
                Toast.makeText(this@MainActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePermissionStatus(status: LocationPermissionStatus) {
        locationUiState.update { it.copy(permissionStatus = status) }
    }

    private fun setLoading(loading: Boolean) {
        locationUiState.update { it.copy(isLoading = loading) }
    }
}
