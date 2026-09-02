package com.example.paintcalculatorestimator.utils

object CeilingCalculator {

    fun calculateRectangleArea(length: Double, width: Double): Double {
        if (length <= 0.0 || width <= 0.0) return 0.0
        return length * width
    }

    fun calculateSquareArea(side: Double): Double {
        if (side <= 0.0) return 0.0
        return side * side
    }

    fun calculateCustomSectionsArea(sections: List<Pair<Double, Double>>): Double {
        return sections.sumOf { (length, width) ->
            if (length <= 0.0 || width <= 0.0) 0.0 else length * width
        }
    }
}
