package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionLabel
import com.example.ui.components.AppColorPickerDialog
import com.example.ui.components.IconPickerModal
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper

private val labelPresetColors = listOf(
    "#10B981", "#3B82F6", "#8B5CF6", "#EC4899", "#F59E0B",
    "#EF4444", "#06B6D4", "#6366F1", "#14B8A6", "#64748B"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLabelDialog(
    label: TransactionLabel? = null,
    initialName: String = "",
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String, colorHex: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(label?.name ?: initialName) }
    var selectedIconName by remember { mutableStateOf(label?.iconName ?: "Label") }
    var selectedColorHex by remember { mutableStateOf(label?.colorHex ?: "#10B981") }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isBn = languageMode == LanguageMode.BANGLA
    val parsedColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            SolidPrimary
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_label_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (label != null) {
                            if (isBn) "লেবেল সম্পাদনা" else "Edit Label"
                        } else {
                            if (isBn) "নতুন লেবেল" else "New Label"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Preview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBn) "প্রিভিউ" else "Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = parsedColor.copy(alpha = 0.16f),
                            border = BorderStroke(0.6.dp, parsedColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                IconHelper.AppIcon(
                                    iconName = selectedIconName,
                                    tint = parsedColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (name.isNotBlank()) name else (if (isBn) "লেবেলের নাম" else "Label Name"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Label Name Input Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text(if (isBn) "লেবেলের নাম" else "Label Name") },
                    placeholder = { Text(if (isBn) "যেমন: ফুড, গ্রোসারি, শপিং" else "e.g. Food, Groceries, Shopping") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("label_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Icon Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBn) "আইকন পরিবর্তন" else "Select Icon",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIconPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(parsedColor.copy(alpha = 0.15f))
                                        .border(
                                            BorderStroke(0.6.dp, parsedColor.copy(alpha = 0.35f)),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconHelper.AppIcon(
                                        iconName = selectedIconName,
                                        tint = parsedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedIconName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBn) "আইকন পরিবর্তন করতে স্পর্শ করুন" else "Tap to choose an icon",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Color Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "রঙ নির্বাচন" else "Select Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        labelPresetColors.take(7).forEach { colorHex ->
                            val color = Color(android.graphics.Color.parseColor(colorHex))
                            val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = colorHex }
                            )
                        }
                        // Custom Palette Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable { showColorPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Custom color",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Actions: Delete (if editing), Cancel, Save
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (label != null && onDelete != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_label_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text(LanguageHelper.getString("cancel", languageMode))
                        }
                        Button(
                            onClick = {
                                val trimmed = name.trim().removePrefix("#")
                                if (trimmed.isBlank()) {
                                    nameError = if (isBn) "লেবেলের নাম খালি রাখা যাবে না" else "Label name cannot be empty"
                                } else {
                                    onSave(trimmed, selectedIconName, selectedColorHex)
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_label_button")
                        ) {
                            Text(LanguageHelper.getString("save", languageMode).ifEmpty { "Save" })
                        }
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerModal(
            selectedIconName = selectedIconName,
            onIconSelected = { icon ->
                selectedIconName = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false },
            initialQuery = name
        )
    }

    if (showColorPicker) {
        AppColorPickerDialog(
            initialColorHex = selectedColorHex,
            onColorSelected = { hex ->
                selectedColorHex = hex
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(if (isBn) "লেবেল মুছে ফেলবেন?" else "Delete Label?")
            },
            text = {
                Text(
                    if (isBn)
                        "\"${name}\" লেবেলটি মুছে ফেলতে চান? লেনদেনগুলো মুছে যাবে না।"
                    else
                        "Are you sure you want to delete label \"${name}\"? Transactions will keep their data."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }
}
