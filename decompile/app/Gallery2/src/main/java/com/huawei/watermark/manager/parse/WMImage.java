package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.view.View;
import com.huawei.watermark.ui.WMImageView;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMImage extends WMElement {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMImage.class.getSimpleName());
    private WMImageView mImageView;
    protected String pic;

    public WMImage(XmlPullParser parser) {
        super(parser);
        this.pic = getStringByAttributeName(parser, "pic");
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        this.mOri = ori;
        WMImageView iv = new WMImageView(context);
        this.mImageView = iv;
        if (WMBaseUtil.supportJELLYBEANMR1()) {
            iv.setLayoutDirection(0);
        }
        if (!WMStringUtil.isEmptyString(this.id)) {
            iv.setId(wm.generateId(this.id));
        }
        iv.setLayoutParams(generateLp(context, wm, parentLayoutMode));
        showBackground(context, iv, wm.getPath());
        decoratorImage(context, wm, iv);
        return iv;
    }

    public void decoratorImage(Context context, WaterMark wm, WMImageView iv) {
        if (!WMStringUtil.isEmptyString(this.pic)) {
            iv.setWMImagePath(wm.getPath(), this.pic);
        }
    }

    public View getView() {
        return this.mImageView;
    }

    public void resume() {
    }

    public void pause() {
        super.pause();
        this.mImageView = null;
    }
}
