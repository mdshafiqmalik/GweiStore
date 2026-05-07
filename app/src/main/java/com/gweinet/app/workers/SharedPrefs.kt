package com.gweinet.app.workers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.Networks
import com.gweinet.app.viewmodels.RPC

// -------------------- DATA MODELS -------------------

data class NamedWallet(
    val id: String,
    val name: String,
    val wallet: WalletData
)

// -------------------- PREFS --------------------

fun getSecurePrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "wallet_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

// -------------------- WALLET MANAGER --------------------

object WalletManager {
    private const val WALLET_IDS = "wallet_ids"
    private const val ACTIVE_WALLET = "active_wallet"
    private const val ACTIVE_CHAIN = "active_chain"
    private const val NETWORK_IDS = "network_id"
    private const val IS_DARK = "IS_DARK"

    private const val PIN = "pin"

    private val gson = Gson()

    // Save Active Chains
    fun setIsDark(isDark: Boolean, context: Context){
        val prefs = getSecurePrefs(context)
        prefs.edit { putString(IS_DARK, isDark.toString()) }
    }

    fun isDark(context: Context): String? {
        val prefs = getSecurePrefs(context)
        return prefs.getString(IS_DARK, null)
    }

    fun setActiveChain(chainID: String, context: Context){
        val prefs = getSecurePrefs(context)
        prefs.edit { putString(ACTIVE_CHAIN, chainID) }
    }

    fun setPIN(newPIN: String, context: Context, appViewModel: AppViewModel):Boolean{
        if (_getPIN(context) == 0L){
            val prefs = getSecurePrefs(context)
            prefs.edit { putString(PIN, newPIN) }
            appViewModel.setPin(newPIN.toLong())
            return  true
        }else{
            return false
        }
    }

    fun resetPIN(newPIN: String, oldPIN: String, context: Context):Boolean{
        if (_getPIN(context) == oldPIN.toLong()){
            val prefs = getSecurePrefs(context)
            prefs.edit { putString(PIN, newPIN) }
            return  true
        }else{
            return false
        }
    }

    fun initPIN(appViewModel: AppViewModel, context: Context){
        appViewModel.setPin(_getPIN(context))
    }

    private fun _getPIN(context: Context): Long {
        val prefs = getSecurePrefs(context)
        val activeId = prefs.getString(PIN, null) ?: return 0L
        return activeId.toLong()
    }
    fun getActiveChain(context: Context): Long {
        val prefs = getSecurePrefs(context)
        val activeId = prefs.getString(ACTIVE_CHAIN, null) ?: return 137
        return activeId.toLong()
    }

    fun saveNetwork(
        context: Context,
        network: Networks
    ): Result<Unit> {

        return runCatching {

            val prefs = getSecurePrefs(context)

            val id = network.chainID.toString()

            // Convert RPC list to JSON
            val rpcJson = gson.toJson(network.RPC)

            prefs.edit {

                putInt(
                    "${id}_icon",
                    network.icon
                )

                putLong(
                    "${id}_chainID",
                    network.chainID
                )

                putBoolean(
                    "${id}_isMainnet",
                    network.isMainnet
                )

                putBoolean(
                    "${id}_isCustom",
                    network.isCustom
                )

                putString(
                    "${id}_name",
                    network.name
                )

                putString(
                    "${id}_symbol",
                    network.symbol
                )

                putString(
                    "${id}_rpc",
                    rpcJson
                )

                putString(
                    "${id}_explorer",
                    network.explorer
                )

                putBoolean(
                    "${id}_active",
                    network.isActive
                )
            }

            val ids =
                prefs.getStringSet(
                    NETWORK_IDS,
                    emptySet()
                )?.toMutableSet()
                    ?: mutableSetOf()

            ids.add(id)

            prefs.edit {
                putStringSet(
                    NETWORK_IDS,
                    ids
                )
            }
        }
    }

    fun renameNetwork(
        context: Context,
        chainID: Long,
        newName: String
    ): Result<Unit> {

        return runCatching {

            val prefs = getSecurePrefs(context)

            val id = chainID.toString()

            // Check network exists
            val exists =
                prefs.contains("${id}_chainID")

            if (!exists) {
                throw Exception("Network not found")
            }

            prefs.edit {
                putString(
                    "${id}_name",
                    newName
                )
            }
        }
    }

