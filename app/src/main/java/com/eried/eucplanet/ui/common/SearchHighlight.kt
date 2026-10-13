package com.eried.eucplanet.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.eried.eucplanet.ui.theme.appColors

val LocalSettingsSearchQuery = compositionLocalOf { "" }

/**
 * @param onFill the text sits on a filled control, a button: the accent behind
 *   a word vanishes on an accent button, so the match is bold and underlined
 *   in the control's own ink instead.
 */
@Composable
fun highlightMatches(text: String, query: String, onFill: Boolean = false): AnnotatedString {
    val q = query.trim()
    if (q.isEmpty()) return AnnotatedString(text)
    val highlightBg = MaterialTheme.appColors.selection.copy(alpha = 0.55f)
    val highlightFg = Color.Black
    val style = if (onFill) SpanStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
    ) else SpanStyle(background = highlightBg, color = highlightFg)
    return buildAnnotatedString {
        append(text)
        var start = 0
        while (true) {
            val idx = text.indexOf(q, start, ignoreCase = true)
            if (idx < 0) break
            addStyle(style, idx, idx + q.length)
            start = idx + q.length
        }
    }
}
