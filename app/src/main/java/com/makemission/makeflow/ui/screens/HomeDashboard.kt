package com.makemission.makeflow.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import com.makemission.makeflow.core.layout.AdaptiveAlertSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.makemission.makeflow.R
import com.makemission.makeflow.core.device.LocalReducedMotion
import com.makemission.makeflow.core.haptics.HapticEvent
import com.makemission.makeflow.core.haptics.HapticManager
import com.makemission.makeflow.core.layout.DeviceLayoutMode
import com.makemission.makeflow.core.notifications.NotificationGate
import com.makemission.makeflow.data.RecurringPattern
import com.makemission.makeflow.data.local.entity.BudgetEntity
import com.makemission.makeflow.data.local.entity.RecurringTransactionEntity
import com.makemission.makeflow.data.local.entity.TransactionEntity
import com.makemission.makeflow.domain.CashFlowProjection
import com.makemission.makeflow.domain.DashboardCardType
import com.makemission.makeflow.domain.VelocityAlert
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.core.format.CurrencyFormatter
import com.makemission.makeflow.core.layout.AdaptiveSheetDialog
import com.makemission.makeflow.core.layout.AdaptiveSheetHeader
import com.makemission.makeflow.ui.components.AnimatedProgressBar
import com.makemission.makeflow.ui.components.FittedAmountText
import com.makemission.makeflow.ui.components.CollapsingTopBar
import com.makemission.makeflow.ui.components.InfoBanner
import com.makemission.makeflow.ui.components.StaggeredItem
import com.makemission.makeflow.ui.components.PrimaryButton
import com.makemission.makeflow.ui.components.ProgressBar
import com.makemission.makeflow.ui.components.PrimaryDockContentInset
import com.makemission.makeflow.ui.components.SoftButton
import com.makemission.makeflow.ui.components.TransactionRow
import com.makemission.makeflow.ui.components.LargeTitleItem
import com.makemission.makeflow.ui.components.LocalAdaptiveConfig
import com.makemission.makeflow.ui.theme.LocalAppDimens
import com.makemission.makeflow.ui.theme.LocalMakeFlowColors
import com.makemission.makeflow.ui.theme.OverlayTransition
import com.makemission.makeflow.ui.components.pressScale
import com.makemission.makeflow.ui.verticalFadingEdge
import com.makemission.makeflow.ui.viewmodel.FocusedAccountState
import com.makemission.makeflow.ui.viewmodel.HomeDashboardViewModel
import com.makemission.makeflow.domain.WeeklyBudgetStatus
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

data class AccountChip(
    val name: String,
    val balance: String,
    val color: Color
)

data class CategoryLegend(
    val name: String,
    val value: String,
    val color: Color
)

