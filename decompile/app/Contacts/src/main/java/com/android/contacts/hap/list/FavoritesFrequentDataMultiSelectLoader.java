package com.android.contacts.hap.list;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchContactsCursor;
import com.android.contacts.util.HwLog;
import com.google.common.collect.Lists;
import java.util.List;

public class FavoritesFrequentDataMultiSelectLoader extends ContactMultiselectListLoader {
    private String[] mFrequentProjection = getProjection();
    private String mFrequentSelection;
    private String[] mFrequentSelectionArgs;
    private String mFrequentSortOrder;
    private Uri mFrequentUri;
    private boolean mIsLoadFrequent = true;

    private static class StrequentCurosr extends MergeCursor {
        private Bundle mExtras;
        private int mFavoritesCount;
        private int mFrequentCount;

        public StrequentCurosr(Cursor[] cursors, Bundle extras, int favoritesCount, int frequentCount) {
            super(cursors);
            this.mExtras = extras;
            this.mFavoritesCount = favoritesCount;
            this.mFrequentCount = frequentCount;
        }

        public Bundle getExtras() {
            if (this.mExtras == null) {
                this.mExtras = new Bundle();
            }
            this.mExtras.putInt("favorites_count", this.mFavoritesCount);
            this.mExtras.putInt("frequent_count", this.mFrequentCount);
            return this.mExtras;
        }
    }

    public FavoritesFrequentDataMultiSelectLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public void setSortOrder(String sortOrder) {
        super.setSortOrder(sortOrder);
    }

    public Cursor loadInBackground() {
        List<Cursor> cursors = Lists.newArrayList();
        Cursor favoritesCursor = super.loadInBackground();
        int favoritesCount = 0;
        if (favoritesCursor != null) {
            try {
                favoritesCount = favoritesCursor.getCount();
            } catch (NullPointerException e) {
                HwLog.w("FavoritesFrequentDataMultiSelectLoader", "loadInBackground:" + e.getMessage());
            }
        } else {
            favoritesCount = 0;
        }
        if (favoritesCursor != null) {
            cursors.add(favoritesCursor);
        }
        Cursor frequentCursor = loadFrequent();
        int frequentCount = frequentCursor != null ? frequentCursor.getCount() : 0;
        if (frequentCursor != null) {
            cursors.add(frequentCursor);
        }
        if (HwLog.HWDBG) {
            HwLog.d("FavoritesFrequentDataMultiSelectLoader", "favoritesCount" + favoritesCount + ",frequentCount=" + frequentCount);
        }
        if (cursors.size() == 0) {
            return null;
        }
        Bundle extras = null;
        if (favoritesCursor != null) {
            try {
                extras = favoritesCursor.getExtras();
            } catch (NullPointerException e2) {
                HwLog.w("FavoritesFrequentDataMultiSelectLoader", "loadInBackground:" + e2.getMessage());
            }
        } else {
            extras = null;
        }
        Cursor resultCursor = new StrequentCurosr((Cursor[]) cursors.toArray(new Cursor[cursors.size()]), extras, favoritesCount, frequentCount);
        if (TextUtils.isEmpty(getQueryString()) || !QueryUtil.isUseHwSearch()) {
            return resultCursor;
        }
        return new HwSearchContactsCursor(resultCursor);
    }

    private Cursor loadFrequent() {
        if (!this.mIsLoadFrequent || this.mFrequentUri == null) {
            return null;
        }
        return getContext().getContentResolver().query(this.mFrequentUri, this.mFrequentProjection, this.mFrequentSelection, this.mFrequentSelectionArgs, this.mFrequentSortOrder);
    }

    public void setFrequentProjection(String[] projection) {
        this.mFrequentProjection = (String[]) projection.clone();
    }

    public void setFrequentUri(Uri uri) {
        this.mFrequentUri = uri;
    }

    public void setFrequentSelection(String selection) {
        this.mFrequentSelection = selection;
    }

    public void setFrequentSelectionArgs(String[] args) {
        if (args != null) {
            this.mFrequentSelectionArgs = (String[]) args.clone();
        }
    }

    public void setFrequentSortOrder(String sortOrder) {
        this.mFrequentSortOrder = sortOrder;
    }

    public void setWetherLoadFrequent(boolean isLoad) {
        this.mIsLoadFrequent = isLoad;
    }
}
