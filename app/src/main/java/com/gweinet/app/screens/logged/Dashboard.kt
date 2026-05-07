package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel



@Composable
fun Dashboard(
    context: Context,
    navController: NavController,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
){

    val wallets by appViewModel.wallets.collectAsState()
    val activeWallet by remember(wallets) {
        derivedStateOf { wallets.find { it.isActive }!! }
    }

    val networks by appViewModel.allNetworks.collectAsState()
    val activeNetwork by remember(networks) {
        derivedStateOf { networks.find { it.isActive }!! }
    }

    BackHandler {
        val popped = navController.popBackStack()
        if (!popped) { (context as Activity).finish() }
    }
    val insets = WindowInsets.systemBars.asPaddingValues()
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
    var showChains by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.navs)
            .padding(top = insets.calculateTopPadding())
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundBase)
                .padding(bottom = insets.calculateBottomPadding())
        )
        {
            Row(
                modifier = Modifier
                    .background(colors.navs)
                    .padding(bottom = 7.dp, top = 5.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                // Wallets Dropdown
                Row(
                    modifier = Modifier
                        .weight(.5f)
                        .padding(start = 15.dp),
                    horizontalArrangement = Arrangement.Start
                )
                {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable{
                                showAccounts = true
                            }
                        ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(.8f)
                                .padding(top = 7.dp, end = 0.dp, bottom = 7.dp, start = 12.dp),
                            text = activeWallet.name,
                            color = colors.textlight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))

                        Icon(
                            modifier = Modifier
                                .weight(.2f)
                                .padding(top = 7.dp, end = 8.dp, bottom = 7.dp, start = 0.dp)
                                .size(24.dp),
                            painter = painterResource(R.drawable.icon_angle_down),
                            contentDescription = "Wallet Dropdown",
                            tint = colors.textlight
                        )
                    }

                }


                Row(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .weight(.5f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                )
                {

                    // Chains Dropdown
                    Row(modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable{
                            showChains = true
                        }
                        .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFFFFFFF), RoundedCornerShape(7.dp))
                                .border(.5.dp, Color(0x675C5C5C), RoundedCornerShape(7.dp))
                                .clip(RoundedCornerShape(7.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (activeNetwork.icon == 0){
                                Row(
                                    modifier =  Modifier
                                        .padding(start = 5.dp, top = 2.dp)
                                        .border(.1.dp, Color.Black, RoundedCornerShape(10.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        modifier =  Modifier
                                            .size(15.dp)
                                            .padding(top = 1.dp)
                                        ,
                                        text = activeNetwork.name.first().toString(),
                                        fontSize = 10.sp,
                                        color= Color.Black,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 0.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                            }else{
                                Icon(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(5.dp),
                                    painter = painterResource(activeNetwork.icon),
                                    contentDescription = "Chains Logo",
                                    tint = Color.Unspecified
                                )
                            }


                            Icon(
                                modifier = Modifier
                                    .size(24.dp),
                                painter = painterResource(R.drawable.icon_dropdown),
                                contentDescription = "Chains Logo",
                                tint = Color.DarkGray
                            )
                        }
                    }

                    Spacer(Modifier.width(5.dp))
                    // Mode select
                    Row(modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable{
                            navController.navigate("Settings")
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
                                    .size(25.dp)
                                    .padding(2.dp),
                                painter = painterResource(R.drawable.icon_elipsis),
                                contentDescription = "Chains Logo",
                                tint = colors.textdark
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.fillMaxWidth().background(Color(0x37828282)).height(.5.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(10.dp, 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp)
                        ,
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "12.92",
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textdark
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            modifier = Modifier.padding(bottom = 10.dp),
                            text = "M",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textdark
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 7.dp),
                            text = "(${activeNetwork.symbol})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textlight
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(.7f)
                                .padding(bottom = 4.dp),
                            textAlign = TextAlign.Center,
                            text = "12,900,000,000 (GWEI)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textExlight
                        )
                    }

                }
            }
        }
    }


    if (showChains){
        ShowChains(
            navController,
            context,
            appViewModel,
            onDismiss = {
                showChains = false
                        },
            styleModel
        )
    }else if(showAccounts){
        ShowAccounts(
            wallets,
            navController,
            context,
            appViewModel,
            onDismiss = { showAccounts = false},
            styleModel
        )
    }

}



