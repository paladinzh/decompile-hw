package cn.com.xy.sms.sdk.ui.popu.util;

import android.app.Activity;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.util.ParseManager;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class d extends ClickableSpan {
    private boolean a;
    private int b = -16776961;
    private int c = -16776961;
    private boolean d;
    private JSONObject e;
    private Map<String, String> f = null;
    private boolean g = false;

    private d(int i, int i2, JSONObject jSONObject, Map<String, String> map) {
        this.b = i;
        this.c = i2;
        this.e = jSONObject;
        this.f = map;
    }

    public d(JSONObject jSONObject, Map<String, String> map) {
        this.e = jSONObject;
        this.f = map;
    }

    public final void a(int i, int i2) {
        if (i != 0) {
            this.b = i;
        }
        if (i2 != 0) {
            this.c = i2;
        }
    }

    public final void a(boolean z) {
        this.d = z;
    }

    public final void b(boolean z) {
        this.a = z;
    }

    public void onClick(View view) {
        String str;
        try {
            if (this.f != null) {
                str = "isClickAble";
                if (this.f.containsKey(str)) {
                    String stringBuilder = new StringBuilder(String.valueOf((String) this.f.get("isClickAble"))).toString();
                    str = "false";
                    if (str.equalsIgnoreCase(stringBuilder)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
        }
        try {
            ViewUtil.clearSpan();
            if (this.g) {
                this.g = false;
                return;
            }
            if (str == null) {
                DuoquUtils.doActionContext(1, ParseManager.getRecogniseActionConfig(this.e, this.f), this.f);
            } else {
                Activity activity = (Activity) 1;
                str = ParseManager.getRecogniseActionConfig(this.e, this.f);
                DuoquUtils.doAction(activity, str, this.f);
            }
            this.g = false;
        } catch (Throwable th) {
        } finally {
            this.g = false;
        }
    }

    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
        textPaint.setColor(!this.a ? this.b : this.c);
        if (this.d) {
            textPaint.setUnderlineText(true);
        } else {
            textPaint.setUnderlineText(this.a);
        }
    }
}
