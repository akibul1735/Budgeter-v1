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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
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

@Composable
fun PopupCalculatorDialog(
    initialValue: Double = 0.0,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onValueConfirmed: (Double) -> Unit
) {
    var expression by remember {
        mutableStateOf(
            if (initialValue > 0) {
                if (initialValue % 1.0 == 0.0) initialValue.toLong().toString() else initialValue.toString()
            } else ""
        )
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
        if (hasError && (char == "+" || char == "−" || char == "×" || char == "÷")) {
            return
        }
        // Prevent consecutive duplicate operators
        val operators = listOf("+", "−", "-", "×", "÷", "%")
        if (expression.isNotEmpty() && char in operators && expression.takeLast(1) in operators) {
            updateExpression(expression.dropLast(1) + char)
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
        val formatted = if (sum % 1.0 == 0.0) sum.toLong().toString() else String.format("%.2f", sum).trimEnd('0').trimEnd('.')
        updateExpression(formatted)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E281E),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("popup_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Handle Pill
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5A725D))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets
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
                                containerColor = Color(0xFF2B4D36),
                                labelColor = Color(0xFFC8E6C9)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Deep Green Display Screen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF264E36),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Formula / Sub-expression if multi-step
                        if (expression.contains("+") || expression.contains("−") || expression.contains("-") || expression.contains("×") || expression.contains("÷") || expression.contains("%")) {
                            Text(
                                text = expression,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA5D6A7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Main Big Value Display
                        val displayValue = when {
                            hasError -> "Error"
                            expression.isEmpty() -> "0"
                            previewResult != null -> {
                                val res = previewResult!!
                                if (res % 1.0 == 0.0 && !expression.contains(".")) res.toLong().toString()
                                else if (!expression.contains("+") && !expression.contains("−") && !expression.contains("-") && !expression.contains("×") && !expression.contains("÷")) expression
                                else if (res % 1.0 == 0.0) res.toLong().toString()
                                else String.format("%.2f", res).trimEnd('0').trimEnd('.')
                            }
                            else -> expression
                        }

                        Text(
                            text = displayValue,
                            fontSize = 38.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = if (hasError) Color(0xFFFF8A80) else Color(0xFFF1F8E9),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5x4 Keypad matching Image 2
                // Colors definition
                val numBgColor = Color(0xFFBCE0B8)     // Minty green for numbers
                val numTextColor = Color(0xFF1E281E)   // Dark text
                val opBgColor = Color(0xFFCCE4A8)      // Pastel sage for operators & backspace
                val opTextColor = Color(0xFF1E281E)    // Dark text
                val clearBgColor = Color(0xFFE55A2B)   // Vibrant orange for C
                val clearTextColor = Color(0xFF1E281E) // Dark text
                val okBgColor = Color(0xFF43A047)      // Vibrant rich green for OK
                val okTextColor = Color(0xFF1E281E)    // Dark text

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: 7, 8, 9, ÷
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton(text = "7", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("7") }
                        CalcButton(text = "8", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("8") }
                        CalcButton(text = "9", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("9") }
                        CalcButton(text = "÷", bg = opBgColor, textColor = opTextColor, modifier = Modifier.weight(1f)) { append("÷") }
                    }

                    // Row 2: 4, 5, 6, ×
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton(text = "4", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("4") }
                        CalcButton(text = "5", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("5") }
                        CalcButton(text = "6", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("6") }
                        CalcButton(text = "×", bg = opBgColor, textColor = opTextColor, modifier = Modifier.weight(1f)) { append("×") }
                    }

                    // Row 3: 1, 2, 3, −
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton(text = "1", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("1") }
                        CalcButton(text = "2", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("2") }
                        CalcButton(text = "3", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("3") }
                        CalcButton(text = "−", bg = opBgColor, textColor = opTextColor, modifier = Modifier.weight(1f)) { append("−") }
                    }

                    // Row 4: ., 0, %, +
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton(text = ".", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append(".") }
                        CalcButton(text = "0", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("0") }
                        CalcButton(text = "%", bg = numBgColor, textColor = numTextColor, modifier = Modifier.weight(1f)) { append("%") }
                        CalcButton(text = "+", bg = opBgColor, textColor = opTextColor, modifier = Modifier.weight(1f)) { append("+") }
                    }

                    // Row 5: C, OK (2 columns width), ⌫
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear Button (C)
                        CalcButton(
                            text = "C",
                            bg = clearBgColor,
                            textColor = clearTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { clear() }
                        )

                        // OK Confirm Button (spans 2 columns)
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(okBgColor)
                                .clickable {
                                    val finalVal = previewResult ?: 0.0
                                    onValueConfirmed(finalVal)
                                    onDismiss()
                                }
                                .testTag("calculator_ok_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OK",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = okTextColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Backspace Button (⌫)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(opBgColor)
                                .clickable { backspace() }
                                .testTag("calculator_backspace_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = opTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .testTag("calc_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (text == "−" || text == "+" || text == "×" || text == "÷") 26.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
