package ru.ekr.zoomimage

import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
fun ZoomImage(
    @DrawableRes image: Int,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val des by rememberUpdatedState(LocalDensity.current)
    val conf by rememberUpdatedState(LocalConfiguration.current)
    val screenWidthPx = remember(des, conf) { with(des) { conf.screenWidthDp.dp.toPx() } }
    val screenHeightPx = remember(des, conf) { with(des) { conf.screenHeightDp.dp.toPx() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    MotionEvent.ACTION_UP
                    do {
                        val event = awaitPointerEvent()
                        if (event.calculateCentroidSize() > 0) {
                            scale *= event.calculateZoom()
                            val offset = event.calculatePan()

                            val offsetXNew = offsetX + offset.x
                            val offsetYNew = offsetY + offset.y

                            val weightItem = (screenWidthPx * scale) - screenWidthPx
                            val heightItem = (screenHeightPx * scale) - screenHeightPx

                            val maxLeftOffset = (weightItem + offsetXNew) * -1
                            val maxRightOffset = weightItem - offsetXNew

                            val maxTopOffset = (heightItem + offsetYNew) * -1
                            val maxBottomOffset = heightItem - offsetYNew

                            when {
                                offsetXNew > maxLeftOffset
                                        && offsetXNew < maxRightOffset -> offsetX = offsetXNew

                                offsetXNew < maxLeftOffset && scale > 1f -> offsetX = maxLeftOffset
                                offsetXNew > maxRightOffset && scale > 1f -> offsetX =
                                    maxRightOffset
                            }

                            when {
                                offsetYNew > maxTopOffset
                                        && offsetYNew < maxBottomOffset -> offsetY = offsetYNew

                                offsetYNew < maxTopOffset && scale > 1f -> offsetY = maxTopOffset
                                offsetYNew > maxBottomOffset && scale > 1f -> offsetY =
                                    maxBottomOffset
                            }
                        }

                    } while (event.changes.any { it.pressed })
                    val isGoneOut = scale < 1f || scale > 12f
                    if (isGoneOut) {
                        scope.launch {
                            val jobScale = launch {
                                animate(
                                    initialValue = scale,
                                    targetValue = 1f,
                                    block = { fl: Float, fl1: Float ->
                                        scale = fl
                                    }
                                )
                            }
                            val jobOffsetX = launch {
                                animate(
                                    initialValue = offsetX,
                                    targetValue = 0f,
                                    block = { fl: Float, fl1: Float ->
                                        offsetX = fl
                                    }
                                )
                            }
                            val jobOffsetY = launch {
                                animate(
                                    initialValue = offsetY,
                                    targetValue = 0f,
                                    block = { fl: Float, fl1: Float ->
                                        offsetY = fl
                                    }
                                )
                            }
                            joinAll(jobScale, jobOffsetX, jobOffsetY)
                        }
                    }
                }
            }
            .fillMaxSize()
    ) {
        BoxImageLoad(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .align(Alignment.Center)
                .fillMaxWidth()
                .clipToBounds(),
            image = image,
            contentScale = ContentScale.FillHeight,
        )
    }
}