import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.Space
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cash.z.ecc.android.bip39.Mnemonics
import com.gweinet.app.R
import com.gweinet.app.findActivity
import com.gweinet.app.screens.nonlogged.AnimatedButton
import com.gweinet.app.viewmodels.AppStyleModel
import com.gweinet.app.viewmodels.AppViewModel
import com.gweinet.app.viewmodels.AppWallet
import com.gweinet.app.workers.WalletData
import com.gweinet.app.workers.WalletManager
import com.gweinet.app.workers.WalletManager.getActiveWallet
import com.gweinet.app.workers.generateEthereumWallet
import kotlinx.coroutines.launch
import java.math.BigInteger


@Composable
fun ImportWallet(
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
    val colors by styleModel.appColors
    var tempWallet: WalletData
    var newWalletName by remember { mutableStateOf("Main Wallet ${appViewModel._wallets.value.size+1}") }
    var isPrivateKey by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var check1 by remember { mutableStateOf(false) }
    var secretPhrase by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    val wallets by appViewModel.wallets.collectAsState()
    var errorMessage by remember { mutableStateOf("") }
    LaunchedEffect(newWalletName) {
        val isDuplicate = wallets.any {
            it.name.equals(newWalletName, ignoreCase = true)
        }
        val isEmpty = newWalletName.isEmpty()
        errorMessage = when {
            isDuplicate && isEmpty -> ""
            isEmpty -> "Name cannot be empty"
            isDuplicate -> "Wallet already exists"
            else -> ""
        }
    }
    LaunchedEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    LaunchedEffect(isPrivateKey, privateKey, secretPhrase) {
        if (isPrivateKey){
            val cleanKey = privateKey
                .removePrefix("0x")
                .trim()
                .replace(Regex("\\r?\\n"), "")

            errorMessage = try {
                when {
                    cleanKey.length != 64 -> "Private key Must be 64 hex chars"

                    !cleanKey.matches(Regex("^[0-9a-fA-F]{64}$"))-> "Invalid Hex Format"

                    else->{
                        val bigint = BigInteger(cleanKey, 16)
                        val max = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141",16)
                        if (bigint <= BigInteger.ZERO || bigint >= max){
                            "Invalid Private Key"
                        }else{
                            ""
                        }
                    }
                }
            }catch (e: Exception){
                "Invalid Private Key"
            }
        }else{
            val phraseList = secretPhrase
                .lowercase()
                .trim()
                .replace(Regex("\\r?\\n"), " ")
                .split("\\s+".toRegex())
            errorMessage = when {
                phraseList.size != 12 && phraseList.size != 24&& phraseList.size != 15->{
                    "12,15 or 24 words required"
                }
                else ->{
                    try {
                        Mnemonics.MnemonicCode(secretPhrase).validate()
                        ""
                    }catch (e: Exception){
                        "Invalid Recovery Phrase"
                    }

                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
            .padding(top = styleModel.insets.calculateTopPadding(), bottom = styleModel.insets.calculateBottomPadding())

    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 0.dp),
        )
        {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(
                    text = "Import Wallet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textdark
                )
            }


            Column(
                modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp, bottom= 5.dp)
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, 10.dp)
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (errorMessage.isNotEmpty())
                                    colors.warnBg else colors.blueBg
                                    , RoundedCornerShape(5.dp))
                                .padding(16.dp, 12.dp)
                        ) {
                            if (errorMessage.isNotEmpty()){
                                Text(
                                    text = "Error : ${errorMessage}",
                                    color = colors.warnText,
                                    fontWeight = FontWeight.Bold
                                )
                            }else{
                                Text(
                                    text = if(isPrivateKey) "✔  Valid Private Key" else "✔  Valid Secret Phrase",
                                    color = colors.blueTxt,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                        }


                    }
                    Spacer(Modifier.height(10.dp))

                    BasicTextField(
                        value = newWalletName,
                        onValueChange = {
                            newWalletName = it
                                        },
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
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top=15.dp, start = 10.dp, end = 10.dp)
                )
                {
                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top= 10.dp, bottom = 0.dp),
                        text="Import Using",
                        color= colors.textlight,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    color = Color( 0xFF00FFFC),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable{
                                    isPrivateKey = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Text(
                                modifier = Modifier
                                    .padding(top = 13.dp, start = 16.dp, bottom = 13.dp),
                                text = "Secret Phrase",
                                color=colors.textdark,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isPrivateKey){
                                Icon(
                                    modifier = Modifier
                                        .padding(top = 10.dp, bottom = 10.dp, end = 16.dp, start = 10.dp)
                                        .size(22.dp),
                                    painter = painterResource(R.drawable.icon_checkmark),
                                    contentDescription = "Select Dark Mode",
                                    tint = Color( 0xFF00FFFC)
                                )
                            }else{
                                Spacer(Modifier.width(16.dp))
                            }

                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    color = Color( 0xFF00FFFC),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable{
                                    isPrivateKey = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Text(
                                modifier = Modifier
                                    .padding(top = 13.dp, start = 16.dp, bottom = 13.dp),
                                text = "Private Key",
                                color=colors.textdark,
                                fontWeight = FontWeight.Bold
                            )

                            if (isPrivateKey){
                                Icon(
                                    modifier = Modifier
                                        .padding(top = 10.dp, bottom = 10.dp, end = 16.dp, start = 10.dp)
                                        .size(22.dp),
                                    painter = painterResource(R.drawable.icon_checkmark),
                                    contentDescription = "Select Dark Mode",
                                    tint = Color( 0xFF00FFFC)
                                )
                            }else{
                                Spacer(Modifier.width(16.dp))
                            }
                        }
                    }

                }

                if (isPrivateKey)
                {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top=10.dp, start = 10.dp, end = 10.dp)
                    )
                    {
                        Text(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top= 10.dp, bottom = 0.dp),
                            text="Enter Private Key",
                            color= colors.textlight,
                            fontWeight = FontWeight.Bold
                        )

                        BasicTextField(
                            singleLine = true,
                            value = privateKey,
                            onValueChange = {
                                privateKey = it.replace(Regex("\\r?\\n"), "").trim()
                            },
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textlight
                            ),
                            cursorBrush = SolidColor(colors.textlight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp)
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
                                if (privateKey.isEmpty()) {
                                    Text(
                                        text = "Enter Private key e.g. a8b23ae......",
                                        color = colors.textExlight,
                                        fontSize = 16.sp
                                    )
                                }

                                innerTextField()
                            }
                        }
                    }
                }
                else
                {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top=10.dp, start = 10.dp, end = 10.dp)
                    )
                    {
                        Text(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top= 10.dp, bottom = 0.dp),
                            text="Enter Secret Recovery Phrase",
                            color= colors.textlight,
                            fontWeight = FontWeight.Bold
                        )
                        BasicTextField(
                            value = secretPhrase,
                            onValueChange = {
                                secretPhrase = it.replace(Regex("\\r?\\n"), " ")
                            },
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textlight
                            ),
                            cursorBrush = SolidColor(colors.textlight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp)
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
                                if (secretPhrase.isEmpty()) {
                                    Text(
                                        text = "Enter 12 or 24 mnemonic words",
                                        color = colors.textExlight,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                )
                {
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
                                text ="I verify that I own this wallet and I understand if someone have this private key or secret phrase he can gain full access of my wallet",
                                color = colors.textlight
                            )
                        }

                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp, bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                )
                {
                    AnimatedButton("Import Wallet", (check1 && errorMessage.isEmpty())) {
                        if (check1 && errorMessage.isEmpty()){
                            scope.launch {

                                tempWallet = if (isPrivateKey){
                                    generateEthereumWallet("",privateKey)
                                }else{
                                    generateEthereumWallet(secretPhrase.lowercase().trim(), null)
                                }

                                if (wallets.any { it.address.equals(tempWallet.address, ignoreCase = true) }) {
                                    errorMessage = "Wallet Already Imported"
                                    return@launch
                                }
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
                                }.onFailure {e->
                                    Log.e("Wallet Error:", newWalletName, e)
                                    navController.navigate("Setup")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}