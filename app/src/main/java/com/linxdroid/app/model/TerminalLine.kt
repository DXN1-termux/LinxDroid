package com.linxdroid.app.model

import androidx.compose.ui.graphics.Color

enum class LineType { OUTPUT, ERROR, INPUT, SYSTEM }

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT
) {
    fun displayColor(): Color = when (type) {
        LineType.OUTPUT -> Color(0xFFD4E8D4)
        LineType.ERROR  -> Color(0xFFFF5252)
        LineType.INPUT  -> Color(0xFF00C853)
        LineType.SYSTEM -> Color(0xFF757575)
    }
}
