package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Metadata descriptor for a selectable icon.
 */
data class IconItem(
    val name: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val isAutoMirrored: Boolean = false
)

object IconHelper {

    val CATEGORIES = listOf(
        "All",
        "Custom",
        "Online Search",
        "BD Banks & MFS",
        "Bills & Housing",
        "Celebrations & Social",
        "Education & Study",
        "Entertainment & Sports",
        "Finance & Accounts",
        "Food & Groceries",
        "Health & Wellness",
        "Income & Earnings",
        "Islamic & Charity",
        "Life & Work",
        "Nature & Weather",
        "Repairs & Maintenance",
        "Shopping & Fashion",
        "Symbols",
        "Tech & Tools",
        "Transport & Travel"
    )

    val BUILTIN_ICONS: List<IconItem> = listOf(
        // Bangladeshi Banks & Mobile Banking Services (MFS) - At least 2 per bank
        IconItem("BankBkash", "BD Banks & MFS", listOf("bkash", "bKash", "বিকাশ", "mobile bank", "mfs", "wallet", "bangladesh", "finance"), false),
        IconItem("BankBkashAlt", "BD Banks & MFS", listOf("bkash", "bKash", "বিকাশ", "pink", "badge", "mfs", "bangladesh"), false),
        IconItem("BankNagad", "BD Banks & MFS", listOf("nagad", "নগদ", "mobile bank", "post office", "mfs", "orange", "flame", "bangladesh"), false),
        IconItem("BankNagadAlt", "BD Banks & MFS", listOf("nagad", "নগদ", "seal", "mfs", "bangladesh", "dak"), false),
        IconItem("BankRocket", "BD Banks & MFS", listOf("rocket", "রকেট", "dbbl", "dutch bangla", "mobile bank", "purple", "mfs", "bangladesh"), false),
        IconItem("BankRocketAlt", "BD Banks & MFS", listOf("rocket", "রকেট", "orbit", "mfs", "bangladesh"), false),
        IconItem("BankUpay", "BD Banks & MFS", listOf("upay", "উপায়", "ucb", "mobile bank", "mfs", "cyan", "bangladesh"), false),
        IconItem("BankUpayAlt", "BD Banks & MFS", listOf("upay", "উপায়", "navy", "badge", "mfs", "bangladesh"), false),
        IconItem("BankCellfin", "BD Banks & MFS", listOf("cellfin", "সেলফিন", "ibbl", "islami bank", "wallet", "app", "bangladesh"), false),
        IconItem("BankCellfinAlt", "BD Banks & MFS", listOf("cellfin", "সেলফিন", "coin", "mfs", "bangladesh"), false),
        IconItem("BankDBBL", "BD Banks & MFS", listOf("dbbl", "dutch bangla", "ডাচ বাংলা", "bank", "banyan", "leaf", "bangladesh"), false),
        IconItem("BankDBBLAlt", "BD Banks & MFS", listOf("dbbl", "dutch bangla", "crest", "vault", "bank", "bangladesh"), false),
        IconItem("BankIBBL", "BD Banks & MFS", listOf("ibbl", "islami bank", "ইসলামী ব্যাংক", "crescent", "wheat", "bank", "bangladesh"), false),
        IconItem("BankIBBLAlt", "BD Banks & MFS", listOf("ibbl", "islami bank", "star", "seal", "islamic", "bank", "bangladesh"), false),
        IconItem("BankBRAC", "BD Banks & MFS", listOf("brac", "brac bank", "ব্র্যাক ব্যাংক", "square", "yellow", "blue", "bank", "bangladesh"), false),
        IconItem("BankAstha", "BD Banks & MFS", listOf("astha", "brac astha", "আস্থা", "mobile app", "shield", "bank", "bangladesh"), false),
        IconItem("BankCity", "BD Banks & MFS", listOf("city bank", "সিটি ব্যাংক", "cube", "red", "bank", "bangladesh"), false),
        IconItem("BankCitytouch", "BD Banks & MFS", listOf("citytouch", "city touch", "সিটি টাচ", "diamond", "bank", "bangladesh"), false),
        IconItem("BankSonali", "BD Banks & MFS", listOf("sonali bank", "সোনালী ব্যাংক", "sun", "wheat", "gold", "public bank", "bangladesh"), false),
        IconItem("BankSonaliSheba", "BD Banks & MFS", listOf("sonali sheba", "সোনালী সেবা", "star", "green", "bank", "bangladesh"), false),
        IconItem("BankEBL", "BD Banks & MFS", listOf("ebl", "eastern bank", "ইস্টার্ন ব্যাংক", "ribbon", "blue", "bank", "bangladesh"), false),
        IconItem("BankSkybanking", "BD Banks & MFS", listOf("skybanking", "ebl skybanking", "cloud", "vault", "bank", "bangladesh"), false),
        IconItem("BankSCB", "BD Banks & MFS", listOf("scb", "standard chartered", "স্ট্যান্ডার্ড চার্টার্ড", "foreign bank", "swirl", "blue", "green", "bank", "bangladesh"), false),
        IconItem("BankSCBAlt", "BD Banks & MFS", listOf("scb", "standard chartered", "স্ট্যান্ডার্ড চার্টার্ড", "navy", "badge", "helix", "bank", "bangladesh"), false),
        IconItem("BankHSBC", "BD Banks & MFS", listOf("hsbc", "এইচএসবিসি", "hexagon", "red", "white", "international", "bank", "bangladesh"), false),
        IconItem("BankHSBCAlt", "BD Banks & MFS", listOf("hsbc", "এইচএসবিসি", "dark", "badge", "bank", "bangladesh"), false),
        IconItem("BankPrime", "BD Banks & MFS", listOf("prime bank", "প্রাইম ব্যাংক", "altitude", "orange", "cyan", "bank", "bangladesh"), false),
        IconItem("BankPrimeAlt", "BD Banks & MFS", listOf("prime bank", "altitude", "badge", "bank", "bangladesh"), false),
        IconItem("BankUCB", "BD Banks & MFS", listOf("ucb", "united commercial bank", "ইউসিবি", "green", "gold", "shield", "bank", "bangladesh"), false),
        IconItem("BankUCBAlt", "BD Banks & MFS", listOf("ucb", "united commercial bank", "badge", "shield", "bank", "bangladesh"), false),
        IconItem("BankMTB", "BD Banks & MFS", listOf("mtb", "mutual trust bank", "এমটিবি", "triangle", "red", "green", "blue", "bank", "bangladesh"), false),
        IconItem("BankMTBAlt", "BD Banks & MFS", listOf("mtb", "mutual trust bank", "badge", "bank", "bangladesh"), false),
        IconItem("BankSoutheast", "BD Banks & MFS", listOf("southeast bank", "সাউথইস্ট ব্যাংক", "sebl", "teal", "diamond", "bank", "bangladesh"), false),
        IconItem("BankSoutheastAlt", "BD Banks & MFS", listOf("southeast bank", "badge", "teal", "bank", "bangladesh"), false),
        IconItem("BankPubali", "BD Banks & MFS", listOf("pubali bank", "পূবালী ব্যাংক", "banyan tree", "bot", "tree", "green", "bank", "bangladesh"), false),
        IconItem("BankPubaliAlt", "BD Banks & MFS", listOf("pubali bank", "tree", "gold", "badge", "bank", "bangladesh"), false),
        IconItem("BankDhaka", "BD Banks & MFS", listOf("dhaka bank", "ঢাকা ব্যাংক", "blue", "diamond", "monogram", "bank", "bangladesh"), false),
        IconItem("BankDhakaAlt", "BD Banks & MFS", listOf("dhaka bank", "badge", "navy", "cyan", "bank", "bangladesh"), false),
        IconItem("BankTrust", "BD Banks & MFS", listOf("trust bank", "ট্রাস্ট ব্যাংক", "army", "shield", "star", "bank", "bangladesh"), false),
        IconItem("BankTap", "BD Banks & MFS", listOf("tap", "ট্যাপ", "trust axiata pay", "mobile bank", "mfs", "yellow", "navy", "bangladesh"), false),
        IconItem("BankSureCash", "BD Banks & MFS", listOf("surecash", "সিওরক্যাশ", "mobile bank", "mfs", "purple", "cyan", "bangladesh"), false),
        IconItem("BankSureCashAlt", "BD Banks & MFS", listOf("surecash", "সিওরক্যাশ", "badge", "phone", "mfs", "bangladesh"), false),
        IconItem("BankAgrani", "BD Banks & MFS", listOf("agrani bank", "অগ্রণী ব্যাংক", "national", "green", "red", "wheat", "public bank", "bangladesh"), false),
        IconItem("BankAgraniAlt", "BD Banks & MFS", listOf("agrani bank", "badge", "gold", "bank", "bangladesh"), false),
        IconItem("BankJanata", "BD Banks & MFS", listOf("janata bank", "জনতা ব্যাংক", "crimson", "public bank", "bank", "bangladesh"), false),
        IconItem("BankJanataAlt", "BD Banks & MFS", listOf("janata bank", "badge", "red", "bank", "bangladesh"), false),
        IconItem("BankMCash", "BD Banks & MFS", listOf("mcash", "এমক্যাশ", "ibbl mcash", "islami bank", "mobile wallet", "mfs", "bangladesh"), false),
        IconItem("BankAB", "BD Banks & MFS", listOf("ab bank", "এবি ব্যাংক", "arab bangladesh bank", "navy", "red", "bank", "bangladesh"), false),
        IconItem("BankABAlt", "BD Banks & MFS", listOf("ab bank", "এবি ব্যাংক", "badge", "bank", "bangladesh"), false),

        // Finance & Accounts
        IconItem("AccountBalance", "Finance & Accounts", listOf("bank", "central bank", "institution", "governor", "finance", "ব্যাংক", "হিসাব"), false),
        IconItem("AccountBalanceWallet", "Finance & Accounts", listOf("wallet", "money", "cash", "funds", "pocket", "মানিব্যাগ", "নগদ"), false),
        IconItem("Wallet", "Finance & Accounts", listOf("purse", "pocket", "money", "leather", "মানিব্যাগ"), false),
        IconItem("Payments", "Finance & Accounts", listOf("cash", "bills", "currency", "transfer", "remittance", "টাকা", "ক্যাশ", "প্রদান"), false),
        IconItem("CreditCard", "Finance & Accounts", listOf("card", "debit", "visa", "mastercard", "amex", "কার্ড", "ক্রেডিট কার্ড"), false),
        IconItem("Savings", "Finance & Accounts", listOf("piggy bank", "invest", "deposit", "savings", "সঞ্চয়", "ডিপিএস"), false),
        IconItem("Paid", "Finance & Accounts", listOf("coin", "dollar", "taka", "payment", "received", "পেইড", "টাকা"), false),
        IconItem("Toll", "Finance & Accounts", listOf("fee", "tax", "charge", "toll bridge", "টোল", "ফি"), false),
        IconItem("Receipt", "Finance & Accounts", listOf("bill", "invoice", "slip", "voucher", "bank charge", "রিসিপ্ট", "বিল", "চার্জ"), false),
        IconItem("ReceiptLong", "Finance & Accounts", listOf("statement", "long bill", "invoice", "cheque", "চেক", "স্টেটমেন্ট"), true),
        IconItem("AttachMoney", "Finance & Accounts", listOf("dollar", "cash", "funds", "টাকা", "অর্থ"), false),
        IconItem("CurrencyExchange", "Finance & Accounts", listOf("convert", "forex", "trading", "exchange", "মুদ্রা বিনিময়"), false),
        IconItem("MonetizationOn", "Finance & Accounts", listOf("gold", "coin", "earnings", "revenue", "কমিশন", "টাকা"), false),
        IconItem("ShowChart", "Finance & Accounts", listOf("stock", "growth", "graph", "market", "trend", "investment", "শেয়ার", "বিনিয়োগ"), true),
        IconItem("Timeline", "Finance & Accounts", listOf("history", "trend", "tracking", "timeline", "রেকর্ড"), false),
        IconItem("TrendingUp", "Finance & Accounts", listOf("gain", "profit", "bullish", "increase", "dividend", "লাভ", "মুনাফা"), true),
        IconItem("TrendingDown", "Finance & Accounts", listOf("loss", "drop", "bearish", "decrease", "ক্ষতি", "লোকসান"), true),
        IconItem("Analytics", "Finance & Accounts", listOf("report", "metrics", "stats", "analysis", "অ্যানালিটিক্স"), false),
        IconItem("LocalAtm", "Finance & Accounts", listOf("atm", "cashout", "withdraw", "machine", "এটিএম", "ক্যাশ আউট"), false),
        IconItem("QrCode", "Finance & Accounts", listOf("scan", "bkash", "nagad", "payment", "qr", "কিউআর কোড"), false),
        IconItem("QrCode2", "Finance & Accounts", listOf("barcode", "scan", "pay", "qr code", "স্ক্যান"), false),
        IconItem("QrCodeScanner", "Finance & Accounts", listOf("scanner", "camera", "pay", "scanner", "স্ক্যানার"), false),
        IconItem("Sell", "Finance & Accounts", listOf("sale", "discount", "offer", "tag", "বিক্রি"), false),
        IconItem("PriceCheck", "Finance & Accounts", listOf("cost", "audit", "verify", "price check", "মূল্য যাচাই"), false),
        IconItem("PriceChange", "Finance & Accounts", listOf("rate", "fluctuation", "market", "change", "দর পরিবর্তন"), false),
        IconItem("AccountTree", "Finance & Accounts", listOf("structure", "hierarchy", "nodes", "branches", "হিসাবের তালিকা"), false),
        IconItem("Inventory", "Finance & Accounts", listOf("stock", "warehouse", "assets", "supplies", "মজুদ"), false),
        IconItem("PointOfSale", "Finance & Accounts", listOf("pos", "terminal", "register", "billing", "পস মেশিন"), false),
        IconItem("CardGiftcard", "Finance & Accounts", listOf("voucher", "gift", "bonus", "reward", "coupon", "উপহার", "ভাউচার"), false),
        IconItem("Redeem", "Finance & Accounts", listOf("claim", "coupon", "gift", "redeem", "রিডিম"), false),
        IconItem("Percent", "Finance & Accounts", listOf("interest", "percentage", "rate", "discount", "vat", "tax", "সুদ", "ভ্যাট", "ট্যাক্স"), false),
        IconItem("Calculate", "Finance & Accounts", listOf("calculator", "math", "accounting", "sum", "হিসাব", "গণনা"), false),
        IconItem("CreditScore", "Finance & Accounts", listOf("score", "rating", "cibil", "credit history", "ক্রেডিট স্কোর"), false),
        IconItem("RequestQuote", "Finance & Accounts", listOf("quote", "estimate", "bid", "proposal", "কোটেশন"), false),
        IconItem("AssuredWorkload", "Finance & Accounts", listOf("security", "audit", "compliance", "bank", "নিরীক্ষা"), false),
        IconItem("CurrencyBitcoin", "Finance & Accounts", listOf("crypto", "btc", "blockchain", "coin", "ক্রিপ্টো"), false),
        IconItem("Money", "Finance & Accounts", listOf("cash", "notes", "paper", "currency", "নগদ টাকা"), false),
        IconItem("MoneyOff", "Finance & Accounts", listOf("bad debt", "loss", "waived", "discount", "মন্দ ঋণ", "অনাদায়ী"), false),
        IconItem("AddCard", "Finance & Accounts", listOf("new card", "link card", "credit card", "কার্ড যুক্ত"), false),
        IconItem("CreditCardOff", "Finance & Accounts", listOf("block card", "disable card", "expired", "কার্ড ব্লক"), false),
        IconItem("Payment", "Finance & Accounts", listOf("checkout", "online pay", "gateway", "পেমেন্ট"), false),
        IconItem("Contactless", "Finance & Accounts", listOf("nfc", "tap", "wave", "contactless pay", "ট্যাপ পে"), false),
        IconItem("RequestPage", "Finance & Accounts", listOf("request", "invoice page", "billing doc", "চালান"), false),
        IconItem("Balance", "Finance & Accounts", listOf("scale", "justice", "equity", "balance sheet", "উদ্বৃত্ত"), false),
        IconItem("PieChart", "Finance & Accounts", listOf("pie", "chart", "breakdown", "distribution", "পাই চার্ট"), false),
        IconItem("BarChart", "Finance & Accounts", listOf("bars", "graph", "histogram", "comparison", "বার চার্ট"), false),
        IconItem("Sms", "Finance & Accounts", listOf("sms charges (bank)", "bank sms", "text message", "মেসেজ চার্জ"), false),
        IconItem("SwapHoriz", "Finance & Accounts", listOf("transfer charges", "fund transfer", "send money", "ট্রান্সফার চার্জ", "টাকা পাঠানো"), false),
        IconItem("Handshake", "Finance & Accounts", listOf("loan repayment", "parental debt", "deal", "debt", "ঋণ পরিশোধ", "কর্জ", "হাওলাত"), false),

        // Income & Earnings
        IconItem("Work", "Income & Earnings", listOf("salary", "wages", "office salary", "job", "বেতন", "চাকরি", "মাসিক বেতন"), false),
        IconItem("Stars", "Income & Earnings", listOf("bonus", "eid bonus", "performance bonus", "incentive", "বোনাস", "ঈদের বোনাস", "ইনসেনটিভ"), false),
        IconItem("Laptop", "Income & Earnings", listOf("freelancing", "upwork", "fiverr", "remote work", "client project", "ফ্রিল্যান্সিং", "প্রজেক্ট আয়"), false),
        IconItem("BusinessCenter", "Income & Earnings", listOf("business", "profit", "sales", "shop revenue", "ব্যবসা", "বিক্রয়", "মুনাফা"), false),
        IconItem("HomeWork", "Income & Earnings", listOf("rent received", "rental income", "house rent", "বাড়ি ভাড়া প্রাপ্তি", "ভাড়া আয়"), false),
        IconItem("MonetizationOn", "Income & Earnings", listOf("commission", "brokerage", "reward", "কমিশন", "দালালি"), false),
        IconItem("Replay", "Income & Earnings", listOf("refund", "cashback", "return money", "রিফান্ড", "ক্যাশব্যাক"), false),
        IconItem("School", "Income & Earnings", listOf("tuition", "scholarship", "teaching income", "টিউশনি", "বৃত্তি"), false),

        // Food & Groceries (including booster foods, staples, meat, fish, snacks)
        IconItem("ElectricBolt", "Food & Groceries", listOf("booster food", "booster", "energy food", "chia seed", "nuts", "peanuts", "nut mix", "superfood", "boost", "পুষ্টি", "বুস্টার ফুড", "চিয়া সিড", "বাদাম", "ছোলা"), false),
        IconItem("EnergySavingsLeaf", "Food & Groceries", listOf("honey", "garlic honey", "pure honey", "raw honey", "organic", "মধু", "রসুন মধু", "খাঁটি মধু"), false),
        IconItem("Egg", "Food & Groceries", listOf("egg", "duck egg", "farm egg", "poultry", "breakfast", "protein", "ডিম", "হাঁসের ডিম", "ফার্মের ডিম"), false),
        IconItem("EggAlt", "Food & Groceries", listOf("fried egg", "omelette", "poached egg", "ডিম ভাজা", "অমলেট"), false),
        IconItem("LocalGroceryStore", "Food & Groceries", listOf("groceries", "fixed mkt exp", "raw bazaar", "spices", "rice", "dal", "flour", "sugar", "salt", "মুদি", "কাঁচাবাজার", "চাল", "ডাল", "আটা", "লবণ", "চিনি", "মশলা"), false),
        IconItem("Phishing", "Food & Groceries", listOf("fish", "shrimp", "prawn", "hilsa", "rui", "dry fish", "shutki", "মাছ", "চিংড়ি", "চিংড়ি", "ইলিশ", "রুই", "শুটকি"), false),
        IconItem("Restaurant", "Food & Groceries", listOf("meat", "beef", "chicken", "mutton", "poultry", "dine", "eating", "meal", "food", "মাংস", "গরুর মাংস", "মুরগি", "খাসির মাংস"), false),
        IconItem("LocalGasStation", "Food & Groceries", listOf("cooking oil", "mustard oil", "soybean oil", "oil", "সরিষার তেল", "সয়াবিন তেল", "তৈল"), false),
        IconItem("SoupKitchen", "Food & Groceries", listOf("ghee", "butter", "cooking", "curry", "pot", "stew", "ঘি", "মাখন", "রান্না"), false),
        IconItem("RiceBowl", "Food & Groceries", listOf("polao rice", "biryani", "rice bowl", "cooked rice", "পোলাও চাল", "বাসমতি", "ভাত", "বিরিয়ানি"), false),
        IconItem("Eco", "Food & Groceries", listOf("fruits", "apple", "banana", "mango", "dates", "khejur", "isabgol", "tokma", "organic", "ফলমূল", "ফল", "আপেল", "কলা", "আম", "খেজুর", "ইসবগুল"), false),
        IconItem("Grass", "Food & Groceries", listOf("vegetables", "spinach", "potato", "onion", "garlic", "ginger", "fresh veggies", "সবজি", "শাক", "আলু", "পেঁয়াজ", "রসুন"), false),
        IconItem("BakeryDining", "Food & Groceries", listOf("semai", "vermicelli", "bread", "biscuit", "toast", "bakery", "powder milk", "curd", "দুধ", "দই", "সেমাই", "পাউরুটি"), false),
        IconItem("LocalCafe", "Food & Groceries", listOf("coffee", "coffee mate", "espresso", "cappuccino", "cafe", "কফি"), false),
        IconItem("EmojiFoodBeverage", "Food & Groceries", listOf("tea", "milk tea", "green tea", "hot tea", "cha", "চা", "দুধ চা", "লাল চা"), false),
        IconItem("LocalDrink", "Food & Groceries", listOf("water", "mineral water", "bottled water", "juice", "beverage", "soft drink", "coke", "পানি", "জুস", "পানীয়"), false),
        IconItem("DinnerDining", "Food & Groceries", listOf("dinner", "restaurant", "kacchi", "biryani", "evening meal", "রাতের খাবার", "রেস্তোরাঁ", "কাচ্চি"), false),
        IconItem("LunchDining", "Food & Groceries", listOf("lunch", "meal", "burger", "afternoon", "দুপুরের খাবার"), false),
        IconItem("BreakfastDining", "Food & Groceries", listOf("breakfast", "morning food", "egg", "toast", "সকালের নাস্তা"), false),
        IconItem("Fastfood", "Food & Groceries", listOf("burger", "fries", "junk food", "snack", "singara", "samucha", "fuchka", "chotpoti", "বার্গার", "সিংগারা", "ফুচকা", "চটপটি"), false),
        IconItem("LocalPizza", "Food & Groceries", listOf("pizza", "slice", "cheese", "crust", "পিৎজা"), false),
        IconItem("RamenDining", "Food & Groceries", listOf("noodles", "pasta", "soup", "ramen", "নুডলস", "পাস্তা"), false),
        IconItem("Icecream", "Food & Groceries", listOf("ice cream", "dessert", "cone", "sweet", "gelato", "আইসক্রিম"), false),
        IconItem("Cake", "Food & Groceries", listOf("cake", "birthday cake", "pastry", "sweets", "মিষ্টি", "কেক"), false),
        IconItem("Cookie", "Food & Groceries", listOf("biscuit", "cookie", "chanachur", "chips", "muri", "nuts", "বিস্কুট", "চানাচুর", "চিপস", "মুড়ি"), false),
        IconItem("KebabDining", "Food & Groceries", listOf("grill", "bbq", "kebab", "skewer", "কাবাব", "গ্রিল"), false),
        IconItem("OutdoorGrill", "Food & Groceries", listOf("barbecue", "steak", "roast", "grill", "বারবিকিউ"), false),
        IconItem("TakeoutDining", "Food & Groceries", listOf("parcel", "delivery", "pack", "takeout", "পার্সেল খাবার"), false),
        IconItem("DeliveryDining", "Food & Groceries", listOf("food delivery", "foodpanda", "rider", "courier", "ফুডপান্ডা"), false),
        IconItem("Kitchen", "Food & Groceries", listOf("fridge", "home food", "grocery", "cooking", "রান্নাঘর"), false),
        IconItem("FoodBank", "Food & Groceries", listOf("charity food", "relief ration", "ত্রাণ খাবার"), false),
        IconItem("LocalDining", "Food & Groceries", listOf("eat", "plate", "fork", "restaurant", "খাবার প্লেট"), false),
        IconItem("Bento", "Food & Groceries", listOf("box", "lunchbox", "tiffin", "টিফিন বক্স"), false),
        IconItem("Flatware", "Food & Groceries", listOf("fork", "spoon", "cutlery", "utensils", "চামচ", "কাটাচামচ"), false),

        // Health & Wellness (care essentials, toiletries, medicines, doctors)
        IconItem("CleanHands", "Health & Wellness", listOf("care essentials", "care essential", "toiletries", "hygiene", "পরিচ্ছন্নতা", "হাত ধোয়া", "যত্ন"), false),
        IconItem("Sanitizer", "Health & Wellness", listOf("sanitizer", "hand rub", "disinfectant", "জীবাণুনাশক", "স্যানিটাইজার"), false),
        IconItem("Soap", "Health & Wellness", listOf("soap", "bath soap", "shampoo", "handwash", "toothpaste", "brush", "razor", "সাবান", "শ্যাম্পু", "টুথপেস্ট", "ব্রাশ"), false),
        IconItem("Spa", "Health & Wellness", listOf("skincare", "facewash", "face wash", "lotion", "body lotion", "cream", "beauty", "cosmetics", "ত্বকের যত্ন", "লোশন", "ফেসওয়াশ", "রূপচর্চা"), false),
        IconItem("AutoFixHigh", "Health & Wellness", listOf("beauty", "glow", "cosmetics", "মেকআপ", "সৌন্দর্য"), false),
        IconItem("Medication", "Health & Wellness", listOf("medicine", "medicines & pharmacy", "drugs", "tablets", "syrup", "capsules", "ওষুধ", "ঔষধ", "ফার্মেসি", "ট্যাবলেট", "সিরাপ"), false),
        IconItem("LocalHospital", "Health & Wellness", listOf("doctor fees", "doctor", "patient visit", "hospital", "clinic", "ডাক্তার ফি", "রোগী দেখা", "হাসপাতাল"), false),
        IconItem("Biotech", "Health & Wellness", listOf("medical test", "lab test", "pathology", "blood test", "ডায়াগনস্টিক", "রক্ত পরীক্ষা", "ল্যাব"), false),
        IconItem("HealthAndSafety", "Health & Wellness", listOf("sanitary pad", "pad", "first aid", "safety", "প্যাড", "স্যানিটারি প্যাড", "প্রাথমিক চিকিৎসা"), false),
        IconItem("MedicalServices", "Health & Wellness", listOf("oral saline", "saline", "bandaid", "clinic", "স্যালাইন", "ব্যান্ডেজ"), false),
        IconItem("MonitorHeart", "Health & Wellness", listOf("heart", "ecg", "cardiology", "pulse", "হৃদযন্ত্র"), false),
        IconItem("Favorite", "Health & Wellness", listOf("sexual wellness", "wellness", "contraceptive", "family planning", "কনডম", "যৌন স্বাস্থ্য"), false),
        IconItem("LocalPharmacy", "Health & Wellness", listOf("pharmacy", "drugstore", "chemist", "ফার্মেসি"), false),
        IconItem("Vaccines", "Health & Wellness", listOf("vaccine", "injection", "dose", "টিকা", "ইনজেকশন"), false),
        IconItem("Healing", "Health & Wellness", listOf("bandage", "wound care", "recovery", "নিরাময়"), false),
        IconItem("FitnessCenter", "Health & Wellness", listOf("gym", "workout", "weights", "fitness gear", "exercise", "ব্যায়াম", "জিম"), false),
        IconItem("SelfImprovement", "Health & Wellness", listOf("yoga", "meditation", "mindfulness", "যোগব্যায়াম", "ধ্যান"), false),

        // Shopping & Fashion (clothing, tailoring, shoes, accessories)
        IconItem("Checkroom", "Shopping & Fashion", listOf("clothes", "shirt", "t-shirt", "pant", "jeans", "punjabi", "sharee", "dress", "fashion", "পোশাক", "কাপড়", "শার্ট", "প্যান্ট", "পাঞ্জাবি", "শাড়ি"), false),
        IconItem("ShoppingBag", "Shopping & Fashion", listOf("shoes", "sandals", "sneakers", "slippers", "shopping haul", "জুতা", "স্যান্ডেল", "জুতো"), false),
        IconItem("ContentCut", "Shopping & Fashion", listOf("hair cuts", "haircut", "tailoring", "tailor", "barber", "scissors", "salon", "চুল কাটা", "টেইলারিং", "দর্জি", "সেলাই মজুরি"), false),
        IconItem("Watch", "Shopping & Fashion", listOf("wrist watch", "watch", "smartwatch", "clock", "ঘড়ি", "হাতঘড়ি"), false),
        IconItem("Diamond", "Shopping & Fashion", listOf("jewelry", "gold", "silver", "ring", "bangle", "earring", "গহনা", "স্বর্ণ", "চুড়ি", "অলঙ্কার"), false),
        IconItem("DryCleaning", "Shopping & Fashion", listOf("dry cleaning", "laundry", "tissue", "laundry wash", "ড্রাই ওয়াশ", "লন্ড্রি"), false),
        IconItem("ShoppingCart", "Shopping & Fashion", listOf("cart", "buy", "supermarket", "store", "shopping", "কেনাকাটা", "বাজার"), false),
        IconItem("ShoppingCartCheckout", "Shopping & Fashion", listOf("checkout", "order", "purchase", "অর্ডার"), false),
        IconItem("ShoppingBasket", "Shopping & Fashion", listOf("basket", "goods", "shop", "market", "ঝুড়ি"), false),
        IconItem("Store", "Shopping & Fashion", listOf("shop", "vendor", "outlet", "retail", "দোকান"), false),
        IconItem("Storefront", "Shopping & Fashion", listOf("boutique", "showroom", "shop front", "শোরুম"), false),
        IconItem("LocalMall", "Shopping & Fashion", listOf("shopping mall", "plaza", "center", "শপিং মল"), false),
        IconItem("Backpack", "Shopping & Fashion", listOf("bag", "travel bag", "hiking", "school bag", "ব্যাগ"), false),
        IconItem("Style", "Shopping & Fashion", listOf("socks", "tag", "fashion", "brand", "style", "মোজা", "স্টাইল"), false),
        IconItem("LocalOffer", "Shopping & Fashion", listOf("deal", "discount", "coupon", "promo", "অফার"), false),
        IconItem("Discount", "Shopping & Fashion", listOf("voucher", "percent off", "markdown", "ডিসকাউন্ট"), false),

        // Bills, Housing & Utilities
        IconItem("Home", "Bills & Housing", listOf("house rent", "home rent", "flat rent", "apartment rent", "বাড়ি ভাড়া", "বাসা ভাড়া"), false),
        IconItem("ElectricBolt", "Bills & Housing", listOf("electricity", "power bill", "current bill", "prepaid meter", "desco", "dpdc", "reb", "বিদ্যুৎ বিল", "কারেন্ট বিল"), false),
        IconItem("GasMeter", "Bills & Housing", listOf("gas bill", "titas gas", "cylinder", "গ্যাস বিল"), false),
        IconItem("Propane", "Bills & Housing", listOf("gas cylinder", "lpg", "bashundhara gas", "omera", "সিলিন্ডার", "এলপিজি"), false),
        IconItem("WaterDrop", "Bills & Housing", listOf("water bill", "wasa", "sewerage", "পানি বিল", "ওয়াসা বিল"), false),
        IconItem("Wifi", "Bills & Housing", listOf("wifi bill", "broadband", "internet bill", "fiber", "ওয়াইফাই বিল", "ইন্টারনেট বিল"), false),
        IconItem("Router", "Bills & Housing", listOf("wifi router", "modem", "রাউটার"), false),
        IconItem("CleaningServices", "Bills & Housing", listOf("clean bill", "cleaning", "waste bill", "garbage", "ময়লা বিল", "পরিষ্কার বিল"), false),
        IconItem("Lightbulb", "Bills & Housing", listOf("bulb", "led light", "lighting", "torch", "বাল্ব", "বাতি"), false),
        IconItem("BatteryChargingFull", "Bills & Housing", listOf("ips battery", "solar battery", "battery", "আইপিএস ব্যাটারি", "ব্যাটারি"), false),
        IconItem("Plumbing", "Bills & Housing", listOf("plumbing", "water filter", "purifier", "pipe repair", "water tap", "পানির ফিল্টার", "প্লাম্বিং"), false),
        IconItem("Lock", "Bills & Housing", listOf("door lock", "padlock", "lock", "তালা"), false),
        IconItem("Chair", "Bills & Housing", listOf("furniture", "sofa", "table", "chair", "bed", "almirah", "ফার্নিচার", "আসবাবপত্র"), false),
        IconItem("Palette", "Bills & Housing", listOf("home decor", "paint", "wall decor", "ঘর সাজানো", "রং"), false),
        IconItem("Kitchen", "Bills & Housing", listOf("household items", "kitchen items", "গৃহস্থালি"), false),
        IconItem("PestControl", "Bills & Housing", listOf("mosquito coil", "pest control", "insect spray", "মশার কয়েল", "কীটনাশক"), false),
        IconItem("Power", "Bills & Housing", listOf("utility bill", "power socket", "বিদ্যুৎ সংযোগ"), false),
        IconItem("Apartment", "Bills & Housing", listOf("building", "flat", "apartment", "অ্যাপার্টমেন্ট"), false),

        // Education & Study
        IconItem("School", "Education & Study", listOf("students", "study", "exam", "school", "college", "university", "tuition", "স্কুল বেতন", "টিউশন ফি", "শিক্ষার্থী"), false),
        IconItem("AutoStories", "Education & Study", listOf("books", "story", "teaching materials", "reading", "textbook", "বই", "শিক্ষা উপকরণ"), false),
        IconItem("MenuBook", "Education & Study", listOf("books", "study", "syllabus", "guide", "গাইড বই"), true),
        IconItem("Class", "Education & Study", listOf("classroom", "lecture", "course", "ক্লাস"), false),
        IconItem("CastForEducation", "Education & Study", listOf("online class", "study", "e-learning", "অনলাইন ক্লাস"), false),
        IconItem("HistoryEdu", "Education & Study", listOf("certificate", "degree", "diploma", "সার্টিফিকেট"), false),
        IconItem("DriveFileRenameOutline", "Education & Study", listOf("pen", "study essentials", "signature", "stationery", "কলম", "খাতা", "স্টেশনারি"), false),
        IconItem("Edit", "Education & Study", listOf("pencil", "notes", "নোট", "পেন্সিল"), false),
        IconItem("Assignment", "Education & Study", listOf("application fees", "exam fee", "admission form", "আবেদন ফি", "ভর্তি ফরম"), true),
        IconItem("FactCheck", "Education & Study", listOf("test", "exam test", "verification", "পরীক্ষা"), true),
        IconItem("Badge", "Education & Study", listOf("id card", "teacher id", "student id", "আইডি কার্ড"), false),

        // Transport & Travel
        IconItem("DirectionsBus", "Transport & Travel", listOf("bus fare", "local bus", "intercity bus", "বাস ভাড়া"), false),
        IconItem("Commute", "Transport & Travel", listOf("train ticket", "metro rail", "rickshaw", "cng", "ভাড়া", "ট্রেন", "মেট্রোরেল", "রিকশা", "সিএনজি"), false),
        IconItem("DirectionsCar", "Transport & Travel", listOf("taxi", "car rental", "uber", "pathao car", "গাড়ি", "উবার", "ট্যাক্সি"), false),
        IconItem("TwoWheeler", "Transport & Travel", listOf("motorcycle", "bike", "pathao ride", "ride sharing", "মোটরসাইকেল", "বাইক", "পাঠাও"), false),
        IconItem("LocalGasStation", "Transport & Travel", listOf("fuel", "petrol", "octane", "diesel", "mobil", "engine oil", "জ্বালানি", "পেট্রোল", "অকটেন", "ডিজেল", "মবিল"), false),
        IconItem("TireRepair", "Transport & Travel", listOf("tire repair", "puncture", "bike servicing", "car repair", "টায়ার মেরামত", "পাংচার"), false),
        IconItem("Flight", "Transport & Travel", listOf("flight ticket", "airplane", "airfare", "বিমান টিকিট"), false),
        IconItem("FlightTakeoff", "Transport & Travel", listOf("tour", "vacation travel", "trip", "ট্যুর", "ভ্রমণ"), false),
        IconItem("Hotel", "Transport & Travel", listOf("hotel booking", "resort", "lodge", "হোটেল"), false),
        IconItem("Luggage", "Transport & Travel", listOf("luggage", "travel suitcase", "লাগেজ"), false),
        IconItem("CarRepair", "Transport & Travel", listOf("vehicle servicing", "mechanic", "গাড়ি সার্ভিসিং"), false),

        // Islamic & Charity
        IconItem("VolunteerActivism", "Islamic & Charity", listOf("charity", "zakat", "sadqah", "fitra", "mosque donation", "relief", "দান", "যাকাত", "সদকা", "ফিতরা", "অনুদান"), false),
        IconItem("AccountBalance", "Islamic & Charity", listOf("mosque donation", "madrasha", "imam hadiya", "muazzin", "মসজিদ", "ইমাম হাদিয়া"), false),
        IconItem("Pets", "Islamic & Charity", listOf("qurbani", "sacrificial cow", "goat", "কোরবানি গরু", "খাসি"), false),
        IconItem("Paid", "Islamic & Charity", listOf("salami", "eid salami", "eidi", "সালামি", "ঈদি"), false),
        IconItem("Handshake", "Islamic & Charity", listOf("loan repayment", "karz hasana", "parental debt", "ঋণ পরিশোধ", "কর্জ"), false),
        IconItem("VolumeUp", "Islamic & Charity", listOf("mahfil", "waz", "islamic lecture", "মাহফিল", "ওয়াজ"), true),

        // Celebrations & Social
        IconItem("CardGiftcard", "Celebrations & Social", listOf("gifts & presents", "treat", "presents", "wedding gift", "উপহার", "ট্রিট"), false),
        IconItem("Cake", "Celebrations & Social", listOf("birthday", "anniversary", "sweets", "জন্মদিন", "কেক"), false),
        IconItem("Celebration", "Celebrations & Social", listOf("celebrations", "farewell ceremony", "eid fest", "party", "অনুষ্ঠান", "উৎসব", "বিদায়"), false),
        IconItem("Festival", "Celebrations & Social", listOf("festival", "fair", "উৎসব", "মেলা"), false),
        IconItem("Park", "Celebrations & Social", listOf("picnic", "outing", "family park", "বনভোজন", "পিকনিক"), false),
        IconItem("Diversity2", "Celebrations & Social", listOf("friends hangout", "adda", "social meetup", "বন্ধু", "আড্ডা"), false),
        IconItem("FamilyRestroom", "Celebrations & Social", listOf("parental debt", "family support", "parents", "পিতামাতা", "পরিবার"), false),
        IconItem("Payments", "Celebrations & Social", listOf("cash given", "pocket money", "allowance", "হাত খরচ", "পকেট মানি"), false),

        // Repairs & Maintenance
        IconItem("Build", "Repairs & Maintenance", listOf("repairs", "maintenance", "tool", "servicing", "মেরামত", "সার্ভিসিং"), false),
        IconItem("Handyman", "Repairs & Maintenance", listOf("shoe repair", "repairman", "মিস্ত্রি", "জুতা মেরামত"), false),
        IconItem("Hardware", "Repairs & Maintenance", listOf("hardware", "tools", "screws", "যন্ত্রপাতি"), false),
        IconItem("PhoneAndroid", "Repairs & Maintenance", listOf("mobile repair", "mobile screen", "display change", "মোবাইল মেরামত"), false),
        IconItem("TwoWheeler", "Repairs & Maintenance", listOf("motorcycle repair", "bike servicing", "বাইক মেরামত"), false),
        IconItem("Plumbing", "Repairs & Maintenance", listOf("plumbing", "pipe fix", "water tap repair", "প্লাম্বিং মেরামত"), false),
        IconItem("ElectricalServices", "Repairs & Maintenance", listOf("electrician", "wiring fix", "electrical repair", "ইলেকট্রিক মেরামত"), false),

        // Tech, Gadgets & Tools
        IconItem("PhoneAndroid", "Tech & Tools", listOf("smartphone", "android phone", "mobile recharge", "মোবাইল", "স্মার্টফোন", "রিচার্জ"), false),
        IconItem("PhoneIphone", "Tech & Tools", listOf("apple", "iphone", "ios", "আইফোন"), false),
        IconItem("Computer", "Tech & Tools", listOf("pc", "computer", "desktop", "laptop", "কম্পিউটার", "পিসি"), false),
        IconItem("Tablet", "Tech & Tools", listOf("ipad", "android tablet", "ট্যাবলেট"), false),
        IconItem("Devices", "Tech & Tools", listOf("devices", "gadgets", "charger", "earbuds", "accessories", "গ্যাজেট", "যন্ত্রপাতি"), false),
        IconItem("Subscriptions", "Tech & Tools", listOf("apps & subscriptions", "netflix", "youtube", "software", "সাবস্ক্রিপশন"), false),
        IconItem("Headphones", "Tech & Tools", listOf("headphones", "earphones", "headset", "হেডফোন"), false),
        IconItem("Bluetooth", "Tech & Tools", listOf("wireless", "bluetooth", "ব্লুটুথ"), false),
        IconItem("SimCard", "Tech & Tools", listOf("sim card", "esim", "সিম কার্ড"), false),
        IconItem("SdCard", "Tech & Tools", listOf("memory card", "storage", "মেমোরি কার্ড"), false),
        IconItem("Usb", "Tech & Tools", listOf("pen drive", "usb cable", "পেন ড্রাইভ"), false),
        IconItem("Security", "Tech & Tools", listOf("security", "antivirus", "নিরাপত্তা"), false),
        IconItem("Code", "Tech & Tools", listOf("programming", "coding", "software", "প্রোগ্রামিং"), false),

        // Life & Work
        IconItem("Work", "Life & Work", listOf("office", "job", "workplace", "চাকরি", "অফিস"), false),
        IconItem("People", "Life & Work", listOf("colleagues", "team", "people", "সহকর্মী"), false),
        IconItem("SupportAgent", "Life & Work", listOf("customer care", "helpdesk", "সাপোর্ট"), false),
        IconItem("Desk", "Life & Work", listOf("office desk", "workstation", "ডেস্ক"), false),

        // Entertainment & Sports
        IconItem("Movie", "Entertainment & Sports", listOf("cinema", "hall", "movie", "film", "সিনেমা"), false),
        IconItem("MusicNote", "Entertainment & Sports", listOf("song", "music", "audio", "গান"), false),
        IconItem("SportsEsports", "Entertainment & Sports", listOf("gaming", "video games", "গেমিং"), false),
        IconItem("SportsCricket", "Entertainment & Sports", listOf("cricket", "bat", "ball", "ক্রিকেট"), false),
        IconItem("SportsSoccer", "Entertainment & Sports", listOf("football", "soccer", "ফুটবল"), false),
        IconItem("SportsTennis", "Entertainment & Sports", listOf("badminton", "tennis", "টেনিস", "ব্যাডমিন্টন"), false),

        // Nature & Weather
        IconItem("Eco", "Nature & Weather", listOf("plants", "gardening", "green", "গাছপালা", "বাগান"), false),
        IconItem("Forest", "Nature & Weather", listOf("trees", "forest", "বন"), false),
        IconItem("WbSunny", "Nature & Weather", listOf("sun", "day", "রোদ"), false),

        // Symbols
        IconItem("Category", "Symbols", listOf("category", "group", "folder", "বিভাগ"), false),
        IconItem("Star", "Symbols", listOf("favorite", "important", "স্টার"), false),
        IconItem("CheckCircle", "Symbols", listOf("completed", "done", "সম্পন্ন"), false),
        IconItem("Warning", "Symbols", listOf("warning", "alert", "সতর্কতা"), false),
        IconItem("Notifications", "Symbols", listOf("notification", "bell", "নোটিফিকেশন"), false)
    )

