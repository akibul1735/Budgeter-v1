package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CustomAppIcon(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val badge: String? = null,
    val activityAlias: String = ".MainActivity",
    val primaryColor: Long = 0xFFF59E0B,
    val secondaryColor: Long = 0xFFD97706,
    val takaSymbolColor: Long = 0xFF1F2937,
    val isDark: Boolean = false
)

object AvailableAppIcons {
    val CLASSIC_GOLD = CustomAppIcon(
        id = "classic_gold",
        titleEn = "Classic 24K Gold ৳",
        titleBn = "ক্লাসিক ২৪কে গোল্ড ৳",
        subtitleEn = "Rich golden coin with deep bronze Taka emblem",
        subtitleBn = "উজ্জ্বল সোনালী ধাতব মুদ্রা ও ব্রোঞ্জ টাকা প্রতীক",
        badge = "DEFAULT",
        activityAlias = ".MainActivity",
        primaryColor = 0xFFF59E0B,
        secondaryColor = 0xFFB45309,
        takaSymbolColor = 0xFF451A03,
        isDark = false
    )

    val EMERALD_MINT = CustomAppIcon(
        id = "emerald_mint",
        titleEn = "Emerald Mint ৳",
        titleBn = "পান্না সবুজ মিন্ট ৳",
        subtitleEn = "Mint-green coin with sharp black Bangladeshi Taka emblem",
        subtitleBn = "মিন্ট গ্রিন মুদ্রা ও স্পষ্ট কালো টাকা প্রতীক",
        badge = "POPULAR",
        activityAlias = ".MainActivityEmerald",
        primaryColor = 0xFF10B981,
        secondaryColor = 0xFF047857,
        takaSymbolColor = 0xFF064E3B,
        isDark = false
    )

    val VINTAGE_GOLD = CustomAppIcon(
        id = "vintage_gold",
        titleEn = "Vintage Textured Gold ৳",
        titleBn = "ভিন্টেজ গোল্ড ৳",
        subtitleEn = "Warm aged yellow coin with etched texture & charcoal Taka",
        subtitleBn = "ঐতিহ্যবাহী প্রাচীন সোনালী মুদ্রা ও চারকোল টাকা প্রতীক",
        badge = "VINTAGE",
        activityAlias = ".MainActivityVintage",
        primaryColor = 0xFFEAB308,
        secondaryColor = 0xFFCA8A04,
        takaSymbolColor = 0xFF1C1917,
        isDark = false
    )

    val ROYAL_SAPPHIRE = CustomAppIcon(
        id = "royal_sapphire",
        titleEn = "Royal Sapphire ৳",
        titleBn = "রাজকীয় নীলমণি ৳",
        subtitleEn = "Deep cobalt blue coin with bright silver Taka emblem",
        subtitleBn = "গাঢ় কোবাল্ট ব্লু মুদ্রা ও রূপালী টাকা প্রতীক",
        badge = "ROYAL",
        activityAlias = ".MainActivitySapphire",
        primaryColor = 0xFF3B82F6,
        secondaryColor = 0xFF1D4ED8,
        takaSymbolColor = 0xFFFFFFFF,
        isDark = true
    )

    val MIDNIGHT_OBSIDIAN = CustomAppIcon(
        id = "midnight_obsidian",
        titleEn = "Midnight Obsidian ৳",
        titleBn = "মিডনাইট অবসিডিয়ান ৳",
        subtitleEn = "Stealth graphite black coin with crisp white Taka emblem",
        subtitleBn = "গ্রাফাইট ব্ল্যাক মুদ্রা ও সাদা টাকা প্রতীক",
        badge = "STEALTH",
        activityAlias = ".MainActivityObsidian",
        primaryColor = 0xFF1E293B,
        secondaryColor = 0xFF0F172A,
        takaSymbolColor = 0xFFF8FAFC,
        isDark = true
    )

