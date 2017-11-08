package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util.SimpleTextWatcher;

public abstract class BaseTrafficSetPreference extends AbsDialogPreference {
    private static final int DECIMAL_PART_MAXLEN = 2;
    private static final int INTEGER_PART_MAXLEN = 6;
    private static final long MAX_MB_VALUE = 602931200;
    private ArrayAdapter<CharSequence> adapter = null;
    private EditText mEditText;
    private TextWatcher mInputTextWatcher = new SimpleTextWatcher() {
        public void afterTextChanged(Editable edit) {
            String temp = edit.toString();
            int posDot = temp.indexOf(".");
            if (posDot < 0) {
                if (temp.length() > 6) {
                    edit.delete(6, 7);
                }
                return;
            }
            if ((temp.length() - posDot) - 1 > 2) {
                edit.delete((posDot + 2) + 1, (posDot + 2) + 2);
            }
        }
    };
    private Button mUnitBtn;
    private Button unitButton;

    public class UnitOnClickListener implements OnClickListener {
        public void onClick(View view) {
            if (TextUtils.equals(BaseTrafficSetPreference.this.unitButton.getText(), (CharSequence) BaseTrafficSetPreference.this.adapter.getItem(0))) {
                BaseTrafficSetPreference.this.unitButton.setText((CharSequence) BaseTrafficSetPreference.this.adapter.getItem(1));
            } else {
                BaseTrafficSetPreference.this.unitButton.setText((CharSequence) BaseTrafficSetPreference.this.adapter.getItem(0));
            }
            if (BaseTrafficSetPreference.this.isEditTextHasContent()) {
                BaseTrafficSetPreference.this.setPositiveBtnEnable(true);
            }
        }
    }

    protected abstract long getEditTxtValue();

    protected abstract void onSetPackage(long j);

    public BaseTrafficSetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseTrafficSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setDialogLayoutResource(R.layout.traffic_num_setting_layout);
    }

    protected void onBindDialogView(View view) {
        this.mEditText = (EditText) view.findViewById(R.id.flow_limited_editText);
        this.mEditText.addTextChangedListener(this.mInputTextWatcher);
        this.mEditText.addTextChangedListener(this.mPositiveBtnTextWatcher);
        this.adapter = ArrayAdapter.createFromResource(getContext(), R.array.size_unit_no_kb, 17367048);
        this.unitButton = (Button) view.findViewById(R.id.size_type);
        this.unitButton.setText((CharSequence) this.adapter.getItem(0));
        this.unitButton.setOnClickListener(new UnitOnClickListener());
        this.mUnitBtn = this.unitButton;
        long value = getEditTxtValue();
        if (value < 0) {
            this.mUnitBtn.setText((CharSequence) this.adapter.getItem(0));
            return;
        }
        String[] result = CommonMethodUtil.formatDivideFileSize(getContext(), value, false);
        this.mEditText.setText(String.valueOf(result[0]));
        if (TextUtils.equals(result[1], getContext().getString(17039499))) {
            this.mUnitBtn.setText((CharSequence) this.adapter.getItem(0));
        } else if (TextUtils.equals(result[1], getContext().getString(17039500))) {
            this.mUnitBtn.setText((CharSequence) this.adapter.getItem(1));
        } else {
            this.mUnitBtn.setText((CharSequence) this.adapter.getItem(0));
        }
        setEditTxtFlagEnd(this.mEditText);
    }

    protected void showDialog(Bundle state) {
        super.showDialog(state);
        setPositiveBtnEnable(false);
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && this.mEditText != null && this.mUnitBtn != null) {
            String edit = this.mEditText.getText().toString();
            if (!TextUtils.isEmpty(edit)) {
                try {
                    float editNum = Float.valueOf(edit).floatValue();
                    long byteCount = CommonMethodUtil.unitConvert(editNum, this.mUnitBtn.getText().toString());
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(unit));
                    HsmStat.statE((int) Events.E_NETASSISTANT_VALUE_SET_UNIT, statParam);
                    onSetPackage(byteCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean needInputMethod() {
        return true;
    }

    private boolean isEditTextHasContent() {
        boolean z = false;
        if (this.mEditText == null) {
            return false;
        }
        if (!TextUtils.isEmpty(this.mEditText.getText().toString())) {
            z = true;
        }
        return z;
    }
}
