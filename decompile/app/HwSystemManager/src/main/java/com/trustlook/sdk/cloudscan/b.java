package com.trustlook.sdk.cloudscan;

import android.util.Log;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.database.DBHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: NetworkUtils */
final class b {
    CloudScanClient a;

    public b(CloudScanClient cloudScanClient) {
        this.a = cloudScanClient;
    }

    final List<AppInfo> a(String str, byte[] bArr) throws IOException, JSONException, a, c, d, Md5InvalidException {
        String readLine;
        int i = this.a.a;
        int i2 = this.a.b;
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setConnectTimeout(i);
        httpURLConnection.setReadTimeout(i2);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(bArr);
        outputStream.flush();
        outputStream.close();
        if (httpURLConnection.getResponseCode() == 200) {
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while (true) {
                    readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer.append(readLine + "\n");
                }
                if (stringBuffer.length() != 0) {
                    readLine = stringBuffer.toString();
                    if (readLine != null) {
                        return null;
                    }
                    return a(readLine);
                }
            }
        }
        readLine = null;
        if (readLine != null) {
            return a(readLine);
        }
        return null;
    }

    private List<AppInfo> a(String str) throws JSONException, c, a, d, Md5InvalidException {
        List<AppInfo> arrayList = new ArrayList();
        JSONObject jSONObject = new JSONObject(str);
        String string = jSONObject.getString(Constants.PAYLOAD_MSGID);
        jSONObject.optString(Constants.PAYLOAD_SUCCESS);
        jSONObject.optString(Constants.PAYLOAD_MESSAGE);
        if (string == null) {
            throw new c();
        } else if (string.equalsIgnoreCase(Constants.MSG_200)) {
            JSONArray optJSONArray = jSONObject.optJSONArray(Constants.PAYLOAD_RESULTS);
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject jSONObject2 = optJSONArray.getJSONObject(i);
                String string2 = jSONObject2.getString(DBHelper.COLUMN_MD5);
                PkgInfo a = this.a.a(string2);
                if (a == null) {
                    Log.e(Constants.TAG, "package name not found by md5 =" + string2);
                } else {
                    double doubleValue;
                    if (jSONObject2.isNull("score")) {
                        doubleValue = Double.valueOf(-1.0d).doubleValue();
                    } else {
                        doubleValue = jSONObject2.getDouble("score");
                    }
                    Double valueOf = Double.valueOf(doubleValue);
                    AppInfo appInfo = new AppInfo(a.getPkgName(), a.getMd5());
                    appInfo.setApkPath(a.getPkgPath());
                    appInfo.setSizeInBytes(a.getPkgSize());
                    appInfo.setScore(valueOf.intValue());
                    appInfo.setCategory(jSONObject2.optString("category"));
                    appInfo.setVirusNameInCloud(jSONObject2.optString("virus_name"));
                    arrayList.add(appInfo);
                }
            }
            return arrayList;
        } else if (string.equalsIgnoreCase(Constants.MSG_403) || string.equalsIgnoreCase(Constants.MSG_401)) {
            throw new a();
        } else if (string.equalsIgnoreCase(Constants.MSG_402)) {
            throw new d();
        } else if (string.equalsIgnoreCase(Constants.MSG_201)) {
            throw new Md5InvalidException();
        } else {
            throw new c();
        }
    }

    public static StringBuffer a(Map<String, String> map, String str) throws UnsupportedEncodingException {
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : map.entrySet()) {
            stringBuffer.append((String) entry.getKey()).append("=").append(URLEncoder.encode((String) entry.getValue(), str)).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer;
    }
}
