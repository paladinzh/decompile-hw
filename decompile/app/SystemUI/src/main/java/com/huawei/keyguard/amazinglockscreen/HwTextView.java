package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils.TruncateAt;
import android.view.animation.Animation;
import android.widget.TextView;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenView;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LayoutCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$TextCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;

public class HwTextView extends TextView implements HwUnlockInterface$HwLockScreenView, HwUnlockInterface$TextCallback, HwUnlockInterface$VisibilityCallback, HwUnlockInterface$LayoutCallback, HwUnlockInterface$ConditionCallback {
    private int dx;
    private int dy;
    private boolean mAnimationStart;
    private HwViewProperty mContent;
    private String mEllipsize;
    private HwViewProperty mH;
    private Handler mHandler;
    private ArrayList<HwAnimationSet> mHwAnimations;
    private PowerManager mPowerManager;
    private String mTextViewName;
    private HwViewProperty mVisiblity;
    private HwViewProperty mW;
    private HwViewProperty mX;
    private HwViewProperty mY;
    private int radius;
    private int shadow;

    public HwTextView(Context context) {
        super(context);
        this.mAnimationStart = false;
        this.mPowerManager = null;
        this.mEllipsize = BuildConfig.FLAVOR;
        this.dx = 0;
        this.dy = 2;
        this.radius = 2;
        this.shadow = 2130706432;
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        this.mHwAnimations = new ArrayList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (HwTextView.this.mAnimationStart) {
                            HwTextView.this.clearAnimation();
                            Animation anim = msg.obj;
                            if (anim != null && HwTextView.this.mPowerManager.isScreenOn()) {
                                HwTextView.this.startAnimation(anim);
                                return;
                            } else if (!HwTextView.this.mPowerManager.isScreenOn()) {
                                HwTextView.this.mAnimationStart = false;
                                return;
                            } else {
                                return;
                            }
                        }
                        return;
                    case 1:
                        HwTextView.this.clearAnimation();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void setBackground(String imagePath) {
        String path = "/data/skin/unlock/drawable-hdpi/" + imagePath;
        Bitmap bm = BitmapFactory.decodeFile(path);
        if (bm != null) {
            byte[] chunk = bm.getNinePatchChunk();
            if (NinePatch.isNinePatchChunk(chunk)) {
                setBackgroundDrawable(new NinePatchDrawable(getContext().getResources(), new NinePatch(bm, chunk, null)));
                return;
            }
            HwLog.d("TextView", "set background drawable");
            setBackgroundDrawable(Drawable.createFromPath(path));
        }
    }

    public void setContent(String strContent) {
        if (strContent.contains("system.")) {
            this.mContent = new HwViewProperty(getContext(), strContent, HwUnlockConstants$ViewPropertyType.TYPE_TEXT, this);
        } else {
            setTextContent(strContent);
        }
    }

    public void setTextContent(String text) {
        setText(text);
        setShadowLayer((float) this.radius, (float) this.dx, (float) this.dy, this.shadow);
        if ((this.mTextViewName == null || !HwUnlockUtils.getMusicTextType().equalsIgnoreCase("multi")) && text != null) {
            fixOutOfBounds(text);
        }
    }

    private void fixOutOfBounds(String text) {
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        if (paint.measureText(text) > ((float) ((Integer) this.mW.getValue()).intValue())) {
            setGravity(8388611);
            setSingleLine(true);
            setHorizontallyScrolling(true);
            if ("start".equals(this.mEllipsize)) {
                setEllipsize(TruncateAt.START);
            } else if ("middle".equals(this.mEllipsize)) {
                setEllipsize(TruncateAt.MIDDLE);
            } else if ("endSmall".equals(this.mEllipsize)) {
                setEllipsize(TruncateAt.valueOf("END_SMALL"));
            } else if ("marquee".equals(this.mEllipsize)) {
                setEllipsize(TruncateAt.MARQUEE);
                setMarqueeRepeatLimit(-1);
                setTextIsSelectable(true);
                setSelected(true);
            } else {
                setEllipsize(TruncateAt.END);
            }
        }
    }

    public void setEllipsizeType(String ellipsize) {
        this.mEllipsize = ellipsize;
    }

    public boolean isFocused() {
        return true;
    }

    public void setLayout(String x, String y, String w, String h) {
        this.mX = new HwViewProperty(getContext(), x, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mY = new HwViewProperty(getContext(), y, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mW = new HwViewProperty(getContext(), w, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mH = new HwViewProperty(getContext(), h, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
    }

    public void layout() {
        int l = ((Integer) this.mX.getValue()).intValue();
        int t = ((Integer) this.mY.getValue()).intValue();
        layout(l, t, l + ((Integer) this.mW.getValue()).intValue(), t + ((Integer) this.mH.getValue()).intValue());
    }

    public void onLayoutChanged() {
        layout();
    }

    public void setVisiblityProp(String visible) {
        HwLog.d("TextView", "visible is " + visible);
        this.mVisiblity = new HwViewProperty(getContext(), visible, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
        try {
            HwLog.d("TextView", "mVisiblity.getValue is " + this.mVisiblity.getValue());
            refreshVisibility(((Boolean) this.mVisiblity.getValue()).booleanValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshVisibility(boolean result) {
        setVisibility(result ? 0 : 4);
    }

    public void refreshCondition(String strValue, boolean value) {
        if (value && !this.mAnimationStart) {
            startAnimation();
        } else if (!value) {
            stopAnimation();
        }
    }

    public void setTextViewName(String name) {
        this.mTextViewName = name;
    }

    public String getTextViewName() {
        return this.mTextViewName;
    }

    private void startAnimation() {
        for (int i = 0; i < this.mHwAnimations.size(); i++) {
            HwAnimationSet anim = (HwAnimationSet) this.mHwAnimations.get(i);
            if (this.mHandler.hasMessages(0)) {
                this.mHandler.removeMessages(0);
            }
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
            this.mAnimationStart = true;
            Message msg = Message.obtain();
            msg.obj = anim;
            msg.what = 0;
            this.mHandler.sendMessage(msg);
        }
    }

    private void stopAnimation() {
        if (this.mHandler.hasMessages(0)) {
            this.mHandler.removeMessages(0);
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mAnimationStart = false;
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        clearAnimation();
        if (this.mHwAnimations != null) {
            for (int i = 0; i < this.mHwAnimations.size(); i++) {
                ((HwAnimationSet) this.mHwAnimations.get(i)).cancel();
            }
            this.mHwAnimations.clear();
        }
    }
}
