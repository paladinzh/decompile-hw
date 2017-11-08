package com.android.gallery3d.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.DialogDetailsView.DetailItem;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import java.util.ArrayList;

public class DetailLayout extends LinearLayout {
    private static String DEFAULT_VALUE = "--";
    private static final ArrayList<int[]> sDetailCameraParamIds = new ArrayList();
    private static final SparseArray<int[]> sDetailIds = new SparseArray();
    private HwCustDetailLayout custLayout;
    private OnDetailLoadedDelegate mDelegate;
    private boolean mPhotoChanged = true;
    private ScrollView mScrollView;

    public interface OnDetailLoadedDelegate {
        Bitmap getCurrentBitmap();

        int getCustomDrawableId();

        String getTitle();
    }

    static {
        sDetailIds.put(R.id.detail_title, new int[]{1});
        sDetailIds.put(R.id.detail_model, new int[]{2});
        sDetailIds.put(R.id.detail_resolution, new int[]{6, 7});
        sDetailIds.put(R.id.detail_size, new int[]{5, 65535, 300});
        sDetailIds.put(R.id.detail_time, new int[]{3});
        sDetailIds.put(R.id.detail_location, new int[]{4});
        sDetailIds.put(R.id.detail_path, new int[]{200});
        sDetailIds.put(R.id.detail_duration, new int[]{8});
        sDetailIds.put(R.id.content_iso, new int[]{102});
        sDetailIds.put(R.id.content_s, new int[]{101});
        sDetailIds.put(R.id.content_af, new int[]{112});
        sDetailIds.put(R.id.image_light, new int[]{107, 111});
        sDetailIds.put(R.id.content_ev, new int[]{109});
        sDetailIds.put(R.id.content_f, new int[]{OfflineMapStatus.EXCEPTION_SDCARD, 104});
        sDetailIds.put(R.id.image_flash, new int[]{108});
        sDetailIds.put(R.id.image_meter, new int[]{110});
        sDetailIds.put(R.id.content_focus, new int[]{106, 105});
        sDetailCameraParamIds.add(new int[]{102});
        sDetailCameraParamIds.add(new int[]{101});
        sDetailCameraParamIds.add(new int[]{112});
        sDetailCameraParamIds.add(new int[]{107, 111});
        sDetailCameraParamIds.add(new int[]{109});
        sDetailCameraParamIds.add(new int[]{OfflineMapStatus.EXCEPTION_SDCARD, 104});
        sDetailCameraParamIds.add(new int[]{108});
        sDetailCameraParamIds.add(new int[]{110});
        sDetailCameraParamIds.add(new int[]{106, 105});
    }

    public void setDelegate(OnDetailLoadedDelegate delegate) {
        this.mDelegate = delegate;
    }

