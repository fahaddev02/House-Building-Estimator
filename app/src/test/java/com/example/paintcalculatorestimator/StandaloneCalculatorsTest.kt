package com.example.paintcalculatorestimator

import com.example.paintcalculatorestimator.domain.model.Material
import com.example.paintcalculatorestimator.utils.CeilingCalculator
import com.example.paintcalculatorestimator.utils.CostCalculator
import com.example.paintcalculatorestimator.utils.PaintCalculator
import com.example.paintcalculatorestimator.utils.WallCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class StandaloneCalculatorsTest {

    @Test
    fun testWallCalculator() {
        // Single wall: 5m x 3m = 15m²
        assertEquals(15.0, WallCalculator.calculateSingleWallArea(5.0, 3.0), 0.001)

        // Multiple walls: 5x3 (15) + 4x3 (12) = 27m²
        val walls = listOf(5.0 to 3.0, 4.0 to 3.0)
        assertEquals(27.0, WallCalculator.calculateMultipleWallArea(walls), 0.001)

        // Room wall: 2 * (5 + 4) * 3 = 54m²
        assertEquals(54.0, WallCalculator.calculateRoomWallArea(5.0, 4.0, 3.0), 0.001)

        // Openings: 1.0 x 2.0 x 2 = 4m²
        assertEquals(4.0, WallCalculator.calculateOpeningArea(1.0, 2.0, 2), 0.001)

        // Paintable area: 54m² - 4m² = 50m²
        assertEquals(50.0, WallCalculator.calculatePaintableWallArea(54.0, 4.0), 0.001)
        
        // Edge cases
        assertEquals(0.0, WallCalculator.calculateSingleWallArea(-5.0, 3.0), 0.0)
        assertEquals(0.0, WallCalculator.calculatePaintableWallArea(10.0, 15.0), 0.0)
    }

    @Test
    fun testCeilingCalculator() {
        // Rectangle ceiling: 5m x 4m = 20m²
        assertEquals(20.0, CeilingCalculator.calculateRectangleArea(5.0, 4.0), 0.001)

        // Square ceiling: 4m x 4m = 16m²
        assertEquals(16.0, CeilingCalculator.calculateSquareArea(4.0), 0.001)

        // Custom ceiling: 5x4 (20) + 3x2 (6) = 26m²
        val sections = listOf(5.0 to 4.0, 3.0 to 2.0)
        assertEquals(26.0, CeilingCalculator.calculateCustomSectionsArea(sections), 0.001)

        // Edge cases
        assertEquals(0.0, CeilingCalculator.calculateSquareArea(-4.0), 0.0)
        assertEquals(0.0, CeilingCalculator.calculateRectangleArea(5.0, 0.0), 0.0)
    }

    @Test
    fun testPaintCalculatorMethods() {
        // Base paint required: Area = 49m², Coverage = 10m²/L, Coats = 2
        // Required = 49 * 2 / 10 = 9.8 L
        val basePaint = PaintCalculator.calculateBasePaintRequired(49.0, 10.0, 2)
        assertEquals(9.8, basePaint, 0.001)

        // Paint with wastage: 9.8 L, Wastage = 10% (0.10 or 10.0)
        assertEquals(10.78, PaintCalculator.calculatePaintWithWastage(basePaint, 0.10), 0.001)
        assertEquals(10.78, PaintCalculator.calculatePaintWithWastage(basePaint, 10.0), 0.001)

        // Edge cases
        assertEquals(0.0, PaintCalculator.calculateBasePaintRequired(-49.0, 10.0, 2), 0.0)
        assertEquals(0.0, PaintCalculator.calculatePaintWithWastage(0.0, 10.0), 0.0)
    }

    @Test
    fun testCostCalculator() {
        // Paint cost: 11 L @ PKR 2500/L = PKR 27500
        assertEquals(27500.0, CostCalculator.calculatePaintCost(11.0, 2500.0), 0.001)

        // Primer cost: 5 L @ PKR 1500/L = PKR 7500
        assertEquals(7500.0, CostCalculator.calculatePrimerCost(5.0, 1500.0), 0.001)

        // Labor cost automatic: 50m² @ PKR 200/m² = PKR 10000
        assertEquals(10000.0, CostCalculator.calculateLaborCost(50.0, 200.0, false, 0.0), 0.001)

        // Labor cost manual: PKR 15000
        assertEquals(15000.0, CostCalculator.calculateLaborCost(50.0, 200.0, true, 15000.0), 0.001)

        // Materials cost:
        val materials = listOf(
            Material(name = "Brush", quantity = 2.0, unitPrice = 300.0),
            Material(name = "Tape", quantity = 3.0, unitPrice = 150.0)
        )
        assertEquals(1050.0, CostCalculator.calculateMaterialCost(materials), 0.001)

        // Total cost: Paint (27500) + Primer (7500) + Labor (10000) + Materials (1050) = PKR 46050
        assertEquals(46050.0, CostCalculator.calculateTotalCost(27500.0, 7500.0, 10000.0, 1050.0), 0.001)
    }
}
