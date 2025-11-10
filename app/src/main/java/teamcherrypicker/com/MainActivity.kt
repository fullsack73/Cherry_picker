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
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import teamcherrypicker.com.api.ApiClient
import teamcherrypicker.com.data.UserLocation
import teamcherrypicker.com.ui.main.MainScreen
import teamcherrypicker.com.ui.theme.Cherry_pickerTheme

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddCardScreen : Screen("add_card_screen")
    object SettingsScreen : Screen("settings_screen")
}

class MainActivity : ComponentActivity() {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val locationState = mutableStateOf("Location not available")
    private var isLoading by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cherry_pickerTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
                    composable(Screen.MainScreen.route) {
                        MainScreen()
                    }
                    composable(Screen.AddCardScreen.route) {
                        // Placeholder for AddCardScreen content
                        Text("Add Card Screen")
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
                getLocation()
            }
            else -> {
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
            isLoading = true
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        locationState.value = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                        sendLocationToBackend(location.latitude, location.longitude)
                    } else {
                        locationState.value = "Location not found"
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    locationState.value = "Failed to get location"
                    isLoading = false
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
}
