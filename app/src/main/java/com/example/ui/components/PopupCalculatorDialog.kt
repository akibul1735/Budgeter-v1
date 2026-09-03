package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode
import com.example.util.CalculatorEvaluator
import com.example.util.LanguageHelper

@Composable
fun PopupCalculatorDialog(
    initialValue: Double = 0.0,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onValueConfirmed: (Double) -> Unit
) {
    var expression by remember {
        mutableStateOf(if (initialValue > 0) String.format("%.2f", initialValue).trimEnd('0').trimEnd('.') else "")
    }
    var previewResult by remember { mutableStateOf<Double?>(if (initialValue > 0) initialValue else null) }
    var hasError by remember { mutableStateOf(false) }

    fun updateExpression(newExpr: String) {
        expression = newExpr
        if (newExpr.isBlank()) {
            previewResult = 0.0
            hasError = false
        } else {
            val eval = CalculatorEvaluator.evaluate(newExpr)
            if (eval.isSuccess) {
                previewResult = eval.getOrNull()
                hasError = false
            } else {
                hasError = true
            }
        }
    }

    fun append(char: String) {
        if (hasError && (char == "+" || char == "-" || char == "×" || char == "÷")) {
            return
        }
        updateExpression(expression + char)
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            updateExpression(expression.dropLast(1))
        }
    }

    fun clear() {
        updateExpression("")
    }

    fun addPreset(amount: Double) {
        val current = previewResult ?: 0.0
        val sum = current + amount
        updateExpression(String.format("%.2f", sum).trimEnd('0').trimEnd('.'))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("popup_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Compact Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("quick_calc", languageMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High Contrast Expression & Result Screen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (hasError) "Error" else LanguageHelper.formatCurrency(previewResult ?: 0.0, languageMode),
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Increment Presets
                val presets = listOf(10.0, 50.0, 100.0, 500.0, 1000.0)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presets) { preset ->
                        AssistChip(
                            onClick = { addPreset(preset) },
                            label = {
                                Text(
                                    text = "+${preset.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4x4 Keypad Grid + Bottom Row
                val keyRows = listOf(
                    listOf("C", "÷", "×", "DEL"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "=")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keyRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                CompactCalcButton(
                                    key = key,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (key) {
                                            "C" -> clear()
                                            "DEL" -> backspace()
                                            "=" -> {
                                                val res = previewResult
                                                if (res != null) {
                                                    updateExpression(String.format("%.2f", res).trimEnd('0').trimEnd('.'))
                                                }
                                            }
                                            else -> append(key)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Row: "0", "00", ".", OK Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactCalcButton(
                            key = "0",
                            modifier = Modifier.weight(1f),
                            onClick = { append("0") }
                        )
                        CompactCalcButton(
                            key = "00",
                            modifier = Modifier.weight(1f),
                            onClick = { append("00") }
                        )
                        CompactCalcButton(
                            key = ".",
                            modifier = Modifier.weight(1f),
                            onClick = { append(".") }
                        )
                        Button(
                            onClick = {
                                val finalVal = previewResult ?: 0.0
                                onValueConfirmed(finalVal)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "OK", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCalcButton(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = key in listOf("+", "-", "×", "÷", "=")
    val isAction = key in listOf("C", "DEL")

    val bg = when {
        key == "C" -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        key == "DEL" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        isOperator -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when {
        key == "C" -> MaterialTheme.colorScheme.error
        key == "DEL" -> MaterialTheme.colorScheme.onSurface
        isOperator -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = key,
                fontSize = 17.sp,
                fontWeight = if (isOperator || isAction) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