    fun loadAllNetworks(
        context: Context
    ): Result<List<Networks>> {

        return runCatching {

            val prefs =getSecurePrefs(context)

            val ids =
                prefs.getStringSet(
                    NETWORK_IDS,
                    emptySet()
                ) ?: emptySet()

            ids.mapNotNull { id ->

                runCatching {

                    val icon =
                        prefs.getInt("${id}_icon", 0)

                    val chainID =
                        prefs.getLong("${id}_chainID", -1)

                    val isMainnet =
                        prefs.getBoolean(
                            "${id}_isMainnet",
                            false
                        )

                    val isCustom =
                        prefs.getBoolean(
                            "${id}_isCustom",
                            true
                        )

                    val name =
                        prefs.getString(
                            "${id}_name",
                            null
                        )

                    val symbol =
                        prefs.getString(
                            "${id}_symbol",
                            null
                        )

                    val explorer =
                        prefs.getString(
                            "${id}_explorer",
                            ""
                        ) ?: ""

                    val active =
                        prefs.getBoolean(
                            "${id}_active",
                            false
                        )

                    // Load RPC JSON
                    val rpcJson =
                        prefs.getString(
                            "${id}_rpc",
                            "[]"
                        ) ?: "[]"

                    val type =
                        object : TypeToken<List<RPC>>() {}.type

                    val rpcList: List<RPC> =
                        gson.fromJson(
                            rpcJson,
                            type
                        ) ?: emptyList()

                    if (
                        chainID != -1L &&
                        name != null &&
                        symbol != null
                    ) {

                        Networks(
                            icon = icon,
                            chainID = chainID,
                            isMainnet = isMainnet,
                            isCustom = isCustom,
                            name = name,
                            symbol = symbol,
                            RPC = rpcList,
                            explorer = explorer,
                            isActive = active
                        )

                    } else {
                        null
                    }

                }.getOrNull()
            }
        }
    }

    fun deleteNetwork(
        context: Context,
        chainID: Long
    ): Result<Unit> {

        return runCatching {

            val prefs = getSecurePrefs(context)

            val id = chainID.toString()

            prefs.edit {

                remove("${id}_icon")
                remove("${id}_chainID")
                remove("${id}_isMainnet")
                remove("${id}_isCustom")
                remove("${id}_name")
                remove("${id}_symbol")
                remove("${id}_rpc")
                remove("${id}_explorer")
                remove("${id}_active")
            }

            val ids =
                prefs.getStringSet(
                    NETWORK_IDS,
                    emptySet()
                )?.toMutableSet()
                    ?: mutableSetOf()

            ids.remove(id)

            prefs.edit {
                putStringSet(
                    NETWORK_IDS,
                    ids
                )
            }
        }
    }


    fun saveRPC(
        context: Context,
        chainID: Long,
        rpcName: String,
        rpcLink: String
    ): Result<String> {
        return runCatching {
            val current = getRPCs(context, chainID).toMutableList()

            val exists = current.any { it.link == rpcLink }

            val updated = if (exists) {
                current.map {
                    it.copy(isActive = it.link == rpcLink)
                }
            } else {
                current.map { it.copy(isActive = false) } +
                        RPC(
                            name = rpcName,
                            link = rpcLink,
                            isActive = true,
                            isCustom = true
                        )
            }

            saveRPCs(context, chainID, updated)
            "RPC saved successfully"
        }
    }


    fun setActiveRPC(
        context: Context,
        chainID: Long,
        rpcLink: String
    ) {
        val current = getRPCs(context, chainID)
        if (current.isEmpty()) return
        val target = current.find { it.link == rpcLink }
        val updated = when {
            target?.name == "Default" -> {
                current.map {
                    it.copy(isActive = it.name == "Default")
                }
            }
            target != null -> {
                current.map {
                    it.copy(isActive = it.link == rpcLink)
                }
            }
            else -> {
                current.map {
                    it.copy(isActive = false)
                }
            }
        }

        saveRPCs(context, chainID, updated)
    }

    fun deleteRPC(
        context: Context,
        chainID: Long,
        rpcLink: String
    ): Result<String> {
        return runCatching {
            val current = getRPCs(context, chainID).toMutableList()
            val target = current.find { it.link == rpcLink }
            if (target == null || !target.isCustom) {
                return@runCatching "RPC not found or not deletable"
            }
            val updated = current.filterNot { it.link == rpcLink }
            val finalList = if (updated.isNotEmpty() && updated.none { it.isActive }) {
                updated.mapIndexed { i, rpc ->
                    rpc.copy(isActive = i == 0)
                }
            } else updated
            saveRPCs(context, chainID, finalList)
            "RPC deleted successfully"
        }
    }

