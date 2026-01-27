package app.curious.lineflow

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.AccentSubtle
import app.curious.lineflow.ui.theme.BorderDefault
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.DarkCard
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.TextPrimary
import app.curious.lineflow.ui.theme.TextSecondary
import app.curious.lineflow.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    progressRepository: GameProgressRepository,
    onMusicToggled: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var musicEnabled by remember { mutableStateOf(progressRepository.isMusicEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(progressRepository.isVibrationEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(Modifier.height(48.dp))

        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBack)

            Spacer(Modifier.width(16.dp))

            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(32.dp))

        // Settings items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Music toggle
            SettingsItem(
                icon = "\uD83C\uDFB5", // 🎵
                title = "Background Music",
                subtitle = "Play serene music while playing",
                trailing = {
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = { enabled ->
                            musicEnabled = enabled
                            progressRepository.setMusicEnabled(enabled)
                            onMusicToggled(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Accent,
                            checkedTrackColor = AccentSubtle,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = BorderDefault
                        )
                    )
                }
            )

            // Vibration toggle
            SettingsItem(
                icon = "\uD83D\uDCF3", // 📳
                title = "Vibration",
                subtitle = "Haptic feedback when tracing edges",
                trailing = {
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { enabled ->
                            vibrationEnabled = enabled
                            progressRepository.setVibrationEnabled(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Accent,
                            checkedTrackColor = AccentSubtle,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = BorderDefault
                        )
                    )
                }
            )
        }

        Spacer(Modifier.weight(1f))

        // Credits section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Music Credit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextTertiary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "\"Musical Ambient Background Loop\"",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "by AKTASOK",
                fontSize = 11.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "pixabay.com",
                fontSize = 10.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            trailing()
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "back_button_scale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(DarkCard)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u2190", // ←
            fontSize = 20.sp,
            color = TextPrimary
        )
    }
}
