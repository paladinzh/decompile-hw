package com.huawei.gallery.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.DataAdapterProxy;
import com.huawei.gallery.util.UIUtils;

public class ListAlbumBaseDataAdapter extends BaseAdapter implements DataAdapterProxy {
    protected Activity mContext;
    protected AlbumSetSlidingWindow mDataWindow;
    protected LayoutInflater mInflater;
    protected ListView mListView;
    protected AlbumSetDataLoader mSource;

    public int getCount() {
        return this.mSource.size();
    }

    public Object getItem(int position) {
        return this.mSource.getMediaSet(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int size = getCount();
        if (position >= size) {
            GalleryLog.d("ListAlbumBaseDataAdapter", "Invalid view position : " + position + ", Actual size is : " + size);
            return null;
        } else if (this.mListView == null) {
            GalleryLog.w("ListAlbumBaseDataAdapter", " mListView is never initialized ");
            return null;
        } else {
            this.mDataWindow.setActiveWindow(Math.max(0, this.mListView.getFirstVisiblePosition() - 1), Math.min(this.mListView.getLastVisiblePosition() + 2, size));
            boolean isValid = this.mSource.isContentValid(position);
            convertView = initConvertViewAtGetView(convertView, parent);
            if (isValid) {
                setLayoutInfo(convertView, position);
            }
            return convertView;
        }
    }

    protected View initConvertViewAtGetView(View convertView, ViewGroup parent) {
        if (convertView != null) {
            return convertView;
        }
        convertView = this.mInflater.inflate(getListViewLayoutId(), parent, false);
        UIUtils.addStateListAnimation(convertView, this.mContext);
        return convertView;
    }

    protected int getListViewLayoutId() {
        return 0;
    }

    protected void setLayoutInfo(View convertView, int position) {
    }

    public void updateView(int position) {
        if (position >= getCount() || this.mListView == null) {
            GalleryLog.d("ListAlbumBaseDataAdapter", " Invalid view position : " + position + ", Actual size is : " + getCount());
            return;
        }
        int firstVisiblePosition = this.mListView.getFirstVisiblePosition();
        int lastVisiblePosition = this.mListView.getLastVisiblePosition();
        if (firstVisiblePosition <= position && position <= lastVisiblePosition) {
            View view = this.mListView.getChildAt(position - firstVisiblePosition);
            if (view != null) {
                setLayoutInfo(view, position);
            }
        }
    }

    public void resume() {
        this.mDataWindow.resume();
    }

    public void pause() {
        this.mDataWindow.pause();
    }
}
