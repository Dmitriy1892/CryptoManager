package com.coldfier.cryptomanagersample.account_manager

interface StorageWrapper {

    fun saveAccountData(key: String, data: String)

    fun getAccountData(key: String): String
}