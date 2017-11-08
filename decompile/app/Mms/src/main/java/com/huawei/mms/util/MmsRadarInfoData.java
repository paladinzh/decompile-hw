package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;

public class MmsRadarInfoData {
    private static String TAG = "MmsRadarInfoData";
    private Context mContext;

    private Context getContext() {
        return MmsApp.getApplication();
    }

    public MmsRadarInfoData() {
        this.mContext = null;
        this.mContext = getContext();
    }

    private boolean isValidSmsc(String smsc) {
        boolean ret = (smsc == null || smsc.length() <= 13) ? false : !smsc.startsWith(StringUtils.MPLUG86) ? smsc.startsWith("0086") : true;
        MLog.d(TAG, "is valid smsc:" + ret);
        return ret;
    }

    public boolean isSmscCorrect(int sub) {
        boolean isSmscCorrect = this.mContext.getSharedPreferences("para_self_repair", 0).getBoolean("is_smsc_correct_sub" + subAdapt(sub), false);
        MLog.d(TAG, "can repair smsc empty slef for sub:" + sub + " isSmscCorrect:" + isSmscCorrect);
        return isSmscCorrect;
    }

    private int subAdapt(int sub) {
        return sub + 1;
    }

    public void updateSelfRepairPara(boolean isSuccess, int type, int sub, int errorId, String des) {
        SharedPreferences sh = this.mContext.getSharedPreferences("para_self_repair", 0);
        Editor editor = sh.edit();
        int subXML = subAdapt(sub);
        String errorValueStr = "";
        if (!isSuccess) {
            int errorCount;
            switch (type) {
                case 1311:
                    editor.putBoolean("is_sms_send_suc_sub" + subXML, false);
                    errorCount = sh.getInt("sms_send_fail_count_sub" + subXML, 0);
                    errorValueStr = sh.getString("sms_send_fail_error_code_sub" + subXML, "");
                    if (errorCount < 4) {
                        Radar.reportChr(sub, 1311, des + ":" + errorId);
                        editor.putInt("sms_send_fail_count_sub" + subXML, errorCount + 1);
                        editor.putString("sms_send_fail_error_code_sub" + subXML, errorValueStr + " " + errorId);
                        break;
                    }
                    ErrorMonitor.reportRadar(907000003, sub, "sms send fail 5 times:" + errorValueStr + " " + errorId);
                    editor.putInt("sms_send_fail_count_sub" + subXML, 0);
                    editor.putString("sms_send_fail_error_code_sub" + subXML, "");
                    break;
                case 1312:
                    break;
                case 1331:
                    errorCount = sh.getInt("mms_send_fail_count_sub" + subXML, 0);
                    errorValueStr = sh.getString("mms_send_fail_error_code_sub" + subXML, "");
                    if (errorCount < 4) {
                        Radar.reportChr(sub, 1331, des + ":" + errorId);
                        editor.putInt("mms_send_fail_count_sub" + subXML, errorCount + 1);
                        editor.putString("mms_send_fail_error_code_sub" + subXML, errorValueStr + " " + errorId);
                        break;
                    }
                    ErrorMonitor.reportRadar(907000011, sub, "mms send fail 5 times:" + errorValueStr + " " + errorId);
                    editor.putInt("mms_send_fail_count_sub" + subXML, 0);
                    editor.putString("mms_send_fail_error_code_sub" + subXML, "");
                    break;
                case 1332:
                    errorCount = sh.getInt("mms_receive_fail_count_sub" + subXML, 0);
                    errorValueStr = sh.getString("mms_receive_fail_error_code_sub" + subXML, "");
                    if (errorCount < 4) {
                        Radar.reportChr(sub, 1332, des + ":" + errorId);
                        editor.putInt("mms_receive_fail_count_sub" + subXML, errorCount + 1);
                        editor.putString("mms_receive_fail_error_code_sub" + subXML, errorValueStr + " " + errorId);
                        break;
                    }
                    ErrorMonitor.reportRadar(907000012, sub, "mms rec fail 5 times:" + errorValueStr + " " + errorId);
                    editor.putInt("mms_receive_fail_count_sub" + subXML, 0);
                    editor.putString("mms_receive_fail_error_code_sub" + subXML, "");
                    break;
                default:
                    break;
            }
        }
        switch (type) {
            case 1311:
                editor.putBoolean("is_sms_send_suc_sub" + subXML, true);
                if (sh.getInt("sms_send_fail_count_sub" + subXML, 0) != 0) {
                    Radar.reportChr(sub, 1311, "suc:" + sh.getString("sms_send_fail_error_code_sub" + subXML, ""));
                    editor.putInt("sms_send_fail_count_sub" + subXML, 0);
                    editor.putString("sms_send_fail_error_code_sub" + subXML, "");
                    break;
                }
                break;
            case 1331:
                if (sh.getInt("mms_send_fail_count_sub" + subXML, 0) != 0) {
                    Radar.reportChr(sub, 1331, "suc:" + sh.getString("mms_send_fail_error_code_sub" + subXML, ""));
                    editor.putInt("mms_send_fail_count_sub" + subXML, 0);
                    editor.putString("mms_send_fail_error_code_sub" + subXML, "");
                    break;
                }
                break;
            case 1332:
                if (sh.getInt("mms_receive_fail_count_sub" + subXML, 0) != 0) {
                    Radar.reportChr(sub, 1332, "suc:" + sh.getString("mms_receive_fail_error_code_sub" + subXML, ""));
                    editor.putInt("mms_receive_fail_count_sub" + subXML, 0);
                    editor.putString("mms_receive_fail_error_code_sub" + subXML, "");
                    break;
                }
                break;
        }
        editor.commit();
    }

