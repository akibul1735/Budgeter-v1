package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader

private data class FaqItem(
    val questionEn: String,
    val questionBn: String,
    val answerEn: String,
    val answerBn: String
)

@Composable
fun FaqSettingsPage(
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    var expandedIndices by remember { mutableStateOf(setOf(0)) }

    val faqList = remember {
        listOf(
            FaqItem(
                questionEn = "How does double-entry accounting work in Budgeter?",
                questionBn = "বাজেটারে ডাবল-এন্ট্রি বুককিপিং কীভাবে কাজ করে?",
                answerEn = "Budgeter adheres to fundamental double-entry principles. When you record an expense, money is deducted from your Asset Account (e.g. Bank/Cash) and debited to the Expense Category. For transfers, one asset is credited and another is debited. This ensures your net worth and balance sheet never have discrepancies.",
                answerBn = "বাজেটার আন্তর্জাতিক ডাবল-এন্ট্রি নীতি অনুসরণ করে। যখন কোনো খরচ যোগ করেন, তখন নির্ধারিত অ্যাকাউন্ট (যেমন ব্যাংক বা ক্যাশ) থেকে টাকা বিয়োগ হয় এবং ক্যাটাগরিতে ডেবিট হয়। ফান্ড ট্রান্সফারে এক অ্যাকাউন্ট থেকে অন্যটিতে সরাসরি ব্যালেন্স সমন্বয় হয়। ফলে সম্পদ ও ব্যালেন্স শিটের হিসেবে কোনো ভুল থাকে না।"
            ),
            FaqItem(
                questionEn = "Where is my financial data stored?",
                questionBn = "আমার আর্থিক হিসাবের ডাটা কোথায় সংরক্ষিত থাকে?",
                answerEn = "All financial records are stored purely offline on your physical Android device using an encrypted local SQLite database. Budgeter does not transmit your personal transactions to any external analytics or ad servers.",
                answerBn = "আপনার সকল আর্থিক হিসাব ও লেনদেনের ডাটা সম্পূর্ণ আপনার ফোনের মেমোরিতে (SQLite ডাটাবেস) সুরক্ষিত থাকে। কোনো ট্র্যাকিং সার্ভারে আপনার ডাটা পাঠানো হয় না।"
            ),
            FaqItem(
                questionEn = "How does Google Drive Backup & Sync work?",
                questionBn = "গুগল ড্রাইভ ব্যাকআপ ও সিঙ্ক কীভাবে কাজ করে?",
                answerEn = "You can connect your Google account in Settings > Data Management. Budgeter securely saves snapshot archives of your database to your private Google Drive AppData folder. You can restore your data at any time when migrating to a new phone.",
                answerBn = "সেটিংস > ডাটা ব্যবস্থাপনা থেকে গুগল অ্যাকাউন্ট সংযুক্ত করে গুগল ড্রাইভে ব্যাকআপ রাখতে পারবেন। নতুন ফোনে অ্যাপ ইনস্টল করলে ড্রাইভে থাকা ব্যাকআপ থেকে সব হিসাব এক ক্লিকে রিস্টোর করা যায়।"
            ),
            FaqItem(
                questionEn = "How does Budgeting and Category limits work?",
                questionBn = "বাজেট ও ক্যাটাগরি লিমিট কীভাবে কাজ করে?",
                answerEn = "You can set monthly spending limits for each category or budget envelope in the Budget tab. As you record expenses throughout the month, progress bars track remaining funds and show warnings when approaching limits.",
                answerBn = "বাজেট ট্যাব থেকে প্রতি মাসের খরচের সর্বোচ্চ সীমা নির্ধারণ করতে পারেন। মাসজুড়ে খরচের সাথে সাথে প্রগ্রেস বার রিয়েল-টাইমে অবশিষ্ট টাকা প্রদর্শন করে।"
            ),
            FaqItem(
                questionEn = "What happens if I forget my security PIN?",
                questionBn = "যদি সিকিউরিটি পিন ভুলে যাই তবে কী করব?",
                answerEn = "If you forget your PIN, tap 'Forgot PIN' on the lock screen. If you have set up a Security Recovery Question in Security Settings, answering it correctly will allow you to reset your PIN instantly.",
                answerBn = "পিন ভুলে গেলে লক স্ক্রিনে 'Forgot PIN' চাপুন। আপনি যদি সিকিউরিটি সেটিংসে রিকভারি প্রশ্ন সেট করে থাকেন, তবে সঠিক উত্তর দিয়ে পিন রিসেট করতে পারবেন।"
            ),
            FaqItem(
                questionEn = "Can I export my data to Excel or CSV?",
                questionBn = "আমি কি ডাটা এক্সেল বা সিএসভিতে এক্সপোর্ট করতে পারি?",
                answerEn = "Yes! Navigate to Settings > Data Management > Google Drive & Backup Center to export your entire ledger and accounts into standard CSV spreadsheets.",
                answerBn = "হ্যাঁ! সেটিংস > ডাটা ব্যবস্থাপনা থেকে আপনার সমস্ত লেজার ও অ্যাকাউন্টের ডাটা সিএসভি স্প্রেডশিট ফাইলে এক্সপোর্ট করতে পারেন।"
            )
        )
    }

    val filteredFaq = remember(searchQuery, isBangla) {
        if (searchQuery.isBlank()) {
            faqList
        } else {
            faqList.filter { item ->
                item.questionEn.contains(searchQuery, ignoreCase = true) ||
                        item.questionBn.contains(searchQuery, ignoreCase = true) ||
                        item.answerEn.contains(searchQuery, ignoreCase = true) ||
                        item.answerBn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("faq_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "প্রশ্নোত্তর ও সহায়তা" else "FAQ & Support",
            tabIcon = Icons.Default.HelpOutline,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "প্রশ্ন খুঁজুন..." else "Search questions...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = if (isBangla) "সাধারণ প্রশ্নোত্তর" else "Frequently Asked Questions",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            filteredFaq.forEachIndexed { index, item ->
                val isExpanded = expandedIndices.contains(index)
                val question = if (isBangla) item.questionBn else item.questionEn
                val answer = if (isBangla) item.answerBn else item.answerEn

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            expandedIndices = if (isExpanded) {
                                expandedIndices - index
                            } else {
                                expandedIndices + index
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = question,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = answer,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
