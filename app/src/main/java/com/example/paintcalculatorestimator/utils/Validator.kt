package com.example.paintcalculatorestimator.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import java.util.Locale

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}

fun Context.logE(message: String){
    Log.e(this.javaClass.simpleName, "${this.javaClass.simpleName} $message", )
}

fun Activity.logE(message: String){
    Log.e(this.javaClass.simpleName, "${this.javaClass.simpleName} $message", )
}

fun Fragment.logE(message: String){
    Log.e(this.javaClass.simpleName, "${this.javaClass.simpleName} $message", )
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