    /**
     * Resolves an ImageVector by iconName.
     */
    fun getIconByName(iconName: String?): ImageVector {
        if (iconName == null) return Icons.Default.Category
        return when (iconName) {
            "AccountBalance" -> Icons.Default.AccountBalance
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "Wallet" -> Icons.Default.Wallet
            "Payments" -> Icons.Default.Payments
            "CreditCard" -> Icons.Default.CreditCard
            "Savings" -> Icons.Default.Savings
            "Paid" -> Icons.Default.Paid
            "Toll" -> Icons.Default.Toll
            "Receipt" -> Icons.Default.Receipt
            "ReceiptLong" -> Icons.AutoMirrored.Filled.ReceiptLong
            "AttachMoney" -> Icons.Default.AttachMoney
            "CurrencyExchange" -> Icons.Default.CurrencyExchange
            "MonetizationOn" -> Icons.Default.MonetizationOn
            "ShowChart" -> Icons.AutoMirrored.Filled.ShowChart
            "Timeline" -> Icons.Default.Timeline
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
            "TrendingDown" -> Icons.AutoMirrored.Filled.TrendingDown
            "Analytics" -> Icons.Default.Analytics
            "LocalAtm" -> Icons.Default.LocalAtm
            "QrCode" -> Icons.Default.QrCode
            "QrCode2" -> Icons.Default.QrCode2
            "QrCodeScanner" -> Icons.Default.QrCodeScanner
            "Sell" -> Icons.Default.Sell
            "PriceCheck" -> Icons.Default.PriceCheck
            "PriceChange" -> Icons.Default.PriceChange
            "AccountTree" -> Icons.Default.AccountTree
            "Inventory" -> Icons.Default.Inventory
            "PointOfSale" -> Icons.Default.PointOfSale
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Redeem" -> Icons.Default.Redeem
            "Percent" -> Icons.Default.Percent
            "Calculate" -> Icons.Default.Calculate
            "CreditScore" -> Icons.Default.CreditScore
            "RequestQuote" -> Icons.Default.RequestQuote
            "AssuredWorkload" -> Icons.Default.AssuredWorkload
            "CurrencyBitcoin" -> Icons.Default.CurrencyBitcoin
            "CurrencyYen" -> Icons.Default.CurrencyYen
            "CurrencyPound" -> Icons.Default.CurrencyPound
            "CurrencyRuble" -> Icons.Default.CurrencyRuble
            "CurrencyFranc" -> Icons.Default.CurrencyFranc
            "CurrencyLira" -> Icons.Default.CurrencyLira
            "CurrencyRupee" -> Icons.Default.CurrencyRupee
            "CurrencyYuan" -> Icons.Default.CurrencyYuan
            "Euro" -> Icons.Default.Euro
            "Money" -> Icons.Default.Money
            "MoneyOff" -> Icons.Default.MoneyOff
            "AddCard" -> Icons.Default.AddCard
            "CreditCardOff" -> Icons.Default.CreditCardOff
            "Payment" -> Icons.Default.Payment
            "Contactless" -> Icons.Default.Contactless
            "RequestPage" -> Icons.Default.RequestPage
            "Balance" -> Icons.Default.Balance
            "PieChart" -> Icons.Default.PieChart
            "QueryStats" -> Icons.Default.QueryStats
            "BarChart" -> Icons.Default.BarChart
            "DonutSmall" -> Icons.Default.DonutSmall
            "DonutLarge" -> Icons.Default.DonutLarge
            "InsertChart" -> Icons.Default.InsertChart
            "RealEstateAgent" -> Icons.Default.RealEstateAgent
            "Subscriptions" -> Icons.Default.Subscriptions
            "LocalShipping" -> Icons.Default.LocalShipping
            "CellTower" -> Icons.Default.CellTower
            "SupportAgent" -> Icons.Default.SupportAgent
            "Restaurant" -> Icons.Default.Restaurant
            "DinnerDining" -> Icons.Default.DinnerDining
            "LunchDining" -> Icons.Default.LunchDining
            "BreakfastDining" -> Icons.Default.BreakfastDining
            "LocalCafe" -> Icons.Default.LocalCafe
            "Coffee" -> Icons.Default.Coffee
            "CoffeeMaker" -> Icons.Default.CoffeeMaker
            "Fastfood" -> Icons.Default.Fastfood
            "LocalPizza" -> Icons.Default.LocalPizza
            "BakeryDining" -> Icons.Default.BakeryDining
            "RamenDining" -> Icons.Default.RamenDining
            "Icecream" -> Icons.Default.Icecream
            "Cake" -> Icons.Default.Cake
            "LocalBar" -> Icons.Default.LocalBar
            "Liquor" -> Icons.Default.Liquor
            "WineBar" -> Icons.Default.WineBar
            "SetMeal" -> Icons.Default.SetMeal
            "TakeoutDining" -> Icons.Default.TakeoutDining
            "BrunchDining" -> Icons.Default.BrunchDining
            "SoupKitchen" -> Icons.Default.SoupKitchen
            "Kitchen" -> Icons.Default.Kitchen
            "LocalGroceryStore" -> Icons.Default.LocalGroceryStore
            "FoodBank" -> Icons.Default.FoodBank
            "LocalDining" -> Icons.Default.LocalDining
            "MenuBook" -> Icons.AutoMirrored.Filled.MenuBook
            "Bento" -> Icons.Default.Bento
            "Tapas" -> Icons.Default.Tapas
            "LocalDrink" -> Icons.Default.LocalDrink
            "Flatware" -> Icons.Default.Flatware
            "Dining" -> Icons.Default.Dining
            "Egg" -> Icons.Default.Egg
            "EggAlt" -> Icons.Default.EggAlt
            "KebabDining" -> Icons.Default.KebabDining
            "RiceBowl" -> Icons.Default.RiceBowl
            "OutdoorGrill" -> Icons.Default.OutdoorGrill
            "EmojiFoodBeverage" -> Icons.Default.EmojiFoodBeverage
            "RoomService" -> Icons.Default.RoomService
            "Cookie" -> Icons.Default.Cookie
            "RestaurantMenu" -> Icons.Default.RestaurantMenu
            "FreeBreakfast" -> Icons.Default.FreeBreakfast
            "DeliveryDining" -> Icons.Default.DeliveryDining
            "SportsBar" -> Icons.Default.SportsBar
            "Nightlife" -> Icons.Default.Nightlife
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "ShoppingCartCheckout" -> Icons.Default.ShoppingCartCheckout
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "ShoppingBasket" -> Icons.Default.ShoppingBasket
            "Store" -> Icons.Default.Store
            "Storefront" -> Icons.Default.Storefront
            "LocalMall" -> Icons.Default.LocalMall
            "Checkroom" -> Icons.Default.Checkroom
            "Diamond" -> Icons.Default.Diamond
            "Watch" -> Icons.Default.Watch
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "Toys" -> Icons.Default.Toys
            "Backpack" -> Icons.Default.Backpack
            "Style" -> Icons.Default.Style
            "Loyalty" -> Icons.Default.Loyalty
            "LocalOffer" -> Icons.Default.LocalOffer
            "Discount" -> Icons.Default.Discount
            "Inventory2" -> Icons.Default.Inventory2
            "ProductionQuantityLimits" -> Icons.Default.ProductionQuantityLimits
            "AddShoppingCart" -> Icons.Default.AddShoppingCart
            "RemoveShoppingCart" -> Icons.Default.RemoveShoppingCart
            "Shop" -> Icons.Default.Shop
            "Shop2" -> Icons.Default.Shop2
            "ShopTwo" -> Icons.Default.ShopTwo
            "CardMembership" -> Icons.Default.CardMembership
            "Outbox" -> Icons.Default.Outbox
            "Inbox" -> Icons.Default.Inbox
            "AllInbox" -> Icons.Default.AllInbox
            "AutoFixHigh" -> Icons.Default.AutoFixHigh
            "DryCleaning" -> Icons.Default.DryCleaning
            "AddBusiness" -> Icons.Default.AddBusiness
            "LocalFlorist" -> Icons.Default.LocalFlorist
            "LocalConvenienceStore" -> Icons.Default.LocalConvenienceStore
            "LocalLaundryService" -> Icons.Default.LocalLaundryService
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Train" -> Icons.Default.Train
            "TwoWheeler" -> Icons.Default.TwoWheeler
            "DirectionsBike" -> Icons.AutoMirrored.Filled.DirectionsBike
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "DirectionsWalk" -> Icons.AutoMirrored.Filled.DirectionsWalk
            "DirectionsRun" -> Icons.AutoMirrored.Filled.DirectionsRun
            "Flight" -> Icons.Default.Flight
            "FlightTakeoff" -> Icons.Default.FlightTakeoff
            "FlightLand" -> Icons.Default.FlightLand
            "LocalTaxi" -> Icons.Default.LocalTaxi
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "ElectricCar" -> Icons.Default.ElectricCar
            "EvStation" -> Icons.Default.EvStation
            "Commute" -> Icons.Default.Commute
            "Subway" -> Icons.Default.Subway
            "Tram" -> Icons.Default.Tram
            "DirectionsBoat" -> Icons.Default.DirectionsBoat
            "Sailing" -> Icons.Default.Sailing
            "Hotel" -> Icons.Default.Hotel
            "Luggage" -> Icons.Default.Luggage
            "Map" -> Icons.Default.Map
            "Navigation" -> Icons.Default.Navigation
            "Explore" -> Icons.Default.Explore
            "DirectionsSubway" -> Icons.Default.DirectionsSubway
            "DirectionsRailway" -> Icons.Default.DirectionsRailway
            "DirectionsTransit" -> Icons.Default.DirectionsTransit
            "ElectricBike" -> Icons.Default.ElectricBike
            "ElectricMoped" -> Icons.Default.ElectricMoped
            "ElectricScooter" -> Icons.Default.ElectricScooter
            "PedalBike" -> Icons.Default.PedalBike
            "Moped" -> Icons.Default.Moped
            "Motorcycle" -> Icons.Default.Motorcycle
            "AirportShuttle" -> Icons.Default.AirportShuttle
            "CarRental" -> Icons.Default.CarRental
            "CarRepair" -> Icons.Default.CarRepair
            "CarCrash" -> Icons.Default.CarCrash
            "TireRepair" -> Icons.Default.TireRepair
            "LocalParking" -> Icons.Default.LocalParking
            "Traffic" -> Icons.Default.Traffic
            "Speed" -> Icons.Default.Speed
            "CompassCalibration" -> Icons.Default.CompassCalibration
            "PinDrop" -> Icons.Default.PinDrop
            "LocationOn" -> Icons.Default.LocationOn
            "LocationSearching" -> Icons.Default.LocationSearching
            "NearMe" -> Icons.Default.NearMe
            "ShareLocation" -> Icons.Default.ShareLocation
            "TransferWithinAStation" -> Icons.Default.TransferWithinAStation
            "FlightClass" -> Icons.Default.FlightClass
            "Airlines" -> Icons.Default.Airlines
            "ConnectingAirports" -> Icons.Default.ConnectingAirports
            "Home" -> Icons.Default.Home
            "Apartment" -> Icons.Default.Apartment
            "Cottage" -> Icons.Default.Cottage
            "Villa" -> Icons.Default.Villa
            "LocationCity" -> Icons.Default.LocationCity
            "ElectricBolt" -> Icons.Default.ElectricBolt
            "Power" -> Icons.Default.Power
            "WaterDrop" -> Icons.Default.WaterDrop
            "Plumbing" -> Icons.Default.Plumbing
            "Wifi" -> Icons.Default.Wifi
            "Router" -> Icons.Default.Router
            "Tv" -> Icons.Default.Tv
            "DesktopWindows" -> Icons.Default.DesktopWindows
            "Laptop" -> Icons.Default.Laptop
            "Chair" -> Icons.Default.Chair
            "Bed" -> Icons.Default.Bed
            "Lightbulb" -> Icons.Default.Lightbulb
            "Build" -> Icons.Default.Build
            "Handyman" -> Icons.Default.Handyman
            "Hardware" -> Icons.Default.Hardware
            "Construction" -> Icons.Default.Construction
            "Security" -> Icons.Default.Security
            "CleaningServices" -> Icons.Default.CleaningServices
            "House" -> Icons.Default.House
            "HomeWork" -> Icons.Default.HomeWork
            "HolidayVillage" -> Icons.Default.HolidayVillage
            "Cabin" -> Icons.Default.Cabin
            "Balcony" -> Icons.Default.Balcony
            "Deck" -> Icons.Default.Deck
            "Yard" -> Icons.Default.Yard
            "Garage" -> Icons.Default.Garage
            "Roofing" -> Icons.Default.Roofing
            "DoorFront" -> Icons.Default.DoorFront
            "DoorSliding" -> Icons.Default.DoorSliding
            "Window" -> Icons.Default.Window
            "Bathtub" -> Icons.Default.Bathtub
            "Shower" -> Icons.Default.Shower
            "HotTub" -> Icons.Default.HotTub
            "Iron" -> Icons.Default.Iron
            "Microwave" -> Icons.Default.Microwave
            "Blender" -> Icons.Default.Blender
            "Air" -> Icons.Default.Air
            "AcUnit" -> Icons.Default.AcUnit
            "Thermostat" -> Icons.Default.Thermostat
            "Fireplace" -> Icons.Default.Fireplace
            "GasMeter" -> Icons.Default.GasMeter
            "HeatPump" -> Icons.Default.HeatPump
            "WaterDamage" -> Icons.Default.WaterDamage
            "SolarPower" -> Icons.Default.SolarPower
            "Propane" -> Icons.Default.Propane
            "ElectricalServices" -> Icons.Default.ElectricalServices
            "Sensors" -> Icons.Default.Sensors
            "LocalHospital" -> Icons.Default.LocalHospital
            "Medication" -> Icons.Default.Medication
            "MedicalServices" -> Icons.Default.MedicalServices
            "Healing" -> Icons.Default.Healing
            "LocalPharmacy" -> Icons.Default.LocalPharmacy
            "Vaccines" -> Icons.Default.Vaccines
            "Psychology" -> Icons.Default.Psychology
            "Favorite" -> Icons.Default.Favorite
            "FavoriteBorder" -> Icons.Default.FavoriteBorder
            "MonitorHeart" -> Icons.Default.MonitorHeart
            "Spa" -> Icons.Default.Spa
            "SelfImprovement" -> Icons.Default.SelfImprovement
            "HealthAndSafety" -> Icons.Default.HealthAndSafety
            "MedicalInformation" -> Icons.Default.MedicalInformation
            "Emergency" -> Icons.Default.Emergency
            "MonitorWeight" -> Icons.Default.MonitorWeight
            "Bloodtype" -> Icons.Default.Bloodtype
            "Blind" -> Icons.Default.Blind
            "Accessible" -> Icons.AutoMirrored.Filled.Accessible
            "AccessibleForward" -> Icons.AutoMirrored.Filled.AccessibleForward
            "CleanHands" -> Icons.Default.CleanHands
            "Sanitizer" -> Icons.Default.Sanitizer
            "Masks" -> Icons.Default.Masks
            "PersonalInjury" -> Icons.Default.PersonalInjury
            "Elderly" -> Icons.Default.Elderly
            "ElderlyWoman" -> Icons.Default.ElderlyWoman
            "PregnantWoman" -> Icons.Default.PregnantWoman
            "Coronavirus" -> Icons.Default.Coronavirus
            "Sick" -> Icons.Default.Sick
            "Mood" -> Icons.Default.Mood
            "MoodBad" -> Icons.Default.MoodBad
            "SentimentSatisfied" -> Icons.Default.SentimentSatisfied
            "SentimentVerySatisfied" -> Icons.Default.SentimentVerySatisfied
            "SentimentDissatisfied" -> Icons.Default.SentimentDissatisfied
            "ShieldMoon" -> Icons.Default.ShieldMoon
            "Bedtime" -> Icons.Default.Bedtime
            "Nightlight" -> Icons.Default.Nightlight
            "SportsGymnastics" -> Icons.Default.SportsGymnastics
            "Biotech" -> Icons.Default.Biotech
            "Science" -> Icons.Default.Science
            "School" -> Icons.Default.School
            "Work" -> Icons.Default.Work
            "WorkOutline" -> Icons.Default.WorkOutline
            "BusinessCenter" -> Icons.Default.BusinessCenter
            "Handshake" -> Icons.Default.Handshake
            "FamilyRestroom" -> Icons.Default.FamilyRestroom
            "ChildCare" -> Icons.Default.ChildCare
            "ChildFriendly" -> Icons.Default.ChildFriendly
            "Pets" -> Icons.Default.Pets
            "AutoStories" -> Icons.Default.AutoStories
            "DesignServices" -> Icons.Default.DesignServices
            "Groups" -> Icons.Default.Groups
            "Person" -> Icons.Default.Person
            "People" -> Icons.Default.People
            "Celebration" -> Icons.Default.Celebration
            "Badge" -> Icons.Default.Badge
            "CardTravel" -> Icons.Default.CardTravel
            "Business" -> Icons.Default.Business
            "CorporateFare" -> Icons.Default.CorporateFare
            "Domain" -> Icons.Default.Domain
            "Hub" -> Icons.Default.Hub
            "Engineering" -> Icons.Default.Engineering
            "FactCheck" -> Icons.AutoMirrored.Filled.FactCheck
            "Assignment" -> Icons.AutoMirrored.Filled.Assignment
            "AssignmentTurnedIn" -> Icons.Default.AssignmentTurnedIn
            "AssignmentInd" -> Icons.Default.AssignmentInd
            "Task" -> Icons.Default.Task
            "TaskAlt" -> Icons.Default.TaskAlt
            "PendingActions" -> Icons.Default.PendingActions
            "HistoryEdu" -> Icons.Default.HistoryEdu
            "Grade" -> Icons.Default.Grade
            "WorkspacePremium" -> Icons.Default.WorkspacePremium
            "Class" -> Icons.Default.Class
            "CastForEducation" -> Icons.Default.CastForEducation
            "ImportContacts" -> Icons.Default.ImportContacts
            "Create" -> Icons.Default.Create
            "Edit" -> Icons.Default.Edit
            "DriveFileRenameOutline" -> Icons.Default.DriveFileRenameOutline
            "Draw" -> Icons.Default.Draw
            "CoPresent" -> Icons.Default.CoPresent
            "ContactPhone" -> Icons.Default.ContactPhone
            "ContactMail" -> Icons.Default.ContactMail
            "Mail" -> Icons.Default.Mail
            "Email" -> Icons.Default.Email
            "Drafts" -> Icons.Default.Drafts
            "MarkEmailRead" -> Icons.Default.MarkEmailRead
            "SupervisedUserCircle" -> Icons.Default.SupervisedUserCircle
            "Crib" -> Icons.Default.Crib
            "BabyChangingStation" -> Icons.Default.BabyChangingStation
            "Stroller" -> Icons.Default.Stroller
            "Woman" -> Icons.Default.Woman
            "Man" -> Icons.Default.Man
            "Boy" -> Icons.Default.Boy
            "Girl" -> Icons.Default.Girl
            "EmojiEmotions" -> Icons.Default.EmojiEmotions
            "Face" -> Icons.Default.Face
            "Diversity1" -> Icons.Default.Diversity1
            "Diversity2" -> Icons.Default.Diversity2
            "Diversity3" -> Icons.Default.Diversity3
            "PeopleAlt" -> Icons.Default.PeopleAlt
            "PersonAdd" -> Icons.Default.PersonAdd
            "GroupAdd" -> Icons.Default.GroupAdd
            "Wc" -> Icons.Default.Wc
            "Wash" -> Icons.Default.Wash
            "Dry" -> Icons.Default.Dry
            "Soap" -> Icons.Default.Soap
            "Stairs" -> Icons.Default.Stairs
            "Elevator" -> Icons.Default.Elevator
            "ChairAlt" -> Icons.Default.ChairAlt
            "Desk" -> Icons.Default.Desk
            "TableRestaurant" -> Icons.Default.TableRestaurant
            "TableBar" -> Icons.Default.TableBar
            "Living" -> Icons.Default.Living
            "KingBed" -> Icons.Default.KingBed
            "SingleBed" -> Icons.Default.SingleBed
            "PhoneAndroid" -> Icons.Default.PhoneAndroid
            "PhoneIphone" -> Icons.Default.PhoneIphone
            "SmartDisplay" -> Icons.Default.SmartDisplay
            "Movie" -> Icons.Default.Movie
            "Theaters" -> Icons.Default.Theaters
            "MusicNote" -> Icons.Default.MusicNote
            "Headphones" -> Icons.Default.Headphones
            "Podcasts" -> Icons.Default.Podcasts
            "Radio" -> Icons.Default.Radio
            "CameraAlt" -> Icons.Default.CameraAlt
            "Videocam" -> Icons.Default.Videocam
            "LiveTv" -> Icons.Default.LiveTv
            "Casino" -> Icons.Default.Casino
            "EmojiEvents" -> Icons.Default.EmojiEvents
            "MilitaryTech" -> Icons.Default.MilitaryTech
            "SportsEsports" -> Icons.Default.SportsEsports
            "Gamepad" -> Icons.Default.Gamepad
            "Piano" -> Icons.Default.Piano
            "QueueMusic" -> Icons.AutoMirrored.Filled.QueueMusic
            "Album" -> Icons.Default.Album
            "LibraryMusic" -> Icons.Default.LibraryMusic
            "Mic" -> Icons.Default.Mic
            "MicExternalOn" -> Icons.Default.MicExternalOn
            "VolumeUp" -> Icons.AutoMirrored.Filled.VolumeUp
            "SurroundSound" -> Icons.Default.SurroundSound
            "Equalizer" -> Icons.Default.Equalizer
            "GraphicEq" -> Icons.Default.GraphicEq
            "MovieCreation" -> Icons.Default.MovieCreation
            "TheaterComedy" -> Icons.Default.TheaterComedy
            "Camera" -> Icons.Default.Camera
            "Photo" -> Icons.Default.Photo
            "PhotoLibrary" -> Icons.Default.PhotoLibrary
            "Collections" -> Icons.Default.Collections
            "VideoLibrary" -> Icons.Default.VideoLibrary
            "SlowMotionVideo" -> Icons.Default.SlowMotionVideo
            "Audiotrack" -> Icons.Default.Audiotrack
            "PianoOff" -> Icons.Default.PianoOff
            "Attractions" -> Icons.Default.Attractions
            "Festival" -> Icons.Default.Festival
            "ConfirmationNumber" -> Icons.Default.ConfirmationNumber
            "AirplaneTicket" -> Icons.AutoMirrored.Filled.AirplaneTicket
            "SportsScore" -> Icons.Default.SportsScore
            "StrikethroughS" -> Icons.Default.StrikethroughS
            "SportsSoccer" -> Icons.Default.SportsSoccer
            "SportsBasketball" -> Icons.Default.SportsBasketball
            "SportsTennis" -> Icons.Default.SportsTennis
            "SportsCricket" -> Icons.Default.SportsCricket
            "SportsBaseball" -> Icons.Default.SportsBaseball
            "SportsFootball" -> Icons.Default.SportsFootball
            "SportsVolleyball" -> Icons.Default.SportsVolleyball
            "SportsGolf" -> Icons.Default.SportsGolf
            "SportsRugby" -> Icons.Default.SportsRugby
            "SportsHandball" -> Icons.Default.SportsHandball
            "SportsHockey" -> Icons.Default.SportsHockey
            "SportsMotorsports" -> Icons.Default.SportsMotorsports
            "SportsMartialArts" -> Icons.Default.SportsMartialArts
            "SportsMma" -> Icons.Default.SportsMma
            "Kayaking" -> Icons.Default.Kayaking
            "Rowing" -> Icons.Default.Rowing
            "Surfing" -> Icons.Default.Surfing
            "Snowboarding" -> Icons.Default.Snowboarding
            "Skateboarding" -> Icons.Default.Skateboarding
            "Pool" -> Icons.Default.Pool
            "Kitesurfing" -> Icons.Default.Kitesurfing
            "Hiking" -> Icons.Default.Hiking
            "DownhillSkiing" -> Icons.Default.DownhillSkiing
            "NordicWalking" -> Icons.Default.NordicWalking
            "Paragliding" -> Icons.Default.Paragliding
            "ScubaDiving" -> Icons.Default.ScubaDiving
            "Scoreboard" -> Icons.Default.Scoreboard
            "Sports" -> Icons.Default.Sports
            "Timer" -> Icons.Default.Timer
            "Alarm" -> Icons.Default.Alarm
            "HourglassBottom" -> Icons.Default.HourglassBottom
            "Computer" -> Icons.Default.Computer
            "Smartphone" -> Icons.Default.Smartphone
            "Tablet" -> Icons.Default.Tablet
            "TabletMac" -> Icons.Default.TabletMac
            "SmartToy" -> Icons.Default.SmartToy
            "Devices" -> Icons.Default.Devices
            "Phonelink" -> Icons.Default.Phonelink
            "DeveloperBoard" -> Icons.Default.DeveloperBoard
            "Memory" -> Icons.Default.Memory
            "SimCard" -> Icons.Default.SimCard
            "SdCard" -> Icons.Default.SdCard
            "Storage" -> Icons.Default.Storage
            "Cloud" -> Icons.Default.Cloud
            "CloudDownload" -> Icons.Default.CloudDownload
            "CloudUpload" -> Icons.Default.CloudUpload
            "CloudDone" -> Icons.Default.CloudDone
            "CloudSync" -> Icons.Default.CloudSync
            "Usb" -> Icons.Default.Usb
            "Bluetooth" -> Icons.Default.Bluetooth
            "BluetoothConnected" -> Icons.Default.BluetoothConnected
            "WifiTethering" -> Icons.Default.WifiTethering
            "Cast" -> Icons.Default.Cast
            "CastConnected" -> Icons.Default.CastConnected
            "Key" -> Icons.Default.Key
            "VpnKey" -> Icons.Default.VpnKey
            "Lock" -> Icons.Default.Lock
            "LockOpen" -> Icons.Default.LockOpen
            "EnhancedEncryption" -> Icons.Default.EnhancedEncryption
            "Fingerprint" -> Icons.Default.Fingerprint
            "Terminal" -> Icons.Default.Terminal
            "Code" -> Icons.Default.Code
            "BugReport" -> Icons.Default.BugReport
            "IntegrationInstructions" -> Icons.Default.IntegrationInstructions
            "Eco" -> Icons.Default.Eco
            "Forest" -> Icons.Default.Forest
            "Nature" -> Icons.Default.Nature
            "NaturePeople" -> Icons.Default.NaturePeople
            "Grass" -> Icons.Default.Grass
            "Park" -> Icons.Default.Park
            "WbSunny" -> Icons.Default.WbSunny
            "DarkMode" -> Icons.Default.DarkMode
            "LightMode" -> Icons.Default.LightMode
            "CloudQueue" -> Icons.Default.CloudQueue
            "Thunderstorm" -> Icons.Default.Thunderstorm
            "Tsunami" -> Icons.Default.Tsunami
            "Volcano" -> Icons.Default.Volcano
            "Landscape" -> Icons.Default.Landscape
            "Terrain" -> Icons.Default.Terrain
            "Flare" -> Icons.Default.Flare
            "Whatshot" -> Icons.Default.Whatshot
            "EnergySavingsLeaf" -> Icons.Default.EnergySavingsLeaf
            "Recycling" -> Icons.Default.Recycling
            "Compost" -> Icons.Default.Compost
            "CrueltyFree" -> Icons.Default.CrueltyFree
            "PestControl" -> Icons.Default.PestControl
            "Category" -> Icons.Default.Category
            "MoreHoriz" -> Icons.Default.MoreHoriz
            "MoreVert" -> Icons.Default.MoreVert
            "Star" -> Icons.Default.Star
            "StarBorder" -> Icons.Default.StarBorder
            "StarHalf" -> Icons.AutoMirrored.Filled.StarHalf
            "Bookmark" -> Icons.Default.Bookmark
            "BookmarkBorder" -> Icons.Default.BookmarkBorder
            "BookmarkAdded" -> Icons.Default.BookmarkAdded
            "Label" -> Icons.AutoMirrored.Filled.Label
            "LabelImportant" -> Icons.AutoMirrored.Filled.LabelImportant
            "Flag" -> Icons.Default.Flag
            "OutlinedFlag" -> Icons.Default.OutlinedFlag
            "FlagCircle" -> Icons.Default.FlagCircle
            "Shield" -> Icons.Default.Shield
            "Notifications" -> Icons.Default.Notifications
            "NotificationsActive" -> Icons.Default.NotificationsActive
            "NotificationsPaused" -> Icons.Default.NotificationsPaused
            "NotificationsOff" -> Icons.Default.NotificationsOff
            "Send" -> Icons.AutoMirrored.Filled.Send
            "Archive" -> Icons.Default.Archive
            "Unarchive" -> Icons.Default.Unarchive
            "Folder" -> Icons.Default.Folder
            "FolderSpecial" -> Icons.Default.FolderSpecial
            "FolderShared" -> Icons.Default.FolderShared
            "CreateNewFolder" -> Icons.Default.CreateNewFolder
            "ThumbUp" -> Icons.Default.ThumbUp
            "ThumbDown" -> Icons.Default.ThumbDown
            "Verified" -> Icons.Default.Verified
            "VerifiedUser" -> Icons.Default.VerifiedUser
            "Policy" -> Icons.Default.Policy
            "Gavel" -> Icons.Default.Gavel
            "AllInclusive" -> Icons.Default.AllInclusive
            "AutoAwesome" -> Icons.Default.AutoAwesome
            "Bolt" -> Icons.Default.Bolt
            "FlashOn" -> Icons.Default.FlashOn
            "FlashOff" -> Icons.Default.FlashOff
            "Check" -> Icons.Default.Check
            "CheckCircle" -> Icons.Default.CheckCircle
            "CheckCircleOutline" -> Icons.Default.CheckCircleOutline
            "Done" -> Icons.Default.Done
            "DoneAll" -> Icons.Default.DoneAll
            "Close" -> Icons.Default.Close
            "Cancel" -> Icons.Default.Cancel
            "Error" -> Icons.Default.Error
            "Warning" -> Icons.Default.Warning
            "Info" -> Icons.Default.Info
            "Help" -> Icons.AutoMirrored.Filled.Help
            "HelpOutline" -> Icons.AutoMirrored.Filled.HelpOutline
            "Sync" -> Icons.Default.Sync
            "Update" -> Icons.Default.Update
            "Refresh" -> Icons.Default.Refresh
            "Cached" -> Icons.Default.Cached
            "Autorenew" -> Icons.Default.Autorenew
            "Schedule" -> Icons.Default.Schedule
            "Event" -> Icons.Default.Event
            "CalendarToday" -> Icons.Default.CalendarToday
            "CalendarMonth" -> Icons.Default.CalendarMonth
            "Visibility" -> Icons.Default.Visibility
            "VisibilityOff" -> Icons.Default.VisibilityOff
            "Tune" -> Icons.Default.Tune
            "FilterList" -> Icons.Default.FilterList
            "Sort" -> Icons.AutoMirrored.Filled.Sort
            "Search" -> Icons.Default.Search
            "Share" -> Icons.Default.Share
            "Brush" -> Icons.Default.Brush
            "Palette" -> Icons.Default.Palette
            "ContentCut" -> Icons.Default.ContentCut
            "VolunteerActivism" -> Icons.Default.VolunteerActivism
            "Sms" -> Icons.Default.Sms
            "SwapHoriz" -> Icons.Default.SwapHoriz
            "Weekend" -> Icons.Default.Weekend
            "Apps" -> Icons.Default.Apps
            "Phishing" -> Icons.Default.Phishing
            "BatteryChargingFull" -> Icons.Default.BatteryChargingFull
            "Stars" -> Icons.Default.Stars
            "Replay" -> Icons.Default.Replay
            "LaptopMac" -> Icons.Default.LaptopMac
            "BankBkash", "BankBkashAlt", "BankNagad", "BankNagadAlt", "BankRocket", "BankRocketAlt",
            "BankUpay", "BankUpayAlt", "BankCellfin", "BankCellfinAlt" -> Icons.Default.Payments
            "BankDBBL", "BankDBBLAlt", "BankIBBL", "BankIBBLAlt", "BankBRAC", "BankAstha",
            "BankCity", "BankCitytouch", "BankSonali", "BankSonaliSheba", "BankEBL", "BankSkybanking" -> Icons.Default.AccountBalance
            else -> Icons.Default.Category
        }
    }

