package cn.com.xy.sms.sdk.ui.popu.web;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyExpressStatus;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTitleHead;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;
import org.json.JSONObject;

public class SdkWebActivity extends HwBaseActivity implements IActivityParamForJS {
    private static final String TAG = "SdkWebActivity";
    private static AtomicInteger atomicCount = new AtomicInteger();
    private String actionType;
    private boolean mBlockLoadingNetworkImage = false;
    private String mChannelId = "";
    private Context mContext = null;
    private RelativeLayout mDuoquBar = null;
    private String mDuoquText = "";
    private RelativeLayout mErrorPage = null;
    private ImageView mHeadBackView = null;
    private JSONObject mJsObj = null;
    private RelativeLayout mNetworkSetting = null;
    private boolean mProgressFlag = true;
    private String mSdkVersion = "";
    private TextView mTitleNameView = null;
    private WebView mWebView = null;
    private RelativeLayout mWebViewLy = null;

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private InitTask() {
        }

        protected Void doInBackground(Void... params) {
            try {
                KeyManager.initAppKey();
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity InitTask KeyManager.initAppKey() error:" + e.getMessage(), e);
            }
            SdkWebActivity.this.mChannelId = KeyManager.getAppKey();
            SdkWebActivity.this.mSdkVersion = ParseManager.getSdkVersion();
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SdkWebActivity.this.loadWebViewUrl();
        }
    }

    @SuppressLint({"JavascriptInterface"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        atomicCount.incrementAndGet();
        setContentView(R.layout.duoqu_sdk_web_main);
        init();
    }

    private void init() {
        try {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayOptions(16);
            RelativeLayout mActionBarLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.duoqu_web_action_bar, null);
            actionBar.setCustomView(mActionBarLayout, new LayoutParams(-1, -1));
            mActionBarLayout.setBackgroundColor(getResources().getColor(R.color.duoqu_actionbar_bg_color));
            this.mContext = this;
            this.mWebViewLy = (RelativeLayout) findViewById(R.id.duoqu_webview);
            this.mWebView = new WebView(this);
            this.mWebViewLy.addView(this.mWebView);
            this.mWebView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            this.mTitleNameView = (TextView) findViewById(R.id.duoqu_title_name);
            this.mHeadBackView = (ImageView) mActionBarLayout.findViewById(R.id.duoqu_header_back);
            setTopStyle(this);
            this.mErrorPage = (RelativeLayout) findViewById(R.id.duoqu_error_page);
            this.mNetworkSetting = (RelativeLayout) findViewById(R.id.duoqu_network_setting);
            this.mDuoquBar = (RelativeLayout) findViewById(R.id.duoqu_progressbar);
            this.mWebView.getSettings().setLoadWithOverviewMode(true);
            this.mWebView.getSettings().setUseWideViewPort(true);
            this.mWebView.getSettings().setDatabaseEnabled(true);
            this.mWebView.getSettings().setTextSize(TextSize.NORMAL);
            this.mWebView.getSettings().setSaveFormData(true);
            this.mWebView.getSettings().setSavePassword(true);
            this.mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
            this.mWebView.getSettings().setGeolocationDatabasePath(getApplicationContext().getDir("database", 0).getPath());
            this.mWebView.getSettings().setGeolocationEnabled(true);
            this.mWebView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    SdkWebActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                }
            });
            this.mDuoquText = getResources().getString(R.string.duoqu_tip_duoqu_name);
            initWebView();
            initListener();
            new InitTask().execute(new Void[0]);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity init " + e.getMessage(), e);
        }
    }

    private void setTopStyle(Context context) {
        if (HwUiStyleUtils.isSuggestDarkStyle(context)) {
            this.mTitleNameView.setTextColor(ResEx.self().getCachedColor(R.color.title_color_primary_dark));
        }
    }

    protected void onDestroy() {
        try {
            sendBroadcast();
            this.mWebView.stopLoading();
            this.mWebViewLy.removeAllViews();
            this.mWebView.destroy();
            int count = atomicCount.decrementAndGet();
            int pid = Process.myPid();
            String processName = getCurProcessName(pid);
            if (count == 0 && "com.xy.web".equals(processName)) {
                MLog.d("XIAOYUAN_WEB", "KILL PROCESS NAME : com.xy.web");
                Process.killProcess(pid);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity onDestroy " + e.getMessage(), e);
        }
        super.onDestroy();
    }

    private String getCurProcessName(int pid) {
        try {
            ActivityManager mActivityManager = (ActivityManager) getSystemService("activity");
            if (mActivityManager == null) {
                return "";
            }
            List<RunningAppProcessInfo> runAppList = mActivityManager.getRunningAppProcesses();
            if (runAppList == null) {
                return "";
            }
            for (RunningAppProcessInfo appProcess : runAppList) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
            return "";
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity getCurProcessName", e);
        }
    }

    void initListener() {
        this.mHeadBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SdkWebActivity.this.finish();
            }
        });
        this.mNetworkSetting.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Exception e;
                try {
                    Intent intent;
                    if (Integer.parseInt(VERSION.SDK) > 10) {
                        intent = new Intent("android.settings.SETTINGS");
                    } else {
                        Intent intent2 = new Intent();
                        try {
                            intent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.WirelessSettings"));
                            intent2.setAction("android.intent.action.VIEW");
                            intent = intent2;
                        } catch (Exception e2) {
                            e = e2;
                            intent = intent2;
                            SmartSmsSdkUtil.smartSdkExceptionLog("NetworkSetting OnClick error: " + e.getMessage(), e);
                        }
                    }
                    SdkWebActivity.this.startActivity(intent);
                } catch (Exception e3) {
                    e = e3;
                    SmartSmsSdkUtil.smartSdkExceptionLog("NetworkSetting OnClick error: " + e.getMessage(), e);
                }
            }
        });
    }

    public void sendBroadcast() {
        if (this.mJsObj == null) {
            try {
                this.mJsObj = new JSONObject(getIntent().getStringExtra("JSONDATA"));
            } catch (JSONException e) {
                LogManager.e("XIAOYUAN", e.getMessage(), e);
            }
        }
        if (this.mJsObj != null) {
            String broadcastAction;
            String permission;
            Intent intent = new Intent();
            intent.putExtra("JSONDATA", this.mJsObj.toString());
            String type = this.mJsObj.optString(NumberInfo.TYPE_KEY, "");
            if (type.equals("WEB_QUERY_EXPRESS_FLOW")) {
                broadcastAction = BubbleBodyExpressStatus.BROADCAST_ACTION;
                permission = "xy.permmisons.smartsms.GET_EXPRESS_STATUS";
            } else if ("WEB_QUERY_FLIGHT_TREND".equals(type)) {
                broadcastAction = BubbleTitleHead.BROADCAST_ACTION;
                permission = "xy.permmisons.smartsms.GET_FLIGHT_STATUS";
            } else {
                broadcastAction = BubbleTrainBody.BROADCAST_ACTION;
                permission = "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED";
            }
            intent.setAction(broadcastAction);
            sendBroadcast(intent, permission);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        if (this.mWebView.canGoBack()) {
            this.mWebView.goBack();
        } else {
            finish();
        }
        return true;
    }

    void initData() {
        try {
            if (this.mJsObj == null) {
                this.mJsObj = new JSONObject(getIntent().getStringExtra("JSONDATA"));
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity initData error: " + e.getMessage(), e);
        }
    }

    void loadWebViewUrl() {
        String pageViewUrl;
        initData();
        String aType = super.getIntent().getStringExtra("actionType");
        this.actionType = getParamData(NumberInfo.TYPE_KEY);
        if (aType == null || !"WEB_URL".equals(aType)) {
            String mHost = getParamData("HOST");
            if (StringUtils.isNull(mHost)) {
                mHost = NetWebUtil.WEB_SERVER_URL;
            }
            pageViewUrl = getParamData("PAGEVIEW");
            if (StringUtils.isNull(pageViewUrl)) {
                String actionType = getParamData(NumberInfo.TYPE_KEY);
                if ("WEB_MAP_SITE".equals(actionType)) {
                    String address = getParamData("address");
                    pageViewUrl = "http://api.map.baidu.com/geocoder?address=" + address + "&output=html&src=xiaoyuan|" + this.mDuoquText;
                    if (StringUtils.isNull(address)) {
                        errorPage();
                    } else {
                        this.mDuoquBar.setVisibility(8);
                        this.mWebView.loadUrl(pageViewUrl);
                    }
                    return;
                }
                pageViewUrl = "H5Service?actionType=" + actionType + "&channelId=" + KeyManager.channel;
            }
            if (!StringUtils.isNull(pageViewUrl)) {
                pageViewUrl = mHost + "/" + pageViewUrl;
            }
        } else {
            pageViewUrl = getParamData(Constant.URLS);
        }
        if (StringUtils.isNull(pageViewUrl)) {
            errorPage();
        } else {
            int isNetWork = XyUtil.checkNetWork(this.mContext);
            if (isNetWork == 0 || isNetWork == 1) {
                Map<String, String> header = new HashMap();
                header.put("xy-channel", this.mChannelId);
                header.put("xy-sdk-ver", this.mSdkVersion);
                header.put("xy-req-time", String.valueOf(new Date().getTime()));
                header.put("xy-x", DuoquUtils.getXid());
                header.put("xy-p", DuoquUtils.getPid());
                if (!TextUtils.isEmpty(getParamData("menuName"))) {
                    try {
                        header.put("xy-menu-name", URLEncoder.encode(getParamData("menuName"), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(getParamData("publicId"))) {
                    try {
                        header.put("xy-public-id", URLEncoder.encode(getParamData("publicId"), "utf-8"));
                    } catch (UnsupportedEncodingException e2) {
                        e2.printStackTrace();
                    }
                }
                this.mWebView.loadUrl(pageViewUrl, header);
            } else {
                errorPage();
            }
        }
    }

    void errorPage() {
        this.mDuoquBar.setVisibility(8);
        this.mWebView.setVisibility(8);
        this.mTitleNameView.setText(R.string.duoqu_web_not_find_page);
        this.mErrorPage.setVisibility(0);
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    void initWebView() {
        try {
            Paint pt = new Paint();
            this.mWebViewLy.setLayerType(2, pt);
            this.mWebView.setLayerType(1, pt);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebActivity initWebView " + e.getMessage(), e);
        }
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.mWebView.getSettings().setBuiltInZoomControls(true);
        this.mWebView.getSettings().setDomStorageEnabled(true);
        this.mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return SdkWebActivity.toIntentByUrl(SdkWebActivity.this.mContext, view, url);
            }

            public void onPageFinished(WebView view, String title) {
                SdkWebActivity.this.mProgressFlag = false;
                super.onPageFinished(view, title);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                SdkWebActivity.this.mProgressFlag = true;
                super.onPageStarted(view, url, favicon);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode != -10) {
                    view.stopLoading();
                    view.clearView();
                    SdkWebActivity.this.errorPage();
                }
            }
        });
        this.mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
        this.mWebView.getSettings().setBlockNetworkImage(true);
        this.mBlockLoadingNetworkImage = true;
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            @Deprecated
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                super.onConsoleMessage(message, lineNumber, sourceID);
            }

            public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                try {
                    callback.invoke(origin, true, false);
                } catch (Throwable th) {
                    MLog.w(SdkWebActivity.TAG, "SdkWebActivitycallback.invoke() is throwalbe");
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return SdkWebActivity.showAlertDialog(SdkWebActivity.this, view, url, message, result);
            }

            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return SdkWebActivity.showConfirmDialog(SdkWebActivity.this, view, url, message, result);
            }

            public void onProgressChanged(WebView view, int newProgress) {
                if (!SdkWebActivity.this.mProgressFlag || newProgress >= 90) {
                    if (!(SdkWebActivity.this.mDuoquBar.getVisibility() == 8 && SdkWebActivity.this.mWebViewLy.getVisibility() == 0)) {
                        SdkWebActivity.this.mDuoquBar.setVisibility(8);
                        SdkWebActivity.this.mWebViewLy.setVisibility(0);
                    }
                    if (SdkWebActivity.this.mBlockLoadingNetworkImage) {
                        SdkWebActivity.this.mWebView.getSettings().setBlockNetworkImage(false);
                        SdkWebActivity.this.mBlockLoadingNetworkImage = false;
                    }
                } else if (!(SdkWebActivity.this.mDuoquBar.getVisibility() == 0 && SdkWebActivity.this.mWebViewLy.getVisibility() == 4)) {
                    SdkWebActivity.this.mDuoquBar.setVisibility(0);
                    SdkWebActivity.this.mWebViewLy.setVisibility(4);
                }
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }

            public void onReceivedTitle(WebView view, String title) {
                SdkWebActivity.this.setTitle(title, SdkWebActivity.this.getParamData("menuName"));
                super.onReceivedTitle(view, title);
            }
        });
        this.mWebView.addJavascriptInterface(new SdkWebJavaScript(this), "injs");
    }

    public static boolean showConfirmDialog(Activity act, WebView view, String url, String message, final JsResult result) {
        Builder builder = new Builder(act);
        builder.setTitle("confirm");
        builder.setMessage(message);
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                result.confirm();
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                result.cancel();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
        return true;
    }

    public static boolean showAlertDialog(Activity act, WebView view, String url, String message, final JsResult result) {
        Builder builder = new Builder(act);
        builder.setTitle("Alert");
        builder.setMessage(message);
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                result.confirm();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
        return true;
    }

    public WebView getWebView() {
        return this.mWebView;
    }

    public String getParamData(String key) {
        String res = null;
        if (key != null) {
            try {
                if (this.mJsObj == null) {
                    this.mJsObj = new JSONObject(getIntent().getStringExtra("JSONDATA"));
                }
                if (this.mJsObj.has(key)) {
                    res = this.mJsObj.getString(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (res == null) {
            return "";
        }
        return res;
    }

    public void setParamData(String key, String value) {
        if (!StringUtils.isNull(key)) {
            try {
                if (this.mJsObj == null) {
                    this.mJsObj = new JSONObject(getIntent().getStringExtra("JSONDATA"));
                }
                this.mJsObj.put(key, value);
            } catch (Exception e) {
                LogManager.e("XIAOYUAN", e.getMessage(), e);
            }
        }
    }

    public Activity getActivity() {
        return this;
    }

    public int checkOrientation() {
        return this.mContext.getResources().getConfiguration().orientation;
    }

    public String getType() {
        return this.actionType;
    }

    public void hideFragmen() {
        finish();
    }

    public void setTitle(String title, String menuName) {
        String titleName = "";
        if (!StringUtils.isNull(menuName)) {
            titleName = menuName;
        } else if (!StringUtils.isNull(title)) {
            titleName = title;
        }
        setTitle(titleName);
        this.mTitleNameView.setText(titleName);
    }

    public void startActivity(Intent intent) {
        try {
            intent.addFlags(262144);
            super.startActivity(intent);
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "WEBVIEW: startActivity(Intent intent): ", e);
        }
    }

    public void startActivity(Intent intent, Bundle options) {
        try {
            intent.addFlags(262144);
            super.startActivity(intent, options);
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "WEBVIEW: startActivity(Intent intent, Bundle options): ", e);
        }
    }

    protected void onUserLeaveHint() {
        try {
            super.onUserLeaveHint();
            finish();
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "onUserLeaveHint error", e);
        }
    }

    public static boolean toIntentByUrl(Context ctx, WebView view, String url) {
        if (url == null || url.toLowerCase(Locale.getDefault()).startsWith("http")) {
            return false;
        }
        try {
            Intent intent;
            if (url.indexOf("tel:") >= 0) {
                intent = new Intent("android.intent.action.DIAL", Uri.parse(url));
                intent.setFlags(268435456);
                ctx.startActivity(intent);
            } else {
                intent = parseIntent(url);
                if (intent == null) {
                    intent = new Intent("android.intent.action.VIEW");
                    intent.setData(Uri.parse(url));
                }
                ctx.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static Intent parseIntent(String url) {
        if (StringUtils.isNull(url)) {
            return null;
        }
        Intent intent = null;
        int flags = 0;
        boolean isIntentUri = false;
        if (url.startsWith("intent:")) {
            isIntentUri = true;
            flags = 1;
        } else if (url.startsWith("#Intent;")) {
            isIntentUri = true;
        }
        if (isIntentUri) {
            try {
                intent = Intent.parseUri(url, flags);
            } catch (Exception e) {
                LogManager.e("XIAOYUAN", "SdkWebActivity parseIntent(String url) error", e);
            }
        }
        return intent;
    }
}
