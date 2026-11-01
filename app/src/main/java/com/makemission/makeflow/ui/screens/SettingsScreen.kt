package com.makemission.makeflow.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import java.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestorePage
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.MaterialTheme
import com.makemission.makeflow.core.layout.AdaptiveAlertSheet
import com.makemission.makeflow.core.layout.AdaptiveSheetDialog
import com.makemission.makeflow.core.layout.AdaptiveSheetHeader
import com.makemission.makeflow.core.layout.AdaptiveSheetPrimaryButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.makemission.makeflow.core.device.LocalReducedMotion
import com.makemission.makeflow.core.haptics.HapticEvent
import com.makemission.makeflow.core.haptics.HapticManager
import com.makemission.makeflow.core.haptics.HapticStrength
import com.makemission.makeflow.core.notifications.NotificationGate
import com.makemission.makeflow.core.notifications.MpesaAutoImportNotification
import com.makemission.makeflow.core.notifications.FinanceReviewReminderScheduler
import com.makemission.makeflow.ui.util.sharePdfStatement
import com.makemission.makeflow.ui.util.shareCsvStatement
import com.makemission.makeflow.core.notifications.openAppNotificationSettings
import com.makemission.makeflow.ui.components.CollapsingTopBar
import com.makemission.makeflow.ui.components.LargeTitleItem
import com.makemission.makeflow.ui.components.LocalAdaptiveConfig
import com.makemission.makeflow.ui.theme.LocalAppDimens
import com.makemission.makeflow.ui.theme.LocalMakeFlowColors
import com.makemission.makeflow.ui.components.StaggeredItem
import com.makemission.makeflow.ui.components.pressScale
import com.makemission.makeflow.ui.viewmodel.SettingsViewModel
import com.makemission.makeflow.core.format.CurrencyFormatter
import com.makemission.makeflow.BuildConfig
import com.makemission.makeflow.R
import com.makemission.makeflow.domain.HistoryPeriod
import java.time.ZonedDateTime

data class CurrencyOption(val code: String, val symbol: String)

private val modeLabels = mapOf(
    "system" to "System",
    "light" to "Light",
    "dark" to "Dark"
)
private val modeOptions = listOf("system", "light", "dark")