    val ROSE_GOLD = CustomAppIcon(
        id = "rose_gold",
        titleEn = "Rose Gold Luxury ৳",
        titleBn = "রোজ গোল্ড লাক্সারি ৳",
        subtitleEn = "Blush copper-gold coin with rich rosewood Taka emblem",
        subtitleBn = "কপার-গোল্ড মুদ্রা ও দৃষ্টিনন্দন টাকা প্রতীক",
        badge = "ELEGANCE",
        activityAlias = ".MainActivityRoseGold",
        primaryColor = 0xFFFB7185,
        secondaryColor = 0xFFBE123C,
        takaSymbolColor = 0xFF4C0519,
        isDark = false
    )

    val CYBER_AMETHYST = CustomAppIcon(
        id = "cyber_amethyst",
        titleEn = "Cyber Amethyst ৳",
        titleBn = "সাইবার অ্যামেথিস্ট ৳",
        subtitleEn = "Electric violet coin with neon lavender Taka emblem",
        subtitleBn = "ভায়োলেট পার্পল মুদ্রা ও নিয়ন টাকা প্রতীক",
        badge = "NEON",
        activityAlias = ".MainActivityAmethyst",
        primaryColor = 0xFF8B5CF6,
        secondaryColor = 0xFF6D28D9,
        takaSymbolColor = 0xFFEDE9FE,
        isDark = true
    )

    val IMPERIAL_RUBY = CustomAppIcon(
        id = "imperial_ruby",
        titleEn = "Imperial Ruby ৳",
        titleBn = "ইম্পেরিয়াল রুবি ৳",
        subtitleEn = "Deep crimson red coin with luminous gold Taka emblem",
        subtitleBn = "রক্তিম লাল মুদ্রা ও সোনালী টাকা প্রতীক",
        badge = "ROYAL",
        activityAlias = ".MainActivityRuby",
        primaryColor = 0xFFEF4444,
        secondaryColor = 0xFFB91C1C,
        takaSymbolColor = 0xFFFEF08A,
        isDark = true
    )

    val allIcons = listOf(
        CLASSIC_GOLD,
        EMERALD_MINT,
        VINTAGE_GOLD,
        ROYAL_SAPPHIRE,
        MIDNIGHT_OBSIDIAN,
        ROSE_GOLD,
        CYBER_AMETHYST,
        IMPERIAL_RUBY
    )

    fun getById(id: String): CustomAppIcon {
        return allIcons.firstOrNull { it.id == id } ?: CLASSIC_GOLD
    }
}

class AppIconPreferences private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentIcon = MutableStateFlow(loadCurrentIcon())
    val currentIcon: StateFlow<CustomAppIcon> = _currentIcon.asStateFlow()

    private fun loadCurrentIcon(): CustomAppIcon {
        val iconId = prefs.getString(KEY_ACTIVE_ICON_ID, AvailableAppIcons.CLASSIC_GOLD.id)
            ?: AvailableAppIcons.CLASSIC_GOLD.id
        return AvailableAppIcons.getById(iconId)
    }

    fun setIcon(icon: CustomAppIcon) {
        prefs.edit().putString(KEY_ACTIVE_ICON_ID, icon.id).apply()
        _currentIcon.value = icon
        updateLauncherAlias(context, icon)
    }

    private fun updateLauncherAlias(context: Context, icon: CustomAppIcon) {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            // Enable selected alias and disable others safely
            AvailableAppIcons.allIcons.forEach { target ->
                val compName = ComponentName(
                    packageName,
                    if (target.activityAlias.startsWith(".")) "$packageName${target.activityAlias}"
                    else target.activityAlias
                )
                val newState = if (target.id == icon.id) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                try {
                    packageManager.setComponentEnabledSetting(
                        compName,
                        newState,
                        PackageManager.DONT_KILL_APP
                    )
                } catch (_: Exception) {
                    // Ignore alias missing on device if running in unit tests / Robolectric
                }
            }
        } catch (_: Exception) {
            // Ignore failure in testing environments
        }
    }

    companion object {
        private const val PREFS_NAME = "budgeter_app_icon_prefs"
        private const val KEY_ACTIVE_ICON_ID = "active_custom_app_icon_id"

        @Volatile
        private var INSTANCE: AppIconPreferences? = null

        fun getInstance(context: Context): AppIconPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppIconPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
