package com.coldfier.cryptomanagersample.account_manager

import com.coldfier.crypto_manager.CryptoManager
import java.security.GeneralSecurityException

class AccountManager(
    private val cryptoManager: CryptoManager,
    private val storageWrapper: StorageWrapper
) {

    fun saveAccountData(userName: String, password: String, data: String) {
        val encryptedToken = cryptoManager.encryptData(password, data)

        storageWrapper.saveAccountData(userName, encryptedToken)
    }

    @Throws(GeneralSecurityException::class, IllegalArgumentException::class)
    fun getAccountData(userName: String, password: String): String {
        val encryptedToken = storageWrapper.getAccountData(userName)

        return cryptoManager.decryptData(password, encryptedToken)
    }

    fun changePassword(
        userName: String,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        return try {
            val encryptedToken = storageWrapper.getAccountData(userName)
            val decryptedToken = cryptoManager.decryptData(oldPassword, encryptedToken)
            val encryptedByNewPassword = cryptoManager.encryptData(newPassword, decryptedToken)
            storageWrapper.saveAccountData(userName, encryptedByNewPassword)

            true
        } catch (e: Exception) {
            false
        }
    }
}