package com.example.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary

@Composable
fun AboutSettingsPage(
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("about_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "বাজেটার সম্পর্কে" else "About Budgeter",
            tabIcon = Icons.Default.Info,
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
            Spacer(modifier = Modifier.height(8.dp))

            // App Emblem Hero Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gold Coin Emblem
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "৳",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4A3800)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Budgeter",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "v3.6 • Enterprise Edition",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBangla)
                            "ডাবল-এন্ট্রি বুককিপিং নীতিতে তৈরি উচ্চমানের অফলাইন পার্সোনাল ফিন্যান্সিয়াল ম্যানেজার।"
                        else
                            "High-performance, offline-first personal financial manager built on strict double-entry bookkeeping architecture.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Key Highlights
            Text(
                text = if (isBangla) "প্রধান বৈশিষ্ট্য ও সুরক্ষাসমূহ" else "Core Architecture & Highlights",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HighlightItem(
                icon = Icons.Default.AccountBalance,
                title = if (isBangla) "ডাবল-এন্ট্রি লেজার নির্ভুলতা" else "Double-Entry Ledger Integrity",
                description = if (isBangla)
                    "প্রতিটি আয়ের বিপরীতে ক্রেডিট এবং খরচের বিপরীতে ডেবিট লেগ সংরক্ষিত হয়, যা শূন্য অমিল নিশ্চিত করে।"
                else
                    "Each entry maintains atomic debit and credit legs across accounts and categories, guaranteeing mathematical balance."
            )

            HighlightItem(
                icon = Icons.Default.Shield,
                title = if (isBangla) "১০০% অফলাইন ও নিরাপদ প্রাইভেসি" else "100% Offline & Private",
                description = if (isBangla)
                    "আপনার আর্থিক ডাটা সম্পূর্ণ আপনার ডিভাইসে সুরক্ষিত থাকে। কোনো বিজ্ঞাপন বা থার্ড-পার্টি ট্র্যাকিং নেই।"
                else
                    "All financial data is stored purely locally on your device in an encrypted SQLite Room database. No ads, no analytics tracking."
            )

            HighlightItem(
                icon = Icons.Default.CloudDone,
                title = if (isBangla) "গুগল ড্রাইভ ব্যাকআপ ও সিঙ্ক" else "Google Drive Sync & Backup",
                description = if (isBangla)
                    "আপনার ব্যক্তিগত গুগল ড্রাইভের সাথে নিরাপদ ক্লাউড ব্যাকআপ ও অটো-সিঙ্ক সুবিধা।"
                else
                    "Automated snapshot backups and seamless restoration directly to your private Google Drive AppData storage."
            )

            HighlightItem(
                icon = Icons.Default.Calculate,
                title = if (isBangla) "ইন্টারেক্টিভ ক্যালকুলেটর" else "Live Interactive Calculator",
                description = if (isBangla)
                    "টাকা লেখার সময় সরাসরি লাইভ গণিত ও শতকরা বা ভ্যাট হিসাব করার সুবিধা।"
                else
                    "Built-in transaction keypad with live math evaluations and fast percentage breakdown shortcuts."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HighlightItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
