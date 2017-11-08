package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.SMSCountryInfo;
import com.huawei.hwid.core.helper.handler.b;
import com.huawei.hwid.ui.common.login.RegisterViaEmailActivity;
import com.huawei.hwid.ui.common.login.RegisterViaPhoneNumberActivity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: UIUtil */
public class j {
    public static final boolean a;
    private static Typeface b = null;

    static {
        boolean z;
        if (VERSION.SDK_INT < 11) {
            z = false;
        } else {
            z = true;
        }
        a = z;
    }

    public static Builder a(Context context, int i, int i2) {
        return a(context, i2, context.getString(i));
    }

    public static Builder a(Context context, int i, int i2, boolean z) {
        return a(context, i2, context.getString(i), z);
    }

    public static Builder a(Context context, int i, String str) {
        Builder builder = new Builder(context, b(context));
        builder.setMessage(str);
        builder.setTitle(i);
        builder.setPositiveButton(17039370, null);
        return builder;
    }

    public static Builder a(Context context, int i, String str, boolean z) {
        if (context != null) {
            Builder builder = new Builder(context, b(context));
            builder.setMessage(str);
            builder.setTitle(i);
            if (z) {
                builder.setPositiveButton(17039370, new k(context));
                builder.setOnCancelListener(new l(context));
            } else {
                builder.setPositiveButton(17039370, null);
            }
            return builder;
        }
        a.b("UIUtil", "activity is null");
        return null;
    }

    public static Builder a(Context context, String str, boolean z) {
        return a(context, m.a(context, "CS_prompt_dialog_title"), str, z);
    }

