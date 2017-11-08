package com.android.contacts.compatibility;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.UserManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import com.android.contacts.ContactsUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.Locale;

public class QueryUtil {
    private static boolean IS_CONTACTS_SYSTEM_APP = false;
    private static String TAG = "QueryUtil";
    private static Context mContext;
    private static String mGeoLocationChina;
    private static boolean sIsSupportHwSeniorSearch = true;

    public static boolean isContainColumn(String[] columnNames, String columnName) {
        int i = 0;
        if (columnNames == null || columnName == null) {
            return false;
        }
        boolean isContainSubscription = false;
        int length = columnNames.length;
        while (i < length) {
            if ("subscription".equals(columnNames[i])) {
                isContainSubscription = true;
                break;
            }
            i++;
        }
        return isContainSubscription;
    }

    public static void reInit(Context context) {
        mContext = null;
        init(context);
    }

    public static void init(Context context) {
        if (context != null && mContext == null) {
            mContext = context;
            try {
                if (mContext.getResources() != null) {
                    mGeoLocationChina = mContext.getResources().getString(R.string.geo_number_location);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            IS_CONTACTS_SYSTEM_APP = true;
        }
    }

    public static Uri getCallsContentUri(Context context) {
        UserManager userManager = (UserManager) getContext().getSystemService("user");
        if (userManager == null || userManager.isUserUnlocked()) {
            return getCallsContentUri();
        }
        return Uri.parse("content://call_log_shadow/calls");
    }

    public static Uri getCallsContentUri() {
        return IS_CONTACTS_SYSTEM_APP ? Calls.CONTENT_URI_WITH_VOICEMAIL : Calls.CONTENT_URI;
    }

    public static boolean isSystemAppForContacts() {
        return IS_CONTACTS_SYSTEM_APP;
    }

    public static Uri getPhoneLookupUri(String path) {
        return getPhoneLookupUri(path, null, null);
    }

    public static Uri getPhoneLookupUri(String path, String param, String value) {
        Uri uri = PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI;
        if (!ContactsUtils.FLAG_N_FEATURE) {
            uri = PhoneLookup.CONTENT_FILTER_URI;
        }
        Builder uriBuilder = uri.buildUpon();
        if (path != null) {
            uriBuilder.appendPath(path);
        }
        if (param != null) {
            uriBuilder.appendQueryParameter(param, value);
        }
        return uriBuilder.build();
    }

    public static boolean isSupportDualSim() {
        return ProviderFeatureChecker.getInstance(mContext).isSupportDualSim();
    }

    public static boolean isHAPProviderInstalled() {
        return ProviderFeatureChecker.getInstance(mContext).isHAPProviderInstalled();
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean checkGeoLocation(String geocode, String number) {
        if (TextUtils.isEmpty(geocode) || geocode.equals(mGeoLocationChina)) {
            return false;
        }
        return true;
    }

    public static void checkProviderHwSearchFeature(final Context context) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r9 = this;
                r0 = "content://com.android.contacts/feature";
                r1 = android.net.Uri.parse(r0);
                r6 = 0;
                r0 = r2;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r3 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r4 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r5 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                if (r6 == 0) goto L_0x0030;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
            L_0x0018:
                r6.moveToFirst();	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = "is_support_hw_senior_search";	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r8 = r6.getString(r0);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = "1";	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = r0.equals(r8);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                com.android.contacts.compatibility.QueryUtil.sIsSupportHwSeniorSearch = r0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
            L_0x0030:
                if (r6 == 0) goto L_0x0035;
            L_0x0032:
                r6.close();
            L_0x0035:
                r0 = com.android.contacts.util.HwLog.HWFLOW;
                if (r0 == 0) goto L_0x0058;
            L_0x0039:
                r0 = com.android.contacts.compatibility.QueryUtil.TAG;
                r2 = new java.lang.StringBuilder;
                r2.<init>();
                r3 = "checkProviderHwSearchFeature supportHwSeniorSearch:";
                r2 = r2.append(r3);
                r3 = com.android.contacts.compatibility.QueryUtil.sIsSupportHwSeniorSearch;
                r2 = r2.append(r3);
                r2 = r2.toString();
                com.android.contacts.util.HwLog.i(r0, r2);
            L_0x0058:
                return;
            L_0x0059:
                r7 = move-exception;
                r0 = com.android.contacts.compatibility.QueryUtil.TAG;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2.<init>();	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r3 = "checkProviderHwSearchFeature exception:";	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2 = r2.append(r3);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2 = r2.append(r7);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r2 = r2.toString();	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                com.android.contacts.util.HwLog.w(r0, r2);	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                r0 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                com.android.contacts.compatibility.QueryUtil.sIsSupportHwSeniorSearch = r0;	 Catch:{ Exception -> 0x0059, all -> 0x00a2 }
                if (r6 == 0) goto L_0x007e;
            L_0x007b:
                r6.close();
            L_0x007e:
                r0 = com.android.contacts.util.HwLog.HWFLOW;
                if (r0 == 0) goto L_0x0058;
            L_0x0082:
                r0 = com.android.contacts.compatibility.QueryUtil.TAG;
                r2 = new java.lang.StringBuilder;
                r2.<init>();
                r3 = "checkProviderHwSearchFeature supportHwSeniorSearch:";
                r2 = r2.append(r3);
                r3 = com.android.contacts.compatibility.QueryUtil.sIsSupportHwSeniorSearch;
                r2 = r2.append(r3);
                r2 = r2.toString();
                com.android.contacts.util.HwLog.i(r0, r2);
                goto L_0x0058;
            L_0x00a2:
                r0 = move-exception;
                if (r6 == 0) goto L_0x00a8;
            L_0x00a5:
                r6.close();
            L_0x00a8:
                r2 = com.android.contacts.util.HwLog.HWFLOW;
                if (r2 == 0) goto L_0x00cb;
            L_0x00ac:
                r2 = com.android.contacts.compatibility.QueryUtil.TAG;
                r3 = new java.lang.StringBuilder;
                r3.<init>();
                r4 = "checkProviderHwSearchFeature supportHwSeniorSearch:";
                r3 = r3.append(r4);
                r4 = com.android.contacts.compatibility.QueryUtil.sIsSupportHwSeniorSearch;
                r3 = r3.append(r4);
                r3 = r3.toString();
                com.android.contacts.util.HwLog.i(r2, r3);
            L_0x00cb:
                throw r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.compatibility.QueryUtil.1.run():void");
            }
        });
    }

    public static boolean isProviderSupportHwSeniorSearch() {
        return sIsSupportHwSeniorSearch;
    }

    public static boolean isSpecialLanguageForSearch() {
        String[] specialLanguages = new String[]{"da", "iw", "he", "ja", "ko", "nb", "ru"};
        String language = Locale.getDefault().getLanguage();
        for (String equals : specialLanguages) {
            if (equals.equals(language)) {
                if (HwLog.HWFLOW) {
                    HwLog.i(TAG, "isSpecialLanguageForSearch true");
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isUseHwSearch() {
        return isProviderSupportHwSeniorSearch() && !isSpecialLanguageForSearch();
    }

    public static String getDefaultLocation(String countryIso) {
        String location = NumberLocationCache.getDefaultLocation(countryIso);
        if (location == null) {
            location = CountryMonitor.getCountryNameByCountryIso(countryIso);
            if (location != null) {
                NumberLocationCache.putDefault(countryIso, location);
            }
        }
        return location;
    }
}
