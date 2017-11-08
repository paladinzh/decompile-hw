package com.android.server.am;

import android.app.AppGlobals;
import android.common.HwFrameworkFactory;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.CompatibilityInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.AbsLocationManagerService;
import com.android.server.job.controllers.JobStatus;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class CompatModePackages {
    public static final int COMPAT_FLAG_DISABLED_DEFAULT = 2048;
    public static final int COMPAT_FLAG_DONT_ASK = 1;
    public static final int COMPAT_FLAG_ENABLED = 2;
    public static final int COMPAT_FLAG_ENABLED_DEFAULT = 1024;
    private static final int LOW_RESOLUTION_APP_TYPE = 9998;
    private static final int MSG_WRITE = 300;
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    public static final int UNSUPPORTED_ZOOM_FLAG_DONT_NOTIFY = 4;
    private static String[] mCompatApps = new String[0];
    private static String[] mCompatGames = new String[]{"com.bfs.ninjump", "org.cocos2dx.FishingJoy2", "com.disney.brave_google", "com.eamobile.nfshp_row_wf", "com.ea.games.nfs13_na", "com.gameloft.android.GAND.GloftM3HP", "com.gameloft.android.GAND.GloftD3HP", "com.gameloft.android.ANMP.GloftN3HM", "com.galapagossoft.trialx2_gl2", "com.halfbrick.fruitninjafree", "com.halfbrick.fruitninjahdB", "com.imangi.templerun", "com.imangi.templerun2", "com.kiloo.subwaysurf", "com.vectorunit.red"};
    private HwCustCompatModePackages mCust = ((HwCustCompatModePackages) HwCustUtils.createObj(HwCustCompatModePackages.class, new Object[0]));
    private final AtomicFile mFile;
    private final CompatHandler mHandler;
    private boolean mIsInitCustList = false;
    private final HashMap<String, Integer> mPackages = new HashMap();
    private final HashMap<String, Integer> mPackagesCompatModeHash = new HashMap();
    private final ActivityManagerService mService;

    private final class CompatHandler extends Handler {
        public CompatHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CompatModePackages.MSG_WRITE /*300*/:
                    CompatModePackages.this.saveCompatModes();
                    return;
                default:
                    return;
            }
        }
    }

    public void loadCompatModeAppList() {
        if (!this.mIsInitCustList) {
            Slog.i("SDR", "APS: SDR: CompatModePackages.loadCompatModeAppList.");
            getCustAppList(LOW_RESOLUTION_APP_TYPE);
            this.mIsInitCustList = true;
        }
    }

    private void getCustAppList(int type) {
        String[] custPkgList = HwFrameworkFactory.getHwNsdImpl().getCustAppList(type);
        if (custPkgList != null) {
            for (String packageName : custPkgList) {
                this.mPackages.put(packageName, Integer.valueOf(1027));
            }
            Slog.i("SDR", "APS: SDR: CompatModePackages.getCustAppList. mPackages size = " + this.mPackages.size());
        }
    }

    public CompatModePackages(ActivityManagerService service, File systemDir, Handler handler) {
        this.mService = service;
        this.mFile = new AtomicFile(new File(systemDir, "packages-compat.xml"));
        this.mHandler = new CompatHandler(handler.getLooper());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType == 1) {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
                addCompatList();
                return;
            }
            if ("compat-packages".equals(parser.getName())) {
                eventType = parser.next();
                do {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        if (parser.getDepth() == 2 && AbsLocationManagerService.DEL_PKG.equals(tagName)) {
                            String pkg = parser.getAttributeValue(null, "name");
                            if (pkg != null) {
                                String mode = parser.getAttributeValue(null, "mode");
                                int modeInt = 0;
                                if (mode != null) {
                                    try {
                                        modeInt = Integer.parseInt(mode);
                                    } catch (NumberFormatException e2) {
                                    }
                                }
                                if (this.mCust == null || !this.mCust.isLowPowerDisplayMode() || (modeInt & 1024) == 0) {
                                    this.mPackages.put(pkg, Integer.valueOf(modeInt));
                                }
                            }
                        }
                    }
                    eventType = parser.next();
                } while (eventType != 1);
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3) {
                }
            }
            addCompatList();
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "Error reading compat-packages", e4);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e5) {
                }
            }
            addCompatList();
        } catch (IOException e6) {
            if (fileInputStream != null) {
                Slog.w(TAG, "Error reading compat-packages", e6);
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
            addCompatList();
        } catch (Throwable th) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e8) {
                }
            }
            addCompatList();
        }
    }

    private void addCompatList() {
        if ((this.mCust == null || !this.mCust.isLowPowerDisplayMode()) && SystemProperties.getInt("ro.config.compatibility_enable", 0) == 1) {
            for (String DefaultStr : mCompatGames) {
                addCompatForCheck(DefaultStr, false);
            }
            for (String DefaultStr2 : mCompatApps) {
                addCompatForCheck(DefaultStr2, false);
            }
        }
    }

    private void addCompatForCheck(String PackageName, boolean NeedCheck) {
        int i = 0;
        if (this.mCust == null || !this.mCust.isLowPowerDisplayMode()) {
            boolean CompatModeHasChanged;
            boolean CompatDefaultDisable = getDisabledPackageFlags(PackageName) != 0;
            if (getPackageFlags(PackageName) != 0) {
                CompatModeHasChanged = true;
            } else {
                CompatModeHasChanged = false;
            }
            if (!CompatModeHasChanged && !CompatDefaultDisable) {
                if (NeedCheck) {
                    for (String DefaultStr : mCompatGames) {
                        if (PackageName.matches(DefaultStr)) {
                            this.mPackages.put(PackageName, Integer.valueOf(1027));
                            return;
                        }
                    }
                    String[] strArr = mCompatApps;
                    int length = strArr.length;
                    while (i < length) {
                        if (PackageName.matches(strArr[i])) {
                            this.mPackages.put(PackageName, Integer.valueOf(1027));
                            return;
                        }
                        i++;
                    }
                    this.mPackagesCompatModeHash.put(PackageName, Integer.valueOf(2048));
                    return;
                }
                this.mPackages.put(PackageName, Integer.valueOf(1027));
            }
        }
    }

    private int getDisabledPackageFlags(String packageName) {
        Integer flags = (Integer) this.mPackagesCompatModeHash.get(packageName);
        return flags != null ? flags.intValue() : 0;
    }

    public HashMap<String, Integer> getPackages() {
        return this.mPackages;
    }

    private int getPackageFlags(String packageName) {
        int i = 0;
        if ((DumpState.DUMP_VERSION & SystemProperties.getInt("sys.aps.support", 0)) == 0) {
            return 0;
        }
        Integer flags = (Integer) this.mPackages.get(packageName);
        if (flags != null) {
            i = flags.intValue();
        }
        return i;
    }

    public void handlePackageDataClearedLocked(String packageName) {
        removePackage(packageName);
    }

    public void handlePackageUninstalledLocked(String packageName) {
        removePackage(packageName);
    }

    private void removePackage(String packageName) {
        if (this.mPackages.containsKey(packageName)) {
            this.mPackages.remove(packageName);
            scheduleWrite();
        }
    }

    public void handlePackageAddedLocked(String packageName, boolean updated) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (ai != null) {
            CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
            boolean mayCompat = !ci.alwaysSupportsScreen() ? !ci.neverSupportsScreen() : false;
            if (updated && !mayCompat && this.mPackages.containsKey(packageName)) {
                this.mPackages.remove(packageName);
                scheduleWrite();
            }
        }
    }

    private void scheduleWrite() {
        this.mHandler.removeMessages(MSG_WRITE);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_WRITE), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.compatibility_enable", 0) == 1) {
            addCompatForCheck(ai.packageName, true);
        }
        int i = this.mService.mConfiguration.screenLayout;
        int i2 = this.mService.mConfiguration.smallestScreenWidthDp;
        if ((getPackageFlags(ai.packageName) & 2) == 0) {
            z = false;
        }
        return new CompatibilityInfo(ai, i, i2, z);
    }

    public int computeCompatModeLocked(ApplicationInfo ai) {
        int i = 0;
        boolean enabled = (getPackageFlags(ai.packageName) & 2) != 0;
        CompatibilityInfo info = new CompatibilityInfo(ai, this.mService.mConfiguration.screenLayout, this.mService.mConfiguration.smallestScreenWidthDp, enabled);
        if (info.alwaysSupportsScreen()) {
            return -2;
        }
        if (info.neverSupportsScreen()) {
            return -1;
        }
        if (enabled) {
            i = 1;
        }
        return i;
    }

    public boolean getFrontActivityAskCompatModeLocked() {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            return false;
        }
        return getPackageAskCompatModeLocked(r.packageName);
    }

    public boolean getPackageAskCompatModeLocked(String packageName) {
        return (getPackageFlags(packageName) & 1) == 0;
    }

    public boolean getPackageNotifyUnsupportedZoomLocked(String packageName) {
        return (getPackageFlags(packageName) & 4) == 0;
    }

    public void setFrontActivityAskCompatModeLocked(boolean ask) {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r != null) {
            setPackageAskCompatModeLocked(r.packageName, ask);
        }
    }

    public void setPackageAskCompatModeLocked(String packageName, boolean ask) {
        int curFlags = getPackageFlags(packageName);
        int newFlags = ask ? curFlags & -2 : curFlags | 1;
        if (curFlags != newFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            scheduleWrite();
        }
    }

    public void setPackageNotifyUnsupportedZoomLocked(String packageName, boolean notify) {
        int newFlags;
        int curFlags = getPackageFlags(packageName);
        if (notify) {
            newFlags = curFlags & -5;
        } else {
            newFlags = curFlags | 4;
        }
        if (curFlags != newFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            scheduleWrite();
        }
    }

    public int getFrontActivityScreenCompatModeLocked() {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            return -3;
        }
        return computeCompatModeLocked(r.info.applicationInfo);
    }

    public void setFrontActivityScreenCompatModeLocked(int mode) {
        ActivityRecord r = this.mService.getFocusedStack().topRunningActivityLocked();
        if (r == null) {
            Slog.w(TAG, "setFrontActivityScreenCompatMode failed: no top activity");
        } else {
            setPackageScreenCompatModeLocked(r.info.applicationInfo, mode);
        }
    }

    public int getPackageScreenCompatModeLocked(String packageName) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getCallingUserId());
        } catch (RemoteException e) {
        }
        if (ai == null) {
            return -3;
        }
        return computeCompatModeLocked(ai);
    }

    public void setPackageScreenCompatModeLocked(String packageName, int mode) {
        ApplicationInfo ai = null;
        try {
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (ai == null) {
            Slog.w(TAG, "setPackageScreenCompatMode failed: unknown package " + packageName);
            if (this.mPackages.keySet().contains(packageName)) {
                this.mPackages.remove(packageName);
                scheduleWrite();
            }
            return;
        }
        setPackageScreenCompatModeLocked(ai, mode);
    }

    private void setPackageScreenCompatModeLocked(ApplicationInfo ai, int mode) {
        boolean enable;
        String packageName = ai.packageName;
        int curFlags = getPackageFlags(packageName);
        switch (mode) {
            case 0:
                enable = false;
                break;
            case 1:
                enable = true;
                break;
            case 2:
                if ((curFlags & 2) != 0) {
                    enable = false;
                    break;
                } else {
                    enable = true;
                    break;
                }
            default:
                Slog.w(TAG, "Unknown screen compat mode req #" + mode + "; ignoring");
                return;
        }
        int newFlags = curFlags;
        if (enable) {
            newFlags = curFlags | 2;
        } else {
            newFlags = curFlags & -3;
        }
        CompatibilityInfo ci = compatibilityInfoForPackageLocked(ai);
        if (ci.alwaysSupportsScreen()) {
            Slog.w(TAG, "Ignoring compat mode change of " + packageName + "; compatibility never needed");
            newFlags = 0;
        }
        if (ci.neverSupportsScreen()) {
            Slog.w(TAG, "Ignoring compat mode change of " + packageName + "; compatibility always needed");
            newFlags = 0;
        }
        if (newFlags != curFlags) {
            if (newFlags != 0) {
                this.mPackages.put(packageName, Integer.valueOf(newFlags));
            } else {
                this.mPackages.remove(packageName);
            }
            scheduleWrite();
            this.mService.forceStopPackage(packageName, UserHandle.myUserId());
        }
    }

    void saveCompatModes() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                HashMap<String, Integer> pkgs = new HashMap(this.mPackages);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "compat-packages");
            IPackageManager pm = AppGlobals.getPackageManager();
            int screenLayout = this.mService.mConfiguration.screenLayout;
            int smallestScreenWidthDp = this.mService.mConfiguration.smallestScreenWidthDp;
            for (Entry<String, Integer> entry : pkgs.entrySet()) {
                String pkg = (String) entry.getKey();
                int mode = ((Integer) entry.getValue()).intValue();
                if (mode != 0) {
                    ApplicationInfo ai = null;
                    try {
                        ai = pm.getApplicationInfo(pkg, 0, 0);
                    } catch (RemoteException e) {
                    }
                    if (ai != null) {
                        CompatibilityInfo info = new CompatibilityInfo(ai, screenLayout, smallestScreenWidthDp, false);
                        if (!(info.alwaysSupportsScreen() || info.neverSupportsScreen())) {
                            out.startTag(null, AbsLocationManagerService.DEL_PKG);
                            out.attribute(null, "name", pkg);
                            out.attribute(null, "mode", Integer.toString(mode));
                            out.endTag(null, AbsLocationManagerService.DEL_PKG);
                        }
                    } else {
                        continue;
                    }
                }
            }
            out.endTag(null, "compat-packages");
            out.endDocument();
            this.mFile.finishWrite(fileOutputStream);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing compat packages", e1);
            if (fileOutputStream != null) {
                this.mFile.failWrite(fileOutputStream);
            }
        }
    }
}
