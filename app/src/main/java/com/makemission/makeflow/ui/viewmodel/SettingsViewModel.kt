package com.makemission.makeflow.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.core.haptics.HapticStrength
import com.makemission.makeflow.data.repository.CorrectionRepository
import com.makemission.makeflow.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import com.makemission.makeflow.data.backup.BackupManager
import com.makemission.makeflow.data.repository.ExchangeRateRepository
import com.makemission.makeflow.core.format.CurrencyFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.domain.CsvStatementBuilder
import com.makemission.makeflow.data.local.entity.TransactionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val financeRepository: FinanceRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val correctionRepository: CorrectionRepository? = null,
    private val backupManager: BackupManager? = null,
    private val exchangeRateRepository: ExchangeRateRepository? = null
) : ViewModel() {

    private val _isExportingCsv = MutableStateFlow(false)
    val isExportingCsv = _isExportingCsv.asStateFlow()

    private val _isExportingDiagnostics = MutableStateFlow(false)
    val isExportingDiagnostics = _isExportingDiagnostics.asStateFlow()

    val selectedCurrency: StateFlow<String> = userPreferencesDataStore.selectedCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "KES")

    val hapticLevel: StateFlow<HapticStrength> = userPreferencesDataStore.hapticLevel
        .map(HapticStrength::fromStorage)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HapticStrength.STANDARD)

    val hapticsEnabled: StateFlow<Boolean> = hapticLevel
        .map { it != HapticStrength.OFF }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appLockEnabled: StateFlow<Boolean> = userPreferencesDataStore.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val smartCategorizationEnabled: StateFlow<Boolean> = userPreferencesDataStore.smartCategorizationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val logMpesaCosts: StateFlow<Boolean> = userPreferencesDataStore.logMpesaCosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val mpesaAutoImportEnabled: StateFlow<Boolean> = userPreferencesDataStore.mpesaAutoImportEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val mpesaBackgroundLoggingEnabled: StateFlow<Boolean> = userPreferencesDataStore.mpesaBackgroundLoggingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = userPreferencesDataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val amoledMode: StateFlow<Boolean> = userPreferencesDataStore.amoledMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPromptedNotificationPermission: StateFlow<Boolean> = userPreferencesDataStore.hasPromptedNotificationPermission
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val financeReviewRemindersEnabled: StateFlow<Boolean> = userPreferencesDataStore.financeReviewRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currencySymbol: StateFlow<String> = selectedCurrency.map { code ->
        CurrencyFormatter.symbolFor(code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "KES")

    fun saveCurrency(currency: String) {
        viewModelScope.launch {
            userPreferencesDataStore.saveCurrency(currency)
        }
    }

    fun saveHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveHapticsEnabled(enabled)
        }
    }

    fun saveHapticLevel(level: HapticStrength) {
        viewModelScope.launch {
            userPreferencesDataStore.saveHapticLevel(level.storageValue)
        }
    }

    fun saveAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveAppLockEnabled(enabled)
        }
    }

    fun saveSmartCategorizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveSmartCategorizationEnabled(enabled)
        }
    }

    fun saveLogMpesaCosts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveLogMpesaCosts(enabled)
            AppLogger.event(
                "Settings",
                "mpesa_fee_logging_changed",
                AppLogger.newOperationId(),
                mapOf("enabled" to enabled)
            )
        }
    }

    fun saveMpesaAutoImportEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveMpesaAutoImportEnabled(enabled)
            AppLogger.event(
                "Settings",
                "message_detection_changed",
                AppLogger.newOperationId(),
                mapOf("enabled" to enabled)
            )
        }
    }

    fun saveMpesaBackgroundLoggingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveMpesaBackgroundLoggingEnabled(enabled)
            AppLogger.event(
                "Settings",
                "background_message_logging_changed",
                AppLogger.newOperationId(),
                mapOf("enabled" to enabled)
            )
        }
    }

    fun saveFinanceReviewRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveFinanceReviewRemindersEnabled(enabled)
        }
    }

    fun saveThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesDataStore.saveThemeMode(mode)
        }
    }

    fun saveAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.saveAmoledMode(enabled)
        }
    }

    fun exportDiagnosticLog(
        context: Context,
        onError: () -> Unit = {},
        onComplete: (String) -> Unit = {}
    ) {
        if (_isExportingDiagnostics.value) return
        _isExportingDiagnostics.value = true
        viewModelScope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    AppLogger.buildDiagnosticReport(context.applicationContext)
                }
                onComplete(report)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AppLogger.error("DiagnosticExport", "Could not build the diagnostic report", error)
                onError()
            } finally {
                _isExportingDiagnostics.value = false
            }
        }
    }

    fun exportCsv(
        context: Context,
        startMillis: Long = 0L,
        endMillis: Long = Long.MAX_VALUE,
        onError: () -> Unit = {},
        onComplete: (Uri, String) -> Unit = { _, _ -> }
    ) {
        if (_isExportingCsv.value) return
        _isExportingCsv.value = true
        viewModelScope.launch {
            try {
                val exported = withContext(Dispatchers.IO) {
                    val fileName = CsvStatementBuilder.fileName(startMillis, endMillis)
                    val transactions = statementTransactions(startMillis, endMillis)
                    val csv = CsvStatementBuilder.build(
                        transactions = transactions,
                        accounts = financeRepository.getAllAccounts().first().associateBy { it.id },
                        categories = financeRepository.getAllCategories().first().associateBy { it.id },
                        defaultCurrency = userPreferencesDataStore.selectedCurrency.first()
                    )
                    val uri = saveStatement(context, fileName, "text/csv") { output ->
                        output.writer(Charsets.UTF_8).use { writer ->
                            writer.write("\uFEFF")
                            writer.write(csv)
                        }
                    }
                    uri to fileName
                }
                onComplete(exported.first, exported.second)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AppLogger.error("CsvExport", "Could not create the CSV statement", error)
                onError()
            } finally {
                _isExportingCsv.value = false
            }
        }
    }

    fun exportPdf(
        context: Context,
        startMillis: Long,
        endMillis: Long,
        onError: () -> Unit = {},
        onComplete: (Uri, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val exported = withContext(Dispatchers.IO) {
                    val transactions = statementTransactions(startMillis, endMillis)
                    val accounts = financeRepository.getAllAccounts().first()
                    val categories = financeRepository.getAllCategories().first()
                    val exchangeRates = exchangeRateRepository?.getAllRatesOnce()
                        ?.associateBy { it.currencyCode }
                        .orEmpty()
                    val displayCurrency = userPreferencesDataStore.selectedCurrency.first()
                    val isAllTime = startMillis <= 0L && endMillis == Long.MAX_VALUE
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    val dateRangeLabel = if (isAllTime) {
                        "All time through ${dateFormat.format(Date())}"
                    } else {
                        val inclusiveEnd = if (endMillis > startMillis) endMillis - 1 else endMillis
                        "${dateFormat.format(Date(startMillis))} - ${dateFormat.format(Date(inclusiveEnd))}"
                    }
                    val transactionDateFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())
                    val exportData = PdfExportDataBuilder.build(
                        transactions = transactions,
                        accountMap = accounts.associateBy { it.id },
                        categoryMap = categories.associateBy { it.id },
                        exchangeRates = exchangeRates,
                        displayCurrency = displayCurrency
                    ) { timestamp -> transactionDateFormat.format(Date(timestamp)) }
                    val fileName = PdfStatementFileName.forRange(startMillis, endMillis)
                    val document = PdfStatementRenderer.render(
                        rows = exportData.rows,
                        summary = exportData.summary,
                        currencyCode = displayCurrency,
                        dateRangeLabel = dateRangeLabel
                    )
                    try {
                        saveStatement(context, fileName, "application/pdf", document::writeTo) to fileName
                    } finally {
                        document.close()
                    }
                }
                onComplete(exported.first, exported.second)
            } catch (error: Exception) {
                AppLogger.error("PdfExport", "Could not create the statement PDF", error)
                onError()
            }
        }
    }

    private suspend fun statementTransactions(startMillis: Long, endExclusiveMillis: Long): List<TransactionEntity> {
        require(endExclusiveMillis > startMillis) { "Invalid statement date range" }
        return if (startMillis == 0L && endExclusiveMillis == Long.MAX_VALUE) {
            financeRepository.getAllTransactions().first()
        } else {
            financeRepository.getTransactionsInRange(startMillis, endExclusiveMillis - 1).first()
        }
    }

    private fun saveStatement(
        context: Context,
        fileName: String,
        mimeType: String,
        write: (OutputStream) -> Unit
    ): Uri {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MakeFlow")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Unable to create a statement file")
            try {
                val output = resolver.openOutputStream(uri)
                    ?: throw IOException("Unable to open the statement file")
                output.use(write)
                val publishedValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(uri, publishedValues, null, null)
                return uri
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
        }

        val documentsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, Environment.DIRECTORY_DOCUMENTS)
        val outputDirectory = File(documentsDirectory, "MakeFlow")
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw IOException("Unable to create the statement folder")
        }
        val outputFile = File(outputDirectory, fileName)
        try {
            outputFile.outputStream().use(write)
        } catch (error: Exception) {
            outputFile.delete()
            throw error
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )
    }

    suspend fun exportBackup(): String? {
        val mgr = backupManager ?: return null
        return mgr.serializeBackup()
    }

    suspend fun importBackup(json: String): String? {
        val mgr = backupManager ?: return "Backup manager not available"
        return mgr.restoreBackup(json)
    }

    fun clearAllData() {
        viewModelScope.launch {
            financeRepository.clearAllTransactions()
            financeRepository.clearAllBudgets()
            financeRepository.clearAllAccounts()
            financeRepository.clearAllCategories()
            userPreferencesDataStore.clearProfile()
        }
    }

    fun resetPersonalization() {
        viewModelScope.launch {
            correctionRepository?.resetCorrections()
            correctionRepository?.clearCorrectionMemory()
        }
    }
}

class SettingsViewModelFactory(
    private val financeRepository: FinanceRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val correctionRepository: CorrectionRepository? = null,
    private val backupManager: BackupManager? = null,
    private val exchangeRateRepository: ExchangeRateRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(financeRepository, userPreferencesDataStore, correctionRepository, backupManager, exchangeRateRepository) as T
    }
}
