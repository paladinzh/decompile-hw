package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.animation.DecelerateInterpolator;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.anim.SupportReverseAnimation;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.anim.AnimationUtils;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.manager.parse.WMElement;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;

public abstract class SlotView extends GLView {
    private Handler mAnimationHandler;
    protected AbsLayout mCurrentLayout;
    protected SlotRenderInterface mCurrentSlotRender;
    protected DeleteSlotAnimation mDeleteSlotAnimation;
    protected DownShiftAnimation mDownShiftAnimation;
    private int mFooterHeight;
    protected AbsLayout mFromLayout;
    protected int mHeadCoverHeight = 0;
    protected boolean mIsLayoutRtl = GalleryUtils.isLayoutRTL();
    private boolean mNeedDeleteAnimation;
    protected HashMap<Object, Object> mVisibleIndexEntryMap;
    private HashMap<Path, Object> mVisiblePathEntryMap;

    public interface SlotUIListener {
        boolean onDeleteSlotAnimationEnd();

        boolean onDeleteSlotAnimationStart();
    }

    public interface SlotRenderInterface {
        void freeVisibleRangeItem(HashMap<Path, Object> hashMap);

        Path getItemPath(Object obj);

        void prepareVisibleRangeItemIndex(HashMap<Path, Object> hashMap, HashMap<Object, Object> hashMap2);
    }

    public abstract class AbsLayout {
        protected int mContentLength;
        protected int mHeight;
        private int[] mOffset = new int[0];
        protected int mScrollPosition;
        private int[] mSizeDelta = new int[0];
        protected int mSlotHeight;
        protected int mSlotHeightGap;
        protected int mSlotWidth;
        protected int mSlotWidthGap;
        protected int mUnitCount;
        protected int mWidth;

        public abstract Rect getTargetSlotRect(Object obj, Rect rect);

        public abstract boolean isPreVisibleStartIndex(Object obj);

        protected int getOffset(int col) {
            if (col < 0 || col >= this.mOffset.length) {
                return 0;
            }
            return this.mOffset[col];
        }

        protected int getSizeDelta(int col) {
            if (col < 0 || col >= this.mSizeDelta.length) {
                return 0;
            }
            return this.mSizeDelta[col];
        }

        protected void apportionMargin() {
            int margin = (this.mWidth - (this.mSlotWidthGap * (this.mUnitCount - 1))) - (this.mSlotWidth * this.mUnitCount);
            GalleryLog.d("SlotView", "status, margin: " + margin + ", cols: " + this.mUnitCount);
            int apportion = margin < 0 ? -1 : 1;
            margin = Math.abs(margin);
            if (margin >= this.mUnitCount) {
                GalleryLog.e("SlotView", "wrong status !!");
                return;
            }
            int i;
            int[] delta = new int[this.mUnitCount];
            Arrays.fill(delta, 0);
            int[] offset = new int[this.mUnitCount];
            Arrays.fill(offset, 0);
            float strid = ((float) this.mUnitCount) / (((float) margin) + WMElement.CAMERASIZEVALUE1B1);
            for (i = 1; i <= margin; i++) {
                delta[(int) (((float) i) * strid)] = apportion;
            }
            int s = 0;
            for (i = 0; i < this.mUnitCount; i++) {
                offset[i] = s;
                s += delta[i];
            }
            this.mSizeDelta = delta;
            this.mOffset = offset;
        }

        public int getScrollLimit() {
            int limit = (this.mContentLength - this.mHeight) + (isPort() ? SlotView.this.mFooterHeight + SlotView.this.mHeadCoverHeight : SlotView.this.mHeadCoverHeight);
            return limit <= 0 ? 0 : limit;
        }

        protected void copyAllParameters(AbsLayout absLayout) {
            absLayout.mSlotWidth = this.mSlotWidth;
            absLayout.mSlotHeight = this.mSlotHeight;
            absLayout.mSlotWidthGap = this.mSlotWidthGap;
            absLayout.mSlotHeightGap = this.mSlotHeightGap;
            absLayout.mWidth = this.mWidth;
            absLayout.mHeight = this.mHeight;
            absLayout.mUnitCount = this.mUnitCount;
            absLayout.mContentLength = this.mContentLength;
            absLayout.mScrollPosition = this.mScrollPosition;
            absLayout.mSizeDelta = (int[]) this.mSizeDelta.clone();
            absLayout.mOffset = (int[]) this.mOffset.clone();
        }

        public boolean isPort() {
            return this.mWidth >= this.mHeight ? LayoutHelper.isPortFeel() : true;
        }
    }

    public class DeleteSlotAnimation extends Animation {
        private AbsLayout mFromLayout;
        private float mProgress;
        private HashMap<Object, Object> mVisibleIndex;
        private HashMap<Path, Object> mVisiblePath;

