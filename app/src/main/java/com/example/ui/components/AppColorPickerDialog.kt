package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper

object AppColorPickerPresets {
    val POPULAR_COLORS = listOf(
        "#10B981", // Emerald
        "#2563EB", // Royal Blue
        "#7C3AED", // Violet
        "#EF4444", // Crimson Red
        "#F59E0B", // Amber Gold
        "#06B6D4", // Cyan
        "#EC4899", // Pink
        "#0D9488", // Teal
        "#1E293B", // Slate Dark
        "#FFFFFF", // Pure White
        "#64748B", // Cool Grey
        "#000000", // Black
        "#EA580C", // Flame Orange
        "#84CC16", // Lime
        "#4F46E5", // Indigo
        "#D97706", // Deep Amber
        "#B91C1C", // Dark Burgundy
        "#0284C7"  // Sky Azure
    )

    fun colorToHex(color: Color): String {
        val argb = color.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun parseHexSafe(hex: String, fallback: Color = Color(0xFF10B981)): Color {
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> {
                    val intVal = clean.toLong(16)
                    Color(0xFF000000 or intVal)
                }
                8 -> {
                    val intVal = clean.toLong(16)
                    Color(intVal)
                }
                3 -> {
                    // e.g. #FFF -> #FFFFFF
                    val r = clean[0].toString().repeat(2).toInt(16)
                    val g = clean[1].toString().repeat(2).toInt(16)
                    val b = clean[2].toString().repeat(2).toInt(16)
                    Color(r, g, b)
                }
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    fun isValidHex(hex: String): Boolean {
        val clean = hex.trim().removePrefix("#")
        return (clean.length == 6 || clean.length == 8) && clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }
}

@Composable
fun AppColorPickerDialog(
    initialColorHex: String,
    title: String = "Pick Color",
    description: String? = "Choose from popular presets, adjust with the color picker, or enter a hex code:",
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialColor = remember(initialColorHex) {
        AppColorPickerPresets.parseHexSafe(initialColorHex, Color(0xFF10B981))
    }

    // HSV representation
    val hsv = remember { FloatArray(3) }
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(if (hsv[1] == 0f && initialColor != Color.White && initialColor != Color.Black) 0.8f else hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    var hexInputText by remember {
        mutableStateOf(AppColorPickerPresets.colorToHex(initialColor))
    }

    // Current active color derived from HSV or validated Hex
    val currentColor = remember(hue, saturation, value) {
        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
        Color(colorInt)
    }

    // Update hexInputText when HSV changes
    LaunchedEffect(hue, saturation, value) {
        val calculatedHex = AppColorPickerPresets.colorToHex(currentColor)
        if (!hexInputText.equals(calculatedHex, ignoreCase = true)) {
            hexInputText = calculatedHex
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 1. Highly Popular Colors (Top)
                Text(
                    text = "Highly Popular Colors",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 6 columns grid of popular colors
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppColorPickerPresets.POPULAR_COLORS.chunked(6).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowColors.forEach { hex ->
                                val swatchColor = AppColorPickerPresets.parseHexSafe(hex)
                                val isSelected = hexInputText.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 3.dp else if (hex == "#FFFFFF") 1.5.dp else 1.dp,
                                            color = if (isSelected) SolidPrimary else if (hex == "#FFFFFF") Color(0xFFDDDDDD) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            hexInputText = hex
                                            val tempHsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(swatchColor.toArgb(), tempHsv)
                                            hue = tempHsv[0]
                                            saturation = tempHsv[1]
                                            value = tempHsv[2]
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        val checkTint = if (hex == "#FFFFFF" || hex == "#84CC16") Color.Black else Color.White
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = checkTint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Custom Color Picker (Interactive Sliders)
                Text(
                    text = "Custom Color Picker",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Hue Spectrum Slider
                Text(
                    text = "Hue Spectrum",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red
                                )
                            )
                        )
                )
                Slider(
                    value = hue,
                    onValueChange = {
                        hue = it
                        if (saturation < 0.15f) saturation = 0.85f
                        if (value < 0.15f) value = 0.9f
                    },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = SolidPrimary,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.height(28.dp)
                )

                // Saturation Slider
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Saturation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${(saturation * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = SolidPrimary, activeTrackColor = SolidPrimary),
                    modifier = Modifier.height(28.dp)
                )

                // Brightness Slider
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Brightness", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${(value * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = SolidPrimary, activeTrackColor = SolidPrimary),
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Enter Color Code (Hex Input & Live Preview)
                Text(
                    text = "Enter Color Code",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Color preview circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(currentColor)
                                .border(
                                    2.dp,
                                    if (currentColor == Color.White) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentColor == Color.White) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.15f))
                                )
                            }
                        }

                        // Hex code text field
                        OutlinedTextField(
                            value = hexInputText,
                            onValueChange = { input ->
                                hexInputText = input
                                val clean = input.trim()
                                if (AppColorPickerPresets.isValidHex(clean)) {
                                    val parsed = AppColorPickerPresets.parseHexSafe(clean)
                                    val tempHsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(parsed.toArgb(), tempHsv)
                                    hue = tempHsv[0]
                                    saturation = tempHsv[1]
                                    value = tempHsv[2]
                                }
                            },
                            label = { Text("Color Code (Hex)") },
                            singleLine = true,
                            trailingIcon = {
                                if (AppColorPickerPresets.isValidHex(hexInputText)) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Valid Color",
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SolidPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHex = if (AppColorPickerPresets.isValidHex(hexInputText)) {
                        val clean = hexInputText.trim().removePrefix("#")
                        "#${clean.uppercase()}"
                    } else {
                        AppColorPickerPresets.colorToHex(currentColor)
                    }
                    onColorSelected(finalHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply Color", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
