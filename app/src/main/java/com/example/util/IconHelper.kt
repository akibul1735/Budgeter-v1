package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class IconItem(
    val name: String,
    val category: String,
    val tags: List<String> = emptyList()
)

object IconHelper {

    fun parseColorHex(hex: String?, fallback: Color = Color(0xFF1E56A0)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }

    fun isCustomIcon(iconName: String?): Boolean {
        if (iconName.isNullOrBlank()) return false
        return iconName.startsWith("custom:") ||
                iconName.startsWith("file:") ||
                iconName.startsWith("custom_") ||
                iconName.endsWith(".png") ||
                iconName.endsWith(".jpg") ||
                iconName.endsWith(".jpeg") ||
                iconName.endsWith(".webp")
    }

    fun getCustomIconsDir(context: Context): File {
        val dir = File(context.filesDir, "custom_icons")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getCustomIconFile(context: Context, iconName: String): File? {
        val fileName = if (iconName.startsWith("custom:")) {
            iconName.removePrefix("custom:")
        } else if (iconName.startsWith("file:")) {
            iconName.removePrefix("file:")
        } else {
            iconName
        }
        val file = File(getCustomIconsDir(context), fileName)
        return if (file.exists()) file else null
    }

    fun getAllCustomIcons(context: Context): List<String> {
        val dir = getCustomIconsDir(context)
        val files = dir.listFiles { _, name ->
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp")
        } ?: return emptyList()

        return files.sortedByDescending { it.lastModified() }
            .map { "custom:${it.name}" }
    }

    fun saveCustomIconFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Crop to center square
            val dimension = minOf(originalBitmap.width, originalBitmap.height)
            val xOffset = (originalBitmap.width - dimension) / 2
            val yOffset = (originalBitmap.height - dimension) / 2
            val squareBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, dimension, dimension)

            // Resize to standard 256x256
            val scaledBitmap = if (dimension > 256) {
                Bitmap.createScaledBitmap(squareBitmap, 256, 256, true)
            } else {
                squareBitmap
            }

            val dir = getCustomIconsDir(context)
            val fileName = "custom_${System.currentTimeMillis()}.png"
            val targetFile = File(dir, fileName)

