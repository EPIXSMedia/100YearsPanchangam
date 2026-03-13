package com.panchangam100.live.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panchangam100.live.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onReady: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(400)
        subtitleVisible = true
        delay(1800)
        onReady()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF4A0000), Color(0xFF8B0000), Color(0xFF1A0500)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Om symbol
                    Text(
                        "ॐ",
                        fontSize = 72.sp,
                        color = Gold,
                        modifier = Modifier.scale(scale)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "100 Years",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GoldLight,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                    Text(
                        "పంచాంగం",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Panchangam",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldLight,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(
                visible = subtitleVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "2020 – 2120",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight.copy(alpha = 0.8f),
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Telugu • English • Tamil\nMalayalam • Hindi • Kannada",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(48.dp))
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Bottom branding
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text(
                "Highest Astronomical Accuracy",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        }
    }
}
