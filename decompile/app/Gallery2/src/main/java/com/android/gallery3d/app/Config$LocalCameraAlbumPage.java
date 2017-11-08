package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.TimeBucketPage.LayoutSpec;
import com.huawei.gallery.ui.ListSlotView.Spec;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;

public class Config$LocalCameraAlbumPage {
    private static Config$LocalCameraAlbumPage sInstance;
    public LayoutSpec layoutSpec;
    public int placeholderColor;
    public Spec slotViewSpec;
    public TitleSpec titleLabelSpec;

    public static synchronized Config$LocalCameraAlbumPage get(Context context) {
        Config$LocalCameraAlbumPage config$LocalCameraAlbumPage;
        synchronized (Config$LocalCameraAlbumPage.class) {
            if (sInstance == null) {
                sInstance = new Config$LocalCameraAlbumPage(context);
            }
            long startTime = System.currentTimeMillis();
            sInstance.titleLabelSpec.background_color = context.getResources().getColor(R.color.album_background);
            sInstance.titleLabelSpec.time_line_text_color = context.getResources().getColor(R.color.time_line_text_color);
            sInstance.titleLabelSpec.mTimeTextPaint = createTextPaint(sInstance.titleLabelSpec.time_line_text_size, sInstance.titleLabelSpec.time_line_text_color);
            GalleryLog.d("Config", "Get color res time: " + (System.currentTimeMillis() - startTime));
            config$LocalCameraAlbumPage = sInstance;
        }
        return config$LocalCameraAlbumPage;
    }

    protected static TextPaint createTextPaint(int textSize, int color) {
        TextPaint paint = new TextPaint();
        paint.setTextSize((float) textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    protected static int getTitleHeight(Context context, TitleSpec titleSpec, int firstLineTextHeight, int secondLineTextHeight) {
        if (titleSpec == null || context == null) {
            return 0;
        }
        if (GalleryUtils.isTabletProduct(context)) {
            return context.getResources().getDimensionPixelSize(R.dimen.time_line_title_height);
        }
        return ((titleSpec.time_line_top_magin + firstLineTextHeight) + secondLineTextHeight) + titleSpec.time_line_bottom_margin;
    }

    private Config$LocalCameraAlbumPage(Context context) {
        Resources r = context.getResources();
        this.titleLabelSpec = new TitleSpec();
        this.titleLabelSpec.time_line_width = r.getDimensionPixelSize(R.dimen.time_line_width);
        this.titleLabelSpec.time_line_start_padding = r.getDimensionPixelSize(R.dimen.time_line_label_start_padding);
        this.titleLabelSpec.time_line_top_magin = r.getDimensionPixelSize(R.dimen.time_line_label_top_margin);
        this.titleLabelSpec.time_line_bottom_margin = r.getDimensionPixelSize(R.dimen.time_line_label_bottom_margin);
        this.titleLabelSpec.time_line_text_size = r.getDimensionPixelSize(R.dimen.time_line_text_size);
        this.titleLabelSpec.time_line_title_gap = r.getDimensionPixelSize(R.dimen.time_line_title_gap);
        this.titleLabelSpec.time_line_icon = r.getDimensionPixelSize(R.dimen.time_line_icon);
        this.titleLabelSpec.time_line_group_title_text_size = r.getDimensionPixelSize(R.dimen.time_line_group_title_text_size);
        this.titleLabelSpec.time_line_text_color = r.getColor(R.color.time_line_text_color);
        this.titleLabelSpec.bardian_time_line_group_title_text_color = r.getColor(R.color.time_line_group_title_text_color);
        this.titleLabelSpec.background_color = r.getColor(R.color.album_background);
        this.titleLabelSpec.label_width = Math.min(1024, GalleryUtils.getWidthPixels()) - GalleryUtils.dpToPixel(20);
        this.titleLabelSpec.mTimeTextPaint = createTextPaint(this.titleLabelSpec.time_line_text_size, this.titleLabelSpec.time_line_text_color);
        this.titleLabelSpec.mTimeTextPaint.setTypeface(Typeface.create("HwChinese-medium", 0));
        this.titleLabelSpec.mTimeTextHeight = GalleryUtils.getFontHeightOfPaint(this.titleLabelSpec.mTimeTextPaint);
        this.titleLabelSpec.mGroupTitleTextPaint = createTextPaint(this.titleLabelSpec.time_line_group_title_text_size, this.titleLabelSpec.bardian_time_line_group_title_text_color);
        GalleryUtils.setTypeFaceAsSlim(this.titleLabelSpec.mGroupTitleTextPaint);
        this.titleLabelSpec.mGroupTitleTextHeight = GalleryUtils.getFontHeightOfPaint(this.titleLabelSpec.mGroupTitleTextPaint);
        this.titleLabelSpec.label_min_height = getTitleHeight(context, this.titleLabelSpec, this.titleLabelSpec.mTimeTextHeight, this.titleLabelSpec.mGroupTitleTextHeight);
        this.titleLabelSpec.label_tag = 1;
        this.layoutSpec = new LayoutSpec();
        this.layoutSpec.time_line_width = this.titleLabelSpec.time_line_width;
        this.layoutSpec.local_camera_page_left_padding = r.getDimensionPixelSize(R.dimen.local_camera_page_left_padding);
        this.layoutSpec.local_camera_page_right_padding = r.getDimensionPixelSize(R.dimen.local_camera_page_right_padding);
        this.placeholderColor = r.getColor(R.color.album_placeholder);
        this.slotViewSpec = new Spec();
        this.slotViewSpec.slotPortCountByDay = r.getInteger(R.integer.port_time_day_slot_count);
        this.slotViewSpec.slotLandCountByDay = r.getInteger(R.integer.land_time_day_slot_count);
        this.slotViewSpec.slotPortCountByMonth = r.getInteger(R.integer.port_time_month_slot_count);
        this.slotViewSpec.slotLandCountByMonth = r.getInteger(R.integer.land_time_month_slot_count);
        this.slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.time_line_slot_gap);
        this.slotViewSpec.titleHeight = this.titleLabelSpec.label_min_height;
    }
}
