package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DisplayLanguage
import com.example.ui.theme.TamilBodyTextStyle
import com.example.ui.theme.TamilHeadingTextStyle

@Composable
fun ShoppingScreen(
    displayLanguage: DisplayLanguage,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.shop_title_tamil),
                        style = TamilHeadingTextStyle.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.shop_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("shop_open_settings_button")
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Centered Coming Soon Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_coming_soon_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val infinite = rememberInfiniteTransition(label = "shopHeroAnim")
                            val haloScale by infinite.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 1.24f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shopHaloScale"
                            )
                            val haloAlpha by infinite.animateFloat(
                                initialValue = 0.28f,
                                targetValue = 0.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shopHaloAlpha"
                            )
                            val bagFloatY by infinite.animateFloat(
                                initialValue = -5f,
                                targetValue = 5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shopBagFloat"
                            )
                            val bagSway by infinite.animateFloat(
                                initialValue = -5f,
                                targetValue = 5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1750, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shopBagSway"
                            )

                            Box(
                                modifier = Modifier.size(112.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .graphicsLayer {
                                            scaleX = haloScale
                                            scaleY = haloScale
                                            alpha = haloAlpha
                                        }
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
                                    ),
                                    modifier = Modifier.size(88.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingBag,
                                            contentDescription = stringResource(R.string.nav_shop),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .graphicsLayer {
                                                    translationY = bagFloatY
                                                    rotationZ = bagSway
                                                }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(R.string.shop_coming_soon_ta),
                                style = TamilHeadingTextStyle.copy(fontSize = 28.sp),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("shop_coming_soon_title")
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.shop_coming_soon_en),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .width(56.dp)
                                    .padding(vertical = 4.dp),
                                thickness = 1.2.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )

                            if (displayLanguage == DisplayLanguage.TAMIL || displayLanguage == DisplayLanguage.BILINGUAL) {
                                Text(
                                    text = stringResource(R.string.shop_coming_soon_desc_ta),
                                    style = TamilBodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (displayLanguage == DisplayLanguage.ENGLISH || displayLanguage == DisplayLanguage.BILINGUAL) {
                                Text(
                                    text = stringResource(R.string.shop_coming_soon_desc_en),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
