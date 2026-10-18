package com.zeki.emdr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.zeki.emdr.audio.AudioSamples
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.theme.EmdrTheme
import com.zeki.emdr.ui.session.ActiveSessionScreen
import com.zeki.emdr.ui.setup.MainSetupScreen

private sealed interface AppDestination {
    data object Setup : AppDestination
    data object Session : AppDestination
}

@Composable
private fun rememberOwnedViewModelStore(): ViewModelStoreOwner {
    val store = remember { ViewModelStore() }
    DisposableEffect(Unit) {
        onDispose { store.clear() }
    }
    return remember(store) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = store
        }
    }
}

@Composable
fun EmdrApp(
    repository: SessionSettingsRepository = remember { SessionSettingsRepository() },
) {
    val settings by repository.state.collectAsState()
    LaunchedEffect(Unit) {
        repository.recordAppLaunch()
        AudioSamples.ensureLoaded()
    }
    val viewModelStoreOwner = rememberOwnedViewModelStore()
    val systemLocale = Locale.current
    LaunchedEffect(systemLocale.toLanguageTag()) {
        // Re-resolve composeResources when the system language changes; no app restart.
    }
    EmdrTheme(preference = settings.themePreference) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            var destination by remember {
                mutableStateOf(
                    if (settings.hasCompletedFirstLaunch) {
                        AppDestination.Session
                    } else {
                        AppDestination.Setup
                    },
                )
            }
            var sessionGeneration by remember { mutableStateOf(0) }
            var startSessionInReady by remember {
                mutableStateOf(settings.hasCompletedFirstLaunch)
            }
            when (destination) {
                AppDestination.Setup -> MainSetupScreen(
                    repository = repository,
                    onStartSession = {
                        startSessionInReady = false
                        sessionGeneration += 1
                        destination = AppDestination.Session
                    },
                )
                AppDestination.Session -> ActiveSessionScreen(
                    repository = repository,
                    sessionKey = sessionGeneration,
                    startInReady = startSessionInReady,
                )
            }
        }
    }
}