    /**
     * Searches the vast in-app icon store for a suitable icon matching the given query name or tags.
     * Returns the icon name if found, or null if no specific in-app match exists.
     */
    fun findMatchingInAppIcon(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val clean = name.trim().lowercase()
        if (clean.isBlank()) return null

        // 1. Direct match with built-in icon names or tags
        val directMatch = BUILTIN_ICONS.firstOrNull { icon ->
            icon.name.equals(clean, ignoreCase = true) ||
            icon.tags.any { tag -> tag.equals(clean, ignoreCase = true) }
        }
        if (directMatch != null) return directMatch.name

        // 2. Keyword-based matching in vast in-app icon library
        val suggested = suggestIconForName(name)
        if (suggested != "Category" && suggested != "Folder") return suggested

        // 3. Word token matching across built-in icon tags (tokens >= 3 chars, skipping stop words)
        val tokens = clean.split(Regex("[\\s\\-_/,.&()]+")).filter { it.length >= 3 && !IGNORED_INITIAL_WORDS.contains(it) }
        for (token in tokens) {
            val tokenMatch = BUILTIN_ICONS.firstOrNull { icon ->
                icon.name.equals(token, ignoreCase = true) ||
                icon.tags.any { tag -> tag.equals(token, ignoreCase = true) }
            }
            if (tokenMatch != null) return tokenMatch.name
        }

        // 4. Substring tag matching for near matches (tags with length >= 4, non-generic)
        for (icon in BUILTIN_ICONS) {
            for (tag in icon.tags) {
                if (tag.length >= 4 && clean.contains(tag.lowercase()) && !IGNORED_INITIAL_WORDS.contains(tag.lowercase())) {
                    return icon.name
                }
            }
        }

        return null
    }

