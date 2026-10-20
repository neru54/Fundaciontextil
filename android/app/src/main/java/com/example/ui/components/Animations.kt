package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Aparición con rebote. */
@Composable
fun PopIn(content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
    ) {
        content()
    }
}

/** Ícono de éxito con rebote. */
@Composable
fun AnimatedSuccessIcon(tint: Color, size: Dp = 44.dp) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}

/** Contador animado al cambiar de valor. */
@Composable
fun BouncingCounter(count: Int, color: Color, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableIntStateOf(count) }
    LaunchedEffect(count) {
        if (count > previous) {
            scale.snapTo(1.5f)
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        previous = count
    }
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically(tween(200)) { it } + fadeIn()) togetherWith (slideOutVertically(tween(200)) { -it } + fadeOut())
            } else {
                (slideInVertically(tween(200)) { -it } + fadeIn()) togetherWith (slideOutVertically(tween(200)) { it } + fadeOut())
            }
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        label = "counter"
    ) { value ->
        Text("$value", color = color, fontWeight = FontWeight.Bold)
    }
}
