package com.example.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import com.example.BuildConfig

data class AppUpdateCheckResult(
    val versionName: String,
    val versionCode: Int,
    val packageName: String,
    val isOnline: Boolean,
    val installerLabel: String,
    val statusTitle: String,
    val statusMessage: String
)

object AppUpdateChecker {

    fun inspectAppUpdateStatus(context: Context): AppUpdateCheckResult {
        val online = isNetworkConnected(context)
        val installer = resolveInstallerName(context)
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val pkg = context.packageName

        return if (!online) {
            AppUpdateCheckResult(
                versionName = versionName,
                versionCode = versionCode,
                packageName = pkg,
                isOnline = false,
                installerLabel = installer,
                statusTitle = "Offline — Cannot Check Store",
                statusMessage = "You are currently on v$versionName (Build $versionCode). Connect to Wi-Fi or mobile data to check Google Play for newer releases."
            )
        } else {
            AppUpdateCheckResult(
                versionName = versionName,
                versionCode = versionCode,
                packageName = pkg,
                isOnline = true,
                installerLabel = installer,
                statusTitle = "You're on v$versionName (Build $versionCode)",
                statusMessage = "Your installation is active and up to date with this release channel ($installer). Tap below to verify availability on Google Play."
            )
        }
    }

    fun openStoreForUpdates(context: Context): Boolean {
        val pkg = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(marketIntent)
            true
        } catch (_: Exception) {
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveInstallerName(context: Context): String {
        return try {
            val installerPkg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            when (installerPkg) {
                "com.android.vending" -> "Google Play Store"
                null, "" -> "Direct APK Install"
                else -> installerPkg
            }
        } catch (_: Exception) {
            "Direct Install"
        }
    }
}