    /**
     * Intelligently suggests an icon name based on category/subcategory/item title keywords in English or Bengali.
     */
    fun suggestIconForName(name: String?): String {
        if (name.isNullOrBlank()) return "Category"
        val clean = name.trim().lowercase()

        return when {
            // MFS & Banks
            clean.contains("bkash") || clean.contains("বিকাশ") -> "BankBkash"
            clean.contains("nagad") || clean.contains("নগদ") -> "BankNagad"
            clean.contains("rocket") || clean.contains("রকেট") -> "BankRocket"
            clean.contains("upay") || clean.contains("উপায়") -> "BankUpay"
            clean.contains("cellfin") || clean.contains("সেলফিন") -> "BankCellfin"
            clean.contains("dbbl") || clean.contains("ডাচ বাংলা") -> "BankDBBL"
            clean.contains("ibbl") || clean.contains("ইসলামী ব্যাংক") -> "BankIBBL"
            clean.contains("brac") || clean.contains("ব্র্যাক") -> "BankBRAC"
            clean.contains("city bank") || clean.contains("সিটি ব্যাংক") || clean.contains("citybank") -> "BankCity"
            clean.contains("sonali") || clean.contains("সোনালী") -> "BankSonali"
            clean.contains("ebl") || clean.contains("ইস্টার্ন") -> "BankEBL"
            clean.contains("scb") || clean.contains("standard chartered") -> "BankSCB"
            clean.contains("hsbc") -> "BankHSBC"
            clean.contains("pubali") || clean.contains("পূবালী") -> "BankPubali"
            clean.contains("agrani") || clean.contains("অগ্রণী") -> "BankAgrani"
            clean.contains("janata") || clean.contains("জনতা") -> "BankJanata"
            clean.contains("dhaka bank") || clean.contains("ঢাকা ব্যাংক") -> "BankDhaka"
            clean.contains("trust bank") || clean.contains("ট্রাস্ট ব্যাংক") -> "BankTrust"

            // Core Financial & Account Terms
            clean.contains("cash") || clean.contains("ক্যাশ") || clean.contains("নগদ টাকা") -> "AccountBalanceWallet"
            clean.contains("wallet") || clean.contains("ওয়ালেট") || clean.contains("মানিব্যাগ") -> "Wallet"
            clean.contains("bank") || clean.contains("ব্যাংক") || clean.contains("হিসাব") -> "AccountBalance"
            clean.contains("card") || clean.contains("কার্ড") || clean.contains("visa") || clean.contains("mastercard") -> "CreditCard"
            clean.contains("saving") || clean.contains("সঞ্চয়") || clean.contains("সঞ্চয়") || clean.contains("ডিপিএস") || clean.contains("dps") || clean.contains("fdr") -> "Savings"
            clean.contains("loan") || clean.contains("debt") || clean.contains("ঋণ") || clean.contains("ধার") || clean.contains("কর্জ") -> "Handshake"
            clean.contains("equity") || clean.contains("owner") || clean.contains("opening balance") || clean.contains("মালিকানা") -> "AccountBalanceWallet"

            // Apps & Subscriptions
            clean.contains("app") || clean.contains("subscription") || clean.contains("সাবস্ক্রিপশন") || clean.contains("live mcq") || clean.contains("software") -> "Subscriptions"
            
            // Devices & Gadgets
            clean.contains("display") || clean.contains("screen") || clean.contains("ডিসপ্লে") -> "PhoneAndroid"
            clean.contains("glass") || clean.contains("গ্লাস") || clean.contains("screen protector") -> "PhoneAndroid"
            clean.contains("device") || clean.contains("gadget") || clean.contains("গ্যাজেট") || clean.contains("ইলেকট্রনিক্স") || clean.contains("charger") || clean.contains("headphone") || clean.contains("earbuds") -> "Devices"
            clean.contains("laptop") || clean.contains("computer") || clean.contains("কম্পিউটার") || clean.contains("পিসি") -> "Computer"

            // Booster Foods / Nutrition / Energy / Gym snacks
            clean.contains("booster") || clean.contains("energy") || clean.contains("পুষ্টি") || clean.contains("ছোলা") -> "ElectricBolt"
            clean.contains("honey") || clean.contains("মধু") || clean.contains("garlic honey") || clean.contains("রসুন") -> "EnergySavingsLeaf"
            clean.contains("egg") || clean.contains("duck egg") || clean.contains("ডিম") || clean.contains("হাঁসের ডিম") -> "Egg"

            // Groceries, Fish, Meat, Poultry, Cooking Essentials
            clean.contains("fish") || clean.contains("চিংড়ি") || clean.contains("চিংড়ি") || clean.contains("শুটকি") || clean.contains("মাছ") -> "Phishing"
            clean.contains("chicken") || clean.contains("মুরগি") || clean.contains("মুরগী") || clean.contains("beef") || clean.contains("গরু") || clean.contains("খাসি") || clean.contains("মাংস") || clean.contains("গোশত") -> "Restaurant"
            clean.contains("polao") || clean.contains("পোলাও") || clean.contains("rice") || clean.contains("চাল") || clean.contains("ভাত") -> "LocalGroceryStore"
            clean.contains("mustard") || clean.contains("সরিষা") || clean.contains("soybean") || clean.contains("সয়াবিন") || clean.contains("oil") || clean.contains("তৈল") -> "LocalGasStation"
            clean.contains("ghee") || clean.contains("ঘি") || clean.contains("butter") || clean.contains("মাখন") -> "SoupKitchen"
            clean.contains("semai") || clean.contains("সেমাই") || clean.contains("vermicelli") -> "BakeryDining"
            clean.contains("sugar") || clean.contains("চিনি") || clean.contains("গুড়") || clean.contains("gur") -> "LocalGroceryStore"
            clean.contains("salt") || clean.contains("লবণ") || clean.contains("নুনের") -> "LocalGroceryStore"
            clean.contains("flour") || clean.contains("আটা") || clean.contains("ময়দা") || clean.contains("সুজি") -> "LocalGroceryStore"
            clean.contains("turmeric") || clean.contains("হলুদ") || clean.contains("মরিচ") || clean.contains("জিরা") || clean.contains("দারচিনি") || clean.contains("এলাচ") || clean.contains("মশলা") || clean.contains("spice") -> "LocalGroceryStore"
            clean.contains("vegetable") || clean.contains("সবজি") || clean.contains("শাক") || clean.contains("বেগুন") || clean.contains("করলা") || clean.contains("শশা") || clean.contains("শসা") || clean.contains("তেঁতুল") -> "Grass"
            clean.contains("ইসবগুল") || clean.contains("তোকমা") || clean.contains("isabgol") -> "Eco"
            clean.contains("grocer") || clean.contains("মুদি") || clean.contains("ডাল") || clean.contains("মটর") -> "LocalGroceryStore"

            // Fruits & Dates
            clean.contains("date") || clean.contains("খেজুর") -> "Eco"
            clean.contains("banana") || clean.contains("কলা") || clean.contains("apple") || clean.contains("আপেল") || clean.contains("আম") || clean.contains("কমলা") || clean.contains("ফল") || clean.contains("fruit") -> "Eco"

            // Drinks, Beverages & Water
            clean.contains("bottled water") || clean.contains("water bottle") || clean.contains("পানি") || clean.contains("ড্রিংক") -> "LocalDrink"
            clean.contains("beverage") || clean.contains("juice") || clean.contains("জুস") || clean.contains("coke") || clean.contains("পানীয়") -> "LocalDrink"
            clean.contains("coffee mate") || clean.contains("coffee") || clean.contains("কফি") -> "LocalCafe"
            clean.contains("tea") || clean.contains("চা") || clean.contains("লিকার") -> "EmojiFoodBeverage"
            clean.contains("ice cream") || clean.contains("icecream") || clean.contains("আইসক্রিম") -> "Icecream"
            clean.contains("powder milk") || clean.contains("condensed milk") || clean.contains("দুধ") || clean.contains("দই") || clean.contains("curd") -> "BakeryDining"

            // Snacks, Bakery & Fast Food
            clean.contains("cake") || clean.contains("কেক") -> "Cake"
            clean.contains("biscuit") || clean.contains("বিস্কুট") || clean.contains("cookie") -> "Cookie"
            clean.contains("chips") || clean.contains("চিপস") || clean.contains("চানাচুর") || clean.contains("মুড়ি") || clean.contains("মুড়ির") || clean.contains("বাদাম") || clean.contains("nut") || clean.contains("পাউরুটি") || clean.contains("bread") || clean.contains("toast") || clean.contains("sauce") || clean.contains("সস") -> "Fastfood"
            clean.contains("street food") || clean.contains("ফুচকা") || clean.contains("চটপটি") || clean.contains("স্ট্রিট ফুড") || clean.contains("সিংগারা") || clean.contains("সমুচা") -> "Fastfood"
            clean.contains("dining") || clean.contains("restaurant") || clean.contains("রেস্তোরাঁ") || clean.contains("হোটেল") || clean.contains("বিরিয়ানি") -> "DinnerDining"
            clean.contains("home snacks") || clean.contains("sweet") || clean.contains("মিষ্টি") || clean.contains("নাস্তা") || clean.contains("স্ন্যাক্স") -> "BakeryDining"
            clean.contains("training snacks") || clean.contains("workout") || clean.contains("ব্যায়াম") || clean.contains("জিম") -> "FitnessCenter"

            // Hardware, Home & Appliances
            clean.contains("ips") || clean.contains("battery") || clean.contains("ব্যাটারি") || clean.contains("solar") || clean.contains("সোলার") -> "BatteryChargingFull"
            clean.contains("plumbing") || clean.contains("প্লাম্বিং") || clean.contains("pipe") || clean.contains("fittings") || clean.contains("water tap") || clean.contains("কল") || clean.contains("ফিল্টার") || clean.contains("filter") -> "Plumbing"
            clean.contains("bulb") || clean.contains("বাল্ব") || clean.contains("বাতি") || clean.contains("light") || clean.contains("torch") || clean.contains("টর্চ") -> "Lightbulb"
            clean.contains("door lock") || clean.contains("তালা") || clean.contains("lock") -> "Lock"
            clean.contains("furniture") || clean.contains("ফার্নিচার") || clean.contains("আসবাবপত্র") || clean.contains("চেয়ার") || clean.contains("সোফা") || clean.contains("খাট") -> "Chair"
            clean.contains("decor") || clean.contains("সাজসজ্জা") || clean.contains("ঘর সাজানো") || clean.contains("পেইন্ট") -> "Palette"
            clean.contains("household") || clean.contains("গৃহস্থালি") || clean.contains("ঘরকন্না") -> "Kitchen"
            clean.contains("mosquito") || clean.contains("মশা") || clean.contains("কয়েল") || clean.contains("coil") || clean.contains("pest") -> "PestControl"

            // Cleaning, Washing & Toiletries
            clean.contains("vim") || clean.contains("মাজনি") || clean.contains("dishwash") || clean.contains("washing bar") || clean.contains("washing powder") || clean.contains("হুইল") || clean.contains("সার্ফ") || clean.contains("ডিটারজেন্ট") || clean.contains("detergent") || clean.contains("toilet cleaner") || clean.contains("হারপিক") -> "CleaningServices"
            clean.contains("tissue") || clean.contains("টিস্যু") || clean.contains("napkin") -> "DryCleaning"
            clean.contains("clean bill") || clean.contains("পরিষ্কার") || clean.contains("ঝাড়ু") -> "CleaningServices"
            clean.contains("hair cut") || clean.contains("haircut") || clean.contains("বার্বার") || clean.contains("চুল") || clean.contains("সেলুন") -> "ContentCut"
            clean.contains("face wash") || clean.contains("facewash") || clean.contains("lotion") || clean.contains("লোশন") || clean.contains("skin") || clean.contains("ত্বক") || clean.contains("সৌন্দর্য") -> "Spa"
            clean.contains("shampoo") || clean.contains("শ্যাম্পু") || clean.contains("handwash") || clean.contains("সাবান") || clean.contains("soap") || clean.contains("toothpaste") || clean.contains("টুথপেস্ট") || clean.contains("brush") || clean.contains("ব্রাশ") || clean.contains("razor") || clean.contains("রেজর") || clean.contains("শেভ") -> "Soap"
            clean.contains("care essential") || clean.contains("যত্ন") || clean.contains("hygiene") -> "CleanHands"

            // Medicine & Health
            clean.contains("saline") || clean.contains("স্যালাইন") || clean.contains("oral saline") -> "MedicalServices"
            clean.contains("sanitary") || clean.contains("pad") || clean.contains("প্যাড") -> "HealthAndSafety"
            clean.contains("doctor") || clean.contains("fees") || clean.contains("ডাক্তার") || clean.contains("রোগী") || clean.contains("patient") -> "LocalHospital"
            clean.contains("medical test") || clean.contains("test") || clean.contains("ল্যাব") || clean.contains("পরীক্ষা") || clean.contains("diagnostic") -> "Biotech"
            clean.contains("med") || clean.contains("medicine") || clean.contains("ওষুধ") || clean.contains("ঔষধ") || clean.contains("ফার্মেসি") || clean.contains("ট্যাবলেট") || clean.contains("সিরাপ") -> "Medication"
            clean.contains("sexual") || clean.contains("wellness") || clean.contains("কনডম") || clean.contains("যৌন") -> "Favorite"
            clean.contains("health") || clean.contains("স্বাস্থ্য") || clean.contains("চিকিৎসা") -> "MedicalServices"

            // Clothing, Shoes & Tailoring
            clean.contains("shoe repair") || clean.contains("জুতা মেরামত") -> "Handyman"
            clean.contains("shoe") || clean.contains("জুতা") || clean.contains("পনচ") || clean.contains("স্যান্ডেল") || clean.contains("জুতো") -> "ShoppingBag"
            clean.contains("sock") || clean.contains("মোজা") -> "Style"
            clean.contains("bangle") || clean.contains("চুড়ি") || clean.contains("চুড়ি") || clean.contains("jewelry") || clean.contains("গহনা") || clean.contains("স্বর্ণ") -> "Diamond"
            clean.contains("tailor") || clean.contains("দর্জি") || clean.contains("সেলাই") || clean.contains("জামা সেলাই") -> "ContentCut"
            clean.contains("pant") || clean.contains("প্যান্ট") || clean.contains("shirt") || clean.contains("শার্ট") || clean.contains("পাঞ্জাবি") || clean.contains("শাড়ি") || clean.contains("cloth") || clean.contains("পোশাক") || clean.contains("কাপড়") || clean.contains("জামা") -> "Checkroom"

            // Vehicles, Bike, Fuel & Travel
            clean.contains("mobil") || clean.contains("মবিল") || clean.contains("engine oil") -> "LocalGasStation"
            clean.contains("motorcycle") || clean.contains("bike") || clean.contains("বাইক") || clean.contains("মোটরসাইকেল") -> "TwoWheeler"
            clean.contains("fuel") || clean.contains("জ্বালানি") || clean.contains("তেল") || clean.contains("পেট্রোল") || clean.contains("অকটেন") || clean.contains("সিএনজি") || clean.contains("t oil") -> "LocalGasStation"
            clean.contains("tour") || clean.contains("ভ্রমণ") || clean.contains("ট্যুর") || clean.contains("ছুটি") -> "FlightTakeoff"
            clean.contains("travel") || clean.contains("যাতায়াত") || clean.contains("সফর") || clean.contains("ভাড়া") || clean.contains("বাস") || clean.contains("ট্রেন") -> "Commute"
            clean.contains("transport") || clean.contains("পরিবহন") || clean.contains("গাড়ি") || clean.contains("taxi") || clean.contains("উবার") -> "DirectionsCar"

            // Islamic, Charity & Community
            clean.contains("imam") || clean.contains("ইমাম") || clean.contains("মুয়াজ্জিন") || clean.contains("হাদিয়া") -> "AccountBalance"
            clean.contains("mosque") || clean.contains("মসজিদ") || clean.contains("মাদ্রাসা") -> "AccountBalance"
            clean.contains("iftar") || clean.contains("ইফতার") || clean.contains("রমজান") || clean.contains("ramadan") -> "Favorite"
            clean.contains("mahfil") || clean.contains("মাহফিল") || clean.contains("ওয়াজ") -> "VolumeUp"
            clean.contains("qurbani") || clean.contains("কোরবানি") || clean.contains("কুরবানি") -> "Pets"
            clean.contains("zakat") || clean.contains("যাকাত") || clean.contains("জাকাত") -> "VolunteerActivism"
            clean.contains("charity") || clean.contains("দান") || clean.contains("সদকা") || clean.contains("অনুদান") || clean.contains("donate") -> "VolunteerActivism"

            // Family, Gifting & Social
            clean.contains("salami") || clean.contains("সালামি") || clean.contains("সালামী") || clean.contains("ঈদি") -> "Paid"
            clean.contains("gift") || clean.contains("present") || clean.contains("উপহার") || clean.contains("treat") || clean.contains("ট্রিট") -> "CardGiftcard"
            clean.contains("birthday") || clean.contains("জন্মদিন") -> "Cake"
            clean.contains("farewell") || clean.contains("বিদায়") -> "Groups"
            clean.contains("picnic") || clean.contains("পিকনিক") || clean.contains("বনভোজন") -> "Park"
            clean.contains("friend") || clean.contains("বন্ধু") || clean.contains("আড্ডা") || clean.contains("toiab") -> "Diversity2"
            clean.contains("parent") || clean.contains("পিতামাতা") || clean.contains("বাবার ঋণ") || clean.contains("মায়ের ঋণ") || clean.contains("abbu") || clean.contains("ammu") || clean.contains("বাবা") || clean.contains("মা") -> "FamilyRestroom"
            clean.contains("cash given") || clean.contains("নগদ প্রদান") || clean.contains("টাকা দেওয়া") || clean.contains("নগদ দান") -> "Payments"

            // Study & Education
            clean.contains("student") || clean.contains("ছাত্র") || clean.contains("শিক্ষার্থী") || clean.contains("টিউশনি") -> "School"
            clean.contains("teaching") || clean.contains("শিক্ষা উপকরণ") || clean.contains("পড়ানো") || clean.contains("শিক্ষক") -> "AutoStories"
            clean.contains("teacher id") || clean.contains("id card") || clean.contains("কার্ড") -> "Badge"
            clean.contains("application fee") || clean.contains("ভর্তি") || clean.contains("আবেদন") -> "Assignment"
            clean.contains("book") || clean.contains("বই") || clean.contains("গাইড") -> "AutoStories"
            clean.contains("study") || clean.contains("পড়াশোনা") || clean.contains("লেখাপড়া") || clean.contains("পরীক্ষা") || clean.contains("exam") -> "School"

            // Banking, Transfers, Fees, VAT, Taxes
            clean.contains("cheque") || clean.contains("চেক") -> "ReceiptLong"
            clean.contains("excise") || clean.contains("শুল্ক") || clean.contains("আবগারি") -> "Gavel"
            clean.contains("sms") || clean.contains("মেসেজ") -> "Sms"
            clean.contains("transfer charge") || clean.contains("ট্রান্সফার") || clean.contains("b2c") || clean.contains("s2dbbl") || clean.contains("s2r") || clean.contains("s2c") || clean.contains("send money") || clean.contains("ক্যাশ আউট") -> "SwapHoriz"
            clean.contains("vat") || clean.contains("ভ্যাট") || clean.contains("tax") || clean.contains("ট্যাক্স") || clean.contains("tds") -> "Percent"
            clean.contains("bank charge") || clean.contains("চার্জ") || clean.contains("ফি") || clean.contains("maintenance charge") -> "Receipt"
            clean.contains("bad debt") || clean.contains("মন্দ ঋণ") || clean.contains("অনাদায়ী") || clean.contains("ক্ষতি") || clean.contains("লোকসান") -> "MoneyOff"
            clean.contains("loan") || clean.contains("repayment") || clean.contains("ঋণ পরিশোধ") || clean.contains("কর্জ") -> "Handshake"
            clean.contains("bribe") || clean.contains("ঘুষ") -> "PointOfSale"

            // Work, Job & Income
            clean.contains("salary") || clean.contains("বেতন") -> "Work"
            clean.contains("bonus") || clean.contains("বোনাস") || clean.contains("incentive") -> "Stars"
            clean.contains("business") || clean.contains("ব্যবসা") || clean.contains("বিক্রয়") || clean.contains("sales") -> "BusinessCenter"
            clean.contains("freelance") || clean.contains("ফ্রিল্যান্সিং") || clean.contains("upwork") || clean.contains("fiverr") -> "LaptopMac"
            clean.contains("investment") || clean.contains("বিনিয়োগ") || clean.contains("শেয়ার") || clean.contains("stock") -> "ShowChart"
            clean.contains("dividend") || clean.contains("লভ্যাংশ") -> "TrendingUp"
            clean.contains("rental") || clean.contains("rent") || clean.contains("ভাড়া") || clean.contains("বাড়ি ভাড়া") -> "HomeWork"
            clean.contains("interest") || clean.contains("মুনাফা") || clean.contains("সুদ") || clean.contains("profit") -> "Percent"
            clean.contains("commission") || clean.contains("কমিশন") -> "MonetizationOn"
            clean.contains("refund") || clean.contains("রিফান্ড") || clean.contains("cashback") -> "Replay"
            clean.contains("pension") || clean.contains("পেনশন") || clean.contains("gratuity") -> "Savings"
            clean.contains("scholarship") || clean.contains("বৃত্তি") -> "School"
            clean.contains("pocket money") || clean.contains("হাত খরচ") -> "Wallet"
            clean.contains("job") || clean.contains("work") || clean.contains("অফিস") || clean.contains("চাকরি") || clean.contains("কর্মক্ষেত্র") -> "Work"

            // Utilities & Bills
            clean.contains("electric") || clean.contains("বিদ্যুৎ") || clean.contains("কারেন্ট") -> "ElectricBolt"
            clean.contains("gas") || clean.contains("গ্যাস") -> "GasMeter"
            clean.contains("recharge") || clean.contains("রিচার্জ") -> "PhoneAndroid"
            clean.contains("wifi") || clean.contains("ওয়াইফাই") || clean.contains("ইন্টারনেট") -> "Wifi"
            clean.contains("utilit") || clean.contains("ইউটিলিটি") || clean.contains("বিল") -> "Power"

            // Market / General Shopping
            clean.contains("mkt") || clean.contains("market") || clean.contains("বাজার") || clean.contains("সওদা") || clean.contains("shopping") || clean.contains("মার্কেট") -> "ShoppingCart"
            clean.contains("watch") || clean.contains("ঘড়ি") || clean.contains("ঘড়ি") -> "Watch"
            clean.contains("mobile (m&r)") || clean.contains("mobile repair") || clean.contains("মোবাইল মেরামত") -> "PhoneAndroid"
            clean.contains("m&r") || clean.contains("repair") || clean.contains("মেরামত") || clean.contains("সার্ভিসিং") -> "Build"
            clean.contains("pet") || clean.contains("বিড়াল") || clean.contains("cat") -> "Pets"
            clean.contains("social") || clean.contains("সামাজিক") -> "People"
            clean.contains("celebrat") || clean.contains("উৎসব") || clean.contains("অনুষ্ঠান") -> "Celebration"

            else -> "Category"
        }
    }

