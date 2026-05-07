package com.gweinet.app.viewmodels


import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import com.gweinet.app.ui.theme.DarkColors
import com.gweinet.app.workers.WalletData
import com.gweinet.app.workers.WalletManager.getRPCs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class AppWallet(
    var id: String ="",
    var name: String= "",
    var address: String = "",
    var isActive: Boolean = false
)

data class Authenticate(
    var isAuth: Boolean = false,
    var reason: String = "",
    var lockUntil: Long = 0L
)


class AppViewModel : ViewModel() {

    // PIN viewmodel
    var isAuthenticating = mutableStateOf(true)
    private var _isAuth = mutableStateOf(false)
    val isAuth: androidx.compose.runtime.State<Boolean> = _isAuth

    private var _authTimer = mutableStateOf(0L)

    private var _authCount = mutableStateOf(0)
    val authCount: MutableState<Int> = _authCount


    private var _pin = mutableLongStateOf(0L)
    fun isPin(): Boolean{
        return _pin.longValue > 0L
    }
    fun setResetPIN(PIN: Long){
        if (_pin.longValue == 0L || isAuth.value){
            _pin.longValue = PIN
        }
    }

    fun setPin(PIN: Long){
        if (!isPin()){
            _pin.longValue = PIN
            _isAuth.value = true
        }
    }
    fun setAuth(isTrue: Boolean){
        _isAuth.value = isTrue
    }
    fun resetPIN(PIN: Long){
        if (isAuth.value){
            _pin.longValue = PIN
        }
    }
    var lockUntil = 0L
        private set

    fun AuthPIN(PIN: Long): Authenticate {
        val currentTime = System.currentTimeMillis()
        if (currentTime < lockUntil) {
            _isAuth.value = false
            _authTimer.value = (lockUntil - currentTime) / 1000
            Authenticate(false, "timer", lockUntil)
        }
        return if (PIN == _pin.longValue || _pin.longValue == 0L) {
            _isAuth.value = true
            isAuthenticating.value = false
            _authCount.value = 0
            Authenticate(true, "", 0L)
        } else {
            _authCount.value++
            _isAuth.value = false
            if (_authCount.value >= 10) {
                lockUntil = currentTime + 60_000L // +60 seconds
                _authTimer.value = 60L
                Authenticate(false, "timer", lockUntil)
            }else{
                Authenticate(false, "wrong", lockUntil)
            }
        }
    }
    fun resetIsAuth(){
        if (_isAuth.value){
            _isAuth.value = false
        }
    }
    var _afterAuth = mutableStateOf("")
        private set
    fun setAfterAuth(screen: String){
        _afterAuth.value = screen
    }
    fun getAfterAuth():String{
        if (_isAuth.value){
            return  _afterAuth.value
        }else{
            return "dashboard"
        }
    }






    val _allNetworks = MutableStateFlow<List<Networks>>(emptyList())
    val allNetworks: StateFlow<List<Networks>> =  _allNetworks
    var activeChainID: Long = APP_NETWORKS[0].chainID
        private set
    fun deleteNetwork(networkID: Long) {
        if (APP_NETWORKS.any {it.chainID == networkID}){
            return
        }
        val updated = _allNetworks.value.filter { it.chainID != networkID }
        _allNetworks.value = if (updated.isEmpty()) {
            emptyList()
        } else {
            // ensure exactly one active network
            val hasActive = updated.any { it.isActive }
            if (hasActive) {
                updated
            } else {
                updated.mapIndexed { index, wallet ->
                    wallet.copy(isActive = index == 0)
                }
            }
        }
    }
    fun renameNetwork(
        chainID: Long,
        newName: String
    ) {

        _allNetworks.update { networks ->

            networks.map { network ->

                if (network.chainID == chainID) {

                    network.copy(
                        name = newName
                    )

                } else {
                    network
                }
            }
        }
    }
    fun addRpcToNetwork(chainId: Long, newRpc: RPC) {
        _allNetworks.value = _allNetworks.value.map { network ->
            if (network.chainID == chainId) {

                // avoid duplicate
                if (network.RPC.any { it.link == newRpc.link }) return@map network

                // make all existing inactive + new one active
                val updatedList = network.RPC.map {
                    it.copy(isActive = false)
                } + newRpc.copy(isActive = true)

                network.copy(RPC = updatedList)
            } else network
        }
    }
    fun setActiveRpc(chainId: Long, rpcLink: String) {
        _allNetworks.value = _allNetworks.value.map { network ->
            if (network.chainID == chainId) {
                network.copy(
                    RPC = network.RPC.map {
                        it.copy(isActive = it.link == rpcLink)
                    }
                )
            } else network
        }
    }

