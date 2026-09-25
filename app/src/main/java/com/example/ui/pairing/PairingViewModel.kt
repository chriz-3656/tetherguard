package com.example.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.QrPairingData
import com.example.data.repository.TetherGuardRepository
import com.example.pairing.QrParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PairingUiState(
    val isScanning: Boolean = true,
    val rawPayload: String = "",
    val validatedData: QrPairingData? = null,
    val errorMessage: String? = null,
    val isPairingInProgress: Boolean = false,
    val isPairingSuccess: Boolean = false
)

class PairingViewModel(
    private val repository: TetherGuardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    fun onQrScanned(rawText: String) {
        val result = QrParser.parse(rawText)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                rawPayload = rawText,
                validatedData = result.getOrNull(),
                errorMessage = null,
                isScanning = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = result.exceptionOrNull()?.message ?: "Invalid QR Code"
            )
        }
    }

    fun loadDemoPayload(laptopType: String = "macbook") {
        val demoJson = if (laptopType == "thinkpad") {
            """
            {
              "device_id": "TG-4419",
              "device_name": "THINKPAD-X1-SECURITY",
              "public_key": "ed25519_pk_x1_990184",
              "nonce": "c9e2b10a",
              "relay_endpoint": "wss://relay.tetherguard.sec/ws",
              "secret_token": "tg_token_demo_x1"
            }
            """.trimIndent()
        } else {
            """
            {
              "device_id": "TG-8842",
              "device_name": "CHRIZ-LAPTOP",
              "public_key": "ed25519_pk_m3_884271",
              "nonce": "a8f3b9c0",
              "relay_endpoint": "wss://relay.tetherguard.sec/ws",
              "secret_token": "tg_token_demo_mac"
            }
            """.trimIndent()
        }
        onQrScanned(demoJson)
    }

    fun confirmPairing() {
        val data = _uiState.value.validatedData ?: return
        _uiState.value = _uiState.value.copy(isPairingInProgress = true)

        viewModelScope.launch {
            val result = repository.pairDevice(data)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isPairingInProgress = false,
                    isPairingSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isPairingInProgress = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Pairing failed"
                )
            }
        }
    }

    fun resetScanner() {
        _uiState.value = PairingUiState(isScanning = true)
    }

    class Factory(private val repository: TetherGuardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PairingViewModel(repository) as T
        }
    }
}
