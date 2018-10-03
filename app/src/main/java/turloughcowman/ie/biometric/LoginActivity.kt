package turloughcowman.ie.biometric

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password or Biometrics
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var mViewModel: LoginViewModel
    private var shortAnimTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        mViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
                .apply { initialize(applicationContext) }

        observeViewModel()
    }

    private fun observeViewModel() {
        mViewModel.mode.observe(this, Observer {
            it?.run {
                when (it) {
                    LoginViewModel.Mode.BIOMETRIC -> hideCredentialView()
                    LoginViewModel.Mode.FINGERPRINT_NOT_REGISTERED -> {
                        Toast.makeText(applicationContext, "You have not registered any fingerprints", Toast.LENGTH_LONG).show()
                        setUpCredentialView()
                    }
                    else -> setUpCredentialView()
                }
            }
        })

        mViewModel.waiting.observe(this, Observer {
            it?.run {
                showProgress(it)
            }
        })

        mViewModel.success.observe(this, Observer {
            it?.run {
                when (it) {
                    true -> toast(getString(R.string.your_identity_has_been_verified))
                    false -> toast(mViewModel.errorMessage)
                }
            }
        })

    }

    private fun hideCredentialView() {
        mViewModel.waiting.value = false
        showLogin(false)
        BiometricDialog(this, this::onBiometricSuccess, this::onBiometricFailure).show()
    }

    private fun setUpCredentialView() {
        mViewModel.waiting.value = false
        showLogin(true)

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        btnLogin.setOnClickListener { attemptLogin() }
        btnBiometric.setOnClickListener { showBiometric() }
    }

    private fun showBiometric() {
        mViewModel.requestBiometric(applicationContext)
    }

    private fun onBiometricSuccess() {
        mViewModel.succeed()
    }

    private fun onBiometricFailure(msg: String) {
        mViewModel.fail(msg)
    }

    private fun toast(msg: String) {
        if (msg.isNotEmpty())
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun attemptLogin() {
        toast("Logged in with credentials (TODO)")
        showProgress(false)
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })

    }

    private fun showLogin(show: Boolean) {
        login_form.visibility = if (show) View.VISIBLE else View.GONE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }
}
