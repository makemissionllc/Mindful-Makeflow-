package com.makemission.makeflow.data.changelog

import com.makemission.makeflow.BuildConfig

data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>
)

val CURRENT_CHANGELOG_VERSION = BuildConfig.VERSION_NAME

val changelogEntries = listOf(
    ChangelogEntry(
        version = BuildConfig.VERSION_NAME,
        date = "2026-10-30",
        changes = listOf(
            "Optionally save confirmed M-PESA transactions in the background to a single M-PESA account",
            "Compare each SMS closing balance with the expected account balance and flag discrepancies without overwriting it",
            "Autosave unfinished full-screen transaction entries on this device and restore them when you return",
            "Use wallpaper-based Material You colors when following the system theme on Android 12 and newer",
            "Use the shared typography and spacing scale for the Home balance and account cards",
            "Suggest the usual existing account for an exact repeat transaction note, with a one-tap choice",
            "Use the same themed, accessible, reduced-motion-aware type selector in quick-add and full-screen transaction entry",
        )
    ),
    ChangelogEntry(
        version = "2.0.19-community",
        date = "2026-10-22",
        changes = listOf(
            "Hide or show the dashboard total balance with a saved privacy control",
            "Start the first transaction directly from History's empty state"
        )
    ),
    ChangelogEntry(
        version = "2.0.18-community",
        date = "2026-10-13",
        changes = listOf(
            "Quick-add widgets show and use the last account you selected",
            "Repeat the most recent income or expense from the expanded Quick Action widget with its amount, note, category, and account filled in",
            "Review a recent transaction before repeating it from the expanded widget on tablets",
            "Start each transaction with the account you used last time",
            "Choose accounts faster from a two-column picker on expanded tablets",
            "Choose frequent transaction categories directly on phones as well as tablets",
            "Explore every feature guide section in a hands-on, animated tour from Settings > What's New",
            "Navigate the feature tour in two panes on expanded tablets",
            "See the selected account's available balance while recording a transaction on expanded tablets",
            "See both account balances while setting up a transfer on expanded tablets",
            "See parser-imported M-PESA charges as distinct fee labels in History",
            "Show account-specific currencies and a clear selection state in account pickers",
            "Give the amount display a surfaced card and make the selected transaction type easier to scan",
            "Animate transaction type and account selection changes, respecting reduced-motion settings",
            "Let amount fields fit narrow layouts instead of forcing a fixed width",
            "Repair the version 13 to 14 database upgrade for older category tables",
            "Browse all major workflows and recent additions in the Settings feature guide",
        )
    ),
    ChangelogEntry(
        version = "2.0.14-community",
        date = "2026-10-12",
        changes = listOf(
            "Check GitHub Releases for Community updates and notify when a compatible APK is ready to install",
            "Apply saved transaction templates with their saved category and account restored",
            "Browse templates in a two-column quick-pick grid on expanded tablets",
            "Fix budget search resource references that blocked the Kotlin build",
            "Browse all major workflows and recent additions in the Settings feature guide"
        )
    ),
    ChangelogEntry(
        version = "2.0.13-community",
        date = "2026-10-12",
        changes = listOf(
            "Save and start another transaction while keeping the same type, account, and category",
            "Choose frequently used categories directly from the transaction form on expanded tablet layouts",
            "Find logged M-PESA charges with the new Fees filter in History",
            "See an imported M-PESA charge alongside its original transaction in tablet review, even when filters hide the fee row",
            "See linked M-PESA fees in History rows when fee logging is enabled",
            "Browse all major workflows and recent additions in the Settings feature guide",
        )
    ),
    ChangelogEntry(
        version = "2.0.11-community",
        date = "2026-10-12",
        changes = listOf(
            "Search budgets by custom name or category and clear the search in one tap",
            "See expanded tablet budgets in two columns in portrait and three in landscape",
        )
    ),
    ChangelogEntry(
        version = "2.0.10-community",
        date = "2026-10-12",
        changes = listOf(
            "Find accounts quickly by name or currency without changing total net worth",
            "View live account balances and recent transactions side by side on tablets",
            "Keep account selection after resizing, with transfers shown in each account's currency",
            "Clear account search in one tap, select activity notes and references to copy, and edit accounts from the tablet pane",
            "Browse a feature guide in Settings > What's New alongside versioned release notes"
        )
    ),
    ChangelogEntry(
        version = "2.0.9-community",
        date = "2026-10-09",
        changes = listOf(
            "Switch account focus directly in History without losing your search or date filters",
            "Review transactions side by side on tablets with Previous/Next controls and keyboard navigation",
            "Keep tablet details current after edits, with scrollable notes and selectable references"
        )
    ),
    ChangelogEntry(
        version = "2.0.8-community",
        date = "2026-10-09",
        changes = listOf(
            "Open a summary of the transactions matching your History search and filters",
            "See income, spending, and net change separately for each currency",
            "Keep transfers out of income and spending totals, with incomplete records clearly marked"
        )
    ),
    ChangelogEntry(
        version = "2.0.7-community",
        date = "2026-10-09",
        changes = listOf(
            "Keep editing or discard when leaving an unfinished transaction",
            "Protect quick-add entries from accidental close, back, and swipe-dismiss actions",
            "Open existing transactions without automatically replacing their recorded category"
        )
    ),
    ChangelogEntry(
        version = "2.0.6-community",
        date = "2026-10-09",
        changes = listOf(
            "Choose a date range for CSV exports and share the saved spreadsheet",
            "Export account and category names, currencies, transfer details, and M-PESA references",
            "More reliable CSV saving, with properly escaped notes and Android 9 support"
        )
    ),
    ChangelogEntry(
        version = "2.0.5-community",
        date = "2026-10-09",
        changes = listOf(
            "Filter History by today, the last 7 days, this month, or last month",
            "Combine date ranges with search, transaction type, and the selected account",
            "Clear search and filters together without changing the selected account"
        )
    ),
    ChangelogEntry(
        version = "2.0.4-community",
        date = "2026-10-08",
        changes = listOf(
            "Log a transaction again with its details filled in and today's date",
            "Repeated entries keep duplicate protection and use current transfer exchange rates"
        )
    ),
    ChangelogEntry(
        version = "2.0.3-community",
        date = "2026-10-08",
        changes = listOf(
            "Find transactions quickly by searching notes, amounts, accounts, categories, or references",
            "Clear an empty search and type filter in one tap",
            "Export readable, paginated PDF statements with friendly account and category names",
            "Save statements with a date-based filename and share them from the Android share menu"
        )
    ),
    ChangelogEntry(
        version = "2.0.1-community",
        date = "2026-10-08",
        changes = listOf(
            "A clear Community permission setup for M-PESA review and notifications",
            "More reliable Settings scrolling and haptic choices"
        )
    ),
    ChangelogEntry(
        version = "2.0.0",
        date = "2026-10-07",
        changes = listOf(
            "A four-step onboarding flow with an optional name and a skippable app tour",
            "A one-time offer to pin a Mindful widget after setup",
            "Optional weekly reminders for a finance check-in",
            "A direct link to the public privacy policy"
        )
    ),
    ChangelogEntry(
        version = "1.1.0",
        date = "2026-10-07",
        changes = listOf(
            "A simpler first setup: choose a currency and accounts, with an optional name",
            "Choose haptic feedback strength: off, soft, standard, or strong",
            "A clearer privacy view, with automatic device backup turned off",
            "M-PESA messages can still be shared or pasted for review; background SMS detection is not included in the Google Play build"
        )
    ),
    ChangelogEntry(
        version = "1.0",
        date = "2026-07-16",
        changes = listOf(
            "Track and categorize income & expenses across multiple accounts",
            "Recurring transactions for bills, subscriptions, and regular income",
            "Budget management with category-level spending limits and progress tracking",
            "Savings goals with feasibility guidance based on spending patterns",
            "M-PESA transaction import via share intent with auto-parsing",
            "Smart category suggestions that learn from your corrections over time",
            "Spending insights with interactive charts and configurable time ranges",
            "Multi-currency support: KES, USD, EUR, GBP, UGX, TZS",
            "Data backup and restore via JSON file export/import",
            "App lock with biometric / PIN authentication",
            "Home screen widget for quick account balance glance",
            "A unified ivory, sage, and chartreuse design with light, dark, and AMOLED modes",
            "Dark theme optimized for low-light use",
            "Privacy-first: all data stored securely on your device"
        )
    )
)
