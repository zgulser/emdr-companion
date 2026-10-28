package com.zeki.emdr.ui.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.isSettingsHighlightLaunch
import com.zeki.emdr.theme.EmdrPrimary
import com.zeki.emdr.session.BilateralOffset
import com.zeki.emdr.session.SessionPhase
import com.zeki.emdr.session.SessionRuntime
import com.zeki.emdr.session.SomaticBreath
import com.zeki.emdr.theme.EmdrButtonShape
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.theme.emdrTouchTarget
import com.zeki.emdr.ui.components.BpmSlider
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.ui.localizedLabel
import com.zeki.emdr.ui.setup.MainSetupViewModel
import com.zeki.emdr.ui.setup.MilestoneSupportSheet
import com.zeki.emdr.ui.setup.SessionPreferencesForm
import com.zeki.emdr.ui.setup.SupporterCard
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

private const val ChromeIdleHideMs = 3_000L
private const val ChromeTapFadeMs = 1_500
private const val ChromeButtonFadeMs = 1_500
private const val GroundingFadeMs = 1_500
private const val SettingsBeaconPulseMs = 1_000
private const val SettingsBeaconRepeats = 2
private val SettingsBeaconRingSize = 96.dp

@Composable
fun ActiveSessionScreen(
    repository: SessionSettingsRepository,
    modifier: Modifier = Modifier,
    sessionKey: Int = 0,
    startInReady: Boolean = true,
    viewModel: ActiveSessionViewModel = viewModel(key = "active-session-$sessionKey") {
        ActiveSessionViewModel(repository, startInReady)
    },
) {
    val settings by viewModel.settings.collectAsState()
    val runtime by viewModel.runtimeState.collectAsState()
    val resetEpoch by viewModel.resetEpoch.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val elapsedActiveSeconds by viewModel.elapsedActiveSeconds.collectAsState()
    val milestone by viewModel.milestoneSheet.collectAsState()
    val colors = emdrColors()
    val isReady = runtime.isReady
    val isPaused = runtime.isPaused
    val isComplete = runtime.isComplete
    val isResting = runtime.isResting
    val isBetweenSets = runtime.isBetweenSets

    var chromeVisible by remember { mutableStateOf(true) }
    var chromeFadeMs by remember { mutableIntStateOf(ChromeTapFadeMs) }
    var interactionEpoch by remember { mutableIntStateOf(0) }
    var showPreferences by remember { mutableStateOf(false) }
    var settingsBeaconDismissed by remember { mutableStateOf(false) }
    val showSettingsBeacon =
        isReady &&
            !settingsBeaconDismissed &&
            isSettingsHighlightLaunch(settings.appLaunchCount)

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, settings.allowBackgroundPlayback) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !settings.allowBackgroundPlayback) {
                viewModel.pauseIfRunning()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(resetEpoch) {
        chromeVisible = true
    }

    fun revealChromeByTap() {
        chromeFadeMs = ChromeTapFadeMs
        chromeVisible = true
        interactionEpoch += 1
    }

    fun hideChromeByButton() {
        chromeFadeMs = ChromeButtonFadeMs
        chromeVisible = false
        interactionEpoch += 1
    }

    val holdChrome = runtime.holdChrome || showPreferences
    LaunchedEffect(chromeVisible, holdChrome, interactionEpoch) {
        if (chromeVisible && !holdChrome) {
            kotlinx.coroutines.delay(ChromeIdleHideMs)
            chromeFadeMs = ChromeTapFadeMs
            chromeVisible = false
        }
    }

    val chromeAlpha by animateFloatAsState(
        targetValue = if (chromeVisible || runtime.holdChrome) 1f else 0f,
        animationSpec = tween(durationMillis = chromeFadeMs),
        label = "sessionChrome",
    )
    val groundingAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0f,
        animationSpec = tween(durationMillis = GroundingFadeMs),
        label = "groundingAnchor",
    )
    val settleProgress by animateFloatAsState(
        targetValue = if (runtime.phase == SessionPhase.Running || runtime.phase == SessionPhase.Paused) 0f else 1f,
        animationSpec = tween(durationMillis = SessionRuntime.SETTLE_MS),
        label = "motionSettle",
    )
    val orbAlpha by animateFloatAsState(
        targetValue = if (runtime.phase == SessionPhase.Ready || runtime.phase == SessionPhase.Running) 1f else 0f,
        animationSpec = tween(durationMillis = SessionRuntime.SETTLE_MS),
        label = "orbFade",
    )
    val breathAlpha by animateFloatAsState(
        targetValue = if (runtime.showBreathRing) 1f else 0f,
        animationSpec = tween(durationMillis = SessionRuntime.SETTLE_MS),
        label = "breathRest",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(chromeVisible, runtime.phase, showPreferences) {
                detectTapGestures {
                    if (isComplete || showPreferences || isResting || isBetweenSets) return@detectTapGestures
                    if (isReady) {
                        settingsBeaconDismissed = true
                        viewModel.beginSet()
                    } else {
                        revealChromeByTap()
                    }
                    if (isReady) interactionEpoch += 1
                }
            },
    ) {
        if (settings.mode.usesVisual && orbAlpha > 0f) {
            SessionOrbMotionLayer(
                offset = viewModel.offset,
                isReady = isReady,
                settleProgress = settleProgress,
                color = settings.orbColor.color,
                diameterDp = settings.orbSize.diameterDp,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(orbAlpha),
            )
        } else if (!settings.mode.usesVisual && !isComplete && !runtime.showBreathRing && !isBetweenSets) {
            AudioOnlyHint(ready = isReady)
        }

        if (breathAlpha > 0f) {
            BreathRestIndicator(
                alpha = breathAlpha,
                color = settings.orbColor.color,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (groundingAlpha > 0f) {
            GroundingAnchorBanner(
                alpha = groundingAlpha,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 72.dp)
                    .padding(horizontal = 20.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(chromeAlpha)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .then(
                    if (isReady && !isComplete && !showPreferences) {
                        Modifier.pointerInput(isReady) {
                            detectTapGestures {
                                settingsBeaconDismissed = true
                                viewModel.beginSet()
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            SessionTopBar(
                title = when (runtime.phase) {
                    SessionPhase.Complete -> stringResource(Res.string.phase_complete)
                    SessionPhase.Ready -> stringResource(Res.string.phase_ready)
                    SessionPhase.Paused -> stringResource(Res.string.phase_paused)
                    SessionPhase.Resting -> stringResource(Res.string.phase_breathe_rest)
                    SessionPhase.BetweenSets -> stringResource(Res.string.phase_rest_complete)
                    SessionPhase.Running -> settings.mode.localizedLabel()
                },
                remainingLabel = when {
                    isReady -> stringResource(Res.string.soft_pulse)
                    isResting -> stringResource(Res.string.let_set_settle)
                    isBetweenSets -> stringResource(Res.string.set_number, runtime.setsCompleted)
                    settings.sessionType == SessionType.SETS ->
                        stringResource(
                            Res.string.passes_progress,
                            runtime.passesInSet,
                            settings.setLength.passes,
                        )
                    else -> remainingLabel(settings, remainingSeconds, elapsedActiveSeconds)
                },
                onEnd = viewModel::returnToReady,
                interactive = chromeVisible || runtime.holdChrome,
                showEnd = !isReady,
            )

            when {
                isComplete -> SessionCompletePanel(onDone = viewModel::returnToReady)
                isResting -> Spacer(Modifier.height(56.dp))
                isBetweenSets -> BetweenSetsPanel(
                    onNext = {
                        viewModel.beginSet()
                        hideChromeByButton()
                    },
                    onDone = viewModel::markComplete,
                )
                else -> SessionControls(
                    isPaused = isPaused || isReady,
                    bpm = settings.bpm,
                    onTogglePause = {
                        if (isReady) settingsBeaconDismissed = true
                        viewModel.togglePause()
                        hideChromeByButton()
                    },
                    onBpmChange = {
                        viewModel.setBpm(it)
                        revealChromeByTap()
                    },
                    interactive = chromeVisible || isReady,
                    ready = isReady,
                )
            }
        }

        if (!chromeVisible && !runtime.holdChrome && !showPreferences) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(interactionEpoch) {
                        detectTapGestures {
                            revealChromeByTap()
                        }
                    },
            )
        }

        PersistentSettingsGear(
            onClick = {
                settingsBeaconDismissed = true
                showPreferences = true
                revealChromeByTap()
            },
            showBeacon = showSettingsBeacon,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        if (showPreferences) {
            SessionPreferencesSheet(
                repository = repository,
                onDismiss = { showPreferences = false },
            )
        }

        milestone?.let { pending ->
            MilestoneSupportSheet(
                milestone = pending,
                repository = repository,
                onDismiss = viewModel::dismissMilestoneSheet,
            )
        }
    }
}

@Composable
private fun SessionOrbMotionLayer(
    offset: StateFlow<BilateralOffset>,
    isReady: Boolean,
    settleProgress: Float,
    color: Color,
    diameterDp: Int,
    modifier: Modifier = Modifier,
) {
    val pose = offset.collectAsState()
    val pulseScale = if (isReady) rememberSoftPulse() else 1f
    SessionOrbCanvas(
        offset = pose,
        isReady = isReady,
        settleProgress = settleProgress,
        color = color,
        diameterDp = diameterDp,
        pulseScale = pulseScale,
        modifier = modifier,
    )
}

@Composable
private fun rememberSoftPulse(
    durationMs: Int = 2_200,
    minScale: Float = 0.92f,
    maxScale: Float = 1.08f,
): Float {
    val transition = rememberInfiniteTransition(label = "softPulse")
    val pulse by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "softPulseScale",
    )
    return pulse
}

@Composable
private fun GroundingAnchorBanner(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val colors = emdrColors()
    Text(
        text = stringResource(Res.string.grounding_banner),
        style = MaterialTheme.typography.bodyLarge,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
    )
}

@Composable
private fun BreathRestIndicator(
    alpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val colors = emdrColors()
    val transition = rememberInfiniteTransition(label = "somaticBreath")
    val cycleProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SomaticBreath.CYCLE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "somaticBreathCycle",
    )
    val scale = SomaticBreath.scale(cycleProgress)
    Column(
        modifier = modifier.alpha(alpha),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val radius = size.minDimension * 0.28f * scale
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.06f), Color.Transparent),
                    center = center,
                    radius = radius * 1.55f,
                ),
                radius = radius * 1.55f,
                center = center,
            )
            drawCircle(
                color = color.copy(alpha = 0.88f),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx()),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(
                if (SomaticBreath.isInhale(cycleProgress)) {
                    Res.string.breathe_in
                } else {
                    Res.string.breathe_out
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
    }
}

@Composable
private fun BetweenSetsPanel(
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = emdrColors()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.between_sets_copy),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            onClick = onNext,
            shape = EmdrButtonShape,
            color = colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .emdrTouchTarget()
                .height(56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.next_set), style = MaterialTheme.typography.titleMedium, color = colors.onPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onDone,
            shape = EmdrButtonShape,
            color = colors.surfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .emdrTouchTarget()
                .height(56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.done), style = MaterialTheme.typography.titleMedium, color = colors.text)
            }
        }
    }
}

