package com.huawei.numberlocation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.preference.UpdateUtil;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.DialerHighlighter;
import java.util.Locale;

public class NLContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI_FVERSION = Uri.parse("content://com.huawei.numberlocationnumberlocation/fversion");
    private static final String[] RESULT_PROJECTION = new String[]{"fversion", "item", "fsversion"};
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private Context mContext;
    private String[] mFilterStrings;
    private Locale mLocale;
    private PhoneNumberOfflineGeocoder mPhoneNumberOfflineGeocoder;
    private PhoneNumberUtil mPhoneNumberUtil;

    static {
        sUriMatcher.addURI("com.huawei.numberlocation", "numberlocation", 1);
        sUriMatcher.addURI("com.huawei.numberlocation", "numberlocation/#", 2);
        sUriMatcher.addURI("com.huawei.numberlocation", "numberlocation/accurate", 3);
        sUriMatcher.addURI("com.huawei.numberlocation", "numberlocation/fversion", 4);
    }

    private void setLocale(Context context) {
        this.mLocale = context.getResources().getConfiguration().locale;
    }

    private synchronized PhoneNumberUtil getPhoneNumberUtil() {
        if (this.mPhoneNumberUtil == null) {
            this.mPhoneNumberUtil = PhoneNumberUtil.getInstance();
        }
        return this.mPhoneNumberUtil;
    }

    private synchronized PhoneNumberOfflineGeocoder getPhoneNumberOfflineGeocoder() {
        if (this.mPhoneNumberOfflineGeocoder == null) {
            this.mPhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance();
        }
        return this.mPhoneNumberOfflineGeocoder;
    }

    private PhoneNumber parsePhoneNumber(String number, String countryIso) {
        try {
            return getPhoneNumberUtil().parse(number, countryIso);
        } catch (NumberParseException e) {
            return null;
        }
    }

    public boolean onCreate() {
        this.mContext = getContext();
        initFilterStrings(this.mContext);
        setLocale(this.mContext);
        return true;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                LogExt.d("NLContentProvider", "return ITEMS");
                return "vnd.android.cursor.dir/vnd.google.numberlocation";
            case 2:
                LogExt.d("NLContentProvider", "return ITEM");
                return "vnd.android.cursor.item/vnd.google.numberlocation";
            case 4:
                LogExt.d("NLContentProvider", "return FILE_VERSION");
                return "vnd.android.cursor.item/vnd.contacts.numberlocation.fversion";
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        boolean showLocation = false;
        switch (sUriMatcher.match(uri)) {
            case 1:
                showLocation = false;
                if ("true".equals(uri.getQueryParameter("showLocation"))) {
                    showLocation = true;
                    break;
                }
                break;
            case 2:
                break;
            case 3:
                if (this.mContext != null) {
                    return getAccurateNumberLocation(this.mContext, uri);
                }
                return new MatrixCursor(new String[]{"location"});
            case 4:
                MatrixCursor result = new MatrixCursor(RESULT_PROJECTION);
                SharedPreferences de = SharePreferenceUtil.getDefaultSp_de(this.mContext);
                long version = de.getLong(UpdateUtil.getSummaryKey(1), 0);
                int item = de.getInt(UpdateUtil.getItemKey(1), 2);
                String fsversion = DateUtils.convertDateToVersion(version);
                result.addRow(new Object[]{Long.valueOf(version), Integer.valueOf(item), fsversion});
                if (this.mContext != null) {
                    result.setNotificationUri(this.mContext.getContentResolver(), CONTENT_URI_FVERSION);
                }
                return result;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String phoneNumber = selectionArgs[0];
        String geoLocation = NLUtils.getGeoNumberLocation(getContext(), phoneNumber, Boolean.valueOf(showLocation));
        if (TextUtils.isEmpty(geoLocation)) {
            geoLocation = getGeocodedLocationFor(phoneNumber, CountryMonitor.getInstance(this.mContext).getCountryIso());
            LogExt.d("NLContentProvider", "geoLocation = " + geoLocation);
        }
        String areacode = NLUtils.getAreaCode(getContext(), phoneNumber);
        MatrixCursor mCursor = new MatrixCursor(new String[]{"number", "geolocation", "areacode"});
        mCursor.addRow(new Object[]{phoneNumber, geoLocation, areacode});
        LogExt.d("NLContentProvider", "mCursor.getCount = " + mCursor.getCount() + ", mCursor.getColumnCount = " + mCursor.getColumnCount());
        return mCursor;
    }

    private String getGeocodedLocationFor(String number, String countryIso) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if ("CN".equals(countryIso) && ((number.length() == 7 || number.length() == 8) && number.charAt(0) != '0')) {
            return null;
        }
        if ("CN".equals(countryIso) && number.matches("^((\\+86)|(0086)|(86))?[4,8]00\\d{7}$")) {
            return null;
        }
        PhoneNumber structuredPhoneNumber = parsePhoneNumber(number, countryIso);
        if (structuredPhoneNumber != null) {
            return getPhoneNumberOfflineGeocoder().getDescriptionForNumber(structuredPhoneNumber, this.mLocale);
        }
        return null;
    }

    private Cursor getAccurateNumberLocation(Context context, Uri uri) {
        String numberLocation = NLUtils.getGeoNumberLocation(context, DialerHighlighter.cleanNumber(uri.getQueryParameter("number"), false), Boolean.valueOf(false));
        if (numberLocation != null) {
            int index;
            for (String indexOf : this.mFilterStrings) {
                index = numberLocation.indexOf(indexOf);
                if (index >= 0) {
                    numberLocation = numberLocation.substring(0, index);
                    break;
                }
            }
            numberLocation = numberLocation.trim();
            if (numberLocation.equals("")) {
                numberLocation = null;
            } else {
                index = numberLocation.indexOf(HwCustPreloadContacts.EMPTY_STRING);
                if (index > 0) {
                    numberLocation = numberLocation.substring(0, index);
                }
            }
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{"location"});
        cursor.addRow(new Object[]{numberLocation});
        LogExt.d("NLContentProvider", "cursor.getCount = " + cursor.getCount() + ", cursor.getColumnCount = " + cursor.getColumnCount());
        return cursor;
    }

    private void initFilterStrings(Context context) {
        String[] uselessNumberLocation = context.getResources().getStringArray(R.array.useless_number_location);
        this.mFilterStrings = new String[(uselessNumberLocation.length + 2)];
        System.arraycopy(uselessNumberLocation, 0, this.mFilterStrings, 0, uselessNumberLocation.length);
        this.mFilterStrings[uselessNumberLocation.length] = context.getResources().getString(R.string.numberLocationUnknownLocation2);
        this.mFilterStrings[uselessNumberLocation.length + 1] = context.getResources().getString(R.string.geo_number_location);
    }

    public static void initFile(Context context) {
        if (context != null) {
            NLUtils.ensureDataExistent(context);
        }
    }
}