@Composable
fun HomeDashboard(
    viewModel: HomeDashboardViewModel,
    username: String = "",
    onOpenAccounts: () -> Unit = {},
    onOpenSavingsGoals: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenBudgets: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onAddTransaction: (String) -> Unit = {},
    onEditTransaction: (TransactionEntity) -> Unit = {},
    onImportMpesaMessage: (MpesaMessageParser.MpesaParsedMessage) -> Unit = {},
    hapticManager: HapticManager? = null,
    notificationGate: NotificationGate? = null
) {
    val dimens = LocalAppDimens.current
    val totalBalance by viewModel.totalBalance.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val focusedAccountReady by viewModel.focusedAccountReady.collectAsState()
    val focusedAccountId by viewModel.focusedAccountId.collectAsState()
    val focusedAccountState by viewModel.focusedAccountState.collectAsState()
    val balanceCurrencyCode by viewModel.balanceCurrencyCode.collectAsState()
    val dashboardBalanceHidden by viewModel.dashboardBalanceHidden.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val parentCategorySpending by viewModel.parentCategorySpending.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val weeklyBudgetStatus by viewModel.weeklyBudgetStatus.collectAsState()
    val recurringPatterns by viewModel.recurringPatterns.collectAsState()
    val pendingRecurringTransactions by viewModel.pendingRecurringTransactions.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cardOrder by viewModel.cardOrder.collectAsState()
    val cashFlowProjection by viewModel.cashFlowProjection.collectAsState()
    val velocityAlerts by viewModel.velocityAlerts.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showReorderSheet by remember { mutableStateOf(false) }
    var showAccountFocusSheet by remember { mutableStateOf(false) }
    var showMpesaImportDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val currentMonthLabel = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) }

    val config = LocalAdaptiveConfig.current
    val reducedMotion = LocalReducedMotion.current
    val dashboardItemFadeIn = if (reducedMotion) null else OverlayTransition.fastEnter
    val dashboardItemPlacement = if (reducedMotion) null else OverlayTransition.itemPlacementChange
    val dashboardItemFadeOut = if (reducedMotion) null else OverlayTransition.fastExit
    val isCompact = config.layoutMode == DeviceLayoutMode.COMPACT
    val horizontalPadding = if (isCompact) dimens.paddingLarge else config.horizontalPadding
    val focusedAccountName = when (val state = focusedAccountState) {
        is FocusedAccountState.Loading -> null
        is FocusedAccountState.ShowAll -> null
        is FocusedAccountState.Focused -> accounts.find { it.id == state.accountId }?.name
    }

    val formattedIncome = CurrencyFormatter.format(monthlyIncome, selectedCurrency)
    val formattedExpenses = CurrencyFormatter.format(monthlyExpenses, selectedCurrency)
    val formattedNet = CurrencyFormatter.format(monthlyIncome - monthlyExpenses, selectedCurrency)

    val accountChips = accounts.map { entity ->
        val color = try {
            Color(android.graphics.Color.parseColor(entity.colorHex))
        } catch (_: Exception) {
            MaterialTheme.colorScheme.primary
        }
        AccountChip(
            entity.name,
            CurrencyFormatter.format(entity.balance, entity.currencyCode ?: selectedCurrency),
            color
        )
    }

    val categoryLegends = parentCategorySpending.map { pcs ->
        val color = try {
            Color(android.graphics.Color.parseColor(pcs.color))
        } catch (_: Exception) {
            MaterialTheme.colorScheme.error
        }
        CategoryLegend(pcs.categoryName, CurrencyFormatter.format(pcs.amount, selectedCurrency), color)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
                .padding(bottom = if (isCompact) PrimaryDockContentInset() else 0.dp)
                .verticalFadingEdge(
                    topFadeHeight = config.fadingEdgeHeight,
                    bottomFadeHeight = config.fadingEdgeHeight
                ),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) dimens.spacingXLarge else config.cardSpacing),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                    if (isCompact) dimens.paddingLarge else config.barHeight,
                bottom = if (isCompact) dimens.paddingLarge else PrimaryDockContentInset()
            )
        ) {
            item {
                StaggeredItem(index = 0) {
                    HomeWelcomeHeader(
                        username = username,
                        onOpenSettings = onOpenSettings,
                        hapticManager = hapticManager,
                        compact = isCompact
                    )
                }
            }
            if (focusedAccountReady) {
                if (!isCompact) {
                    item {
                        AccountFocusSelector(
                            focusedAccountName = focusedAccountName,
                            onClick = {
                                hapticManager?.fire(HapticEvent.TAB_SWITCH)
                                showAccountFocusSheet = true
                            }
                        )
                    }
                }
                item {
                    StaggeredItem(index = 1) {
                        HomeBalanceHero(
                            balance = totalBalance,
                            currencyCode = balanceCurrencyCode,
                            balanceHidden = dashboardBalanceHidden,
                            onToggleBalanceVisibility = {
                                viewModel.setDashboardBalanceHidden(!dashboardBalanceHidden)
                            },
                            label = focusedAccountName ?: stringResource(R.string.home_total_balance),
                            supportingLabel = if (focusedAccountName == null) {
                                when (accounts.size) {
                                    0 -> stringResource(R.string.home_no_accounts)
                                    1 -> stringResource(R.string.home_across_one_account)
                                    else -> stringResource(R.string.home_across_accounts, accounts.size)
                                }
                            } else {
                                stringResource(R.string.home_focused_account)
                            },
                            showSupportingLabel = !isCompact,
                            canChangeFocus = isCompact && accounts.size > 1,
                            onChangeFocus = {
                                hapticManager?.fire(HapticEvent.TAB_SWITCH)
                                showAccountFocusSheet = true
                            },
                            onAddTransaction = onAddTransaction,
                            hapticManager = hapticManager
                        )
                    }
                }
                item {
                    StaggeredItem(index = 2) {
                        WeeklySpendingSummary(
                            status = weeklyBudgetStatus,
                            currencyCode = selectedCurrency,
                            onOpenBudgets = onOpenBudgets,
                            hapticManager = hapticManager
                        )
                    }
                }
                if (isCompact) {
                    item {
                        StaggeredItem(index = 3) {
                            InfoBanner(
                                message = stringResource(R.string.home_mpesa_import_banner),
                                onClick = { showMpesaImportDialog = true },
                                hapticManager = hapticManager,
                                emphasized = true
                            )
                        }
                    }
                }
                item {
                    StaggeredItem(index = 4) {
                        RecentActivitySection(
                            transactions = recentTransactions,
                            accounts = accounts,
                            categories = categories,
                            currencyCode = selectedCurrency,
                            onOpenHistory = onOpenHistory,
                            onEditTransaction = onEditTransaction,
                            hapticManager = hapticManager
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(dimens.shapeCornerMedium))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
            cardOrder.forEach { cardType ->
                when (cardType) {
                    DashboardCardType.INSIGHTS_SUMMARY -> item {
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = dashboardItemFadeIn,
                                placementSpec = dashboardItemPlacement,
                                fadeOutSpec = dashboardItemFadeOut
                            )
                        ) {
                            CashFlowCard(
                                income = formattedIncome,
                                expenses = formattedExpenses,
                                net = formattedNet,
                                monthLabel = currentMonthLabel,
                                onTap = { viewModel.logCardInteraction(DashboardCardType.INSIGHTS_SUMMARY) }
                            )
                        }
                    }
                    DashboardCardType.ACCOUNTS -> item {
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = dashboardItemFadeIn,
                                placementSpec = dashboardItemPlacement,
                                fadeOutSpec = dashboardItemFadeOut
                            )
                        ) {
                            AccountsCard(
                                accounts = accountChips,
                                onOpenAccounts = {
                                    viewModel.logCardInteraction(DashboardCardType.ACCOUNTS)
                                    onOpenAccounts()
                                },
                                onOpenSavingsGoals = onOpenSavingsGoals
                            )
                        }
                    }
                    DashboardCardType.BUDGET_PROGRESS -> if (budgets.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .animateItem(
                                        fadeInSpec = dashboardItemFadeIn,
                                        placementSpec = dashboardItemPlacement,
                                        fadeOutSpec = dashboardItemFadeOut
                                    )
                                    .clickable {
                                        viewModel.logCardInteraction(DashboardCardType.BUDGET_PROGRESS)
                                    }
                            ) {
                                BudgetsCard(budgets = budgets, currencyCode = selectedCurrency, hapticManager = hapticManager, notificationGate = notificationGate)
                            }
                        }
                    }
                    DashboardCardType.RECURRING -> {
                        if (pendingRecurringTransactions.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = dashboardItemFadeIn,
                                        placementSpec = dashboardItemPlacement,
                                        fadeOutSpec = dashboardItemFadeOut
                                    )
                                ) {
                                    PendingRecurringCard(
                                        items = pendingRecurringTransactions,
                                        currencyCode = selectedCurrency,
                                        onConfirm = { viewModel.confirmRecurringTransaction(it) },
                                        onSkip = { viewModel.skipRecurringTransaction(it) }
                                    )
                                }
                            }
                        }
                        if (recurringPatterns.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = dashboardItemFadeIn,
                                            placementSpec = dashboardItemPlacement,
                                            fadeOutSpec = dashboardItemFadeOut
                                        )
                                        .clickable {
                                            viewModel.logCardInteraction(DashboardCardType.RECURRING)
                                        }
                                ) {
                                    RecurringCard(patterns = recurringPatterns, currencyCode = selectedCurrency)
                                }
                            }
                        }
                    }
                }
            }
            if (categoryLegends.isNotEmpty()) item {
                CategoriesCard(categories = categoryLegends.take(5), monthLabel = currentMonthLabel)
            }
            if (velocityAlerts.isNotEmpty()) {
                item {
                    VelocityAlertsCard(
                        alerts = velocityAlerts,
                        currencyCode = selectedCurrency
                    )
                }
            }
            cashFlowProjection?.let { projection ->
                item {
                    ProjectionCard(projection = projection, currencyCode = selectedCurrency)
                }
            }
            item {
                Spacer(modifier = Modifier.height(dimens.spacingXSmall))
                Text(
                    text = "Edit cards",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReorderSheet = true }
                        .padding(vertical = dimens.spacingLarge),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (!isCompact) {
            CollapsingTopBar(
                title = "Home",
                scrollState = listState,
                modifier = Modifier.align(Alignment.TopStart),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        }
    }

    if (showMpesaImportDialog) {
        MpesaMessageImportSheet(
            onDismiss = { showMpesaImportDialog = false },
            onImport = { parsedMessage ->
                hapticManager?.fire(HapticEvent.NAV_SELECT)
                onImportMpesaMessage(parsedMessage)
                showMpesaImportDialog = false
            }
        )
    }

    if (showReorderSheet) {
        ReorderCardsSheet(
            currentOrder = cardOrder,
            onSave = { newOrder ->
                viewModel.saveManualCardOrder(newOrder)
            },
            onReset = {
                viewModel.clearManualCardOrder()
            },
            onDismiss = {
                showReorderSheet = false
            }
        )
    }

    if (showAccountFocusSheet) {
        AccountFocusSheet(
            accounts = accounts,
            focusedAccountId = focusedAccountId,
            onSelectAccount = { accountId ->
                hapticManager?.fire(HapticEvent.TAB_SWITCH)
                viewModel.setFocusedAccountId(accountId)
                showAccountFocusSheet = false
            },
            onDismiss = {
                showAccountFocusSheet = false
            }
        )
    }
}

