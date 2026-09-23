package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class OnlineIconResult(
    val title: String,
    val imageUrl: String,
    val sourceName: String,
    val isColorful: Boolean = false
) {
    val name: String get() = title
    val downloadUrl: String get() = imageUrl
    val previewUrl: String get() = imageUrl
}

data class OnlineImageResult(
    val title: String,
    val imageUrl: String,
    val thumbUrl: String = imageUrl,
    val sourceName: String,
    val width: Int = 0,
    val height: Int = 0
) {
    val name: String get() = title
    val downloadUrl: String get() = imageUrl
    val previewUrl: String get() = thumbUrl
    val source: String get() = sourceName
}

data class SearchKeywordTiers(
    val exactWord: String,
    val splitWords: List<String>,
    val synonyms: List<String>
)

object OnlineIconSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    private const val DDG_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private val KNOWN_COLORFUL_SOURCES = setOf(
        "SVGL", "Brandfetch", "Favicon", "LOGOS", "FLAT-COLOR-ICONS",
        "FLUENT-EMOJI", "FLUENT-EMOJI-FLAT", "TWEMOJI", "OPENMOJI", "CIRCLE-FLAGS",
        "SKILL-ICONS", "VSCODE-ICONS", "NOTO", "STREAMLINE-COLOR", "STREAMLINE-PLUMP-COLOR",
        "STREAMLINE-ULTIMATE-COLOR", "THESVG-COLOR", "ICON-PARK", "MARKETEQ", "TOKEN-BRANDED",
        "DEVICON", "DEVICON-PLAIN", "BI", "EMOJIONE", "NOTO-V1", "VectorLogoZone",
        "Wikipedia", "CoinGecko", "DuckDuckGo Favicon", "SIMPLE-ICONS", "DuckDuckGo", "Bing"
    )

    private val GENERIC_STOP_WORDS = setOf(
        "and", "or", "the", "a", "an", "of", "to", "in", "for", "on", "with", "at", "by", "from",
        "is", "it", "my", "your", "our", "all", "this", "that", "cost", "fee", "expense", "pay",
        "icon", "icons", "logo", "logos", "symbol", "symbols", "vector", "svg", "png", "image",
        "images", "pic", "picture", "pictures", "photo", "photos", "clipart", "graphic", "graphics"
    )

    private val SYNONYM_MAP: Map<String, List<String>> = mapOf(
        // Compound Phrases
        "electricity bill" to listOf("electricity", "bill", "power", "energy", "invoice", "meter", "receipt", "utility"),
        "electric bill" to listOf("electricity", "bill", "power", "energy", "invoice", "meter", "receipt"),
        "current bill" to listOf("electricity", "bill", "power", "energy", "invoice"),
        "water bill" to listOf("water", "bill", "tap", "droplet", "wasa", "invoice", "utility"),
        "gas bill" to listOf("gas", "bill", "flame", "cylinder", "titas", "invoice"),
        "internet bill" to listOf("internet", "wifi", "router", "broadband", "network", "bill"),
        "wifi bill" to listOf("wifi", "internet", "router", "network", "bill"),
        "mobile recharge" to listOf("mobile", "phone", "recharge", "topup", "sim", "telecom"),
        "mobile bill" to listOf("mobile", "phone", "recharge", "telecom", "bill"),
        "house rent" to listOf("rent", "house", "apartment", "building", "home", "key"),
        "home rent" to listOf("rent", "house", "apartment", "building", "home"),
        "grocery shopping" to listOf("grocery", "shopping", "supermarket", "cart", "vegetables", "fruit"),
        "credit card" to listOf("card", "credit-card", "payment", "bank", "visa", "mastercard"),
        "doctor visit" to listOf("doctor", "medical", "hospital", "clinic", "health", "stethoscope"),
        "school fee" to listOf("school", "education", "tuition", "study", "student", "books"),
        "car fuel" to listOf("fuel", "petrol", "car", "gasoline", "gas-station", "pump"),
        "tea oil" to listOf("oil", "tea", "bottle", "drop", "cooking-oil"),
        "cooking oil" to listOf("oil", "bottle", "cooking", "kitchen", "drop", "chef"),
        "mustard oil" to listOf("oil", "bottle", "cooking", "kitchen", "drop"),
        "soyabean oil" to listOf("oil", "bottle", "cooking", "kitchen", "drop"),
        "hair oil" to listOf("oil", "bottle", "beauty", "hair", "salon"),
        "duck egg" to listOf("egg", "duck", "poultry", "breakfast", "food"),
        "hen egg" to listOf("egg", "chicken", "poultry", "breakfast", "food"),

        // Oils & Fuel
        "oil" to listOf("cooking-oil", "fuel", "petrol", "gasoline", "bottle", "drop", "oil-barrel", "diesel"),
        "oils" to listOf("cooking-oil", "fuel", "petrol", "gasoline", "bottle", "drop"),
        "tel" to listOf("oil", "cooking-oil", "fuel", "petrol", "bottle"),
        "fuel" to listOf("petrol", "gasoline", "octane", "diesel", "gas-station", "pump", "oil"),
        "petrol" to listOf("fuel", "gasoline", "octane", "pump", "car", "gas-station", "oil"),
        "gasoline" to listOf("fuel", "petrol", "pump", "oil", "car"),
        "octane" to listOf("fuel", "petrol", "gasoline", "pump", "oil"),
        "diesel" to listOf("fuel", "petrol", "pump", "truck", "oil"),

        // Mobile, Recharge & Telecom
        "recharge" to listOf("mobile", "phone", "topup", "battery", "charge", "payment", "sim", "telecom"),
        "topup" to listOf("recharge", "mobile", "phone", "sim", "telecom", "payment"),
        "mobile" to listOf("phone", "smartphone", "cell", "sim", "recharge", "topup", "call"),
        "phone" to listOf("smartphone", "mobile", "call", "cell", "telephone"),
        "flexiload" to listOf("recharge", "mobile", "phone", "topup"),
        "sim" to listOf("mobile", "phone", "recharge", "telecom", "chip"),

        // Health, Medicine & Medical
        "medicine" to listOf("pill", "capsule", "drug", "pharmacy", "medical", "syrup", "health", "hospital"),
        "medicines" to listOf("pill", "capsule", "pharmacy", "medical", "drug", "health"),
        "meds" to listOf("medicine", "pill", "capsule", "pharmacy", "medical"),
        "pill" to listOf("medicine", "capsule", "pharmacy", "medical", "drug"),
        "capsule" to listOf("medicine", "pill", "pharmacy", "medical"),
        "pharmacy" to listOf("medicine", "pill", "drugstore", "medical", "cross", "prescription"),
        "doctor" to listOf("medical", "hospital", "stethoscope", "health", "consultant", "clinic"),
        "hospital" to listOf("medical", "clinic", "health", "building", "cross", "emergency"),
        "clinic" to listOf("hospital", "medical", "doctor", "health"),
        "health" to listOf("medical", "hospital", "doctor", "heart", "fitness", "wellness"),
        "medical" to listOf("health", "hospital", "doctor", "medicine", "pill", "clinic"),
        "diagnostic" to listOf("medical", "test", "lab", "hospital", "microscope"),

        // Utilities & Bills
        "electricity" to listOf("power", "energy", "electric", "lightning", "bolt", "meter", "volt", "utility", "current"),
        "electric" to listOf("electricity", "power", "energy", "lightning", "bolt", "meter", "plug", "socket"),
        "bill" to listOf("invoice", "receipt", "document", "payment", "utility", "statement", "check"),
        "bills" to listOf("invoice", "receipt", "payment", "utility", "statement"),
        "utility" to listOf("electricity", "water", "gas", "bill", "energy", "power"),
        "utilities" to listOf("electricity", "water", "gas", "bill", "energy"),
        "power" to listOf("electricity", "energy", "lightning", "bolt", "electric", "battery", "charge"),
        "energy" to listOf("power", "electricity", "solar", "renewable", "battery"),
        "water" to listOf("tap", "droplet", "plumbing", "pipe", "aqua", "drink", "faucet"),
        "gas" to listOf("flame", "cylinder", "stove", "fuel", "fire", "burner"),
        "titas" to listOf("gas", "flame", "cylinder", "fuel"),
        "wasa" to listOf("water", "tap", "droplet", "plumbing"),
        "desco" to listOf("electricity", "power", "energy", "electric"),
        "dpdc" to listOf("electricity", "power", "energy", "electric"),
        "nesco" to listOf("electricity", "power", "energy", "electric"),
        "internet" to listOf("wifi", "router", "network", "broadband", "web", "globe", "ethernet", "online"),
        "wifi" to listOf("router", "internet", "signal", "network", "wireless", "broadband"),
        "broadband" to listOf("internet", "router", "wifi", "fiber", "network"),
        "tv" to listOf("television", "screen", "monitor", "cable", "channel", "display"),
        "television" to listOf("tv", "screen", "monitor", "cable", "broadcast"),
        "cable" to listOf("tv", "television", "wire", "dish", "network"),
        "dish" to listOf("antenna", "satellite", "tv", "cable"),
        "rent" to listOf("house", "apartment", "building", "home", "key", "room", "property"),
        "house" to listOf("home", "apartment", "building", "rent", "residence"),
        "apartment" to listOf("building", "flat", "house", "home", "rent"),
        "maintenance" to listOf("repair", "service", "wrench", "tool", "gear", "fix", "cleaning"),
        "repair" to listOf("wrench", "tool", "fix", "service", "maintenance", "screwdriver"),
        "service" to listOf("maintenance", "repair", "support", "customer-service", "settings"),
        "cleaning" to listOf("clean", "broom", "mop", "wash", "sparkle", "soap"),
        "maid" to listOf("cleaning", "service", "helper", "home"),
        "toilet" to listOf("wc", "restroom", "bath", "bathroom", "hygiene", "washroom"),
        "washroom" to listOf("toilet", "bathroom", "restroom", "wc"),

        // Groceries & Food
        "egg" to listOf("eggs", "food", "breakfast", "poultry", "grocery", "nest"),
        "eggs" to listOf("egg", "food", "breakfast", "poultry", "grocery"),
        "grocery" to listOf("supermarket", "shopping", "cart", "basket", "vegetables", "fruit", "food", "market"),
        "groceries" to listOf("supermarket", "shopping", "cart", "basket", "vegetables", "fruit", "food"),
        "bazar" to listOf("grocery", "market", "shopping", "vegetables", "food", "store"),
        "bazaar" to listOf("grocery", "market", "shopping", "vegetables", "food"),
        "supermarket" to listOf("grocery", "shopping", "cart", "store", "market"),
        "food" to listOf("restaurant", "meal", "dining", "dish", "cooking", "snack", "eat"),
        "restaurant" to listOf("food", "dining", "meal", "fork", "chef", "cafe", "menu"),
        "dining" to listOf("restaurant", "food", "meal", "dinner", "lunch"),
        "meal" to listOf("food", "dining", "dish", "lunch", "dinner", "breakfast"),
        "lunch" to listOf("food", "meal", "dining", "restaurant", "box"),
        "dinner" to listOf("food", "meal", "dining", "restaurant"),
        "breakfast" to listOf("food", "coffee", "egg", "bread", "tea", "morning"),
        "snack" to listOf("food", "fastfood", "chips", "cookie", "coffee"),
        "fastfood" to listOf("burger", "pizza", "fries", "food", "drink"),
        "burger" to listOf("fastfood", "food", "restaurant", "sandwich"),
        "pizza" to listOf("fastfood", "food", "restaurant", "slice"),
        "coffee" to listOf("cafe", "cup", "mug", "espresso", "tea", "drink"),
        "tea" to listOf("cup", "mug", "coffee", "drink", "teapot"),
        "drink" to listOf("beverage", "soda", "water", "juice", "glass", "cup"),
        "fish" to listOf("seafood", "meat", "grocery", "market"),
        "meat" to listOf("beef", "chicken", "grocery", "food", "steak"),
        "chicken" to listOf("meat", "poultry", "food", "grocery"),
        "vegetable" to listOf("vegetables", "grocery", "carrot", "salad", "food"),
        "vegetables" to listOf("grocery", "carrot", "salad", "food", "market"),
        "fruit" to listOf("fruits", "apple", "banana", "orange", "grocery", "fresh"),
        "fruits" to listOf("apple", "banana", "grocery", "fresh", "food"),
        "sweet" to listOf("sweets", "dessert", "candy", "cake", "sugar"),
        "cake" to listOf("dessert", "bakery", "sweet", "birthday"),

        // Transport & Vehicles
        "transport" to listOf("travel", "vehicle", "car", "bus", "train", "commute", "transit"),
        "travel" to listOf("transport", "flight", "airplane", "trip", "vacation", "luggage", "hotel"),
        "car" to listOf("vehicle", "auto", "drive", "sedan", "transport"),
        "bike" to listOf("motorcycle", "bicycle", "scooter", "ride", "helmet"),
        "motorcycle" to listOf("bike", "scooter", "helmet", "ride"),
        "bicycle" to listOf("bike", "cycle", "ride", "pedal"),
        "bus" to listOf("transport", "transit", "ticket", "travel"),
        "train" to listOf("railway", "metro", "subway", "transit", "station"),
        "metro" to listOf("train", "subway", "railway", "transit"),
        "subway" to listOf("train", "metro", "transit"),
        "flight" to listOf("airplane", "plane", "travel", "airport", "ticket"),
        "airplane" to listOf("flight", "plane", "travel", "airport"),
        "taxi" to listOf("cab", "car", "uber", "ride", "transport"),
        "cab" to listOf("taxi", "car", "uber", "ride"),
        "rickshaw" to listOf("transport", "cng", "ride", "commute"),
        "cng" to listOf("auto", "rickshaw", "taxi", "transport"),
        "parking" to listOf("car", "vehicle", "garage", "lot"),
        "toll" to listOf("road", "highway", "bridge", "fee", "car"),

        // Finance, Income, Banking & Investments
        "salary" to listOf("income", "cash", "money", "wallet", "paycheck", "wage", "dollar", "bank"),
        "income" to listOf("salary", "cash", "money", "wallet", "dollar", "revenue", "profit"),
        "wage" to listOf("salary", "income", "money", "paycheck", "cash"),
        "bonus" to listOf("gift", "money", "reward", "star", "dollar", "income"),
        "profit" to listOf("growth", "trending-up", "chart", "money", "dollar"),
        "bank" to listOf("building", "vault", "atm", "finance", "transfer", "account"),
        "card" to listOf("credit-card", "payment", "debit-card", "mastercard", "visa"),
        "credit" to listOf("card", "credit-card", "payment", "loan"),
        "debit" to listOf("card", "credit-card", "payment", "bank"),
        "atm" to listOf("cash", "bank", "money", "dispenser", "card"),
        "loan" to listOf("borrow", "debt", "interest", "handshake", "bank", "money"),
        "debt" to listOf("loan", "borrow", "payment", "money"),
        "emi" to listOf("installment", "loan", "payment", "calendar", "card"),
        "interest" to listOf("percentage", "growth", "bank", "investment"),
        "savings" to listOf("piggy-bank", "safe", "vault", "target", "deposit", "money"),
        "saving" to listOf("piggy-bank", "safe", "vault", "money"),
        "dps" to listOf("savings", "deposit", "bank", "piggy-bank"),
        "fdr" to listOf("deposit", "bank", "certificate", "savings"),
        "investment" to listOf("invest", "stocks", "chart", "trending-up", "growth", "gold", "share"),
        "invest" to listOf("stocks", "chart", "trending-up", "growth", "money"),
        "stocks" to listOf("investment", "chart", "trending-up", "market", "share", "stock"),
        "stock" to listOf("stocks", "investment", "chart", "market"),
        "share" to listOf("stocks", "investment", "chart", "market"),
        "gold" to listOf("bullion", "jewelry", "gold-bar", "treasure", "wealth"),
        "tax" to listOf("vat", "document", "calculator", "government", "percentage"),
        "vat" to listOf("tax", "receipt", "percentage", "invoice"),
        "insurance" to listOf("shield", "protection", "health-insurance", "security", "car-insurance"),

        // Education, Office & Work
        "education" to listOf("school", "study", "books", "university", "graduation", "pencil"),
        "school" to listOf("education", "study", "books", "pencil", "student", "building"),
        "college" to listOf("university", "school", "education", "graduation", "study"),
        "university" to listOf("graduation", "degree", "college", "education", "study"),
        "tuition" to listOf("education", "school", "teacher", "study", "class"),
        "book" to listOf("books", "reading", "study", "education", "library"),
        "books" to listOf("book", "reading", "study", "library"),
        "course" to listOf("education", "learning", "certificate", "class", "laptop"),
        "exam" to listOf("test", "paper", "pencil", "grade", "check"),
        "office" to listOf("work", "desk", "business", "building", "briefcase", "computer"),
        "work" to listOf("briefcase", "office", "laptop", "job", "business"),

        // Shopping, Fashion & Lifestyle
        "shopping" to listOf("cart", "bag", "store", "mall", "market", "tag", "gift"),
        "clothes" to listOf("clothing", "shirt", "tshirt", "dress", "fashion", "hanger"),
        "clothing" to listOf("clothes", "shirt", "dress", "fashion"),
        "dress" to listOf("clothes", "fashion", "woman", "shopping"),
        "shirt" to listOf("tshirt", "clothes", "fashion", "polo"),
        "shoes" to listOf("sneakers", "footwear", "boots", "fashion", "shopping"),
        "watch" to listOf("clock", "time", "accessory", "wristwatch", "luxury"),
        "cosmetics" to listOf("makeup", "beauty", "lipstick", "perfume", "salon"),
        "beauty" to listOf("cosmetics", "salon", "spa", "flower", "skincare"),
        "salon" to listOf("barber", "haircut", "scissors", "beauty", "spa"),
        "barber" to listOf("salon", "haircut", "scissors", "grooming"),
        "gift" to listOf("present", "box", "surprise", "ribbon", "birthday", "heart"),

        // Bangla Translations & Synonyms
        "ডিম" to listOf("egg", "eggs", "food", "breakfast"),
        "তেল" to listOf("oil", "cooking-oil", "fuel", "petrol", "bottle"),
        "তেলের" to listOf("oil", "cooking-oil", "fuel", "petrol"),
        "রিচার্জ" to listOf("recharge", "mobile", "topup", "phone"),
        "ফ্লেক্সিলোড" to listOf("recharge", "mobile", "topup"),
        "ঔষধ" to listOf("medicine", "pill", "pharmacy", "capsule"),
        "ওষুধ" to listOf("medicine", "pill", "pharmacy", "capsule"),
        "ডাক্তার" to listOf("doctor", "medical", "hospital", "stethoscope"),
        "হাসপাতাল" to listOf("hospital", "medical", "clinic", "health"),
        "বিদ্যুৎ" to listOf("electricity", "power", "electric", "lightning", "energy", "meter", "bill"),
        "কারেন্ট" to listOf("electricity", "power", "electric", "current", "volt"),
        "বিল" to listOf("bill", "invoice", "receipt", "payment", "utility"),
        "পানি" to listOf("water", "tap", "droplet", "plumbing", "aqua"),
        "গ্যাস" to listOf("gas", "flame", "cylinder", "stove", "fuel"),
        "ওয়াইফাই" to listOf("wifi", "internet", "router", "network"),
        "ইন্টারনেট" to listOf("internet", "wifi", "router", "network", "web"),
        "মোবাইল" to listOf("mobile", "phone", "smartphone", "sim"),
        "ভাড়া" to listOf("rent", "house", "apartment", "building", "home"),
        "বাড়ি" to listOf("house", "home", "apartment", "building"),
        "বাজার" to listOf("grocery", "market", "shopping", "vegetables", "food"),
        "সদাই" to listOf("grocery", "shopping", "cart", "vegetables"),
        "মুদি" to listOf("grocery", "market", "store", "shopping"),
        "খাবার" to listOf("food", "meal", "restaurant", "dining"),
        "রেস্টুরেন্ট" to listOf("restaurant", "food", "dining", "cafe"),
        "চা" to listOf("tea", "cup", "coffee", "mug"),
        "কফি" to listOf("coffee", "cup", "cafe", "espresso"),
        "নাস্তা" to listOf("breakfast", "snack", "food", "coffee"),
        "যাতায়াত" to listOf("transport", "travel", "car", "bus", "train"),
        "গাড়ি" to listOf("car", "vehicle", "taxi"),
        "বাস" to listOf("bus", "transport", "transit"),
        "রিকশা" to listOf("rickshaw", "transport", "cng"),
        "বেতন" to listOf("salary", "income", "cash", "money", "wallet"),
        "চিকিৎসা" to listOf("health", "medical", "hospital", "doctor"),
        "শিক্ষা" to listOf("education", "school", "book", "university"),
        "বই" to listOf("book", "books", "reading", "study"),
        "টিউশন" to listOf("tuition", "education", "school", "study"),
        "কেনাকাটা" to listOf("shopping", "cart", "bag", "store"),
        "পোশাক" to listOf("clothes", "dress", "shirt", "fashion"),
        "কাপড়" to listOf("clothes", "dress", "fashion"),
        "জুতা" to listOf("shoes", "sneakers", "footwear"),
        "বিনোদন" to listOf("entertainment", "movie", "game", "cinema"),
        "সিনেমা" to listOf("movie", "cinema", "film"),
        "দান" to listOf("charity", "donation", "gift", "give"),
        "যাকাত" to listOf("zakat", "charity", "islam", "mosque"),
        "মসজিদ" to listOf("mosque", "islam", "crescent")
    )

    /**
     * Common modifier words in transaction descriptions (family members, personal qualifiers, prefixes)
     * e.g. "Ammu recharge", "Abbu medicine", "purny Medicine", "Bhaiya burger"
     */
    private val MODIFIER_WORDS = setOf(
        "ammu", "abbu", "bhaiya", "apu", "bhai", "bubu", "choto", "boro", "uncle", "aunty",
        "khalamma", "mama", "mami", "friend", "self", "me", "my", "our", "his", "her",
        "brother", "sister", "mother", "father", "mom", "dad", "boss", "colleague",
        "driver", "maid", "bua", "baby", "kid", "kids", "purny", "john", "mr", "mrs", "dr",
        "daily", "monthly", "weekly", "yearly", "morning", "noon", "evening", "night",
        "today", "yesterday", "special", "extra", "urgent", "quick", "new", "old", "mini",
        "maxi", "big", "small", "fresh", "hot", "cold", "home", "office", "personal", "local"
    )

    /**
     * Sanitizes user search queries by stripping redundant stop-words like 'icon', 'logo', 'symbol',
     * which cause zero-hit failures on strict search APIs like Iconify.
     */
    fun cleanSearchQuery(query: String): String {
        val raw = query.trim()
        if (raw.isBlank()) return ""
        val stopWordsRegex = Regex("(?i)\\b(icon|icons|logo|logos|symbol|symbols|vector|svg|png|image|images|pic|picture|pictures|photo|photos|clipart|graphic|graphics)\\b")
        val stripped = raw.replace(stopWordsRegex, "").trim().replace(Regex("\\s+"), " ")
        return if (stripped.length >= 2) stripped else raw
    }

    private fun isGenericStopWord(word: String): Boolean {
        return word.lowercase() in GENERIC_STOP_WORDS || word.length < 2
    }

    private fun isAnchorKeyword(token: String): Boolean {
        val lower = token.lowercase()
        return SYNONYM_MAP.containsKey(lower)
    }

    /**
     * Splits query into 3 sequential search tiers:
     * 1. Exact full query (the whole word / phrase / anchor core)
     * 2. Split individual words (prioritizing anchor keywords over modifiers)
     * 3. Synonyms & domain keywords
     */
    fun generateSearchKeywordTiers(rawQuery: String): SearchKeywordTiers {
        val clean = cleanSearchQuery(rawQuery).trim()
        if (clean.isBlank()) return SearchKeywordTiers("", emptyList(), emptyList())

        val cleanLower = clean.lowercase()
        val allTokens = clean.split(Regex("[\\s,_\\-]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        // Detect single-letter prefix (e.g. "T oil", "E bike", "A water")
        val isSingleLetterPrefix = allTokens.size > 1 && allTokens.first().length == 1
        val mainTokens = if (isSingleLetterPrefix) allTokens.drop(1) else allTokens

        // Separate tokens into Anchor domain words vs Modifier / Unknown words
        val anchorTokens = mutableListOf<String>()
        val modifierTokens = mutableListOf<String>()

        for (t in mainTokens) {
            if (isAnchorKeyword(t)) {
                anchorTokens.add(t)
            } else if (!isGenericStopWord(t) && t !in MODIFIER_WORDS) {
                modifierTokens.add(t)
            }
        }

        // Determine primary anchor keyword (e.g. "recharge" from "Ammu recharge", "medicine" from "purny Medicine", "oil" from "T oil")
        val primaryAnchor = anchorTokens.firstOrNull()
            ?: mainTokens.firstOrNull { it.length >= 2 && it !in MODIFIER_WORDS }
            ?: cleanLower

        // Tier 1 Exact Target:
        // If "T oil" -> exact target is "oil" to prevent false fuzzy match to "toilet"
        val exactWord = if (isSingleLetterPrefix && allTokens.first() == "t") {
            primaryAnchor
        } else if (allTokens.size > 1 && allTokens.first() in MODIFIER_WORDS && anchorTokens.isNotEmpty()) {
            // For modifier combos like "Ammu recharge" or "purny Medicine", elevate the anchor word to Tier 1
            primaryAnchor
        } else {
            clean
        }

        // Tier 2 Split words: Anchor tokens first, then other meaningful words
        val splitList = mutableListOf<String>()
        if (primaryAnchor.isNotBlank() && primaryAnchor != exactWord.lowercase()) {
            splitList.add(primaryAnchor)
        }
        for (a in anchorTokens) {
            if (a !in splitList && a != exactWord.lowercase()) splitList.add(a)
        }
        if (clean.lowercase() != exactWord.lowercase() && !isSingleLetterPrefix) {
            splitList.add(clean)
        }
        for (m in modifierTokens) {
            if (m !in splitList && m != exactWord.lowercase() && m.length >= 2) splitList.add(m)
        }

        // Tier 3 Synonyms:
        val synList = mutableListOf<String>()
        SYNONYM_MAP[cleanLower]?.let { synList.addAll(it) }
        SYNONYM_MAP[exactWord.lowercase()]?.let { synList.addAll(it) }
        SYNONYM_MAP[primaryAnchor]?.let { synList.addAll(it) }
        for (a in anchorTokens) {
            SYNONYM_MAP[a]?.let { synList.addAll(it) }
        }
        for (m in modifierTokens) {
            SYNONYM_MAP[m]?.let { synList.addAll(it) }
        }

        val excludeSet = (listOf(cleanLower, exactWord.lowercase()) + splitList.map { it.lowercase() }).toSet()
        val finalSynonyms = synList.distinct().filter { it.lowercase() !in excludeSet }

        return SearchKeywordTiers(
            exactWord = exactWord,
            splitWords = splitList.distinct(),
            synonyms = finalSynonyms
        )
    }

    /**
     * Searches SVGL open logos for a single keyword.
     */
    private suspend fun fetchSvglLogos(term: String, page: Int, limit: Int = 8): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val enc = URLEncoder.encode(term, "UTF-8")
            val url = "https://api.svgl.app?search=$enc"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val jsonArr = JSONArray(body)
                val results = mutableListOf<OnlineIconResult>()
                val startIdx = (page - 1) * limit
                val endIdx = minOf(jsonArr.length(), page * limit)
                for (i in startIdx until endIdx) {
                    val obj = jsonArr.optJSONObject(i) ?: continue
                    val title = obj.optString("title").ifBlank { obj.optString("name") }
                    var routeUrl = obj.optString("route")
                    if (routeUrl.isBlank()) {
                        val routeObj = obj.optJSONObject("route")
                        if (routeObj != null) {
                            routeUrl = routeObj.optString("light").ifBlank { routeObj.optString("dark") }
                        }
                    }
                    if (routeUrl.isNotBlank() && routeUrl.startsWith("http")) {
                        results.add(
                            OnlineIconResult(
                                title = title.ifBlank { term },
                                imageUrl = routeUrl,
                                sourceName = "SVGL",
                                isColorful = true
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Iconify vector icon API for a single keyword.
     * Rejects false fuzzy matches (e.g. "toilet" when searching for "oil").
     */
    private suspend fun fetchIconifyIcons(
        term: String,
        page: Int,
        limit: Int = 24,
        prefixes: String? = null
    ): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val termEnc = URLEncoder.encode(term, "UTF-8")
            val start = (page - 1) * limit
            val prefixParam = if (prefixes.isNullOrBlank()) "" else "&prefixes=$prefixes"
            val url = "https://api.iconify.design/search?query=$termEnc&limit=$limit&start=$start$prefixParam"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val iconsArr = json.optJSONArray("icons") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineIconResult>()
                val termLower = term.lowercase()

                for (i in 0 until iconsArr.length()) {
                    val iconStr = iconsArr.optString(i)
                    if (iconStr.isNotBlank() && iconStr.contains(":")) {
                        val parts = iconStr.split(":", limit = 2)
                        val prefix = parts[0].uppercase()
                        val iconName = parts[1]

                        // Anti-fuzzy mismatch filter:
                        // If searching "oil", do not match "toilet" or "toil"
                        val iconNameLower = iconName.lowercase()
                        if (termLower == "oil" && (iconNameLower.contains("toilet") || iconNameLower.contains("spoil"))) {
                            continue
                        }
                        if (termLower == "egg" && iconNameLower.contains("veggie")) {
                            continue
                        }

                        val svgUrl = "https://api.iconify.design/${parts[0]}/$iconName.svg"
                        val cleanTitle = iconName.replace("-", " ")
                            .replaceFirstChar { it.uppercase() }
                        val isKnownColorful = prefix in KNOWN_COLORFUL_SOURCES
                        results.add(
                            OnlineIconResult(
                                title = cleanTitle,
                                imageUrl = svgUrl,
                                sourceName = prefix,
                                isColorful = isKnownColorful
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Brandfetch for corporate / company logos.
     */
    private suspend fun fetchBrandfetchLogos(term: String, page: Int, limit: Int = 6): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val request = Request.Builder()
                .url("https://api.brandfetch.io/v2/search/$encoded")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val jsonArr = JSONArray(body)
                val results = mutableListOf<OnlineIconResult>()
                val startIdx = (page - 1) * limit
                val endIdx = minOf(jsonArr.length(), page * limit)
                for (i in startIdx until endIdx) {
                    val obj = jsonArr.optJSONObject(i) ?: continue
                    val iconUrl = obj.optString("icon")
                    val name = obj.optString("name").ifBlank { obj.optString("domain") }
                    if (iconUrl.isNotBlank() && iconUrl.startsWith("http")) {
                        results.add(
                            OnlineIconResult(
                                title = name,
                                imageUrl = iconUrl,
                                sourceName = "Brandfetch",
                                isColorful = true
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Wikimedia Commons for logos/drawings.
     */
    private suspend fun fetchWikimediaIcons(term: String, page: Int, limit: Int = 8): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val offset = (page - 1) * limit
            val wmUrl = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap|drawing+$encoded+logo|icon&gsrlimit=$limit&gsroffset=$offset&prop=imageinfo&iiprop=url|thumburl&iiurlwidth=160&format=json"
            val request = Request.Builder()
                .url(wmUrl)
                .header("User-Agent", "BudgeterApp/1.0 (Android; open-source)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val root = JSONObject(body)
                val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineIconResult>()
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val pageObj = pages.optJSONObject(key) ?: continue
                    val imageInfoArr = pageObj.optJSONArray("imageinfo") ?: continue
                    val info = imageInfoArr.optJSONObject(0) ?: continue
                    val thumbUrl = info.optString("thumburl").ifBlank { info.optString("url") }
                    if (thumbUrl.isNotBlank()) {
                        val rawTitle = pageObj.optString("title", term)
                            .replace("File:", "")
                            .substringBeforeLast(".")
                            .take(28)
                        results.add(
                            OnlineIconResult(
                                title = rawTitle,
                                imageUrl = thumbUrl,
                                sourceName = "Wikimedia",
                                isColorful = true
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches DuckDuckGo topics/icons for a term.
     */
    private suspend fun fetchDuckDuckGoIcons(term: String, page: Int): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val request = Request.Builder()
                .url("https://api.duckduckgo.com/?q=$encoded&format=json")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val obj = JSONObject(body)
                val results = mutableListOf<OnlineIconResult>()
                if (page == 1) {
                    val mainImg = obj.optString("Image")
                    if (mainImg.isNotBlank()) {
                        val fullUrl = if (mainImg.startsWith("/")) "https://duckduckgo.com$mainImg" else mainImg
                        results.add(
                            OnlineIconResult(
                                title = obj.optString("Heading", term),
                                imageUrl = fullUrl,
                                sourceName = "DuckDuckGo",
                                isColorful = true
                            )
                        )
                    }
                }

                val relatedTopics = obj.optJSONArray("RelatedTopics")
                if (relatedTopics != null) {
                    val startIdx = (page - 1) * 6
                    val endIdx = minOf(relatedTopics.length(), page * 6)
                    for (i in startIdx until endIdx) {
                        val topic = relatedTopics.optJSONObject(i) ?: continue
                        val iconObj = topic.optJSONObject("Icon") ?: continue
                        val iconUrl = iconObj.optString("URL")
                        if (iconUrl.isNotBlank()) {
                            val fullUrl = if (iconUrl.startsWith("/")) "https://duckduckgo.com$iconUrl" else iconUrl
                            val text = topic.optString("Text", term).take(30)
                            results.add(
                                OnlineIconResult(
                                    title = text,
                                    imageUrl = fullUrl,
                                    sourceName = "DuckDuckGo",
                                    isColorful = true
                                )
                            )
                        }
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Checks VectorLogoZone for common brand vector assets.
     */
    private suspend fun fetchVectorLogoZone(term: String): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val slugCandidates = listOf(
            term.lowercase().replace(" ", ""),
            term.lowercase().replace(" ", "-"),
            term.lowercase().replace(" ", "_")
        ).distinct()

        val results = mutableListOf<OnlineIconResult>()
        for (slug in slugCandidates) {
            val candidateUrls = listOf(
                "https://www.vectorlogo.zone/logos/$slug/$slug-icon.svg",
                "https://www.vectorlogo.zone/logos/$slug/$slug-tile.svg"
            )
            for (candUrl in candidateUrls) {
                try {
                    val req = Request.Builder()
                        .url(candUrl)
                        .header("User-Agent", USER_AGENT)
                        .head()
                        .build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            results.add(
                                OnlineIconResult(
                                    title = term.replaceFirstChar { it.uppercase() },
                                    imageUrl = candUrl,
                                    sourceName = "VectorLogoZone",
                                    isColorful = true
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        results
    }

    /**
     * Searches CoinGecko for cryptocurrency / financial token icons.
     */
    private suspend fun fetchCoinGeckoIcons(term: String, page: Int): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val cgUrl = "https://api.coingecko.com/api/v3/search?query=$encoded"
            val request = Request.Builder()
                .url(cgUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val coinsArr = json.optJSONArray("coins") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineIconResult>()
                val limit = minOf(coinsArr.length(), if (page == 1) 6 else 3)
                for (i in 0 until limit) {
                    val coin = coinsArr.optJSONObject(i) ?: continue
                    val name = coin.optString("name")
                    val symbol = coin.optString("symbol")
                    val imgUrl = coin.optString("large").ifBlank { coin.optString("thumb") }
                    if (imgUrl.isNotBlank()) {
                        results.add(
                            OnlineIconResult(
                                title = "$name ($symbol)",
                                imageUrl = imgUrl,
                                sourceName = "CoinGecko",
                                isColorful = true
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Wikipedia for logos & lead thumbnail icons.
     */
    private suspend fun fetchWikipediaIcons(term: String, page: Int): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val wikiPrefixUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages|pageterms&generator=prefixsearch&gpssearch=$encoded&gpslimit=8&piprop=thumbnail&pithumbsize=256"
            val request = Request.Builder()
                .url(wikiPrefixUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineIconResult>()
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val pageObj = pages.optJSONObject(key) ?: continue
                    val title = pageObj.optString("title", term)
                    val thumbObj = pageObj.optJSONObject("thumbnail")
                    val thumbUrl = thumbObj?.optString("source")
                    if (!thumbUrl.isNullOrBlank()) {
                        results.add(
                            OnlineIconResult(
                                title = title,
                                imageUrl = thumbUrl,
                                sourceName = "Wikipedia",
                                isColorful = true
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches Google / DDG web favicons for domain-like terms.
     */
    private suspend fun fetchFavicons(term: String): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val noSpaces = term.replace(" ", "").lowercase()
        val domain = if (noSpaces.contains(".")) noSpaces else "$noSpaces.com"
        val results = mutableListOf<OnlineIconResult>()

        val googleFaviconUrl = "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://$domain&size=128"
        results.add(
            OnlineIconResult(
                title = term.replaceFirstChar { it.uppercase() },
                imageUrl = googleFaviconUrl,
                sourceName = "Favicon",
                isColorful = true
            )
        )

        val ddgFaviconUrl = "https://icons.duckduckgo.com/ip3/$domain.ico"
        results.add(
            OnlineIconResult(
                title = term.replaceFirstChar { it.uppercase() },
                imageUrl = ddgFaviconUrl,
                sourceName = "DuckDuckGo Favicon",
                isColorful = true
            )
        )

        results
    }

    /**
     * Gathers all icons for a single search term across all icon providers concurrently.
     */
    private suspend fun fetchIconsForTerm(
        term: String,
        page: Int,
        isExact: Boolean,
        limit: Int = 24
    ): List<OnlineIconResult> = coroutineScope {
        val isFinance = term.lowercase() in setOf("crypto", "bitcoin", "ethereum", "token", "coin", "finance", "bank", "money")

        val svglJob = async { fetchSvglLogos(term, page, limit = if (isExact) 8 else 4) }
        val iconifyJob = async { fetchIconifyIcons(term, page, limit = limit) }
        val curatedIconifyJob = async {
            if (page == 1) {
                val curatedPrefixes = "flat-color-icons,skill-icons,vscode-icons,devicon,thesvg-color,streamline-color,streamline-plump-color,token-branded,circle-flags,openmoji,fluent-emoji-flat"
                fetchIconifyIcons(term, page, limit = 16, prefixes = curatedPrefixes)
            } else emptyList()
        }
        val brandfetchJob = async { fetchBrandfetchLogos(term, page, limit = if (isExact) 6 else 3) }
        val wikiMediaJob = async { fetchWikimediaIcons(term, page, limit = if (isExact) 8 else 4) }
        val ddgJob = async { if (isExact) fetchDuckDuckGoIcons(term, page) else emptyList() }
        val wikiJob = async { if (isExact) fetchWikipediaIcons(term, page) else emptyList() }
        val vlzJob = async { if (page == 1 && isExact) fetchVectorLogoZone(term) else emptyList() }
        val cgJob = async { if (isFinance) fetchCoinGeckoIcons(term, page) else emptyList() }
        val faviconJob = async { if (page == 1 && isExact) fetchFavicons(term) else emptyList() }

        val all = svglJob.await() +
                curatedIconifyJob.await() +
                iconifyJob.await() +
                brandfetchJob.await() +
                wikiMediaJob.await() +
                ddgJob.await() +
                wikiJob.await() +
                vlzJob.await() +
                cgJob.await() +
                faviconJob.await()

        // Sort colorful and high-priority sources first
        all.sortedWith(
            compareByDescending<OnlineIconResult> { it.isColorful && it.sourceName != "Brandfetch" && it.sourceName != "Favicon" && it.sourceName != "DuckDuckGo Favicon" }
                .thenByDescending { it.sourceName in setOf("SVGL", "VectorLogoZone", "CoinGecko", "FLAT-COLOR-ICONS", "FLUENT-EMOJI-FLAT") }
                .thenByDescending { it.isColorful }
                .thenBy { it.sourceName == "Wikimedia" }
        )
    }

    /**
     * Searches for logos and icons online using keyless free providers with 3-tier priority:
     * 1. Exact full word / phrase match (or elevated primary anchor word)
     * 2. Split individual words match (anchor tokens first, then modifiers)
     * 3. Synonyms & domain keywords match
     */
    suspend fun searchIcons(context: Context?, query: String, page: Int = 1): List<OnlineIconResult> = searchIcons(query, context, page)

    suspend fun searchIcons(query: String, context: Context? = null, page: Int = 1): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val tiers = generateSearchKeywordTiers(query)
        if (tiers.exactWord.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineIconResult>()
        val seenUrls = mutableSetOf<String>()

        coroutineScope {
            // Tier 1: Search exact word / primary anchor
            val tier1Job = async {
                fetchIconsForTerm(tiers.exactWord, page, isExact = true, limit = 28)
            }

            // Tier 2: Search split individual words concurrently (anchor words prioritized)
            val tier2Jobs = if (tiers.splitWords.isNotEmpty()) {
                tiers.splitWords.map { splitWord ->
                    async { fetchIconsForTerm(splitWord, page, isExact = false, limit = 16) }
                }
            } else emptyList()

            // Tier 3: Search synonyms concurrently (up to 8 top synonyms)
            val tier3Jobs = if (tiers.synonyms.isNotEmpty()) {
                tiers.synonyms.take(8).map { synWord ->
                    async { fetchIconsForTerm(synWord, page, isExact = false, limit = 12) }
                }
            } else emptyList()

            // Await and collect in STRICT 1 -> 2 -> 3 order:
            // 1. Exact full word / primary anchor results
            val tier1Results = tier1Job.await()
            for (item in tier1Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }

            // 2. Split words results
            val tier2Results = tier2Jobs.awaitAll().flatten()
            for (item in tier2Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }

            // 3. Synonym results
            val tier3Results = tier3Jobs.awaitAll().flatten()
            for (item in tier3Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }
        }

        results
    }

    /**
     * Searches Wikimedia Commons High-Res Media API for a single keyword.
     */
    private suspend fun fetchWikimediaImages(term: String, page: Int, limit: Int = 16): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val offset = (page - 1) * limit
            val url = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap+$encoded&gsrlimit=$limit&gsroffset=$offset&prop=imageinfo&iiprop=url|dimensions&iiurlwidth=500&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val queryObj = json.optJSONObject("query")
                val pages = queryObj?.optJSONObject("pages") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineImageResult>()
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val pageItem = pages.optJSONObject(key) ?: continue
                    val title = pageItem.optString("title", "Image")
                        .removePrefix("File:")
                        .substringBeforeLast(".")
                        .replace("_", " ")
                    val imageinfo = pageItem.optJSONArray("imageinfo") ?: continue
                    if (imageinfo.length() > 0) {
                        val info = imageinfo.optJSONObject(0) ?: continue
                        val fullUrl = info.optString("url")
                        val thumbUrl = info.optString("thumburl", fullUrl)
                        val width = info.optInt("width", 0)
                        val height = info.optInt("height", 0)
                        if (fullUrl.isNotBlank() && !fullUrl.endsWith(".svg", ignoreCase = true)) {
                            results.add(
                                OnlineImageResult(
                                    title = title,
                                    imageUrl = fullUrl,
                                    thumbUrl = thumbUrl,
                                    sourceName = "Wikimedia",
                                    width = width,
                                    height = height
                                )
                            )
                        }
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Openverse free creative commons photo library.
     */
    private suspend fun fetchOpenverseImages(term: String, page: Int, limit: Int = 16): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val url = "https://api.openverse.org/v1/images/?q=$encoded&page=$page&page_size=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val resultsArr = json.optJSONArray("results") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineImageResult>()
                for (i in 0 until resultsArr.length()) {
                    val item = resultsArr.optJSONObject(i) ?: continue
                    val title = item.optString("title", term)
                    val imgUrl = item.optString("url")
                    val thumb = item.optString("thumbnail", imgUrl)
                    val provider = item.optString("provider", "Openverse").uppercase()
                    if (imgUrl.isNotBlank()) {
                        results.add(
                            OnlineImageResult(
                                title = title,
                                imageUrl = imgUrl,
                                thumbUrl = thumb,
                                sourceName = provider
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Unsplash public photo API.
     */
    private suspend fun fetchUnsplashImages(term: String, page: Int, limit: Int = 16): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val url = "https://unsplash.com/napi/search/photos?query=$encoded&per_page=$limit&page=$page"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val resultsArr = json.optJSONArray("results") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineImageResult>()
                for (i in 0 until resultsArr.length()) {
                    val item = resultsArr.optJSONObject(i) ?: continue
                    val title = item.optString("alt_description", item.optString("description", term))
                    val urls = item.optJSONObject("urls") ?: continue
                    val regular = urls.optString("regular")
                    val small = urls.optString("small", regular)
                    if (regular.isNotBlank()) {
                        results.add(
                            OnlineImageResult(
                                title = if (title.isBlank()) term else title,
                                imageUrl = regular,
                                thumbUrl = small,
                                sourceName = "Unsplash"
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Searches Wikipedia article lead images & media.
     */
    private suspend fun fetchWikipediaImages(term: String, page: Int, limit: Int = 12): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val offset = (page - 1) * limit
            val url = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=$encoded&gsrlimit=$limit&gsroffset=$offset&prop=pageimages&piprop=original|thumbnail&pithumbsize=600&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineImageResult>()
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val pageObj = pages.optJSONObject(key) ?: continue
                    val title = pageObj.optString("title", term)
                    val orig = pageObj.optJSONObject("original")?.optString("source")
                    val thumb = pageObj.optJSONObject("thumbnail")?.optString("source")
                    val fullImg = orig ?: thumb
                    if (!fullImg.isNullOrBlank() && !fullImg.endsWith(".svg", ignoreCase = true)) {
                        results.add(
                            OnlineImageResult(
                                title = title,
                                imageUrl = fullImg,
                                thumbUrl = thumb ?: fullImg,
                                sourceName = "Wikipedia"
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Cache for DuckDuckGo vqd session tokens to avoid repeated handshake calls.
     */
    private val ddgVqdCache = ConcurrentHashMap<String, Pair<String, Long>>()

    /**
     * Retrieves the session token (vqd) required by DuckDuckGo's image search endpoint.
     */
    private suspend fun getDuckDuckGoVqd(term: String): String? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = ddgVqdCache[term]
        if (cached != null && (now - cached.second) < 300_000L) {
            return@withContext cached.first
        }

        try {
            val encoded = URLEncoder.encode(term, "UTF-8")
            val url = "https://duckduckgo.com/?q=$encoded&iax=images&ia=images"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", DDG_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            client.newCall(request).execute().use { response ->
                val headerVqd = response.header("x-vqd-4") ?: response.header("vqd")
                if (!headerVqd.isNullOrBlank()) {
                    ddgVqdCache[term] = Pair(headerVqd, now)
                    return@withContext headerVqd
                }

                val body = response.body?.string().orEmpty()
                if (body.isNotBlank()) {
                    val match = Regex("""vqd=([0-9a-zA-Z_-]+)""").find(body)
                        ?: Regex("""vqd=['"]([0-9a-zA-Z_-]+)['"]""").find(body)
                        ?: Regex("""vqd:\s*['"]([0-9a-zA-Z_-]+)['"]""").find(body)
                        ?: Regex("""data-vqd=['"]([0-9a-zA-Z_-]+)['"]""").find(body)

                    if (match != null) {
                        val token = match.groupValues[1]
                        if (token.isNotBlank()) {
                            ddgVqdCache[term] = Pair(token, now)
                            return@withContext token
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ignore network failure
        }
        null
    }

    /**
     * Searches real web image results via DuckDuckGo (powered by Bing Web Index).
     * Retrieves actual commercial product packaging, brand photos, and item pictures with no API key needed.
     */
    private suspend fun fetchDuckDuckGoImages(term: String, page: Int, limit: Int = 16): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        try {
            val vqd = getDuckDuckGoVqd(term) ?: return@withContext emptyList()
            val encoded = URLEncoder.encode(term, "UTF-8")
            val p = if (page <= 1) 1 else page

            val url = "https://duckduckgo.com/i.js?l=us-en&o=json&q=$encoded&vqd=$vqd&f=,,,&p=$p"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", DDG_USER_AGENT)
                .header("Referer", "https://duckduckgo.com/")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("x-requested-with", "XMLHttpRequest")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@withContext emptyList()

                val json = JSONObject(body)
                val resultsArr = json.optJSONArray("results") ?: return@withContext emptyList()
                val results = mutableListOf<OnlineImageResult>()

                val count = minOf(resultsArr.length(), limit)
                for (i in 0 until count) {
                    val item = resultsArr.optJSONObject(i) ?: continue
                    val title = item.optString("title", term)
                    val fullImg = item.optString("image")
                    val thumb = item.optString("thumbnail").ifBlank { fullImg }
                    val width = item.optInt("width", 0)
                    val height = item.optInt("height", 0)

                    if (fullImg.isNotBlank() && (fullImg.startsWith("http://") || fullImg.startsWith("https://"))) {
                        results.add(
                            OnlineImageResult(
                                title = title,
                                imageUrl = fullImg,
                                thumbUrl = thumb,
                                sourceName = "DuckDuckGo",
                                width = width,
                                height = height
                            )
                        )
                    }
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches images for a single search term across all media engines concurrently.
     */
    private suspend fun fetchImagesForTerm(term: String, page: Int, limit: Int = 16): List<OnlineImageResult> = coroutineScope {
        val ddgJob = async { fetchDuckDuckGoImages(term, page, limit) }
        val wikiJob = async { fetchWikimediaImages(term, page, limit) }
        val openverseJob = async { fetchOpenverseImages(term, page, limit) }
        val unsplashJob = async { fetchUnsplashImages(term, page, limit) }
        val wikiPageJob = async { fetchWikipediaImages(term, page, limit = 8) }

        val (ddg, wiki, openverse, unsplash, wikiPage) = awaitAll(ddgJob, wikiJob, openverseJob, unsplashJob, wikiPageJob)
        val combined = mutableListOf<OnlineImageResult>()
        val seen = mutableSetOf<String>()

        val maxLen = maxOf(ddg.size, wiki.size, openverse.size, unsplash.size, wikiPage.size)
        for (i in 0 until maxLen) {
            if (i < ddg.size && seen.add(ddg[i].imageUrl)) combined.add(ddg[i])
            if (i < wiki.size && seen.add(wiki[i].imageUrl)) combined.add(wiki[i])
            if (i < unsplash.size && seen.add(unsplash[i].imageUrl)) combined.add(unsplash[i])
            if (i < wikiPage.size && seen.add(wikiPage[i].imageUrl)) combined.add(wikiPage[i])
            if (i < openverse.size && seen.add(openverse[i].imageUrl)) combined.add(openverse[i])
        }
        combined
    }

    /**
     * Searches online images across multiple free, keyless endpoints with 3-tier priority:
     * 1. Exact full word / phrase match (or elevated primary anchor word)
     * 2. Split individual words match
     * 3. Synonyms & domain keywords match
     */
    suspend fun searchImages(context: Context?, query: String, page: Int = 1): List<OnlineImageResult> = searchImages(query, context, page)

    suspend fun searchImages(query: String, context: Context? = null, page: Int = 1): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        val tiers = generateSearchKeywordTiers(query)
        if (tiers.exactWord.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineImageResult>()
        val seenUrls = mutableSetOf<String>()

        coroutineScope {
            // Tier 1: Search exact word / primary anchor
            val tier1Job = async {
                fetchImagesForTerm(tiers.exactWord, page, limit = 16)
            }

            // Tier 2: Search split individual words (anchor words prioritized)
            val tier2Jobs = if (tiers.splitWords.isNotEmpty()) {
                tiers.splitWords.map { splitWord ->
                    async { fetchImagesForTerm(splitWord, page, limit = 12) }
                }
            } else emptyList()

            // Tier 3: Search synonyms (up to 6 synonyms)
            val tier3Jobs = if (tiers.synonyms.isNotEmpty()) {
                tiers.synonyms.take(6).map { synWord ->
                    async { fetchImagesForTerm(synWord, page, limit = 8) }
                }
            } else emptyList()

            // Await and collect in STRICT 1 -> 2 -> 3 order:
            // 1. Exact full word / primary anchor results
            val tier1Results = tier1Job.await()
            for (item in tier1Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }

            // 2. Split words results
            val tier2Results = tier2Jobs.awaitAll().flatten()
            for (item in tier2Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }

            // 3. Synonym results
            val tier3Results = tier3Jobs.awaitAll().flatten()
            for (item in tier3Results) {
                if (seenUrls.add(item.imageUrl)) {
                    results.add(item)
                }
            }
        }

        results
    }

    /**
     * Downloads an online image by URL (supports vector SVG, PNG, WebP, JPG)
     * and persists it locally into the app's custom icons directory with optimal compression.
     * Optionally draws a solid [backgroundColor] (e.g. for transparent icons/logos).
     * Returns the persistent custom icon key (e.g. "custom_icon_172...").
     */
    suspend fun downloadAndSaveIcon(
        context: Context,
        imageUrl: String,
        name: String? = null,
        backgroundColor: Int? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()

            val iconKey = "custom_icon_${System.currentTimeMillis()}"
            val destFile = File(customDir, "$iconKey.png")

            // 1. Attempt loading and rasterizing via Coil ImageLoader (handles SVGs, PNGs, WebP, JPG)
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(384, 384)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val targetSize = 384
                    val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    if (backgroundColor != null) {
                        canvas.drawColor(backgroundColor)
                    }
                    drawable.setBounds(0, 0, targetSize, targetSize)
                    drawable.draw(canvas)

                    FileOutputStream(destFile).use { outStream ->
                        val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                        val compressed = try {
                            bitmap.compress(format, 92, outStream)
                        } catch (_: Exception) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                        }
                        if (!compressed) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                        }
                        outStream.flush()
                    }
                    return@withContext iconKey
                }
            } catch (_: Exception) {
                // Fallback to direct HTTP stream
            }

            // 2. Direct HTTP download fallback
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val bytes = response.body?.bytes() ?: return@withContext null
            response.close()

            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext null

            val targetDim = 384
            val scaleFactor = targetDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(1),
                (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(1),
                true
            )

            val finalBitmap = if (backgroundColor != null) {
                val bgBmp = Bitmap.createBitmap(targetDim, targetDim, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bgBmp)
                canvas.drawColor(backgroundColor)
                val left = (targetDim - scaledBitmap.width) / 2f
                val top = (targetDim - scaledBitmap.height) / 2f
                canvas.drawBitmap(scaledBitmap, left, top, null)
                bgBmp
            } else {
                scaledBitmap
            }

            FileOutputStream(destFile).use { outStream ->
                val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                val compressed = try {
                    finalBitmap.compress(format, 92, outStream)
                } catch (_: Exception) {
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                }
                if (!compressed) {
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                }
                outStream.flush()
            }

            iconKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads an online image into a high-res in-memory Bitmap suitable for editing and cropping.
     */
    suspend fun downloadBitmap(context: Context, imageUrl: String, targetSize: Int = 512): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. Attempt loading and rasterizing via Coil ImageLoader (supports SVGs, PNGs, WebP, JPG)
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(targetSize, targetSize)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, targetSize, targetSize)
                    drawable.draw(canvas)
                    return@withContext bitmap
                }
            } catch (_: Exception) {}

            // 2. Direct HTTP download fallback
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val bytes = response.body?.bytes() ?: return@withContext null
            response.close()

            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
            Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
