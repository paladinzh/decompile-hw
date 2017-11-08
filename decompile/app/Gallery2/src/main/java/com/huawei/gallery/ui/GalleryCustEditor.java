package com.huawei.gallery.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.util.ColorfulUtils;

public class GalleryCustEditor {
    private View mBackView;
    private View mClearButton;
    private Context mContext;
    private Dialog mDialog;
    private Window mDialogWindow;
    private View mEditLayout;
    private EditText mEditText;
    private Runnable mExitEditRunnable = new Runnable() {
        public void run() {
            if (GalleryCustEditor.this.mDialog != null && GalleryCustEditor.this.mDialog.isShowing()) {
                GalleryCustEditor.this.mDialog.dismiss();
            }
            ((InputMethodManager) GalleryCustEditor.this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(GalleryCustEditor.this.mEditLayout.getWindowToken(), 2);
        }
    };
    private boolean mIsSecureCameraMode;
    private OnTextChangedListener mListener;
    private Runnable mPrepareEditRunnable = new Runnable() {
        public void run() {
            GalleryCustEditor.this.mEditText.selectAll();
            GalleryCustEditor.this.mEditText.requestFocus();
            ((InputMethodManager) GalleryCustEditor.this.mContext.getSystemService("input_method")).toggleSoftInput(0, 2);
        }
    };
    private int mSizeLimit = 0;
    private View mSubmitButton;

    public interface OnTextChangedListener {
        void onTextChanged(String str);
    }

    public interface EditorController extends OnTextChangedListener {
        int getSizeLimit();
    }

    public GalleryCustEditor(Context context, String input, OnTextChangedListener listener, boolean isSecureCameraMode) {
        this.mListener = listener;
        this.mIsSecureCameraMode = isSecureCameraMode;
        initEditView(context, input);
    }

    public GalleryCustEditor(Context context, String input, EditorController listener, boolean isSecureCameraMode) {
        this.mListener = listener;
        this.mSizeLimit = listener.getSizeLimit();
        this.mIsSecureCameraMode = isSecureCameraMode;
        initEditView(context, input);
    }

    private void initEditView(Context context, String input) {
        this.mContext = context;
        this.mDialog = new Dialog(GalleryUtils.getHwThemeContext(context, "androidhwext:style/Theme.Emui"), R.style.editor_inputDialogTheme);
        this.mDialogWindow = this.mDialog.getWindow();
        this.mDialogWindow.requestFeature(1);
        updateDialogWindowSize();
        this.mDialogWindow.addFlags(134218752);
        if (this.mIsSecureCameraMode) {
            this.mDialogWindow.addFlags(524288);
        }
        this.mDialog.setContentView(R.layout.gallery_cust_editor);
        this.mDialog.show();
        initializeViews();
        if (!TextUtils.isEmpty(input)) {
            this.mEditText.setText(input);
        }
        if (this.mSizeLimit > 0) {
            GalleryLog.d("GalleryCustEditor", "editor size limit : " + this.mSizeLimit);
            this.mEditText.setFilters(new InputFilter[]{new LengthFilter(this.mSizeLimit)});
        }
        this.mEditText.post(this.mPrepareEditRunnable);
        this.mEditLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                GalleryCustEditor.this.hide();
            }
        });
    }

    public void pause() {
        if (this.mExitEditRunnable != null) {
            hide();
        }
    }

    private void hide() {
        this.mExitEditRunnable.run();
    }

    private void initializeViews() {
        this.mClearButton = this.mDialog.findViewById(R.id.edit_clear);
        this.mSubmitButton = this.mDialog.findViewById(R.id.edit_submit);
        this.mSubmitButton.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_ok));
        this.mBackView = this.mDialog.findViewById(R.id.edit_back);
        this.mBackView.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_cancel));
        this.mEditText = (EditText) this.mDialog.findViewById(R.id.edit_text);
        ColorfulUtils.decorateColorfulForEditText(this.mContext, this.mEditText);
        this.mEditLayout = this.mDialog.findViewById(R.id.gallery_cust_edit);
        this.mClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GalleryCustEditor.this.mEditText.setText("");
            }
        });
        this.mSubmitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (GalleryCustEditor.this.mListener != null) {
                    GalleryCustEditor.this.mListener.onTextChanged(GalleryCustEditor.this.mEditText.getText().toString());
                }
                GalleryCustEditor.this.hide();
            }
        });
        this.mBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GalleryCustEditor.this.hide();
            }
        });
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                GalleryCustEditor.this.mClearButton.setVisibility(TextUtils.isEmpty(s.toString()) ? 8 : 0);
                if (GalleryCustEditor.this.mContext != null) {
                    ((Activity) GalleryCustEditor.this.mContext).onUserInteraction();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void updateDialogWindowSize() {
        LayoutParams lp = this.mDialogWindow.getAttributes();
        lp.width = -1;
        lp.height = -1;
        this.mDialogWindow.setAttributes(lp);
        this.mDialogWindow.getDecorView().setMinimumWidth(GalleryUtils.getHeightPixels());
    }
}