    public boolean isSmsCanSend(int sub) {
        boolean ret = this.mContext.getSharedPreferences("para_self_repair", 0).getBoolean("is_sms_send_suc_sub" + subAdapt(sub), true);
        MLog.d(TAG, "is now sms can send success:" + ret);
        return ret;
    }

    private void initSelfRepairParaInner(int sub) {
        String smscAddrInXml = "";
        String smscAddrInCard = "";
        String imsiInXml = "";
        String imsiInSub = "";
        imsiInSub = MessageUtils.encode(HwMessageUtils.getImsiFromCard(this.mContext, sub));
        SharedPreferences sh = this.mContext.getSharedPreferences("para_self_repair", 0);
        Editor editor = sh.edit();
        int subXml = subAdapt(sub);
        imsiInXml = sh.getString("imsi_sub" + subXml, "");
        if (imsiInSub == null || imsiInSub.equals(imsiInXml)) {
            MLog.d(TAG, "imsiInsub is same as imsiInXml sub:" + sub);
            smscAddrInXml = sh.getString("smsc_address_sub" + subXml, null);
            smscAddrInCard = MessageUtils.getSmsAddressBySubID(sub);
            if (!isValidSmsc(smscAddrInCard) || smscAddrInCard.equals(smscAddrInXml)) {
                MLog.e(TAG, "smsc in card:" + smscAddrInCard);
            } else {
                MLog.d(TAG, "smsc addr in card is not same as smsc addr in xml");
                editor.putBoolean("is_smsc_correct_sub" + subXml, false);
            }
        } else {
            MLog.d(TAG, "imsiInsub is not same as imsiInXml sub:" + sub);
            editor.putString("imsi_sub" + subXml, imsiInSub);
            editor.putBoolean("is_smsc_correct_sub" + subXml, false);
            editor.putString("smsc_address_sub" + subXml, "");
            editor.putBoolean("is_sms_send_suc_sub" + subXml, false);
            editor.putInt("sms_send_fail_count_sub" + subXml, 0);
            editor.putString("sms_send_fail_error_code_sub" + subXml, "");
            editor.putInt("mms_send_fail_count_sub" + subXml, 0);
            editor.putString("mms_send_fail_error_code_sub" + subXml, "");
            editor.putInt("mms_receive_fail_count_sub" + subXml, 0);
            editor.putString("mms_receive_fail_error_code_sub" + subXml, "");
        }
        editor.commit();
    }

