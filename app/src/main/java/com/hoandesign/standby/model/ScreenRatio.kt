package com.hoandesign.standby.model

/**
 * Screen ratio archetypes for StandBy Android.
 *
 * Dynamically categorizes displays into 4 distinct form factor archetypes based on
 * the width-to-height aspect ratio.
 */
enum class LayoutArchetype {
    ULTRA_TALL_LANDSCAPE,
    STANDARD_LANDSCAPE,
    SQUARISH_FOLDABLE,
    TALL_PORTRAIT;

    /**
     * True if the layout archetype represents a landscape orientation.
     */
    val isLandscape: Boolean
        get() = this == ULTRA_TALL_LANDSCAPE || this == STANDARD_LANDSCAPE
}

/**
 * Layout dimension specifications calculated for measured window dimensions.
 *
 * @property archetype The active layout archetype based on aspect ratio
 * @property aspectRatio The width / height ratio
 * @property isLandscape Whether the display is in landscape orientation
 * @property cardCornerRadiusDp Recommended corner radius in dp for widget cards
 * @property contentPaddingDp Recommended outer content padding in dp
 */
data class LayoutDimensions(
    val archetype: LayoutArchetype,
    val aspectRatio: Float,
    val isLandscape: Boolean,
    val cardCornerRadiusDp: Float,
    val contentPaddingDp: Float
) {
    companion object {
        fun from(widthDp: Float, heightDp: Float): LayoutDimensions =
            calculateLayoutDimensions(widthDp, heightDp)
    }
}

/**
 * Pure function determining the layout archetype based on width and height in dp.
 *
 * Ratio classification:
 * - aspectRatio >= 1.85f -> ULTRA_TALL_LANDSCAPE (20:9, 21:9, 22.1:9 cover screens)
 * - aspectRatio in 1.35f..<1.85f -> STANDARD_LANDSCAPE (16:9, 16:10, 3:2 tri-fold/tablets)
 * - aspectRatio in 0.85f..<1.35f -> SQUARISH_FOLDABLE (OnePlus Open ~1.08:1, Fold 8 inner 1.16:1, Honor Magic V3 inner)
 * - aspectRatio < 0.85f -> TALL_PORTRAIT (9:19.5, 9:20, 9:22 vertical desk stands)
 */
fun determineLayoutArchetype(widthDp: Float, heightDp: Float): LayoutArchetype {
    if (heightDp <= 0f) return LayoutArchetype.STANDARD_LANDSCAPE
    val aspectRatio = widthDp / heightDp
    return when {
        aspectRatio >= 1.85f -> LayoutArchetype.ULTRA_TALL_LANDSCAPE
        aspectRatio in 1.35f..<1.85f -> LayoutArchetype.STANDARD_LANDSCAPE
        aspectRatio in 0.85f..<1.35f -> LayoutArchetype.SQUARISH_FOLDABLE
        else -> LayoutArchetype.TALL_PORTRAIT
    }
}

/**
 * Calculates complete layout dimensions and styling guidelines for the given dimensions.
 */
fun calculateLayoutDimensions(widthDp: Float, heightDp: Float): LayoutDimensions {
    val aspectRatio = if (heightDp > 0f) widthDp / heightDp else 1.78f
    val archetype = determineLayoutArchetype(widthDp, heightDp)
    val isLandscape = archetype.isLandscape

    val (cornerRadius, padding) = when (archetype) {
        LayoutArchetype.ULTRA_TALL_LANDSCAPE -> 24f to 20f
        LayoutArchetype.STANDARD_LANDSCAPE -> 24f to 16f
        LayoutArchetype.SQUARISH_FOLDABLE -> 20f to 12f
        LayoutArchetype.TALL_PORTRAIT -> 24f to 16f
    }

    return LayoutDimensions(
        archetype = archetype,
        aspectRatio = aspectRatio,
        isLandscape = isLandscape,
        cardCornerRadiusDp = cornerRadius,
        contentPaddingDp = padding
    )
}
