package com.panchangam100.live.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary Palette: Deep Saffron Gold + Sacred Crimson ───
val Gold = Color(0xFFD4A017)
val GoldLight = Color(0xFFEBC84A)
val GoldDark = Color(0xFF9C7400)

val Crimson = Color(0xFF8B0000)
val CrimsonLight = Color(0xFFB22222)
val CrimsonDark = Color(0xFF5C0000)

val SacredOrange = Color(0xFFFF6B35)
val SacredOrangeLight = Color(0xFFFF8C5A)
val SacredOrangeDark = Color(0xFFCC4A1A)

// ─── Background tones ───
val ParchmentWhite = Color(0xFFFFF8F0)
val ParchmentLight = Color(0xFFFFF3E0)
val TempleBg = Color(0xFF1A0A00)
val TempleCardBg = Color(0xFF2C1A00)
val TempleCardBgLight = Color(0xFF3D2500)

// ─── Text ───
val TextPrimary = Color(0xFF1A0A00)
val TextSecondary = Color(0xFF5C3A1E)
val TextOnDark = Color(0xFFFFF8F0)
val TextOnDarkSecondary = Color(0xFFD4A017)
val TextHint = Color(0xFF9E7B5A)

// ─── Auspicious / Inauspicious indicators ───
val AuspiciousGreen = Color(0xFF2E7D32)
val AuspiciousGreenLight = Color(0xFF4CAF50)
val InauspiciousRed = Color(0xFFC62828)
val InauspiciousRedLight = Color(0xFFEF5350)
val NeutralBlue = Color(0xFF1565C0)

// ─── Surface / Card ───
val SurfaceLight = Color(0xFFFFFBF5)
val SurfaceDark = Color(0xFF1E1000)
val CardLight = Color(0xFFFFF8F0)
val CardDark = Color(0xFF2A1800)
val CardBorderLight = Color(0xFFE8C57A)
val CardBorderDark = Color(0xFF5A3A00)

// ─── Gradient endpoints ───
val GradientTop = Color(0xFF8B0000)
val GradientMid = Color(0xFFD4A017)
val GradientBottom = Color(0xFF4A0000)

// ─── Weekday colors ───
val SundayColor = Color(0xFFD32F2F)
val MondayColor = Color(0xFF1565C0)
val TuesdayColor = Color(0xFF880E4F)
val WednesdayColor = Color(0xFF2E7D32)
val ThursdayColor = Color(0xFFE65100)
val FridayColor = Color(0xFF6A1B9A)
val SaturdayColor = Color(0xFF4A148C)

fun weekdayColor(dayOfWeek: Int): Color = when (dayOfWeek) {
    1 -> MondayColor
    2 -> TuesdayColor
    3 -> WednesdayColor
    4 -> ThursdayColor
    5 -> FridayColor
    6 -> SaturdayColor
    7 -> SundayColor
    else -> TextPrimary
}
