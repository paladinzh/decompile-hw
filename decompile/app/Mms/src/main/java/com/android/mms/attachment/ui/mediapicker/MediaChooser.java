package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.mms.attachment.datamodel.binding.ImmutableBindingRef;
import com.android.mms.attachment.datamodel.data.MediaPickerData;
import com.android.mms.attachment.ui.BasePagerViewHolder;
import com.google.android.gms.R;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.util.StatisticalHelper;

public abstract class MediaChooser extends BasePagerViewHolder {
    protected final ImmutableBindingRef<MediaPickerData> mBindingRef;
    protected final MediaPicker mMediaPicker;
    protected boolean mOpen;
    protected boolean mSelected = false;
    private ImageView mTabIcon;
    private TextView mTabText;
    private View mTabView;

    protected abstract int getActionBarTitleResId();

    protected abstract int getIconResource();

    protected abstract int getIconTextResource();

    public abstract int getSupportedMediaTypes();

    protected abstract void onRestoreChooserState();

    public MediaChooser(MediaPicker mediaPicker) {
        this.mMediaPicker = mediaPicker;
        this.mBindingRef = mediaPicker.getMediaPickerDataBinding();
    }

    protected void setSelected(boolean selected) {
        refreshTabButton(selected);
    }

    View getTabButton() {
        return this.mTabView;
    }

    private void refreshTabButton(boolean selected) {
        this.mSelected = selected;
        if (selected) {
            this.mOpen = true;
        }
        if (this.mTabIcon != null) {
            this.mTabIcon.setImageResource(getIconResource());
        }
        if (this.mTabText != null) {
            int color;
            TextView textView = this.mTabText;
            if (selected) {
                color = getContext().getResources().getColor(R.color.attahcment_tab_text_selected);
            } else {
                color = getContext().getResources().getColor(R.color.attahcment_tab_text_unselected);
            }
            textView.setTextColor(color);
        }
    }

    void onCreateTabButton(LayoutInflater inflater, ViewGroup parent) {
        this.mTabView = inflater.inflate(R.layout.mediapicker_tab_button, parent, false);
        this.mTabIcon = (ImageView) this.mTabView.findViewById(R.id.attachment_tab_button);
        this.mTabText = (TextView) this.mTabView.findViewById(R.id.attachment_tab_text);
        this.mTabText.setText(getContext().getResources().getString(getIconTextResource()));
        refreshTabButton(this.mSelected);
        this.mTabView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MediaChooser.this.mMediaPicker.selectChooser(MediaChooser.this);
                if (MediaChooser.this instanceof CameraMediaChooser) {
                    StatisticalHelper.incrementReportCount(MediaChooser.this.getContext(), 2094);
                } else if (MediaChooser.this instanceof GalleryMediaChooser) {
                    StatisticalHelper.incrementReportCount(MediaChooser.this.getContext(), 2095);
                } else if (MediaChooser.this instanceof AudioMediaChooser) {
                    StatisticalHelper.incrementReportCount(MediaChooser.this.getContext(), AMapException.CODE_AMAP_NEARBY_KEY_NOT_BIND);
                } else {
                    StatisticalHelper.incrementReportCount(MediaChooser.this.getContext(), 2264);
                }
            }
        });
    }

    protected Context getContext() {
        return this.mMediaPicker.getActivity();
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }

    protected void onFullScreenChanged(boolean fullScreen) {
    }

    protected void onOpenedChanged(boolean open) {
        this.mOpen = open;
    }

    protected void updateActionBar(AbstractEmuiActionBar actionBar) {
    }

    public boolean canSwipeDown() {
        return false;
    }

    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public boolean isHandlingTouch() {
        return false;
    }

    public void stopTouchHandling() {
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void onStop() {
    }

    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }
}
