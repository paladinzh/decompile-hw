package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccRecords;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public final class CsgSearch extends Handler {
    private static final int EVENT_CSG_MANUAL_SCAN_DONE = 2;
    private static final int EVENT_CSG_MANUAL_SELECT_DONE = 3;
    private static final int EVENT_CSG_OCSGL_LOADED = 7;
    private static final int EVENT_CSG_PERIODIC_SCAN_DONE = 5;
    private static final int EVENT_CSG_PERIODIC_SEARCH_TIMEOUT = 4;
    private static final int EVENT_CSG_PERIODIC_SELECT_DONE = 6;
    private static final int EVENT_GET_AVAILABLE_CSG_NETWORK_DONE = 0;
    private static final int EVENT_SELECT_CSG_NETWORK_DONE = 1;
    private static final String LOG_TAG = "CsgSearch";
    private static final String OPERATOR_NAME_ATT_MICROCELL = "AT&T MicroCell";
    private static final int TIMER_CSG_PERIODIC_SEARCH = 7200000;
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private GsmCdmaPhone mPhone;

    private static class CSGNetworkInfo {
        public byte bIncludePcsDigit;
        public int iCSGId;
        public int iCSGListCat;
        public int iSignalStrength;
        public boolean isSelectedFail;
        public short mcc;
        public short mnc;
        public String sCSGName;

        private CSGNetworkInfo() {
            this.isSelectedFail = false;
        }

        public boolean isEmpty() {
            if (this.mcc == (short) 0 && this.mnc == (short) 0 && this.bIncludePcsDigit == (byte) 0 && this.iCSGListCat == 0 && this.iCSGId == 0) {
                return (this.sCSGName == null || this.sCSGName.isEmpty()) && this.iSignalStrength == 0;
            } else {
                return false;
            }
        }

        public void set(CSGNetworkInfo csgNeworkInfo) {
            this.mcc = csgNeworkInfo.mcc;
            this.mnc = csgNeworkInfo.mnc;
            this.bIncludePcsDigit = csgNeworkInfo.bIncludePcsDigit;
            this.iCSGListCat = csgNeworkInfo.iCSGListCat;
            this.iCSGId = csgNeworkInfo.iCSGId;
            if (csgNeworkInfo.sCSGName != null) {
                this.sCSGName = csgNeworkInfo.sCSGName;
            }
            this.iSignalStrength = csgNeworkInfo.iSignalStrength;
            this.isSelectedFail = csgNeworkInfo.isSelectedFail;
        }

        public String toString() {
            return "CSGNetworkInfo: mcc: " + this.mcc + ", mnc: " + this.mnc + ", bIncludePcsDigit: " + this.bIncludePcsDigit + ", iCSGListCat: " + this.iCSGListCat + ", iCSGId: " + this.iCSGId + ", sCSGName: " + this.sCSGName + ", iSignalStrength: " + this.iSignalStrength + " ,isSelectedFail:" + this.isSelectedFail;
        }
    }

    private class CSGNetworkList {
        private static final byte CSG_INFO_TAG = (byte) 20;
        public static final int CSG_LIST_CAT_ALLOWED = 1;
        public static final int CSG_LIST_CAT_OPERATOR = 2;
        public static final int CSG_LIST_CAT_UNKNOWN = 0;
        private static final byte CSG_SCAN_RESULT_TAG = (byte) 19;
        private static final byte CSG_SIG_INFO_TAG = (byte) 21;
        public static final byte GSM_ONLY = (byte) 1;
        public static final byte LTE_ONLY = (byte) 4;
        public static final byte MNC_DIGIT_IS_THREE = (byte) 1;
        public static final byte MNC_DIGIT_IS_TWO = (byte) 0;
        public static final int NAS_SCAN_AS_ABORT = 1;
        public static final int NAS_SCAN_REJ_IN_RLF = 2;
        public static final int NAS_SCAN_SUCCESS = 0;
        public static final int RADIO_IF_GSM = 4;
        public static final int RADIO_IF_LTE = 8;
        public static final int RADIO_IF_TDSCDMA = 9;
        public static final int RADIO_IF_UMTS = 5;
        public static final int SCAN_RESULT_LEN_FAIL = 0;
        public static final int SCAN_RESULT_LEN_SUCC = 4;
        public static final byte TDSCDMA_ONLY = (byte) 8;
        public static final byte UMTS_ONLY = (byte) 2;
        public ArrayList<CSGNetworkInfo> mCSGNetworks;
        private CSGNetworkInfo mCurSelectingCsgNetwork;

        private CSGNetworkList() {
            this.mCSGNetworks = new ArrayList();
            this.mCurSelectingCsgNetwork = null;
        }

        public CSGNetworkInfo getCurrentSelectingCsgNetwork() {
            return this.mCurSelectingCsgNetwork;
        }

        public boolean parseCsgResponseData(byte[] data) {
            boolean isParseSucc = false;
            if (data == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= response data is null");
                return false;
            }
            try {
                ByteBuffer resultBuffer = ByteBuffer.wrap(data);
                resultBuffer.order(ByteOrder.nativeOrder());
                byte byteVar = resultBuffer.get();
                if ((byte) 19 != byteVar) {
                    Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResult  tag is an unexpected value: " + byteVar);
                } else {
                    short scanResultLen = resultBuffer.getShort();
                    if (scanResultLen == (short) 0) {
                        Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResultLen is 0x00, scan failed");
                    } else if ((short) 4 == scanResultLen) {
                        int intVar = resultBuffer.getInt();
                        if (intVar != 0) {
                            Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResult is not success with the value: " + intVar + ", break");
                        } else {
                            Rlog.d(CsgSearch.LOG_TAG, "=csg= scanResult is success, go on with the parsing");
                            byteVar = resultBuffer.get();
                            if ((byte) 20 != byteVar) {
                                Rlog.e(CsgSearch.LOG_TAG, "=csg= CSG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                            } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                Rlog.e(CsgSearch.LOG_TAG, "csg_info_total_len is 0x00, break");
                            } else {
                                byte numOfCsgInfoEntries = resultBuffer.get();
                                Rlog.d(CsgSearch.LOG_TAG, "=csg= numOfEntries for CSG info = " + numOfCsgInfoEntries);
                                if (numOfCsgInfoEntries > (byte) 0) {
                                    byte i;
                                    for (i = (byte) 0; i < numOfCsgInfoEntries; i++) {
                                        CSGNetworkInfo csgNetworkInfo = new CSGNetworkInfo();
                                        csgNetworkInfo.mcc = resultBuffer.getShort();
                                        csgNetworkInfo.mnc = resultBuffer.getShort();
                                        csgNetworkInfo.bIncludePcsDigit = resultBuffer.get();
                                        csgNetworkInfo.iCSGListCat = resultBuffer.getInt();
                                        csgNetworkInfo.iCSGId = resultBuffer.getInt();
                                        byte[] nameBuffer = new byte[(resultBuffer.get() * 2)];
                                        resultBuffer.get(nameBuffer);
                                        csgNetworkInfo.sCSGName = new String(nameBuffer, "UTF-16");
                                        this.mCSGNetworks.add(csgNetworkInfo);
                                    }
                                    byteVar = resultBuffer.get();
                                    if ((byte) 21 != byteVar) {
                                        Rlog.e(CsgSearch.LOG_TAG, "=csg= CSG_SIG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                                    } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                        Rlog.e(CsgSearch.LOG_TAG, "=csg= csg_sig_info_total_len is 0x00, break");
                                    } else {
                                        byte numOfCsgSigInfoEntries = resultBuffer.get();
                                        Rlog.d(CsgSearch.LOG_TAG, "=csg= numOfCsgSigInfoEntries for CSG sig info = " + numOfCsgSigInfoEntries);
                                        if (numOfCsgSigInfoEntries > (byte) 0) {
                                            for (i = (byte) 0; i < numOfCsgSigInfoEntries; i++) {
                                                short mcc = resultBuffer.getShort();
                                                short mnc = resultBuffer.getShort();
                                                byte bIncludePcsDigit = resultBuffer.get();
                                                int iCSGId = resultBuffer.getInt();
                                                int iCSGSignalStrength = resultBuffer.getInt();
                                                int j = 0;
                                                int s = this.mCSGNetworks.size();
                                                while (j < s) {
                                                    if (mcc == ((CSGNetworkInfo) this.mCSGNetworks.get(j)).mcc && mnc == ((CSGNetworkInfo) this.mCSGNetworks.get(j)).mnc && bIncludePcsDigit == ((CSGNetworkInfo) this.mCSGNetworks.get(j)).bIncludePcsDigit && iCSGId == ((CSGNetworkInfo) this.mCSGNetworks.get(j)).iCSGId) {
                                                        ((CSGNetworkInfo) this.mCSGNetworks.get(j)).iSignalStrength = iCSGSignalStrength;
                                                        break;
                                                    }
                                                    j++;
                                                }
                                            }
                                            Rlog.i(CsgSearch.LOG_TAG, "=csg= parse csg response data successfull");
                                            isParseSucc = true;
                                        } else {
                                            Rlog.e(CsgSearch.LOG_TAG, "=csg= num Of Csg Sig Info Entries is not corrcet break");
                                        }
                                    }
                                } else {
                                    Rlog.e(CsgSearch.LOG_TAG, "=csg= numOfCsgInfoEntries is not correct break");
                                }
                            }
                        }
                    } else {
                        Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResultLen is invalid, scan failed");
                    }
                }
            } catch (Exception e) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= exception occurrs: " + e);
            }
            return isParseSucc;
        }

        public CSGNetworkInfo getToBeRegsiteredCSGNetwork() {
            this.mCurSelectingCsgNetwork = null;
            if (this.mCSGNetworks == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return this.mCurSelectingCsgNetwork;
            }
            try {
                boolean uiccIsCsgAware = CsgSearch.this.isCsgAwareUicc();
                Rlog.d(CsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
                for (CSGNetworkInfo csgInfo : this.mCSGNetworks) {
                    if (csgInfo.isSelectedFail) {
                        Rlog.d(CsgSearch.LOG_TAG, "=csg=  had selected and failed, so not reselect again!");
                    } else if (uiccIsCsgAware) {
                        if (2 == csgInfo.iCSGListCat && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                            this.mCurSelectingCsgNetwork = csgInfo;
                        }
                    } else if ((1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                        this.mCurSelectingCsgNetwork = csgInfo;
                    }
                }
                Rlog.i(CsgSearch.LOG_TAG, "=csg=  get the strongest CSG network: " + this.mCurSelectingCsgNetwork);
            } catch (Exception e) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg=  exception occurrs: " + e);
            }
            return this.mCurSelectingCsgNetwork;
        }

        public boolean isToBeSearchedCsgListsEmpty() {
            boolean isEmpty = true;
            boolean uiccIsCsgAware = CsgSearch.this.isCsgAwareUicc();
            Rlog.d(CsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
            if (this.mCSGNetworks == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return true;
            }
            for (CSGNetworkInfo csgInfo : this.mCSGNetworks) {
                if (uiccIsCsgAware) {
                    if (2 == csgInfo.iCSGListCat) {
                        Rlog.d(CsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                        isEmpty = false;
                        break;
                    }
                } else if (1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) {
                    Rlog.d(CsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }

    public static boolean isSupportCsgSearch() {
        return mIsSupportCsgSearch;
    }

    public CsgSearch(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        CSGNetworkList csgNetworklist;
        switch (msg.what) {
            case 0:
                Rlog.d(LOG_TAG, "=csg= Receved EVENT_GET_AVAILABLE_CSG_NETWORK_DONE.");
                ar = msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                    return;
                } else {
                    handleCsgNetworkQueryResult(ar);
                    return;
                }
            case 1:
                Rlog.d(LOG_TAG, "=csg=  Receved EVENT_SELECT_CSG_NETWORK_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                    return;
                }
                Message onComplete = ar.userObj;
                if (onComplete != null) {
                    csgNetworklist = onComplete.obj.result;
                    if (ar.exception != null) {
                        Rlog.e(LOG_TAG, "=csg= select CSG failed! " + ar.exception);
                        CSGNetworkInfo curSelectingCsgNetwork = csgNetworklist.getCurrentSelectingCsgNetwork();
                        if (curSelectingCsgNetwork == null) {
                            Rlog.i(LOG_TAG, "=csg= current select CSG is null->maybe loop end. response result.");
                            AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                            onComplete.sendToTarget();
                            return;
                        }
                        curSelectingCsgNetwork.isSelectedFail = true;
                        Rlog.e(LOG_TAG, "=csg= mark  current CSG-ID item Failed!" + csgNetworklist.mCurSelectingCsgNetwork);
                        Rlog.i(LOG_TAG, "=csg= select next strongest CSG-ID->start select");
                        selectCSGNetwork(onComplete);
                        return;
                    }
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                Rlog.e(LOG_TAG, "=csg=  ar.userObj is null, the code should never come here!!");
                return;
            case 2:
                Rlog.d(LOG_TAG, "=csg= Receved EVENT_CSG_MANUAL_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                } else if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "=csg= Manual Search: get avaiable CSG list failed! -> response " + ar.exception);
                    AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                    return;
                } else {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: get avaiable CSG list success -> select Csg! ");
                    selectCSGNetwork(obtainMessage(EVENT_CSG_MANUAL_SELECT_DONE, ar));
                    return;
                }
            case EVENT_CSG_MANUAL_SELECT_DONE /*3*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_MANUAL_SELECT_DONE!");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                }
                if (ar.exception != null) {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is failed! " + ar.exception);
                } else {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is success! ");
                }
                AsyncResult arUsrObj = (AsyncResult) ar.userObj;
                if (arUsrObj == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                }
                AsyncResult.forMessage((Message) arUsrObj.userObj, null, ar.exception);
                ((Message) arUsrObj.userObj).sendToTarget();
                return;
            case 4:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SEARCH_TIMEOUT!");
                trigerPeriodicCsgSearch();
                Rlog.d(LOG_TAG, "=csg=  launch next Csg Periodic search timer!");
                judgeToLaunchCsgPeriodicSearchTimer();
                return;
            case 5:
                Rlog.i(LOG_TAG, "=csg= Receved EVENT_CSG_PERIODIC_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                } else if (ar.exception != null || ar.result == null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list failed! " + ar.exception);
                    return;
                } else {
                    csgNetworklist = (CSGNetworkList) ar.result;
                    Rlog.d(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list success -> select Csg! ");
                    if (csgNetworklist.isToBeSearchedCsgListsEmpty()) {
                        Rlog.i(LOG_TAG, "=csg= Periodic Search: no avaiable CSG-ID -> cancel periodic search! ");
                        cancelCsgPeriodicSearchTimer();
                        return;
                    }
                    selectCSGNetwork(obtainMessage(EVENT_CSG_PERIODIC_SELECT_DONE, ar));
                    return;
                }
            case EVENT_CSG_PERIODIC_SELECT_DONE /*6*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SELECT_DONE!");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                } else if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is failed! " + ar.exception);
                    return;
                } else {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is success! ");
                    return;
                }
            case EVENT_CSG_OCSGL_LOADED /*7*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_OCSGL_LOADED!");
                judgeToLaunchCsgPeriodicSearchTimer();
                return;
            default:
                Rlog.e(LOG_TAG, "unexpected event not handled: " + msg.what);
                return;
        }
    }

    public void selectCsgNetworkManually(Message response) {
        Rlog.i(LOG_TAG, "start manual select CSG network...");
        getAvailableCSGNetworks(obtainMessage(2, response));
    }

    public void registerForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.registerForCsgRecordsLoaded(this, EVENT_CSG_OCSGL_LOADED, null);
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.unregisterForCsgRecordsLoaded(this);
        }
    }

    private void getAvailableCSGNetworks(Message response) {
        byte[] requestData = new byte[EVENT_CSG_OCSGL_LOADED];
        try {
            ByteBuffer buf = ByteBuffer.wrap(requestData);
            buf.order(ByteOrder.nativeOrder());
            buf.put((byte) 16);
            buf.putShort((short) 1);
            buf.put((byte) 6);
            buf.put((byte) 17);
            buf.putShort((short) 0);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "exception occurrs: " + e);
        }
        this.mPhone.mCi.getAvailableCSGNetworks(requestData, obtainMessage(0, response));
    }

    private void selectCSGNetwork(Message response) {
        AsyncResult ar = response.obj;
        if (ar == null || ar.result == null) {
            Rlog.e(LOG_TAG, "=csg= parsed CSG list is null, return exception");
            AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
            response.sendToTarget();
            return;
        }
        CSGNetworkList csgNetworklist = ar.result;
        if (csgNetworklist.mCSGNetworks.size() > 0) {
            CSGNetworkInfo curSelCsgNetwork = csgNetworklist.getToBeRegsiteredCSGNetwork();
            Rlog.d(LOG_TAG, "to be registered CSG info is " + curSelCsgNetwork);
            if (curSelCsgNetwork != null && !curSelCsgNetwork.isEmpty()) {
                byte[] requestData = new byte[13];
                try {
                    ByteBuffer buf = ByteBuffer.wrap(requestData);
                    buf.order(ByteOrder.nativeOrder());
                    buf.put((byte) 32);
                    buf.putShort((short) 10);
                    buf.putShort(curSelCsgNetwork.mcc);
                    buf.putShort(curSelCsgNetwork.mnc);
                    buf.put(curSelCsgNetwork.bIncludePcsDigit);
                    buf.putInt(curSelCsgNetwork.iCSGId);
                    buf.put((byte) 5);
                    this.mPhone.mCi.setCSGNetworkSelectionModeManual(requestData, obtainMessage(1, response));
                    return;
                } catch (Exception e) {
                    Rlog.e(LOG_TAG, "=csg= exception occurrs: " + e);
                    AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                    response.sendToTarget();
                    return;
                }
            } else if (curSelCsgNetwork == null || !curSelCsgNetwork.isEmpty()) {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, Select CSG fail!");
                AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                response.sendToTarget();
                return;
            } else {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, so finish search! ");
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
                return;
            }
        }
        Rlog.e(LOG_TAG, "=csg= mCSGNetworks is not initailized, return with exception");
        AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
        response.sendToTarget();
    }

    private void handleCsgNetworkQueryResult(AsyncResult ar) {
        if (ar == null || ar.userObj == null) {
            Rlog.e(LOG_TAG, "=csg=  ar or userObj is null, the code should never come here!!");
        } else if (ar.exception != null) {
            Rlog.e(LOG_TAG, "=csg=  exception happen: " + ar.exception);
            AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        } else {
            CSGNetworkList csgNetworklist = new CSGNetworkList();
            if (csgNetworklist.parseCsgResponseData((byte[]) ar.result)) {
                AsyncResult.forMessage((Message) ar.userObj, csgNetworklist, null);
                ((Message) ar.userObj).sendToTarget();
            } else {
                AsyncResult.forMessage((Message) ar.userObj, null, new CommandException(Error.GENERIC_FAILURE));
                ((Message) ar.userObj).sendToTarget();
            }
        }
    }

    private boolean isCsgAwareUicc() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r == null || r.getOcsgl().length > 0 || r.getCsglexist()) {
            return true;
        }
        Rlog.d(LOG_TAG, "=csg=  EF-Operator not present =>CSG not Aware UICC");
        return false;
    }

    private void trigerPeriodicCsgSearch() {
        getAvailableCSGNetworks(obtainMessage(5));
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        boolean isLaunchTimer = false;
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        ServiceState ss = this.mPhone.getServiceState();
        String operatorAlpha = SystemProperties.get("gsm.operator.alpha", "");
        if (!(ss == null || ((ss.getVoiceRegState() != 0 && ss.getDataRegState() != 0) || ss.getRoaming() || operatorAlpha == null || OPERATOR_NAME_ATT_MICROCELL.equals(operatorAlpha)))) {
            isLaunchTimer = true;
        }
        if (isLaunchTimer && r != null) {
            byte[] csgLists = r.getOcsgl();
            if (r.getCsglexist() && csgLists.length == 0) {
                Rlog.d(LOG_TAG, "=csg= EFOCSGL is empty, not trigger periodic search!");
                isLaunchTimer = false;
            }
        }
        if (isLaunchTimer) {
            launchCsgPeriodicSearchTimer();
        } else {
            cancelCsgPeriodicSearchTimer();
        }
    }

    private void launchCsgPeriodicSearchTimer() {
        if (!hasMessages(4)) {
            Rlog.d(LOG_TAG, "=csg= lauch periodic search timer!");
            sendEmptyMessageDelayed(4, 7200000);
        }
    }

    private void cancelCsgPeriodicSearchTimer() {
        if (hasMessages(4)) {
            Rlog.d(LOG_TAG, "=csg= cancel periodic search timer!");
            removeMessages(4);
        }
    }
}
