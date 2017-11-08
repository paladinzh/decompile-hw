package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;
import android.text.TextUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchContactsCursor;
import com.android.contacts.list.ContactEntryListFragment.ContactsSearchLoader;
import com.android.contacts.util.HwLog;
import com.google.common.collect.Lists;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.HideRowsCursor;
import java.util.List;

public class ProfileAndContactsLoader extends CursorLoader implements ContactsSearchLoader {
    private ContactsApplication mContactsApp = null;
    private boolean mHasProfile;
    private boolean mIsLoadStar;
    private boolean mLoadProfile;
    private String[] mProjection;
    private String mQueryString;
    private String mSelection;

    public void cancelLoadInBackground() {
        if (HwLog.HWDBG) {
            HwLog.d("ProfileAndContactsLoader", "cancelLoadInBackground");
        }
        super.cancelLoadInBackground();
    }

    public void deliverResult(Cursor cursor) {
        if (HwLog.HWDBG) {
            HwLog.d("ProfileAndContactsLoader", "deliverResult");
        }
        super.deliverResult(cursor);
    }

    public void stopLoading() {
        if (HwLog.HWDBG) {
            HwLog.d("ProfileAndContactsLoader", "stopLoading");
        }
        super.stopLoading();
    }

    public ProfileAndContactsLoader(Context context, ContactsApplication app) {
        super(context);
        this.mContactsApp = app;
    }

    public ProfileAndContactsLoader(Context context) {
        super(context);
    }

    public void setLoadStar(boolean flag) {
        this.mIsLoadStar = flag;
    }

    public void setLoadProfile(boolean flag) {
        this.mLoadProfile = flag;
    }

    public void setProjection(String[] projection) {
        super.setProjection(projection);
        if (projection == null) {
            this.mProjection = null;
        } else {
            this.mProjection = (String[]) projection.clone();
        }
    }

    public void setSelection(String selection) {
        this.mSelection = selection;
        super.setSelection(selection);
    }

    public String getSelection() {
        return this.mSelection;
    }

    public void setQueryString(String queryStr) {
        this.mQueryString = queryStr;
    }

    public String getQueryString() {
        return this.mQueryString;
    }

    public Cursor loadInBackground() {
        Cursor starCursor = null;
        HwLog.d("ProfileAndContactsLoader", "loadInBackground begin");
        long loadTime = 0;
        if (PLog.DEBUG) {
            loadTime = System.currentTimeMillis();
        }
        if (TextUtils.isEmpty(this.mQueryString)) {
            Cursor contactsCursor;
            List<Cursor> cursors = Lists.newArrayList();
            if (CommonUtilMethods.getIsLiteFeatureProducts() && this.mContactsApp != null && this.mContactsApp.getIsFirstStartContacts()) {
                synchronized (CommonUtilMethods.mContactCursorLoad) {
                    Cursor mContactsDataCursor = this.mContactsApp.getPreLoadContactsCursor();
                    if (mContactsDataCursor == null || mContactsDataCursor.isClosed()) {
                        contactsCursor = super.loadInBackground();
                    } else {
                        contactsCursor = mContactsDataCursor;
                    }
                }
            } else {
                contactsCursor = super.loadInBackground();
            }
            if (this.mLoadProfile && QueryUtil.isSystemAppForContacts()) {
                cursors.add(loadProfile());
            } else {
                this.mHasProfile = doesProfileExists();
            }
            if (this.mIsLoadStar && ((long) getId()) == 0) {
                starCursor = loadStarContacts();
            }
            if (starCursor != null) {
                cursors.add(starCursor);
            }
            cursors.add(contactsCursor);
            Cursor cursor = new MergeCursor((Cursor[]) cursors.toArray(new Cursor[cursors.size()])) {
                public Bundle getExtras() {
                    if (contactsCursor == null) {
                        return null;
                    }
                    Bundle b = contactsCursor.getExtras();
                    if (b == null) {
                        return null;
                    }
                    try {
                        b.putBoolean("has_profile", ProfileAndContactsLoader.this.mHasProfile);
                        if (b.containsKey("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES") && starCursor != null && starCursor.getCount() > 0) {
                            String[] sections = b.getStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES");
                            if (sections.length > 0 && sections[0].equals("☆")) {
                                return b;
                            }
                            String[] newSections = new String[(sections.length + 1)];
                            newSections[0] = "☆";
                            System.arraycopy(sections, 0, newSections, 1, sections.length);
                            b.putStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES", newSections);
                            int[] counts = b.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS");
                            int[] newCounts = new int[(counts.length + 1)];
                            newCounts[0] = starCursor.getCount();
                            System.arraycopy(counts, 0, newCounts, 1, counts.length);
                            b.putIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS", newCounts);
                        }
                        return b;
                    } catch (NullPointerException e) {
                        HwLog.w("ProfileAndContactsLoader", "concurrent exception happen to cause null pointer");
                        return null;
                    }
                }
            };
            if (PLog.DEBUG) {
                PLog.d(0, "ProfileAndContactsLoader loadInBackground, cost = " + (System.currentTimeMillis() - loadTime));
            }
            return new HideRowsCursor(cursor);
        }
        cursor = super.loadInBackground();
        if (cursor == null) {
            cursor = new MatrixCursor(this.mProjection);
            HwLog.i("ProfileAndContactsLoader", "loadInBackground,seach mode,return cursor is null");
        }
        if (QueryUtil.isUseHwSearch()) {
            if (PLog.DEBUG) {
                PLog.d(0, "ProfileAndContactsLoader loadInBackground, cost = " + (System.currentTimeMillis() - loadTime));
            }
            return new HwSearchContactsCursor(cursor);
        }
        if (PLog.DEBUG) {
            PLog.d(0, "ProfileAndContactsLoader loadInBackground, cost = " + (System.currentTimeMillis() - loadTime));
        }
        return cursor;
    }

    private MatrixCursor loadProfile() {
        Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, this.mProjection, null, null, null);
        try {
            MatrixCursor matrix = new MatrixCursor(this.mProjection);
            if (cursor == null) {
                return matrix;
            }
            Object[] row = new Object[this.mProjection.length];
            while (cursor.moveToNext()) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = cursor.getString(i);
                }
                matrix.addRow(row);
            }
            if (cursor != null) {
                cursor.close();
            }
            return matrix;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Cursor loadStarContacts() {
        if (!EmuiFeatureManager.isSimpleSectionEnable()) {
            return null;
        }
        String selection = this.mSelection;
        if (TextUtils.isEmpty(selection)) {
            selection = "starred = 1";
        } else {
            selection = selection + " AND " + "starred" + " = 1";
        }
        return getContext().getContentResolver().query(getUri(), getProjection(), selection, getSelectionArgs(), getSortOrder());
    }

    private boolean doesProfileExists() {
        Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
        if (cursor == null) {
            return false;
        }
        try {
            if (cursor.moveToFirst()) {
                return true;
            }
            cursor.close();
            return false;
        } finally {
            cursor.close();
        }
    }
}
