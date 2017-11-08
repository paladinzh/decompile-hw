package cn.com.xy.sms.sdk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import com.google.android.gms.R;

public class HarassNumberUtil {
    public static final int MARK_PHONE_CLASSIFY_INDEX = 1;
    public static final String NUMBER = "number";
    public static final String NUMBERMARK_URI = "content://com.android.contacts.app/number_mark";
    public static final String PRE_CRANK_PHONE_DESC = "crank";
    public static final String PRE_FRAUD_PHONE_DESC = "fraud";
    public static final String PRE_HOUSE_AGENT_PHONE_DESC = "house agent";
    public static final String PRE_PROMOTE_SALES_PHONE_DESC = "promote sales";

    public static String queryHarassNameByNumber(Context context, String number) {
        String str = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse(NUMBERMARK_URI).buildUpon().appendQueryParameter(NUMBER, Uri.encode(PhoneNumberUtils.stripSeparators(number))).build(), null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                String classIfy = cursor.getString(1);
                if (PRE_FRAUD_PHONE_DESC.equals(classIfy)) {
                    str = context.getString(R.string.number_mark_fraud);
                } else if (PRE_CRANK_PHONE_DESC.equals(classIfy)) {
                    str = context.getString(R.string.number_mark_crank);
                } else if (PRE_HOUSE_AGENT_PHONE_DESC.equals(classIfy)) {
                    str = context.getString(R.string.number_mark_house_agent);
                } else if (PRE_PROMOTE_SALES_PHONE_DESC.equals(classIfy)) {
                    str = context.getString(R.string.number_mark_promote_sales);
                }
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
        }
        return str;
    }
}
