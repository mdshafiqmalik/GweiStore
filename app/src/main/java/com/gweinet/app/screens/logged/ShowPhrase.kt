package com.gweinet.app.screens.logged

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gweinet.app.R
import com.gweinet.app.findActivity
import com.gweinet.app.screens.nonlogged.AnimatedButton
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.AppWallet
import com.gweinet.app.workers.WalletData
import com.gweinet.app.workers.WalletManager
import com.gweinet.app.workers.WalletManager.getActiveWallet
import kotlinx.coroutines.launch


@Composable
fun ShowPhrase(
    navController: NavController,
    context: Context,
    appViewModel: AppViewModel,
    styleModel: AppStyleModel
){
    val activity = context.findActivity()
    BackHandler {
        val popped = navController.popBackStack()

        if (!popped) {
            (context as Activity).finish()
        }
    }
    val wallets by appViewModel.wallets.collectAsState()
    val insets = WindowInsets.systemBars.asPaddingValues()
    val colors by styleModel.appColors
    val tempWallet = appViewModel.tWallet ?: WalletData("","","","")
    val names = wallets.map { it.name }
    var newWalletName by remember { mutableStateOf("Main Wallet ${wallets.size+1}") }

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    val isError by remember(newWalletName) {
        derivedStateOf {
            names.any { it.equals(newWalletName, ignoreCase = true) }
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
                .padding(10.dp, 0.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textdark
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 10.dp, bottom= 5.dp)
            ) {


                Row(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 8.dp)
                ) {

                    if (isError){
                        Text(
                            modifier = Modifier
                                .padding(10.dp, 0.dp),
                            text = "Name Already Exists",
                            color = Color(0xFFFF6200),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                BasicTextField(
                    value = newWalletName,
                    onValueChange = { newWalletName = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textlight
                    ),
                    cursorBrush = SolidColor(colors.textlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .border(
                            width = 0.5.dp,
                            color = colors.borderExLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                ) { innerTextField ->

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
            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top= 10.dp, bottom = 7.dp),
                    text="Wallet Address",
                    color= colors.textlight,
                    fontWeight = FontWeight.Bold
                )
                SelectionContainer {
                    Row(
                        modifier = Modifier
                            .clickable {
                                clipboardManager.setText(AnnotatedString(tempWallet.address))
                                Toast.makeText(context, "Copied! ${tempWallet.address}", Toast.LENGTH_SHORT).show()
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, colors.borderExLight, RoundedCornerShape(12.dp)),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(15.dp, 10.dp),
                            fontWeight = FontWeight.Bold,
                            text= tempWallet.address,
                            color= colors.textlight,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(top=15.dp, start = 10.dp, end = 10.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top= 10.dp, bottom = 0.dp),
                    text="Secret Recovery Phrase",
                    color= colors.textlight,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .padding(top=15.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(0.5.dp, colors.borderExLight, shape = RoundedCornerShape(12.dp))
                        .background(color = colors.backgroundBase),
                ) {
                    val words = tempWallet.mnemonic?.split(" ")

                    SelectionContainer {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            words?.forEachIndexed { _, word ->
                                Text(
                                    text = word,
                                    color = colors.textlight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }


            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {

                Modifier.height(10.dp)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .clickable{
                            check2 = true
                        }
                        .background(color = if (check2) colors.blueBg else colors.warnBg, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end= 8.dp, top = 3.dp)
                                .size(24.dp),
                            painter = painterResource(R.drawable.icon_checkmark),
                            contentDescription = "I have securely written down my recovery phrase offline.",
                            tint = if (check2) colors.blueTxt else colors.textlight
                        )

                        Text(
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .padding(end = 8.dp),
                            text ="I have securely written down my recovery phrase offline.",
                            color = colors.textlight
                        )
                    }

                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .clickable{
                            check1 = true
                        }

                        .background(color = if (check1) colors.blueBg else colors.warnBg, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end= 8.dp, top = 3.dp)
                                .size(24.dp),
                            painter = painterResource(R.drawable.icon_checkmark),
                            contentDescription = "Recovery Phrase is the only way to recover and access wallet",
                            tint = if (check1) colors.blueTxt else colors.textlight
                        )

                        Text(
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .padding(end = 8.dp),
                            text ="This is the ONLY way to recover my wallet. If I lose it, my funds and activity cannot be recovered.",
                            color = colors.textlight
                        )
                    }

                }
            }

          Column(
              modifier = Modifier
                  .fillMaxSize()
                  .weight(1f)
                  .padding(start = 10.dp, top = 10.dp, bottom = 30.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom
          ) {
              AnimatedButton("Save Wallet", (check1 && check2 && !isError)) {
                  if (check1 && check2 && !isError){
                      scope.launch {
                          val walletStore = WalletManager.saveWallet(context, tempWallet, newWalletName)

                          walletStore.onSuccess { walletId ->
                              WalletManager.setActiveWallet(context, walletId)
                              val newWallet = AppWallet(walletId,newWalletName, tempWallet.address)
                              appViewModel.addWallet(newWallet)
                              appViewModel.setActiveWallet(walletId)
                              navController.navigate("dashboard") {
                                  popUpTo(0)
                                  launchSingleTop = true
                              }
                          }.onFailure {
                              navController.navigate("Setup")
                          }
                      }
                  }

              }
          }
        }
    }
}

