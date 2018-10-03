package turloughcowman.ie.biometric

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat

object FingerprintUtils {

    fun hasEnrolledFingerprints(context: Context): Boolean =
            FingerprintManagerCompat.from(context).hasEnrolledFingerprints()

    fun mayUseFingerprint(context: Context): Boolean =
            isSdkVersionSupported and
                    hasReader(context) and
                    hasManifestPermission(context) and
                    hasEnrolledFingerprints(context)

    private fun hasReader(context: Context): Boolean =
            FingerprintManagerCompat.from(context).isHardwareDetected

    private fun hasManifestPermission(context: Context): Boolean =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED

    private val isSdkVersionSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

}
