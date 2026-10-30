package com.makemission.makeflow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.makemission.makeflow.core.format.CurrencyFormatter
import com.makemission.makeflow.core.device.LocalReducedMotion
import com.makemission.makeflow.core.haptics.HapticEvent
import com.makemission.makeflow.core.haptics.HapticManager
import com.makemission.makeflow.core.layout.DeviceLayoutMode
import com.makemission.makeflow.data.local.entity.TransactionEntity
import com.makemission.makeflow.ui.theme.ExpressiveShapes
import com.makemission.makeflow.ui.theme.LocalAppDimens
import com.makemission.makeflow.ui.theme.LocalExpressiveMotion
import com.makemission.makeflow.ui.theme.LocalMakeFlowColors
import com.makemission.makeflow.ui.theme.MindfulFontFamily
import com.makemission.makeflow.ui.theme.NavbarPillShape
import com.makemission.makeflow.ui.theme.OverlayTransition

@Composable
fun PrimaryDockContentInset(): androidx.compose.ui.unit.Dp {
    val dimens = LocalAppDimens.current
    val config = LocalAdaptiveConfig.current
    return if (config.layoutMode == DeviceLayoutMode.COMPACT) {
        dimens.navbarHeight + dimens.navbarFabSize + dimens.spacingSmall + dimens.navbarMarginBottom +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    } else {
        dimens.paddingLarge
    }
}

@Composable
fun FittedAmountText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start
) {
    val dimens = LocalAppDimens.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart
    ) {
        val availableWidth = with(density) { maxWidth.roundToPx() }
        val fontSize = listOf(
            dimens.textAmountLarge,
            dimens.textAmount,
            dimens.textDisplayLarge,
            dimens.textSmall
        ).firstOrNull { candidate ->
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontFamily = MindfulFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = candidate
                ),
                maxLines = 1
            ).size.width <= availableWidth
        } ?: dimens.textSmall

        Text(
            text = text,
            color = color,
            fontFamily = MindfulFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = 0.sp,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    hapticManager: HapticManager?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val dimens = LocalAppDimens.current
    Button(
        onClick = {
            hapticManager?.fire(HapticEvent.NAV_SELECT)
            onClick()
        },
        modifier = modifier.height(dimens.primaryButtonHeight),
        enabled = enabled,
        shape = NavbarPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = dimens.paddingLarge)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(dimens.iconSizeMedium))
            Spacer(Modifier.width(dimens.spacingSmall))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SoftButton(
    label: String,
    onClick: () -> Unit,
    hapticManager: HapticManager?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val dimens = LocalAppDimens.current
    Button(
        onClick = {
            hapticManager?.fire(HapticEvent.NAV_SELECT)
            onClick()
        },
        modifier = modifier.height(dimens.primaryButtonHeight),
        enabled = enabled,
        shape = NavbarPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = dimens.paddingLarge)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(dimens.iconSizeMedium))
            Spacer(Modifier.width(dimens.spacingSmall))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BigAmount(
    amount: Double,
    currencyCode: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    val formattedAmount = remember(amount, currencyCode) {
        CurrencyFormatter.format(amount, currencyCode)
    }

    FittedAmountText(text = formattedAmount, modifier = modifier, color = color)
}

@Composable
fun TransactionRow(
    title: String,
    subtitle: String,
    amount: Double,
    currencyCode: String,
    type: String,
    amountLabel: String? = null,
    onClick: (() -> Unit)? = null,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val amountColor = when (type) {
        "INCOME" -> LocalMakeFlowColors.current.colorSuccess
        "EXPENSE" -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val amountPrefix = when (type) {
        "INCOME" -> "+"
        "EXPENSE" -> "-"
        else -> ""
    }
    val resolvedModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable {
            hapticManager?.fire(HapticEvent.NAV_SELECT)
            onClick()
        }
    }

    Row(
        modifier = resolvedModifier
            .fillMaxWidth()
            .heightIn(min = dimens.transactionRowMinHeight)
            .padding(vertical = dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimens.avatarSize)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(dimens.spacingLarge))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(dimens.spacingSmall))
        Text(
            text = amountLabel ?: amountPrefix + CurrencyFormatter.formatCompact(amount, currencyCode),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = amountColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    title: String,
    subtitle: String,
    currencyCode: String,
    onClick: (() -> Unit)? = null,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) = TransactionRow(
    title = title,
    subtitle = subtitle,
    amount = transaction.amount,
    currencyCode = currencyCode,
    type = transaction.type,
    onClick = onClick,
    hapticManager = hapticManager,
    modifier = modifier
)

@Composable
fun InfoBanner(
    message: String,
    onClick: (() -> Unit)? = null,
    hapticManager: HapticManager? = null,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val clickModifier = if (onClick == null) modifier else modifier.clickable {
        hapticManager?.fire(HapticEvent.NAV_SELECT)
        onClick()
    }
    Row(
        modifier = clickModifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(LocalMakeFlowColors.current.warningBanner)
            .padding(horizontal = dimens.paddingMedium, vertical = dimens.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = if (emphasized) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            },
            color = LocalMakeFlowColors.current.onWarningBanner
        )
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val dimens = LocalAppDimens.current
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.progressTrackHeight)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

data class PillNavItem(val label: String, val icon: ImageVector)

@Composable
fun FloatingPillNav(
    items: List<PillNavItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    hapticManager: HapticManager,
    modifier: Modifier = Modifier,
    addContentDescription: String? = null
) {
    val dimens = LocalAppDimens.current
    val navContentColor = MaterialTheme.colorScheme.onSurface
    val navHeight = dimens.navbarHeight
    val fabSize = dimens.navbarFabSize
    val gap = dimens.spacingSmall
    val reducedMotion = LocalReducedMotion.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navHeight + fabSize + gap)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navHeight)
                .shadow(dimens.navbarElevation, shape = NavbarPillShape)
                .clip(NavbarPillShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(dimens.navbarPaddingHorizontal),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val iconScale = animateFloatAsState(
                    targetValue = if (selected && !reducedMotion) OverlayTransition.NavSelectedIconScale else 1f,
                    animationSpec = if (reducedMotion) snap() else OverlayTransition.navSelectionScale,
                    label = "navIconScale"
                ).value
                val selectedColor = animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                    animationSpec = if (reducedMotion) {
                        OverlayTransition.reducedColor
                    } else {
                        LocalExpressiveMotion.current.colorAnimationSpec
                    },
                    label = "navSelection"
                ).value
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(NavbarPillShape)
                        .background(selectedColor)
                        .clickable {
                            hapticManager.fire(HapticEvent.TAB_SWITCH)
                            onTabSelected(index)
                        }
                        .padding(vertical = dimens.spacingXSmall),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = navContentColor,
                        modifier = Modifier
                            .size(dimens.navbarItemSize)
                            .scale(iconScale)
                    )
                    Text(
                        text = item.label,
                        color = navContentColor,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {
                hapticManager.fire(HapticEvent.NAV_SELECT)
                onAddClick()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(fabSize),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = addContentDescription
                    ?: stringResource(com.makemission.makeflow.R.string.action_add_transaction),
                modifier = Modifier.size(dimens.iconSizeMedium)
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hapticManager: HapticManager,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) {
                hapticManager.fire(HapticEvent.TOGGLE)
                onCheckedChange(!checked)
            }
            .padding(vertical = dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.spacingXSmall)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (supportingText != null) {
                Text(supportingText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}
