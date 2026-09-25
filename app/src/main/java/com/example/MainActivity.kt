package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.IncidentResponseStatus
import com.example.data.repository.TetherGuardRepository
import com.example.data.storage.SecureStorage
import com.example.notifications.TetherNotificationManager
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.incidents.IncidentHistoryScreen
import com.example.ui.pairing.PairingScreen
import com.example.ui.pairing.PairingViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.TetherGuardTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object Pairing : Screen()
    object History : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val repository = TetherGuardRepository.getInstance(applicationContext)
        val secureStorage = SecureStorage(applicationContext)

        val launchedFromNotification = intent?.hasExtra(TetherNotificationManager.EXTRA_INCIDENT_ID) == true

        setContent {
            TetherGuardTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = CyberBackground
                ) {
                    TetherGuardApp(
                        repository = repository,
                        secureStorage = secureStorage,
                        initialScreen = if (launchedFromNotification) Screen.History else Screen.Dashboard
                    )
                }
            }
        }
    }
}

@Composable
fun TetherGuardApp(
    repository: TetherGuardRepository,
    secureStorage: SecureStorage,
    initialScreen: Screen = Screen.Dashboard
) {
    var currentScreen by remember { mutableStateOf(initialScreen) }
    val coroutineScope = rememberCoroutineScope()

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(repository)
    )

    val pairingViewModel: PairingViewModel = viewModel(
        factory = PairingViewModel.Factory(repository)
    )

    val pairedDevice by repository.pairedDevice.collectAsState()
    val allIncidents by dashboardViewModel.allIncidents.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            Screen.Dashboard -> {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToPairing = {
                        pairingViewModel.resetScanner()
                        currentScreen = Screen.Pairing
                    },
                    onNavigateToHistory = {
                        currentScreen = Screen.History
                    },
                    onNavigateToSettings = {
                        currentScreen = Screen.Settings
                    }
                )
            }
            Screen.Pairing -> {
                PairingScreen(
                    viewModel = pairingViewModel,
                    onBack = { currentScreen = Screen.Dashboard },
                    onPairingComplete = { currentScreen = Screen.Dashboard }
                )
            }
            Screen.History -> {
                IncidentHistoryScreen(
                    incidents = allIncidents,
                    onBack = { currentScreen = Screen.Dashboard },
                    onClearHistory = {
                        coroutineScope.launch {
                            repository.clearHistory()
                        }
                    },
                    onKeepLocked = { incidentId ->
                        coroutineScope.launch {
                            repository.sendLockCommand()
                            repository.resolveIncident(incidentId, IncidentResponseStatus.LOCKED)
                        }
                    },
                    onRemoteShutdown = {
                        repository.sendShutdownCommand()
                    }
                )
            }
            Screen.Settings -> {
                SettingsScreen(
                    pairedDevice = pairedDevice,
                    secureStorage = secureStorage,
                    onBack = { currentScreen = Screen.Dashboard },
                    onUnpair = {
                        repository.unpairDevice()
                        currentScreen = Screen.Dashboard
                    },
                    onNavigateToPairing = {
                        pairingViewModel.resetScanner()
                        currentScreen = Screen.Pairing
                    }
                )
            }
        }
    }
}
