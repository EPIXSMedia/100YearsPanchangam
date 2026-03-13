package com.panchangam100.live

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.ui.navigation.AppNavigation
import com.panchangam100.live.ui.theme.PanchangamTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModePref by prefs.darkModeFlow.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = darkModePref ?: systemDark

            PanchangamTheme(darkTheme = isDark) {
                AppNavigation(prefs = prefs)
            }
        }
    }
}
