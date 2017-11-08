package com.android.contacts.hap.camcard;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.google.android.gms.R;
import java.util.Locale;

public class AboutActivity extends Activity {
    private TextView download;
    private ActionBar mActonBar;
    private TextView privacy;

    private static class PrivacyPolicySpan extends ClickableSpan {
        private Context context;
        private boolean isPrivacy;
        private int type;

        private PrivacyPolicySpan(Context context, int type, boolean isPrivacy) {
            this.type = 0;
            this.type = type;
            this.context = context;
            this.isPrivacy = isPrivacy;
        }

        public void onClick(View view) {
            Activity activity = this.context;
            if (this.type == 0) {
                AboutActivity.startPrivacyActivity(activity);
            } else {
                AboutActivity.downloadFromUri(activity);
            }
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            int color = ImmersionUtils.getControlColor(this.context.getResources());
            if (color == 0) {
                color = this.context.getResources().getColor(R.color.download_selector_color_state_default);
            }
            ds.setColor(color);
            ds.setUnderlineText(this.isPrivacy);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setContentView(R.layout.camcard_about);
        this.mActonBar = getActionBar();
        this.mActonBar.setDisplayHomeAsUpEnabled(true);
        this.download = (TextView) findViewById(R.id.cc_href);
        this.privacy = (TextView) findViewById(R.id.privacy);
        setSpanString(this.download, R.string.camcard_market, R.string.camcard_download2, 1, false);
        setSpanString(this.privacy, R.string.camcard_privacy_policy, R.string.camcard_privacy_ref_message, 0, true);
    }

    private void setSpanString(TextView textView, int resId, int resIdRef, int type, boolean isPrivacy) {
        String href = getString(resId);
        String ref = getString(resIdRef, new Object[]{href});
        int start = ref.indexOf(href);
        int end = start + href.length();
        SpannableString sString = new SpannableString(ref);
        if (start >= 0 && end <= sString.length()) {
            sString.setSpan(new PrivacyPolicySpan(this, type, isPrivacy), start, end, 33);
        }
        textView.append(sString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void startPrivacyActivity(Activity activity) {
        try {
            activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getLocaleUrl())));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    private static String getLocaleUrl() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        String url = "http://s.intsig.net/r/terms/PP_CamCard_en-us.html";
        if (Locale.CHINESE.getLanguage().equals(lang)) {
            if ("TW".equals(country) || "HK".equals(country)) {
                return "http://s.intsig.net/r/terms/PP_CamCard_zh-tw.html";
            }
            return "http://s.intsig.net/r/terms/PP_CamCard_zh-cn.html";
        } else if (Locale.JAPANESE.getLanguage().equals(lang)) {
            return "http://s.intsig.net/r/terms/PP_CamCard_ja-jp.html";
        } else {
            if (Locale.KOREAN.getLanguage().equals(lang)) {
                return "http://s.intsig.net/r/terms/PP_CamCard_ko-kr.html";
            }
            return url;
        }
    }

    private static void downloadFromUri(Activity activity) {
        String packageName;
        String MARKET_BASE = "market://details?id=";
        try {
            Intent localIntent1 = new Intent("android.intent.action.VIEW");
            localIntent1.setPackage("com.huawei.appmarket");
            localIntent1.setData(Uri.parse("market://details?id=com.intsig.BizCardReader"));
            localIntent1.setFlags(268435456);
            activity.startActivity(localIntent1);
        } catch (ActivityNotFoundException e) {
            if (EmuiFeatureManager.isChinaArea()) {
                packageName = "com.intsig.BizCardReader";
            } else {
                packageName = "com.intsig.BCRLite";
            }
            try {
                String pkg = "market://details?id=" + packageName;
                Intent localIntent2 = new Intent("android.intent.action.VIEW");
                localIntent2.setData(Uri.parse(pkg));
                localIntent2.setFlags(268435456);
                activity.startActivity(localIntent2);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(activity, R.string.quickcontact_missing_app_Toast, 0).show();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
