package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;
import com.huawei.gallery.editor.ui.SplashMenu.MenuClickListener;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashUIController extends BasePaintUIController implements MenuClickListener {
    private SplashListener mSplashListener;
    private SplashMenu mSplashMenu;

    public interface SplashListener extends BaseMosaicListener {
        void bringCompareButtonFront();
    }

    public SplashUIController(Context context, ViewGroup parentLayout, SplashListener splashListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, splashListener, EditorViewDelegate);
        this.mSplashListener = splashListener;
    }

    public void show() {
        super.show();
        if (this.mMosaicView instanceof SplashView) {
            ((SplashView) this.mMosaicView).setStyle(2);
        }
    }

    protected int getControlLayoutId() {
        return R.layout.editor_splash_control;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_splash_foot_bar : R.layout.editor_splash_foot_bar_land;
    }

    protected void attachToParent() {
        super.attachToParent();
        this.mSplashListener.bringCompareButtonFront();
    }

    protected void createView() {
        super.createView();
        this.mSplashMenu = (SplashMenu) this.mContainer.findViewById(R.id.paint_menu);
        this.mSplashMenu.initialize(this);
    }

    protected void removeView() {
    }

    public void onTouchChanged(boolean clickable) {
        if (this.mSplashMenu == null) {
            return;
        }
        if (clickable) {
            this.mSplashMenu.unLock();
        } else {
            this.mSplashMenu.lock();
        }
    }

    public void onStyleChange(int style) {
        if (this.mMosaicView != null && (this.mMosaicView instanceof SplashView)) {
            ((SplashView) this.mMosaicView).setStyle(style);
        }
    }

    public int getPaintType() {
        return 2;
    }

    public boolean closeMenu() {
        if (this.mSplashMenu == null || !this.mSplashMenu.inOpenState()) {
            return false;
        }
        this.mSplashMenu.close();
        return true;
    }

    public boolean isPointValid(int x, int y) {
        return this.mSourceRectVisible != null ? this.mSourceRectVisible.contains(x, y) : false;
    }

    public void reportToBigdataOfSplashUsed() {
        if (this.mMosaicView instanceof SplashView) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("SetColorCount", ((SplashView) this.mMosaicView).getSetColorCount());
                jsonObject.put("UseEraserCount", ((SplashView) this.mMosaicView).getUseEraserCount());
            } catch (JSONException e) {
                GalleryLog.i("SplashUIController", "JSONObject.put() failed in reportToBigdataOfSplashUsed() method, reason: JSONException.");
            }
            ReportToBigData.report(82, jsonObject.toString());
        }
    }
}