    val INITIALS_PALETTE: List<Color> = listOf(
        Color(0xFF2563EB), // Royal Blue
        Color(0xFF059669), // Emerald
        Color(0xFF7C3AED), // Violet
        Color(0xFFD97706), // Amber
        Color(0xFFDB2777), // Pink
        Color(0xFF0891B2), // Cyan
        Color(0xFF4F46E5), // Indigo
        Color(0xFF0D9488), // Teal
        Color(0xFFEA580C), // Orange
        Color(0xFFE11D48), // Rose
        Color(0xFF0284C7), // Sky
        Color(0xFF6D28D9)  // Deep Purple
    )

    val IGNORED_INITIAL_WORDS: Set<String> = setOf(
        "and", "&", "+", "the", "a", "an", "of", "in", "for", "to", "at", "by", "on", "with", "from",
        "ltd", "limited", "co", "corp", "corporation", "inc", "incorporated", "llc", "plc", "pvt", "private",
        "ac", "a/c", "acc", "account",
        "এবং", "ও", "বা", "আর", "এর", "লিমিটেড", "লিঃ"
    )

    /**
     * Checks if the given icon identifier represents an initials monogram avatar.
     */
    fun isInitialsIcon(iconName: String?): Boolean {
        if (iconName.isNullOrBlank()) return false
        return iconName.startsWith("INITIALS:", ignoreCase = true)
    }

