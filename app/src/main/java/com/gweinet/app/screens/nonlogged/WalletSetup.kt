package com.gweinet.app.screens.nonlogged

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.workers.generateEthereumWallet
import kotlinx.coroutines.launch

@Composable
fun WalletSetup(
    navController: NavController,
    context: Context,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
){
    BackHandler {
        val popped = navController.popBackStack()

        if (!popped) {
            (context as Activity).finish()
        }
    }
    val insets = WindowInsets.systemBars.asPaddingValues()
    val colors by styleModel.appColors
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    fun createWallet() {
        try {
            val wallet = generateEthereumWallet("", null)
            appViewModel.setTempWallet(wallet)
            navController.navigate("walletCreation/${"ShowSeed"}/${false}/${"Creating Wallet..."}/${"Created Successfully"}") {
                launchSingleTop = true
            }
        } catch (e: Exception) {
            appViewModel.e = e
            navController.navigate("walletCreation/${"Setup"}/${true}/${""}/${""}"){
                launchSingleTop = true
            }
        }
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundBase)
                .padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding())

        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Column{
                    Image(
                        modifier = Modifier
                            .fillMaxWidth(.70f),
                        painter = painterResource(R.drawable.icon_account),
                        contentDescription = null
                    )
                }
                Spacer(Modifier.height(40.dp))
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(20.dp))

                    AnimatedButton("Create New Wallet", true) {
                        scope.launch {
                            createWallet()
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    AnimatedButton("Import Existing Wallet", true) {
                        scope.launch {
                          navController.navigate("ImportWallet")
                        }
                    }
                }
            }
        }
    }


@Composable
fun AnimatedButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }

    val enabledColor = Color(0xFF64CEBD)
    val clickedColor = Color(0xFF4B9A8F)
    val disabledColor = Color(0xFFB0BEC5)

    val color by animateColorAsState(
        targetValue = when {
            !enabled -> disabledColor
            clicked -> clickedColor
            else -> enabledColor
        },
        animationSpec = tween(300),
        label = ""
    )


    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .fillMaxWidth(.9f)
            .clickable(enabled = enabled) {
                onClick()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(vertical = 15.dp),
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}