package com.android.contacts.compatibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.dialpad.DialpadFragment.AllContactListItemViews;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import java.util.concurrent.LinkedBlockingQueue;

public class NumberLocationLoader {
    private static final Uri NUMBER_LOCATION_URI = Uri.parse("content://com.huawei.numberlocation/numberlocation");
    static final String[] PROJECTIONS = new String[]{"number", "geolocation"};
    private Context mContext;
    private ListView mListView;
    private AsyncTask<Void, PhoneCallDetails, Void> mLoader;
    private LinkedBlockingQueue<PhoneCallDetails> viewWaitingForNumber = new LinkedBlockingQueue();

    public NumberLocationLoader(Context context, ListView parent) {
        this.mContext = context;
        this.mListView = parent;
    }

    public void cancelLoading() {
        if (this.mLoader != null) {
            putWaitingView(PhoneCallDetails.getEmptyInstance());
            this.mLoader.cancel(false);
            this.mLoader = null;
        }
    }

    public void putWaitingView(PhoneCallDetails details) {
        try {
            this.viewWaitingForNumber.put(details);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadInBackground() {
        if (this.mListView == null) {
            throw new RuntimeException("please set listview!");
        }
        cancelLoading();
        this.mLoader = new AsyncTask<Void, PhoneCallDetails, Void>() {
            protected void onProgressUpdate(PhoneCallDetails... values) {
                if (!isCancelled()) {
                    NumberLocationLoader.this.refreshListView(values[0]);
                }
            }

            protected Void doInBackground(Void... params) {
                int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(10);
                while (!isCancelled()) {
                    PhoneCallDetails phoneCallDetails = null;
                    while (phoneCallDetails == null) {
                        try {
                            phoneCallDetails = (PhoneCallDetails) NumberLocationLoader.this.viewWaitingForNumber.take();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (phoneCallDetails.isEmpty()) {
                        break;
                    }
                    String number = String.valueOf(phoneCallDetails.number);
                    String geocode = NumberLocationLoader.getAndUpdateGeoNumLocation(NumberLocationLoader.this.mContext, phoneCallDetails.geocode, number);
                    if (geocode == null) {
                        geocode = "";
                    }
                    phoneCallDetails.geocode = geocode;
                    NumberLocationCache.put(number, geocode);
                    if (TextUtils.isEmpty(geocode)) {
                        if (HwLog.HWDBG) {
                            HwLog.d("NumberLocationLoader", "Loading default details.countryIso:" + phoneCallDetails.countryIso);
                        }
                        String displayCountry = QueryUtil.getDefaultLocation(phoneCallDetails.countryIso);
                        if (!TextUtils.isEmpty(displayCountry)) {
                            String defGeoCode = displayCountry;
                            if (HwLog.HWDBG) {
                                HwLog.d("NumberLocationLoader", "Loading default geocode defGeoCode:" + displayCountry);
                            }
                            phoneCallDetails.geocode = displayCountry;
                        }
                    }
                    synchronized (phoneCallDetails) {
                        publishProgress(new PhoneCallDetails[]{phoneCallDetails});
                    }
                }
                Process.setThreadPriority(origPri);
                return null;
            }
        };
        this.mLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void refreshListView(PhoneCallDetails details) {
        synchronized (details) {
            if (this.mListView != null) {
                ViewGroup container = this.mListView;
                for (int i = 0; i < container.getChildCount(); i++) {
                    updateview(details, container.getChildAt(i));
                }
                if (this.mListView.getAdapter() != null && (this.mListView.getAdapter() instanceof CallLogAdapter)) {
                    ((CallLogAdapter) this.mListView.getAdapter()).refreshListView(this.mListView);
                }
            }
        }
    }

    private void updateview(PhoneCallDetails details, View v) {
        if (v.getTag() instanceof CallLogListItemViews) {
            CallLogListItemViews itemview = (CallLogListItemViews) v.getTag();
            if (itemview.phoneCallDetailsViews.numberView.getTag() == details && this.mListView.getAdapter() != null && (this.mListView.getAdapter() instanceof CallLogAdapter)) {
                ((CallLogAdapter) this.mListView.getAdapter()).updateNumberLocation(details, itemview);
            }
        } else if (v.getTag() instanceof AllContactListItemViews) {
            AllContactListItemViews itemview2 = (AllContactListItemViews) v.getTag();
            if (itemview2.mPhoneCallDetailsViews.dateView != null && itemview2.mPhoneCallDetailsViews.dateView.getTag() == details) {
                itemview2.mPhoneCallDetailsViews.numberView.setText(details.geocode);
            }
        }
    }

    private static String getAndUpdateGeoNumLocation(Context aContext, String geocode, String number) {
        String geoLocation = geocode;
        if (!PhoneCapabilityTester.isGeoCodeFeatureEnabled(aContext) || QueryUtil.checkGeoLocation(geocode, number)) {
            return geocode;
        }
        geoLocation = getAndUpdateGeoNumLocation(aContext, number, Boolean.valueOf(true));
        if (TextUtils.isEmpty(geoLocation)) {
            return geocode;
        }
        return geoLocation;
    }

    public static String getAndUpdateGeoNumLocation(Context aContext, String number) {
        return getAndUpdateGeoNumLocation(aContext, number, Boolean.valueOf(true));
    }

    private static ContentResolver getContentResolverOfOwnerUser(Context context) {
        if (MultiUsersUtils.isCurrentUserGuest()) {
            try {
                Context primaryContext = context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(0));
                if (primaryContext != null) {
                    return primaryContext.getContentResolver();
                }
            } catch (NameNotFoundException e) {
                HwLog.e("NumberLocationLoader", "Can't find self package", e);
            }
        }
        return context.getContentResolver();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getAndUpdateGeoNumLocation(Context context, String number, Boolean orgin) {
        ContentResolver aResolver = getContentResolverOfOwnerUser(context);
        String[] selectionArgs = new String[]{ContactsUtils.removeDashesAndBlanks(number)};
        String geolocation = null;
        Uri uri = NUMBER_LOCATION_URI;
        if (orgin.booleanValue()) {
            uri = NUMBER_LOCATION_URI.buildUpon().appendQueryParameter("showLocation", "true").build();
        }
        Cursor c = aResolver.query(uri, PROJECTIONS, "number = ?", selectionArgs, null);
        if (c == null || c.getCount() != 1) {
            HwLog.e("NumberLocationLoader", "get an abnormal cursor when query geo-location for ");
            if (c != null) {
                c.close();
            }
            return "";
        }
        try {
            if (c.moveToFirst()) {
                geolocation = c.getString(1);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            c.close();
        }
        if (geolocation == null) {
            return "";
        }
        return geolocation;
    }
}
