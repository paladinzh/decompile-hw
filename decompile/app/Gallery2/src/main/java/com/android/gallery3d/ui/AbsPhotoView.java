package com.android.gallery3d.ui;

import android.graphics.Rect;
import android.view.MotionEvent;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.MenuExecutor.ExtraActionListener;
import com.android.gallery3d.ui.PhotoMagnifierView.PhotoMagnifierListener;
import com.huawei.gallery.anim.PhotoFallbackEffect;

public abstract class AbsPhotoView extends GLView implements PhotoMagnifierListener {

    public interface Model extends com.android.gallery3d.ui.TileImageView.Model {
        int getCurrentIndex();

        int getImageRotation(int i);

        void getImageSize(int i, Size size);

        int getLoadingState(int i);

        MediaItem getMediaItem(int i);

        ScreenNail getScreenNail(int i);

        boolean isCamera(int i);

        boolean isLCDDownloaded();

        boolean isPanorama(int i);

        boolean isStaticCamera(int i);

        boolean isVideo(int i);

        void moveTo(int i, int i2);

        void setFocusHintDirection(int i);

        void setNeedFullImage(boolean z);
    }

    public interface Listener {
        boolean calledToSimpleEditor();

        boolean inBurstMode();

        boolean inEditorMode();

        boolean isDetailsShow();

        void onActionBarAllowed(boolean z);

        void onActionBarWanted();

        void onCommitDeleteImage();

        void onCurrentImageUpdated();

        void onDeleteImage(Path path, int i);

        void onEnterPhotoMagnifierMode();

        void onFilmModeChanged(boolean z);

        void onFlingDown();

        void onFlingUp();

        void onLeavePhotoMagnifierMode();

        void onLoadStateChange(int i);

        void onPhotoTranslationChange(float f, float f2, int i, boolean z, MediaItem mediaItem);

        void onPictureCenter(boolean z);

        void onPictureFullView();

        void onRenderFinish();

        void onScroll(float f, float f2, float f3, float f4);

        void onSingleTapUp(int i, int i2);

        void onSlidePicture();

        void onSnapback();

        void onSwipeImages(float f, float f2);

        void onTouchEventReceived(MotionEvent motionEvent);

        void onUndoBarVisibilityChanged(boolean z);
    }

    public static class Size {
        public int height;
        public int width;
    }

    public abstract PhotoFallbackEffect buildFallbackEffect(GLView gLView, GLCanvas gLCanvas);

    public abstract boolean getFilmMode();

    public abstract Rect getPhotoRect(int i);

    public abstract float getScaleForAnimation(float f);

    public abstract boolean isExtraActionDoing();

    public abstract boolean isTileViewFromCache();

    public abstract boolean resetToFullView();

    public void setModel(Model model) {
    }

    public void setWantPictureCenterCallbacks(boolean wanted) {
    }

    public void setWantPictureFullViewCallbacks(boolean wanted) {
    }

    public void notifyDataChange(int[] fromIndex, int prevBound, int nextBound, int maskOffset) {
    }

    public void notifyImageChange(int index) {
    }

    public void setSimpleGestureListener(SimpleGestureListener listner) {
    }

    public MediaItem getMediaItem(int offset) {
        return null;
    }

    public void drawPhotoMagnifier(float x, float y) {
    }

    public TileImageView getTileImageView() {
        return null;
    }

    public void onMagnifierAnimationEnd() {
    }

    public void setSwipingEnabled(boolean enabled) {
    }

    public void setFilmModeAllowed(boolean allow) {
    }

    public void setFilmMode(boolean enabled) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void switchPictureByFingerprintKey(boolean isForward) {
    }

    public void autoSlidePicture(ExtraActionListener listener) {
    }

    public void setListener(Listener listener) {
    }

    public void setMediaItemScreenNail(MediaItem mediaItem, long startFromCamera) {
    }

    public void setMediaItemScreenNail(MediaItem mediaItem) {
    }

    public void onDeleteDelay() {
    }

    public void onDataUpdate() {
    }

    public void prepareTextures() {
    }

    public void freeTextures() {
    }
}