    fun getRPCs(context: Context, chainID: Long): List<RPC> {
        val prefs = getSecurePrefs(context)
        val key = "${chainID}_rpcs"

        val set = prefs.getStringSet(key, emptySet()) ?: emptySet()

        return set.mapNotNull {
            val parts = it.split("||")   // ✅ FIXED

            if (parts.size >= 4) {
                RPC(
                    name = parts[0],
                    link = parts[1],
                    isActive = parts[2].toBoolean(),
                    isCustom = parts[3].toBoolean()
                )
            } else null
        }
    }

    fun saveRPCs(context: Context, chainID: Long, rpcs: List<RPC>) {
        val prefs = getSecurePrefs(context)
        val key = "${chainID}_rpcs"

        val set = rpcs.map {
            "${it.name}||${it.link}||${it.isActive}||${it.isCustom}"
        }.toSet()

        prefs.edit().putStringSet(key, set).apply()
    }


    // 🔹 Save wallet
    fun saveWallet(
        context: Context,
        wallet: WalletData,
        walletName: String
    ): Result<String> {

        return runCatching {
            val prefs = getSecurePrefs(context)
            val key = getOrCreateKey()

            val walletId = UUID.randomUUID().toString()

            prefs.edit {
                putString("${walletId}_name", walletName)
                    .putString("${walletId}_mnemonic", AESUtil.encrypt(wallet.mnemonic, key))
                    .putString("${walletId}_private", AESUtil.encrypt(wallet.privateKey, key))
                    .putString("${walletId}_public", AESUtil.encrypt(wallet.publicKey, key))
                    .putString("${walletId}_address", AESUtil.encrypt(wallet.address, key))
            }

            // update wallet list
            val ids = prefs.getStringSet(WALLET_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            ids.add(walletId)
            prefs.edit { putStringSet(WALLET_IDS, ids) }

            // set first wallet as active
            if (prefs.getString(ACTIVE_WALLET, null) == null) {
                prefs.edit { putString(ACTIVE_WALLET, walletId) }
            }

            walletId // ✅ return this on success
        }
    }

    // 🔹 Load all wallets
    fun loadAllWallets(context: Context): List<NamedWallet> {
        val prefs = getSecurePrefs(context)
        val key = getOrCreateKey()

        val ids = prefs.getStringSet(WALLET_IDS, emptySet()) ?: emptySet()

        return ids.mapNotNull { id ->
            val name = prefs.getString("${id}_name", null) ?: return@mapNotNull null

            val mnemonic = prefs.getString("${id}_mnemonic", null)
            val privateKey = prefs.getString("${id}_private", null)
            val publicKey = prefs.getString("${id}_public", null)
            val address = prefs.getString("${id}_address", null)

            if (mnemonic != null && privateKey != null && publicKey != null && address != null) {
                NamedWallet(
                    id = id,
                    name = name,
                    wallet = WalletData(
                        AESUtil.decrypt(mnemonic, key),
                        AESUtil.decrypt(privateKey, key),
                        AESUtil.decrypt(publicKey, key),
                        AESUtil.decrypt(address, key)
                    )
                )
            } else null
        }
    }

    // 🔹 Get active wallet
    fun getActiveWallet(context: Context): NamedWallet? {
        val prefs = getSecurePrefs(context)
        val activeId = prefs.getString(ACTIVE_WALLET, null) ?: return null
        return loadAllWallets(context).find { it.id == activeId }
    }

    // 🔹 Switch active wallet
    fun setActiveWallet(context: Context, walletId: String) {
        val prefs = getSecurePrefs(context)
        prefs.edit { putString(ACTIVE_WALLET, walletId) }
    }

    // 🔹 Rename wallet
    fun renameWallet(context: Context, walletId: String, newName: String): Result<String> {
        return runCatching {
        val prefs = getSecurePrefs(context)
            prefs.edit { putString("${walletId}_name", newName) }.toString()
        }
    }

    // 🔹 Delete wallet
    fun deleteWallet(context: Context, walletId: String): Result<Unit> {
        return runCatching {
            val prefs = getSecurePrefs(context)

            val ids = prefs.getStringSet(WALLET_IDS, mutableSetOf())?.toMutableSet()
                ?: mutableSetOf()

            ids.remove(walletId)

            prefs.edit()
                .remove("${walletId}_name")
                .remove("${walletId}_mnemonic")
                .remove("${walletId}_private")
                .remove("${walletId}_public")
                .remove("${walletId}_address")
                .putStringSet(WALLET_IDS, ids)
                .apply()

            // if deleted wallet was active → reset
            val active = prefs.getString(ACTIVE_WALLET, null)
            if (active == walletId) {
                val newActive = ids.firstOrNull()
                prefs.edit().putString(ACTIVE_WALLET, newActive).apply()
            }
        }
    }
}