    fun deleteRpc(chainId: Long, rpcLink: String) {
        _allNetworks.value = _allNetworks.value.map { network ->
            if (network.chainID == chainId) {

                val target = network.RPC.find { it.link == rpcLink }

                // ❌ only delete if custom
                if (target == null || !target.isCustom) return@map network

                val newList = network.RPC.filterNot { it.link == rpcLink }

                // ✅ ensure at least one active RPC
                val fixedList = if (newList.isNotEmpty() && newList.none { it.isActive }) {
                    newList.mapIndexed { index, rpc ->
                        rpc.copy(isActive = index == 0) // make first active
                    }
                } else newList

                network.copy(RPC = fixedList)
            } else network
        }
    }
    fun mergeRPCs(context: Context, networks: List<Networks>): List<Networks> {
        return networks.map { net ->
            val extra = getRPCs(context, net.chainID)
            net.copy(
                RPC = (net.RPC + extra).distinctBy { it.link } // avoid duplicates
            )
        }
    }
    fun setActiveNetwork(chainID: Long) {
        _allNetworks.value = _allNetworks.value.map { network ->
            network.copy(isActive = network.chainID == chainID)
        }
            .sortedByDescending { it.isActive }
    }
    fun addNetwork(network: Networks){
        _allNetworks.value = _allNetworks.value + network
    }
    fun initNetworks(network: List<Networks>, context: Context) {
        _allNetworks.value = mergeRPCs(context, network)
    }


    val _wallets = MutableStateFlow<List<AppWallet>>(emptyList())
    val wallets: StateFlow<List<AppWallet>> =  _wallets
    var e: Exception? = null
    var tWallet: WalletData? = null
    fun renameWallet(id: String, newName: String) {
        _wallets.value = _wallets.value.map { wallet ->
            if (wallet.id == id) {
                wallet.copy(name = newName)
            } else {
                wallet
            }
        }
    }
    fun deleteWallet(walletId: String) {
        val updated = _wallets.value.filter { it.id != walletId }
        _wallets.value = if (updated.isEmpty()) {
            emptyList()
        } else {
            // ensure exactly one active wallet
            val hasActive = updated.any { it.isActive }
            if (hasActive) {
                updated
            } else {
                updated.mapIndexed { index, wallet ->
                    wallet.copy(isActive = index == 0)
                }
            }
        }
    }
    fun addWallet(wallet: AppWallet) {
        _wallets.value = _wallets.value + wallet
    }
    fun setActiveCID(network: Long){
        activeChainID = network
    }
    fun setActiveWallet(walletID: String) {
        _wallets.value = _wallets.value.map { wallet ->
            wallet.copy(isActive = wallet.id == walletID)
        }
    }
    fun setTempWallet(wallet: WalletData) {
        tWallet = wallet
    }
    fun clearTempWallet() {
        tWallet = null
    }
}

class AppStyleModel: ViewModel(){
    var insets: PaddingValues = PaddingValues()
    var isDarkTheme = mutableStateOf(true)
        private set
    var appColors = mutableStateOf(DarkColors)
    fun setIsDark(isDark: Boolean){
        isDarkTheme.value = isDark
    }
}