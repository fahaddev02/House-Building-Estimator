package com.example.paintcalculatorestimator.utils

import com.example.paintcalculatorestimator.domain.model.Material

object CostCalculator {

    fun calculatePaintCost(quantity: Double, pricePerUnit: Double): Double {
        if (quantity <= 0.0 || pricePerUnit <= 0.0) return 0.0
        return quantity * pricePerUnit
    }

    fun calculatePrimerCost(quantity: Double, pricePerUnit: Double): Double {
        if (quantity <= 0.0 || pricePerUnit <= 0.0) return 0.0
        return quantity * pricePerUnit
    }

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

    fun calculateMaterialCost(materials: List<Material>): Double {
        return materials.sumOf { it.quantity * it.unitPrice }
    }

    fun calculateTotalCost(
        paintCost: Double,
        primerCost: Double,
        laborCost: Double,
        materialsCost: Double
    ): Double {
        return paintCost + primerCost + laborCost + materialsCost
    }
}
