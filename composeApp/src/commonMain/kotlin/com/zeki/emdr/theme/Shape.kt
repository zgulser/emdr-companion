package com.zeki.emdr.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val EmdrCardCorner = 20.dp
val EmdrButtonCorner = 16.dp
val EmdrChipCorner = 12.dp

val EmdrCardShape = RoundedCornerShape(EmdrCardCorner)
val EmdrButtonShape = RoundedCornerShape(EmdrButtonCorner)
val EmdrChipShape = RoundedCornerShape(EmdrChipCorner)
val EmdrSwatchShape = CircleShape

val EmdrShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = EmdrChipShape,
    medium = EmdrCardShape,
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
