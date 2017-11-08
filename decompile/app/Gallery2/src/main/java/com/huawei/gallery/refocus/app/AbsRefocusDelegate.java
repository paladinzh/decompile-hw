package com.huawei.gallery.refocus.app;

import android.graphics.Bitmap;
import android.graphics.Point;
import com.android.gallery3d.ui.BitmapScreenNail;

public abstract class AbsRefocusDelegate {
    public String getFilePath() {
        return null;
    }

    public Point getTouchPositionInImage(Point touchPosion) {
        return null;
    }

    public Point transformToScreenCoordinate(Point focusPoint) {
        return null;
    }

    public int getPhotoWidth() {
        return 0;
    }

    public int getPhotoHeight() {
        return 0;
    }

    public void showFocusIndicator(Point pointer) {
    }

    public void finishRefocus() {
    }

    public void refreshPhoto(byte[] bytes, int offset, int length) {
    }

    public void saveFileComplete(int saveState) {
    }

    public void saveAsComplete(int saveState) {
    }

    public void refreshPhoto(Bitmap bitmap) {
    }

    public void refreshPhoto(BitmapScreenNail bitmapScreenNail) {
    }

    public void setWideApertureValue(int value) {
    }

    public void preparePhotoComplete() {
    }

    public void onGotFocusPoint() {
    }

    public void sendEmptyMessage(int what) {
    }

    public void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
    }

    public void removeMessages(int what) {
    }

    public int getDoRefocusMessageID() {
        return 0;
    }

    public int getApplyFilterMessageID() {
        return 0;
    }

    public int getRefocusSaveMessageID() {
        return 0;
    }

    public int getFinishRefocusMessageID() {
        return 0;
    }

    public int getShowProgressMessageID() {
        return 0;
    }

    public int getWideApertureValueChangedMessageID() {
        return 0;
    }

    public void enableSaveAction(boolean enabled) {
    }

    public void finishActivity() {
    }

    public void doBackPress() {
    }

    public void refreshLayout() {
    }
}
