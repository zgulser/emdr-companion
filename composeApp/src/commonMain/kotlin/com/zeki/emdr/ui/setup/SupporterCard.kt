package com.zeki.emdr.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zeki.emdr.billing.SupportPurchaseResult
import com.zeki.emdr.billing.SupportTip
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.EmdrButtonShape
import com.zeki.emdr.theme.EmdrCardShape
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.theme.emdrTouchTarget
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupporterCard(
    repository: SessionSettingsRepository,
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EmdrCardShape,
        color = colors.supporter,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(Res.string.supporter_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
            )
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = settings.hasSupportedApp,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "supporter-message",
            ) { supported ->
                Text(
                    text = stringResource(
                        if (supported) Res.string.supporter_thanks else Res.string.supporter_subtitle,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.height(16.dp))
            SupportTipButtons(
                prices = prices,
                enabled = busy == null,
                onSelect = viewModel::purchase,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.supporter_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportTipButtons(
    prices: Map<SupportTip, String>,
    enabled: Boolean,
    onSelect: (SupportTip) -> Unit,
) {
    val colors = emdrColors()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SupportTip.entries.forEach { tip ->
            Surface(
                onClick = { onSelect(tip) },
                enabled = enabled,
                shape = EmdrButtonShape,
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.outline),
                modifier = Modifier
                    .emdrTouchTarget()
                    .widthIn(min = 96.dp)
                    .heightIn(min = EmdrMinTouchTarget),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = prices[tip] ?: tip.fallbackPrice,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (enabled) colors.text else colors.textSecondary,
                    )
                }
            }
        }
    }
}
