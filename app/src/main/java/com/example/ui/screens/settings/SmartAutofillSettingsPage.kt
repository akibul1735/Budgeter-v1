package com.example.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = LanguageHelper.getString("autofill_desc", languageMode),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    AutofillToggleRow(
                        title = LanguageHelper.getString("autofill_category", languageMode),
                        subtitle = if (isBangla) "ক্যাটাগরি ও সাব-ক্যাটাগরি স্বয়ংক্রিয় নির্বাচন" else "Auto-assign Category & Sub-Category",
                        checked = config.autofillCategory,
                        onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillCategory = it)) }
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    AutofillToggleRow(
                        title = LanguageHelper.getString("autofill_account", languageMode),
                        subtitle = if (isBangla) "অ্যাকাউন্ট বা পেমেন্ট মেথড স্বয়ংক্রিয় নির্বাচন" else "Auto-select Account / Payment Method",
                        checked = config.autofillAccount,
                        onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillAccount = it)) }
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    AutofillToggleRow(
                        title = LanguageHelper.getString("autofill_amount", languageMode),
                        subtitle = if (isBangla) "পূর্ববর্তী একই লেনদেনের পরিমাণ স্বয়ংক্রিয়ভাবে পূরণ" else "Fill same amount from last transaction",
                        checked = config.autofillAmount,
                        onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillAmount = it)) }
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    AutofillToggleRow(
                        title = LanguageHelper.getString("autofill_notes", languageMode),
                        subtitle = if (isBangla) "পূর্ববর্তী নোট বা বিবরণ স্বয়ংক্রিয় পূরণ" else "Fill note from previous entry",
                        checked = config.autofillNotes,
                        onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillNotes = it)) }
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    AutofillToggleRow(
                        title = LanguageHelper.getString("autofill_labels", languageMode),
                        subtitle = if (isBangla) "পূর্ববর্তী লেবেল বা ট্যাগ স্বয়ংক্রিয় পূরণ" else "Fill label/tag from previous entry",
                        checked = config.autofillLabel,
                        onCheckedChange = { viewModel.updateAutofillConfig(config.copy(autofillLabel = it)) }
                    )
                }
            }

            // Quick Reset Defaults
            OutlinedButton(
                onClick = {
                    viewModel.updateAutofillConfig(AutofillConfig())
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBangla) "ডিফল্ট সেটিংসে ফিরিয়ে আনুন" else "Reset to Defaults",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AutofillToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
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
