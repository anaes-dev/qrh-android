package dev.anaes.qrh.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
val PrimaryLight = Color(0xFFF44336)
val PrimaryDark = Color(0xFF272727)
val AccentLight = Color(0xFF0096FF)
val AccentDark = Color(0xFF4FC3F7)

val WindowBackgroundLight = Color(0xFFFAFAFA)
val WindowBackgroundDark = Color(0xFF121212)

val CardBgLight = Color(0xFFFFFFFF)
val CardBgDark = Color(0xFF292929)

val SearchBackgroundLight = Color(0xFFFAFAFA)
val SearchBackgroundDark = Color(0xFF1E1E1E)

val SnackbarBgLight = Color(0xD9000000)
val SnackbarBgDark = Color(0xD9FFFFFF)
val SnackbarTextLight = Color(0xFFFFFFFF)
val SnackbarTextDark = Color(0xFF000000)

// Box color schemes — light
val OrangeBgLight = Color(0xFFFBE9E7)
val OrangeArrLight = Color(0xFFFFCCBC)
val OrangeTxtLight = Color(0xFFBF360C)

val BlueBgLight = Color(0xFFE3F2FD)
val BlueArrLight = Color(0xFFBBDEFB)
val BlueTxtLight = Color(0xFF1565C0)

val GreenBgLight = Color(0xFFE8F5E9)
val GreenArrLight = Color(0xFFC8E6C9)
val GreenTxtLight = Color(0xFF2E7D32)

val BlackBgLight = Color(0xFFEDEDED)
val BlackArrLight = Color(0xFFD1D1D1)
val BlackTxtLight = Color(0xFF000000)

val PurpleBgLight = Color(0xFFEDE7F6)
val PurpleArrLight = Color(0xFFD1C4E9)
val PurpleTxtLight = Color(0xFF512DA8)

val RedBgLight = Color(0xFFFFCDD2)
val RedTxtLight = Color(0xFFB71C1C)

// Box color schemes — dark
val OrangeBgDark = Color(0xFF251813)
val OrangeArrDark = Color(0xFF592717)
val OrangeTxtDark = Color(0xFFFFAB91)

val BlueBgDark = Color(0xFF131D24)
val BlueArrDark = Color(0xFF173A55)
val BlueTxtDark = Color(0xFF42A5F5)

val GreenBgDark = Color(0xFF171F17)
val GreenArrDark = Color(0xFF234125)
val GreenTxtDark = Color(0xFF66BB6A)

val BlackBgDark = Color(0xFF000000)
val BlackArrDark = Color(0xFF252525)
val BlackTxtDark = Color(0xFFEEEEEE)

val PurpleBgDark = Color(0xFF19151F)
val PurpleArrDark = Color(0xFF2C1E44)
val PurpleTxtDark = Color(0xFFB39DDB)

val RedBgDark = Color(0xFF241615)
val RedTxtDark = Color(0xFFE57373)

// Subtle card backgrounds (non-expanding items)
val SubtleCardBgLight = Color(0xFFF5F5F5)
val SubtleCardBgDark = Color(0xFF1E1E1E)

// Launch buttons
val LaunchBtnTxtDisabledLight = Color(0xFF555555)
val LaunchBtnTxtEnabledLight = Color(0xFFFFFFFF)
val LaunchBtnBgExitLight = Color(0xFFEEEEEE)
val LaunchBtnBgDisabledLight = Color(0xFFDDDDDD)
val LaunchBtnBgEnabledLight = Color(0xFFF44336)

val LaunchBtnTxtDisabledDark = Color(0xFFDDDDDD)
val LaunchBtnTxtEnabledDark = Color(0xFFFFFFFF)
val LaunchBtnBgExitDark = Color(0xFF383838)
val LaunchBtnBgDisabledDark = Color(0xFF414141)
val LaunchBtnBgEnabledDark = Color(0xFFF44336)

data class BoxColors(
    val background: Color,
    val text: Color,
    val arrow: Color
) {
    companion object {
        fun forType(type: Int, isDark: Boolean): BoxColors = when (type) {
            5 -> if (isDark) BoxColors(OrangeBgDark, OrangeTxtDark, OrangeArrDark)
                 else BoxColors(OrangeBgLight, OrangeTxtLight, OrangeArrLight)
            6 -> if (isDark) BoxColors(BlueBgDark, BlueTxtDark, BlueArrDark)
                 else BoxColors(BlueBgLight, BlueTxtLight, BlueArrLight)
            7 -> if (isDark) BoxColors(GreenBgDark, GreenTxtDark, GreenArrDark)
                 else BoxColors(GreenBgLight, GreenTxtLight, GreenArrLight)
            8 -> if (isDark) BoxColors(BlackBgDark, BlackTxtDark, BlackArrDark)
                 else BoxColors(BlackBgLight, BlackTxtLight, BlackArrLight)
            9 -> if (isDark) BoxColors(PurpleBgDark, PurpleTxtDark, PurpleArrDark)
                 else BoxColors(PurpleBgLight, PurpleTxtLight, PurpleArrLight)
            else -> if (isDark) BoxColors(CardBgDark, Color.White, CardBgDark)
                    else BoxColors(CardBgLight, Color.Black, CardBgLight)
        }
    }
}
