package com.huawei.hwid.core.c;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import com.huawei.hwid.core.c.b.a;
import java.util.HashMap;

/* compiled from: CheckPassWordUtil */
public class g {
    private static HashMap a(String str) {
        int i = 0;
        HashMap hashMap = new HashMap();
        if (str != null) {
            boolean z;
            boolean z2;
            if (str.length() <= 32) {
                z = false;
            } else {
                z = true;
            }
            if (str.length() >= 8) {
                z2 = false;
            } else {
                z2 = true;
            }
            boolean z3 = false;
            boolean z4 = false;
            boolean z5 = false;
            boolean z6 = false;
            while (i < str.length()) {
                char charAt = str.charAt(i);
                if (charAt >= '0' && charAt <= '9') {
                    z4 = true;
                } else if (charAt >= 'A' && charAt <= 'Z') {
                    z6 = true;
                } else if (charAt >= 'a' && charAt <= 'z') {
                    z5 = true;
                } else if (charAt < '!' || charAt > '~') {
                    z3 = true;
                }
                i++;
            }
            hashMap.put(Integer.valueOf(3), Boolean.valueOf(z3));
            hashMap.put(Integer.valueOf(6), Boolean.valueOf(z6));
            hashMap.put(Integer.valueOf(5), Boolean.valueOf(z4));
            hashMap.put(Integer.valueOf(4), Boolean.valueOf(z5));
            hashMap.put(Integer.valueOf(1), Boolean.valueOf(z2));
            hashMap.put(Integer.valueOf(2), Boolean.valueOf(z));
            return hashMap;
        }
        a.d("PBCheckPassWordUtil", "check null");
        return hashMap;
    }

    public static void a(EditText editText, EditText editText2, Button button) {
        if (editText != null && editText2 != null && button != null) {
            boolean z = (TextUtils.isEmpty(editText.getError()) && TextUtils.isEmpty(editText2.getError())) ? false : true;
            boolean z2 = TextUtils.isEmpty(editText.getText().toString()) && TextUtils.isEmpty(editText2.getText().toString());
            if (z || z2) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
            }
        }
    }

    public static boolean a(EditText editText, Context context, boolean z) {
        if (editText == null || context == null) {
            a.d("PBCheckPassWordUtil", "checkPassWordWhenFoucusChanged null");
            return false;
        }
        a.b("PBCheckPassWordUtil", "checkPassWordWhenFoucusChanged");
        HashMap a = a(editText.getText().toString());
        boolean booleanValue = ((Boolean) a.get(Integer.valueOf(6))).booleanValue();
        boolean booleanValue2 = ((Boolean) a.get(Integer.valueOf(5))).booleanValue();
        boolean booleanValue3 = ((Boolean) a.get(Integer.valueOf(4))).booleanValue();
        boolean booleanValue4 = ((Boolean) a.get(Integer.valueOf(3))).booleanValue();
        boolean booleanValue5 = ((Boolean) a.get(Integer.valueOf(1))).booleanValue();
        boolean booleanValue6 = ((Boolean) a.get(Integer.valueOf(2))).booleanValue();
        boolean z2 = (booleanValue && booleanValue3 && booleanValue2) ? false : true;
        if (booleanValue && booleanValue3 && booleanValue2 && !booleanValue5 && !booleanValue6 && !booleanValue4) {
            editText.setError(null);
            a.a("PBCheckPassWordUtil", "s valid");
            return true;
        }
        if (booleanValue4) {
            editText.setError(context.getString(m.a(context, "CS_error_have_special_symbol")));
        } else if (z && booleanValue5) {
            a.a("PBCheckPassWordUtil", "s less");
            editText.setError(context.getString(m.a(context, "CS_password_too_short_new2")));
        } else if (booleanValue6) {
            a.a("PBCheckPassWordUtil", "s more");
            editText.setError(context.getString(m.a(context, "CS_error_more")));
        } else if (!z || !z2) {
            editText.setError(null);
        } else if (a(booleanValue, booleanValue3, booleanValue2)) {
            a.a("PBCheckPassWordUtil", "s did not contain upper");
            editText.setError(context.getString(m.a(context, "CS_error_least_upper_letter")));
        } else if (a(booleanValue3, booleanValue, booleanValue2)) {
            a.a("PBCheckPassWordUtil", "s did not contain lower");
            editText.setError(context.getString(m.a(context, "CS_error_least_lower_letter")));
        } else if (a(booleanValue2, booleanValue, booleanValue3)) {
            a.a("PBCheckPassWordUtil", "s did not contain digit");
            editText.setError(context.getString(m.a(context, "CS_error_least_digit")));
        } else {
            a.a("PBCheckPassWordUtil", "s only contain 1/3");
            editText.setError(context.getString(m.a(context, "CS_error_no_meet_quirement")));
        }
        return false;
    }

    public static boolean a(String str, EditText editText, EditText editText2, Context context) {
        if (a(editText, context, true) && b(editText, editText2, context)) {
            return b(str, editText, editText2, context);
        }
        return false;
    }

    private static boolean b(String str, EditText editText, EditText editText2, Context context) {
        if (editText == null || editText2 == null || !p.d(str, editText.getText().toString())) {
            return true;
        }
        editText.setText("");
        editText2.setText("");
        editText.setError(context.getString(m.a(context, "CS_pwd_not_same_as_account_new")));
        editText.requestFocus();
        a.e("PBCheckPassWordUtil", "the password cannot same as aaccount name");
        return false;
    }

    public static boolean a(EditText editText, EditText editText2, Context context) {
        if (editText == null || editText2 == null || context == null) {
            a.d("PBCheckPassWordUtil", "checkPasswordFirst null == first || null == context");
            return false;
        }
        String obj = editText.getText().toString();
        Object obj2 = editText2.getText().toString();
        if (TextUtils.isEmpty(obj2) || obj.startsWith(obj2)) {
            editText2.setError(null);
            return true;
        }
        editText2.setError(context.getString(m.a(context, "CS_error_not_same")));
        return false;
    }

    public static boolean b(EditText editText, EditText editText2, Context context) {
        if (editText == null || editText2 == null || context == null) {
            a.d("PBCheckPassWordUtil", "checkEqual null");
            return false;
        } else if (editText.getText().toString().equals(editText2.getText().toString())) {
            editText2.setError(null);
            return true;
        } else {
            editText2.setError(context.getString(m.a(context, "CS_error_not_same")));
            return false;
        }
    }

    private static boolean a(boolean z, boolean z2, boolean z3) {
        return !z && z2 && z3;
    }
}
