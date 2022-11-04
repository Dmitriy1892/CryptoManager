package com.coldfier.cryptomanagersample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coldfier.crypto_manager.CryptoManager
import com.coldfier.cryptomanagersample.account_manager.AccountManager
import com.coldfier.cryptomanagersample.account_manager.StorageWrapper
import com.coldfier.cryptomanagersample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val cryptoManager by lazy { CryptoManager(applicationContext) }

    private val prefs by lazy { getSharedPreferences("secret_prefs", MODE_PRIVATE) }

    private val storageWrapper by lazy {
        object : StorageWrapper {
            override fun saveAccountData(key: String, data: String) {
                prefs.edit()
                    .putString(key, data)
                    .apply()
            }

            override fun getAccountData(key: String): String {
                return prefs.getString(key, null)
                    ?: throw NoSuchElementException("Account not saved")
            }
        }
    }

    private val accountManager by lazy {
        AccountManager(cryptoManager, storageWrapper)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnEncrypt.setOnClickListener {
            try {
                accountManager.saveAccountData(
                    userName = binding.etUserName.text.toString(),
                    password = binding.etPin.text.toString(),
                    data = binding.etText.text.toString()
                )

                val encryptedData = storageWrapper.getAccountData(binding.etUserName.text.toString())

                binding.etText.setText(encryptedData)

                showToast("Encrypted data for user ${binding.etUserName.text} saved")
            } catch (e: Exception) {
                showToast("Password must contains 4 or more symbols")
            }

        }

        binding.btnDecrypt.setOnClickListener {
            try {
                val decryptedData = accountManager.getAccountData(
                    userName = binding.etUserName.text.toString(),
                    password = binding.etPin.text.toString()
                )

                binding.etText.setText(decryptedData)

                showToast("Data decrypted")
            } catch (e: Exception) {
                showToast("Incorrect username or password")
            }
        }

        binding.btnChangePassword.setOnClickListener {

            val isChanged = accountManager.changePassword(
                userName = binding.etUserName.text.toString(),
                oldPassword = binding.etPin.text.toString(),
                newPassword = binding.etNewPin.text.toString()
            )

            if (isChanged) {
                showToast("Password successfully changed")
            } else {
                showToast("Password not changed, check credentials")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}