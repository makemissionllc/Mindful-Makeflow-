# Documentation.md — MakeFlow change log

Living record of code changes, errors encountered, and fixes applied.
**Newest entries go first.** Each entry: date (UTC), scope, what changed and why,
plus verification status.

Conventions: `path:line` for code refs, commit hashes where available.

## 2026-11-01 — Resolve ZiDi transfer direction and add diagnostic sessions

- **ZiDi imports:** parse ZiDi investment and withdrawal confirmations alongside M-PESA
  messages. Investment maps from M-PESA to the `ZiDi` account; withdrawal maps from
  `ZiDi` to M-PESA. Imports use the matching message's closing balance and reconcile it
  against the corresponding account. Ambiguous accounts and unrecognized directions
  remain in review. SMS timestamps are retained when ZiDi messages omit an internal date.
- **Diagnostics:** app log entries now carry a process session and thread context;
  transaction-import steps are correlated by operation ID and record parser decisions,
  direction, balances, save outcomes, and errors. Transaction references and account IDs
  are hashed in structured events. Settings can share a local report containing recent
  events and crash details; message bodies are not added to the report. Existing log
  retention remains bounded to 1,000 entries and 30 days.
- **Release and docs:** Community advanced to `2.0.23-community` / code 26. Updated the
  README, feature guide, privacy policy, and release notes. No Room schema or dependency
  changes. Added parser, direction, and balance-reconciliation unit coverage.
- **Verification:** `git diff --check` passed. The required Gradle unit/compile checks
  could not start because this environment has no `JAVA_HOME` or `java` executable;
  followed the repository's static import/usage cross-check fallback. No connected-device
  UI verification was run.

## 2026-10-30 — Refine shared motion toward a fluid spring feel

- **Touch feedback:** shared press scaling now settles with a lightly damped spring,
  giving taps a softer, more responsive release across interactive controls.
- **Navigation:** the selected bottom-navigation icon scales in with a restrained spring;
  tab content now combines its directional slide and fade with a small scale settle. The
  scale magnitudes and motion specs are shared tokens, and reduced-motion mode keeps the
  added scale effects disabled.
- **Verification:** `./gradlew test :app:compileDebugKotlin
  :app:compileDebugAndroidTestKotlin` passed. All 107 connected instrumented tests passed
  on the OPPO CPH2745 (Android 16). Visually checked Home, Insights, and Budget using a
  temporary `.codextest` package; SMS and notification access were not enabled, no
  transaction was saved, and the package was removed afterward. The regular Community
  install was left untouched. `git diff --check` passed.

## 2026-10-30 — Refine phone layout and validate M-PESA logging

- **Home layout:** reserved the compact dashboard's bottom navigation/action dock in the
  scroll viewport so it no longer covers values in a partially visible cash-flow card.
- **Privacy wording:** clarified that Community SMS detection reads only new matching
  M-PESA confirmations, keeps the original message on-device, and sends transactions to
  review by default. Background logging is a separate opt-in for eligible transactions
  when exactly one KES M-PESA account can be identified; ambiguous cases remain in review.
- **On-device verification:** installed an isolated `.community.codextest` build on the
  OPPO CPH2745 (Android 16). Unit tests and all 107 instrumented tests passed with no test
  failures. Gradle could not collect logcat for one completed test. Visually reviewed the
  Settings toggles and both M-PESA disclosures; background logging remained off. The Home
  cash-flow amounts stayed readable above the dock after scrolling. Temporary SMS and
  notification permissions were granted only to the test package, then removed with it.
  No real SMS was injected and no transaction was saved; the installed Community package
  was left untouched. `git diff --check` passed.

## 2026-10-30 — Add optional M-PESA background logging and balance checks

- **Background import:** added a separate DataStore opt-in for saving recognized M-PESA
  transactions directly from the SMS receiver. It uses the transaction commit handler,
  external-reference deduplication, existing fee logging, and the single unambiguous M-PESA
  account only; ambiguous account setups continue through the review notification. Auto-saved
  transactions remain uncategorized for later categorization.
- **Balance reference:** the receiver and review form retain the parsed SMS closing balance.
  MakeFlow compares that value with the expected account balance after the transaction and
  any fee being logged. Mismatches are reported in a notification or review form; account
  balances are never silently replaced. Added pure-domain coverage for incoming, outgoing,
  fee, missing-data, and mismatch cases.
- **Release and docs:** Community advanced to `2.0.22-community` / code 25. Updated the README,
  feature guide, privacy policy, settings disclosure, and release notes. No Room schema or
  dependency changes.
- **Verification:** `./gradlew :app:compileDebugKotlin`, `./gradlew test`, and
  `./gradlew :app:compileDebugAndroidTestKotlin` passed. Device UI tests were not run because
  `adb` and a connected physical device are unavailable. The first combined Gradle attempt
  reported cascading unresolved Compose references; standalone compilation then passed without
  a source change. `git diff --check` passed.

## 2026-10-24 — Prevent transaction suggestion overlap

- **Type changes:** transaction details now use one composed branch for Transfer or
  category/account suggestions. Switching types removes the old chips immediately;
  only the surrounding content height animates. This prevents the outgoing “From/To”
  labels and pill backgrounds from overlapping the new suggestion row. Existing chip
  color, selection, and haptic feedback remain active.
- **Compact alignment:** retained the centered account/category labels, themed selected
  suggestion surface, 48dp compact calculator keys, tightened amount spacing, and opaque
  fixed Save background. The keypad stays clear of the Save actions on phones.
- **Verification:** clean unit tests, debug and Android-test Kotlin compilation, and APK
  assembly passed. Four focused Compose instrumentation tests passed on the connected
  OPPO CPH2745 (Android 16), including a Transfer-to-Expense regression check that the
  old chips disappear and the category suggestion and account chip appear. Tests used
  an isolated `.codextest` application ID; no transaction was saved. A previous
  incremental build reported a missing generated class, and the clean build then passed.
  Only the phone was enumerated for this pass. `git diff --check` passed.

## 2026-10-22 — Polish widgets and transaction motion

- **Widget polish:** aligned widget light/dark surfaces with app surfaces, improved
  text hierarchy and short-height layouts across the six Glance widgets, and brought
  quick-action hit targets to 48dp. Glance widgets remain static; their tap-through
  Compose screens use the app's motion system.
- **Predictable motion:** transaction type and picker changes now crossfade/resize,
  calculator amount and pending-operation feedback animate, and usual amounts, notes,
  save status, and tablet review details transition in sync. Motion respects the
  reduced-motion setting. Save confirmation fades after a short interval while a
  restored draft remains visible until it is changed.
- **Verification:** unit tests and debug plus instrumentation-test Kotlin compilation
  passed; debug APK assembly passed. The connected full suite reported 105/106 on each
  device because a guide restoration test exposed missing “available balance” copy;
  after fixing the guide description, that test passed on the OPPO CPH2745 and
  25091RP04G. The transaction flow was visually checked on both devices and the
  snapshot widget was checked in the phone launcher. `git diff --check` passed.

## 2026-10-22 — Refine transaction entry and tablet review

- **Transaction start:** removed receipt-photo scanning from entry, deleted its on-device
  scanner and focused unit test, and updated the README, feature guide, and privacy copy. The
  Expense, Income, and Transfer selectors now center their labels vertically. A short
  first-use prompt appears for a blank new entry after draft recovery has resolved.
- **Tablet workspace:** expanded layouts place the calculator and transaction controls beside
  a live review of amount, account, category, note, date, balances, and transfer conversion.
  Save actions align beneath the review pane; compact and medium layouts remain single-column.
- **Verification:** `./gradlew test :app:compileDebugKotlin
  :app:compileDebugAndroidTestKotlin` passed (503 unit tests and both Kotlin compilations).
  On the connected phone, the type labels were centered and Transfer controls appeared
  correctly. On the tablet, a usual amount updated the review live without saving. A system
  picture-in-picture window covered part of the lower review in the screenshot; UI hierarchy
  inspection confirmed the covered note, date, and review guidance were present. `git diff
  --check` passed. The isolated `.codextest` package and temporary device captures were removed.

