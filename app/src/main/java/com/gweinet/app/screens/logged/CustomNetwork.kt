package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.screens.nonlogged.AnimatedButton
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.Networks
import com.gweinet.app.viewmodels.RPC
import com.gweinet.app.workers.RpcChecker
import com.gweinet.app.workers.WalletManager
import kotlinx.coroutines.launch
@Composable
fun AddNetwork(
    context: Context,
    navController: NavController,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
) {

    val networks by appViewModel.allNetworks.collectAsState()
    val colors = styleModel.appColors.value

    var newNetworkLink by remember { mutableStateOf("") }
    var chainID by remember { mutableLongStateOf(0L) }
    var networkName by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var exLink by remember { mutableStateOf("") }

    var verifying by remember { mutableStateOf(false) }

    var message by remember {
        mutableStateOf("Enter custom RPC endpoint")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    var validRPC by remember {
        mutableStateOf(false)
    }

    var rpcVerified by remember {
        mutableStateOf(false)
    }

    var nameVerified by remember {
        mutableStateOf(false)
    }

    var symbolVerified by remember {
        mutableStateOf(false)
    }

    var exLinkVerified by remember {
        mutableStateOf(true)
    }

    var isChecked by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val isAddNetwork =
        rpcVerified &&
                nameVerified &&
                symbolVerified &&
                exLinkVerified &&
                isChecked

    fun showError(text: String) {
        message = text
        isError = true
    }

    fun showSuccess(text: String) {
        message = text
        isError = false
    }

    BackHandler {
        val popped = navController.popBackStack()
        if (!popped) {
            (context as Activity).finish()
        }
    }

    LaunchedEffect(newNetworkLink) {

        rpcVerified = false
        validRPC = false

        when {

            newNetworkLink.isBlank() -> {
                showError("RPC endpoint is required")
            }

            !newNetworkLink.startsWith("https://") -> {
                showError("RPC must start with https://")
            }

            !Patterns.WEB_URL.matcher(newNetworkLink).matches() -> {
                showError("Invalid RPC URL")
            }

            else -> {
                validRPC = true
                showSuccess("Valid endpoint")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.navs)
            .padding(top = styleModel.insets.calculateTopPadding())
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundBase)
                .padding(bottom = styleModel.insets.calculateBottomPadding())
        )
        {

            Row(
                modifier = Modifier
                    .background(colors.navs)
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            )
            {

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .clickable {
                                val popped = navController.popBackStack()
                                if (!popped) {
                                    (context as Activity).finish()
                                }
                            }
                            .padding(5.dp)
                    ) {

                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.icon_back_arrow),
                            contentDescription = "Back",
                            tint = colors.textdark
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Add Custom Network",
                        color = colors.textlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(.5.dp)
                    .background(Color(0x37828282))
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            )
            {

                // Message Box
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier
                        .background(
                            if (isError)
                                colors.warnBg
                            else
                                colors.blueBg,
                            RoundedCornerShape(10.dp)
                        )
                        .fillMaxWidth()
                )
                {

                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            modifier = Modifier
                                .size(20.dp),
                            painter = painterResource(
                                if (isError)
                                    R.drawable.warn_circle
                                else
                                    R.drawable.icon_checkmark
                            ),
                            contentDescription = null,
                            tint =
                                if (isError)
                                    colors.warnText
                                else
                                    colors.blueTxt
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = message,
                            color =
                                if (isError)
                                    colors.warnText
                                else
                                    colors.blueTxt,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // RPC FIELD

                Text(
                    text = "RPC Endpoint",
                    color = colors.textExlight,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                BasicTextField(
                    value = newNetworkLink,
                    onValueChange = {
                        newNetworkLink = it
                    },
                    textStyle = TextStyle(
                        color = colors.textlight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            .5.dp,
                            colors.borderLight,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                ) { innerTextField ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp)
                    )
                    {

                        if (newNetworkLink.isEmpty()) {

                            Text(
                                text = "https://rpc.example.com",
                                color = colors.textExlight
                            )
                        }

                        innerTextField()
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (validRPC) {

                    Text(
                        modifier = Modifier.clickable {

                            verifying = true

                            scope.launch {

                                RpcChecker.check(
                                    newNetworkLink
                                ) { ok, chainId, error ->

                                    when {

                                        !ok -> {
                                            showError(
                                                error
                                                    ?: "Unable to connect to RPC"
                                            )
                                        }

                                        chainId == null || chainId <= 0 -> {
                                            showError(
                                                "Invalid chain ID returned"
                                            )
                                        }

                                        networks.any {
                                            it.chainID == chainId
                                        } -> {
                                            showError(
                                                "Chain already exists: $chainId"
                                            )
                                        }

                                        else -> {

                                            rpcVerified = true
                                            chainID = chainId

                                            showSuccess(
                                                "RPC Verified (Chain ID: $chainId)"
                                            )
                                        }
                                    }

                                    verifying = false
                                }
                            }
                        },
                        text =
                            when {

                                verifying -> "Verifying..."

                                rpcVerified -> "Verified"

                                else -> "Verify RPC"
                            },
                        color = colors.blueTxt,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(20.dp))

                // NETWORK NAME

                Text(
                    text = "Network Name",
                    color = colors.textExlight,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                BasicTextField(
                    value = networkName,
                    onValueChange = { thisNetwork ->

                        networkName = thisNetwork
                        nameVerified = false

                        when {

                            networkName.isBlank() -> {
                                showError("Network name is required")
                            }

                            networkName.length < 3 -> {
                                showError("Network name is too short")
                            }

                            networks.any {
                                it.name.equals(networkName, true)
                            } -> {
                                showError("Network name already exists")
                            }

                            else -> {

                                nameVerified = true
                                showSuccess("Network name verified")
                            }
                        }
                    },
                    textStyle = TextStyle(
                        color = colors.textlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            .5.dp,
                            colors.borderLight,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                ) { innerTextField ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp)
                    )
                    {

                        if (networkName.isEmpty()) {

                            Text(
                                text = "e.g. Polygon Mainnet",
                                color = colors.textExlight
                            )
                        }

                        innerTextField()
                    }
                }

                Spacer(Modifier.height(20.dp))

                // SYMBOL

                Text(
                    text = "Symbol",
                    color = colors.textExlight,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                BasicTextField(
                    value = symbol,
                    onValueChange = { thisSymbol ->

                        symbol = thisSymbol.uppercase()
                        symbolVerified = false

                        when {

                            symbol.isBlank() -> {
                                showError("Symbol is required")
                            }

                            symbol.length < 2 -> {
                                showError("Symbol too short")
                            }

                            symbol.length > 6 -> {
                                showError("Symbol too long")
                            }

                            networks.any {
                                it.symbol.equals(symbol, true)
                            } -> {
                                showError("Symbol already exists")
                            }

                            else -> {

                                symbolVerified = true
                                showSuccess("Symbol verified")
                            }
                        }
                    },
                    textStyle = TextStyle(
                        color = colors.textlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            .5.dp,
                            colors.borderLight,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                )
                { innerTextField ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp)
                    ) {

                        if (symbol.isEmpty()) {

                            Text(
                                text = "ETH / BNB / MATIC",
                                color = colors.textExlight
                            )
                        }

                        innerTextField()
                    }
                }

                Spacer(Modifier.height(20.dp))

                // EXPLORER

                Text(
                    text = "Explorer Link (Optional)",
                    color = colors.textExlight,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                BasicTextField(
                    value = exLink,
                    onValueChange = {

                        exLink = it
                        exLinkVerified = false

                        if (exLink.isBlank()) {

                            exLinkVerified = true

                        } else if (
                            exLink.startsWith("https://") &&
                            Patterns.WEB_URL.matcher(exLink).matches()
                        ) {

                            exLinkVerified = true
                            showSuccess("Explorer link verified")
                        } else {
                            showError("Invalid explorer URL")
                        }
                    },
                    textStyle = TextStyle(
                        color = colors.textlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            .5.dp,
                            colors.borderLight,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                )
                { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp)
                    ) {
                        if (exLink.isEmpty()) {

                            Text(
                                text = "https://etherscan.io",
                                color = colors.textExlight
                            )
                        }
                        innerTextField()
                    }
                }

                Spacer(Modifier.height(25.dp))

                // ACKNOWLEDGEMENT

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .clickable {
                            isChecked = !isChecked
                        }
                        .background(
                            if (isChecked)
                                colors.blueBg
                            else
                                colors.warnBg,
                            RoundedCornerShape(12.dp)
                        )
                )
                {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 8.dp, top = 2.dp)
                                .size(22.dp),
                            painter = painterResource(
                                R.drawable.icon_checkmark
                            ),
                            contentDescription = null,
                            tint =
                                if (isChecked)
                                    colors.blueTxt
                                else
                                    colors.textlight
                        )
                        Text(
                            text =
                                "I acknowledge that custom networks may provide incorrect balances, transactions, or fees. I will only use RPC links from trusted sources.",
                            color = colors.textlight,
                            lineHeight = 18.sp,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(25.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                )
                {
                    AnimatedButton(
                        "Add Network",
                        true,
                        onClick = {
                            if (!isAddNetwork) {
                                showError("Complete all required fields")
                                return@AnimatedButton
                            }else{

                                val network = Networks(
                                    icon = 0,
                                    chainID = chainID,
                                    isMainnet = false,
                                    isCustom = true,
                                    name = networkName,
                                    symbol = symbol,
                                    RPC = listOf(
                                        RPC("Default", newNetworkLink, true, false)
                                    ),
                                    explorer = exLink.ifEmpty { "None" },
                                    isActive = false
                                )

                                WalletManager.saveNetwork(context, network)
                                    .onSuccess {
                                        appViewModel.addNetwork(network)
                                        showSuccess("Network added successfully")
                                    }
                                    .onFailure {e->
                                        showError("Error: ${e.message}")
                                    }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}