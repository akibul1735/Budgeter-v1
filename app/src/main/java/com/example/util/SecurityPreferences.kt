package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class SecurityConfig(
    val isAppLockEnabled: Boolean = false,
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false,
    val requireAuthForGroupDeletion: Boolean = true,
    val requireAuthForMultiSelect: Boolean = true,
    val requireAuthForTrashClear: Boolean = true,
    val requireAuthForBackupRestore: Boolean = true,
    val requireAuthForBackupDeletion: Boolean = true,
    val lockTimeoutSeconds: Int = 0, // 0 = Immediately, 60 = 1 min, 300 = 5 mins, 900 = 15 mins
    val securityQuestion: String = "What was the name of your first school?",
    val securityAnswerHash: String = ""
) {
    val hasPin: Boolean
        get() = pinHash.isNotBlank()

    val hasRecoveryQuestion: Boolean
        get() = securityAnswerHash.isNotBlank() && securityQuestion.isNotBlank()
}

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_security_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<SecurityConfig> = _config.asStateFlow()

    private fun loadConfig(): SecurityConfig {
        return SecurityConfig(
            isAppLockEnabled = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false),
            pinHash = prefs.getString(KEY_PIN_HASH, "") ?: "",
            isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            requireAuthForGroupDeletion = prefs.getBoolean(KEY_REQUIRE_AUTH_GROUP_DELETION, true),
            requireAuthForMultiSelect = prefs.getBoolean(KEY_REQUIRE_AUTH_MULTI_SELECT, true),
            requireAuthForTrashClear = prefs.getBoolean(KEY_REQUIRE_AUTH_TRASH_CLEAR, true),
            requireAuthForBackupRestore = prefs.getBoolean(KEY_REQUIRE_AUTH_BACKUP_RESTORE, true),
            requireAuthForBackupDeletion = prefs.getBoolean(KEY_REQUIRE_AUTH_BACKUP_DELETION, true),
            lockTimeoutSeconds = prefs.getInt(KEY_LOCK_TIMEOUT_SECONDS, 0),
            securityQuestion = prefs.getString(KEY_SECURITY_QUESTION, "What is your favorite color?") ?: "What is your favorite color?",
            securityAnswerHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, "") ?: ""
        )
    }

    private fun hashSecure(input: String, saltBase64: String? = null): String {
        if (input.isEmpty()) return ""
        return try {
            val salt = if (saltBase64 != null) {
                Base64.decode(saltBase64, Base64.NO_WRAP)
            } else {
                val random = SecureRandom()
                val newSalt = ByteArray(16)
                random.nextBytes(newSalt)
                newSalt
            }
            val spec = PBEKeySpec(input.toCharArray(), salt, 5000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            val saltEncoded = Base64.encodeToString(salt, Base64.NO_WRAP)
            val hashEncoded = Base64.encodeToString(hash, Base64.NO_WRAP)
            "v2\$$saltEncoded\$$hashEncoded"
        } catch (_: Exception) {
            // Fallback to SHA-256 in rare legacy environment
            legacyHash(input)
        }
    }

    private fun legacyHash(input: String): String {
        if (input.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyHash(input: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return true
        if (storedHash.startsWith("v2$")) {
            val parts = storedHash.split("$")
            if (parts.size == 3) {
                val salt = parts[1]
                val computed = hashSecure(input, salt)
                return computed == storedHash
            }
        }
        return legacyHash(input) == storedHash
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        _config.value = _config.value.copy(isAppLockEnabled = enabled)
    }

    fun setPin(rawPin: String) {
        val hash = if (rawPin.isBlank()) "" else hashSecure(rawPin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
        _config.value = _config.value.copy(pinHash = hash)
    }

    fun verifyPin(rawInput: String): Boolean {
        val currentHash = _config.value.pinHash
        if (currentHash.isBlank()) return true
        val isMatch = verifyHash(rawInput, currentHash)
        // Opportunistic upgrade from legacy SHA-256 to salted PBKDF2
        if (isMatch && !currentHash.startsWith("v2$") && rawInput.isNotBlank()) {
            setPin(rawInput)
        }
        return isMatch
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _config.value = _config.value.copy(isBiometricEnabled = enabled)
    }

    fun setRequireAuthForGroupDeletion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_AUTH_GROUP_DELETION, enabled).apply()
        _config.value = _config.value.copy(requireAuthForGroupDeletion = enabled)
    }

    fun setRequireAuthForMultiSelect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_AUTH_MULTI_SELECT, enabled).apply()
        _config.value = _config.value.copy(requireAuthForMultiSelect = enabled)
    }

    fun setRequireAuthForTrashClear(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_AUTH_TRASH_CLEAR, enabled).apply()
        _config.value = _config.value.copy(requireAuthForTrashClear = enabled)
    }

    fun setRequireAuthForBackupRestore(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_AUTH_BACKUP_RESTORE, enabled).apply()
        _config.value = _config.value.copy(requireAuthForBackupRestore = enabled)
    }

    fun setRequireAuthForBackupDeletion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_AUTH_BACKUP_DELETION, enabled).apply()
        _config.value = _config.value.copy(requireAuthForBackupDeletion = enabled)
    }

    fun setLockTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECONDS, seconds).apply()
        _config.value = _config.value.copy(lockTimeoutSeconds = seconds)
    }

    fun setSecurityRecovery(question: String, rawAnswer: String) {
        val answerHash = if (rawAnswer.isBlank()) "" else hashSecure(rawAnswer.trim().lowercase())
        prefs.edit()
            .putString(KEY_SECURITY_QUESTION, question)
            .putString(KEY_SECURITY_ANSWER_HASH, answerHash)
            .apply()
        _config.value = _config.value.copy(
            securityQuestion = question,
            securityAnswerHash = answerHash
        )
    }

    fun verifySecurityAnswer(rawAnswer: String): Boolean {
        val currentHash = _config.value.securityAnswerHash
        if (currentHash.isBlank()) return false
        val isMatch = verifyHash(rawAnswer.trim().lowercase(), currentHash)
        if (isMatch && !currentHash.startsWith("v2$") && rawAnswer.isNotBlank()) {
            setSecurityRecovery(_config.value.securityQuestion, rawAnswer)
        }
        return isMatch
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _config.value = loadConfig()
    }

    companion object {
        private const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_REQUIRE_AUTH_GROUP_DELETION = "key_require_auth_group_deletion"
        private const val KEY_REQUIRE_AUTH_MULTI_SELECT = "key_require_auth_multi_select"
        private const val KEY_REQUIRE_AUTH_TRASH_CLEAR = "key_require_auth_trash_clear"
        private const val KEY_REQUIRE_AUTH_BACKUP_RESTORE = "key_require_auth_backup_restore"
        private const val KEY_REQUIRE_AUTH_BACKUP_DELETION = "key_require_auth_backup_deletion"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "key_lock_timeout_seconds"
        private const val KEY_SECURITY_QUESTION = "key_security_question"
        private const val KEY_SECURITY_ANSWER_HASH = "key_security_answer_hash"
    }
}
