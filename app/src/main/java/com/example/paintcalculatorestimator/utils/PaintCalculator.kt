package com.example.paintcalculatorestimator.utils

import com.example.paintcalculatorestimator.domain.model.Material
import kotlin.math.ceil
import kotlin.math.roundToInt

object PaintCalculator {

    // Conversion constants
    const val METERS_TO_FEET = 3.28084
    const val SQ_METERS_TO_SQ_FEET = 10.76391
    const val LITERS_TO_GALLONS = 0.264172

    /**
     * Calculate wall area for a rectangular room.
     * Formula: 2 * (length + width) * height
     * (Assuming rectangular wall configuration. For custom wall count, we can adjust.)
     */
    fun calculateWallArea(
        length: Double,
        width: Double,
        height: Double,
        wallCount: Int = 4
    ): Double {
        if (length <= 0.0 || width <= 0.0 || height <= 0.0 || wallCount <= 0) return 0.0
        
        return if (wallCount == 4) {
            2 * (length + width) * height
        } else {
            // For custom wall count, we estimate average wall width as average of length & width
            val avgWidth = (length + width) / 2.0
            wallCount * avgWidth * height
        }
    }

    /**
     * Calculate ceiling area.
     * Formula: length * width
     */
    fun calculateCeilingArea(length: Double, width: Double): Double {
        if (length <= 0.0 || width <= 0.0) return 0.0
        return length * width
    }

    /**
     * Calculate opening area.
     * Formula: width * height * quantity
     */
    fun calculateOpeningArea(width: Double, height: Double, quantity: Int): Double {
        if (width <= 0.0 || height <= 0.0 || quantity <= 0) return 0.0
        return width * height * quantity
    }

    /**
     * Calculate paintable area by deducting openings from walls (and optionally adding ceiling).
     */
    fun calculatePaintableArea(
        wallArea: Double,
        ceilingArea: Double,
        totalOpeningsArea: Double
    ): Double {
        val grossArea = wallArea + ceilingArea
        val netArea = grossArea - totalOpeningsArea
        return if (netArea < 0.0) 0.0 else netArea
    }

    /**
     * Calculate paint required in liters or gallons.
     * Formula: (Paintable Area * Coats / Coverage) * (1 + Wastage)
     */
    fun calculatePaintRequired(
        paintableArea: Double,
        coverage: Double,
        coats: Int,
        wastagePercentage: Double
    ): Double {
        if (paintableArea <= 0.0 || coverage <= 0.0 || coats <= 0) return 0.0
        val basePaint = (paintableArea * coats) / coverage
        return basePaint * (1.0 + wastagePercentage)
    }

    /**
     * Round recommended paint/primer quantity.
     * e.g., round to 1 decimal place or ceil to integer for purchase recommendations.
     */
    fun roundQuantity(quantity: Double): Double {
        return (quantity * 100.0).roundToInt() / 100.0
    }

    fun recommendPurchaseQuantity(quantity: Double): Int {
        return ceil(quantity).toInt()
    }

    /**
     * Calculate cost based on quantity and unit price.
     */
    fun calculateCost(quantity: Double, pricePerUnit: Double): Double {
        if (quantity <= 0.0 || pricePerUnit <= 0.0) return 0.0
        return quantity * pricePerUnit
    }

    /**
     * Calculate labor cost automatically based on paintable area and rate,
     * or use manual cost.
     */
    fun calculateLaborCost(
        paintableArea: Double,
        ratePerUnit: Double,
        isManual: Boolean,
        manualCost: Double
    ): Double {
        return if (isManual) {
            if (manualCost < 0.0) 0.0 else manualCost
        } else {
            if (paintableArea <= 0.0 || ratePerUnit <= 0.0) return 0.0
            paintableArea * ratePerUnit
        }
    }

    /**
     * Calculate sum of additional materials.
     */
    fun calculateMaterialsCost(materials: List<Material>): Double {
        return materials.sumOf { it.quantity * it.unitPrice }
    }

    // --- Conversions ---

    fun convertLength(value: Double, toImperial: Boolean): Double {
        return if (toImperial) value * METERS_TO_FEET else value / METERS_TO_FEET
    }

    fun convertArea(value: Double, toImperial: Boolean): Double {
        return if (toImperial) value * SQ_METERS_TO_SQ_FEET else value / SQ_METERS_TO_SQ_FEET
    }

    fun convertVolume(value: Double, toImperial: Boolean): Double {
        return if (toImperial) value * LITERS_TO_GALLONS else value / LITERS_TO_GALLONS
    }

    fun calculateBasePaintRequired(surfaceArea: Double, coverage: Double, coats: Int): Double {
        if (surfaceArea <= 0.0 || coverage <= 0.0 || coats <= 0) return 0.0
        return (surfaceArea * coats) / coverage
    }

    fun calculatePaintWithWastage(basePaint: Double, wastagePercentage: Double): Double {
        if (basePaint <= 0.0) return 0.0
        val factor = if (wastagePercentage > 1.0) wastagePercentage / 100.0 else wastagePercentage
        return basePaint * (1.0 + factor)
    }
}
