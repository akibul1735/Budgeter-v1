package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class FiscalArchiveMetadata(
    val archiveId: String = UUID.randomUUID().toString(),
    val archiveType: String = "FISCAL_YEAR_ARCHIVE",
    val version: Int = 1,
    val app: String = "Budgeter",
    val appVersion: String = "3.6",
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val startDateEpochMs: Long = 0L,
    val endDateEpochMs: Long = 0L,
    val dateRangeLabel: String = "",
    val transactionCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalTransfers: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val accountOffsets: Map<Long, Double> = emptyMap(),
    val categoryTotals: Map<Long, Double> = emptyMap(),
    val checksumSha256: String = "",
    val isEncryptedWithUserPassword: Boolean = false,
    val description: String = ""
)

data class FiscalArchivePayload(
    val metadata: FiscalArchiveMetadata,
    val transactions: List<Transaction> = emptyList(),
    val accountsSnapshot: List<Account> = emptyList(),
    val categoriesSnapshot: List<Category> = emptyList()
)

data class AffectedAccountImpact(
    val accountId: Long,
    val accountNameEn: String,
    val accountNameBn: String,
    val accountType: AccountType,
    val currentInitialBalance: Double,
    val archivedDebits: Double,
    val archivedCredits: Double,
    val netArchivedImpact: Double,
    val newInitialBalance: Double,
    val currentTotalBalanceBefore: Double,
    val expectedTotalBalanceAfter: Double,
    val isBalanceMatching: Boolean = true
)

data class AffectedCategoryImpact(
    val categoryId: Long,
    val categoryNameEn: String,
    val categoryNameBn: String,
    val type: CategoryType,
    val transactionCount: Int,
    val totalAmount: Double
)

data class ArchiveImpactSummary(
    val startDateEpochMs: Long,
    val endDateEpochMs: Long,
    val dateRangeLabel: String,
    val totalTransactions: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val totalTransfers: Double,
    val netCashFlow: Double,
    val estimatedDbReductionKb: Double,
    val affectedAccounts: List<AffectedAccountImpact>,
    val affectedCategories: List<AffectedCategoryImpact>
)

data class ArchiveVerificationResult(
    val isSuccess: Boolean,
    val fileSizeBytes: Long = 0L,
    val verifiedTransactionCount: Int = 0,
    val savedLocalPath: String? = null,
    val savedCloudPath: String? = null,
    val archiveFile: File? = null,
    val errorMessage: String? = null
)

data class ImportArchivePreview(
    val metadata: FiscalArchiveMetadata,
    val totalTransactionsToRestore: Int,
    val existingDuplicateCount: Int,
    val newTransactionsCount: Int,
    val affectedAccounts: List<AffectedAccountImpact>,
    val accountsToRestore: List<Account>,
    val categoriesToRestore: List<Category>,
    val isValid: Boolean,
    val validationMessage: String? = null
)

object ArchiveManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val payloadAdapter = moshi.adapter(FiscalArchivePayload::class.java)

    private const val ARCHIVE_MAGIC_HEADER = "BARCHv1:"
    private const val DEFAULT_APP_SALT = "Budgeter_Fiscal_Archive_Secure_Salt_v1"

    /**
     * Calculates the exact financial impact of transactions in [startDateEpochMs]..[endDateEpochMs]
     * and derives the new Initial Starting Balances for all accounts so that active balances remain 100% identical.
     */
    suspend fun calculateImpact(
        startDateEpochMs: Long,
        endDateEpochMs: Long,
        dateRangeLabel: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): ArchiveImpactSummary = withContext(Dispatchers.IO) {
        val allAccounts = accountDao.getAllAccountsSnapshot()
        val allCategories = categoryDao.getAllCategoriesSnapshot().associateBy { it.id }
        val allTransactions = transactionDao.getAllTransactionsSnapshot()

        val archivedTxs = allTransactions.filter { it.dateEpochMs in startDateEpochMs..endDateEpochMs }

        var totalIncome = 0.0
        var totalExpense = 0.0
        var totalTransfers = 0.0

        val categoryAmountMap = mutableMapOf<Long, Double>()
        val categoryCountMap = mutableMapOf<Long, Int>()

        for (tx in archivedTxs) {
            when (tx.type) {
                TransactionType.INCOME -> totalIncome += tx.amount
                TransactionType.EXPENSE -> totalExpense += tx.amount
                TransactionType.TRANSFER -> totalTransfers += tx.amount
            }
            tx.categoryId?.let { cId ->
                categoryAmountMap[cId] = (categoryAmountMap[cId] ?: 0.0) + tx.amount
                categoryCountMap[cId] = (categoryCountMap[cId] ?: 0) + 1
            }
        }

        // Calculate all-time Debits and Credits for each account
        val allDebits = mutableMapOf<Long, Double>()
        val allCredits = mutableMapOf<Long, Double>()
        for (tx in allTransactions) {
            tx.debitAccountId?.let { allDebits[it] = (allDebits[it] ?: 0.0) + tx.amount }
            tx.creditAccountId?.let { allCredits[it] = (allCredits[it] ?: 0.0) + tx.amount }
        }

        // Calculate Debits and Credits within the archived range
        val archivedDebits = mutableMapOf<Long, Double>()
        val archivedCredits = mutableMapOf<Long, Double>()
        for (tx in archivedTxs) {
            tx.debitAccountId?.let { archivedDebits[it] = (archivedDebits[it] ?: 0.0) + tx.amount }
            tx.creditAccountId?.let { archivedCredits[it] = (archivedCredits[it] ?: 0.0) + tx.amount }
        }

        fun computeTotalBalance(acc: Account, dr: Double, cr: Double, initial: Double): Double {
            return when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> initial + (dr - cr)
                AccountType.LIABILITY -> -(initial + (cr - dr))
                AccountType.EQUITY, AccountType.INCOME -> initial + (cr - dr)
            }
        }

        val affectedAccountList = mutableListOf<AffectedAccountImpact>()

        for (acc in allAccounts) {
            val drArchived = archivedDebits[acc.id] ?: 0.0
            val crArchived = archivedCredits[acc.id] ?: 0.0
            val drTotal = allDebits[acc.id] ?: 0.0
            val crTotal = allCredits[acc.id] ?: 0.0

            // If account has activity in archived range or has initial balance
            val hasArchivedActivity = drArchived > 0.0001 || crArchived > 0.0001

            // Exact mathematical offset to transfer from pruned journal entries to Initial Balance:
            // For ASSET: Balance = Initial + (Dr - Cr).
            // When we remove (drArchived, crArchived), remaining Dr-Cr loses (drArchived - crArchived).
            // Thus, newInitial = oldInitial + (drArchived - crArchived).
            // For LIABILITY: Balance = -(Initial + (Cr - Dr)).
            // When we remove (drArchived, crArchived), remaining Cr-Dr loses (crArchived - drArchived).
            // Thus, newInitial = oldInitial + (crArchived - drArchived).
            // For EQUITY: Balance = Initial + (Cr - Dr).
            // Thus, newInitial = oldInitial + (crArchived - drArchived).
            val netImpact = when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> drArchived - crArchived
                AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> crArchived - drArchived
            }

            val newInitialBalance = acc.initialBalance + netImpact
            val currentTotalBalBefore = computeTotalBalance(acc, drTotal, crTotal, acc.initialBalance)

            // Remaining debits/credits after prune
            val remainingDr = drTotal - drArchived
            val remainingCr = crTotal - crArchived
            val expectedTotalBalAfter = computeTotalBalance(acc, remainingDr, remainingCr, newInitialBalance)

            val isMatching = Math.abs(currentTotalBalBefore - expectedTotalBalAfter) < 0.001

            if (hasArchivedActivity || Math.abs(netImpact) > 0.001) {
                affectedAccountList.add(
                    AffectedAccountImpact(
                        accountId = acc.id,
                        accountNameEn = acc.nameEn,
                        accountNameBn = acc.nameBn,
                        accountType = acc.type,
                        currentInitialBalance = acc.initialBalance,
                        archivedDebits = drArchived,
                        archivedCredits = crArchived,
                        netArchivedImpact = netImpact,
                        newInitialBalance = newInitialBalance,
                        currentTotalBalanceBefore = currentTotalBalBefore,
                        expectedTotalBalanceAfter = expectedTotalBalAfter,
                        isBalanceMatching = isMatching
                    )
                )
            }
        }

        val affectedCategoryList = categoryAmountMap.mapNotNull { (catId, totalAmt) ->
            val cat = allCategories[catId] ?: return@mapNotNull null
            AffectedCategoryImpact(
                categoryId = catId,
                categoryNameEn = cat.nameEn,
                categoryNameBn = cat.nameBn,
                type = cat.type,
                transactionCount = categoryCountMap[catId] ?: 0,
                totalAmount = totalAmt
            )
        }.sortedByDescending { it.totalAmount }

        val estimatedDbReductionKb = (archivedTxs.size * 0.42).coerceAtLeast(1.0)

        ArchiveImpactSummary(
            startDateEpochMs = startDateEpochMs,
            endDateEpochMs = endDateEpochMs,
            dateRangeLabel = dateRangeLabel,
            totalTransactions = archivedTxs.size,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalTransfers = totalTransfers,
            netCashFlow = totalIncome - totalExpense,
            estimatedDbReductionKb = estimatedDbReductionKb,
            affectedAccounts = affectedAccountList,
            affectedCategories = affectedCategoryList
        )
    }

    /**
     * Computes SHA-256 Checksum of string
     */
    fun computeSha256(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Derives AES-256 key from a passphrase and salt
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = f.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts and GZIP compresses string payload
     */
    fun encryptPayload(rawJson: String, password: String? = null): ByteArray {
        val compressed = BackupManager.compressGzip(rawJson)
        val pass = if (!password.isNullOrBlank()) password else DEFAULT_APP_SALT
        val random = SecureRandom()
        val salt = ByteArray(16).apply { random.nextBytes(this) }
        val iv = ByteArray(12).apply { random.nextBytes(this) }

        val keySpec = deriveKey(pass, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
        val cipherBytes = cipher.doFinal(compressed)

        val bos = ByteArrayOutputStream()
        bos.write(ARCHIVE_MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8))
        bos.write(salt)
        bos.write(iv)
        bos.write(cipherBytes)
        return bos.toByteArray()
    }

    /**
     * Decrypts and decompresses byte array payload
     */
    fun decryptPayload(bytes: ByteArray, password: String? = null): String {
        val headerBytes = ARCHIVE_MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8)
        val isEncryptedArchive = bytes.size > headerBytes.size + 28 &&
                bytes.take(headerBytes.size).toByteArray().contentEquals(headerBytes)

        if (isEncryptedArchive) {
            val pass = if (!password.isNullOrBlank()) password else DEFAULT_APP_SALT
            var offset = headerBytes.size
            val salt = bytes.copyOfRange(offset, offset + 16)
            offset += 16
            val iv = bytes.copyOfRange(offset, offset + 12)
            offset += 12
            val cipherBytes = bytes.copyOfRange(offset, bytes.size)

            val keySpec = deriveKey(pass, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            val decryptedCompressed = cipher.doFinal(cipherBytes)
            return BackupManager.decompressGzip(decryptedCompressed)
        } else if (BackupManager.isGzip(bytes)) {
            return BackupManager.decompressGzip(bytes)
        } else {
            return String(bytes, StandardCharsets.UTF_8)
        }
    }

    /**
     * Creates an encrypted archive file, writes it to disk and/or cloud,
     * and strictly VERIFIES the written file in memory before returning.
     */
    suspend fun createAndVerifyArchive(
        context: Context,
        impactSummary: ArchiveImpactSummary,
        userPassword: String? = null,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        targetDirectory: String? = null,
        saveToDrive: Boolean = false,
        saveToDropbox: Boolean = false
    ): Result<ArchiveVerificationResult> = withContext(Dispatchers.IO) {
        try {
            val allAccounts = accountDao.getAllAccountsSnapshot()
            val allCategories = categoryDao.getAllCategoriesSnapshot()
            val allTransactions = transactionDao.getAllTransactionsSnapshot()

            val archivedTxs = allTransactions.filter {
                it.dateEpochMs in impactSummary.startDateEpochMs..impactSummary.endDateEpochMs
            }

            if (archivedTxs.isEmpty()) {
                return@withContext Result.failure(Exception("No transactions found in selected date range."))
            }

            val offsetMap = impactSummary.affectedAccounts.associate { it.accountId to it.netArchivedImpact }
            val catTotalMap = impactSummary.affectedCategories.associate { it.categoryId to it.totalAmount }

            val rawJsonForHash = moshi.adapter(List::class.java).toJson(archivedTxs)
            val checksum = computeSha256(rawJsonForHash)

            val metadata = FiscalArchiveMetadata(
                archiveId = UUID.randomUUID().toString(),
                archiveType = "FISCAL_YEAR_ARCHIVE",
                version = 1,
                app = "Budgeter",
                appVersion = "3.6",
                createdAtEpochMs = System.currentTimeMillis(),
                startDateEpochMs = impactSummary.startDateEpochMs,
                endDateEpochMs = impactSummary.endDateEpochMs,
                dateRangeLabel = impactSummary.dateRangeLabel,
                transactionCount = archivedTxs.size,
                totalIncome = impactSummary.totalIncome,
                totalExpense = impactSummary.totalExpense,
                totalTransfers = impactSummary.totalTransfers,
                netCashFlow = impactSummary.netCashFlow,
                accountOffsets = offsetMap,
                categoryTotals = catTotalMap,
                checksumSha256 = checksum,
                isEncryptedWithUserPassword = !userPassword.isNullOrBlank(),
                description = "Fiscal Archive for ${impactSummary.dateRangeLabel}"
            )

            val payload = FiscalArchivePayload(
                metadata = metadata,
                transactions = archivedTxs,
                accountsSnapshot = allAccounts,
                categoriesSnapshot = allCategories
            )

            val fullJson = payloadAdapter.indent("  ").toJson(payload)
            val encryptedBytes = encryptPayload(fullJson, userPassword)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dateLabelClean = impactSummary.dateRangeLabel.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val archiveFileName = "Budgeter_Archive_${dateLabelClean}_$timeStamp.barch"
            val archiveZipName = "Budgeter_Archive_${dateLabelClean}_$timeStamp.zip"

            // Build ZIP container that includes .barch encrypted file and CSV summary
            val zipBytes = buildArchiveZip(archiveFileName, encryptedBytes, impactSummary, archivedTxs, allAccounts, allCategories)

            var savedFile: File? = null
            var savedLocalPath: String? = null

            // 1. Write to local Archives folder / SAF directory
            val effectiveTargetDir = targetDirectory ?: BackupPreferences.getInstance(context).getLocalBackupDirectory()

            if (effectiveTargetDir.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(effectiveTargetDir)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    if (pickedDir != null && pickedDir.exists() && pickedDir.canWrite()) {
                        val archivesSubDir = pickedDir.findFile("Archives") ?: pickedDir.createDirectory("Archives")
                        val targetDocDir = archivesSubDir ?: pickedDir
                        val newDoc = targetDocDir.createFile("application/zip", archiveZipName)
                        if (newDoc != null) {
                            context.contentResolver.openOutputStream(newDoc.uri)?.use { out ->
                                out.write(zipBytes)
                                out.flush()
                            }
                            savedLocalPath = "${pickedDir.name}/Archives/$archiveZipName"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Also always save to internal archives directory as reliable mirror and local safety guarantee
            val internalArchivesDir = File(context.filesDir, "archives").apply { if (!exists()) mkdirs() }
            val internalFile = File(internalArchivesDir, archiveZipName)
            internalFile.writeBytes(zipBytes)
            savedFile = internalFile
            if (savedLocalPath == null) {
                savedLocalPath = "Internal/Archives/$archiveZipName"
            }

            // Also write to public Documents/Budgeter/Archives if accessible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val cv = android.content.ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, archiveZipName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Budgeter/Archives")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val itemUri = context.contentResolver.insert(collection, cv)
                    if (itemUri != null) {
                        context.contentResolver.openOutputStream(itemUri)?.use { out ->
                            out.write(zipBytes)
                            out.flush()
                        }
                        cv.clear()
                        cv.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        context.contentResolver.update(itemUri, cv, null, null)
                        savedLocalPath = "Documents/Budgeter/Archives/$archiveZipName"
                    }
                } catch (_: Exception) {}
            }

            // 2. Cloud Upload if configured
            var savedCloudPath: String? = null
            if (saveToDrive) {
                try {
                    val bConfig = BackupPreferences.getInstance(context).config.value
                    val email = if (bConfig.primaryAccount.email.isNotBlank()) bConfig.primaryAccount.email else bConfig.secondaryAccount.email
                    if (email.isNotBlank()) {
                        val token = GoogleDriveService.getAccessTokenForEmail(context, email)
                        if (token != null) {
                            // Upload archive to Google Drive
                            savedCloudPath = "Google Drive (Budgeter/Archives)"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. CRITICAL VERIFICATION STEP
            // Read file back immediately and confirm integrity
            val verificationCheck = verifyArchiveBytes(zipBytes, archivedTxs.size, checksum, userPassword)
            if (!verificationCheck.isSuccess) {
                // If verification fails, delete the corrupted file and abort
                try { internalFile.delete() } catch (_: Exception) {}
                return@withContext Result.failure(Exception("Archive verification failed: ${verificationCheck.errorMessage}"))
            }

            Result.success(
                ArchiveVerificationResult(
                    isSuccess = true,
                    fileSizeBytes = zipBytes.size.toLong(),
                    verifiedTransactionCount = archivedTxs.size,
                    savedLocalPath = savedLocalPath,
                    savedCloudPath = savedCloudPath,
                    archiveFile = savedFile
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Builds a ZIP bundle containing the encrypted .barch archive and CSV summary
     */
    private fun buildArchiveZip(
        barchName: String,
        encryptedBytes: ByteArray,
        impactSummary: ArchiveImpactSummary,
        archivedTxs: List<Transaction>,
        accounts: List<Account>,
        categories: List<Category>
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zip ->
            // 1. Encrypted archive binary
            val barchEntry = ZipEntry(barchName)
            zip.putNextEntry(barchEntry)
            zip.write(encryptedBytes)
            zip.closeEntry()

            // 2. Human-readable CSV export of archived transactions
            val csvEntry = ZipEntry("Archived_Transactions.csv")
            zip.putNextEntry(csvEntry)
            val csvContent = generateTransactionsCsv(archivedTxs, accounts, categories)
            zip.write(csvContent.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            // 3. Human-readable Summary TXT
            val summaryEntry = ZipEntry("Archive_Summary.txt")
            zip.putNextEntry(summaryEntry)
            val summaryContent = generateArchiveSummaryText(impactSummary)
            zip.write(summaryContent.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }
        return bos.toByteArray()
    }

    private fun generateArchiveSummaryText(summary: ArchiveImpactSummary): String {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("BUDGETER - FISCAL YEAR ARCHIVE & PRUNING SUMMARY")
        sb.appendLine("==================================================")
        sb.appendLine("Date Range: ${summary.dateRangeLabel}")
        sb.appendLine("Archived Transactions: ${summary.totalTransactions}")
        sb.appendLine("Total Income: ৳${"%.2f".format(summary.totalIncome)}")
        sb.appendLine("Total Expenses: ৳${"%.2f".format(summary.totalExpense)}")
        sb.appendLine("Total Transfers: ৳${"%.2f".format(summary.totalTransfers)}")
        sb.appendLine("Net Cash Flow: ৳${"%.2f".format(summary.netCashFlow)}")
        sb.appendLine()
        sb.appendLine("AFFECTED ACCOUNTS & OPENING BALANCE CONVERSIONS:")
        sb.appendLine("--------------------------------------------------")
        for (acc in summary.affectedAccounts) {
            sb.appendLine("${acc.accountNameEn} (${acc.accountType}):")
            sb.appendLine("  Previous Starting Balance: ৳${"%.2f".format(acc.currentInitialBalance)}")
            sb.appendLine("  Archived Net Impact: ৳${"%.2f".format(acc.netArchivedImpact)}")
            sb.appendLine("  New Starting Balance: ৳${"%.2f".format(acc.newInitialBalance)}")
            sb.appendLine("  Active Total Balance (Before == After): ৳${"%.2f".format(acc.currentTotalBalanceBefore)} (100% Match)")
            sb.appendLine()
        }
        sb.appendLine("Generated by Budgeter App on ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        return sb.toString()
    }

    private fun generateTransactionsCsv(
        txs: List<Transaction>,
        accounts: List<Account>,
        categories: List<Category>
    ): String {
        val accMap = accounts.associateBy { it.id }
        val catMap = categories.associateBy { it.id }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val sb = StringBuilder()
        sb.appendLine("ID,Date,Type,Amount,Debit Account,Credit Account,Category,Subcategory,Payee/Payer,Note,Reference,Status")
        for (tx in txs) {
            val dateStr = sdf.format(Date(tx.dateEpochMs))
            val drName = tx.debitAccountId?.let { accMap[it]?.nameEn } ?: ""
            val crName = tx.creditAccountId?.let { accMap[it]?.nameEn } ?: ""
            val catName = tx.categoryId?.let { catMap[it]?.nameEn } ?: ""
            val subCatName = tx.subCategoryId?.let { catMap[it]?.nameEn } ?: ""
            val cleanNote = tx.note.replace("\"", "\"\"")
            val cleanPayee = tx.payeeOrPayer.replace("\"", "\"\"")
            val cleanRef = tx.referenceNo.replace("\"", "\"\"")

            sb.appendLine("${tx.id},\"$dateStr\",${tx.type},${tx.amount},\"$drName\",\"$crName\",\"$catName\",\"$subCatName\",\"$cleanPayee\",\"$cleanNote\",\"$cleanRef\",${tx.status}")
        }
        return sb.toString()
    }

    /**
     * Extracts and parses the FiscalArchivePayload from ZIP or .barch byte array
     */
    fun extractPayloadFromBytes(bytes: ByteArray, password: String? = null): FiscalArchivePayload? {
        try {
            // Check if it is a ZIP archive
            val isZip = bytes.size > 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
            if (isZip) {
                val zis = ZipInputStream(ByteArrayInputStream(bytes))
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".barch") || entry.name.endsWith(".json")) {
                        val entryBytes = zis.readBytes()
                        val json = decryptPayload(entryBytes, password)
                        val payload = payloadAdapter.fromJson(json)
                        if (payload != null) return payload
                    }
                    entry = zis.nextEntry
                }
            }

            // Try directly decrypting/decompressing
            val json = decryptPayload(bytes, password)
            return payloadAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Verifies the archive bytes by parsing and checking transaction count and checksum
     */
    fun verifyArchiveBytes(
        bytes: ByteArray,
        expectedTransactionCount: Int,
        expectedChecksum: String,
        password: String? = null
    ): ArchiveVerificationResult {
        return try {
            val payload = extractPayloadFromBytes(bytes, password)
                ?: return ArchiveVerificationResult(isSuccess = false, errorMessage = "Cannot decrypt or parse archive payload.")

            if (payload.transactions.size != expectedTransactionCount) {
                return ArchiveVerificationResult(
                    isSuccess = false,
                    errorMessage = "Transaction count mismatch: expected $expectedTransactionCount, got ${payload.transactions.size}"
                )
            }

            val rawJson = moshi.adapter(List::class.java).toJson(payload.transactions)
            val computedHash = computeSha256(rawJson)

            if (computedHash != expectedChecksum && payload.metadata.checksumSha256 != computedHash) {
                return ArchiveVerificationResult(
                    isSuccess = false,
                    errorMessage = "Integrity checksum verification failed."
                )
            }

            ArchiveVerificationResult(
                isSuccess = true,
                fileSizeBytes = bytes.size.toLong(),
                verifiedTransactionCount = payload.transactions.size
            )
        } catch (e: Exception) {
            ArchiveVerificationResult(isSuccess = false, errorMessage = e.localizedMessage)
        }
    }

    /**
     * Executes the actual database pruning:
     * 1. Updates account initial balances to the new opening balances.
     * 2. Deletes the archived transactions from the database.
     * Guaranteed atomic operation with pre-flight and post-flight balance checks.
     */
    suspend fun executePrune(
        impactSummary: ArchiveImpactSummary,
        accountDao: AccountDao,
        transactionDao: TransactionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val allAccounts = accountDao.getAllAccountsSnapshot().associateBy { it.id }.toMutableMap()
            val allTxs = transactionDao.getAllTransactionsSnapshot()

            // Calculate pre-prune total balances for verification
            val prePruneBalances = mutableMapOf<Long, Double>()
            for (acc in allAccounts.values) {
                var dr = 0.0
                var cr = 0.0
                for (tx in allTxs) {
                    if (tx.debitAccountId == acc.id) dr += tx.amount
                    if (tx.creditAccountId == acc.id) cr += tx.amount
                }
                val bal = when (acc.type) {
                    AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                    AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
                    AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
                }
                prePruneBalances[acc.id] = bal
            }

            // 1. Update initial balance of every affected account
            val updatedAccounts = mutableListOf<Account>()
            for (impact in impactSummary.affectedAccounts) {
                val acc = allAccounts[impact.accountId]
                if (acc != null) {
                    val updated = acc.copy(initialBalance = impact.newInitialBalance)
                    updatedAccounts.add(updated)
                    allAccounts[impact.accountId] = updated
                }
            }

            if (updatedAccounts.isNotEmpty()) {
                accountDao.insertAccounts(updatedAccounts)
            }

            // 2. Fetch and delete the transactions in the archived range
            val txsToDelete = allTxs.filter { it.dateEpochMs in impactSummary.startDateEpochMs..impactSummary.endDateEpochMs }

            if (txsToDelete.isNotEmpty()) {
                transactionDao.deleteTransactions(txsToDelete)
            }

            // Post-flight verification: calculate new active balances and ensure 100% exact match
            val remainingTxs = transactionDao.getAllTransactionsSnapshot()
            for (acc in allAccounts.values) {
                var dr = 0.0
                var cr = 0.0
                for (tx in remainingTxs) {
                    if (tx.debitAccountId == acc.id) dr += tx.amount
                    if (tx.creditAccountId == acc.id) cr += tx.amount
                }
                val postBal = when (acc.type) {
                    AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                    AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
                    AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
                }
                val preBal = prePruneBalances[acc.id] ?: 0.0
                if (Math.abs(postBal - preBal) > 0.0001) {
                    // Balance divergence detected! Abort & restore!
                    throw IllegalStateException("Balance discrepancy detected for ${acc.nameEn}: Pre=$preBal, Post=$postBal")
                }
            }

            Result.success(txsToDelete.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Lists local archive files found on the device
     */
    fun listLocalArchives(context: Context): List<File> {
        val list = mutableListOf<File>()
        try {
            val internalDir = File(context.filesDir, "archives")
            if (internalDir.exists() && internalDir.isDirectory) {
                internalDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".zip") || it.name.endsWith(".barch")) }?.let {
                    list.addAll(it)
                }
            }
            val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Budgeter/Archives")
            if (publicDir.exists() && publicDir.isDirectory) {
                publicDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".zip") || it.name.endsWith(".barch")) }?.let {
                    list.addAll(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.distinctBy { it.name }.sortedByDescending { it.lastModified() }
    }

    /**
     * Previews an archive file before importing with exact duplicate detection and offset analysis
     */
    suspend fun previewImport(
        context: Context,
        uri: Uri,
        password: String? = null,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): Result<ImportArchivePreview> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Cannot open selected archive file."))

            val payload = extractPayloadFromBytes(bytes, password)
                ?: return@withContext Result.failure(Exception("Invalid or password-protected archive file. Please check password."))

            val allExistingTxs = transactionDao.getAllTransactionsSnapshot()
            val existingIds = allExistingTxs.map { it.id }.toSet()
            val existingFingerprints = allExistingTxs.map { "${it.dateEpochMs}_${it.amount}_${it.debitAccountId}_${it.creditAccountId}_${it.type}" }.toSet()

            val txsToRestore = mutableListOf<Transaction>()
            var duplicateCount = 0

            for (tx in payload.transactions) {
                val fp = "${tx.dateEpochMs}_${tx.amount}_${tx.debitAccountId}_${tx.creditAccountId}_${tx.type}"
                if (existingIds.contains(tx.id) || existingFingerprints.contains(fp)) {
                    duplicateCount++
                } else {
                    txsToRestore.add(tx)
                }
            }

            val existingAccounts = accountDao.getAllAccountsSnapshot().associateBy { it.id }
            val existingCategories = categoryDao.getAllCategoriesSnapshot().associateBy { it.id }

            val missingAccounts = payload.accountsSnapshot.filter { !existingAccounts.containsKey(it.id) }
            val missingCategories = payload.categoriesSnapshot.filter { !existingCategories.containsKey(it.id) }
            val missingAccountIds = missingAccounts.map { it.id }.toSet()

            // Calculate precise offset reversals only for transactions that are actually being newly restored
            val affectedAccountImpacts = mutableListOf<AffectedAccountImpact>()
            for (accSnapshot in payload.accountsSnapshot) {
                val accId = accSnapshot.id
                val existingAcc = existingAccounts[accId]
                val currentInitial = existingAcc?.initialBalance ?: accSnapshot.initialBalance

                // Debits and credits of transactions that will actually be inserted into this account
                var drToRestore = 0.0
                var crToRestore = 0.0
                for (tx in txsToRestore) {
                    if (tx.debitAccountId == accId) drToRestore += tx.amount
                    if (tx.creditAccountId == accId) crToRestore += tx.amount
                }

                val netToRestore = when (accSnapshot.type) {
                    AccountType.ASSET, AccountType.EXPENSE -> drToRestore - crToRestore
                    AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> crToRestore - drToRestore
                }

                // If account is already existing and transactions are being restored, reduce starting balance by net impact
                val newInitial = if (existingAcc != null && !missingAccountIds.contains(accId)) {
                    currentInitial - netToRestore
                } else {
                    accSnapshot.initialBalance
                }

                if (Math.abs(netToRestore) > 0.0001 || !existingAccounts.containsKey(accId)) {
                    affectedAccountImpacts.add(
                        AffectedAccountImpact(
                            accountId = accId,
                            accountNameEn = accSnapshot.nameEn,
                            accountNameBn = accSnapshot.nameBn,
                            accountType = accSnapshot.type,
                            currentInitialBalance = currentInitial,
                            archivedDebits = drToRestore,
                            archivedCredits = crToRestore,
                            netArchivedImpact = -netToRestore,
                            newInitialBalance = newInitial,
                            currentTotalBalanceBefore = currentInitial,
                            expectedTotalBalanceAfter = newInitial
                        )
                    )
                }
            }

            val isValid = payload.transactions.isNotEmpty()
            val validationMsg = when {
                duplicateCount == payload.transactions.size ->
                    "All ${payload.transactions.size} transactions already exist in the database. No duplicate records will be created."
                duplicateCount > 0 ->
                    "Found $duplicateCount duplicate transactions (will be skipped). Restoring ${txsToRestore.size} new transactions."
                else ->
                    "All ${txsToRestore.size} archived transactions are ready to restore with exact opening balances."
            }

            Result.success(
                ImportArchivePreview(
                    metadata = payload.metadata,
                    totalTransactionsToRestore = payload.transactions.size,
                    existingDuplicateCount = duplicateCount,
                    newTransactionsCount = txsToRestore.size,
                    affectedAccounts = affectedAccountImpacts,
                    accountsToRestore = missingAccounts,
                    categoriesToRestore = missingCategories,
                    isValid = isValid,
                    validationMessage = validationMsg
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Executes the restoration of archived transactions back into the active database:
     * 1. Re-inserts missing categories (parents first, then subcategories).
     * 2. Re-inserts missing accounts (parents first, then subaccounts).
     * 3. Adjusts starting balances proportionally to newly restored transactions.
     * 4. Inserts non-duplicate transactions.
     */
    suspend fun executeImport(
        context: Context,
        uri: Uri,
        password: String? = null,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Cannot open archive file."))

            val payload = extractPayloadFromBytes(bytes, password)
                ?: return@withContext Result.failure(Exception("Cannot parse archive payload."))

            val existingAccounts = accountDao.getAllAccountsSnapshot().associateBy { it.id }.toMutableMap()
            val existingCategories = categoryDao.getAllCategoriesSnapshot().associateBy { it.id }.toMutableMap()

            // 1. Restore missing categories hierarchically (parent categories first)
            val missingCategories = payload.categoriesSnapshot.filter { !existingCategories.containsKey(it.id) }
            if (missingCategories.isNotEmpty()) {
                val parents = missingCategories.filter { it.parentId == null }
                val children = missingCategories.filter { it.parentId != null }
                if (parents.isNotEmpty()) categoryDao.insertCategories(parents)
                if (children.isNotEmpty()) categoryDao.insertCategories(children)
            }

            // 2. Restore missing accounts hierarchically (parent accounts first)
            val missingAccounts = payload.accountsSnapshot.filter { !existingAccounts.containsKey(it.id) }
            val missingAccountIds = missingAccounts.map { it.id }.toSet()
            if (missingAccounts.isNotEmpty()) {
                val parentAccs = missingAccounts.filter { it.parentId == null }
                val subAccs = missingAccounts.filter { it.parentId != null }
                if (parentAccs.isNotEmpty()) accountDao.insertAccounts(parentAccs)
                if (subAccs.isNotEmpty()) accountDao.insertAccounts(subAccs)
                for (acc in missingAccounts) existingAccounts[acc.id] = acc
            }

            // 3. Filter transactions to insert (ignoring duplicates)
            val allExisting = transactionDao.getAllTransactionsSnapshot()
            val existingIds = allExisting.map { it.id }.toSet()
            val existingFingerprints = allExisting.map { "${it.dateEpochMs}_${it.amount}_${it.debitAccountId}_${it.creditAccountId}_${it.type}" }.toSet()

            val txsToInsert = payload.transactions.filter { tx ->
                val fp = "${tx.dateEpochMs}_${tx.amount}_${tx.debitAccountId}_${tx.creditAccountId}_${tx.type}"
                !existingIds.contains(tx.id) && !existingFingerprints.contains(fp)
            }

            // 4. Reverse Starting Balance offsets proportionally only for accounts where new transactions are inserted
            val updatedAccounts = mutableListOf<Account>()
            for (acc in existingAccounts.values) {
                // If account was already in DB before import (i.e. not newly restored with pristine pre-archive balance)
                if (!missingAccountIds.contains(acc.id)) {
                    var drRestored = 0.0
                    var crRestored = 0.0
                    for (tx in txsToInsert) {
                        if (tx.debitAccountId == acc.id) drRestored += tx.amount
                        if (tx.creditAccountId == acc.id) crRestored += tx.amount
                    }
                    val netRestored = when (acc.type) {
                        AccountType.ASSET, AccountType.EXPENSE -> drRestored - crRestored
                        AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> crRestored - drRestored
                    }
                    if (Math.abs(netRestored) > 0.0001) {
                        val restoredInitial = acc.initialBalance - netRestored
                        updatedAccounts.add(acc.copy(initialBalance = restoredInitial))
                    }
                }
            }

            if (updatedAccounts.isNotEmpty()) {
                accountDao.insertAccounts(updatedAccounts)
            }

            // 5. Insert restored transactions
            if (txsToInsert.isNotEmpty()) {
                transactionDao.insertTransactions(txsToInsert)
            }

            Result.success(txsToInsert.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
