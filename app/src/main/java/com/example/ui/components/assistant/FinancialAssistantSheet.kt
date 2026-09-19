package com.example.ui.components.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAssistantSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    languageMode: LanguageMode,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    allCategories: List<Category>,
    transactions: List<TransactionWithDetails>,
    overview: FinancialOverview,
    budgets: List<MonthlyBudget>,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val isBn = languageMode == LanguageMode.BANGLA
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initial greeting message with quick prompt suggestions
    val initialWelcomeMessage = remember(languageMode) {
        val welcomeText = if (isBn) {
            "👋 আসসালামু আলাইকুম! আমি আপনার অফলাইন পার্সোনাল ফাইন্যান্স সহকারী। আপনার কোনো আর্থিক তথ্য ইন্টারনেটে পাঠানো হয় না।\n\nআপনি নির্দিষ্ট কোনো অ্যাকাউন্ট (যেমন: রকেট, বিকাশ, ক্যাশ), খরচ, বাজেট বা মোট সম্পদ সম্পর্কে জানতে চাইতে পারেন:"
        } else {
            "👋 Hello! I am your 100% offline Financial Assistant. Your financial data stays strictly on your device and is never sent to the cloud.\n\nYou can ask about account transactions, date periods, spending, net worth, or budgets:"
        }

        val initialChips = listOf(
            AssistantChip(
                label = if (isBn) "রকেট বিগত ৭ দিন" else "Rocket in past 7 days",
                actionQuery = "Is there any Rocket account related transactions in past 7 days"
            ),
            AssistantChip(
                label = if (isBn) "অ্যাকাউন্ট লেনদেন (৭ দিন)" else "Account transactions (7 days)",
                actionQuery = "Is there any account related transactions in past 7 days"
            ),
            AssistantChip(
                label = if (isBn) "মোট সম্পদ ও ব্যালেন্স" else "Net worth & Balances",
                actionQuery = "What is my current net worth?"
            ),
            AssistantChip(
                label = if (isBn) "চলতি মাসের খরচ" else "This month's expenses",
                actionQuery = "How much did I spend this month?"
            ),
            AssistantChip(
                label = if (isBn) "শীর্ষ খরচসমূহ" else "Top spending categories",
                actionQuery = "Show top spending categories"
            ),
            AssistantChip(
                label = if (isBn) "বাজেটের অবস্থা" else "Budget status",
                actionQuery = "Show my budget status"
            )
        )

        AssistantMessage(
            text = welcomeText,
            isUser = false,
            interactiveChips = initialChips
        )
    }

    val messages = remember { mutableStateListOf(initialWelcomeMessage) }
    var inputText by remember { mutableStateOf("") }

    fun sendMessage(queryText: String) {
        if (queryText.isBlank()) return
        val userMsg = AssistantMessage(text = queryText.trim(), isUser = true)
        messages.add(userMsg)
        inputText = ""

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }

        // Process query via offline engine
        val response = FinancialAssistantEngine.processQuery(
            query = queryText,
            languageMode = languageMode,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            allCategories = allCategories,
            transactions = transactions,
            overview = overview,
            budgets = budgets
        )

        messages.add(response)

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("financial_assistant_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) "বাজেটার সহকারী" else "Budgeter Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Safe & Offline Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (isBn) "অফলাইন" else "100% Offline",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isBn) "নিরাপদ ও স্থানীয় এআই সহকারী" else "Private On-Device Financial Intelligence",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.size > 1) {
                        IconButton(
                            onClick = {
                                messages.clear()
                                messages.add(initialWelcomeMessage)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(messages, key = { it.id }) { message ->
                    AssistantMessageItem(
                        message = message,
                        languageMode = languageMode,
                        onChipClicked = { chipQuery ->
                            sendMessage(chipQuery)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isBn) "লেনদেন বা হিসাব সম্পর্কে জিজ্ঞাসা করুন..." else "Ask about your transactions, budgets...",
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assistant_input_text_field")
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            sendMessage(inputText)
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("assistant_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssistantMessageItem(
    message: AssistantMessage,
    languageMode: LanguageMode,
    onChipClicked: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)) else null
                ) {
                    Text(
                        text = message.text,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }

                // Metrics Summary Card if present
                if (message.metricsSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricsSummaryCard(message.metricsSummary, languageMode)
                }

                // Transaction Items List if present
                if (message.transactionList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TransactionSnippetList(message.transactionList, languageMode)
                }

                // Interactive Quick Clarification / Action Chips
                if (message.interactiveChips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.interactiveChips.forEach { chip ->
                            AssistChip(
                                onClick = { onChipClicked(chip.actionQuery) },
                                label = {
                                    Text(
                                        text = chip.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    enabled = true
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("assistant_chip_${chip.label}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsSummaryCard(
    metrics: AssistantMetrics,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (metrics.totalIn != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "আয় / জমা" else "Total In",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(metrics.totalIn, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A) // Green
                    )
                }
            }

            if (metrics.totalOut != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ব্যয় / খরচ" else "Total Out",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(metrics.totalOut, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626) // Red
                    )
                }
            }

            if (metrics.netAmount != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিট প্রবাহ" else "Net Flow",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(metrics.netAmount, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.netAmount >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionSnippetList(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            transactions.take(8).forEach { td ->
                val tx = td.transaction
                val dateStr = DateUtils.formatDate(tx.dateEpochMs, languageMode)
                val catName = td.category?.localizedName(languageMode)
                    ?: td.subCategory?.localizedName(languageMode)
                    ?: if (languageMode == LanguageMode.BANGLA) "অন্যান্য" else "General"
                val title = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer
                else if (tx.note.isNotBlank()) tx.note
                else catName

                val isExpense = tx.type == TransactionType.EXPENSE
                val isIncome = tx.type == TransactionType.INCOME
                val amtColor = if (isExpense) Color(0xFFDC2626) else if (isIncome) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "$dateStr • $catName",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = (if (isExpense) "-" else if (isIncome) "+" else "") +
                                LanguageHelper.formatCurrency(tx.amount, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = amtColor
                    )
                }
            }

            if (transactions.size > 8) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "+ আরও ${transactions.size - 8}টি লেনদেন রয়েছে" else "+ and ${transactions.size - 8} more transactions",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp)
                )
            }
        }
    }
}
