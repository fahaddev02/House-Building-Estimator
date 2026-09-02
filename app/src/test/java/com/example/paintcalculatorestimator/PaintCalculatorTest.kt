package com.example.paintcalculatorestimator

import com.example.paintcalculatorestimator.domain.model.Material
import com.example.paintcalculatorestimator.utils.PaintCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PaintCalculatorTest {

    @Test
    fun testCalculateWallArea_rectangularRoom() {
        // Length = 5 m, Width = 4 m, Height = 3 m, 4 walls
        // Wall Area = 2 × (5 + 4) × 3 = 54 m²
        val wallArea = PaintCalculator.calculateWallArea(5.0, 4.0, 3.0, 4)
        assertEquals(54.0, wallArea, 0.001)
    }

    @Test
    fun testCalculateWallArea_customWalls() {
        // 3 walls: average width = (5 + 4) / 2 = 4.5
        // Area = 3 * 4.5 * 3 = 40.5
        val wallArea = PaintCalculator.calculateWallArea(5.0, 4.0, 3.0, 3)
        assertEquals(40.5, wallArea, 0.001)
    }

    @Test
    fun testCalculateCeilingArea() {
        // 5 m × 4 m = 20 m²
        val ceilingArea = PaintCalculator.calculateCeilingArea(5.0, 4.0)
        assertEquals(20.0, ceilingArea, 0.001)
    }

    @Test
    fun testCalculateOpeningArea() {
        // Width = 1.0 m, Height = 2.0 m, Qty = 2
        // Area = 1.0 * 2.0 * 2 = 4.0 m²
        val openingArea = PaintCalculator.calculateOpeningArea(1.0, 2.0, 2)
        assertEquals(4.0, openingArea, 0.001)
    }

    @Test
    fun testCalculatePaintableArea_withDeduction() {
        // Wall Area = 54 m², Ceiling = 20 m², Openings = 5 m²
        // Total Area = 54 + 20 - 5 = 69 m²
        val paintableArea = PaintCalculator.calculatePaintableArea(54.0, 20.0, 5.0)
        assertEquals(69.0, paintableArea, 0.001)
    }

    @Test
    fun testCalculatePaintRequired_withWastage() {
        // Paintable Area = 49 m²
        // Coats = 2
        // Coverage = 10 m²/L
        // Wastage = 10% (0.10)
        // Required Paint = 49 × 2 / 10 = 9.8 L
        // With wastage: 9.8 × 1.10 = 10.78 L
        val paintRequired = PaintCalculator.calculatePaintRequired(49.0, 10.0, 2, 0.10)
        assertEquals(10.78, paintRequired, 0.001)
    }

    @Test
    fun testCalculatePaintRequired_imperial() {
        // Paintable Area = 500 sq ft
        // Coats = 2
        // Coverage = 350 sq ft/gal
        // Wastage = 15%
        // Required Paint = 500 * 2 / 350 = 2.857 gal
        // With wastage = 2.857 * 1.15 = 3.2857 gal
        val paintRequired = PaintCalculator.calculatePaintRequired(500.0, 350.0, 2, 0.15)
        assertEquals(3.2857, paintRequired, 0.001)
    }

    @Test
    fun testCalculateLaborCost_automatic() {
        // Paintable Area = 49 m², Rate = PKR 150
        // Cost = 49 * 150 = PKR 7350
        val laborCost = PaintCalculator.calculateLaborCost(49.0, 150.0, false, 0.0)
        assertEquals(7350.0, laborCost, 0.001)
    }

    @Test
    fun testCalculateLaborCost_manual() {
        // Manual Cost = PKR 10000
        val laborCost = PaintCalculator.calculateLaborCost(49.0, 150.0, true, 10000.0)
        assertEquals(10000.0, laborCost, 0.001)
    }

    @Test
    fun testCalculateMaterialsCost() {
        val materials = listOf(
            Material(name = "Putty", quantity = 2.0, unitPrice = 500.0),
            Material(name = "Tape", quantity = 3.0, unitPrice = 100.0)
        )
        val cost = PaintCalculator.calculateMaterialsCost(materials)
        assertEquals(1300.0, cost, 0.001)
    }

    @Test
    fun testEdgeCases_zeroAndNegativeValues() {
        assertEquals(0.0, PaintCalculator.calculateWallArea(-5.0, 4.0, 3.0, 4), 0.0)
        assertEquals(0.0, PaintCalculator.calculateCeilingArea(0.0, 4.0), 0.0)
        assertEquals(0.0, PaintCalculator.calculateOpeningArea(1.0, 2.0, -1), 0.0)
        assertEquals(0.0, PaintCalculator.calculatePaintRequired(49.0, -10.0, 2, 0.10), 0.0)
        assertEquals(0.0, PaintCalculator.calculatePaintableArea(10.0, 5.0, 20.0), 0.0) // deduction exceeds area
    }

    @Test
    fun testUnitConversions() {
        // 1 meter = 3.28084 feet
        assertEquals(3.28084, PaintCalculator.convertLength(1.0, true), 0.0001)
        // 1 sq meter = 10.76391 sq feet
        assertEquals(10.76391, PaintCalculator.convertArea(1.0, true), 0.0001)
        // 1 liter = 0.264172 gallons
        assertEquals(0.264172, PaintCalculator.convertVolume(1.0, true), 0.0001)
    }
}
