package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.util.LanguageHelper

@Composable
fun DoubleEntryFlowBadge(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (tx.type) {
            TransactionType.EXPENSE -> {
                // Debit: Expense Category/Subcategory, Credit: Payment Account
                val debitText = item.subCategory?.localizedName(languageMode)
                    ?: item.category?.localizedName(languageMode)
                    ?: "Expense"
                val creditText = item.creditAccount?.localizedName(languageMode) ?: "Account"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                AccountPill(label = "Cr", name = creditText, isDebit = false)
            }
            TransactionType.INCOME -> {
                // Debit: Asset Account, Credit: Income Category
                val debitText = item.debitAccount?.localizedName(languageMode) ?: "Account"
                val creditText = item.subCategory?.localizedName(languageMode)
                    ?: item.category?.localizedName(languageMode)
                    ?: "Income"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                AccountPill(label = "Cr", name = creditText, isDebit = false)
            }
            TransactionType.TRANSFER -> {
                // Debit: Destination Account, Credit: Source Account
                val debitText = item.debitAccount?.localizedName(languageMode) ?: "Destination"
                val creditText = item.creditAccount?.localizedName(languageMode) ?: "Source"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                AccountPill(label = "Cr", name = creditText, isDebit = false)
            }
        }
    }
}

@Composable
private fun AccountPill(
    label: String,
    name: String,
    isDebit: Boolean
) {
    val bgColor = if (isDebit) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    }
    val tagColor = if (isDebit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tagColor
            )
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
