package com.huawei.watermark.manager.parse;

import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.ui.WMLocationEditor;
import com.huawei.watermark.ui.WMLocationEditor.OnTextChangedListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.util.Collection;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class WMLocation extends WMText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMLocation.class.getSimpleName());
    private String beShow;
    private DecoratorLocationRunnable mDecoratorLocationRunnable;
    private TextView mTextView;
    private WMLocationEditor mWmEditor;

    private class DecoratorLocationRunnable implements Runnable {
        private boolean mCancel;
        private String mCurrentCity;
        private LocationUpdateCallback mlocationUpdateCallback;

        private DecoratorLocationRunnable() {
            this.mlocationUpdateCallback = new LocationUpdateCallback() {
                public void onAddressReport(List<Address> addresses) {
                    if (!WMCollectionUtil.isEmptyCollection((Collection) addresses)) {
                        DecoratorLocationRunnable.this.mCurrentCity = ((Address) addresses.get(0)).getLocality();
                    }
                }
            };
            WMLocation.this.mLogicDelegate.addLocationUpdateCallback(this.mlocationUpdateCallback);
        }

        public void run() {
            String location = WMLogicData.getInstance(WMLocation.this.mTextView.getContext()).getLocationTextWithKeyname(WMLocation.this.id + WMLocation.this.mLogicDelegate.getAPPToken());
            String text_src;
            String text_new;
            if (!WMStringUtil.isEmptyString(location)) {
                text_src = WMLocation.this.mTextView.getText().toString();
                text_new = location;
                WMLocation.this.beShow = location;
                text_new = WMLocation.this.processStringToShow(location, WMLocation.this.mTextView.getContext());
                if (!text_src.equals(text_new)) {
                    WMLocation.this.mTextView.setText(text_new);
                }
            } else if (WMStringUtil.isEmptyString(this.mCurrentCity)) {
                text_src = WMLocation.this.mTextView.getText().toString();
                text_new = WMUIUtil.getDecoratorText(WMLocation.this.mTextView.getContext(), WMLocation.this.getText());
                WMLocation.this.beShow = text_new;
                if (!text_src.equals(WMLocation.this.processStringToShow(text_new, WMLocation.this.mTextView.getContext()))) {
                    super.decoratorText(WMLocation.this.mTextView);
                }
            } else {
                text_src = WMLocation.this.mTextView.getText().toString();
                text_new = this.mCurrentCity;
                WMLocation.this.beShow = text_new;
                text_new = WMLocation.this.processStringToShow(text_new, WMLocation.this.mTextView.getContext());
                if (!text_src.equals(text_new)) {
                    WMLocation.this.mTextView.setText(text_new);
                    WMLocation.this.resetWaterMarkLayoutParams();
                }
            }
            if (!this.mCancel) {
                WMLocation.this.mTextView.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMLocation(XmlPullParser parser) {
        super(parser);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
        }
        this.mDecoratorLocationRunnable = new DecoratorLocationRunnable();
        this.mDecoratorLocationRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mWmEditor != null) {
            this.mWmEditor.pause();
        }
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
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
            this.mWmEditor = new WMLocationEditor(this.mTextView.getContext(), this.beShow, getCanShowWhenLocked(), new OnTextChangedListener() {
                public void onTextChanged(String text) {
                    if (!WMStringUtil.isEmptyString(text) && WMLocation.this.mTextView != null) {
                        String text_new = WMLocation.this.processStringToShow(text, WMLocation.this.mTextView.getContext());
                        if (!text_new.equals(WMLocation.this.mTextView.getText().toString())) {
                            WMLogicData.getInstance(WMLocation.this.mTextView.getContext()).setLocationTextWithKeyname(WMLocation.this.id + WMLocation.this.mLogicDelegate.getAPPToken(), text);
                            WMLocation.this.mTextView.setText(text_new);
                            WMLocation.this.resetWaterMarkLayoutParams();
                        }
                    }
                }
            }, this.mLogicDelegate);
        }
    }
}
