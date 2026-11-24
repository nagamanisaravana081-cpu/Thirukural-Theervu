package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TamilHeadingTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CalmClarityEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1.0f)

private val MandalaGold = Color(0xFFC8A138)
private val MandalaSoftGold = Color(0xFFE5CD78)
private val MandalaTerracotta = Color(0xFF76362F)
private val MandalaWarmClay = Color(0xFFA36D5C)
private val CalmClarityTitleLight = Color(0xFF6E2121)
private val CalmClaritySubtitleLight = Color(0xFF2F2E28)

@Composable
fun SplashScreen(
    isDarkMode: Boolean = false,
    onFinished: () -> Unit
) {
    val ringDraw = remember { Animatable(0f) }
    val outerUnfurl = remember { Animatable(0f) }
    val innerUnfurl = remember { Animatable(0f) }
    val lotusBloom = remember { Animatable(0f) }
    val titleRise = remember { Animatable(0f) }
    val subtitleRise = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "mandalaDrift")
    val outerDriftDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 54_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerDriftCW"
    )
    val innerDriftDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerDriftCCW"
    )

    LaunchedEffect(Unit) {
        launch {
            delay(60L)
            ringDraw.animateTo(1f, tween(1080, easing = CalmClarityEasing))
        }
        launch {
            delay(420L)
            outerUnfurl.animateTo(1f, tween(1260, easing = CalmClarityEasing))
        }
        launch {
            delay(660L)
            innerUnfurl.animateTo(1f, tween(1260, easing = CalmClarityEasing))
        }
        launch {
            delay(1020L)
            lotusBloom.animateTo(1f, tween(1200, easing = CalmClarityEasing))
        }
        launch {
            delay(1680L)
            titleRise.animateTo(1f, tween(680, easing = CalmClarityEasing))
        }
        launch {
            delay(2040L)
            subtitleRise.animateTo(1f, tween(650, easing = CalmClarityEasing))
        }
        delay(3700L)
        onFinished()
    }

    val splashBackground = if (isDarkMode) MaterialTheme.colorScheme.background else Color.White
    val lotusCenterFill = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
    val titleColor = if (isDarkMode) MaterialTheme.colorScheme.primary else CalmClarityTitleLight
    val subtitleColor = if (isDarkMode) MaterialTheme.colorScheme.onBackground else CalmClaritySubtitleLight
    val terracottaStroke = if (isDarkMode) Color(0xFFD98B7C) else MandalaTerracotta
    val warmClayFill = if (isDarkMode) Color(0xFFD99A87) else MandalaWarmClay
    val emblemContentDescription = stringResource(R.string.cd_splash_emblem)

    val density = LocalDensity.current
    val titleOffsetPx = with(density) { 16.dp.toPx() }
    val subtitleOffsetPx = with(density) { 10.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashBackground)
            .safeDrawingPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .padding(24.dp)
    ) {
        TextButton(
            onClick = onFinished,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .minimumInteractiveComponentSize()
                .testTag("splash_skip_button")
        ) {
            Text(
                text = stringResource(R.string.action_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CalmClarityMandalaCanvas(
                ringProgress = ringDraw.value,
                outerProgress = outerUnfurl.value,
                innerProgress = innerUnfurl.value,
                lotusProgress = lotusBloom.value,
                outerDriftDeg = if (outerUnfurl.value > 0.98f) outerDriftDeg else 0f,
                innerDriftDeg = if (innerUnfurl.value > 0.98f) innerDriftDeg else 0f,
                lotusCenterFill = lotusCenterFill,
                terracottaColor = terracottaStroke,
                warmClayColor = warmClayFill,
                modifier = Modifier
                    .size(204.dp)
                    .semantics { contentDescription = emblemContentDescription }
                    .testTag("splash_mandala_emblem")
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = stringResource(R.string.app_title_tamil),
                style = TamilHeadingTextStyle.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 40.sp
                ),
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = titleRise.value
                    translationY = (1f - titleRise.value) * titleOffsetPx
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.35.sp
                ),
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleRise.value
                    translationY = (1f - subtitleRise.value) * subtitleOffsetPx
                }
            )
        }

        Text(
            text = "Tap anywhere to continue",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun CalmClarityMandalaCanvas(
    ringProgress: Float,
    outerProgress: Float,
    innerProgress: Float,
    lotusProgress: Float,
    outerDriftDeg: Float,
    innerDriftDeg: Float,
    lotusCenterFill: Color,
    terracottaColor: Color,
    warmClayColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val unit = size.minDimension / 260f
        val center = Offset(size.width / 2f, size.height / 2f)

        fun sx(x: Float): Float = center.x + (x - 130f) * unit
        fun sy(y: Float): Float = center.y + (y - 130f) * unit

        // 1. Soft golden radial aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MandalaSoftGold.copy(alpha = 0.28f),
                    MandalaSoftGold.copy(alpha = 0f)
                ),
                center = center,
                radius = 126f * unit
            ),
            radius = 126f * unit,
            center = center
        )

        // 2. Drawing outer gold ring (r=118, stroke 2.1, sweeps from -90 deg)
        if (ringProgress > 0f) {
            val ringRadius = 118f * unit
            drawArc(
                color = MandalaGold,
                startAngle = -90f,
                sweepAngle = 360f * ringProgress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 2.1f * unit, cap = StrokeCap.Round)
            )
        }

        // 3. Outer Mandala (16 golden petals + warm clay accents + ring dividers)
        if (outerProgress > 0.01f) {
            val outerScale = 0.76f + 0.24f * outerProgress
            val outerRot = -8f * (1f - outerProgress) + outerDriftDeg
            val alphaFactor = outerProgress.coerceIn(0f, 1f)

            withTransform({
                scale(outerScale, outerScale, pivot = center)
                rotate(outerRot, pivot = center)
            }) {
                drawCircle(
                    color = MandalaGold.copy(alpha = 0.50f * alphaFactor),
                    radius = 111.5f * unit,
                    center = center,
                    style = Stroke(width = 0.7f * unit)
                )
                drawCircle(
                    color = MandalaGold.copy(alpha = 0.60f * alphaFactor),
                    radius = 93.5f * unit,
                    center = center,
                    style = Stroke(width = 0.85f * unit)
                )

                val outerPetalPath = buildOuterPetalPath(::sx, ::sy)
                val innerAccentPath = buildOuterAccentPath(::sx, ::sy)

                for (i in 0 until 16) {
                    val deg = i * 22.5f
                    withTransform({
                        rotate(deg, pivot = center)
                    }) {
                        drawPath(
                            path = outerPetalPath,
                            color = MandalaSoftGold.copy(alpha = 0.18f * alphaFactor)
                        )
                        drawPath(
                            path = outerPetalPath,
                            color = MandalaGold.copy(alpha = alphaFactor),
                            style = Stroke(width = 1.05f * unit)
                        )
                        drawPath(
                            path = innerAccentPath,
                            color = warmClayColor.copy(alpha = 0.42f * alphaFactor)
                        )
                        withTransform({
                            rotate(11.25f, pivot = center)
                        }) {
                            drawCircle(
                                color = MandalaGold.copy(alpha = 0.85f * alphaFactor),
                                radius = 1.4f * unit,
                                center = Offset(sx(130f), sy(24.5f))
                            )
                        }
                    }
                }
            }
        }

        // 4. Inner Mandala (16 terracotta petals + gold beads + inner ring)
        if (innerProgress > 0.01f) {
            val innerScale = 0.72f + 0.28f * innerProgress
            val innerRot = 10f * (1f - innerProgress) + innerDriftDeg
            val alphaFactor = innerProgress.coerceIn(0f, 1f)

            withTransform({
                scale(innerScale, innerScale, pivot = center)
                rotate(innerRot, pivot = center)
            }) {
                val innerPetalPath = buildInnerPetalPath(::sx, ::sy)

                for (i in 0 until 16) {
                    val deg = i * 22.5f
                    withTransform({
                        rotate(deg, pivot = center)
                    }) {
                        drawPath(
                            path = innerPetalPath,
                            color = terracottaColor.copy(alpha = 0.14f * alphaFactor)
                        )
                        drawPath(
                            path = innerPetalPath,
                            color = terracottaColor.copy(alpha = alphaFactor),
                            style = Stroke(width = 1.25f * unit)
                        )
                        drawCircle(
                            color = MandalaGold.copy(alpha = alphaFactor),
                            radius = 1.4f * unit,
                            center = Offset(sx(130f), sy(54.2f))
                        )
                    }
                }

                drawCircle(
                    color = terracottaColor.copy(alpha = 0.65f * alphaFactor),
                    radius = 65.5f * unit,
                    center = center,
                    style = Stroke(width = 1.05f * unit)
                )
            }
        }

        // 5. Blooming Lotus Core (pivot 130, 144)
        if (lotusProgress > 0.01f) {
            val lotusScale = 0.72f + 0.28f * lotusProgress
            val alphaFactor = lotusProgress.coerceIn(0f, 1f)
            val lotusPivot = Offset(sx(130f), sy(144f))
            val petalBasePivot = Offset(sx(130f), sy(148f))

            withTransform({
                scale(lotusScale, lotusScale, pivot = lotusPivot)
            }) {
                // Medallion center backdrop
                drawCircle(
                    color = lotusCenterFill.copy(alpha = alphaFactor),
                    radius = 59.5f * unit,
                    center = center
                )
                drawCircle(
                    color = MandalaGold.copy(alpha = alphaFactor),
                    radius = 59.5f * unit,
                    center = center,
                    style = Stroke(width = 1.25f * unit)
                )
                drawCircle(
                    color = MandalaSoftGold.copy(alpha = alphaFactor),
                    radius = 55f * unit,
                    center = center,
                    style = Stroke(width = 0.75f * unit)
                )

                // Graceful curved water/lotus base strokes
                val baseArc1 = Path().apply {
                    moveTo(sx(107f), sy(151.5f))
                    quadraticTo(sx(130f), sy(157.5f), sx(153f), sy(151.5f))
                }
                drawPath(
                    path = baseArc1,
                    color = MandalaGold.copy(alpha = alphaFactor),
                    style = Stroke(width = 1.7f * unit, cap = StrokeCap.Round)
                )

                val baseArc2 = Path().apply {
                    moveTo(sx(113f), sy(156.5f))
                    quadraticTo(sx(130f), sy(163f), sx(147f), sy(156.5f))
                }
                drawPath(
                    path = baseArc2,
                    color = MandalaGold.copy(alpha = alphaFactor),
                    style = Stroke(width = 1.35f * unit, cap = StrokeCap.Round)
                )

                // 7 layered symmetrical lotus petals
                drawLotusPetalPair(
                    angles = listOf(-64f, 64f),
                    pivot = petalBasePivot,
                    path = buildLotusPetal(::sx, ::sy, 122f, 135f, 121f, 116f, 104f, 139f, 138f),
                    fillAlpha = 0.28f * alphaFactor,
                    strokeAlpha = alphaFactor,
                    strokeWidth = 1.5f * unit
                )
                drawLotusPetalPair(
                    angles = listOf(-44f, 44f),
                    pivot = petalBasePivot,
                    path = buildLotusPetal(::sx, ::sy, 120f, 133f, 119f, 112f, 98f, 141f, 140f),
                    fillAlpha = 0.32f * alphaFactor,
                    strokeAlpha = alphaFactor,
                    strokeWidth = 1.6f * unit
                )
                drawLotusPetalPair(
                    angles = listOf(-24f, 24f),
                    pivot = petalBasePivot,
                    path = buildLotusPetal(::sx, ::sy, 120.5f, 132f, 120.5f, 109f, 94f, 139.5f, 139.5f),
                    fillAlpha = 0.36f * alphaFactor,
                    strokeAlpha = alphaFactor,
                    strokeWidth = 1.7f * unit
                )

                // Central crown lotus petal
                val centerPetal = buildLotusPetal(::sx, ::sy, 119f, 131f, 119.5f, 106f, 90f, 140.5f, 141f)
                drawPath(
                    path = centerPetal,
                    color = MandalaSoftGold.copy(alpha = 0.40f * alphaFactor)
                )
                drawPath(
                    path = centerPetal,
                    color = MandalaGold.copy(alpha = alphaFactor),
                    style = Stroke(width = 1.9f * unit)
                )
            }
        }
    }
}

