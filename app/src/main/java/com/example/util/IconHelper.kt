package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
        "Finance",
        "Food & Drinks",
        "Shopping",
        "Transport",
        "Bills & Housing",
        "Health",
        "Life & Work",
        "Entertainment",
        "Sports",
        "Tech & Tools",
        "Nature & Weather",
        "Symbols"
    )

    val BUILTIN_ICONS: List<IconItem> = listOf(
        IconItem("AccountBalance", "Finance", listOf("bank", "central bank", "institution", "governor", "finance"), false),
        IconItem("AccountBalanceWallet", "Finance", listOf("wallet", "money", "cash", "funds", "pocket"), false),
        IconItem("Wallet", "Finance", listOf("purse", "pocket", "money", "leather"), false),
        IconItem("Payments", "Finance", listOf("cash", "bills", "currency", "transfer", "remittance"), false),
        IconItem("CreditCard", "Finance", listOf("card", "debit", "visa", "mastercard", "amex"), false),
        IconItem("Savings", "Finance", listOf("piggy bank", "invest", "deposit", "savings"), false),
        IconItem("Paid", "Finance", listOf("coin", "dollar", "taka", "payment", "received"), false),
        IconItem("Toll", "Finance", listOf("fee", "tax", "charge", "toll bridge"), false),
        IconItem("Receipt", "Finance", listOf("bill", "invoice", "slip", "voucher"), false),
        IconItem("ReceiptLong", "Finance", listOf("statement", "long bill", "invoice"), true),
        IconItem("AttachMoney", "Finance", listOf("dollar", "cash", "salary", "wage", "income"), false),
        IconItem("CurrencyExchange", "Finance", listOf("convert", "forex", "trading", "exchange"), false),
        IconItem("MonetizationOn", "Finance", listOf("gold", "coin", "earnings", "revenue"), false),
        IconItem("ShowChart", "Finance", listOf("stock", "growth", "graph", "market", "trend"), true),
        IconItem("Timeline", "Finance", listOf("history", "trend", "tracking", "timeline"), false),
        IconItem("TrendingUp", "Finance", listOf("gain", "profit", "bullish", "increase"), true),
        IconItem("TrendingDown", "Finance", listOf("loss", "drop", "bearish", "decrease"), true),
        IconItem("Analytics", "Finance", listOf("report", "metrics", "stats", "analysis"), false),
        IconItem("LocalAtm", "Finance", listOf("atm", "cashout", "withdraw", "machine"), false),
        IconItem("QrCode", "Finance", listOf("scan", "bkash", "nagad", "payment", "qr"), false),
        IconItem("QrCode2", "Finance", listOf("barcode", "scan", "pay", "qr code"), false),
        IconItem("QrCodeScanner", "Finance", listOf("scanner", "camera", "pay", "scanner"), false),
        IconItem("Sell", "Finance", listOf("sale", "discount", "offer", "tag"), false),
        IconItem("PriceCheck", "Finance", listOf("cost", "audit", "verify", "price check"), false),
        IconItem("PriceChange", "Finance", listOf("rate", "fluctuation", "market", "change"), false),
        IconItem("AccountTree", "Finance", listOf("structure", "hierarchy", "nodes", "branches"), false),
        IconItem("Inventory", "Finance", listOf("stock", "warehouse", "assets", "supplies"), false),
        IconItem("PointOfSale", "Finance", listOf("pos", "terminal", "register", "billing"), false),
        IconItem("CardGiftcard", "Finance", listOf("voucher", "gift", "bonus", "reward", "coupon"), false),
        IconItem("Redeem", "Finance", listOf("claim", "coupon", "gift", "redeem"), false),
        IconItem("Percent", "Finance", listOf("interest", "percentage", "rate", "discount"), false),
        IconItem("Calculate", "Finance", listOf("calculator", "math", "accounting", "sum"), false),
        IconItem("CreditScore", "Finance", listOf("score", "rating", "cibil", "credit history"), false),
        IconItem("RequestQuote", "Finance", listOf("quote", "estimate", "bid", "proposal"), false),
        IconItem("AssuredWorkload", "Finance", listOf("security", "audit", "compliance", "bank"), false),
        IconItem("CurrencyBitcoin", "Finance", listOf("crypto", "btc", "blockchain", "coin"), false),
        IconItem("CurrencyYen", "Finance", listOf("yen", "jpy", "japan", "currency"), false),
        IconItem("CurrencyPound", "Finance", listOf("pound", "gbp", "uk", "sterling"), false),
        IconItem("CurrencyRuble", "Finance", listOf("ruble", "rub", "russia", "currency"), false),
        IconItem("CurrencyFranc", "Finance", listOf("franc", "chf", "swiss", "currency"), false),
        IconItem("CurrencyLira", "Finance", listOf("lira", "try", "turkey", "currency"), false),
        IconItem("CurrencyRupee", "Finance", listOf("rupee", "inr", "india", "currency"), false),
        IconItem("CurrencyYuan", "Finance", listOf("yuan", "cny", "china", "currency"), false),
        IconItem("Euro", "Finance", listOf("euro", "eur", "europe", "currency"), false),
        IconItem("Money", "Finance", listOf("cash", "notes", "paper", "currency"), false),
        IconItem("MoneyOff", "Finance", listOf("free", "no cost", "discount", "waived"), false),
        IconItem("AddCard", "Finance", listOf("new card", "link card", "credit card"), false),
        IconItem("CreditCardOff", "Finance", listOf("block card", "disable card", "expired"), false),
        IconItem("Payment", "Finance", listOf("checkout", "online pay", "gateway"), false),
        IconItem("Contactless", "Finance", listOf("nfc", "tap", "wave", "contactless pay"), false),
        IconItem("RequestPage", "Finance", listOf("request", "invoice page", "billing doc"), false),
        IconItem("Balance", "Finance", listOf("scale", "justice", "equity", "balance sheet"), false),
        IconItem("PieChart", "Finance", listOf("pie", "chart", "breakdown", "distribution"), false),
        IconItem("QueryStats", "Finance", listOf("analytics", "search", "metrics", "stats"), false),
        IconItem("BarChart", "Finance", listOf("bars", "graph", "histogram", "comparison"), false),
        IconItem("DonutSmall", "Finance", listOf("donut", "chart", "portion", "share"), false),
        IconItem("DonutLarge", "Finance", listOf("ring", "donut", "budget split", "graph"), false),
        IconItem("InsertChart", "Finance", listOf("chart", "insert", "graph", "records"), false),
        IconItem("Restaurant", "Food & Drinks", listOf("dine", "eating", "meal", "food"), false),
        IconItem("DinnerDining", "Food & Drinks", listOf("dinner", "night", "food", "evening meal"), false),
        IconItem("LunchDining", "Food & Drinks", listOf("lunch", "meal", "burger", "afternoon"), false),
        IconItem("BreakfastDining", "Food & Drinks", listOf("breakfast", "morning", "egg", "toast"), false),
        IconItem("LocalCafe", "Food & Drinks", listOf("tea", "coffee", "cappuccino", "cafe"), false),
        IconItem("Coffee", "Food & Drinks", listOf("espresso", "cafe", "latte", "mocha"), false),
        IconItem("CoffeeMaker", "Food & Drinks", listOf("brewing", "machine", "coffee pot"), false),
        IconItem("Fastfood", "Food & Drinks", listOf("burger", "fries", "junk food", "snack"), false),
        IconItem("LocalPizza", "Food & Drinks", listOf("pizza", "slice", "cheese", "crust"), false),
        IconItem("BakeryDining", "Food & Drinks", listOf("bread", "biscuit", "toast", "bakery"), false),
        IconItem("RamenDining", "Food & Drinks", listOf("noodles", "pasta", "soup", "ramen"), false),
        IconItem("Icecream", "Food & Drinks", listOf("dessert", "cone", "sweet", "gelato"), false),
        IconItem("Cake", "Food & Drinks", listOf("birthday", "party", "celebrate", "pastry"), false),
        IconItem("LocalBar", "Food & Drinks", listOf("drinks", "juice", "beverage", "mocktail"), false),
        IconItem("Liquor", "Food & Drinks", listOf("bottle", "wine", "spirits", "drink"), false),
        IconItem("WineBar", "Food & Drinks", listOf("cocktail", "bar", "glass", "wine"), false),
        IconItem("SetMeal", "Food & Drinks", listOf("fish", "thali", "dish", "platter"), false),
        IconItem("TakeoutDining", "Food & Drinks", listOf("parcel", "delivery", "pack", "takeout"), false),
        IconItem("BrunchDining", "Food & Drinks", listOf("brunch", "snacks", "midday food"), false),
        IconItem("SoupKitchen", "Food & Drinks", listOf("cooking", "curry", "pot", "stew"), false),
        IconItem("Kitchen", "Food & Drinks", listOf("fridge", "home food", "grocery", "cooking"), false),
        IconItem("LocalGroceryStore", "Food & Drinks", listOf("raw food", "market", "bazaar", "groceries"), false),
        IconItem("FoodBank", "Food & Drinks", listOf("charity", "food ration", "relief"), false),
        IconItem("LocalDining", "Food & Drinks", listOf("eat", "plate", "fork", "restaurant"), false),
        IconItem("MenuBook", "Food & Drinks", listOf("menu", "recipe", "dishes", "cookbook"), true),
        IconItem("Bento", "Food & Drinks", listOf("box", "japanese", "lunchbox", "meal pack"), false),
        IconItem("Tapas", "Food & Drinks", listOf("appetizer", "finger food", "starter"), false),
        IconItem("LocalDrink", "Food & Drinks", listOf("water", "juice", "glass", "beverage"), false),
        IconItem("Flatware", "Food & Drinks", listOf("fork", "spoon", "cutlery", "utensils"), false),
        IconItem("Dining", "Food & Drinks", listOf("table", "meal", "dine in"), false),
        IconItem("Egg", "Food & Drinks", listOf("poultry", "breakfast", "protein", "eggs"), false),
        IconItem("EggAlt", "Food & Drinks", listOf("fried egg", "omelette", "poached"), false),
        IconItem("KebabDining", "Food & Drinks", listOf("grill", "bbq", "kebab", "skewer"), false),
        IconItem("RiceBowl", "Food & Drinks", listOf("rice", "curry", "biryani", "bowl"), false),
        IconItem("OutdoorGrill", "Food & Drinks", listOf("barbecue", "steak", "roast", "grill"), false),
        IconItem("EmojiFoodBeverage", "Food & Drinks", listOf("hot tea", "cup", "herbal tea", "mug"), false),
        IconItem("RoomService", "Food & Drinks", listOf("hotel food", "tray", "service"), false),
        IconItem("Cookie", "Food & Drinks", listOf("biscuit", "cookie", "bakery", "sweet"), false),
        IconItem("RestaurantMenu", "Food & Drinks", listOf("menu card", "list", "bill of fare"), false),
        IconItem("FreeBreakfast", "Food & Drinks", listOf("complimentary", "morning tea", "breakfast"), false),
        IconItem("DeliveryDining", "Food & Drinks", listOf("food delivery", "rider", "courier"), false),
        IconItem("SportsBar", "Food & Drinks", listOf("pub", "drinks", "match viewing"), false),
        IconItem("Nightlife", "Food & Drinks", listOf("club", "party", "drinks", "evening"), false),
        IconItem("ShoppingCart", "Shopping", listOf("cart", "buy", "supermarket", "store"), false),
        IconItem("ShoppingCartCheckout", "Shopping", listOf("checkout", "order", "purchase"), false),
        IconItem("ShoppingBag", "Shopping", listOf("mall", "haul", "store", "shopping"), false),
        IconItem("ShoppingBasket", "Shopping", listOf("basket", "goods", "shop", "market"), false),
        IconItem("Store", "Shopping", listOf("shop", "vendor", "outlet", "retail"), false),
        IconItem("Storefront", "Shopping", listOf("boutique", "showroom", "shop front"), false),
        IconItem("LocalMall", "Shopping", listOf("shopping mall", "plaza", "center"), false),
        IconItem("Checkroom", "Shopping", listOf("clothes", "wardrobe", "dress", "fashion"), false),
        IconItem("Diamond", "Shopping", listOf("jewelry", "gem", "gold", "luxury", "ring"), false),
        IconItem("Watch", "Shopping", listOf("wrist watch", "time", "clock", "accessory"), false),
        IconItem("FitnessCenter", "Shopping", listOf("gym", "workout", "weights", "fitness gear"), false),
        IconItem("Toys", "Shopping", listOf("kids toys", "play", "doll", "action figure"), false),
        IconItem("Backpack", "Shopping", listOf("bag", "travel bag", "hiking", "school bag"), false),
        IconItem("Style", "Shopping", listOf("tag", "fashion", "brand", "style"), false),
        IconItem("Loyalty", "Shopping", listOf("membership", "points", "club", "reward card"), false),
        IconItem("LocalOffer", "Shopping", listOf("deal", "discount", "coupon", "promo"), false),
        IconItem("Discount", "Shopping", listOf("voucher", "percent off", "markdown"), false),
        IconItem("Inventory2", "Shopping", listOf("box", "package", "parcel", "shipment"), false),
        IconItem("ProductionQuantityLimits", "Shopping", listOf("cart limit", "stock limit"), false),
        IconItem("AddShoppingCart", "Shopping", listOf("add to cart", "buy item", "wishlist"), false),
        IconItem("RemoveShoppingCart", "Shopping", listOf("empty cart", "remove item"), false),
        IconItem("Shop", "Shopping", listOf("bag", "store item", "boutique"), false),
        IconItem("Shop2", "Shopping", listOf("outlet", "retail store"), false),
        IconItem("ShopTwo", "Shopping", listOf("brand store", "fashion shop"), false),
        IconItem("CardMembership", "Shopping", listOf("vip card", "loyalty badge"), false),
        IconItem("Outbox", "Shopping", listOf("sent goods", "dispatch", "outbound"), false),
        IconItem("Inbox", "Shopping", listOf("received goods", "orders received"), false),
        IconItem("AllInbox", "Shopping", listOf("all orders", "deliveries", "inbox"), false),
        IconItem("AutoFixHigh", "Shopping", listOf("cosmetics", "glow", "beauty", "makeup"), false),
        IconItem("DryCleaning", "Shopping", listOf("laundry", "dry wash", "iron", "cleaning"), false),
        IconItem("AddBusiness", "Shopping", listOf("new store", "merchant", "vendor"), false),
        IconItem("LocalFlorist", "Shopping", listOf("flower", "bouquet", "gift", "plants"), false),
        IconItem("LocalConvenienceStore", "Shopping", listOf("corner shop", "kiosk", "convenience"), false),
        IconItem("LocalLaundryService", "Shopping", listOf("washing machine", "laundry"), false),
        IconItem("DirectionsCar", "Transport", listOf("car", "auto", "vehicle", "cab", "ride"), false),
        IconItem("Train", "Transport", listOf("railway", "metro", "commute", "train"), false),
        IconItem("TwoWheeler", "Transport", listOf("bike", "motorcycle", "scooter", "ride"), false),
        IconItem("DirectionsBike", "Transport", listOf("bicycle", "cycle", "ride", "cycling"), true),
        IconItem("DirectionsBus", "Transport", listOf("bus", "public transit", "coach"), false),
        IconItem("DirectionsWalk", "Transport", listOf("foot", "walking", "pedestrian", "walk"), true),
        IconItem("DirectionsRun", "Transport", listOf("running", "jog", "sprint", "run"), true),
        IconItem("Flight", "Transport", listOf("plane", "airplane", "air ticket", "flight"), false),
        IconItem("FlightTakeoff", "Transport", listOf("departure", "trip", "tour", "takeoff"), false),
        IconItem("FlightLand", "Transport", listOf("arrival", "return", "landing"), false),
        IconItem("LocalTaxi", "Transport", listOf("uber", "pathao", "cab", "taxi fare"), false),
        IconItem("LocalGasStation", "Transport", listOf("fuel", "petrol", "octane", "cng", "diesel"), false),
        IconItem("ElectricCar", "Transport", listOf("ev", "tesla", "charging", "green car"), false),
        IconItem("EvStation", "Transport", listOf("charging station", "battery charge"), false),
        IconItem("Commute", "Transport", listOf("routine", "journey", "ride", "commute"), false),
        IconItem("Subway", "Transport", listOf("underground", "tube", "metro transit"), false),
        IconItem("Tram", "Transport", listOf("rail", "tramway", "streetcar"), false),
        IconItem("DirectionsBoat", "Transport", listOf("ship", "launch", "ferry", "boat"), false),
        IconItem("Sailing", "Transport", listOf("boat", "cruise", "yacht", "sail"), false),
        IconItem("Hotel", "Transport", listOf("stay", "resort", "motel", "room", "lodging"), false),
        IconItem("Luggage", "Transport", listOf("baggage", "suitcase", "vacation", "luggage"), false),
        IconItem("Map", "Transport", listOf("location", "route", "gps", "navigation"), false),
        IconItem("Navigation", "Transport", listOf("direction", "compass", "turn by turn"), false),
        IconItem("Explore", "Transport", listOf("adventure", "safari", "trip", "explore"), false),
        IconItem("DirectionsSubway", "Transport", listOf("metro train", "transit station"), false),
        IconItem("DirectionsRailway", "Transport", listOf("intercity rail", "track", "train station"), false),
        IconItem("DirectionsTransit", "Transport", listOf("public transport", "city transit"), false),
        IconItem("ElectricBike", "Transport", listOf("ebike", "battery cycle", "electric bike"), false),
        IconItem("ElectricMoped", "Transport", listOf("electric scooter", "moped"), false),
        IconItem("ElectricScooter", "Transport", listOf("escooter", "kick scooter"), false),
        IconItem("PedalBike", "Transport", listOf("bicycle", "cycling", "pedal"), false),
        IconItem("Moped", "Transport", listOf("scooter", "vespa", "moped"), false),
        IconItem("Motorcycle", "Transport", listOf("biker", "motorbike", "two wheeler"), false),
        IconItem("AirportShuttle", "Transport", listOf("microbus", "pickup van", "shuttle"), false),
        IconItem("CarRental", "Transport", listOf("rent a car", "hire car", "leasing"), false),
        IconItem("CarRepair", "Transport", listOf("garage", "mechanic", "servicing", "repair"), false),
        IconItem("CarCrash", "Transport", listOf("accident", "insurance claim", "crash"), false),
        IconItem("TireRepair", "Transport", listOf("wheel", "puncture", "alignment", "tire"), false),
        IconItem("LocalParking", "Transport", listOf("parking fee", "garage", "park car"), false),
        IconItem("Traffic", "Transport", listOf("signal", "congestion", "traffic jam"), false),
        IconItem("Speed", "Transport", listOf("speedometer", "toll road", "fast"), false),
        IconItem("CompassCalibration", "Transport", listOf("gps calibration", "sensor"), false),
        IconItem("PinDrop", "Transport", listOf("destination", "pin", "drop off"), false),
        IconItem("LocationOn", "Transport", listOf("place", "geo marker", "current location"), false),
        IconItem("LocationSearching", "Transport", listOf("find location", "searching"), false),
        IconItem("NearMe", "Transport", listOf("nearby places", "distance", "near"), false),
        IconItem("ShareLocation", "Transport", listOf("live tracking", "share gps"), false),
        IconItem("TransferWithinAStation", "Transport", listOf("transfer", "interchange", "connection"), false),
        IconItem("FlightClass", "Transport", listOf("business class", "seat", "first class"), false),
        IconItem("Airlines", "Transport", listOf("airline", "ticket", "carrier"), false),
        IconItem("ConnectingAirports", "Transport", listOf("layover", "transit airport", "connecting flight"), false),
        IconItem("Home", "Bills & Housing", listOf("house", "rent", "residence", "home sweet home"), false),
        IconItem("Apartment", "Bills & Housing", listOf("flat", "building", "society", "condo"), false),
        IconItem("Cottage", "Bills & Housing", listOf("village", "bungalow", "cottage"), false),
        IconItem("Villa", "Bills & Housing", listOf("mansion", "estate", "resort", "villa"), false),
        IconItem("LocationCity", "Bills & Housing", listOf("city", "property", "holding", "municipality"), false),
        IconItem("ElectricBolt", "Bills & Housing", listOf("electricity", "current", "power", "desco", "dpdc"), false),
        IconItem("Power", "Bills & Housing", listOf("plug", "socket", "charge", "power"), false),
        IconItem("WaterDrop", "Bills & Housing", listOf("water bill", "wasa", "utility", "water"), false),
        IconItem("Plumbing", "Bills & Housing", listOf("pipe", "sanitary", "faucet", "plumber"), false),
        IconItem("Wifi", "Bills & Housing", listOf("internet", "broadband", "fiber", "wifi bill"), false),
        IconItem("Router", "Bills & Housing", listOf("modem", "network", "lan", "router"), false),
        IconItem("Tv", "Bills & Housing", listOf("television", "dish bill", "cable", "screen"), false),
        IconItem("DesktopWindows", "Bills & Housing", listOf("pc", "computer", "desktop setup"), false),
        IconItem("Laptop", "Bills & Housing", listOf("notebook", "macbook", "laptop"), false),
        IconItem("Chair", "Bills & Housing", listOf("furniture", "interior", "chair"), false),
        IconItem("Bed", "Bills & Housing", listOf("mattress", "sleep", "decor", "bedroom"), false),
        IconItem("Lightbulb", "Bills & Housing", listOf("lighting", "lamp", "idea", "bulb"), false),
        IconItem("Build", "Bills & Housing", listOf("repair", "tools", "fix", "renovation"), false),
        IconItem("Handyman", "Bills & Housing", listOf("carpenter", "technician", "worker"), false),
        IconItem("Hardware", "Bills & Housing", listOf("screw", "wrench", "hardware store"), false),
        IconItem("Construction", "Bills & Housing", listOf("building", "cement", "renovation", "construction"), false),
        IconItem("Security", "Bills & Housing", listOf("guard", "cctv", "safe", "security bill"), false),
        IconItem("CleaningServices", "Bills & Housing", listOf("maid", "sweep", "dusting", "cleaner"), false),
        IconItem("House", "Bills & Housing", listOf("home", "building", "dwelling"), false),
        IconItem("HomeWork", "Bills & Housing", listOf("wfh", "office building", "remote work"), false),
        IconItem("HolidayVillage", "Bills & Housing", listOf("vacation home", "village"), false),
        IconItem("Cabin", "Bills & Housing", listOf("wooden cabin", "retreat", "resort"), false),
        IconItem("Balcony", "Bills & Housing", listOf("veranda", "terrace", "balcony"), false),
        IconItem("Deck", "Bills & Housing", listOf("patio", "outdoor deck"), false),
        IconItem("Yard", "Bills & Housing", listOf("lawn", "garden", "backyard"), false),
        IconItem("Garage", "Bills & Housing", listOf("carport", "parking lot", "garage"), false),
        IconItem("Roofing", "Bills & Housing", listOf("shed", "roof repair", "tiles"), false),
        IconItem("DoorFront", "Bills & Housing", listOf("entrance", "front door"), false),
        IconItem("DoorSliding", "Bills & Housing", listOf("sliding door", "patio door"), false),
        IconItem("Window", "Bills & Housing", listOf("ventilation", "glass window", "curtains"), false),
        IconItem("Bathtub", "Bills & Housing", listOf("bath", "tub", "washroom"), false),
        IconItem("Shower", "Bills & Housing", listOf("hot shower", "bath", "water"), false),
        IconItem("HotTub", "Bills & Housing", listOf("jacuzzi", "spa tub", "bath"), false),
        IconItem("Iron", "Bills & Housing", listOf("press", "laundry iron", "clothes"), false),
        IconItem("Microwave", "Bills & Housing", listOf("oven", "heater", "kitchen appliance"), false),
        IconItem("Blender", "Bills & Housing", listOf("mixer", "grinder", "juice machine"), false),
        IconItem("Air", "Bills & Housing", listOf("purifier", "ventilation", "fresh air"), false),
        IconItem("AcUnit", "Bills & Housing", listOf("ac", "air conditioner", "cooling"), false),
        IconItem("Thermostat", "Bills & Housing", listOf("climate control", "temperature"), false),
        IconItem("Fireplace", "Bills & Housing", listOf("heater", "cozy", "fire"), false),
        IconItem("GasMeter", "Bills & Housing", listOf("gas bill", "titas", "lpg", "gas cylinder"), false),
        IconItem("HeatPump", "Bills & Housing", listOf("water heater", "geyser"), false),
        IconItem("WaterDamage", "Bills & Housing", listOf("leakage", "water problem", "repair"), false),
        IconItem("SolarPower", "Bills & Housing", listOf("solar panel", "green energy", "solar"), false),
        IconItem("Propane", "Bills & Housing", listOf("gas cylinder", "cylinder", "propane gas"), false),
        IconItem("ElectricalServices", "Bills & Housing", listOf("electrician", "wiring", "power repair"), false),
        IconItem("Sensors", "Bills & Housing", listOf("iot", "smart home sensor", "alarm"), false),
        IconItem("LocalHospital", "Health", listOf("hospital", "clinic", "doctor", "emergency"), false),
        IconItem("Medication", "Health", listOf("medicine", "tablet", "pharma", "prescription"), false),
        IconItem("MedicalServices", "Health", listOf("first aid", "emergency", "medical kit"), false),
        IconItem("Healing", "Health", listOf("bandage", "treatment", "recovery"), false),
        IconItem("LocalPharmacy", "Health", listOf("drugstore", "chemist", "pharmacy"), false),
        IconItem("Vaccines", "Health", listOf("injection", "dose", "syringe", "vaccine"), false),
        IconItem("Psychology", "Health", listOf("mental health", "counseling", "mind", "therapy"), false),
        IconItem("Favorite", "Health", listOf("heart", "cardio", "life", "love"), false),
        IconItem("FavoriteBorder", "Health", listOf("wellness", "care", "heart outline"), false),
        IconItem("MonitorHeart", "Health", listOf("pulse", "ecg", "pressure", "heart rate"), false),
        IconItem("Spa", "Health", listOf("massage", "relax", "salon", "wellness"), false),
        IconItem("SelfImprovement", "Health", listOf("yoga", "meditation", "mindfulness"), false),
        IconItem("HealthAndSafety", "Health", listOf("protection", "insurance", "safety"), false),
        IconItem("MedicalInformation", "Health", listOf("prescription", "record", "health card"), false),
        IconItem("Emergency", "Health", listOf("ambulance", "urgent care", "sos"), false),
        IconItem("MonitorWeight", "Health", listOf("weighing scale", "diet", "weight track"), false),
        IconItem("Bloodtype", "Health", listOf("blood donation", "test", "blood group"), false),
        IconItem("Blind", "Health", listOf("eye care", "optical", "vision"), false),
        IconItem("Accessible", "Health", listOf("wheelchair", "special care", "accessible"), true),
        IconItem("AccessibleForward", "Health", listOf("mobility", "rehab", "forward"), true),
        IconItem("CleanHands", "Health", listOf("hygiene", "wash hands", "sanitation"), false),
        IconItem("Sanitizer", "Health", listOf("hand sanitizer", "antiseptic", "clean"), false),
        IconItem("Masks", "Health", listOf("surgical mask", "n95", "protection"), false),
        IconItem("PersonalInjury", "Health", listOf("injury", "claim", "accident"), false),
        IconItem("Elderly", "Health", listOf("senior citizen", "grandparent", "elderly"), false),
        IconItem("ElderlyWoman", "Health", listOf("grandma", "senior care", "elderly woman"), false),
        IconItem("PregnantWoman", "Health", listOf("maternity", "prenatal", "pregnancy"), false),
        IconItem("Coronavirus", "Health", listOf("virus", "infection", "epidemic"), false),
        IconItem("Sick", "Health", listOf("fever", "illness", "cold", "sick"), false),
        IconItem("Mood", "Health", listOf("happy", "mental health", "good mood"), false),
        IconItem("MoodBad", "Health", listOf("sad", "depression", "stress", "bad mood"), false),
        IconItem("SentimentSatisfied", "Health", listOf("content", "calm", "satisfied"), false),
        IconItem("SentimentVerySatisfied", "Health", listOf("joy", "wellness", "ecstatic"), false),
        IconItem("SentimentDissatisfied", "Health", listOf("unwell", "gloomy", "unhappy"), false),
        IconItem("ShieldMoon", "Health", listOf("night health", "sleep security"), false),
        IconItem("Bedtime", "Health", listOf("sleep", "insomnia care", "bedtime"), false),
        IconItem("Nightlight", "Health", listOf("night rest", "soothing", "sleep light"), false),
        IconItem("SportsGymnastics", "Health", listOf("stretching", "flexibility", "aerobics"), false),
        IconItem("Biotech", "Health", listOf("diagnostics", "lab test", "biotech"), false),
        IconItem("Science", "Health", listOf("pathology", "test report", "science"), false),
        IconItem("School", "Life & Work", listOf("college", "university", "tuition", "school"), false),
        IconItem("Work", "Life & Work", listOf("job", "office", "career", "salary"), false),
        IconItem("WorkOutline", "Life & Work", listOf("business", "contract", "freelance"), false),
        IconItem("BusinessCenter", "Life & Work", listOf("briefcase", "corporate", "portfolio"), false),
        IconItem("Handshake", "Life & Work", listOf("deal", "loan", "agreement", "borrow", "lending"), false),
        IconItem("FamilyRestroom", "Life & Work", listOf("family", "parents", "home", "kids"), false),
        IconItem("ChildCare", "Life & Work", listOf("baby", "kids", "daycare", "child care"), false),
        IconItem("ChildFriendly", "Life & Work", listOf("stroller", "infant", "toddler"), false),
        IconItem("Pets", "Life & Work", listOf("cat", "dog", "veterinary", "animal", "pet food"), false),
        IconItem("AutoStories", "Life & Work", listOf("novel", "learning", "story book"), false),
        IconItem("DesignServices", "Life & Work", listOf("creative", "graphic", "art", "design"), false),
        IconItem("Groups", "Life & Work", listOf("team", "meeting", "colleagues", "group"), false),
        IconItem("Person", "Life & Work", listOf("profile", "individual", "user", "me"), false),
        IconItem("People", "Life & Work", listOf("friends", "crowd", "community", "buddies"), false),
        IconItem("Celebration", "Life & Work", listOf("event", "party", "anniversary", "eid", "puja"), false),
        IconItem("Badge", "Life & Work", listOf("id card", "employee badge", "credentials"), false),
        IconItem("CardTravel", "Life & Work", listOf("business trip", "luggage", "travel"), false),
        IconItem("Business", "Life & Work", listOf("company", "firm", "enterprise", "business"), false),
        IconItem("CorporateFare", "Life & Work", listOf("headquarters", "building", "corporate"), false),
        IconItem("Domain", "Life & Work", listOf("institution", "organization", "domain"), false),
        IconItem("Hub", "Life & Work", listOf("network", "connections", "branch", "hub"), false),
        IconItem("Engineering", "Life & Work", listOf("engineer", "factory", "machinery", "tech"), false),
        IconItem("FactCheck", "Life & Work", listOf("audit", "inspection", "review", "fact check"), true),
        IconItem("Assignment", "Life & Work", listOf("task", "homework", "contract", "assignment"), true),
        IconItem("AssignmentTurnedIn", "Life & Work", listOf("completed work", "project done"), false),
        IconItem("AssignmentInd", "Life & Work", listOf("assigned lead", "delegate", "assignee"), false),
        IconItem("Task", "Life & Work", listOf("to do", "action item", "task"), false),
        IconItem("TaskAlt", "Life & Work", listOf("finished task", "tick mark", "done"), false),
        IconItem("PendingActions", "Life & Work", listOf("waiting approval", "queue", "pending"), false),
        IconItem("HistoryEdu", "Life & Work", listOf("certificate", "diploma", "degree", "education"), false),
        IconItem("Grade", "Life & Work", listOf("score", "grade sheet", "marks", "star"), false),
        IconItem("WorkspacePremium", "Life & Work", listOf("award", "distinction", "trophy", "premium"), false),
        IconItem("Class", "Life & Work", listOf("classroom", "lesson", "batch", "lecture"), false),
        IconItem("CastForEducation", "Life & Work", listOf("online course", "elearning", "webinar"), false),
        IconItem("ImportContacts", "Life & Work", listOf("open book", "syllabus", "contacts"), false),
        IconItem("Create", "Life & Work", listOf("write", "pen", "compose", "draft"), false),
        IconItem("Edit", "Life & Work", listOf("modify", "draft", "author", "pencil"), false),
        IconItem("DriveFileRenameOutline", "Life & Work", listOf("signature", "sign doc", "rename"), false),
        IconItem("Draw", "Life & Work", listOf("sketch", "drawing", "illustration", "art"), false),
        IconItem("CoPresent", "Life & Work", listOf("presentation", "slides", "meeting", "pitch"), false),
        IconItem("ContactPhone", "Life & Work", listOf("phonebook", "directory", "call"), false),
        IconItem("ContactMail", "Life & Work", listOf("email address", "contacts", "mail"), false),
        IconItem("Mail", "Life & Work", listOf("postal", "inbox", "correspondence", "letter"), false),
        IconItem("Email", "Life & Work", listOf("electronic mail", "letter", "email"), false),
        IconItem("Drafts", "Life & Work", listOf("draft email", "notes", "draft"), false),
        IconItem("MarkEmailRead", "Life & Work", listOf("cleared inbox", "read mail"), false),
        IconItem("SupervisedUserCircle", "Life & Work", listOf("mentor", "supervisor", "lead"), false),
        IconItem("Crib", "Life & Work", listOf("baby crib", "cot", "nursery", "infant"), false),
        IconItem("BabyChangingStation", "Life & Work", listOf("diaper change", "baby room"), false),
        IconItem("Stroller", "Life & Work", listOf("pram", "baby walk", "stroller"), false),
        IconItem("Woman", "Life & Work", listOf("female", "mother", "sister", "lady"), false),
        IconItem("Man", "Life & Work", listOf("male", "father", "brother", "gentleman"), false),
        IconItem("Boy", "Life & Work", listOf("son", "little boy", "child"), false),
        IconItem("Girl", "Life & Work", listOf("daughter", "little girl", "child"), false),
        IconItem("EmojiEmotions", "Life & Work", listOf("smileys", "reactions", "emojis"), false),
        IconItem("Face", "Life & Work", listOf("avatar", "user profile", "face"), false),
        IconItem("Diversity1", "Life & Work", listOf("inclusive community", "diversity"), false),
        IconItem("Diversity2", "Life & Work", listOf("unity", "gathering", "team"), false),
        IconItem("Diversity3", "Life & Work", listOf("global team", "international"), false),
        IconItem("PeopleAlt", "Life & Work", listOf("users", "family members", "people"), false),
        IconItem("PersonAdd", "Life & Work", listOf("add contact", "new member", "invite"), false),
        IconItem("GroupAdd", "Life & Work", listOf("create group", "add buddies"), false),
        IconItem("Wc", "Life & Work", listOf("restroom", "toilet washroom", "wc"), false),
        IconItem("Wash", "Life & Work", listOf("handwash", "cleanliness", "wash"), false),
        IconItem("Dry", "Life & Work", listOf("towel dry", "hand dryer", "dry"), false),
        IconItem("Soap", "Life & Work", listOf("bath soap", "shampoo", "cleaning"), false),
        IconItem("Stairs", "Life & Work", listOf("steps", "climb", "stairs"), false),
        IconItem("Elevator", "Life & Work", listOf("lift", "elevator fee", "elevator"), false),
        IconItem("ChairAlt", "Life & Work", listOf("desk chair", "ergonomic chair"), false),
        IconItem("Desk", "Life & Work", listOf("study table", "office desk", "workstation"), false),
        IconItem("TableRestaurant", "Life & Work", listOf("dining table", "restaurant table"), false),
        IconItem("TableBar", "Life & Work", listOf("high table", "bar stool", "counter"), false),
        IconItem("Living", "Life & Work", listOf("living room", "sofa set", "hall"), false),
        IconItem("KingBed", "Life & Work", listOf("master bed", "luxury suite", "king bed"), false),
        IconItem("SingleBed", "Life & Work", listOf("single bed", "guest bed", "cot"), false),
        IconItem("PhoneAndroid", "Entertainment", listOf("mobile", "smartphone", "recharge", "phone"), false),
        IconItem("PhoneIphone", "Entertainment", listOf("apple", "iphone", "ios", "iphone"), false),
        IconItem("SmartDisplay", "Entertainment", listOf("screen", "streaming", "ott", "display"), false),
        IconItem("Movie", "Entertainment", listOf("cinema", "film", "netflix", "hall", "movie"), false),
        IconItem("Theaters", "Entertainment", listOf("show", "play", "stage", "theater"), false),
        IconItem("MusicNote", "Entertainment", listOf("audio", "song", "spotify", "music"), false),
        IconItem("Headphones", "Entertainment", listOf("earphones", "headset", "headphones"), false),
        IconItem("Podcasts", "Entertainment", listOf("talk", "broadcast", "podcast", "radio"), false),
        IconItem("Radio", "Entertainment", listOf("fm", "wireless", "broadcasting", "radio"), false),
        IconItem("CameraAlt", "Entertainment", listOf("dslr", "photography", "photos", "camera"), false),
        IconItem("Videocam", "Entertainment", listOf("video", "recording", "vlog", "video camera"), false),
        IconItem("LiveTv", "Entertainment", listOf("cable", "broadcasting", "live tv", "stream"), false),
        IconItem("Casino", "Entertainment", listOf("gaming", "bet", "lottery", "dice"), false),
        IconItem("EmojiEvents", "Entertainment", listOf("trophy", "award", "prize", "winner"), false),
        IconItem("MilitaryTech", "Entertainment", listOf("medal", "honor", "military"), false),
        IconItem("SportsEsports", "Entertainment", listOf("gaming", "playstation", "xbox", "esports"), false),
        IconItem("Gamepad", "Entertainment", listOf("controller", "joystick", "gamepad"), false),
        IconItem("Piano", "Entertainment", listOf("keyboard", "synthesizer", "music", "piano"), false),
        IconItem("QueueMusic", "Entertainment", listOf("playlist", "queue", "songs"), true),
        IconItem("Album", "Entertainment", listOf("vinyl", "record", "cd", "album"), false),
        IconItem("LibraryMusic", "Entertainment", listOf("tracks", "music store", "songs library"), false),
        IconItem("Mic", "Entertainment", listOf("microphone", "singing", "karaoke", "mic"), false),
        IconItem("MicExternalOn", "Entertainment", listOf("recording mic", "studio mic"), false),
        IconItem("VolumeUp", "Entertainment", listOf("loudspeaker", "speaker", "audio", "volume"), true),
        IconItem("SurroundSound", "Entertainment", listOf("dolby", "home theater", "surround"), false),
        IconItem("Equalizer", "Entertainment", listOf("sound mixer", "dj", "equalizer"), false),
        IconItem("GraphicEq", "Entertainment", listOf("audio wave", "sound bar", "frequencies"), false),
        IconItem("MovieCreation", "Entertainment", listOf("clapperboard", "film shoot", "production"), false),
        IconItem("TheaterComedy", "Entertainment", listOf("drama", "comedy", "mask", "play"), false),
        IconItem("Camera", "Entertainment", listOf("photo capture", "lens", "camera"), false),
        IconItem("Photo", "Entertainment", listOf("picture", "gallery item", "photo"), false),
        IconItem("PhotoLibrary", "Entertainment", listOf("albums", "snapshots", "gallery"), false),
        IconItem("Collections", "Entertainment", listOf("photo gallery", "memories", "photos"), false),
        IconItem("VideoLibrary", "Entertainment", listOf("video archive", "movies collection"), false),
        IconItem("SlowMotionVideo", "Entertainment", listOf("slow mo", "effects", "video clip"), false),
        IconItem("Audiotrack", "Entertainment", listOf("music file", "soundtrack", "audio"), false),
        IconItem("PianoOff", "Entertainment", listOf("mute music", "quiet"), false),
        IconItem("Attractions", "Entertainment", listOf("amusement park", "fair", "ride", "ferris wheel"), false),
        IconItem("Festival", "Entertainment", listOf("carnival", "concert", "fair", "festival"), false),
        IconItem("ConfirmationNumber", "Entertainment", listOf("cinema ticket", "coupon", "entry pass"), false),
        IconItem("AirplaneTicket", "Entertainment", listOf("boarding pass", "entry", "flight ticket"), true),
        IconItem("SportsScore", "Entertainment", listOf("scoreboard", "match points", "result"), false),
        IconItem("StrikethroughS", "Entertainment", listOf("special price", "sale tag"), false),
        IconItem("SportsSoccer", "Sports", listOf("football", "fifa", "match", "soccer"), false),
        IconItem("SportsBasketball", "Sports", listOf("nba", "basketball", "hoop", "ball"), false),
        IconItem("SportsTennis", "Sports", listOf("tennis", "court", "racket", "lawn tennis"), false),
        IconItem("SportsCricket", "Sports", listOf("cricket", "bat", "ipl", "bpl", "world cup"), false),
        IconItem("SportsBaseball", "Sports", listOf("baseball", "mlb", "softball"), false),
        IconItem("SportsFootball", "Sports", listOf("american football", "nfl", "rugby"), false),
        IconItem("SportsVolleyball", "Sports", listOf("volleyball", "beach match"), false),
        IconItem("SportsGolf", "Sports", listOf("golf", "club", "putt", "course"), false),
        IconItem("SportsRugby", "Sports", listOf("rugby", "scrum", "try"), false),
        IconItem("SportsHandball", "Sports", listOf("handball match", "indoor sport"), false),
        IconItem("SportsHockey", "Sports", listOf("ice hockey", "field hockey", "puck"), false),
        IconItem("SportsMotorsports", "Sports", listOf("f1", "racing", "motogp", "car race"), false),
        IconItem("SportsMartialArts", "Sports", listOf("karate", "judo", "taekwondo", "martial arts"), false),
        IconItem("SportsMma", "Sports", listOf("ufc", "boxing", "cage fight", "mma"), false),
        IconItem("Kayaking", "Sports", listOf("canoe", "river kayak", "paddle"), false),
        IconItem("Rowing", "Sports", listOf("boat rowing", "crew", "oar"), false),
        IconItem("Surfing", "Sports", listOf("wave surf", "beach sport", "surfboard"), false),
        IconItem("Snowboarding", "Sports", listOf("snow", "winter sports", "snowboard"), false),
        IconItem("Skateboarding", "Sports", listOf("skateboard", "skating", "deck"), false),
        IconItem("Pool", "Sports", listOf("swimming pool", "swim lap", "swimming"), false),
        IconItem("Kitesurfing", "Sports", listOf("kite", "water sport", "kitesurf"), false),
        IconItem("Hiking", "Sports", listOf("trekking", "mountain walk", "hiking trail"), false),
        IconItem("DownhillSkiing", "Sports", listOf("ski resort", "snow", "downhill"), false),
        IconItem("NordicWalking", "Sports", listOf("hiking sticks", "outdoor walking"), false),
        IconItem("Paragliding", "Sports", listOf("skydiving", "glider", "flying", "parachute"), false),
        IconItem("ScubaDiving", "Sports", listOf("deep dive", "ocean sport", "scuba"), false),
        IconItem("Scoreboard", "Sports", listOf("scores", "match scoreboard", "tally"), false),
        IconItem("Sports", "Sports", listOf("whistle", "referee", "game", "match"), false),
        IconItem("Timer", "Sports", listOf("stopwatch", "lap time", "interval"), false),
        IconItem("Alarm", "Sports", listOf("morning bell", "workout timer", "alarm"), false),
        IconItem("HourglassBottom", "Sports", listOf("countdown", "duration", "timer"), false),
        IconItem("Computer", "Tech & Tools", listOf("pc", "workstation", "rig", "computer"), false),
        IconItem("Smartphone", "Tech & Tools", listOf("cellphone", "android phone", "smartphone"), false),
        IconItem("Tablet", "Tech & Tools", listOf("ipad", "android tablet", "tab"), false),
        IconItem("TabletMac", "Tech & Tools", listOf("ipad pro", "apple tablet", "tablet"), false),
        IconItem("SmartToy", "Tech & Tools", listOf("robot", "ai bot", "gadget", "toy"), false),
        IconItem("Devices", "Tech & Tools", listOf("gadgets", "hardware collection", "devices"), false),
        IconItem("Phonelink", "Tech & Tools", listOf("sync phone", "bluetooth device", "link"), false),
        IconItem("DeveloperBoard", "Tech & Tools", listOf("raspberry pi", "arduino", "chip", "hardware"), false),
        IconItem("Memory", "Tech & Tools", listOf("cpu", "processor", "ram", "microchip"), false),
        IconItem("SimCard", "Tech & Tools", listOf("mobile sim", "esim", "network", "sim card"), false),
        IconItem("SdCard", "Tech & Tools", listOf("micro sd", "storage card", "memory card"), false),
        IconItem("Storage", "Tech & Tools", listOf("hard drive", "ssd", "nas", "storage"), false),
        IconItem("Cloud", "Tech & Tools", listOf("cloud storage", "gdrive", "backup", "cloud"), false),
        IconItem("CloudDownload", "Tech & Tools", listOf("download file", "fetch cloud", "download"), false),
        IconItem("CloudUpload", "Tech & Tools", listOf("upload file", "cloud backup", "upload"), false),
        IconItem("CloudDone", "Tech & Tools", listOf("synced", "cloud completed", "cloud ok"), false),
        IconItem("CloudSync", "Tech & Tools", listOf("syncing cloud data", "cloud refresh"), false),
        IconItem("Usb", "Tech & Tools", listOf("flash drive", "pen drive", "cable", "usb"), false),
        IconItem("Bluetooth", "Tech & Tools", listOf("wireless", "pairing", "bluetooth"), false),
        IconItem("BluetoothConnected", "Tech & Tools", listOf("paired device", "connected bluetooth"), false),
        IconItem("WifiTethering", "Tech & Tools", listOf("hotspot", "mobile hotspot", "tethering"), false),
        IconItem("Cast", "Tech & Tools", listOf("chromecast", "screen mirror", "cast"), false),
        IconItem("CastConnected", "Tech & Tools", listOf("tv casting", "mirroring display"), false),
        IconItem("Key", "Tech & Tools", listOf("access key", "secret key", "passkey"), false),
        IconItem("VpnKey", "Tech & Tools", listOf("encryption key", "vpn", "security key"), false),
        IconItem("Lock", "Tech & Tools", listOf("lock screen", "secured", "lock"), false),
        IconItem("LockOpen", "Tech & Tools", listOf("unlocked", "access granted", "open lock"), false),
        IconItem("EnhancedEncryption", "Tech & Tools", listOf("2fa", "security safe", "shield lock"), false),
        IconItem("Fingerprint", "Tech & Tools", listOf("biometric", "touch id", "fingerprint"), false),
        IconItem("Terminal", "Tech & Tools", listOf("console", "command line", "bash", "terminal"), false),
        IconItem("Code", "Tech & Tools", listOf("software", "programming", "script", "coding"), false),
        IconItem("BugReport", "Tech & Tools", listOf("debug", "software fix", "bug"), false),
        IconItem("IntegrationInstructions", "Tech & Tools", listOf("api", "integration", "docs", "code sample"), false),
        IconItem("Eco", "Nature & Weather", listOf("green", "nature", "organic", "ecology", "leaf"), false),
        IconItem("Forest", "Nature & Weather", listOf("trees", "plants", "garden", "jungle", "forest"), false),
        IconItem("Nature", "Nature & Weather", listOf("tree", "environment", "park", "nature"), false),
        IconItem("NaturePeople", "Nature & Weather", listOf("gardener", "plantation", "nature lovers"), false),
        IconItem("Grass", "Nature & Weather", listOf("lawn", "grass field", "meadow", "grass"), false),
        IconItem("Park", "Nature & Weather", listOf("public park", "botanical", "park"), false),
        IconItem("WbSunny", "Nature & Weather", listOf("sun", "sunny", "daylight", "bright sun"), false),
        IconItem("DarkMode", "Nature & Weather", listOf("night theme", "moon", "crescent"), false),
        IconItem("LightMode", "Nature & Weather", listOf("day theme", "bright", "sunlight"), false),
        IconItem("CloudQueue", "Nature & Weather", listOf("cloudy", "overcast", "weather"), false),
        IconItem("Thunderstorm", "Nature & Weather", listOf("storm", "lightning", "heavy rain", "thunder"), false),
        IconItem("Tsunami", "Nature & Weather", listOf("wave", "disaster care", "flood"), false),
        IconItem("Volcano", "Nature & Weather", listOf("lava", "eruption", "mountain"), false),
        IconItem("Landscape", "Nature & Weather", listOf("scenery", "hills", "mountains", "view"), false),
        IconItem("Terrain", "Nature & Weather", listOf("mountains", "peaks", "hiking range", "terrain"), false),
        IconItem("Flare", "Nature & Weather", listOf("sun flare", "spark", "light flare"), false),
        IconItem("Whatshot", "Nature & Weather", listOf("fire", "hot trend", "flame", "hot"), false),
        IconItem("EnergySavingsLeaf", "Nature & Weather", listOf("energy saver", "green leaf", "solar eco"), false),
        IconItem("Recycling", "Nature & Weather", listOf("recycle", "waste management", "green recycling"), false),
        IconItem("Compost", "Nature & Weather", listOf("organic fertilizer", "soil", "compost"), false),
        IconItem("CrueltyFree", "Nature & Weather", listOf("bunny", "animal care", "cruelty free"), false),
        IconItem("PestControl", "Nature & Weather", listOf("insect repellent", "pest spray", "fumigation"), false),
        IconItem("Category", "Symbols", listOf("general", "group", "folder", "all categories"), false),
        IconItem("MoreHoriz", "Symbols", listOf("misc", "others", "extra", "dots", "ellipsis"), false),
        IconItem("MoreVert", "Symbols", listOf("options", "more actions", "vertical menu"), false),
        IconItem("Star", "Symbols", listOf("favorite", "important", "vip", "star", "bookmark"), false),
        IconItem("StarBorder", "Symbols", listOf("unfilled star", "watchlist", "rate"), false),
        IconItem("StarHalf", "Symbols", listOf("half star", "rating", "score"), true),
        IconItem("Bookmark", "Symbols", listOf("save", "pinned", "bookmark", "favorite"), false),
        IconItem("BookmarkBorder", "Symbols", listOf("save for later", "outline", "bookmark"), false),
        IconItem("BookmarkAdded", "Symbols", listOf("bookmarked", "saved item", "flagged"), false),
        IconItem("Label", "Symbols", listOf("tag", "badge", "label", "category tag"), true),
        IconItem("LabelImportant", "Symbols", listOf("high priority label", "important badge"), true),
        IconItem("Flag", "Symbols", listOf("priority", "target", "milestone", "flag"), false),
        IconItem("OutlinedFlag", "Symbols", listOf("flag marker", "milestone goal"), false),
        IconItem("FlagCircle", "Symbols", listOf("country flag", "badge", "round flag"), false),
        IconItem("Shield", "Symbols", listOf("security", "safe", "lock", "guarantee", "protection"), false),
        IconItem("Notifications", "Symbols", listOf("alert", "reminder", "bell", "notifications"), false),
        IconItem("NotificationsActive", "Symbols", listOf("ringing bell", "urgent alert", "alarm"), false),
        IconItem("NotificationsPaused", "Symbols", listOf("snoozed alert", "mute bell", "paused"), false),
        IconItem("NotificationsOff", "Symbols", listOf("silent mode", "dnd", "mute notifications"), false),
        IconItem("Send", "Symbols", listOf("remit", "transfer", "dispatch", "paper plane", "send"), true),
        IconItem("Archive", "Symbols", listOf("storage", "history", "archive box", "archive"), false),
        IconItem("Unarchive", "Symbols", listOf("restore from archive", "unarchive"), false),
        IconItem("Folder", "Symbols", listOf("directory", "files", "folder", "documents"), false),
        IconItem("FolderSpecial", "Symbols", listOf("starred folder", "key folder", "favorite files"), false),
        IconItem("FolderShared", "Symbols", listOf("shared directory", "collaborate", "shared drive"), false),
        IconItem("CreateNewFolder", "Symbols", listOf("new category folder", "create folder"), false),
        IconItem("ThumbUp", "Symbols", listOf("like", "approval", "thumbs up", "agree"), false),
        IconItem("ThumbDown", "Symbols", listOf("dislike", "thumbs down", "disagree"), false),
        IconItem("Verified", "Symbols", listOf("certified", "official", "verified", "blue tick"), false),
        IconItem("VerifiedUser", "Symbols", listOf("verified profile", "security tick", "trusted"), false),
        IconItem("Policy", "Symbols", listOf("terms", "privacy policy", "legal badge"), false),
        IconItem("Gavel", "Symbols", listOf("court", "lawyer", "legal fee", "judge"), false),
        IconItem("AllInclusive", "Symbols", listOf("infinity", "unlimited", "forever", "endless"), false),
        IconItem("AutoAwesome", "Symbols", listOf("magic", "ai", "smart feature", "sparkle", "stars"), false),
        IconItem("Bolt", "Symbols", listOf("flash", "lightning fast", "bolt"), false),
        IconItem("FlashOn", "Symbols", listOf("camera flash", "power on", "active"), false),
        IconItem("FlashOff", "Symbols", listOf("no flash", "power off", "disabled"), false),
        IconItem("Check", "Symbols", listOf("check", "confirm", "done", "tick"), false),
        IconItem("CheckCircle", "Symbols", listOf("completed", "success tick", "circle check"), false),
        IconItem("CheckCircleOutline", "Symbols", listOf("success outline", "ready"), false),
        IconItem("Done", "Symbols", listOf("finished", "checked", "done"), false),
        IconItem("DoneAll", "Symbols", listOf("double tick", "all cleared", "double check"), false),
        IconItem("Close", "Symbols", listOf("cancel", "dismiss", "exit", "cross"), false),
        IconItem("Cancel", "Symbols", listOf("reject", "abort", "cross", "cancel"), false),
        IconItem("Error", "Symbols", listOf("alert", "warning issue", "error mark"), false),
        IconItem("Warning", "Symbols", listOf("caution", "danger alert", "triangle warning"), false),
        IconItem("Info", "Symbols", listOf("information", "details note", "info circle"), false),
        IconItem("Help", "Symbols", listOf("question", "info", "faq", "help circle"), true),
        IconItem("HelpOutline", "Symbols", listOf("help circle", "support guide", "question"), true),
        IconItem("Sync", "Symbols", listOf("synchronize", "sync data", "refresh"), false),
        IconItem("Update", "Symbols", listOf("latest version", "refresh update", "clock arrow"), false),
        IconItem("Refresh", "Symbols", listOf("reload", "fetch latest", "refresh page"), false),
        IconItem("Cached", "Symbols", listOf("cached data", "circular refresh", "cache"), false),
        IconItem("Autorenew", "Symbols", listOf("auto subscription", "recurring cycle", "renew"), false),
        IconItem("Schedule", "Symbols", listOf("clock", "timing", "due date", "schedule"), false),
        IconItem("Event", "Symbols", listOf("calendar event", "appointment", "date"), false),
        IconItem("CalendarToday", "Symbols", listOf("today date", "daily log", "calendar"), false),
        IconItem("CalendarMonth", "Symbols", listOf("monthly planner", "month view", "calendar"), false),
        IconItem("Visibility", "Symbols", listOf("show", "eye view", "visible"), false),
        IconItem("VisibilityOff", "Symbols", listOf("hide", "eye conceal", "invisible"), false),
        IconItem("Tune", "Symbols", listOf("adjust", "settings filter", "sliders"), false),
        IconItem("FilterList", "Symbols", listOf("filter criteria", "funnel", "sort filter"), false),
        IconItem("Sort", "Symbols", listOf("sorting order", "asc desc", "sort"), true),
        IconItem("Search", "Symbols", listOf("lookup", "find item", "search lens"), false),
        IconItem("Share", "Symbols", listOf("share with friends", "export link", "share"), false),
        IconItem("Brush", "Symbols", listOf("paint", "decor", "drawing", "brush"), false),
        IconItem("Palette", "Symbols", listOf("colors", "theme", "art", "palette"), false),
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
            else -> Icons.Default.Category
        }
    }

    /**
     * Checks if the given icon identifier represents a custom image asset from internal storage.
     */
    fun isCustomIcon(iconName: String?): Boolean {
        if (iconName.isNullOrBlank()) return false
        return iconName.startsWith("custom_icon_") || iconName.startsWith("file://") || iconName.startsWith("content://")
    }

    /**
     * Returns the File pointer for a stored custom icon.
     */
    fun getCustomIconFile(context: Context, iconName: String): File {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) customDir.mkdirs()
        val cleanName = if (iconName.endsWith(".png")) iconName else "$iconName.png"
        return File(customDir, cleanName)
    }

    /**
     * Retrieves all saved custom icon file keys.
     */
    fun getAllCustomIcons(context: Context): List<String> {
        val customDir = File(context.filesDir, "custom_icons")
        if (!customDir.exists()) return emptyList()
        return customDir.listFiles { file -> file.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
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

            // Scale to max 256x256 while preserving aspect ratio
            val size = 256
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true)

            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()

            val iconKey = "custom_icon_${System.currentTimeMillis()}"
            val destFile = File(customDir, "$iconKey.png")

            val outStream = FileOutputStream(destFile)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, outStream)
            outStream.flush()
            outStream.close()

            iconKey
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
            val file = getCustomIconFile(context, iconKey)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Universal Composable to render either a built-in Material Icon or a Custom Image Icon.
     */
    @Composable
    fun AppIcon(
        iconName: String?,
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current
    ) {
        val context = LocalContext.current
        if (isCustomIcon(iconName)) {
            val customFile = getCustomIconFile(context, iconName ?: "")
            if (customFile.exists()) {
                AsyncImage(
                    model = customFile,
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
}
