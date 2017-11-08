package cn.com.xy.sms.sdk.ui.popu.web;

import android.app.Activity;
import android.webkit.WebView;

public interface IActivityParamForJS {
    int checkOrientation();

    Activity getActivity();

    String getParamData(String str);

    String getType();

    WebView getWebView();

    void hideFragmen();

    void setParamData(String str, String str2);
}