private val currencies = listOf(
    CurrencyOption("KES", "KES"),
    CurrencyOption("USD", "$"),
    CurrencyOption("EUR", "\u20AC"),
    CurrencyOption("GBP", "\u00A3"),
    CurrencyOption("UGX", "UGX"),
    CurrencyOption("TZS", "TZS")
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    hapticManager: HapticManager? = null,
    notificationGate: NotificationGate? = null,
    onOpenRecurringTransactions: () -> Unit = {},
    onOpenSavingsGoals: () -> Unit = {},
    onOpenManageTemplates: () -> Unit = {},
    onOpenManageCategories: () -> Unit = {},
    onOpenExchangeRates: () -> Unit = {},
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenPermissionSetup: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val currentCurrency by viewModel.selectedCurrency.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val amoledMode by viewModel.amoledMode.collectAsState()
    val hapticLevel by viewModel.hapticLevel.collectAsState()
    val smartCategorizationEnabled by viewModel.smartCategorizationEnabled.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val logMpesaCosts by viewModel.logMpesaCosts.collectAsState()
    val mpesaAutoImportEnabled by viewModel.mpesaAutoImportEnabled.collectAsState()
    val mpesaBackgroundLoggingEnabled by viewModel.mpesaBackgroundLoggingEnabled.collectAsState()
    val financeReviewRemindersEnabled by viewModel.financeReviewRemindersEnabled.collectAsState()
    val hasPromptedNotif by viewModel.hasPromptedNotificationPermission.collectAsState()
    val listState = rememberLazyListState()
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showModePicker by remember { mutableStateOf(false) }
    val isEffectivelyDark = when (currentThemeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetPersonalizationConfirm by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var showCsvExportSheet by rememberSaveable { mutableStateOf(false) }
    var csvCalendarNow by remember { mutableStateOf(ZonedDateTime.now()) }
    val isExportingCsv by viewModel.isExportingCsv.collectAsState()
    val isExportingDiagnostics by viewModel.isExportingDiagnostics.collectAsState()
    val context = LocalContext.current
    val openCsvExport = {
        hapticManager?.fire(HapticEvent.NAV_SELECT)
        csvCalendarNow = ZonedDateTime.now()
        showCsvExportSheet = true
    }
    val exportDiagnostics = {
        hapticManager?.fire(HapticEvent.NAV_SELECT)
        viewModel.exportDiagnosticLog(
            context = context,
            onError = {
                Toast.makeText(context, "Could not prepare the diagnostic log.", Toast.LENGTH_LONG).show()
            },
            onComplete = { report ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "MakeFlow diagnostic report")
                    putExtra(Intent.EXTRA_TEXT, report)
                }
                runCatching {
                    context.startActivity(Intent.createChooser(shareIntent, "Share diagnostic report"))
                }.onFailure {
                    Toast.makeText(context, "No app is available to share the diagnostic log.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
    var showMpesaImportDisclosure by remember { mutableStateOf(false) }
    var showMpesaBackgroundLoggingDisclosure by remember { mutableStateOf(false) }
    val config = LocalAdaptiveConfig.current
    val reducedMotion = LocalReducedMotion.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && MpesaAutoImportNotification.hasNotificationAccess(context)) {
            viewModel.saveMpesaAutoImportEnabled(true)
        } else {
            Toast.makeText(context, "Notification access is needed for M-PESA review alerts.", Toast.LENGTH_LONG).show()
        }
    }
    val requestNotificationAccess = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (MpesaAutoImportNotification.hasNotificationAccess(context)) {
            viewModel.saveMpesaAutoImportEnabled(true)
        } else {
            Toast.makeText(context, "Enable MakeFlow notifications to review detected transactions.", Toast.LENGTH_LONG).show()
            openAppNotificationSettings(context)
        }
    }
    val receiveSmsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            requestNotificationAccess()
        } else {
            Toast.makeText(context, "M-PESA auto-import remains off without SMS access.", Toast.LENGTH_LONG).show()
        }
    }
    val financeReviewPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.saveFinanceReviewRemindersEnabled(true)
            FinanceReviewReminderScheduler.setEnabled(context, true)
        } else {
            Toast.makeText(context, "Weekly review reminders remain off.", Toast.LENGTH_LONG).show()
        }
    }
    val onFinanceReviewReminderToggle: (Boolean) -> Unit = { enable ->
        if (!enable) {
            viewModel.saveFinanceReviewRemindersEnabled(false)
            FinanceReviewReminderScheduler.setEnabled(context, false)
        } else if (!androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "Allow notifications to receive weekly reminders.", Toast.LENGTH_LONG).show()
            openAppNotificationSettings(context)
        } else if (isNotificationPermissionGranted(context)) {
            viewModel.saveFinanceReviewRemindersEnabled(true)
            FinanceReviewReminderScheduler.setEnabled(context, true)
        } else {
            financeReviewPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val smsPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED
    val notificationsEnabled = MpesaAutoImportNotification.hasNotificationAccess(context)
    val mpesaAutoImportActive = mpesaAutoImportEnabled && smsPermissionGranted && notificationsEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_list")
                .padding(horizontal = config.horizontalPadding),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + config.barHeight,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)
        ) {
            item {
                LargeTitleItem(title = "Settings", scrollState = listState)
            }

            if (config.isTablet && config.isLandscape) {
                item { StaggeredItem(index = 1) { SectionHeader(text = "Preferences") } }

                item {
                    StaggeredItem(index = 2) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(config.cardSpacing)) {
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.SwapHoriz, title="Currency", subtitle=currentCurrency, onClick={ showCurrencyPicker = true })
                        }
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.DarkMode, title="Mode", subtitle=modeLabels[currentThemeMode] ?: "System", onClick={ showModePicker = true })
                        }
                    }
                    }
                }

                item {
                    StaggeredItem(index = 3) {
                        AmoledToggleRow(
                            enabled = amoledMode,
                            isDarkAvailable = isEffectivelyDark,
                            onToggle = { viewModel.saveAmoledMode(it) }
                        )
                    }
                }

                item {
                    StaggeredItem(index = 3) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Recurring Transactions", subtitle="Manage scheduled bills & income", onClick={ onOpenRecurringTransactions() })
                    }
                }

                item {
                    StaggeredItem(index = 4) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Manage Templates", subtitle="Create, edit & delete templates", onClick={ onOpenManageTemplates() })
                    }
                }

                item {
                    StaggeredItem(index = 5) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Manage Categories", subtitle="Merge or convert categories", onClick={ onOpenManageCategories() })
                    }
                }

                item {
                    StaggeredItem(index = 6) {
                    SettingsRow(icon=Icons.Outlined.SwapHoriz, title="Exchange Rates", subtitle="Set conversion rates for multi-currency", onClick={ onOpenExchangeRates() })
                    }
                }

                item {
                    StaggeredItem(index = 7) {
                    SettingsRow(icon=Icons.Outlined.EmojiEvents, title="Savings Goals", subtitle="Track and manage savings targets", onClick={ onOpenSavingsGoals() })
                    }
                }

                item {
                    StaggeredItem(index = 8) {
                    SmartCategorizationToggleRow(
                        enabled = smartCategorizationEnabled,
                        onToggle = { viewModel.saveSmartCategorizationEnabled(it) }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 8) {
                        HapticLevelRow(
                            selected = hapticLevel,
                            hapticManager = hapticManager,
                            onSelect = { viewModel.saveHapticLevel(it) }
                        )
                    }
                }

                item {
                    StaggeredItem(index = 9) {
                    AppLockToggleRow(
                        enabled = appLockEnabled,
                        onToggle = { viewModel.saveAppLockEnabled(it) }
                    )
                    }
                }

                if (BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED) item {
                    StaggeredItem(index = 10) {
                    MpesaAutoImportToggleRow(
                        enabled = mpesaAutoImportActive,
                        subtitle = if (mpesaAutoImportActive) "Detect M-PESA and ZiDi messages; review before saving" else "Optional · detects M-PESA and ZiDi messages for review",
                        onToggle = { enable ->
                            if (enable) showMpesaImportDisclosure = true
                            else {
                                viewModel.saveMpesaAutoImportEnabled(false)
                                viewModel.saveMpesaBackgroundLoggingEnabled(false)
                            }
                        }
                    )
                    }
                }

                if (BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED && mpesaAutoImportActive) item {
                    StaggeredItem(index = 11) {
                        MpesaBackgroundLoggingToggleRow(
                            enabled = mpesaBackgroundLoggingEnabled,
                            onToggle = { enable ->
                                if (enable) showMpesaBackgroundLoggingDisclosure = true
                                else viewModel.saveMpesaBackgroundLoggingEnabled(false)
                            }
                        )
                    }
                }

                item {
                    StaggeredItem(index = 12) {
                    MpesaCostsToggleRow(
                        enabled = logMpesaCosts,
                        onToggle = { viewModel.saveLogMpesaCosts(it) }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 12) {
                        FinanceReviewReminderToggleRow(
                            enabled = financeReviewRemindersEnabled,
                            onToggle = onFinanceReviewReminderToggle
                        )
                    }
                }

                if (hasPromptedNotif && !isNotificationPermissionGranted(context)) {
                    item {
                        StaggeredItem(index = 11) {
                        Box(Modifier.fillMaxWidth()) {
                            NotificationEnableRow()
                        }
                        }
                    }
                    item { StaggeredItem(index = 11) { Spacer(modifier = Modifier.height(dimens.spacingXSmall)) } }
                }

                item { StaggeredItem(index = 12) { Spacer(modifier = Modifier.height(dimens.spacingXLarge)) } }
                item { StaggeredItem(index = 13) { SectionHeader(text = "Data") } }

                item {
                    StaggeredItem(index = 14) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(config.cardSpacing)) {
                        Box(Modifier.weight(1f)) {
                        SettingsRow(
                            icon = Icons.Outlined.FileDownload,
                            title = stringResource(R.string.export_csv_title),
                            subtitle = stringResource(if (isExportingCsv) R.string.export_csv_preparing else R.string.export_csv_subtitle),
                            enabled = !isExportingCsv,
                            onClick = openCsvExport
                        )
                        }
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.FileDownload, title="Export PDF", subtitle="Statement with date range", onClick={ showPdfExportDialog = true })
                        }
                    }
                    }
                }

                item {
                    StaggeredItem(index = 15) {
                        SettingsRow(
                            icon = Icons.Outlined.Info,
                            title = "Share diagnostic log",
                            subtitle = if (isExportingDiagnostics) "Preparing local event history" else "Troubleshooting events and balance checks; review before sharing",
                            enabled = !isExportingDiagnostics,
                            onClick = exportDiagnostics
                        )
                    }
                }

                item { StaggeredItem(index = 100) { Spacer(modifier = Modifier.height(dimens.spacingXSmall)) } }

                item {
                    StaggeredItem(index = 101) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(config.cardSpacing)) {
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.Save, title="Backup Now", subtitle="Save all data to a file", onClick={ onBackup() })
                        }
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.RestorePage, title="Restore", subtitle="Replace from a file", onClick={ onRestore() })
                        }
                    }
                    }
                }

                item { StaggeredItem(index = 102) { Spacer(modifier = Modifier.height(dimens.spacingXSmall)) } }

                item {
                    StaggeredItem(index = 103) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(config.cardSpacing)) {
                        Box(Modifier.weight(1f)) {
                        ResetPersonalizationRow(
                            viewModel = viewModel,
                            showConfirm = showResetPersonalizationConfirm,
                            onShowConfirm = { showResetPersonalizationConfirm = true },
                            onDismissConfirm = { showResetPersonalizationConfirm = false }
                        )
                        }
                        Box(Modifier.weight(1f)) {
                        SettingsRow(icon=Icons.Outlined.Delete, title="Clear All Data", subtitle="Reset everything", onClick={ showClearConfirm = true })
                        }
                    }
                    }
                }

                item { StaggeredItem(index = 104) { Spacer(modifier = Modifier.height(dimens.spacingXLarge)) } }
                item { StaggeredItem(index = 105) { SectionHeader(text = "About") } }

                item {
                    StaggeredItem(index = 106) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Mindful Wallet", subtitle="Version ${BuildConfig.VERSION_NAME}")
                    }
                }
                if (BuildConfig.APPLICATION_ID == "com.makemission.makeflow.community" && !BuildConfig.DEBUG) item {
                    StaggeredItem(index = 107) {
                        SettingsRow(
                            icon = Icons.Outlined.FileDownload,
                            title = stringResource(R.string.update_check_title),
                            subtitle = stringResource(R.string.update_check_summary),
                            onClick = {
                                hapticManager?.fire(HapticEvent.NAV_SELECT)
                                onCheckForUpdates()
                            }
                        )
                    }
                }
                item {
                    StaggeredItem(index = 108) {
                    SettingsRow(icon=Icons.Outlined.Campaign, title="What's New", subtitle="Updates and feature guide", onClick = onOpenChangelog)
                    }
                }
                item {
                    StaggeredItem(index = 109) {
                        SettingsRow(icon=Icons.Outlined.Info, title="Permission setup", subtitle="Review SMS and notifications access", onClick = onOpenPermissionSetup)
                    }
                }
                item {
                    StaggeredItem(index = 110) {
                        SettingsRow(icon=Icons.Outlined.Lock, title="Privacy & data", subtitle="How your information is handled", onClick = onOpenPrivacyPolicy)
                    }
                }
                if (reducedMotion) {
                    item {
                        StaggeredItem(index = 111) {
                        ReducedMotionRow()
                        }
                    }
                }
            } else {
                item { StaggeredItem(index = 1) { SectionHeader(text = "Preferences") } }

                item {
                    StaggeredItem(index = 2) {
                    SettingsRow(icon=Icons.Outlined.SwapHoriz, title="Currency", subtitle=currentCurrency, onClick={ showCurrencyPicker = true })
                    }
                }

                item {
                    StaggeredItem(index = 3) {
                    SettingsRow(icon=Icons.Outlined.DarkMode, title="Mode", subtitle=modeLabels[currentThemeMode] ?: "System", onClick={ showModePicker = true })
                    }
                }

                item {
                    StaggeredItem(index = 4) {
                    AmoledToggleRow(
                        enabled = amoledMode,
                        isDarkAvailable = isEffectivelyDark,
                        onToggle = { viewModel.saveAmoledMode(it) }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 4) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Recurring Transactions", subtitle="Manage scheduled bills & income", onClick={ onOpenRecurringTransactions() })
                    }
                }

                item {
                    StaggeredItem(index = 5) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Manage Templates", subtitle="Create, edit & delete templates", onClick={ onOpenManageTemplates() })
                    }
                }

                item {
                    StaggeredItem(index = 6) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Manage Categories", subtitle="Merge or convert categories", onClick={ onOpenManageCategories() })
                    }
                }

                item {
                    StaggeredItem(index = 7) {
                    SettingsRow(icon=Icons.Outlined.SwapHoriz, title="Exchange Rates", subtitle="Set conversion rates for multi-currency", onClick={ onOpenExchangeRates() })
                    }
                }

                item {
                    StaggeredItem(index = 8) {
                    SettingsRow(icon=Icons.Outlined.EmojiEvents, title="Savings Goals", subtitle="Track and manage savings targets", onClick={ onOpenSavingsGoals() })
                    }
                }

                item {
                    StaggeredItem(index = 9) {
                    SmartCategorizationToggleRow(
                        enabled = smartCategorizationEnabled,
                        onToggle = { viewModel.saveSmartCategorizationEnabled(it) }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 9) {
                        HapticLevelRow(
                            selected = hapticLevel,
                            hapticManager = hapticManager,
                            onSelect = { viewModel.saveHapticLevel(it) }
                        )
                    }
                }

                item {
                    StaggeredItem(index = 10) {
                    AppLockToggleRow(
                        enabled = appLockEnabled,
                        onToggle = { viewModel.saveAppLockEnabled(it) }
                    )
                    }
                }

                if (BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED) item {
                    StaggeredItem(index = 11) {
                    MpesaAutoImportToggleRow(
                        enabled = mpesaAutoImportActive,
                        subtitle = if (mpesaAutoImportActive) "Detect M-PESA and ZiDi messages; review before saving" else "Optional · detects M-PESA and ZiDi messages for review",
                        onToggle = { enable ->
                            if (enable) showMpesaImportDisclosure = true
                            else {
                                viewModel.saveMpesaAutoImportEnabled(false)
                                viewModel.saveMpesaBackgroundLoggingEnabled(false)
                            }
                        }
                    )
                    }
                }

                if (BuildConfig.MPESA_SMS_AUTO_IMPORT_ENABLED && mpesaAutoImportActive) item {
                    StaggeredItem(index = 12) {
                        MpesaBackgroundLoggingToggleRow(
                            enabled = mpesaBackgroundLoggingEnabled,
                            onToggle = { enable ->
                                if (enable) showMpesaBackgroundLoggingDisclosure = true
                                else viewModel.saveMpesaBackgroundLoggingEnabled(false)
                            }
                        )
                    }
                }

                item {
                    StaggeredItem(index = 13) {
                    MpesaCostsToggleRow(
                        enabled = logMpesaCosts,
                        onToggle = { viewModel.saveLogMpesaCosts(it) }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 14) {
                        FinanceReviewReminderToggleRow(
                            enabled = financeReviewRemindersEnabled,
                            onToggle = onFinanceReviewReminderToggle
                        )
                    }
                }

                if (hasPromptedNotif && !isNotificationPermissionGranted(context)) {
                    item {
                        StaggeredItem(index = 11) {
                        NotificationEnableRow()
                        }
                    }
                }

                item { StaggeredItem(index = 12) { Spacer(modifier = Modifier.height(dimens.spacingXLarge)) } }
                item { StaggeredItem(index = 13) { SectionHeader(text = "Data") } }

                item {
                    StaggeredItem(index = 14) {
                    SettingsRow(
                        icon = Icons.Outlined.FileDownload,
                        title = stringResource(R.string.export_csv_title),
                        subtitle = stringResource(if (isExportingCsv) R.string.export_csv_preparing else R.string.export_csv_subtitle),
                        enabled = !isExportingCsv,
                        onClick = openCsvExport
                    )
                    }
                }

                item {
                    StaggeredItem(index = 15) {
                    SettingsRow(icon=Icons.Outlined.FileDownload, title="Export PDF", subtitle="Statement with date range", onClick={ showPdfExportDialog = true })
                    }
                }

                item {
                    StaggeredItem(index = 16) {
                        SettingsRow(
                            icon = Icons.Outlined.Info,
                            title = "Share diagnostic log",
                            subtitle = if (isExportingDiagnostics) "Preparing local event history" else "Troubleshooting events and balance checks; review before sharing",
                            enabled = !isExportingDiagnostics,
                            onClick = exportDiagnostics
                        )
                    }
                }

                item {
                    StaggeredItem(index = 17) {
                    SettingsRow(icon=Icons.Outlined.Save, title="Backup Now", subtitle="Save all data to a file", onClick={ onBackup() })
                    }
                }

                item {
                    StaggeredItem(index = 18) {
                    SettingsRow(icon=Icons.Outlined.RestorePage, title="Restore from Backup", subtitle="Replace all data from a file", onClick={ onRestore() })
                    }
                }

                item {
                    StaggeredItem(index = 19) {
                    ResetPersonalizationRow(
                        viewModel = viewModel,
                        showConfirm = showResetPersonalizationConfirm,
                        onShowConfirm = { showResetPersonalizationConfirm = true },
                        onDismissConfirm = { showResetPersonalizationConfirm = false }
                    )
                    }
                }

                item {
                    StaggeredItem(index = 20) {
                    SettingsRow(icon=Icons.Outlined.Delete, title="Clear All Data", subtitle="Reset everything", onClick={ showClearConfirm = true })
                    }
                }

                item { StaggeredItem(index = 20) { Spacer(modifier = Modifier.height(dimens.spacingXLarge)) } }
                item { StaggeredItem(index = 21) { SectionHeader(text = "About") } }

                item {
                    StaggeredItem(index = 22) {
                    SettingsRow(icon=Icons.Outlined.Info, title="Mindful Wallet", subtitle="Version ${BuildConfig.VERSION_NAME}")
                    }
                }
                if (BuildConfig.APPLICATION_ID == "com.makemission.makeflow.community" && !BuildConfig.DEBUG) item {
                    StaggeredItem(index = 23) {
                        SettingsRow(
                            icon = Icons.Outlined.FileDownload,
                            title = stringResource(R.string.update_check_title),
                            subtitle = stringResource(R.string.update_check_summary),
                            onClick = {
                                hapticManager?.fire(HapticEvent.NAV_SELECT)
                                onCheckForUpdates()
                            }
                        )
                    }
                }
                item {
                    StaggeredItem(index = 24) {
                    SettingsRow(icon=Icons.Outlined.Campaign, title="What's New", subtitle="Updates and feature guide", onClick = onOpenChangelog)
                    }
                }
                item {
                    StaggeredItem(index = 25) {
                        SettingsRow(icon=Icons.Outlined.Info, title="Permission setup", subtitle="Review SMS and notifications access", onClick = onOpenPermissionSetup)
                    }
                }
                item {
                    StaggeredItem(index = 26) {
                        SettingsRow(icon=Icons.Outlined.Lock, title="Privacy & data", subtitle="How your information is handled", onClick = onOpenPrivacyPolicy)
                    }
                }
                if (reducedMotion) {
                    item {
                        StaggeredItem(index = 26) {
                        ReducedMotionRow()
                        }
                    }
                }
            }
        }

        CollapsingTopBar(
            title = "Settings",
            scrollState = listState,
            modifier = Modifier.align(Alignment.TopStart),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }

    if (showMpesaImportDisclosure) {
        AdaptiveSheetDialog(onDismiss = { showMpesaImportDisclosure = false }) {
            AdaptiveSheetHeader(
                title = "M-PESA auto-import",
                subtitle = "Review each transaction before saving by default",
                onDismiss = { showMpesaImportDisclosure = false }
            )
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
                Text(
                    text = "When enabled, MakeFlow receives new SMS events, checks the sender ID first, and reads only messages from MPESA or ZIIDI whose transaction confirmation format is valid. Other senders are ignored. Your SMS inbox history is not read.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "The original message is not saved or sent off this device. By default, a matching transaction is passed to a private MakeFlow notification and opened in the existing review screen. Background logging is a separate opt-in in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Android will ask for SMS access and notification permission. You can turn detection off here at any time; the Android SMS permission can also be revoked in system settings. Manual entry and share import remain available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AdaptiveSheetPrimaryButton(
                    label = "Continue",
                    onClick = {
                        showMpesaImportDisclosure = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                            requestNotificationAccess()
                        } else {
                            receiveSmsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        }
                    }
                )
                TextButton(
                    onClick = { showMpesaImportDisclosure = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Not now")
                }
            }
        }
    }

    if (showMpesaBackgroundLoggingDisclosure) {
        AdaptiveSheetDialog(onDismiss = { showMpesaBackgroundLoggingDisclosure = false }) {
            AdaptiveSheetHeader(
                title = "Background transaction logging",
                subtitle = "Save confirmed transactions without opening the form",
                onDismiss = { showMpesaBackgroundLoggingDisclosure = false }
            )
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
                Text(
                    text = "MakeFlow logs M-PESA activity to the single KES account whose name includes M-PESA. ZiDi investments and withdrawals become transfers between that account and the account named ZiDi (or Ziidi). Direction comes from the confirmed sent, received, invested, or withdrawn wording. If accounts or direction are ambiguous, it opens the review flow instead. Automatically logged entries start uncategorized.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "When the message includes a closing balance, MakeFlow compares it with the expected balance for the named M-PESA or ZiDi account after the transaction and any logged fee. Differences are reported for you to review; account balances are not overwritten. Message bodies stay on this device and are not added to diagnostics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AdaptiveSheetPrimaryButton(
                    label = "Enable background logging",
                    onClick = {
                        showMpesaBackgroundLoggingDisclosure = false
                        viewModel.saveMpesaBackgroundLoggingEnabled(true)
                    }
                )
                TextButton(
                    onClick = { showMpesaBackgroundLoggingDisclosure = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Not now")
                }
            }
        }
    }

    if (showCurrencyPicker) {
        PickerSheet(
            title = "Select Currency",
            items = currencies.map { it.code },
            currentItem = currentCurrency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = {
                viewModel.saveCurrency(it)
                showCurrencyPicker = false
            }
        )
    }

    if (showModePicker) {
        PickerSheet(
            title = "Select Mode",
            items = modeOptions.map { modeLabels[it] ?: it },
            currentItem = modeLabels[currentThemeMode] ?: "System",
            onDismiss = { showModePicker = false },
            onSelect = { label ->
                val key = modeLabels.entries.find { it.value == label }?.key ?: "system"
                viewModel.saveThemeMode(key)
                showModePicker = false
            }
        )
    }

    if (showClearConfirm) {
        AdaptiveAlertSheet(
            onDismissRequest = { showClearConfirm = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Clear All Data?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will delete all transactions, budgets, accounts, and categories. This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearConfirm = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showResetPersonalizationConfirm) {
        AdaptiveAlertSheet(
            onDismissRequest = { showResetPersonalizationConfirm = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Reset Personalization?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will clear all correction history and reset the ML model to its default state. Your transactions and categories will not be affected.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPersonalization()
                    showResetPersonalizationConfirm = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPersonalizationConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showCsvExportSheet) {
        HistoryPeriodSheet(
            selectedPeriod = HistoryPeriod.ALL_TIME,
            calendarNow = csvCalendarNow,
            title = stringResource(R.string.export_csv_title),
            onDismiss = { showCsvExportSheet = false },
            onSelect = { period ->
                hapticManager?.fire(HapticEvent.NAV_SELECT)
                showCsvExportSheet = false
                val range = period.timestampRange(csvCalendarNow.toLocalDate(), csvCalendarNow.zone)
                viewModel.exportCsv(
                    context,
                    startMillis = range?.first ?: 0L,
                    endMillis = range?.last?.plus(1) ?: Long.MAX_VALUE,
                    onError = {
                        hapticManager?.fire(HapticEvent.VALIDATION_ERROR)
                        Toast.makeText(context, R.string.export_csv_error, Toast.LENGTH_LONG).show()
                    }
                ) { uri, fileName ->
                    shareCsvStatement(context, uri, fileName)
                }
            }
        )
    }

    if (showPdfExportDialog) {
        AdaptiveAlertSheet(
            onDismissRequest = { showPdfExportDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text("Export PDF Statement", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Select date range:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 12.dp))
                    PdfRangeOption("This Month", onClick = {
                        showPdfExportDialog = false
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val start = cal.timeInMillis
                        cal.add(Calendar.MONTH, 1)
                        val end = cal.timeInMillis
                        viewModel.exportPdf(
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
                    })
                    PdfRangeOption("All Time", onClick = {
                        showPdfExportDialog = false
                        viewModel.exportPdf(
                            context,
                            0L,
                            Long.MAX_VALUE,
                            onError = {
                                Toast.makeText(context, "Could not create the PDF statement", Toast.LENGTH_LONG).show()
                            }
                        ) { uri, fileName ->
                            Toast.makeText(context, "Statement ready to share", Toast.LENGTH_SHORT).show()
                            sharePdfStatement(context, uri, fileName)
                        }
                    })
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfExportDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    val dimens = LocalAppDimens.current
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(vertical = dimens.spacingMedium, horizontal = dimens.spacingXSmall)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val showLeadingIcons = LocalAdaptiveConfig.current.isTablet
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier.pressScale() else Modifier)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(vertical = dimens.spacingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (showLeadingIcons) dimens.spacingLarge else dimens.spacingSmall)
        ) {
            if (showLeadingIcons) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimens.iconSizeMedium)
                )
            }
            SettingText(title = title, subtitle = subtitle, modifier = Modifier.weight(1f))
        }
    }

}

@Composable
private fun SettingText(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    subtitleColor: Color? = null,
    titleWeight: FontWeight = FontWeight.Medium
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacingXSmall)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = titleWeight),
            maxLines = 1
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HapticLevelRow(
    selected: HapticStrength,
    hapticManager: HapticManager?,
    onSelect: (HapticStrength) -> Unit
) {
    val dimens = LocalAppDimens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacingSmall),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
    ) {
        SettingText(
            title = "Touch feedback",
            subtitle = "Choose how strong feedback feels",
            modifier = Modifier.fillMaxWidth(),
            titleWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)) {
            HapticStrength.entries.chunked(2).forEach { rowLevels ->
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall)) {
                    rowLevels.forEach { level ->
                        FilterChip(
                            selected = selected == level,
                            onClick = {
                                onSelect(level)
                                hapticManager?.strength = level
                                if (level != HapticStrength.OFF) hapticManager?.fire(HapticEvent.TOGGLE)
                            },
                            label = {
                                Text(
                                    when (level) {
                                        HapticStrength.OFF -> "Off"
                                        HapticStrength.SOFT -> "Soft"
                                        HapticStrength.STANDARD -> "Standard"
                                        HapticStrength.STRONG -> "Strong"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLockToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Lock with fingerprint",
            subtitle = "",
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun SmartCategorizationToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Smart categorization",
            subtitle = "",
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun MpesaAutoImportToggleRow(
    enabled: Boolean,
    subtitle: String,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "M-PESA auto-import",
            subtitle = subtitle,
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun MpesaBackgroundLoggingToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Background logging",
            subtitle = "Save new M-PESA and ZiDi transactions; check their balance alerts",
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun MpesaCostsToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Log M-PESA fees",
            subtitle = "",
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun ReducedMotionRow() {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Reduced motion active",
            subtitle = "Power saving mode",
            subtitleColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun NotificationEnableRow() {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openAppNotificationSettings(context) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Enable notifications",
            subtitle = "Notifications are disabled",
            subtitleColor = LocalMakeFlowColors.current.colorWarning
        )
    }
}

@Composable
private fun FinanceReviewReminderToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onToggle(!enabled) }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Weekly finance review",
            subtitle = "A gentle reminder to check in on your money",
            modifier = Modifier.weight(1f),
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun PdfRangeOption(label: String, onClick: () -> Unit) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.shapeCornerXSmall))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = dimens.textMl)
    }
}

