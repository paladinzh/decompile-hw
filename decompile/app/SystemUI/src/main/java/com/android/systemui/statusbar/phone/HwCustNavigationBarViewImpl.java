package com.android.systemui.statusbar.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.huawei.android.os.DebugCustEx;
import fyusion.vislib.BuildConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class HwCustNavigationBarViewImpl extends HwCustNavigationBarView {
    private static final int LAND_4 = 10;
    private static final int LAND_5 = 11;
    private static final int MSG_UPDATE_VIEW = 9002;
    private static final int PORT_4 = 8;
    private static final int PORT_5 = 9;
    private static final int ROT0_HAS_SCREENLOCK = 4;
    private static final int ROT90_HAS_SCREENLOCK = 5;
    private static final String TAG = "HwCustNavigationBarView";
    private ParcelFileDescriptor[] fds = null;
    protected ConditionVariable mCondition = new ConditionVariable(true);
    protected Context mContext = null;
    protected Display mDisplay = null;
    protected Handler mHandler = null;
    protected LinearLayout mLayout = null;
    protected Looper mLooper = null;
    protected View[] mRotatedViews = null;
    private OnClickListener mScreenLockClickListener = new OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.android.keyguard", "com.android.keyguard.keyguardplus.OneKeyLockActivity"));
            i.setFlags(268435456);
            HwCustNavigationBarViewImpl.this.mContext.startActivity(i);
        }
    };
    protected TextView mTextView = null;
    protected TextView[] mTextViews = null;
    protected Runnable mThread = new Runnable() {
        public void run() {
            do {
                if (HwCustNavigationBarViewImpl.this.mHandler != null) {
                    HwCustNavigationBarViewImpl.this.mHandler.sendEmptyMessage(HwCustNavigationBarViewImpl.MSG_UPDATE_VIEW);
                }
            } while (!HwCustNavigationBarViewImpl.this.mCondition.block(2000));
        }
    };
    private IBinder powerprofile = null;

    private class CustHandler extends Handler {
        public CustHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message m) {
            switch (m.what) {
                case HwCustNavigationBarViewImpl.MSG_UPDATE_VIEW /*9002*/:
                    HwCustNavigationBarViewImpl.this.update();
                    return;
                default:
                    return;
            }
        }
    }

    public HwCustNavigationBarViewImpl(View[] views, Looper looper, Context context) {
        super(views, looper, context);
        this.mContext = context;
        this.mRotatedViews = (View[]) views.clone();
        this.mLooper = looper;
        this.mHandler = new CustHandler(this.mLooper);
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
    }

    public boolean supportDebugInfo() {
        return SystemProperties.getBoolean("ro.systemui.debug", false);
    }

    public void toggle() {
        int i;
        if (!SystemProperties.getBoolean("debug.systemui.stat2", false)) {
            if (!(this.mRotatedViews == null || this.mTextViews == null)) {
                for (i = 0; i < this.mRotatedViews.length; i++) {
                    ((ViewGroup) this.mRotatedViews[i]).removeView(this.mTextViews[i]);
                }
            }
            this.mTextViews = null;
            this.mCondition.open();
        } else if (this.mTextViews == null) {
            if (this.mRotatedViews != null) {
                this.mTextViews = new TextView[this.mRotatedViews.length];
                i = 0;
                while (i < this.mRotatedViews.length) {
                    this.mTextViews[i] = new TextView(this.mContext);
                    this.mTextViews[i].setTextColor(-256);
                    if (i % 4 == 1 || i % 4 == 3) {
                        this.mTextViews[i].setGravity(5);
                    }
                    this.mRotatedViews[i].addView(this.mTextViews[i]);
                    i++;
                }
            }
            this.mCondition.close();
            new Thread(null, this.mThread, "DebuggingBarThread").start();
        }
    }

    public void update() {
        String s = BuildConfig.FLAVOR;
        ArrayList<String> list = getDebuggingString();
        int rot = this.mDisplay.getRotation();
        if (rot == 0 || rot == 2) {
            s = (String) list.get(0);
        } else {
            s = (String) list.get(1);
        }
        if (this.mTextViews != null) {
            for (TextView text : this.mTextViews) {
                text.setText(s);
            }
        }
    }

    private ArrayList<String> getDebuggingString() {
        Reader inputStreamReader;
        ArrayList<String> list;
        String sh;
        Throwable th;
        Reader inStream;
        String fps = "F: ";
        try {
            fps = fps + String.format("%.1f", new Object[]{Float.valueOf(DebugCustEx.getSurfaceFlingerFrameRate())});
        } catch (Exception e) {
            Log.e(TAG, "SurfaceFlinger: FAILED reading frame rate ...", e);
            fps = fps + "?";
        } catch (UnsatisfiedLinkError e2) {
            Log.e(TAG, "SurfaceFlinger: JNI entry link error ...", e2);
            fps = fps + "?";
        }
        String empty = BuildConfig.FLAVOR;
        StringBuffer cpu = new StringBuffer(empty);
        String ddr = empty;
        String gpu = empty;
        String cur = empty;
        String hmp = empty;
        String aptemp = empty;
        boolean got_cpu = false;
        if (this.powerprofile == null) {
            this.powerprofile = ServiceManager.checkService("powerprofile");
        }
        if (this.fds == null) {
            try {
                this.fds = ParcelFileDescriptor.createPipe();
            } catch (IOException e3) {
                Log.e(TAG, "Create pipe failed!");
            }
        }
        if (this.powerprofile == null) {
            Log.e(TAG, "get powerprofile service failed!");
        } else if (this.fds == null || this.fds.length <= 1 || this.fds[0] == null || this.fds[1] == null) {
            Log.e(TAG, "null fds!");
        } else {
            FileInputStream fileInputStream = null;
            InputStreamReader inStream2 = null;
            JsonReader reader = null;
            try {
                this.powerprofile.dump(this.fds[1].getFileDescriptor(), new String[0]);
                FileInputStream fIns = new FileInputStream(this.fds[0].getFileDescriptor());
                try {
                    inputStreamReader = new InputStreamReader(fIns, "UTF-8");
                } catch (RemoteException e4) {
                    fileInputStream = fIns;
                    try {
                        cpu.append("C:?");
                        Log.e(TAG, "dump failed");
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e5) {
                                Log.e(TAG, "Close error");
                            }
                        }
                        if (inStream2 != null) {
                            inStream2.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        list = new ArrayList();
                        sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                        Log.i(TAG, "getDebuggingString:" + sh);
                        list.add(sh);
                        list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e6) {
                                Log.e(TAG, "Close error");
                                throw th;
                            }
                        }
                        if (inStream2 != null) {
                            inStream2.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    fileInputStream = fIns;
                    Log.e(TAG, "IO exception");
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e8) {
                            Log.e(TAG, "Close error");
                        }
                    }
                    if (inStream2 != null) {
                        inStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    list = new ArrayList();
                    sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                    Log.i(TAG, "getDebuggingString:" + sh);
                    list.add(sh);
                    list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                    return list;
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fIns;
                    if (reader != null) {
                        reader.close();
                    }
                    if (inStream2 != null) {
                        inStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
                try {
                    JsonReader jsonReader = new JsonReader(inputStreamReader);
                    try {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            if (jsonReader.nextName().equals("powerprofile")) {
                                jsonReader.beginObject();
                                while (jsonReader.hasNext()) {
                                    String name = jsonReader.nextName();
                                    if (name.startsWith("cpu")) {
                                        String prefix = "C" + name.substring(3) + ": ";
                                        if (got_cpu) {
                                            cpu.append(" ");
                                        } else {
                                            got_cpu = true;
                                        }
                                        cpu.append(getFreq(jsonReader, prefix));
                                    } else if (name.equals("gpu")) {
                                        gpu = getFreq(jsonReader, "G: ");
                                    } else if (name.equals("ddr")) {
                                        ddr = getFreq(jsonReader, "D: ");
                                    } else if (name.equals("hmp")) {
                                        hmp = getHmp(jsonReader, "H: ");
                                    } else if (name.equals("therm")) {
                                        aptemp = getApTemp(jsonReader, "T: ");
                                    } else if (name.equals("batt")) {
                                        cur = getCurrent(jsonReader, "I: ");
                                    } else {
                                        jsonReader.skipValue();
                                    }
                                }
                                jsonReader.endObject();
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        if (jsonReader != null) {
                            try {
                                jsonReader.close();
                            } catch (IOException e9) {
                                Log.e(TAG, "Close error");
                            }
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (fIns != null) {
                            fIns.close();
                        }
                    } catch (RemoteException e10) {
                        reader = jsonReader;
                        inStream2 = inputStreamReader;
                        fileInputStream = fIns;
                    } catch (IOException e11) {
                        reader = jsonReader;
                        inStream2 = inputStreamReader;
                        fileInputStream = fIns;
                    } catch (Throwable th4) {
                        th = th4;
                        reader = jsonReader;
                        inStream2 = inputStreamReader;
                        fileInputStream = fIns;
                    }
                } catch (RemoteException e12) {
                    inStream = inputStreamReader;
                    fileInputStream = fIns;
                    cpu.append("C:?");
                    Log.e(TAG, "dump failed");
                    if (reader != null) {
                        reader.close();
                    }
                    if (inStream2 != null) {
                        inStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    list = new ArrayList();
                    sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                    Log.i(TAG, "getDebuggingString:" + sh);
                    list.add(sh);
                    list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                    return list;
                } catch (IOException e13) {
                    inStream = inputStreamReader;
                    fileInputStream = fIns;
                    Log.e(TAG, "IO exception");
                    if (reader != null) {
                        reader.close();
                    }
                    if (inStream2 != null) {
                        inStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    list = new ArrayList();
                    sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                    Log.i(TAG, "getDebuggingString:" + sh);
                    list.add(sh);
                    list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                    return list;
                } catch (Throwable th5) {
                    th = th5;
                    inStream = inputStreamReader;
                    fileInputStream = fIns;
                    if (reader != null) {
                        reader.close();
                    }
                    if (inStream2 != null) {
                        inStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (RemoteException e14) {
                cpu.append("C:?");
                Log.e(TAG, "dump failed");
                if (reader != null) {
                    reader.close();
                }
                if (inStream2 != null) {
                    inStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                list = new ArrayList();
                sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                Log.i(TAG, "getDebuggingString:" + sh);
                list.add(sh);
                list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                return list;
            } catch (IOException e15) {
                Log.e(TAG, "IO exception");
                if (reader != null) {
                    reader.close();
                }
                if (inStream2 != null) {
                    inStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                list = new ArrayList();
                sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
                Log.i(TAG, "getDebuggingString:" + sh);
                list.add(sh);
                list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
                return list;
            }
        }
        list = new ArrayList();
        sh = fps + " " + cpu.toString() + " " + hmp + " " + gpu + " " + ddr + " " + aptemp + " " + cur;
        Log.i(TAG, "getDebuggingString:" + sh);
        list.add(sh);
        list.add(sh.replaceAll(" ", "\n").replaceAll(",", "\n"));
        return list;
    }

    private String getFreq(JsonReader reader, String prefix) {
        long freq = 0;
        int restricted = 0;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("cur")) {
                    freq = normalizeValue(reader.nextLong());
                } else if (name.equals("restricted")) {
                    restricted = reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            Log.e(TAG, "getFreq IO exception");
        }
        String info = prefix + freq;
        if (restricted != 0) {
            return info + "!";
        }
        return info;
    }

    private long normalizeValue(long freq) {
        if (freq > 10000000) {
            return freq / 1000000;
        }
        if (freq > 10000) {
            return freq / 1000;
        }
        return freq;
    }

    private String getHmp(JsonReader reader, String prefix) {
        int up = 0;
        int down = 0;
        int restricted = 0;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("up")) {
                    up = reader.nextInt();
                } else if (name.equals("down")) {
                    down = reader.nextInt();
                } else if (name.equals("restricted")) {
                    restricted = reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            Log.e(TAG, "getHmp IO exception");
        }
        String info = prefix + up + " / " + down;
        if (restricted != 0) {
            return info + "!";
        }
        return info;
    }

    private String getApTemp(JsonReader reader, String prefix) {
        long ap_temp = 0;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.nextName().equals("system_h")) {
                    ap_temp = normalizeValue((long) reader.nextInt());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            Log.e(TAG, "getApTemp IO exception");
        }
        return prefix + ap_temp;
    }

    private String getCurrent(JsonReader reader, String prefix) {
        long current = 0;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.nextName().equals("current")) {
                    current = (long) reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            Log.e(TAG, "getCurrent IO exception");
        }
        return prefix + current;
    }

    public void updateReorient(int currentNavigationType, View currentView) {
        if (isSoftLockEnabled() && hasScreenLock(currentNavigationType)) {
            getScreenLockButton(currentView).setVisibility(0);
            getScreenLockButton(currentView).setContentDescription(this.mContext.getText(R.string.accessibility_screenlock));
            getScreenLockButton(currentView).setOnClickListener(this.mScreenLockClickListener);
        }
    }

    private boolean hasScreenLock(int currentNavigationType) {
        if (currentNavigationType == 4 || currentNavigationType == 5) {
            return true;
        }
        return false;
    }

    public boolean isCustExpandType(int currentNavigationType) {
        boolean z = true;
        if (!isSoftLockEnabled()) {
            return true;
        }
        if (currentNavigationType >= 4) {
            z = false;
        }
        return z;
    }

    private View getScreenLockButton(View currentView) {
        return currentView.findViewById(R.id.screenlock);
    }

    public ViewStub[] getCustomizedViewStubArray(ViewStub[] viewStubArray) {
        if (isSoftLockEnabled()) {
            return new ViewStub[12];
        }
        return viewStubArray;
    }

    public void populateScreenLockViewStubs(ViewStub[] viewStubs) {
        if (isSoftLockEnabled()) {
            viewStubs[8] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_screenlock_port);
            viewStubs[9] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_screenlock_port_swap);
            viewStubs[10] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_screenlock_land);
            viewStubs[11] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_screenlock_land_swap);
        }
    }

    public int getCustLayoutId(int currLayoutId, int rot, int navigationType) {
        if (!isSoftLockEnabled() || !hasScreenLock(navigationType)) {
            return currLayoutId;
        }
        int layoutId = currLayoutId;
        switch (rot) {
            case 0:
            case 2:
                switch (navigationType) {
                    case 4:
                        layoutId = 8;
                        break;
                    case 5:
                        layoutId = 9;
                        break;
                    default:
                        break;
                }
            case 1:
            case 3:
                switch (navigationType) {
                    case 4:
                        layoutId = 10;
                        break;
                    case 5:
                        layoutId = 11;
                        break;
                    default:
                        break;
                }
        }
        return layoutId;
    }

    private boolean isSoftLockEnabled() {
        return SystemProperties.getBoolean("ro.config.soft_lock_enable", false);
    }
}