    public DetailLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.bg_info);
        this.custLayout = (HwCustDetailLayout) HwCustUtilsWrapper.createObj(HwCustDetailLayout.class, new Object[0]);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (getLayoutParams() instanceof LayoutParams) {
            LayoutParams lp = (LayoutParams) getLayoutParams();
            if (newConfig.orientation == 2) {
                lp.setMargins(0, 0, 0, 0);
                lp.removeRule(14);
                lp.addRule(13);
            } else {
                lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.detail_layout_margin_top), 0, 0);
                lp.removeRule(13);
                lp.addRule(14);
            }
            super.onConfigurationChanged(newConfig);
        }
    }

    public void setDetails(SparseArray<DetailItem> items) {
        setDetails(items, true);
    }

    public void setDetails(SparseArray<DetailItem> items, boolean updateBitmap) {
        if (sDetailIds != null) {
            onConfigurationChanged(getResources().getConfiguration());
            boolean cameraParamExist = false;
            for (int i = 0; i < sDetailIds.size(); i++) {
                int key_id = sDetailIds.keyAt(i);
                if (setText(key_id, (int[]) sDetailIds.get(key_id), items)) {
                    cameraParamExist = true;
                }
            }
            findViewById(R.id.detail_camerainfo).setVisibility(cameraParamExist ? 0 : 8);
            this.mScrollView = (ScrollView) findViewById(R.id.details_scroll);
            if (this.mScrollView != null) {
                this.mScrollView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (DetailLayout.this.mPhotoChanged && event.getAction() == 2 && v.getScrollY() >= 10) {
                            ReportToBigData.report(63);
                            DetailLayout.this.mPhotoChanged = false;
                        }
                        return false;
                    }
                });
            }
            if (updateBitmap) {
                drawHistogram();
            }
        }
    }

    public void onPhotoChanged() {
        this.mPhotoChanged = true;
    }

    private boolean setText(int keyId, int[] targetItems, SparseArray<DetailItem> srcItems) {
        View target = findViewById(keyId);
        if (target instanceof TextView) {
            Context context = getContext();
            TextView view = (TextView) findViewById(keyId);
            view.setVisibility(0);
            switch (keyId) {
                case R.id.detail_title:
                    view.setText(srcItems.get(targetItems[0], null) != null ? ((DetailItem) srcItems.get(targetItems[0])).mContent : this.mDelegate.getTitle());
                    if (this.mDelegate == null || this.mDelegate.getCustomDrawableId() < 0) {
                        return false;
                    }
                    view.setCompoundDrawablesWithIntrinsicBounds(0, 0, this.mDelegate.getCustomDrawableId(), 0);
                    return false;
                case R.id.detail_time:
                case R.id.detail_size:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(((DetailItem) srcItems.get(targetItems[0])).mContent);
                        return false;
                    }
                    view.setVisibility(8);
                    return false;
                case R.id.detail_resolution:
                    if (srcItems.get(targetItems[0], null) == null || srcItems.get(targetItems[1], null) == null) {
                        view.setVisibility(8);
                        return false;
                    }
                    view.setText(String.format("%sx%s", new Object[]{((DetailItem) srcItems.get(targetItems[0])).mContent, ((DetailItem) srcItems.get(targetItems[1])).mContent}));
                    return false;
                case R.id.detail_model:
                case R.id.detail_location:
                case R.id.detail_path:
                case R.id.detail_duration:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s %s", new Object[]{((DetailItem) srcItems.get(targetItems[0])).mTitle, ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        if (keyId != R.id.detail_model || this.custLayout == null || !this.custLayout.deleteModleInfo()) {
                            return false;
                        }
                        view.setVisibility(8);
                        return false;
                    }
                    view.setVisibility(8);
                    return false;
                case R.id.content_iso:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.iso_short), ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        return true;
                    }
                    view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.iso_short), DEFAULT_VALUE}));
                    return false;
                case R.id.content_s:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.exposure_time_short), ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        return true;
                    }
                    view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.exposure_time_short), DEFAULT_VALUE}));
                    return false;
                case R.id.content_af:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(((DetailItem) srcItems.get(targetItems[0])).mContent);
                        return true;
                    }
                    view.setText(DEFAULT_VALUE);
                    return false;
                case R.id.content_ev:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.exposure_bias_time), ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        return true;
                    }
                    view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.exposure_bias_time), DEFAULT_VALUE}));
                    return false;
                case R.id.content_f:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.aperture_short), ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        return true;
                    } else if (srcItems.get(targetItems[1], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.aperture_short), ((DetailItem) srcItems.get(targetItems[1])).mContent}));
                        return false;
                    } else {
                        view.setText(String.format("%s: %s", new Object[]{context.getString(R.string.aperture_short), DEFAULT_VALUE}));
                        return false;
                    }
                case R.id.content_focus:
                    if (srcItems.get(targetItems[0], null) != null) {
                        view.setText(String.format("%s: %s", new Object[]{getContext().getString(R.string.focal_length), ((DetailItem) srcItems.get(targetItems[0])).mContent}));
                        return true;
                    }
                    view.setVisibility(8);
                    return false;
                default:
                    CharSequence charSequence;
                    if (srcItems.get(targetItems[0], null) != null) {
                        charSequence = ((DetailItem) srcItems.get(targetItems[0])).mContent;
                    } else {
                        charSequence = "";
                    }
                    view.setText(charSequence);
                    return false;
            }
        } else if (!(target instanceof ImageView)) {
            return false;
        } else {
            ImageView image = (ImageView) target;
            image.setVisibility(0);
            switch (keyId) {
                case R.id.image_light:
                    int drawId = 0;
                    if (srcItems.get(targetItems[0], null) != null) {
                        if (targetItems[0] == 107) {
                            if (((DetailItem) srcItems.get(targetItems[0])).mDrawId == R.drawable.ic_btn_wb_auto) {
                                drawId = R.drawable.ic_btn_wb_auto;
                            } else if (srcItems.get(targetItems[1], null) != null) {
                                drawId = ((DetailItem) srcItems.get(targetItems[1])).mDrawId;
                            }
                        }
                        image.setImageResource(drawId);
                        return true;
                    }
                    image.setVisibility(4);
                    return false;
                case R.id.image_flash:
                case R.id.image_meter:
                    if (srcItems.get(targetItems[0], null) != null) {
                        image.setImageResource(((DetailItem) srcItems.get(targetItems[0], null)).mDrawId);
                        return true;
                    }
                    image.setVisibility(4);
                    return false;
                default:
                    return false;
            }
        }
    }

    private void drawHistogram() {
        HistogramView image = (HistogramView) findViewById(R.id.image_hist);
        if (image != null && this.mDelegate != null) {
            image.setBitmap(this.mDelegate.getCurrentBitmap());
        }
    }
}
