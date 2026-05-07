package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.ui.theme.Colors
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.AppWallet
import kotlinx.coroutines.launch
import com.gweinet.app.viewmodels.Networks
import com.gweinet.app.viewmodels.RPC
import com.gweinet.app.viewmodels.rpc
import com.gweinet.app.workers.WalletManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowChains(
    navController: NavController,
    context: Context,
    appViewModel: AppViewModel,
    onDismiss: ()-> Unit,
    styleModel: AppStyleModel
)
{
    val scope = rememberCoroutineScope()
    val networks by appViewModel.allNetworks.collectAsState()
    val activeNetwork by remember(networks) {
        derivedStateOf { networks.find { it.isActive }!! }
    }
    BackHandler {
        val popped = navController.popBackStack()
        if (!popped) { (context as Activity).finish() }
    }
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
    val scroll2 = rememberScrollState()
    var allowClose by remember { mutableStateOf(false) }
    val allowCloseState = rememberUpdatedState(allowClose)
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { newValue ->
            val isAtTop = scrollState.value == 0
            if (newValue == SheetValue.Hidden) {
                isAtTop && allowCloseState.value
            } else {
                true
            }
        }
    )
    networks.forEach { network ->
        android.util.Log.d(
            "NETWORK_DEBUG",
            """
            Name: ${network.name}
            Chain ID: ${network.chainID}
            Symbol: ${network.symbol}
            Mainnet: ${network.isMainnet}
            Custom: ${network.isCustom}
            Explorer: ${network.explorer}
            Active: ${network.isActive}
            RPCs:
            ${
                network.RPC.joinToString("\n") {
                    "- ${it.name} | ${it.link} | Active=${it.isActive}"
                }
            }
            """.trimIndent())
    }
    val pagerState = rememberPagerState(
        pageCount = { 2 },
        initialPage = if (!activeNetwork.isMainnet){ 1 } else {0}
    )
    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = {
            onDismiss()
                           },
        sheetState =  rememberModalBottomSheetState(
            confirmValueChange = { true } // allow all gestures
        ),
        containerColor = colors.backgroundBase,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage Networks",
                color = colors.textlight,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(15.dp))
            Row(
                Modifier.fillMaxWidth()
            ) {

                listOf("Popular", "Custom").forEachIndexed { index, title ->

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(15.dp),
                            fontWeight = FontWeight.Bold,
                            color = colors.textlight,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            val indicatorOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val tabWidth = maxWidth / 2

                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * indicatorOffset)
                        .width(tabWidth)
                        .height(3.dp)
                        .background(colors.textExlight)
                )
            }
            Spacer(Modifier.height(15.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight()
                    .background(colors.backgroundBase)
                ,
                verticalAlignment = Alignment.Top
            ) { page ->

                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            networks
                                .filter { it.isMainnet }
                                .forEach { network ->
                                    Networks(
                                        navController,
                                        styleModel,
                                        activeNetwork.chainID,
                                        network,
                                        onChainSelected = { chainID->
                                            allowClose = true
                                            scope.launch {

                                                WalletManager.setActiveChain(
                                                    chainID.toString(),
                                                    context
                                                )
                                                appViewModel.setActiveNetwork(chainID)
                                                onDismiss()
                                                sheetState.hide()
                                            }

                                        },
                                        onManageChain = {
                                            scope.launch {
                                                onDismiss()
                                                sheetState.hide()
                                            }

                                        }
                                    )
                                }
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scroll2)
                        ){
                            networks
                                .filter { !it.isMainnet  || it.isCustom }
                                .forEach { network ->
                                    Networks(
                                        navController,
                                        styleModel,
                                        activeNetwork.chainID,
                                        network,
                                        onChainSelected = { chainID ->
                                            allowClose = true
                                            scope.launch {
                                                WalletManager.setActiveChain(chainID.toString(), context)
                                                appViewModel.setActiveNetwork(chainID)
                                                onDismiss()
                                                sheetState.hide()
                                            }
                                        },
                                        onManageChain = {
                                            scope.launch {
                                                onDismiss()
                                                sheetState.hide()
                                            }
                                        }
                                    )
                                }
                            Spacer(Modifier.height(25.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Spacer(
                                Modifier.height(4.dp).width(80.dp).background(colors.textExlight, RoundedCornerShape(2.dp))) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Row(
                                    modifier = Modifier.padding(30.dp, 20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF64B5F6), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(10.dp))
                                        .fillMaxWidth()
                                        .clickable{
                                            scope.launch {
                                                onDismiss()
                                                sheetState.hide()
                                                navController.navigate("AddNetworks")
                                            }

                                        }
                                    ,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(top = 12.dp, end = 0.dp, bottom = 12.dp, start = 15.dp)
                                            .size(24.dp),
                                        painter = painterResource(R.drawable.icon_add),
                                        contentDescription = "Add Custom Network",
                                        tint = Color.White
                                    )
                                    Text(
                                        modifier = Modifier.padding(12.dp),
                                        text = "Add Custom Network",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Networks(
    navController: NavController,
    styleModel: AppStyleModel,
    aChainId: Long,
    network: Networks,
    onChainSelected: (chainID: Long) -> Unit,
    onManageChain:()->Unit
)
{
    val colors by styleModel.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .clickable{
                    if (network.chainID != aChainId){
                        onChainSelected(network.chainID)
                    }
                }
                .weight(.5f),
            verticalAlignment = Alignment.CenterVertically)
        {
            if (network.icon == 0){
                Spacer(Modifier.width(30.dp))
                Row(
                    modifier = Modifier
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier =  Modifier
                            .padding(top = 1.dp)
                            .size(20.dp),
                        text = network.name.first().toString(),
                        color= colors.textdark,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }else{
                Icon(
                    painter = painterResource(network.icon),
                    contentDescription = null,
                    modifier =  Modifier
                        .padding(start = 30.dp, top = 16.dp,  end = 0.dp, bottom = 16.dp)
                        .size(20.dp),
                    tint =
                        if (
                            network.chainID.toInt() == 66 ||
                            network.chainID.toInt() == 146 ||
                            network.chainID.toInt() == 59144
                        ){
                            colors.textdark
                        }else{
                            Color.Unspecified
                        }

                )
            }


            Spacer(Modifier.width(10.dp))

            Text(
                modifier =  Modifier
                    .padding(start = 10.dp, top = 16.dp,  end = 0.dp, bottom = 16.dp),
                text = network.name,
                fontWeight = FontWeight(500),
                fontSize = 18.sp,
                color = if (network.chainID ==  aChainId) Color.Green else colors.textlight
            )
        }

        Row(
            modifier = Modifier
                .weight(.5f)
                .padding(end = 30.dp)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (network.chainID == aChainId){
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_checkmark),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(3.dp)
                        ,
                        tint = Color.Green
                    )
                }
                Spacer(Modifier.width(20.dp))
            }


            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .clickable{
                        onManageChain()
                        navController.navigate("ViewNetworks/${network.chainID}")
                        
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_elipsis),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .padding(5.dp)
                    ,
                    tint = if (network.chainID ==  aChainId) Color.Green else colors.textlight
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAccounts(
    wallets: List<AppWallet>,
    navController: NavController,
    context: Context,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
    styleModel: AppStyleModel
)
{
    val scope = rememberCoroutineScope()
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
    var allowClose by remember { mutableStateOf(false) }
    val allowCloseState = rememberUpdatedState(allowClose)
    val sheetState = rememberModalBottomSheetState(

        confirmValueChange = { newValue ->
            val isAtTop = scrollState.value == 0
            if (newValue == SheetValue.Hidden) {
                isAtTop && allowCloseState.value
            } else {
                true
            }
        }
    )

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState =  rememberModalBottomSheetState(
            skipPartiallyExpanded = wallets.size > 2,
            confirmValueChange = { true } // allow all gestures
        ),
        containerColor = colors.bg1,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage Wallets",
                color = colors.textdark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(15.dp))
                wallets
                    .forEach { wallet ->
                        WalletShow(
                            navController,
                            context,
                            colors,
                            wallet,
                            onWalletSelected = { wallet ->
                                allowClose = true
                                scope.launch {
                                    WalletManager.setActiveWallet(context, wallet.id)
                                    appViewModel.setActiveWallet(wallet.id)
                                    onDismiss()
                                    sheetState.hide()
                                }
                            },
                            onManageWallet = {
                                scope.launch {
                                    onDismiss()
                                    sheetState.hide()
                                }

                            }
                        )
                    }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clickable{
                                scope.launch {
                                    onDismiss()
                                    sheetState.hide()
                                    navController.navigate("Setup")
                                }
                            }
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF34A7FF), RoundedCornerShape(13.dp))
                            .padding(15.dp, 17.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Create/Import New Wallet",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }

        }
    }

}

