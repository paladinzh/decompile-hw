package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ViewPropertyCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import fyusion.vislib.BuildConfig;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HwAnimationSet extends AnimationSet {
    private HwUnlockInterface$ViewPropertyCallback mCallback;
    private HwViewProperty mCondition = null;
    private Context mContext;
    public long mDelay = 0;
    public long mInterval = -1;
    private String mName = BuildConfig.FLAVOR;

    public HwAnimationSet(Context context, NamedNodeMap attrs, HwUnlockInterface$ViewPropertyCallback callback) {
        super(false);
        this.mContext = context;
        this.mCallback = callback;
        parseAttrs(attrs);
        setFillAfter(true);
    }

    private void parseAttrs(NamedNodeMap attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if ("name".equalsIgnoreCase(name)) {
                this.mName = value;
            } else if ("interval".equalsIgnoreCase(name)) {
                this.mInterval = Long.parseLong(value);
            } else if ("condition".equalsIgnoreCase(name)) {
                this.mCondition = new HwViewProperty(this.mContext, "system.press", HwUnlockConstants$ViewPropertyType.TYPE_CONDITION, this.mCallback);
            } else if ("delay".equalsIgnoreCase(name)) {
                this.mDelay = Long.parseLong(value);
            }
        }
    }

    private String getAttributeValue(NamedNodeMap attrs, String attrName) {
        String result = BuildConfig.FLAVOR;
        Node node = attrs.getNamedItem(attrName);
        if (node != null) {
            return node.getNodeValue();
        }
        return result;
    }

    private Float getAttributeFloatValue(NamedNodeMap attrs, String attrName, float defaultValue) {
        Float result = Float.valueOf(0.0f);
        Node node = attrs.getNamedItem(attrName);
        if (node == null) {
            return result;
        }
        try {
            return Float.valueOf(Float.parseFloat(node.getNodeValue()));
        } catch (NumberFormatException e) {
            result = Float.valueOf(defaultValue);
            e.printStackTrace();
            return result;
        }
    }

    private int getAttributeIntValue(NamedNodeMap attrs, String attrName, int defaultValue) {
        Node node = attrs.getNamedItem(attrName);
        if (node == null) {
            return 0;
        }
        try {
            return Integer.parseInt(node.getNodeValue());
        } catch (NumberFormatException e) {
            int result = defaultValue;
            e.printStackTrace();
            return result;
        }
    }

    public void addAnimation(NamedNodeMap attrs) {
        Animation animation = getAnimation(attrs);
        if (animation != null) {
            String interpolator = getAttributeValue(attrs, "interpolator");
            if ("accelerate".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new AccelerateInterpolator());
            } else if ("linear".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new LinearInterpolator());
            } else if ("overshoot".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new OvershootInterpolator());
            } else if ("anticipate".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new AnticipateInterpolator());
            } else if ("accelerateDecelerate".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
            } else if ("anticipateOvershoot".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new AnticipateOvershootInterpolator());
            } else if ("bounce".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new BounceInterpolator());
            } else if ("cycle".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new CycleInterpolator(1.0f));
            } else if ("decelerate".equalsIgnoreCase(interpolator)) {
                animation.setInterpolator(new DecelerateInterpolator());
            }
            animation.setDuration((long) getAttributeIntValue(attrs, "duration", 0));
            if ("reverse".equalsIgnoreCase(getAttributeValue(attrs, "repeatMode"))) {
                animation.setRepeatCount(1);
                animation.setRepeatMode(2);
            }
            animation.setFillAfter(true);
            addAnimation(animation);
        }
    }

    public Animation getAnimation(NamedNodeMap attrs) {
        String type = getAttributeValue(attrs, "type");
        float scale = AmazingUtils.getScalePara();
        if ("alpha".equalsIgnoreCase(type)) {
            return new AlphaAnimation(getAttributeFloatValue(attrs, "fromAlpha", 1.0f).floatValue(), getAttributeFloatValue(attrs, "toAlpha", 1.0f).floatValue());
        }
        float fromX;
        float toX;
        float fromY;
        float toY;
        if ("translate".equalsIgnoreCase(type)) {
            fromX = getAttributeFloatValue(attrs, "fromX", 0.0f).floatValue();
            toX = getAttributeFloatValue(attrs, "toX", 0.0f).floatValue();
            fromY = getAttributeFloatValue(attrs, "fromY", 0.0f).floatValue();
            toY = getAttributeFloatValue(attrs, "toY", 0.0f).floatValue();
            if (scale != 1.0f) {
                fromX *= scale;
                toX *= scale;
                fromY *= scale;
                toY *= scale;
            }
            return new TranslateAnimation(fromX, toX, fromY, toY);
        } else if ("scale".equalsIgnoreCase(type)) {
            fromX = getAttributeFloatValue(attrs, "fromX", 0.0f).floatValue();
            toX = getAttributeFloatValue(attrs, "toX", 0.0f).floatValue();
            fromY = getAttributeFloatValue(attrs, "fromY", 0.0f).floatValue();
            toY = getAttributeFloatValue(attrs, "toY", 0.0f).floatValue();
            pivotX = getAttributeFloatValue(attrs, "pivotX", 0.0f).floatValue();
            pivotY = getAttributeFloatValue(attrs, "pivotY", 0.0f).floatValue();
            if (scale != 1.0f) {
                pivotX *= scale;
                pivotY *= scale;
            }
            return new ScaleAnimation(fromX, toX, fromY, toY, pivotX, pivotY);
        } else if (!"rotate".equalsIgnoreCase(type)) {
            return null;
        } else {
            float fromDegrees = getAttributeFloatValue(attrs, "fromDegrees", 0.0f).floatValue();
            float toDegrees = getAttributeFloatValue(attrs, "toDegrees", 0.0f).floatValue();
            pivotX = getAttributeFloatValue(attrs, "pivotX", 0.0f).floatValue();
            pivotY = getAttributeFloatValue(attrs, "pivotY", 0.0f).floatValue();
            if (scale != 1.0f) {
                pivotX *= scale;
                pivotY *= scale;
            }
            return new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        }
    }
}
