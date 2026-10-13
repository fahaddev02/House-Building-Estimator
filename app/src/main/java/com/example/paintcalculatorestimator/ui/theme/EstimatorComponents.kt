package com.example.paintcalculatorestimator.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

enum class EstimatorSymbol { HOUSE, WALL, ROOF, PAINT, COST, BUILDING, SHOP, PLAN }

/** Small, scalable construction pictograms; no bitmap or icon-library dependency. */
@Composable
fun EstimatorIcon(symbol: EstimatorSymbol, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.size(24.dp)) {
        withTransform({ scale(size.width / 24f, size.height / 24f, Offset.Zero) }) {
            fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(tint, Offset(x, y), Offset(x2, y2), 1.6f, StrokeCap.Round)
            fun box(x: Float, y: Float, w: Float, h: Float) = drawRoundRect(tint, Offset(x, y), Size(w, h), androidx.compose.ui.geometry.CornerRadius(1.5f), style = Stroke(1.5f))
            when (symbol) {
                EstimatorSymbol.HOUSE, EstimatorSymbol.ROOF -> {
                    line(3f, 11f, 12f, 3f); line(12f, 3f, 21f, 11f)
                    line(5f, 10f, 5f, 21f); line(19f, 10f, 19f, 21f); line(5f, 21f, 19f, 21f)
                    if (symbol == EstimatorSymbol.HOUSE) box(10f, 14f, 4f, 7f)
                    else { line(8f, 12f, 16f, 12f); line(8f, 15f, 16f, 15f) }
                }
                EstimatorSymbol.WALL -> {
                    box(3f, 4f, 18f, 16f)
                    line(3f, 9f, 21f, 9f); line(3f, 15f, 21f, 15f)
                    line(9f, 4f, 9f, 9f); line(16f, 4f, 16f, 9f)
                    line(12f, 9f, 12f, 15f); line(9f, 15f, 9f, 20f); line(16f, 15f, 16f, 20f)
                }
                EstimatorSymbol.PAINT -> {
                    val path = Path().apply { moveTo(12f, 3f); cubicTo(10f, 7f, 5f, 11f, 5f, 15f); cubicTo(5f, 23f, 19f, 23f, 19f, 15f); cubicTo(19f, 11f, 14f, 7f, 12f, 3f); close() }
                    drawPath(path, tint, style = Stroke(1.6f)); line(8f, 15f, 9f, 18f)
                }
                EstimatorSymbol.COST -> {
                    drawCircle(tint, 9f, Offset(12f, 12f), style = Stroke(1.5f))
                    val p = Path().apply { moveTo(16f, 7f); lineTo(10f, 7f); cubicTo(6f, 7f, 6f, 12f, 11f, 12f); lineTo(13f, 12f); cubicTo(18f, 12f, 18f, 17f, 13f, 17f); lineTo(8f, 17f) }
                    drawPath(p, tint, style = Stroke(1.6f)); line(12f, 5f, 12f, 19f)
                }
                EstimatorSymbol.BUILDING -> {
                    box(5f, 3f, 14f, 18f)
                    for (x in listOf(8f, 14f)) for (y in listOf(7f, 12f)) box(x, y, 2f, 2f)
                    box(10f, 17f, 4f, 4f)
                }
                EstimatorSymbol.SHOP -> {
                    box(4f, 9f, 16f, 12f); box(3f, 4f, 18f, 5f); box(7f, 14f, 4f, 7f)
                    line(15f, 13f, 18f, 13f); line(15f, 16f, 18f, 16f)
                }
                EstimatorSymbol.PLAN -> {
                    box(5f, 3f, 14f, 18f)
                    line(8f, 8f, 16f, 8f); line(8f, 12f, 16f, 12f); line(8f, 16f, 13f, 16f)
                }
            }
        }
    }
}

@Composable
fun IconTile(symbol: EstimatorSymbol, tint: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
        EstimatorIcon(symbol, tint = tint)
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Box(modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(BrandPurple, BrandPurpleDeep))), contentAlignment = Alignment.Center) {
        EstimatorIcon(EstimatorSymbol.HOUSE, tint = Color.White)
    }
}

/** Shared input treatment retains the caller's validation, keyboard and focus behavior. */
@Composable
fun EstimateTextField(
    value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null, singleLine: Boolean = false,
    isError: Boolean = false, supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true, readOnly: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1, textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = modifier,
        label = label, singleLine = singleLine, isError = isError, supportingText = supportingText,
        keyboardOptions = keyboardOptions, placeholder = placeholder,
        leadingIcon = leadingIcon, trailingIcon = trailingIcon, enabled = enabled, readOnly = readOnly,
        maxLines = maxLines, minLines = minLines, textStyle = textStyle,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun EstimatePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}