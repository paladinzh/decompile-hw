package cn.com.xy.sms.sdk.ui.popu.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyExpressStatus;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTitleHead;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseFragment;
import org.json.JSONException;
import org.json.JSONObject;

public class SdkWebFragment extends HwBaseFragment implements IActivityParamForJS {
    private static final String TAG = "SdkWebFragment";
    private String actionType;
    private boolean isPrepared;
    private JSONObject jsObject;
    public RelativeLayout mActionBar;
    private Activity mActivity = null;
    private boolean mBlockLoadingNetworkImage = false;
    private RelativeLayout mDuoquBar = null;
    private String mDuoquText = "";
    private RelativeLayout mErrorPage = null;
    public ImageView mHeadBackView = null;
    private JSONObject mJsObj = null;
    private RelativeLayout mNetworkSetting = null;
    private boolean mProgressFlag = false;
    public TextView mTitleNameView = null;
    private WebView mWebView = null;
    private RelativeLayout mWebViewLy = null;
    private View rootView;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.rootView == null) {
            this.rootView = inflater.inflate(R.layout.duoqu_sdk_web_fragment, null);
        }
        ViewGroup parent = (ViewGroup) this.rootView.getParent();
        if (parent != null) {
            parent.removeView(this.rootView);
        }
        return this.rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initWebView();
        initListener();
        setDefaultDate();
    }

    public void onDestroy() {
        this.mWebView.stopLoading();
        this.mWebViewLy.removeAllViews();
        this.mWebView.destroy();
        super.onDestroy();
    }

    private void setDefaultDate() {
        this.isPrepared = true;
        loadWebViewUrl(this.jsObject);
    }

    private void initViews(View view) {
        this.mWebViewLy = (RelativeLayout) view.findViewById(R.id.duoqu_webview);
        this.mWebView = new WebView(this.mActivity);
        this.mWebViewLy.addView(this.mWebView);
        this.mWebView.setLayoutParams(new LayoutParams(-1, -1));
        this.mErrorPage = (RelativeLayout) view.findViewById(R.id.duoqu_error_page);
        this.mNetworkSetting = (RelativeLayout) view.findViewById(R.id.duoqu_network_setting);
        this.mDuoquBar = (RelativeLayout) view.findViewById(R.id.duoqu_progressbar);
        this.mWebView.getSettings().setLoadWithOverviewMode(true);
        this.mWebView.getSettings().setUseWideViewPort(true);
        this.mWebView.getSettings().setDatabaseEnabled(true);
        this.mWebView.getSettings().setTextSize(TextSize.NORMAL);
        this.mWebView.getSettings().setSaveFormData(true);
        this.mWebView.getSettings().setSavePassword(true);
        this.mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        this.mWebView.getSettings().setGeolocationDatabasePath(this.mActivity.getApplicationContext().getDir("database", 0).getPath());
        this.mWebView.getSettings().setGeolocationEnabled(true);
        this.mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                SdkWebFragment.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            }
        });
        this.mDuoquText = getResources().getString(R.string.duoqu_tip_duoqu_name);
        try {
            KeyManager.initAppKey();
        } catch (Exception e) {
        }
        this.mActionBar = (RelativeLayout) view.findViewById(R.id.action_bar);
        this.mTitleNameView = (TextView) view.findViewById(R.id.duoqu_title_name);
        this.mHeadBackView = (ImageView) view.findViewById(R.id.duoqu_header_back);
    }

    private void initWebView() {
        try {
            Paint pt = new Paint();
            this.mWebViewLy.setLayerType(2, pt);
            this.mWebView.setLayerType(1, pt);
        } catch (Throwable th) {
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
                return SdkWebActivity.toIntentByUrl(SdkWebFragment.this.getActivity(), view, url);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                SdkWebFragment.this.mProgressFlag = true;
                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView view, String title) {
                SdkWebFragment.this.mProgressFlag = false;
                super.onPageFinished(view, title);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode != -10) {
                    view.stopLoading();
                    view.clearView();
                    SdkWebFragment.this.errorPage();
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
                    Log.w(SdkWebFragment.TAG, "SdkWebFragmentcallback.invoke() is throwalbe");
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return SdkWebActivity.showAlertDialog(SdkWebFragment.this.mActivity, view, url, message, result);
            }

            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return SdkWebActivity.showConfirmDialog(SdkWebFragment.this.mActivity, view, url, message, result);
            }

            public void onProgressChanged(WebView view, int newProgress) {
                if (!SdkWebFragment.this.mProgressFlag || newProgress >= 90) {
                    if (!(SdkWebFragment.this.mDuoquBar.getVisibility() == 8 && SdkWebFragment.this.mWebViewLy.getVisibility() == 0)) {
                        SdkWebFragment.this.mDuoquBar.setVisibility(8);
                        SdkWebFragment.this.mWebViewLy.setVisibility(0);
                    }
                    if (SdkWebFragment.this.mBlockLoadingNetworkImage) {
                        SdkWebFragment.this.mWebView.getSettings().setBlockNetworkImage(false);
                        SdkWebFragment.this.mBlockLoadingNetworkImage = false;
                    }
                } else if (!(SdkWebFragment.this.mDuoquBar.getVisibility() == 0 && SdkWebFragment.this.mWebViewLy.getVisibility() == 4)) {
                    SdkWebFragment.this.mWebViewLy.setVisibility(4);
                    SdkWebFragment.this.mDuoquBar.setVisibility(0);
                }
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }

            public void onReceivedTitle(WebView view, String title) {
                SdkWebFragment.this.setTitle(title, SdkWebFragment.this.getParamData("menuName"));
                super.onReceivedTitle(view, title);
            }
        });
        this.mWebView.addJavascriptInterface(new SdkWebJavaScript(this), "injs");
    }

    private void initListener() {
        this.mHeadBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SdkWebFragment.this.hideFragmen();
            }
        });
        this.mNetworkSetting.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Exception e;
                Intent intent;
                int sdkVersion = 0;
                try {
                    sdkVersion = Integer.parseInt(VERSION.SDK);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                if (sdkVersion > 10) {
                    try {
                        intent = new Intent("android.settings.SETTINGS");
                    } catch (Exception e3) {
                        e2 = e3;
                        e2.printStackTrace();
                        return;
                    }
                }
                Intent intent2 = new Intent();
                try {
                    intent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.WirelessSettings"));
                    intent2.setAction("android.intent.action.VIEW");
                    intent = intent2;
                } catch (Exception e4) {
                    e2 = e4;
                    intent = intent2;
                    e2.printStackTrace();
                    return;
                }
                SdkWebFragment.this.startActivity(intent);
            }
        });
        Log.e(TAG, "initListener == ");
    }

    @SuppressLint({"NewApi"})
    public void loadWebViewUrl(JSONObject jsObject) {
        if (jsObject != null) {
            this.jsObject = jsObject;
            if (this.isPrepared) {
                String pageUrl;
                String aType = jsObject.optString("actionType");
                this.actionType = getParamData(NumberInfo.TYPE_KEY);
                if (aType == null || !"WEB_URL".equals(aType)) {
                    String host = getParamData("HOST");
                    if (StringUtils.isNull(host)) {
                        host = NetWebUtil.WEB_SERVER_URL;
                    }
                    pageUrl = getParamData("PAGEVIEW");
                    if (StringUtils.isNull(pageUrl)) {
                        String actionType = getParamData(NumberInfo.TYPE_KEY);
                        if ("WEB_MAP_SITE".equals(actionType)) {
                            String address = getParamData("address");
                            pageUrl = "http://api.map.baidu.com/geocoder?address=" + address + "&output=html&src=xiaoyuan|" + this.mDuoquText;
                            if (StringUtils.isNull(address)) {
                                errorPage();
                            } else {
                                this.mDuoquBar.setVisibility(8);
                                this.mWebView.loadUrl(pageUrl);
                            }
                            return;
                        }
                        pageUrl = "H5Service?actionType=" + actionType + "&channelId=" + KeyManager.channel;
                    }
                    if (!StringUtils.isNull(pageUrl)) {
                        pageUrl = host + "/" + pageUrl;
                    }
                } else {
                    pageUrl = getParamData(Constant.URLS);
                    Log.e("TAG", "pageUrl = " + pageUrl);
                }
                if (StringUtils.isNull(pageUrl)) {
                    errorPage();
                } else {
                    int isNetWork = XyUtil.checkNetWork(this.mActivity);
                    if (isNetWork == 0 || isNetWork == 1) {
                        this.mWebView.loadUrl(pageUrl);
                    } else {
                        errorPage();
                    }
                }
            }
        }
    }

    private void errorPage() {
        this.mDuoquBar.setVisibility(8);
        this.mWebView.setVisibility(8);
        this.mErrorPage.setVisibility(0);
        this.mTitleNameView.setText(R.string.duoqu_web_not_find_page);
    }

    protected void setTitle(String title, String menuName) {
        String titleName = "";
        if (!StringUtils.isNull(menuName)) {
            titleName = menuName;
        } else if (!StringUtils.isNull(title)) {
            titleName = title;
        }
        this.mTitleNameView.setText(titleName);
    }

    public WebView getWebView() {
        return this.mWebView;
    }

    public String getParamData(String key) {
        String res = null;
        if (!(key == null || this.jsObject == null)) {
            try {
                this.mJsObj = new JSONObject(this.jsObject.getString("JSONDATA"));
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

    public int checkOrientation() {
        return this.mActivity.getResources().getConfiguration().orientation;
    }

    public void hideFragmen() {
        if (this.mActivity instanceof IXYSmartMessageActivity) {
            ((IXYSmartMessageActivity) this.mActivity).finshFragemnt(this);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setActionBarHeight();
    }

    private void setActionBarHeight() {
        if (this.mActionBar != null) {
            TypedArray actionBarSizeTypeArray = this.mActivity.obtainStyledAttributes(new int[]{16843499});
            int actionBarHeight = (int) actionBarSizeTypeArray.getDimension(0, 0.0f);
            if (actionBarHeight != 0) {
                ViewGroup.LayoutParams pp = this.mActionBar.getLayoutParams();
                pp.height = actionBarHeight;
                this.mActionBar.setLayoutParams(pp);
            }
            actionBarSizeTypeArray.recycle();
        }
    }

    public String getType() {
        return this.actionType;
    }

    public void setParamData(String key, String value) {
        if (!StringUtils.isNull(key) && this.jsObject != null) {
            try {
                if (this.mJsObj == null) {
                    this.mJsObj = new JSONObject(this.jsObject.optString("JSONDATA"));
                }
                this.mJsObj.put(key, value);
            } catch (Exception e) {
                LogManager.e("XIAOYUAN", e.getMessage(), e);
            }
        }
    }

    public void sendBroadcast() {
        if (this.jsObject != null && this.mActivity != null) {
            if (this.mJsObj == null) {
                try {
                    this.mJsObj = new JSONObject(this.jsObject.optString("JSONDATA"));
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
                this.mActivity.sendBroadcast(intent, permission);
            }
        }
    }
}