private fun DrawScope.drawLotusPetalPair(
    angles: List<Float>,
    pivot: Offset,
    path: Path,
    fillAlpha: Float,
    strokeAlpha: Float,
    strokeWidth: Float
) {
    angles.forEach { deg ->
        withTransform({
            rotate(deg, pivot = pivot)
        }) {
            drawPath(path = path, color = MandalaSoftGold.copy(alpha = fillAlpha))
            drawPath(
                path = path,
                color = MandalaGold.copy(alpha = strokeAlpha),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

private fun buildOuterPetalPath(sx: (Float) -> Float, sy: (Float) -> Float): Path = Path().apply {
    moveTo(sx(130f), sy(19.5f))
    cubicTo(sx(134.5f), sy(23.5f), sx(137f), sy(29.5f), sx(135.2f), sy(35.5f))
    cubicTo(sx(132.5f), sy(34f), sx(130f), sy(35.8f), sx(130f), sy(35.8f))
    cubicTo(sx(130f), sy(35.8f), sx(127.5f), sy(34f), sx(124.8f), sy(35.5f))
    cubicTo(sx(123f), sy(29.5f), sx(125.5f), sy(23.5f), sx(130f), sy(19.5f))
    close()
}

private fun buildOuterAccentPath(sx: (Float) -> Float, sy: (Float) -> Float): Path = Path().apply {
    moveTo(sx(130f), sy(24.5f))
    cubicTo(sx(132.4f), sy(27.4f), sx(133.2f), sy(30.8f), sx(130f), sy(33.4f))
    cubicTo(sx(126.8f), sy(30.8f), sx(127.6f), sy(27.4f), sx(130f), sy(24.5f))
    close()
}

private fun buildInnerPetalPath(sx: (Float) -> Float, sy: (Float) -> Float): Path = Path().apply {
    moveTo(sx(130f), sy(43f))
    cubicTo(sx(135.8f), sy(48.2f), sx(138.2f), sy(55.6f), sx(135.6f), sy(63.5f))
    cubicTo(sx(133.2f), sy(61.8f), sx(130f), sy(63.5f), sx(130f), sy(63.5f))
    cubicTo(sx(130f), sy(63.5f), sx(126.8f), sy(61.8f), sx(124.4f), sy(63.5f))
    cubicTo(sx(121.8f), sy(55.6f), sx(124.2f), sy(48.2f), sx(130f), sy(43f))
    close()
}

private fun buildLotusPetal(
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    c1x: Float,
    c1y: Float,
    c2x: Float,
    c2y: Float,
    tipY: Float,
    r1x: Float,
    r2x: Float
): Path = Path().apply {
    moveTo(sx(130f), sy(148f))
    cubicTo(sx(c1x), sy(c1y), sx(c2x), sy(c2y), sx(130f), sy(tipY))
    cubicTo(sx(r1x), sy(c2y), sx(r2x), sy(c1y), sx(130f), sy(148f))
    close()
}
