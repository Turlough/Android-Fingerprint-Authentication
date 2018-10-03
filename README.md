# Android-Fingerprint-Authentication
Single Activity app that verifies user's fingerprint

Language: Kotlin

Runs on Marshmallow or higher.

*LoginActivity* runs on startup. If device supports fingerprint authentication, shows a *BottomSheetDialog* (*FingerprintDialog*).

*FingerprintDialog* is dismissed once the fingerprint has been recognised, and a success toast is shown.

*FingerprintUtils* contains utility methods to determine whether or not the device/sdk level supports Fingerprint recognition.

*LoginViewModel* manages state variables as LiveData for *LoginActivity*, which observes it.
