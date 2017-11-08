package com.huawei.gallery.recycle.ui;

import android.content.res.Resources;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.util.SparseArray;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.StringStaticLayoutTexture;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.CommonAlbumSlotRender;
import com.huawei.gallery.ui.CommonAlbumSlotView;

public class RecycleAlbumSlotRender extends CommonAlbumSlotRender {
    private static final int MARGIN_BOTTOM = GalleryUtils.dpToPixel(5);
    private LeftDaysTextureProvider mLeftDaysTextureProvider;
    private long mServerTime = -1;
    private TextPaint mTextPaint;
    private StringStaticLayoutTexture mTitleTexture;
    private int mTopHorizonPadding;
    private int mTopTitleHeight = 0;
    private int mTopTitleWidth = 0;

    public class LeftDaysTextureProvider {
        private SparseArray mDaysTexture = new SparseArray();

        public StringTexture getLeftDaysTexture(long recycleTime, int width, boolean isCloudRecycleItem) {
            int days;
            if (isCloudRecycleItem) {
                RecycleAlbumSlotRender.this.mServerTime = RecycleAlbumSlotRender.this.mServerTime > 0 ? RecycleAlbumSlotRender.this.mServerTime : System.currentTimeMillis();
                days = Utils.clamp(30 - Utils.severalDaysFromNow(recycleTime, RecycleAlbumSlotRender.this.mServerTime, true), 0, 29);
            } else {
                days = Utils.clamp(30 - Utils.severalDaysFromNow(recycleTime, System.currentTimeMillis(), true), 0, 29);
            }
            StringTexture texture = (StringTexture) this.mDaysTexture.get(days);
            if (texture != null && texture.isContentValid()) {
                return texture;
            }
            texture = getSingleDaysTexture(days, width);
            this.mDaysTexture.put(days, texture);
            return texture;
        }

        private StringTexture getSingleDaysTexture(int days, int width) {
            Resources r = RecycleAlbumSlotRender.this.mActivity.getResources();
            return StringTexture.newInstance(r.getQuantityString(R.plurals.text_recentlydeletedremainingdays, days, new Object[]{Integer.valueOf(days)}), (float) ((int) (((float) width) * 0.85f)), StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_albumSet_sub_name_size), r.getColor(R.color.video_title_text_color)));
        }

        public void freeTexture() {
            if (this.mDaysTexture != null) {
                int size = this.mDaysTexture.size();
                for (int i = 0; i < size; i++) {
                    ((StringTexture) this.mDaysTexture.valueAt(i)).recycle();
                }
                this.mDaysTexture.clear();
                this.mDaysTexture = null;
            }
        }
    }

    public void updateDaysTextureIfNeed(long time) {
        int delta = Math.abs(Utils.severalDaysFromNow(time, this.mServerTime, true));
        this.mServerTime = time;
        if (time > -1 && delta >= 1) {
            this.mSlotView.invalidate();
        }
    }

    public RecycleAlbumSlotRender(GalleryContext context, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(context, slotView, selectionManager, placeholderColor);
        Resources r = context.getResources();
        this.mTextPaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.time_line_text_size), r.getColor(R.color.photoshare_login_title_color));
        this.mTopHorizonPadding = r.getDimensionPixelSize(R.dimen.recycle_head_tip_port_margin);
    }

    protected void renderOverlay(GLCanvas canvas, AlbumEntry entry, int width, int height) {
        super.renderOverlay(canvas, entry, width, height);
        if (this.mLeftDaysTextureProvider == null) {
            this.mLeftDaysTextureProvider = new LeftDaysTextureProvider();
        }
        StringTexture leftDaysTexture = this.mLeftDaysTextureProvider.getLeftDaysTexture(entry.recycleTime, width, entry.isCloudRecycleItem);
        int w = leftDaysTexture.getWidth();
        int h = leftDaysTexture.getHeight();
        leftDaysTexture.draw(canvas, (width - w) / 2, (height - h) - MARGIN_BOTTOM, w, h);
    }

    public void renderTopTitle(GLCanvas canvas, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        if (w == this.mTopTitleWidth && h == this.mTopTitleHeight) {
            if (this.mTitleTexture == null) {
            }
            this.mTitleTexture.draw(canvas, this.mTopHorizonPadding + left, top);
            this.mTopTitleWidth = w;
            this.mTopTitleHeight = h;
        }
        this.mTitleTexture = new StringStaticLayoutTexture(String.format(this.mActivity.getString(R.string.text_recentlydeletedtips), new Object[]{Integer.valueOf(30)}), this.mTextPaint, Alignment.ALIGN_NORMAL, w - (this.mTopHorizonPadding * 2), h, 5);
        this.mTitleTexture.draw(canvas, this.mTopHorizonPadding + left, top);
        this.mTopTitleWidth = w;
        this.mTopTitleHeight = h;
    }

    public void destroy() {
        if (this.mLeftDaysTextureProvider != null) {
            this.mLeftDaysTextureProvider.freeTexture();
            this.mLeftDaysTextureProvider = null;
        }
        super.destroy();
    }
}
