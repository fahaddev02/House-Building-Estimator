package com.example.paintcalculatorestimator.utils

import java.util.Locale

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}

object Validator {
    fun validateRequiredField(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        if (value.trim().isEmpty()) {
            return ValidationResult.Invalid(customEmptyMsg ?: "Please enter ${fieldName.lowercase(Locale.ROOT)}")
        }
        return ValidationResult.Valid
    }

    fun validatePositiveNumber(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        val req = validateRequiredField(value, fieldName, customEmptyMsg)
        if (req is ValidationResult.Invalid) return req

        val num = value.toDoubleOrNull()
            ?: return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")

        if (num.isNaN() || num.isInfinite()) {
            return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")
        }
        if (num <= 0.0) {
            val capitalizedFieldName = fieldName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return ValidationResult.Invalid("$capitalizedFieldName must be greater than 0")
        }
        return ValidationResult.Valid
    }

    fun validateNonNegativeNumber(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        val req = validateRequiredField(value, fieldName, customEmptyMsg)
        if (req is ValidationResult.Invalid) return req

        val num = value.toDoubleOrNull()
            ?: return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")

        if (num.isNaN() || num.isInfinite()) {
            return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")
        }
        if (num < 0.0) {
            val capitalizedFieldName = fieldName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return ValidationResult.Invalid("$capitalizedFieldName cannot be negative")
        }
        return ValidationResult.Valid
    }

    fun validateInteger(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        val req = validateRequiredField(value, fieldName, customEmptyMsg)
        if (req is ValidationResult.Invalid) return req

        val num = value.toIntOrNull()
            ?: return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")

        if (num <= 0) {
            val capitalizedFieldName = fieldName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return ValidationResult.Invalid("$capitalizedFieldName must be greater than 0")
        }
        return ValidationResult.Valid
    }

    fun validatePositiveInteger(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        return validateInteger(value, fieldName, customEmptyMsg)
    }

    fun validateNonNegativeInteger(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        val req = validateRequiredField(value, fieldName, customEmptyMsg)
        if (req is ValidationResult.Invalid) return req

        val num = value.toIntOrNull()
            ?: return ValidationResult.Invalid("Please enter a valid ${fieldName.lowercase(Locale.ROOT)}")

        if (num < 0) {
            val capitalizedFieldName = fieldName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return ValidationResult.Invalid("$capitalizedFieldName cannot be negative")
        }
        return ValidationResult.Valid
    }

    fun validateDecimal(value: String, fieldName: String, customEmptyMsg: String? = null): ValidationResult {
        return validatePositiveNumber(value, fieldName, customEmptyMsg)
    }
}
