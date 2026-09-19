package com.hoandesign.standby.model

/**
 * Immutable representation of device battery health and power charging state.
 *
 * @param percentage Battery charge level from 0 to 100%.
 * @param isCharging True if the device is connected to a power supply.
 * @param chargeSpeed Human-readable charge rate ("⚡ Fast Charging", "⚡ Wireless Qi", "⚡ USB Charging", "On Battery").
 * @param capacityMah Nominal device battery capacity in milliampere-hours (default: 4895 mAh).
 * @param timeToFullMinutes Estimated duration in minutes remaining until full 100% charge, or null when discharging.
 */
data class BatteryState(
    val percentage: Int = 85,
    val isCharging: Boolean = true,
    val chargeSpeed: String = "⚡ Fast Charging",
    val capacityMah: Int = 4895,
    val timeToFullMinutes: Int? = 22
)
