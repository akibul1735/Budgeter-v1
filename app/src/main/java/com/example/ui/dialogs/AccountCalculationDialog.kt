package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcSetting
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun AccountCalculationDialog(
    account: Account,
    actualBalance: Double,
    currentSetting: AccountCalcSetting,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (isIncluded: Boolean, adjustment: Double) -> Unit,
    onReset: () -> Unit
) {
    var isIncluded by remember { mutableStateOf(currentSetting.isIncluded) }
    var adjustmentAmount by remember { mutableStateOf(currentSetting.adjustmentAmount) }
    
    // String representations for text fields
    val initialEffective = actualBalance + currentSetting.adjustmentAmount
    var targetAmountText by remember {
        mutableStateOf(if (currentSetting.adjustmentAmount != 0.0) String.format("%.2f", initialEffective).removeSuffix(".00") else "")
    }
    var adjustmentText by remember {
        mutableStateOf(if (currentSetting.adjustmentAmount != 0.0) String.format("%.2f", currentSetting.adjustmentAmount).removeSuffix(".00") else "")
    }

    val typeColor = when (account.type) {
        AccountType.ASSET -> SolidIncome
        AccountType.LIABILITY -> SolidExpense
        else -> SolidPrimary
    }

    val effectiveBalance = if (isIncluded) actualBalance + adjustmentAmount else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_calc_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SolidPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = LanguageHelper.getString("account_calculation", languageMode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = account.localizedName(languageMode),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Account Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(typeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(account.iconName),
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = account.localizedName(languageMode),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = LanguageHelper.getString("actual_balance", languageMode),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Text(
                            text = LanguageHelper.formatCurrency(actualBalance, languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 1. Include / Exclude Toggle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isIncluded) SolidIncome.copy(alpha = 0.08f)
                        else SolidExpense.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isIncluded) SolidIncome.copy(alpha = 0.3f) else SolidExpense.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (isIncluded) SolidIncome else SolidExpense,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isIncluded) LanguageHelper.getString("include_in_calculation", languageMode)
                                    else LanguageHelper.getString("exclude_from_calculation", languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncluded) SolidIncome else SolidExpense
                                )
                                Text(
                                    text = if (isIncluded) "Balance is included in total calculations"
                                    else "Excluded from total calculations (0 in totals)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isIncluded,
                            onCheckedChange = { isIncluded = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SolidIncome,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SolidExpense
                            ),
                            modifier = Modifier.testTag("include_exclude_switch")
                        )
                    }
                }

                // 2. Adjustment Section (Only visible when included)
                if (isIncluded) {
                    Text(
                        text = LanguageHelper.getString("adjust_calculation", languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Target Calculated Amount Input
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { input ->
                            targetAmountText = input
                            val target = input.toDoubleOrNull()
                            if (target != null) {
                                val adj = target - actualBalance
                                adjustmentAmount = adj
                                adjustmentText = String.format("%.2f", adj).removeSuffix(".00")
                            } else if (input.isBlank()) {
                                adjustmentAmount = 0.0
                                adjustmentText = ""
                            }
                        },
                        label = { Text(LanguageHelper.getString("target_calc_balance", languageMode), fontSize = 12.sp) },
                        placeholder = { Text(String.format("%.2f", actualBalance), fontSize = 12.sp) },
                        leadingIcon = { Text("৳", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_calc_amount_input")
                    )

                    // Adjustment Amount (+/-) Input
                    OutlinedTextField(
                        value = adjustmentText,
                        onValueChange = { input ->
                            adjustmentText = input
                            val adj = input.toDoubleOrNull()
                            if (adj != null) {
                                adjustmentAmount = adj
                                val target = actualBalance + adj
                                targetAmountText = String.format("%.2f", target).removeSuffix(".00")
                            } else if (input.isBlank()) {
                                adjustmentAmount = 0.0
                                targetAmountText = ""
                            }
                        },
                        label = { Text("${LanguageHelper.getString("adjustment_amount", languageMode)} (+/-)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. -7000 or +1000", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adjustment_amount_input")
                    )

                    // Quick Adjustment Chips
                    Text(
                        text = LanguageHelper.getString("quick_adjust", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-500.0, -1000.0, -5000.0, 1000.0).forEach { delta ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val newAdj = adjustmentAmount + delta
                                    adjustmentAmount = newAdj
                                    val newTarget = actualBalance + newAdj
                                    adjustmentText = String.format("%.2f", newAdj).removeSuffix(".00")
                                    targetAmountText = String.format("%.2f", newTarget).removeSuffix(".00")
                                },
                                label = {
                                    Text(
                                        text = if (delta > 0) "+৳${delta.toInt()}" else "-৳${(-delta).toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        if (adjustmentAmount != 0.0) {
                            FilterChip(
                                selected = true,
                                onClick = {
                                    adjustmentAmount = 0.0
                                    adjustmentText = ""
                                    targetAmountText = ""
                                },
                                label = {
                                    Text("Reset (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }

                    // Calculation Summary Result Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Original Balance:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(LanguageHelper.formatCurrency(actualBalance, languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (adjustmentAmount != 0.0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Adjustment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = (if (adjustmentAmount > 0) "+" else "") + LanguageHelper.formatCurrency(adjustmentAmount, languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (adjustmentAmount > 0) SolidIncome else SolidExpense
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageHelper.getString("effective_amount", languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidPrimary
                                )
                                Text(
                                    text = LanguageHelper.formatCurrency(effectiveBalance, languageMode),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolidPrimary
                                )
                            }
                        }
                    }
                }

                // Explanatory Note (Mandatory as per requirements)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("calc_adjust_note", languageMode),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(isIncluded, if (isIncluded) adjustmentAmount else 0.0)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                modifier = Modifier.testTag("save_account_calc_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!currentSetting.isIncluded || currentSetting.adjustmentAmount != 0.0) {
                    TextButton(
                        onClick = { onReset() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(LanguageHelper.getString("reset_calculation", languageMode), fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        }
    )
}