        public DeleteSlotAnimation(HashMap<Path, Object> visiblePath, HashMap<Object, Object> visibleIndex, AbsLayout layout) {
            this.mVisiblePath = visiblePath;
            this.mVisibleIndex = visibleIndex;
            this.mFromLayout = layout;
            setInterpolator(AnimationUtils.getInterpolator());
            setDuration(AnimationUtils.DEBUG_ANIM_TIME);
        }

        public HashMap<Path, Object> getVisibleItemPathMap() {
            return this.mVisiblePath;
        }

        public HashMap<Object, Object> getVisibleItemIndexMap() {
            return this.mVisibleIndex;
        }

        public AbsLayout getFromLayout() {
            return this.mFromLayout;
        }

        protected void onCalculate(float progress) {
            this.mProgress = progress;
        }

        public void apply(Object index, Rect target) {
            SlotView.this.applyDeleteAnimation(index, target, this.mProgress, this.mVisiblePath, this.mFromLayout);
        }

        public void applyDeletedItem(Rect target) {
            SlotView.this.scaleRect(target, WMElement.CAMERASIZEVALUE1B1 - (this.mProgress * 0.15f));
        }

        public float getAlpha() {
            return WMElement.CAMERASIZEVALUE1B1 - Utils.clamp(this.mProgress, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        }
    }

    public class DownShiftAnimation extends SupportReverseAnimation {
        private boolean mDisable;
        private final Object mDownIndex;
        protected float mProgress;

        public DownShiftAnimation(Object downIndex) {
            this.mProgress = 0.0f;
            this.mDownIndex = downIndex;
            setDuration(92);
            setInterpolator(new DecelerateInterpolator());
        }

        public DownShiftAnimation(SlotView this$0, Object downIndex, boolean disable) {
            this(downIndex);
            this.mDisable = disable;
        }

        protected void onCalculate(float progress) {
            this.mProgress = progress;
        }

        public void apply(Object index, Rect target) {
            if (!this.mDisable) {
                if (index.equals(this.mDownIndex)) {
                    float scaleFactor = SlotView.this.getScaleFactor();
                    SlotView.this.scaleRect(target, scaleFactor + ((WMElement.CAMERASIZEVALUE1B1 - scaleFactor) * (WMElement.CAMERASIZEVALUE1B1 - this.mProgress)));
                    return;
                }
                SlotView.this.transformRect(index, this.mDownIndex, this.mProgress, target);
            }
        }
    }

    public abstract AbsLayout cloneLayout();

    public abstract SlotUIListener getSlotUIListener();

    public SlotView(Context context) {
        this.mFooterHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_footer_height);
        this.mAnimationHandler = new Handler() {
            public void handleMessage(android.os.Message r3) {
                /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0015 in list [B:7:0x0012]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r2 = this;
                r1 = com.huawei.gallery.ui.SlotView.this;
                r0 = r1.getGLRoot();
                if (r0 == 0) goto L_0x000b;
            L_0x0008:
                r0.lockRenderThread();
            L_0x000b:
                r1 = r3.what;	 Catch:{ all -> 0x001c }
                switch(r1) {
                    case 0: goto L_0x0016;
                    default: goto L_0x0010;
                };
            L_0x0010:
                if (r0 == 0) goto L_0x0015;
            L_0x0012:
                r0.unlockRenderThread();
            L_0x0015:
                return;
            L_0x0016:
                r1 = com.huawei.gallery.ui.SlotView.this;	 Catch:{ all -> 0x001c }
                r1.startDeleteSlotAnimation();	 Catch:{ all -> 0x001c }
                goto L_0x0010;
            L_0x001c:
                r1 = move-exception;
                if (r0 == 0) goto L_0x0022;
            L_0x001f:
                r0.unlockRenderThread();
            L_0x0022:
                throw r1;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.ui.SlotView.1.handleMessage(android.os.Message):void");
            }
        };
    }

    protected void scaleRect(Rect target, float scale) {
        float halfWidth = (((float) target.width()) * scale) / 2.0f;
        float halfHeight = (((float) target.height()) * scale) / 2.0f;
        int centerX = target.centerX();
        int centerY = target.centerY();
        target.set((int) (((float) centerX) - halfWidth), (int) (((float) centerY) - halfHeight), (int) (((float) centerX) + halfWidth), (int) (((float) centerY) + halfHeight));
    }

    protected void translateRect(Rect target, float[] shift) {
        int left = (int) (((float) target.left) + shift[0]);
        int top = (int) (((float) target.top) + shift[1]);
        target.set(left, top, left + target.width(), top + target.height());
    }

