package com.huawei.powergenie.core.policy;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;

public class ConfigUpdateManager {
    private String mCloudConfigVer = null;
    private HashMap<String, String> mConfigSettingsCache = new HashMap();
    private final Context mContext;
    private myHandler mHandler = null;
    private final ICoreContext mICoreContext;
    private long mLastUpdateTime = 0;
    private String mLocalConfigVer = null;
    private final PolicyService mPolicyService;
    private boolean mUseCloudConfig = false;

    private class myHandler extends Handler {
        public myHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    ConfigUpdateManager.this.handleCloudConfigMsg((MsgEvent) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public ConfigUpdateManager(ICoreContext coreContext, PolicyService service) {
        this.mICoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mPolicyService = service;
        synchronized (this) {
            this.mUseCloudConfig = determineIfUseCloudConfigLocked();
            cacheConfigSettingsLocked();
        }
    }

    public void processCloundConfigEvent(MsgEvent event) {
        long now = SystemClock.elapsedRealtime();
        if (this.mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("cloud_config");
            handlerThread.start();
            this.mHandler = new myHandler(handlerThread.getLooper());
        }
        this.mHandler.removeMessages(1000);
        Message msg = this.mHandler.obtainMessage(1000, new MsgEvent(event.getEventId(), event.getIntent()));
        long delay = 0;
        if (now - this.mLastUpdateTime < 5000) {
            delay = 5000;
        }
        this.mHandler.sendMessageDelayed(msg, delay);
        this.mLastUpdateTime = now;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudConfigMsg(MsgEvent event) {
        if (event != null) {
            String pushType = event.getIntent().getStringExtra("pushType");
            String uri = event.getIntent().getStringExtra("uri");
            Log.i("ConfigUpdateManager", "handleCloudConfigMsg,type: " + pushType + ", uri: " + uri);
            if (pushType != null && !pushType.equals("") && uri != null && !uri.equals("")) {
                synchronized (this) {
                    if (!saveCloudConfigLocked(pushType, uri)) {
                    } else if (pushType.equals("pg_config_list")) {
                        boolean old = this.mUseCloudConfig;
                        this.mUseCloudConfig = determineIfUseCloudConfigLocked();
                        if (old != this.mUseCloudConfig || this.mUseCloudConfig) {
                            cacheConfigSettingsLocked();
                        }
                    }
                }
            }
        }
    }

    private boolean saveCloudConfigLocked(String pushType, String uri) {
        try {
            boolean ret;
            InputStream inStream = this.mContext.getContentResolver().openInputStream(Uri.parse(uri));
            if (inStream == null) {
                return false;
            }
            synchronized (this) {
                ByteArrayOutputStream memStream = new ByteArrayOutputStream();
                ret = true;
                byte[] b = new byte[1024];
                while (true) {
                    try {
                        int len = inStream.read(b);
                        if (len == -1) {
                            break;
                        }
                        memStream.write(b, 0, len);
                    } catch (IOException e) {
                        Log.w("ConfigUpdateManager", "io exception: " + e);
                        ret = false;
                    } finally {
                        closeInputStream(inStream);
                    }
                }
                if (ret) {
                    AtomicFile atomicFile = new AtomicFile(new File(this.mContext.getDir("cloud_config", 0), pushType + ".xml"));
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = atomicFile.startWrite();
                        memStream.writeTo(fileOutputStream);
                        atomicFile.finishWrite(fileOutputStream);
                    } catch (IOException e2) {
                        Log.w("ConfigUpdateManager", "Error writing config file", e2);
                        atomicFile.failWrite(fileOutputStream);
                        ret = false;
                    }
                } else {
                    return false;
                }
            }
            return ret;
        } catch (FileNotFoundException e3) {
            Log.w("ConfigUpdateManager", "Uri :(" + uri + ") not found");
            return false;
        }
    }

    public boolean updateConfigList(String name, ArrayList<String> inOutList) {
        HashSet<String> rmList;
        Exception e;
        Object obj;
        Iterator<String> it;
        Throwable th;
        if (inOutList == null) {
            return false;
        }
        long bgTime = SystemClock.uptimeMillis();
        Iterable iterable = null;
        HashSet hashSet = null;
        synchronized (this) {
            InputStream inStream = this.mUseCloudConfig ? getCloudConfigStreamLocked() : getLocalConfigStreamLocked();
            if (inStream == null) {
                return false;
            }
            boolean z = false;
            try {
                XmlPullParser parser = Xml.newPullParser();
                if (parser != null) {
                    parser.setInput(inStream, "UTF-8");
                    HashSet<String> addList = new HashSet();
                    try {
                        rmList = new HashSet();
                    } catch (Exception e2) {
                        e = e2;
                        obj = addList;
                        try {
                            Log.e("ConfigUpdateManager", "updateConfigList err:" + e);
                            closeInputStream(inStream);
                            if (z) {
                                for (String pkg : r2) {
                                    if (inOutList.contains(pkg)) {
                                        inOutList.add(pkg);
                                    }
                                }
                                it = inOutList.iterator();
                                while (it.hasNext()) {
                                    if (!hashSet.contains((String) it.next())) {
                                        it.remove();
                                    }
                                }
                                return z;
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            closeInputStream(inStream);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        closeInputStream(inStream);
                        throw th;
                    }
                    try {
                        z = parseConfigList(parser, name, addList, rmList);
                        hashSet = rmList;
                        iterable = addList;
                    } catch (Exception e3) {
                        e = e3;
                        HashSet<String> rmList2 = rmList;
                        obj = addList;
                        Log.e("ConfigUpdateManager", "updateConfigList err:" + e);
                        closeInputStream(inStream);
                        if (z) {
                            return false;
                        }
                        for (String pkg2 : r2) {
                            if (inOutList.contains(pkg2)) {
                                inOutList.add(pkg2);
                            }
                        }
                        it = inOutList.iterator();
                        while (it.hasNext()) {
                            if (!hashSet.contains((String) it.next())) {
                                it.remove();
                            }
                        }
                        return z;
                    } catch (Throwable th4) {
                        th = th4;
                        HashSet<String> hashSet2 = addList;
                        closeInputStream(inStream);
                        throw th;
                    }
                }
                closeInputStream(inStream);
            } catch (Exception e4) {
                e = e4;
                Log.e("ConfigUpdateManager", "updateConfigList err:" + e);
                closeInputStream(inStream);
                if (z) {
                    for (String pkg22 : r2) {
                        if (inOutList.contains(pkg22)) {
                            inOutList.add(pkg22);
                        }
                    }
                    it = inOutList.iterator();
                    while (it.hasNext()) {
                        if (!hashSet.contains((String) it.next())) {
                            it.remove();
                        }
                    }
                    return z;
                }
                return false;
            }
            if (z) {
                return false;
            }
            if (r2 != null && r2.size() > 0) {
                for (String pkg222 : r2) {
                    if (inOutList.contains(pkg222)) {
                        inOutList.add(pkg222);
                    }
                }
            }
            if (hashSet != null && hashSet.size() > 0) {
                it = inOutList.iterator();
                while (it.hasNext()) {
                    if (!hashSet.contains((String) it.next())) {
                        it.remove();
                    }
                }
            }
        }
    }

    private boolean parseConfigList(XmlPullParser parser, String name, Set<String> addList, Set<String> rmList) {
        boolean targetName = false;
        boolean addType = false;
        boolean rmType = false;
        try {
            XmlHelper.beginDocument(parser, "pg_config_list");
            while (true) {
                XmlHelper.nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    Log.i("ConfigUpdateManager", "parseConfigList, name: " + name + ", add: " + addList + ", remove: " + rmList);
                    return true;
                } else if ("item".equals(tag)) {
                    if (targetName && addType) {
                        addList.add(parser.nextText());
                    } else if (targetName && rmType) {
                        rmList.add(parser.nextText());
                    }
                } else if ("array".equals(tag)) {
                    if (targetName && "add".equals(parser.getAttributeValue(0))) {
                        addType = true;
                        rmType = false;
                    } else if (targetName && "remove".equals(parser.getAttributeValue(0))) {
                        rmType = true;
                        addType = false;
                    } else {
                        addType = false;
                        rmType = false;
                    }
                } else if ("list".equals(tag)) {
                    if (name.equals(parser.getAttributeValue(0))) {
                        targetName = true;
                    } else {
                        targetName = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseConfigSettings(XmlPullParser parser, HashMap<String, String> out) {
        boolean ret = false;
        if (out == null) {
            return false;
        }
        try {
            XmlHelper.beginDocument(parser, "pg_config_list");
            while (true) {
                XmlHelper.nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                } else if ("setting".equals(tag)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value = parser.getAttributeValue(null, "value");
                    if (!(name == null || value == null)) {
                        out.put(name, value);
                    }
                }
            }
            Log.i("ConfigUpdateManager", "parseConfigSettings : " + out);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean determineIfUseCloudConfigLocked() {
        InputStream inStream = getCloudConfigStreamLocked();
        this.mCloudConfigVer = getConfigVersion(inStream);
        closeInputStream(inStream);
        inStream = getLocalConfigStreamLocked();
        this.mLocalConfigVer = getConfigVersion(inStream);
        closeInputStream(inStream);
        boolean ret = false;
        if (compareVersion(this.mCloudConfigVer, this.mLocalConfigVer) > 0) {
            ret = true;
        }
        Log.i("ConfigUpdateManager", "LocalConfigVer: " + this.mLocalConfigVer + ", CloudConfigVer: " + this.mCloudConfigVer + ", useCloudConfig: " + ret);
        return ret;
    }

    private String getConfigVersion(InputStream inStream) {
        String version = null;
        if (inStream == null) {
            return null;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            if (parser != null) {
                parser.setInput(inStream, "UTF-8");
                XmlHelper.beginDocument(parser, "pg_config_list");
                version = parser.getAttributeValue(null, "version");
            }
        } catch (Exception e) {
            Log.e("ConfigUpdateManager", "found versin err:" + e);
        }
        return version;
    }

    private int compareVersion(String version1, String version2) {
        if (version1 != null && version2 == null) {
            return 1;
        }
        if (version1 == null && version2 != null) {
            return -1;
        }
        if (version1 == null && version2 == null) {
            return 0;
        }
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;
        for (int idx = 0; idx < minLength; idx++) {
            diff = versionArray1[idx].length() - versionArray2[idx].length();
            if (diff != 0) {
                break;
            }
            diff = versionArray1[idx].compareTo(versionArray2[idx]);
            if (diff != 0) {
                break;
            }
        }
        if (diff == 0) {
            diff = versionArray1.length - versionArray2.length;
        }
        return diff;
    }

    private InputStream getCloudConfigStreamLocked() {
        File file = new File(this.mContext.getDir("cloud_config", 0), "pg_config_list.xml");
        if (!file.exists()) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (Exception e) {
            Log.w("ConfigUpdateManager", "getCloudConfigStreamLocked err:" + e);
            return null;
        }
    }

    private InputStream getLocalConfigStreamLocked() {
        try {
            return this.mContext.getAssets().open("pg_config_list.xml");
        } catch (Exception e) {
            Log.w("ConfigUpdateManager", "getLocalConfigStreamLocked err:" + e);
            return null;
        }
    }

    private boolean closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void cacheConfigSettingsLocked() {
        this.mConfigSettingsCache.clear();
        InputStream inStream = this.mUseCloudConfig ? getCloudConfigStreamLocked() : getLocalConfigStreamLocked();
        if (inStream != null) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                if (parser != null) {
                    parser.setInput(inStream, "UTF-8");
                    parseConfigSettings(parser, this.mConfigSettingsCache);
                }
                closeInputStream(inStream);
            } catch (Exception e) {
                Log.e("ConfigUpdateManager", "updateConfigList err:" + e);
            } catch (Throwable th) {
                closeInputStream(inStream);
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        synchronized (this) {
            pw.println();
            pw.println("ConfigUpdateManager:");
            pw.println("    mUseCloudConfig: " + this.mUseCloudConfig);
            pw.println("    mLocalConfigVer: " + this.mLocalConfigVer + ", mCloudConfigVer: " + this.mCloudConfigVer);
            pw.println("    mConfigSettingsCache: " + this.mConfigSettingsCache);
        }
    }
}
