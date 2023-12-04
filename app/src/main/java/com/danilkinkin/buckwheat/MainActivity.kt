package com.danilkinkin.buckwheat

import OverrideLocalize
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.danilkinkin.buckwheat.base.balloon.BalloonProvider
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.di.migrateToDataStore
import com.danilkinkin.buckwheat.home.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.ThemeMode
import com.danilkinkin.buckwheat.ui.syncTheme
import com.danilkinkin.buckwheat.util.locScreenOrientation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import syncOverrideLocale
import java.util.*
import javax.inject.Inject

val Context.budgetDataStore by preferencesDataStore("budget")
val Context.settingsDataStore by preferencesDataStore("settings")
var Context.appTheme by mutableStateOf(ThemeMode.SYSTEM)
var Context.appLocale: Locale? by mutableStateOf(null)
var Context.systemLocale: Locale? by mutableStateOf(null)

val LocalWindowSize = compositionLocalOf { WindowWidthSizeClass.Compact }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val isDone: MutableState<Boolean> = mutableStateOf(false)

    //TODO: Remove after 01.01.2024. Need for migration to DataStore
    @Inject
    lateinit var storageDao: StorageDao

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this.applicationContext
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen().setKeepOnScreenCondition { !isDone.value }
        lifecycleScope.launch {
            context.settingsDataStore.data.first()
        }

        super.onCreate(savedInstanceState)

        setContent {
            val localContext = LocalContext.current
            val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current

            LaunchedEffect(Unit) {
                syncTheme(localContext)
                syncOverrideLocale(localContext)
                migrateToDataStore(context, storageDao)

                isDone.value = true
            }

            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                locScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }

            if (isDone.value) {
                BuckwheatTheme {
                    OverrideLocalize {
                        BalloonProvider {
                            CompositionLocalProvider(
                                LocalWindowSize provides widthSizeClass
                            ) {
                                MainScreen(activityResultRegistryOwner)
                            }
                        }
                    }
                }
            }
        }
    }
}
