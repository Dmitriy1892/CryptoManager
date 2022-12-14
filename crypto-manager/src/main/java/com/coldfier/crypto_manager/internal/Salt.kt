package com.coldfier.crypto_manager.internal

import java.security.SecureRandom

object Salt {
    fun generate(lengthByte: Int = 32): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(lengthByte)
        random.nextBytes(salt)
        return salt
    }
}