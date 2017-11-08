package com.android.contacts.hap.list;

import android.content.Context;
import android.net.Uri;
import com.android.contacts.list.ChildListItemView;

public class HwCustFavoritesAndGroupsAdapter {
    protected static final String TAG = "HwCustFavoritesAndGroupsAdapter";
    Context mContext;

    public HwCustFavoritesAndGroupsAdapter(Context context) {
        this.mContext = context;
    }

    public boolean getEnableEmailContactInMms() {
        return false;
    }

    public String getChildSelection() {
        return "";
    }

    public void initDataList() {
    }

    public boolean isInDataList(long dataId) {
        return false;
    }

    public void updateGroupCheckBoxStateForCustomizations(Context context, String number, ChildListItemView childView, Uri dataUri) {
    }

    public boolean isIgnoreChildClick(Context context, Uri uri) {
        return false;
    }

    public void initService(Context ctx) {
    }

    public String getChildCursorSelection(int mFilterType, boolean mHasFavourites, int groupPosition) {
        return null;
    }

    public StringBuffer appendSbForRcs() {
        return null;
    }

    public boolean isRcsEnable() {
        return false;
    }
}
