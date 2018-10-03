package turloughcowman.ie.biometric

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context

class LoginViewModel: ViewModel() {

    enum class Mode{ BIOMETRIC, CREDENTIALS, FINGERPRINT_NOT_REGISTERED }

    var errorMessage = ""

    var mode = MutableLiveData<Mode>()
    var waiting = MutableLiveData<Boolean>()
    var success = MutableLiveData<Boolean>()


    fun initialize(context: Context){
        mode.value = when {
            !FingerprintUtils.mayUseFingerprint(context) -> Mode.CREDENTIALS
            !FingerprintUtils.hasEnrolledFingerprints(context) -> Mode.FINGERPRINT_NOT_REGISTERED
            else -> Mode.BIOMETRIC
        }
    }

    fun fail(msg: String){
        //set the error message first, so it is populated before observables change
        errorMessage = msg
        success.value = false
        waiting.value = false
        mode.value = Mode.CREDENTIALS

    }

    fun succeed(){
        success.value = true
        waiting.value = false
    }

    fun requestBiometric(context: Context) {
        when{
            FingerprintUtils.mayUseFingerprint(context) -> mode.value = Mode.BIOMETRIC
            ! FingerprintUtils.hasEnrolledFingerprints(context) -> mode.value = Mode.FINGERPRINT_NOT_REGISTERED
            else -> {
                fail("This device does not support fingerprint authentication")
                mode.value = Mode.CREDENTIALS
            }
        }
    }
}