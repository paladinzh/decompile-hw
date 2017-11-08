package com.huawei.systemmanager.antimal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UserBehaviorManager {
    public static final String ACTION_DATE_CHANGED = "android.intent.action.TIME_SET";
    public static final String ACTION_NEW_CALL = "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    public static final String ACTION_NEW_PICTURE = "com.android.camera.NEW_PICTURE";
    private static final String CALL_DURATION = "duration";
    private static final int NUM_STOP_COLLECTION = 5;
    public static final int STATUS_TYPE_DATE_CHANGED = 4;
    public static final int STATUS_TYPE_DEFAULT = 0;
    public static final int STATUS_TYPE_INSTALL_MAL = 8;
    public static final int STATUS_TYPE_NEW_CALL = 2;
    public static final int STATUS_TYPE_NEW_PICTURE = 1;
    public static final String TAG = "UserBehaviorManager";
    private boolean isReceiverRegistered = false;
    private int mBehaviorStatus = 0;
    private Context mContext = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private String mUserBePath = null;
    private List<UserBehavior> mUserBehaviorList = null;
    public BroadcastReceiver mUserBehaviorReceiver = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                if (intent.getAction().equals(UserBehaviorManager.ACTION_NEW_PICTURE)) {
                    UserBehaviorManager.this.obtainBehavior(1);
                } else if (intent.getAction().equals(UserBehaviorManager.ACTION_NEW_CALL)) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
                    if (!(tm == null || tm.getSimState() == 1 || intent.getLongExtra(UserBehaviorManager.CALL_DURATION, 0) <= 0)) {
                        UserBehaviorManager.this.obtainBehavior(2);
                    }
                } else if (intent.getAction().equals(UserBehaviorManager.ACTION_DATE_CHANGED)) {
                    UserBehaviorManager.this.obtainBehavior(4);
                }
            }
        }
    };
    private int mUserBehaviorSequence = 0;
    private String oldUserBePath = null;

    public static class UserBehavior {
        public int count;
        public int sequence;
        public int type;

        public String toString() {
            return "UserBehavior: type=" + this.type + " sequence=" + this.sequence + "count =" + this.count;
        }
    }

    public UserBehaviorManager(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("UserBehaviorCollectionThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        UserBehaviorManager.this.insertUserBehavior(2);
                        return;
                    case 2:
                        UserBehaviorManager.this.insertUserBehavior(1);
                        return;
                    case 4:
                        UserBehaviorManager.this.insertUserBehavior(4);
                        return;
                    default:
                        HwLog.e(UserBehaviorManager.TAG, "obtain error message");
                        return;
                }
            }
        };
    }

    public int getBehaviorStatus() {
        int i;
        synchronized (UserBehaviorManager.class) {
            i = this.mBehaviorStatus;
        }
        return i;
    }

    public void init(boolean needCollect, boolean upgrade, String antimalPath, String oldPath) {
        this.mUserBePath = antimalPath + "/" + MalwareConst.ANTI_MAL_USER_BEHAVIOR_FILE;
        if (upgrade) {
            this.oldUserBePath = oldPath + "/" + MalwareConst.ANTI_MAL_USER_BEHAVIOR_FILE;
            readUserBehaviorFromXmlFile(this.oldUserBePath);
            saveUserBehaviorToXmlFile(this.mUserBePath);
        } else {
            readUserBehaviorFromXmlFile(this.mUserBePath);
        }
        synchronized (UserBehaviorManager.class) {
            if (this.mUserBehaviorList != null) {
                this.mUserBehaviorSequence = this.mUserBehaviorList.size();
            }
        }
        if (needCollect) {
            registerUserBehaviorReceiver();
        }
    }

    public void registerUserBehaviorReceiver() {
        IntentFilter pictureFilter = new IntentFilter();
        IntentFilter callAndDateChangeFilter = new IntentFilter();
        try {
            pictureFilter.addAction(ACTION_NEW_PICTURE);
            pictureFilter.addDataType("image/*");
        } catch (MalformedMimeTypeException e) {
            HwLog.e(TAG, "MalformedInputException :" + e);
        }
        callAndDateChangeFilter.addAction(ACTION_NEW_CALL);
        callAndDateChangeFilter.addAction(ACTION_DATE_CHANGED);
        this.mContext.registerReceiver(this.mUserBehaviorReceiver, pictureFilter);
        this.mContext.registerReceiver(this.mUserBehaviorReceiver, callAndDateChangeFilter);
        this.isReceiverRegistered = true;
    }

    public void unregisterUserBeReceiver() {
        if (this.isReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mUserBehaviorReceiver);
            this.isReceiverRegistered = false;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
        }
    }

    private void obtainBehavior(int type) {
        Message msg = Message.obtain();
        msg.what = type;
        this.mHandler.sendMessage(msg);
    }

    public void insertUserBehavior(int type) {
        synchronized (UserBehaviorManager.class) {
            if (this.mUserBehaviorList == null) {
                HwLog.i(TAG, "insertUserBehavior's input parameter error.");
                this.mUserBehaviorList = new ArrayList();
                addNewBehavior(type);
            } else {
                boolean isNewBehaviorType = true;
                for (UserBehavior userBe : this.mUserBehaviorList) {
                    if (userBe.type == type) {
                        if (userBe.count >= 5) {
                            return;
                        } else {
                            userBe.count++;
                            isNewBehaviorType = false;
                        }
                    }
                }
                if (isNewBehaviorType) {
                    addNewBehavior(type);
                }
            }
            saveUserBehaviorToXmlFile(this.mUserBePath);
        }
    }

    private void addNewBehavior(int type) {
        UserBehavior behavior = new UserBehavior();
        behavior.type = type;
        this.mUserBehaviorSequence++;
        behavior.sequence = this.mUserBehaviorSequence;
        behavior.count = 1;
        this.mUserBehaviorList.add(behavior);
    }

    private void saveUserBehaviorToXmlFile(String path) {
        BufferedOutputStream outStream;
        FileNotFoundException filenfe;
        Object fos;
        Throwable th;
        IOException ioe;
        Exception e;
        Object outStream2;
        synchronized (UserBehaviorManager.class) {
            if (TextUtils.isEmpty(path)) {
                HwLog.e(TAG, "saveUserBehaviorToXmlFile path is null");
            } else if (this.mUserBehaviorList == null) {
                HwLog.e(TAG, "Behaviour list is null,write file error");
            } else {
                AutoCloseable autoCloseable = null;
                AutoCloseable autoCloseable2 = null;
                try {
                    FileOutputStream fos2 = new FileOutputStream(path, false);
                    try {
                        outStream = new BufferedOutputStream(fos2);
                    } catch (FileNotFoundException e2) {
                        filenfe = e2;
                        fos = fos2;
                        try {
                            HwLog.e(TAG, "saveUserBehaviorToXmlFile FileNotFoundException:" + filenfe);
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    } catch (IOException e3) {
                        ioe = e3;
                        fos = fos2;
                        HwLog.e(TAG, "saveUserBehaviorToXmlFile IOException:" + ioe);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e4) {
                        e = e4;
                        fos = fos2;
                        HwLog.e(TAG, "saveUserBehaviorToXmlFile Exception:" + e);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Throwable th3) {
                        th = th3;
                        fos = fos2;
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                    try {
                        XmlSerializer serializer = Xml.newSerializer();
                        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                        serializer.setOutput(outStream, "UTF-8");
                        serializer.startDocument("UTF-8", Boolean.valueOf(true));
                        serializer.startTag(null, MalwareConst.BEHAVIOR_LIST);
                        for (UserBehavior behavior : this.mUserBehaviorList) {
                            serializer.startTag(null, MalwareConst.USER_BEHAVIOR);
                            serializer.attribute(null, "type", String.valueOf(behavior.type));
                            serializer.attribute(null, MalwareConst.USER_BEHAVIOR_SEQUENCE, String.valueOf(behavior.sequence));
                            serializer.attribute(null, "count", String.valueOf(behavior.count));
                            serializer.endTag(null, MalwareConst.USER_BEHAVIOR);
                        }
                        serializer.endTag(null, MalwareConst.BEHAVIOR_LIST);
                        serializer.endDocument();
                        outStream.flush();
                        fos2.flush();
                        IoUtils.closeQuietly(outStream);
                        IoUtils.closeQuietly(fos2);
                    } catch (FileNotFoundException e5) {
                        filenfe = e5;
                        autoCloseable2 = outStream;
                        autoCloseable = fos2;
                        HwLog.e(TAG, "saveUserBehaviorToXmlFile FileNotFoundException:" + filenfe);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (IOException e6) {
                        ioe = e6;
                        outStream2 = outStream;
                        fos = fos2;
                        HwLog.e(TAG, "saveUserBehaviorToXmlFile IOException:" + ioe);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e7) {
                        e = e7;
                        outStream2 = outStream;
                        fos = fos2;
                        HwLog.e(TAG, "saveUserBehaviorToXmlFile Exception:" + e);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Throwable th4) {
                        th = th4;
                        outStream2 = outStream;
                        fos = fos2;
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    filenfe = e8;
                    HwLog.e(TAG, "saveUserBehaviorToXmlFile FileNotFoundException:" + filenfe);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (IOException e9) {
                    ioe = e9;
                    HwLog.e(TAG, "saveUserBehaviorToXmlFile IOException:" + ioe);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Exception e10) {
                    e = e10;
                    HwLog.e(TAG, "saveUserBehaviorToXmlFile Exception:" + e);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readUserBehaviorFromXmlFile(String path) {
        FileNotFoundException filenfe;
        XmlPullParserException xmlPPe;
        IOException ioe;
        Throwable th;
        synchronized (UserBehaviorManager.class) {
            if (TextUtils.isEmpty(path)) {
                HwLog.e(TAG, "readUserBehaviorFromXmlFile path is null");
                return;
            }
            File userBehviorXml = new File(path);
            if (userBehviorXml.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    XmlPullParser xmlParser = Xml.newPullParser();
                    FileInputStream inStream = new FileInputStream(userBehviorXml);
                    try {
                        xmlParser.setInput(inStream, "UTF-8");
                        int eventType = xmlParser.getEventType();
                        UserBehavior behavior = null;
                        while (eventType != 1) {
                            UserBehavior behavior2;
                            switch (eventType) {
                                case 0:
                                    try {
                                        HwLog.d(TAG, "Start to read the XML file");
                                        behavior2 = behavior;
                                        break;
                                    } catch (FileNotFoundException e) {
                                        filenfe = e;
                                        fileInputStream = inStream;
                                        break;
                                    } catch (XmlPullParserException e2) {
                                        xmlPPe = e2;
                                        behavior2 = behavior;
                                        fileInputStream = inStream;
                                        break;
                                    } catch (IOException e3) {
                                        ioe = e3;
                                        behavior2 = behavior;
                                        fileInputStream = inStream;
                                        break;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        fileInputStream = inStream;
                                        break;
                                    }
                                case 2:
                                    String name = xmlParser.getName();
                                    if (MalwareConst.BEHAVIOR_LIST.equals(xmlParser.getName())) {
                                        this.mUserBehaviorList = new ArrayList();
                                    }
                                    if (!MalwareConst.USER_BEHAVIOR.equals(name)) {
                                        behavior2 = behavior;
                                        break;
                                    }
                                    behavior2 = new UserBehavior();
                                    if ("type".equals(xmlParser.getAttributeName(0))) {
                                        behavior2.type = Integer.parseInt(xmlParser.getAttributeValue(null, "type"));
                                    }
                                    if (MalwareConst.USER_BEHAVIOR_SEQUENCE.equals(xmlParser.getAttributeName(1))) {
                                        behavior2.sequence = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.USER_BEHAVIOR_SEQUENCE));
                                    }
                                    if ("count".equals(xmlParser.getAttributeName(2))) {
                                        behavior2.count = Integer.parseInt(xmlParser.getAttributeValue(null, "count"));
                                        break;
                                    }
                                    break;
                                case 3:
                                    if (MalwareConst.USER_BEHAVIOR.equals(xmlParser.getName())) {
                                        this.mUserBehaviorList.add(behavior);
                                    }
                                    if (MalwareConst.BEHAVIOR_LIST.equals(xmlParser.getName())) {
                                        HwLog.i(TAG, "Finish reading UserBehaviorFile");
                                    }
                                    behavior2 = behavior;
                                    break;
                                default:
                                    behavior2 = behavior;
                                    break;
                            }
                        }
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (IOException e4) {
                                HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + e4);
                            }
                        }
                    } catch (FileNotFoundException e5) {
                        filenfe = e5;
                        fileInputStream = inStream;
                    } catch (XmlPullParserException e6) {
                        xmlPPe = e6;
                        fileInputStream = inStream;
                    } catch (IOException e7) {
                        ioe = e7;
                        fileInputStream = inStream;
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream = inStream;
                    }
                } catch (FileNotFoundException e8) {
                    filenfe = e8;
                    try {
                        HwLog.e(TAG, "readMalStatusFile FileNotFoundException:" + filenfe);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e42) {
                                HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + e42);
                            }
                        }
                        return;
                    } catch (Throwable th4) {
                        th = th4;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e422) {
                                HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + e422);
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e9) {
                    xmlPPe = e9;
                    HwLog.e(TAG, "readUserBehaviorFromXmlFile XmlPullParserException:" + xmlPPe);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4222) {
                            HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + e4222);
                        }
                    }
                    return;
                } catch (IOException e10) {
                    ioe = e10;
                    HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + ioe);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e42222) {
                            HwLog.e(TAG, "readUserBehaviorFromXmlFile IOException:" + e42222);
                        }
                    }
                    return;
                }
            }
            HwLog.i(TAG, "readUserBehaviorFromXmlFile file:" + path + " is not exist.");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean analyze() {
        boolean z = true;
        synchronized (UserBehaviorManager.class) {
            if (this.mUserBehaviorList == null || this.mUserBehaviorList.size() == 0) {
                HwLog.e(TAG, "Input parameter for analysis is null.");
                return false;
            }
            int callCount = 0;
            int newPicCount = 0;
            int installMalNo = 0;
            for (UserBehavior behavior : this.mUserBehaviorList) {
                if (behavior.type == 3) {
                    installMalNo = behavior.sequence;
                }
            }
            if (installMalNo == 0) {
                HwLog.i(TAG, "Analysed installMalNo==0");
                return false;
            }
            for (UserBehavior userBe : this.mUserBehaviorList) {
                if (userBe.type == 1 && userBe.sequence < installMalNo) {
                    this.mBehaviorStatus |= 2;
                    callCount = userBe.count;
                }
                if (userBe.type == 2 && userBe.sequence < installMalNo) {
                    this.mBehaviorStatus |= 1;
                    newPicCount = userBe.count;
                }
                if (userBe.type == 4 && userBe.sequence < installMalNo) {
                    this.mBehaviorStatus |= 4;
                }
            }
            if (callCount <= 0 || newPicCount <= 0) {
                z = false;
            }
        }
    }
}
