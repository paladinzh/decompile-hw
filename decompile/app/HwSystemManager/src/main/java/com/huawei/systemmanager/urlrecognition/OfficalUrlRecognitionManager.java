package com.huawei.systemmanager.urlrecognition;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OfficalUrlRecognitionManager {
    public static final int CHECK_ERROR = 4;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final String HEAD_FIELD_LOCATION = "Location";
    public static final int MALICIOUS_LINK = 3;
    private static final String MESSAGE_SAFE_VIEW = "vMessageSafe";
    public static final int NON_OFFCIAL_LINK = 2;
    public static final int NON_OFFCIAL_NUMBER = 1;
    public static final int OFFCIAL_LINK = 0;
    private static final int REDIRECT_CODE_1 = 301;
    private static final int REDIRECT_CODE_2 = 302;
    private static final String[] RESULT_COLUMNS = new String[]{"result"};
    private static final String TAG = "OfficalUrlRecognitionManager";
    private static final Uri message_safe_view = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider"), "vMessageSafe");

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isMatched(String recievedUrl, String storedUrl) {
        if (TextUtils.isEmpty(storedUrl) || TextUtils.isEmpty(recievedUrl) || !recievedUrl.endsWith(storedUrl)) {
            return false;
        }
        int index = recievedUrl.indexOf(storedUrl);
        if (index == 0) {
            return true;
        }
        return index > 0 && recievedUrl.charAt(index - 1) == '.';
    }

    private String getUrlDomain(String url) {
        try {
            URL u = new URL(url);
            try {
                return u.getHost();
            } catch (Exception e) {
                HwLog.e(TAG, "get url domain error");
                return "";
            }
        } catch (Exception e2) {
            HwLog.e(TAG, "get url domain error");
            return "";
        }
    }

    private String getRealUrlIfNeeded(String strUrl) {
        try {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(strUrl).openConnection();
                urlConnection.setUseCaches(false);
                try {
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    try {
                        urlConnection.setInstanceFollowRedirects(false);
                        urlConnection.connect();
                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == 301 || responseCode == 302) {
                            String location = urlConnection.getHeaderField(HEAD_FIELD_LOCATION);
                            if (!TextUtils.isEmpty(location)) {
                                strUrl = location;
                            }
                        }
                        return strUrl;
                    } catch (IOException e) {
                        HwLog.e(TAG, "get real url error duing to IOException");
                        return strUrl;
                    }
                } catch (Exception e2) {
                    HwLog.e(TAG, "get real url error duing to Exception");
                    return strUrl;
                }
            } catch (IOException e3) {
                HwLog.e(TAG, "get real url error duing to IOException");
                return strUrl;
            }
        } catch (MalformedURLException e4) {
            HwLog.e(TAG, "get real url error duing to MalformedURLException");
            return strUrl;
        }
    }

    public int checkOfficialLink(Context context, String[] args) {
        if (context == null || args == null || args.length < 2 || TextUtils.isEmpty(args[0]) || TextUtils.isEmpty(args[1])) {
            return 4;
        }
        Cursor cursor = context.getContentResolver().query(message_safe_view, null, "messageNo = '" + getRealNumber(args[1]) + "'", null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        String secureLinks = cursor.getString(cursor.getColumnIndex(MessageSafeConfigFile.COL_SECURE_LINK));
                        if (TextUtils.isEmpty(secureLinks)) {
                            HwLog.i(TAG, "secureLinks is empty");
                            return 4;
                        }
                        String[] links = secureLinks.split(SqlMarker.SQL_END);
                        boolean bool = isOfficialLink(args[0], links);
                        if (!bool) {
                            String shortUrl = getRealUrlIfNeeded(args[0]);
                            if (!TextUtils.equals(shortUrl, args[0])) {
                                bool = isOfficialLink(shortUrl, links);
                            }
                        }
                        int i = bool ? 0 : 2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        return i;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return 1;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        HwLog.i(TAG, "no such officical number");
        if (cursor != null) {
            cursor.close();
        }
        return 1;
    }

    public Cursor getCursorFromCheckUrlResult(int reValue) {
        MatrixCursor cursor = new MatrixCursor(RESULT_COLUMNS);
        Object[] result = new Object[1];
        switch (reValue) {
            case 2:
                result[0] = Integer.valueOf(-1);
                break;
            case 3:
                result[0] = Integer.valueOf(1);
                break;
            default:
                result[0] = Integer.valueOf(0);
                break;
        }
        cursor.addRow(result);
        return cursor;
    }

    private String getRealNumber(String number) {
        if (number.startsWith(ConstValues.PHONE_COUNTRY_CODE_CHINA)) {
            return number.replace(ConstValues.PHONE_COUNTRY_CODE_CHINA, "");
        }
        return number;
    }

    private boolean isOfficialLink(String link, String[] links) {
        String host = getUrlDomain(link);
        for (String l : links) {
            if (isMatched(host, l)) {
                return true;
            }
        }
        return false;
    }
}
