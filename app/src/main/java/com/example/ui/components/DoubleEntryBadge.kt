package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidOnIncomeContainer
import com.example.ui.theme.SolidOnPrimaryContainer
import com.example.ui.theme.SolidPrimaryContainer

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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (tx.type) {
            TransactionType.EXPENSE -> {
                val debitText = item.subCategory?.localizedName(languageMode)
                    ?: item.category?.localizedName(languageMode)
                    ?: "Expense"
                val creditText = item.creditAccount?.localizedName(languageMode) ?: "Account"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(10.dp)
                )
                AccountPill(label = "Cr", name = creditText, isDebit = false)
            }
            TransactionType.INCOME -> {
                val debitText = item.debitAccount?.localizedName(languageMode) ?: "Account"
                val creditText = item.subCategory?.localizedName(languageMode)
                    ?: item.category?.localizedName(languageMode)
                    ?: "Income"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(10.dp)
                )
                AccountPill(label = "Cr", name = creditText, isDebit = false)
            }
            TransactionType.TRANSFER -> {
                val debitText = item.debitAccount?.localizedName(languageMode) ?: "Destination"
                val creditText = item.creditAccount?.localizedName(languageMode) ?: "Source"

                AccountPill(label = "Dr", name = debitText, isDebit = true)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(10.dp)
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
    val bgColor = if (isDebit) SolidPrimaryContainer else SolidIncomeContainer
    val tagColor = if (isDebit) SolidOnPrimaryContainer else SolidOnIncomeContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label ",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tagColor
            )
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
