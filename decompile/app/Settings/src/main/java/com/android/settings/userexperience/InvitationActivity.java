package com.android.settings.userexperience;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.util.ArrayList;
import java.util.List;

public class InvitationActivity extends SettingsDrawerActivity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String screenTitle = context.getResources().getString(2131627580);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.userexperience.InvitationActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.userexperience.InvitationActivity";
            boolean hasHwUE = Utils.hasPackageInfo(context.getPackageManager(), "com.huawei.bd");
            boolean hasHwLogUpload = Utils.hasPackageInfo(context.getPackageManager(), "com.huawei.logupload");
            if (hasHwUE || hasHwLogUpload) {
                result.add(data);
            }
            return result;
        }
    };
    private CheckBox agreeBox;
    private TextView involvedInfoView;
    private String key;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Context mContext = InvitationActivity.this;
            switch (buttonView.getId()) {
                case 2131887391:
                    try {
                        String str;
                        Secure.putInt(mContext.getContentResolver(), "user_experience_involved", isChecked ? 1 : 0);
                        ItemUseStat instance = ItemUseStat.getInstance();
                        String str2 = "user_experience_involved";
                        if (isChecked) {
                            str = "on";
                        } else {
                            str = "off";
                        }
                        instance.handleClick(mContext, 2, str2, str);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        }
    };
    int skinColor = -16776961;

    private class NoUnderLineClickSpan extends ClickableSpan {
        private NoUnderLineClickSpan() {
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(InvitationActivity.this.getResources().getColor(2131427515));
            ds.setUnderlineText(false);
        }

        public void onClick(View widget) {
            Log.d("InvitationActivity", "onClick");
            if (widget instanceof TextView) {
                Intent intent = new Intent();
                intent.setClass(InvitationActivity.this, DeclarationActivity.class);
                InvitationActivity.this.startActivity(intent);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayOptions(32768);
        }
        this.key = intent.getStringExtra("portal_key");
        if (this.key == null || !(this.key.equals("setting_access") || this.key.equals("Bootstrap_access"))) {
            this.key = "setting_access";
        }
        try {
            this.skinColor = getResources().getColor(2131427413);
        } catch (NotFoundException e) {
        }
        setTheme(2131755518);
        setContentView(2130969249);
        findViewById(2131887384).setBackgroundColor(this.skinColor);
        initTitle();
        this.involvedInfoView = (TextView) findViewById(2131887392);
        this.agreeBox = (CheckBox) findViewById(2131887391);
        this.agreeBox.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
        findViewById(2131887385).setBackgroundColor(getResources().getColor(2131427554));
        String linkStr = getResources().getString(2131627598);
        String tmpInfo = getResources().getString(2131627601);
        String agreeInfo = String.format(tmpInfo, new Object[]{linkStr}) + " ";
        int start = agreeInfo.lastIndexOf(linkStr);
        int end = start + linkStr.length();
        setClickableSpanForTextView(this.involvedInfoView, new NoUnderLineClickSpan(), agreeInfo, start, end);
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        win.setStatusBarColor(getResources().getColor(2131427554));
    }

    protected void onResume() {
        super.onResume();
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        boolean involved_value = "Bootstrap_access".equals(this.key) ? true : Secure.getInt(getContentResolver(), "user_experience_involved", 0) == 1;
        if (this.agreeBox != null) {
            this.agreeBox.setChecked(involved_value);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if ("Bootstrap_access".equals(this.key)) {
            try {
                if (this.agreeBox != null) {
                    Secure.putInt(getContentResolver(), "user_experience_involved", this.agreeBox.isChecked() ? 1 : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initTitle() {
        TextView titleView = (TextView) findViewById(2131887386);
        if (titleView != null) {
            String title = getResources().getString(2131627598);
            String titleInfo = String.format(getResources().getString(2131627599), new Object[]{title});
            int start = titleInfo.lastIndexOf(title);
            int end = start + title.length();
            ForegroundColorSpan titleSpan = new ForegroundColorSpan(getResources().getColor(2131427515));
            SpannableString sp = new SpannableString(titleInfo);
            sp.setSpan(titleSpan, start, end, 33);
            titleView.setText(sp);
        }
    }

    private void setClickableSpanForTextView(TextView tv, ClickableSpan clickableSpan, String text, int start, int end) {
        if (start < 0 || start >= end || end >= text.length()) {
            tv.setText(text);
            return;
        }
        SpannableString sp = new SpannableString(text);
        sp.setSpan(clickableSpan, start, end, 33);
        tv.setText(sp);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setFocusable(false);
        tv.setClickable(false);
        tv.setLongClickable(false);
    }
}
