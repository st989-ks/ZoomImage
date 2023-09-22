package ru.ekr.zoomimage

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Size
import kotlinx.coroutines.Dispatchers

/**
 * BoxImageLoad(
 * modifier = Modifier
 * .fillMaxWidth()
 * .background(Color.Black),
 * contentScale = contentScale,
 * image = R.raw.ic_navigation_bar,
 * isSVG = true
 * )
 * */
@Composable
fun BoxImageLoad(
    modifier: Modifier = Modifier,
    image: Any?,
    alignment: Alignment = Alignment.Center,
    sizeToIntrinsics: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    colorLoader: Color = MaterialTheme.colorScheme.primary,
    strokeWidthLoader: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    modifierImage: Modifier = Modifier,
    modifierOnImage: Modifier = Modifier,
    content: @Composable BoxScope.(error: Boolean) -> Unit = {},
) {

    val isWebSVG = remember(image) {
        val isWebSVG = image.toString()
        isWebSVG.isNotEmpty() && isWebSVG.length > 3 && isWebSVG.takeLast(3).equals("svg", true)
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current
    var sizeLoader by remember {
        mutableStateOf(0.dp)
    }

    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val model = remember(image) {
        ImageRequest.Builder(context).apply {
            dispatcher(Dispatchers.IO)
            networkCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.ENABLED)
            decoderFactory(SvgDecoder.Factory())
            data(image)
            size(
                if (isWebSVG) {
                    Size(
                        with(density) { configuration.screenWidthDp.dp.roundToPx() },
                        Dimension.Undefined
                    )
                } else {
                    Size(1920, Dimension.Undefined)
                }
            )
        }.build()
    }

    val painter = rememberAsyncImagePainter(
        model = model,
        contentScale = contentScale,
        onLoading = {
            isLoading = true
            isError = false
            isSuccess = false
        },
        onError = {
            isLoading = false
            isError = true
            isSuccess = true
        },
        onSuccess = {
            isLoading = false
            isError = false
            isSuccess = true
        })


    Box(
        modifier = modifier
            .onGloballyPositioned {
                sizeLoader = with(density) {
                    (it.size.height / 2)
                        .toDp()
                        .coerceIn(
                            25.dp,
                            40.dp
                        )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .then(modifierImage)
                .paint(
                    painter = painter,
                    alignment = alignment,
                    sizeToIntrinsics = sizeToIntrinsics,
                    contentScale = contentScale,
                    colorFilter = colorFilter,
                    alpha = alpha
                )
                .then(modifierOnImage)
        )

        content.invoke(this, isError)

        if (isLoading) CircularProgressIndicator(
            modifier = Modifier
                .size(sizeLoader),
            color = colorLoader,
            strokeWidth = strokeWidthLoader,
        )
    }
}