## 2026-10-22 — Align transaction entry on tablets

- **Tablet layout:** transaction form content and fixed save actions now share centered,
  adaptive maximum widths. The calculator keypad has its own narrower centered width, and
  the usual-amount choices fit within the form. Medium and expanded widths come from the
  shared dimension tokens; compact phone sizing stays full width.
- **Verification:** visually reviewed the transaction screen on the connected OPPO CPH2745
  phone and 25091RP04G tablet using an isolated debug package. The phone layout remained
  aligned; on tablet the form and footer share a centered width and all usual-amount options
  are visible. A system picture-in-picture window covered part of the tablet screen during
  capture. `./gradlew test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed (506 unit tests and both Kotlin compilations); `git diff --check` passed.

## 2026-10-22 — Restore transaction drafts and tune dashboard styling

- **Quality of life:** regular full-screen expense entries now autosave locally after a short
  pause and can restore the amount (including a pending calculator operation), account, category,
  note, and date after the form or app closes. Saving or discarding clears the draft. Edit,
  M-PESA-prefilled, shortcut, and quick-add flows keep their existing behavior.
- **Visual audit:** system theme follows Android wallpaper colors on Android 12+; explicit light
  and dark modes keep MakeFlow's palette. The Home balance, account labels, and card-edit control
  now use typography and spacing tokens with clearer semantic text colors. Swipe-to-delete keeps
  its standard undo path, and dashboard reordering remains an explicit long-press action; no
  conflicting swipe action was found in the reviewed flows.
- **Release:** Community advanced to `2.0.21-community` / code 24. Updated README and in-app guide.
  Drafts are device-local; no database schema or dependency changes.
- **Verification:** `./gradlew test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed (506 unit tests and both Kotlin compilations). `./gradlew :app:connectedAndroidTest`
  passed all 106 tests on the OPPO CPH2745 phone and all 106 on the 25091RP04G tablet. Visual
  review of isolated builds showed the first-run layouts fitting both screens; the tablet had a
  pre-existing system picture-in-picture window over part of the app. One phone test attempt was
  interrupted at 57/106 and passed after a complete rerun. `git diff --check` passed.
- **Device state:** tests targeted the generated `.community.codextest` package. At cleanup,
  uninstalling that package on the phone returned `DELETE_FAILED_INTERNAL_ERROR`; a later package
  query found no MakeFlow package on the phone, while `.community` remained on the tablet. The
  available package logs did not establish the cause, and the original package was not reinstalled.

## 2026-10-22 — Align transaction entry and suggest the usual account

- **UI audit:** quick-add and full-screen transaction forms had different selected colors and
  touch affordances. They now share the themed segmented type selector, radio-button semantics,
  haptic feedback, and reduced-motion-aware color transitions. Account/category choices use the
  same selected palette; suggestions are visually distinct and quick-add choices meet the shared
  48dp minimum target.
- **Quality of life:** when a new income or expense uses an exact note from recent categorized
  entries, the form can suggest the most-used still-existing account. The user chooses whether to
  apply it; ties, missing accounts, partial notes, and transfers do not produce a suggestion.
- **Release:** Community advanced to `2.0.20-community` / code 23. Added the account suggestion to
  the feature guide and README. No database schema or dependency changes.
- **Verification:** `./gradlew test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed (502 unit tests and both Kotlin compilations). `./gradlew :app:connectedAndroidTest`
  passed all 105 tests on the physical OPPO CPH2745 (Android 16); tests used a temporary isolated
  debug package, and `com.makemission.makeflow.community` remained installed with its data intact.
  `git diff --check` passed. Initial compile errors were missing Compose/resource imports and an
  invalid test assertion import; both were fixed. Device assertions were corrected to scroll to
  the suggestion and honor quick-add's Income default. One full run lost its ADB monitor at 47/105;
  restarting ADB and retrying completed successfully.

## 2026-10-22 — Improve first-use History and dashboard privacy

- **History UX:** the empty ledger now explains how to get started and offers an
  “Add your first transaction” action. It waits for the first Room result before
  appearing so existing records are not mistaken for an empty ledger during load.
- **Quality of life:** Home can hide or show its balance from an accessible eye
  control. The choice is saved in DataStore; the first frame stays masked until the
  preference loads, and the amount transition respects reduced-motion settings.
- **Release notes:** updated the feature guide and README; Community advanced to
  `2.0.19-community` / code 22.
- **Verification:** `./gradlew clean test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed (497 unit tests and both compilations).
  `./gradlew :app:connectedAndroidTest` passed all 102 instrumented tests on the
  physical OPPO CPH2745 (Android 16). Device tests used a temporary isolated debug
  package; the installed `2.0.18-community` app and its data remained intact.
  The first unit run read stale generated version metadata; a clean rebuild fixed it.
  The initial temporary package suffix contained an invalid hyphen and was corrected.
  The sandbox denied the first ADB daemon start; retrying with device access succeeded.

## 2026-10-20 — Honor reduced motion in dashboard card movement

- **Dashboard motion:** dashboard cards use shared ease-in-out placement timing and quick
  ease-out fades when inserted or removed. Those item animations are disabled when reduced
  motion is active, while normal reordering still shows where each card moves.
- **Verification:** `./gradlew :app:compileDebugKotlin :app:assembleDebug --rerun-tasks`
  passed (38 tasks). Compilation reported existing unchecked-cast warnings in the category
  classifiers and a shadowed `String.compareTo` extension in `BudgetSuggestionEngine.kt`.
  `git diff --check` passed; no tests or device UI checks were run.

## 2026-10-20 — Refine shared UI motion for faster navigation

- **Motion:** screen and tab transitions now use short ease-out timing, while changing
  values, progress, and content size use smooth ease-in-out timing. Reduced-motion timings
  are shorter; selection labels and the transaction action button use quick fades without
  bounce. List entrances stagger less, and bounce remains for illustrative moments.
- **Navigation:** retained directional tab movement and overlay entry paths so transitions
  continue to reinforce where each screen sits in the app.
- **Verification:** `git diff --check` and static motion-token/import usage checks passed.
  Gradle compilation could not run because this environment has no Java runtime. No tests
  or device UI checks were run.

## 2026-10-13 — Speed up widget entry and repeat recent transactions

- **Quality of life:** quick-add widgets now show the account that will be selected and
  open with the remembered account. Saving from a widget updates the same account preference
  used by in-app transaction entry.
- **Tablet widget feature:** the expanded Quick Action widget previews the latest income or
  expense and opens a fresh form with its amount, note, account, and category filled in. The
  date is today and the normal duplicate and anomaly review remains active.
- **Animated tutorial:** Settings > What's New now demonstrates the widget flow in an animated
  preview. The section is part of the existing interactive tour and respects reduced-motion.
- **Feature discovery:** added these changes to the feature guide, release notes, and README.
  Community advanced to `2.0.18-community` / code 21.
- **Verification:** `./gradlew test :app:compileDebugAndroidTestKotlin` passed. The complete
  `connectedAndroidTest` suite passed all 100 tests on the physical OPPO CPH2745 (Android 16);
  no emulator was started. An earlier run showed eight transient Compose startup failures in
  account UI suites; the immediate full rerun passed, and the final run after the tutorial
  change also passed all 100 tests.

## 2026-10-13 — Remember transaction accounts and streamline tablet selection

- **Quality of life:** new transactions now default to the most recently used account. The
  choice is saved in DataStore, restored on later app launches, and ignored if that account
  has since been removed. Transfers remember their source account.
- **Tablet feature:** expanded tablet account selection uses two columns, keeping account
  names, per-account balances, and currencies together in a scan-friendly grid.
- **Interactive tutorial:** updated the Accounts and currencies and Tablet workspaces steps;
  the Settings tour automatically includes these details and continues to animate step changes.
- **Feature discovery:** added both features to the release notes, feature guide, and README.
  Community advanced to `2.0.17-community` / code 20.
