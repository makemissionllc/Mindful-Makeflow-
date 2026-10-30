package com.makemission.makeflow.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import com.makemission.makeflow.core.layout.AdaptiveAlertSheet
import com.makemission.makeflow.core.layout.AdaptiveSheetDialog
import com.makemission.makeflow.core.layout.AdaptiveSheetHeader
import com.makemission.makeflow.core.layout.AdaptiveSheetPrimaryButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.makemission.makeflow.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makemission.makeflow.core.layout.DeviceLayoutMode
import com.makemission.makeflow.core.layout.LocalDeviceLayoutMode
import com.makemission.makeflow.ui.components.AdaptiveConfig
import com.makemission.makeflow.ui.components.FloatingPillNav
import com.makemission.makeflow.ui.components.PillNavItem
import com.makemission.makeflow.ui.components.LocalAdaptiveConfig
import com.makemission.makeflow.ui.components.animateAppContentSize
import com.makemission.makeflow.ui.components.rememberAdaptiveConfig
import androidx.lifecycle.viewmodel.compose.viewModel
import com.makemission.makeflow.core.device.LocalReducedMotion
import com.makemission.makeflow.core.haptics.HapticEvent
import com.makemission.makeflow.core.haptics.rememberHapticManager
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.core.format.CurrencyFormatter
import com.makemission.makeflow.ui.util.sharePdfStatement
import com.makemission.makeflow.core.ml.CategoryClassifier
import com.makemission.makeflow.core.ml.RetrainableCategoryClassifier
import com.makemission.makeflow.data.repository.CorrectionRepository
import com.makemission.makeflow.data.repository.CorrectionRepositoryImpl
import com.makemission.makeflow.domain.CorrectionRoute
import com.makemission.makeflow.domain.CorrectionRouter
import com.makemission.makeflow.domain.CategorySuggester
import com.makemission.makeflow.core.notifications.NotificationGate
import com.makemission.makeflow.core.update.GithubAppUpdateScheduler
import com.makemission.makeflow.BuildConfig
import com.makemission.makeflow.data.local.AppDatabase
import com.makemission.makeflow.domain.AnomalyDetector
import com.makemission.makeflow.domain.PastTransactionForSuggestion
import com.makemission.makeflow.widget.WidgetUpdateHelper
import com.makemission.makeflow.widget.MakeFlowWidgetReceiver
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.data.local.entity.TransactionEntity
import com.makemission.makeflow.data.local.entity.TransactionTemplateEntity
import com.makemission.makeflow.data.local.userPrefDataStore
import com.makemission.makeflow.data.repository.FinanceRepositoryImpl
import com.makemission.makeflow.data.backup.BackupManager
import com.makemission.makeflow.data.local.entity.ExchangeRateEntity
import com.makemission.makeflow.data.repository.ExchangeRateRepositoryImpl
import com.makemission.makeflow.data.repository.ExchangeRateSyncer
import com.makemission.makeflow.data.repository.TransactionTemplateRepositoryImpl
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.domain.MpesaBalanceCheck
import com.makemission.makeflow.domain.MpesaBalanceReconciler
import com.makemission.makeflow.ui.screens.AccountsScreen
import com.makemission.makeflow.ui.screens.AddEditAccountBottomSheet
import com.makemission.makeflow.ui.screens.AddEditRecurringTransactionBottomSheet
import com.makemission.makeflow.ui.screens.AddTransactionScreen
import com.makemission.makeflow.ui.screens.BudgetScreen
import com.makemission.makeflow.ui.screens.CategoryManagementScreen
import com.makemission.makeflow.ui.screens.ExchangeRatesSheet
import com.makemission.makeflow.ui.screens.HistoryScreen
import com.makemission.makeflow.ui.screens.HomeDashboard
import com.makemission.makeflow.ui.screens.InsightsScreen
import com.makemission.makeflow.ui.screens.ManageTemplatesSheet
import com.makemission.makeflow.ui.screens.RecurringTransactionsScreen
import com.makemission.makeflow.ui.screens.SavingsGoalsScreen
import com.makemission.makeflow.ui.screens.ChangelogScreen
import com.makemission.makeflow.ui.screens.CommunityPermissionSetupSheet
import com.makemission.makeflow.ui.screens.PrivacyPolicyScreen
import com.makemission.makeflow.ui.screens.SettingsScreen
import com.makemission.makeflow.data.changelog.CURRENT_CHANGELOG_VERSION
import com.makemission.makeflow.ui.theme.LocalAppDimens
import com.makemission.makeflow.ui.theme.LocalMakeFlowColors
import com.makemission.makeflow.ui.theme.OverlayTransition
import com.makemission.makeflow.ui.viewmodel.AccountsViewModel
import com.makemission.makeflow.ui.viewmodel.AccountsViewModelFactory
import com.makemission.makeflow.ui.viewmodel.BudgetViewModel
import com.makemission.makeflow.ui.viewmodel.BudgetViewModelFactory
import com.makemission.makeflow.ui.viewmodel.HistoryViewModel
import com.makemission.makeflow.ui.viewmodel.HistoryViewModelFactory
import com.makemission.makeflow.data.local.entity.AccountEntity
import com.makemission.makeflow.ui.viewmodel.HomeDashboardViewModel
import com.makemission.makeflow.ui.viewmodel.HomeDashboardViewModelFactory
import com.makemission.makeflow.ui.viewmodel.InsightsViewModel
import com.makemission.makeflow.ui.viewmodel.InsightsViewModelFactory
import com.makemission.makeflow.ui.viewmodel.RecurringTransactionsViewModel
import com.makemission.makeflow.ui.viewmodel.RecurringTransactionsViewModelFactory
import com.makemission.makeflow.ui.viewmodel.SavingsGoalsViewModel
import com.makemission.makeflow.ui.viewmodel.SavingsGoalsViewModelFactory
import com.makemission.makeflow.ui.viewmodel.SettingsViewModel
import com.makemission.makeflow.ui.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MakeFlowTab {
    HOME, INSIGHTS, BUDGET, GOALS, HISTORY
}

private val primaryTabs = listOf(
    MakeFlowTab.HOME,
    MakeFlowTab.INSIGHTS,
    MakeFlowTab.BUDGET,
    MakeFlowTab.GOALS
)

