package com.android.contacts.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android.contacts.ContactsActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.android.contacts.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;

public final class ShowOrCreateActivity extends ContactsActivity implements AsyncQueryListener {
    static final String[] CONTACTS_PROJECTION = new String[]{"contact_id", "lookup"};
    static final String[] PHONES_PROJECTION = new String[]{"_id", "lookup"};
    private boolean isContact;
    private String mCreateDescrip;
    private Bundle mCreateExtras;
    private boolean mCreateForce;
    private NotifyingAsyncQueryHandler mQueryHandler;
    private IHarassmentInterceptionService mService;

    class DialogListener implements OnClickListener {
        private ArrayAdapter<Integer> adapter = null;

        public DialogListener(ArrayAdapter<Integer> adapter) {
            this.adapter = adapter;
        }

        public void onClick(DialogInterface dialog, int which) {
            Intent createIntent;
            switch (((Integer) this.adapter.getItem(which)).intValue()) {
                case R.string.pickerNewContactText:
                    createIntent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                    createIntent.putExtras(ShowOrCreateActivity.this.mCreateExtras);
                    ShowOrCreateActivity.this.startActivity(createIntent);
                    dialog.dismiss();
                    ShowOrCreateActivity.this.finish();
                    return;
                case R.string.contact_saveto_existed_contact:
                    createIntent = new Intent("android.intent.action.INSERT_OR_EDIT");
                    createIntent.putExtra("handle_create_new_contact", false);
                    createIntent.putExtras(ShowOrCreateActivity.this.mCreateExtras);
                    createIntent.setType("vnd.android.cursor.item/raw_contact");
                    ShowOrCreateActivity.this.startActivity(createIntent);
                    dialog.dismiss();
                    ShowOrCreateActivity.this.finish();
                    return;
                case R.string.contact_menu_add_to_blacklist:
                    BlacklistCommonUtils.handleNumberBlockList(ShowOrCreateActivity.this, ShowOrCreateActivity.this.mService, ShowOrCreateActivity.this.mCreateExtras.getString("phone").replaceAll(HwCustPreloadContacts.EMPTY_STRING, ""), "", 0, true);
                    dialog.dismiss();
                    ShowOrCreateActivity.this.finish();
                    return;
                case R.string.contact_menu_remove_from_blacklist:
                    BlacklistCommonUtils.handleNumberBlockList(ShowOrCreateActivity.this, ShowOrCreateActivity.this.mService, ShowOrCreateActivity.this.mCreateExtras.getString("phone").replaceAll(HwCustPreloadContacts.EMPTY_STRING, ""), "", 1, true);
                    dialog.dismiss();
                    ShowOrCreateActivity.this.finish();
                    return;
                case R.string.menu_mark_as:
                    try {
                        ShowOrCreateActivity.this.startActivityForResult(NumberMarkUtil.getIntentForMark(ShowOrCreateActivity.this.getApplicationContext(), ShowOrCreateActivity.this.mCreateExtras.getString("phone").replaceAll(HwCustPreloadContacts.EMPTY_STRING, "")), 100);
                    } catch (ActivityNotFoundException e) {
                        HwLog.w("ShowOrCreateActivity", "Activity not found." + e);
                    }
                    dialog.dismiss();
                    ShowOrCreateActivity.this.finish();
                    return;
                default:
                    return;
            }
        }
    }

    private static class IntentClickListener implements OnClickListener {
        private Intent mIntent;
        private Activity mParent;

        public IntentClickListener(Activity parent, Intent intent) {
            this.mParent = parent;
            this.mIntent = intent;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (this.mIntent != null) {
                this.mParent.startActivity(this.mIntent);
            }
            this.mParent.finish();
        }
    }

