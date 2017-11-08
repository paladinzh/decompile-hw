package com.android.contacts.vcard;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.AccountSelectionUtil.AccountSelectedListener;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.VCardService.MyBinder;
import com.android.vcard.VCardEntry;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class NfcImportVCardActivity extends Activity implements ServiceConnection, VCardImportExportListener {
    private AccountWithDataSet mAccount;
    private AccountSelectedListener mAccountSelectionListener;
    private NdefRecord mRecord;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            NfcImportVCardActivity.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            NfcImportVCardActivity.this.finish();
        }
    }

    class ImportTask extends AsyncTask<VCardService, Void, ImportRequest> {
        ImportTask() {
        }

        public ImportRequest doInBackground(VCardService... services) {
            ImportRequest request = NfcImportVCardActivity.this.createImportRequest();
            if (request == null) {
                return null;
            }
            ArrayList<ImportRequest> requests = new ArrayList();
            requests.add(request);
            services[0].handleImportRequest(requests, NfcImportVCardActivity.this);
            return request;
        }

        public void onCancelled() {
            super.onCancelled();
            NfcImportVCardActivity.this.unbindService(NfcImportVCardActivity.this);
        }

        public void onPostExecute(ImportRequest request) {
            NfcImportVCardActivity.this.unbindService(NfcImportVCardActivity.this);
        }
    }

    com.android.contacts.vcard.ImportRequest createImportRequest() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.contacts.vcard.NfcImportVCardActivity.createImportRequest():com.android.contacts.vcard.ImportRequest. bs: [B:10:0x003a, B:29:0x007e]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r22 = this;
        r10 = 0;
        r12 = 0;
        r8 = 1;
        r19 = new java.io.ByteArrayInputStream;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r0 = r22;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r2 = r0.mRecord;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r2 = r2.getPayload();	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r0 = r19;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r0.<init>(r2);	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r2 = 0;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r0 = r19;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r0.mark(r2);	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r20 = new com.android.vcard.VCardParser_V21;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r20.<init>();	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
        r11 = new com.android.vcard.VCardEntryCounter;	 Catch:{ VCardVersionException -> 0x0057 }
        r11.<init>();	 Catch:{ VCardVersionException -> 0x0057 }
        r13 = new com.android.vcard.VCardSourceDetector;	 Catch:{ VCardVersionException -> 0x0109, all -> 0x00eb }
        r13.<init>();	 Catch:{ VCardVersionException -> 0x0109, all -> 0x00eb }
        r0 = r20;	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r0.addInterpreter(r11);	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r0 = r20;	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r0.addInterpreter(r13);	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r0 = r20;	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r1 = r19;	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        r0.parse(r1);	 Catch:{ VCardVersionException -> 0x010d, all -> 0x00ee }
        if (r19 == 0) goto L_0x003d;
    L_0x003a:
        r19.close();	 Catch:{ IOException -> 0x0055, VCardNestedException -> 0x00e3, VCardException -> 0x00e7 }
    L_0x003d:
        r12 = r13;
        r10 = r11;
    L_0x003f:
        if (r12 == 0) goto L_0x0043;
    L_0x0041:
        if (r10 != 0) goto L_0x00bc;
    L_0x0043:
        r2 = "NfcImportVCardActivity";
        r3 = "NFC import VCard exception.";
        com.android.contacts.util.HwLog.e(r2, r3);
        r2 = "NFC import VCard exception.";
        r3 = 0;
        com.android.contacts.util.ExceptionCapture.captureNfcImportException(r2, r3);
        r2 = 0;
        return r2;
    L_0x0055:
        r16 = move-exception;
        goto L_0x003d;
    L_0x0057:
        r17 = move-exception;
    L_0x0058:
        r19.reset();	 Catch:{ all -> 0x008e }
        r8 = 2;	 Catch:{ all -> 0x008e }
        r21 = new com.android.vcard.VCardParser_V30;	 Catch:{ all -> 0x008e }
        r21.<init>();	 Catch:{ all -> 0x008e }
        r11 = new com.android.vcard.VCardEntryCounter;	 Catch:{ VCardVersionException -> 0x0084, all -> 0x00f2 }
        r11.<init>();	 Catch:{ VCardVersionException -> 0x0084, all -> 0x00f2 }
        r13 = new com.android.vcard.VCardSourceDetector;	 Catch:{ VCardVersionException -> 0x0101, all -> 0x00f6 }
        r13.<init>();	 Catch:{ VCardVersionException -> 0x0101, all -> 0x00f6 }
        r0 = r21;	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r0.addInterpreter(r11);	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r0 = r21;	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r0.addInterpreter(r13);	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r0 = r21;	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r1 = r19;	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        r0.parse(r1);	 Catch:{ VCardVersionException -> 0x0104, all -> 0x00fb }
        if (r19 == 0) goto L_0x003d;
    L_0x007e:
        r19.close();	 Catch:{ IOException -> 0x0082, VCardNestedException -> 0x00e3, VCardException -> 0x00e7 }
        goto L_0x003d;
    L_0x0082:
        r16 = move-exception;
        goto L_0x003d;
    L_0x0084:
        r18 = move-exception;
    L_0x0085:
        r2 = 0;
        if (r19 == 0) goto L_0x008b;
    L_0x0088:
        r19.close();	 Catch:{ IOException -> 0x008c, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
    L_0x008b:
        return r2;
    L_0x008c:
        r16 = move-exception;
        goto L_0x008b;
    L_0x008e:
        r2 = move-exception;
    L_0x008f:
        if (r19 == 0) goto L_0x0094;
    L_0x0091:
        r19.close();	 Catch:{ IOException -> 0x00a3, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
    L_0x0094:
        throw r2;	 Catch:{ IOException -> 0x0095, VCardNestedException -> 0x00b1, VCardException -> 0x00a5 }
    L_0x0095:
        r16 = move-exception;
        r2 = "NfcImportVCardActivity";
        r3 = "Failed reading vcard data";
        r0 = r16;
        com.android.contacts.util.HwLog.e(r2, r3, r0);
        r2 = 0;
        return r2;
    L_0x00a3:
        r16 = move-exception;
        goto L_0x0094;
    L_0x00a5:
        r14 = move-exception;
    L_0x00a6:
        r2 = "NfcImportVCardActivity";
        r3 = "Error parsing vcard";
        com.android.contacts.util.HwLog.e(r2, r3, r14);
        r2 = 0;
        return r2;
    L_0x00b1:
        r15 = move-exception;
    L_0x00b2:
        r2 = "NfcImportVCardActivity";
        r3 = "Nested Exception is found (it may be false-positive).";
        com.android.contacts.util.HwLog.w(r2, r3);
        goto L_0x003f;
    L_0x00bc:
        r0 = r22;
        r2 = r0.mAccount;
        r0 = r22;
        r3 = r0.mRecord;
        r3 = r3.getPayload();
        r4 = 2131362525; // 0x7f0a02dd float:1.8344833E38 double:1.0530330024E-314;
        r0 = r22;
        r5 = r0.getString(r4);
        r6 = r12.getEstimatedType();
        r7 = r12.getEstimatedCharset();
        r9 = r10.getCount();
        r4 = 0;
        r2 = com.android.contacts.vcard.ImportRequest.newInstance(r2, r3, r4, r5, r6, r7, r8, r9);
        return r2;
    L_0x00e3:
        r15 = move-exception;
        r12 = r13;
        r10 = r11;
        goto L_0x00b2;
    L_0x00e7:
        r14 = move-exception;
        r12 = r13;
        r10 = r11;
        goto L_0x00a6;
    L_0x00eb:
        r2 = move-exception;
        r10 = r11;
        goto L_0x008f;
    L_0x00ee:
        r2 = move-exception;
        r12 = r13;
        r10 = r11;
        goto L_0x008f;
    L_0x00f2:
        r2 = move-exception;
        r20 = r21;
        goto L_0x008f;
    L_0x00f6:
        r2 = move-exception;
        r10 = r11;
        r20 = r21;
        goto L_0x008f;
    L_0x00fb:
        r2 = move-exception;
        r12 = r13;
        r10 = r11;
        r20 = r21;
        goto L_0x008f;
    L_0x0101:
        r18 = move-exception;
        r10 = r11;
        goto L_0x0085;
    L_0x0104:
        r18 = move-exception;
        r12 = r13;
        r10 = r11;
        goto L_0x0085;
    L_0x0109:
        r17 = move-exception;
        r10 = r11;
        goto L_0x0058;
    L_0x010d:
        r17 = move-exception;
        r12 = r13;
        r10 = r11;
        goto L_0x0058;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.vcard.NfcImportVCardActivity.createImportRequest():com.android.contacts.vcard.ImportRequest");
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        VCardService service = ((MyBinder) binder).getService();
        new ImportTask().execute(new VCardService[]{service});
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            RefelctionUtils.invokeMethod("setHwFloating", getWindow(), new Object[]{Boolean.valueOf(true)});
        } catch (UnsupportedException e) {
            HwLog.e("NfcImportVCardActivity", "UnsupportedException");
        }
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        Intent intent = getIntent();
        if ("android.nfc.action.NDEF_DISCOVERED".equals(intent.getAction())) {
            String type = intent.getType();
            if (type == null || !("text/x-vcard".equals(type) || "text/vcard".equals(type))) {
                HwLog.w("NfcImportVCardActivity", "Not a vcard");
                finish();
                return;
            }
            Parcelable[] parcelable = intent.getParcelableArrayExtra("android.nfc.extra.NDEF_MESSAGES");
            if (parcelable != null) {
                this.mRecord = parcelable[0].getRecords()[0];
            }
            List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(this).getAccountsExcludeSim(true);
            if (accountList.size() == 0) {
                this.mAccount = null;
            } else if (accountList.size() == 1) {
                this.mAccount = (AccountWithDataSet) accountList.get(0);
            } else {
                this.mAccountSelectionListener = new AccountSelectedListener(this, accountList, R.string.import_from_sdcard) {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        NfcImportVCardActivity.this.mAccount = (AccountWithDataSet) this.mAccountList.get(which);
                        NfcImportVCardActivity.this.startImport();
                    }
                };
                showDialog(R.string.import_from_sdcard);
                return;
            }
            startImport();
            return;
        }
        HwLog.w("NfcImportVCardActivity", "Unknowon intent " + intent);
        finish();
    }

    protected Dialog onCreateDialog(int resId, Bundle bundle) {
        switch (resId) {
            case R.string.import_from_sdcard:
                if (this.mAccountSelectionListener != null) {
                    return AccountSelectionUtil.getSelectAccountDialog(this, resId, this.mAccountSelectionListener, new CancelListener(), true);
                }
                throw new NullPointerException("mAccountSelectionListener must not be null.");
            default:
                return super.onCreateDialog(resId, bundle);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != 1) {
            return;
        }
        if (resultCode == -1) {
            this.mAccount = new AccountWithDataSet(intent.getStringExtra("account_name"), intent.getStringExtra("account_type"), intent.getStringExtra("data_set"));
            startImport();
            return;
        }
        finish();
    }

    private void startImport() {
        Intent intent = new Intent(this, VCardService.class);
        startService(intent);
        bindService(intent, this, 1);
    }

    public void onImportProcessed(ImportRequest request, int jobId, int sequence) {
    }

    public void onImportParsed(ImportRequest request, int jobId, VCardEntry entry, int currentCount, int totalCount, VCardService service) {
    }

    public void onImportFinished(ImportRequest request, int jobId, Uri uri) {
        if (isFinishing()) {
            HwLog.i("NfcImportVCardActivity", "Late import -- ignoring");
            return;
        }
        if (uri != null) {
            startActivity(new Intent("android.intent.action.VIEW", RawContacts.getContactLookupUri(getContentResolver(), uri)));
            finish();
        }
    }

    public void onImportFailed(ImportRequest request) {
        if (isFinishing()) {
            HwLog.i("NfcImportVCardActivity", "Late import failure -- ignoring");
        }
    }

    public void onImportCanceled(ImportRequest request, int jobId) {
    }

    public void onExportProcessed(ExportRequest request, int jobId) {
    }

    public void onExportFailed(ExportRequest request) {
    }

    public void onCancelRequest(CancelRequest request, int type) {
    }

    public void onMemoryFull(ImportRequest request, int jobId) {
    }
}
