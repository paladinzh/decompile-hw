package com.android.contacts.hap.hwsearch;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.text.TextUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.location.places.Place;

public abstract class HwSearchCursor extends CursorWrapper {
    protected Cursor mCursor;

    public static final class HwSearchContactsCursor extends HwSearchCursor {
        public HwSearchContactsCursor(Cursor cursor) {
            super(cursor);
        }

        public int getMatchType() {
            int columnIndex = this.mCursor.getColumnIndex("search_result");
            if (columnIndex < 0) {
                return -1;
            }
            return HwSearchCursor.shiftMatchType(this.mCursor.getLong(columnIndex));
        }

        public int getMatchPosition() {
            int columnIndex = this.mCursor.getColumnIndex("search_result");
            if (columnIndex < 0) {
                return -1;
            }
            return HwSearchCursor.shiftMatchPosition(this.mCursor.getLong(columnIndex));
        }

        public int[] getMatchInfoArray(int position) {
            int matchInfoIndex = position;
            int columnIndex = this.mCursor.getColumnIndex("search_result");
            if (columnIndex < 0) {
                return new int[0];
            }
            byte[] blobResult = this.mCursor.getBlob(columnIndex);
            if (blobResult == null) {
                return new int[0];
            }
            int[] matchInfoArray = new int[(blobResult.length / 8)];
            for (int i = 0; i < blobResult.length / 8; i++) {
                matchInfoArray[i] = blobResult[(i * 8) + position];
            }
            return matchInfoArray;
        }
    }

    public static final class HwSearchDialerCursor extends HwSearchCursor {
        private int mEnterPriseCount;
        private int mEnterpriseOffset;
        private int mYellowPageCount;
        private int mYellowPageOffset;

        public HwSearchDialerCursor(Cursor cursor, int yellowPageOffset, int yellowPageCount, int enterpriseOffset, int enterpriseCount) {
            super(cursor);
            this.mYellowPageOffset = yellowPageOffset;
            this.mYellowPageCount = yellowPageCount;
            this.mEnterpriseOffset = enterpriseOffset;
            this.mEnterPriseCount = enterpriseCount;
        }

        public int getMatchType() {
            int columnIndex = this.mCursor.getColumnIndexOrThrow("search_result");
            if (columnIndex < 0) {
                return -1;
            }
            int type = HwSearchCursor.shiftMatchType(this.mCursor.getLong(columnIndex));
            switch (type) {
                case 32:
                case Place.TYPE_FURNITURE_STORE /*40*/:
                    type = 2;
                    break;
                case Place.TYPE_HINDU_TEMPLE /*48*/:
                    type = 0;
                    break;
                case Place.TYPE_LIQUOR_STORE /*56*/:
                case Place.TYPE_LOCAL_GOVERNMENT_OFFICE /*57*/:
                case Place.TYPE_LOCKSMITH /*58*/:
                case Place.TYPE_LODGING /*59*/:
                case Place.TYPE_MOSQUE /*62*/:
                case Place.TYPE_MOVIE_RENTAL /*63*/:
                    type = -1;
                    break;
                case Place.TYPE_MEAL_DELIVERY /*60*/:
                    type = -2;
                    break;
                case Place.TYPE_MEAL_TAKEAWAY /*61*/:
                    type = -3;
                    break;
            }
            return type;
        }

        public int getMatchPosition() {
            return 0;
        }

        public int[] getMatchInfoArray(int position) {
            return new int[]{-1};
        }

        public int getYellowPageFirstPosition() {
            return this.mYellowPageCount > 0 ? this.mYellowPageOffset : Integer.MAX_VALUE;
        }

        public int getEnterpriseFirstPosition() {
            return this.mEnterPriseCount > 0 ? this.mEnterpriseOffset : Integer.MAX_VALUE;
        }

        public int getEnterpriseItemCount() {
            return this.mEnterPriseCount;
        }
    }

    public abstract int[] getMatchInfoArray(int i);

    public abstract int getMatchPosition();

    public abstract int getMatchType();

    public static String[] splitString(String str) {
        try {
            return str.substring(2, str.length() - 2).split(" \\|\\| ");
        } catch (Exception e) {
            HwLog.e("HwSearchCursor", "splitString:" + e.getMessage());
            return new String[0];
        }
    }

    public static String replaceString(String str) {
        return TextUtils.isEmpty(str) ? null : str.replace("%|", "|");
    }

    public HwSearchCursor(Cursor cursor) {
        super(cursor);
        this.mCursor = cursor;
    }

    protected static int shiftMatchType(long result) {
        return (int) ((result >> 32) & 255);
    }

    protected static int shiftMatchPosition(long result) {
        return (int) (255 & result);
    }

    public int getYellowPageFirstPosition() {
        return Integer.MAX_VALUE;
    }
}
