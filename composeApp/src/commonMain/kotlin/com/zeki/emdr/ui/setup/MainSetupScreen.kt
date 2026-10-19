package com.zeki.emdr.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.ui.components.StartSessionButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainSetupScreen(
    repository: SessionSettingsRepository,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainSetupViewModel = viewModel { MainSetupViewModel(repository) },
) {
    val settings by viewModel.settings.collectAsState()
    val colors = emdrColors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = colors.text,
            )
            Text(
                text = stringResource(Res.string.setup_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
            SessionPreferencesForm(
                settings = settings,
                onMode = viewModel::setMode,
                onOrbColor = viewModel::setOrbColor,
                onPattern = viewModel::setPattern,
                onOrbSize = viewModel::setOrbSize,
                onAudioSound = viewModel::setAudioSound,
                onPreviewSound = viewModel::previewSound,
                onBpm = viewModel::setBpm,
                onDuration = viewModel::setDuration,
                onSessionType = viewModel::setSessionType,
                onSetLength = viewModel::setSetLength,
                onThemePreference = viewModel::setThemePreference,
                onAllowBackgroundPlayback = viewModel::setAllowBackgroundPlayback,
                onHapticsEnabled = viewModel::setHapticsEnabled,
            )
            SupporterCard(repository = repository)
        }

        StartSessionButton(
            onClick = {
                viewModel.markFirstLaunchComplete()
                onStartSession()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
        )
    }
}
