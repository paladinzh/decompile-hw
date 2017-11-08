package com.android.settings;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.android.app.ActionBarEx;
import java.util.Iterator;
import java.util.Map.Entry;

public class ApnEditorHwBase extends SettingsPreferenceFragment {
    private static final String TAG = ApnEditorHwBase.class.getSimpleName();
    static final String[] sProjection = new String[]{"_id", "name", "apn", "proxy", "port", "user", "server", "password", "mmsc", "mcc", "mnc", "numeric", "mmsproxy", "mmsport", "authtype", "type", "protocol", "carrier_enabled", "bearer", "bearer_bitmask", "roaming_protocol", "mvno_type", "mvno_match_data", "ppppwd"};
    protected boolean[] apnDisableEditOrDel = new boolean[]{false, false};
    private OnClickListener mApplyListener = new OnClickListener() {
        public void onClick(View v) {
            if (ApnEditorHwBase.this.validateAndSave(false)) {
                ApnEditorHwBase.this.finish();
            }
        }
    };
    private OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(View v) {
            ApnEditorHwBase.this.showDialog(1);
        }
    };
    protected Cursor mCursor;
    protected boolean mIsDeleted = false;
    protected boolean mIsPresetAPN = false;
    protected boolean mIsReadOnly;
    protected boolean mNeedReloadData = true;
    protected boolean mNewApn;
    protected ContentValues mOldContentValues;
    protected EditTextPreference mPppPwd;
    protected ListPreference mProtocol;
    protected ListPreference mRoamingProtocol;
    protected int mSlotId = 0;
    protected Uri mUri;

    protected boolean isPresetAPN(android.net.Uri r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0056 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r12 = this;
        r11 = 1;
        r10 = 0;
        r8 = -1;
        r6 = 0;
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r2 = 0;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r3 = 0;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r4 = 0;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r5 = 0;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1 = r13;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        if (r6 == 0) goto L_0x0026;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
    L_0x0013:
        r0 = r6.moveToFirst();	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        if (r0 == 0) goto L_0x0026;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
    L_0x0019:
        r0 = "visible";	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r9 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        if (r9 <= 0) goto L_0x0026;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
    L_0x0022:
        r8 = r6.getInt(r9);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
    L_0x0026:
        if (r8 != r11) goto L_0x002f;
    L_0x0028:
        if (r6 == 0) goto L_0x002e;
    L_0x002a:
        r6.close();
        r6 = 0;
    L_0x002e:
        return r11;
    L_0x002f:
        if (r6 == 0) goto L_0x0035;
    L_0x0031:
        r6.close();
        r6 = 0;
    L_0x0035:
        return r10;
    L_0x0036:
        r7 = move-exception;
        r0 = TAG;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1.<init>();	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r2 = "isPresetAPN()--> e :";	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1 = r1.append(r7);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        android.util.Log.e(r0, r1);	 Catch:{ Exception -> 0x0036, all -> 0x0057 }
        if (r6 == 0) goto L_0x0056;
    L_0x0052:
        r6.close();
        r6 = 0;
    L_0x0056:
        return r10;
    L_0x0057:
        r0 = move-exception;
        if (r6 == 0) goto L_0x005e;
    L_0x005a:
        r6.close();
        r6 = 0;
    L_0x005e:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.ApnEditorHwBase.isPresetAPN(android.net.Uri):boolean");
    }

    protected int getMetricsCategory() {
        return 13;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230733);
        this.mPppPwd = (EditTextPreference) findPreference("prompt_password");
        if (!"TELECOM".equalsIgnoreCase(SystemProperties.get("ro.config.operators", ""))) {
            getPreferenceScreen().removePreference(this.mPppPwd);
        }
        Utils.changePermanentMenuKey(getContext());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (this.mIsPresetAPN && (this.mIsReadOnly || (this.apnDisableEditOrDel[0] && this.apnDisableEditOrDel[1]))) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            } else {
                ActionBarEx.setStartIcon(actionBar, true, null, this.mCancelListener);
                ActionBarEx.setEndIcon(actionBar, true, null, this.mApplyListener);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    public Dialog onCreateDialog(int id) {
        return id == 1 ? new Builder(getContext()).setNegativeButton(17039360, null).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ApnEditorHwBase.this.mNewApn && ApnEditorHwBase.this.mUri != null) {
                    ApnEditorHwBase.this.getContentResolver().delete(ApnEditorHwBase.this.mUri, null, null);
                }
                ApnEditorHwBase.this.finish();
            }
        }).setMessage(2131627436).create() : super.onCreateDialog(id);
    }

    protected void asynUpdateValuesToDb(ContentResolver resolver, final Uri uri, final ContentValues values) {
        ContentValues updateValue = values;
        Uri updateUri = uri;
        new Thread() {
            public void run() {
                ApnEditorHwBase.this.getContentResolver().update(uri, values, null, null);
                ApnEditorHwBase.this.mNeedReloadData = true;
            }
        }.start();
    }

    private Object invoke(String className, String methodName, Class[] parameterTypes, Object[] params) {
        try {
            Class c = Class.forName(className);
            return c.getMethod(methodName, parameterTypes).invoke(c, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean isRoamingBroker() {
        try {
            Object obj;
            boolean booleanValue;
            if (Utils.isMultiSimEnabled()) {
                obj = invoke("com.android.internal.telephony.RoamingBroker", "isRoamingBrokerActivated", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(this.mSlotId)});
            } else {
                obj = invoke("com.android.internal.telephony.RoamingBroker", "isRoamingBrokerActivated", null, null);
            }
            if (obj != null) {
                booleanValue = ((Boolean) obj).booleanValue();
            } else {
                booleanValue = false;
            }
            return booleanValue;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String getAuthTypeConfiguration(String mccmnc) {
        if (mccmnc == null) {
            return null;
        }
        String apnTypesConfig = System.getString(getContentResolver(), "hw_apn_authtype");
        if (apnTypesConfig == null) {
            return null;
        }
        for (String mccmncType : apnTypesConfig.split(";")) {
            String[] pair = mccmncType.split(":");
            if (pair.length == 2) {
                String mccmncPart = pair[0];
                if (mccmncPart != null && mccmncPart.trim().equals(mccmnc)) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof SelectableEditTextPreference) {
            ((SelectableEditTextPreference) preference).setInitialSelectionMode(0);
        }
        return super.onPreferenceTreeClick(preference);
    }

    protected boolean isAnyItemsChanged(ContentValues newValues) {
        if (newValues == null || this.mOldContentValues == null) {
            return false;
        }
        boolean isChanged = false;
        Iterator iter = newValues.valueSet().iterator();
        while (iter.hasNext() && !isChanged) {
            Entry entry = (Entry) iter.next();
            String key = (String) entry.getKey();
            if (!this.mOldContentValues.containsKey(key)) {
                isChanged = true;
                break;
            }
            Object val = entry.getValue();
            Object oldVal = this.mOldContentValues.get(key);
            if (oldVal != null && val != null) {
                isChanged = !val.toString().equals(oldVal.toString());
            } else if (oldVal != null || val != null) {
                isChanged = true;
                break;
            }
        }
        return isChanged;
    }

    protected void insertEmptyRecordIfNeed() {
        Intent intent = getIntent();
        if ("android.intent.action.INSERT".equals(intent.getAction()) && this.mIsDeleted) {
            this.mUri = getContentResolver().insert(intent.getData(), new ContentValues());
            this.mNewApn = true;
            if (this.mUri == null) {
                Log.w(TAG, "Failed to insert new telephony provider into " + getIntent().getData());
                finish();
                return;
            }
            this.mIsDeleted = false;
        }
        this.mIsPresetAPN = isPresetAPN(this.mUri);
    }

    protected void loadDataAndFillUiIfNeed() {
        if (this.mNeedReloadData) {
            this.mCursor = getActivity().managedQuery(this.mUri, sProjection, null, null);
            if (this.mCursor == null || !this.mCursor.moveToFirst()) {
                Log.w(TAG, "cursor moveToFirst is failed!, finish activity");
                finish();
            } else {
                this.mOldContentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(this.mCursor, this.mOldContentValues);
                fillUi(getIntent().getStringExtra("operator"));
                updateCustomizationPreference();
                this.mNeedReloadData = false;
            }
        }
    }

    protected void fillUi(String defaultOperatorNumeric) {
    }

    private void updateCustomizationPreference() {
        boolean isProtocolEditable = true;
        if (System.getInt(getContentResolver(), "apn_pro_editable", 1) <= 0) {
            isProtocolEditable = false;
        }
        if (!isProtocolEditable) {
            this.mProtocol.setEnabled(false);
            this.mRoamingProtocol.setEnabled(false);
        }
    }

    protected String getSimOperator() {
        if (Utils.isMultiSimEnabled()) {
            return MSimTelephonyManager.getDefault().getSimOperator(this.mSlotId);
        }
        return SystemProperties.get("gsm.sim.operator.numeric");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean[] initApnEidtOrDel(Context context, int slotId) {
        boolean[] apnEidtOrDel = new boolean[]{false, false};
        String cardNum = TelephonyManager.from(context).getSimOperator(slotId);
        String confPlmn = System.getString(context.getContentResolver(), "plmn_apn");
        if (!(TextUtils.isEmpty(cardNum) || cardNum.length() <= 4 || confPlmn == null)) {
            String[] mccmncVS = confPlmn.split(";");
            int length = mccmncVS.length;
            int i = 0;
            while (i < length) {
                String[] mcc = mccmncVS[i].split(":");
                if (mcc.length != 3) {
                    return apnEidtOrDel;
                }
                if (cardNum.equals(mcc[0].trim())) {
                    if ("1".equals(mcc[1].trim())) {
                        apnEidtOrDel[0] = true;
                    }
                    if ("1".equals(mcc[2].trim())) {
                        apnEidtOrDel[1] = true;
                    }
                } else {
                    i++;
                }
            }
        }
        return apnEidtOrDel;
    }

    protected boolean validateAndSave(boolean force) {
        return false;
    }
}
