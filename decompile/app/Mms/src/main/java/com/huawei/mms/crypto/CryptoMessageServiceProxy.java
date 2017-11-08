package com.huawei.mms.crypto;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.mms.transaction.CryptoSmsReceiverService;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cryptosms.CryptoMessageManager;
import com.huawei.cryptosms.CryptoMessageManager.ConnectCallBack;
import com.huawei.cryptosms.ICryptoMessageClient;
import com.huawei.cryptosms.ICryptoMessageService;
import com.huawei.cryptosms.ICryptoMessageService.Stub;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.util.StatisticalHelper;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CryptoMessageServiceProxy {
    private static boolean LOCAL_DEBUG = false;
    private static ICryptoMessageService iCryptoMessageService = null;
    private static HashMap<String, String> mAccountHashCodeMap = new HashMap();
    private static CallBack mCallBack;
    private static ConnectCallBack mConnectCallBack = new ConnectCallBack() {
        public void connectSuccess() {
            MLog.d("CryptoMessageServiceProxy", "start ConnectCallBack connectSuccess method");
            CryptoMessageServiceProxy.reTryConnect(false);
        }
    };
    private static Handler mConnectHandler;
    private static HandlerThread mConnectThread;
    private static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            CryptoMessageServiceProxy.iCryptoMessageService = null;
            MLog.d("CryptoMessageServiceProxy", "onServiceDisconnected(ComponentName name) method begin execute,and not connect sms encryption service");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            MLog.d("CryptoMessageServiceProxy", "onServiceConnected(ComponentName name, IBinder service) method begin execute,and begin connect sms encryption service");
            CryptoMessageServiceProxy.iCryptoMessageService = Stub.asInterface(service);
            if (CryptoMessageServiceProxy.mServiceProxy == null) {
                MLog.d("CryptoMessageServiceProxy", "onServiceConnected: initialize the sms encryption service");
                CryptoMessageServiceProxy.mServiceProxy = new CryptoMessageServiceProxy();
                CryptoMessageServiceProxy.mCallBack = new CallBack();
                if (CryptoMessageServiceProxy.LOCAL_DEBUG) {
                    CryptoMessageServiceStub.registerCallback(CryptoMessageServiceProxy.mCallBack);
                    return;
                }
                CryptoMessageServiceProxy.mCryptoMessageManager = new CryptoMessageManager();
                CryptoMessageServiceProxy.mCryptoMessageManager.init(CryptoMessageServiceProxy.mContext);
                CryptoMessageServiceProxy.registerCallback();
            }
        }
    };
    private static Context mContext = null;
    private static CryptoMessageManager mCryptoMessageManager;
    private static CryptoSmsReceiverService mCryptoSmsReceiverService = new CryptoSmsReceiverService();
    private static DeathRecipient mDeathRecipint = new DeathRecipient() {
        public void binderDied() {
            MLog.d("CryptoMessageServiceProxy", "Binder is dead");
            Message message = CryptoMessageServiceProxy.mConnectHandler.obtainMessage();
            message.what = 1000;
            CryptoMessageServiceProxy.mConnectHandler.sendMessage(message);
        }
    };
    private static byte[] mFakeUKey = new byte[64];
    private static Handler mHandler = new Handler();
    private static boolean mIsServiceReady = false;
    private static ArrayList<Handler> mRegisteredHandlers = new ArrayList();
    private static int mServiceLinkReady = 0;
    private static CryptoMessageServiceProxy mServiceProxy;
    private static int tryConnectCount = 0;

    static class CallBack extends ICryptoMessageClient.Stub {
        CallBack() {
        }

        public void onMessage(int what, int arg1, String arg2) throws RemoteException {
            CryptoMessageServiceProxy.notifyActivationResult(what, arg1, arg2);
            for (Handler handler : CryptoMessageServiceProxy.mRegisteredHandlers) {
                Message msg = Message.obtain(handler, what);
                msg.arg1 = arg1;
                msg.obj = arg2;
                handler.sendMessage(msg);
            }
        }
    }

    private static class ConnectHandler extends Handler {
        public ConnectHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    CryptoMessageServiceProxy.mServiceLinkReady = 1;
                    CryptoMessageServiceProxy.mIsServiceReady = false;
                    CryptoMessageServiceProxy.initService(CryptoMessageServiceProxy.mContext);
                    return;
                case 1001:
                    CryptoMessageServiceProxy.onTryConnect();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        Arrays.fill(mFakeUKey, (byte) 65);
    }

    private CryptoMessageServiceProxy() {
    }

    public static void init(Context context) {
        MLog.d("CryptoMessageServiceProxy", "init(Context context) method begin init sms encryption service");
        if (context == null) {
            MLog.e("CryptoMessageServiceProxy", "init(Context context) method bad parameter for context is null");
            return;
        }
        mContext = context;
        initService(context);
        mCryptoSmsReceiverService.onCreate();
    }

    public static void initService(Context context) {
        if (mServiceProxy == null) {
            mServiceProxy = new CryptoMessageServiceProxy();
            mCallBack = new CallBack();
            createConnectHandler();
        }
        if (LOCAL_DEBUG) {
            CryptoMessageServiceStub.registerCallback(mCallBack);
            return;
        }
        destoryCryptoMessageManager();
        mCryptoMessageManager = new CryptoMessageManager();
        mCryptoMessageManager.setConnectCallBack(mConnectCallBack);
        mCryptoMessageManager.init(mContext);
        mServiceLinkReady = 0;
    }

    private static void destoryCryptoMessageManager() {
        if (mCryptoMessageManager != null) {
            mCryptoMessageManager.setConnectCallBack(null);
            mCryptoMessageManager.unregisterCallback(mCallBack);
            mCryptoMessageManager.setmService(null);
        }
    }

    private static void createConnectHandler() {
        mConnectThread = new HandlerThread("CryptoMessageServiceProxy");
        mConnectThread.start();
        mConnectHandler = new ConnectHandler(mConnectThread.getLooper());
    }

    private static void destoryConnectHander() {
        mConnectHandler = null;
    }

    private static void onTryConnect() {
        if (linkToDeath()) {
            MLog.d("CryptoMessageServiceProxy", "Binder linttodeath is success");
            if (LOCAL_DEBUG) {
                CryptoMessageServiceStub.registerCallback(mCallBack);
                return;
            } else if (registerCallback()) {
                mServiceLinkReady = 2;
                mIsServiceReady = true;
                tryConnectCount = 0;
                return;
            } else if (tryConnectCount < 30) {
                tryConnectCount++;
                reTryConnect(true);
                return;
            } else {
                tryConnectCount = 0;
                return;
            }
        }
        MLog.d("CryptoMessageServiceProxy", "Binder is not linktodeath");
    }

    private static void reTryConnect(boolean isTimeDelay) {
        if (mConnectHandler != null) {
            MLog.d("CryptoMessageServiceProxy", "start method tryConnect");
            Message message = mConnectHandler.obtainMessage();
            message.what = 1001;
            if (isTimeDelay) {
                mConnectHandler.sendMessageDelayed(message, 200);
            } else {
                mConnectHandler.sendMessage(message);
            }
        }
    }

    private static boolean registerCallback() {
        int result = mCryptoMessageManager.registerCallback(mCallBack);
        if (result == -1) {
            MLog.d("CryptoMessageServiceProxy", "registerCallback() method register callback failed, count is :" + tryConnectCount);
            return false;
        } else if (result == 0) {
            MLog.d("CryptoMessageServiceProxy", "registerCallback() method register callback success, count is: " + tryConnectCount);
            return true;
        } else {
            MLog.e("CryptoMessageServiceProxy", "registerCallback() method register callback time out, count is:" + tryConnectCount);
            return false;
        }
    }

    private static boolean linkToDeath() {
        if (mServiceLinkReady != 0) {
            return true;
        }
        if (!mCryptoMessageManager.linkToDeath(mDeathRecipint)) {
            return false;
        }
        mServiceLinkReady = 1;
        mIsServiceReady = false;
        return true;
    }

    private static void unLinkToDeath() {
        if (mDeathRecipint != null) {
            mCryptoMessageManager.unLinkToDeath(mDeathRecipint);
        }
    }

    public static void deInit() {
        MLog.d("CryptoMessageServiceProxy", "deInit() method begin deinitialize the sms encryption service");
        if (LOCAL_DEBUG) {
            CryptoMessageServiceStub.unregisterCallback(mCallBack);
        } else {
            unLinkToDeath();
            mServiceLinkReady = 0;
            destoryConnectHander();
            if (mConnectThread != null) {
                mConnectThread.getLooper().quit();
            }
            if (mIsServiceReady) {
                mCryptoMessageManager.unregisterCallback(mCallBack);
            }
        }
        mContext = null;
        mServiceProxy = null;
        mIsServiceReady = false;
        mCryptoMessageManager = null;
        mCryptoSmsReceiverService.onDestroy();
    }

    public static void addListener(Handler handler) {
        if (handler != null && !mRegisteredHandlers.contains(handler)) {
            mRegisteredHandlers.add(handler);
        }
    }

    public static void removeListener(Handler handler) {
        if (handler != null && mRegisteredHandlers.contains(handler)) {
            mRegisteredHandlers.remove(handler);
        }
    }

    private static void notifyActivationResult(final int msgWhat, int msgArg, final String msgObj) {
        MLog.d("CryptoMessageServiceProxy", "notifyActivationResult() method: notifyActivationResult start");
        int activeStatus = 0;
        AccountManager accountManager = AccountManager.getInstance();
        switch (msgWhat) {
            case 5:
                CryptoMessageUtil.clearImsiState();
                accountManager.setShouldBindFingerPrompt(mContext, true);
                activeStatus = 1;
                break;
            case 20:
            case 21:
            case 22:
            case 24:
            case 25:
            case 26:
            case 27:
                activeStatus = 2;
                break;
            case 23:
                String accountName = "";
                int subId = 0;
                try {
                    accountName = msgObj.substring(2, msgObj.length());
                    subId = Integer.parseInt(msgObj.substring(0, 1));
                } catch (Exception e) {
                    MLog.e("CryptoMessageServiceProxy", "notifyActivationResult() method:exception in notifyActivationResult", (Throwable) e);
                }
                accountManager.setNeedUnbindPrompt(true);
                accountManager.setNeedUnbindSubId(subId);
                accountManager.setNeedUnbindAccountName(accountName);
                activeStatus = 2;
                break;
            case AMapException.CODE_AMAP_ID_NOT_EXIST /*2001*/:
                if (accountManager.isNeedReactiveLater(mContext)) {
                    MLog.d("CryptoMessageServiceProxy", "notifyActivationResult() method:setNeedReactive when simcard state change");
                    accountManager.setNeedReactive(mContext);
                    break;
                }
                break;
            case 3001:
                MLog.d("CryptoMessageServiceProxy", "notifyActivationResult() method: new key version found, subId index is: " + msgArg);
                accountManager.setNeedReactiveSubId(msgArg);
                accountManager.setNeedReactive(mContext);
                break;
        }
        if (activeStatus == 1) {
            StatisticalHelper.incrementReportCount(mContext, 2156);
        } else if (activeStatus == 2) {
            StatisticalHelper.incrementReportCount(mContext, 2157);
        }
        mHandler.post(new Runnable() {
            public void run() {
                int tipStringId = 0;
                if (!MessageUtils.isMultiSimEnabled()) {
                    switch (msgWhat) {
                        case 5:
                            tipStringId = R.string.open_encrypt_sms_function_success;
                            break;
                        case 20:
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                        case 26:
                        case 27:
                            tipStringId = R.string.open_encrypt_sms_function_failed;
                            break;
                        case 21:
                            tipStringId = R.string.activate_send_sms_failed;
                            break;
                        default:
                            break;
                    }
                }
                switch (msgWhat) {
                    case 5:
                        if (CryptoMessageServiceProxy.getSubIdFromMessage(msgWhat, msgObj) != 0) {
                            tipStringId = R.string.card2_open_encrypt_sms_function_success;
                            break;
                        } else {
                            tipStringId = R.string.card1_open_encrypt_sms_function_success;
                            break;
                        }
                    case 20:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                        if (CryptoMessageServiceProxy.getSubIdFromMessage(msgWhat, msgObj) != 0) {
                            tipStringId = R.string.card2_open_encrypt_sms_function_failed;
                            break;
                        } else {
                            tipStringId = R.string.card1_open_encrypt_sms_function_failed;
                            break;
                        }
                    case 21:
                        tipStringId = R.string.activate_send_sms_failed;
                        break;
                }
                if (tipStringId != 0) {
                    Toast.makeText(new ContextThemeWrapper(CryptoMessageServiceProxy.mContext, CryptoMessageServiceProxy.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)), tipStringId, 0).show();
                }
            }
        });
    }

    private static int getSubIdFromMessage(int msgWhat, String msgObj) {
        if (msgWhat != 23) {
            return Integer.parseInt(msgObj);
        }
        try {
            return Integer.parseInt(msgObj.substring(0, 1));
        } catch (Exception e) {
            MLog.e("CryptoMessageServiceProxy", "getSubIdFromMessage(int msgWhat, String msgObj) method:exception in getSubIdFromMessage", (Throwable) e);
            return 0;
        }
    }

    public static boolean isServiceReady() {
        return mIsServiceReady;
    }

    public static boolean isLocalEncrypted(String msg) {
        return 4 == getEncryptedType(msg);
    }

    public static boolean isNetworkEncryptedConversation(String msg) {
        return 2 == getEncryptedType(msg);
    }

    public static boolean isNetworkEncryptoRegisterMessage(String msg) {
        return 1 == getEncryptedType(msg);
    }

    public static boolean isLocalStoredNEMsg(String msg) {
        return 3 == getEncryptedType(msg);
    }

    public static int getEncryptedType(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return 0;
        }
        int wholeTextLen = msg.length();
        if (wholeTextLen < 9) {
            return 0;
        }
        int headIndex = msg.indexOf("CM=>");
        if (headIndex < 0) {
            return 0;
        }
        if (isRegisterResponseMessage(msg, headIndex)) {
            MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: it is a register response message");
            return 1;
        }
        char version = msg.charAt(wholeTextLen - 1);
        if (!isVersionValid(version)) {
            MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: version is invalid: " + version);
            return 0;
        } else if (headIndex != 0) {
            return 0;
        } else {
            int bodyLength = parseLengthToDigit(msg.substring(5, 9));
            if (bodyLength < 1 || bodyLength + 9 > wholeTextLen) {
                MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: the encrypted message real length is invalid");
                return 0;
            }
            char type = msg.charAt(4);
            if ('2' == type) {
                if (bodyLength + 10 == wholeTextLen) {
                    byte[] bMsg = stringToByte(removeEncryptPackage(msg));
                    try {
                        Base64.decode(bMsg, 0, bMsg.length, 0);
                        MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: it is a network conversation message");
                        return 2;
                    } catch (Exception e) {
                        MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: base64 decode failed");
                        return 0;
                    }
                }
            } else if ('4' == type) {
                if (bodyLength + 12 > wholeTextLen) {
                    MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: the whole message length is too short");
                    return 0;
                }
                int accountLengthIndex = bodyLength + 9;
                int accountIndex = accountLengthIndex + 2;
                int accountLength = parseLengthToDigit(msg.substring(accountLengthIndex, accountIndex));
                if (accountLength < 1) {
                    return 0;
                }
                String account = msg.substring(accountIndex, wholeTextLen - 1);
                if (account.isEmpty() || account.length() != accountLength) {
                    return 0;
                }
                if ((accountIndex + accountLength) + 1 == wholeTextLen) {
                    MLog.d("CryptoMessageServiceProxy", "getEncryptedType(String msg) method: it is a local encrypted message");
                    return 4;
                }
            } else if ('5' == type) {
                if (bodyLength + 12 > wholeTextLen) {
                    return 0;
                }
                int imsiLengthIndex = bodyLength + 9;
                int imsiIndex = imsiLengthIndex + 2;
                int imsiLength = parseLengthToDigit(msg.substring(imsiLengthIndex, imsiIndex));
                if (imsiLength < 1) {
                    return 0;
                }
                String imsi = msg.substring(imsiIndex, wholeTextLen - 1);
                if (imsi.isEmpty() || imsi.length() != imsiLength) {
                    return 0;
                }
                if ((imsiIndex + imsiLength) + 1 == wholeTextLen) {
                    MLog.i("CryptoMessageServiceProxy", "getEncryptedType: it is a local stored network encrypted message");
                    return 3;
                }
            }
            return 0;
        }
    }

    public static String getAccountFromLEMsg(String msg, boolean isLEMsg) {
        if (TextUtils.isEmpty(msg)) {
            MLog.e("CryptoMessageServiceProxy", "getAccountFromLEMsg(String msg, boolean isLEMsg) method: msg is null or empty");
            return "";
        } else if (isLEMsg || isLocalEncrypted(msg)) {
            return msg.substring((parseLengthToDigit(msg.substring(5, 9)) + 9) + 2, msg.length() - 1);
        } else {
            MLog.e("CryptoMessageServiceProxy", "getAccountFromLEMsg(String msg, boolean isLEMsg) method: it is not a local encrypted message");
            return "";
        }
    }

    public static String localEncrypt(String msg, int subID) {
        if (TextUtils.isEmpty(msg) || !mIsServiceReady || 9999 < msg.length() + 19) {
            MLog.e("CryptoMessageServiceProxy", "localEncrypt(String msg, int subID): parameter error,crypto sms service is not ready,mIsServiceReady is null, or message body length overflow.");
            return "";
        }
        char version = 'A';
        try {
            if (msg.equals(new String(msg.getBytes("US-ASCII"), "US-ASCII"))) {
                version = 'a';
            } else {
                version = 'A';
            }
        } catch (UnsupportedEncodingException e) {
            MLog.e("CryptoMessageServiceProxy", "localEncrypt() method :it unsupport encoding");
        }
        String account = CryptoMessageUtil.getHuaweiAccount(subID);
        if (TextUtils.isEmpty(account) || 99 < account.length()) {
            MLog.e("CryptoMessageServiceProxy", "localEncrypt() method : account is invalid");
            return "";
        }
        String tmpMsg = mCryptoMessageManager.encryptData(new StringBuffer(getHashCode(account)).append(msg).toString());
        if (!TextUtils.isEmpty(tmpMsg)) {
            return addEncryptPackage(tmpMsg, 4, subID, account, version);
        }
        MLog.e("CryptoMessageServiceProxy", "localEncrypt() method: message encrypt failed from crypto sms service.");
        return "";
    }

    public static String localDecrypt(String msg, boolean isLocalEncrypted) {
        if (!mIsServiceReady) {
            MLog.e("CryptoMessageServiceProxy", "localDecrypt() method: crypto sms serive is not ready,mIsServiceReady is null");
            return "";
        } else if (isLocalEncrypted || isLocalEncrypted(msg)) {
            String tmpMsg = mCryptoMessageManager.decryptData(removeEncryptPackage(msg));
            if (TextUtils.isEmpty(tmpMsg)) {
                MLog.e("CryptoMessageServiceProxy", "localDecrypt() method: message is null or empty.");
                return "";
            }
            String account = getAccountFromLEMsg(msg, true);
            int len = getHashCode(account).length();
            if (getHashCode(account).equals(tmpMsg.substring(0, len))) {
                return tmpMsg.substring(len, tmpMsg.length());
            }
            MLog.e("CryptoMessageServiceProxy", "localDecrypt() method: decrypt failed, account miss-match.");
            return "";
        } else {
            MLog.e("CryptoMessageServiceProxy", "localDecrypt() method: it is not a local encrypted message");
            return "";
        }
    }

    public static String localDecrypt(String msg) {
        return localDecrypt(msg, false);
    }

    public static String localStoreNEMsg(String msg, int subID) {
        if (2 != getEncryptedType(msg)) {
            MLog.e("CryptoMessageServiceProxy", "localStoreNEMsg: error message type error ");
            return msg;
        }
        String tmpMsg = addEncryptPackage(removeEncryptPackage(msg), 3, subID, null, msg.charAt(msg.length() - 1));
        if (!TextUtils.isEmpty(tmpMsg)) {
            return tmpMsg;
        }
        MLog.e("CryptoMessageServiceProxy", "localStoreNEMsg(String msg, int subID) method: message encrypt failed from add package.");
        return msg;
    }

    public static String decryptLocalStoredNEMsg(String msg, boolean isLocalStoredNEMsg) {
        if (!mIsServiceReady) {
            MLog.e("CryptoMessageServiceProxy", "decryptLocalStoredNEMsg(String msg, boolean isLocalStoredNEMsg) method: crypto sms service is not ready,mIsServiceReady is null");
            return "";
        } else if (isLocalStoredNEMsg || isLocalStoredNEMsg(msg)) {
            int subID = CryptoMessageUtil.getSubIDByImsi(getImsiFromLSNEMsg(msg, true));
            if (-1 == subID) {
                MLog.e("CryptoMessageServiceProxy", "decryptLocalStoreNEMsg() method: sub id is invalid");
                return "";
            }
            char version = msg.charAt(msg.length() - 1);
            char encodeVersion = version;
            version = Character.toLowerCase(version);
            byte[] bMsg = stringToByte(removeEncryptPackage(msg));
            bMsg = Base64.decode(bMsg, 0, bMsg.length, 0);
            int size = bMsg.length;
            bMsg = Arrays.copyOf(bMsg, size + 1);
            bMsg[size] = (byte) version;
            bMsg = mCryptoMessageManager.decryptMessage(bMsg, subID);
            if (bMsg == null || bMsg.length < 1) {
                MLog.e("CryptoMessageServiceProxy", "decryptLocalStoreNEMsg() method: message decrypt failed from crypto sms service.");
                return "";
            }
            try {
                String tmpMsg;
                if (Character.isLowerCase(encodeVersion)) {
                    tmpMsg = new String(bMsg, "UTF-8");
                } else if (Character.isUpperCase(encodeVersion)) {
                    tmpMsg = new String(bMsg, "UTF-16");
                } else {
                    MLog.e("CryptoMessageServiceProxy", "decryptLocalStoredNEMsg() method: charactor sequence type error,it is not UTF-8 or UTF-16");
                    return "";
                }
                MLog.e("CryptoMessageServiceProxy", "decryptLocalStoredNEMsg() method: Decrypt the local stored network encrypted message success");
                return tmpMsg;
            } catch (UnsupportedEncodingException e) {
                MLog.e("CryptoMessageServiceProxy", "decryptLocalStoredNEMsg() method: it unsupport encoding,it is not UTF-8 or UTF-16");
                return "";
            }
        } else {
            MLog.e("CryptoMessageServiceProxy", "decryptLocalStoredNEMsg() method: it is not a local stored network encrypted message");
            return "";
        }
    }

    public static String getImsiFromLSNEMsg(String msg, boolean isLSNEMsg) {
        if (TextUtils.isEmpty(msg)) {
            MLog.e("CryptoMessageServiceProxy", "getImsiFromLSNEMsg(String msg, boolean isLSNEMsg) method: msg is null or empty");
            return "";
        } else if (isLSNEMsg || isLocalStoredNEMsg(msg)) {
            int imsiIndex = (parseLengthToDigit(msg.substring(5, 9)) + 9) + 2;
            MLog.e("CryptoMessageServiceProxy", "getImsiFromLSNEMsg(String msg, boolean isLSNEMsg) method: Get imsi from the local stored network encrypted message success");
            return msg.substring(imsiIndex, msg.length() - 1);
        } else {
            MLog.e("CryptoMessageServiceProxy", "getImsiFromLSNEMsg(String msg, boolean isLSNEMsg) method: it is not a local stored network encrypted message");
            return "";
        }
    }

    public static String networkEncrypt(String msg, String contactNum, int subID) {
        if (msg == null || contactNum == null || !mIsServiceReady || 9999 < msg.length() + 64) {
            MLog.e("CryptoMessageServiceProxy", "networkEncrypt(String msg, String contactNum, int subID): crypto sms service is not ready,mIsServiceReady is null, or message body length overflow.");
            return "";
        }
        try {
            byte[] bMsg = mCryptoMessageManager.encryptMessage(msg.getBytes("UTF-16"), formatContactsNumber(contactNum).getBytes("UTF-8"), subID);
            if (bMsg == null || bMsg.length <= 1) {
                MLog.e("CryptoMessageServiceProxy", "networkEncrypt() method: message decrypt failed from crypto sms service.");
                return "";
            }
            int len = bMsg.length;
            char version = (char) bMsg[len - 1];
            if (isVersionValid(version)) {
                version = Character.toUpperCase(version);
                bMsg = Arrays.copyOf(bMsg, len - 1);
                String tmpMsg = addEncryptPackage(byteToString(Base64.encode(bMsg, 0, bMsg.length, 0)), 2, subID, null, version);
                MLog.d("CryptoMessageServiceProxy", "networkEncrypt() method:net encrypt success,length=" + tmpMsg.length() + ", version=" + version);
                return tmpMsg;
            }
            MLog.e("CryptoMessageServiceProxy", "networkEncrypt() method:private key version is invalid");
            return "";
        } catch (UnsupportedEncodingException e) {
            MLog.e("CryptoMessageServiceProxy", "networkEncrypt() method: it unsupport encodeing ,it is not UTF-8 or UTF-16", (Throwable) e);
            return "";
        }
    }

    public static String networkDecrypt(String msg, int subID, boolean isNetworkEncrypted) {
        if (!isNetworkEncrypted && !isNetworkEncryptedConversation(msg)) {
            MLog.d("CryptoMessageServiceProxy", "networkDecrypt() method: it is not a network encrypted message");
            return msg;
        } else if (mIsServiceReady) {
            char version = msg.charAt(msg.length() - 1);
            char encodeVersion = version;
            byte[] bMsg = stringToByte(removeEncryptPackage(msg));
            version = Character.toLowerCase(version);
            bMsg = Base64.decode(bMsg, 0, bMsg.length, 0);
            int size = bMsg.length;
            bMsg = Arrays.copyOf(bMsg, size + 1);
            bMsg[size] = (byte) version;
            bMsg = mCryptoMessageManager.decryptMessage(bMsg, subID);
            if (bMsg == null || bMsg.length < 1) {
                MLog.e("CryptoMessageServiceProxy", "networkDecrypt() method: message decrypt failed from crypto sms service.");
                return localStoreNEMsg(msg, subID);
            }
            try {
                String tmpMsg;
                if (Character.isLowerCase(encodeVersion)) {
                    tmpMsg = new String(bMsg, "UTF-8");
                } else if (Character.isUpperCase(encodeVersion)) {
                    tmpMsg = new String(bMsg, "UTF-16");
                } else {
                    MLog.e("CryptoMessageServiceProxy", "networkDecrypt() method: charactor sequence type error,it is not UTF-8 or UTF-16");
                    return localStoreNEMsg(msg, subID);
                }
                MLog.d("CryptoMessageServiceProxy", "networkDecrypt() method:net dencrypt success,length=" + tmpMsg.length() + ", version=" + version);
                return tmpMsg;
            } catch (UnsupportedEncodingException e) {
                MLog.e("CryptoMessageServiceProxy", "networkDecrypt() method: it unsupport encodeing ,it is not UTF-8 or UTF-16", (Throwable) e);
                return localStoreNEMsg(msg, subID);
            }
        } else {
            MLog.e("CryptoMessageServiceProxy", "networkDecrypt() method: crypto sms service is not ready,mIsServiceReady is null");
            return localStoreNEMsg(msg, subID);
        }
    }

    private static String getHashCode(String msg) {
        String hashCodeString = "";
        if (mAccountHashCodeMap.containsKey(msg)) {
            return (String) mAccountHashCodeMap.get(msg);
        }
        hashCodeString = calculateHashCode(msg);
        mAccountHashCodeMap.put(msg, hashCodeString);
        return hashCodeString;
    }

    private static String calculateHashCode(String msg) {
        String tmpString = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.update(msg.getBytes("UTF-8"), 0, msg.getBytes("UTF-8").length);
            return new String(digest.digest(), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return tmpString;
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return tmpString;
        }
    }

    public static int getCardActivatedState(int subId) {
        if (LOCAL_DEBUG) {
            return CryptoMessageServiceStub.getState(mContext, subId);
        }
        if (mIsServiceReady) {
            return mCryptoMessageManager.getState(subId);
        }
        MLog.e("CryptoMessageServiceProxy", "getCardActivatedState() method:crypto sms service is not ready,mIsServiceReady is null");
        return 3;
    }

    public static String getCardCloudAccount(int subId) {
        if (LOCAL_DEBUG) {
            return CryptoMessageServiceStub.getCloudAccount(mContext, subId);
        }
        if (mIsServiceReady) {
            return mCryptoMessageManager.getCloudAccount(subId);
        }
        MLog.e("CryptoMessageServiceProxy", "getCardCloudAccount() method:crypto sms service is not ready,mIsServiceReady is null");
        return "";
    }

    public static int bindAccount(int subId, String accountName, String userId, String serviceToken) {
        if (LOCAL_DEBUG) {
            return CryptoMessageServiceStub.activate(mContext, mCallBack, subId, accountName, userId, serviceToken);
        }
        if (mIsServiceReady) {
            return mCryptoMessageManager.activate(subId, accountName, userId, serviceToken);
        }
        MLog.e("CryptoMessageServiceProxy", "bindAccount() method:crypto sms service is not ready,mIsServiceReady is null");
        return -1;
    }

    public static int unbindAccount(int subId, String userName, String password) {
        if (LOCAL_DEBUG) {
            return CryptoMessageServiceStub.deactivate(mContext, mCallBack, subId, userName, password);
        }
        if (mIsServiceReady) {
            return mCryptoMessageManager.deactivate(subId, userName, password);
        }
        MLog.e("CryptoMessageServiceProxy", "unbindAccount() method:crypto sms service is not ready,mIsServiceReady is null");
        return -1;
    }

    public static int closeAccountBind(int subId, String userName, String password) {
        if (LOCAL_DEBUG) {
            return CryptoMessageServiceStub.localDeactivate(mContext, mCallBack, subId, userName, password);
        }
        if (mIsServiceReady) {
            StatisticalHelper.incrementReportCount(mContext, 2158);
            return mCryptoMessageManager.localDeactivate(subId, userName, password);
        }
        MLog.e("CryptoMessageServiceProxy", "closeAccountBind() method:crypto sms service is not ready,mIsServiceReady is null");
        return -1;
    }

    public static int handleEnrollMessage(String msg, int subID, boolean isEnrolledMessage) {
        if (!mIsServiceReady) {
            MLog.e("CryptoMessageServiceProxy", "handleEnrollMessage() method:crypto sms service is not ready,mIsServiceReady is null");
            return -1;
        } else if (isEnrolledMessage || isNetworkEncryptoRegisterMessage(msg)) {
            try {
                return mCryptoMessageManager.enrollMessage(msg.getBytes("UTF-8"), subID);
            } catch (UnsupportedEncodingException e) {
                MLog.e("CryptoMessageServiceProxy", "handleEnrollMessage() method: msg unsupport encode ,and msg is not utf-8");
                return -1;
            }
        } else {
            MLog.e("CryptoMessageServiceProxy", "handleEnrollMessage: it is not a enrolled message");
            return -1;
        }
    }

    private static String addEncryptPackage(String msg, int encryptType, int subID, String account, char version) {
        String msgLenText = parseLengthToText(msg.length(), 4);
        StringBuffer msgSB = new StringBuffer(msg);
        msgSB.insert(0, msgLenText);
        switch (encryptType) {
            case 2:
                msgSB.insert(0, '2');
                break;
            case 3:
                String imsi = CryptoMessageUtil.getImsiBySubID(subID);
                if (!TextUtils.isEmpty(imsi)) {
                    int imsiLen = imsi.length();
                    if (imsiLen <= 99) {
                        msgSB.insert(0, '5');
                        msgSB.append(parseLengthToText(imsiLen, 2));
                        msgSB.append(imsi);
                        break;
                    }
                    return "";
                }
                return "";
            case 4:
                msgSB.insert(0, '4');
                msgSB.append(parseLengthToText(account.length(), 2));
                msgSB.append(account);
                break;
            default:
                MLog.e("CryptoMessageServiceProxy", "addEncryptPackage: failed, Type error.");
                return "";
        }
        msgSB.append(version);
        msgSB.insert(0, "CM=>");
        return msgSB.toString();
    }

    private static String removeEncryptPackage(String msg) {
        int msgLen = msg.length();
        if (msgLen < 9) {
            return "";
        }
        int bodyLen = parseLengthToDigit(msg.substring(5, 9));
        if (msgLen < bodyLen + 9) {
            return "";
        }
        return msg.substring(9, bodyLen + 9);
    }

    private static String formatContactsNumber(String address) {
        if (TextUtils.isEmpty(address)) {
            return address;
        }
        address = PhoneNumberUtils.formatNumber(address).replaceAll("\\s+", "").replaceAll("-+", "");
        int length = address.length();
        if (length >= 11) {
            return address.substring(length - 11);
        }
        return address;
    }

    private static byte[] stringToByte(String str) {
        if (str == null) {
            return null;
        }
        char[] c = str.toCharArray();
        byte[] b = new byte[c.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (c[i] & 255);
        }
        return b;
    }

    private static String byteToString(byte[] b) {
        if (b == null) {
            return null;
        }
        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = (char) (b[i] & 255);
        }
        return new String(c);
    }

    private static int parseLengthToDigit(String lenText) {
        if (isDigit(lenText)) {
            return Integer.parseInt(lenText);
        }
        MLog.w("CryptoMessageServiceProxy", "parseLengthToDigit: it is a not digit");
        return -1;
    }

    private static String parseLengthToText(int lenDigit, int byteNum) {
        StringBuffer sb = new StringBuffer(String.valueOf(lenDigit));
        while (sb.length() < byteNum) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    private static boolean isVersionValid(char ch) {
        if (ch >= 'a' && ch <= 'z') {
            return true;
        }
        if (ch < 'A' || ch > 'Z') {
            return false;
        }
        return true;
    }

    private static boolean isDigit(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean isRegisterResponseMessage(String msg, int headIndex) {
        if (headIndex > 16) {
            return false;
        }
        boolean signInTheFront = headIndex > 0;
        if (msg.length() <= 9) {
            return false;
        }
        int bodyLengthIndex = headIndex + 5;
        int bodyIndex = bodyLengthIndex + 4;
        if ('3' != msg.charAt(bodyLengthIndex - 1)) {
            return false;
        }
        int claimedBodyLen = parseLengthToDigit(msg.substring(bodyLengthIndex, bodyLengthIndex + 4));
        if (claimedBodyLen < 1) {
            return false;
        }
        int actualBodyLen = msg.length() - bodyIndex;
        if ((!signInTheFront || actualBodyLen != claimedBodyLen) && (signInTheFront || actualBodyLen < claimedBodyLen || actualBodyLen - claimedBodyLen >= 16)) {
            return false;
        }
        MLog.d("CryptoMessageServiceProxy", "isRegisterResponseMessage() method it is register response message");
        return true;
    }

    public static HashMap<String, Integer> networkDecryptInDB(String eText, boolean isNetworkEncrypted) {
        if (!mIsServiceReady) {
            MLog.d("CryptoMessageServiceProxy", "networkDecryptInDB() method crypto sms service is not ready,mIsServiceReady = false");
            return null;
        } else if (isNetworkEncrypted || isNetworkEncryptedConversation(eText)) {
            HashMap<String, Integer> map;
            char version = eText.charAt(eText.length() - 1);
            char encodeVersion = version;
            version = Character.toLowerCase(version);
            CharSequence removeEncryptPackage = removeEncryptPackage(eText);
            byte[] bMsg = stringToByte(removeEncryptPackage);
            bMsg = Base64.decode(bMsg, 0, bMsg.length, 0);
            int size = bMsg.length;
            bMsg = Arrays.copyOf(bMsg, size + 1);
            bMsg[size] = (byte) version;
            byte[] bs = mCryptoMessageManager.decryptMessage(bMsg, 0);
            if (bs != null && bs.length > 3 && isValid(bs, encodeVersion)) {
                try {
                    if (Character.isUpperCase(encodeVersion)) {
                        removeEncryptPackage = new String(bs, "UTF-16");
                    } else if (Character.isLowerCase(encodeVersion)) {
                        removeEncryptPackage = new String(bs, "UTF-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    removeEncryptPackage = null;
                }
                if (!TextUtils.isEmpty(removeEncryptPackage)) {
                    map = new HashMap(1);
                    map.put(removeEncryptPackage, Integer.valueOf(0));
                    return map;
                }
            }
            bs = mCryptoMessageManager.decryptMessage(bMsg, 1);
            if (bs != null && bs.length > 3 && isValid(bs, encodeVersion)) {
                try {
                    if (Character.isLowerCase(encodeVersion)) {
                        removeEncryptPackage = new String(bs, "UTF-8");
                    } else if (Character.isUpperCase(encodeVersion)) {
                        removeEncryptPackage = new String(bs, "UTF-16");
                    }
                } catch (UnsupportedEncodingException e2) {
                    removeEncryptPackage = null;
                }
                if (!TextUtils.isEmpty(removeEncryptPackage)) {
                    map = new HashMap(1);
                    map.put(removeEncryptPackage, Integer.valueOf(1));
                    return map;
                }
            }
            return null;
        } else {
            MLog.d("CryptoMessageServiceProxy", "networkDecryptInDB() method: it is not a network encrypted message");
            return null;
        }
    }

    private static boolean isValid(byte[] bs, char version) {
        boolean z = true;
        if (isUTF16Version(version)) {
            if (!((bs[0] == (byte) -1 && bs[1] == (byte) -2) || ((bs[0] == (byte) -2 && bs[1] == (byte) -1) || ((bs[0] == (byte) 34 && bs[1] == (byte) 0) || (bs[0] == (byte) 0 && bs[1] == (byte) 34))))) {
                z = false;
            }
            return z;
        } else if (!isUTF8Version(version)) {
            return false;
        } else {
            if (!(bs[0] == (byte) -17 && bs[1] == (byte) -69 && bs[2] == (byte) -65)) {
                z = false;
            }
            return z;
        }
    }

    private static boolean isUTF8Version(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    private static boolean isUTF16Version(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }
}
