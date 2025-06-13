package com.julioolivares90.rpgmakerandroidclient

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.nio.charset.Charset

object ActivityConstants {
    val TOUCH_INPUT_ON_CANCEL = "TouchInput._onCancel();"
}
class MainActivity : AppCompatActivity() {

    private var mPlayer : Player? = null
    private var mSystemUiVisibility = 0
    private var mQuitDialog : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)


        if (BuildConfig.BACK_BUTTON_QUITS) {
            createQuitDialog()
        }
        mSystemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSystemUiVisibility = mSystemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN
            mSystemUiVisibility = mSystemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            mSystemUiVisibility = mSystemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            mSystemUiVisibility = mSystemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSystemUiVisibility = mSystemUiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
        mPlayer = PlayerHelper.create(this)
        mPlayer?.setKeepScreenOn()
        setContentView(mPlayer?.getView())
        if (!addBootstrapInterface(mPlayer!!)) {
            val projectURIBuilder =
                Uri.fromFile(File(getString(R.string.mv_project_index))).buildUpon()
            Bootstrapper.appendQuery(
                projectURIBuilder,
                getString(R.string.query_noaudio)
            )
            if (BuildConfig.SHOW_FPS) {
                Bootstrapper.appendQuery(
                    projectURIBuilder,
                    getString(R.string.query_showfps)
                )
            }
            mPlayer?.loadUrl(projectURIBuilder.build().toString())
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onBackPressed() {
        if (BuildConfig.BACK_BUTTON_QUITS) {
            if (mQuitDialog != null) {
                mQuitDialog!!.show()
            } else {
                super.onBackPressed()
            }
        } else {
            mPlayer!!.evaluateJavascript(ActivityConstants.TOUCH_INPUT_ON_CANCEL)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
    override fun onPause() {
        mPlayer!!.pauseTimers()
        mPlayer!!.onHide()

        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = mSystemUiVisibility
        if (mPlayer != null) {
            mPlayer!!.resumeTimers()
            mPlayer!!.onShow()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mPlayer!!.onDestroy()
    }
    override fun onRestart() {
        super.onRestart()
    }
    private fun createQuitDialog() {
        val appName = getString(R.string.app_name)
        val quitLines = resources.getStringArray(R.array.quit_message)
        val quitMessage = StringBuilder()
        for (ii in quitLines.indices) {
            quitMessage.append(quitLines[ii].replace("$1", appName))
            if (ii < quitLines.size - 1) {
                quitMessage.append("\n")
            }
        }

        if (quitMessage.length > 0) {
            mQuitDialog = AlertDialog.Builder(this)
                .setPositiveButton(
                    "Cancel"
                ) { dialog, which -> dialog.dismiss() }
                .setOnDismissListener { window.decorView.systemUiVisibility = mSystemUiVisibility }
                .setNegativeButton(
                    "Quit"
                ) { dialog, which -> super@MainActivity.onBackPressed() }
                .setMessage(quitMessage.toString())
                .create()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun addBootstrapInterface(player: Player): Boolean {
        if (BuildConfig.BOOTSTRAP_INTERFACE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
           Bootstrapper(player)
            return true
        }
        return false
    }

    private companion object class Bootstrapper(player: Player) : PlayerHelper.Interface(),
        Runnable {
        private val mPlayer: Player
        private var mURIBuilder: Uri.Builder

        init {
            val context = player.getContext()
            player.addJavascriptInterface(this, INTERFACE)

            mPlayer = player
            mURIBuilder =
                Uri.fromFile(File(context!!.getString(R.string.mv_project_index))).buildUpon()
            mPlayer.loadData(context.getString(R.string.webview_default_page))
        }

        override fun onStart() {
            val context = mPlayer.getContext()
            val code = String(
                Base64.decode(
                    context!!.getString(R.string.webview_detection_source),
                    Base64.DEFAULT
                ), Charset.forName("UTF-8")
            ) + INTERFACE + "." + PREPARE_FUNC + ";"
            mPlayer.post { mPlayer.evaluateJavascript(code) }
        }

        override fun onPrepare(webgl: Boolean, webaudio: Boolean, showfps: Boolean) {
            val context = mPlayer.getContext()
            mURIBuilder = if (webgl && !BuildConfig.FORCE_CANVAS) {
                appendQuery(mURIBuilder, context!!.getString(R.string.query_webgl))
            } else {
                appendQuery(mURIBuilder, context!!.getString(R.string.query_canvas))
            }
            if (!webaudio || BuildConfig.FORCE_NO_AUDIO) {
                mURIBuilder = appendQuery(mURIBuilder, context.getString(R.string.query_noaudio))
            }
            if (showfps || BuildConfig.SHOW_FPS) {
                mURIBuilder = appendQuery(mURIBuilder, context.getString(R.string.query_showfps))
            }
            mPlayer.post(this)
        }

        override fun run() {
            mPlayer.removeJavascriptInterface(INTERFACE)
            mPlayer.loadUrl(mURIBuilder.build().toString())
        }

        companion object {
            fun appendQuery(builder: Uri.Builder, query: String): Uri.Builder {
                var query = query
                val current = builder.build()
                val oldQuery = current.encodedQuery
                if (oldQuery != null && oldQuery.length > 0) {
                    query = "$oldQuery&$query"
                }
                return builder.encodedQuery(query)
            }

            private const val INTERFACE = "boot"
            private const val PREPARE_FUNC = "prepare( webgl(), webaudio(), false )"
        }
    }
}