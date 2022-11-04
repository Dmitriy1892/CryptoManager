package com.coldfier.crypto_manager

import android.content.Context
import com.coldfier.crypto_manager.internal.Pbkdf2Factory
import com.coldfier.crypto_manager.internal.Salt
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.subtle.Base64
import java.io.ByteArrayInputStream
import java.security.GeneralSecurityException

/**
 * [CryptoManager] class provides functions for encryption and decryption data.
 * It's not save user's passwords/PIN-codes
 * [pinSecuredAead] saves only internal master key for device.
 *
 * @param [applicationContext] must be application's context, not activity context!!!
 */
class CryptoManager(private val applicationContext: Context) {

    companion object {
        private const val SALT_SIZE = 32
        private const val PREF_FILE_NAME = "pin_secured_key_preference"
        private const val KEYSET_NAME = "pin_secured_keyset"
    }

    init {
        if (applicationContext != applicationContext.applicationContext)
            throw IllegalArgumentException("Provided context is not an applicationContext")

        AeadConfig.register()
    }

    private val pinSecuredAead: Aead
        get() = kotlin.run {
            val masterKeyUri = "android-keystore://pin_secured_key"

            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(applicationContext, KEYSET_NAME, PREF_FILE_NAME)
                .apply {
                    try {
                        withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    } catch (e: Exception) {
                        withKeyTemplate(AeadKeyTemplates.AES128_GCM)
                    }
                }
                .withMasterKeyUri(masterKeyUri)
                .build()
                .keysetHandle

            keysetHandle.getPrimitive(Aead::class.java)
        }

    @Throws(GeneralSecurityException::class, IllegalArgumentException::class)
    fun encryptData(passwordOrPin: String, dataToEncrypt: String): String {
        if (passwordOrPin.length < 4)
            throw IllegalArgumentException("Password/PIN length must contains 4 or more symbols")

        val salt = Salt.generate(SALT_SIZE)
        val secretKey = Pbkdf2Factory.createKey(passwordOrPin.toCharArray(), salt)

        val encryptedData = pinSecuredAead.encrypt(
            dataToEncrypt.encodeToByteArray(),
            secretKey.encoded
        )

        val result = salt + encryptedData

        return Base64.encodeToString(result, Base64.DEFAULT)
    }

    @Throws(GeneralSecurityException::class, IllegalArgumentException::class)
    fun decryptData(passwordOrPin: String, encryptedData: String): String {
        if (passwordOrPin.length < 4)
            throw IllegalArgumentException("Incorrect password/PIN")

        val resBytes = Base64.decode(encryptedData, Base64.DEFAULT)

        val inputStream = ByteArrayInputStream(resBytes)

        val salt = ByteArray(SALT_SIZE)
        inputStream.read(salt)
        val data = ByteArray(resBytes.size - SALT_SIZE)
        inputStream.read(data)
        inputStream.close()

        val secretKey = Pbkdf2Factory.createKey(passwordOrPin.toCharArray(), salt)

        return pinSecuredAead.decrypt(data, secretKey.encoded).decodeToString()
    }
}
