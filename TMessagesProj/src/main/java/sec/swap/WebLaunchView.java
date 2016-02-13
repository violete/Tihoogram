/*
Copyright (c) 2015 Samsung Electronics. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
4. Redistributions of source code must retain the specific URL of the main site(http://swap.sec.net/search/) at least one time. The main site must be the first web page which is connected with the messenger. In addition, changing of main site URL it is not allowed.

ALTERNATIVELY, this product may be distributed under the terms of the GNU General Public License Version 2, in which case the provisions of the GNU GPL are required INSTEAD OF the above restrictions.  (This clause is necessary due to a potential conflict between the GNU GPL and the restrictions contained in a BSD-style copyright.)

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sec.swap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class WebLaunchView extends WebView  {

    private static final int EVENT_SET_RECT = 0;
    private static final int EVENT_SEND_TEXT = 1;
    private static final int EVENT_SEND_WEBAPP = 2;
    private static final int EVENT_FINISH = 3;
    private static final int EVENT_LOAD_JS = 4;
    private static final int EVENT_CHECK_SETRECT = 5;
    private static final int EVENT_CHECK_TIMEOUT = 6;
    private static final int EVENT_START_ALONE = 7;
    public static final int EVENT_CALLBACK_GETRANKING = 8;
    public static final int EVENT_CALLBACK_GETRANKINGBYIDS = 9;
    public static final int EVENT_CALLBACK_PUTRANKING = 10;

    private static final float COMPATIBLE_JS_VERSION = 1.01f;
    private static final String LAUNCHED_WEBAPP_LOG = "LaunchedWebAppList";

    private static String appName = "";
    private static String cacheDirectory = "";

    private WebLaunchListener webLaunchListener = null;
    private WebAppLaunchInfo webappLaunchInfo = null;
    private HashMap<String, WebAppInfo> webAppInfoMap = new HashMap<String, WebAppInfo>();
    private HashMap<String, Long> launchedWebAppMap = new HashMap<String, Long>();
    public static EventHandler eventHandler;
    private SharedPreferences webAppLogPreferences;

    private long backKeyPressedTime = 0;
    private boolean versionChecked = false;
    private boolean setRectCalled = false;
    private boolean pageLoading = false;
    private boolean keyboardAppeared = false;

    static public class Participant {
        public String id;
        public String nick;
        public Bitmap picture;

        public Participant(String memberId, String memberNick, Bitmap memberImg){
            id = memberId;
            nick = memberNick;
            picture = memberImg;
        }
    }

    public WebLaunchView(android.content.Context context) {
        super(context);
        appName = getAppName(context);
    }

    public WebLaunchView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        appName = getAppName(context);
    }

    public WebLaunchView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        appName = getAppName(context);
    }

    public boolean init(WebLaunchListener listener, String directoryPath) {
        if(webLaunchListener != null)
            return false;   // already initialized

        eventHandler = new EventHandler();
        cacheDirectory = directoryPath + "/swap";
        File directory = new File(cacheDirectory);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                return false;   // failed to create the directory
            }
        }
        File dirInvalid = new File(cacheDirectory + "/invalid");
        if (!dirInvalid.exists()) {
            if (!dirInvalid.mkdir()) {
                return false;   // failed to create the directory
            }
        }
        webLaunchListener = listener;
        fileDeleteTask.execute();

        SharedPreferences sp = getContext().getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        Float ver = sp.getFloat("SWAP_VER", 0);
        if (Math.abs(ver - COMPATIBLE_JS_VERSION) < 0.00001f) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("SWAP_VER", COMPATIBLE_JS_VERSION);
            editor.commit();
            this.clearCache(true);
            this.clearHistory();
            this.clearFormData();
        }

        webAppLogPreferences = getContext().getSharedPreferences(LAUNCHED_WEBAPP_LOG, Context.MODE_PRIVATE);
        getLaunchedWebAppList();

        WebSettings settings = getSettings();

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        if(!settings.getJavaScriptEnabled())
            settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        // for local storage
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setWebContentsDebuggingEnabled(true); // remote debugging
        else
            settings.setDatabasePath(directoryPath); // local storage

        // for Android 2.3
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new AlertDialog.Builder(WebLaunchView.this.getContext())
                        .setTitle("AlertDialog")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }
        });

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(!url.equals("about:blank")) {

                    Activity host = (Activity) WebLaunchView.this.getContext();
                    host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    WebLaunchView.this.hideKeyboard();
                    versionChecked = false;
                    setRectCalled = false;
                    setBackgroundColor(0xFFFFFFFF);

                    pageLoading = true;
                    eventHandler.removeMessages(EVENT_CHECK_SETRECT);
                    eventHandler.removeMessages(EVENT_CHECK_TIMEOUT);
                    eventHandler.sendMessageDelayed(Message.obtain(eventHandler, EVENT_CHECK_TIMEOUT, 0, 0, url), 20000);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!url.equals("about:blank")) {
                    if(pageLoading) {
                        WebLaunchView.this.webLaunchListener.onWebAppLoadingCompleted();
                        pageLoading = false;
                    }
                    eventHandler.removeMessages(EVENT_CHECK_SETRECT);
                    eventHandler.removeMessages(EVENT_CHECK_TIMEOUT);
                    eventHandler.sendMessageDelayed(Message.obtain(eventHandler, EVENT_CHECK_SETRECT, 0, 0, url), 10000);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                if(webappLaunchInfo != null) {
                    new WebAppLaunchLogTask().execute(webappLaunchInfo.getManifest(), webappLaunchInfo.getMyId(),
                            webappLaunchInfo.getMessageId(), "" + errorCode + "/" + description + "/" + failingUrl, appName);
                }
                Toast.makeText(getContext(), description, Toast.LENGTH_LONG).show();
                unLoad();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        addJavascriptInterface(new SWAP_JS_Interface(), "SWAP_JS_Interface");
        return true;
    }

    final class SWAP_JS_Interface{
        SWAP_JS_Interface() {
        }

        @JavascriptInterface
        public void getInfo() {

            if(webappLaunchInfo == null)
                return;

            StringBuilder strJavascript = new StringBuilder();
            StringBuilder encodedImage  = new StringBuilder();
            strJavascript.append("javascript:$swap._callbacks.getInfo({sessionID:\""+webappLaunchInfo.getSessionId()+"\",messageID:\""+webappLaunchInfo.getMessageId()+"\",myID:\""+ webappLaunchInfo.getMyId()+"\",isStarter:"+webappLaunchInfo.isStarter()+",isStartedAlone:"+webappLaunchInfo.isStartAlone()+",members:[");
            int count =  webappLaunchInfo.getMemberCount();
            for(int i=0; i<count; i++)
            {
                encodedImage.setLength(0);
                Bitmap bm = getResizedBitmap(webappLaunchInfo.getMemberPicture(i));
                if(bm != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] byteArrayImage = baos.toByteArray();
                    encodedImage.append("data:image/png;base64," + Base64.encodeToString(byteArrayImage, Base64.NO_WRAP));
                    bm.recycle();
                }
                if(i>0)
                    strJavascript.append(",");
                strJavascript.append("{id:\""+ webappLaunchInfo.getMemberId(i)+"\",nick:\""+webappLaunchInfo.getMemberNick(i)+"\",img:\"" +encodedImage+ "\"}");
            }
            strJavascript.append("]});");

            eventHandler.sendMessage(Message.obtain(eventHandler, EVENT_LOAD_JS, 0, 0, strJavascript.toString()));
        }

        @JavascriptInterface
        public void setOrientation(final boolean isPortrait) {

            Activity host = (Activity)WebLaunchView.this.getContext();
            if(isPortrait)
                host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else
                host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @JavascriptInterface
        public void setRect(final float x, final float y, final float w, final float h ) {
            if(webappLaunchInfo == null)
                return;

            eventHandler.set(x, y, w, h);
            eventHandler.removeMessages(EVENT_SET_RECT);
            eventHandler.sendMessageDelayed(Message.obtain(eventHandler, EVENT_SET_RECT, 0, 0, null), 10);
        }

        @JavascriptInterface
        public void setBackgroundColor(String color ) {
            if(webappLaunchInfo == null)
                return;
            try {
                WebLaunchView.this.setBackgroundColor(Color.parseColor(color));
            }
            catch(Exception e)
            {
                WebLaunchView.this.setBackgroundColor(0xFFFFFFFF);
            }
        }
        @JavascriptInterface
        public void popupUrl(String url ) {

            if(url == null || !url.toLowerCase().startsWith("http"))
                return;

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            WebLaunchView.this.getContext().startActivity(i);

        }

        @JavascriptInterface
        public void shareString(String str ) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, str);
                WebLaunchView.this.getContext().startActivity(Intent.createChooser(intent, ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void sendWebApp() {
            if(webappLaunchInfo == null ||  !webappLaunchInfo.isStartAlone())
                return;

            if(webappLaunchInfo.isStarter() && !webappLaunchInfo.isSendedWebApp()) {
                eventHandler.sendMessage(Message.obtain(eventHandler,EVENT_FINISH, 0, 0, null));
                eventHandler.sendMessageDelayed(Message.obtain(eventHandler, EVENT_SEND_WEBAPP, 0, 0, webappLaunchInfo.getOriginalMessage()), 1000);
                webappLaunchInfo.setSendedWebApp();
            }
        }

        @JavascriptInterface
        public void sendText(final String str) {
            if(webappLaunchInfo == null)
                return;

            if(webappLaunchInfo.isStarter() && !webappLaunchInfo.isSendedText()) {
                String msg;
                if(webappLaunchInfo.isFromMarket())
                    msg = str;
                else {
                    WebAppInfo info = getWebAppInfo(webappLaunchInfo.getManifest(), false);
                    if (info == null) {
                        return;
                    }
                    msg = "["+info.getName()+"]\n" + str;
                }

                eventHandler.sendMessageDelayed(Message.obtain(eventHandler, EVENT_SEND_TEXT, 0, 0, msg), 1000);
                webappLaunchInfo.setSendedText();
            }
        }

        @JavascriptInterface
        public void startAlone(final String url) {
            if (webappLaunchInfo == null || !webappLaunchInfo.isFromMarket() )
                return;

            if (!isWebApp(url, true))
                return;

            webappLaunchInfo.setMessage(url);
            webappLaunchInfo.setStartAlone();
            eventHandler.sendMessage(Message.obtain(eventHandler, EVENT_START_ALONE, 0, 0, null));
        }

        @JavascriptInterface
        public void copyToClipboard(final String str) {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(str);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", str);
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(getContext(), "It has been copied to clipboard", Toast.LENGTH_SHORT).show();

        }

        @JavascriptInterface
        public void sendErrorLog(final String str) {
            if(webappLaunchInfo == null)
                return;

            new WebAppLaunchLogTask().execute(webappLaunchInfo.getManifest(), webappLaunchInfo.getMyId(), webappLaunchInfo.getMessageId(), str, appName);
        }

        @JavascriptInterface
        public void finish() {
            if(webappLaunchInfo == null)
                return;

            eventHandler.sendMessage(Message.obtain(eventHandler, EVENT_FINISH, 0, 0, null));
        }

        @JavascriptInterface
        public void getRanking(final int top) {
            if(webappLaunchInfo == null)
                return;
            new WebAppRankingTask().execute("GET", webappLaunchInfo.getManifest(), "" + top);
        }

        @JavascriptInterface
        public void getRankingByIDs(final String strData) {
            if(webappLaunchInfo == null)
                return;
            new WebAppRankingTask().execute("POST", webappLaunchInfo.getManifest(), strData);
        }

        @JavascriptInterface
        public void putRanking(final String strData) {
            if(webappLaunchInfo == null)
                return;
            new WebAppRankingTask().execute("PUT", webappLaunchInfo.getManifest(), strData);
        }
    }

    private Bitmap getResizedBitmap(Bitmap img) {
        final int MAX = 128;

        int height = img.getHeight();
        int width = img.getWidth();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if (width > height) {
            if (width > MAX) {
                rate = MAX / (float) width;
                newHeight = (int) (height * rate);
                newWidth = MAX;
            }
        } else {
            if (height > MAX) {
                rate = MAX / (float) height;
                newWidth = (int) (width * rate);
                newHeight = MAX;
            }
        }
        return Bitmap.createScaledBitmap(img, newWidth, newHeight, true);
    }

    private WebAppInfo getWebAppInfo(String msg, boolean reconn)
    {
        if(msg.contains("#")) {
            String[] strMsg = msg.split("#");
            if(strMsg.length == 2) {
                msg = strMsg[0];
            }
        }

        if (!android.webkit.URLUtil.isValidUrl(msg)) {
            return null;
        }

        msg = changeHostNameToLower(msg);

        if (!isWebAppUrlPattern(msg)) {
            return null;
        }

        WebAppInfo webAppInfo = webAppInfoMap.get(msg);
        if(webAppInfo != null)
            return webAppInfo;

        String filePath = cacheDirectory;
        String url = "";
        try {
            url = URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        filePath += "/" + url;
        File jsonFile = new File(filePath);

        if (jsonFile.exists() && jsonFile.isFile()) {
            webAppInfo = new WebAppInfo(filePath);
            webAppInfoMap.put(msg, webAppInfo);
            return webAppInfo;
        }

        File invalidFile = new File(cacheDirectory + "/invalid/" + url);
        if (invalidFile.exists() && invalidFile.isFile()) {
            if (reconn) {
                invalidFile.delete();
            } else {
                return null;
            }
        }

        return getWebAppInfoFromNetwork(msg);
    }

    private WebAppInfo getWebAppInfoFromNetwork(String url)
    {
        if (!isWebAppUrlPattern(url)) {
            return null;
        }

        if (!isNetworkOnline()) {
            return null;
        }

        String filePath = cacheDirectory;
        String name = "";
        try {
            name = URLEncoder.encode(url, "UTF-8");
            filePath = filePath + "/" + name;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            if (!new WebAppInfoGetTask().execute(url, filePath).get()) {
                File file = new File(cacheDirectory + "/invalid/" + name);
                if (false == file.isFile()) {
                    file.createNewFile();
                }
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        WebAppInfo webAppInfo  = new WebAppInfo(filePath);
        webAppInfoMap.put(url, webAppInfo);
        return webAppInfo;
    }

    public boolean isWebApp(String msg, boolean reconn) {
        if(msg == null)
            return false;

        if (getWebAppInfo(msg, reconn) != null) {
            return true;
        }
        return false;
    }

    public void launchWebAppSearch(String sessionId, String myId, List<WebLaunchView.Participant> memberlist) {

        webLaunchListener.onWebAppLoadingStarted();

        if (!isNetworkOnline()) {
            Toast.makeText(getContext(), "Error: No connection to Internet", Toast.LENGTH_SHORT).show();
            webLaunchListener.onWebAppLoadingCanceled();
            return;
        }

        webappLaunchInfo= new WebAppLaunchInfo(sessionId, myId, memberlist);
        loadUrl("http://swap.sec.net/search/");

    }

    public boolean launchWebApp(String msg, String sessionId, boolean isStarter, String myId, List<WebLaunchView.Participant> memberlist) {

        if (!isNetworkOnline()) {
            Toast.makeText(getContext(), "Error: No connection to Internet", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (webappLaunchInfo != null) {
            Toast.makeText(getContext(), "Information: Another WebApp is running", Toast.LENGTH_SHORT).show();
            return false;
        }

        webappLaunchInfo = new WebAppLaunchInfo(changeHostNameToLower(msg), sessionId, isStarter, myId, memberlist);
        if(webappLaunchInfo.getManifest() == null || webappLaunchInfo.getMessageId() == null)
        {
            Toast.makeText(getContext(), "Error: The message doesn't include message_id", Toast.LENGTH_SHORT).show();
            return false;
        }


        if(!realLaunchWebApp()) {
            unLoad();
            return false;
        }

        if (launchedWebAppMap.get(webappLaunchInfo.getMessageId()) == null) {
            long launchTime = System.currentTimeMillis();
            webAppLogPreferences.edit().putLong(webappLaunchInfo.getMessageId(), launchTime).commit();
            launchedWebAppMap.put(webappLaunchInfo.getMessageId(), launchTime);
        }
        return true;
    }

    private boolean realLaunchWebApp()
    {
        if (!isNetworkOnline()) {
            Toast.makeText(getContext(), "Error: No connection to Internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        WebAppInfo webAppInfo = getWebAppInfoFromNetwork(webappLaunchInfo.getManifest());
        if (webAppInfo == null)
        {
            Toast.makeText(getContext(), "Error: Can't connect to "+webappLaunchInfo.getManifest(), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!webAppInfo.isValid()) {
            Toast.makeText(WebLaunchView.this.getContext(),String.format("Error: %s is invalid", webappLaunchInfo.getManifest()), Toast.LENGTH_LONG).show();
            return false;
        }

        webLaunchListener.onWebAppLoadingStarted();
        String tempURL = webAppInfo.getURL();

        if(tempURL.contains("?")){
            tempURL = tempURL + "&sessionID=" + webappLaunchInfo.getSessionId();
        }else{
            tempURL = tempURL + "?sessionID=" + webappLaunchInfo.getSessionId();
        }

        loadUrl(tempURL);
        new WebAppLaunchLogTask().execute(webappLaunchInfo.getManifest(), webappLaunchInfo.getMyId(), webappLaunchInfo.getMessageId(), appName);
        return true;
    }


    public void unLoad()
    {
        if(webappLaunchInfo != null)
        {
            webappLaunchInfo = null;
            stopLoading();
            loadUrl("about:blank");

            eventHandler.removeMessages(EVENT_SET_RECT);
            eventHandler.removeMessages(EVENT_FINISH);
            eventHandler.removeMessages(EVENT_LOAD_JS);
            eventHandler.removeMessages(EVENT_CHECK_SETRECT);
            eventHandler.removeMessages(EVENT_CHECK_TIMEOUT);
            eventHandler.removeMessages(EVENT_START_ALONE);

            if(pageLoading) {
                webLaunchListener.onWebAppLoadingCanceled();
                pageLoading = false;
            }

            Activity host = (Activity)WebLaunchView.this.getContext();
            host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            WebLaunchView.this.hideKeyboard();
        }

    }

    public boolean isAutoLaunch(String msg)
    {
        WebAppInfo webAppInfo = getWebAppInfo(msg, false);
        if( webAppInfo != null)
            return webAppInfo.isAutoLaunch();
        return false;
    }

    public boolean isOneOff(String msg)
    {
        WebAppInfo webAppInfo = getWebAppInfo(msg, false);
        if( webAppInfo != null)
            return webAppInfo.isOneOff();
        return true;
    }

    public String getIconUrl(String msg){
        WebAppInfo webAppInfo = getWebAppInfo(msg, false);
        if( webAppInfo == null)
            return null;

        return webAppInfo.getIcon();
    }

    class EventHandler extends Handler {

        private float mx=0, my=0, mw=0, mh=0;

        public void set(float x, float y, float w, float h) {

            mx = x;
            my = y;
            mw = w;
            mh = h;
        }

        public void handleMessage(Message msg) {

            if(msg.what == EVENT_SET_RECT) {
                if(webappLaunchInfo == null)
                    return;

                float x = mx;
                float y = my;
                float w = mw;
                float h = mh;

                if(x > 1.0 || x<0 )
                    x = convertPixelToDp(x);
                if(y > 1.0 || y<0 )
                    y = convertPixelToDp(y);
                if(w > 1.0 || w<0 )
                    w = convertPixelToDp(w);
                if(h > 1.0 || h<0 )
                    h = convertPixelToDp(h);

                if(pageLoading)
                {
                    pageLoading = false;
                    WebLaunchView.this.webLaunchListener.onWebAppLoadingCompleted();
                }

                webLaunchListener.onWebAppSize(x, y, w, h);

                setRectCalled = true;
                if(!versionChecked) {
                    WebLaunchView.this.loadUrl("javascript:$swap._checkVersion("+ COMPATIBLE_JS_VERSION +");");
                    versionChecked = true;
                }
            }
            else if(msg.what == EVENT_FINISH) {
                webLaunchListener.onWebAppFinish();
            }
            else if(msg.what == EVENT_SEND_TEXT) {
                webLaunchListener.onWebAppSend((String)msg.obj);
            }
            else if(msg.what == EVENT_SEND_WEBAPP) {
                webLaunchListener.onWebAppSend((String)msg.obj);
            }
            else if (msg.what == EVENT_LOAD_JS) {
                WebLaunchView.this.loadUrl((String)msg.obj);
            }
            else if (msg.what == EVENT_CHECK_SETRECT) {

                if(setRectCalled == false && webappLaunchInfo != null && WebLaunchView.this.getVisibility() != View.VISIBLE)
                {
                    Toast.makeText(WebLaunchView.this.getContext(),String.format("Error: %s failed to call $swap.setRect API", (String)msg.obj), Toast.LENGTH_LONG).show();
                    unLoad();
                }
            }
            else if (msg.what == EVENT_CHECK_TIMEOUT) {


                if(pageLoading == true && webappLaunchInfo != null)
                {
                    Toast.makeText(WebLaunchView.this.getContext(),String.format("Error: Timeout to %s", (String)msg.obj), Toast.LENGTH_LONG).show();
                    unLoad();
                }
            }
            else if (msg.what == EVENT_START_ALONE) {
                if (!realLaunchWebApp())
                    unLoad();
            }
            else if (msg.what == EVENT_CALLBACK_GETRANKING) {
                if(webappLaunchInfo == null)
                    return;
                WebLaunchView.this.loadUrl("javascript:$swap._callbacks.getRanking("+ (String)msg.obj + ");");
            }
            else if (msg.what == EVENT_CALLBACK_GETRANKINGBYIDS) {
                if(webappLaunchInfo == null)
                    return;
                WebLaunchView.this.loadUrl("javascript:$swap._callbacks.getRankingByIDs("+ (String)msg.obj + ");");
            }
            else if (msg.what == EVENT_CALLBACK_PUTRANKING) {
                if(webappLaunchInfo == null)
                    return;
                WebLaunchView.this.loadUrl("javascript:$swap._callbacks.putRanking("+ (String)msg.obj + ");");
            }
        }
    }

    private float convertPixelToDp(float input) {
        return (input * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private int convertDpToPixel(int input) {
        return (int) (input / getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean isNetworkOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfoMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo netInfoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if ((netInfoMobile != null && netInfoMobile.isConnected()) || (netInfoWifi != null && netInfoWifi.isConnected())) {
            return true;
        }

        return false;
    }

    public boolean isLaunchedApp(String sessionId, String msg, long msgDate) {
        if (msg.contains("#")) {
            long aWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            String[] strMsg = msg.split("#");
            if(strMsg.length != 2)
                return false;
            if (launchedWebAppMap.get("m" + sessionId + strMsg[1]) != null) {
                return true;
            } else if (msgDate < aWeekAgo) {
                return true;
            }
        }
        return false;
    }

    private AsyncTask<Void, Void, Void> fileDeleteTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            File directory = new File(cacheDirectory);
            if (directory.exists()) {
                File[] listFiles = directory.listFiles();
                long aWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
                for (File listFile : listFiles) {
                    if (listFile.lastModified() < aWeekAgo) {
                        listFile.delete();
                    }
                }
            }
            return null;
        }
    };

    private void getLaunchedWebAppList() {
        if (webAppLogPreferences == null) {
            webAppLogPreferences = getContext().getSharedPreferences(LAUNCHED_WEBAPP_LOG, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = webAppLogPreferences.edit();
        long aWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        Map<String, ?> allItem = webAppLogPreferences.getAll();
        for (Map.Entry<String, ?> item : allItem.entrySet()) {
            long date = webAppLogPreferences.getLong(item.getKey(), 0);
            if (date < aWeekAgo) {
                editor.remove(item.getKey());
            } else {
                launchedWebAppMap.put(item.getKey(), date);
            }
        }
        editor.commit();
    }

    public void hideKeyboard() {
        Activity activity =  (Activity)this.getContext();
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private String getAppName(Context ctx){
        String name = ctx.getString(ctx.getApplicationInfo().labelRes);
        try{
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            name += " v" + info.versionName;
        } catch (PackageManager.NameNotFoundException e){

        }
        return name;
    }

    public void onBackKey()  {
        long currentTime =  System.currentTimeMillis();
        if(currentTime - backKeyPressedTime <= 300)
            eventHandler.sendMessage(Message.obtain(eventHandler,EVENT_FINISH, 0, 0, null));
        backKeyPressedTime = currentTime;

        if(webappLaunchInfo != null) {
            loadUrl("javascript:$swap._callbacks.onBackKey();");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        super.pauseTimers(); // for timer
        if(webappLaunchInfo != null)
            loadUrl("javascript:$swap._callbacks.onPause();");
    }

    @Override
    public void onResume() {
        super.onResume();
        super.resumeTimers();
        if(webappLaunchInfo != null)
            loadUrl("javascript:$swap._callbacks.onResume();");
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        Rect rect = new Rect();
        WebLaunchView.this.getWindowVisibleDisplayFrame(rect);
        int heightDiff = WebLaunchView.this.getRootView().getHeight() - (rect.bottom - rect.top);
        if (heightDiff > 100) {
            keyboardAppeared = true;
        } else { // fire resize event when keyboard is gone
            if(keyboardAppeared) {
                keyboardAppeared = false;
                dispatchResizeEvent();
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dispatchResizeEvent();
    }

    private boolean isWebAppUrlPattern(String url){
        String regexAlphaChars = "a-z" +
                "\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff" + // Latin-1
                "\\u0100-\\u024f" + // Latin Extended A and B
                "\\u0253\\u0254\\u0256\\u0257\\u0259\\u025b\\u0263\\u0268\\u026f\\u0272\\u0289\\u028b" + // IPA Extensions
                "\\u02bb" + // Hawaiian
                "\\u0300-\\u036f" + // Combining diacritics
                "\\u1e00-\\u1eff" + // Latin Extended Additional (mostly for Vietnamese)
                "\\u0400-\\u04ff\\u0500-\\u0527" +  // Cyrillic
                "\\u2de0-\\u2dff\\ua640-\\ua69f" +  // Cyrillic Extended A/B
                "\\u0591-\\u05bf\\u05c1-\\u05c2\\u05c4-\\u05c5\\u05c7" +
                "\\u05d0-\\u05ea\\u05f0-\\u05f4" + // Hebrew
                "\\ufb1d-\\ufb28\\ufb2a-\\ufb36\\ufb38-\\ufb3c\\ufb3e\\ufb40-\\ufb41" +
                "\\ufb43-\\ufb44\\ufb46-\\ufb4f" + // Hebrew Pres. Forms
                "\\u0610-\\u061a\\u0620-\\u065f\\u066e-\\u06d3\\u06d5-\\u06dc" +
                "\\u06de-\\u06e8\\u06ea-\\u06ef\\u06fa-\\u06fc\\u06ff" + // Arabic
                "\\u0750-\\u077f\\u08a0\\u08a2-\\u08ac\\u08e4-\\u08fe" + // Arabic Supplement and Extended A
                "\\ufb50-\\ufbb1\\ufbd3-\\ufd3d\\ufd50-\\ufd8f\\ufd92-\\ufdc7\\ufdf0-\\ufdfb" + // Pres. Forms A
                "\\ufe70-\\ufe74\\ufe76-\\ufefc" + // Pres. Forms B
                "\\u200c" +                        // Zero-Width Non-Joiner
                "\\u0e01-\\u0e3a\\u0e40-\\u0e4e" + // Thai
                "\\u1100-\\u11ff\\u3130-\\u3185\\uA960-\\uA97F\\uAC00-\\uD7AF\\uD7B0-\\uD7FF" + // Hangul (Korean)
                "\\u3003\\u3005\\u303b" +           // Kanji/Han iteration marks
                "\\uff21-\\uff3a\\uff41-\\uff5a" +  // full width Alphabet
                "\\uff66-\\uff9f" +                 // half width Katakana
                "\\uffa1-\\uffdc";

        String urlRegex = "(https?://)?" +
                // user:pass authentication
                "(?:\\S{1,64}(?::\\S{0,64})?@)?" +
                "(?:" +
                // sindresorhus/ip-regex
                "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}" +
                "|" +
                // host name
                "[" + regexAlphaChars + "0-9][" + regexAlphaChars + "0-9\\-]{0,64}" +
                // domain name
                "(?:\\.[" + regexAlphaChars + "0-9][" + regexAlphaChars + "0-9\\-]{1,64}){0,10}" +

                // TLD identifier
                "(?:\\.(xn--[0-9a-z]{2,16}|[" + regexAlphaChars + "]{2,24}))" +
                ")" +
                // port number
                "(?::\\d{2,5})?" +
                // resource path
                "(?:/(?:\\S{0,255}[^\\s.;,(\\[\\]{}<>\"'])?)?";

        Pattern p = Pattern.compile("(?i)" + urlRegex + "(\\.json)(#\\d+)?");
        return p.matcher(url).matches();
    }

    private String changeHostNameToLower(String msg){
        try {
            URL url = new URL(msg);
            String host = url.getHost();
            String lowerHost = host.toLowerCase();
            msg = msg.replaceFirst(host, lowerHost);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return msg;
    }
    public void dispatchResizeEvent() {
        loadUrl("javascript:window.dispatchEvent(new Event('resize'));");
    }

}