    /**
     * Extracts the 1-3 letter monogram from an INITIALS icon identifier.
     */
    fun getInitialsFromIconName(iconName: String?): String {
        if (iconName.isNullOrBlank()) return "NA"
        val raw = if (iconName.contains(":")) iconName.substringAfter(":") else iconName
        return raw.substringBefore(":").take(3).uppercase()
    }

    /**
     * Deterministically derives an aesthetic Material 3 background color for given initials.
     */
    fun getInitialsColor(initials: String): Color {
        if (initials.isBlank()) return INITIALS_PALETTE[0]
        val hash = kotlin.math.abs(initials.hashCode())
        return INITIALS_PALETTE[hash % INITIALS_PALETTE.size]
    }

    /**
     * Generates clean, recognizable initials from a name:
     * - Multi-word: First letter of first word + First letter of last/second word (ignoring generic words)
     *   e.g. "Ashique Sir" -> "AS", "Digital and Tech" -> "DT", "John Michael Smith" -> "JS"
     * - Single-word: First letter + onset of second syllable / distinctive consonant
     *   e.g. "Ashique" -> "AQ", "Purny" -> "PN"
     */
    fun generateInitials(name: String?): String {
        if (name.isNullOrBlank()) return "NA"
        val clean = name.trim()

        val rawTokens = clean.split(Regex("[\\s\\-_/,.&()]+")).filter { it.isNotBlank() }
        if (rawTokens.isEmpty()) return "NA"

        val significantWords = rawTokens.filter { !IGNORED_INITIAL_WORDS.contains(it.lowercase()) }
        val words = if (significantWords.isNotEmpty()) significantWords else rawTokens

        if (words.size >= 2) {
            val firstWord = words.first()
            val lastWord = if (words.size == 2) words[1] else words.last()

            val firstChar = firstWord.firstOrNull { it.isLetterOrDigit() } ?: firstWord.first()
            val secondChar = lastWord.firstOrNull { it.isLetterOrDigit() } ?: lastWord.first()
            return "$firstChar$secondChar".uppercase()
        }

        return extractSingleWordInitials(words.first())
    }

    private fun extractSingleWordInitials(word: String): String {
        val clean = word.filter { it.isLetterOrDigit() }
        if (clean.length <= 1) return clean.uppercase()
        if (clean.length == 2) return clean.uppercase()

        // Check if word has embedded CamelCase (e.g. CashOut -> CO)
        val upperLetters = clean.filter { it.isUpperCase() }
        if (upperLetters.length >= 2) {
            return "${upperLetters.first()}${upperLetters[1]}".uppercase()
        }

        val firstChar = clean.first()
        val lower = clean.lowercase()

        // Words ending in "que" (e.g. Ashique -> AQ, Tarique -> TQ, Shafique -> SQ)
        if (lower.endsWith("que") && clean.length >= 4) {
            return "${firstChar}Q".uppercase()
        }

        val vowels = setOf('a', 'e', 'i', 'o', 'u')

        // Syllable onset detection
        val firstVowelIdx = lower.indexOfFirst { it in vowels }
        if (firstVowelIdx in 0 until lower.length - 1) {
            var nextVowelIdx = -1
            for (i in (firstVowelIdx + 1) until lower.length) {
                if (lower[i] in vowels || lower[i] == 'y') {
                    nextVowelIdx = i
                    break
                }
            }

            if (nextVowelIdx > firstVowelIdx + 1) {
                val consonantCluster = lower.substring(firstVowelIdx + 1, nextVowelIdx)
                if (consonantCluster.length >= 2) {
                    // VCCV pattern (e.g. Pur-ny -> N, Mar-tin -> T)
                    val secondSyllableOnset = consonantCluster.last()
                    return "$firstChar$secondSyllableOnset".uppercase()
                } else if (consonantCluster.isNotEmpty()) {
                    // VCV pattern (e.g. Da-vid -> V)
                    val onset = consonantCluster.first()
                    return "$firstChar$onset".uppercase()
                }
            } else if (nextVowelIdx != -1 && nextVowelIdx < lower.length - 1) {
                val consonantAfterSecondVowel = lower.substring(nextVowelIdx + 1).firstOrNull { it !in vowels && it != 'y' }
                if (consonantAfterSecondVowel != null) {
                    return "$firstChar$consonantAfterSecondVowel".uppercase()
                }
            }
        }

        val lastConsonant = lower.drop(1).lastOrNull { it !in vowels } ?: lower[1]
        return "$firstChar$lastConsonant".uppercase()
    }

    /**
     * Resolves the best icon for an account or category name:
     * 1. First tries to find closest matching icon from the app's internal icon library.
     * 2. If unavailable, generates initials from the name (e.g. Ashique Sir -> AS, Ashique -> AQ, Purny -> PN).
     * Guaranteed to never return empty or generic placeholder.
     */
    fun resolveBestIconOrInitials(name: String?): String {
        if (name.isNullOrBlank()) return "INITIALS:NA"
        val matchedInApp = findMatchingInAppIcon(name)
        if (matchedInApp != null && matchedInApp.isNotBlank() && matchedInApp != "Category") {
            return matchedInApp
        }
        val initials = generateInitials(name)
        return "INITIALS:$initials"
    }

    /**
     * Checks if the given icon identifier represents a custom image asset from internal storage or URL.
     */
    fun isCustomIcon(iconName: String?): Boolean {
        if (iconName.isNullOrBlank()) return false
        return iconName.startsWith("custom_icon_") ||
                iconName.startsWith("file://") ||
                iconName.startsWith("content://") ||
                iconName.startsWith("http://") ||
                iconName.startsWith("https://")
    }

