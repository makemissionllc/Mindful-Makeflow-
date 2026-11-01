package com.makemission.makeflow.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.makemission.makeflow.core.device.InterruptionFilterProvider
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.core.ml.MpesaGeminiNanoParser
import com.makemission.makeflow.core.device.LocalReducedMotion
import com.makemission.makeflow.core.device.rememberPowerState
import com.makemission.makeflow.core.haptics.rememberHapticManager
import com.makemission.makeflow.core.haptics.HapticStrength
import com.makemission.makeflow.core.layout.LocalDeviceLayoutMode
import com.makemission.makeflow.core.layout.rememberDeviceLayoutMode
import com.makemission.makeflow.core.notifications.NotificationChannels
import com.makemission.makeflow.core.notifications.NotificationGate
import com.makemission.makeflow.core.notifications.NotificationSenderImpl
import com.makemission.makeflow.data.local.AppDatabase
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.data.local.userPrefDataStore
import com.makemission.makeflow.data.repository.FinanceRepositoryImpl
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.domain.ZiidiMessageParser
import com.makemission.makeflow.ui.MainScreen
import com.makemission.makeflow.ui.screens.OnboardingScreen
import com.makemission.makeflow.ui.theme.MakeFlowTheme
import com.makemission.makeflow.ui.theme.MindfulAccentHex
import com.makemission.makeflow.ui.theme.OverlayTransition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun AppNavigationEngine(
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    importedMpesaMessage: MpesaMessageParser.MpesaParsedMessage? = null,
    onImportedMpesaMessageConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        FinanceRepositoryImpl(
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            taskDao = db.taskDao(),
            accountDao = db.accountDao(),
            recurringTransactionDao = db.recurringTransactionDao()
        )
    }
    val userPrefDataStore = remember { UserPreferencesDataStore(context.userPrefDataStore) }

    val isProfiled by userPrefDataStore.isProfiled
        .map { it as Boolean? }
        .collectAsState(initial = null)
    val username by userPrefDataStore.userName.collectAsState(initial = "")
    val currency by userPrefDataStore.selectedCurrency.collectAsState(initial = "KES")
    val themeMode by userPrefDataStore.themeMode.collectAsState(initial = "system")
    val amoledMode by userPrefDataStore.amoledMode.collectAsState(initial = false)
    val hapticLevel by userPrefDataStore.hapticLevel.collectAsState(initial = HapticStrength.STANDARD.storageValue)
    val hapticManager = rememberHapticManager(HapticStrength.fromStorage(hapticLevel))
    val powerState by rememberPowerState()

    LaunchedEffect(Unit) {
        NotificationChannels.createChannels(context)
    }

    val interruptionFilterProvider = remember { InterruptionFilterProvider(context) }
    val notificationSender = remember { NotificationSenderImpl(context.applicationContext) }
    val notificationGate = remember {
        NotificationGate(
            interruptionFilterProvider = interruptionFilterProvider,
            queuedNotificationDao = db.queuedNotificationDao(),
            notificationSender = notificationSender
        )
    }

    LaunchedEffect(Unit) {
        interruptionFilterProvider.observeState().collect { state ->
            if (state == com.makemission.makeflow.core.device.InterruptionState.NORMAL) {
                notificationGate.flushQueueIfClear()
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val patterns = db.recurringPatternDao().getAllOnce()
            val now = System.currentTimeMillis()
            val oneDay = 24L * 60 * 60 * 1000
            for (pattern in patterns) {
                if (pattern.nextExpectedDate in (now - oneDay)..(now + oneDay)) {
                    if (pattern.nextExpectedDate != pattern.lastNotifiedNextDate) {
                        notificationGate.dispatchOrQueue(
                            type = "bill_reminder",
                            title = "Upcoming bill",
                            body = "${pattern.normalizedDescription} is due soon"
                        )
                        db.recurringPatternDao().updateLastNotifiedNextDate(
                            pattern.id, pattern.nextExpectedDate
                        )
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.error("AppNav", "Failed to update recurring pattern lastNotifiedNextDate", e)
        }
    }

    LaunchedEffect(Unit) {
        try {
            repository.processDueRecurringTransactions()
        } catch (e: Exception) {
            AppLogger.error("AppNav", "Failed to process due recurring transactions", e)
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    val windowSizeClass = calculateWindowSizeClass(context as ComponentActivity)
    val deviceLayoutMode = rememberDeviceLayoutMode(windowSizeClass)

    val shortcutType = remember {
        (context as ComponentActivity).intent?.getStringExtra("shortcut_type")
            ?.takeIf { it == "EXPENSE" || it == "INCOME" }
    }

    val appLockEnabled by userPrefDataStore.appLockEnabled.collectAsState(initial = false)
    var isUnlocked by remember { mutableStateOf(!appLockEnabled) }

    LaunchedEffect(appLockEnabled) {
        isUnlocked = !appLockEnabled
    }

    // M-PESA share intent parsing
    var parsedMpesa by remember { mutableStateOf<MpesaMessageParser.MpesaParsedMessage?>(null) }
    var shareParseError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            val operationId = AppLogger.newOperationId()
            AppLogger.event(
                tag = "AppNav",
                event = "shared_message_received",
                operationId = operationId,
                details = mapOf("characters" to sharedText.length)
            )
            val ziidi = ZiidiMessageParser.parse(sharedText)
            val deterministic = ziidi ?: MpesaMessageParser.parse(sharedText)
            val result = deterministic
                ?.takeIf { it.type != MpesaMessageParser.TransactionType.UNKNOWN }
                ?: MpesaGeminiNanoParser.parseIfAvailable(sharedText)
            if (result != null) {
                parsedMpesa = result.copy(operationId = operationId)
                shareParseError = null
                AppLogger.event(
                    "AppNav",
                    "shared_message_parsed",
                    operationId,
                    mapOf(
                        "source" to result.balanceSource,
                        "type" to result.type,
                        "amount" to result.amount,
                        "balance" to result.balance,
                        "reference_hash" to AppLogger.hashIdentifier(result.code),
                        "ai_assisted" to result.isAiAssisted
                    )
                )
            } else {
                parsedMpesa = null
                shareParseError = "MakeFlow couldn't verify this as a completed M-PESA transaction. You can enter it manually."
                AppLogger.event("AppNav", "shared_message_not_recognized", operationId, level = "WARN")
            }
            onSharedTextConsumed()
        }
    }

    LaunchedEffect(importedMpesaMessage) {
        importedMpesaMessage?.let { message ->
            parsedMpesa = message
            shareParseError = null
            onImportedMpesaMessageConsumed()
        }
    }

    CompositionLocalProvider(
        LocalReducedMotion provides powerState.shouldReduceMotion,
        LocalDeviceLayoutMode provides deviceLayoutMode
    ) {
    MakeFlowTheme(themeMode = themeMode, amoledMode = amoledMode) {
        if (!isUnlocked) {
            com.makemission.makeflow.core.lock.LockScreen(
                onUnlocked = { isUnlocked = true }
            )
        } else {
        val currentIsProfiled = isProfiled
        if (currentIsProfiled != null) {
        Crossfade(
            targetState = currentIsProfiled,
            animationSpec = if (LocalReducedMotion.current) {
                OverlayTransition.reducedNavigationCrossfade
            } else {
                OverlayTransition.navigationCrossfade
            },
            label = "AppNavigationEngineCrossfade"
        ) { profiled ->
            if (profiled) {
                MainScreen(
                    username = username,
                    onLogout = {
                        scope.launch {
                            userPrefDataStore.saveName("")
                            userPrefDataStore.clearProfile()
                        }
                    },
                    notificationGate = notificationGate,
                    initialTransactionType = shortcutType,
                    sharedMpesaData = parsedMpesa,
                    shareParseError = shareParseError,
                    onShareParseErrorShown = { shareParseError = null }
                )
            } else {
                OnboardingScreen(
                    selectedCurrency = currency,
                    onCurrencyChange = { scope.launch { userPrefDataStore.saveCurrency(it) } },
                    onSaveName = { name -> userPrefDataStore.saveName(name) },
                    onSaveAccounts = { accounts ->
                        for (account in accounts) {
                            val entity = account.copy(colorHex = account.colorHex.ifEmpty { MindfulAccentHex })
                            repository.insertAccount(entity)
                        }
                    },
                    onOnboardingComplete = {
                        scope.launch {
                            userPrefDataStore.setProfiled()
                            userPrefDataStore.setShouldOfferHomeWidget(true)
                        }
                    },
                    hapticManager = hapticManager
                )
            }
        }
    }
    }
    }
    }
}
