package com.example.paintcalculatorestimator.utils

import kotlin.math.max
import kotlin.math.ceil

object WallCalculator {

    fun calculateSingleWallArea(width: Double, height: Double): Double {
        if (width <= 0.0 || height <= 0.0) return 0.0
        return width * height
    }

    fun calculateMultipleWallArea(walls: List<Pair<Double, Double>>): Double {
        return walls.sumOf { (width, height) ->
            if (width <= 0.0 || height <= 0.0) 0.0 else width * height
        }
    }

    fun calculateRoomWallArea(length: Double, width: Double, height: Double): Double {
        if (length <= 0.0 || width <= 0.0 || height <= 0.0) return 0.0
        return 2 * (length + width) * height
    }

    fun calculateOpeningArea(width: Double, height: Double, quantity: Int): Double {
        if (width <= 0.0 || height <= 0.0 || quantity <= 0) return 0.0
        return width * height * quantity
    }

    fun calculatePaintableWallArea(grossWallArea: Double, totalOpeningArea: Double): Double {
        return max(0.0, grossWallArea - totalOpeningArea)
    }

    fun calculateWallVolume(netArea: Double, thickness: Double, isImperial: Boolean): Double {
        if (netArea <= 0.0 || thickness <= 0.0) return 0.0
        return if (isImperial) {
            // netArea in sq ft, thickness in inches. Volume in cu ft = netArea * (thickness / 12)
            val volCuFt = netArea * (thickness / 12.0)
            // convert to cubic meters
            volCuFt * 0.0283168
        } else {
            // netArea in sq meters, thickness in cm. Volume in cu meters = netArea * (thickness / 100)
            netArea * (thickness / 100.0)
        }
    }

    fun calculateBricksCount(wallVolumeM3: Double): Int {
        if (wallVolumeM3 <= 0.0) return 0
        // Volume with 10mm mortar joint = 0.20 * 0.10 * 0.10 = 0.002 m³
        val count = wallVolumeM3 / 0.002
        return ceil(count).toInt()
    }

    fun calculateBlocksCount(wallVolumeM3: Double): Int {
        if (wallVolumeM3 <= 0.0) return 0
        // Volume with 10mm mortar joint = 0.40 * 0.20 * 0.20 = 0.016 m³
        val count = wallVolumeM3 / 0.016
        return ceil(count).toInt()
    }

    fun calculateCementBagsForBricks(wallVolumeM3: Double, bricksCount: Int): Double {
        if (wallVolumeM3 <= 0.0 || bricksCount <= 0) return 0.0
        // Volume of modular brick without joint = 0.19 * 0.09 * 0.09 = 0.001539 m³
        val bricksVol = bricksCount * 0.001539
        val wetMortarVol = max(0.0, wallVolumeM3 - bricksVol)
        val dryMortarVol = wetMortarVol * 1.33
        // 1:6 ratio: cement volume is dryMortarVol / 7
        val cementVol = dryMortarVol / 7.0
        // 1 bag = 0.035 m³
        return cementVol / 0.035
    }

    fun calculateCementBagsForBlocks(wallVolumeM3: Double, blocksCount: Int): Double {
        if (wallVolumeM3 <= 0.0 || blocksCount <= 0) return 0.0
        // Volume of standard block without joint = 0.39 * 0.19 * 0.19 = 0.014079 m³
        val blocksVol = blocksCount * 0.014079
        val wetMortarVol = max(0.0, wallVolumeM3 - blocksVol)
        val dryMortarVol = wetMortarVol * 1.33
        // 1:6 ratio: cement volume is dryMortarVol / 7
        val cementVol = dryMortarVol / 7.0
        // 1 bag = 0.035 m³
        return cementVol / 0.035
    }

    fun calculateSandVolumeForBricks(wallVolumeM3: Double, bricksCount: Int): Double {
        if (wallVolumeM3 <= 0.0 || bricksCount <= 0) return 0.0
        val bricksVol = bricksCount * 0.001539
        val wetMortarVol = max(0.0, wallVolumeM3 - bricksVol)
        val dryMortarVol = wetMortarVol * 1.33
        // 1:6 ratio: sand volume is dryMortarVol * 6 / 7
        return dryMortarVol * 6.0 / 7.0
    }

    fun calculateSandVolumeForBlocks(wallVolumeM3: Double, blocksCount: Int): Double {
        if (wallVolumeM3 <= 0.0 || blocksCount <= 0) return 0.0
        val blocksVol = blocksCount * 0.014079
        val wetMortarVol = max(0.0, wallVolumeM3 - blocksVol)
        val dryMortarVol = wetMortarVol * 1.33
        // 1:6 ratio: sand volume is dryMortarVol * 6 / 7
        return dryMortarVol * 6.0 / 7.0
    }
}
