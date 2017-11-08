package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import java.text.NumberFormat;

public class EditSlideDurationActivity extends HwBaseActivity {
    private View contentView;
    private AlertDialog mAertDialog;
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
            switch (arg1) {
                case -2:
                    EditSlideDurationActivity.this.finish();
                    return;
                case -1:
                    EditSlideDurationActivity.this.editDone();
                    return;
                default:
                    return;
            }
        }
    };
    private int mCurSlide;
    private EditText mDurEditText;
    private final OnKeyListener mOnKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != 0) {
                return false;
            }
            switch (keyCode) {
                case 23:
                    EditSlideDurationActivity.this.editDone();
                    break;
            }
            return false;
        }
    };
    private Bundle mState;
    private final TextWatcher mTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (TextUtils.isEmpty(arg0)) {
                EditSlideDurationActivity.this.mAertDialog.getButton(-1).setEnabled(false);
            } else {
                try {
                    if (Integer.parseInt(arg0.toString()) <= 0) {
                        EditSlideDurationActivity.this.mAertDialog.getButton(-1).setEnabled(false);
                    } else {
                        EditSlideDurationActivity.this.mAertDialog.getButton(-1).setEnabled(true);
                    }
                } catch (NumberFormatException e) {
                    MLog.e("EditSlideDurationActivity", "showEditSlideDurationDialog NumberFormatException", (Throwable) e);
                    EditSlideDurationActivity.this.mAertDialog.getButton(-1).setEnabled(false);
                }
            }
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable arg0) {
        }
    };
    private int mTotal;

    protected void onCreate(Bundle icicle) {
        int duration;
        super.onCreate(icicle);
        this.contentView = LayoutInflater.from(this).inflate(R.layout.edit_slide_duration, null);
        if (icicle == null) {
            Intent intent = getIntent();
            this.mCurSlide = intent.getIntExtra("slide_index", 1);
            this.mTotal = intent.getIntExtra("slide_total", 1);
            duration = intent.getIntExtra("dur", 8);
        } else {
            this.mState = icicle.getBundle(ParseItemManager.STATE);
            if (this.mState != null) {
                this.mCurSlide = this.mState.getInt("slide_index", 1);
                this.mTotal = this.mState.getInt("slide_total", 1);
                duration = this.mState.getInt("dur", 8);
            } else {
                return;
            }
        }
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        String titleStr = new StringBuffer().append(getString(R.string.duration_selector_title)).append(" ").append(nf.format((long) (this.mCurSlide + 1))).append("/").append(nf.format((long) this.mTotal)).toString();
        this.mDurEditText = (EditText) this.contentView.findViewById(R.id.text);
        this.mDurEditText.setText(nf.format((long) duration));
        this.mDurEditText.setSelection(String.valueOf(duration).length());
        this.mDurEditText.setOnKeyListener(this.mOnKeyListener);
        this.mDurEditText.addTextChangedListener(this.mTextWatcher);
        this.mDurEditText.requestFocus();
        showEditSlideDurationDialog(titleStr);
    }

    protected void onSaveInstanceState(Bundle outState) {
        int durValue;
        super.onSaveInstanceState(outState);
        this.mState = new Bundle();
        this.mState.putInt("slide_index", this.mCurSlide);
        this.mState.putInt("slide_total", this.mTotal);
        try {
            durValue = Integer.parseInt(this.mDurEditText.getText().toString());
        } catch (NumberFormatException e) {
            durValue = 5;
        }
        this.mState.putInt("dur", durValue);
        outState.putBundle(ParseItemManager.STATE, this.mState);
    }

    protected void editDone() {
        try {
            if (Integer.parseInt(this.mDurEditText.getText().toString()) <= 0) {
                notifyUser(R.string.duration_zero_Toast);
                return;
            }
            setResult(-1, new Intent(this.mDurEditText.getText().toString()));
            finish();
        } catch (NumberFormatException e) {
        }
    }

    private void notifyUser(int msgId) {
        this.mDurEditText.requestFocus();
        this.mDurEditText.selectAll();
        Toast.makeText(this, msgId, 0).show();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 17432577);
    }

    private void showEditSlideDurationDialog(String title) {
        int themeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        if (themeID == 0) {
            themeID = 3;
        }
        this.mAertDialog = new Builder(this, themeID).setTitle(title).setView(this.contentView).create();
        this.mAertDialog.getWindow().setSoftInputMode(37);
        this.mAertDialog.setButton(-1, getString(R.string.yes), this.mClickListener);
        this.mAertDialog.setButton(-2, getString(R.string.no), this.mClickListener);
        this.mAertDialog.show();
    }
}
