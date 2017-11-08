package com.huawei.watermark.manager.parse;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.ui.WMImageView;
import com.huawei.watermark.ui.baseview.WMRotateRelativeLayout;
import com.huawei.watermark.wmdata.wminterface.WaterMarkProperty;
import com.huawei.watermark.wmdata.wmlogicdata.WMShowRectData;
import com.huawei.watermark.wmdata.wmlogicdata.WMShowRectData.ViewSizeObject;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;

public class WaterMark extends WMElement implements WaterMarkProperty {
    private ArrayList<WMElement> elements = new ArrayList();
    public boolean isAbsolutePosition = false;
    private boolean mBeConvertToBitmap;
    private boolean mCanShowWhenLocked;
    public WMComponent mComponent;
    private Map<String, Integer> mIds = new HashMap();
    public boolean mNeedClearLayoutParams = false;
    private float mScaleSize;
    private RelativeLayout mView = null;
    private WMRotateRelativeLayout mWMRotateLayout = null;
    private String path;
    private String version;
    private WMConfig wmConfig;

    public WaterMark(XmlPullParser parser) {
        super(parser);
        this.version = getStringByAttributeName(parser, "version");
    }

    public void setDisplayRectSizeType(int w, int h) {
        super.setDisplayRectSizeType(w, h);
        this.wmConfig.setDisplayRectSizeType(w, h);
    }

    public void setCanShowWhenLocked(boolean show) {
        this.mCanShowWhenLocked = show;
    }

    public boolean getCanShowWhenLocked() {
        return this.mCanShowWhenLocked;
    }

    public void onOrientationChanged(int ori) {
        this.mOri = ori;
    }

    public void onScaleSizeChange(Context context, float imageWidth, float imageHeight) {
        this.mScaleSize = Math.min(imageWidth / ((float) this.wmConfig.getTipWidth(context)), imageHeight / ((float) this.wmConfig.getTipHeight(context)));
        this.mScaleSize = Math.min(this.mScaleSize, WMElement.CAMERASIZEVALUE1B1);
    }

    public float getScale() {
        return this.mScaleSize == 0.0f ? WMElement.CAMERASIZEVALUE1B1 : this.mScaleSize;
    }

    public View getWaterMarkRoot() {
        return this.mWMRotateLayout;
    }

    private void clearLayoutParamsWhenRelativeToAbosolute() {
        String wmlocation = WMShowRectData.getInstance(this.mComponent.getContext()).getWMMovePositionData(getLocationKey() + this.mComponent.getToken(), "");
        if (!this.isAbsolutePosition && !WMStringUtil.isEmptyString(wmlocation)) {
            this.mNeedClearLayoutParams = true;
            clearLayoutParams();
        }
    }

    private synchronized View toView(Context context) {
        if (this.mWMRotateLayout == null || this.mView == null) {
            if (context == null) {
                return null;
            }
            this.mWMRotateLayout = (WMRotateRelativeLayout) LayoutInflater.from(context).inflate(WMResourceUtil.getLayoutId(context, "wm_jar_rotatelayout"), null);
            this.mWMRotateLayout.setContentDescription(context.getResources().getString(R.string.accessubility_watermark_tapwatermarkprompt));
            this.mView = (RelativeLayout) this.mWMRotateLayout.findViewById(WMResourceUtil.getId(context, "wm_base_relativelayout"));
        }
        this.mWMRotateLayout.setOrientation(this.mOri, false);
        return toView(context, this, WMStaticFinalParameter.ISRELATIVELAYOUT, this.mOri);
    }

    public synchronized View getViewForCurrentWaterMarkHolder() {
        return toView(null);
    }

    public synchronized View getViewForShowOnViewpager(Context context) {
        if (!(this.mWMRotateLayout == null || this.mView == null || context == null)) {
            clearLayoutParamsWhenRelativeToAbosolute();
        }
        return toView(context);
    }

    public boolean isResourceReady() {
        if (WMCollectionUtil.isEmptyCollection(this.elements)) {
            return false;
        }
        for (WMElement element : this.elements) {
            if ((element instanceof WMImage) && !((WMImageView) element.getView()).isTackFinished()) {
                return false;
            }
        }
        return true;
    }