            val outputStream = FileOutputStream(targetFile)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            "custom:$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteCustomIcon(context: Context, iconName: String): Boolean {
        val file = getCustomIconFile(context, iconName)
        return file?.delete() ?: false
    }

    val CATEGORIES = listOf(
        "All",
        "Custom",
        "Finance",
        "Food & Drinks",
        "Shopping",
        "Transport",
        "Bills & Housing",
        "Health",
        "Life & Work",
        "Entertainment",
        "Tech & Tools",
        "Symbols"
    )

    val BUILTIN_ICONS: List<IconItem> = listOf(
        // Finance & Banking
        IconItem("AccountBalance", "Finance", listOf("bank", "institution", "governor", "finance")),
        IconItem("AccountBalanceWallet", "Finance", listOf("wallet", "money", "cash", "funds")),
        IconItem("Wallet", "Finance", listOf("purse", "pocket", "money")),
        IconItem("Payments", "Finance", listOf("cash", "bills", "currency", "transfer")),
        IconItem("CreditCard", "Finance", listOf("card", "debit", "visa", "mastercard")),
        IconItem("Savings", "Finance", listOf("piggy", "bank", "invest", "deposit")),
        IconItem("Paid", "Finance", listOf("coin", "dollar", "taka", "payment")),
        IconItem("Toll", "Finance", listOf("fee", "tax", "charge")),
        IconItem("Receipt", "Finance", listOf("bill", "invoice", "slip")),
        IconItem("ReceiptLong", "Finance", listOf("statement", "voucher")),
        IconItem("AttachMoney", "Finance", listOf("dollar", "cash", "salary")),
        IconItem("CurrencyExchange", "Finance", listOf("convert", "forex", "trading")),
        IconItem("MonetizationOn", "Finance", listOf("gold", "coin", "earnings")),
        IconItem("ShowChart", "Finance", listOf("stock", "growth", "graph", "market")),
        IconItem("Timeline", "Finance", listOf("history", "trend", "tracking")),
        IconItem("TrendingUp", "Finance", listOf("gain", "profit", "bullish")),
        IconItem("TrendingDown", "Finance", listOf("loss", "drop", "bearish")),
        IconItem("Analytics", "Finance", listOf("report", "metrics", "stats")),
        IconItem("LocalAtm", "Finance", listOf("atm", "cashout", "withdraw")),
        IconItem("QrCode", "Finance", listOf("scan", "bkash", "nagad", "payment")),
        IconItem("Sell", "Finance", listOf("sale", "discount", "offer")),
        IconItem("PriceCheck", "Finance", listOf("cost", "audit", "verify")),
        IconItem("AccountTree", "Finance", listOf("structure", "hierarchy", "nodes")),
        IconItem("Inventory", "Finance", listOf("stock", "warehouse", "assets")),
        IconItem("PointOfSale", "Finance", listOf("pos", "terminal", "register")),
        IconItem("CardGiftcard", "Finance", listOf("voucher", "gift", "bonus", "reward")),
        IconItem("Redeem", "Finance", listOf("claim", "coupon", "gift")),
        IconItem("Percent", "Finance", listOf("interest", "percentage", "rate")),

        // Food & Drinks
        IconItem("Restaurant", "Food & Drinks", listOf("dine", "eating", "meal")),
        IconItem("DinnerDining", "Food & Drinks", listOf("dinner", "night", "food")),
        IconItem("LunchDining", "Food & Drinks", listOf("lunch", "meal", "burger")),
        IconItem("BreakfastDining", "Food & Drinks", listOf("breakfast", "morning", "egg")),
        IconItem("LocalCafe", "Food & Drinks", listOf("tea", "coffee", "cappuccino")),
        IconItem("Coffee", "Food & Drinks", listOf("espresso", "cafe", "latte")),
        IconItem("Fastfood", "Food & Drinks", listOf("burger", "fries", "junk")),
        IconItem("LocalPizza", "Food & Drinks", listOf("pizza", "slice", "cheese")),
        IconItem("BakeryDining", "Food & Drinks", listOf("bread", "biscuit", "toast")),
        IconItem("RamenDining", "Food & Drinks", listOf("noodles", "pasta", "soup")),
        IconItem("Icecream", "Food & Drinks", listOf("dessert", "cone", "sweet")),
        IconItem("Cake", "Food & Drinks", listOf("birthday", "party", "celebrate")),
        IconItem("LocalBar", "Food & Drinks", listOf("drinks", "juice", "beverage")),
        IconItem("Liquor", "Food & Drinks", listOf("bottle", "alcohol", "wine")),
        IconItem("WineBar", "Food & Drinks", listOf("cocktail", "bar", "glass")),
        IconItem("SetMeal", "Food & Drinks", listOf("fish", "thali", "dish")),
        IconItem("TakeoutDining", "Food & Drinks", listOf("parcel", "delivery", "pack")),
        IconItem("BrunchDining", "Food & Drinks", listOf("snacks", "food")),
        IconItem("SoupKitchen", "Food & Drinks", listOf("cooking", "curry", "pot")),
        IconItem("Kitchen", "Food & Drinks", listOf("fridge", "home food", "grocery")),
        IconItem("LocalGroceryStore", "Food & Drinks", listOf("raw food", "market", "bazaar")),

        // Shopping & Goods
        IconItem("ShoppingCart", "Shopping", listOf("cart", "buy", "supermarket")),
        IconItem("ShoppingBag", "Shopping", listOf("mall", "haul", "store")),
        IconItem("ShoppingBasket", "Shopping", listOf("basket", "goods", "shop")),
        IconItem("Store", "Shopping", listOf("shop", "vendor", "outlet")),
        IconItem("Storefront", "Shopping", listOf("boutique", "showroom")),
        IconItem("LocalMall", "Shopping", listOf("shopping mall", "plaza")),
        IconItem("Checkroom", "Shopping", listOf("clothes", "wardrobe", "dress")),
        IconItem("Diamond", "Shopping", listOf("jewelry", "gem", "gold", "luxury")),
        IconItem("Watch", "Shopping", listOf("wrist", "time", "clock", "accessory")),
        IconItem("FitnessCenter", "Shopping", listOf("gym", "workout", "weights")),
        IconItem("SportsSoccer", "Shopping", listOf("football", "ball", "game")),
        IconItem("SportsBasketball", "Shopping", listOf("hoop", "sports")),
        IconItem("SportsTennis", "Shopping", listOf("racket", "badminton")),
        IconItem("SportsEsports", "Shopping", listOf("gaming", "playstation", "xbox")),
        IconItem("Gamepad", "Shopping", listOf("controller", "joystick")),
        IconItem("Toys", "Shopping", listOf("kids", "play", "doll")),
        IconItem("Backpack", "Shopping", listOf("bag", "travel", "hiking")),
        IconItem("Style", "Shopping", listOf("tag", "fashion", "brand")),

        // Transportation & Travel
        IconItem("DirectionsCar", "Transport", listOf("car", "auto", "vehicle", "cab")),
        IconItem("Train", "Transport", listOf("railway", "metro", "commute")),
        IconItem("TwoWheeler", "Transport", listOf("bike", "motorcycle", "scooter")),
        IconItem("DirectionsBike", "Transport", listOf("bicycle", "cycle", "ride")),
        IconItem("DirectionsBus", "Transport", listOf("bus", "public", "transit")),
        IconItem("DirectionsWalk", "Transport", listOf("foot", "walking", "pedestrian")),
        IconItem("Flight", "Transport", listOf("plane", "airplane", "air")),
        IconItem("FlightTakeoff", "Transport", listOf("departure", "trip", "tour")),
        IconItem("FlightLand", "Transport", listOf("arrival", "return")),
        IconItem("LocalTaxi", "Transport", listOf("uber", "pathao", "cab")),
        IconItem("LocalGasStation", "Transport", listOf("fuel", "petrol", "octane", "cng")),
        IconItem("ElectricCar", "Transport", listOf("ev", "tesla", "charging")),
        IconItem("EvStation", "Transport", listOf("charge", "battery")),
        IconItem("Commute", "Transport", listOf("routine", "journey")),
        IconItem("Subway", "Transport", listOf("underground", "transit")),
        IconItem("Tram", "Transport", listOf("rail", "tramway")),
        IconItem("DirectionsBoat", "Transport", listOf("ship", "launch", "ferry")),
        IconItem("Sailing", "Transport", listOf("boat", "cruise")),
        IconItem("Hotel", "Transport", listOf("stay", "resort", "motel", "room")),
        IconItem("Luggage", "Transport", listOf("baggage", "suitcase", "vacation")),
        IconItem("Map", "Transport", listOf("location", "route", "gps")),
        IconItem("Navigation", "Transport", listOf("direction", "compass")),
        IconItem("Explore", "Transport", listOf("adventure", "safari", "trip")),

        // Bills & Housing
        IconItem("Home", "Bills & Housing", listOf("house", "rent", "residence")),
        IconItem("Apartment", "Bills & Housing", listOf("flat", "building", "society")),
        IconItem("Cottage", "Bills & Housing", listOf("village", "bungalow")),
        IconItem("Villa", "Bills & Housing", listOf("mansion", "estate")),
        IconItem("LocationCity", "Bills & Housing", listOf("city", "property")),
        IconItem("ElectricBolt", "Bills & Housing", listOf("electricity", "current", "power")),
        IconItem("Power", "Bills & Housing", listOf("plug", "socket", "charge")),
        IconItem("WaterDrop", "Bills & Housing", listOf("water", "wasa", "utility")),
        IconItem("Plumbing", "Bills & Housing", listOf("pipe", "sanitary", "faucet")),
        IconItem("Wifi", "Bills & Housing", listOf("internet", "broadband", "fiber")),
        IconItem("Router", "Bills & Housing", listOf("modem", "network", "lan")),
        IconItem("Tv", "Bills & Housing", listOf("television", "dish", "cable")),
        IconItem("DesktopWindows", "Bills & Housing", listOf("pc", "computer", "desktop")),
        IconItem("Laptop", "Bills & Housing", listOf("notebook", "macbook")),
        IconItem("Chair", "Bills & Housing", listOf("furniture", "interior")),
        IconItem("Bed", "Bills & Housing", listOf("mattress", "sleep", "decor")),
        IconItem("Lightbulb", "Bills & Housing", listOf("lighting", "lamp", "idea")),
        IconItem("Build", "Bills & Housing", listOf("repair", "tools", "fix")),
        IconItem("Handyman", "Bills & Housing", listOf("carpenter", "technician")),
        IconItem("Hardware", "Bills & Housing", listOf("screw", "wrench", "material")),
        IconItem("Construction", "Bills & Housing", listOf("building", "cement", "renovation")),
        IconItem("Security", "Bills & Housing", listOf("guard", "cctv", "safe")),
        IconItem("CleaningServices", "Bills & Housing", listOf("maid", "sweep", "dusting")),

        // Health & Medical
        IconItem("LocalHospital", "Health", listOf("hospital", "clinic", "doctor")),
        IconItem("Medication", "Health", listOf("medicine", "tablet", "pharma")),
        IconItem("MedicalServices", "Health", listOf("first aid", "emergency")),
        IconItem("Healing", "Health", listOf("bandage", "treatment")),
        IconItem("LocalPharmacy", "Health", listOf("drugstore", "chemist")),
        IconItem("Vaccines", "Health", listOf("injection", "dose", "syringe")),
        IconItem("Psychology", "Health", listOf("mental", "counseling", "mind")),
        IconItem("Favorite", "Health", listOf("heart", "cardio", "life")),
        IconItem("FavoriteBorder", "Health", listOf("wellness", "care")),
        IconItem("MonitorHeart", "Health", listOf("pulse", "ecg", "pressure")),
        IconItem("Spa", "Health", listOf("massage", "relax", "salon")),
        IconItem("SelfImprovement", "Health", listOf("yoga", "meditation")),
        IconItem("HealthAndSafety", "Health", listOf("protection", "insurance")),

        // Life, Work & Education
        IconItem("School", "Life & Work", listOf("college", "university", "tuition")),
        IconItem("Work", "Life & Work", listOf("job", "office", "career", "salary")),
        IconItem("BusinessCenter", "Life & Work", listOf("briefcase", "corporate")),
        IconItem("Handshake", "Life & Work", listOf("deal", "loan", "agreement", "borrow")),
        IconItem("FamilyRestroom", "Life & Work", listOf("family", "parents", "home")),
        IconItem("ChildCare", "Life & Work", listOf("baby", "kids", "daycare")),
        IconItem("ChildFriendly", "Life & Work", listOf("stroller", "infant")),
        IconItem("Pets", "Life & Work", listOf("cat", "dog", "veterinary", "animal")),
        IconItem("MenuBook", "Life & Work", listOf("book", "reading", "study", "library")),
        IconItem("AutoStories", "Life & Work", listOf("novel", "learning")),
        IconItem("Science", "Life & Work", listOf("lab", "experiment", "research")),
        IconItem("Calculate", "Life & Work", listOf("calculator", "math", "accounting")),
        IconItem("DesignServices", "Life & Work", listOf("creative", "graphic", "art")),
        IconItem("Groups", "Life & Work", listOf("team", "meeting", "colleagues")),
        IconItem("Person", "Life & Work", listOf("profile", "individual", "user")),
        IconItem("People", "Life & Work", listOf("friends", "crowd", "community")),
        IconItem("Celebration", "Life & Work", listOf("event", "party", "anniversary", "eid")),

        // Entertainment & Media
        IconItem("PhoneAndroid", "Entertainment", listOf("mobile", "smartphone", "recharge")),
        IconItem("PhoneIphone", "Entertainment", listOf("apple", "iphone", "ios")),
        IconItem("SmartDisplay", "Entertainment", listOf("screen", "streaming", "ott")),
        IconItem("Movie", "Entertainment", listOf("cinema", "film", "netflix", "hall")),
        IconItem("Theaters", "Entertainment", listOf("show", "play", "stage")),
        IconItem("MusicNote", "Entertainment", listOf("audio", "song", "spotify")),
        IconItem("Headphones", "Entertainment", listOf("earphones", "headset")),
        IconItem("Podcasts", "Entertainment", listOf("talk", "broadcast")),
        IconItem("Radio", "Entertainment", listOf("fm", "wireless")),
        IconItem("CameraAlt", "Entertainment", listOf("dslr", "photography", "photos")),
        IconItem("Videocam", "Entertainment", listOf("video", "recording", "vlog")),
        IconItem("LiveTv", "Entertainment", listOf("cable", "broadcasting")),
        IconItem("Casino", "Entertainment", listOf("gaming", "bet", "lottery")),
        IconItem("EmojiEvents", "Entertainment", listOf("trophy", "award", "prize")),
        IconItem("MilitaryTech", "Entertainment", listOf("medal", "honor")),

        // Tech, Tools & Symbols
        IconItem("Category", "Symbols", listOf("general", "group", "folder")),
        IconItem("MoreHoriz", "Symbols", listOf("misc", "others", "extra")),
        IconItem("Star", "Symbols", listOf("favorite", "important", "vip")),
        IconItem("Bookmark", "Symbols", listOf("save", "pinned")),
        IconItem("Label", "Symbols", listOf("tag", "badge")),
        IconItem("Flag", "Symbols", listOf("priority", "target")),
        IconItem("Shield", "Symbols", listOf("security", "safe", "lock")),
        IconItem("VpnKey", "Symbols", listOf("key", "access", "password")),
        IconItem("Lock", "Symbols", listOf("secure", "privacy")),
        IconItem("Notifications", "Symbols", listOf("alert", "reminder", "bell")),
        IconItem("Send", "Symbols", listOf("remit", "transfer", "dispatch")),
        IconItem("Archive", "Symbols", listOf("storage", "history")),
        IconItem("Folder", "Symbols", listOf("directory", "files")),
        IconItem("Grade", "Symbols", listOf("star", "rating")),
        IconItem("ThumbUp", "Symbols", listOf("like", "approval")),
        IconItem("Verified", "Symbols", listOf("certified", "official")),
        IconItem("Eco", "Symbols", listOf("green", "nature", "organic")),
        IconItem("Forest", "Symbols", listOf("trees", "plants", "garden")),
        IconItem("Brush", "Symbols", listOf("paint", "decor", "drawing")),
        IconItem("Palette", "Symbols", listOf("colors", "theme", "art")),
        IconItem("Help", "Symbols", listOf("question", "info", "faq"))
    )

    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            // Finance
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
            "Sell" -> Icons.Default.Sell
            "PriceCheck" -> Icons.Default.PriceCheck
            "AccountTree" -> Icons.Default.AccountTree
            "Inventory" -> Icons.Default.Inventory
            "PointOfSale" -> Icons.Default.PointOfSale
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Redeem" -> Icons.Default.Redeem
            "Percent" -> Icons.Default.Percent

            // Food & Drinks
            "Restaurant" -> Icons.Default.Restaurant
            "DinnerDining" -> Icons.Default.DinnerDining
            "LunchDining" -> Icons.Default.LunchDining
            "BreakfastDining" -> Icons.Default.BreakfastDining
            "LocalCafe" -> Icons.Default.LocalCafe
            "Coffee" -> Icons.Default.Coffee
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

            // Shopping
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "ShoppingBasket" -> Icons.Default.ShoppingBasket
            "Store" -> Icons.Default.Store
            "Storefront" -> Icons.Default.Storefront
            "LocalMall" -> Icons.Default.LocalMall
            "Checkroom" -> Icons.Default.Checkroom
            "Diamond" -> Icons.Default.Diamond
            "Watch" -> Icons.Default.Watch
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "SportsSoccer" -> Icons.Default.SportsSoccer
            "SportsBasketball" -> Icons.Default.SportsBasketball
            "SportsTennis" -> Icons.Default.SportsTennis
            "SportsEsports" -> Icons.Default.SportsEsports
            "Gamepad" -> Icons.Default.Gamepad
            "Toys" -> Icons.Default.Toys
            "Backpack" -> Icons.Default.Backpack
            "Style" -> Icons.Default.Style

            // Transport
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Train" -> Icons.Default.Train
            "TwoWheeler" -> Icons.Default.TwoWheeler
            "DirectionsBike" -> Icons.Default.DirectionsBike
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "DirectionsWalk" -> Icons.Default.DirectionsWalk
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

            // Bills & Housing
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

            // Health
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

            // Life & Work
            "School" -> Icons.Default.School
            "Work" -> Icons.Default.Work
            "BusinessCenter" -> Icons.Default.BusinessCenter
            "Handshake" -> Icons.Default.Handshake
            "FamilyRestroom" -> Icons.Default.FamilyRestroom
            "ChildCare" -> Icons.Default.ChildCare
            "ChildFriendly" -> Icons.Default.ChildFriendly
            "Pets" -> Icons.Default.Pets
            "MenuBook" -> Icons.AutoMirrored.Filled.MenuBook
            "AutoStories" -> Icons.Default.AutoStories
            "Science" -> Icons.Default.Science
            "Calculate" -> Icons.Default.Calculate
            "DesignServices" -> Icons.Default.DesignServices
            "Groups" -> Icons.Default.Groups
            "Person" -> Icons.Default.Person
            "People" -> Icons.Default.People
            "Celebration" -> Icons.Default.Celebration

            // Entertainment
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

            // Symbols
            "Category" -> Icons.Default.Category
            "MoreHoriz" -> Icons.Default.MoreHoriz
            "Star" -> Icons.Default.Star
            "Bookmark" -> Icons.Default.Bookmark
            "Label" -> Icons.AutoMirrored.Filled.Label
            "Flag" -> Icons.Default.Flag
            "Shield" -> Icons.Default.Shield
            "VpnKey" -> Icons.Default.VpnKey
            "Lock" -> Icons.Default.Lock
            "Notifications" -> Icons.Default.Notifications
            "Send" -> Icons.AutoMirrored.Filled.Send
            "Archive" -> Icons.Default.Archive
            "Folder" -> Icons.Default.Folder
            "Grade" -> Icons.Default.Grade
            "ThumbUp" -> Icons.Default.ThumbUp
            "Verified" -> Icons.Default.Verified
            "Eco" -> Icons.Default.Eco
            "Forest" -> Icons.Default.Forest
            "Brush" -> Icons.Default.Brush
            "Palette" -> Icons.Default.Palette
            "Help" -> Icons.AutoMirrored.Filled.Help

            else -> Icons.Default.Category
        }
    }

    @Composable
    fun AppIcon(
        iconName: String,
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current
    ) {
        val context = LocalContext.current
        if (isCustomIcon(iconName)) {
            val file = getCustomIconFile(context, iconName)
            if (file != null && file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = contentDescription,
                    modifier = modifier.clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    tint = tint
                )
            }
        } else {
            Icon(
                imageVector = getIconByName(iconName),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}
