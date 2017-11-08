package com.android.settings;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.R;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class TrustAgentUtils {

    public static class TrustAgentComponentInfo {
        EnforcedAdmin admin = null;
        ComponentName componentName;
        String summary;
        String title;
    }

    public static boolean checkProvidePermission(ResolveInfo resolveInfo, PackageManager pm) {
        String packageName = resolveInfo.serviceInfo.packageName;
        if (pm.checkPermission("android.permission.PROVIDE_TRUST_AGENT", packageName) == 0) {
            return true;
        }
        Log.w("TrustAgentUtils", "Skipping agent because package " + packageName + " does not have permission " + "android.permission.PROVIDE_TRUST_AGENT" + ".");
        return false;
    }

    public static ComponentName getComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    public static TrustAgentComponentInfo getSettingsComponent(PackageManager pm, ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return null;
        }
        String str = null;
        TrustAgentComponentInfo trustAgentComponentInfo = new TrustAgentComponentInfo();
        XmlResourceParser xmlResourceParser = null;
        Throwable caughtException = null;
        try {
            xmlResourceParser = resolveInfo.serviceInfo.loadXmlMetaData(pm, "android.service.trust.trustagent");
            if (xmlResourceParser == null) {
                Slog.w("TrustAgentUtils", "Can't find android.service.trust.trustagent meta-data");
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return null;
            }
            Resources res = pm.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if ("trust-agent".equals(xmlResourceParser.getName())) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.TrustAgent);
                trustAgentComponentInfo.summary = sa.getString(1);
                trustAgentComponentInfo.title = sa.getString(0);
                str = sa.getString(2);
                sa.recycle();
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                if (caughtException != null) {
                    Slog.w("TrustAgentUtils", "Error parsing : " + resolveInfo.serviceInfo.packageName, caughtException);
                    return null;
                }
                if (str != null && str.indexOf(47) < 0) {
                    str = resolveInfo.serviceInfo.packageName + "/" + str;
                }
                trustAgentComponentInfo.componentName = str == null ? null : ComponentName.unflattenFromString(str);
                return trustAgentComponentInfo;
            }
            Slog.w("TrustAgentUtils", "Meta-data does not start with trust-agent tag");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        } catch (Throwable e) {
            caughtException = e;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable e2) {
            caughtException = e2;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable e3) {
            caughtException = e3;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }
}