private fun isNotificationPermissionGranted(context: android.content.Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            )
    } else {
        true
    }
}

@Composable
private fun ResetPersonalizationRow(
    viewModel: SettingsViewModel,
    showConfirm: Boolean,
    onShowConfirm: () -> Unit,
    onDismissConfirm: () -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onShowConfirm() }
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Reset personalization",
            subtitle = "Clear corrections & ML checkpoint",
            subtitleColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PickerSheet(
    title: String,
    items: List<String>,
    currentItem: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val dimens = LocalAppDimens.current
    AdaptiveAlertSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.shapeCornerXSmall))
                            .clickable { onSelect(item) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item, color = MaterialTheme.colorScheme.onSurface, fontSize = dimens.textMl)
                        if (item == currentItem) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = LocalMakeFlowColors.current.colorSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    )
}

@Composable
private fun AmoledToggleRow(
    enabled: Boolean,
    isDarkAvailable: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    val alpha = if (isDarkAvailable) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isDarkAvailable) Modifier.pressScale() else Modifier)
            .then(if (isDarkAvailable) Modifier.clickable { onToggle(!enabled) } else Modifier)
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingText(
            title = "Dark AMOLED mode",
            subtitle = "",
            modifier = Modifier.weight(1f),
            alpha = alpha,
            titleWeight = FontWeight.Bold
        )
        Switch(
            checked = enabled && isDarkAvailable,
            onCheckedChange = { if (isDarkAvailable) onToggle(it) },
            enabled = isDarkAvailable,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = LocalMakeFlowColors.current.colorSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledCheckedTrackColor = LocalMakeFlowColors.current.colorSuccess.copy(alpha = 0.5f),
                disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            )
        )
    }
}
