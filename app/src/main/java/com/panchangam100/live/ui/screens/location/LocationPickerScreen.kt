package com.panchangam100.live.ui.screens.location

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.AppLocation
import com.panchangam100.live.data.model.CityDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val fusedLocation: FusedLocationProviderClient
) : ViewModel() {

    val currentLocation = prefs.locationFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences.DEFAULT_LOCATION
    )

    fun selectCity(city: AppLocation) {
        viewModelScope.launch { prefs.setLocation(city) }
    }

    @SuppressWarnings("MissingPermission")
    fun useGpsLocation(onSuccess: (AppLocation) -> Unit, onFail: () -> Unit) {
        fusedLocation.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val appLoc = AppLocation(
                        name = "GPS Location",
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        timezone = java.util.TimeZone.getDefault().id
                    )
                    viewModelScope.launch { prefs.setLocation(appLoc) }
                    onSuccess(appLoc)
                } else onFail()
            }
            .addOnFailureListener { onFail() }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    prefs: AppPreferences,
    onBack: () -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val searchResults = remember(query) { CityDatabase.search(query) }
    val currentLocation by viewModel.currentLocation.collectAsState()

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var gpsError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // GPS button
            OutlinedButton(
                onClick = {
                    if (permissionState.status.isGranted) {
                        viewModel.useGpsLocation(
                            onSuccess = { onBack() },
                            onFail = { gpsError = true }
                        )
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, null)
                Spacer(Modifier.width(8.dp))
                Text("Use Current GPS Location")
            }

            if (gpsError) {
                Text("Could not get GPS location. Please select a city.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            // Current selection
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Current Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentLocation.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("%.4f°N, %.4f°E".format(currentLocation.latitude, currentLocation.longitude),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Search field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search city...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Results list
            val displayList = if (query.length >= 2) searchResults else CityDatabase.cities.take(50)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(displayList) { city ->
                    ListItem(
                        headlineContent = { Text(city.name, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Text(
                                if (city.stateName.isNotBlank()) "${city.stateName}, ${city.country}" else city.country,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            if (city.name == currentLocation.name && city.stateName == currentLocation.stateName) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectCity(city)
                            onBack()
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(0.3f))
                }
            }
        }
    }
}
