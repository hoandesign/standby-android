package com.hoandesign.standby

import com.hoandesign.standby.data.SystemTelemetryHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemTelemetryHelperTest {

    @Test
    fun testStorageMetrics_returnsValidStructure() {
        val storage = SystemTelemetryHelper.getStorageMetrics()
        assertNotNull(storage)
        assertTrue(storage.remainPercent in 0..100)
        assertTrue(storage.usedPercent in 0..100)
        assertNotNull(storage.availableGbFormatted)
        assertNotNull(storage.totalGbFormatted)
    }

    @Test
    fun testStorageMetrics_calculationIntegrity() {
        val storage = SystemTelemetryHelper.getStorageMetrics()
        assertTrue(storage.remainPercent + storage.usedPercent <= 101)
    }
}
