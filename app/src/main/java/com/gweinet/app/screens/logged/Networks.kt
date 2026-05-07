package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.ui.theme.Colors
import com.gweinet.app.viewmodels.*
import com.gweinet.app.workers.RpcChecker
import com.gweinet.app.workers.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ViewNetworks(
    context: Context,
    navController: NavController,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel,
    chainID: String
    )
{
    val networks by appViewModel.allNetworks.collectAsState()
    var addRPC by remember { mutableStateOf(false) }
    var viewRPCS by remember { mutableStateOf(false) }
    var deleteRPCDialog by remember { mutableStateOf(false) }
    var deleteNetDialog by remember { mutableStateOf(false) }
    var focusNetwork = networks.find { it.chainID.toString() == chainID}
    if (focusNetwork == null){
        focusNetwork = APP_NETWORKS[0]
    }
    var renameNetwork by remember { mutableStateOf(false) }
    val rpcs = focusNetwork.RPC
    val activeRPC = rpcs.find { it.isActive } ?: rpcs.first()
    val colors = styleModel.appColors.value

    var focusRPC by remember { mutableStateOf(activeRPC) }
    BackHandler{
        if (addRPC){
            addRPC = false
        }else if(deleteRPCDialog){
            deleteRPCDialog = false
        }else{
            val popped = navController.popBackStack()
            if (!popped) { (context as Activity).finish() }
        }
    }


    if (addRPC){
        AddNewRPC(
            focusNetwork,
            styleModel,
            focusNetwork.chainID,
            context,
            appViewModel,
            rpcs,
            colors,
            onBodyClicked = {
                addRPC = false
            }
        )
    }

    if (renameNetwork){
        RenameNetwork(
            focusNetwork.name,
            networks,
            styleModel,
            focusNetwork.chainID,
            context,
            appViewModel,
            colors,
            onBodyClicked = {
                renameNetwork = false
            }
        )
    }

    if (viewRPCS){
        ShowRpcs(
            focusNetwork.chainID,
            rpcs,
            context,

            appViewModel,
            onDismiss = {
               viewRPCS = false
            },
            onDeleteRPC = { rpc->
                deleteRPCDialog = true
                focusRPC = rpc
            },
            onAddingRPC = {
                addRPC = true
            },
            styleModel
        )
    }
    if (deleteRPCDialog){
        DeleteRPC(
            context,
            appViewModel,
            focusNetwork.chainID,
            focusRPC,
            colors,
            onBodyClicked = {
                deleteRPCDialog = false
            }
        )
    }

    if (deleteNetDialog){
        DeleteNetwork(
            context,
            appViewModel,
            focusNetwork.name,
            focusNetwork.chainID,
            colors,
            onBodyClicked = {
                deleteNetDialog = false
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.navs)
            .padding(top = styleModel.insets.calculateTopPadding())
    )
    {


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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                // Heading
                Row(
                    modifier = Modifier
                        .weight(.5f)
                        .padding(start = 10.dp),
                    horizontalArrangement = Arrangement.Start
                )
                {
                    Spacer(Modifier.width(5.dp))
                    // back
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .clickable {
                                val popped = navController.popBackStack()
                                if (!popped) {
                                    (context as Activity).finish()
                                }
                            }
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(2.dp),
                                painter = painterResource(R.drawable.icon_back_arrow),
                                contentDescription = "Go Back",
                                tint = colors.textdark
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(top = 7.dp, end = 0.dp, bottom = 7.dp, start = 12.dp),
                            text = "${focusNetwork.name} Network",
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                }
            }
            Spacer(Modifier
                .fillMaxWidth()
                .background(Color(0x37828282))
                .height(.5.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 25.dp)
                    .verticalScroll(rememberScrollState())
            ){

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                    Text(
                        text = "Network Name",
                        color = colors.textExlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 15.dp, vertical = 10.dp)
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                modifier = Modifier.padding(5.dp),
                                text = focusNetwork.name,
                                color = colors.textExlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Text(
                                modifier = Modifier.padding(5.dp),
                                text = if (focusNetwork.isMainnet) "(Mainnet)" else if(focusNetwork.isCustom) "(Custom)" else "(Testnet)",
                                color = colors.textExlight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }

                        if (focusNetwork.isCustom){
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .clickable {
                                        renameNetwork = true
                                    }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .padding(5.dp)
                                        .size(24.dp)
                                    ,
                                    painter = painterResource(R.drawable.icon_pen),
                                    contentDescription = "Edit network name",
                                    tint = colors.blueTxt
                                )
                            }

                        }

                    }

                }



                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                    Text(
                        text = "Native Symbol",
                        color = colors.textExlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 15.dp, vertical = 10.dp)
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = focusNetwork.symbol,
                            color = colors.textExlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                    Text(
                        text = "Chain ID",
                        color = colors.textExlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 15.dp, vertical = 10.dp)
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = focusNetwork.chainID.toString(),
                            color = colors.textExlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                        Text(
                            text = "RPCs",
                            color = colors.textExlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewRPCS = true
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(20.dp, 15.dp),
                                text = activeRPC.name,
                                color = colors.textlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Icon(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .padding(20.dp, 5.dp)
                                    .size(24.dp)
                                ,
                                painter = painterResource(R.drawable.icon_angle_down),
                                contentDescription = "Select Network",
                                tint = colors.textExlight
                            )
                        }
                    }

                }



                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                    Text(
                        text = "Explorer Link",
                        color = colors.textExlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 15.dp, vertical = 10.dp)
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = focusNetwork.explorer,
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                    }

                }

                if(focusNetwork.isCustom){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp)
                    )
                    {
                        Text(
                            text = "Network Deletion",
                            color = colors.textExlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(13.dp))
                        Row(
                            modifier = Modifier
                                .border(.1.dp, Color(0xFFD32F2F), RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .fillMaxWidth()
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Delete Network",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .clickable {
                                        deleteNetDialog = true
                                    }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .padding(5.dp)
                                        .size(24.dp)
                                    ,
                                    painter = painterResource(R.drawable.icon_del),
                                    contentDescription = "Edit wallet name",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}


@Composable
fun  AddNewRPC(
    network: Networks,
    styleModel: AppStyleModel,
    chainID: Long,
    context: Context,
    appViewModel: AppViewModel,
    rpcs: List<RPC>,
    colors: Colors,
    onBodyClicked: () -> Unit
)
{
    var newNetworkName by remember { mutableStateOf("RPC Link ${rpcs.size+1}") }
    var newNetworkLink by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isChangedL by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(newNetworkName) {
        val isDuplicate = rpcs.any {
            it.name.equals(newNetworkName, ignoreCase = true)
        }
        val isMax = newNetworkName.length > 15
        val isEmpty = newNetworkName.isEmpty()
        if (!isChangedL){
            errorMessage = "Enter RPC Name"
            isError = false
        }else if (isEmpty){
            errorMessage = "Name cannot be empty"
            isError = true
        }else if(isMax){
            errorMessage = "Name should be less than 15 chars"
            isError = true
        }
        else if(isDuplicate){
            errorMessage = "Name already exists"
            isError = true
        }else{
            errorMessage = "Name Given"
            isError = false
        }
    }

    LaunchedEffect(newNetworkLink) {
        val isDuplicate = rpcs.any {
            it.link.equals(newNetworkLink, ignoreCase = true)
        }
        var isHttps = newNetworkLink.startsWith("https://")
        isHttps = isHttps && Patterns.WEB_URL.matcher(newNetworkLink).matches()
        val isEmpty = newNetworkLink.isEmpty()

        if (!isChangedL){
            errorMessage = "Enter RPC endpoint Link"
            isError = false
        }else if(!isHttps){
            errorMessage = "Enter valid endpoint with https://"
            isError = true
        }else if (isEmpty){
            errorMessage = "Link cannot be empty"
            isError = true
        }else if(isDuplicate){
            errorMessage = "Link already exists"
            isError = true
        }else{
            errorMessage = "RPC Endpoint Link Given"
            isError = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAB000000))
            .zIndex(.1f)
    ){
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onBodyClicked() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
                .fillMaxHeight()
                .widthIn(min = 250.dp, max = 400.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.navs, RoundedCornerShape(15.dp))
        )
        {
            Spacer(Modifier.height(styleModel.insets.calculateTopPadding()))
            Column(
                modifier = Modifier
                    .padding(25.dp),
                horizontalAlignment = Alignment.Start
            )
            {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable {
                                onBodyClicked()
                            }
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(30.dp)
                                .padding(5.dp)

                            ,
                            painter = painterResource(R.drawable.icon_back_arrow),
                            contentDescription = "Back to Networks",
                            tint = colors.textdark
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(

                        text = "Add Custom RPC Endpoint",
                        color = colors.textdark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier
                        .background(colors.warnBg, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                )
                {
                    Row(
                        Modifier.padding(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.warn_triangle),
                            contentDescription = "Warningn icon",
                            tint = colors.warnText
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Custom RPC can return false data or trick you into unsafe transactions use only trusted endpoints ",
                            color = colors.warnText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Spacer(Modifier.height(15.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )
                {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 5.dp),
                        text = "RPC Name : ",
                        fontSize = 16.sp,
                        color = colors.textlight
                    )
                    Text(
                        modifier = Modifier
                            .padding(vertical = 5.dp),
                        text = newNetworkName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textlight
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )
                {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 5.dp),
                        text = "Network : ",
                        fontSize = 16.sp,
                        color = colors.textlight
                    )
                    Text(
                        modifier = Modifier
                            .padding(vertical = 5.dp),
                        text = "${network.name} (${network.chainID})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textlight
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier
                        .background(colors.warnBg, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                )
                {
                    Row(
                        Modifier.padding(10.dp, 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.warn_circle),
                            contentDescription = "Warningn icon",
                            tint = if (isError) colors.warnText else colors.blueTxt
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = errorMessage,
                            color = if (isError) colors.warnText else colors.blueTxt,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
                Spacer(Modifier.height(10.dp))
                BasicTextField(
                    value = newNetworkLink,
                    onValueChange = {
                        newNetworkLink = it
                        isChangedL = true
                    },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textlight
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = colors.borderExLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )
                { innerTextField ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp) // ✅ inner padding control
                            .fillMaxWidth()
                    ) {

                        // Placeholder (optional)
                        if (newNetworkLink.isEmpty()) {
                            Text(
                                text = "https://",
                                color = colors.textExlight,
                                fontSize = 16.sp
                            )
                        }

                        innerTextField()
                    }
                }

                Spacer(Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawButton(Color(0xFFFE4632), Color.White, "Cancel",
                        onClick = {
                            onBodyClicked()
                        }
                    )
                    DrawButton(Color(0xFF00FFD4), Color.White, "Cofirm",
                        onClick = {
                            errorMessage = "Checking Link..."
                            scope.launch {
                                RpcChecker.check(newNetworkLink) { ok, chainId, error ->
                                    if (ok){
                                        if (chainId == chainID){
                                            val result = WalletManager.saveRPC(
                                                context,
                                                chainID,
                                                newNetworkName,
                                                newNetworkLink
                                            )
                                            result.onSuccess {
                                                errorMessage = "Added Successfully"
                                                WalletManager.setActiveRPC(context, chainID, newNetworkLink)
                                                appViewModel.addRpcToNetwork(chainID, RPC(
                                                    newNetworkName, newNetworkLink, false, true
                                                ))
                                                onBodyClicked()
                                            }.onFailure {
                                                errorMessage = "Unknown error while saving rpc link"
                                                isError = true
                                            }
                                            isError = false
                                        }else{
                                            errorMessage = "Wrong Network RPC used"
                                            isError = true
                                        }
                                    }else{
                                        errorMessage = "Error $error"
                                        isError = true
                                    }
                                }
                            }
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun  DeleteRPC(
    context: Context,
    appViewModel: AppViewModel,
    chainID: Long,
    focusRPC: RPC,
    colors: Colors,
    onBodyClicked: () -> Unit
)
{
    var deleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAB000000))
            .zIndex(.1f)
    ){
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onBodyClicked() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
                .padding(20.dp)
                .widthIn(min = 250.dp, max = 400.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.navs, RoundedCornerShape(15.dp))
        )
        {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            )
            {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        text = "Do you really want to delete this RPC?",
                        color = colors.textdark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                }
                if (error){
                    Spacer(Modifier.height(15.dp))

                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(10.dp, 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.warn_circle),
                                contentDescription = "Warning icon",
                                tint = colors.warnText
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Unknown error while deleting rpc",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }

                }
                Spacer(Modifier.height(15.dp))
                if (deleted){
                    Row(Modifier
                        .background(colors.blueBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Deleted Successfully!",
                                color = colors.blueTxt,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }else{
                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(start = 10.dp, end = 15.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Name: ${focusRPC.name}",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Link: ${focusRPC.link}",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }


                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawButton(Color(0xFF00FFD4), Color.White, if(deleted) "Go Back" else "Cancel",
                        onClick = {
                            onBodyClicked()
                        }
                    )
                    DrawButton(Color(0xFFFE4632), Color.White, if (deleted) "Deleted" else "Delete",
                        onClick = {
                            scope.launch {
                                if (!deleted){
                                    WalletManager.deleteRPC(context, chainID, focusRPC.link)
                                        .onSuccess {
                                            appViewModel.deleteRpc(chainID, focusRPC.link)
                                            error = false
                                            deleted = true
                                            delay(1000)
                                            onBodyClicked()
                                        }
                                        .onFailure {
                                            error = true
                                        }
                                }

                            }
                        }
                    )
                }
            }

        }
    }
}


@Composable
fun  DeleteNetwork(
    context: Context,
    appViewModel: AppViewModel,
    networkName:String,
    chainID: Long,
    colors: Colors,
    onBodyClicked: () -> Unit
)
{
    var deleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAB000000))
            .zIndex(.1f)
    ){
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onBodyClicked() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
                .padding(20.dp)
                .widthIn(min = 250.dp, max = 400.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.navs, RoundedCornerShape(15.dp))
        )
        {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            )
            {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        text = "Do you really want to delete this Network?",
                        color = colors.textdark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (error){
                    Spacer(Modifier.height(15.dp))

                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(10.dp, 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.warn_circle),
                                contentDescription = "Warning icon",
                                tint = colors.warnText
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Unknown error while deleting network",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }

                }
                Spacer(Modifier.height(15.dp))
                if (deleted){
                    Row(Modifier
                        .background(colors.blueBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Deleted Successfully!",
                                color = colors.blueTxt,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }else
                {
                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(start = 10.dp, end = 15.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Name: $networkName",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(Modifier
                        .background(colors.warnBg)
                        .fillMaxWidth()) {
                        Row(
                            Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(Modifier.width(7.5.dp))
                            Text(
                                text = "Chain: ${chainID}",
                                color = colors.warnText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }


                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawButton(Color(0xFF00FFD4), Color.White, if(deleted) "Go Back" else "Cancel",
                        onClick = {
                            onBodyClicked()
                        }
                    )
                    DrawButton(Color(0xFFFE4632), Color.White, if (deleted) "Deleted" else "Delete",
                        onClick = {
                            scope.launch {
                                if (!deleted){
                                    WalletManager.deleteNetwork(context,chainID)
                                        .onSuccess {
                                            appViewModel.deleteNetwork(chainID)
                                            if(appViewModel.activeChainID == chainID){
                                                WalletManager.setActiveChain("137", context)
                                                appViewModel.setActiveNetwork(137)
                                            }
                                            error = false
                                            deleted = true
                                            delay(1000)
                                            onBodyClicked()
                                        }
                                        .onFailure {
                                            error = true
                                        }
                                }
                            }
                        }
                    )
                }
            }

        }
    }
}


@Composable
fun  RenameNetwork(
    networkName: String,
    network: List<Networks>,
    styleModel: AppStyleModel,
    chainID: Long,
    context: Context,
    appViewModel: AppViewModel,
    colors: Colors,
    onBodyClicked: () -> Unit
)
{
    var newNetworkName by remember { mutableStateOf(networkName) }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isChangedL by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(newNetworkName) {
        val isDuplicate = network.any {
            it.name.equals(newNetworkName, ignoreCase = true)
        }
        val isMax = newNetworkName.length > 15
        val isEmpty = newNetworkName.isEmpty()
        if (!isChangedL){
            errorMessage = "Enter Network Name"
            isError = false
        }else if (isEmpty){
            errorMessage = "Name cannot be empty"
            isError = true
        }else if(isMax){
            errorMessage = "Name should be less than 15 chars"
            isError = true
        } else if(isDuplicate && (newNetworkName != networkName)){
            errorMessage = "Name already exists"
            isError = true
        }else{
            errorMessage = "Name Given"
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAB000000))
            .zIndex(.1f)
    ){
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onBodyClicked() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
                .widthIn(min = 250.dp, max = 400.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.navs, RoundedCornerShape(15.dp))
        )
        {
            Column(
                modifier = Modifier
                    .padding(25.dp),
                horizontalAlignment = Alignment.Start
            )
            {
                Spacer(Modifier.width(20.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                )
                {


                    Text(

                        text = "Change Network Name",
                        color = colors.textdark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(20.dp))



                Row(
                    Modifier
                        .background(if (isError) colors.warnBg else colors.blueBg, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                )
                {
                    Row(
                        Modifier.padding(10.dp, 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.warn_circle),
                            contentDescription = "Warningn icon",
                            tint = if (isError) colors.warnText else colors.blueTxt
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = errorMessage,
                            color = if (isError) colors.warnText else colors.blueTxt,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
                Spacer(Modifier.height(20.dp))
                BasicTextField(
                    value = newNetworkName,
                    onValueChange = {
                        newNetworkName = it
                        isChangedL = true
                    },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textlight
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = colors.borderExLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )
                { innerTextField ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 14.dp) // ✅ inner padding control
                            .fillMaxWidth()
                    ) {

                        // Placeholder (optional)
                        if (newNetworkName.isEmpty()) {
                            Text(
                                text = "e.g. Ethereum Network",
                                color = colors.textExlight,
                                fontSize = 16.sp
                            )
                        }

                        innerTextField()
                    }
                }

                Spacer(Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawButton(Color(0xFFFE4632), Color.White, "Cancel",
                        onClick = {
                            onBodyClicked()
                        }
                    )
                    DrawButton(Color(0xFF00FFD4), Color.White, "Rename",
                        onClick = {
                            scope.launch {
                                WalletManager.renameNetwork(context, chainID, newNetworkName)
                                    .onSuccess {
                                        appViewModel.renameNetwork(chainID, newNetworkName)
                                        isError = false
                                        errorMessage = "Renamed Successfully"
                                        onBodyClicked()
                                    }
                                    .onFailure {
                                        isError = true
                                        errorMessage = "Cannot change rename due to unknown error"
                                    }
                            }
                        }
                    )
                }
            }

        }
    }
}
