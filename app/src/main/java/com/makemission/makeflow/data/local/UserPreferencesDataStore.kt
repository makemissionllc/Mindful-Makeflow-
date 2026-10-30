package com.makemission.makeflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.makemission.makeflow.core.haptics.HapticStrength
import com.makemission.makeflow.domain.TransactionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userPrefDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    val selectedCurrency: Flow<String> = dataStore.data.map { prefs ->
        prefs[SELECTED_CURRENCY_KEY] ?: "KES"
    }

    suspend fun saveCurrency(currency: String) {
        dataStore.edit { prefs ->
            prefs[SELECTED_CURRENCY_KEY] = currency
        }
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[PREF_USER_NAME] ?: ""
    }

    val isProfiled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PREF_IS_PROFILED] ?: false
    }

    suspend fun saveName(name: String) {
        dataStore.edit { prefs ->
            prefs[PREF_USER_NAME] = name
        }
    }

    suspend fun setProfiled() {
        dataStore.edit { prefs ->
            prefs[PREF_IS_PROFILED] = true
        }
    }

    val shouldOfferHomeWidget: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[OFFER_HOME_WIDGET_KEY] ?: false
    }

    suspend fun setShouldOfferHomeWidget(shouldOffer: Boolean) {
        dataStore.edit { prefs ->
            prefs[OFFER_HOME_WIDGET_KEY] = shouldOffer
        }
    }

    val communityPermissionSetupCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[COMMUNITY_PERMISSION_SETUP_COMPLETED_KEY] ?: false
    }

    suspend fun setCommunityPermissionSetupCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[COMMUNITY_PERMISSION_SETUP_COMPLETED_KEY] = completed
        }
    }

    val financeReviewRemindersEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[FINANCE_REVIEW_REMINDERS_ENABLED_KEY] ?: false
    }

    suspend fun saveFinanceReviewRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[FINANCE_REVIEW_REMINDERS_ENABLED_KEY] = enabled
        }
    }

    val hapticLevel: Flow<String> = dataStore.data.map { prefs ->
        val stored = prefs[HAPTIC_LEVEL_KEY]
        HapticStrength.fromStorage(stored ?: if (prefs[HAPTICS_ENABLED_KEY] == false) "off" else null)
            .storageValue
    }

    val hapticsEnabled: Flow<Boolean> = hapticLevel.map { it != HapticStrength.OFF.storageValue }

    suspend fun saveHapticLevel(level: String) {
        val normalized = HapticStrength.fromStorage(level)
        dataStore.edit { prefs ->
            prefs[HAPTIC_LEVEL_KEY] = normalized.storageValue
            prefs[HAPTICS_ENABLED_KEY] = normalized != HapticStrength.OFF
        }
    }

    suspend fun saveHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[HAPTICS_ENABLED_KEY] = enabled
            val current = HapticStrength.fromStorage(prefs[HAPTIC_LEVEL_KEY])
            val next = if (!enabled) HapticStrength.OFF else current.takeUnless { it == HapticStrength.OFF } ?: HapticStrength.STANDARD
            prefs[HAPTIC_LEVEL_KEY] = next.storageValue
        }
    }

    val smartCategorizationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SMART_CATEGORIZATION_KEY] ?: true
    }

    suspend fun saveSmartCategorizationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SMART_CATEGORIZATION_KEY] = enabled
        }
    }

    val dismissedSuggestions: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[DISMISSED_SUGGESTIONS_KEY]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    suspend fun dismissSuggestion(key: String) {
        dataStore.edit { prefs ->
            val current = prefs[DISMISSED_SUGGESTIONS_KEY]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            prefs[DISMISSED_SUGGESTIONS_KEY] = (current + key).joinToString(",")
        }
    }

    val appLockEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[APP_LOCK_ENABLED_KEY] ?: false
    }

    suspend fun saveAppLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[APP_LOCK_ENABLED_KEY] = enabled
        }
    }

    val hasPromptedNotificationPermission: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAS_PROMPTED_NOTIFICATION_PERMISSION] ?: false
    }

    suspend fun setHasPromptedNotificationPermission() {
        dataStore.edit { prefs ->
            prefs[HAS_PROMPTED_NOTIFICATION_PERMISSION] = true
        }
    }

    val manualCardOrder: Flow<List<String>?> = dataStore.data.map { prefs ->
        prefs[MANUAL_CARD_ORDER_KEY]?.split(",")?.filter { it.isNotBlank() }
    }

    suspend fun saveManualCardOrder(order: List<String>) {
        dataStore.edit { prefs ->
            prefs[MANUAL_CARD_ORDER_KEY] = order.joinToString(",")
        }
    }

    suspend fun clearManualCardOrder() {
        dataStore.edit { prefs ->
            prefs.remove(MANUAL_CARD_ORDER_KEY)
        }
    }

    val focusedAccountId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[FOCUSED_ACCOUNT_ID_KEY]
    }

    suspend fun saveFocusedAccountId(accountId: String?) {
        dataStore.edit { prefs ->
            if (accountId != null) {
                prefs[FOCUSED_ACCOUNT_ID_KEY] = accountId
            } else {
                prefs.remove(FOCUSED_ACCOUNT_ID_KEY)
            }
        }
    }

    val lastTransactionAccountId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_TRANSACTION_ACCOUNT_KEY]
    }

    val dashboardBalanceHidden: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DASHBOARD_BALANCE_HIDDEN_KEY] ?: false
    }

    suspend fun saveDashboardBalanceHidden(hidden: Boolean) {
        dataStore.edit { prefs ->
            prefs[DASHBOARD_BALANCE_HIDDEN_KEY] = hidden
        }
    }

    suspend fun saveLastTransactionAccountId(accountId: String?) {
        dataStore.edit { prefs ->
            if (accountId.isNullOrBlank()) {
                prefs.remove(LAST_TRANSACTION_ACCOUNT_KEY)
            } else {
                prefs[LAST_TRANSACTION_ACCOUNT_KEY] = accountId
            }
        }
    }

    suspend fun getTransactionDraft(): TransactionDraft? = dataStore.data
        .map { prefs -> TransactionDraft.decode(prefs[TRANSACTION_DRAFT_KEY]) }
        .first()

    suspend fun saveTransactionDraft(draft: TransactionDraft?) {
        dataStore.edit { prefs ->
            if (draft == null) {
                prefs.remove(TRANSACTION_DRAFT_KEY)
            } else {
                prefs[TRANSACTION_DRAFT_KEY] = draft.encode()
            }
        }
    }

    suspend fun clearProfile() {
        dataStore.edit { prefs ->
            prefs.remove(PREF_USER_NAME)
            prefs.remove(PREF_IS_PROFILED)
        }
    }

    val lastSeenChangelogVersion: Flow<String> = dataStore.data.map { prefs ->
        prefs[LAST_SEEN_CHANGELOG_KEY] ?: ""
    }

    suspend fun saveLastSeenChangelogVersion(version: String) {
        dataStore.edit { prefs ->
            prefs[LAST_SEEN_CHANGELOG_KEY] = version
        }
    }

    val logMpesaCosts: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[LOG_MPESA_COSTS_KEY] ?: true
    }

    suspend fun saveLogMpesaCosts(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[LOG_MPESA_COSTS_KEY] = enabled
        }
    }

    val mpesaAutoImportEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[MPESA_AUTO_IMPORT_ENABLED_KEY] ?: false
    }

    suspend fun saveMpesaAutoImportEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[MPESA_AUTO_IMPORT_ENABLED_KEY] = enabled
            if (!enabled) prefs[MPESA_BACKGROUND_LOGGING_ENABLED_KEY] = false
        }
    }

    val mpesaBackgroundLoggingEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[MPESA_BACKGROUND_LOGGING_ENABLED_KEY] ?: false
    }

    suspend fun saveMpesaBackgroundLoggingEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[MPESA_BACKGROUND_LOGGING_ENABLED_KEY] = enabled
        }
    }

    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY] ?: "system"
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode
        }
    }

    val amoledMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AMOLED_MODE_KEY] ?: false
    }

    suspend fun saveAmoledMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AMOLED_MODE_KEY] = enabled
        }
    }

    companion object {
        private val SELECTED_CURRENCY_KEY = stringPreferencesKey("selected_currency")
        private val PREF_USER_NAME = stringPreferencesKey("user_name")
        private val PREF_IS_PROFILED = booleanPreferencesKey("is_profiled")
        private val OFFER_HOME_WIDGET_KEY = booleanPreferencesKey("offer_home_widget")
        private val COMMUNITY_PERMISSION_SETUP_COMPLETED_KEY = booleanPreferencesKey("community_permission_setup_completed")
        private val FINANCE_REVIEW_REMINDERS_ENABLED_KEY = booleanPreferencesKey("finance_review_reminders_enabled")
        private val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")
        private val HAPTIC_LEVEL_KEY = stringPreferencesKey("haptic_level")
        private val DISMISSED_SUGGESTIONS_KEY = stringPreferencesKey("dismissed_suggestions")
        private val HAS_PROMPTED_NOTIFICATION_PERMISSION = booleanPreferencesKey("has_prompted_notification_permission")
        private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        private val MANUAL_CARD_ORDER_KEY = stringPreferencesKey("manual_card_order")
        private val SMART_CATEGORIZATION_KEY = booleanPreferencesKey("smart_categorization")
        private val FOCUSED_ACCOUNT_ID_KEY = stringPreferencesKey("focused_account_id")
        private val LAST_TRANSACTION_ACCOUNT_KEY = stringPreferencesKey("last_transaction_account_id")
        private val DASHBOARD_BALANCE_HIDDEN_KEY = booleanPreferencesKey("dashboard_balance_hidden")
        private val LAST_SEEN_CHANGELOG_KEY = stringPreferencesKey("last_seen_changelog_version")
        private val LOG_MPESA_COSTS_KEY = booleanPreferencesKey("log_mpesa_costs")
        private val MPESA_AUTO_IMPORT_ENABLED_KEY = booleanPreferencesKey("mpesa_auto_import_enabled")
        private val MPESA_BACKGROUND_LOGGING_ENABLED_KEY = booleanPreferencesKey("mpesa_background_logging_enabled")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val AMOLED_MODE_KEY = booleanPreferencesKey("amoled_mode")
        private val TRANSACTION_DRAFT_KEY = stringPreferencesKey("transaction_draft")
    }
}
