package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.PhotoShareDownUpSlidingWindow.ItemsEntry;
import com.huawei.gallery.app.PhotoShareDownUpSlidingWindow.Listener;
import com.huawei.gallery.util.ImmersionUtils;
import com.huawei.gallery.util.UIUtils;
import java.util.Locale;

public class PhotoShareDownUpDataAdapter extends BaseAdapter implements Listener {
    private final Context mContext;
    private final LayoutInflater mInflater = ((LayoutInflater) this.mContext.getSystemService("layout_inflater"));
    private ListView mListView;
    private ClickListener mListener;
    private final SelectionManager mSelectionManager;
    private PhotoShareDownUpSlidingWindow mSource;

    public interface ClickListener {
        void onClick(int i, int i2);

        void updateTitle();
    }

    public PhotoShareDownUpDataAdapter(Activity context, PhotoShareDownUpSlidingWindow source, SelectionManager selectionManager) {
        this.mContext = context;
        this.mSource = source;
        this.mSource.setListener(this);
        this.mSelectionManager = selectionManager;
    }

    public void setListener(ClickListener listener) {
        this.mListener = listener;
    }

    public void setView(ListView view) {
        this.mListView = view;
    }

    public int getCount() {
        return this.mSource.getCount();
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
        int size = getCount();
        if (position >= size) {
            GalleryLog.d("PhotoShareDownUpDataAdapter", "Invalid view position : " + position + ",size is : " + size);
            return null;
        } else if (this.mListView == null) {
            GalleryLog.w("PhotoShareDownUpDataAdapter", " mListView is never initialized ");
            return null;
        } else {
            this.mSource.setActiveWindow(Math.max(0, this.mListView.getFirstVisiblePosition() - 1), Math.min(this.mListView.getLastVisiblePosition() + 2, size));
            if (view == null) {
                view = this.mInflater.inflate(R.layout.photoshare_down_up_fragment_items, viewGroup, false);
                decorateProgressBar(view);
                UIUtils.addStateListAnimation(view, this.mContext);
            }
            updateView(view, position);
            return view;
        }
    }

    private void updateView(int position) {
        if (position >= getCount() || this.mListView == null) {
            GalleryLog.d("PhotoShareDownUpDataAdapter", " Invalid view position : " + position + ",size is : " + getCount());
            return;
        }
        int firstVisiblePosition = this.mListView.getFirstVisiblePosition();
        int lastVisiblePosition = this.mListView.getLastVisiblePosition();
        if (firstVisiblePosition <= position && position <= lastVisiblePosition) {
            View view = this.mListView.getChildAt(position - firstVisiblePosition);
            if (view != null) {
                updateView(view, position);
            }
        }
    }

    private void updateView(View view, int position) {
        ItemsEntry info = this.mSource.getItem(position);
        if (info != null) {
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView name = (TextView) view.findViewById(R.id.title);
            TextView content = (TextView) view.findViewById(R.id.content);
            View button_progress = view.findViewById(R.id.button_progress);
            ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
            TextView button = (TextView) view.findViewById(R.id.button);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.list_checkbox);
            updateIcon(icon, info.icon, info.rotation);
            updateTitle(name, info.fileName);
            updateContent(content, info.time, info.fileSize);
            updateProgressButton(button_progress, info.state, position);
            updateProgress(progress, info.state, info.percent);
            updateStateButton(button, info.state, info.percent);
            updateCheckBox(checkBox, position);
        }
    }

    private void updateCheckBox(CheckBox checkBox, int position) {
        if (this.mSelectionManager.inSelectionMode()) {
            ItemsEntry info = this.mSource.getItem(position);
            if (info != null && info.mediaItem != null) {
                final MediaItem mediaSet = info.mediaItem;
                checkBox.setVisibility(0);
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        PhotoShareDownUpDataAdapter.this.mSelectionManager.toggle(mediaSet.getPath());
                    }
                });
                if (this.mSelectionManager.isItemSelected(mediaSet.getPath())) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            } else {
                return;
            }
        }
        checkBox.setVisibility(8);
        checkBox.setChecked(false);
    }

    private void updateIcon(ImageView view, Bitmap icon, int rotate) {
        if (icon != null) {
            view.setImageBitmap(icon);
            view.setRotation((float) rotate);
        }
    }

    private void updateTitle(TextView view, String name) {
        if (name != null) {
            view.setText(name);
        }
    }

    private void updateContent(TextView view, long time, long size) {
        StringBuilder timeSize = new StringBuilder();
        if (0 <= time) {
            timeSize.append(GalleryUtils.getSettingFormatDateTime(this.mContext, time));
        }
        if (0 <= size) {
            timeSize.append("  ");
            timeSize.append(Formatter.formatFileSize(this.mContext, size));
        }
        view.setText(timeSize.toString());
    }

    private void updateProgressButton(View view, final int state, final int position) {
        if (this.mSelectionManager.inSelectionMode() || state == 16) {
            view.setVisibility(8);
            return;
        }
        view.setVisibility(0);
        if (state != 16) {
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (PhotoShareDownUpDataAdapter.this.mListener != null) {
                        PhotoShareDownUpDataAdapter.this.mListener.onClick(position, state);
                    }
                }
            });
        }
    }

    private void updateProgress(ProgressBar view, int state, int percent) {
        if (this.mSelectionManager.inSelectionMode() || 1 != state) {
            view.setVisibility(8);
            return;
        }
        view.setVisibility(0);
        view.setProgress(percent);
    }

    private void updateStateButton(TextView view, int state, int percent) {
        if (this.mSelectionManager.inSelectionMode()) {
            view.setVisibility(8);
            return;
        }
        Locale defloc = Locale.getDefault();
        switch (state) {
            case 1:
                view.setText(String.format("%d%%", new Object[]{Integer.valueOf(percent)}));
                view.setTextColor(-16744961);
                view.setVisibility(0);
                break;
            case 2:
                view.setText(this.mContext.getString(R.string.photoshare_down_up_wait).toUpperCase(defloc));
                view.setTextColor(-16744961);
                view.setVisibility(0);
                break;
            case 4:
            case 8:
                view.setText(this.mContext.getString(R.string.photoshare_statusbar_continue).toUpperCase(defloc));
                view.setTextColor(-16744961);
                view.setVisibility(0);
                break;
            case 16:
                view.setVisibility(8);
                break;
            case 32:
                view.setText(this.mContext.getString(R.string.photoshare_down_up_redo).toUpperCase(defloc));
                view.setTextColor(-52448);
                view.setVisibility(0);
                break;
            default:
                GalleryLog.w("PhotoShareDownUpDataAdapter", "the state is error:" + state);
                break;
        }
    }

    public void onSizeChanged(int size) {
        notifyDataSetChanged();
        if (this.mListener != null) {
            this.mListener.updateTitle();
        }
    }

    public void onContentChanged(int index) {
        updateView(index);
    }

    private void decorateProgressBar(View view) {
        int color = ImmersionUtils.getControlColor(this.mContext);
        if (color != 0) {
            try {
                ClipDrawable clipDrawable = (ClipDrawable) ((LayerDrawable) ((ProgressBar) view.findViewById(R.id.progress)).getProgressDrawable()).findDrawableByLayerId(16908301);
                if (clipDrawable != null) {
                    ((GradientDrawable) clipDrawable.getDrawable()).setColor(color);
                }
            } catch (RuntimeException e) {
                GalleryLog.e("PhotoShareDownUpDataAdapter", "decorateProgressBar exception:" + e);
            }
        }
    }
}
