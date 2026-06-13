package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object DashboardRoute

@Serializable
object ProvidersRoute

@Serializable
object SettingsRoute

@Serializable
data class AddProviderRoute(val modelId: Int = -1)