    public static void a(Context context, IBinder iBinder) {
        if (context != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService("input_method");
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(iBinder, 0);
            }
        }
    }

    public static AlertDialog a(Activity activity, String str, boolean z, b bVar) {
        View inflate;
        if (d.r(activity)) {
            inflate = View.inflate(activity, m.d(activity, "cs_disable_pwd_dialog_3"), null);
        } else {
            inflate = View.inflate(activity, m.d(activity, "cs_disable_pwd_dialog"), null);
        }
        ((Button) inflate.findViewById(m.e(activity, "disableBtn"))).setText(activity.getString(m.a(activity, "CS_pwd_disable_please")));
        TextView textView = (TextView) inflate.findViewById(m.e(activity, "forget_pwd"));
        ((TextView) inflate.findViewById(m.e(activity, "error_overtime_tip"))).setText(activity.getString(m.a(activity, "CS_inputerror_toomany_message_new")));
        Builder positiveButton = new Builder(activity, b(activity)).setView(inflate).setNegativeButton(17039360, new m(z, activity)).setPositiveButton(activity.getString(m.a(activity, "CS_verify_account")), null);
        positiveButton.setTitle(m.a(activity, "CS_pwd_disable_verify_login_pwd"));
        AlertDialog create = positiveButton.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
        create.getButton(-1).setEnabled(false);
        textView.setOnClickListener(new n(bVar, str, activity, create, z));
        return create;
    }

    public static Builder b(Context context, String str, boolean z) {
        View inflate;
        if (d.r(context)) {
            inflate = View.inflate(context, m.d(context, "cs_common_weblink_dialog_3"), null);
        } else {
            inflate = View.inflate(context, m.d(context, "cs_common_weblink_dialog"), null);
        }
        ((TextView) inflate.findViewById(m.e(context, "text"))).setText(str);
        return a(context, "", z).setView(inflate);
    }

    public static void a(Context context, int i) {
        a(context, context.getString(i), 1);
    }

    public static void a(Context context, EditText editText, TextView textView, boolean z) {
        int selectionStart;
        if (editText.hasFocus()) {
            selectionStart = editText.getSelectionStart();
        } else {
            selectionStart = -1;
        }
        if (z) {
            editText.setInputType(145);
        } else {
            editText.setInputType(129);
        }
        if (selectionStart > -1 && selectionStart <= editText.getText().toString().length()) {
            editText.setSelection(selectionStart);
        }
        if (textView != null) {
            if (z) {
                textView.setBackgroundResource(m.g(context, "cs_pass_undisplay"));
            } else {
                textView.setBackgroundResource(m.g(context, "cs_pass_display"));
            }
        }
    }

    public static ArrayList a(Context context) {
        String d;
        Object obj = null;
        CharSequence g = k.g(context);
        if (TextUtils.isEmpty(g)) {
            d = k.d(context);
        } else {
            d = g;
        }
        if (!TextUtils.isEmpty(d)) {
            Iterator it = k.b(context).iterator();
            while (it.hasNext()) {
                Object obj2;
                if (d.equals(((SMSCountryInfo) it.next()).a())) {
                    obj2 = 1;
                } else {
                    obj2 = obj;
                }
                obj = obj2;
            }
        }
        if (obj == null) {
            d = StringUtils.MPLUG86;
        }
        ArrayList arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        List arrayList3 = new ArrayList();
        ArrayList b = k.b(context);
        if (b.isEmpty()) {
            return arrayList;
        }
        Iterator it2 = b.iterator();
        while (it2.hasNext() && !p.b(r1, ((SMSCountryInfo) it2.next()).a())) {
        }
        Iterator it3 = b.iterator();
        while (it3.hasNext()) {
            SMSCountryInfo sMSCountryInfo = (SMSCountryInfo) it3.next();
            arrayList2.add(sMSCountryInfo.b());
            arrayList3.add(sMSCountryInfo.a());
        }
        arrayList.add(arrayList2.toArray(new String[arrayList2.size()]));
        arrayList.add(arrayList3.toArray(new String[arrayList3.size()]));
        return arrayList;
    }

    public static Bundle a(Bundle bundle, String str) {
        if (bundle == null) {
            return new Bundle();
        }
        Bundle bundle2 = new Bundle();
        bundle2.putString("accountName", bundle.getString("userName"));
        bundle2.putString("userId", bundle.getString("userId"));
        bundle2.putString("deviceId", bundle.getString("deviceId"));
        bundle2.putString("deviceType", bundle.getString("deviceType"));
        bundle2.putInt("siteId", bundle.getInt("siteId"));
        bundle2.putString("serviceToken", d.b(bundle.getString(NetUtil.REQ_QUERY_TOEKN), str));
        return bundle2;
    }

    public static void a(Context context, String str, int i) {
        int c = c(context);
        if (c != 0) {
            context.setTheme(c);
        }
        Toast.makeText(context, str, i).show();
    }

    public static void a(TextView textView, String str, ClickableSpan clickableSpan) {
        CharSequence spannableString = new SpannableString(textView.getText());
        int indexOf = textView.getText().toString().indexOf(str);
        spannableString.setSpan(clickableSpan, indexOf, str.length() + indexOf, 33);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setFocusable(false);
        textView.setClickable(false);
        textView.setLongClickable(false);
    }

    public static DialogFragment a(Activity activity, String str, String str2, String str3, int i, String str4) {
        FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
        DialogFragment a = com.huawei.hwid.ui.common.login.a.a.a(str, str2, str3, i);
        try {
            if (!(a.isAdded() || a.isVisible() || a.isRemoving())) {
                a.show(beginTransaction, str4);
            }
        } catch (IllegalStateException e) {
            a.c("UIUtil", e.getMessage());
        }
        return a;
    }

    public static void a(Activity activity, boolean z, String str) {
        FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
        DialogFragment a = com.huawei.hwid.ui.common.login.a.a.a(z);
        try {
            if (!a.isAdded() && !a.isVisible() && !a.isRemoving()) {
                a.show(beginTransaction, str);
            }
        } catch (IllegalStateException e) {
            a.c("UIUtil", e.getMessage());
        }
    }

    public static int b(Context context) {
        if (context != null) {
            return (VERSION.SDK_INT >= 16 && c(context) != 0) ? 0 : 3;
        } else {
            a.b("UIUtil", "getDialogThemeId, context is null");
            return 3;
        }
    }

    public static void a(Context context, int i, f fVar, boolean z, String str, String str2, Bundle bundle) {
        boolean z2 = false;
        Intent intent = new Intent();
        String str3 = "isEmotionIntroduce";
        if (f.FromOOBE == fVar) {
            z2 = true;
        }
        intent.putExtra(str3, z2);
        intent.putExtra("isFromGuide", z);
        intent.putExtra("requestTokenType", str);
        intent.putExtra("startActivityWay", fVar.ordinal());
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (k.f(context) || k.e(context) || q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL).startsWith("460") || com.huawei.hwid.c.a.a()) {
            intent.setClass(context, RegisterViaPhoneNumberActivity.class);
        } else {
            intent.setClass(context, RegisterViaEmailActivity.class);
        }
        if (!TextUtils.isEmpty(str2)) {
            intent.putExtra("topActivity", str2);
            ((Activity) context).startActivityForResult(intent, i);
        } else if (context instanceof Activity) {
            intent.putExtra("topActivity", context.getClass().getName());
            ((Activity) context).startActivityForResult(intent, i);
        } else {
            intent.addFlags(268435456);
            context.startActivity(intent);
        }
    }

    public static void a(Context context, int i, f fVar, boolean z, String str, Bundle bundle) {
        a(context, i, fVar, z, str, "", bundle);
    }

    public static int c(Context context) {
        if (com.huawei.hwid.core.a.a.a()) {
            return context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        }
        return context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
    }

    public static AlertDialog a(Activity activity, Bundle bundle, String str, int i, HwAccount hwAccount) {
        Builder builder = new Builder(activity, b(activity));
        builder.setMessage(activity.getString(m.a(activity, "CS_new_agree_tip")));
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.setButton(-1, activity.getString(m.a(activity, "CS_next")), new o(bundle, activity, str, hwAccount, i));
        return create;
    }

    public static AlertDialog a(Activity activity) {
        Builder builder = new Builder(activity, b(activity));
        builder.setMessage(activity.getString(m.a(activity, "CS_logout_no_agree_new")));
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.setButton(-2, activity.getString(17039360), new p());
        return create;
    }

    public static void d(Context context) {
        if (context != null) {
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
            } catch (Exception e) {
                a.d("UIUtil", e.toString());
            }
        }
    }
}
