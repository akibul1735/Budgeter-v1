package com.example.util

import android.content.Context
import com.example.data.local.ItemImageCacheDao
import com.example.data.model.Category
import com.example.data.model.ImageCacheSource
import com.example.data.model.ItemImageCache
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.remote.OnlineIconSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object ItemCacheHelper {

    /**
     * Normalizes an item or payee name for consistent cache lookups and matching.
     * e.g., "  LED Bulb!  " -> "led bulb"
     */
    fun normalizeItemName(name: String?): String {
        if (name.isNullOrBlank()) return ""
        val cleaned = name.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ") // keep letters, digits, and spaces
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned
    }

    /**
     * Looks up an item in the memory cache map using exact normalized match.
     * Prevents distinct items (e.g. "egg" vs "duck egg", "oil" vs "mustard oil") from colliding.
     */
    fun findCachedIcon(
        rawName: String?,
        cacheMap: Map<String, ItemImageCache>
    ): ItemImageCache? {
        if (rawName.isNullOrBlank() || cacheMap.isEmpty()) return null

        val normalized = normalizeItemName(rawName)
        if (normalized.isEmpty()) return null

        // 1. Direct exact normalized key match
        cacheMap[normalized]?.let { return it }

        // 2. Direct exact raw lowercase match
        val rawLower = rawName.trim().lowercase(Locale.ROOT)
        cacheMap[rawLower]?.let { return it }

        // 3. Match across cached items by exact normalizedItemName or exact trimmed itemName
        for ((_, entry) in cacheMap) {
            if (entry.normalizedItemName.equals(normalized, ignoreCase = true) ||
                entry.itemName.trim().equals(rawName.trim(), ignoreCase = true)
            ) {
                return entry
            }
        }

        // 4. Singular / Plural normalization (e.g. "eggs" -> "egg")
        val singular = if (normalized.endsWith("s") && normalized.length > 3) normalized.removeSuffix("s") else null
        if (singular != null) {
            cacheMap[singular]?.let { return it }
        }
        val plural = normalized + "s"
        cacheMap[plural]?.let { return it }

        return null
    }

    /**
     * Resolves the icon for a transaction following the strict priority logic:
     * 1. Custom cached item image/icon (if found in cache)
     * 2. Category icon (subCategory icon if defined and valid, otherwise Category icon)
     * 3. Category-group icon (Category icon)
     * 4. Fallback default type icon
     */
    fun resolveTransactionIconName(
        transaction: Transaction,
        category: Category? = null,
        subCategory: Category? = null,
        cacheMap: Map<String, ItemImageCache> = emptyMap()
    ): String {
        // Priority 1: Custom cached item image or user-selected icon
        val cached = findCachedIcon(transaction.payeeOrPayer, cacheMap)
            ?: (if (transaction.payeeOrPayer.isBlank() && transaction.note.isNotBlank()) {
                val clean = com.example.util.TransactionLinkHelper.getCleanNote(transaction.note)
                findCachedIcon(clean, cacheMap) ?: findCachedIcon(transaction.note, cacheMap)
            } else null)

        if (cached != null && cached.iconKey.isNotBlank()) {
            return cached.iconKey
        }

        // Priority 2: Direct match in the vast in-app icon store for payee or note
        val rawItem = transaction.payeeOrPayer.takeIf { it.isNotBlank() }
            ?: (if (transaction.payeeOrPayer.isBlank() && transaction.note.isNotBlank()) {
                com.example.util.TransactionLinkHelper.getCleanNote(transaction.note)
            } else null)

        if (!rawItem.isNullOrBlank()) {
            val matchedInApp = IconHelper.findMatchingInAppIcon(rawItem)
            if (matchedInApp != null && matchedInApp.isNotBlank() && matchedInApp != "Category") {
                return matchedInApp
            }
        }

        // Priority 3: Sub-category icon (if valid and not default generic)
        val subCatIcon = subCategory?.iconName?.takeIf { it.isNotBlank() && it != "Category" }
        if (subCatIcon != null) {
            return subCatIcon
        }

        // Priority 4: Category / Group icon
        val catIcon = category?.iconName?.takeIf { it.isNotBlank() }
        if (catIcon != null) {
            return catIcon
        }

        // Priority 5: Fallback default based on transaction type
        return when (transaction.type) {
            TransactionType.EXPENSE -> "Category"
            TransactionType.INCOME -> "Payments"
            TransactionType.TRANSFER -> "SwapHoriz"
        }
    }

    fun resolveTransactionIconName(
        item: com.example.data.model.TransactionWithDetails,
        cacheMap: Map<String, ItemImageCache> = emptyMap()
    ): String {
        return resolveTransactionIconName(
            transaction = item.transaction,
            category = item.category,
            subCategory = item.subCategory,
            cacheMap = cacheMap
        )
    }

    /**
     * Searches the vast in-app icon store when an item is added.
     * If found, saves the in-app icon in cache.
     * If not found, returns null so the item smoothly uses its Category icon.
     * Online auto fetching is completely disabled as requested.
     */
    suspend fun discoverAndCacheItemIcon(
        context: Context,
        itemName: String,
        dao: ItemImageCacheDao
    ): ItemImageCache? = withContext(Dispatchers.IO) {
        val trimmed = itemName.trim()
        if (trimmed.isBlank()) return@withContext null

        val normalized = normalizeItemName(trimmed)
        if (normalized.isBlank()) return@withContext null

        // Check if already in database
        val existing = dao.getByNormalizedName(normalized)
        if (existing != null) return@withContext existing

        try {
            // Search the vast in-app icon library
            val matchedInApp = IconHelper.findMatchingInAppIcon(trimmed)
            if (matchedInApp != null && matchedInApp.isNotBlank() && matchedInApp != "Category") {
                val entry = ItemImageCache(
                    itemName = trimmed,
                    normalizedItemName = normalized,
                    iconKey = matchedInApp,
                    source = ImageCacheSource.AUTO_SELECTED,
                    sourceTitle = "In-App Library",
                    originalQuery = trimmed,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.insertOrUpdate(entry)
                return@withContext entry
            }

            // Not found in in-app library -> return null so it falls back to Category icon
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