@Composable
fun MainScreen(
    username: String,
    onLogout: () -> Unit,
    notificationGate: NotificationGate? = null,
    initialTransactionType: String? = null,
    sharedMpesaData: MpesaMessageParser.MpesaParsedMessage? = null,
    shareParseError: String? = null,
    onShareParseErrorShown: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    var activeTab by remember { mutableStateOf(MakeFlowTab.HOME) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        FinanceRepositoryImpl(
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            taskDao = db.taskDao(),
            accountDao = db.accountDao(),
            budgetDao = db.budgetDao(),
            recurringPatternDao = db.recurringPatternDao(),
            recurringTransactionDao = db.recurringTransactionDao()
        )
    }
    val exchangeRateRepository = remember { ExchangeRateRepositoryImpl(db.exchangeRateDao()) }
    val backupManager = remember {
        BackupManager(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            budgetDao = db.budgetDao(),
            taskDao = db.taskDao(),
            recurringPatternDao = db.recurringPatternDao(),
            recurringTransactionDao = db.recurringTransactionDao(),
            transactionTemplateDao = db.transactionTemplateDao(),
            exchangeRateDao = db.exchangeRateDao(),
            database = db
        )
    }
    val templateRepository = remember { TransactionTemplateRepositoryImpl(db.transactionTemplateDao()) }
    val templates by templateRepository.getAllTemplates().collectAsState(initial = emptyList())
    val goalRepository = remember { com.makemission.makeflow.data.repository.GoalRepositoryImpl(db.goalDao()) }
    val correctionRepository = remember { CorrectionRepositoryImpl(db.correctionDao(), db.correctionMemoryDao()) as CorrectionRepository }
    val userPrefDataStore = remember { UserPreferencesDataStore(context.userPrefDataStore) }
    val shouldOfferHomeWidget by userPrefDataStore.shouldOfferHomeWidget.collectAsState(initial = false)
    val communityPermissionSetupCompleted by userPrefDataStore.communityPermissionSetupCompleted
        .map { it as Boolean? }
        .collectAsState(initial = null)
    val homeViewModel: HomeDashboardViewModel = viewModel(
        factory = HomeDashboardViewModelFactory(repository, userPrefDataStore, db.cardInteractionDao(), exchangeRateRepository)
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository, userPrefDataStore)
    )
    val insightsViewModel: InsightsViewModel = viewModel(
        factory = InsightsViewModelFactory(repository, userPrefDataStore, exchangeRateRepository)
    )
    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(repository, userPrefDataStore)
    )
    val accountsViewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModelFactory(repository, userPrefDataStore, exchangeRateRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository, userPrefDataStore, correctionRepository, backupManager, exchangeRateRepository)
    )
    val recurringTransactionsViewModel: RecurringTransactionsViewModel = viewModel(
        factory = RecurringTransactionsViewModelFactory(repository, userPrefDataStore)
    )
    val savingsGoalsViewModel: SavingsGoalsViewModel = viewModel(
        factory = SavingsGoalsViewModelFactory(goalRepository, repository, userPrefDataStore)
    )
    val hapticLevel by settingsViewModel.hapticLevel.collectAsState()
    val smartCategorizationEnabled by settingsViewModel.smartCategorizationEnabled.collectAsState()
    val logMpesaCosts by settingsViewModel.logMpesaCosts.collectAsState()

    val hapticManager = rememberHapticManager(hapticLevel)

    var categoryClassifier by remember { mutableStateOf<CategoryClassifier?>(null) }
    var labelSet by remember { mutableStateOf<List<String>>(emptyList()) }
    var correctionMemory by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val classifier = withContext(Dispatchers.IO) {
            val c = RetrainableCategoryClassifier.create(context)
            val labels = try { RetrainableCategoryClassifier.loadLabelSet(context) } catch (_: Exception) { emptyList<String>() }
            c?.restoreCheckpoint()
            c to labels
        }
        categoryClassifier = classifier?.first
        labelSet = classifier?.second ?: emptyList()
        correctionMemory = withContext(Dispatchers.IO) { correctionRepository.getAllCorrectionMemory() }
        withContext(Dispatchers.IO) {
            val clf = categoryClassifier as? RetrainableCategoryClassifier ?: return@withContext
            var trained = true
            while (trained) {
                trained = correctionRepository.trainBatchIfReady(clf)
            }
        }
    }

    val accounts by homeViewModel.accounts.collectAsState()
    val lastTransactionAccountId by userPrefDataStore.lastTransactionAccountId.collectAsState(initial = null)
    val defaultTransactionAccountId = lastTransactionAccountId?.takeIf { savedId ->
        accounts.any { it.id == savedId }
    }
    val categories by homeViewModel.categories.collectAsState()
    val currencySymbol by homeViewModel.currencySymbol.collectAsState()
    val currencyCode by homeViewModel.selectedCurrency.collectAsState()
    val exportSelectedInsightsMonth = remember(context, insightsViewModel, settingsViewModel) {
        {
            val month = insightsViewModel.selectedMonth.value
            val zone = java.time.ZoneId.systemDefault()
            val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            settingsViewModel.exportPdf(
                context,
                start,
                end,
                onError = {
                    Toast.makeText(context, "Could not create the PDF statement", Toast.LENGTH_LONG).show()
                }
            ) { uri, fileName ->
                Toast.makeText(context, "Statement ready to share", Toast.LENGTH_SHORT).show()
                sharePdfStatement(context, uri, fileName)
            }
        }
    }

    var showAddTransaction by remember { mutableStateOf(false) }
    val budgetDockAddRequest = remember { mutableIntStateOf(0) }
    val goalsDockAddRequest = remember { mutableIntStateOf(0) }
    val shortcutPendingType = remember { mutableStateOf<String?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    LaunchedEffect(initialTransactionType) {
        if (initialTransactionType != null) {
            shortcutPendingType.value = initialTransactionType
            showAddTransaction = true
        }
    }

    // M-PESA import review handling
    var mpesaInitialAmount by remember { mutableStateOf("") }
    var mpesaInitialType by remember { mutableStateOf<String?>(null) }
    var mpesaInitialNote by remember { mutableStateOf("") }
    var mpesaInitialTimestamp by remember { mutableStateOf<Long?>(null) }
    var mpesaExternalReference by remember { mutableStateOf<String?>(null) }
    var mpesaDuplicateMessage by remember { mutableStateOf<String?>(null) }
    var mpesaShareErrorMessage by remember { mutableStateOf<String?>(null) }
    var mpesaCreatedAccountMessage by remember { mutableStateOf<String?>(null) }
    var mpesaInitialAccountId by remember { mutableStateOf<String?>(null) }
    var mpesaInitialTransferFromId by remember { mutableStateOf<String?>(null) }
    var mpesaInitialTransferToId by remember { mutableStateOf<String?>(null) }
    var mpesaTransactionCost by remember { mutableStateOf<Double?>(null) }
    var mpesaBalanceCheck by remember { mutableStateOf<MpesaBalanceCheck?>(null) }
    var mpesaAiAssisted by remember { mutableStateOf(false) }
    var pendingMpesaImport by remember { mutableStateOf<MpesaMessageParser.MpesaParsedMessage?>(null) }
    val mpesaImportRequestId = remember { mutableIntStateOf(0) }

    LaunchedEffect(sharedMpesaData) {
        sharedMpesaData?.let { data ->
            pendingMpesaImport = data
            mpesaImportRequestId.intValue++
        }
    }

    LaunchedEffect(mpesaImportRequestId.intValue) {
        val data = pendingMpesaImport ?: return@LaunchedEffect
        pendingMpesaImport = null
        mpesaAiAssisted = data.isAiAssisted
        if (data.code.isNotEmpty()) {
            val isDuplicate = repository.hasExternalReference(data.code)
            if (isDuplicate) {
                mpesaDuplicateMessage = "This M-PESA transaction has already been logged"
                return@LaunchedEffect
            }
        }
        val isFeeDuplicate = data.code.isNotEmpty() && logMpesaCosts &&
            repository.hasExternalReference("${data.code}-fee")

        val normalizedName = MpesaMessageParser.normalizeWhitespace(data.counterpartyName)
        val isZiidi = normalizedName.equals("ZIIDI", ignoreCase = true)

        val mpesaAccount = withContext(Dispatchers.IO) {
            db.accountDao().getAllAccountsOnce().firstOrNull { a ->
                a.name.contains("mpesa", ignoreCase = true)
            }
        }
        val mpesaAccountId = mpesaAccount?.id

        if (isZiidi) {
            val ziidiAccountId = withContext(Dispatchers.IO) {
                val existing = db.accountDao().getAllAccountsOnce()
                    .firstOrNull { a -> a.name.equals("Ziidi", ignoreCase = true) }
                if (existing != null) {
                    existing.id
                } else {
                    val newId = java.util.UUID.randomUUID().toString()
                    db.accountDao().insertAccount(
                        com.makemission.makeflow.data.local.entity.AccountEntity(
                            id = newId, name = "Ziidi", balance = 0.0,
                            initialBalance = 0.0, colorHex = "#26A69A"
                        )
                    )
                    mpesaCreatedAccountMessage = "Created 'Ziidi' account for M-PESA savings transfers"
                    newId
                }
            }
            mpesaInitialTransferFromId = mpesaAccountId
            mpesaInitialTransferToId = ziidiAccountId
            mpesaInitialType = "TRANSFER"
        } else {
            val type = when (data.type) {
                MpesaMessageParser.TransactionType.RECEIVED -> "INCOME"
                MpesaMessageParser.TransactionType.SENT,
                MpesaMessageParser.TransactionType.PAYBILL,
                MpesaMessageParser.TransactionType.WITHDRAW,
                MpesaMessageParser.TransactionType.AIRTIME -> "EXPENSE"
                else -> "EXPENSE"
            }
            mpesaInitialAccountId = mpesaAccountId
            mpesaInitialType = type
        }

        mpesaInitialAmount = if (data.amount == data.amount.toLong().toDouble())
            data.amount.toLong().toString() else data.amount.toString()
        mpesaInitialNote = data.counterpartyName ?: ""
        mpesaInitialTimestamp = try {
            val normalizedDate = data.date.replace(Regex("""/(\d{2})$""")) { "/20${it.groupValues[1]}" }
            val fmt = java.text.SimpleDateFormat("d/M/yyyy h:mm a", java.util.Locale.getDefault())
            fmt.parse("$normalizedDate ${data.time}")?.time
        } catch (_: Exception) { null }
        mpesaExternalReference = data.code
        mpesaTransactionCost = data.transactionCost.takeUnless { isFeeDuplicate }
        val accountBalanceBeforeMessage = mpesaAccount?.takeIf {
            (it.currencyCode ?: currencyCode).equals("KES", ignoreCase = true)
        }?.let { account ->
            mpesaInitialTimestamp?.let { timestamp ->
                withContext(Dispatchers.IO) {
                    MpesaBalanceReconciler.estimateBalanceBefore(
                        currentAccountBalance = account.balance,
                        accountId = account.id,
                        transactionTimestamp = timestamp,
                        laterTransactions = db.transactionDao().getTransactionsSince(timestamp)
                    )
                }
            }
        }
        mpesaBalanceCheck = accountBalanceBeforeMessage?.let { balanceBefore ->
            MpesaBalanceReconciler.compare(
                appBalanceBeforeTransaction = balanceBefore,
                type = data.type,
                amount = data.amount,
                transactionCost = data.transactionCost.takeIf { logMpesaCosts && !isFeeDuplicate },
                statementBalance = data.balance
            )
        }
        showAddTransaction = true
    }

    LaunchedEffect(shareParseError) {
        shareParseError?.let {
            mpesaShareErrorMessage = it
            onShareParseErrorShown()
            showAddTransaction = true
        }
    }

    if (mpesaDuplicateMessage != null) {
        AdaptiveAlertSheet(
            onDismissRequest = { mpesaDuplicateMessage = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Duplicate Transaction", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(mpesaDuplicateMessage ?: "", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = { mpesaDuplicateMessage = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (mpesaShareErrorMessage != null) {
        AdaptiveAlertSheet(
            onDismissRequest = { mpesaShareErrorMessage = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Couldn't Parse Message", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(mpesaShareErrorMessage ?: "", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = { mpesaShareErrorMessage = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (mpesaCreatedAccountMessage != null) {
        AdaptiveAlertSheet(
            onDismissRequest = { mpesaCreatedAccountMessage = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Account Created", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(mpesaCreatedAccountMessage ?: "", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = { mpesaCreatedAccountMessage = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    var showBudgetScreen by remember { mutableStateOf(false) }
    var showAccountsScreen by remember { mutableStateOf(false) }
    var showSavingsGoalsScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showRecurringTransactionsScreen by remember { mutableStateOf(false) }
    var showRecurringSetupFromInsights by remember { mutableStateOf(false) }
    var recurringSetupAmount by remember { mutableStateOf(0.0) }
    var showManageTemplates by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showExchangeRates by remember { mutableStateOf(false) }
    var showChangelogScreen by remember { mutableStateOf(false) }
    var showPrivacyPolicyScreen by remember { mutableStateOf(false) }
    var showHomeWidgetOffer by remember { mutableStateOf(false) }
    var showCommunityPermissionSetup by remember { mutableStateOf(false) }
    val exchangeRates by exchangeRateRepository.getAllRates().collectAsState(initial = emptyList())
    val exchangeRateSyncer = remember(exchangeRateRepository) { ExchangeRateSyncer(exchangeRateRepository) }
    var exchangeRatesRefreshing by remember { mutableStateOf(false) }
    var exchangeRatesRefreshError by remember { mutableStateOf<String?>(null) }
    val previousTab = remember { mutableStateOf(activeTab) }
    LaunchedEffect(activeTab) {
        if (previousTab.value != activeTab) {
            hapticManager.fire(HapticEvent.TAB_SWITCH)
            previousTab.value = activeTab
        }
    }

    LaunchedEffect(Unit) {
        val lastSeen = userPrefDataStore.lastSeenChangelogVersion.first()
        if (lastSeen != CURRENT_CHANGELOG_VERSION) {
            showChangelogScreen = true
        }
    }

    LaunchedEffect(shouldOfferHomeWidget, communityPermissionSetupCompleted, showChangelogScreen, showPrivacyPolicyScreen) {
        showHomeWidgetOffer = shouldOfferHomeWidget &&
            (!BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED || communityPermissionSetupCompleted == true) &&
            !showChangelogScreen && !showPrivacyPolicyScreen
    }

    LaunchedEffect(communityPermissionSetupCompleted, showChangelogScreen, showPrivacyPolicyScreen) {
        showCommunityPermissionSetup = BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED &&
            communityPermissionSetupCompleted == false &&
            !showChangelogScreen &&
            !showPrivacyPolicyScreen
    }

    val scope = rememberCoroutineScope()

    fun completeCommunityPermissionSetup() {
        showCommunityPermissionSetup = false
        scope.launch { userPrefDataStore.setCommunityPermissionSetupCompleted(true) }
    }

    fun refreshExchangeRates(force: Boolean) {
        if (exchangeRatesRefreshing) return
        exchangeRatesRefreshing = true
        exchangeRatesRefreshError = null
        scope.launch {
            try {
                exchangeRateSyncer.refresh(currencyCode, force)
                WidgetUpdateHelper.updateWidgets(context)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                exchangeRatesRefreshError = "Couldn't refresh exchange rates."
                AppLogger.error("ExchangeRates", "Automatic exchange-rate refresh failed", error)
            } finally {
                exchangeRatesRefreshing = false
            }
        }
    }

    LaunchedEffect(currencyCode) {
        refreshExchangeRates(force = false)
    }

    var backupJson by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreError by remember { mutableStateOf<String?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val json = settingsViewModel.exportBackup()
                    if (json != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray())
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.error("MainScreen", "Backup failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.use { it.reader().readText() } ?: ""
                    val validation = backupManager.validateBackup(json)
                    if (!validation.valid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Restore failed: ${validation.error}", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        showRestoreConfirm = true
                        backupJson = json
                    }
                } catch (e: Exception) {
                    AppLogger.error("MainScreen", "Failed to read backup", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to read backup: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun performBackup() {
        backupLauncher.launch("Mindful_Backup_${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())}.json")
    }

    fun pickRestoreFile() {
        restoreLauncher.launch(arrayOf("application/json", "*/*"))
    }

    val config = rememberAdaptiveConfig()
    val reducedMotion = LocalReducedMotion.current
    val slideEnter = if (reducedMotion) OverlayTransition.reducedEnterSlide else OverlayTransition.enterSlide
    val slideExit = if (reducedMotion) OverlayTransition.reducedExitSlide else OverlayTransition.exitSlide
    val fadeEnter = if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.enterFade
    val fadeExit = if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.exitFade
    val layoutMode = LocalDeviceLayoutMode.current
    val isNavRail = layoutMode != DeviceLayoutMode.COMPACT

    val maxWidthModifier = if (layoutMode == DeviceLayoutMode.MEDIUM && !config.isLandscape)
        Modifier.widthIn(max = config.contentMaxWidth) else Modifier

    CompositionLocalProvider(LocalAdaptiveConfig provides config) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isNavRail) {
            Row(Modifier.fillMaxSize()) {
                MakeFlowNavRail(
                    selectedTab = activeTab,
                    onTabSelected = { activeTab = it },
                    layoutMode = layoutMode
                )

                Box(Modifier.weight(1f).fillMaxHeight()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(maxWidthModifier)
                            .align(Alignment.Center)
                    ) {
                        TabContentAnimated(
                            activeTab = activeTab,
                            username = username,
                            reducedMotion = reducedMotion,
                            homeViewModel = homeViewModel,
                            insightsViewModel = insightsViewModel,
                            historyViewModel = historyViewModel,
                            budgetViewModel = budgetViewModel,
                            savingsGoalsViewModel = savingsGoalsViewModel,
                            budgetDockAddRequest = budgetDockAddRequest.intValue,
                            goalsDockAddRequest = goalsDockAddRequest.intValue,
                            accounts = accounts,
                            currencyCode = currencyCode,
                            currencySymbol = currencySymbol,
                            hapticManager = hapticManager,
                            notificationGate = notificationGate,
                            onOpenAccounts = { showAccountsScreen = true },
                            onOpenSavingsGoals = { activeTab = MakeFlowTab.GOALS },
                            onOpenSettings = { showSettingsScreen = true },
                            onOpenBudgets = { activeTab = MakeFlowTab.BUDGET },
                            onOpenRecurringTransactions = { showRecurringTransactionsScreen = true },
                            onOpenHistory = { activeTab = MakeFlowTab.HISTORY },
                            onAddTransaction = { type ->
                                shortcutPendingType.value = type
                                showAddTransaction = true
                            },
                            onNavigateToHome = { activeTab = MakeFlowTab.HOME },
                            onSetUpRecurringFromInsights = { amount ->
                                recurringSetupAmount = amount
                                showRecurringSetupFromInsights = true
                            },
                            onExportPdf = exportSelectedInsightsMonth,
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                showAddTransaction = true
                            },
                            onImportMpesaMessage = { data ->
                                pendingMpesaImport = data
                                mpesaImportRequestId.intValue++
                            }
                        )
            }

            Box(Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 24.dp)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !showAddTransaction,
                    enter = scaleIn(
                        animationSpec = if (reducedMotion) {
                            OverlayTransition.reducedEnter
                        } else {
                            OverlayTransition.fastEnter
                        }
                    ) + fadeIn(
                        if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.fastEnter
                    ),
                    exit = scaleOut(
                        animationSpec = if (reducedMotion) {
                            OverlayTransition.reducedExit
                        } else {
                            OverlayTransition.fastExit
                        }
                    ) + fadeOut(
                        if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.fastExit
                    ) + slideOutVertically(
                        animationSpec = if (reducedMotion) OverlayTransition.reducedExitSlide else OverlayTransition.exitSlide,
                        targetOffsetY = { -(it * OverlayTransition.ValueShiftFraction).toInt() }
                    )
                ) {
                    AddTransactionFloatingActionButton(
                        onClick = { hapticManager.fire(HapticEvent.NAV_SELECT); showAddTransaction = true },
                        fabSize = config.fabSize
                    )
                }
            }

                    OverlayPanels(
                        showBudgetScreen = showBudgetScreen,
                        onHideBudget = { showBudgetScreen = false },
                        showAccountsScreen = showAccountsScreen,
                        onHideAccounts = { showAccountsScreen = false },
                        showSettingsScreen = showSettingsScreen,
                        onHideSettings = { showSettingsScreen = false },
                        showRecurringTransactionsScreen = showRecurringTransactionsScreen,
                        onHideRecurringTransactions = { showRecurringTransactionsScreen = false },
                        budgetViewModel = budgetViewModel,
                        accountsViewModel = accountsViewModel,
                        settingsViewModel = settingsViewModel,
                        recurringTransactionsViewModel = recurringTransactionsViewModel,
                        hapticManager = hapticManager,
                        notificationGate = notificationGate,
                        reducedMotion = reducedMotion,
                        onOpenManageTemplates = { showManageTemplates = true },
                        onOpenSavingsGoals = { showSavingsGoalsScreen = true },
                        onOpenManageCategories = { showManageCategories = true },
                        onOpenExchangeRates = { showExchangeRates = true },
                        onBackup = { performBackup() },
                        onRestore = { pickRestoreFile() },
                        onOpenChangelog = { showChangelogScreen = true },
                        onOpenPrivacyPolicy = { showPrivacyPolicyScreen = true },
                        onOpenPermissionSetup = { showCommunityPermissionSetup = true },
                        onCheckForUpdates = {
                            GithubAppUpdateScheduler.checkNow(context)
                            Toast.makeText(context, R.string.update_check_started, Toast.LENGTH_SHORT).show()
                        },
                        onDeleteAccount = { account ->
                            savingsGoalsViewModel.getActiveGoalCountForAccount(account.id)
                                .let { count ->
                                    if (count > 0) "Cannot delete account with active savings goals" else null
                                }
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                TabContentAnimated(
                    activeTab = activeTab,
                    username = username,
                    reducedMotion = reducedMotion,
                    homeViewModel = homeViewModel,
                    insightsViewModel = insightsViewModel,
                    historyViewModel = historyViewModel,
                    budgetViewModel = budgetViewModel,
                    savingsGoalsViewModel = savingsGoalsViewModel,
                    budgetDockAddRequest = budgetDockAddRequest.intValue,
                    goalsDockAddRequest = goalsDockAddRequest.intValue,
                    accounts = accounts,
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    hapticManager = hapticManager,
                    notificationGate = notificationGate,
                    onOpenAccounts = { showAccountsScreen = true },
                    onOpenSavingsGoals = { activeTab = MakeFlowTab.GOALS },
                    onOpenSettings = { showSettingsScreen = true },
                    onOpenBudgets = { activeTab = MakeFlowTab.BUDGET },
                    onOpenRecurringTransactions = { showRecurringTransactionsScreen = true },
                    onOpenHistory = { activeTab = MakeFlowTab.HISTORY },
                    onAddTransaction = { type ->
                        shortcutPendingType.value = type
                        showAddTransaction = true
                    },
                    onNavigateToHome = { activeTab = MakeFlowTab.HOME },
                    onSetUpRecurringFromInsights = { amount ->
                        recurringSetupAmount = amount
                        showRecurringSetupFromInsights = true
                    },
                    onExportPdf = exportSelectedInsightsMonth,
                    onEditTransaction = { tx ->
                        editingTransaction = tx
                        showAddTransaction = true
                    },
                    onImportMpesaMessage = { data ->
                        pendingMpesaImport = data
                        mpesaImportRequestId.intValue++
                    }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = dimens.navbarMarginHorizontal)
                        .padding(
                            bottom = dimens.navbarMarginBottom + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    FloatingPillNav(
                        items = listOf(
                            PillNavItem(stringResource(R.string.nav_home), Icons.Rounded.Home),
                            PillNavItem(stringResource(R.string.nav_insights), Icons.Rounded.BarChart),
                            PillNavItem(stringResource(R.string.nav_budget), Icons.Rounded.PieChart),
                            PillNavItem(stringResource(R.string.nav_goals), Icons.Rounded.TrackChanges)
                        ),
                        selectedIndex = primaryTabs.indexOf(activeTab),
                        onTabSelected = { activeTab = primaryTabs[it] },
                        onAddClick = {
                            when (activeTab) {
                                MakeFlowTab.BUDGET -> budgetDockAddRequest.intValue++
                                MakeFlowTab.GOALS -> goalsDockAddRequest.intValue++
                                else -> {
                                    shortcutPendingType.value = null
                                    showAddTransaction = true
                                }
                            }
                        },
                        hapticManager = hapticManager,
                        addContentDescription = when (activeTab) {
                            MakeFlowTab.BUDGET -> "Add budget"
                            MakeFlowTab.GOALS -> "Add goal"
                            else -> stringResource(R.string.action_add_transaction)
                        }
                    )
                }

                OverlayPanels(
                    showBudgetScreen = showBudgetScreen,
                    onHideBudget = { showBudgetScreen = false },
                    showAccountsScreen = showAccountsScreen,
                    onHideAccounts = { showAccountsScreen = false },
                    showSettingsScreen = showSettingsScreen,
                    onHideSettings = { showSettingsScreen = false },
                    showRecurringTransactionsScreen = showRecurringTransactionsScreen,
                    onHideRecurringTransactions = { showRecurringTransactionsScreen = false },
                    budgetViewModel = budgetViewModel,
                    accountsViewModel = accountsViewModel,
                    settingsViewModel = settingsViewModel,
                    recurringTransactionsViewModel = recurringTransactionsViewModel,
                    hapticManager = hapticManager,
                    notificationGate = notificationGate,
                    reducedMotion = reducedMotion,
                    onOpenManageTemplates = { showManageTemplates = true },
                    onOpenSavingsGoals = { showSavingsGoalsScreen = true },
                    onOpenManageCategories = { showManageCategories = true },
                    onOpenExchangeRates = { showExchangeRates = true },
                    onBackup = { performBackup() },
                    onRestore = { pickRestoreFile() },
                    onOpenChangelog = { showChangelogScreen = true },
                    onOpenPrivacyPolicy = { showPrivacyPolicyScreen = true },
                    onOpenPermissionSetup = { showCommunityPermissionSetup = true },
                    onCheckForUpdates = {
                        GithubAppUpdateScheduler.checkNow(context)
                        Toast.makeText(context, R.string.update_check_started, Toast.LENGTH_SHORT).show()
                    },
                    onDeleteAccount = { account ->
                        savingsGoalsViewModel.getActiveGoalCountForAccount(account.id)
                            .let { count ->
                                if (count > 0) "Cannot delete account with active savings goals" else null
                            }
                    }
                )
            }
        }
    }

    AnimatedVisibility(
        visible = showExchangeRates,
        enter = slideInVertically(animationSpec = slideEnter, initialOffsetY = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutVertically(animationSpec = slideExit, targetOffsetY = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        ExchangeRatesSheet(
            rates = exchangeRates,
            displayCurrency = currencyCode,
            isRefreshing = exchangeRatesRefreshing,
            refreshError = exchangeRatesRefreshError,
            onRefresh = { refreshExchangeRates(force = true) },
            onDismiss = { showExchangeRates = false },
        )
    }

    AnimatedVisibility(
        visible = showManageCategories,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler { showManageCategories = false }
            CategoryManagementScreen(
                categories = categories,
                onBack = { showManageCategories = false },
                onMerge = { sourceId, targetId ->
                    repository.mergeCategories(sourceId, targetId)
                },
                onConvertType = { categoryId, newType ->
                    repository.convertCategoryType(categoryId, newType)
                },
                onCreate = { entity ->
                    repository.insertCategory(entity)
                    null
                },
                onDelete = { categoryId ->
                    repository.deleteCategoryWithGuards(categoryId)
                }
            )
        }
    }

    if (showRestoreConfirm && backupJson != null) {
        AdaptiveAlertSheet(
            onDismissRequest = { showRestoreConfirm = false; backupJson = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Restore Backup?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will replace ALL current data (accounts, transactions, categories, budgets, etc.) with the data from the backup file. This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = {
                    val json = backupJson
                    showRestoreConfirm = false
                    backupJson = null
                    if (json != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val error = settingsViewModel.importBackup(json)
                                withContext(Dispatchers.Main) {
                                    if (error != null) {
                                        restoreError = error
                                    } else {
                                        Toast.makeText(context, "Restore completed successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                AppLogger.error("MainScreen", "Restore failed", e)
                                withContext(Dispatchers.Main) {
                                    restoreError = "Restore failed: ${e.message}"
                                }
                            }
                        }
                    }
                }) {
                    Text("Restore", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false; backupJson = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (restoreError != null) {
        AdaptiveAlertSheet(
            onDismissRequest = { restoreError = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Restore Failed", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(restoreError ?: "Unknown error", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = { restoreError = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showChangelogScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler {
                showChangelogScreen = false
                scope.launch(Dispatchers.IO) {
                    userPrefDataStore.saveLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION)
                }
            }
            ChangelogScreen(
                hapticManager = hapticManager,
                onStartTransactionEntry = {
                    showChangelogScreen = false
                    shortcutPendingType.value = "EXPENSE"
                    showAddTransaction = true
                    scope.launch(Dispatchers.IO) {
                        userPrefDataStore.saveLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION)
                    }
                },
                onBack = {
                    showChangelogScreen = false
                    scope.launch(Dispatchers.IO) {
                        userPrefDataStore.saveLastSeenChangelogVersion(CURRENT_CHANGELOG_VERSION)
                    }
                }
            )
        }
    }

    if (showPrivacyPolicyScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler { showPrivacyPolicyScreen = false }
            PrivacyPolicyScreen(onBack = { showPrivacyPolicyScreen = false })
        }
    }

    if (showCommunityPermissionSetup) {
        CommunityPermissionSetupSheet(
            onComplete = ::completeCommunityPermissionSetup,
            onEnableMpesaAutoImport = {
                settingsViewModel.saveMpesaAutoImportEnabled(true)
            }
        )
    }

    if (showHomeWidgetOffer) {
        val appWidgetManager = remember(context) { AppWidgetManager.getInstance(context) }
        val widgetProvider = remember(context) { ComponentName(context, MakeFlowWidgetReceiver::class.java) }
        val dismissWidgetOffer: () -> Unit = {
            showHomeWidgetOffer = false
            scope.launch { userPrefDataStore.setShouldOfferHomeWidget(false) }
            Unit
        }
        AdaptiveSheetDialog(onDismiss = dismissWidgetOffer) {
            AdaptiveSheetHeader(
                title = "Keep Mindful close",
                subtitle = if (username.isBlank()) {
                    "Add a balance widget or quick action to your home screen."
                } else {
                    "${username.trim().substringBefore(' ')}, add a balance widget or quick action to your home screen."
                },
                onDismiss = dismissWidgetOffer
            )
            Text(
                text = if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                    "See your money at a glance and open logging in a tap. Your launcher will ask before adding it."
                } else {
                    "Your launcher can add Mindful widgets from its widget picker whenever you're ready."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                AdaptiveSheetPrimaryButton(
                    label = "Add a home-screen widget",
                    onClick = {
                        val requested = appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
                        if (!requested) {
                            Toast.makeText(context, "Open your launcher's widget picker to add Mindful.", Toast.LENGTH_LONG).show()
                        }
                        dismissWidgetOffer()
                    }
                )
            }
            TextButton(
                onClick = dismissWidgetOffer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Maybe later")
            }
        }
    }

    AnimatedVisibility(
        visible = showAddTransaction,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val onDismissAction = {
                shortcutPendingType.value = null
                editingTransaction = null
                mpesaInitialAmount = ""
                mpesaInitialType = null
                mpesaInitialNote = ""
                mpesaInitialTimestamp = null
                mpesaExternalReference = null
                mpesaTransactionCost = null
                mpesaBalanceCheck = null
                mpesaAiAssisted = false
                mpesaInitialAccountId = null
                mpesaInitialTransferFromId = null
                mpesaInitialTransferToId = null
                showAddTransaction = false
            }
            BackHandler { onDismissAction() }
            AddTransactionScreen(
                initialType = editingTransaction?.type ?: mpesaInitialType ?: shortcutPendingType.value ?: "EXPENSE",
                initialAmount = mpesaInitialAmount,
                initialNote = mpesaInitialNote,
                initialTimestamp = mpesaInitialTimestamp,
                initialAccountId = mpesaInitialAccountId ?: defaultTransactionAccountId,
                initialTransferFromId = mpesaInitialTransferFromId,
                initialTransferToId = mpesaInitialTransferToId,
                onDismiss = onDismissAction,
                showSaveAndAddAnother = editingTransaction == null && mpesaExternalReference == null,
                onSave = { amount, type, categoryId, accountId, note, timestamp, fromAccountId, toAccountId, destinationAmount ->
                    val externalReference = mpesaExternalReference
                    val cost = mpesaTransactionCost
                    withContext(Dispatchers.IO) {
                        repository.insertTransaction(
                            TransactionEntity(
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                accountId = accountId,
                                fromAccountId = fromAccountId,
                                toAccountId = toAccountId,
                                note = note,
                                timestamp = timestamp,
                                externalReference = externalReference,
                                destinationAmount = destinationAmount
                            )
                        )
                        if (externalReference != null && logMpesaCosts && cost != null && cost > 0) {
                            val feeCategoryId = categories.find {
                                it.name == "Transaction Fees" && it.parentCategoryId != null
                            }?.id
                            repository.insertTransaction(
                                TransactionEntity(
                                    amount = cost,
                                    type = "EXPENSE",
                                    categoryId = feeCategoryId,
                                    accountId = accountId,
                                    note = "M-PESA transaction cost for ${externalReference}",
                                    timestamp = timestamp,
                                    externalReference = "${externalReference}-fee"
                                )
                            )
                        }
                    }
                    userPrefDataStore.saveLastTransactionAccountId(accountId)
                    WidgetUpdateHelper.updateWidgets(context)
                    mpesaExternalReference = null
                    mpesaTransactionCost = null
                    mpesaBalanceCheck = null
                    mpesaAiAssisted = false
                    shortcutPendingType.value = null
                },
                accounts = accounts,
                categories = categories,
                currencyCode = currencyCode,
                exchangeRates = exchangeRates,
                hapticManager = hapticManager,
                templates = if (editingTransaction == null) templates else emptyList(),
                onSaveAsTemplate = if (editingTransaction == null) ({ name, amount, type, categoryId, accountId, note ->
                    scope.launch(Dispatchers.IO) {
                        templateRepository.insertTemplate(
                            TransactionTemplateEntity(
                                name = name,
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                accountId = accountId,
                                note = note
                            )
                        )
                    }
                }) else null,
                onAnomalyCheck = { amount, categoryId ->
                    if (categoryId == null) return@AddTransactionScreen null
                    val history = repository.getRecentAmountsForCategory(categoryId)
                    AnomalyDetector.analyze(amount, history)
                },
                onCheckDuplicate = { entity ->
                    repository.checkDuplicate(entity)
                },
                onGetPastTransactions = {
                    val pairs = repository.getRecentDescriptionsAndCategories()
                    pairs.map { PastTransactionForSuggestion(it.description, it.categoryId, it.accountId) }
                },
                onGetCategoryUsages = {
                    val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                    val txs = repository.getTransactionsInRange(cutoff, System.currentTimeMillis())
                    txs.first().map { tx ->
                        com.makemission.makeflow.domain.CategoryUsage(
                            categoryId = tx.categoryId ?: "",
                            type = tx.type,
                            timestamp = tx.timestamp
                        )
                    }
                },
                onGetUsualAmounts = { accountId, type, categoryId ->
                    val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                    repository.getTransactionsInRange(cutoff, System.currentTimeMillis()).first()
                        .asSequence()
                        .filter { it.accountId == accountId && it.type == type }
                        .filter { categoryId == null || it.categoryId == categoryId }
                        .groupingBy { it.amount }
                        .eachCount()
                        .entries
                        .sortedWith(compareByDescending<Map.Entry<Double, Int>> { it.value }.thenBy { it.key })
                        .take(4)
                        .map { it.key }
                },
                categoryClassifier = categoryClassifier,
                smartCategorizationEnabled = smartCategorizationEnabled,
                correctionMemory = correctionMemory,
                onCategoryCorrected = { description, correctedCategoryId, originalSuggestedCategoryId ->
                    val correctedCat = categories.find { it.id == correctedCategoryId } ?: return@AddTransactionScreen
                    val originalCat = originalSuggestedCategoryId?.let { id -> categories.find { it.id == id } }
                    val route = CorrectionRouter.route(correctedCat.name, labelSet)
                    scope.launch(Dispatchers.IO) {
                        when (route) {
                            CorrectionRoute.PART_B_TFLITE -> {
                                val labelIndex = labelSet.indexOf(correctedCat.name)
                                if (labelIndex >= 0) {
                                    correctionRepository.queueCorrection(
                                        description = description,
                                        correctedLabelName = correctedCat.name,
                                        correctedLabelIndex = labelIndex,
                                        originalSuggestedLabelName = originalCat?.name
                                    )
                                    if (categoryClassifier is RetrainableCategoryClassifier) {
                                        correctionRepository.trainBatchIfReady(
                                            categoryClassifier as RetrainableCategoryClassifier
                                        )
                                    }
                                }
                            }
                            CorrectionRoute.PART_C_CORRECTION_MEMORY -> {
                                val normalized = CategorySuggester.normalize(description)
                                correctionRepository.upsertCorrectionMemory(normalized, correctedCategoryId)
                                val updatedMemory = correctionRepository.getAllCorrectionMemory()
                                withContext(Dispatchers.Main) {
                                    correctionMemory = updatedMemory
                                }
                            }
                        }
                    }
                },
                editTransaction = editingTransaction,
                onUpdate = { entity ->
                    scope.launch(Dispatchers.IO) {
                        repository.updateTransaction(entity)
                        userPrefDataStore.saveLastTransactionAccountId(entity.accountId)
                        WidgetUpdateHelper.updateWidgets(context)
                        editingTransaction = null
                        showAddTransaction = false
                    }
                },
                mpesaTransactionCost = mpesaTransactionCost,
                mpesaBalanceCheck = mpesaBalanceCheck,
                mpesaAiAssisted = mpesaAiAssisted,
                logMpesaCosts = logMpesaCosts,
                onLoadDraft = { userPrefDataStore.getTransactionDraft() },
                onSaveDraft = { draft -> userPrefDataStore.saveTransactionDraft(draft) }
            )
        }
    }

    AnimatedVisibility(
        visible = showManageTemplates,
        enter = slideInVertically(animationSpec = slideEnter, initialOffsetY = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutVertically(animationSpec = slideExit, targetOffsetY = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        ManageTemplatesSheet(
            templates = templates,
            onDismiss = { showManageTemplates = false },
            onRename = { id, newName ->
                scope.launch(Dispatchers.IO) {
                    templateRepository.getTemplateById(id)?.let {
                        templateRepository.updateTemplate(it.copy(name = newName))
                    }
                }
            },
            onDelete = { template ->
                scope.launch(Dispatchers.IO) {
                    templateRepository.deleteTemplate(template)
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showSavingsGoalsScreen,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        SavingsGoalsScreen(
            onBack = { showSavingsGoalsScreen = false },
            viewModel = savingsGoalsViewModel,
            accounts = accounts,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            onAddAccount = { showAddAccountSheet = true }
        )
    }

    if (showAddAccountSheet) {
        AddEditAccountBottomSheet(
            onDismiss = { showAddAccountSheet = false },
            onSave = { name, balance, colorHex, accountCurrencyCode ->
                val account = com.makemission.makeflow.data.local.entity.AccountEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    balance = balance,
                    initialBalance = balance,
                    colorHex = colorHex,
                    currencyCode = accountCurrencyCode
                )
                repository.insertAccount(account)
            },
            defaultCurrencyCode = currencyCode
        )
    }

    if (showRecurringSetupFromInsights) {
        AddEditRecurringTransactionBottomSheet(
            onDismiss = { showRecurringSetupFromInsights = false },
            onSave = { entity: com.makemission.makeflow.data.local.entity.RecurringTransactionEntity ->
                scope.launch(Dispatchers.IO) {
                    repository.createRecurringTransaction(entity)
                    showRecurringSetupFromInsights = false
                }
            },
            accounts = accounts,
            categories = categories,
            currencySymbol = currencySymbol,
            prefillAmount = recurringSetupAmount,
            prefillDescription = "Savings"
        )
    }
}

}

@Composable
private fun TabContentAnimated(
    activeTab: MakeFlowTab,
    username: String,
    reducedMotion: Boolean,
    homeViewModel: HomeDashboardViewModel,
    insightsViewModel: InsightsViewModel,
    historyViewModel: HistoryViewModel,
    budgetViewModel: BudgetViewModel,
    savingsGoalsViewModel: SavingsGoalsViewModel,
    budgetDockAddRequest: Int,
    goalsDockAddRequest: Int,
    accounts: List<AccountEntity>,
    currencyCode: String,
    currencySymbol: String,
    hapticManager: com.makemission.makeflow.core.haptics.HapticManager,
    notificationGate: NotificationGate?,
    onOpenAccounts: () -> Unit,
    onOpenSavingsGoals: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenRecurringTransactions: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddTransaction: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onSetUpRecurringFromInsights: (Double) -> Unit = {},
    onExportPdf: () -> Unit = {},
    onEditTransaction: ((TransactionEntity) -> Unit)? = null,
    onImportMpesaMessage: (MpesaMessageParser.MpesaParsedMessage) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val tabIndex = when (activeTab) {
        MakeFlowTab.HOME -> 0
        MakeFlowTab.INSIGHTS -> 1
        MakeFlowTab.BUDGET -> 2
        MakeFlowTab.GOALS -> 3
        MakeFlowTab.HISTORY -> 4
    }
    val previousTabIndex = remember { mutableStateOf(0) }

    AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
            // Keep tab direction tied to the app's left-to-right order for spatial continuity.
            val direction = if (tabIndex >= previousTabIndex.value) 1 else -1
            previousTabIndex.value = tabIndex
            val enterSlide = if (reducedMotion) OverlayTransition.reducedEnterSlide else OverlayTransition.enterSlide
            val exitSlide = if (reducedMotion) OverlayTransition.reducedExitSlide else OverlayTransition.exitSlide
            (fadeIn(if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.enterFade) +
                slideInHorizontally(
                    animationSpec = enterSlide,
                    initialOffsetX = { (it * OverlayTransition.TabSlideFraction * direction).toInt() }
                ) + scaleIn(
                    initialScale = if (reducedMotion) 1f else OverlayTransition.TabEnterScale,
                    animationSpec = if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.tabScaleEnter
                )) togetherWith
                (fadeOut(if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.exitFade) +
                    slideOutHorizontally(
                        animationSpec = exitSlide,
                        targetOffsetX = { -(it * OverlayTransition.TabSlideFraction * direction).toInt() }
                    ) + scaleOut(
                        targetScale = if (reducedMotion) 1f else OverlayTransition.TabExitScale,
                        animationSpec = if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.fastExit
                    ))
        },
        label = "TabContent"
    ) { tab ->
        when (tab) {
            MakeFlowTab.HOME -> {
                HomeDashboard(
                    viewModel = homeViewModel,
                    username = username,
                    onOpenAccounts = onOpenAccounts,
                    onOpenSavingsGoals = onOpenSavingsGoals,
                    onOpenSettings = onOpenSettings,
                    onOpenBudgets = onOpenBudgets,
                    onOpenHistory = onOpenHistory,
                    onAddTransaction = onAddTransaction,
                    onEditTransaction = { tx -> onEditTransaction?.invoke(tx) },
                    onImportMpesaMessage = onImportMpesaMessage,
                    hapticManager = hapticManager,
                    notificationGate = notificationGate
                )
            }
            MakeFlowTab.INSIGHTS -> {
                BackHandler { onNavigateToHome() }
                InsightsScreen(
                    viewModel = insightsViewModel,
                    onOpenBudgets = onOpenBudgets,
                    onSetUpRecurring = onSetUpRecurringFromInsights,
                    onExportPdf = onExportPdf,
                    hapticManager = hapticManager,
                    notificationGate = notificationGate
                )
            }
            MakeFlowTab.BUDGET -> {
                BackHandler { onNavigateToHome() }
                BudgetScreen(
                    onBack = onNavigateToHome,
                    viewModel = budgetViewModel,
                    hapticManager = hapticManager,
                    notificationGate = notificationGate,
                    isPrimaryDestination = true,
                    dockAddRequest = budgetDockAddRequest
                )
            }
            MakeFlowTab.GOALS -> {
                BackHandler { onNavigateToHome() }
                SavingsGoalsScreen(
                    onBack = onNavigateToHome,
                    viewModel = savingsGoalsViewModel,
                    accounts = accounts,
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    onAddAccount = onOpenAccounts,
                    isPrimaryDestination = true,
                    dockAddRequest = goalsDockAddRequest
                )
            }
            MakeFlowTab.HISTORY -> {
                BackHandler { onNavigateToHome() }
                HistoryScreen(
                    viewModel = historyViewModel,
                    hapticManager = hapticManager,
                    onEditTransaction = onEditTransaction,
                    onAddTransaction = onAddTransaction
                )
            }
        }
    }
}

@Composable
private fun AddTransactionFloatingActionButton(
    onClick: () -> Unit,
    fabSize: androidx.compose.ui.unit.Dp
) {
    val dimens = LocalAppDimens.current
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.background,
        shape = CircleShape,
        modifier = Modifier.size(fabSize)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add transaction",
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun OverlayPanels(
    showBudgetScreen: Boolean,
    onHideBudget: () -> Unit,
    showAccountsScreen: Boolean,
    onHideAccounts: () -> Unit,
    showSettingsScreen: Boolean,
    onHideSettings: () -> Unit,
    showRecurringTransactionsScreen: Boolean,
    onHideRecurringTransactions: () -> Unit,
    budgetViewModel: BudgetViewModel,
    accountsViewModel: AccountsViewModel,
    settingsViewModel: SettingsViewModel,
    recurringTransactionsViewModel: RecurringTransactionsViewModel,
    hapticManager: com.makemission.makeflow.core.haptics.HapticManager,
    notificationGate: NotificationGate?,
    reducedMotion: Boolean,
    onOpenManageTemplates: () -> Unit = {},
    onOpenSavingsGoals: () -> Unit = {},
    onOpenManageCategories: () -> Unit = {},
    onOpenExchangeRates: () -> Unit = {},
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenPermissionSetup: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onDeleteAccount: (suspend (AccountEntity) -> String?)? = null
) {
    val dimens = LocalAppDimens.current
    val slideEnter = if (reducedMotion) OverlayTransition.reducedEnterSlide else OverlayTransition.enterSlide
    val slideExit = if (reducedMotion) OverlayTransition.reducedExitSlide else OverlayTransition.exitSlide
    val fadeEnter = if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.enterFade
    val fadeExit = if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.exitFade

    AnimatedVisibility(
        visible = showBudgetScreen,
        enter = slideInVertically(animationSpec = slideEnter, initialOffsetY = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutVertically(animationSpec = slideExit, targetOffsetY = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(Modifier.fillMaxSize()) {
            BackHandler { onHideBudget() }
            BudgetScreen(
                onBack = onHideBudget,
                viewModel = budgetViewModel,
                hapticManager = hapticManager,
                notificationGate = notificationGate
            )
        }
    }

    AnimatedVisibility(
        visible = showAccountsScreen,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceDim)
        ) {
            BackHandler { onHideAccounts() }
            AccountsScreen(
                onBack = onHideAccounts,
                viewModel = accountsViewModel,
                onDeleteAccount = onDeleteAccount,
                onOpenExchangeRates = onOpenExchangeRates,
                hapticManager = hapticManager
            )
        }
    }

    AnimatedVisibility(
        visible = showSettingsScreen,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler { onHideSettings() }
            SettingsScreen(
                onBack = onHideSettings,
                viewModel = settingsViewModel,
                hapticManager = hapticManager,
                notificationGate = notificationGate,
                onOpenRecurringTransactions = onHideRecurringTransactions,
                onOpenSavingsGoals = onOpenSavingsGoals,
                onOpenManageTemplates = onOpenManageTemplates,
                onOpenManageCategories = onOpenManageCategories,
                onOpenExchangeRates = onOpenExchangeRates,
                onBackup = onBackup,
                onRestore = onRestore,
                onOpenChangelog = onOpenChangelog,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                onOpenPermissionSetup = onOpenPermissionSetup,
                onCheckForUpdates = onCheckForUpdates
            )
        }
    }

    AnimatedVisibility(
        visible = showRecurringTransactionsScreen,
        enter = slideInHorizontally(animationSpec = slideEnter, initialOffsetX = { it }) +
            fadeIn(animationSpec = fadeEnter),
        exit = slideOutHorizontally(animationSpec = slideExit, targetOffsetX = { it }) +
            fadeOut(animationSpec = fadeExit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler { onHideRecurringTransactions() }
            RecurringTransactionsScreen(
                onBack = onHideRecurringTransactions,
                viewModel = recurringTransactionsViewModel
            )
        }
    }
}

@Composable
private fun MakeFlowNavRail(
    selectedTab: MakeFlowTab,
    onTabSelected: (MakeFlowTab) -> Unit,
    layoutMode: DeviceLayoutMode
) {
    val dimens = LocalAppDimens.current
    val alwaysShowLabel = layoutMode == DeviceLayoutMode.EXPANDED

    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp),
        containerColor = Color.Transparent,
        header = {
            Text(
                text = "Mindful Wallet",
                color = MaterialTheme.colorScheme.outline,
                fontSize = if (alwaysShowLabel) 12.sp else 0.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) {
        primaryTabs.forEach { tab ->
            NavigationRailItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            MakeFlowTab.HOME -> Icons.Rounded.Home
                            MakeFlowTab.INSIGHTS -> Icons.Rounded.BarChart
                            MakeFlowTab.BUDGET -> Icons.Rounded.PieChart
                            MakeFlowTab.GOALS -> Icons.Rounded.TrackChanges
                            MakeFlowTab.HISTORY -> Icons.Rounded.History
                        },
                        contentDescription = tab.name
                    )
                },
                label = {
                    Text(
                        text = when (tab) {
                            MakeFlowTab.HOME -> stringResource(R.string.nav_home)
                            MakeFlowTab.INSIGHTS -> stringResource(R.string.nav_insights)
                            MakeFlowTab.BUDGET -> stringResource(R.string.nav_budget)
                            MakeFlowTab.GOALS -> stringResource(R.string.nav_goals)
                            MakeFlowTab.HISTORY -> stringResource(R.string.nav_history)
                        },
                        fontSize = dimens.textS
                    )
                },
                alwaysShowLabel = alwaysShowLabel
            )
        }
    }
}

@Composable
fun FloatingCapsuleNavBar(
    selectedTab: MakeFlowTab,
    onTabSelected: (MakeFlowTab) -> Unit,
    config: AdaptiveConfig = AdaptiveConfig()
) {
    val dimens = LocalAppDimens.current
    val tabs = primaryTabs
    val reducedMotion = LocalReducedMotion.current

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = if (config.isTablet) 12.dp else 8.dp, vertical = if (config.isTablet) 8.dp else 6.dp),
        horizontalArrangement = Arrangement.spacedBy(if (config.isTablet) 10.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                    .animateAppContentSize()
                    .padding(horizontal = if (isSelected) (if (config.isTablet) 20.dp else 16.dp) else (if (config.isTablet) 16.dp else 12.dp), vertical = if (config.isTablet) 14.dp else 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (tab) {
                        MakeFlowTab.HOME -> Icons.Rounded.Home
                        MakeFlowTab.INSIGHTS -> Icons.Rounded.BarChart
                        MakeFlowTab.BUDGET -> Icons.Rounded.PieChart
                        MakeFlowTab.GOALS -> Icons.Rounded.TrackChanges
                        MakeFlowTab.HISTORY -> Icons.Rounded.History
                    },
                    contentDescription = tab.name,
                    tint = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(config.navIconSize)
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(
                        animationSpec = if (reducedMotion) OverlayTransition.reducedEnter else OverlayTransition.fastEnter
                    ),
                    exit = fadeOut(
                        animationSpec = if (reducedMotion) OverlayTransition.reducedExit else OverlayTransition.fastExit
                    )
                ) {
                    Text(
                        text = when (tab) {
                            MakeFlowTab.HOME -> "Home"
                            MakeFlowTab.INSIGHTS -> "Insights"
                            MakeFlowTab.BUDGET -> "Budget"
                            MakeFlowTab.GOALS -> "Goals"
                            MakeFlowTab.HISTORY -> "History"
                        },
                        color = MaterialTheme.colorScheme.background,
                        fontSize = if (config.isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(start = if (config.isTablet) 10.dp else 8.dp)
                    )
                }
            }
        }
    }
}