    public synchronized View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        if (this.mView.getChildCount() > 0) {
            for (WMElement element : this.elements) {
                element.onOrientationChanged(ori);
            }
            return this.mWMRotateLayout;
        } else if (context == null) {
            return null;
        } else {
            showBackground(context, this.mView, getPath(), this.wmConfig);
            for (WMElement element2 : this.elements) {
                View view = element2.toView(context, wm, parentLayoutMode, ori);
                if (view != null) {
                    this.mView.addView(view);
                }
            }
            return this.mWMRotateLayout;
        }
    }

    public void addElement(WMElement element) {
        if (element instanceof WMConfig) {
            this.wmConfig = (WMConfig) element;
        } else {
            this.elements.add(element);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBeConvertToBitmap(boolean value) {
        this.mBeConvertToBitmap = value;
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        this.mComponent = (WMComponent) ((Activity) context).findViewById(WMResourceUtil.getId(context, "wm_component"));
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.initBaseLogicData(context, delegate);
            }
        }
    }

    private void clearLayoutParams() {
        if (this.mNeedClearLayoutParams) {
            this.mNeedClearLayoutParams = false;
            if (this.mWMRotateLayout != null && this.mView != null && this.mView.getChildCount() > 0) {
                int i;
                Vector<View> childview = new Vector();
                OnTouchListener listener = this.mWMRotateLayout.getOnTouchListener();
                if (this.mView.getChildCount() > 0) {
                    for (i = 0; i < this.mView.getChildCount(); i++) {
                        childview.add(this.mView.getChildAt(i));
                    }
                }
                destoryView();
                this.mWMRotateLayout = (WMRotateRelativeLayout) LayoutInflater.from(this.mComponent.getContext()).inflate(WMResourceUtil.getLayoutId(this.mComponent.getContext(), "wm_jar_rotatelayout"), null);
                this.mWMRotateLayout.setContentDescription(this.mComponent.getContext().getResources().getString(R.string.accessubility_watermark_tapwatermarkprompt));
                this.mWMRotateLayout.setOnTouchListener(listener);
                this.mWMRotateLayout.setOrientation(this.mOri, false);
                this.mView = (RelativeLayout) this.mWMRotateLayout.findViewById(WMResourceUtil.getId(this.mComponent.getContext(), "wm_base_relativelayout"));
                for (i = 0; i < childview.size(); i++) {
                    this.mView.addView((View) childview.elementAt(i));
                }
                showBackground(this.mComponent.getContext(), this.mView, getPath(), this.wmConfig);
            }
        }
    }

    public void setAbosoluteWMLayoutPrarms(LayoutParams rl, int x, int y) {
        if (this.mWMRotateLayout != null) {
            final ViewGroup parent = (ViewGroup) this.mWMRotateLayout.getParent();
            if (parent != null) {
                parent.removeView(this.mWMRotateLayout);
                clearLayoutParams();
            }
            this.mWMRotateLayout.setRotateLayoutParams(rl);
            this.mWMRotateLayout.setX((float) x);
            this.mWMRotateLayout.setY((float) y);
            if (parent != null) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    parent.addView(this.mWMRotateLayout);
                } else {
                    ((Activity) this.mComponent.getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            if (parent != null) {
                                parent.addView(WaterMark.this.mWMRotateLayout);
                            }
                        }
                    });
                }
            }
        }
    }

    public void setWMLayoutParams() {
        if (this.mComponent != null && this.mComponent.getContext() != null && this.mWMRotateLayout != null) {
            String wmlocation = WMShowRectData.getInstance(this.mComponent.getContext()).getWMMovePositionData(getLocationKey() + this.mComponent.getToken(), "");
            ViewSizeObject sizeObject = WMShowRectData.getInstance(this.mComponent.getContext()).getWMViewSizeData(getLocationKey());
            if (WMStringUtil.isEmptyString(wmlocation) || sizeObject == null) {
                this.isAbsolutePosition = false;
                this.mWMRotateLayout.setRotateLayoutParams(getLayoutParams(this.mComponent.getContext()));
            } else {
                boolean z;
                this.isAbsolutePosition = true;
                float newScale = getScale();
                if (Math.abs(newScale - sizeObject.scale) > 1.0E-7f) {
                    sizeObject.w = (sizeObject.w * newScale) / sizeObject.scale;
                    sizeObject.h = (sizeObject.h * newScale) / sizeObject.scale;
                    sizeObject.scale = newScale;
                }
                float f = sizeObject.w;
                float f2 = sizeObject.h;
                if (this.mOri == 90 || this.mOri == 270) {
                    z = true;
                } else {
                    z = false;
                }
                float[] wh = WMUIUtil.getWH(f, f2, z);
                int[] xy = WMUIUtil.rebasePosition(Float.parseFloat(wmlocation.split("\\|")[0]), Float.parseFloat(wmlocation.split("\\|")[1]), wh[0], wh[1], WMShowRectData.getInstance(this.mComponent.getContext()).getWMViewpagerWidth(), WMShowRectData.getInstance(this.mComponent.getContext()).getWMViewpagerHeight());
                int x = xy[0];
                int y = xy[1];
                WMShowRectData.getInstance(this.mComponent.getContext()).setWMMovePositionData(getLocationKey() + this.mComponent.getToken(), x + "|" + y);
                setAbosoluteWMLayoutPrarms((LayoutParams) getLayoutParamsWithoutPositionParams(this.mComponent.getContext()), x, y);
            }
        }
    }

    public ViewGroup.LayoutParams getLayoutParams(Context context) {
        LayoutParams lp = new LayoutParams(-2, -2);
        switch (this.mOri) {
            case 0:
                setLayoutParamsOri0(context, lp);
                break;
            case WMComponent.ORI_90 /*90*/:
                setLayoutParamsOri90(context, lp);
                break;
            case 180:
                setLayoutParamsOri180(context, lp);
                break;
            case 270:
                setLayoutParamsOri270(context, lp);
                break;
        }
        setLayoutParamsByOri(context, lp);
        if (lp.bottomMargin != 0) {
            lp.bottomMargin = Math.round(((float) lp.bottomMargin) * this.mScaleSize);
        }
        if (lp.rightMargin != 0) {
            lp.rightMargin = Math.round(((float) lp.rightMargin) * this.mScaleSize);
        }
        if (lp.leftMargin != 0) {
            lp.leftMargin = Math.round(((float) lp.leftMargin) * this.mScaleSize);
        }
        if (lp.topMargin != 0) {
            lp.topMargin = Math.round(((float) lp.topMargin) * this.mScaleSize);
        }
        return lp;
    }

    private void setLayoutParamsByOri(Context context, LayoutParams lp) {
        if (this.mOri == 0 || this.mOri == 180) {
            lp.width = this.wmConfig.getViewWidth() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewWidth(), context) : this.wmConfig.getViewWidth();
            lp.height = this.wmConfig.getViewHeight() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewHeight(), context) : this.wmConfig.getViewHeight();
        } else if (this.mOri == 90 || this.mOri == 270) {
            lp.height = this.wmConfig.getViewWidth() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewWidth(), context) : this.wmConfig.getViewWidth();
            lp.width = this.wmConfig.getViewHeight() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewHeight(), context) : this.wmConfig.getViewHeight();
        }
        if (lp.width > 0) {
            lp.width = Math.round(((float) lp.width) * this.mScaleSize);
        }
        if (lp.height > 0) {
            lp.height = Math.round(((float) lp.height) * this.mScaleSize);
        }
    }

    private void setLayoutParamsOri270(Context context, LayoutParams lp) {
        String layout_gravity = this.wmConfig.getLandLayoutGravity();
        if (!WMStringUtil.isEmptyString(layout_gravity)) {
            List<String> args = WMStringUtil.split(layout_gravity, "|");
            if (args.contains("left")) {
                lp.addRule(10);
                lp.topMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginLeft(this.mCameraSizeType), context);
            }
            if (args.contains("right")) {
                lp.addRule(12);
                lp.bottomMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginRgiht(this.mCameraSizeType), context);
            }
            if (args.contains("top")) {
                lp.addRule(11);
                lp.rightMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginTop(this.mCameraSizeType), context);
            }
            if (args.contains("bottom")) {
                lp.addRule(9);
                lp.leftMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginBottom(this.mCameraSizeType), context);
            }
            if (args.contains("center")) {
                lp.addRule(13);
            }
            if (args.contains("center_horizontal")) {
                lp.addRule(15);
            }
            if (args.contains("center_vertical")) {
                lp.addRule(14);
            }
        }
    }

    private void setLayoutParamsOri180(Context context, LayoutParams lp) {
        String layout_gravity = this.wmConfig.getPortLayoutGravity();
        if (!WMStringUtil.isEmptyString(layout_gravity)) {
            List<String> args = WMStringUtil.split(layout_gravity, "|");
            if (args.contains("left")) {
                lp.addRule(11);
                lp.rightMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginLeft(this.mCameraSizeType), context);
            }
            if (args.contains("right")) {
                lp.addRule(9);
                lp.leftMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginRgiht(this.mCameraSizeType), context);
            }
            if (args.contains("top")) {
                lp.addRule(12);
                lp.bottomMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginTop(this.mCameraSizeType), context);
            }
            if (args.contains("bottom")) {
                lp.addRule(10);
                lp.topMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginBottom(this.mCameraSizeType), context);
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
    }

    private void setLayoutParamsOri90(Context context, LayoutParams lp) {
        String layout_gravity = this.wmConfig.getPortLayoutGravity();
        if (!WMStringUtil.isEmptyString(layout_gravity)) {
            List<String> args = WMStringUtil.split(layout_gravity, "|");
            if (args.contains("left")) {
                lp.addRule(12);
                lp.bottomMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginLeft(this.mCameraSizeType), context);
            }
            if (args.contains("right")) {
                lp.addRule(10);
                lp.topMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginRgiht(this.mCameraSizeType), context);
            }
            if (args.contains("top")) {
                lp.addRule(9);
                lp.leftMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginTop(this.mCameraSizeType), context);
            }
            if (args.contains("bottom")) {
                lp.addRule(11);
                lp.rightMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getLandMarginBottom(this.mCameraSizeType), context);
            }
            if (args.contains("center")) {
                lp.addRule(13);
            }
            if (args.contains("center_horizontal")) {
                lp.addRule(15);
            }
            if (args.contains("center_vertical")) {
                lp.addRule(14);
            }
        }
    }

    private void setLayoutParamsOri0(Context context, LayoutParams lp) {
        String layout_gravity = this.wmConfig.getPortLayoutGravity();
        if (!WMStringUtil.isEmptyString(layout_gravity)) {
            List<String> args = WMStringUtil.split(layout_gravity, "|");
            if (args.contains("left")) {
                lp.addRule(9);
                lp.leftMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginLeft(this.mCameraSizeType), context);
            }
            if (args.contains("right")) {
                lp.addRule(11);
                lp.rightMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginRgiht(this.mCameraSizeType), context);
            }
            if (args.contains("top")) {
                lp.addRule(10);
                lp.topMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginTop(this.mCameraSizeType), context);
            }
            if (args.contains("bottom")) {
                lp.addRule(12);
                lp.bottomMargin = WMBaseUtil.dpToPixel((float) this.wmConfig.getPortMarginBottom(this.mCameraSizeType), context);
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
    }

    public ViewGroup.LayoutParams getLayoutParamsWithoutPositionParams(Context context) {
        LayoutParams lp = new LayoutParams(-2, -2);
        setLayoutParamsByOri(context, lp);
        return lp;
    }

    public int getWidth(Context context) {
        return this.wmConfig.getViewWidth() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewWidth(), context) : this.wmConfig.getViewWidth();
    }

    public int getHeight(Context context) {
        return this.wmConfig.getViewHeight() > 0 ? WMBaseUtil.dpToPixel((float) this.wmConfig.getViewHeight(), context) : this.wmConfig.getViewHeight();
    }

    public int generateId(String idStr) {
        if (WMStringUtil.isEmptyString(idStr)) {
            return 0;
        }
        Integer id = (Integer) this.mIds.get(idStr);
        if (id == null) {
            id = Integer.valueOf(View.generateViewId());
            this.mIds.put(idStr, id);
        }
        return id.intValue();
    }

    public String getPath() {
        return this.path;
    }

    public String getLocationKey() {
        return this.wmConfig.getName() + "_" + this.wmConfig.getCategory();
    }

    public synchronized void destoryWaterMark() {
        if (!this.mBeConvertToBitmap) {
            pause();
            if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
                for (WMElement element : this.elements) {
                    element.pause();
                }
                recyleWaterMark();
            }
        }
    }

    private void recyleWaterMark() {
        WMUIUtil.recycleViewGroup(this.mWMRotateLayout);
        if (this.mWMRotateLayout != null) {
            ViewGroup parent = (ViewGroup) this.mWMRotateLayout.getParent();
            if (parent != null) {
                parent.removeView(this.mWMRotateLayout);
            }
        }
        destoryView();
    }

    private void destoryView() {
        if (this.mView != null) {
            this.mView.removeAllViews();
        }
        if (this.mWMRotateLayout != null) {
            this.mWMRotateLayout.setOnTouchListener(null);
            this.mWMRotateLayout.removeAllViews();
        }
        this.mView = null;
        this.mWMRotateLayout = null;
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public void showAnimationTips() {
        if (!WMCollectionUtil.isEmptyCollection(this.elements) && this.mWMRotateLayout != null && this.mView != null) {
            for (WMElement element : this.elements) {
                element.showAnimationTips();
            }
        }
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public void hideAnimationTips() {
        if (!WMCollectionUtil.isEmptyCollection(this.elements) && this.mWMRotateLayout != null && this.mView != null) {
            for (WMElement element : this.elements) {
                element.hideAnimationTips();
            }
        }
    }

    public void onWaterMarkClicked(float x, float y) {
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.onWaterMarkClicked(x, y);
            }
        }
    }

    public void resetWaterMarkLayoutParams() {
        View wmView = getWaterMarkRoot();
        if (wmView != null) {
            wmView.measure(-2, -2);
            int w_src_measured = wmView.getMeasuredWidth();
            int h_src_measured = wmView.getMeasuredHeight();
            if (w_src_measured > 0 && h_src_measured > 0) {
                WMShowRectData.getInstance(wmView.getContext()).setWMViewSizeData(getLocationKey(), w_src_measured, h_src_measured, getScale());
            }
        } else {
            WMShowRectData.getInstance(null).removeWMViewSizeData(getLocationKey());
        }
        this.mNeedClearLayoutParams = true;
        setWMLayoutParams();
    }
}