- **Verification:** `./gradlew test :app:compileDebugAndroidTestKotlin` passed. The connected
  Android test task compiled, but could not install on the attached device because the installed
  `com.makemission.makeflow.community` package uses a different signing key
  (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). The installed app and its data were left intact.

## 2026-10-12 — Add transaction category shortcuts and an interactive feature tour

- **Quality of life:** frequent transaction categories now appear as quick-pick chips on
  phones as well as tablets, avoiding an extra category-picker step for common entries.
- **Tablet feature:** the Settings guided tour uses a two-pane layout on expanded screens,
  with feature topics beside the selected walkthrough step.
- **Interactive tutorial:** Settings > What's New now has an animated step-by-step tour
  covering every Feature Guide section. The transaction step opens the actual expense form
  so users can try the flow. Step changes use shared motion tokens and honor reduced motion;
  navigation controls provide haptic feedback.
- **Feature discovery:** added all three changes to the release notes, feature guide, and
  README. Community advanced to `2.0.16-community` / code 19.
- **Verification:** `./gradlew test` passed. `./gradlew connectedAndroidTest` passed all 99
  instrumented tests on the connected OPPO CPH2745 (Android 16); no emulator was started.
  An initial incremental build hit a KSP/Kotlin cache flush issue, so checks were rerun in a
  fresh Gradle process with in-process Kotlin compilation. The first compile found an
  incorrect Compose animation import, which was fixed. The first UI run exposed a mismatch
  in the tablet tour test's expected copy; after correcting it, an intermittent destroyed
  Activity failure appeared in `AccountFocusSheetTest`, then the final full device rerun passed.

## 2026-10-12 — Smooth selection feedback and fit narrow forms

- **Motion:** transaction type and account selection colors now transition using shared motion
  tokens; reduced-motion mode uses the immediate color spec. Screen-level transitions already
  used the shared adaptive overlay specs.
- **Alignment:** quick transaction, budget, and recurring-payment amount fields now shrink to
  available row width, with a shared maximum-width dimension and display text token.
- **Build cleanup:** removed redundant always-true edit checks from the quick transaction sheet.
  The first compile caught a missing Compose state delegate import in the account picker; the import
  was added and the follow-up compile passed.
- **Feature discovery:** added the selection feedback and responsive amount field behavior to the
  current release notes and Settings feature guide.
- **Verification:** `./gradlew test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed. `./gradlew connectedAndroidTest` passed all 96 instrumented tests on the connected OPPO
  CPH2745 (Android 16). No emulator was started.

## 2026-10-12 — Polish transaction entry, history, and tablet balances

- **Transaction entry:** styled the selected income/expense/transfer choice with the primary
  container colors and placed the amount display in a tokenized surface card for clearer review.
- **Account selection:** account picker rows now use each account's own currency and show a
  strong, accessible selected state. The edit account name field and Settings list have stable
  test selectors; Settings scroll tests now bring lazy content into view before checking it.
- **History:** linked parser-imported M-PESA charges now appear as distinct fee labels beneath
  their source transactions. History rows use a quieter surfaced background and secondary text
  uses the theme's on-surface variant.
- **Tablet feature:** expanded transaction entry shows the selected account's available balance;
  transfers show both source and destination balances using their respective currencies.
- **Upgrade bug:** the v13→14 database migration assumed category tables already had `type` and
  `parentCategoryId`. It now checks existing columns and adds either missing column before
  seeding categories, fixing upgrades from older category schemas. Enabled Room schema export and
  checked in the current v22 schema snapshot.
- **Device test fixes:** replaced historical Room test-helper setup with an instrumented SQLite
  helper because the repository had no historical schema snapshots; corrected the alphabetical
  migration expectation and ignored SQLite internal tables. Also corrected the sample package
  ID, account-currency fixtures, and scrolling/semantics selectors for lazy and combined UI rows.
- **Discarded experiment:** a recent-name note shortcut did not update note state in the connected
  UI run, so its control and test were removed before release rather than shipping an unverified
  shortcut.
- **Feature discovery:** added this release to Settings > What's New and the Feature guide, and
  updated README. Community advanced to `2.0.15-community` / code 18; `2.0.14-community` notes
  remain in the versioned changelog.
- **Verification:** `./gradlew test :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
  passed. `./gradlew connectedAndroidTest` passed all 96 instrumented tests on the connected
  OPPO CPH2745 (Android 16). No emulator was started.

## 2026-10-12 — Add Community GitHub updates and faster template entry

- **Community app updates:** added automatic GitHub latest-release checks on app start and daily,
  with a manual Settings action. A newer compatible APK downloads in the background; a notification
  opens Android's install flow. Checks are limited to non-debug Community installs and do not run
  for the Play package. The APK package, version, signing certificate, size, and GitHub SHA-256
  (when supplied) are checked before notification. Android still requires install-source access
  and final user confirmation.
- **Release constraint:** GitHub must publish a signed APK whose signing key matches the installed
  Community app. Incompatible or unsigned APKs are rejected. No signing keys were added to the repo.
- **Quality of life:** applying a saved transaction template now restores its saved category as
  well as amount, type, account, and note.
- **Tablet feature:** expanded transaction forms display saved templates as a two-column quick-pick
  grid with amounts and account currencies.
- **Build bug:** fixed the missing `R` import in `BudgetScreen.kt` that caused the three unresolved
  resource references shown in the attached Android Studio build output.
- **Search fix:** the unit run caught budget search omitting category IDs; search now checks the
  category ID alongside its display name, custom budget name, and period.
- **Feature discovery:** added these changes to Settings > What's New, the feature guide, and README.
  Community advanced to `2.0.14-community` / code 17; prior `2.0.13-community` notes remain.
- **Verification:** `git diff --check`, `./gradlew test`, `:app:compileDebugKotlin`, and
  `:app:compileDebugAndroidTestKotlin` passed. The first unit run exposed the category-ID search
  failure noted above; the rerun passed. A connected OPPO device ran 95 instrumented tests: 69
  passed and 26 failed. Twenty migration tests could not find historical Room schema JSON assets;
  the remaining failures were the sample test's outdated package ID and five UI assertions that
  could not find or uniquely identify expected content, including the parser-fee History row in
  the Fees filter. No emulator was started.

## 2026-10-12 — Simplify repeat transaction entry and add tablet category shortcuts

- **Quality of life:** added Save & add another to the transaction form. After the guarded save
  completes, the form clears amount, note, and date while keeping transaction type, account, and
  category selected. Duplicate checks remain in place; parsed M-PESA review hides this action.
- **Tablet feature:** expanded transaction forms show up to four frequently used categories for
  the selected transaction type. Choosing a chip applies the category and keeps account entry in
  the same flow.
- **Feature discovery:** added both features to Settings > What's New and the feature guide, and
  updated README. Community advanced to `2.0.13-community` / code 16; the `2.0.12-community`
  notes remain in the versioned changelog.
- **Verification:** `git diff --check` and static import/call-site checks passed. Gradle checks
  could not start because Java and `adb` are unavailable in this shell. The attached physical
  device could not be verified from this environment; no emulator was started.

## 2026-10-12 — Link parsed M-PESA fees in History

- **Quality of life:** History adds a Fees filter for parser-created M-PESA fee expenses. Linked
  charges also appear in the source transaction row; the filter and link only show logged fees,
  preserving the existing Log M-PESA fees preference.
- **Tablet feature:** expanded History details show the source transaction's fee and currency,
  even when search or the type filter omits the separate fee row. The charge is matched by the
  parser import's `-fee` reference convention.
- **Parser flow:** deterministic and AI-assisted parsing already extract transaction costs; the
  reviewed import flow persists enabled costs as separate expenses. This change exposes that
  existing relation in History without changing storage or parser behavior.
- **Feature discovery:** updated Settings > What's New release notes and the feature guide, plus
  README. Community advanced to `2.0.12-community` / code 15.
