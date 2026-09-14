package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AutofillConfig
import com.example.util.LanguageHelper

@Composable
fun SmartAutofillSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val config by viewModel.autofillConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    val activeCount = listOf(
        config.autofillCategory,
        config.autofillAccount,
        config.autofillAmount,
        config.autofillNotes,
        config.autofillLabel
    ).count { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("smart_autofill_settings_page")
    ) {
        AppTabHeader(
            title = LanguageHelper.getString("autofill_settings", languageMode),
            tabIcon = Icons.Default.AutoAwesome,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. Live Hero Status Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "স্মার্ট অটোফিল স্ট্যাটাস" else "Smart Autofill Status",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "$activeCount/৫ টি নিয়ম সক্রিয়" else "$activeCount/5 Rules Active",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "বিগত লেনদেনের ইতিহাস অনুযায়ী তথ্য পূরণ" else "Fills inputs based on past transaction trends",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Section: Autofill Options
            Text(
                text = if (isBangla) "স্বয়ংক্রিয় পূরণ সংক্রান্ত সেটিংস" else "Autofill Fields & Automation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AutofillOptionCard(
                    icon = Icons.Default.Category,
                    title = LanguageHelper.getString("autofill_category", languageMode),
                    subtitle = if (isBangla) "ক্যাটাগরি ও সাব-ক্যাটাগরি স্বয়ংক্রিয় নির্বাচন" else "Auto-assign Category & Sub-Category",
                    checked = config.autofillCategory,
                    onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillCategory = it)) }
                )

                AutofillOptionCard(
                    icon = Icons.Default.AccountBalance,
                    title = LanguageHelper.getString("autofill_account", languageMode),
                    subtitle = if (isBangla) "অ্যাকাউন্ট বা পেমেন্ট মেথড স্বয়ংক্রিয় নির্বাচন" else "Auto-select Account / Payment Method",
                    checked = config.autofillAccount,
                    onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillAccount = it)) }
                )

                AutofillOptionCard(
                    icon = Icons.Default.Payments,
                    title = LanguageHelper.getString("autofill_amount", languageMode),
                    subtitle = if (isBangla) "পূর্ববর্তী একই লেনদেনের পরিমাণ স্বয়ংক্রিয়ভাবে পূরণ" else "Fill same amount from last transaction",
                    checked = config.autofillAmount,
                    onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillAmount = it)) }
                )

                AutofillOptionCard(
                    icon = Icons.Default.Description,
                    title = LanguageHelper.getString("autofill_notes", languageMode),
                    subtitle = if (isBangla) "পূর্ববর্তী নোট বা বিবরণ স্বয়ংক্রিয় পূরণ" else "Fill note from previous entry",
                    checked = config.autofillNotes,
                    onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillNotes = it)) }
                )

                AutofillOptionCard(
                    icon = Icons.Default.Tag,
                    title = LanguageHelper.getString("autofill_labels", languageMode),
                    subtitle = if (isBangla) "পূর্ববর্তী লেবেল বা ট্যাগ স্বয়ংক্রিয় পূরণ" else "Fill label/tag from previous entry",
                    checked = config.autofillLabel,
                    onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillLabel = it)) }
                )
            }

            // Quick Reset Defaults Button
            OutlinedButton(
                onClick = { viewModel.updateAutofillConfig(AutofillConfig()) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "ডিফল্ট সেটিংসে ফিরিয়ে আনুন" else "Reset to Defaults",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AutofillOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 15.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