@Composable
private fun PersistentSettingsGear(
    onClick: () -> Unit,
    showBeacon: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = emdrColors()
    val beaconProgress = rememberSettingsBeaconProgress(active = showBeacon)
    Box(
        modifier = modifier.size(EmdrMinTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        if (beaconProgress != null) {
            Canvas(
                modifier = Modifier
                    .wrapContentSize(unbounded = true, align = Alignment.Center)
                    .size(SettingsBeaconRingSize),
            ) {
                val minRadius = EmdrMinTouchTarget.toPx() * 0.38f
                val maxRadius = size.minDimension * 0.46f
                val radius = minRadius + (maxRadius - minRadius) * beaconProgress
                drawCircle(
                    color = EmdrPrimary.copy(alpha = 0.4f * (1f - beaconProgress)),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        Surface(
            onClick = onClick,
            shape = EmdrButtonShape,
            color = colors.surfaceElevated,
            modifier = Modifier.size(EmdrMinTouchTarget),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(Res.string.preferences),
                    tint = colors.text,
                )
            }
        }
    }
}

@Composable
private fun rememberSettingsBeaconProgress(active: Boolean): Float? {
    val progress = remember { Animatable(0f) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(active) {
        if (!active) {
            running = false
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        running = true
        repeat(SettingsBeaconRepeats) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = SettingsBeaconPulseMs,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        running = false
        progress.snapTo(0f)
    }
    return if (running) progress.value else null
}

@Composable
private fun SessionPreferencesSheet(
    repository: SessionSettingsRepository,
    onDismiss: () -> Unit,
) {
    val colors = emdrColors()
    val setupViewModel = viewModel { MainSetupViewModel(repository) }
    val settings by setupViewModel.settings.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                },
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.background,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 36.dp, height = 4.dp)
                        .background(colors.outline, EmdrButtonShape),
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.preferences),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.text,
                    )
                    Surface(
                        onClick = onDismiss,
                        shape = EmdrButtonShape,
                        color = colors.surfaceElevated,
                        modifier = Modifier.size(EmdrMinTouchTarget),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.close_preferences), tint = colors.text)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SessionPreferencesForm(
                        settings = settings,
                        onMode = setupViewModel::setMode,
                        onOrbColor = setupViewModel::setOrbColor,
                        onPattern = setupViewModel::setPattern,
                        onOrbSize = setupViewModel::setOrbSize,
                        onAudioSound = setupViewModel::setAudioSound,
                        onBpm = setupViewModel::setBpm,
                        onDuration = setupViewModel::setDuration,
                        onSessionType = setupViewModel::setSessionType,
                        onSetLength = setupViewModel::setSetLength,
                        onThemePreference = setupViewModel::setThemePreference,
                        onAllowBackgroundPlayback = setupViewModel::setAllowBackgroundPlayback,
                        onHapticsEnabled = setupViewModel::setHapticsEnabled,
                    )
                    SupporterCard(repository = repository)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionOrbCanvas(
    offset: State<BilateralOffset>,
    isReady: Boolean,
    settleProgress: Float,
    color: Color,
    diameterDp: Int,
    pulseScale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.drawWithCache {
            val radius = (diameterDp.dp.toPx() / 2f) * pulseScale
            val glowRadius = radius * 3.1f
            val inset = radius * 2.4f
            val glowColors = listOf(
                color.copy(alpha = 0.55f),
                color.copy(alpha = 0.12f),
                Color.Transparent,
            )
            onDrawBehind {
                val pose = offset.value
                val display = if (isReady) {
                    BilateralOffset(0f, 0f)
                } else {
                    BilateralOffset(
                        x = pose.x * (1f - settleProgress),
                        y = pose.y * (1f - settleProgress),
                    )
                }
                val travelX = (size.width / 2f) - inset
                val travelY = (size.height / 2f) - inset
                // offset.x = -1 draws at field x = 0; offset.x = +1 draws at field x = max.
                val center = Offset(
                    x = size.width / 2f + (display.x * travelX),
                    y = size.height / 2f + (display.y * travelY),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = glowColors,
                        center = center,
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = center,
                )
                drawCircle(color = color, radius = radius, center = center)
            }
        },
    )
}

