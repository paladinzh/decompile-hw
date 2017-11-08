package com.huawei.gallery.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;

public class PhotoShareTagAlbumSetSlotView extends CommonAlbumSetSlotView {
    private GestureDetector mGestureDetector;
    private boolean mIsAlbumName = false;
    private Listener mListener = null;

    public interface Listener extends com.huawei.gallery.ui.CommonAlbumSlotView.Listener {
        void onAlbumNameTapUp(int i);
    }

    public class Layout extends com.huawei.gallery.ui.CommonAlbumSlotView.Layout {
        public Layout(Spec spec) {
            super(spec);
        }

        public boolean isAlbumName(float y) {
            int absoluteY = (Math.round(y) + this.mScrollPosition) - this.mVerticalPadding.get();
            if (absoluteY < 0) {
                return false;
            }
            if (this.mSpec.album_name_text_height + (absoluteY % (this.mSlotHeight + this.mSlotHeightGap)) >= this.mSlotWidth) {
                return true;
            }
            return false;
        }
    }

    private class MyGestureListener extends MyGestureListener {
        private MyGestureListener() {
            super();
        }

        public void onShowPress(MotionEvent e) {
            PhotoShareTagAlbumSetSlotView.this.logd("onShowPress");
            GLRoot root = PhotoShareTagAlbumSetSlotView.this.getGLRoot();
            if (root != null) {
                root.lockRenderThread();
                try {
                    if (!isDown()) {
                        int index = PhotoShareTagAlbumSetSlotView.this.mIndexDown;
                        if (index != -1) {
                            setDownFlag(true);
                            if (!(PhotoShareTagAlbumSetSlotView.this.mIsAlbumName || PhotoShareTagAlbumSetSlotView.this.mListener == null)) {
                                PhotoShareTagAlbumSetSlotView.this.mListener.onDown(index);
                            }
                        }
                        root.unlockRenderThread();
                    }
                } finally {
                    root.unlockRenderThread();
                }
            }
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            PhotoShareTagAlbumSetSlotView.this.logd("onScroll");
            cancelDown(false);
            if (PhotoShareTagAlbumSetSlotView.this.mFirstScroll) {
                PhotoShareTagAlbumSetSlotView.this.mFirstScroll = false;
                PhotoShareTagAlbumSetSlotView.this.mDoSelect = Math.abs(distanceX) > Math.abs(distanceY);
            }
            if (PhotoShareTagAlbumSetSlotView.this.mListener != null && PhotoShareTagAlbumSetSlotView.this.mListener.inSelectionMode() && PhotoShareTagAlbumSetSlotView.this.mDoSelect) {
                PhotoShareTagAlbumSetSlotView.this.mListener.onScroll(PhotoShareTagAlbumSetSlotView.this.mLayout.getSlotIndexByPosition(e2.getX(), e2.getY()));
            } else {
                int overDistance = PhotoShareTagAlbumSetSlotView.this.mScroller.startScroll(Math.round(distanceY), 0, PhotoShareTagAlbumSetSlotView.this.mLayout.getScrollLimit(), PhotoShareTagAlbumSetSlotView.this.mLayout.mHeight);
                if (PhotoShareTagAlbumSetSlotView.this.mOverscrollEffect == 0 && overDistance != 0) {
                    PhotoShareTagAlbumSetSlotView.this.mPaper.overScroll((float) overDistance);
                }
            }
            PhotoShareTagAlbumSetSlotView.this.invalidate();
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            PhotoShareTagAlbumSetSlotView.this.logd("PhotoShareTagAlbumSetSlotView onSingleTapUp");
            PhotoShareTagAlbumSetSlotView.this.mIndexUp = PhotoShareTagAlbumSetSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            cancelDown(false);
            if (!(PhotoShareTagAlbumSetSlotView.this.mDownInScrolling || PhotoShareTagAlbumSetSlotView.this.mIndexUp == -1)) {
                if (PhotoShareTagAlbumSetSlotView.this.mLayout.isAlbumName(e.getY())) {
                    if (PhotoShareTagAlbumSetSlotView.this.mListener != null) {
                        PhotoShareTagAlbumSetSlotView.this.mListener.onAlbumNameTapUp(PhotoShareTagAlbumSetSlotView.this.mIndexUp);
                    }
                } else if (PhotoShareTagAlbumSetSlotView.this.mListener != null) {
                    PhotoShareTagAlbumSetSlotView.this.mListener.onSingleTapUp(PhotoShareTagAlbumSetSlotView.this.mIndexUp, false);
                }
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
            PhotoShareTagAlbumSetSlotView.this.logd("onLongPress");
            cancelDown(true);
            if (!PhotoShareTagAlbumSetSlotView.this.mDownInScrolling) {
                PhotoShareTagAlbumSetSlotView.this.lockRendering();
                try {
                    int index = PhotoShareTagAlbumSetSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                    if (!(index == -1 || PhotoShareTagAlbumSetSlotView.this.mLayout.isAlbumName(e.getY()))) {
                        if (PhotoShareTagAlbumSetSlotView.this.mListener != null) {
                            PhotoShareTagAlbumSetSlotView.this.mListener.onLongTap(index);
                        }
                    }
                    PhotoShareTagAlbumSetSlotView.this.unlockRendering();
                } catch (Throwable th) {
                    PhotoShareTagAlbumSetSlotView.this.unlockRendering();
                }
            }
        }

        public boolean onDown(MotionEvent e) {
            PhotoShareTagAlbumSetSlotView.this.logd("onDown");
            cancelTimeOut();
            PhotoShareTagAlbumSetSlotView.this.mIndexUp = -1;
            PhotoShareTagAlbumSetSlotView.this.mIndexDown = PhotoShareTagAlbumSetSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            if (PhotoShareTagAlbumSetSlotView.this.mIndexDown != -1) {
                PhotoShareTagAlbumSetSlotView.this.mIsAlbumName = PhotoShareTagAlbumSetSlotView.this.mLayout.isAlbumName(e.getY());
            }
            startTimeOut();
            return false;
        }
    }

    public PhotoShareTagAlbumSetSlotView(GalleryContext activity, Spec spec) {
        super(activity, spec);
    }

    protected com.huawei.gallery.ui.CommonAlbumSlotView.Layout createLayout(Spec spec) {
        return new Layout(spec);
    }

    protected GestureDetector createGestureDetector(GalleryContext activity) {
        this.mGestureDetector = new GestureDetector(activity.getAndroidContext(), new MyGestureListener());
        return this.mGestureDetector;
    }

    public void setListener(Listener listener) {
        super.setListener(listener);
        this.mListener = listener;
    }
}
