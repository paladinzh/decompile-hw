package com.android.settings.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

public class GlobalSearchProvider extends ContentProvider {
    private static final String[] SEARCH_SUGGESTIONS_COLUMNS = new String[]{"suggest_text_1", "suggest_text_3", "suggest_intent_data", "suggest_intent_extra_data"};

    private static class GlobalSearchDetail {
        String intentData;
        String intentExtraData;
        String text1;
        String text2;

        private GlobalSearchDetail() {
        }

        public ArrayList<?> asList() {
            ArrayList<Object> list = new ArrayList();
            list.add(this.text1);
            list.add(this.text2);
            list.add(this.intentData);
            list.add(this.intentExtraData);
            return list;
        }

        public void reset() {
            this.text1 = "";
            this.text2 = "";
            this.intentData = null;
            this.intentExtraData = "";
        }

        public String toString() {
            return "text1:" + this.text1 + "; text2:" + this.text2 + "; intentData:" + this.intentData;
        }
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String key = "";
        if (selectionArgs == null || selectionArgs.length <= 0) {
            Log.e("GlobalSearchProvider", "query->parse key failed!");
        } else {
            key = selectionArgs[0];
        }
        return handleGlobalSearch(uri, key);
    }

    private Cursor handleGlobalSearch(Uri uri, String key) {
        MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGESTIONS_COLUMNS);
        Cursor cursor2 = null;
        try {
            cursor2 = Index.getInstance(getContext()).search(key);
            GlobalSearchDetail detailItem = new GlobalSearchDetail();
            while (cursor2.moveToNext()) {
                String title = cursor2.getString(1);
                String summaryOn = cursor2.getString(2);
                String summaryOff = cursor2.getString(3);
                String entries = cursor2.getString(4);
                String className = cursor2.getString(6);
                String targetClassName = cursor2.getString(11);
                String packageName = cursor2.getString(10);
                String keyIndex = cursor2.getString(13);
                String action = cursor2.getString(9);
                String summary = "";
                if (!TextUtils.isEmpty(summaryOn)) {
                    summary = summaryOn;
                } else if (!TextUtils.isEmpty(summaryOff)) {
                    summary = summaryOff;
                } else if (TextUtils.isEmpty(entries)) {
                    summary = "";
                } else {
                    summary = entries;
                }
                detailItem.text1 = title;
                detailItem.text2 = summary;
                Intent intent = new Intent();
                intent.putExtra("classname", className);
                intent.putExtra("targetclassname", targetClassName);
                intent.putExtra("packagename", packageName);
                intent.putExtra("action", action);
                intent.putExtra("keyIndex", keyIndex);
                detailItem.intentExtraData = intent.toUri(0);
                cursor.addRow(detailItem.asList());
                detailItem.reset();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Exception ex) {
            Log.e("GlobalSearchProvider", "handleGlobalSearch->ex:" + ex);
            return cursor;
        } finally {
            if (cursor2 != null) {
                cursor2.close();
            }
        }
        return cursor;
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }
}
