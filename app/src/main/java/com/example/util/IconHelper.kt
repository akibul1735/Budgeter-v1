package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {

    fun parseColorHex(hex: String?, fallback: Color = Color(0xFF1E56A0)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }

    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "AccountBalance" -> Icons.Default.AccountBalance
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "Wallet" -> Icons.Default.Wallet
            "Payments" -> Icons.Default.Payments
            "CreditCard" -> Icons.Default.CreditCard
            "Savings" -> Icons.Default.Savings
            "PhoneAndroid", "PhoneIphone" -> Icons.Default.PhoneAndroid
            "Restaurant" -> Icons.Default.Restaurant
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "DinnerDining" -> Icons.Default.DinnerDining
            "LocalCafe" -> Icons.Default.LocalCafe
            "Home" -> Icons.Default.Home
            "Apartment" -> Icons.Default.Apartment
            "ElectricBolt" -> Icons.Default.ElectricBolt
            "Wifi" -> Icons.Default.Wifi
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Train" -> Icons.Default.Train
            "TwoWheeler" -> Icons.Default.TwoWheeler
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "LocalHospital" -> Icons.Default.LocalHospital
            "Medication" -> Icons.Default.Medication
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "Checkroom" -> Icons.Default.Checkroom
            "Work" -> Icons.Default.Work
            "School" -> Icons.Default.School
            "Handshake" -> Icons.Default.Handshake
            "MoneyOff" -> Icons.Default.MoneyOff
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
            else -> Icons.Default.Category
        }
    }
}
