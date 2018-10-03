package turloughcowman.ie.biometric

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import android.support.design.widget.BottomSheetDialog
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.view.View
import kotlinx.android.synthetic.main.biometric_bottom_sheet.*
import timber.log.Timber

import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class FingerprintDialog(context: Context, val onSuccess: ()->Unit?, val onFailure: (String)->Unit?) : BottomSheetDialog(context) {

    private val keyName = "userName"
    private val provider = "AndroidKeyStore"
    private lateinit var keyStore: KeyStore
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null
    private var cipher: Cipher? = null

    init {
        setupView()
        generateKey()
        initCipher()

        cipher?.run {
            cryptoObject = FingerprintManagerCompat.CryptoObject(this)
            val fingerprintManagerCompat = FingerprintManagerCompat.from(context)

            fingerprintManagerCompat.authenticate(
                    cryptoObject,
                    0,
                    CancellationSignal(),
                    AuthCallback(),
                    null
            )
        } ?: onFailure("Error preparing Fingerprint reader")
    }

    private fun setupView() {
        val bottomSheetView = View.inflate(context, R.layout.biometric_bottom_sheet, null)
        setContentView(bottomSheetView)

        btnCancel.setOnClickListener {
            onFailure("Cancelled by user")
            dismiss()
        }
    }

    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance(provider)
            keyStore.load(null)

            val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, provider)
            keyGenerator.init(KeyGenParameterSpec.Builder(keyName, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
                    .build())

            keyGenerator.generateKey()

        } catch (e: Exception) {
            Timber.e(e)
            onFailure("Error preparing Fingerprint reader")
            dismiss()
        }

    }

    private fun initCipher(): Boolean {
        return try {
            cipher = Cipher.getInstance("$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7")
            keyStore.load(null)
            val key = keyStore.getKey(keyName, null) as SecretKey
            cipher?.run { init(Cipher.ENCRYPT_MODE, key) } ?: return false
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }

    }

    private fun updateStatus(status: String) {
        tvStatus.text = status
    }

    inner class AuthCallback : FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
            super.onAuthenticationError(errMsgId, errString)
            updateStatus(errString.toString())
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
            super.onAuthenticationHelp(helpMsgId, helpString)
            updateStatus(helpString.toString())
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess.invoke()
            dismiss()
        }


        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            updateStatus("Failed to authenticate. Please try again")
        }
    }
}