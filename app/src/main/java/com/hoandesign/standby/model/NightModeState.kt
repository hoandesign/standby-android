package com.hoandesign.standby.model

/**
 * User preference for Night Mode activation.
 */
enum class NightModePreference {
    AUTO,
    ALWAYS_ON,
    DISABLED
}

/**
 * Represents the current ambient lighting and night mode status.
 *
 * @param isNightModeActive Whether night mode rendering (deep red tint & dimming) is active.
 * @param preference User preference (AUTO switches below 5.0f lux, ALWAYS_ON, DISABLED).
 * @param ambientLux Last reported ambient light sensor value in lumens/m^2 (lux).
 */
data class NightModeState(
    val isNightModeActive: Boolean = false,
    val preference: NightModePreference = NightModePreference.AUTO,
    val ambientLux: Float = 50f
) {
    companion object {
        /**
         * Ambient lux threshold below which night mode is activated when in AUTO mode.
         */
        const val LUX_THRESHOLD: Float = 5.0f

        /**
         * Evaluates whether night mode should be active given a preference and ambient lux reading.
         */
        fun computeIsNightModeActive(
            preference: NightModePreference,
            ambientLux: Float
        ): Boolean {
            return when (preference) {
                NightModePreference.ALWAYS_ON -> true
                NightModePreference.DISABLED -> false
                NightModePreference.AUTO -> ambientLux < LUX_THRESHOLD
            }
        }

        /**
         * Factory function creating a state with automatically evaluated active flag.
         */
        fun fromLux(
            ambientLux: Float,
            preference: NightModePreference = NightModePreference.AUTO
        ): NightModeState {
            return NightModeState(
                isNightModeActive = computeIsNightModeActive(preference, ambientLux),
                preference = preference,
                ambientLux = ambientLux
            )
        }
    }

    /**
     * Computes the active state based on this instance's preference and ambientLux.
     */
    fun computeIsNightModeActive(): Boolean = computeIsNightModeActive(preference, ambientLux)

    /**
     * Returns a copy with updated ambientLux and re-evaluated isNightModeActive.
     */
    fun withLux(newLux: Float): NightModeState = copy(
        ambientLux = newLux,
        isNightModeActive = computeIsNightModeActive(preference, newLux)
    )

    /**
     * Returns a copy with updated preference and re-evaluated isNightModeActive.
     */
    fun withPreference(newPreference: NightModePreference): NightModeState = copy(
        preference = newPreference,
        isNightModeActive = computeIsNightModeActive(newPreference, ambientLux)
    )
}
