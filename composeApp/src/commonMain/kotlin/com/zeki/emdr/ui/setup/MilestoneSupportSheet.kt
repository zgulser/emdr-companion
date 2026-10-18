package com.zeki.emdr.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zeki.emdr.billing.SupportPurchaseResult
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.model.SupportMilestone
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.EmdrButtonShape
import com.zeki.emdr.theme.EmdrCardShape
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.theme.emdrTouchTarget
import org.jetbrains.compose.resources.stringResource

@Composable
fun MilestoneSupportSheet(
    milestone: SupportMilestone,
    repository: SessionSettingsRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupportViewModel = viewModel(key = "supporter-card") {
        SupportViewModel(repository)
    },
) {
    val settings by viewModel.settings.collectAsState()
    val prices by viewModel.prices.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val status by viewModel.status.collectAsState()
    val colors = emdrColors()
    val title = stringResource(
        when (milestone) {
            SupportMilestone.Five -> Res.string.milestone_5_title
            SupportMilestone.Twenty -> Res.string.milestone_20_title
            SupportMilestone.Fifty -> Res.string.milestone_50_title
        },
    )
    val body = stringResource(
        when (milestone) {
            SupportMilestone.Five -> Res.string.milestone_5_copy
            SupportMilestone.Twenty -> Res.string.milestone_20_copy
            SupportMilestone.Fifty -> Res.string.milestone_50_copy
        },
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                },
        )
        Surface(
            shape = EmdrCardShape,
            color = colors.supporter,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.text,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (settings.hasSupportedApp) {
                        stringResource(Res.string.supporter_thanks)
                    } else {
                        body
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(16.dp))
                SupportTipButtons(
                    prices = prices,
                    enabled = busy == null,
                    onSelect = viewModel::purchase,
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    onClick = onDismiss,
                    shape = EmdrButtonShape,
                    color = colors.surfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .emdrTouchTarget()
                        .heightIn(min = EmdrMinTouchTarget),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.not_now),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                        )
                    }
                }
                val statusText = when (status) {
                    SupportPurchaseResult.Unavailable -> stringResource(Res.string.store_unavailable)
                    SupportPurchaseResult.Failed -> stringResource(Res.string.store_failed)
                    else -> null
                }
                if (statusText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}
