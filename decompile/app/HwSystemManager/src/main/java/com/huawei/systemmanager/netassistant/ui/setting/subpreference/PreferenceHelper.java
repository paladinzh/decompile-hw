package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.os.Handler;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import java.util.concurrent.Executor;

class PreferenceHelper {
    private Executor mExecutor;
    private Handler mHandler = new Handler();
    private IValueChangedListener mListener;
    private CharSequence mSummary2;
    private final Preference mWrapper;

    PreferenceHelper(Preference preference) {
        this.mWrapper = preference;
    }

    void initLayout() {
        this.mWrapper.setPersistent(false);
        this.mWrapper.setLayoutResource(R.layout.preference_status);
        this.mWrapper.setWidgetLayoutResource(R.layout.preference_widget_arrow);
    }

    void onBindView(View view) {
        TextView summary2Txt = (TextView) view.findViewById(R.id.summary2);
        if (summary2Txt == null) {
            return;
        }
        if (TextUtils.isEmpty(this.mSummary2)) {
            summary2Txt.setVisibility(8);
            return;
        }
        summary2Txt.setVisibility(0);
        summary2Txt.setText(this.mSummary2);
    }

    boolean setSummary2(String summary2) {
        if ((summary2 != null || this.mSummary2 == null) && (summary2 == null || summary2.equals(this.mSummary2))) {
            return false;
        }
        this.mSummary2 = summary2;
        return true;
    }

    void postSetSummary(final String str) {
        postRunnableUI(new Runnable() {
            public void run() {
                PreferenceHelper.this.mWrapper.setSummary(str);
            }
        });
    }

    Executor getExecutor() {
        if (this.mExecutor == null) {
            this.mExecutor = new HsmSingleExecutor();
        }
        return this.mExecutor;
    }

    void postRunnableUI(Runnable r) {
        this.mHandler.post(r);
    }

    void postRunnableAsync(Runnable r) {
        getExecutor().execute(r);
    }

    void setValueChangeListener(IValueChangedListener l) {
        this.mListener = l;
    }

    void callValueChanged(Object newValue) {
        if (this.mListener != null) {
            this.mListener.onValueChanged(newValue);
        }
    }
}
