package com.example.paintcalculatorestimator

import com.example.paintcalculatorestimator.utils.SlabCalculator
import com.example.paintcalculatorestimator.utils.WallCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomConstructionCalculatorTest {

    @Test
    fun testWallConstructionMath() {
        // Wall Volume: Net Area = 50m², Thickness = 23cm, Metric
        // Vol = 50 * 0.23 = 11.5 m³
        val vol = WallCalculator.calculateWallVolume(50.0, 23.0, false)
        assertEquals(11.5, vol, 0.001)

        // Bricks Count: 11.5 / 0.002 = 5750 bricks
        val bricks = WallCalculator.calculateBricksCount(vol)
        assertEquals(5750, bricks)

        // Blocks Count: 11.5 / 0.016 = 718.75 -> 719 blocks
        val blocks = WallCalculator.calculateBlocksCount(vol)
        assertEquals(719, blocks)

        // Cement bags for bricks:
        // Bricks Vol only = 5750 * 0.001539 = 8.84925 m³
        // Wet Mortar = 11.5 - 8.84925 = 2.65075 m³
        // Dry Mortar = 2.65075 * 1.33 = 3.5255 m³
        // Cement Vol (1:6) = 3.5255 / 7 = 0.50364 m³
        // Bags = 0.50364 / 0.035 = 14.389 bags
        val cementBricks = WallCalculator.calculateCementBagsForBricks(vol, bricks)
        assertEquals(14.389, cementBricks, 0.01)

        // Sand volume for bricks (in m3):
        // Sand Vol = 3.5255 * 6 / 7 = 3.0218 m³
        val sandBricks = WallCalculator.calculateSandVolumeForBricks(vol, bricks)
        assertEquals(3.0218, sandBricks, 0.01)
    }

    @Test
    fun testSlabConstructionMath() {
        // Slab: 5m x 4m x 12cm = 2.4 m³
        val vol = SlabCalculator.calculateSlabVolume(5.0, 4.0, 12.0, false)
        assertEquals(2.4, vol, 0.001)

        // Cement bags: Dry = 2.4 * 1.54 = 3.696 m³
        // Cement Vol = 3.696 / 7 = 0.528 m³
        // Bags = 0.528 / 0.035 = 15.08 bags
        val bags = SlabCalculator.calculateCementBags(vol)
        assertEquals(15.08, bags, 0.01)

        // Steel weight: 2.4 m³ * 80 kg/m³ = 192 kg
        val steel = SlabCalculator.calculateSteelWeight(vol)
        assertEquals(192.0, steel, 0.001)
    }
}
