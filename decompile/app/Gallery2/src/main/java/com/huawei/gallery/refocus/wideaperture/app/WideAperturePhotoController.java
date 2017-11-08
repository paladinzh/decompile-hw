package com.huawei.gallery.refocus.wideaperture.app;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.refocus.app.AbsRefocusController;
import com.huawei.gallery.refocus.app.AbsRefocusDelegate;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.FilterType;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.WideAperturePhotoListener;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;

public class WideAperturePhotoController extends AbsRefocusController implements WideAperturePhotoListener {
    private Handler mHandler;
    private WideAperturePhotoImpl mWideAperturePhoto = new WideAperturePhotoImpl(this.mDelegate.getFilePath(), this.mPhotoWidth, this.mPhotoHeight);

    public WideAperturePhotoController(Context context, AbsRefocusDelegate delegate) {
        super(context, delegate);
        this.mWideAperturePhoto.setWideAperturePhotoListener(this);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        File newFile = new File((String) msg.obj);
                        if (msg.arg1 == 0) {
                            RefocusMediaScannerClient refocusMediaScannerClient = new RefocusMediaScannerClient(WideAperturePhotoController.this.mContext, newFile);
                            return;
                        } else if (-1 == msg.arg1) {
                            AbsRefocusController.showHint(WideAperturePhotoController.this.mContext, WideAperturePhotoController.this.mContext.getString(R.string.photoshare_toast_nospace_Toast), 0);
                            if (newFile.exists() && !newFile.delete()) {
                                newFile.deleteOnExit();
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    case 2:
                        WideAperturePhotoController.this.mDelegate.setWideApertureValue(WideAperturePhotoController.this.mWideAperturePhoto.getWideApertureValue());
                        WideAperturePhotoController.this.mPrepareComplete = true;
                        WideAperturePhotoController.this.mDelegate.preparePhotoComplete();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public boolean prepare() {
        return this.mWideAperturePhoto.prepare();
    }

    public void setViewMode(int viewMode) {
        this.mWideAperturePhoto.setViewMode(viewMode);
    }

    public void resizePhoto() {
        this.mPhotoWidth = this.mDelegate.getPhotoWidth();
        this.mPhotoHeight = this.mDelegate.getPhotoHeight();
        checkIfNeedSwapPhotoWidthAndHeight();
        this.mWideAperturePhoto.resizePhoto(this.mPhotoWidth, this.mPhotoHeight);
    }

    public boolean doRefocus(Point touchPoint) {
        if (!this.mWideAperturePhoto.isRefocusPhoto()) {
            return false;
        }
        Point refocusPoint = this.mDelegate.getTouchPositionInImage(touchPoint);
        if (refocusPoint.x < 0 || refocusPoint.x > this.mPhotoWidth || refocusPoint.y < 0 || refocusPoint.y > this.mPhotoHeight) {
            return false;
        }
        this.mDelegate.showFocusIndicator(touchPoint);
        return this.mWideAperturePhoto.doRefocus(refocusPoint) == 1;
    }

    public Point getCurrentDoRefocusPointInImage() {
        return this.mWideAperturePhoto.getFocusPoint();
    }

    public boolean saveFileIfNecessary() {
        if (!ifPhotoChanged()) {
            return false;
        }
        this.mWideAperturePhoto.saveFile();
        return true;
    }

    public void saveAs() {
        String filePath = getFilePath();
        if (!this.mWideAperturePhoto.isRefocusPhoto()) {
            this.mDelegate.saveAsComplete(-2);
        } else if (filePath == null) {
            GalleryLog.e("WideAperturePhotoController", "Cannot get file path, return.");
            this.mDelegate.saveAsComplete(-2);
        } else {
            this.mWideAperturePhoto.saveAs(filePath);
        }
    }

    public void cleanUp() {
        if (this.mWideAperturePhoto != null) {
            this.mWideAperturePhoto.cleanupResource();
        }
    }

    public void showFocusIndicator() {
        if (this.mWideAperturePhoto.isRefocusPhoto()) {
            this.mDelegate.showFocusIndicator(this.mDelegate.transformToScreenCoordinate(this.mWideAperturePhoto.getFocusPoint()));
        }
    }

    public boolean ifPhotoChanged() {
        return this.mWideAperturePhoto.photoChanged();
    }

    public void finishRefocus() {
        this.mDelegate.finishRefocus();
    }

    public void onRefocusComplete() {
        this.mDelegate.refreshPhoto(this.mWideAperturePhoto.getProcessedPhoto());
        this.mDelegate.finishRefocus();
    }

    public void onSaveFileComplete(int saveState) {
        this.mDelegate.saveFileComplete(saveState);
    }

    public void onSaveAsComplete(int saveState, String filePath) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, saveState, 0, filePath));
    }

    public void onPrepareComplete() {
        this.mHandler.sendEmptyMessage(2);
    }

    public void setWideApertureValue(int value) {
        this.mWideAperturePhoto.setWideApertureValue(value);
    }

    public boolean isRefocusPhoto() {
        return this.mWideAperturePhoto.isRefocusPhoto();
    }

    public int getWideApertureLevel() {
        return this.mWideAperturePhoto.getWideApertureLevel();
    }

    public int getFilterTypeIndex() {
        return this.mWideAperturePhoto.getFilterTypeIndex();
    }

    public void applyFilter(int filterType) {
        this.mWideAperturePhoto.applyFilter(getFilter(filterType));
    }

    protected void checkIfNeedSwapPhotoWidthAndHeight() {
        if (this.mWideAperturePhoto.getOrientation() == 90 || this.mWideAperturePhoto.getOrientation() == 270) {
            this.mPhotoWidth ^= this.mPhotoHeight;
            this.mPhotoHeight ^= this.mPhotoWidth;
            this.mPhotoWidth ^= this.mPhotoHeight;
            this.mWideAperturePhoto.resizePhoto(this.mPhotoWidth, this.mPhotoHeight);
        }
    }

    public void onGotFocusPoint() {
        this.mDelegate.onGotFocusPoint();
    }

    public void restoreOriginalRefocusPoint() {
        this.mWideAperturePhoto.restoreOriginalRefocusPoint();
    }

    public int getRefocusFnumValue() {
        return this.mWideAperturePhoto.getWideApertureValue();
    }

    public int getWideAperturePhotoMode() {
        return this.mWideAperturePhoto.getWideAperturePhotoMode();
    }

    private FilterType getFilter(int filterType) {
        if (filterType < FilterType.NORMAL.ordinal() || filterType >= FilterType.INVALID.ordinal()) {
            return FilterType.NORMAL;
        }
        return FilterType.values()[filterType];
    }

    public String getSaveMessage() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("WideAperture Mode", this.mWideAperturePhoto.getWideAperturePhotoMode());
            jsonObject.put("WideAperture value", this.mWideAperturePhoto.getWideApertureValue());
        } catch (JSONException e) {
            GalleryLog.d("WideAperturePhotoController", "JSONException save Message");
        }
        return jsonObject.toString();
    }
}
