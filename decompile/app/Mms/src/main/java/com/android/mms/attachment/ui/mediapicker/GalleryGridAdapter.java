package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import com.android.mms.attachment.ui.mediapicker.GalleryGridItemView.HostInterface;
import com.google.android.gms.R;

public class GalleryGridAdapter extends CursorAdapter {
    private HostInterface mGgivHostInterface;

    public GalleryGridAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    public void setHostInterface(HostInterface ggivHostInterface) {
        this.mGgivHostInterface = ggivHostInterface;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ((GalleryGridItemView) view).bind(cursor, this.mGgivHostInterface);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.gallery_grid_item_view, parent, false);
    }
}
