package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.util.LanguageHelper
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    selectedDateEpochMs: Long,
    languageMode: LanguageMode,
    quickSelectMode: Boolean = true,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDate = remember { selectedDateEpochMs }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateEpochMs
    )
    var isQuickModeActive by remember { mutableStateOf(quickSelectMode) }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Small delay to allow state initialization without premature firing
        kotlinx.coroutines.delay(120)
        isInitialized = true
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val picked = datePickerState.selectedDateMillis
        if (picked != null && isInitialized && isQuickModeActive) {
            onDateSelected(picked)
            onDismiss()
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!isQuickModeActive) {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("date_picker_confirm_btn")
                ) {
                    Text(
                        text = LanguageHelper.getString("save", languageMode),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = LanguageHelper.getString("cancel", languageMode))
            }
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Compact Title, 1-Tap Mode Toggle & Quick Action Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isQuickModeActive = !isQuickModeActive }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isQuickModeActive)
                            (if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার (১-ট্যাপ অটো)" else "1-Tap Auto Select")
                        else
                            (if (languageMode == LanguageMode.BANGLA) "ক্যালেন্ডার" else "Calendar"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val now = System.currentTimeMillis()
                    val oneDay = 86400000L

                    AssistChip(
                        onClick = {
                            onDateSelected(now)
                            onDismiss()
                        },
                        label = { Text(LanguageHelper.getString("today", languageMode), fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    )

                    AssistChip(
                        onClick = {
                            onDateSelected(now - oneDay)
                            onDismiss()
                        },
                        label = { Text(LanguageHelper.getString("yesterday", languageMode), fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Compact DatePicker without redundant huge title/headline
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
