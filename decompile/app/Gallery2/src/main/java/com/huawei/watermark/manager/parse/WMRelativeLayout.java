package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class WMRelativeLayout extends WMElement {
    private ArrayList<WMElement> elements = new ArrayList();

    public WMRelativeLayout(XmlPullParser parser) {
        super(parser);
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        this.mOri = ori;
        RelativeLayout rootLayout = new RelativeLayout(context);
        if (WMBaseUtil.supportJELLYBEANMR1()) {
            rootLayout.setLayoutDirection(0);
        }
        rootLayout.setLayoutParams(generateLp(context, wm, parentLayoutMode));
        rootLayout.setId(wm.generateId(this.id));
        rootLayout.setGravity(getGravity());
        for (WMElement element : this.elements) {
            View view = element.toView(context, wm, WMStaticFinalParameter.ISRELATIVELAYOUT, ori);
            if (view != null) {
                rootLayout.addView(view);
            }
        }
        showBackground(context, rootLayout, wm.getPath());
        setPadding(context, wm, rootLayout);
        return rootLayout;
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.initBaseLogicData(context, delegate);
            }
        }
    }

    public void onOrientationChanged(int ori) {
        super.onOrientationChanged(ori);
        for (WMElement element : this.elements) {
            element.onOrientationChanged(ori);
        }
    }

    public void addElement(WMElement element) {
        this.elements.add(element);
    }

    public void resume() {
        super.resume();
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.resume();
            }
        }
    }

    public void pause() {
        super.pause();
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.pause();
            }
        }
    }

    public void showAnimationTips() {
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
            for (WMElement element : this.elements) {
                element.showAnimationTips();
            }
        }
    }

    public void hideAnimationTips() {
        if (!WMCollectionUtil.isEmptyCollection(this.elements)) {
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

    public WMElement getElementById(String idtemp) {
        for (int i = 0; i < this.elements.size(); i++) {
            WMElement element = (WMElement) this.elements.get(i);
            if (!WMStringUtil.isEmptyString(element.id) && element.id.equalsIgnoreCase(idtemp)) {
                return element;
            }
        }
        return null;
    }
}