- **Verification:** `git diff --check` and static call-site/resource checks passed. Existing parser
  fixtures cover parsed transaction costs; new unit and History UI regressions cover fee matching,
  filtering, and tablet details. Gradle checks could not start because `JAVA_HOME` is unset and no
  `java` executable is available. No emulator was started.

## 2026-10-12 — Add budget search and tablet budget grids

- **Quality of life:** search budgets by custom name, category, or period using case-insensitive
  multi-term matching; clear in one tap. The monthly overview and warning follow the visible results.
- **Tablet feature:** expanded windows show budget cards in two columns in portrait and three in
  landscape; medium landscape retains its two-column layout. Phone and medium portrait stay in a list.
- **Feature discovery:** added both improvements to What's New and its feature guide. The guide
  continues to cover every major workflow and recent addition. README now notes budget search and
  responsive tablet grids. Release advanced to Community `2.0.11-community` / code 14.
- **Verification:** `git diff --check` passed. Gradle unit and Kotlin compile checks could not
  start because `JAVA_HOME` is unset and no `java` executable is available. No emulator was
  started; tablet UI behavior was not device-verified.

## 2026-10-12 — Add account search and tablet account activity

- **Quality of life:** Accounts gains case-insensitive, multi-term name/currency search
  with one-tap clearing, saved across restoration and resizing. Net worth still includes
  all accounts, not just search matches; legacy currencies follow the selected default.
- **Tablet feature:** expanded Accounts has live selected-account balances and the latest
  20 transactions, scrollable separately from the account list. Transfers use source or
  destination amounts for the selected account, with missing/invalid amounts disclosed.
  Notes/references are selectable; Edit opens the existing account sheet. Selection is
  saved by ID and details disappear when deleted or filtered out, avoiding stale entities.
- **Design:** reused typography/spacing/color tokens, existing reduced-motion-aware
  counters and row feedback, themed surfaces, and haptic selection/clear/edit feedback.
  The narrow tablet account list no longer squeezes two account cards into each row.
- **Release:** Community `2.0.10-community` / code 13. Updated README and in-app changelog.
  No schema, permission, dependency, or network changes.
- **Feature discovery:** expanded Settings > What's New into Updates and Feature guide
  tabs covering all major workflows, recent additions, widget access, tablet workspaces,
  and permission/device limitations, without making users repeat onboarding. The page
  uses design tokens, bounded tablet text width, saved tab/scroll state, and haptic feedback.
- **Verification policy:** per the user's latest instruction, do not start emulators or
  other device simulation environments. Recorded this in `AGENTS.md`. No emulator is
  running and no physical device is connected; UI tests will remain unrun. Earlier
  emulator attempts had ADB authorization issues, resolved with temporary data, but no
  UI tests completed. A missing temporary data directory was created before retrying.
  Interrupted build sessions are not treated as successful.
- **Final checks:** `:app:test` passed all 486 tests (15 new search/activity/guide and
  release-note checks); `:app:compileDebugAndroidTestKotlin` passed, including 11 new
  Accounts/What's New UI tests for adaptive layouts, large text, restoration, search,
  transfer amounts, live updates, and deletion. These UI tests were compiled, not run.
  `:app:assembleRelease` passed with lint and R8 minification, producing the unsigned
  Community APK. The initial offline release attempt lacked desktop/JVM lint artifacts;
  retrying with downloads enabled resolved dependencies without changing library versions
  or disabling lint. `git diff --check` passed. No visual or physical haptic verification
  is claimed without a connected device.

---

## 2026-10-09 — Add History account switching and tablet review controls

- **Quality of life:** choose the shared account focus directly from History using the
  existing Home account sheet. Search, date, and type filters remain intact; invalid/deleted
  account choices are rejected and All accounts can clear an obsolete focus. The shared
  picker now uses design tokens, wrapping labels, and accessible single-selection semantics.
- **Tablet feature:** expanded windows gain Previous/Next review controls, position count,
  Page Up/Page Down navigation while the details pane is focused, and selectable notes and
  references. Details scroll independently; selection is saved by ID across restoration and
  resizing, reads live records, and never displays records outside the current filtered list.
  Selected rows are highlighted using the app's reduced-motion-aware color specs; the details
  crossfade uses existing motion tokens. Compact and medium windows retain the single list.
- **Related fixes:** replaced the fixed, non-scrollable tablet details block, which retained
  a stale entity after updates/deletion and incorrectly reused a transfer's source amount when
  its destination amount was missing. Missing transfer amounts/currencies are now disclosed.
  The narrow list beside the review pane uses the stacked row layout rather than a squeezed
  three-column layout intended for full-width tables, with long-note previews limited to two
  lines. A deleted account focus is labelled unavailable rather than misleadingly All accounts.
- **Release:** Community `2.0.9-community` / code 12; updated README and in-app changelog.
  No schema, permission, dependency, or network changes.
- **Verification:** Android test compilation initially required an explicit experimental-API
  opt-in for keyboard-event injection; added it to the test class only. A combined verification
  run stopped advancing; terminated only its isolated Gradle daemon and retried with two
  workers and no persistent daemon. An Android-test startup attempt found missing generated
  APK outputs; explicitly rebuilt them and retried successfully. Added a deleted-focus UI
  regression after review. Screenshot inspection then found the review pane depended on
  host surface/content colors, causing poor contrast when embedded without a surface;
  gave it an explicit themed background and content color. Final verification passed:
  all 471 unit tests, 12 Android interaction tests on the connected Android 16 device
  with forced compact/medium/expanded layouts, and the minified `:app:assembleRelease`
  build. Inspected tablet screenshots at normal and doubled font scale; keyboard navigation,
  saved selection/resizing, live edits/deletes/filtering, missing transfer amounts, account
  switching/recovery, sheet dismissal, and long notes/references are covered.
  `git diff --check` passed. No physical haptic-strength assessment was performed.

---

## 2026-10-09 — Add filtered History summaries

- **Scope:** a Summary action alongside the visible History result count on phone and tablet.
- **What:** the shared bottom sheet shows income, spending, and net change for the current
  date, type, account, and search filters, grouped by account currency. It updates from Room
  flows after edits, deletes, and undo; empty result sets disable the entry action.
- **Correctness:** transfers are counted separately, never as income/spending. Missing
  accounts, unsupported types/currencies, and non-finite amounts are omitted with a warning.
  Legacy accounts with no currency use the default; explicit currencies are not converted.
  Decimal accumulation and a CurrencyFormatter decimal overload preserve precision without
  converting totals through Double. No new storage, permissions, network calls, or dependencies.
- **Design:** reused adaptive sheet/header, typography/spacing/color tokens, haptic entry
  feedback, and existing reduced-motion behavior; long values and large text can wrap/scroll.
- **Release:** Community `2.0.8-community` / code 11; updated README and in-app changelog.
- **Verification:** the first unit run exposed a BigDecimal scale-sensitive test assertion
  for extreme totals; changed it to compare numeric values. All 468 unit tests passed,
  including eight new summary, two ViewModel, and two formatter regressions. Seven Android
  tests passed on the connected Android 16 phone: History entry/search, empty state, summary
  values, live updates, transfer disclosure, close, and large-text/reduced-motion access.
  The minified `:app:assembleRelease` build and `git diff --check` passed.

---

## 2026-10-09 — Protect unfinished transaction entries

- **Scope:** the full transaction editor and widget/shortcut quick-add sheet.
- **What:** added a shared Keep editing / Discard sheet before abandoning an entered amount,
  note, unfinished calculation, or changes to an existing transaction. Empty new forms and
  unchanged edits close immediately; equivalent amount formatting does not count as a change.
  Transfers compare their actual source/destination, ignoring irrelevant hidden form fields.
  Successful saves still dismiss directly through the existing transaction commit workflow.
- **Dismissal handling:** quick-add vetoes the sheet's Hidden transition before opening the
  confirmation, preserving the visible form if the user keeps editing. The optional shared
  sheet hook reads current callbacks and leaves other sheets' default dismissal unchanged.
