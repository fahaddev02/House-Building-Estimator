package com.example.paintcalculatorestimator.utils

import kotlin.math.ceil

object SlabCalculator {

    fun calculateSlabVolume(length: Double, width: Double, thickness: Double, isImperial: Boolean): Double {
        if (length <= 0.0 || width <= 0.0 || thickness <= 0.0) return 0.0
        return if (isImperial) {
            // length & width in ft, thickness in inches. Volume in cu ft = length * width * (thickness / 12)
            val volCuFt = length * width * (thickness / 12.0)
            // convert to cubic meters
            volCuFt * 0.0283168
        } else {
            // length & width in meters, thickness in cm. Volume in cu meters = length * width * (thickness / 100)
            length * width * (thickness / 100.0)
        }
    }

    /**
     * Concrete ratio 1:2:4 (cement:sand:aggregate).
     * Compaction/dry volume factor = 1.54.
     * Cement volume = dry volume / 7.
     * Cement bags = cement volume / 0.035.
     */
    fun calculateCementBags(slabVolumeM3: Double): Double {
        if (slabVolumeM3 <= 0.0) return 0.0
        val dryVolume = slabVolumeM3 * 1.54
        val cementVolume = dryVolume / 7.0
        return cementVolume / 0.035
    }

    /**
     * Sand volume = dry volume * 2 / 7.
     */
    fun calculateSandVolume(slabVolumeM3: Double): Double {
        if (slabVolumeM3 <= 0.0) return 0.0
        val dryVolume = slabVolumeM3 * 1.54
        return dryVolume * 2.0 / 7.0
    }

    /**
     * Aggregate/Crush volume = dry volume * 4 / 7.
     */
    fun calculateAggregateVolume(slabVolumeM3: Double): Double {
        if (slabVolumeM3 <= 0.0) return 0.0
        val dryVolume = slabVolumeM3 * 1.54
        return dryVolume * 4.0 / 7.0
    }

    /**
     * Rebar weight (steel) is estimated at standard 80 kg per m3 of wet concrete.
     */
    fun calculateSteelWeight(slabVolumeM3: Double): Double {
        if (slabVolumeM3 <= 0.0) return 0.0
        return slabVolumeM3 * 80.0
    }
}
