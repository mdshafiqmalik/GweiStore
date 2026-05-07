package com.gweinet.app.screens.nonlogged

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import kotlinx.coroutines.delay



@Composable
fun LoadingScreen(
    navController: NavController,
    destination: String?,
    message: String,
    Success: String,
    isError: Boolean?,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
){
    val insets = WindowInsets.systemBars.asPaddingValues()
    val colors by styleModel.appColors
    var showLoader by remember { mutableStateOf(true) }
    val destination = destination?: "ShowSeed"
    val isError = isError?:false
    LaunchedEffect(Unit) {
        delay(1000)
        showLoader = false
    }

    LaunchedEffect(Unit) {
        delay(1800)
        navController.navigate(destination)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
            .padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding())
    ){
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PremiumLoader(!showLoader,isError)

            Spacer(Modifier.height(40.dp))

            Text(
                text = if (showLoader) message else if (isError) "Error: ${appViewModel.e?.message}" else Success,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textdark
            )
        }
    }
}