- **Related fix:** opening an existing transaction no longer auto-categorizes its unchanged
  note. Quick-add suggestion effects also react to transaction type and loaded history.
  Repeated Back presses during the editor's exit animation cannot reopen the discard prompt.
- **Release:** Community `2.0.7-community` / code 10; updated README and in-app changelog.
  This protects explicit exits during entry; it does not add persistent drafts or background saves.
- **Verification:** the first Android test compile found an invalid `assertDoesNotExist`
  extension import; removed it because this Compose version exposes a member method.
  `:app:test` passed, including 11 new exit-guard unit regressions;
  `:app:compileDebugAndroidTestKotlin`, the minified `:app:assembleRelease` build, and
  `git diff --check` passed. Eight Android interaction tests cover blank/unchanged exits,
  Back/Close/swipe cancellation, discard, and save/exit handling. They compiled but were
  not run because no Android device was connected.

---

## 2026-10-09 — Add shareable date-range spreadsheet statements

- **Scope:** CSV export from Settings on phone and tablet.
- **What:** added the shared calendar-range sheet and Android sharing after successful saving,
  with an in-progress state that prevents overlapping CSV jobs. Exports include account/category
  names, ISO dates with offsets, original currencies, transfer source/destination values, and
  external references. Files use date-based names and UTF-8 with a spreadsheet-friendly BOM.
- **Bugs fixed:** CSV previously read the immediate empty `stateIn` value, left commas/quotes/
  newlines unescaped, displayed success before writing, and did nothing on Android 9. Both
  CSV and PDF now reuse the existing pending-file storage path: Downloads/MakeFlow on Android
  10+, app Documents/FileProvider on Android 9, with partial-file cleanup on write failure.
