@file:Suppress("MagicNumber")

package com.kturker.multiplatform.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IcSettings: ImageVector?
    get() {
        if (icSettings != null) {
            return icSettings
        }
        icSettings = ImageVector.Builder(
            name = "icSettings",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1C274C)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(14.279f, 2.152f)
                curveTo(13.908f, 2f, 13.439f, 2f, 12.5f, 2f)
                curveTo(11.561f, 2f, 11.092f, 2f, 10.721f, 2.152f)
                curveTo(10.227f, 2.355f, 9.835f, 2.745f, 9.631f, 3.235f)
                curveTo(9.537f, 3.458f, 9.501f, 3.718f, 9.486f, 4.098f)
                curveTo(9.465f, 4.656f, 9.177f, 5.172f, 8.69f, 5.451f)
                curveTo(8.203f, 5.73f, 7.609f, 5.72f, 7.111f, 5.459f)
                curveTo(6.773f, 5.281f, 6.528f, 5.183f, 6.286f, 5.151f)
                curveTo(5.756f, 5.082f, 5.22f, 5.224f, 4.796f, 5.547f)
                curveTo(4.478f, 5.789f, 4.243f, 6.193f, 3.774f, 7f)
                curveTo(3.304f, 7.807f, 3.07f, 8.21f, 3.017f, 8.605f)
                curveTo(2.948f, 9.131f, 3.091f, 9.663f, 3.417f, 10.083f)
                curveTo(3.565f, 10.276f, 3.774f, 10.437f, 4.098f, 10.639f)
                curveTo(4.574f, 10.936f, 4.88f, 11.442f, 4.88f, 12f)
                curveTo(4.88f, 12.558f, 4.574f, 13.064f, 4.098f, 13.361f)
                curveTo(3.774f, 13.563f, 3.565f, 13.724f, 3.416f, 13.917f)
                curveTo(3.091f, 14.337f, 2.947f, 14.869f, 3.017f, 15.395f)
                curveTo(3.07f, 15.789f, 3.304f, 16.193f, 3.774f, 17f)
                curveTo(4.243f, 17.807f, 4.478f, 18.211f, 4.796f, 18.453f)
                curveTo(5.22f, 18.776f, 5.756f, 18.918f, 6.286f, 18.849f)
                curveTo(6.528f, 18.817f, 6.773f, 18.719f, 7.111f, 18.541f)
                curveTo(7.609f, 18.28f, 8.203f, 18.27f, 8.69f, 18.549f)
                curveTo(9.177f, 18.828f, 9.465f, 19.344f, 9.486f, 19.902f)
                curveTo(9.501f, 20.281f, 9.537f, 20.542f, 9.631f, 20.765f)
                curveTo(9.835f, 21.255f, 10.227f, 21.645f, 10.721f, 21.848f)
                curveTo(11.092f, 22f, 11.561f, 22f, 12.5f, 22f)
                curveTo(13.439f, 22f, 13.908f, 22f, 14.279f, 21.848f)
                curveTo(14.773f, 21.645f, 15.165f, 21.255f, 15.369f, 20.765f)
                curveTo(15.463f, 20.542f, 15.499f, 20.281f, 15.514f, 19.902f)
                curveTo(15.535f, 19.344f, 15.823f, 18.828f, 16.31f, 18.549f)
                curveTo(16.797f, 18.27f, 17.391f, 18.28f, 17.889f, 18.541f)
                curveTo(18.227f, 18.719f, 18.472f, 18.817f, 18.714f, 18.849f)
                curveTo(19.244f, 18.918f, 19.78f, 18.776f, 20.204f, 18.453f)
                curveTo(20.522f, 18.211f, 20.757f, 17.807f, 21.226f, 17f)
                curveTo(21.696f, 16.193f, 21.93f, 15.789f, 21.983f, 15.395f)
                curveTo(22.052f, 14.869f, 21.909f, 14.337f, 21.583f, 13.916f)
                curveTo(21.435f, 13.724f, 21.226f, 13.563f, 20.902f, 13.361f)
                curveTo(20.426f, 13.064f, 20.12f, 12.558f, 20.12f, 12f)
                curveTo(20.12f, 11.442f, 20.426f, 10.936f, 20.902f, 10.639f)
                curveTo(21.226f, 10.437f, 21.435f, 10.276f, 21.584f, 10.083f)
                curveTo(21.909f, 9.663f, 22.052f, 9.131f, 21.983f, 8.605f)
                curveTo(21.93f, 8.211f, 21.696f, 7.807f, 21.226f, 7f)
                curveTo(20.757f, 6.193f, 20.522f, 5.789f, 20.204f, 5.547f)
                curveTo(19.78f, 5.224f, 19.244f, 5.082f, 18.714f, 5.151f)
                curveTo(18.472f, 5.183f, 18.227f, 5.281f, 17.889f, 5.459f)
                curveTo(17.392f, 5.72f, 16.797f, 5.73f, 16.31f, 5.451f)
                curveTo(15.823f, 5.172f, 15.535f, 4.656f, 15.514f, 4.098f)
                curveTo(15.499f, 3.718f, 15.463f, 3.458f, 15.369f, 3.235f)
                curveTo(15.165f, 2.745f, 14.773f, 2.355f, 14.279f, 2.152f)
                close()
                moveTo(12.5f, 15f)
                curveTo(14.17f, 15f, 15.523f, 13.657f, 15.523f, 12f)
                curveTo(15.523f, 10.343f, 14.17f, 9f, 12.5f, 9f)
                curveTo(10.83f, 9f, 9.477f, 10.343f, 9.477f, 12f)
                curveTo(9.477f, 13.657f, 10.83f, 15f, 12.5f, 15f)
                close()
            }
        }.build()

        return icSettings
    }

private var icSettings: ImageVector? = null