@Composable
private fun MpesaMessageImportSheet(
    onDismiss: () -> Unit,
    onImport: (MpesaMessageParser.MpesaParsedMessage) -> Unit
) {
    var message by remember { mutableStateOf("") }
    val parsedMessage = remember(message) { MpesaMessageParser.parse(message) }
    val showParseError = message.isNotBlank() && parsedMessage == null

    AdaptiveAlertSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.home_mpesa_import_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacingMedium)) {
                Text(
                    text = stringResource(R.string.home_mpesa_import_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.home_mpesa_message_label)) },
                    minLines = 4,
                    maxLines = 7,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    isError = showParseError
                )
                if (showParseError) {
                    Text(
                        text = stringResource(R.string.home_mpesa_message_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedMessage?.let(onImport) },
                enabled = parsedMessage != null
            ) {
                Text(stringResource(R.string.home_mpesa_review_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_mpesa_cancel))
            }
        }
    )
}

@Composable
private fun HomeWelcomeHeader(
    username: String,
    onOpenSettings: () -> Unit,
    hapticManager: HapticManager?,
    compact: Boolean
) {
    val dimens = LocalAppDimens.current
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.home_greeting_morning)
        in 12..16 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }
    val today = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    val settingsLabel = stringResource(R.string.home_open_settings)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimens.avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .semantics { contentDescription = settingsLabel }
                .clickable(role = Role.Button) {
                    hapticManager?.fire(HapticEvent.NAV_SELECT)
                    onOpenSettings()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(dimens.iconSizeMedium)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)
        ) {
            Text(
                text = if (username.isBlank()) greeting else stringResource(
                    R.string.home_greeting_name,
                    greeting,
                    username.trim()
                ),
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                },
                color = if (compact) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Text(
                    text = stringResource(R.string.home_money_at_a_glance, today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HomeBalanceHero(
    balance: Double,
    currencyCode: String,
    balanceHidden: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    label: String,
    supportingLabel: String,
    showSupportingLabel: Boolean,
    canChangeFocus: Boolean,
    onChangeFocus: () -> Unit,
    onAddTransaction: (String) -> Unit,
    hapticManager: HapticManager?
) {
    val dimens = LocalAppDimens.current
    val reducedMotion = LocalReducedMotion.current
    val hiddenBalanceDescription = stringResource(R.string.home_balance_hidden)
    val visibilityActionDescription = stringResource(
        if (balanceHidden) R.string.home_show_balance else R.string.home_hide_balance
    )
    val displayedBalance = if (balanceHidden) {
        stringResource(R.string.home_balance_mask)
    } else {
        CurrencyFormatter.formatCompact(balance, currencyCode)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
    ) {
        Text(
            text = label,
            modifier = if (canChangeFocus) {
                Modifier.clickable(role = Role.Button, onClick = onChangeFocus)
            } else {
                Modifier
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
        ) {
            AnimatedContent(
                targetState = displayedBalance,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (reducedMotion) {
                        (fadeIn(OverlayTransition.reducedEnter) + slideInVertically(
                            animationSpec = OverlayTransition.reducedEnterSlide,
                            initialOffsetY = { (it * OverlayTransition.ValueShiftFraction).toInt() }
                        )) togetherWith (fadeOut(OverlayTransition.reducedExit) + slideOutVertically(
                            animationSpec = OverlayTransition.reducedExitSlide,
                            targetOffsetY = { -(it * OverlayTransition.ValueShiftFraction).toInt() }
                        ))
                    } else {
                        (fadeIn(OverlayTransition.enterFade) + slideInVertically(
                            animationSpec = OverlayTransition.enterSlide,
                            initialOffsetY = { (it * OverlayTransition.ValueShiftFraction).toInt() }
                        )) togetherWith (fadeOut(OverlayTransition.exitFade) + slideOutVertically(
                            animationSpec = OverlayTransition.exitSlide,
                            targetOffsetY = { -(it * OverlayTransition.ValueShiftFraction).toInt() }
                        ))
                    }
                },
                label = "homeBalance"
            ) { amount ->
                FittedAmountText(
                    text = amount,
                    modifier = if (balanceHidden) {
                        Modifier.semantics { contentDescription = hiddenBalanceDescription }
                    } else {
                        Modifier
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = {
                    hapticManager?.fire(HapticEvent.NAV_SELECT)
                    onToggleBalanceVisibility()
                },
                modifier = Modifier.semantics { contentDescription = visibilityActionDescription }
            ) {
                Icon(
                    imageVector = if (balanceHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.iconSizeMedium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showSupportingLabel) {
            Text(
                text = supportingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
            PrimaryButton(
                label = stringResource(R.string.home_action_add),
                onClick = { onAddTransaction("EXPENSE") },
                hapticManager = hapticManager
            )
            SoftButton(
                label = stringResource(R.string.home_action_transfer),
                onClick = { onAddTransaction("TRANSFER") },
                hapticManager = hapticManager
            )
        }
    }
}

@Composable
private fun WeeklySpendingSummary(
    status: WeeklyBudgetStatus,
    currencyCode: String,
    onOpenBudgets: () -> Unit,
    hapticManager: HapticManager?
) {
    val dimens = LocalAppDimens.current
    val clickModifier = if (status.hasBudget) {
        Modifier
    } else {
        Modifier.clickable(role = Role.Button) {
            hapticManager?.fire(HapticEvent.NAV_SELECT)
            onOpenBudgets()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(clickModifier)
            .padding(dimens.paddingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)
        ) {
            Text(
                text = stringResource(R.string.home_weekly_left),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (status.hasBudget) {
                    CurrencyFormatter.formatCompact(
                        (status.limit - status.spent).coerceAtLeast(0.0),
                        currencyCode
                    )
                } else {
                    stringResource(R.string.home_weekly_budget_setup)
                },
                style = if (status.hasBudget) {
                    MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!status.hasBudget) {
                Text(
                    text = stringResource(R.string.home_weekly_budget_setup_support),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (status.hasBudget) {
            AnimatedProgressBar(
                progress = if (status.limit > 0.0) (status.spent / status.limit).toFloat() else 0f,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .width(dimens.dashboardMeterWidth)
                    .height(dimens.progressTrackHeight)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    transactions: List<TransactionEntity>,
    accounts: List<com.makemission.makeflow.data.local.entity.AccountEntity>,
    categories: List<com.makemission.makeflow.data.local.entity.CategoryEntity>,
    currencyCode: String,
    onOpenHistory: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    hapticManager: HapticManager?
) {
    val dimens = LocalAppDimens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_recent),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (transactions.isNotEmpty()) {
                TextButton(onClick = {
                    hapticManager?.fire(HapticEvent.NAV_SELECT)
                    onOpenHistory()
                }) {
                    Text(stringResource(R.string.home_view_all))
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (transactions.isEmpty()) {
            Text(
                text = stringResource(R.string.home_recent_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = dimens.spacingMedium)
            )
        } else {
            val visibleTransactions = transactions.take(3)
            visibleTransactions.forEach { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                val account = accounts.find { it.id == transaction.accountId }
                val title = transaction.note
                    .takeIf { it.isNotBlank() }
                    ?: if (transaction.type == "TRANSFER") stringResource(R.string.home_transfer)
                    else category?.name ?: stringResource(R.string.home_uncategorized)
                val accountLabel = when (transaction.type) {
                    "TRANSFER" -> {
                        val from = accounts.find { it.id == transaction.fromAccountId }?.name
                        val to = accounts.find { it.id == transaction.toAccountId }?.name
                        listOfNotNull(from, to).joinToString(" to ").ifBlank { account?.name.orEmpty() }
                    }
                    else -> account?.name ?: ""
                }
                val fromAccount = accounts.find { it.id == transaction.fromAccountId }
                val toAccount = accounts.find { it.id == transaction.toAccountId }
                val transactionCurrency = if (transaction.type == "TRANSFER") {
                    fromAccount?.currencyCode ?: currencyCode
                } else account?.currencyCode ?: currencyCode
                val transferAmountLabel = if (transaction.type == "TRANSFER") {
                    "${CurrencyFormatter.formatCompact(transaction.amount, fromAccount?.currencyCode ?: currencyCode)} → " +
                        CurrencyFormatter.formatCompact(transaction.destinationAmount ?: transaction.amount, toAccount?.currencyCode ?: currencyCode)
                } else null
                val subtitle = listOfNotNull(
                    category?.name?.takeIf { it != title },
                    accountLabel.takeIf { it.isNotBlank() }
                )
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" · ")
                TransactionRow(
                    title = title,
                    subtitle = subtitle,
                    amount = transaction.amount,
                    currencyCode = transactionCurrency,
                    type = transaction.type,
                    amountLabel = transferAmountLabel,
                    onClick = { onEditTransaction(transaction) },
                    hapticManager = hapticManager
                )
            }
        }
    }
}

@Composable
fun TotalBalanceCard(
    balance: String = "KES 0.00",
    label: String = "Total balance",
    supportingLabel: String = "Across all accounts"
) {
    val dimens = LocalAppDimens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(dimens.cornerRadiusLarge))
            .padding(dimens.spacingXXLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.size(dimens.iconSizeMedium)
            )
        }

        Spacer(modifier = Modifier.height(dimens.spacingLarge))

        Text(
            text = balance,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(dimens.spacingMedium))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f), RoundedCornerShape(2.dp))
            )
            Text(
                text = supportingLabel,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AccountsCard(
    accounts: List<AccountChip> = emptyList(),
    onOpenAccounts: () -> Unit = {},
    onOpenSavingsGoals: () -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    CardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            val chevronInteractionSource = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "View all",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(20.dp)
                    .pressScale(interactionSource = chevronInteractionSource)
                    .clickable(
                        interactionSource = chevronInteractionSource,
                        indication = null
                    ) { onOpenAccounts() }
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSavingsGoals() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Savings Goals",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "View goals",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            accounts.forEach { account ->
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(dimens.cornerRadiusMedium))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(account.color, CircleShape)
                    )
                    Column {
                        Text(
                            text = account.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = account.balance,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowCard(
    income: String = "KES 0.00",
    expenses: String = "KES 0.00",
    net: String = "KES 0.00",
    monthLabel: String = "June 2026",
    onTap: (() -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    Box(
        modifier = if (onTap != null) Modifier.clickable { onTap() } else Modifier
    ) {
        CardContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Cash flow",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = monthLabel,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(dimens.spacingXLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(LocalMakeFlowColors.current.colorSuccess.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Income",
                            tint = LocalMakeFlowColors.current.colorSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Income",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = income,
                    color = LocalMakeFlowColors.current.colorSuccess,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Expenses",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Expenses",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = expenses,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(dimens.spacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total:",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = net,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoriesCard(
    categories: List<CategoryLegend> = emptyList(),
    monthLabel: String = "June 2026"
) {
    val dimens = LocalAppDimens.current
    CardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Categories",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = monthLabel,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(dimens.spacingXLarge))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXXLarge)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expense",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = dimens.textS,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(cat.color, CircleShape)
                            )
                            Text(
                                text = cat.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = cat.value,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetsCard(
    budgets: List<BudgetEntity> = emptyList(),
    currencyCode: String = "KES",
    hapticManager: HapticManager? = null,
    notificationGate: NotificationGate? = null
) {
    val dimens = LocalAppDimens.current
    val previousBudgetZones = remember { mutableMapOf<String, String>() }
    LaunchedEffect(budgets) {
        budgets.forEach { budget ->
            val progress = (budget.spentAmount / budget.amount).toFloat().coerceIn(0f, 1f)
            val currentZone = when {
                progress >= 1f -> "red"
                progress >= 0.7f -> "orange"
                else -> "green"
            }
            val prevZone = previousBudgetZones[budget.id]
            if (prevZone != null && prevZone != currentZone) {
                when (currentZone) {
                    "orange" -> hapticManager?.fire(HapticEvent.BUDGET_WARNING)
                    "red" -> {
                        hapticManager?.fire(HapticEvent.BUDGET_EXCEEDED)
                        notificationGate?.dispatchOrQueue(
                            type = "budget_alert",
                            title = "Budget exceeded",
                            body = "${budget.displayName} has exceeded its limit"
                        )
                    }
                }
            }
            previousBudgetZones[budget.id] = currentZone
        }
    }
    CardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budgets",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "View all",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (budgets.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No active budgets",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        } else {
            budgets.take(3).forEach { budget ->
                val progress = (budget.spentAmount / budget.amount).toFloat().coerceIn(0f, 1f)
                val progressColor = when {
                    progress >= 1f -> MaterialTheme.colorScheme.error
                    progress >= 0.7f -> LocalMakeFlowColors.current.colorWarning
                    else -> LocalMakeFlowColors.current.colorSuccess
                }

                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = budget.displayName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${CurrencyFormatter.format(budget.spentAmount, currencyCode)} / ${CurrencyFormatter.format(budget.amount, currencyCode)}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = dimens.textS
                        )
                    }

                    AnimatedProgressBar(
                        progress = progress,
                        color = progressColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(dimens.spacingXSmall))
    }
}

@Composable
fun RecurringCard(
    patterns: List<RecurringPattern>,
    currencyCode: String
) {
    val dimens = LocalAppDimens.current
    CardContainer {
        Text(
            text = "Upcoming",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(14.dp))
        patterns.take(5).forEach { pattern ->
            val titleCase = pattern.normalizedDescription
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            val relativeDate = getRelativeDateLabel(pattern.nextExpectedDate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = titleCase,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (pattern.confidence < 0.6f) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "estimated",
                                color = LocalMakeFlowColors.current.colorWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    Text(
                        text = CurrencyFormatter.format(pattern.avgAmount, currencyCode),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = relativeDate,
                    color = if (relativeDate == "Tomorrow") LocalMakeFlowColors.current.colorWarning else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (pattern != patterns.take(5).last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

private fun getRelativeDateLabel(nextExpectedDate: Long): String {
    val now = System.currentTimeMillis()
    val diffDays = ((nextExpectedDate - now) / (1000.0 * 60 * 60 * 24)).roundToInt()
    return when {
        diffDays <= 0 -> "Today"
        diffDays == 1 -> "Tomorrow"
        diffDays <= 7 -> "In $diffDays days"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            sdf.format(java.util.Date(nextExpectedDate))
        }
    }
}

@Composable
fun PendingRecurringCard(
    items: List<RecurringTransactionEntity>,
    currencyCode: String,
    onConfirm: (RecurringTransactionEntity) -> Unit,
    onSkip: (RecurringTransactionEntity) -> Unit
) {
    val dimens = LocalAppDimens.current
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    CardContainer {
        Text(
            text = "Pending transactions",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        items.take(3).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${CurrencyFormatter.format(item.amount, currencyCode)} \u00B7 Due ${dateFormat.format(Date(item.nextDueDate))}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = dimens.textS
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall)) {
                    Text(
                        text = "Confirm",
                        color = LocalMakeFlowColors.current.colorSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimens.shapeCornerXSmall))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { onConfirm(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Text(
                        text = "Skip",
                        color = LocalMakeFlowColors.current.colorWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimens.shapeCornerXSmall))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { onSkip(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            if (item != items.take(3).last()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun CardContainer(content: @Composable () -> Unit) {
    val dimens = LocalAppDimens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(dimens.cornerRadiusLarge))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun VelocityAlertsCard(
    alerts: List<VelocityAlert>,
    currencyCode: String
) {
    val dimens = LocalAppDimens.current
    CardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = LocalMakeFlowColors.current.colorWarning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Spending velocity",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(dimens.spacingLarge))
        alerts.forEach { alert ->
            val severityColor = when (alert.severity) {
                com.makemission.makeflow.domain.VelocitySeverity.CRITICAL -> MaterialTheme.colorScheme.error
                com.makemission.makeflow.domain.VelocitySeverity.HIGH -> MaterialTheme.colorScheme.error
                com.makemission.makeflow.domain.VelocitySeverity.ELEVATED -> LocalMakeFlowColors.current.colorWarning
                com.makemission.makeflow.domain.VelocitySeverity.NONE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
            val formattedPct = "${(alert.percentAhead * 100).toInt()}%"
            val projectedFormatted = CurrencyFormatter.format(alert.projectedMonthEndAmount, currencyCode)
            val actualFormatted = CurrencyFormatter.format(alert.actualCumulativeSpend, currencyCode)
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.categoryName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$formattedPct ahead",
                        color = severityColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$actualFormatted spent \u00B7 at this pace: $projectedFormatted by month end",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = dimens.textS
                )
            }
            if (alert != alerts.last()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ProjectionCard(
    projection: CashFlowProjection,
    currencyCode: String
) {
    val dimens = LocalAppDimens.current
    val dateFormat = remember { SimpleDateFormat("d MMMM", Locale.getDefault()) }
    val endOfMonth = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, projection.daysRemainingInMonth + java.time.LocalDate.now().dayOfMonth)
        dateFormat.format(cal.time)
    }
    val isNegative = projection.projectedEndOfMonthBalance < 0
    val formattedProjected = CurrencyFormatter.format(projection.projectedEndOfMonthBalance, currencyCode)

    CardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isNegative) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Projected balance",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = dimens.textS,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(dimens.spacingXSmall))
                Text(
                    text = "At this pace, you'll have $formattedProjected by $endOfMonth",
                    color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = if (isNegative) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AccountFocusSelector(
    focusedAccountName: String?,
    onClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadiusMedium))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = focusedAccountName ?: "All Accounts",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "Change account",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AccountFocusSheet(
    accounts: List<com.makemission.makeflow.data.local.entity.AccountEntity>,
    focusedAccountId: String?,
    onSelectAccount: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val dimens = LocalAppDimens.current
    AdaptiveSheetDialog(onDismiss = onDismiss) {
        AdaptiveSheetHeader(title = stringResource(R.string.history_choose_account), onDismiss = onDismiss)
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
        ) {
            val options = listOf(null to stringResource(R.string.history_all_accounts)) +
                accounts.map { it.id to it.name }
            options.forEach { (accountId, name) ->
                val isSelected = focusedAccountId == accountId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.shapeCornerSmall))
                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelectAccount(accountId) })
                        .padding(horizontal = dimens.paddingMedium, vertical = dimens.spacingLarge),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    RadioButton(selected = isSelected, onClick = null)
                }
            }
        }
    }
}
