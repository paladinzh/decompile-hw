package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util.SimpleTextWatcher;

public class AbsDialogPreference extends DialogPreference implements ICardPrefer {
    protected CardItem mCard;
    protected final TextWatcher mPositiveBtnTextWatcher = new SimpleTextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().trim().length() == 0) {
                AbsDialogPreference.this.setPositiveBtnEnable(false);
            } else {
                AbsDialogPreference.this.setPositiveBtnEnable(true);
            }
        }
    };
    private PreferenceHelper preferenceHelper = new PreferenceHelper(this);

    public AbsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }

    public AbsDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValue();
    }

    protected void initValue() {
        this.preferenceHelper.initLayout();
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.preferenceHelper.onBindView(view);
    }

    public void setCard(CardItem card) {
        this.mCard = card;
    }

    public void refreshPreferShow() {
    }

    protected void setEditTxtFlagEnd(EditText editText) {
        if (editText != null) {
            Editable eText = editText.getText();
            if (eText != null) {
                Selection.setSelection(eText, eText.length());
            }
        }
    }

    public void setSummary2(String summary2) {
        if (this.preferenceHelper.setSummary2(summary2)) {
            notifyChanged();
        }
    }

    public void setSummary2(int resId) {
        setSummary2(getContext().getString(resId));
    }

    public void postSetSummary(String str) {
        this.preferenceHelper.postSetSummary(str);
    }

    public void postRunnableAsync(Runnable r) {
        this.preferenceHelper.postRunnableAsync(r);
    }

    public void setValueChangedListener(IValueChangedListener l) {
        this.preferenceHelper.setValueChangeListener(l);
    }

    protected void callValueChanged(Object newValue) {
        this.preferenceHelper.callValueChanged(newValue);
    }

    protected void setPositiveBtnEnable(boolean enable) {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(-1).setEnabled(enable);
        }
    }
}
