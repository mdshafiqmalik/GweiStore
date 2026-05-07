package com.gweinet.app.screens.logged

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gweinet.app.R
import com.gweinet.app.screens.nonlogged.AnimatedButton
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.workers.WalletManager


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AuthenticatePin(
    appViewModel: AppViewModel,
    styleModel: AppStyleModel,
    purpose: String,
    context: Context
) {

    val pinList = remember { mutableStateListOf<Int>() }
    val pinList2 = remember { mutableStateListOf<Int>() }
    val rePinList = remember { mutableStateListOf<Int>() }

    var isRepin by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }

    var errorMessage by remember {
        mutableStateOf(
            if (purpose == "set")
                "Create a new PIN"
            else
                "Enter PIN"
        )
    }

    val colors by styleModel.appColors

    val currentPin =
        if (isRepin) rePinList else pinList

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
            .padding(
                top = styleModel.insets.calculateTopPadding(),
                bottom = styleModel.insets.calculateBottomPadding()
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                modifier = Modifier
                    .weight(.4f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(R.drawable.icon_lock),
                    contentDescription = "Lock",
                    tint = if (isError) Color.Red else colors.textdark
                )

                Spacer(Modifier.height(30.dp))

                Text(
                    text = errorMessage,
                    fontSize = 18.sp,
                    color = if (isError) Color.Red else colors.textdark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(40.dp))

                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth(.6f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    repeat(6) { index ->

                        val filled = index < currentPin.size

                        val scale = remember {
                            Animatable(1f)
                        }

                        LaunchedEffect(filled) {

                            if (filled) {

                                scale.snapTo(1.6f)

                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.4f,
                                        stiffness = 400f
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .scale(scale.value)
                                .size(
                                    if (filled) 14.dp else 10.dp
                                )
                                .background(
                                    if (filled)
                                        colors.textdark
                                    else
                                        colors.bg2,
                                    RoundedCornerShape(20.dp)
                                )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            Column(
                modifier = Modifier
                    .weight(.6f)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                val boxMod = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .border(
                        .1.dp,
                        color = colors.bg2,
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        colors.bg1,
                        RoundedCornerShape(50.dp)
                    )
                    .size(80.dp)

                fun addDigit(digit: Int) {
                    if (currentPin.size < 12) {
                        currentPin.add(digit)
                    }
                }

                @Composable
                fun numberRow(range: IntRange) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        for (i in range) {

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                Row(
                                    modifier = boxMod.clickable {
                                        addDigit(i)
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {

                                    Text(
                                        text = i.toString(),
                                        fontSize = 25.sp,
                                        color = colors.textdark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                numberRow(1..3)
                numberRow(4..6)
                numberRow(7..9)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    // BACKSPACE

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Row(
                            modifier = boxMod.clickable {

                                if (currentPin.isNotEmpty()) {
                                    currentPin.removeLast()
                                }
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Icon(
                                modifier = Modifier.size(35.dp),
                                painter = painterResource(R.drawable.icon_backspace),
                                contentDescription = "Backspace",
                                tint = colors.textdark
                            )
                        }
                    }

                    // 0

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Row(
                            modifier = boxMod.clickable {
                                addDigit(0)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "0",
                                fontSize = 25.sp,
                                color = colors.textdark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // CHECK

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Row(
                            modifier = boxMod.clickable {

                                if (purpose == "set") {
                                    // FIRST PIN ENTRY
                                    if (!isRepin) {
                                        if (pinList.size < 6) {
                                            isError = true
                                            errorMessage = "Min 6 digits"
                                        } else {
                                            pinList2.clear()
                                            pinList2.addAll(pinList)
                                            pinList.clear()
                                            isRepin = true
                                            isError = false
                                            errorMessage = "Enter PIN again"
                                        }
                                    } else {
                                        // CONFIRM PIN
                                        val firstPin =
                                            pinList2.joinToString("")
                                        val secondPin =
                                            rePinList.joinToString("")
                                        if (secondPin.length < 6) {
                                            isError = true
                                            errorMessage = "Min 6 digits"
                                        } else if (firstPin != secondPin) {
                                            isError = true
                                            errorMessage = "PIN not matched"
                                        } else {

                                            if(WalletManager.setPIN(firstPin, context, appViewModel)){
                                                isError = false
                                                errorMessage = "PIN Set Successfully"
                                            }else{
                                                isError = true
                                                errorMessage = "PIN not Set"
                                            }
                                        }
                                    }
                                } else {
                                    // AUTH MODE
                                    if (pinList.size < 6) {
                                        isError = true
                                        errorMessage = "Min 6 digits"
                                    } else {
                                        val enteredPIN =
                                            pinList.joinToString("")
                                        val auth =
                                            appViewModel.AuthPIN(
                                                enteredPIN.toLong()
                                            )
                                        if (!auth.isAuth) {
                                            isError = true
                                            errorMessage = "Wrong PIN"

                                            pinList.clear()
                                        } else {
                                            isError = false
                                            errorMessage = "Authenticated"
                                        }
                                    }
                                }
                            }
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Icon(
                                modifier = Modifier.size(35.dp),
                                painter = painterResource(R.drawable.icon_checkmark),
                                contentDescription = "Check",
                                tint = colors.textdark
                            )
                        }
                    }
                }
            }
        }
    }
}
