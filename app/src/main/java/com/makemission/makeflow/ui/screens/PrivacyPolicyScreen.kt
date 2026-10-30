package com.makemission.makeflow.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.makemission.makeflow.BuildConfig
import com.makemission.makeflow.ui.components.CollapsingTopBar
import com.makemission.makeflow.ui.theme.LocalAppDimens

private data class PrivacyPolicySection(val title: String, val body: String)

private val privacyPolicySections = listOf(
    PrivacyPolicySection(
        "At a glance",
        "Mindful Wallet keeps your finance records in private app storage on this device. It does not create an online account or sync your transactions to a MakeFlow server. Automatic Android backup is disabled."
    ),
    PrivacyPolicySection(
        "Information stored on your device",
        "The app stores the name you choose, account names and balances, transactions, categories, budgets, savings goals, recurring items, exchange-rate preferences, and app settings. These records are kept locally using Android app storage."
    ),
    PrivacyPolicySection(
        "Online exchange rates",
        "When exchange rates are refreshed, Mindful requests public reference rates from api.frankfurter.dev using currency codes. It does not send your account names, balances, transaction amounts, notes, or history in that request. The rate service may process ordinary connection data, such as an IP address, under its own privacy practices."
    ),
    PrivacyPolicySection(
        "Optional on-device assistance",
        "On supported devices, optional shared-message classification and monthly reflections use Gemini Nano through Android's on-device ML Kit Prompt API. Shared text or a finance summary is processed on the device for that request; the app does not save the generated reflection. Suggestions remain yours to review."
    ),
    PrivacyPolicySection(
        "Permissions and imports",
        "Optional weekly finance reminders use Android notifications and can be turned off in Settings. Biometric authentication can protect app access, and vibration is used for touch feedback. The Google Play build does not request SMS access. The separately labeled Community build can optionally process new M-PESA SMS events after you grant permission. It checks for the M-PESA sender and a valid confirmation, does not read inbox history, and does not save or send the original message off this device. Matching transactions go to review by default. A separate background-logging opt-in can save eligible transactions automatically to one clear KES M-PESA account; ambiguous cases still go to review. Share and paste import are also available."
    ),
    PrivacyPolicySection(
        "Backups and deleting data",
        "Android cloud backup and device-to-device transfer are disabled for app data. JSON backups are saved to a location you select; CSV files and PDF statements are saved in Downloads/MakeFlow on Android 10 and newer, and in app Documents storage on Android 9. You can share a statement with an app you choose. A file provider or cloud-synced Downloads location may have its own privacy practices. Use Settings > Clear All Data to remove app records, or clear the app's storage or uninstall it. Any exported files must be deleted separately."
    ),
    PrivacyPolicySection(
        "Contact and updates",
        "Mindful Wallet does not include advertising or analytics SDKs. This policy was last updated on 30 October 2026 for app version ${BuildConfig.VERSION_NAME}. For privacy questions, contact the developer using the email shown on the Mindful Wallet Google Play listing."
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val dimens = LocalAppDimens.current
    val listState = rememberLazyListState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = dimens.paddingLarge,
                end = dimens.paddingLarge,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + dimens.spacingXLarge,
                bottom = dimens.paddingExtra
            ),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXLarge)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)) {
                    Text(
                        text = "Mindful Wallet",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last updated 7 October 2026 · Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(privacyPolicySections) { section ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                TextButton(
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://makemission.gt.tc/privacy"))
                            )
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "No browser is available to open the privacy policy.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open online privacy policy")
                }
            }
        }

        CollapsingTopBar(
            title = "Privacy & data",
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
}
