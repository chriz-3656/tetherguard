package com.example.data.models

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTHENTICATING,
    ERROR;

    val isLive: Boolean
        get() = this == CONNECTED
}
