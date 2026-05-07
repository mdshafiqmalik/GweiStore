package com.gweinet.app


import ImportWallet
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gweinet.app.screens.logged.AddNetwork
import com.gweinet.app.screens.logged.AuthenticatePin
import com.gweinet.app.screens.logged.Dashboard
import com.gweinet.app.screens.logged.Settings
import com.gweinet.app.screens.logged.ShowPhrase
import com.gweinet.app.screens.logged.ViewNetworks
import com.gweinet.app.screens.logged.WalletDetails
import com.gweinet.app.screens.nonlogged.LoadingScreen
import com.gweinet.app.screens.nonlogged.WalletSetup
import com.gweinet.app.ui.theme.DarkColors
import com.gweinet.app.ui.theme.LightColors
import com.gweinet.app.viewmodels.APP_NETWORKS
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.AppWallet
import com.gweinet.app.workers.WalletManager
import com.gweinet.app.workers.WalletManager.getActiveChain
import com.gweinet.app.workers.WalletManager.getActiveWallet
import com.gweinet.app.workers.WalletManager.isDark

class MainActivity : ComponentActivity() {
    private var keepSplash = true
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            keepSplash
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val styleModel: AppStyleModel = viewModel()
            val isDark by styleModel.isDarkTheme
            val view = LocalView.current
            val context = LocalContext.current
            styleModel.insets =
                WindowInsets.systemBars.asPaddingValues()
            var loggedIn by remember {
                mutableStateOf(false)
            }
            var initialized by remember {
                mutableStateOf(false)
            }

            var sDest by remember {
                mutableStateOf("setup")
            }
            var allNetworks = APP_NETWORKS

            // SYSTEM BARS

            LaunchedEffect(isDark) {
                val window =
                    (view.context as? Activity)?.window
                        ?: return@LaunchedEffect
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    false
                )
                val controller =
                    WindowCompat.getInsetsController(
                        window,
                        view
                    )
                controller.isAppearanceLightStatusBars =
                    !isDark
                controller.isAppearanceLightNavigationBars =
                    !isDark

                if (Build.VERSION.SDK_INT >= 29) {

                    window.isStatusBarContrastEnforced =
                        false

                    window.isNavigationBarContrastEnforced =
                        false
                }
            }
            // INI
            LaunchedEffect(Unit) {
                val activeChainID =
                    getActiveChain(context)
                WalletManager.initPIN(
                    appViewModel,
                    context
                )
                WalletManager.loadAllNetworks(context)
                    .onSuccess { customNetworks ->
                        allNetworks =
                            if (customNetworks.isNotEmpty()) {
                                customNetworks + APP_NETWORKS
                            } else {
                                APP_NETWORKS
                            }
                    }
                appViewModel.initNetworks(
                    allNetworks,
                    context
                )
                if (
                    allNetworks.any {
                        it.chainID == activeChainID
                    }
                ) {
                    appViewModel.setActiveNetwork(
                        activeChainID
                    )
                } else {

                    appViewModel.setActiveNetwork(137)
                }
                val activeWallet =
                    getActiveWallet(context)
                val allWallets =
                    WalletManager.loadAllWallets(context)
                if (
                    allWallets.isNotEmpty() &&
                    activeWallet == null
                ) {
                    WalletManager.setActiveWallet(
                        context,
                        allWallets[0].id
                    )
                }
                allWallets.forEach { wallet ->
                    appViewModel.addWallet(
                        AppWallet(
                            wallet.id,
                            wallet.name,
                            wallet.wallet.address,
                            wallet.id == activeWallet?.id
                        )
                    )
                }
                if (activeWallet != null) {
                    appViewModel.resetIsAuth()
                    appViewModel.setAfterAuth(
                        "dashboard"
                    )
                    appViewModel.setActiveWallet(
                        activeWallet.id
                    )
                    loggedIn = true
                    sDest = "dashboard"
                } else {
                    loggedIn = false
                    sDest = "setup"
                }
                initialized = true
                // REMOVE SPLASH
                keepSplash = false
            }

            // UI
            when {
                !initialized -> {
                    // SplashScreen still visible
                }
                loggedIn &&
                        !appViewModel.isAuth.value -> {
                    if (!appViewModel.isPin()) {
                        AuthenticatePin(
                            appViewModel,
                            styleModel,
                            "set",
                            context
                        )
                    } else {
                        AuthenticatePin(
                            appViewModel,
                            styleModel,
                            "auth",
                            context
                        )
                    }
                }
                else -> {
                    App(
                        appViewModel,
                        styleModel,
                        sDest
                    )
                }
            }
        }
    }
}
@Composable
fun App(appViewModel: AppViewModel, styleModel: AppStyleModel, startDestination: String){
    val context = LocalContext.current;
    val navController = rememberNavController()
    val isDarkStored = isDark(context)
    if (isDarkStored != null){
        styleModel.setIsDark(isDarkStored.toBoolean())
    }

    styleModel.appColors.value = if (styleModel.isDarkTheme.value == true){
        DarkColors
    }else{
        LightColors
    }


    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    )
    {


        composable("setup"){
            WalletSetup(navController, context, appViewModel, styleModel)
        }

        composable("ShowSeed"){
            ShowPhrase(navController, context, appViewModel, styleModel)
        }

        composable(
            route = "walletCreation/{dest}/{isError}/{message}/{success}",
            arguments = listOf(
                navArgument("message") { type = NavType.StringType },
                navArgument("success") { type = NavType.StringType },
                navArgument("dest") { type = NavType.StringType },
                navArgument("isError") { type = NavType.BoolType },
            )
        ) { backStackEntry ->
            val dest = backStackEntry.arguments?.getString("dest")
            val isError = backStackEntry.arguments?.getBoolean("isError")
            val message = backStackEntry.arguments?.getString("message")?:"Loading..."
            val successMessage = backStackEntry.arguments?.getString("success")?: "Successful"
            LoadingScreen(navController, dest, message, successMessage, isError, appViewModel, styleModel)
        }
        composable("ImportWallet"){
            ImportWallet(navController, context, appViewModel, styleModel)
        }



        composable("dashboard") {
            Dashboard(context, navController, appViewModel, styleModel)
        }


        composable("Settings"){
            Settings(context, navController, styleModel)
        }

        composable(
            route = "WalletDetails/{walletID}",
            arguments = listOf(navArgument("walletID") { type = NavType.StringType })
            )
        { backStackEntry ->
            val walletID = backStackEntry.arguments?.getString("walletID")!!
            WalletDetails(context, navController, appViewModel, styleModel, walletID)
        }


        composable(
            route = "ViewNetworks/{chainID}",
            arguments = listOf(navArgument("chainID") { type = NavType.StringType })
        )
        { backStackEntry ->
            val chainID = backStackEntry.arguments?.getString("chainID")!!
            ViewNetworks(context, navController, appViewModel, styleModel, chainID)
        }


        composable("AddNetworks") {
            AddNetwork(context, navController, appViewModel, styleModel)
        }
    }
}
fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}


