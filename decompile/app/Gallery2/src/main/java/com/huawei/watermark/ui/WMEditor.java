package com.huawei.watermark.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.report.HwWatermarkReporter;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;

public class WMEditor {
    public static final String TYPENUM = "number";
    public static final String TYPENUMANDDECIMAL = "number|decimal";
    public static final String TYPESIGNEDNUM = "signedNumber";
    public static final String TYPETEXT = "text";
    private final char[] FLOATCHARS = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.'};
    private final char[] INTEGERCHARS = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private View mBackView;
    private View mClearButton;
    private Context mContext;
    private Dialog mDialog;
    private EditText mEditText;
    private Runnable mExitEditRunnable = new Runnable() {
        public void run() {
            if (WMEditor.this.mDialog != null) {
                WMEditor.this.mDialog.dismiss();
            }
            ((InputMethodManager) WMEditor.this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(WMEditor.this.mWMEditLayout.getWindowToken(), 2);
        }
    };
    private OnTextChangedListener mListener;
    private LogicDelegate mLogicDelegate;
    private int mMaxInputLength = -1;
    private Runnable mPrepareEditRunnable = new Runnable() {
        public void run() {
            WMEditor.this.mEditText.selectAll();
            WMEditor.this.mEditText.requestFocus();
            if (!WMResourceUtil.isTabletProduct(WMEditor.this.mContext)) {
                ((InputMethodManager) WMEditor.this.mContext.getSystemService("input_method")).toggleSoftInput(0, 2);
            }
        }
    };
    private View mSubmitButton;
    private View mWMEditLayout;

    public interface OnTextChangedListener {
        void onDialogDismissed();

        void onTextChanged(String str);
    }

    public void pause() {
        if (this.mExitEditRunnable == null) {
            return;
        }
        if (this.mLogicDelegate != null) {
            hide(this.mLogicDelegate.getShouldHideSoftKeyboard());
        } else {
            hide();
        }
    }

    public WMEditor(Context context, String input, String texttype, boolean showWhenLocked, OnTextChangedListener mListener, LogicDelegate logicDelegate) {
        this.mListener = mListener;
        this.mLogicDelegate = logicDelegate;
        initEditView(context, input, texttype, showWhenLocked);
    }

    public WMEditor(Context context, String input, String texttype, boolean showWhenLocked, int maxInputLength, OnTextChangedListener mListener, LogicDelegate logicDelegate) {
        this.mListener = mListener;
        this.mMaxInputLength = maxInputLength;
        this.mLogicDelegate = logicDelegate;
        initEditView(context, input, texttype, showWhenLocked);
    }

    private void initEditView(Context context, String input, String texttype, boolean showWhenLocked) {
        this.mContext = context;
        this.mDialog = new Dialog(context, WMResourceUtil.getStyleId(context, "wm_jar_inputDialogTheme"));
        this.mDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode != 4) {
                    return false;
                }
                WMEditor.this.hide();
                return true;
            }
        });
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                if (WMEditor.this.mListener != null) {
                    WMEditor.this.mListener.onDialogDismissed();
                }
            }
        });
        this.mDialog.setContentView(WMResourceUtil.getLayoutId(context, "wm_jar_food_edit"));
        Window dialogWindow = this.mDialog.getWindow();
        LayoutParams lp = dialogWindow.getAttributes();
        lp.width = -1;
        lp.height = -1;
        if (showWhenLocked) {
            lp.flags |= 524288;
        }
        lp.flags |= 134217728;
        lp.flags |= 1024;
        dialogWindow.setAttributes(lp);
        this.mDialog.show();
        initializeViews();
        if (!WMStringUtil.isEmptyString(input)) {
            this.mEditText.setText(input);
        }
        if (!WMStringUtil.isEmptyString(texttype)) {
            if (TYPETEXT.equalsIgnoreCase(texttype)) {
                this.mEditText.setInputType(1);
            } else if (TYPENUM.equalsIgnoreCase(texttype)) {
                this.mEditText.setKeyListener(new NumberKeyListener() {
                    protected char[] getAcceptedChars() {
                        return WMEditor.this.INTEGERCHARS;
                    }

                    public int getInputType() {
                        return 2;
                    }
                });
            } else if (TYPESIGNEDNUM.equalsIgnoreCase(texttype)) {
                this.mEditText.setInputType(4098);
            } else if (TYPENUMANDDECIMAL.equalsIgnoreCase(texttype)) {
                this.mEditText.setKeyListener(new NumberKeyListener() {
                    protected char[] getAcceptedChars() {
                        return WMEditor.this.FLOATCHARS;
                    }

                    public int getInputType() {
                        return FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
                    }
                });
            } else {
                this.mEditText.setInputType(1);
            }
        }
        this.mEditText.post(this.mPrepareEditRunnable);
        this.mWMEditLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WMEditor.this.hide();
            }
        });
    }

    private void hide() {
        hide(true);
    }

    private void hide(boolean shouldHide) {
        if (shouldHide) {
            this.mExitEditRunnable.run();
        }
    }

    private void initializeViews() {
        this.mClearButton = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_clear"));
        this.mSubmitButton = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_submit"));
        this.mSubmitButton.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_ok));
        this.mBackView = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_back"));
        this.mBackView.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_cancel));
        this.mEditText = (EditText) this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_text"));
        WMStringUtil.setEditTextStringStyle(this.mEditText);
        if (this.mMaxInputLength > 0) {
            this.mEditText.setFilters(new InputFilter[]{new LengthFilter(this.mMaxInputLength)});
        }
        this.mWMEditLayout = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "water_mark_food_edit"));
        this.mClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WMEditor.this.mEditText.setText("");
            }
        });
        this.mSubmitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwWatermarkReporter.reportHwWatermarkEdit(WMEditor.this.mContext);
                if (WMEditor.this.mListener != null) {
                    WMEditor.this.mListener.onTextChanged(WMEditor.this.mEditText.getText().toString());
                }
                WMEditor.this.hide();
            }
        });
        this.mBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WMEditor.this.hide();
            }
        });
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WMEditor.this.mClearButton.setVisibility(WMStringUtil.isEmptyString(s.toString()) ? 8 : 0);
                if (WMEditor.this.mContext != null) {
                    ((Activity) WMEditor.this.mContext).onUserInteraction();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }
}
