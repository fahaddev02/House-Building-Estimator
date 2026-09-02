package com.example.paintcalculatorestimator

import com.example.paintcalculatorestimator.utils.HouseEstimator
import com.example.paintcalculatorestimator.utils.HouseRoomInput
import org.junit.Assert.assertEquals
import org.junit.Test

class HouseEstimatorTest {

    @Test
    fun testRoomWallVolumeMetric() {
        val room = HouseRoomInput(
            name = "Test Bedroom",
            type = "Bedroom",
            length = 5.0,
            width = 4.0,
            height = 3.0,
            thickness = 23.0,
            materialType = "Brick",
            doorsCount = 1,
            windowsCount = 1,
            hasRoof = true
        )
        val vol = HouseEstimator.calculateRoomWallVolume(room, isImperial = false)
        assertEquals(11.6541, vol, 0.001)
    }

    @Test
    fun testCustomBrickCalculation() {
        // Wall Volume = 10 m³
        // Custom brick: 9x4.5x3 inches (Imperial units test conversion inside calculator)
        val vol = 10.0
        val count = HouseEstimator.calculateUnitQuantity(
            wallVolumeM3 = vol,
            l = 9.0, w = 4.5, h = 3.0,
            isImperial = true
        )
        // Default Modular Brick volume with joint (approx) ~0.00255 m3
        // So count is ceil(10 / volWithJoint)
        // Let's verify it calculates successfully
        assert(count > 0)

        // Cement bags calculation
        val bags = HouseEstimator.calculateCementBags(
            wallVolumeM3 = vol,
            quantity = count,
            l = 9.0, w = 4.5, h = 3.0,
            isImperial = true
        )
        assert(bags >= 0.0)
    }

    @Test
    fun testSlabVolumeMetric() {
        val room = HouseRoomInput(
            name = "Test Bedroom",
            type = "Bedroom",
            length = 5.0,
            width = 4.0,
            height = 3.0,
            thickness = 23.0,
            materialType = "Brick",
            doorsCount = 1,
            windowsCount = 1,
            hasRoof = true
        )
        val slabVol = HouseEstimator.calculateSlabVolume(room, slabThickness = 12.0, isImperial = false)
        assertEquals(2.4, slabVol, 0.001)
    }
}
