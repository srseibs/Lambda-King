package com.sailinghawklabs.lambdaking

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY

object Characters {
    const val LAMBDA = 'λ'
    const val EPSILON = 'ε'
    val EPSILON_SUB_R = HtmlCompat.fromHtml("ε<sub>r</sub>",FROM_HTML_MODE_LEGACY)
}