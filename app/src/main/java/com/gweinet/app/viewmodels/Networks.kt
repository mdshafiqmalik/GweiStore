package com.gweinet.app.viewmodels

import com.gweinet.app.R


data class  RPC(
    var name: String = "Default",
    var link: String,
    var isActive: Boolean = false,
    var isCustom: Boolean
)
data class Networks(
    var icon : Int,
    var chainID: Long = 0,
    var isMainnet: Boolean =  false,
    var isCustom: Boolean = true,
    var name: String = "",
    var symbol: String = "",
    var RPC: List<RPC> = emptyList(),
    var explorer: String,
    var isActive: Boolean= false
)


fun rpc(url: String) = RPC("Default", url, false, false)

val APP_NETWORKS = listOf(
    // working
    Networks(
        R.drawable.crypto_pol,137, true,false,
        "Polygon", "POL",
        listOf(rpc("https://polygon-bor-rpc.publicnode.com")),
        "https://polygonscan.com", false
    ),

    // working
    Networks(
        R.drawable.crypto_eth,1, true, false,
        "Ethereum", "ETH",
        listOf(rpc("https://ethereum-rpc.publicnode.com")),
        "https://etherscan.io", false
    ),

    // working
    Networks(
        R.drawable.crypto_bnb,56, true, false,
        "BSC", "BNB",
        listOf(rpc("https://bsc-dataseed.binance.org")),
        "https://bscscan.com", false
    ),

    // Working
    Networks(
        R.drawable.crypto_arb,42161, true, false,
        "Arbitrum", "ETH",
        listOf(rpc("https://arb1.arbitrum.io/rpc")),
        "https://arbiscan.io", false
    ),

   // working
    Networks(
        R.drawable.crypto_opt,10, true, false,
        "Optimism", "ETH",
        listOf(rpc("https://mainnet.optimism.io")),
        "https://optimistic.etherscan.io", false
    ),

    // working
    Networks(
        R.drawable.crypto_avax,43114, true, false,
        "Avalanche", "AVAX",
        listOf(rpc("https://api.avax.network/ext/bc/C/rpc")),
        "https://snowtrace.io", false
    ),

    // Not working
    Networks(
        R.drawable.crypto_ftm,250, true, false,
        "Fantom", "FTM",
        listOf(rpc("https://rpc.ftm.tools")),
        "https://ftmscan.com", false
    ),

    // Not Working
    Networks(
        R.drawable.crypto_sonic,146, true, false,
        "Sonic", "SONIC",
        listOf(rpc("https://explorer.soniclabs.com")),
        "https://sonicscan.org", false
    ),

    // Working
    Networks(
        R.drawable.crypto_base,8453, true, false,
        "Base", "ETH",
        listOf(rpc("https://mainnet.base.org")),
        "https://basescan.org", false
    ),

    // Working
    Networks(
        R.drawable.crypto_linea,59144, true, false,
        "Linea", "ETH",
        listOf(rpc("https://rpc.linea.build")),
        "https://lineascan.build", false
    ),


    //Working
    Networks(
        R.drawable.crypto_zksync,324, true, false,
        "zkSync Era", "ETH",
        listOf(rpc("https://mainnet.era.zksync.io")),
        "https://explorer.zksync.io", false
    ),

    Networks(
        R.drawable.crypto_one,1666600000, true, false,
        "Harmony", "ONE",
        listOf(rpc("https://api.harmony.one")),
        "https://explorer.harmony.one", false
    ),

    Networks(
        R.drawable.crypto_okx,66, true, false,
        "OKX Chains", "OKT",
        listOf(rpc("https://exchainrpc.okx.org")),
        "https://www.oklink.com/okc", false
    ),

    Networks(
        R.drawable.cryto_bdag,1404, true, false,
        "BlockDAG", "BDAG",
        listOf(rpc("https://rpc.bdagscan.com")),
        "https://bdagscan.com", false
    ),

    // Testnets

    Networks(
        R.drawable.crypto_eth,11155111, false, false,
        "Ethereum Sepolia", "tETH",
        listOf(rpc("https://ethereum-sepolia.publicnode.com")),
        "https://sepolia.etherscan.io", false
    ),

    Networks(
        R.drawable.crypto_bnb,97, false, false,
        "BSC Testnet", "tBNB",
        listOf(rpc("https://data-seed-prebsc-1-s1.binance.org:8545")),
        "https://testnet.bscscan.com", false
    ),

    Networks(
        R.drawable.crypto_pol,80001, false, false,
        "Polygon Amoy", "tPOL",
        listOf(rpc("https://rpc-amoy.polygon.technology/")),
        "https://amoy.polygonscan.com", false
    ),
)


