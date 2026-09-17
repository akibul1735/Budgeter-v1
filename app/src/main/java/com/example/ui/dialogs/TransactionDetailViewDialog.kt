package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TransactionLinkHelper

@Composable
fun TransactionDetailViewDialog(
    item: TransactionWithDetails,
    allTransactions: List<TransactionWithDetails> = emptyList(),
    allAccounts: List<Account> = emptyList(),
    allCategories: List<Category> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onShowSimilar: (String) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val tx = item.transaction
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val isExpense = tx.type == TransactionType.EXPENSE
    val isIncome = tx.type == TransactionType.INCOME
    val isTransfer = tx.type == TransactionType.TRANSFER

    val linkedFeeItem = if (isTransfer) {
        TransactionLinkHelper.findLinkedFeeTransaction(tx, allTransactions)
    } else null

    val isSplitTx = TransactionLinkHelper.isSplitTransaction(tx)
    val splitSiblings = remember(tx, allTransactions) {
        if (isSplitTx) TransactionLinkHelper.findLinkedSplitTransactions(tx, allTransactions)
        else emptyList()
    }
    val totalSplitAmount = remember(splitSiblings) {
        if (splitSiblings.isNotEmpty()) TransactionLinkHelper.getSplitGroupTotal(splitSiblings)
        else tx.amount
    }

    // Determine Amount color matching Screenshot and M3 Design
    val amtColor = when {
        isTransfer -> Color(0xFF2563EB) // Royal Blue
        isIncome -> if (tx.amount >= 0) SolidIncome else SolidExpense
        else -> if (tx.amount >= 0) SolidExpense else SolidIncome
    }

    val sign = when {
        isTransfer -> ""
        isIncome -> if (tx.amount >= 0) "+ " else "- "
        else -> if (tx.amount >= 0) "- " else "+ "
    }

    // Resolve Category Group (Parent) and Category (Child / Self)
    val parentCategory = remember(item, allCategories) {
        val cat = item.category
        val sub = item.subCategory
        if (sub != null && sub.parentId != null) {
            allCategories.firstOrNull { it.id == sub.parentId } ?: cat
        } else if (cat != null && cat.parentId != null) {
            allCategories.firstOrNull { it.id == cat.parentId }
        } else {
            cat
        }
    }

    val childCategory = remember(item, allCategories) {
        if (item.subCategory != null) {
            item.subCategory
        } else {
            item.category
        }
    }

    val categoryGroupName = when {
        parentCategory != null -> parentCategory.localizedName(languageMode)
        item.category != null -> item.category.localizedName(languageMode)
        else -> if (languageMode == LanguageMode.BANGLA) "সাধারণ" else "Uncategorized"
    }

    val categoryItemName = when {
        childCategory != null && parentCategory != null && childCategory.id != parentCategory.id -> childCategory.localizedName(languageMode)
        childCategory != null -> childCategory.localizedName(languageMode)
        item.category != null -> item.category.localizedName(languageMode)
        else -> if (languageMode == LanguageMode.BANGLA) "সাধারণ" else "General"
    }

    // Resolve Account & Account Group
    val targetAccount = if (isExpense) item.creditAccount else item.debitAccount
    val parentAccount = remember(targetAccount, allAccounts) {
        if (targetAccount?.parentId != null) {
            allAccounts.firstOrNull { it.id == targetAccount.parentId }
        } else {
            targetAccount
        }
    }

    val accountGroupName = when {
        parentAccount != null && parentAccount.id != targetAccount?.id -> parentAccount.localizedName(languageMode)
        targetAccount != null -> when (targetAccount.type) {
            AccountType.ASSET -> if (languageMode == LanguageMode.BANGLA) "ক্যাশ ও অ্যাসেট" else "Cash accounts"
            AccountType.LIABILITY -> if (languageMode == LanguageMode.BANGLA) "দায় অ্যাকাউন্ট" else "Liability accounts"
            AccountType.EQUITY -> if (languageMode == LanguageMode.BANGLA) "ইক্যুইটি" else "Equity"
            else -> targetAccount.localizedName(languageMode)
        }
        else -> "-"
    }

    val accountItemName = targetAccount?.localizedName(languageMode) ?: "-"

    // Name / Payee
    val displayName = when {
        tx.payeeOrPayer.isNotBlank() -> tx.payeeOrPayer
        categoryItemName.isNotBlank() && categoryItemName != "-" -> categoryItemName
        categoryGroupName.isNotBlank() && categoryGroupName != "-" -> categoryGroupName
        else -> if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Transaction"
    }

    // Labels & Notes
    val labelsDisplay = if (tx.referenceNo.isNotBlank()) {
        tx.referenceNo
    } else {
        if (languageMode == LanguageMode.BANGLA) "কোনো লেবেল নেই" else "No labels"
    }

    val cleanNote = remember(tx.note) {
        TransactionLinkHelper.getCleanNote(tx.note)
    }

    val notesDisplay = if (cleanNote.isNotBlank()) {
        cleanNote
    } else {
        if (languageMode == LanguageMode.BANGLA) "কোনো নোট নেই" else "No notes"
    }

    // Date & Time formatting
    val formattedDateOnly = DateUtils.formatDate(tx.dateEpochMs, languageMode)
    val formattedTimeOnly = DateUtils.formatTime(tx.dateEpochMs, languageMode)
    val formattedDateTime = "$formattedDateOnly • $formattedTimeOnly"

    // Helper for toast feedback on selecting an item
    fun handleSelectSimilar(query: String, filterSubject: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isNotBlank() && cleanQuery != "-" && cleanQuery != "No labels" && cleanQuery != "No notes" && cleanQuery != "কোনো লেবেল নেই" && cleanQuery != "কোনো নোট নেই") {
            val msg = if (languageMode == LanguageMode.BANGLA) {
                "অনুরূপ লেনদেন দেখানো হচ্ছে: $cleanQuery"
            } else {
                "Showing similar transactions for: $cleanQuery"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onShowSimilar(cleanQuery)
        } else {
            val msg = if (languageMode == LanguageMode.BANGLA) {
                "$filterSubject নির্বাচন করা হয়েছে"
            } else {
                "$filterSubject selected"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 20.dp)
                .testTag("transaction_view_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Dismiss Close Icon (Subtle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Scrollable container for cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Top Card: Name (Selectable / Clickable)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                handleSelectSimilar(
                                    displayName,
                                    if (languageMode == LanguageMode.BANGLA) "নাম" else "Name"
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নাম" else "Name",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 2. Second Card: Amount & Date Badge (Selectable / Clickable)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val amtStr = LanguageHelper.formatNumber(kotlin.math.abs(tx.amount), languageMode)
                                handleSelectSimilar(
                                    amtStr,
                                    if (languageMode == LanguageMode.BANGLA) "পরিমাণ" else "Amount"
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Amount Display
                            Text(
                                text = "$sign${LanguageHelper.formatCurrency(kotlin.math.abs(tx.amount), languageMode)}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = amtColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Date & Time Greenish Chip Badge
                            val chipBg = if (isDark) Color(0xFF163826) else Color(0xFFD1E7DD)
                            val chipContentColor = if (isDark) Color(0xFF81C784) else Color(0xFF155724)
                            val chipIconColor = if (isDark) Color(0xFF81C784) else Color(0xFF198754)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = chipBg,
                                border = BorderStroke(0.5.dp, chipIconColor.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    handleSelectSimilar(
                                        formattedDateOnly,
                                        if (languageMode == LanguageMode.BANGLA) "তারিখ" else "Date"
                                    )
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = chipIconColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(
                                        text = formattedDateTime,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipContentColor
                                    )
                                }
                            }
                        }
                    }

                    // 3. Grid Row 1: Category Group & Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailGridCard(
                            title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি গ্রুপ" else "Category group",
                            value = categoryGroupName,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                handleSelectSimilar(
                                    categoryGroupName,
                                    if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি গ্রুপ" else "Category group"
                                )
                            }
                        )

                        DetailGridCard(
                            title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                            value = categoryItemName,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                handleSelectSimilar(
                                    categoryItemName,
                                    if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category"
                                )
                            }
                        )
                    }

                    // 4. Grid Row 2: Account Group & Account (or Transfer Route)
                    if (isTransfer) {
                        val fromAcc = item.creditAccount
                        val toAcc = item.debitAccount
                        val fromName = fromAcc?.localizedName(languageMode) ?: "-"
                        val toName = toAcc?.localizedName(languageMode) ?: "-"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailGridCard(
                                title = if (languageMode == LanguageMode.BANGLA) "উৎস অ্যাকাউন্ট" else "From account",
                                value = fromName,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (onAccountClick != null && fromAcc != null) {
                                        onAccountClick(fromAcc)
                                    } else {
                                        handleSelectSimilar(
                                            fromName,
                                            if (languageMode == LanguageMode.BANGLA) "উৎস অ্যাকাউন্ট" else "From account"
                                        )
                                    }
                                }
                            )

                            DetailGridCard(
                                title = if (languageMode == LanguageMode.BANGLA) "গন্তব্য অ্যাকাউন্ট" else "To account",
                                value = toName,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (onAccountClick != null && toAcc != null) {
                                        onAccountClick(toAcc)
                                    } else {
                                        handleSelectSimilar(
                                            toName,
                                            if (languageMode == LanguageMode.BANGLA) "গন্তব্য অ্যাকাউন্ট" else "To account"
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailGridCard(
                                title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট গ্রুপ" else "Account group",
                                value = accountGroupName,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    handleSelectSimilar(
                                        accountGroupName,
                                        if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট গ্রুপ" else "Account group"
                                    )
                                }
                            )

                            DetailGridCard(
                                title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account",
                                value = accountItemName,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (onAccountClick != null && targetAccount != null) {
                                        onAccountClick(targetAccount)
                                    } else {
                                        handleSelectSimilar(
                                            accountItemName,
                                            if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // 5. Grid Row 3: Labels & Notes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val hasLabels = tx.referenceNo.isNotBlank()
                        DetailGridCard(
                            title = if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels",
                            value = labelsDisplay,
                            isMuted = !hasLabels,
                            modifier = Modifier.weight(1f),
                            onClick = if (hasLabels) {
                                {
                                    handleSelectSimilar(
                                        tx.referenceNo,
                                        if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels"
                                    )
                                }
                            } else null
                        )

                        val hasNotes = cleanNote.isNotBlank()
                        DetailGridCard(
                            title = if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes",
                            value = notesDisplay,
                            isMuted = !hasNotes,
                            modifier = Modifier.weight(1f),
                            onClick = if (hasNotes) {
                                {
                                    handleSelectSimilar(
                                        cleanNote,
                                        if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes"
                                    )
                                }
                            } else null
                        )
                    }

                    // Linked Transfer Fee (if present)
                    if (linkedFeeItem != null) {
                        val feeAcc = linkedFeeItem.creditAccount?.localizedName(languageMode) ?: ""
                        val feeDisplay = buildString {
                            append(LanguageHelper.formatCurrency(linkedFeeItem.transaction.amount, languageMode))
                            if (feeAcc.isNotBlank()) append(" ($feeAcc)")
                        }

                        DetailGridCard(
                            title = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার ফি" else "Transfer Fee",
                            value = feeDisplay,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                handleSelectSimilar(
                                    feeDisplay,
                                    if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার ফি" else "Transfer Fee"
                                )
                            }
                        )
                    }

                    // Split Transaction Breakdown (if split transaction)
                    if (isSplitTx && splitSiblings.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CallSplit,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "বিভাজিত লেনদেন (${splitSiblings.size}টি আইটেম)" else "Split Transaction (${splitSiblings.size} items)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = LanguageHelper.formatCurrency(totalSplitAmount, languageMode),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SolidPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))

                                splitSiblings.forEach { sibling ->
                                    val isCurrent = sibling.transaction.id == tx.id
                                    val siblingCat = sibling.category?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "সাধারণ" else "General")
                                    val siblingNote = TransactionLinkHelper.getCleanNote(sibling.transaction.note)
                                    val siblingPct = if (totalSplitAmount > 0) (sibling.transaction.amount / totalSplitAmount * 100).toInt() else 0

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrent) SolidPrimary.copy(alpha = 0.10f) else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (sibling.category != null) IconHelper.parseColorHex(sibling.category.colorHex).copy(alpha = 0.15f)
                                                            else MaterialTheme.colorScheme.surfaceVariant
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    IconHelper.AppIcon(
                                                        iconName = sibling.category?.iconName ?: "Category",
                                                        modifier = Modifier.size(14.dp),
                                                        tint = if (sibling.category != null) IconHelper.parseColorHex(sibling.category.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    val accName = sibling.creditAccount?.localizedName(languageMode)
                                                        ?: sibling.debitAccount?.localizedName(languageMode)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = siblingCat,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (!accName.isNullOrBlank()) {
                                                            Text(
                                                                text = " • $accName",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    if (siblingNote.isNotBlank()) {
                                                        Text(
                                                            text = siblingNote,
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = LanguageHelper.formatCurrency(sibling.transaction.amount, languageMode),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCurrent) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (siblingPct > 0) {
                                                    Text(
                                                        text = "$siblingPct%",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Attachment indicator (if present)
                    if (tx.attachmentUri.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ফাইল সংযুক্ত রয়েছে" else "Attachment attached",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Status badge (if CLEARED / RECONCILED / VOID / PENDING)
                    if (tx.status != TransactionStatus.NONE) {
                        val statusTitle = if (languageMode == LanguageMode.BANGLA) tx.status.titleBn else tx.status.titleEn
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = amtColor.copy(alpha = 0.1f),
                            border = BorderStroke(0.5.dp, amtColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status"}: $statusTitle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = amtColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action: Green Edit Button (matching Screenshot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onEdit(tx) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00875A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("edit_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সম্পাদনা" else "Edit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailGridCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
