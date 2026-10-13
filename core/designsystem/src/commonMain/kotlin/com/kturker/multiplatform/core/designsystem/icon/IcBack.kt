@file:Suppress("MagicNumber")

package com.kturker.multiplatform.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IcBack: ImageVector?
    get() {
        if (icBack != null) {
            return icBack
        }
        icBack = ImageVector.Builder(
            name = "icBack",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(222.9f, 580.1f)
                lineToRelative(301.4f, 328.5f)
                curveToRelative(24.4f, 28.7f, 20.8f, 71.7f, -7.9f, 96.1f)
                reflectiveCurveToRelative(-71.7f, 20.8f, -96.1f, -7.9f)
                lineTo(19.6f, 560f)
                arcToRelative(
                    67.8f,
                    67.8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -13.8f,
                    -20f
                )
                arcToRelative(
                    68f,
                    68f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -6f,
                    -29.5f
                )
                lineToRelative(0f, -0.1f)
                arcToRelative(
                    68.3f,
                    68.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    7.3f,
                    -29.1f
                )
                arcToRelative(
                    68.3f,
                    68.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.4f,
                    -2.6f
                )
                arcToRelative(
                    67.6f,
                    67.6f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    10.1f,
                    -13.7f
                )
                lineTo(430f, 21.1f)
                curveToRelative(25.6f, -27.6f, 68.7f, -29.2f, 96.3f, -3.7f)
                reflectiveCurveToRelative(29.2f, 68.7f, 3.7f, 96.3f)
                lineTo(224.1f, 443.8f)
                horizontalLineToRelative(730.5f)
                curveToRelative(37.6f, 0f, 68.2f, 30.5f, 68.2f, 68.2f)
                reflectiveCurveToRelative(-30.5f, 68.2f, -68.2f, 68.2f)
                horizontalLineTo(222.9f)
                close()
            }
        }.build()

        return icBack
    }

private var icBack: ImageVector? = null
