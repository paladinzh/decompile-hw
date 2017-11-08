package com.huawei.gallery.editor.category;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.fx.FilterChangableParameter;
import com.huawei.gallery.util.ColorfulUtils;

public class SeekBarManager {
    private SeekBarAdapter mAdapter;
    private boolean mIsVisible = false;
    private OnSeekBarChangeListener mListener;
    public android.widget.SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new android.widget.SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            int progress = (int) ((((float) seekBar.getProgress()) / ((float) seekBar.getMax())) * 100.0f);
            if (SeekBarManager.this.mListener != null) {
                SeekBarManager.this.mListener.onProgressChanged(((Integer) seekBar.getTag()).intValue(), progress);
            }
            if (GalleryUtils.isCVAAMode()) {
                SeekBarManager.this.setDescription(seekBar);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (SeekBarManager.this.mListener != null) {
                SeekBarManager.this.mListener.onStopTrackingTouch(seekBar);
            }
        }
    };
    private VolatileViewTrack mTrack;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(int i, int i2);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    private class SeekBarAdapter extends BaseViewAdapter {
        private Context mContext;
        private SparseIntArray mItems;
        private FilterChangableParameter mParameter;

        public SeekBarAdapter(SparseIntArray items, Context context, FilterChangableParameter parameter) {
            super(context);
            this.mItems = items;
            this.mContext = context;
            this.mParameter = parameter;
        }

        public void setItems(SparseIntArray items, FilterChangableParameter parameter) {
            this.mItems = items;
            this.mParameter = parameter;
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.mItems.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                int darkThemeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
                this.mContext.getTheme().applyStyle(this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null), true);
                if (this.mContext.getResources().getConfiguration().orientation == 1) {
                    view = LayoutInflater.from(this.mContext).inflate(R.layout.editor_filter_seekbar_item, parent, false);
                } else {
                    view = LayoutInflater.from(this.mContext).inflate(R.layout.editor_filter_seekbar_item_land, parent, false);
                }
                this.mContext.getTheme().applyStyle(darkThemeId, true);
            } else {
                view = convertView;
            }
            int key = this.mItems.keyAt(position);
            ((TextView) view.findViewById(R.id.filter_seekbar_text)).setText(this.mItems.get(key));
            SeekBar seekBar = (SeekBar) view.findViewById(R.id.filter_seekbar);
            seekBar.setFocusable(false);
            seekBar.setTag(Integer.valueOf(key));
            seekBar.setOnSeekBarChangeListener(null);
            seekBar.setProgress(this.mParameter.getOneParameter(key));
            seekBar.setOnSeekBarChangeListener(SeekBarManager.this.mOnSeekBarChangeListener);
            ColorfulUtils.decorateColorfulForSeekbar(this.mContext, seekBar);
            view.setTag(Integer.valueOf(key));
            view.setContentDescription(this.mContext.getString(this.mItems.get(key)) + seekBar.getProgress() + "%");
            return view;
        }

        public String getContentDescription(SeekBar seekBar) {
            return this.mContext.getString(this.mItems.get(((Integer) seekBar.getTag()).intValue())) + seekBar.getProgress() + "%";
        }
    }

    public SeekBarManager(VolatileViewTrack track) {
        this.mTrack = track;
    }

    public void reloadedListView(SparseIntArray seekbarItems, FilterChangableParameter parameter) {
        if (this.mTrack != null) {
            if (this.mAdapter == null) {
                this.mAdapter = new SeekBarAdapter(seekbarItems, this.mTrack.getContext(), parameter);
                this.mTrack.setAdapter(this.mAdapter);
                this.mAdapter.setContainer(this.mTrack);
            } else {
                this.mAdapter.setItems(seekbarItems, parameter);
            }
        }
    }

    public void saveUIController(TransitionStore store) {
        store.put("seekbar_adapter", this.mAdapter);
        store.put("seekbar_visible", Boolean.valueOf(this.mIsVisible));
    }

    public void restoreUIController(TransitionStore store) {
        Object obj = store.get("seekbar_adapter");
        if (obj instanceof SeekBarAdapter) {
            this.mAdapter = (SeekBarAdapter) obj;
            this.mTrack.setAdapter(this.mAdapter);
            this.mAdapter.setContainer(this.mTrack);
        }
        Object visible = store.get("seekbar_visible");
        if (!(visible instanceof Boolean)) {
            return;
        }
        if (((Boolean) visible).booleanValue()) {
            show();
        } else {
            hide();
        }
    }

    public void show() {
        this.mTrack.setVisibility(0);
        this.mIsVisible = true;
    }

    public void hide() {
        this.mTrack.setVisibility(4);
        this.mIsVisible = false;
    }

    public boolean isVisible() {
        return this.mIsVisible;
    }

    public void setListener(OnSeekBarChangeListener listener) {
        this.mListener = listener;
    }

    private void setDescription(SeekBar seekBar) {
        for (int i = 0; i < this.mTrack.getChildCount(); i++) {
            View view = this.mTrack.getChildAt(i);
            if (view.getTag().equals(seekBar.getTag()) && this.mAdapter != null) {
                view.setContentDescription(this.mAdapter.getContentDescription(seekBar));
            }
        }
    }
}