    private void isPreNumbers(android.os.Bundle r12) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r1 = "phone";
        r1 = r12.getString(r1);
        r2 = " ";
        r3 = "";
        r1 = r1.replaceAll(r2, r3);
        if (r1 == 0) goto L_0x0068;
    L_0x0013:
        r0 = 0;
        r9 = 0;
        r10 = com.android.contacts.hap.provider.ContactsAppDatabaseHelper.getInstance(r11);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r0 = r10.getWritableDatabase();	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r1 = "yellow_page_phone";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = 1;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r3 = "number";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r4 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r3 = "number=?";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r4 = 1;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r5 = "phone";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r5 = r12.getString(r5);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r6 = " ";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r7 = "";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r5 = r5.replaceAll(r6, r7);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r6 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r4[r6] = r5;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r5 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r6 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r7 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        if (r9 == 0) goto L_0x0069;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
    L_0x004c:
        r1 = r9.getCount();	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        if (r1 <= 0) goto L_0x0069;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
    L_0x0052:
        r1 = 0;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r11.isContact = r1;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
    L_0x0055:
        if (r9 == 0) goto L_0x005d;
    L_0x0057:
        r1 = r9.isClosed();
        if (r1 == 0) goto L_0x00a0;
    L_0x005d:
        if (r0 == 0) goto L_0x0068;
    L_0x005f:
        r1 = r0.isOpen();
        if (r1 == 0) goto L_0x0068;
    L_0x0065:
        r0.close();
    L_0x0068:
        return;
    L_0x0069:
        r1 = 1;
        r11.isContact = r1;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        goto L_0x0055;
    L_0x006d:
        r8 = move-exception;
        r1 = "ShowOrCreateActivity";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2.<init>();	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r3 = "Exception: ";	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r3 = r8.toString();	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        com.android.contacts.util.HwLog.e(r1, r2);	 Catch:{ Exception -> 0x006d, all -> 0x00a8 }
        if (r9 == 0) goto L_0x0094;
    L_0x008e:
        r1 = r9.isClosed();
        if (r1 == 0) goto L_0x00a4;
    L_0x0094:
        if (r0 == 0) goto L_0x0068;
    L_0x0096:
        r1 = r0.isOpen();
        if (r1 == 0) goto L_0x0068;
    L_0x009c:
        r0.close();
        goto L_0x0068;
    L_0x00a0:
        r9.close();
        goto L_0x005d;
    L_0x00a4:
        r9.close();
        goto L_0x0094;
    L_0x00a8:
        r1 = move-exception;
        if (r9 == 0) goto L_0x00b1;
    L_0x00ab:
        r2 = r9.isClosed();
        if (r2 == 0) goto L_0x00bd;
    L_0x00b1:
        if (r0 == 0) goto L_0x00bc;
    L_0x00b3:
        r2 = r0.isOpen();
        if (r2 == 0) goto L_0x00bc;
    L_0x00b9:
        r0.close();
    L_0x00bc:
        throw r1;
    L_0x00bd:
        r9.close();
        goto L_0x00b1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.activities.ShowOrCreateActivity.isPreNumbers(android.os.Bundle):void");
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        String setprofile = getIntent().getStringExtra("setprofile");
        if (setprofile == null || !setprofile.equals("setprofile")) {
            if (this.mQueryHandler == null) {
                this.mQueryHandler = new NotifyingAsyncQueryHandler(this, this);
            } else {
                this.mQueryHandler.cancelOperation(42);
            }
            Intent intent = getIntent();
            Uri data = intent.getData();
            String str = null;
            String ssp = null;
            if (data != null) {
                str = data.getScheme();
                ssp = data.getSchemeSpecificPart();
            }
            if (ssp == null || ssp.isEmpty()) {
                finish();
            }
            this.mCreateExtras = new Bundle();
            Bundle originalExtras = intent.getExtras();
            if (originalExtras != null) {
                this.mCreateExtras.putAll(originalExtras);
            }
            this.mCreateDescrip = intent.getStringExtra("com.android.contacts.action.CREATE_DESCRIPTION");
            if (this.mCreateDescrip == null) {
                this.mCreateDescrip = ssp;
            }
            this.mCreateForce = intent.getBooleanExtra("com.android.contacts.action.FORCE_CREATE", false);
            handleSpecificQueryByScheme(str, ssp, this.mCreateExtras, this.mQueryHandler);
        } else {
            showDialog(2);
        }
    }

    private void handleSpecificQueryByScheme(String scheme, String ssp, Bundle bundle, NotifyingAsyncQueryHandler handler) {
        if ("mailto".equals(scheme)) {
            bundle.putString(Scopes.EMAIL, ssp);
            handler.startQuery(42, null, Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(ssp)), CONTACTS_PROJECTION, null, null, null);
        } else if ("tel".equals(scheme)) {
            bundle.putString("phone", ssp);
            handler.startQuery(42, ssp, QueryUtil.getPhoneLookupUri(ssp), PHONES_PROJECTION, null, null, null);
            isPreNumbers(this.mCreateExtras);
        } else {
            HwLog.w("ShowOrCreateActivity", "Invalid intent:" + getIntent());
            finish();
        }
    }

    protected void onStop() {
        super.onStop();
        if (this.mQueryHandler != null) {
            this.mQueryHandler.cancelOperation(42);
        }
    }

    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cursor == null) {
            finish();
            return;
        }
        long contactId = -1;
        String lookupKey = null;
        try {
            int count = cursor.getCount();
            if (count == 1 && cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                lookupKey = cursor.getString(1);
            }
            Intent createIntent;
            if (count == 1 && contactId != -1) {
                int fixedIndex = CommonUtilMethods.getCallerInfoHW(cursor, (String) cookie, "number", "");
                if ((fixedIndex != -1 && cursor.moveToPosition(fixedIndex)) || fixedIndex == -2) {
                    startActivity(new Intent("android.intent.action.VIEW", Contacts.getLookupUri(contactId, lookupKey)));
                    finish();
                } else if (this.mCreateForce) {
                    createIntent = new Intent("android.intent.action.INSERT", RawContacts.CONTENT_URI);
                    createIntent.putExtras(this.mCreateExtras);
                    createIntent.setType("vnd.android.cursor.dir/raw_contact");
                    startActivity(createIntent);
                    finish();
                } else if (!isFinishing()) {
                    showDialog(1);
                }
            } else if (count > 1) {
                Intent listIntent = new Intent("android.intent.action.SEARCH");
                listIntent.setComponent(new ComponentName(this, PeopleActivity.class));
                listIntent.putExtras(this.mCreateExtras);
                startActivity(listIntent);
                finish();
            } else if (this.mCreateForce) {
                createIntent = new Intent("android.intent.action.INSERT", RawContacts.CONTENT_URI);
                createIntent.putExtras(this.mCreateExtras);
                createIntent.setType("vnd.android.cursor.dir/raw_contact");
                startActivity(createIntent);
                finish();
            } else if (!isFinishing()) {
                showDialog(1);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        int lDialogTheme = SystemProperties.getInt("ro.config.hw_customizeType", 0) == 1 ? 2 : 3;
        switch (id) {
            case 1:
                final LayoutInflater dialogInflater = (LayoutInflater) getSystemService("layout_inflater");
                ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, R.layout.select_dialog_item) {
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView result;
                        if (convertView != null) {
                            result = convertView;
                        } else {
                            result = dialogInflater.inflate(R.layout.select_dialog_item, parent, false);
                        }
                        result = result;
                        result.setText(((Integer) getItem(position)).intValue());
                        return result;
                    }
                };
                adapter.add(Integer.valueOf(R.string.pickerNewContactText));
                adapter.add(Integer.valueOf(R.string.contact_saveto_existed_contact));
                String orgin = this.mCreateExtras.getString("phone");
                if (EmuiFeatureManager.isBlackListFeatureEnabled() && orgin != null && MultiUsersUtils.isCurrentUserOwner()) {
                    int blacklistMenuString;
                    this.mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
                    if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(this.mService, orgin.replaceAll(HwCustPreloadContacts.EMPTY_STRING, ""))) {
                        blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
                    } else {
                        blacklistMenuString = R.string.contact_menu_add_to_blacklist;
                    }
                    adapter.add(Integer.valueOf(blacklistMenuString));
                }
                if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && this.isContact) {
                    adapter.add(Integer.valueOf(R.string.menu_mark_as));
                }
                return new Builder(this).setTitle(R.string.add_contact_dlg_title).setSingleChoiceItems(adapter, -1, new DialogListener(adapter)).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        ShowOrCreateActivity.this.finish();
                    }
                }).create();
            case 2:
                CharSequence profilemessage = getResources().getString(R.string.string_setup_profile);
                Intent profileIntent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                profileIntent.putExtra("newLocalProfile", true);
                Builder builder = new Builder(this, lDialogTheme).setTitle(R.string.string_setup_profile).setPositiveButton(17039370, new IntentClickListener(this, profileIntent)).setNegativeButton(17039360, new IntentClickListener(this, null)).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        ShowOrCreateActivity.this.finish();
                    }
                });
                View view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(profilemessage + "?");
                builder.setView(view);
                return builder.create();
            default:
                return super.onCreateDialog(id);
        }
    }
}
