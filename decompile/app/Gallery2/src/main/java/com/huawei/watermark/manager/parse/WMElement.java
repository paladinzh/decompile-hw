package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.huawei.watermark.manager.parse.util.WMAltitudeService.AltitudeUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMHealthyReportService.HealthUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMWeatherService.WeatherUpdateCallback;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMFileUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public abstract class WMElement {
    public static final int CAMERASIZE16B9 = 2;
    public static final int CAMERASIZE1B1 = 0;
    public static final int CAMERASIZE4B3 = 1;
    public static final float CAMERASIZEVALUE16B9 = 1.7777778f;
    public static final float CAMERASIZEVALUE1B1 = 1.0f;
    public static final float CAMERASIZEVALUE4B3 = 1.3333334f;
    private static final String TAG = ("CAMERA3WATERMARK_" + WMElement.class.getSimpleName());
    protected String above;
    private String backgroundcolor;
    private String backgroundpic;
    protected String below;
    protected String gravity;
    protected int h;
    protected String id;
    protected String layout_gravity;
    protected String left;
    protected int mCameraSizeType;
    protected LogicDelegate mLogicDelegate;
    protected int mOri;
    protected int marginBottom;
    protected int marginLeft;
    protected int marginRight;
    protected int marginTop;
    protected int paddingBottom;
    protected int paddingLeft;
    protected int paddingRight;
    protected int paddingTop;
    protected String right;
    protected int w;

    public interface LogicDelegate {
        void addAltitudeUpdateCallback(AltitudeUpdateCallback altitudeUpdateCallback);

        void addHealthUpdateCallback(HealthUpdateCallback healthUpdateCallback);

        void addLocationUpdateCallback(LocationUpdateCallback locationUpdateCallback);

        void addWeatherUpdateCallback(WeatherUpdateCallback weatherUpdateCallback);

        String getAPPToken();

        boolean getShouldHideSoftKeyboard();

        void setFullScreenViewShowStatus(boolean z);
    }

    public abstract View toView(Context context, WaterMark waterMark, String str, int i);

    public WMElement(XmlPullParser parser) {
        this.id = getStringByAttributeName(parser, "id");
        this.layout_gravity = getStringByAttributeName(parser, "layout_gravity");
        this.gravity = getStringByAttributeName(parser, "gravity");
        this.w = getIntByAttributeName(parser, "w", -2);
        this.h = getIntByAttributeName(parser, "h", -2);
        this.below = getStringByAttributeName(parser, "below");
        this.left = getStringByAttributeName(parser, "left");
        this.right = getStringByAttributeName(parser, "right");
        this.above = getStringByAttributeName(parser, "above");
        this.paddingTop = getIntByAttributeName(parser, "paddingTop", 0);
        this.paddingBottom = getIntByAttributeName(parser, "paddingBottom", 0);
        this.paddingLeft = getIntByAttributeName(parser, "paddingLeft", 0);
        this.paddingRight = getIntByAttributeName(parser, "paddingRight", 0);
        this.marginTop = getIntByAttributeName(parser, "marginTop", 0);
        this.marginBottom = getIntByAttributeName(parser, "marginBottom", 0);
        this.marginLeft = getIntByAttributeName(parser, "marginLeft", 0);
        this.marginRight = getIntByAttributeName(parser, "marginRight", 0);
        this.backgroundcolor = getStringByAttributeName(parser, "backgroundcolor", "#00000000");
        this.backgroundpic = getStringByAttributeName(parser, "backgroundpic");
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        this.mLogicDelegate = delegate;
    }

    public void setDisplayRectSizeType(int w, int h) {
        this.mCameraSizeType = consCameraSizeType(w, h);
    }

    public String getStringByAttributeName(XmlPullParser parser, String attName, String defaultValue) {
        String value = parser.getAttributeValue(null, attName);
        return WMStringUtil.isEmptyString(value) ? defaultValue : value;
    }

    public String getStringByAttributeName(XmlPullParser parser, String attName) {
        return getStringByAttributeName(parser, attName, "");
    }

    public int getIntByAttributeName(XmlPullParser parser, String attName, int defaultValue) {
        try {
            defaultValue = Integer.parseInt(getStringByAttributeName(parser, attName, ""));
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    public int getIntByAttributeName(XmlPullParser parser, String attName) {
        return getIntByAttributeName(parser, attName, 0);
    }

    public float getFloatByAttributeName(XmlPullParser parser, String attName, float defaultValue) {
        try {
            defaultValue = Float.parseFloat(getStringByAttributeName(parser, attName, ""));
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    public float getFloatByAttributeName(XmlPullParser parser, String attName) {
        return getFloatByAttributeName(parser, attName, 0.0f);
    }

    public boolean getBooleanByAttributeName(XmlPullParser parser, String attName, boolean defaultValue) {
        return Boolean.valueOf(getStringByAttributeName(parser, attName, "" + defaultValue)).booleanValue();
    }

    public boolean getBooleanByAttributeName(XmlPullParser parser, String attName) {
        return getBooleanByAttributeName(parser, attName, true);
    }

    public LayoutParams generateLp(Context context, WaterMark wm, String parentLayoutMode) {
        if (WMStaticFinalParameter.ISLINEARLAYOUT.equals(parentLayoutMode)) {
            return generateLinearLayoutLp(context, wm);
        }
        if (WMStaticFinalParameter.ISRELATIVELAYOUT.equals(parentLayoutMode)) {
            return generateRelativeLayoutLp(context, wm);
        }
        return generateRelativeLayoutLp(context, wm);
    }

    protected void setPadding(Context context, WaterMark wm, View rootLayout) {
        float scale = wm.getScale();
        rootLayout.setPadding(WMBaseUtil.dpToPixel(((float) this.paddingLeft) * scale, context), WMBaseUtil.dpToPixel(((float) this.paddingTop) * scale, context), WMBaseUtil.dpToPixel(((float) this.paddingRight) * scale, context), WMBaseUtil.dpToPixel(((float) this.paddingBottom) * scale, context));
    }

    public int getGravity() {
        if (WMStringUtil.isEmptyString(this.gravity)) {
            return 0;
        }
        int _gravity = 0;
        List<String> args = WMStringUtil.split(this.gravity, "|");
        if (args.contains("left")) {
            _gravity = 3;
        }
        if (args.contains("right")) {
            _gravity |= 5;
        }
        if (args.contains("top")) {
            _gravity |= 48;
        }
        if (args.contains("bottom")) {
            _gravity |= 80;
        }
        if (args.contains("center")) {
            _gravity |= 17;
        }
        if (args.contains("center_horizontal")) {
            _gravity |= 1;
        }
        if (args.contains("center_vertical")) {
            _gravity |= 16;
        }
        return _gravity;
    }

    public void resume() {
    }

    public void pause() {
    }

    public void showAnimationTips() {
    }

    public void hideAnimationTips() {
    }

    public View getView() {
        return null;
    }

    public void onWaterMarkClicked(float x, float y) {
    }

    public boolean clickOnElement(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int[] locationParent = WMBaseUtil.getWaterMarkAbsolutePosition(view);
        if (locationParent[2] == 0 && locationParent[3] == 0) {
            return false;
        }
        int width = view.getWidth();
        int height = view.getHeight();
        int viewx = 0;
        int viewy = 0;
        switch (this.mOri) {
            case 0:
                viewx = location[0] - locationParent[0];
                viewy = location[1] - locationParent[1];
                break;
            case WMComponent.ORI_90 /*90*/:
                viewx = locationParent[1] - location[1];
                viewy = location[0] - locationParent[0];
                break;
            case 180:
                viewx = locationParent[0] - location[0];
                viewy = locationParent[1] - location[1];
                break;
            case 270:
                viewx = location[1] - locationParent[1];
                viewy = locationParent[0] - location[0];
                break;
        }
        return WMBaseUtil.isInside(viewx, viewy, width, height, (int) x, (int) y);
    }

    private LayoutParams generateLinearLayoutLp(Context context, WaterMark wm) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        if (!WMStringUtil.isEmptyString(this.layout_gravity)) {
            List<String> args = WMStringUtil.split(this.layout_gravity, "|");
            lp.gravity = 0;
            if (args.contains("left")) {
                lp.gravity |= 3;
            }
            if (args.contains("right")) {
                lp.gravity |= 5;
            }
            if (args.contains("top")) {
                lp.gravity |= 48;
            }
            if (args.contains("bottom")) {
                lp.gravity |= 80;
            }
            if (args.contains("center")) {
                lp.gravity |= 17;
            }
            if (args.contains("center_horizontal")) {
                lp.gravity |= 1;
            }
            if (args.contains("center_vertical")) {
                lp.gravity |= 16;
            }
        }
        lp.width = this.w > 0 ? WMBaseUtil.dpToPixel((float) this.w, context) : this.w;
        lp.height = this.h > 0 ? WMBaseUtil.dpToPixel((float) this.h, context) : this.h;
        float scale = wm.getScale();
        if (lp.width > 0) {
            lp.width = Math.round(((float) lp.width) * scale);
        }
        if (lp.height > 0) {
            lp.height = Math.round(((float) lp.height) * scale);
        }
        lp.setMargins(WMBaseUtil.dpToPixel(((float) this.marginLeft) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginTop) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginRight) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginBottom) * scale, context));
        return lp;
    }

    private LayoutParams generateRelativeLayoutLp(Context context, WaterMark wm) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        if (!WMStringUtil.isEmptyString(this.layout_gravity)) {
            List<String> args = WMStringUtil.split(this.layout_gravity, "|");
            if (args.contains("left")) {
                lp.addRule(9);
            }
            if (args.contains("right")) {
                lp.addRule(11);
            }
            if (args.contains("top")) {
                lp.addRule(10);
            }
            if (args.contains("bottom")) {
                lp.addRule(12);
            }
            if (args.contains("center")) {
                lp.addRule(13);
            }
            if (args.contains("center_horizontal")) {
                lp.addRule(14);
            }
            if (args.contains("center_vertical")) {
                lp.addRule(15);
            }
        }
        if (!WMStringUtil.isEmptyString(this.below)) {
            lp.addRule(3, wm.generateId(this.below));
        }
        if (!WMStringUtil.isEmptyString(this.left)) {
            lp.addRule(0, wm.generateId(this.left));
        }
        if (!WMStringUtil.isEmptyString(this.right)) {
            lp.addRule(1, wm.generateId(this.right));
        }
        if (!WMStringUtil.isEmptyString(this.above)) {
            lp.addRule(2, wm.generateId(this.above));
        }
        lp.width = this.w > 0 ? WMBaseUtil.dpToPixel((float) this.w, context) : this.w;
        lp.height = this.h > 0 ? WMBaseUtil.dpToPixel((float) this.h, context) : this.h;
        float scale = wm.getScale();
        if (lp.width > 0) {
            lp.width = Math.round(((float) lp.width) * scale);
        }
        if (lp.height > 0) {
            lp.height = Math.round(((float) lp.height) * scale);
        }
        lp.setMargins(WMBaseUtil.dpToPixel(((float) this.marginLeft) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginTop) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginRight) * scale, context), WMBaseUtil.dpToPixel(((float) this.marginBottom) * scale, context));
        return lp;
    }

    public String getBackgroundcolor() {
        return this.backgroundcolor;
    }

    public String getBackgroundpic() {
        return this.backgroundpic;
    }

    public void showBackground(Context context, View view, String wmPath, WMElement element) {
        if (!WMStringUtil.isEmptyString(element.getBackgroundcolor())) {
            view.setBackgroundColor(Color.parseColor(element.getBackgroundcolor()));
        }
        if (!WMStringUtil.isEmptyString(element.getBackgroundpic())) {
            showBackgroundPic(context, view, wmPath, element.getBackgroundpic());
        }
    }

    public void showBackground(Context context, View view, String wmPath) {
        if (!WMStringUtil.isEmptyString(getBackgroundcolor())) {
            view.setBackgroundColor(Color.parseColor(getBackgroundcolor()));
        }
        if (!WMStringUtil.isEmptyString(getBackgroundpic())) {
            showBackgroundPic(context, view, wmPath, getBackgroundpic());
        }
    }

    public void showBackgroundPic(Context context, View view, String wmPath, String backgroundpic) {
        int resId = WMResourceUtil.getDrawableId(context, backgroundpic);
        if (resId > 0) {
            try {
                view.setBackgroundResource(resId);
                return;
            } catch (NotFoundException e) {
                view.setBackground(new BitmapDrawable(WMFileUtil.decodeBitmap(context, wmPath, backgroundpic)));
                return;
            }
        }
        view.setBackground(new BitmapDrawable(WMFileUtil.decodeBitmap(context, wmPath, backgroundpic)));
    }

    public void onOrientationChanged(int ori) {
        this.mOri = ori;
    }

    protected int consCameraSizeType(int w, int h) {
        int h_real;
        int w_real = w < h ? w : h;
        if (h > w) {
            h_real = h;
        } else {
            h_real = w;
        }
        float temppreviewsize = ((float) h_real) / ((float) w_real);
        if (Math.abs(temppreviewsize - CAMERASIZEVALUE1B1) < 0.05f) {
            return 0;
        }
        if (Math.abs(temppreviewsize - CAMERASIZEVALUE4B3) < 0.05f) {
            return 1;
        }
        if (Math.abs(temppreviewsize - CAMERASIZEVALUE16B9) < 0.05f) {
            return 2;
        }
        return 1;
    }
}
