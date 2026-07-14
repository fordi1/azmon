package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.72f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoOffset = remember { Animatable(28f) }
    val logoRotation = remember { Animatable(-3f) }
    var visibleChecks by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(420))
        }
        launch {
            logoScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoOffset.animateTo(0f, tween(620, easing = FastOutSlowInEasing))
        }
        launch {
            logoRotation.animateTo(0f, tween(620, easing = FastOutSlowInEasing))
        }

        delay(520)
        repeat(3) {
            visibleChecks = it + 1
            delay(160)
        }
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF9FAFF), Color(0xFFEEF3FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(310.dp)
                .scale(1.08f)
                .alpha(0.55f)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x337A9AE8), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.azmon_logo),
                contentDescription = "لوگوی طراح آزمون",
                modifier = Modifier
                    .size(230.dp)
                    .offset(y = logoOffset.value.dp)
                    .graphicsLayer { rotationZ = logoRotation.value }
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "طراح آزمون",
                color = Color(0xFF18366F),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "از ایده تا برگه امتحان",
                color = Color(0xFF6B7CA8),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { index ->
                    val target = if (index < visibleChecks) 1f else 0.25f
                    val scale by animateFloatAsState(
                        targetValue = target,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "checkScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(scale)
                            .background(
                                if (index < visibleChecks) Color(0xFFF16C55) else Color(0xFFCAD5EF),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}