- **Data safeguards:** shared CSV/PDF range queries exclude the next period's midnight
  (PDF previously passed an exclusive end to an inclusive DAO query); transfer
  amounts are not re-converted or counted as income. Unknown account currencies stay blank.
  Formula-like text is quoted/escaped and tab-prefixed to reduce spreadsheet interpretation
  risk, following [OWASP's CSV guidance](https://owasp.org/www-community/attacks/CSV_Injection).
  This guard changes those text cells and is not a guarantee across every spreadsheet/importer.
- **Release:** Community `2.0.6-community` / code 9; updated README, in-app changelog,
  and the repository/in-app export privacy disclosures.
- **Verification:** `:app:test` passed, including 16 new CSV builder regressions;
  `:app:compileDebugAndroidTestKotlin`, the minified `:app:assembleRelease` build, and
  `git diff --check` passed. Added three Android export tests (persisted rows/date boundary,
  overlapping requests, invalid-range recovery) and a Settings sheet regression. Android
  tests compiled but were not run because no device was connected.

---

## 2026-10-09 — Add calendar-aware History filters

- **Scope:** quicker transaction lookup without scrolling through all history.
- **What:** added an accessible date-range bottom sheet with All time, Today, Last 7 days,
  This month, and Last month. Date filters compose with search, type, and the focused account,
  including transfer destinations. The shared sheet and themed filter chips use existing design
  tokens and haptic feedback. Empty results offer a reset that preserves the focused account.
- **Calendar correctness:** ranges use local midnight boundaries and an exclusive end,
  handling month/year changes, leap years, and daylight-saving transitions. Returning to
  History or reopening the date sheet refreshes relative dates and day headers.
- **Release:** Community `2.0.5-community` / code 8; updated the in-app changelog and README.
- **Verification:** `:app:test`, `:app:compileDebugAndroidTestKotlin`, and the minified
  `:app:assembleRelease` build passed, as did `git diff --check`. Added eight calendar
  tests and four ViewModel regressions for combined filters, reset, day/time-zone refresh,
  and transfer destinations. Four sheet interaction/accessibility tests compiled; they
  were not run because no Android device was connected.

---

## 2026-10-08 — Add Log again for existing transactions

- **Scope:** repeat common purchases, payments, income, and transfers from the transaction editor.
- **What:** added an icon-and-text "Log again" action that retains the current form details,
  switches to new-entry mode, and sets today's date/time. Saving creates a new transaction through
  `TransactionCommitHandler`, including duplicate checks and existing save feedback, without copying
  the original identity or M-PESA reference. Transfer values use current conversion rates.
- **Related fixes:** Save requires accounts that still exist, and transfer writes now use the
  selected source account as `accountId`. Repeat mode and date survive saved-state restoration.
- **Release:** Community `2.0.4-community` / code 7; updated the in-app changelog and README.
- **Verification:** unit tests and all four `RepeatTransactionTest` regressions passed on the
  connected Android 16 phone: fresh identity/date/reference, duplicate confirmation, transfer
  conversion, and a missing account. The minified `:app:assembleRelease` build and
  `git diff --check` also passed.

---

## 2026-10-08 — Improve History lookup and PDF statements

- **Scope:** faster transaction lookup and legible, shareable PDF statements.
- **What:** History search matches notes, amounts, transaction type, category, account, and
  external reference while respecting the existing type/account filters. Empty results distinguish
  a search miss from a new account and offer a one-tap reset. PDF exports now resolve account and
  category labels, wrap long text across rows/pages, align amounts, format totals, and show the
  inclusive date range. Files use a date-based `MakeFlow_Statement` name, save to
  `Downloads/MakeFlow` on Android 10+, and open the system share chooser; Android 9 uses app
  Documents storage and a granted FileProvider URI. Added search, PDF builder, and filename tests.
- **Issue reviewed:** `Images/Issues/Mindful_Statement_20261008_083358.pdf` showed transaction
  columns colliding because IDs and descriptions were drawn as unbounded single-line text.
- **Release:** advanced Community to `2.0.3-community` / code 6 and added both improvements to
  the in-app changelog.
- **Verification:** `:app:test`, `:app:compileDebugAndroidTestKotlin`, and
  `:app:assembleRelease` passed. Instrumented tests compiled but were not run on a device.

---

## 2026-10-08 — Fix Settings haptic crash and add Community permission setup

- **Scope:** Community first-run access setup and the Settings scroll crash reported from
  an Oppo device.
- **Crash diagnosis:** the attached trace shows `NoSuchMethodError` for the Compose
  Foundation `FlowRow` overload in `SettingsScreen.kt:909`, reached while composing the
  haptic choices during LazyColumn prefetch. Replaced that call with stable two-row layout;
  the failure was a runtime Compose ABI mismatch, not a Settings scroll-state exception.
- **Permission setup:** added a one-time, optional access sheet for Community users, also
  reachable from Settings. It clearly explains the separate SMS and notification prompts,
  requests only `RECEIVE_SMS` and Android 13+ `POST_NOTIFICATIONS`, enables M-PESA review
  only when both are allowed, and leaves weekly reminders opt-in. Other system-mediated
  features are called out as not needing blanket permissions. Dismissal is remembered in
  DataStore, and the existing widget offer waits until this sheet is completed or skipped.
- **Release:** incremented the Community package to version `2.0.1-community` / code 4 so
  existing 2.0.0 Community installs can accept the update. Added the changes to the in-app
  changelog.
- **Verification:** `:app:test`, `:app:compileDebugAndroidTestKotlin`, and
  `:app:assembleRelease` passed; `git diff --check` passed. Android tests were compiled but
  not executed on a device in this environment.

---

## 2026-10-08 — Stabilize Settings scrolling and replace the profile glyph

- **Scope:** compact home header settings entry and Settings list scrolling on `community`.
- **What:** replaced the tappable profile initial with the settings gear. Removed the scroll-
  state-driven `LocalReducedMotion` override, which restarted staggered row animations when
  scrolling began or ended; Settings now respects the user's reduced-motion preference
  without changing animation mode during a gesture. Updated the scroll regression test to
  perform repeated upward swipes before checking the last settings row.
- **Verification:** `gradle test --offline` passed and `:app:compileDebugAndroidTestKotlin`
  passed with the repeated-swipe regression. `git diff --check` passed. `adb` is unavailable
  in this environment, so the instrumented test could not run on a device and the user's
  original crash was not reproduced against a live build.

---

## 2026-10-08 — Separate Play and Community release branches

- **Scope:** distribution-specific feature availability after the release branches were
  created from the same 2.0.0 commit.
- **What:** make `community` a full-feature build by default, with its own app ID and version,
  the explicit M-PESA permission/receiver, and the opt-in setting enabled. This branch no
  longer needs a flavor selector. `play` will build as its own restricted single-variant
  branch, without SMS access. Updated release instructions to build the correct artifact on
  each branch.
- **Verification:** unit tests passed; Android test sources compiled; `:app:assembleRelease`
  completed and produced an unsigned GitHub APK. Its merged manifest reports version
  `2.0.0-community` and includes `RECEIVE_SMS` plus `MpesaSmsReceiver`. An initial offline
  release-lint attempt could not resolve uncached Compose desktop metadata; rerunning with
  dependency access resolved the artifacts and release lint/build passed. Play branch
  verification is still pending.

---

## 2026-10-07 — Prepare the 2.0.0 release and complete first-run follow-up

- **Scope:** Play release metadata, Settings scrolling, onboarding, widget discovery,
  optional finance reminders, and privacy-policy access.
- **What:** moved the Play app to version 2.0.0 / code 3. Added Play and Community product
  flavors from one source branch: Play does not declare `RECEIVE_SMS`; the separately named Community
  release uses the existing opt-in receiver and `.community` application ID. The Community
  package is intended for GitHub side-loading and not Play submission. Onboarding now includes a skippable
  practical tour; after setup, a one-time sheet offers Android's supported widget-pinning
  flow and uses the optional profile name. Added opt-in weekly finance-review notifications
  through WorkManager, with a seven-day first delay, a dedicated notification channel, and
  a name-aware message. The in-app privacy page now opens
  `https://makemission.gt.tc/privacy`.
- **Settings scroll:** staggered entrance effects now settle immediately when the user is
  actively scrolling, and an instrumented regression test scrolls to the final privacy row.
  No device crash trace was available, so the original device-specific trigger still needs
  confirmation on a representative phone.
- **SMS policy:** the Play release manifest has neither `RECEIVE_SMS` nor its receiver. Current Play
  guidance does not allow alternate APIs to derive SMS content, so a notification listener
  or accessibility service is not an acceptable workaround. User-initiated share/paste
  import remains the Play-compatible path. SMS Retriever is not used for M-PESA because
  its app-hash-matched message must be sent by a system MakeFlow controls; SMS User Consent
  is a one-message OTP flow, not persistent transaction monitoring. Only the Community
  flavor uses the disclosed direct permission flow.
- **Fixes:** moved the Community app label from a disabled custom `resValue` path into its
  flavor resource file. Fixed onboarding widget-sheet lambda typing, a missing layout import,
  and a misplaced privacy-screen list item found during the first compile. Android-test
  compilation then exposed a stale savings-goal/account test API; updated it to the current
  default-currency and four-argument save callback.
- **Verification:** `gradle test --offline` passed for Play and Community. Both Android-test
  source sets compiled; both release AAB tasks completed. The merged Play manifest contains
  no SMS permission or receiver; Community contains both. `git diff --check` passed. There is
  no attached device/ADB here, so instrumented tests and device QA were not run. The public
  policy URL, Play Console declarations, production signing, and store submission remain
  publisher-side checks.

---

## 2026-10-07 — Prepare the 1.1.0 Play release candidate

- **Scope:** first-run onboarding, app-wide haptics, privacy disclosures and backup
  behavior, release manifest, versioning, store materials, and release readiness checks.
- **What:** made onboarding name entry optional and clarified that exchange-rate requests
  use currency codes while transaction records stay local. Added saved Off, Soft, Standard,
  and Strong haptic levels; all app entry points now apply the selected level, and waveform
  pauses remain silent. Added an in-app privacy page, public policy copy, Play listing draft,
  and a publisher checklist. Updated the build and in-app changelog to version 1.1.0 / code 2.
- **Privacy and Play policy:** Android-managed backups are disabled and backup/extraction
  rules exclude finance data. The Play release removes `RECEIVE_SMS` and the SMS receiver;
  local debug builds retain the opt-in development path, while share/paste import remains
  available in Play. The SMS policy note now reflects this release decision.
- **Verification:** unit tests passed. The release bundle and lint-vital checks passed; the
  merged release manifest was inspected and contains no `RECEIVE_SMS` or
  `MpesaSmsReceiver`. XML parsing and `git diff --check` passed. The environment has only
  Java 25 while the repository pins Java 21, so Gradle was run with a temporary Java 25
  daemon selection (Kotlin reported its JVM target fallback); the repository's Java 21 pin
  was restored afterward. The produced AAB is unsigned. Play signing, real store contact,
  public policy URL submission, listing assets, Console declarations, and device testing
  remain publisher-side steps.

---

## 2026-10-07 — Add receipt scanning and monthly reflections with Gemini Nano

- **Scope:** transaction entry, monthly finance review, on-device GenAI helpers, and unit
  coverage.
- **What:** added a photo-picker receipt flow that downsamples the selected image in memory
  and asks Gemini Nano for the final total and merchant. A consistent bottom sheet previews
  the suggestion; only after the user accepts are the amount and note placed in the regular
  editable transaction form. No receipt image is persisted or uploaded. Added an optional
  monthly-review page that generates a short reflection from aggregate numeric data
  only, plus a system share action for the recap. Generated text is ephemeral and never
  replaces the deterministic figures.
- **Availability and privacy:** both experiences require an explicit user action, check
  Prompt API feature status, and do not trigger model downloads. Unsupported or temporarily
  unavailable devices retain the manual transaction and standard monthly review flows.
- **Fixes:** the first integration compile caught a mismatched receipt-result mapper and a
  missing shared-sheet import; both were corrected before verification.
- **Verification:** `./gradlew test` passed, including debug Kotlin compilation and the new
  receipt/reflection unit tests; `git diff --check` passed. Runtime inference and visual QA
  still require a compatible Android device.

---

## 2026-10-07 — Improve M-PESA parsing with optional on-device Nano assistance

- **Scope:** M-PESA confirmation parsing, shared-message review, and unit coverage.
- **What:** expanded local parsing for case-insensitive/spaced `Ksh` amounts, timestamp
  variants without the word “at”, and agent cash withdrawals. Reversal and service/balance
  notices stay out of transaction classification. Audited the user-provided local sample
  export: all 5,025 confirmation messages now parse; 5,011 receive a known transaction type
  and 14 remain conservatively unknown. The sample files and temporary audit harness are
  not part of the app or commit.
- **Nano assistance:** added the beta ML Kit Prompt API as a foreground-only fallback for
  shared messages on supported devices where Gemini Nano is already available. No model
  download is triggered; background SMS processing never invokes AI. Candidate type and
  reference are validated against source text, numeric fields still come from local parsing,
  and the review screen marks AI-assisted suggestions for user verification. This does not
  train on or upload personal SMS.
- **Verification:** focused parser tests, the local corpus audit, and `./gradlew test`
  passed; `git diff --check` passed. Gemini Nano runtime behavior still requires a
  compatible physical device and was not verified in this environment.

---

## 2026-10-07 — Add opt-in M-PESA SMS detection with review-before-save

- **Scope:** SMS permission handling, M-PESA sender validation, review notifications,
  Settings consent, and transaction-editor handoff.
- **What:** added an off-by-default `RECEIVE_SMS` flow for new broadcasts only. It accepts
  the exact M-PESA sender ID, parses matching confirmation messages locally, suppresses
  references already recorded, then opens the existing transaction review/save flow from
  a private notification. No inbox-history permission or automatic transaction write is
  used. Added a prominent pre-permission disclosure, notification channel, opt-in setting,
  and sender-filter tests. Share/paste import remains available.
- **Privacy and release gate:** raw SMS text is not logged, persisted, or sent off-device;
  only parsed fields are temporarily carried by the app's notification action for review.
  Google Play Console declaration and permission approval are publisher-side steps and
  have not been performed. Eligibility is uncertain because manual/share import remains an
  alternative; see `GOOGLE_PLAY_SMS_PERMISSION_REVIEW.md` and do not release with this
  permission until eligibility is confirmed and Play approves the declaration.
- **Verification:** `./gradlew test` passed, including debug Kotlin compilation;
  `git diff --check` passed. Play Console declaration and device SMS-flow testing remain
  publisher/device-side checks.

---

## 2026-10-07 — Unify widgets and bottom-sheet interactions

- **Scope:** Glance widget family, shared adaptive sheet layout, transaction/account/
  budget/goal/category/currency/review sheets, onboarding account entry, and date picker.
- **What:** redesigned balance, monthly summary, quick-add, expense, and income widgets
  around the same rounded surface, typography, spacing, and chartreuse action hierarchy;
  included richer monthly and projected-balance details at expanded sizes. Standardized
  sheet insets, headers, close affordances, primary actions, and reduced-motion-aware
  content-size transitions across the app's editing and selection sheets.
- **Fixes:** corrected dark-mode widget action-label contrast so secondary actions remain
  readable on dark widget surfaces.
- **Verification:** `./gradlew test` passed, including debug Kotlin compilation;
  `git diff --check` passed. Device screenshot verification was unavailable because
  `adb` is not installed in this environment.

---

## 2026-10-07 — Add automatic multi-currency accounts and monthly review

- **Scope:** exchange-rate data, account/transaction currency entry, net worth and
  dashboard totals, insights, transfer handling, widgets, PDF summaries, and Room schema.
- **What:** added a searchable ISO currency picker and per-account currency display;
  integrated daily reference-rate refresh from Frankfurter with cached rates and manual
  refresh; convert account balances, dashboard totals, insights, widgets, and exports to
  the selected default currency, excluding amounts without a valid rate. Transfers now
  preserve their destination-currency amount. Added an animated four-part monthly finance
  review and migration to Room v22.
- **Fixes:** the first unit run found an incorrect transaction-count expectation in the
  review fixture and an existing dashboard test that added USD/EUR raw balances without
  rates. Corrected the fixture and aligned Home’s monthly/category totals and account
  chips with the currency conversion rules.
- **Verification:** `./gradlew test` passed (398 unit tests), including debug Kotlin
  compilation; `git diff --check` passed. Instrumented migration tests were not run because
  `adb` is not installed in this environment.

---

## 2026-10-07 — Strengthen type hierarchy and unify motion

- **Scope:** shared typography and motion tokens, navigation, dashboard, onboarding,
  transaction feedback, history, and expandable content.
- **What:** separated display, heading, title, body, and label roles with clearer size
  and weight steps, including a declared SemiBold font face. Standardized navigation,
  content-size, progress, value, and reduced-motion specs in the shared transition
  system. Applied them to onboarding paging, tab and FAB transitions, balance changes,
  staggered reveals, selection colors, anomaly banners, swipe feedback, and expanding
  rows; centralized stagger and movement ratios. Screen and section titles now carry
  clearer emphasis.
- **Verification:** `:app:compileDebugKotlin test --rerun-tasks` passed with the bundled
  Android Studio JDK and Gradle 9.3.1. `git diff --check` passed. Device screenshot
  verification was unavailable because `adb` is not installed in this environment.

---

## 2026-10-07 — Standardize sheets and rebuild onboarding

- **Scope:** shared modal surfaces, date selection, onboarding, and account setup.
- **What:** routed app prompts and date pickers through the shared Material 3 bottom
  sheet pattern on compact and larger layouts. Added a reusable alert-sheet layout and
  consistent themed surface, spacing, and scrolling. Reworked onboarding into a guided
  three-step flow for a quick app preview, profile/currency, and starter accounts, with
  add-account and currency selection sheets, inline validation, save feedback, haptics,
  and reduced-motion-aware paging. New accounts use the app's signature accent.
- **Fixes:** the first compile caught an inaccessible explicit `weight` import in the
  shared sheet; removed it and relied on the `RowScope` extension.
- **Verification:** `:app:compileDebugKotlin test --rerun-tasks` passed with the bundled
  Android Studio JDK and Gradle 9.3.1. `git diff --check` passed. Device screenshot
  verification was unavailable because `adb` is not installed in this environment.

---

## 2026-10-07 — Unify the app around one signature color

- **Scope:** shared Material theme, Settings, saved preferences, Glance widgets, and
  theme-resolution tests.
- **What:** replaced selectable/dynamic palettes with a fixed chartreuse brand accent,
  warm ivory light surfaces, sage containers, and olive-ink text. Dark and AMOLED modes
  remain available, with a consistent accent and semantic status colors. Removed palette
  selection from Settings, ViewModel, DataStore, and app startup; existing saved palette
  values are now ignored. Added night-qualified widget and splash colors.
- **Verification:** `:app:compileDebugKotlin test` passed with the Android Studio JDK
  and Gradle 9.3.1. The first token refactor surfaced duplicate legacy alias names;
  the role tokens were renamed before the passing run.

---

## 2026-10-07 — Polish Home around the mockup and complete M-PESA review import

- **Scope:** compact Home dashboard, shared banner/button styling, primary navigation,
  onboarding copy, and transaction import handoff.
- **What:** aligned the phone Home layout to the reference with a compact greeting and
  avatar, compact currency balance, naturally sized actions, horizontal weekly summary,
  emphasized yellow import banner, and unboxed recent transaction rows. Recent rows now
  prioritize merchant notes and show category/account context without dates; navigation
  icons match the Home, Insights, Budget, and Goals references. Added a paste-and-review
  M-PESA path that uses the existing parser, duplicate check, and transaction editor,
  without implying SMS inbox access. Updated onboarding to describe share/paste import.
  Balance changes use shared reduced-motion-aware transition specs; action haptics remain
  connected through the existing haptic manager. Compact currency formatting is covered
  by unit tests.
- **Fixes:** the initial compile found a stale composable argument and a removed shared
  divider import still used by dashboard modules; both were corrected. Kotlin also fell
  back from a damaged incremental cache entry to a full compile.
- **Verification:** `:app:compileDebugKotlin test` passed with the Android Studio JDK
  and Gradle 9.3.1. Device screenshot verification was unavailable because `adb` is not
  installed in this environment.

---

## 2026-10-03 — Unify app screens around the mockup system

- **Scope:** Shared amounts, navigation dock, primary tab actions, Accounts, and Settings.
- **What:** matched title/amount emphasis and transaction type selection to the mockups;
  unified bottom-dock insets across Home, Insights, Budget, Goals, and History; animated
  dock selection with reduced-motion support; routed its add action to transaction,
  budget, or goal entry by active tab; removed duplicate primary-screen add buttons;
  prevented Insights metric truncation; aligned Settings palette selection, switch rows,
  and notices with the unboxed reference; and rebuilt compact Accounts around initials,
  per-account balances, editable exchange rates, and a fixed add action while preserving
  expanded layouts. Compile checks found missing `fillMaxHeight` and
  `ExperimentalFoundationApi` opt-ins plus an invalid mixed `PaddingValues` overload;
  all were corrected.
- **Verification:** `:app:compileDebugKotlin test` passed with the cached JDK 21 and
  Gradle 9.3.1.

---

## 2026-10-03 — Match Budget and Goals tabs to their references

- **Scope:** Primary Budget and Goals destinations.
- **What:** restyled monthly budget usage around the total planned/spent values, an
  adaptive progress indicator, threshold warning, and unboxed editable rows. The primary
  Goals tab now uses the mockup title and hides its secondary-screen back affordance.
  Compile validation caught optional composable-lambda typing errors in the two header
  conditions; both were corrected.
- **Verification:** `:app:compileDebugKotlin test` passed with the cached JDK 21 and Gradle 9.6.

---

## 2026-10-03 — Align core screens with the mockups

- **Scope:** System bars, primary navigation, Home balance actions, History/Insights
  layouts, Settings appearance, and shared mockup buttons.
- **What:** restored readable status and navigation bar icons in light and dark themes;
  made Home, Insights, Budget, and Goals the four primary destinations, with History
  remaining reachable from Recent; removed the Home balance card treatment; added
  one-line action labels; and simplified Settings rows while exposing theme palettes as
  direct swatches. History keeps the primary dock available and reserves its height;
  Insights uses the same adaptive dock inset. Insights now opens with spending trend, a cash-flow
  projection, frugal/savings summaries, budget-pace warnings, and selected-month PDF
  export while retaining its detailed breakdowns below. A compile check caught a missing
  Compose `clip` import for the chart bars; the import was added. Projection arithmetic
  now uses the existing cash-flow domain helper, with direct tests for positive and
  negative monthly outcomes.
- **Verification:** `:app:compileDebugKotlin test` passed with the cached JDK 21 and
  Gradle 9.6.

---

## 2026-10-03 — Match Add Transaction to the mockup

- **Scope:** Add Transaction screen, calculator expression state, recent amount shortcuts,
  transaction commit flow, and UI strings.
- **What:** reordered the form around the mockup's type selector, grouped amount and
  expression, smart category/account chips, usual amounts, open numeric keypad, and
  pill-shaped Save action. Usual amounts now come from recent matching transactions,
  falling back to familiar amounts when there is no history. The default type is Expense.
  Routed save, duplicate confirmation, anomaly feedback, and edits through
  `TransactionCommitHandler`; blocked invalid, negative, incomplete, and account-less
  saves; and replaced the keypad's overlapping tap/long-press handling with a single
  combined gesture. Added calculator expression coverage and localized the new labels.
- **Verification:** `:app:compileDebugKotlin test` passed with the cached JDK 21 and
  Gradle 9.6. The first compile identified the need to opt the long-press gesture into
  Compose's experimental foundation API; that was fixed before the passing run.

---

## 2026-10-03 — Redesign the Home dashboard around daily money actions

- **Scope:** Home dashboard, dashboard view model, weekly budget calculation, and tests.
- **What:** reshaped Home around a personalized greeting, adaptive total balance, quick
  expense and transfer actions, weekly budget feedback, and recent transactions. Weekly
  spend is calculated from dated expenses and budget categories instead of the app's
  cumulative budget counter; added tests for date, category, and focused-account filtering.
  Existing dashboard modules remain below the primary content.
- **Verification:** `:app:compileDebugKotlin test` passed with cached JDK 21 and
  Gradle 9.6. An intermediate compile caught a misplaced modifier declaration in the
  weekly card; it was moved into the correct composable before the successful run.

## 2026-10-03 — Add shared mockup components and compact navigation dock

- **Scope:** shared Compose controls, app dimensions, compact navigation, and UI strings.
- **What:** introduced reusable amount, transaction row, action button, banner, progress,
  switch row, and pill navigation components using design tokens. The compact phone dock
  now combines the app's three real tabs with a one-tap add action; adaptive navigation
  rails remain unchanged for larger layouts.
- **Verification:** `:app:compileDebugKotlin test` passed with the cached JDK 21 and
  Gradle 9.6. An initial compile caught stale/unavailable Compose extension imports in
  the new controls; those were removed or replaced with supported click semantics.

## 2026-10-03 — Add mockup theme foundation

- **Scope:** `ui/theme/`, DM Sans font resource and license, theme resolution tests.
- **What:** matched the light and dark mockups with white/black backgrounds, neutral
  surfaces, green actions with black text, and a yellow alert surface with black text.
  Added shared dimensions for pill buttons, cards, chips, progress, and large amounts;
  updated Material typography to use DM Sans and aligned the shared shapes. Kept the
  other palettes, dynamic color, and AMOLED handling available.
- **Verification:** `:app:compileDebugKotlin test` passed using the cached JDK 21 and
  Gradle 9.6. The first offline attempt could not find a cached AAPT2 artifact;
  rerunning with dependency access resolved it and the full compile and unit suite passed.

## 2026-10-03 — Project docs created (`README.md`, `AGENTS.md`, `Documentation.md`)

- **Scope:** repo root (new files, no code touched).
- **What:** added `README.md` (overview, tech stack table, features, structure, build/test
  commands, ML workflow), `AGENTS.md` (mandatory agent procedures: read-first, exact
  edits, verify-by-execution, log changes, commit+push hygiene, design-token-only UI,
  Room-migration + test rules), and this file.
- **Basis:** full exploration of `data/` (Room v20, 15 entities, 15 DAOs, 19 migrations,
  backup/restore, DataStore keys), `ui/` (24 screens, state-driven navigation, 6 theme
  files, 8 ViewModels), `domain/` (19 pure-Kotlin objects), `core/` (ml, haptics,
  notifications, lock, logging, layout, device), `widget/` (6 Glance widgets),
  `train_category_classifier.py`, `PLAN.md`, and the `app/src/test` (38 classes) +
  `app/src/androidTest` (5 classes) suites.
- **Verification:** n/a (markdown only). Committed and pushed with the fix below.

## 2026-10-03 — Fix: missing `LaunchedEffect` import in `AccountsScreen.kt`

- **Error:** `e: .../ui/screens/AccountsScreen.kt:98:5 Unresolved reference 'LaunchedEffect'`
  (used at `AccountsScreen.kt:99` to refresh widgets when `accounts` changes).
- **Fix:** added `import androidx.compose.runtime.LaunchedEffect`
  (`app/src/main/java/com/makemission/makeflow/ui/screens/AccountsScreen.kt:41`).
- **Verification:** static cross-check — all 8 `androidx.compose.runtime` symbols used in
  the file now have matching imports, zero missing. Full Gradle compile not possible in
  this environment: no JDK (`JAVA_HOME is not set`). Rebuild in Android Studio to confirm.

## 2026-10-03 — Local work in progress (uncommitted at time of writing)

- `app/src/main/AndroidManifest.xml` and `.idea/misc.xml` carry uncommitted local
  modifications; commit `38347b4 Improve dashboard and add adaptive widgets` (15 files,
  HomeDashboard/widgets/onboarding/animations) sits on `main` ahead of `origin/main`.
  Agent docs commits do not touch these paths.

## 2026-10-03 — History: `Backup1` merged onto `main`, pushed

- `main` was a stub (`Initial commit`: `.gitignore` + `LICENSE` only). Latest branch by
  date was `origin/Backup1` (`8ab9368`, 83 commits). Merged with
  `--allow-unrelated-histories` into `3411a96 Merge Backup1 into main...`,
  `.gitignore` add/add conflict resolved as a union (kept `Backup1`'s selective `.idea/`
  ignores so tracked IDE configs stay tracked, plus `main`'s keystore/`google-services`/
  profiling entries), `LICENSE` retained. Pushed `0715fb7..3411a96 main -> main`.

## Notable history (from `git log`, for context)

- `8ab9368` Design-system migration: hardcoded values → Material 3 tokens, advanced theming.
- `0fc8c56` Production polish; account-focus filtering across dashboard/history/insights.
- Splash Screen API + initial-load optimization; centralized motion system with reduced-motion.
- Release changelog system + onboarding; suspending backup/restore refactor.
- Room migration test suite; TensorFlow Lite → LiteRT migration.
- Self-learning categorization (on-device retraining + correction memory).
- Savings goals with feasibility analysis; quick-add transaction system + validation.

## Known environment / repo issues

- Agent sandbox has **no JDK**: `./gradlew` fails with `JAVA_HOME is not set`. Builds and
  instrumented tests must run in Android Studio or a JDK-equipped machine.
- `err.txt` (1 MB binary) and `.kotlin/errors/` dumps are committed accidentally in
  history; per `AGENTS.md` these paths must not be committed going forward.
- Display name is "Mindful / Mindful Wallet" while the package stays
  `com.makemission.makeflow` — keep both in mind when editing launcher/widget strings.
