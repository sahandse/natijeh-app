package com.example.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.DownloadManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@JsonClass(generateAdapter = true)
data class GithubAsset(
    val name: String,
    val browser_download_url: String
)

@JsonClass(generateAdapter = true)
data class GithubRelease(
    val tag_name: String,
    val name: String? = null,
    val body: String? = null,
    val assets: List<GithubAsset> = emptyList()
)

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String?
)

/**
 * Checks GitHub Releases for a newer build of the app and can download + launch
 * the installer for the APK asset attached to that release.
 */
object UpdateManager {

    // Public repo this app is published from.
    private const val OWNER = "sahandse"
    private const val REPO = "natijeh-app"
    private const val RELEASES_API = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

    private val httpClient = OkHttpClient()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val releaseAdapter = moshi.adapter(GithubRelease::class.java)

    fun currentVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    /** Returns update info if a newer version is published, or null if already up to date. */
    suspend fun checkForUpdate(context: Context): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RELEASES_API)
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("پاسخ نامعتبر از سرور (${response.code})"))
            }
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("پاسخ خالی از سرور"))
            val release = releaseAdapter.fromJson(body) ?: return@withContext Result.failure(Exception("خطا در پردازش اطلاعات نسخه"))

            val latestVersion = release.tag_name.trimStart('v', 'V')
            val current = currentVersionName(context)

            if (!isNewer(latestVersion, current)) {
                return@withContext Result.success(null)
            }

            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }

            Result.success(
                UpdateInfo(
                    versionName = latestVersion,
                    releaseNotes = release.body?.takeIf { it.isNotBlank() } ?: "بهبودها و رفع اشکالات جزئی.",
                    downloadUrl = apkAsset?.browser_download_url
                )
            )
        } catch (e: Exception) {
            Log.e("UpdateManager", "Update check failed", e)
            Result.failure(e)
        }
    }

    /** Compares two dot-separated version strings, e.g. "1.5" vs "1.4.2". */
    private fun isNewer(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".", "-").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".", "-").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    /**
     * Downloads the APK using the system DownloadManager and, once finished,
     * launches the package installer. Requires REQUEST_INSTALL_PACKAGES permission
     * (granted by the user via Settings on first attempt on Android 8+).
     */
    fun downloadAndInstall(context: Context, downloadUrl: String, versionName: String, onEnqueued: () -> Unit = {}) {
        val fileName = "natijeh-update-$versionName.apk"
        val destination = File(context.getExternalFilesDir(null), fileName)
        if (destination.exists()) destination.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("به‌روزرسانی نتیجه")
            .setDescription("در حال دانلود نسخه $versionName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destination))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        onEnqueued()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    try {
                        ctx.unregisterReceiver(this)
                    } catch (e: Exception) { /* already unregistered */ }
                    installApk(ctx, destination)
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    private fun installApk(context: Context, file: File) {
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