    /**
     * Returns the File pointer for a stored custom icon.
     */
    fun getCustomIconFile(context: Context, iconName: String): File {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) customDir.mkdirs()
        val rawName = iconName.substringAfterLast("/")
        val fileDirect = File(customDir, rawName)
        if (fileDirect.exists()) return fileDirect
        val base = rawName.substringBeforeLast(".")
        for (ext in listOf("png", "webp", "jpg", "jpeg")) {
            val f = File(customDir, "$base.$ext")
            if (f.exists()) return f
        }
        return File(customDir, if (rawName.contains(".")) rawName else "$rawName.png")
    }

    data class IconEditState(
        val scale: Float = 1.0f,
        val panX: Float = 0f,
        val panY: Float = 0f,
        val quarterTurns: Int = 0,
        val fineAngle: Float = 0f,
        val flipHorizontal: Boolean = false,
        val flipVertical: Boolean = false,
        val cropShape: String = "circle",
        val fitMode: String = "fill",
        val bgColorHex: String? = null,
        val filterPreset: String = "ORIGINAL",
        val brightness: Float = 0f,
        val contrast: Float = 0f,
        val saturation: Float = 0f
    ) {
        fun toJsonObject(): org.json.JSONObject {
            return org.json.JSONObject().apply {
                put("scale", scale.toDouble())
                put("panX", panX.toDouble())
                put("panY", panY.toDouble())
                put("quarterTurns", quarterTurns)
                put("fineAngle", fineAngle.toDouble())
                put("flipHorizontal", flipHorizontal)
                put("flipVertical", flipVertical)
                put("cropShape", cropShape)
                put("fitMode", fitMode)
                if (bgColorHex != null) put("bgColorHex", bgColorHex) else put("bgColorHex", org.json.JSONObject.NULL)
                put("filterPreset", filterPreset)
                put("brightness", brightness.toDouble())
                put("contrast", contrast.toDouble())
                put("saturation", saturation.toDouble())
            }
        }

        companion object {
            fun fromJsonObject(json: org.json.JSONObject?): IconEditState {
                if (json == null) return IconEditState()
                val bgHex = if (json.isNull("bgColorHex")) null else json.optString("bgColorHex", null)
                return IconEditState(
                    scale = json.optDouble("scale", 1.0).toFloat(),
                    panX = json.optDouble("panX", 0.0).toFloat(),
                    panY = json.optDouble("panY", 0.0).toFloat(),
                    quarterTurns = json.optInt("quarterTurns", 0),
                    fineAngle = json.optDouble("fineAngle", 0.0).toFloat(),
                    flipHorizontal = json.optBoolean("flipHorizontal", false),
                    flipVertical = json.optBoolean("flipVertical", false),
                    cropShape = json.optString("cropShape", "circle"),
                    fitMode = json.optString("fitMode", "fill"),
                    bgColorHex = bgHex,
                    filterPreset = json.optString("filterPreset", "ORIGINAL"),
                    brightness = json.optDouble("brightness", 0.0).toFloat(),
                    contrast = json.optDouble("contrast", 0.0).toFloat(),
                    saturation = json.optDouble("saturation", 0.0).toFloat()
                )
            }
        }
    }

    data class CustomIconMetadata(
        val originalState: IconEditState,
        val currentState: IconEditState
    ) {
        fun toJsonString(): String {
            return org.json.JSONObject().apply {
                put("originalState", originalState.toJsonObject())
                put("currentState", currentState.toJsonObject())
            }.toString()
        }

        companion object {
            fun fromJsonString(jsonStr: String): CustomIconMetadata? {
                return try {
                    val json = org.json.JSONObject(jsonStr)
                    val orig = IconEditState.fromJsonObject(json.optJSONObject("originalState"))
                    val curr = IconEditState.fromJsonObject(json.optJSONObject("currentState"))
                    CustomIconMetadata(originalState = orig, currentState = curr)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Checks if a Bitmap contains transparent/translucent pixels.
     */
    fun hasTransparency(bitmap: Bitmap?): Boolean {
        if (bitmap == null) return false
        if (!bitmap.hasAlpha()) return false
        val width = bitmap.width
        val height = bitmap.height
        val stepX = maxOf(1, width / 40)
        val stepY = maxOf(1, height / 40)
        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val alpha = (bitmap.getPixel(x, y) ushr 24) and 0xFF
                if (alpha < 240) return true
            }
        }
        return false
    }

    /**
     * Loads custom icon edit metadata (original state & current state) from disk.
     */
    fun loadCustomIconMetadata(context: Context, iconKey: String): CustomIconMetadata? {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val metaFile = File(customDir, "${cleanKey}_meta.json")
            if (metaFile.exists()) {
                CustomIconMetadata.fromJsonString(metaFile.readText())
            } else {
                val metaAlt = File(customDir, "${iconKey}_meta.json")
                if (metaAlt.exists()) {
                    CustomIconMetadata.fromJsonString(metaAlt.readText())
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves custom icon edit metadata (original state & current state) to disk.
     */
    fun saveCustomIconMetadata(context: Context, iconKey: String, metadata: CustomIconMetadata): Boolean {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val metaFile = File(customDir, "${cleanKey}_meta.json")
            metaFile.writeText(metadata.toJsonString())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves all saved custom icon file objects.
     */
    fun getCustomIcons(context: Context): List<File> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return emptyList()
        return customDir.listFiles { file -> 
            val ext = file.extension.lowercase()
            val name = file.nameWithoutExtension
            (ext in listOf("png", "jpg", "jpeg", "webp")) && !name.endsWith("_raw") && !name.endsWith("_orig") && !ext.equals("json", ignoreCase = true)
        }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /**
     * Retrieves all saved custom icon file keys.
     */
    fun getAllCustomIcons(context: Context): List<String> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return emptyList()
        return customDir.listFiles { file -> 
            val ext = file.extension.lowercase()
            val name = file.nameWithoutExtension
            (ext in listOf("png", "jpg", "jpeg", "webp")) && !name.endsWith("_raw") && !name.endsWith("_orig") && !ext.equals("json", ignoreCase = true)
        }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.nameWithoutExtension }
            ?.distinct()
            ?: emptyList()
    }

    data class IconCacheStats(
        val totalCount: Int = 0,
        val usedCount: Int = 0,
        val unusedCount: Int = 0,
        val totalBytes: Long = 0L,
        val usedBytes: Long = 0L,
        val unusedBytes: Long = 0L
    ) {
        val activeCount: Int get() = usedCount
        val activeBytes: Long get() = usedBytes
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }

    /**
     * Decodes a Bitmap from a Uri with a maximum dimension constraint, preserving EXIF orientation.
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1200): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }
            val origW = boundsOptions.outWidth
            val origH = boundsOptions.outHeight
            if (origW <= 0 || origH <= 0) return null

            var sampleSize = 1
            while (origW / (sampleSize * 2) >= maxDimension || origH / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decoded = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val exifMatrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> exifMatrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> exifMatrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> exifMatrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> exifMatrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> exifMatrix.postScale(1f, -1f)
                else -> null
            }

            if (exifMatrix.isIdentity) {
                decoded
            } else {
                val oriented = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, exifMatrix, true)
                if (oriented != decoded) {
                    decoded.recycle()
                }
                oriented
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a Bitmap from an existing stored custom icon key.
     */
    fun decodeBitmapFromCustomKey(context: Context, iconKey: String): Bitmap? {
        return try {
            val file = getCustomIconFile(context, iconKey)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes the pristine original source Bitmap before any edits or cropping was applied.
     */
    fun decodeOriginalBitmapFromCustomKey(context: Context, iconKey: String): Bitmap? {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val origFile = File(customDir, "${cleanKey}_orig.png")
            if (origFile.exists()) {
                return BitmapFactory.decodeFile(origFile.absolutePath)
            }
            val origAlt = File(customDir, "${iconKey}_orig.png")
            if (origAlt.exists()) {
                return BitmapFactory.decodeFile(origAlt.absolutePath)
            }
            val rawFile = File(customDir, "${cleanKey}_raw.png")
            if (rawFile.exists()) {
                return BitmapFactory.decodeFile(rawFile.absolutePath)
            }
            val rawAlt = File(customDir, "${iconKey}_raw.png")
            if (rawAlt.exists()) {
                return BitmapFactory.decodeFile(rawAlt.absolutePath)
            }
            val mainFile = getCustomIconFile(context, iconKey)
            if (mainFile.exists()) {
                return BitmapFactory.decodeFile(mainFile.absolutePath)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a raw Bitmap without background color for editing, keeping the crop size.
     */
    fun decodeRawBitmapFromCustomKey(context: Context, iconKey: String): Bitmap? {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val origFile = File(customDir, "${cleanKey}_orig.png")
            if (origFile.exists()) {
                return BitmapFactory.decodeFile(origFile.absolutePath)
            }
            val origAlt = File(customDir, "${iconKey}_orig.png")
            if (origAlt.exists()) {
                return BitmapFactory.decodeFile(origAlt.absolutePath)
            }
            val rawFile = File(customDir, "${cleanKey}_raw.png")
            if (rawFile.exists()) {
                return BitmapFactory.decodeFile(rawFile.absolutePath)
            }
            val rawAlt = File(customDir, "${iconKey}_raw.png")
            if (rawAlt.exists()) {
                return BitmapFactory.decodeFile(rawAlt.absolutePath)
            }
            val mainFile = getCustomIconFile(context, iconKey)
            if (mainFile.exists()) {
                val original = BitmapFactory.decodeFile(mainFile.absolutePath) ?: return null
                stripBackgroundColor(original)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Strips solid background colors from an icon bitmap while preserving foreground content and crop dimensions.
     */
    fun stripBackgroundColor(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= 4 || height <= 4) return source

        val mutableBitmap = try {
            source.copy(Bitmap.Config.ARGB_8888, true)
        } catch (_: Exception) {
            null
        } ?: return source

        val candidates = mutableListOf<Int>()
        fun samplePixel(x: Int, y: Int) {
            val cx = x.coerceIn(0, width - 1)
            val cy = y.coerceIn(0, height - 1)
            val p = mutableBitmap.getPixel(cx, cy)
            if (android.graphics.Color.alpha(p) > 50) {
                candidates.add(p)
            }
        }

        // Try direct corners
        samplePixel(0, 0)
        samplePixel(width - 1, 0)
        samplePixel(0, height - 1)
        samplePixel(width - 1, height - 1)

        // Try inner diagonal sample for circle/squircle crops
        val inset = (minOf(width, height) * 0.16f).toInt().coerceAtLeast(1)
        samplePixel(inset, inset)
        samplePixel(width - 1 - inset, inset)
        samplePixel(inset, height - 1 - inset)
        samplePixel(width - 1 - inset, height - 1 - inset)

        if (candidates.isEmpty()) return mutableBitmap

        fun colorDist(c1: Int, c2: Int): Double {
            val rDiff = android.graphics.Color.red(c1) - android.graphics.Color.red(c2)
            val gDiff = android.graphics.Color.green(c1) - android.graphics.Color.green(c2)
            val bDiff = android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2)
            return Math.sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
        }

        // Check if candidates have a consistent background color (at least 3 match)
        var detectedBg: Int? = null
        for (c in candidates) {
            val matchCount = candidates.count { colorDist(it, c) <= 24.0 }
            if (matchCount >= 3) {
                detectedBg = c
                break
            }
        }

        if (detectedBg == null) {
            return mutableBitmap
        }

        val targetBg = detectedBg
        val visited = BooleanArray(width * height)
        val queue = java.util.ArrayDeque<Int>()

        fun push(x: Int, y: Int) {
            val idx = y * width + x
            if (!visited[idx]) {
                visited[idx] = true
                queue.add(idx)
            }
        }

        // Seed BFS from all borders
        for (x in 0 until width) {
            val pTop = mutableBitmap.getPixel(x, 0)
            if (android.graphics.Color.alpha(pTop) <= 20 || colorDist(pTop, targetBg) <= 35.0) push(x, 0)
            val pBottom = mutableBitmap.getPixel(x, height - 1)
            if (android.graphics.Color.alpha(pBottom) <= 20 || colorDist(pBottom, targetBg) <= 35.0) push(x, height - 1)
        }
        for (y in 0 until height) {
            val pLeft = mutableBitmap.getPixel(0, y)
            if (android.graphics.Color.alpha(pLeft) <= 20 || colorDist(pLeft, targetBg) <= 35.0) push(0, y)
            val pRight = mutableBitmap.getPixel(width - 1, y)
            if (android.graphics.Color.alpha(pRight) <= 20 || colorDist(pRight, targetBg) <= 35.0) push(width - 1, y)
        }

        val dx = intArrayOf(1, -1, 0, 0)
        val dy = intArrayOf(0, 0, 1, -1)

        while (!queue.isEmpty()) {
            val curr = queue.poll() ?: break
            val cx = curr % width
            val cy = curr / width
            val color = mutableBitmap.getPixel(cx, cy)
            val alpha = android.graphics.Color.alpha(color)

            if (alpha > 0) {
                if (colorDist(color, targetBg) <= 35.0) {
                    mutableBitmap.setPixel(cx, cy, 0) // Make transparent
                    for (i in 0 until 4) {
                        val nx = cx + dx[i]
                        val ny = cy + dy[i]
                        if (nx in 0 until width && ny in 0 until height) {
                            val nIdx = ny * width + nx
                            if (!visited[nIdx]) {
                                visited[nIdx] = true
                                queue.add(nIdx)
                            }
                        }
                    }
                }
            } else {
                for (i in 0 until 4) {
                    val nx = cx + dx[i]
                    val ny = cy + dy[i]
                    if (nx in 0 until width && ny in 0 until height) {
                        val nIdx = ny * width + nx
                        if (!visited[nIdx]) {
                            visited[nIdx] = true
                            queue.add(nIdx)
                        }
                    }
                }
            }
        }

        return mutableBitmap
    }

    /**
     * Decodes a raw Bitmap without background color from any icon representation.
     */
    fun decodeRawBitmapFromAnyIcon(context: Context, iconKey: String?, targetSize: Int = 512): Bitmap? {
        if (iconKey.isNullOrBlank()) return null
        return try {
            if (isCustomIcon(iconKey)) {
                decodeRawBitmapFromCustomKey(context, iconKey)
            } else {
                decodeBitmapFromAnyIcon(context, iconKey, targetSize)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a Bitmap from any icon representation: custom key, drawable res name, initials, or vector.
     */
    fun decodeBitmapFromAnyIcon(context: Context, iconKey: String?, targetSize: Int = 512): Bitmap? {
        if (iconKey.isNullOrBlank()) return null
        return try {
            if (isInitialsIcon(iconKey)) {
                val initials = getInitialsFromIconName(iconKey)
                val bgCol = getInitialsColor(initials)
                val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.rgb(
                        (bgCol.red * 255).toInt(),
                        (bgCol.green * 255).toInt(),
                        (bgCol.blue * 255).toInt()
                    )
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, paint)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = targetSize * 0.42f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                val textBounds = Rect()
                textPaint.getTextBounds(initials, 0, initials.length, textBounds)
                val y = targetSize / 2f - textBounds.exactCenterY()
                canvas.drawText(initials, targetSize / 2f, y, textPaint)
                bitmap
            } else if (isCustomIcon(iconKey)) {
                decodeBitmapFromCustomKey(context, iconKey)
            } else {
                val drawableRes = getDrawableResId(iconKey)
                if (drawableRes != null) {
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableRes) ?: return null
                    val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders a transformed (scaled, rotated, translated, flipped, and masked) Bitmap.
     * [panXRatio] and [panYRatio] are normalized pan offsets relative to the crop viewport diameter.
     */
    fun renderTransformedBitmap(
        sourceBitmap: Bitmap,
        scale: Float,
        rotationDegrees: Float,
        panXRatio: Float,
        panYRatio: Float,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false,
        isCircleShape: Boolean = true,
        outputSize: Int = 384
    ): Bitmap {
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
            isFilterBitmap = true
        }

        // Apply masking shape
        if (isCircleShape) {
            val path = Path().apply {
                addCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f, Path.Direction.CW)
            }
            canvas.clipPath(path)
        } else {
            val cornerRadius = outputSize * 0.18f
            val rectF = RectF(0f, 0f, outputSize.toFloat(), outputSize.toFloat())
            val path = Path().apply {
                addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            canvas.clipPath(path)
        }

        val matrix = Matrix()
        val srcW = sourceBitmap.width.toFloat()
        val srcH = sourceBitmap.height.toFloat()
        val baseScale = outputSize.toFloat() / maxOf(srcW, srcH)

        // 1. Move bitmap center to origin (0,0)
        matrix.postTranslate(-srcW / 2f, -srcH / 2f)

        // 2. Scale with flip
        val effectiveScaleX = if (flipHorizontal) -scale * baseScale else scale * baseScale
        val effectiveScaleY = if (flipVertical) -scale * baseScale else scale * baseScale
        matrix.postScale(effectiveScaleX, effectiveScaleY)

        // 3. Rotate around origin
        matrix.postRotate(rotationDegrees)

        // 4. Translate to output center + normalized pan offset
        val outputPanX = panXRatio * outputSize.toFloat()
        val outputPanY = panYRatio * outputSize.toFloat()
        matrix.postTranslate((outputSize / 2f) + outputPanX, (outputSize / 2f) + outputPanY)

        canvas.drawBitmap(sourceBitmap, matrix, paint)
        return output
    }

    /**
     * Renders a pixel-perfect WYSIWYG cropped Bitmap matching the interactive screen viewport.
     * [cropBoxSizePx] is the on-screen size of the crop frame in pixels.
     * [effectiveScreenScale] is the total scale applied on screen.
     * [panOffsetScreenX], [panOffsetScreenY] are the screen pixel offsets from the crop frame center.
     * [rotationDegrees] is the total rotation in degrees.
     */
    fun renderWysiwygCroppedBitmap(
        sourceBitmap: Bitmap,
        cropBoxSizePx: Float,
        effectiveScreenScale: Float,
        panOffsetScreenX: Float,
        panOffsetScreenY: Float,
        rotationDegrees: Float,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false,
        cropShape: String = "circle", // "circle", "rounded_square", "square"
        colorMatrixValues: FloatArray? = null,
        backgroundColor: Int? = null,
        outputSize: Int = 512
    ): Bitmap {
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Apply boundary shape masking
        when (cropShape.lowercase()) {
            "circle" -> {
                val path = Path().apply {
                    addCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f, Path.Direction.CW)
                }
                canvas.clipPath(path)
            }
            "rounded_square" -> {
                val cornerRadius = outputSize * 0.20f
                val rectF = RectF(0f, 0f, outputSize.toFloat(), outputSize.toFloat())
                val path = Path().apply {
                    addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
                }
                canvas.clipPath(path)
            }
            else -> {
                // Square: keep full rectangular area
            }
        }

        // Draw solid background color if specified (e.g. for transparent icons)
        if (backgroundColor != null) {
            canvas.drawColor(backgroundColor)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
            isFilterBitmap = true
            if (colorMatrixValues != null) {
                colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrixValues)
            }
        }

        val srcW = sourceBitmap.width.toFloat()
        val srcH = sourceBitmap.height.toFloat()
        val ratio = if (cropBoxSizePx > 0f) outputSize.toFloat() / cropBoxSizePx else 1f

        val matrix = Matrix()
        // 1. Move bitmap center to origin
        matrix.postTranslate(-srcW / 2f, -srcH / 2f)

        // 2. Rotate around center
        matrix.postRotate(rotationDegrees)

        // 3. Flip & Scale to output
        val outScale = effectiveScreenScale * ratio
        val outScaleX = if (flipHorizontal) -outScale else outScale
        val outScaleY = if (flipVertical) -outScale else outScale
        matrix.postScale(outScaleX, outScaleY)

        // 4. Translate to output center + scaled pan offset
        val outPanX = panOffsetScreenX * ratio
        val outPanY = panOffsetScreenY * ratio
        matrix.postTranslate((outputSize / 2f) + outPanX, (outputSize / 2f) + outPanY)

        canvas.drawBitmap(sourceBitmap, matrix, paint)
        return output
    }

    /**
     * Saves a Bitmap directly into the app's internal custom icons directory with optimal compression.
     * Supports persisting original source image and original vs current editing metadata.
     */
    fun saveCustomIconBitmap(
        context: Context,
        bitmap: Bitmap,
        rawBitmap: Bitmap? = null,
        sourceOrigBitmap: Bitmap? = null,
        targetIconKey: String? = null,
        originalState: IconEditState? = null,
        currentState: IconEditState? = null
    ): String? {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()

            val isExistingKey = !targetIconKey.isNullOrBlank()
            val iconKey = if (isExistingKey) targetIconKey!! else "custom_icon_${System.currentTimeMillis()}"
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val destFile = File(customDir, "$cleanKey.png")

            // Ensure optimized size (up to 160x160 for crisp icons on high-res screens while keeping file sizes extremely small ~6-10KB)
            val maxDimension = 160
            val scaleFactor = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            } else 1f

            val optimizedBitmap = if (scaleFactor < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scaleFactor).toInt().coerceAtLeast(1),
                    (bitmap.height * scaleFactor).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                bitmap
            }

            FileOutputStream(destFile).use { outStream ->
                optimizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                outStream.flush()
            }

            // Save companion raw bitmap if available
            val rawToSave = rawBitmap ?: stripBackgroundColor(bitmap)
            val rawFile = File(customDir, "${cleanKey}_raw.png")
            val optRaw = if (scaleFactor < 1f) {
                Bitmap.createScaledBitmap(
                    rawToSave,
                    (rawToSave.width * scaleFactor).toInt().coerceAtLeast(1),
                    (rawToSave.height * scaleFactor).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                rawToSave
            }
            FileOutputStream(rawFile).use { outStream ->
                optRaw.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                outStream.flush()
            }

            // Save pristine original unedited source bitmap (ONLY on first creation or if missing, never overwrite existing original!)
            val origFile = File(customDir, "${cleanKey}_orig.png")
            if (sourceOrigBitmap != null && (!origFile.exists() || !isExistingKey)) {
                val origMaxDim = 1024
                val origScale = if (sourceOrigBitmap.width > origMaxDim || sourceOrigBitmap.height > origMaxDim) {
                    origMaxDim.toFloat() / maxOf(sourceOrigBitmap.width, sourceOrigBitmap.height)
                } else 1f
                val optOrig = if (origScale < 1f) {
                    Bitmap.createScaledBitmap(
                        sourceOrigBitmap,
                        (sourceOrigBitmap.width * origScale).toInt().coerceAtLeast(1),
                        (sourceOrigBitmap.height * origScale).toInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    sourceOrigBitmap
                }
                FileOutputStream(origFile).use { outStream ->
                    optOrig.compress(Bitmap.CompressFormat.PNG, 95, outStream)
                    outStream.flush()
                }
            }

            // Save metadata (original state is strictly preserved and never overwritten when editing)
            if (originalState != null && currentState != null) {
                val existingMeta = loadCustomIconMetadata(context, iconKey)
                val preservedOriginalState = existingMeta?.originalState ?: originalState
                val metadata = CustomIconMetadata(
                    originalState = preservedOriginalState,
                    currentState = currentState
                )
                saveCustomIconMetadata(context, iconKey, metadata)
            }

            iconKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class CustomIconFileInfo(
        val iconKey: String,
        val file: File,
        val sizeBytes: Long,
        val lastModifiedEpochMs: Long
    )

    /**
     * Comprehensively queries all active icon names across Categories, Accounts,
     * Savings Goals, Item Image Cache, and all database entities.
     */
    suspend fun queryAllActiveIconNames(context: Context): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                val db = com.example.data.local.AppDatabase.getDatabase(context)
                val catIcons = db.categoryDao().getAllCategoriesSnapshot().map { it.iconName }
                val accIcons = db.accountDao().getAllAccountsSnapshot().map { it.iconName }
                val goalIcons = db.savingsGoalDao().getAllGoals().firstOrNull()?.map { it.iconName } ?: emptyList()
                val cachedItemIcons = db.itemImageCacheDao().getAllCachedItemsSnapshot().map { it.iconKey }
                (catIcons + accIcons + goalIcons + cachedItemIcons)
                    .filter { it.isNotBlank() }
                    .toSet()
            } catch (_: Exception) {
                emptySet()
            }
        }
    }

    /**
     * Computes statistics about unused and used custom icons currently stored on disk.
     */
    fun getUnusedCustomIconsStats(context: Context, activeIconNames: Set<String>): IconCacheStats {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return IconCacheStats()

        val allFiles = customDir.listFiles { file ->
            val ext = file.extension.lowercase()
            val name = file.nameWithoutExtension
            (ext in listOf("png", "jpg", "jpeg", "webp")) && !name.endsWith("_raw") && !name.endsWith("_orig") && !ext.equals("json", ignoreCase = true)
        } ?: return IconCacheStats()

        var totalBytes = 0L
        var unusedBytes = 0L
        var usedBytes = 0L
        var unusedCount = 0
        var usedCount = 0

        val normalizedActive = activeIconNames.flatMap {
            val raw = it.trim()
            listOf(
                raw,
                raw.removeSuffix(".png").removeSuffix(".jpg").removeSuffix(".jpeg").removeSuffix(".webp"),
                raw.substringAfterLast("/").substringBeforeLast(".")
            )
        }.filter { it.isNotBlank() }.toSet()

        for (file in allFiles) {
            val size = file.length()
            totalBytes += size
            val nameNoExt = file.nameWithoutExtension
            if (nameNoExt in normalizedActive || file.name in activeIconNames || file.name in normalizedActive) {
                usedCount++
                usedBytes += size
            } else {
                unusedCount++
                unusedBytes += size
            }
        }

        return IconCacheStats(
            totalCount = allFiles.size,
            usedCount = usedCount,
            unusedCount = unusedCount,
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            unusedBytes = unusedBytes
        )
    }

    /**
     * Returns a list of detailed file info objects for all unused custom icons.
     */
    fun getUnusedCustomIconsList(context: Context, activeIconNames: Set<String>): List<CustomIconFileInfo> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return emptyList()

        val allFiles = customDir.listFiles { file ->
            val ext = file.extension.lowercase()
            val name = file.nameWithoutExtension
            (ext in listOf("png", "jpg", "jpeg", "webp")) && !name.endsWith("_raw") && !name.endsWith("_orig") && !ext.equals("json", ignoreCase = true)
        } ?: return emptyList()

        val normalizedActive = activeIconNames.flatMap {
            val raw = it.trim()
            listOf(
                raw,
                raw.removeSuffix(".png").removeSuffix(".jpg").removeSuffix(".jpeg").removeSuffix(".webp"),
                raw.substringAfterLast("/").substringBeforeLast(".")
            )
        }.filter { it.isNotBlank() }.toSet()

        val unusedList = mutableListOf<CustomIconFileInfo>()
        for (file in allFiles) {
            val nameNoExt = file.nameWithoutExtension
            val fileName = file.name
            val isUsed = nameNoExt in normalizedActive || fileName in normalizedActive || activeIconNames.contains(nameNoExt) || activeIconNames.contains(fileName)
            if (!isUsed) {
                unusedList.add(
                    CustomIconFileInfo(
                        iconKey = nameNoExt,
                        file = file,
                        sizeBytes = file.length(),
                        lastModifiedEpochMs = file.lastModified()
                    )
                )
            }
        }
        return unusedList.sortedByDescending { it.lastModifiedEpochMs }
    }

    /**
     * Deletes all custom icon files that are not referenced in the database.
     * Returns Pair(number_of_files_deleted, bytes_freed).
     */
    fun clearUnusedCustomIcons(context: Context, activeIconNames: Set<String>): Pair<Int, Long> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return 0 to 0L

        val allFiles = customDir.listFiles() ?: return 0 to 0L

        var deletedCount = 0
        var bytesFreed = 0L

        val normalizedActive = activeIconNames.flatMap {
            val raw = it.trim()
            listOf(
                raw,
                raw.removeSuffix(".png").removeSuffix(".jpg").removeSuffix(".jpeg").removeSuffix(".webp"),
                raw.substringAfterLast("/").substringBeforeLast(".")
            )
        }.filter { it.isNotBlank() }.toSet()

        for (file in allFiles) {
            val nameNoExt = file.nameWithoutExtension
            val baseKey = nameNoExt.removeSuffix("_raw").removeSuffix("_orig").removeSuffix("_meta")
            if (baseKey !in normalizedActive && file.name !in activeIconNames && baseKey !in activeIconNames) {
                val size = file.length()
                if (file.delete()) {
                    deletedCount++
                    bytesFreed += size
                }
            }
        }

        return deletedCount to bytesFreed
    }

    /**
     * Deletes specific custom icons by their keys.
     * Returns Pair(number_of_files_deleted, bytes_freed).
     */
    fun deleteSelectedCustomIcons(context: Context, iconKeys: Set<String>): Pair<Int, Long> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return 0 to 0L

        var deletedCount = 0
        var bytesFreed = 0L

        for (key in iconKeys) {
            val cleanKey = if (key.startsWith("custom_icon_")) key else "custom_icon_$key"
            listOf(
                getCustomIconFile(context, key),
                File(customDir, "$cleanKey.png"),
                File(customDir, "${cleanKey}_raw.png"),
                File(customDir, "${cleanKey}_orig.png"),
                File(customDir, "${cleanKey}_meta.json"),
                File(customDir, "${key}_raw.png"),
                File(customDir, "${key}_orig.png"),
                File(customDir, "${key}_meta.json")
            ).distinct().forEach { file ->
                if (file.exists()) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        bytesFreed += size
                    }
                }
            }
        }

        return deletedCount to bytesFreed
    }

    /**
     * Imports a user-selected image from a Uri into the app's internal custom icons directory.
     * Automatically downscales to a crisp square (up to 256x256) to maintain optimal performance.
     */
    fun saveCustomIconFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            val isTrans = hasTransparency(originalBitmap)
            val editState = IconEditState(bgColorHex = if (isTrans) null else null)

            saveCustomIconBitmap(
                context = context,
                bitmap = originalBitmap,
                rawBitmap = if (isTrans) originalBitmap else stripBackgroundColor(originalBitmap),
                sourceOrigBitmap = originalBitmap,
                originalState = editState,
                currentState = editState
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a custom icon from internal storage.
     */
    fun deleteCustomIcon(context: Context, iconKey: String): Boolean {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            val cleanKey = if (iconKey.startsWith("custom_icon_")) iconKey else "custom_icon_$iconKey"
            val file = getCustomIconFile(context, iconKey)
            
            File(customDir, "${cleanKey}_raw.png").let { if (it.exists()) it.delete() }
            File(customDir, "${cleanKey}_orig.png").let { if (it.exists()) it.delete() }
            File(customDir, "${cleanKey}_meta.json").let { if (it.exists()) it.delete() }
            File(customDir, "${iconKey}_raw.png").let { if (it.exists()) it.delete() }
            File(customDir, "${iconKey}_orig.png").let { if (it.exists()) it.delete() }
            File(customDir, "${iconKey}_meta.json").let { if (it.exists()) it.delete() }
            
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes all custom icon files from internal storage.
     */
    fun deleteAllCustomIcons(context: Context): Int {
        return try {
            val customDir = File(context.filesDir, "custom_icons")
            var count = 0
            if (customDir.exists()) {
                customDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        if (file.delete()) count++
                    }
                }
            }
            count
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Maps named bank and MFS logos to their respective XML Vector Drawables.
     */
    fun getDrawableResId(iconName: String?): Int? {
        return when (iconName) {
            "BankBkash" -> com.example.R.drawable.ic_bank_bkash
            "BankBkashAlt" -> com.example.R.drawable.ic_bank_bkash_alt
            "BankNagad" -> com.example.R.drawable.ic_bank_nagad
            "BankNagadAlt" -> com.example.R.drawable.ic_bank_nagad_alt
            "BankRocket" -> com.example.R.drawable.ic_bank_rocket
            "BankRocketAlt" -> com.example.R.drawable.ic_bank_rocket_alt
            "BankUpay" -> com.example.R.drawable.ic_bank_upay
            "BankUpayAlt" -> com.example.R.drawable.ic_bank_upay_alt
            "BankCellfin" -> com.example.R.drawable.ic_bank_cellfin
            "BankCellfinAlt" -> com.example.R.drawable.ic_bank_cellfin_alt
            "BankDBBL" -> com.example.R.drawable.ic_bank_dbbl
            "BankDBBLAlt" -> com.example.R.drawable.ic_bank_dbbl_alt
            "BankIBBL" -> com.example.R.drawable.ic_bank_ibbl
            "BankIBBLAlt" -> com.example.R.drawable.ic_bank_ibbl_alt
            "BankBRAC" -> com.example.R.drawable.ic_bank_brac
            "BankAstha" -> com.example.R.drawable.ic_bank_astha
            "BankCity" -> com.example.R.drawable.ic_bank_city
            "BankCitytouch" -> com.example.R.drawable.ic_bank_citytouch
            "BankSonali" -> com.example.R.drawable.ic_bank_sonali
            "BankSonaliSheba" -> com.example.R.drawable.ic_bank_sonali_sheba
            "BankEBL" -> com.example.R.drawable.ic_bank_ebl
            "BankSkybanking" -> com.example.R.drawable.ic_bank_skybanking
            "BankSCB" -> com.example.R.drawable.ic_bank_scb
            "BankSCBAlt" -> com.example.R.drawable.ic_bank_scb_alt
            "BankHSBC" -> com.example.R.drawable.ic_bank_hsbc
            "BankHSBCAlt" -> com.example.R.drawable.ic_bank_hsbc_alt
            "BankPrime" -> com.example.R.drawable.ic_bank_prime
            "BankPrimeAlt" -> com.example.R.drawable.ic_bank_prime_alt
            "BankUCB" -> com.example.R.drawable.ic_bank_ucb
            "BankUCBAlt" -> com.example.R.drawable.ic_bank_ucb_alt
            "BankMTB" -> com.example.R.drawable.ic_bank_mtb
            "BankMTBAlt" -> com.example.R.drawable.ic_bank_mtb_alt
            "BankSoutheast" -> com.example.R.drawable.ic_bank_southeast
            "BankSoutheastAlt" -> com.example.R.drawable.ic_bank_southeast_alt
            "BankPubali" -> com.example.R.drawable.ic_bank_pubali
            "BankPubaliAlt" -> com.example.R.drawable.ic_bank_pubali_alt
            "BankDhaka" -> com.example.R.drawable.ic_bank_dhaka
            "BankDhakaAlt" -> com.example.R.drawable.ic_bank_dhaka_alt
            "BankTrust" -> com.example.R.drawable.ic_bank_trust
            "BankTap" -> com.example.R.drawable.ic_bank_tap
            "BankSureCash" -> com.example.R.drawable.ic_bank_surecash
            "BankSureCashAlt" -> com.example.R.drawable.ic_bank_surecash_alt
            "BankAgrani" -> com.example.R.drawable.ic_bank_agrani
            "BankAgraniAlt" -> com.example.R.drawable.ic_bank_agrani_alt
            "BankJanata" -> com.example.R.drawable.ic_bank_janata
            "BankJanataAlt" -> com.example.R.drawable.ic_bank_janata_alt
            "BankMCash" -> com.example.R.drawable.ic_bank_mcash
            "BankAB" -> com.example.R.drawable.ic_bank_ab
            "BankABAlt" -> com.example.R.drawable.ic_bank_ab_alt
            else -> null
        }
    }

    /**
     * Checks if the icon is a built-in multi-color drawable vector.
     */
    fun isDrawableIcon(iconName: String?): Boolean {
        return getDrawableResId(iconName) != null
    }

    /**
     * Checks if the icon identifier represents a known valid icon in the application.
     */
    fun isKnownIcon(iconName: String?): Boolean {
        if (iconName.isNullOrBlank()) return false
        if (iconName == "Category" || iconName == "Folder" || iconName.equals("none", ignoreCase = true) || iconName.equals("null", ignoreCase = true) || iconName.equals("NA", ignoreCase = true)) return false
        if (isInitialsIcon(iconName)) return true
        if (isDrawableIcon(iconName)) return true
        if (isCustomIcon(iconName)) return true
        return BUILTIN_ICONS.any { it.name.equals(iconName, ignoreCase = true) }
    }

    /**
     * Checks if the icon requires edge-to-edge / full surface rendering (e.g. logos, custom images, initials).
     */
    fun isFullSurfaceIcon(iconName: String?): Boolean {
        return isCustomIcon(iconName) || isDrawableIcon(iconName) || isInitialsIcon(iconName)
    }

    /**
     * Universal Composable to render either a built-in Material Icon, a Drawable Bank Logo, a Custom Image Icon, or an Initials Monogram Avatar.
     * Guaranteed to never render empty or broken icons.
     */
    @Composable
    fun AppIcon(
        iconName: String?,
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current,
        fallbackName: String? = null
    ) {
        val effectiveIcon = when {
            isKnownIcon(iconName) && iconName != "AccountBalance" && iconName != "Category" && iconName != "Folder" -> iconName!!
            !fallbackName.isNullOrBlank() && (iconName.isNullOrBlank() || iconName == "AccountBalance" || iconName == "Category" || iconName == "Folder") -> resolveBestIconOrInitials(fallbackName)
            isKnownIcon(iconName) -> iconName!!
            !fallbackName.isNullOrBlank() -> resolveBestIconOrInitials(fallbackName)
            !contentDescription.isNullOrBlank() -> resolveBestIconOrInitials(contentDescription)
            !iconName.isNullOrBlank() && iconName != "Category" && iconName != "Folder" && !iconName.equals("none", ignoreCase = true) && !iconName.equals("null", ignoreCase = true) -> resolveBestIconOrInitials(iconName)
            else -> "AccountBalanceWallet"
        }

        val drawableRes = getDrawableResId(effectiveIcon)
        if (drawableRes != null) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = drawableRes),
                contentDescription = contentDescription,
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Fit
            )
            return
        }

        if (isInitialsIcon(effectiveIcon)) {
            val initials = getInitialsFromIconName(effectiveIcon)
            val bgCol = getInitialsColor(initials)
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = modifier
                    .clip(CircleShape)
                    .background(bgCol),
                contentAlignment = Alignment.Center
            ) {
                val minDim = if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified && maxHeight != androidx.compose.ui.unit.Dp.Unspecified) {
                    minOf(maxWidth, maxHeight)
                } else if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified) {
                    maxWidth
                } else {
                    24.dp
                }
                val rawSp = when {
                    initials.length == 1 -> (minDim.value * 0.52f).coerceIn(9f, 24f)
                    initials.length == 2 -> (minDim.value * 0.44f).coerceIn(8f, 20f)
                    else -> (minDim.value * 0.35f).coerceIn(7f, 16f)
                }
                val fontSize = rawSp.sp
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        if (isCustomIcon(effectiveIcon)) {
            val context = LocalContext.current
            val model: Any = if (effectiveIcon.startsWith("http://") || effectiveIcon.startsWith("https://")) {
                effectiveIcon
            } else if (effectiveIcon.startsWith("content://") || effectiveIcon.startsWith("file://")) {
                android.net.Uri.parse(effectiveIcon)
            } else {
                getCustomIconFile(context, effectiveIcon)
            }
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
            return
        } else {
            Icon(
                imageVector = getIconByName(effectiveIcon),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }

    /**
     * Utility method to safely parse a hex color string or return a fallback color.
     */
    fun parseColorHex(hex: String?, fallback: Color = Color(0xFF2196F3)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val clean = hex.removePrefix("#")
            val colorLong = when (clean.length) {
                6 -> ("FF$clean").toLong(16)
                8 -> clean.toLong(16)
                else -> return fallback
            }
            Color(colorLong)
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Resolves an account's own icon rather than falling back to its parent account group icon.
     * Guaranteed to return the account's brand, custom image, or personalized initials avatar.
     */
    fun resolveAccountOwnIcon(account: com.example.data.model.Account?, parentAccount: com.example.data.model.Account? = null): String {
        if (account == null) return "AccountBalanceWallet"

        // If account has an explicit custom icon, drawable bank/wallet logo, or initials:
        if (isCustomIcon(account.iconName) ||
            isDrawableIcon(account.iconName) ||
            isInitialsIcon(account.iconName)
        ) {
            return account.iconName
        }

        val parentIcon = parentAccount?.iconName
        val isGenericGroupIcon = account.iconName.isNullOrBlank() ||
                account.iconName == "AccountBalance" ||
                account.iconName == "Category" ||
                account.iconName == "Folder" ||
                account.iconName == "CreditCard" ||
                account.iconName == "Wallet" ||
                account.iconName == "MoneyOff" ||
                (parentIcon != null && account.iconName.equals(parentIcon, ignoreCase = true))

        if (!isGenericGroupIcon && isKnownIcon(account.iconName)) {
            return account.iconName
        }

        // Resolve account's own icon from its specific account name (e.g. bKash, DBBL, Nagad, City Bank, or Initials)
        val accountName = account.nameEn.ifBlank { account.nameBn }
        val resolvedFromName = resolveBestIconOrInitials(accountName)
        if (resolvedFromName.isNotBlank() && resolvedFromName != "Category" && resolvedFromName != "Folder" && resolvedFromName != "AccountBalance") {
            return resolvedFromName
        }

        return if (isKnownIcon(account.iconName)) account.iconName else "AccountBalanceWallet"
    }

    /**
     * Renders a badge for an account with its own icon and color, taking into account
     * custom drawable logos, full surface images, and monogram initials.
     */
    @Composable
    fun AccountBadge(
        account: com.example.data.model.Account?,
        parentAccount: com.example.data.model.Account? = null,
        modifier: Modifier = Modifier,
        size: androidx.compose.ui.unit.Dp = 28.dp,
        iconSize: androidx.compose.ui.unit.Dp = 18.dp,
        fallbackColor: Color = Color(0xFF2563EB),
        languageMode: com.example.data.model.LanguageMode = com.example.data.model.LanguageMode.ENGLISH
    ) {
        val ownIcon = resolveAccountOwnIcon(account, parentAccount)
        val isFullSurface = isFullSurfaceIcon(ownIcon)
        val accColor = parseColorHex(account?.colorHex, fallbackColor)

        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isFullSurface) Color.Transparent
                    else accColor.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                iconName = ownIcon,
                fallbackName = account?.nameEn?.ifBlank { account.nameBn },
                contentDescription = account?.nameEn,
                tint = if (isFullSurface) Color.Unspecified else accColor,
                modifier = Modifier.size(if (isFullSurface) size else iconSize)
            )
        }
    }
}
