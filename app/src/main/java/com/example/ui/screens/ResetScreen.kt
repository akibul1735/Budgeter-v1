package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.BiometricHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class ResetActionType {
    RESET_DATA,
    RESET_SETTINGS,
    RESET_DATA_AND_SETTINGS,
    RESET_TO_DEFAULT_STRUCTURE,
    DELETE_ALL_ACCOUNTS,
    DELETE_ALL_CATEGORIES,
    RESET_EVERYTHING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()

    // Every time the user enters this tab/screen, require authentication afresh
    var isAuthenticated by remember { mutableStateOf(false) }
    var activeDialogAction by remember { mutableStateOf<ResetActionType?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var pinErrorMessage by remember { mutableStateOf("") }
    var isAlphaPasswordMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-20f) at 50
                    20f at 100
                    (-15f) at 150
                    15f at 200
                    (-10f) at 250
                    10f at 300
                    (-5f) at 350
                    0f at 400
                }
            )
        }
    }

    fun promptBiometricAuth() {
        val activity = context as? FragmentActivity ?: return
        if (BiometricHelper.canAuthenticate(activity)) {
            val title = if (languageMode == LanguageMode.BANGLA) "রিসেট ট্যাব আনলক করুন" else "Unlock Reset Tab"
            val subtitle = if (languageMode == LanguageMode.BANGLA) "রিসেট বিকল্পগুলো দেখতে ফিঙ্গারপ্রিন্ট দিন" else "Authenticate with biometric to access reset settings"
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = title,
                subtitle = subtitle,
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    isAuthenticated = true
                    isPinError = false
                },
                onError = { _, _ -> },
                onNegativeClick = {}
            )
        }
    }

    // Trigger biometric prompt on entry if biometric is available
    LaunchedEffect(Unit) {
        if (!isAuthenticated) {
            promptBiometricAuth()
        }
    }

    // Re-lock when leaving
    DisposableEffect(Unit) {
        onDispose {
            isAuthenticated = false
        }
    }

    fun verifyPinInput(input: String) {
        if (!securityConfig.hasPin) {
            // No PIN is set in app security; allow access directly
            isAuthenticated = true
            isPinError = false
            enteredPin = ""
            return
        }
        if (viewModel.verifySecurityPin(input)) {
            isAuthenticated = true
            isPinError = false
            enteredPin = ""
        } else {
            isPinError = true
            pinErrorMessage = if (languageMode == LanguageMode.BANGLA) "ভুল পিন / পাসওয়ার্ড! পুনরায় চেষ্টা করুন।" else "Incorrect PIN or Password! Please try again."
            triggerShake()
            enteredPin = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (onBack != {}) onBack() else onOpenDrawer()
                        },
                        modifier = Modifier.testTag("reset_screen_nav_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(
                            onClick = {
                                isAuthenticated = false
                                enteredPin = ""
                            },
                            modifier = Modifier.testTag("reset_screen_lock_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Tab",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isAuthenticated) {
                // SECURITY AUTHENTICATION GATE SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা প্রমাণীকরণ প্রয়োজন" else "Security Authentication Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "রিসেট ট্যাবে প্রবেশের জন্য আপনার পাসওয়ার্ড বা বায়োমেট্রিক প্রদান করুন।"
                        else
                            "Please enter your PIN / Password or use biometric fingerprint to access reset options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // PIN Dots or Text Field
                    Box(
                        modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAlphaPasswordMode) {
                            OutlinedTextField(
                                value = enteredPin,
                                onValueChange = {
                                    enteredPin = it
                                    isPinError = false
                                },
                                label = { Text(if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড দিন" else "Enter Password") },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { verifyPinInput(enteredPin) }),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .testTag("reset_auth_password_input")
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 4) {
                                    val isFilled = i < enteredPin.length
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPinError) MaterialTheme.colorScheme.error
                                                else if (isFilled) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isPinError) MaterialTheme.colorScheme.error
                                                else if (isFilled) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    if (isPinError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isAlphaPasswordMode) {
                        Button(
                            onClick = { verifyPinInput(enteredPin) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp)
                                .testTag("reset_auth_submit_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "আনলক করুন" else "Unlock")
                        }
                    } else {
                        // Numeric Keypad
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val rows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("BIOMETRIC", "0", "BACKSPACE")
                            )

                            rows.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.forEach { key ->
                                        when (key) {
                                            "BIOMETRIC" -> {
                                                IconButton(
                                                    onClick = { promptBiometricAuth() },
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                        .testTag("reset_auth_biometric_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Fingerprint,
                                                        contentDescription = "Fingerprint",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                            "BACKSPACE" -> {
                                                IconButton(
                                                    onClick = {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                            isPinError = false
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                        .testTag("reset_auth_backspace_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                        contentDescription = "Backspace",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            else -> {
                                                Surface(
                                                    onClick = {
                                                        if (enteredPin.length < 6) {
                                                            val newPin = enteredPin + key
                                                            enteredPin = newPin
                                                            isPinError = false
                                                            if (newPin.length >= 4) {
                                                                verifyPinInput(newPin)
                                                            }
                                                        }
                                                    },
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .testTag("reset_auth_key_$key")
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = key,
                                                            fontSize = 22.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isAlphaPasswordMode = !isAlphaPasswordMode
                                enteredPin = ""
                                isPinError = false
                            }
                        ) {
                            Icon(
                                imageVector = if (isAlphaPasswordMode) Icons.Default.Password else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAlphaPasswordMode)
                                    (if (languageMode == LanguageMode.BANGLA) "সংখ্যা কীপ্যাড ব্যবহার করুন" else "Use Number Pad")
                                else
                                    (if (languageMode == LanguageMode.BANGLA) "টেক্সট পাসওয়ার্ড ব্যবহার করুন" else "Use Text Password"),
                                fontSize = 12.sp
                            )
                        }

                        if (!securityConfig.hasPin) {
                            TextButton(
                                onClick = {
                                    isAuthenticated = true
                                }
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সরাসরি প্রবেশ করুন" else "Continue without PIN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // AUTHENTICATED: DISPLAY 7 RESET OPTIONS
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Safety Warning Banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সতর্কতা: ধ্বংসাত্মক অপারেশন" else "Caution: Destructive Actions",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "রিসেট ও ডিলিট অপারেশনগুলো অপরিবর্তনীয়। নিশ্চিত হয়ে বিকল্প নির্বাচন করুন।"
                                    else
                                        "These operations will permanently modify or erase application data. Please choose carefully.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // 1. Reset Data
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "১. ডাটা রিসেট (Reset data)" else "1. Reset data",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "সকল লেনদেন, মাসিক বাজেট, রিমাইন্ডার ও ট্র্যাশ মুছে যাবে। ক্যাটাগরি, অ্যাকাউন্ট ও সেটিংস অপরিবর্তিত থাকবে।"
                        else
                            "Deletes all transactions, monthly budgets, recurring bills and trash. Preserves your accounts, categories and settings.",
                        icon = Icons.Default.CleaningServices,
                        iconTint = Color(0xFFE65100),
                        testTag = "reset_option_data",
                        onClick = { activeDialogAction = ResetActionType.RESET_DATA }
                    )

                    // 2. Reset Settings
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "২. সেটিংস রিসেট (Reset settings)" else "2. Reset settings",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "থিম, কারেন্সি, ট্যাব বার, ফরম্যাট ও ড্যাশবোর্ড সেটিংস ফ্যাক্টরি ডিফল্টে ফিরে যাবে। কোন লেনদেন বা ডাটা মুছবে না।"
                        else
                            "Restores themes, currency, tab preferences, formats and display configs to default. Keeps all financial data intact.",
                        icon = Icons.Default.Tune,
                        iconTint = SolidPrimary,
                        testTag = "reset_option_settings",
                        onClick = { activeDialogAction = ResetActionType.RESET_SETTINGS }
                    )

                    // 3. Reset Data and Settings
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "৩. ডাটা ও সেটিংস রিসেট (Reset data and settings)" else "3. Reset data and settings",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "লেনদেন ও বাজেট ডাটা মুছে যাবে এবং সকল সেটিংস ডিফল্টে রিসেট হবে। শুধু অ্যাকাউন্ট ও ক্যাটাগরি চার্ট বহাল থাকবে।"
                        else
                            "Clears all transactions, budgets and resets all settings to default. Preserves account & category structures.",
                        icon = Icons.Default.LayersClear,
                        iconTint = Color(0xFFD97706),
                        testTag = "reset_option_data_and_settings",
                        onClick = { activeDialogAction = ResetActionType.RESET_DATA_AND_SETTINGS }
                    )

                    // 4. Reset to Default (Editable & Deletable Structure)
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "৪. ডিফল্ট স্ট্রাকচারে রিসেট (Reset to default)" else "4. Reset to default (Starter Structure)",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "সকল বর্তমান ডাটা মুছে একটি আদর্শ নতুন চার্ট অব অ্যাকাউন্ট ও ক্যাটাগরি যুক্ত হবে (সবগুলোই সম্পাদন ও ডিলিটযোগ্য)।"
                        else
                            "Resets data and creates a fresh default structure of parent/sub accounts and categories (100% editable & deletable).",
                        icon = Icons.Default.AccountTree,
                        iconTint = Color(0xFF7C3AED),
                        testTag = "reset_option_default_structure",
                        onClick = { activeDialogAction = ResetActionType.RESET_TO_DEFAULT_STRUCTURE }
                    )

                    // 5. Delete All Accounts
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "৫. সকল অ্যাকাউন্ট মুছুন (Delete all accounts)" else "5. Delete all accounts",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "ডাটাবেজ থেকে সমস্ত ব্যাংক, ক্যাশ, ওয়ালেট ও ঋণ অ্যাকাউন্ট এবং তাদের লেনদেন মুছে যাবে।"
                        else
                            "Permanently removes all asset, liability and equity accounts along with referencing transactions.",
                        icon = Icons.Default.AccountBalance,
                        iconTint = SolidExpense,
                        testTag = "reset_option_delete_accounts",
                        onClick = { activeDialogAction = ResetActionType.DELETE_ALL_ACCOUNTS }
                    )

                    // 6. Delete All Categories
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "৬. সকল ক্যাটাগরি মুছুন (Delete all categories)" else "6. Delete all categories",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "সমস্ত আয় ও ব্যয়ের ক্যাটাগরি ও সাব-ক্যাটাগরি এবং তাদের সংশ্লিষ্ট লেনদেন মুছে যাবে।"
                        else
                            "Permanently removes all income and expense categories/subcategories along with referencing transactions.",
                        icon = Icons.Default.Category,
                        iconTint = Color(0xFFDC2626),
                        testTag = "reset_option_delete_categories",
                        onClick = { activeDialogAction = ResetActionType.DELETE_ALL_CATEGORIES }
                    )

                    // 7. Reset Everything from App
                    ResetOptionCard(
                        title = if (languageMode == LanguageMode.BANGLA) "৭. অ্যাপের সবকিছু সম্পূর্ণ রিসেট (Reset everything from app)" else "7. Reset everything from app",
                        description = if (languageMode == LanguageMode.BANGLA)
                            "সম্পূর্ণ ফ্যাক্টরি ওয়াইপ: সমস্ত লেনদেন, অ্যাকাউন্ট, ক্যাটাগরি, বাজেট, সেটিংস মুছে একদম নতুন অবস্থায় ফিরে যাবে।"
                        else
                            "Complete factory wipe: Erases all accounts, categories, transactions, budgets, settings and restores fresh default setup.",
                        icon = Icons.Default.DeleteSweep,
                        iconTint = MaterialTheme.colorScheme.error,
                        isDanger = true,
                        testTag = "reset_option_reset_everything",
                        onClick = { activeDialogAction = ResetActionType.RESET_EVERYTHING }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // CONFIRMATION DIALOG
    activeDialogAction?.let { action ->
        val (dialogTitle, dialogDesc, confirmButtonText) = when (action) {
            ResetActionType.RESET_DATA -> Triple(
                if (languageMode == LanguageMode.BANGLA) "লেনদেন ও বাজেট ডাটা রিসেট করবেন?" else "Reset Transaction & Budget Data?",
                if (languageMode == LanguageMode.BANGLA)
                    "আপনার সমস্ত লেনদেন, মাসিক বাজেট, বাজেট সমন্বয় ও রিমাইন্ডার বিল মুছে ফেলা হবে। অ্যাকাউন্ট ও ক্যাটাগরি বহাল থাকবে।"
                else
                    "All transactions, monthly budgets, adjustments and recurring bills will be cleared. Accounts, categories, and settings will remain.",
                if (languageMode == LanguageMode.BANGLA) "ডাটা রিসেট করুন" else "Reset Data"
            )
            ResetActionType.RESET_SETTINGS -> Triple(
                if (languageMode == LanguageMode.BANGLA) "সমস্ত সেটিংস রিসেট করবেন?" else "Reset All Settings to Defaults?",
                if (languageMode == LanguageMode.BANGLA)
                    "থিম, ভাষা, কারেন্সি, ট্যাব বিন্যাস ও ড্যাশবোর্ড সেটিংস ফ্যাক্টরি ডিফল্টে ফিরে যাবে।"
                else
                    "Theme, currency, tab configuration, autofill and dashboard preferences will be restored to defaults.",
                if (languageMode == LanguageMode.BANGLA) "সেটিংস রিসেট করুন" else "Reset Settings"
            )
            ResetActionType.RESET_DATA_AND_SETTINGS -> Triple(
                if (languageMode == LanguageMode.BANGLA) "ডাটা ও সেটিংস রিসেট করবেন?" else "Reset Data and Settings?",
                if (languageMode == LanguageMode.BANGLA)
                    "সমস্ত লেনদেন ডাটা মুছে যাবে এবং সব সেটিংস ডিফল্টে রিসেট হবে।"
                else
                    "All recorded transactions will be cleared and all application settings will return to factory defaults.",
                if (languageMode == LanguageMode.BANGLA) "ডাটা ও সেটিংস রিসেট করুন" else "Reset Data & Settings"
            )
            ResetActionType.RESET_TO_DEFAULT_STRUCTURE -> Triple(
                if (languageMode == LanguageMode.BANGLA) "ডিফল্ট স্ট্রাকচারে রিসেট করবেন?" else "Reset to Default Starter Structure?",
                if (languageMode == LanguageMode.BANGLA)
                    "বর্তমান সমস্ত ডাটা মুছে একটি নতুন আদর্শ চার্ট অব অ্যাকাউন্ট ও ক্যাটাগরি সেট করা হবে যা আপনি সম্পূর্ণ এডিট ও ডিলিট করতে পারবেন।"
                else
                    "Existing data will be replaced with a clean starter set of editable and deletable parent & sub accounts and categories.",
                if (languageMode == LanguageMode.BANGLA) "ডিফল্টে রিসেট করুন" else "Restore Default Structure"
            )
            ResetActionType.DELETE_ALL_ACCOUNTS -> Triple(
                if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট মুছে ফেলবেন?" else "Delete All Accounts?",
                if (languageMode == LanguageMode.BANGLA)
                    "সমস্ত ক্যাশ, ব্যাংক, ওয়ালেট ও ঋণ অ্যাকাউন্ট এবং এদের সাথে সম্পর্কিত সকল লেনদেন স্থায়ীভাবে মুছে যাবে।"
                else
                    "All accounts and their associated transaction history will be permanently deleted.",
                if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট মুছুন" else "Delete All Accounts"
            )
            ResetActionType.DELETE_ALL_CATEGORIES -> Triple(
                if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি মুছে ফেলবেন?" else "Delete All Categories?",
                if (languageMode == LanguageMode.BANGLA)
                    "সমস্ত আয় ও ব্যয়ের ক্যাটাগরি এবং এদের সাথে সম্পর্কিত সমস্ত লেনদেন স্থায়ীভাবে মুছে যাবে।"
                else
                    "All categories, subcategories, budgets and associated transactions will be permanently deleted.",
                if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি মুছুন" else "Delete All Categories"
            )
            ResetActionType.RESET_EVERYTHING -> Triple(
                if (languageMode == LanguageMode.BANGLA) "অ্যাপের সবকিছু সম্পূর্ণ রিসেট করবেন?" else "Reset Everything From App?",
                if (languageMode == LanguageMode.BANGLA)
                    "এটি একটি সম্পূর্ণ ফ্যাক্টরি ক্লিন রিসেট। আপনার সমস্ত লেনদেন, অ্যাকাউন্ট, ক্যাটাগরি ও সেটিংস মুছে অ্যাপটি একদম নতুন ইন্সটলেশনের মতো হবে।"
                else
                    "This is a complete factory wipe. All transactions, accounts, categories, budgets, trash and settings will be wiped clean.",
                if (languageMode == LanguageMode.BANGLA) "সবকিছু সম্পূর্ণ রিসেট করুন" else "Wipe Everything"
            )
        }

        AlertDialog(
            onDismissRequest = { activeDialogAction = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = dialogDesc,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentAction = action
                        activeDialogAction = null
                        when (currentAction) {
                            ResetActionType.RESET_DATA -> {
                                viewModel.resetData {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "ডাটা সফলভাবে রিসেট করা হয়েছে।" else "Data reset successfully."
                                        )
                                    }
                                }
                            }
                            ResetActionType.RESET_SETTINGS -> {
                                viewModel.resetSettings {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "সেটিংস ফ্যাক্টরি ডিফল্টে ফিরে গেছে।" else "Settings restored to factory defaults."
                                        )
                                    }
                                }
                            }
                            ResetActionType.RESET_DATA_AND_SETTINGS -> {
                                viewModel.resetDataAndSettings {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "ডাটা ও সেটিংস সফলভাবে রিসেট হয়েছে।" else "Data and settings reset successfully."
                                        )
                                    }
                                }
                            }
                            ResetActionType.RESET_TO_DEFAULT_STRUCTURE -> {
                                viewModel.resetToDefaultStructure {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "ডিফল্ট সম্পাদনযোগ্য স্ট্রাকচার সেটআপ সম্পন্ন।" else "Default editable structure restored successfully."
                                        )
                                    }
                                }
                            }
                            ResetActionType.DELETE_ALL_ACCOUNTS -> {
                                viewModel.deleteAllAccounts {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট সফলভাবে মুছে ফেলা হয়েছে।" else "All accounts deleted successfully."
                                        )
                                    }
                                }
                            }
                            ResetActionType.DELETE_ALL_CATEGORIES -> {
                                viewModel.deleteAllCategories {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি সফলভাবে মুছে ফেলা হয়েছে।" else "All categories deleted successfully."
                                        )
                                    }
                                }
                            }
                            ResetActionType.RESET_EVERYTHING -> {
                                viewModel.resetEverything {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (languageMode == LanguageMode.BANGLA) "অ্যাপের সবকিছু সম্পূর্ণ নতুন অবস্থায় রিসেট হয়েছে।" else "App completely wiped and reset to factory defaults."
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("reset_dialog_confirm_btn")
                ) {
                    Text(confirmButtonText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { activeDialogAction = null },
                    modifier = Modifier.testTag("reset_dialog_cancel_btn")
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ResetOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    isDanger: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.14f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
