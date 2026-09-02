package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedButton
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("popup_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("quick_calc", languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else {
                                if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(expression) else expression
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val displayResult = previewResult ?: 0.0
                        Text(
                            text = LanguageHelper.formatCurrency(displayResult, languageMode),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Add Chips (+50, +100, +500, +1000)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(50.0, 100.0, 500.0, 1000.0, 5000.0)
                    items(presets) { preset ->
                        val presetText = "+${LanguageHelper.formatNumber(preset, languageMode, includeDecimals = false)}"
                        AssistChip(
                            onClick = { addPreset(preset) },
                            label = {
                                Text(
                                    text = presetText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keypad Layout (4 rows x 4 cols)
                val buttonRows = listOf(
                    listOf("C", "÷", "×", "⌫"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "="),
                    listOf("0", "00", ".", "DONE")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                val isOperator = key in listOf("+", "-", "×", "÷", "=")
                                val isAction = key in listOf("C", "⌫", "DONE")
                                val isDone = key == "DONE"

                                val flexModifier = if (isDone) {
                                    Modifier.weight(1f)
                                } else {
                                    Modifier.weight(1f)
                                }

                                val displayKey = when {
                                    key == "DONE" -> LanguageHelper.getString("done", languageMode)
                                    languageMode == LanguageMode.BANGLA && key in "0".."9" -> LanguageHelper.toBanglaDigits(key)
                                    languageMode == LanguageMode.BANGLA && key == "00" -> "০০"
                                    else -> key
                                }

                                val containerColor = when {
                                    isDone -> MaterialTheme.colorScheme.primary
                                    isOperator -> MaterialTheme.colorScheme.primaryContainer
                                    isAction -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                val contentColor = when {
                                    isDone -> MaterialTheme.colorScheme.onPrimary
                                    isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isAction -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = flexModifier
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(containerColor)
                                        .clickable {
                                            when (key) {
                                                "C" -> clear()
                                                "⌫" -> backspace()
                                                "=" -> {
                                                    val eval = CalculatorEvaluator.evaluate(expression)
                                                    if (eval.isSuccess) {
                                                        val res = eval.getOrNull() ?: 0.0
                                                        updateExpression(
                                                            String.format("%.2f", res).trimEnd('0').trimEnd('.')
                                                        )
                                                    }
                                                }
                                                "DONE" -> {
                                                    val finalVal = previewResult ?: 0.0
                                                    onValueConfirmed(finalVal)
                                                    onDismiss()
                                                }
                                                else -> append(key)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = contentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = displayKey,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isOperator || isDone) FontWeight.Bold else FontWeight.Medium,
                                            color = contentColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