@Composable
private fun AudioOnlyHint(ready: Boolean) {
    val colors = emdrColors()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(if (ready) Res.string.phase_ready else Res.string.audio_only),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(if (ready) Res.string.soft_pulse else Res.string.listen),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
        )
    }
}

@Composable
private fun SessionTopBar(
    title: String,
    remainingLabel: String,
    onEnd: () -> Unit,
    interactive: Boolean,
    showEnd: Boolean,
) {
    val colors = emdrColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showEnd) {
            SessionIconButton(
                onClick = onEnd,
                enabled = interactive,
            ) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.return_to_ready), tint = colors.text)
            }
        } else {
            Spacer(Modifier.size(EmdrMinTouchTarget))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.text)
            Text(text = remainingLabel, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        Spacer(Modifier.size(EmdrMinTouchTarget))
    }
}

@Composable
private fun SessionControls(
    isPaused: Boolean,
    bpm: Int,
    onTogglePause: () -> Unit,
    onBpmChange: (Int) -> Unit,
    interactive: Boolean,
    ready: Boolean,
) {
    val colors = emdrColors()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onTogglePause,
            enabled = interactive,
            shape = EmdrButtonShape,
            color = colors.primary,
            modifier = Modifier
                .emdrTouchTarget()
                .size(width = 72.dp, height = 56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = stringResource(
                        when {
                            ready -> Res.string.begin_set
                            isPaused -> Res.string.resume
                            else -> Res.string.pause
                        },
                    ),
                    tint = colors.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = EmdrButtonShape,
            color = colors.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                BpmSlider(
                    bpm = bpm,
                    onBpmChange = onBpmChange,
                    enabled = interactive,
                )
            }
        }
    }
}

@Composable
private fun SessionCompletePanel(onDone: () -> Unit) {
    val colors = emdrColors()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.session_complete_copy),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            onClick = onDone,
            shape = EmdrButtonShape,
            color = colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .emdrTouchTarget()
                .height(56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.done), style = MaterialTheme.typography.titleMedium, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun SessionIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = emdrColors()
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = EmdrButtonShape,
        color = colors.surfaceElevated,
        modifier = Modifier.size(EmdrMinTouchTarget),
    ) {
        Box(contentAlignment = Alignment.Center, content = { content() })
    }
}

private fun remainingLabel(
    settings: SessionSettings,
    remainingSeconds: Int,
    elapsedActiveSeconds: Int,
): String {
    val limit = settings.duration.seconds
    return if (limit == null) {
        formatClock(elapsedActiveSeconds)
    } else {
        formatClock(remainingSeconds)
    }
}

private fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
