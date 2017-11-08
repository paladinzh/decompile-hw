package com.huawei.watermark.manager.parse;

import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.ui.WMEditor.OnTextChangedListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMEditText extends WMText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMEditText.class.getSimpleName());
    private String beShow;
    private DecoratorEditTextRunnable mDecoratorEditTextRunnable;
    private TextView mTextView;
    private WMEditor mWmEditor;
    private int maxwidth;
    private String texttype;

    private class DecoratorEditTextRunnable implements Runnable {
        private boolean mCancel;

        private DecoratorEditTextRunnable() {
        }

        public void run() {
            String edittext = WMLogicData.getInstance(WMEditText.this.mTextView.getContext()).getEditTextWithKeyname(WMEditText.this.id + WMEditText.this.mLogicDelegate.getAPPToken());
            String text_src;
            String text_new;
            if (WMStringUtil.isEmptyString(edittext)) {
                text_src = WMEditText.this.mTextView.getText().toString();
                text_new = WMUIUtil.getDecoratorText(WMEditText.this.mTextView.getContext(), WMEditText.this.getText());
                WMEditText.this.beShow = text_new;
                if (!text_src.equals(WMEditText.this.processStringToShow(text_new, WMEditText.this.mTextView.getContext()))) {
                    super.decoratorText(WMEditText.this.mTextView);
                }
            } else {
                text_src = WMEditText.this.mTextView.getText().toString();
                text_new = edittext;
                WMEditText.this.beShow = edittext;
                text_new = WMEditText.this.processStringToShow(edittext, WMEditText.this.mTextView.getContext());
                if (!text_src.equals(text_new)) {
                    WMEditText.this.mTextView.setText(text_new);
                }
            }
            if (!this.mCancel) {
                WMEditText.this.mTextView.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMEditText(XmlPullParser parser) {
        super(parser);
        this.texttype = getStringByAttributeName(parser, "texttype", WMEditor.TYPETEXT);
        this.maxwidth = getIntByAttributeName(parser, "maxwidth", 0);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        if (this.maxwidth > 0) {
            this.mTextView.setMaxWidth(WMBaseUtil.dpToPixel((float) this.maxwidth, tv.getContext()));
        }
        tv.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
        if (this.mDecoratorEditTextRunnable != null) {
            this.mDecoratorEditTextRunnable.pause();
        }
        this.mDecoratorEditTextRunnable = new DecoratorEditTextRunnable();
        this.mDecoratorEditTextRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mWmEditor != null) {
            this.mWmEditor.pause();
        }
        if (this.mDecoratorEditTextRunnable != null) {
            this.mDecoratorEditTextRunnable.pause();
        }
    }

    public void showAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        if (animationDrawable == null) {
            this.mTextView.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
            animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        }
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        animationDrawable.start();
    }

    public void hideAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        if (animationDrawable != null) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            this.mTextView.setBackground(null);
        }
    }

    public void onWaterMarkClicked(float x, float y) {
        if (clickOnElement(x, y, this.mTextView)) {
            this.mWmEditor = new WMEditor(this.mTextView.getContext(), this.beShow, this.texttype, getCanShowWhenLocked(), new OnTextChangedListener() {
                public void onTextChanged(String text) {
                    if (!WMStringUtil.isEmptyString(text) && WMEditText.this.mTextView != null) {
                        String text_new = WMEditText.this.processStringToShow(text, WMEditText.this.mTextView.getContext());
                        if (!text_new.equals(WMEditText.this.mTextView.getText().toString()) && WMEditText.this.mTextView.getContext() != null) {
                            WMLogicData.getInstance(WMEditText.this.mTextView.getContext()).setEditTextWithKeyname(WMEditText.this.id + WMEditText.this.mLogicDelegate.getAPPToken(), text);
                            WMEditText.this.mTextView.setText(text_new);
                            WMEditText.this.resetWaterMarkLayoutParams();
                        }
                    }
                }

                public void onDialogDismissed() {
                }
            }, this.mLogicDelegate);
        }
    }
}
