package defpackage;

import com.huawei.android.pushagent.utils.multicard.MultiCard;
import com.huawei.android.pushagent.utils.multicard.MultiCard.SupportMode;
import java.lang.reflect.Field;

/* renamed from: bn */
public class bn {
    private static SupportMode cg = SupportMode.MODE_SUPPORT_UNKNOWN;
    private static MultiCard ch;

    public static MultiCard cg() {
        bn.isMultiSimEnabled();
        if (cg == SupportMode.MODE_SUPPORT_MTK_GEMINI) {
            ch = bp.cl();
        } else {
            ch = bo.cj();
        }
        return ch;
    }

    private static boolean ch() {
        boolean z = false;
        try {
            Object ck = bo.ck();
            z = ck != null ? ((Boolean) ck.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(ck, new Object[0])).booleanValue() : false;
        } catch (Exception e) {
            aw.e("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()?" + e.toString());
        } catch (Error e2) {
            aw.e("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()" + e2.toString());
        }
        aw.i("mutiCardFactory", "isHwGeminiSupport1 " + z);
        return z;
    }

    private static boolean ci() {
        boolean z = false;
        try {
            Field declaredField = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            declaredField.setAccessible(true);
            z = declaredField.getBoolean(null);
        } catch (Exception e) {
            aw.e("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e.toString());
        } catch (Error e2) {
            aw.e("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e2.toString());
        }
        aw.i("mutiCardFactory", "isMtkGeminiSupport " + z);
        return z;
    }

    public static boolean isMultiSimEnabled() {
        boolean z = true;
        if (cg != SupportMode.MODE_SUPPORT_UNKNOWN) {
            return cg == SupportMode.MODE_SUPPORT_HW_GEMINI || cg == SupportMode.MODE_SUPPORT_MTK_GEMINI;
        } else {
            try {
                if (bn.ci()) {
                    cg = SupportMode.MODE_SUPPORT_MTK_GEMINI;
                } else if (bn.ch()) {
                    cg = SupportMode.MODE_SUPPORT_HW_GEMINI;
                } else {
                    cg = SupportMode.MODE_NOT_SUPPORT_GEMINI;
                    z = false;
                }
                return z;
            } catch (Exception e) {
                aw.e("mutiCardFactory", " " + e.toString());
                return false;
            } catch (Error e2) {
                aw.e("mutiCardFactory", "" + e2.toString());
                return false;
            }
        }
    }
}