@Composable
fun WalletShow(
    navController: NavController,
    context: Context,
    colors: Colors,
    wallet: AppWallet,
    onWalletSelected: (wallet: AppWallet) -> Unit,
    onManageWallet: ()->Unit
)
{

    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(13.dp))
                .border(if (wallet.isActive) {1.dp} else {.5.dp}, if(wallet.isActive) Color(0xFF34A7FF) else  colors.borderLight, RoundedCornerShape(13.dp))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(.5f)
                    .clip(RoundedCornerShape(13.dp))
                    .clickable{
                        if (!wallet.isActive){
                            onWalletSelected(wallet)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Icon(
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                        .size(25.dp),
                    painter = painterResource(R.drawable.icon_check_circle),
                    contentDescription = "Wallet Active",
                    tint =
                        if (wallet.isActive){
                            Color(0xFF34A7FF)
                        }else{
                            colors.textExlight
                        }
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp),
                    text = wallet.name,
                    color =  if (wallet.isActive){
                        Color(0xFF34A7FF)
                    }else{
                        colors.textExlight
                    }
                    ,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                Modifier.padding(end = 8.dp)
            ) {
                var isCoped by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable{
                            clipboardManager.setText(AnnotatedString(wallet.address))
                            Toast.makeText(context, "Copied! ${wallet.address}", Toast.LENGTH_SHORT).show()
                            isCoped = true
                        }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(5.dp)
                            .size(24.dp),
                        painter = painterResource(R.drawable.icon_copy),
                        contentDescription = "Wallet active or inactive icon",
                        tint =
                            if (isCoped) Color.Green
                            else if (wallet.isActive) Color(0xFF34A7FF)
                            else colors.textExlight

                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable{
                            onManageWallet()
                            navController.navigate("WalletDetails/${wallet.id}")
                        }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(5.dp)
                            .size(24.dp),
                        painter = painterResource(R.drawable.icon_elipsis),
                        contentDescription = "Wallet active or inactive icon",
                        tint =  if (wallet.isActive){
                            Color(0xFF34A7FF)
                        }else{
                            colors.textExlight
                        }
                    )
                }

            }


        }
    }
    Spacer(Modifier.height(15.dp))
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowRpcs(
    chainID: Long,
    rpcs: List<RPC>,
    context: Context,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
    onDeleteRPC: (rpc: RPC)-> Unit,
    onAddingRPC: ()->Unit,
    styleModel: AppStyleModel
)
{
    val scope = rememberCoroutineScope()
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
    var allowClose by remember { mutableStateOf(false) }
    val activeRPC = rpcs.find { it.isActive } ?: rpcs.first()
    val allowCloseState = rememberUpdatedState(allowClose)
    val sheetState = rememberModalBottomSheetState(

        confirmValueChange = { newValue ->
            val isAtTop = scrollState.value == 0
            if (newValue == SheetValue.Hidden) {
                isAtTop && allowCloseState.value
            } else {
                true
            }
        }
    )

    ModalBottomSheet(
        modifier = Modifier,
        onDismissRequest = { onDismiss() },
        sheetState =  rememberModalBottomSheetState(
            confirmValueChange = { true } // allow all gestures
        ),
        containerColor = colors.bg1,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage Rpc Links",
                color = colors.textdark,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(15.dp))
                rpcs
                    .forEach { rpc ->
                        RPCShow (
                            isActive = (rpc.link == activeRPC.link),
                            colors,
                            rpc,
                            onRPCSelected = { rpc ->
                                allowClose = true
                                scope.launch {
                                    WalletManager.setActiveRPC(context, chainID, rpc.link)
                                    appViewModel.setActiveRpc(chainID, rpc.link)
                                    onDismiss()
                                    sheetState.hide()
                                }
                            },
                            onDeletingRPC = { rpc->
                                scope.launch {
                                    scope.launch {
                                        allowClose = true
                                        onDeleteRPC(rpc)
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Row(
                        modifier = Modifier
                            .clickable{
                                scope.launch {
                                    onDismiss()
                                    sheetState.hide()
                                    onAddingRPC()
                                }
                            }
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF34A7FF), RoundedCornerShape(13.dp))
                            .padding(15.dp, 17.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Add New RPC",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }

        }
    }

}

@Composable
fun RPCShow(
    isActive: Boolean,
    colors: Colors,
    rpc: RPC,
    onRPCSelected: (rpc: RPC) -> Unit,
    onDeletingRPC: (rpc: RPC)->Unit
)
{
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(13.dp))
                .border(if (isActive) {1.dp} else {.5.dp}, if(isActive) Color(0xFF34A7FF) else  colors.borderLight, RoundedCornerShape(13.dp))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Row(
                modifier = Modifier
                    .weight(.5f)
                    .clip(RoundedCornerShape(13.dp))
                    .clickable{
                        if (!isActive){
                            onRPCSelected(rpc)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Icon(
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                        .size(25.dp),
                    painter = painterResource(R.drawable.icon_check_circle),
                    contentDescription = "Wallet Active",
                    tint =
                        if (isActive){
                            Color(0xFF34A7FF)
                        }else{
                            colors.textExlight
                        }
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp),
                    text = rpc.name,
                    color =  if (isActive){
                        Color(0xFF34A7FF)
                    }else{
                        colors.textExlight
                    }
                    ,
                    fontWeight = FontWeight.Bold
                )
            }
            if (rpc.isCustom){
                Row(
                    Modifier.padding(end = 15.dp)
                )
                {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .clickable{
                                onDeletingRPC(rpc)
                            }
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(5.dp)
                                .size(24.dp),
                            painter = painterResource(R.drawable.icon_del),
                            contentDescription = "Delete a rpc",
                            tint =  Color.Red
                        )
                    }

                }
            }
        }
    }
    Spacer(Modifier.height(15.dp))
}




