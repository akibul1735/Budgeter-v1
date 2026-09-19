package com.example.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.CustomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationStudioSheet(
    isBangla: Boolean,
    initialTheme: CustomTheme? = null,
    onDismiss: () -> Unit,
    onSaveCustomTheme: (CustomTheme) -> Unit
) {
    var themeName by remember {
        mutableStateOf(initialTheme?.name ?: if (isBangla) "আমার কাস্টম থিম" else "My Custom Theme")
    }

    // Color Swatches (Standard, pleasing, non-disturbing palettes)
    val standardPrimarySwatches = listOf(
        0xFF059669L to "Emerald",
        0xFF1D4ED8L to "Sapphire",
        0xFF0284C7L to "Sky Azure",
        0xFFB45309L to "Warm Amber",
        0xFF7C3AEDL to "Imperial Violet",
        0xFF0D9488L to "Mint Teal",
        0xFF4F46E5L to "Indigo",
        0xFF38BDF8L to "Slate Cyan",
        0xFFE11D48L to "Ruby Rose",
        0xFF475569L to "Graphite"
    )

    val headerToneSwatches = listOf(
        0xFF0D281EL to "Deep Emerald",
        0xFF0E2248L to "Midnight Cobalt",
        0xFF1E293BL to "Slate Navy",
        0xFF18181BL to "Dark Zinc",
        0xFF2E1C0AL to "Warm Espresso",
        0xFF13111CL to "Deep Obsidian",
        0xFF000000L to "Pure OLED"
    )

    val incomeSwatches = listOf(
        0xFF059669L to "Emerald Green",
        0xFF10B981L to "Bright Mint",
        0xFF0D9488L to "Teal Cyan",
        0xFF15803DL to "Deep Forest"
    )

    val expenseSwatches = listOf(
        0xFFE11D48L to "Rose Crimson",
        0xFFEF4444L to "Coral Red",
        0xFFF43F5EL to "Flamingo",
        0xFFB91C1CL to "Deep Ruby"
    )

    val transferSwatches = listOf(
        0xFF0284C7L to "Sky Blue",
        0xFF3B82F6L to "Ocean Blue",
        0xFF6366F1L to "Royal Indigo",
        0xFF8B5CF6L to "Violet"
    )

    var selectedPrimaryHex by remember {
        mutableLongStateOf(initialTheme?.primaryColorHex ?: 0xFF059669L)
    }

    var selectedHeaderHex by remember {
        mutableLongStateOf(initialTheme?.headerColorHex ?: 0xFF0D281EL)
    }

    var selectedIncomeHex by remember {
        mutableLongStateOf(initialTheme?.incomeColorHex ?: 0xFF059669L)
    }

    var selectedExpenseHex by remember {
        mutableLongStateOf(initialTheme?.expenseColorHex ?: 0xFFE11D48L)
    }

    var selectedTransferHex by remember {
        mutableLongStateOf(initialTheme?.transferColorHex ?: 0xFF0284C7L)
    }

    var shadeIntensity by remember {
        mutableFloatStateOf(initialTheme?.shadeIntensity?.toFloat() ?: 100f)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = Color(selectedPrimaryHex),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBangla) "থিম কাস্টমাইজেশন স্টুডিও" else "Theme Customization Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "বিভিন্ন সেকশন, শেড ও রঙের সূক্ষ্ম সমন্বয়" else "Adjust section shades, accents & financial flow colors",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 1. Live Real-Time Studio Preview Card
            Text(
                text = if (isBangla) "লাইভ প্রিভিউ (রিয়েল-টাইম)" else "Live Real-Time Preview",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(selectedPrimaryHex)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(selectedHeaderHex)),
                border = BorderStroke(1.dp, Color(selectedPrimaryHex).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "মোট ব্যালেন্স ও সঞ্চয়" else "Net Balance & Savings",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBangla) "৳ ১,৮৫,৪৫০.০০" else "৳ 185,450.00",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(selectedPrimaryHex)
                        ) {
                            Text(
                                text = if (isBangla) "সক্রিয়" else "Active",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Income Pill
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(selectedIncomeHex),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "আয়" else "Income",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "+৳ 45,000",
                                        color = Color(selectedIncomeHex),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Expense Pill
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(selectedExpenseHex),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "ব্যয়" else "Expense",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "-৳ 18,200",
                                        color = Color(selectedExpenseHex),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Theme Name Input
            OutlinedTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = { Text(if (isBangla) "থিমের নাম" else "Custom Theme Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(selectedPrimaryHex),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Section: Primary Accent Color Selection
            Text(
                text = if (isBangla) "প্রধান অ্যাকসেন্ট রঙ (Primary Accent)" else "Primary Accent Color",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                standardPrimarySwatches.take(5).forEach { (hex, _) ->
                    ColorCircleButton(
                        colorHex = hex,
                        isSelected = selectedPrimaryHex == hex,
                        onClick = { selectedPrimaryHex = hex }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                standardPrimarySwatches.drop(5).forEach { (hex, _) ->
                    ColorCircleButton(
                        colorHex = hex,
                        isSelected = selectedPrimaryHex == hex,
                        onClick = { selectedPrimaryHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. Section: Upper Card & Header Area Tone
            Text(
                text = if (isBangla) "উপরের সেকশন ও কার্ড শেড (Header & Top Card Tone)" else "Header & Top Card Tone",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                headerToneSwatches.take(4).forEach { (hex, _) ->
                    ColorCircleButton(
                        colorHex = hex,
                        isSelected = selectedHeaderHex == hex,
                        onClick = { selectedHeaderHex = hex }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                headerToneSwatches.drop(4).forEach { (hex, _) ->
                    ColorCircleButton(
                        colorHex = hex,
                        isSelected = selectedHeaderHex == hex,
                        onClick = { selectedHeaderHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 4. Section: Financial Flow Semantic Colors
            Text(
                text = if (isBangla) "আয় ও ব্যয়ের রঙ (Financial Flow Indicators)" else "Financial Flow Indicators",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income color selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "আয়ের রঙ" else "Income Color",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        incomeSwatches.forEach { (hex, _) ->
                            ColorCircleButton(
                                colorHex = hex,
                                isSelected = selectedIncomeHex == hex,
                                sizeDp = 28,
                                onClick = { selectedIncomeHex = hex }
                            )
                        }
                    }
                }

                // Expense color selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "ব্যয়ের রঙ" else "Expense Color",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        expenseSwatches.forEach { (hex, _) ->
                            ColorCircleButton(
                                colorHex = hex,
                                isSelected = selectedExpenseHex == hex,
                                sizeDp = 28,
                                onClick = { selectedExpenseHex = hex }
                            )
                        }
                    }
                }
            }

            // Transfer color selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBangla) "স্থানান্তর / নিরপেক্ষ সূচক" else "Transfer / Neutral Color",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    transferSwatches.forEach { (hex, _) ->
                        ColorCircleButton(
                            colorHex = hex,
                            isSelected = selectedTransferHex == hex,
                            sizeDp = 28,
                            onClick = { selectedTransferHex = hex }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 5. Section: Shade & Saturation Intensity
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "শেড ও স্যাচুরেশন তীব্রতা" else "Shade & Saturation Intensity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${shadeIntensity.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(selectedPrimaryHex)
                    )
                }

                Slider(
                    value = shadeIntensity,
                    onValueChange = { shadeIntensity = it },
                    valueRange = 70f..130f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(selectedPrimaryHex),
                        activeTrackColor = Color(selectedPrimaryHex)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Save & Apply / Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedPrimaryHex = 0xFF059669L
                        selectedHeaderHex = 0xFF0D281EL
                        selectedIncomeHex = 0xFF059669L
                        selectedExpenseHex = 0xFFE11D48L
                        selectedTransferHex = 0xFF0284C7L
                        shadeIntensity = 100f
                        themeName = if (isBangla) "আমার কাস্টম থিম" else "My Custom Theme"
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "রিসেট" else "Reset", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val customTheme = CustomTheme(
                            id = initialTheme?.id ?: "theme_${System.currentTimeMillis()}",
                            name = themeName.trim().ifBlank { "Custom Theme" },
                            primaryColorHex = selectedPrimaryHex,
                            secondaryColorHex = selectedTransferHex,
                            surfaceColorHex = null,
                            headerColorHex = selectedHeaderHex,
                            incomeColorHex = selectedIncomeHex,
                            expenseColorHex = selectedExpenseHex,
                            transferColorHex = selectedTransferHex,
                            shadeIntensity = shadeIntensity.toInt()
                        )
                        onSaveCustomTheme(customTheme)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(selectedPrimaryHex)),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "সংরক্ষণ ও প্রয়োগ" else "Save & Apply",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorCircleButton(
    colorHex: Long,
    isSelected: Boolean,
    sizeDp: Int = 36,
    onClick: () -> Unit
) {
    val color = Color(colorHex)
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size((sizeDp * 0.55).dp)
            )
        }
    }
}
