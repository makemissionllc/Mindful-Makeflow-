# Mindful Wallet Privacy Policy

Last updated: 9 October 2026.

Applies to Play version 2.0.0 and Community version 2.0.6-community.

## At a glance

Mindful Wallet keeps your finance records in private app storage on your device. It does
not create an online account or sync your transactions to a MakeFlow server. Automatic
Android backup is disabled.

## Information stored on your device

The app stores the name you choose, account names and balances, transactions, categories,
budgets, savings goals, recurring items, exchange-rate preferences, and app settings.
These records are kept locally using Android app storage.

## Online exchange rates

When exchange rates are refreshed, Mindful requests public reference rates from
`api.frankfurter.dev` using currency codes. It does not send your account names, balances,
transaction amounts, notes, or history in that request. The rate service may process
ordinary connection data, such as an IP address, under its own privacy practices.

## Optional on-device assistance

On supported devices, optional receipt suggestions, shared-message classification, and
monthly reflections use Gemini Nano through Android's on-device ML Kit Prompt API. The
selected receipt image, shared text, or finance summary is processed on the device for
that request; the app does not save the image or generated reflection. Suggestions remain
yours to review.

## Permissions and imports

Optional weekly finance reminders use Android notifications and can be turned off in
Settings. Notification permission is requested only after you enable that setting.
Biometric authentication can protect app access, and vibration is used for touch feedback.
The Google Play build does not request SMS access. The separately labeled Community build
can optionally request permission to process new M-PESA messages; it checks only new
message broadcasts and filters for the M-PESA sender. Messages go to review before saving by
default; a separate explicit Community setting can save a confirmed transaction in the
background when one M-PESA account is available. When present, the SMS closing balance is
compared with the expected account balance, and differences are reported without overwriting
the account. Message history is not read. M-PESA confirmations can also be shared or pasted
into either build for review.

## Backups and deleting data

Android cloud backup and device-to-device transfer are disabled for app data. JSON backups
are saved to a location you select. CSV and PDF statements are saved in Downloads/MakeFlow
on Android 10 and newer, or in app Documents storage on Android 9, and can be shared with
an app you choose. A file provider or cloud-synced Downloads location may have its own privacy practices. Use
**Settings > Clear All Data** to remove app records, or clear the app's storage or uninstall
it. Any exported files must be deleted separately.

## Contact and updates

Mindful Wallet does not include advertising or analytics SDKs. For privacy questions,
contact the developer using the email shown on the Mindful Wallet Google Play listing.
Policy updates will be made available in the app and on the linked policy page.
