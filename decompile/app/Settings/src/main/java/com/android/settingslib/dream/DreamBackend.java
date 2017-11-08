package com.android.settingslib.dream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DreamBackend {
    private final DreamInfoComparator mComparator = new DreamInfoComparator(getDefaultDream());
    private final Context mContext;
    private final IDreamManager mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
    private final boolean mDreamsActivatedOnDockByDefault;
    private final boolean mDreamsActivatedOnSleepByDefault;
    private final boolean mDreamsEnabledByDefault;

    public static class DreamInfo {
        public CharSequence caption;
        public ComponentName componentName;
        public Drawable icon;
        public boolean isActive;
        public ComponentName settingsComponentName;

        public String toString() {
            StringBuilder sb = new StringBuilder(DreamInfo.class.getSimpleName());
            sb.append('[').append(this.caption);
            if (this.isActive) {
                sb.append(",active");
            }
            sb.append(',').append(this.componentName);
            if (this.settingsComponentName != null) {
                sb.append("settings=").append(this.settingsComponentName);
            }
            return sb.append(']').toString();
        }
    }

    private static class DreamInfoComparator implements Comparator<DreamInfo> {
        private final ComponentName mDefaultDream;

        public DreamInfoComparator(ComponentName defaultDream) {
            this.mDefaultDream = defaultDream;
        }

        public int compare(DreamInfo lhs, DreamInfo rhs) {
            return sortKey(lhs).compareTo(sortKey(rhs));
        }

        private String sortKey(DreamInfo di) {
            StringBuilder sb = new StringBuilder();
            sb.append(di.componentName.equals(this.mDefaultDream) ? '0' : '1');
            sb.append(di.caption);
            return sb.toString();
        }
    }

    public DreamBackend(Context context) {
        this.mContext = context;
        this.mDreamsEnabledByDefault = context.getResources().getBoolean(17956974);
        this.mDreamsActivatedOnSleepByDefault = context.getResources().getBoolean(17956976);
        this.mDreamsActivatedOnDockByDefault = context.getResources().getBoolean(17956975);
    }

    public List<DreamInfo> getDreamInfos() {
        logd("getDreamInfos()", new Object[0]);
        ComponentName activeDream = getActiveDream();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent("android.service.dreams.DreamService"), 128);
        List<DreamInfo> dreamInfos = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.serviceInfo != null) {
                DreamInfo dreamInfo = new DreamInfo();
                dreamInfo.caption = resolveInfo.loadLabel(pm);
                dreamInfo.icon = resolveInfo.loadIcon(pm);
                dreamInfo.componentName = getDreamComponentName(resolveInfo);
                dreamInfo.isActive = dreamInfo.componentName.equals(activeDream);
                dreamInfo.settingsComponentName = getSettingsComponentName(pm, resolveInfo);
                dreamInfos.add(dreamInfo);
            }
        }
        Collections.sort(dreamInfos, this.mComparator);
        return dreamInfos;
    }

    public ComponentName getDefaultDream() {
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            return this.mDreamManager.getDefaultDreamComponent();
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to get default dream", e);
            return null;
        }
    }

    public boolean isEnabled() {
        return getBoolean("screensaver_enabled", this.mDreamsEnabledByDefault);
    }

    public boolean isActivatedOnDock() {
        return getBoolean("screensaver_activate_on_dock", this.mDreamsActivatedOnDockByDefault);
    }

    public void setActivatedOnDock(boolean value) {
        logd("setActivatedOnDock(%s)", Boolean.valueOf(value));
        setBoolean("screensaver_activate_on_dock", value);
    }

    public boolean isActivatedOnSleep() {
        return getBoolean("screensaver_activate_on_sleep", this.mDreamsActivatedOnSleepByDefault);
    }

    public void setActivatedOnSleep(boolean value) {
        logd("setActivatedOnSleep(%s)", Boolean.valueOf(value));
        setBoolean("screensaver_activate_on_sleep", value);
    }

    private boolean getBoolean(String key, boolean def) {
        return Secure.getInt(this.mContext.getContentResolver(), key, def ? 1 : 0) == 1;
    }

    private void setBoolean(String key, boolean value) {
        Secure.putInt(this.mContext.getContentResolver(), key, value ? 1 : 0);
    }

    public void setActiveDream(ComponentName dream) {
        logd("setActiveDream(%s)", dream);
        if (this.mDreamManager != null) {
            try {
                ComponentName[] dreams = new ComponentName[]{dream};
                IDreamManager iDreamManager = this.mDreamManager;
                if (dream == null) {
                    dreams = null;
                }
                iDreamManager.setDreamComponents(dreams);
            } catch (RemoteException e) {
                Log.w("DreamBackend", "Failed to set active dream to " + dream, e);
            }
        }
    }

    public ComponentName getActiveDream() {
        ComponentName componentName = null;
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            ComponentName[] dreams = this.mDreamManager.getDreamComponents();
            if (dreams != null && dreams.length > 0) {
                componentName = dreams[0];
            }
            return componentName;
        } catch (RemoteException e) {
            Log.w("DreamBackend", "Failed to get active dream", e);
            return null;
        }
    }

    public void launchSettings(DreamInfo dreamInfo) {
        logd("launchSettings(%s)", dreamInfo);
        if (dreamInfo != null && dreamInfo.settingsComponentName != null) {
            this.mContext.startActivity(new Intent().setComponent(dreamInfo.settingsComponentName));
        }
    }

    public void startDreaming() {
        logd("startDreaming()", new Object[0]);
        if (this.mDreamManager != null) {
            try {
                this.mDreamManager.dream();
            } catch (RemoteException e) {
                Log.w("DreamBackend", "Failed to dream", e);
            }
        }
    }

    private static ComponentName getDreamComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    private static ComponentName getSettingsComponentName(PackageManager pm, ResolveInfo resolveInfo) {
        ComponentName componentName = null;
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return null;
        }
        String str = null;
        XmlResourceParser xmlResourceParser = null;
        Throwable caughtException = null;
        try {
            xmlResourceParser = resolveInfo.serviceInfo.loadXmlMetaData(pm, "android.service.dream");
            if (xmlResourceParser == null) {
                Log.w("DreamBackend", "No android.service.dream meta-data");
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
            if ("dream".equals(xmlResourceParser.getName())) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.Dream);
                str = sa.getString(0);
                sa.recycle();
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                if (caughtException != null) {
                    Log.w("DreamBackend", "Error parsing : " + resolveInfo.serviceInfo.packageName, caughtException);
                    return null;
                }
                if (str != null && str.indexOf(47) < 0) {
                    str = resolveInfo.serviceInfo.packageName + "/" + str;
                }
                if (str != null) {
                    componentName = ComponentName.unflattenFromString(str);
                }
                return componentName;
            }
            Log.w("DreamBackend", "Meta-data does not start with dream tag");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        } catch (Throwable e) {
            caughtException = e;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private static void logd(String msg, Object... args) {
    }
}
