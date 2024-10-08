package com.sakura.anime.util

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider.getUriForFile
import com.sakura.anime.BuildConfig
import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.data.remote.parse.AnimeSource
import com.sakura.download.utils.decrypt
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

/**
 * 先Base64解码数据，然后再AES解密
 */
fun AnimeSource.decryptData(data: String, key: String, iv: String): String {
    val bytes = Base64.getDecoder().decode(data.toByteArray())
    val debytes = bytes.decrypt(key, iv)
    return debytes.decodeToString()
}

val AnimeSource.preferences: SharedPreferences
    get() = AnimeApplication.getInstance().preferences

/**
 * 获取默认的动漫域名
 */
fun AnimeSource.getDefaultDomain(): String {
    return preferences.getString(KEY_SOURCE_DOMAIN, DEFAULT_DOMAIN) ?: DEFAULT_DOMAIN
}

/*
fun getVersionName(context: Context): String {
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName
}*/

fun Context.installApk(file: File) {
    val intent = Intent(ACTION_VIEW)
    val authority = "$packageName.provider"
    val uri = getUriForFile(this, authority, file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}

fun <T> T.log(tag: String, prefix: String = ""): T {
    val prefixStr = if (prefix.isEmpty()) "" else "[$prefix] "
    if (BuildConfig.DEBUG) {
        if (this is Throwable) {
            Log.w(tag, prefixStr + this.message, this)
        } else {
            Log.d(tag, prefixStr + toString())
        }
    }
    return this
}

fun openExternalPlayer(videoUrl: String) {
    val context = AnimeApplication.getInstance()
    val intent = Intent(ACTION_VIEW)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    var uri = Uri.parse(videoUrl)
    if (!videoUrl.contains("http")) {
        val authority = "${context.packageName}.provider"
        uri = getUriForFile(context, authority, File(videoUrl))
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
    }
    intent.setDataAndType(uri, "video/*")
    context.startActivity(intent)
}

/**
 * 判断是否为AndroidTV
 * 用于处理AndroidTV的交互，例如遥控器
 */
fun isAndroidTV(context: Context): Boolean {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    val isTV = uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    val hasLeanbackFeature =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    return isTV || hasLeanbackFeature
}

// 判断是否为平板或大屏设备
fun isTabletDevice(context: Context): Boolean {
    val configuration = context.resources.configuration
    val screenWidthDp = configuration.smallestScreenWidthDp

    return screenWidthDp >= 600
}

/**
 * 用于处理宽屏设备布局
 */
fun isWideScreen(context: Context): Boolean {
    val configuration = context.resources.configuration
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return screenWidthDp > screenHeightDp
}

/**
 * Network util
 */
fun createDefaultHttpClient(
    clientConfig: HttpClientConfig<*>.() -> Unit = {},
) = HttpClient {
    install(HttpRequestRetry) {
        maxRetries = 1
        delayMillis { 1000 }
    }
    install(HttpCookies)
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
    }
    clientConfig()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}