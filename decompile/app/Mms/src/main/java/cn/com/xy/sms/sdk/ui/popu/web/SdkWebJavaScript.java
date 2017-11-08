package cn.com.xy.sms.sdk.ui.popu.web;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.util.ParseManager;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;

public class SdkWebJavaScript {
    private IActivityParamForJS mActivityParam;

    public SdkWebJavaScript(IActivityParamForJS activityParam) {
        this.mActivityParam = activityParam;
    }

    @JavascriptInterface
    public void runOnAndroidJavaScript(String str) {
    }

    @JavascriptInterface
    public String getConfigByKey(String cfKey) {
        return this.mActivityParam.getParamData(cfKey);
    }

    @JavascriptInterface
    public String getExtendValue(int type, String jsonStr) {
        try {
            JSONObject res = DuoquUtils.getSdkDoAction().getExtendValue(type, new JSONObject(jsonStr));
            if (res != null) {
                return res.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @JavascriptInterface
    public int checkOrientation() {
        return this.mActivityParam.checkOrientation();
    }

    @JavascriptInterface
    public void asyncRequest(String url, String postParamValue, final String callBackJSFunc) {
        NetWebUtil.sendPostRequest(url, postParamValue, new XyCallBack() {
            public void execute(final Object... obj) {
                WebView webView = SdkWebJavaScript.this.mActivityParam.getWebView();
                final String str = callBackJSFunc;
                webView.post(new Runnable() {
                    public void run() {
                        SdkWebJavaScript.this.mActivityParam.getWebView().loadUrl("javascript:" + str + "('" + obj[0] + "','" + obj[1] + "')");
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void asyncRequestByParamKey(String url, String postParamKey, final String callBackJSFunc) {
        NetWebUtil.sendPostRequest(url, this.mActivityParam.getParamData(postParamKey), new XyCallBack() {
            public void execute(final Object... obj) {
                WebView webView = SdkWebJavaScript.this.mActivityParam.getWebView();
                final String str = callBackJSFunc;
                webView.post(new Runnable() {
                    public void run() {
                        SdkWebJavaScript.this.mActivityParam.getWebView().loadUrl("javascript:" + str + "('" + ((String) obj[0]) + "','" + ((String) obj[1]) + "')");
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public boolean downloadApp(String jsonParam) {
        return doAction("download", jsonParam);
    }

    @JavascriptInterface
    public boolean doAction(String actionType, String jsonParam) {
        if (jsonParam != null) {
            try {
                JSONObject jsObj = new JSONObject(jsonParam);
                Iterator<String> it = jsObj.keys();
                if (it != null) {
                    HashMap<String, Object> mapParam = new HashMap();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        mapParam.put(key, (String) jsObj.get(key));
                    }
                    return DuoquUtils.doCustomAction(this.mActivityParam.getActivity(), actionType, mapParam);
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript doAction", e);
            }
        }
        return false;
    }

    @JavascriptInterface
    public void closeWebView() {
        this.mActivityParam.hideFragmen();
    }

    @JavascriptInterface
    public long openDefService() {
        return ParseManager.setDefServiceSwitch(this.mActivityParam.getActivity(), "1");
    }

    @JavascriptInterface
    public long saveValueByKey(String key, String value) {
        try {
            SysParamEntityManager.insertOrUpdateKeyValue(this.mActivityParam.getActivity(), key, value, null);
            return 0;
        } catch (Exception e) {
            return -2;
        }
    }

    @JavascriptInterface
    public String getValueByKey(String key) {
        return SysParamEntityManager.queryValueParamKey(this.mActivityParam.getActivity(), key);
    }

    @JavascriptInterface
    public long closeDefService() {
        return ParseManager.setDefServiceSwitch(this.mActivityParam.getActivity(), "0");
    }

    @JavascriptInterface
    public String queryDefServiceSwitch() {
        return ParseManager.queryDefService(this.mActivityParam.getActivity());
    }

    @JavascriptInterface
    public boolean checkHasAppName(String appName) {
        try {
            this.mActivityParam.getActivity().getPackageManager().getPackageInfo(appName, 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public void setConfigByKey(String cfKey, String value) {
        this.mActivityParam.setParamData(cfKey, value);
    }
}
