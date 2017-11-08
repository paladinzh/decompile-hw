package com.huawei.systemmanager.secpatch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.Locale;

public class SecurityPatchDetailActivity extends HsmActivity {
    private static final String TAG = "SecurityPatchSearchActivity";
    private TextView mDeatailTextView;
    private String mDetailDesChn = "";
    private String mDetailDesEng = "";
    private String mDetailFixVersion = "";
    private TextView mFixedTextView;
    private LinearLayout mFixedView;
    private TextView mIndexTextView;
    private String mOcidIndex = "";
    private String mSrcName = "";
    private TextView mSrcTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_patch_detail);
        getIntentValues();
        this.mIndexTextView = (TextView) findViewById(R.id.index_name);
        this.mSrcTextView = (TextView) findViewById(R.id.source_name);
        this.mDeatailTextView = (TextView) findViewById(R.id.detail_content);
        this.mFixedView = (LinearLayout) findViewById(R.id.fixed_version);
        this.mFixedTextView = (TextView) findViewById(R.id.fixed_version_name);
        if (!TextUtils.isEmpty(this.mDetailFixVersion)) {
            this.mFixedTextView.setText(this.mDetailFixVersion);
            this.mFixedView.setVisibility(0);
        }
        this.mIndexTextView.setText(this.mOcidIndex);
        this.mSrcTextView.setText(this.mSrcName);
    }

    protected void onResume() {
        super.onResume();
        try {
            if (ConstValues.CHINA_COUNTRY_CODE.equals(Locale.getDefault().getLanguage())) {
                this.mDeatailTextView.setText(this.mDetailDesChn);
            } else {
                this.mDeatailTextView.setText(this.mDetailDesEng);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.mDeatailTextView.setText(this.mDetailDesEng);
        }
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.e(TAG, "The intent is null");
            return;
        }
        this.mOcidIndex = intent.getStringExtra(ConstValues.INTENT_DETAIL_OCID);
        this.mSrcName = intent.getStringExtra(ConstValues.INTENT_DETAIL_SRC);
        this.mDetailDesChn = intent.getStringExtra(ConstValues.INTENT_DETAIL_CHN);
        this.mDetailDesEng = intent.getStringExtra(ConstValues.INTENT_DETAIL_ENG);
        this.mDetailFixVersion = intent.getStringExtra(ConstValues.INTENT_FIX_VERSION);
    }
}