    protected boolean needRenderSlotAnimationMore(long animTime) {
        boolean z = false;
        DownShiftAnimation downShiftAnimation = this.mDownShiftAnimation;
        if (downShiftAnimation != null) {
            z = downShiftAnimation.calculate(animTime);
            if (!z) {
                this.mDownShiftAnimation = null;
            }
        }
        DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
        if (deleteSlotAnimation != null) {
            boolean needMoreDeleteAnimation = deleteSlotAnimation.calculate(animTime);
            z |= needMoreDeleteAnimation;
            if (!needMoreDeleteAnimation) {
                this.mDeleteSlotAnimation = null;
            }
        }
        return z;
    }

    public void transformRect(Object index, Object downIndex, float progress, Rect target) {
    }

    public float getScaleFactor() {
        return 0.925f;
    }

    @SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public void startClickSlotAnimation(Object index, final Runnable runnable) {
        this.mDownShiftAnimation = new DownShiftAnimation(this, index, runnable != null);
        this.mDownShiftAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd() {
                if (runnable != null) {
                    TraceController.traceBegin("DownShiftAnimation onAnimationEnd");
                    runnable.run();
                    TraceController.traceEnd();
                }
            }
        });
        this.mDownShiftAnimation.start();
        invalidate();
    }

    public void clearDisabledClickSlotAnimation() {
        DownShiftAnimation downShiftAnimation = this.mDownShiftAnimation;
        if (downShiftAnimation != null && downShiftAnimation.mDisable) {
            GLRoot glRoot = getGLRoot();
            if (glRoot != null) {
                glRoot.lockRenderThread();
            }
            try {
                clearAnimation();
            } finally {
                if (glRoot != null) {
                    glRoot.unlockRenderThread();
                }
            }
        }
    }

    public void clearAnimation() {
        boolean invalidate = false;
        DownShiftAnimation downShiftAnimation = this.mDownShiftAnimation;
        if (downShiftAnimation != null) {
            downShiftAnimation.forceStop();
            this.mDownShiftAnimation = null;
            invalidate = true;
        }
        if (invalidate) {
            invalidate();
        }
        clearDeleteSlotAnimation();
    }

    public void enableDeleteAnimation(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap, AbsLayout layout) {
        this.mNeedDeleteAnimation = true;
        this.mVisiblePathEntryMap = visiblePathMap;
        this.mVisibleIndexEntryMap = visibleIndexMap;
        this.mFromLayout = layout;
    }

    private void startDeleteSlotAnimation() {
        if (this.mNeedDeleteAnimation) {
            this.mNeedDeleteAnimation = false;
            final DeleteSlotAnimation deleteSlotAnimation = new DeleteSlotAnimation(this.mVisiblePathEntryMap, this.mVisibleIndexEntryMap, this.mFromLayout);
            this.mVisiblePathEntryMap = null;
            this.mVisibleIndexEntryMap = null;
            this.mFromLayout = null;
            deleteSlotAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd() {
                    final GLRootView glRootView = (GLRootView) SlotView.this.getGLRoot();
                    if (glRootView != null) {
                        final DeleteSlotAnimation deleteSlotAnimation = deleteSlotAnimation;
                        glRootView.post(new Runnable() {
                            public void run() {
                                glRootView.lockRenderThread();
                                try {
                                    HashMap<Path, Object> visiblePathMap = deleteSlotAnimation.getVisibleItemPathMap();
                                    HashMap<Object, Object> visibleIndexMap = deleteSlotAnimation.getVisibleItemIndexMap();
                                    SlotView.this.mCurrentSlotRender.freeVisibleRangeItem(visiblePathMap);
                                    visiblePathMap.clear();
                                    visibleIndexMap.clear();
                                    SlotView.this.getSlotUIListener().onDeleteSlotAnimationEnd();
                                } finally {
                                    glRootView.unlockRenderThread();
                                }
                            }
                        });
                    }
                }
            });
            this.mDeleteSlotAnimation = deleteSlotAnimation;
            this.mDeleteSlotAnimation.start();
            invalidate();
        }
    }

    public void startDeleteSlotAnimationIfNeed() {
        if (this.mNeedDeleteAnimation) {
            boolean haveDialog = getSlotUIListener().onDeleteSlotAnimationStart();
            GalleryLog.d("SlotView", "startDeleteSlotAnimationIfNeed haveDialog:" + haveDialog);
            if (haveDialog) {
                this.mAnimationHandler.sendEmptyMessageDelayed(0, 200);
            } else {
                startDeleteSlotAnimation();
            }
        }
    }

    private void clearDeleteSlotAnimation() {
        this.mNeedDeleteAnimation = false;
        boolean invalidate = false;
        DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
        if (deleteSlotAnimation != null) {
            deleteSlotAnimation.forceStop();
            this.mDeleteSlotAnimation = null;
            invalidate = true;
        }
        if (invalidate) {
            invalidate();
        }
    }

    public boolean needToDoDeleteAnimation() {
        return this.mNeedDeleteAnimation;
    }

    public void clearDeleteVisibleRangeItem() {
        if (this.mVisiblePathEntryMap != null && this.mVisibleIndexEntryMap != null) {
            this.mCurrentSlotRender.freeVisibleRangeItem(this.mVisiblePathEntryMap);
            this.mVisiblePathEntryMap.clear();
            this.mVisibleIndexEntryMap.clear();
        }
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap) {
        if (visiblePathMap != null && visibleIndexMap != null) {
            visiblePathMap.clear();
            visibleIndexMap.clear();
            this.mCurrentSlotRender.prepareVisibleRangeItemIndex(visiblePathMap, visibleIndexMap);
        }
    }

    public void pause() {
        clearAnimation();
        this.mAnimationHandler.removeCallbacksAndMessages(null);
    }

    protected boolean noRender() {
        return this.mNeedDeleteAnimation && this.mDeleteSlotAnimation == null;
    }

    protected void applyDeleteAnimation(Object index, Rect target, float progress, HashMap<Path, Object> visiblePath, AbsLayout fromLayout) {
        if (visiblePath != null && fromLayout != null) {
            Path targetPath = this.mCurrentSlotRender.getItemPath(index);
            if (targetPath != null) {
                Rect fromRect = null;
                if (visiblePath.containsKey(targetPath)) {
                    Object entryIndex;
                    Object entry = visiblePath.get(targetPath);
                    if (entry instanceof BaseEntry) {
                        entryIndex = ((BaseEntry) entry).index;
                    } else {
                        entryIndex = Integer.valueOf(((AlbumEntry) entry).index);
                    }
                    fromRect = fromLayout.getTargetSlotRect(entryIndex, new Rect());
                }
                if (fromRect == null) {
                    fromRect = new Rect(target);
                    if (fromLayout.isPreVisibleStartIndex(index)) {
                        fromRect.offset(0, -this.mCurrentLayout.mHeight);
                    } else {
                        fromRect.offset(0, this.mCurrentLayout.mHeight);
                    }
                } else {
                    fromRect.set(fromRect.left, (fromRect.top - fromLayout.mScrollPosition) + this.mCurrentLayout.mScrollPosition, fromRect.right, (fromRect.bottom - fromLayout.mScrollPosition) + this.mCurrentLayout.mScrollPosition);
                }
                transformTargetRect(target, fromRect, progress);
            }
        }
    }

    private void transformTargetRect(Rect target, Rect from, float progress) {
        if (target.left == from.left) {
            verticalShift(target, from, progress);
        } else if (this.mIsLayoutRtl) {
            horizontalShiftRTL(target, from, progress);
        } else {
            horizontalShift(target, from, progress);
        }
    }

    private void verticalShift(Rect target, Rect from, float progress) {
        int top = (int) (((float) from.top) - (((float) (from.top - target.top)) * progress));
        target.set(target.left, top, target.right, target.height() + top);
    }

    private void horizontalShift(Rect target, Rect from, float progress) {
        int step1;
        int step2;
        int right;
        int left;
        int top;
        int bottom;
        if (from.top == target.top) {
            step1 = from.right - target.right;
            step2 = 0;
        } else {
            step1 = from.right + this.mBounds.left;
            step2 = ((this.mCurrentLayout.mWidth - target.right) + (this.mParent.getWidth() - this.mBounds.right)) + target.width();
        }
        float animationSumX = (float) (step1 + step2);
        int animationX = (int) (animationSumX * progress);
        if (Math.abs(animationX) <= Math.abs(step1)) {
            right = from.right - animationX;
            left = right - from.width();
            top = from.top;
            bottom = from.bottom;
        } else {
            right = (int) (((float) target.right) + (animationSumX - ((float) animationX)));
            left = right - target.width();
            top = target.top;
            bottom = target.bottom;
        }
        target.set(left, top, right, bottom);
    }

    private void horizontalShiftRTL(Rect target, Rect from, float progress) {
        int step1;
        int step2;
        int left;
        int right;
        int top;
        int bottom;
        if (from.top == target.top) {
            step1 = target.left - from.left;
            step2 = 0;
        } else {
            step1 = (this.mCurrentLayout.mWidth - from.left) + (this.mParent.getWidth() - this.mBounds.right);
            step2 = (target.left + target.width()) + this.mBounds.left;
        }
        float animationSumX = (float) (step1 + step2);
        int animationX = (int) (animationSumX * progress);
        if (Math.abs(animationX) <= Math.abs(step1)) {
            left = from.left + animationX;
            right = left + from.width();
            top = from.top;
            bottom = from.bottom;
        } else {
            left = target.left - ((int) (animationSumX - ((float) animationX)));
            right = left + target.width();
            top = target.top;
            bottom = target.bottom;
        }
        target.set(left, top, right, bottom);
    }
}
