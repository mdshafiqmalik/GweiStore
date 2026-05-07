package com.gweinet.app.workers

import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.security.SecureRandom

data class WalletData(
    val mnemonic: String?,
    val privateKey: String,
    val publicKey: String,
    val address: String
)

fun generateEthereumWallet(mnemonic: String, fromKey: String?): WalletData {
    if (fromKey != null){
        val credentials = Credentials.create(fromKey)
        return WalletData(
            mnemonic = "null",
            privateKey = fromKey,
            publicKey = Numeric.toHexStringNoPrefix(credentials.ecKeyPair.publicKey),
            address = credentials.address
        )
    }else{
        val secureRandom = SecureRandom()
        var mnemonicPhrase = mnemonic
        val initialEntropy = ByteArray(16)
        secureRandom.nextBytes(initialEntropy)
        if (mnemonic.isEmpty()){
            mnemonicPhrase = MnemonicUtils.generateMnemonic(initialEntropy)
        }
        val seed = MnemonicUtils.generateSeed(mnemonicPhrase, "")
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        // Ethereum derivation path: m/44'/60'/0'/0/0
        val path = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        )
        val derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        val credentials = Credentials.create(derivedKeyPair)
        return WalletData(
            mnemonic = mnemonicPhrase,
            privateKey = Numeric.toHexStringNoPrefix(derivedKeyPair.privateKey),
            publicKey = Numeric.toHexStringNoPrefix(derivedKeyPair.publicKey),
            address = credentials.address
        )
    }

}
