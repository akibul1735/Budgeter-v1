package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. App Emblem Hero Preview Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official App Launcher Icon Emblem
                    Surface(
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_512),
                            contentDescription = "Budgeter App Icon",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Budgeter",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

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

            // 2. Core Architectural Pillars
            Text(
                text = if (isBangla) "অ্যাপ্লিকেশনের মূল বৈশিষ্ট্য ও স্তম্ভ" else "Core Engineering Pillars",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutFeatureCard(
                    icon = Icons.Default.AccountBalance,
                    title = if (isBangla) "ডাবল-এন্ট্রি বুককিপিং" else "Double-Entry Accounting",
                    desc = if (isBangla)
                        "আন্তর্জাতিক মানদণ্ড অনুযায়ী প্রতিটি লেনদেন স্বয়ংক্রিয়ভাবে ক্রেডিট ও ডেবিট সমতা রক্ষা করে।"
                    else
                        "Strict debit/credit ledger maintaining 100% mathematical integrity across all balance sheets."
                )

                AboutFeatureCard(
                    icon = Icons.Default.Shield,
                    title = if (isBangla) "১০০% অফলাইন ও ডেটা নিরাপত্তা" else "100% Offline & Private",
                    desc = if (isBangla)
                        "কোনো ট্র্যাকিং বা বাণিজ্যিক বিজ্ঞাপনী সার্ভারে আপনার গোপনীয় আর্থিক ডাটা প্রেরিত হয় না।"
                    else
                        "Zero tracking, zero analytics telemetry. All data remains strictly on your device."
                )

                AboutFeatureCard(
                    icon = Icons.Default.CloudDone,
                    title = if (isBangla) "গুগল ড্রাইভ এনক্রিপ্টেড ব্যাকআপ" else "Google Drive Cloud Snapshots",
                    desc = if (isBangla)
                        "এক ক্লিকে আপনার ব্যক্তিগত ড্রাইভে ডাটাবেসের আর্কাইভ ব্যাকআপ রাখুন এবং যেকোনো সময় রিস্টোর করুন।"
                    else
                        "Seamless encrypted database sync and snapshot backups directly to your personal Google Drive."
                )

                AboutFeatureCard(
                    icon = Icons.Default.Calculate,
                    title = if (isBangla) "স্মার্ট বাজেট ও ফাইন্যান্সিয়াল রিপোর্ট" else "Smart Budgeting & Analytics",
                    desc = if (isBangla)
                        "উন্নত গ্রাফিক্যাল অ্যানালিটিক্স, ক্যাটাগরি লিমিট ট্র্যাকিং এবং ক্যাশ ফ্লো পূর্বাভাস।"
                    else
                        "Advanced multi-period visual charts, expense envelope budgets, and real-time cashflow monitors."
                )
            }

            // 3. Technical Specifications Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "প্রযুক্তিগত বিবরণ (Technical Specifications)" else "Technical Specifications",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    TechSpecRow(
                        label = if (isBangla) "আর্কিটেকচার" else "Architecture",
                        value = "Clean Architecture + MVVM"
                    )
                    TechSpecRow(
                        label = if (isBangla) "ইউআই ফ্রেমওয়ার্ক" else "UI Framework",
                        value = "Jetpack Compose (M3 Dynamic)"
                    )
                    TechSpecRow(
                        label = if (isBangla) "ডাটাবেস ইঞ্জিন" else "Database Engine",
                        value = "Android Room SQLite (Encrypted)"
                    )
                    TechSpecRow(
                        label = if (isBangla) "রিলিজ কনফিগারেশন" else "Release Name",
                        value = "Budgeter-release.apk"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutFeatureCard(
    icon: ImageVector,
    title: String,
    desc: String
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
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
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
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TechSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
