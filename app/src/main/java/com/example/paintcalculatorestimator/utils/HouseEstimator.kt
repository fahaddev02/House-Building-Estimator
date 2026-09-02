package com.example.paintcalculatorestimator.utils

import kotlin.math.ceil
import kotlin.math.max

data class HouseRoomInput(
    val id: Long = 0,
    val name: String,
    val type: String, // "Master Bedroom", "Bedroom", "Living Room", "TV Lounge", "Drawing Room", "Kitchen", "Dining Room", "Store Room", "Garage", "Veranda / Baramda", "Other Custom Room"
    val length: Double,
    val width: Double,
    val height: Double,
    val thickness: Double, // cm or inches
    val materialType: String, // "Brick" or "Block"
    val doorsCount: Int = 0,
    val doorsWidth: Double = 0.9,
    val doorsHeight: Double = 2.1,
    val windowsCount: Int = 0,
    val windowsWidth: Double = 1.2,
    val windowsHeight: Double = 1.2,
    val hasRoof: Boolean = true,

    // Attached Washroom Options
    val isWashroomEnabled: Boolean = false,
    val washroomLength: Double = 0.0,
    val washroomWidth: Double = 0.0,
    val washroomHeight: Double = 0.0,
    val washroomThickness: Double = 0.0,
    val washroomDoorsCount: Int = 1,
    val washroomDoorsWidth: Double = 0.75,
    val washroomDoorsHeight: Double = 2.0,
    val washroomWindowsCount: Int = 1,
    val washroomWindowsWidth: Double = 0.6,
    val washroomWindowsHeight: Double = 0.6,
    val washroomMaterialType: String = "Brick",
    val quantity: Int = 1
)

object HouseEstimator {

    fun calculateRoomWallVolume(room: HouseRoomInput, isImperial: Boolean): Double {
        if (room.length <= 0.0 || room.width <= 0.0 || room.height <= 0.0 || room.thickness <= 0.0) return 0.0
        val grossArea = 2 * (room.length + room.width) * room.height
        val deductions = (room.doorsCount * room.doorsWidth * room.doorsHeight) +
                (room.windowsCount * room.windowsWidth * room.windowsHeight)
        val netArea = max(0.0, grossArea - deductions)

        return if (isImperial) {
            val volCft = netArea * (room.thickness / 12.0)
            volCft * 0.0283168
        } else {
            netArea * (room.thickness / 100.0)
        }
    }

    fun calculateWashroomWallVolume(room: HouseRoomInput, isImperial: Boolean): Double {
        if (!room.isWashroomEnabled || room.washroomLength <= 0.0 || room.washroomWidth <= 0.0 || room.washroomHeight <= 0.0 || room.washroomThickness <= 0.0) return 0.0
        val grossArea = 2 * (room.washroomLength + room.washroomWidth) * room.washroomHeight
        val deductions = (room.washroomDoorsCount * room.washroomDoorsWidth * room.washroomDoorsHeight) +
                (room.washroomWindowsCount * room.washroomWindowsWidth * room.washroomWindowsHeight)
        val netArea = max(0.0, grossArea - deductions)

        return if (isImperial) {
            val volCft = netArea * (room.washroomThickness / 12.0)
            volCft * 0.0283168
        } else {
            netArea * (room.washroomThickness / 100.0)
        }
    }

    fun calculateSlabVolume(room: HouseRoomInput, slabThickness: Double, isImperial: Boolean): Double {
        if (!room.hasRoof || room.length <= 0.0 || room.width <= 0.0 || slabThickness <= 0.0) return 0.0
        return if (isImperial) {
            val volCft = room.length * room.width * (slabThickness / 12.0)
            volCft * 0.0283168
        } else {
            room.length * room.width * (slabThickness / 100.0)
        }
    }

    fun calculateBoundaryWallVolume(length: Double, height: Double, thickness: Double, gateWidth: Double, gateHeight: Double, isImperial: Boolean): Double {
        if (length <= 0.0 || height <= 0.0 || thickness <= 0.0) return 0.0
        val grossArea = length * height
        val deductions = gateWidth * gateHeight
        val netArea = max(0.0, grossArea - deductions)

        return if (isImperial) {
            val volCft = netArea * (thickness / 12.0)
            volCft * 0.0283168
        } else {
            netArea * (thickness / 100.0)
        }
    }

    fun calculateBoundaryPillarsVolume(length: Double, height: Double, thickness: Double, isImperial: Boolean): Double {
        if (length <= 0.0 || height <= 0.0 || thickness <= 0.0) return 0.0
        val spacing = if (isImperial) 10.0 else 3.0
        val pillarCount = ceil(length / spacing).toInt() + 1
        
        return if (isImperial) {
            val thickFt = thickness / 12.0
            val volCft = pillarCount * thickFt * thickFt * height
            volCft * 0.0283168
        } else {
            val thickM = thickness / 100.0
            pillarCount * thickM * thickM * height
        }
    }

    // --- CONFIGURABLE MASONS CALCULATIONS ---

    fun calculateUnitQuantity(
        wallVolumeM3: Double,
        l: Double, w: Double, h: Double,
        isImperial: Boolean
    ): Int {
        if (wallVolumeM3 <= 0.0 || l <= 0.0 || w <= 0.0 || h <= 0.0) return 0
        val lMet = if (isImperial) (l / 12.0) * 0.3048 else l / 100.0
        val wMet = if (isImperial) (w / 12.0) * 0.3048 else w / 100.0
        val hMet = if (isImperial) (h / 12.0) * 0.3048 else h / 100.0
        val jointMet = 0.01

        val volWithJoint = (lMet + jointMet) * (wMet + jointMet) * (hMet + jointMet)
        val count = wallVolumeM3 / volWithJoint
        return ceil(count).toInt()
    }

    fun calculateCementBags(
        wallVolumeM3: Double,
        quantity: Int,
        l: Double, w: Double, h: Double,
        isImperial: Boolean
    ): Double {
        if (wallVolumeM3 <= 0.0 || quantity <= 0) return 0.0
        val lMet = if (isImperial) (l / 12.0) * 0.3048 else l / 100.0
        val wMet = if (isImperial) (w / 12.0) * 0.3048 else w / 100.0
        val hMet = if (isImperial) (h / 12.0) * 0.3048 else h / 100.0

        val volNoJoint = lMet * wMet * hMet
        val unitVolOnly = quantity * volNoJoint
        val wetMortar = max(0.0, wallVolumeM3 - unitVolOnly)
        val dryMortar = wetMortar * 1.33
        val cementVol = dryMortar / 7.0
        return cementVol / 0.035
    }

    fun calculateSandVolume(
        wallVolumeM3: Double,
        quantity: Int,
        l: Double, w: Double, h: Double,
        isImperial: Boolean
    ): Double {
        if (wallVolumeM3 <= 0.0 || quantity <= 0) return 0.0
        val lMet = if (isImperial) (l / 12.0) * 0.3048 else l / 100.0
        val wMet = if (isImperial) (w / 12.0) * 0.3048 else w / 100.0
        val hMet = if (isImperial) (h / 12.0) * 0.3048 else h / 100.0

        val volNoJoint = lMet * wMet * hMet
        val unitVolOnly = quantity * volNoJoint
        val wetMortar = max(0.0, wallVolumeM3 - unitVolOnly)
        val dryMortar = wetMortar * 1.33
        return dryMortar * 6.0 / 7.0
    }
}