    public void initSelfRepairPara() {
        MLog.d(TAG, "initSelfRepairPara entry");
        if (!HwMessageUtils.IS_CHINA_REGION || HwMessageUtils.isInRoaming()) {
            MLog.d(TAG, "not in china region or roaming, return");
            return;
        }
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                MmsRadarInfoData.this.initSelfRepairParaInner(0);
                if (MessageUtils.isMultiSimEnabled()) {
                    MmsRadarInfoData.this.initSelfRepairParaInner(1);
                }
            }
        });
        MLog.d(TAG, "initSelfRepairPara done");
    }

    public void initSelfRepairPara(final int sub) {
        MLog.d(TAG, "initSelfRepairPara with sub entry");
        if (!HwMessageUtils.IS_CHINA_REGION || HwMessageUtils.isInRoaming()) {
            MLog.d(TAG, "not in china region or roaming, return");
            return;
        }
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                MmsRadarInfoData.this.initSelfRepairParaInner(sub);
            }
        });
        MLog.d(TAG, "initSelfRepairPara with sub done");
    }

    private void writeSmscToSharedPref(int sub) {
        String smscAddrInCard = MessageUtils.getSmsAddressBySubID(sub);
        if (isValidSmsc(smscAddrInCard)) {
            SharedPreferences sh = this.mContext.getSharedPreferences("para_self_repair", 0);
            Editor editor = sh.edit();
            int subXML = subAdapt(sub);
            if (!smscAddrInCard.equals(sh.getString("smsc_address_sub" + subXML, null))) {
                editor.putString("smsc_address_sub" + subXML, smscAddrInCard);
                editor.putBoolean("is_smsc_correct_sub" + subXML, true);
                editor.commit();
            }
        }
    }

    public void updateSmscAddrToSharedPref(int sub) {
        MLog.d(TAG, "updateSmscAddrToSharedPref entry");
        if (HwMessageUtils.IS_CHINA_REGION) {
            if (this.mContext.getSharedPreferences("para_self_repair", 0).getBoolean("is_smsc_correct_sub" + subAdapt(sub), false)) {
                MLog.d(TAG, "smsc already updated, return");
            } else if (HwMessageUtils.getIsUsingOutgoingServiceCenter()) {
                MLog.d(TAG, "using outgoing service center, return");
            } else if (!HwMessageUtils.isInRoaming()) {
                writeSmscToSharedPref(sub);
                MLog.d(TAG, "updateSmscAddrToSharedPref done");
            }
        }
    }

    private void updateSmscMark(Context context, int sub) {
        SharedPreferences sh = context.getSharedPreferences("para_self_repair", 0);
        Editor editor = sh.edit();
        int subXML = subAdapt(sub);
        if (isValidSmsc(sh.getString("smsc_address_sub" + subXML, null))) {
            String imsiInXml = sh.getString("imsi_sub" + subXML, null);
            String imsiInCard = MessageUtils.encode(HwMessageUtils.getImsiFromCard(context, sub));
            if (imsiInCard != null && imsiInCard.equals(imsiInXml)) {
                editor.putBoolean("is_smsc_correct_sub" + subXML, true);
                editor.commit();
            }
        }
    }

    public void updateSmscAddrToSimCard(int sub) {
        MLog.d(TAG, "updateSmscAddrToSimCard entry");
        if (HwMessageUtils.IS_CHINA_REGION) {
            SharedPreferences sh = this.mContext.getSharedPreferences("para_self_repair", 0);
            int subXML = subAdapt(sub);
            if (!sh.getBoolean("is_smsc_correct_sub" + subXML, false)) {
                MLog.d(TAG, "no correct smsc to write to sim card, return");
                updateSmscMark(this.mContext, sub);
            } else if (!HwMessageUtils.isInRoaming()) {
                String smscAddrInXml = sh.getString("smsc_address_sub" + subXML, null);
                if (isValidSmsc(smscAddrInXml)) {
                    String smscAddrInCard = MessageUtils.getSmsAddressBySubID(sub);
                    if (!smscAddrInXml.equals(smscAddrInCard)) {
                        boolean ret = MessageUtils.setSmsAddressBySubID(smscAddrInXml, sub);
                        int lenSmscAddrInCard = 0;
                        if (smscAddrInCard != null) {
                            lenSmscAddrInCard = smscAddrInCard.length();
                        }
                        Radar.reportChr(sub, 1314, "update smsc: " + ret + " " + lenSmscAddrInCard);
                    }
                }
                MLog.d(TAG, "updateSmscAddrToSimCard done");
            }
        }
    }
}
