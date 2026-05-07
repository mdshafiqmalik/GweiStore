package com.gweinet.app.screens.nonlogged

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gweinet.app.R
import androidx.compose.ui.util.lerp
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel


@Composable
fun Loader(
    loaderMessage: String,
    showBottom: Boolean,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
){
    val colors by styleModel.appColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
    ) {
        Column(
            modifier = Modifier.weight(.9f)
                .fillMaxWidth()
                .background(colors.backgroundBase),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumLoader(false)
            Spacer(Modifier.height(40.dp))
            Text(
                text = loaderMessage,
                color = colors.textdark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (showBottom){
            Row(
                modifier = Modifier
                    .fillMaxWidth()

                    .weight(.1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .height(20.dp),
                    painter = painterResource(R.drawable.icon_gweicore_full),
                    contentDescription = null
                )

            }
        }

    }

}
@Composable
fun PremiumLoader(
    isDone: Boolean,
    showError: Boolean = false
) {

    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDone) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "scale"
    )

    val progress by animateFloatAsState(
        targetValue = if (isDone) 1f else 0f,
        animationSpec = tween(600),
        label = "progress"
    )

    val brush = Brush.sweepGradient(
        listOf(
            Color(0xFF00F5A0),
            Color(0xFF00D9F5),
            Color(0xFF9B5CFF),
            Color(0xFF00F5A0)
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .rotate(if (isDone) 0f else rotation)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            // 🔵 Always draw ring
            drawCircle(
                brush = brush,
                style = Stroke(width = 12.dp.toPx())
            )

            if (progress > 0f) {
                val w = size.width
                val h = size.height

                if (showError) {
                    // ❌ Draw cross
                    val p1 = Offset(w * 0.3f, h * 0.3f)
                    val p2 = Offset(w * 0.7f, h * 0.7f)
                    val p3 = Offset(w * 0.7f, h * 0.3f)
                    val p4 = Offset(w * 0.3f, h * 0.7f)

                    val path = Path()

                    if (progress < 0.5f) {
                        val t = progress / 0.5f
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(
                            lerp(p1.x, p2.x, t),
                            lerp(p1.y, p2.y, t)
                        )
                    } else {
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(p2.x, p2.y)

                        val t = (progress - 0.5f) / 0.5f
                        path.moveTo(p3.x, p3.y)
                        path.lineTo(
                            lerp(p3.x, p4.x, t),
                            lerp(p3.y, p4.y, t)
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF00D9F5),
                        style = Stroke(
                            width = 10.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                } else {
                    // ✅ Draw check
                    val p1 = Offset(w * 0.25f, h * 0.55f)
                    val p2 = Offset(w * 0.45f, h * 0.75f)
                    val p3 = Offset(w * 0.75f, h * 0.35f)

                    val path = Path()

                    if (progress < 0.5f) {
                        val t = progress / 0.5f
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(
                            lerp(p1.x, p2.x, t),
                            lerp(p1.y, p2.y, t)
                        )
                    } else {
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(p2.x, p2.y)

                        val t = (progress - 0.5f) / 0.5f
                        path.lineTo(
                            lerp(p2.x, p3.x, t),
                            lerp(p2.y, p3.y, t)
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF00D9F5),
                        style = Stroke(
                            width = 10.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}