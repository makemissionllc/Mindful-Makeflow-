package com.makemission.makeflow.data.changelog

data class FeatureGuideEntry(val title: String, val features: List<String>)

val featureGuideEntries = listOf(
    FeatureGuideEntry("Accounts and currencies", listOf(
        "Create and edit accounts with individual currencies and opening balances. Search by name or currency code; clearing search restores the list without changing net worth.",
        "Net worth converts balances to your default currency using available daily reference rates. Accounts with missing conversions are disclosed and excluded.",
        "New transactions start with the account used last time. If that account no longer exists, entry falls back to the first available account."
    )),
    FeatureGuideEntry("Transaction entry", listOf(
        "Record income, expenses, and transfers with the calculator, category, account, note, and date controls. Duplicate and unusual-amount checks help review entries.",
        "Use Save & add another to clear the amount, note, and date while keeping the type, account, and category ready for the next entry. Duplicate checks still apply.",
        "Unfinished full-screen transaction entries autosave privately on this device and can be recovered after an unexpected app close or restart. Choosing Discard removes the draft.",
        "Expanded transaction entry keeps the calculator and editing controls beside a live review of the amount, account, category, note, and date.",
        "For an exact repeated note, the form can suggest the most-used account from recent categorized entries. Choose the suggestion to apply it; ties, deleted accounts, and transfers do not trigger a suggestion.",
        "The surfaced amount display and highlighted income, expense, or transfer choice make the current entry easier to scan before saving.",
        "Transaction type and account selection colors transition smoothly and respect reduced-motion settings. Amount fields adapt to the available width in quick-add, budget, and recurring-payment forms.",
        "Applying a saved template restores its saved amount, type, account, category, and note.",
        "Use Log again in the editor to reuse an entry with today's date. Transfers use current available rates. Leaving an unfinished editor or quick-add form offers Keep editing or Discard."
    )),
    FeatureGuideEntry("History and summaries", listOf(
        "Search notes, amounts, accounts, categories, and references. Combine search with date presets, type, and account focus. Account focus is shared with Home; choose All accounts to clear it.",
        "When History has no records yet, add the first transaction from the empty state. Use Fees to find parser-imported M-PESA charges. With Log M-PESA fees enabled, History links each charge to its original transaction. Open Summary for matching income, spending, and net change grouped by currency. Transfers and incomplete records are disclosed separately. Swipe an entry to delete it; the brief Undo action restores it."
    )),
    FeatureGuideEntry("Tablet workspaces", listOf(
        "In expanded History, select an entry for side-by-side details. Previous and Next review filtered results; Page Up and Page Down work while the details pane is focused.",
        "In expanded Accounts, select an account for its live balance and latest 20 transactions. Transfer in and Transfer out use that account's own amount; unavailable amounts are labelled.",
        "Select accounts from a two-column grid on expanded tablets, with each account's currency and balance visible on its card.",
        "The expanded Quick Action widget on tablets includes a Repeat last transaction action, with the recent amount and note shown before you open the prefilled form.",
        "Choose frequently used categories from the transaction form without opening the category picker on phones or tablets.",
        "Expanded transaction forms show saved templates in a two-column grid with their amount and account currency.",
        "Transaction entry keeps calculator controls on one side and a live amount, account, category, note, date, and selected account's available balance on the other. Transfers include both account balances and the estimated destination amount.",
        "Settings > What's New opens an interactive tour. Expanded tablets show a feature list beside animated step details; phones use Previous and Next controls.",
        "In expanded History, an M-PESA transaction shows its logged fee beside the original amount, even when the current search or filters hide the separate fee entry.",
        "Scroll details independently, select notes and references to copy, and open Edit from the pane. Selection survives resizing and saved-state restoration."
    )),
    FeatureGuideEntry("Budgets and savings goals", listOf(
        "Search budgets by custom name or category, track spending progress, and review suggestions and spending alerts. Clear search to restore every budget.",
        "Create savings goals, link accounts, and review feasibility estimates and spending trade-offs. Estimates depend on your recorded activity."
    )),
    FeatureGuideEntry("Tablet budget workspace", listOf(
        "Expanded tablet layouts show budgets in two columns in portrait and three in landscape, so more limits stay visible together."
    )),
    FeatureGuideEntry("Recurring payments and reminders", listOf(
        "Create recurring income or expenses, edit schedules, and pause or resume rules. Due entries are processed by the app.",
        "Enable optional weekly finance-review reminders in Settings. Notification permission is required; reminders can be disabled again."
    )),
    FeatureGuideEntry("M-PESA and on-device assistance", listOf(
        "Share or paste M-PESA messages for review before saving. Parsed transaction costs are logged as a separate expense when Log M-PESA fees is enabled and linked back in History. Community builds offer an opt-in new-message listener; background logging is a separate opt-in and saves only when one M-PESA account can be identified. SMS closing balances are compared with the expected account balance, and mismatches are reported without changing the balance. The Play build does not include SMS listening.",
        "Category suggestions use history, corrections, and on-device classification. Supported devices may offer optional Gemini Nano message suggestions and monthly reflections. Review suggestions before use."
    )),
    FeatureGuideEntry("Insights, reviews, and exports", listOf(
        "Explore charts, trends, forecasts, and cash-flow estimates in Insights. The shareable monthly finance review covers income, spending, top categories, and month-over-month context.",
        "Export date-range PDF statements or CSV spreadsheets from Settings. Files have readable names, save on your device, and open Android's share menu. Currencies and transfer amounts stay explicit."
    )),
    FeatureGuideEntry("Widgets and quick access", listOf(
        "Add snapshot, quick-action, balance, monthly-summary, quick-expense, or quick-income widgets from your launcher's widget picker. Sizes depend on the widget and launcher.",
        "Quick-add widgets show the account that will be used and open entry with your last-used account selected. Setup offers a one-time widget-pin request on supported launchers.",
        "The expanded Quick Action widget can repeat your most recent income or expense. It shows the recent note and amount, then opens a new entry with its account and category filled in and today's date; normal duplicate checks still apply."
    )),
    FeatureGuideEntry("Privacy, backup, and preferences", listOf(
        "Finance records are stored locally. JSON backup and restore are user initiated; keep backup and shared statement files private.",
        "Hide or show the Home dashboard's total balance with the eye control. Your choice is remembered on this device.",
        "Community builds check GitHub Releases on launch and daily; Check for app updates in Settings starts a manual check. A newer compatible signed APK downloads in the background and notifies you when ready. Android asks you to allow this install source and confirm the update.",
        "Settings includes app lock, light/dark and AMOLED modes, haptic strength, privacy information, and What's New. System theme follows wallpaper colors on Android 12 and newer; explicit light or dark modes use MakeFlow's palette. The interactive tour covers every feature guide section and can open transaction entry for practice. Setup allows an optional name and skippable tour; motion respects reduced-motion preferences. Some features need permissions or supported device services."
    ))
)
