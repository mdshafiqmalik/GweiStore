package com.gweinet.app.workers
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

object RpcChecker {

    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    fun check(rpcUrl: String, callback: (Boolean, Long?, String?) -> Unit) {

        val json = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("method", "eth_chainId")
            put("params", JSONArray())   // ✅ IMPORTANT
            put("id", 1)
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(rpcUrl)
            .post(body)
            .addHeader("Content-Type", "application/json") // ✅ IMPORTANT
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()

                if (!response.isSuccessful) {
                    callback(false, null, "HTTP ${response.code} → $res")
                    return
                }

                try {
                    val jsonRes = JSONObject(res ?: "")
                    val result = jsonRes.optString("result", null)

                    if (result != null) {
                        val chainId = result.removePrefix("0x").toLong(16)
                        callback(true, chainId, null)
                    } else {
                        callback(false, null, "Invalid RPC response: $res")
                    }

                } catch (e: Exception) {
                    callback(false, null, "Parse error: ${e.message}")
                }
            }
        })
    }
}