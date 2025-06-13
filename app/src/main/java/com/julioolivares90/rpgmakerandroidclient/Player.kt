package com.julioolivares90.rpgmakerandroidclient

import android.content.Context
import android.view.View

interface Player {
    fun setKeepScreenOn()
    fun getView(): View?
    fun loadUrl(url: String?)
    fun addJavascriptInterface(`object`: Any?, name: String?)
    fun getContext(): Context?
    fun loadData(data: String?)
    fun evaluateJavascript(script: String?)
    fun post(runnable: Runnable?)
    fun removeJavascriptInterface(name: String?)
    fun pauseTimers()
    fun onHide()
    fun resumeTimers()
    fun onShow()
    fun onDestroy()
}