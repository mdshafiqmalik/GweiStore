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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.ui.theme.DarkColors
import com.gweinet.app.ui.theme.LightColors
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.workers.WalletManager

@Composable
fun Settings(
    context: Context,
    navController: NavController,
    styleModel: AppStyleModel
){
    BackHandler {
        val popped = navController.popBackStack()
        if (!popped) { (context as Activity).finish() }
    }
    val insets = WindowInsets.systemBars.asPaddingValues()
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
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
                            text = "Settings",
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
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.bg0, RoundedCornerShape(10.dp))
                        .border(.1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .padding(15.dp, 15.dp)
                )
                {
                    Text(
                        text = "Select App Theme",
                        fontSize = 16.sp,
                        color= colors.textdark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Row(
                            modifier = Modifier
                                .background(colors.bg1, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    color = if(styleModel.appColors.value == DarkColors ) Color( 0xFF00FFFC) else Color.Unspecified,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable{
                                    styleModel.setIsDark(true)
                                    WalletManager.setIsDark(true, context)
                                    styleModel.appColors.value = DarkColors
                                }
                        )
                        {
                            Text(
                                modifier = Modifier
                                    .padding(top = 11.dp, start = 16.dp, bottom = 10.dp),
                                text = "Dark Mode",
                                color=colors.textdark,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                modifier = Modifier
                                    .padding(top = 11.dp, start = 10.dp, bottom = 10.dp, end = 16.dp)
                                    .size(16.dp),
                                painter = painterResource(R.drawable.icon_moon),
                                contentDescription = "Select Dark Mode",
                                tint = colors.textdark
                            )
                        }

                        Row(
                            modifier = Modifier
                                .background(colors.bg1, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    color = if(styleModel.appColors.value == LightColors ) Color( 0xFF00FFFC) else Color.Unspecified,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable{
                                    styleModel.setIsDark(false)
                                    WalletManager.setIsDark(
                                        false, context)
                                    styleModel.appColors.value = LightColors
                                }
                        )
                        {
                            Text(
                                modifier = Modifier
                                    .padding(top = 11.dp, start = 16.dp, bottom = 10.dp),
                                text = "Light Mode",
                                color=colors.textdark,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                modifier = Modifier
                                    .padding(top = 11.dp, start = 10.dp, bottom = 10.dp, end = 16.dp)
                                    .size(16.dp),
                                painter = painterResource(R.drawable.icon_sun),
                                contentDescription = "Select Dark Mode",
                                tint = colors.textdark
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.bg0, RoundedCornerShape(10.dp))
                        .border(.1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .clickable{
                            navController.navigate("AddNetworks")
                        }
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        modifier = Modifier.padding(15.dp, 15.dp),
                        text = "Add Custom Network",
                        fontSize = 16.sp,
                        color= colors.textdark,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        modifier = Modifier
                            .padding(20.dp, 15.dp)
                            .rotate(45f),
                        painter = painterResource(R.drawable.icon_close),
                        contentDescription = "Add Netwrok",
                        tint = colors.textdark
                    )
                }



            }
        }

    }
}