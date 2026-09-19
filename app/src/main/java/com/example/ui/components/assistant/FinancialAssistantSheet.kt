package com.example.ui.components.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    accountCalcConfig: com.example.util.AccountCalcConfig = com.example.util.AccountCalcConfig(),
    onNavigateAction: ((AssistantAction) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val isBn = languageMode == LanguageMode.BANGLA
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val handleAction: (AssistantAction) -> Unit = { action ->
        onDismiss()
        onNavigateAction?.invoke(action)
    }

    // Initial greeting message
    val initialWelcomeMessage = remember(languageMode) {
        val welcomeText = if (isBn) {
            "👋 **আসসালামু আলাইকুম!** আমি আপনার অফলাইন পার্সোনাল ফাইন্যান্স সহকারী। আপনার কোনো আর্থিক ডাটা ডিভাইসের বাইরে পাঠানো হয় না।\n\nআপনি নির্দিষ্ট অ্যাকাউন্ট (যেমন RM Others, Rocket, Cash), বাদ দেওয়া অ্যাকাউন্ট, বাজেট ও বাজেট মেকার (যেমন Hair Cut বাজেট), চলতি মাসের খরচ, বা মোট সম্পদ সম্পর্কে প্রশ্ন করতে পারেন:"
        } else {
            "👋 **Hello!** I am your 100% private offline Financial Assistant. Your financial records stay strictly secure on your device.\n\nYou can ask about specific accounts (e.g. RM Others, Rocket, Cash), excluded accounts, category budgets & budget maker (e.g. Hair Cut budget), recent activity, or net worth:"
        }

        val initialChips = listOf(
            AssistantChip(
                label = if (isBn) "🛡️ বাদ দেওয়া অ্যাকাউন্ট" else "🛡️ Excluded Accounts",
                actionQuery = "tell me about excluded accounts",
                navigationAction = AssistantAction(
                    label = if (isBn) "বাদ দেওয়া অ্যাকাউন্ট দেখুন" else "View Excluded Accounts",
                    destination = "ACCOUNTS_EXCLUDED"
                )
            ),
            AssistantChip(
                label = if (isBn) "💇 চুল কাটার বাজেট" else "💇 Hair Cut Budget",
                actionQuery = "hair cut budget",
                navigationAction = AssistantAction(
                    label = if (isBn) "বাজেট মেকারে যান" else "Open Budget Maker",
                    destination = "BUDGET_MAKER",
                    searchQuery = "Hair Cut"
                )
            ),
            AssistantChip(
                label = if (isBn) "🎯 বাজেটের অবস্থা" else "🎯 Budget Status",
                actionQuery = "Show my budget status"
            ),
            AssistantChip(
                label = if (isBn) "🏦 RM Others একাউন্ট" else "🏦 RM Others Account",
                actionQuery = "tell me about RM Others"
            ),
            AssistantChip(
                label = if (isBn) "💰 মোট সম্পদ" else "💰 Net Worth",
                actionQuery = "What is my net worth"
            ),
            AssistantChip(
                label = if (isBn) "👥 আরএম দেনা-পাওনা" else "👥 RM Debts & Loans",
                actionQuery = "Tell me about RM Manager"
            ),
            AssistantChip(
                label = if (isBn) "📊 চলতি মাসের খরচ" else "📊 This Month Spending",
                actionQuery = "How much did I spend this month?"
            ),
            AssistantChip(
                label = if (isBn) "💡 সঞ্চয়ের টিপস (৫০/৩০/২০)" else "💡 Savings Tips (50/30/20)",
                actionQuery = "Give me savings tips"
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
            budgets = budgets,
            accountCalcConfig = accountCalcConfig
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) },
        modifier = Modifier
            .fillMaxHeight(0.95f)
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
                    .padding(horizontal = 18.dp, vertical = 6.dp),
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
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
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
                                color = Color(0xFF16A34A).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (isBn) "অফলাইন" else "100% Offline",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isBn) "অন-ডিভাইস নিরাপদ আর্থিক বুদ্ধিমত্তা" else "Private On-Device Financial Intelligence",
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(messages, key = { it.id }) { message ->
                    AssistantMessageItem(
                        message = message,
                        languageMode = languageMode,
                        onChipClicked = { chipQuery ->
                            sendMessage(chipQuery)
                        },
                        onActionClicked = handleAction
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Quick Suggestion Chips Row above Input
            val quickPrompts = remember(languageMode) {
                if (isBn) {
                    listOf(
                        "🛡️ বাদ দেওয়া একাউন্ট" to "tell me about excluded accounts",
                        "💇 চুল কাটার বাজেট" to "hair cut budget",
                        "🎯 বাজেটের অবস্থা" to "Show my budget status",
                        "🏦 RM Others" to "tell me about RM Others",
                        "💰 মোট সম্পদ" to "What is my net worth",
                        "👥 আরএম দেনা পাওনা" to "Tell me about RM Manager",
                        "📊 চলতি মাসের খরচ" to "How much did I spend this month?",
                        "💡 সঞ্চয়ের টিপস" to "Give me savings tips"
                    )
                } else {
                    listOf(
                        "🛡️ Excluded Accounts" to "tell me about excluded accounts",
                        "💇 Hair Cut Budget" to "hair cut budget",
                        "🎯 Budget Status" to "Show my budget status",
                        "🏦 RM Others" to "tell me about RM Others",
                        "💰 Net Worth" to "What is my net worth",
                        "👥 RM Debts" to "Tell me about RM Manager",
                        "📊 Expenses" to "How much did I spend this month?",
                        "💡 Savings Tips" to "Give me savings tips"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { (label, actionQuery) ->
                    SuggestionChip(
                        onClick = { sendMessage(actionQuery) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            enabled = true
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                text = if (isBn) "RM Others, বাজেট বা খরচ নিয়ে লিখুন..." else "Ask about RM Others, budgets, spending...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        maxLines = 3,
                        singleLine = false
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                sendMessage(inputText)
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssistantMessageItem(
    message: AssistantMessage,
    languageMode: LanguageMode,
    onChipClicked: (String) -> Unit,
    onActionClicked: (AssistantAction) -> Unit = {}
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.96f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
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
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    ),
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) else null
                ) {
                    FormattedAssistantText(
                        text = message.text,
                        isUser = isUser,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                // Excluded Accounts Card if present
                if (message.excludedAccountsCard != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExcludedAccountsCard(message.excludedAccountsCard, languageMode, onActionClicked)
                }

                // Category Budget Card if present (e.g. Hair Cut budget)
                if (message.categoryBudgetCard != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryBudgetCard(message.categoryBudgetCard, languageMode, onActionClicked)
                }

                // Account Detail Card if present (e.g. RM Others, Rocket, Cash)
                if (message.accountCard != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AccountDetailCard(message.accountCard, languageMode)
                }

                // Budget Status Card if present
                if (message.budgetCard != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetStatusCard(message.budgetCard, languageMode)
                }

                // Metrics Summary Card if present
                if (message.metricsSummary != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricsSummaryCard(message.metricsSummary, languageMode)
                }

                // Transaction Items List if present
                if (message.transactionList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TransactionSnippetList(message.transactionList, languageMode)
                }

                // Direct Navigation Action Buttons (Tap to go to data page)
                if (message.primaryAction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { onActionClicked(message.primaryAction) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.primaryAction.label,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (message.secondaryActions.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.secondaryActions.forEach { action ->
                            AssistChip(
                                onClick = { onActionClicked(action) },
                                label = {
                                    Text(
                                        text = action.label,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
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
                                onClick = {
                                    if (chip.navigationAction != null) {
                                        onActionClicked(chip.navigationAction)
                                    } else {
                                        onChipClicked(chip.actionQuery)
                                    }
                                },
                                label = {
                                    Text(
                                        text = chip.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (chip.navigationAction != null) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
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
private fun CategoryBudgetCard(
    card: AssistantCategoryBudgetCard,
    languageMode: LanguageMode,
    onActionClicked: (AssistantAction) -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val progress = (card.percentage / 100.0).coerceIn(0.0, 1.0).toFloat()
    val progressColor = when {
        card.isOverBudget -> Color(0xFFDC2626)
        card.percentage >= 85.0 -> Color(0xFFEAB308)
        else -> Color(0xFF16A34A)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = card.categoryName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "ক্যাটাগরি বাজেট স্থিতি" else "Category Budget Insight",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (card.isOverBudget) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (card.isOverBudget) {
                            if (isBn) "বাজেট অতিক্রান্ত" else "Over Budget"
                        } else {
                            if (isBn) "বাজেটের মধ্যে" else "On Track"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (card.isOverBudget) Color(0xFFDC2626) else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Limit vs Spent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBn) "নির্ধারিত বাজেট" else "Budget Limit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(card.budgetLimit, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBn) "মোট খরচ" else "Total Spent",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(card.spentAmount, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Remaining & % Used
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isBn) "ব্যয়" else "Used"}: ${String.format(java.util.Locale.US, "%.1f", card.percentage)}%",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (isBn) "অবশিষ্ট" else "Remaining"}: ${LanguageHelper.formatCurrency(card.remainingAmount, languageMode)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (card.remainingAmount < 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                )
            }

            // Quick deep-link button to open Budget Maker directly
            FilledTonalButton(
                onClick = {
                    onActionClicked(
                        AssistantAction(
                            label = if (isBn) "${card.categoryName} বাজেট মেকার খুলুন" else "Open Budget Maker (${card.categoryName})",
                            destination = "BUDGET_MAKER",
                            searchQuery = card.categoryName
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "বাজেট মেকারে দেখুন ও পরিবর্তন করুন" else "View & Edit in Budget Maker",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ExcludedAccountsCard(
    card: AssistantExcludedAccountsCard,
    languageMode: LanguageMode,
    onActionClicked: (AssistantAction) -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isBn) "বাদ দেওয়া অ্যাকাউন্টসমূহ" else "Excluded Accounts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "${card.totalExcludedCount}টি অ্যাকাউন্ট বাদ দেওয়া হয়েছে" else "${card.totalExcludedCount} accounts excluded from net worth",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (isBn) "মোট ${card.totalExcludedCount}" else "${card.totalExcludedCount} Total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Total Excluded Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "বাদ দেওয়া অ্যাকাউন্টের মোট ব্যালেন্স" else "Total Excluded Balance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = LanguageHelper.formatCurrency(card.totalExcludedBalance, languageMode),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Excluded account items preview
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                card.items.take(4).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.accountName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(item.balance, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (card.items.size > 4) {
                    Text(
                        text = if (isBn) "+ আরও ${card.items.size - 4}টি অ্যাকাউন্ট..." else "+ ${card.items.size - 4} more accounts...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Quick button to open Excluded Accounts screen
            FilledTonalButton(
                onClick = {
                    onActionClicked(
                        AssistantAction(
                            label = if (isBn) "বাদ দেওয়া অ্যাকাউন্টের পাতায় যান" else "Open Excluded Accounts Screen",
                            destination = "ACCOUNTS_EXCLUDED"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "অ্যাকাউন্ট পাতায় বাদ দেওয়া তালিকা দেখুন" else "View Excluded Accounts List",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Intelligent Markdown formatter that parses **bold** text, bullet points,
 * and clean headings without rendering raw asterisks on screen.
 */
@Composable
private fun FormattedAssistantText(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val bulletColor = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary

    val annotated = remember(text, isUser) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { lineIdx, line ->
                val trimmed = line.trim()
                val isBullet = trimmed.startsWith("•") || trimmed.startsWith("- ")
                val cleanLine = if (isBullet) trimmed.removePrefix("- ").removePrefix("•").trim() else trimmed

                if (isBullet) {
                    withStyle(SpanStyle(color = bulletColor, fontWeight = FontWeight.Bold)) {
                        append("  • ")
                    }
                }

                // Parse **bold** inside the line
                var i = 0
                while (i < cleanLine.length) {
                    if (i + 1 < cleanLine.length && cleanLine[i] == '*' && cleanLine[i + 1] == '*') {
                        val closeIdx = cleanLine.indexOf("**", i + 2)
                        if (closeIdx != -1) {
                            val boldSegment = cleanLine.substring(i + 2, closeIdx)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                                append(boldSegment)
                            }
                            i = closeIdx + 2
                            continue
                        }
                    }
                    append(cleanLine[i])
                    i++
                }

                if (lineIdx < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 13.5.sp,
        lineHeight = 19.5.sp,
        color = textColor,
        modifier = modifier
    )
}

/**
 * Dedicated Account Detail Card rendering balance, type, and activity snapshot.
 */
@Composable
private fun AccountDetailCard(
    accountCard: AssistantAccountCard,
    languageMode: LanguageMode
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val isLiability = accountCard.accountType.equals("LIABILITY", ignoreCase = true)
    val balanceColor = if (isLiability) {
        if (accountCard.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
    } else {
        if (accountCard.currentBalance >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Account Name & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = accountCard.accountName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "হিসাব স্থিতি" else "Account Snapshot",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = accountCard.accountType,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Current Balance Big Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBn) "বর্তমান ব্যালেন্স" else "CURRENT BALANCE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = LanguageHelper.formatCurrency(accountCard.currentBalance, languageMode),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }

            // Inflow, Outflow, Count Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBn) "মোট জমা" else "Total Inflow",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(accountCard.totalIn, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBn) "মোট খরচ" else "Total Outflow",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(accountCard.totalOut, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBn) "মোট লেনদেন" else "Total Txs",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${accountCard.transactionCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (accountCard.lastActiveDate != null) {
                Text(
                    text = if (isBn) "সর্বশেষ লেনদেন: ${accountCard.lastActiveDate}" else "Last Activity: ${accountCard.lastActiveDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Dedicated Budget Status Card with visual progress bars and category health checks.
 */
@Composable
private fun BudgetStatusCard(
    budgetCard: AssistantBudgetCard,
    languageMode: LanguageMode
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val totalPct = if (budgetCard.totalAllocated > 0) (budgetCard.totalSpent / budgetCard.totalAllocated * 100).toInt() else 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = if (isBn) "বাজেট অগ্রগতি সামারি" else "Budget Progress Overview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$totalPct%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalPct > 100) Color(0xFFDC2626) else if (totalPct >= 85) Color(0xFFEA580C) else Color(0xFF16A34A)
                )
            }

            // Total Progress Bar
            LinearProgressIndicator(
                progress = { (budgetCard.totalSpent / (budgetCard.totalAllocated.coerceAtLeast(1.0))).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (totalPct > 100) Color(0xFFDC2626) else if (totalPct >= 85) Color(0xFFEA580C) else Color(0xFF16A34A),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Allocated vs Spent Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (isBn) "খরচ:" else "Spent:"} ${LanguageHelper.formatCurrency(budgetCard.totalSpent, languageMode)}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${if (isBn) "মোট বাজেট:" else "Total Limit:"} ${LanguageHelper.formatCurrency(budgetCard.totalAllocated, languageMode)}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Top budget items breakdown
            if (budgetCard.items.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    budgetCard.items.take(5).forEach { item ->
                        val itemPct = item.percentage.toInt()
                        val barColor = if (itemPct > 100) Color(0xFFDC2626) else if (itemPct >= 85) Color(0xFFEA580C) else Color(0xFF16A34A)

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.categoryName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${LanguageHelper.formatCurrency(item.spent, languageMode)} / ${LanguageHelper.formatCurrency(item.limit, languageMode)} ($itemPct%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = barColor
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (item.spent / item.limit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        color = Color(0xFF16A34A)
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
                        color = Color(0xFFDC2626)
                    )
                }
            }

            if (metrics.netAmount != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিট স্থিতি" else "Net Flow",
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            transactions.take(6).forEach { td ->
                val tx = td.transaction
                val dateStr = DateUtils.formatDate(tx.dateEpochMs, languageMode)
                val catName = td.category?.localizedName(languageMode)
                    ?: td.subCategory?.localizedName(languageMode)
                    ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ" else "General"
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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

            if (transactions.size > 6) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "+ আরও ${transactions.size - 6}টি লেনদেন রয়েছে" else "+ and ${transactions.size - 6} more transactions",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp)
                )
            }
        }
    }
}
