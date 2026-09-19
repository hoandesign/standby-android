package com.hoandesign.standby

import com.hoandesign.standby.model.LayoutArchetype
import com.hoandesign.standby.model.calculateLayoutDimensions
import com.hoandesign.standby.model.determineLayoutArchetype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutTest {

    @Test
    fun testDetermineLayoutArchetype_ultraTallLandscape() {
        // Galaxy Z Fold cover screens, 21:9 Xperia (2200x1000 = 2.20)
        val archetype = determineLayoutArchetype(2200f, 1000f)
        assertEquals(LayoutArchetype.ULTRA_TALL_LANDSCAPE, archetype)
        assertTrue(archetype.isLandscape)
    }

    @Test
    fun testDetermineLayoutArchetype_standardLandscape() {
        // Standard 16:9 landscape (1920x1080 = 1.777...)
        val archetype = determineLayoutArchetype(1920f, 1080f)
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, archetype)
        assertTrue(archetype.isLandscape)
    }

    @Test
    fun testDetermineLayoutArchetype_squarishFoldables() {
        // OnePlus Open unfolded (2156x1968 = 1.095...)
        val onePlusOpen = determineLayoutArchetype(2156f, 1968f)
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, onePlusOpen)
        assertFalse(onePlusOpen.isLandscape)

        // Galaxy Z Fold 8 inner screen portrait-folded (2076x2152 = 0.964...)
        val zFoldInner = determineLayoutArchetype(2076f, 2152f)
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, zFoldInner)
        assertFalse(zFoldInner.isLandscape)
    }

    @Test
    fun testDetermineLayoutArchetype_tallPortrait() {
        // 20:9 vertical desk stand (1080x2400 = 0.45)
        val tallDeskStand = determineLayoutArchetype(1080f, 2400f)
        assertEquals(LayoutArchetype.TALL_PORTRAIT, tallDeskStand)
        assertFalse(tallDeskStand.isLandscape)

        // 16:9 portrait phone (1080x1920 = 0.5625)
        val standardPortrait = determineLayoutArchetype(1080f, 1920f)
        assertEquals(LayoutArchetype.TALL_PORTRAIT, standardPortrait)
        assertFalse(standardPortrait.isLandscape)
    }

    @Test
    fun testDetermineLayoutArchetype_edgeBoundaries() {
        // Boundary at 1.85f:
        // >= 1.85f -> ULTRA_TALL_LANDSCAPE
        // < 1.85f (down to 1.35f) -> STANDARD_LANDSCAPE
        assertEquals(LayoutArchetype.ULTRA_TALL_LANDSCAPE, determineLayoutArchetype(1850f, 1000f))
        assertEquals(LayoutArchetype.ULTRA_TALL_LANDSCAPE, determineLayoutArchetype(1.85f, 1.0f))
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, determineLayoutArchetype(1.8499f, 1.0f))

        // Boundary at 1.35f:
        // >= 1.35f -> STANDARD_LANDSCAPE
        // < 1.35f (down to 0.85f) -> SQUARISH_FOLDABLE
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, determineLayoutArchetype(1350f, 1000f))
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, determineLayoutArchetype(1.35f, 1.0f))
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, determineLayoutArchetype(1.3499f, 1.0f))

        // Boundary at 0.85f:
        // >= 0.85f -> SQUARISH_FOLDABLE
        // < 0.85f -> TALL_PORTRAIT
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, determineLayoutArchetype(850f, 1000f))
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, determineLayoutArchetype(0.85f, 1.0f))
        assertEquals(LayoutArchetype.TALL_PORTRAIT, determineLayoutArchetype(0.8499f, 1.0f))
    }

    @Test
    fun testCalculateLayoutDimensions() {
        val ultraWideDims = calculateLayoutDimensions(2200f, 1000f)
        assertEquals(LayoutArchetype.ULTRA_TALL_LANDSCAPE, ultraWideDims.archetype)
        assertEquals(2.2f, ultraWideDims.aspectRatio, 0.001f)
        assertTrue(ultraWideDims.isLandscape)
        assertEquals(24f, ultraWideDims.cardCornerRadiusDp, 0.001f)
        assertEquals(20f, ultraWideDims.contentPaddingDp, 0.001f)

        val foldableDims = calculateLayoutDimensions(2156f, 1968f)
        assertEquals(LayoutArchetype.SQUARISH_FOLDABLE, foldableDims.archetype)
        assertFalse(foldableDims.isLandscape)
        assertEquals(20f, foldableDims.cardCornerRadiusDp, 0.001f)
        assertEquals(12f, foldableDims.contentPaddingDp, 0.001f)

        val portraitDims = calculateLayoutDimensions(1080f, 2400f)
        assertEquals(LayoutArchetype.TALL_PORTRAIT, portraitDims.archetype)
        assertFalse(portraitDims.isLandscape)
        assertEquals(24f, portraitDims.cardCornerRadiusDp, 0.001f)
        assertEquals(16f, portraitDims.contentPaddingDp, 0.001f)
    }

    @Test
    fun testDetermineLayoutArchetype_zeroOrNegativeHeight() {
        val zeroHeight = determineLayoutArchetype(1080f, 0f)
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, zeroHeight)

        val negativeHeight = determineLayoutArchetype(1080f, -500f)
        assertEquals(LayoutArchetype.STANDARD_LANDSCAPE, negativeHeight)
    }
}
