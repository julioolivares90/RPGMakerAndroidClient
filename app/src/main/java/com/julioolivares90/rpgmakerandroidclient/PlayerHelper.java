package com.julioolivares90.rpgmakerandroidclient;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.julioolivares90.rpgmakerandroidclient.rpgmakermv.WebPlayerView;

public class PlayerHelper {
    public static Player create(Context context) {
        return new WebPlayerView(context).getPlayer();
    }

    /**
     *
     */
    public static abstract class Interface {

        protected abstract void onStart();
        protected abstract void onPrepare(boolean webgl, boolean webaudio, boolean showfps);

        @JavascriptInterface
        public void start() {
            onStart();
        }

        @JavascriptInterface
        public void prepare(boolean webgl, boolean webaudio, boolean showfps) {
            onPrepare(webgl, webaudio, showfps);
        }

    }
}
