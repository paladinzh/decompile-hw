package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util.SimpleTextWatcher;
import com.huawei.systemmanager.util.HwLog;

public class StartDayPreference extends AbsDialogPreference {
    public static final String TAG = "StartDayPreference";
    private TextWatcher mDateEditTextWather = new SimpleTextWatcher() {
        private static final int DECIMAL_PART_MAXLEN = 2;

        public void afterTextChanged(Editable s) {
            String tmp = s.toString();
            if (s.toString().trim().length() == 0) {
                StartDayPreference.this.setPositiveBtnEnable(false);
                return;
            }
            StartDayPreference.this.setPositiveBtnEnable(true);
            int value = Integer.parseInt(tmp);
            if (value > 31) {
                s.delete(1, 2);
            } else if (value <= 0) {
                s.clear();
            }
        }
    };
    private EditText mEditText;
    private Runnable mLoadDataTask = new Runnable() {
        public void run() {
            if (StartDayPreference.this.mCard == null) {
                HwLog.e(StartDayPreference.TAG, "mLoadDataTask card is null!");
                return;
            }
            int startDay = StartDayPreference.this.mCard.getStartDay();
            StartDayPreference.this.postSetSummary(startDay > 0 ? String.valueOf(startDay) : StartDayPreference.this.getContext().getString(R.string.pref_not_set));
        }
    };

    public StartDayPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StartDayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setDialogLayoutResource(R.layout.traffic_setting_dialog_startday);
        setTitle(R.string.net_assistant_month_startday);
        setDialogTitle(R.string.net_assistant_month_startday);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadDataTask);
    }

    protected void onBindDialogView(View view) {
        if (this.mCard == null) {
            HwLog.e(TAG, "onBindDialogView mcard is null!");
            return;
        }
        this.mEditText = (EditText) view.findViewById(R.id.count_day);
        int countDay = this.mCard.getStartDay();
        if (countDay > 0) {
            this.mEditText.setText(String.valueOf(countDay));
        }
        this.mEditText.addTextChangedListener(this.mDateEditTextWather);
        setEditTxtFlagEnd(this.mEditText);
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && this.mEditText != null && this.mCard != null) {
            int countDay = 1;
            String editTxt = this.mEditText.getText().toString();
            try {
                if (!TextUtils.isEmpty(editTxt)) {
                    countDay = Integer.parseInt(editTxt);
                }
            } catch (Exception e) {
                HwLog.e(TAG, "set countDay failed");
            }
            this.mCard.setStartDay(countDay);
            refreshPreferShow();
        }
    }

    protected boolean needInputMethod() {
        return true;
    }
}
