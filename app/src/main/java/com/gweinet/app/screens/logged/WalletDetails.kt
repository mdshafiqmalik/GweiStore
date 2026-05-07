package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.gweinet.app.R
import com.gweinet.app.ui.theme.Colors
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.AppWallet
import com.gweinet.app.workers.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WalletDetails(
    context: Context,
    navController: NavController,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel,
    focusWallet: String
)
{
    var editName by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var showQR by remember { mutableStateOf(false) }
    val wallets by appViewModel.wallets.collectAsState()
    val focusWallet = wallets.find { it.id == focusWallet }
    BackHandler{
        if (showQR){
          showQR = false
        }else if (editName){
            editName = false
        }else if(deleteDialog){
            deleteDialog = false
        }else{
        val popped = navController.popBackStack()
        if (!popped) { (context as Activity).finish() }
        }
    }
    val colors = styleModel.appColors.value

    if (editName){
        EditName(
            context,
            appViewModel,
            focusWallet,
            colors,
            onBodyClicked = {
                editName = false
            }
        )
    }

    if (showQR){
        ShowQr(
            focusWallet,
            colors,
            onBodyClicked = {
                showQR = false
            }
        )
    }

    if (deleteDialog){
        ConfirmDelete(
            navController,
            context,
            appViewModel,
            focusWallet,
            colors,
            onBodyClicked = { goBack->
                deleteDialog = false
                if (goBack){
                    val popped = navController.popBackStack()
                    if (!popped) { (context as Activity).finish() }
                }
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.navs)
            .padding(top = styleModel.insets.calculateTopPadding())
    ){



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
                    Row(modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable{
                            val popped = navController.popBackStack()
                            if (!popped) { (context as Activity).finish() }
                        }
                        .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
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
                            .clickable{
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(top = 7.dp, end = 0.dp, bottom = 7.dp, start = 12.dp),
                            text = "Manage Wallet",
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                }
            }
            Spacer(Modifier.fillMaxWidth().background(Color(0x37828282)).height(.5.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
                {
                    Text(
                        text = "Wallet Address",
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
                            modifier = Modifier.fillMaxWidth(.7f),
                            text = focusWallet?.address ?: "0x00000000000000000000000000000000",
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(.4f)
                                .clip(RoundedCornerShape(30.dp))
                                .clickable{
                                    showQR = true
                                }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(30.dp)
                                ,
                                painter = painterResource(R.drawable.icon_qr),
                                contentDescription = "Edit wallet name",
                                tint = colors.blueTxt
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
                        text = "Wallet Name",
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
                            text = focusWallet?.name ?: "No Wallet",
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .clickable{
                                    editName = true
                                }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .padding(5.dp)
                                    .size(24.dp)
                                    ,
                                painter = painterResource(R.drawable.icon_pen),
                                contentDescription = "Edit wallet name",
                                tint = colors.blueTxt
                            )
                        }

                    }

                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                )
                {
                    Text(
                        text = "Recovery and Security",
                        color = colors.textExlight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(15.dp))
                    Column(
                        modifier = Modifier
                            .border(.1.dp, colors.borderExLight, RoundedCornerShape(10.dp))
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(start = 18.dp, top = 15.dp, end = 18.dp)
                                .fillMaxWidth()
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Text(
                                text = "Private Key",
                                color = colors.textlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_lock),
                                        contentDescription = "Edit wallet name",
                                        tint = colors.textdark
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_close_eye),
                                        contentDescription = "Edit wallet name",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }


                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .padding(start = 18.dp, top = 10.dp, end = 18.dp)
                                .fillMaxWidth()
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Text(
                                text = "Public Key",
                                color = colors.textlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_unlock),
                                        contentDescription = "Edit wallet name",
                                        tint = colors.blueTxt
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_close_eye),
                                        contentDescription = "Edit wallet name",
                                        tint = colors.blueTxt
                                    )
                                }
                            }

                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 15.dp)
                                .fillMaxWidth()
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Text(
                                text = "Secret Phrase",
                                color = colors.textlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_lock),
                                        contentDescription = "Edit wallet name",
                                        tint = colors.textdark
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                )
                                {
                                    Icon(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .padding(5.dp)
                                            .size(20.dp)
                                        ,
                                        painter = painterResource(R.drawable.icon_close_eye),
                                        contentDescription = "Edit wallet name",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }

                        }
                    }



                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                )
                {
                    Text(
                        text = "Wallet Deletion",
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
                            text = "Delete Wallet",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .clickable{
                                    deleteDialog = true
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

@Composable
fun  EditName(
    context: Context,
    appViewModel: AppViewModel,
    focusWallet: AppWallet?,
    colors: Colors,
    onBodyClicked: () -> Unit
)
{
    var newWalletName by remember { mutableStateOf(focusWallet?.name ?: "No Wallet") }
    var errorMessage by remember { mutableStateOf("") }
    val wallets by appViewModel.wallets.collectAsState()
    var isChanged by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(newWalletName) {
        val isDuplicate = wallets.any {
            it.name.equals(newWalletName, ignoreCase = true)
        }
        val isEmpty = newWalletName.isEmpty()
        errorMessage = when {
            !isChanged->"Enter New Name"
            isDuplicate && isEmpty -> ""
            isEmpty -> "Name cannot be empty"
            isDuplicate -> "Name already exists"
            else -> ""
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
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        text = "Edit Wallet Name",
                        color = colors.textdark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(15.dp))

                if (errorMessage.isNotEmpty()){
                    Row(Modifier.background(colors.warnBg).fillMaxWidth()) {
                        Row(
                            Modifier.padding(10.dp, 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.warn_circle),
                                contentDescription = "Warningn icon",
                                tint = if (isChanged) colors.warnText else colors.blueTxt
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = errorMessage,
                                color = if (isChanged) colors.warnText else colors.blueTxt,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                    Spacer(Modifier.height(15.dp))
                }


                BasicTextField(
                    value = newWalletName,
                    onValueChange = {
                        newWalletName = it
                        isChanged = true
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
                        if (newWalletName.isEmpty()) {
                            Text(
                                text = "Enter wallet name",
                                color = colors.textdark,
                                fontSize = 16.sp
                            )
                        }

                        innerTextField()
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

                    DrawButton(Color(0xFFFE4632), Color.White, "Cancel",
                        onClick = {
                            onBodyClicked()
                        }
                    )
                    if (errorMessage.isNotEmpty()){
                        DrawButton(Color(0xFF16FFD5), Color.White, "Cofirm", onClick = {})
                    }else{
                        DrawButton(Color(0xFF00FFD4), Color.White, "Cofirm",
                            onClick = {
                                scope.launch {
                                    val walletStore = WalletManager.renameWallet(context,
                                        focusWallet?.id ?: "", newWalletName)

                                    walletStore.onSuccess {
                                        appViewModel.renameWallet(focusWallet?.id ?: "", newWalletName)
                                        onBodyClicked()
                                    }.onFailure {
                                        errorMessage = "Unknown Error"
                                    }
                                }
                            }
                        )
                    }
                }
            }

        }
    }
}


@Composable
fun  ConfirmDelete(
    navController: NavController,
    context: Context,
    appViewModel: AppViewModel,
    focusWallet: AppWallet?,
    colors: Colors,
    onBodyClicked: (goBack: Boolean) -> Unit
)
{
    var errorMessage by remember { mutableStateOf("This wallet cannot be recovered without your private key or recovery phrase") }
    var errorMessage2 by remember { mutableStateOf("If you want to delete it now, please take backup of your private key or secret phrase") }
    val wallets by appViewModel.wallets.collectAsState()
    var deleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAB000000))
            .zIndex(.1f)
    ){
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onBodyClicked(false) }
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
                        text = "Are you Sure?",
                        color = colors.textdark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(15.dp))

                if (deleted){
                    Row(Modifier.background(colors.blueBg).fillMaxWidth()) {
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
                    Spacer(Modifier.height(15.dp))
                }else{
                    Row(
                        Modifier
                            .background(Color(0xFFFF4935), RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                    )
                    {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.warn_triangle),
                                contentDescription = "Warning icon",
                                tint = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = errorMessage,
                                color  = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .background(Color(0xFFFF4935), RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                    )
                    {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.warn_triangle),
                                contentDescription = "Warningn icon",
                                tint = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = errorMessage2,
                                color  = Color.White,
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
                    DrawButton(Color(0xFF00FFD4), Color.White, "Cancel",
                        onClick = {
                            onBodyClicked(false)
                        }
                    )
                    DrawButton(Color(0xFFFE4632), Color.White, "Delete",
                        onClick = {
                            scope.launch {
                                val walletStore = WalletManager.deleteWallet(context,
                                    focusWallet?.id ?: ""
                                )

                                walletStore.onSuccess {
                                    appViewModel.deleteWallet(focusWallet?.id ?: "")
                                    deleted = true
                                    delay(1000)
                                    if (wallets.size < 1){
                                        navController.navigate("setup") {
                                            popUpTo(0)
                                            launchSingleTop = true
                                        }
                                    }else{
                                        onBodyClicked(true)
                                    }




                                }.onFailure {
                                    deleted = false
                                    errorMessage = "Unknown Error"
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
fun DrawButton(
    bgColor: Color,
    txtColor: Color,
    name: String,
    onClick: () -> Unit
)
{
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
    ) {
        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .padding(horizontal = 30.dp, vertical = 13.dp),
            text = name,
            fontWeight = FontWeight.Bold,
            color = txtColor,
            fontSize = 16.sp
        )
    }
}



@Composable
fun  ShowQr(
    focusWallet: AppWallet?,
    colors: Colors,
    onBodyClicked: ()->Unit
)
{
    val walletsAddress = focusWallet?.address
    val bitmap = remember(walletsAddress) {
        generateQr(address = walletsAddress, bgColor = colors.bg0, qrColor = colors.textdark)
    }
    val scope = rememberCoroutineScope()

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
                .background(colors.bg0, RoundedCornerShape(15.dp))
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
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Text(
                        text = "Wallet QR Code",
                        color = colors.textdark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(15.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code"
                        )
                    }

                }
                Spacer(Modifier.height(20.dp))
            }

        }
    }
}

fun generateQr(
    address: String?,
    size: Int = 512,
    qrColor: Color,
    bgColor: Color
): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, size, size)

    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) qrColor.toArgb() else bgColor.toArgb()
            )
        }
    }
    return bmp
}

