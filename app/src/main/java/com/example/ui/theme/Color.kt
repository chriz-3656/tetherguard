package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Professional Dark Enterprise Palette
val Slate950 = Color(0xFF080C14) // Main app background
val Slate900 = Color(0xFF0F1523) // Primary card background
val Slate850 = Color(0xFF161E30) // Elevated card / modal
val Slate800 = Color(0xFF1E283D) // Secondary surface / highlight
val Slate700 = Color(0xFF2B3752) // Outline & subtle dividers
val Slate600 = Color(0xFF43516F) // Subtle interactive borders

// Security Cobalt / Sapphire Brand
val Cobalt500 = Color(0xFF3B82F6) // Primary brand accent
val Cobalt600 = Color(0xFF2563EB) // Filled buttons
val Cobalt700 = Color(0xFF1D4ED8) // Active states
val Cobalt900 = Color(0xFF1E3A8A) // Dark cobalt container
val CobaltAlpha10 = Color(0x1A3B82F6)
val CobaltAlpha20 = Color(0x333B82F6)

// Protected State (Emerald)
val Emerald500 = Color(0xFF10B981) // Protected active green
val Emerald400 = Color(0xFF34D399) // Bright indicator
val Emerald900 = Color(0xFF064E3B) // Dark container
val EmeraldAlpha15 = Color(0x2610B981)

// Threat State (Crimson)
val Crimson500 = Color(0xFFEF4444) // Alert red
val Crimson600 = Color(0xFFDC2626) // Heavy warning
val Crimson900 = Color(0xFF450A0A) // Dark alert container
val CrimsonAlpha15 = Color(0x26EF4444)

// Caution State (Amber)
val Amber500 = Color(0xFFF59E0B) // Warning state
val Amber400 = Color(0xFFFBBF24) // Light indicator
val Amber900 = Color(0xFF451A03) // Dark warning container
val AmberAlpha15 = Color(0x26F59E0B)

// Typography & Content
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Backward compatible aliases to ensure complete build stability
val CyberBackground = Slate950
val CyberSurface = Slate900
val CyberSurfaceVariant = Slate850
val CyberSurfaceHighlight = Slate800
val CyberBorder = Slate700

val CyberPrimary = Cobalt500
val CyberPrimaryContainer = Cobalt900
val CyberOnPrimary = Color.White

val CyberSecondary = Cobalt500
val CyberSecondaryContainer = Cobalt900

val CyberProtectedGreen = Emerald500
val CyberProtectedGreenContainer = EmeraldAlpha15
val CyberProtectedGreenBorder = Emerald500

val CyberWarningAmber = Amber500
val CyberWarningAmberContainer = AmberAlpha15

val CyberAlertRed = Crimson500
val CyberAlertRedContainer = CrimsonAlpha15
val CyberAlertRedBorder = Crimson500

val CyberOfflineGray = Color(0xFF64748B)
val CyberOfflineContainer = Slate850

val CyberTextPrimary = TextPrimary
val CyberTextSecondary = TextSecondary
val CyberTextTertiary = TextTertiary
