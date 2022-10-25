package com.danilkinkin.buckwheat

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import com.danilkinkin.buckwheat.home.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.syncTheme
import com.danilkinkin.buckwheat.util.locScreenOrientation

val Context.dataStore by preferencesDataStore("settings")
var Context.appTheme by mutableStateOf(ThemeMode.SYSTEM)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val isDone: MutableState<Boolean> = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen().setKeepOnScreenCondition { !isDone.value }

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                syncTheme(context)

                isDone.value = true
            }

            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                locScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }

            BuckwheatTheme {
                MainScreen(widthSizeClass)
            }
        }
    }
}