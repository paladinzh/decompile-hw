package com.android.contacts;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.BackScrollManager.ScrollableHeader;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.format.FormatUtils;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.calllog.CallRecord.CallRecordItem;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.Constants;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.phone.hap.service.IPhoneServer;
import com.android.phone.hap.service.IPhoneServer.Stub;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import java.io.File;
import java.util.ArrayList;

public class CallDetailActivity extends Activity implements SimStateListener {
    static final String[] CALL_LOG_PROJECTION;
    static final int COLUMN_POST_DIAL_DIGITS;
    static final int IS_CLOUD_MARK;
    static final int MARK_CONTENT;
    static final int MARK_COUNT;
    static final int MARK_TYPE;
    static final int RING_TIMES;
    static final int SUBSCRIPTION;
    static final String[] _PROJECTION = new String[]{"date", "duration", "number", "type", "countryiso", "geocoded_location", "presentation", "features"};
    private static boolean mIsDualSim;
    long deletedId = 0;
    private CallDetailHistoryAdapter mAdapter;
    private AsyncTaskExecutor mAsyncTaskExecutor;
    private View mCallKnownContactContainer;
    private CallLogTableListener mCallLogChangeObserver;
    private long[] mCallLogIds;
    private String mCallNumberCountryIso = null;
    private CallTypeHelper mCallTypeHelper;
    private View mCallUnknownContactContainer;
    private ImageView mContactBackgroundView;
    private ContactInfoHelper mContactInfoHelper;
    private ContactPhotoManager mContactPhotoManager;
    private String mDefaultCountryIso;
    private Boolean mDualSimBtnClick;
    private final OnClickListener mDualSimCallListener = new OnClickListener() {
        public void onClick(View v) {
            int i = 0;
            Uri numberCallUri = CallDetailActivity.this.mPhoneNumberHelper.getCallUri(CallDetailActivity.this.mNumber);
            switch (v.getId()) {
                case R.id.sim1_call:
                    if (CallDetailActivity.this.mIsFirstSimEnabled && CallDetailActivity.this.mIsSecondSimEnabled) {
                        CommonUtilMethods.dialNumberFromcalllog(CallDetailActivity.this, numberCallUri, CallDetailActivity.this.getResources().getString(R.string.call_other), -1, false, true, CallDetailActivity.this.mNumber);
                    } else {
                        CallDetailActivity callDetailActivity = CallDetailActivity.this;
                        if (CallDetailActivity.this.mIsSecondSimEnabled) {
                            i = 1;
                        }
                        callDetailActivity.dialNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(i), numberCallUri);
                    }
                    CallDetailActivity.this.mDualSimBtnClick = Boolean.valueOf(true);
                    return;
                case R.id.sim2_call:
                    CallDetailActivity.this.dialNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(1), numberCallUri);
                    CallDetailActivity.this.mDualSimBtnClick = Boolean.valueOf(true);
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView mDualSimSMS;
    private final Handler mHandler = new Handler();
    private boolean mHasEditNumberBeforeCallOption;
    private TextView mHeaderTextView;
    LayoutInflater mInflater;
    private boolean mIsContact;
    boolean mIsFirstSimEnabled = false;
    private boolean mIsFromDialer = false;
    private boolean mIsPaused;
    private boolean mIsPhoneConnectionBind = false;
    boolean mIsSecondSimEnabled = false;
    private ImageButton mMainActionPushLayerView;
    private String mName = "";
    private String mNameAndNumberSendByMMS;
    private String mNameOrNumber;
    private String mNumber = null;
    private TextView mNumberMakInfoView;
    private Menu mOptionsMenu = null;
    private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private ServiceConnection mPhoneConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            HwLog.d("CallDetail", "connect service");
            CallDetailActivity.this.mPhoneService = Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            HwLog.d("CallDetail", "disconnect service");
            CallDetailActivity.this.mPhoneService = null;
        }
    };
    private PhoneNumberHelper mPhoneNumberHelper;
    private CharSequence mPhoneNumberLabelToCopy;
    private CharSequence mPhoneNumberToCopy;
    private IPhoneServer mPhoneService;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state != 2 && !CallDetailActivity.this.isFinishing()) {
                CallDetailActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        CallDetailActivity.this.callButton();
                    }
                });
            }
        }
    };
    private String mPostDialDigits;
    private int mPresentation;
    Resources mResources;
    private ImageView mSim1Call;
    private ImageView mSim2Call;
    private ImageView mSimIPCall;
    private AsyncTask mUpdateContactDetailsTask;

    private class CallLogTableListener extends ContentObserver {
        public CallLogTableListener(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (HwLog.HWDBG) {
                HwLog.d("CallDetail", "Call log table is changed so re querying for the data.");
            }
            if (!CallDetailActivity.this.mIsFromDialer) {
                CallDetailActivity.this.mCallLogIds = null;
                if (!CallDetailActivity.this.mIsPaused) {
                    CallDetailActivity.this.updateData(CallDetailActivity.this.mCallLogIds);
                }
            }
        }
    }

    public enum Tasks {
        MARK_VOICEMAIL_READ,
        DELETE_VOICEMAIL_AND_FINISH,
        REMOVE_FROM_CALL_LOG_AND_FINISH,
        UPDATE_PHONE_CALL_DETAILS
    }

    class UpdateContactDetailsTask extends AsyncTask<Void, Void, PhoneCallDetails[]> {
        private long[] callUris;
        private Context mContext;

        public com.android.contacts.PhoneCallDetails[] doInBackground(java.lang.Void... r21) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0106 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r20 = this;
            r0 = r20;
            r3 = r0.callUris;
            if (r3 != 0) goto L_0x0047;
        L_0x0006:
            r0 = r20;
            r3 = com.android.contacts.CallDetailActivity.this;
            r3 = r3.mNumber;
            r3 = android.text.TextUtils.isEmpty(r3);
            if (r3 != 0) goto L_0x003a;
        L_0x0014:
            r0 = r20;
            r3 = com.android.contacts.CallDetailActivity.this;
            r3 = r3.getCallLogIdsForNumber();
            r0 = r20;
            r0.callUris = r3;
        L_0x0020:
            r0 = r20;
            r3 = com.android.contacts.CallDetailActivity.this;
            r3 = r3.getIntent();
            r4 = "EXTRA_CALL_LOG_IDS";
            r0 = r20;
            r5 = r0.callUris;
            r3.putExtra(r4, r5);
            r0 = r20;
            r3 = r0.callUris;
            if (r3 != 0) goto L_0x0047;
        L_0x0038:
            r3 = 0;
            return r3;
        L_0x003a:
            r0 = r20;
            r3 = com.android.contacts.CallDetailActivity.this;
            r3 = r3.getCallLogIdsForUnknowNumber();
            r0 = r20;
            r0.callUris = r3;
            goto L_0x0020;
        L_0x0047:
            r0 = r20;
            r3 = r0.callUris;
            r0 = r3.length;
            r18 = r0;
            r12 = new java.lang.StringBuilder;
            r12.<init>();
            r0 = r20;
            r4 = r0.callUris;
            r3 = 0;
            r5 = r4.length;
        L_0x0059:
            if (r3 >= r5) goto L_0x006f;
        L_0x005b:
            r10 = r4[r3];
            r6 = r12.length();
            if (r6 == 0) goto L_0x0069;
        L_0x0063:
            r6 = ",";
            r12.append(r6);
        L_0x0069:
            r12.append(r10);
            r3 = r3 + 1;
            goto L_0x0059;
        L_0x006f:
            r0 = r20;
            r3 = com.android.contacts.CallDetailActivity.this;
            r2 = r3.getContentResolver();
            r3 = com.android.contacts.compatibility.QueryUtil.getCallsContentUri();
            r0 = r20;
            r4 = com.android.contacts.CallDetailActivity.this;
            r4 = r4.getCallLogProjection();
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "_id IN (";
            r5 = r5.append(r6);
            r5 = r5.append(r12);
            r6 = ")";
            r5 = r5.append(r6);
            r5 = r5.toString();
            r7 = "date DESC";
            r6 = 0;
            r8 = r2.query(r3, r4, r5, r6, r7);
            if (r8 != 0) goto L_0x00aa;
        L_0x00a8:
            r3 = 0;
            return r3;
        L_0x00aa:
            r3 = r8.getColumnNames();
            r4 = "subscription";
            r3 = com.android.contacts.compatibility.QueryUtil.isContainColumn(r3, r4);
            if (r3 != 0) goto L_0x00bd;
        L_0x00b7:
            r9 = new com.android.contacts.compatibility.ExtendedSubscriptionCursor;
            r9.<init>(r8);
            r8 = r9;
        L_0x00bd:
            r13 = new java.util.ArrayList;
            r0 = r18;
            r13.<init>(r0);
            r17 = 1;
            r15 = 0;
        L_0x00c7:
            r3 = r8.moveToNext();	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            if (r3 == 0) goto L_0x00e5;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
        L_0x00cd:
            r0 = r20;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r3 = com.android.contacts.CallDetailActivity.this;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r0 = r17;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r16 = r3.getPhoneCallDetailsForUri(r8, r15, r0);	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r0 = r16;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r13.add(r0);	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            if (r17 == 0) goto L_0x00c7;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
        L_0x00de:
            r17 = 0;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r0 = r16;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r15 = r0.contactInfo;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            goto L_0x00c7;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
        L_0x00e5:
            r0 = r18;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r0 = new com.android.contacts.PhoneCallDetails[r0];	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r19 = r0;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r0 = r19;	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r13.toArray(r0);	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            if (r8 == 0) goto L_0x00f5;
        L_0x00f2:
            r8.close();
        L_0x00f5:
            return r19;
        L_0x00f6:
            r14 = move-exception;
            r3 = "CallDetail";	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r4 = "invalid URI starting call details";	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            com.android.contacts.util.HwLog.w(r3, r4, r14);	 Catch:{ IllegalArgumentException -> 0x00f6, all -> 0x0107 }
            r3 = 0;
            if (r8 == 0) goto L_0x0106;
        L_0x0103:
            r8.close();
        L_0x0106:
            return r3;
        L_0x0107:
            r3 = move-exception;
            if (r8 == 0) goto L_0x010d;
        L_0x010a:
            r8.close();
        L_0x010d:
            throw r3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.CallDetailActivity.UpdateContactDetailsTask.doInBackground(java.lang.Void[]):com.android.contacts.PhoneCallDetails[]");
        }

        public UpdateContactDetailsTask(long[] callUris) {
            this.callUris = callUris;
            this.mContext = CallDetailActivity.this;
        }

        private boolean isDetailsValid(PhoneCallDetails[] details) {
            return details == null || details.length == 0 || details[0] == null;
        }

        public void onPostExecute(PhoneCallDetails[] details) {
            if (isDetailsValid(details)) {
                CallDetailActivity.this.finish();
            } else if (CallDetailActivity.this != null && !CallDetailActivity.this.isFinishing()) {
                CharSequence nameOrNumber;
                boolean z;
                int i;
                String charSequence;
                PhoneCallDetails firstDetails = details[0];
                ContactInfo IContactInfo = firstDetails.contactInfo;
                String tmpNumber = CallDetailActivity.this.getIntent().getStringExtra("EXTRA_CALL_LOG_NUMBER");
                boolean isFromDialCallLog = false;
                if (tmpNumber != null) {
                    if (!tmpNumber.equalsIgnoreCase(firstDetails.number.toString())) {
                        int arraySize = details.length;
                        for (int i2 = 0; i2 < arraySize; i2++) {
                            if (details[i2] != null) {
                                CharSequence tmpNum = details[i2].number;
                                if (!TextUtils.isEmpty(tmpNum) && tmpNum.toString().equalsIgnoreCase(tmpNumber)) {
                                    isFromDialCallLog = true;
                                    firstDetails = details[i2];
                                    break;
                                }
                            }
                        }
                    }
                }
                if (firstDetails.number != null) {
                    CallDetailActivity.this.mNumber = firstDetails.number.toString();
                    CallDetailActivity.this.mPostDialDigits = CompatUtils.isNCompatible() ? firstDetails.postDialDigits : "";
                }
                Uri contactUri = firstDetails.contactUri;
                Uri photoUri = firstDetails.photoUri;
                long photoId = IContactInfo != null ? IContactInfo.photoId : 0;
                CallDetailActivity.this.mPhoneCallDetailsHelper.setCallDetailsHeader(CallDetailActivity.this.mHeaderTextView, firstDetails, CallDetailActivity.this.getResources().getConfiguration().orientation, CallDetailActivity.this.getActionBar());
                boolean canPlaceCallsTo = CallDetailActivity.this.mPhoneNumberHelper.canPlaceCallsTo(CallDetailActivity.this.mNumber, CallDetailActivity.this.mPresentation);
                boolean isVoicemailNumber = CallDetailActivity.this.mPhoneNumberHelper.isVoicemailNumber(CallDetailActivity.this.mNumber);
                boolean isSipNumber = CallDetailActivity.this.mPhoneNumberHelper.isSipNumber(CallDetailActivity.this.mNumber);
                if (TextUtils.isEmpty(firstDetails.name)) {
                    nameOrNumber = firstDetails.number;
                    CallDetailActivity.this.mName = "";
                } else {
                    nameOrNumber = firstDetails.name;
                    CallDetailActivity.this.mName = firstDetails.name.toString();
                }
                if (contactUri == null) {
                    CallDetailActivity.this.mNameAndNumberSendByMMS = CallDetailActivity.this.mNumber;
                } else {
                    CallDetailActivity.this.mNameAndNumberSendByMMS = firstDetails.name + "\n" + CallDetailActivity.this.mNumber;
                }
                if (nameOrNumber != null) {
                    CallDetailActivity.this.mNameOrNumber = nameOrNumber.toString();
                }
                CallDetailActivity.this.mMainActionPushLayerView.setVisibility(8);
                CallDetailActivity.this.mHeaderTextView.setVisibility(0);
                if (canPlaceCallsTo) {
                    CharSequence formattedNumber = firstDetails.formattedNumber;
                    if (isFromDialCallLog) {
                        ContactInfo tmpInfo = CallDetailActivity.this.mContactInfoHelper.lookupNumber(firstDetails.number.toString(), firstDetails.countryIso);
                        if (tmpInfo != null) {
                            formattedNumber = tmpInfo.formattedNumber;
                        }
                    }
                    CharSequence displayNumber = CallDetailActivity.this.mPhoneNumberHelper.getDisplayNumber(firstDetails.number, firstDetails.getPresentation(), formattedNumber, firstDetails.postDialDigits);
                    ViewEntry viewEntry = new ViewEntry((String) FormatUtils.forceLeftToRight(displayNumber), CallUtil.getCallIntent(CallDetailActivity.this.mNumber), (String) nameOrNumber);
                    if (!(TextUtils.isEmpty(firstDetails.number) || PhoneNumberUtils.isUriNumber(firstDetails.number.toString()))) {
                        int type = firstDetails.numberType;
                        if (type < 1 && TextUtils.isEmpty(firstDetails.name)) {
                            type = 2;
                        }
                        if (TextUtils.isEmpty(firstDetails.geocode)) {
                            if (firstDetails.contactUri != null) {
                                viewEntry.label = Phone.getTypeLabel(CallDetailActivity.this.mResources, type, firstDetails.numberLabel);
                            }
                        } else if (firstDetails.contactUri == null) {
                            viewEntry.label = firstDetails.geocode;
                        } else {
                            viewEntry.label = Phone.getTypeLabel(CallDetailActivity.this.mResources, type, firstDetails.numberLabel) + " - " + firstDetails.geocode;
                        }
                    }
                    CallDetailActivity.this.configureCallButton(viewEntry);
                    CallDetailActivity.this.callButton();
                    CallDetailActivity.this.mPhoneNumberToCopy = displayNumber;
                    CallDetailActivity.this.mPhoneNumberLabelToCopy = viewEntry.label;
                } else {
                    CallDetailActivity.this.disableCallAndSmsItem();
                    CallDetailActivity.this.mPhoneNumberToCopy = null;
                    CallDetailActivity.this.mPhoneNumberLabelToCopy = null;
                }
                boolean isNameEmpty = TextUtils.isEmpty(firstDetails.name);
                CallDetailActivity callDetailActivity = CallDetailActivity.this;
                if (firstDetails.contactUri == null) {
                    z = false;
                } else {
                    z = true;
                }
                callDetailActivity.mIsContact = z;
                View -get4 = CallDetailActivity.this.mCallUnknownContactContainer;
                if (isNameEmpty || !CallDetailActivity.this.mIsContact) {
                    i = 0;
                } else {
                    i = 8;
                }
                -get4.setVisibility(i);
                -get4 = CallDetailActivity.this.mCallKnownContactContainer;
                if (isNameEmpty || !CallDetailActivity.this.mIsContact) {
                    i = 8;
                } else {
                    i = 0;
                }
                -get4.setVisibility(i);
                CallDetailActivity.this.mMainActionPushLayerView.setVisibility(8);
                Button newContactButton = (Button) CallDetailActivity.this.findViewById(R.id.call_new_contact);
                if (firstDetails.name != null) {
                    charSequence = firstDetails.name.toString();
                } else {
                    charSequence = null;
                }
                final String str = charSequence;
                newContactButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                        if (!TextUtils.isEmpty(str)) {
                            intent.putExtra("name", str);
                        }
                        intent.putExtra("phone", CallDetailActivity.this.mNumber);
                        CallDetailActivity.this.startActivity(intent);
                    }
                });
                Button addContactButton = (Button) CallDetailActivity.this.findViewById(R.id.call_add_contact);
                addContactButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
                        intent.putExtra("phone", CallDetailActivity.this.mNumber);
                        intent.putExtra("handle_create_new_contact", false);
                        intent.setType("vnd.android.cursor.item/contact");
                        CallDetailActivity.this.startActivity(intent);
                    }
                });
                if (Constants.isFontSizeHugeorMore()) {
                    addContactButton.setSingleLine();
                    newContactButton.setSingleLine();
                    addContactButton.setEllipsize(TruncateAt.END);
                    newContactButton.setEllipsize(TruncateAt.END);
                }
                Button viewContactButton = (Button) CallDetailActivity.this.findViewById(R.id.view_contact);
                final Uri uri = firstDetails.contactUri;
                viewContactButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent("android.intent.action.VIEW", uri);
                        intent.setFlags(335544320);
                        CallDetailActivity.this.startActivity(intent);
                    }
                });
                callDetailActivity = CallDetailActivity.this;
                z = (!canPlaceCallsTo || isSipNumber || isVoicemailNumber) ? false : true;
                callDetailActivity.mHasEditNumberBeforeCallOption = z;
                CallDetailActivity.this.invalidateOptionsMenu();
                ListView historyList = (ListView) CallDetailActivity.this.findViewById(R.id.history);
                historyList.setFastScrollEnabled(true);
                CallDetailActivity.this.mAdapter = CallDetailHistoryAdapter.newInstance(CallDetailActivity.this, CallDetailActivity.this.mInflater, CallDetailActivity.this.mCallTypeHelper, details, CallDetailActivity.this.hasVoicemail(), canPlaceCallsTo, CallDetailActivity.this.findViewById(R.id.controls));
                historyList.setAdapter(CallDetailActivity.this.mAdapter);
                CallDetailActivity.this.mAdapter.setupCallRecords(CallDetailActivity.this.mNumber);
                historyList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        PhoneCallDetails details = (PhoneCallDetails) parent.getItemAtPosition(position);
                        if (details != null) {
                            CallRecordItem[] items = details.mCallRecordItems;
                            if (items != null && items.length > 0) {
                                if (items.length == 1) {
                                    CallDetailActivity.this.startRecordPlaybackSafely(UpdateContactDetailsTask.this.mContext, items[0].mAbsolutePath);
                                } else {
                                    String[] itemName = new String[items.length];
                                    String[] itemPath = new String[items.length];
                                    String[] time = new String[items.length];
                                    String[] data = new String[items.length];
                                    for (int i = 0; i < items.length; i++) {
                                        itemPath[i] = items[i].mAbsolutePath;
                                        int timeIdenx = itemPath[i].lastIndexOf(46);
                                        int dataIndex = itemPath[i].lastIndexOf(95);
                                        time[i] = itemPath[i].substring(timeIdenx - 6, timeIdenx);
                                        data[i] = itemPath[i].substring(dataIndex + 1, dataIndex + 9);
                                        itemName[i] = UpdateContactDetailsTask.this.mContext.getString(R.string.call_record) + HwCustPreloadContacts.EMPTY_STRING + data[i] + "_" + time[i];
                                    }
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArray("item_name", itemName);
                                    bundle.putStringArray("item_path", itemPath);
                                    ((Activity) UpdateContactDetailsTask.this.mContext).showDialog(257, bundle);
                                }
                            }
                        }
                    }
                });
                BackScrollManager.bind(new ScrollableHeader() {
                    private View mCallLogsSep = CallDetailActivity.this.findViewById(R.id.call_log_separator_container);
                    private View mCallSmsContainer = CallDetailActivity.this.findViewById(R.id.call_and_sms_container);
                    private View mControls = CallDetailActivity.this.findViewById(R.id.controls);
                    private View mPhoto = CallDetailActivity.this.findViewById(R.id.contact_background_sizer);
                    int orientation = CallDetailActivity.this.getApplicationContext().getResources().getConfiguration().orientation;

                    public void setOffset(int offset) {
                        if (2 == this.orientation) {
                            this.mCallSmsContainer.setY((float) (-offset));
                        } else {
                            this.mControls.setY((float) (-offset));
                        }
                    }

                    public int getMaximumScrollableHeaderOffset() {
                        if (2 == this.orientation) {
                            return this.mCallSmsContainer.getHeight();
                        }
                        return ((this.mPhoto.getHeight() + this.mCallSmsContainer.getHeight()) + this.mCallLogsSep.getHeight()) + CallDetailActivity.this.mCallUnknownContactContainer.getHeight();
                    }
                }, historyList);
                CallDetailActivity.this.loadContactPhotos(photoId, photoUri, firstDetails.isPrivate(), contactUri);
                CallDetailActivity.this.displayNumberMark(firstDetails.numberMarkInfo);
                CallDetailActivity.this.findViewById(R.id.call_detail).setVisibility(0);
            }
        }
    }

    public static final class ViewEntry {
        public CharSequence label = null;
        public final String primaryDescription;
        public final Intent primaryIntent;
        public final String text;

        public ViewEntry(String text, Intent intent, String description) {
            this.text = text;
            this.primaryIntent = intent;
            this.primaryDescription = description;
        }
    }

    static {
        int current_column_index = 8;
        if (EmuiFeatureManager.isRingTimesDisplayEnabled(null)) {
            current_column_index = 9;
        }
        if (QueryUtil.isSupportDualSim()) {
            current_column_index++;
        }
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
            current_column_index += 4;
        }
        if (CompatUtils.isNCompatible()) {
            current_column_index++;
        }
        CALL_LOG_PROJECTION = new String[current_column_index];
        System.arraycopy(_PROJECTION, 0, CALL_LOG_PROJECTION, 0, 8);
        current_column_index = 8;
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
            MARK_TYPE = 8;
            CALL_LOG_PROJECTION[8] = "mark_type";
            MARK_CONTENT = 9;
            int i = 9 + 1;
            CALL_LOG_PROJECTION[9] = "mark_content";
            IS_CLOUD_MARK = i;
            current_column_index = i + 1;
            CALL_LOG_PROJECTION[i] = "is_cloud_mark";
            MARK_COUNT = current_column_index;
            i = current_column_index + 1;
            CALL_LOG_PROJECTION[current_column_index] = "mark_count";
            current_column_index = i;
        } else {
            MARK_TYPE = 0;
            MARK_CONTENT = 0;
            IS_CLOUD_MARK = 0;
            MARK_COUNT = 0;
        }
        if (EmuiFeatureManager.isRingTimesDisplayEnabled(null)) {
            RING_TIMES = current_column_index;
            i = current_column_index + 1;
            CALL_LOG_PROJECTION[current_column_index] = "ring_times";
            current_column_index = i;
        } else {
            RING_TIMES = 0;
        }
        if (QueryUtil.isSupportDualSim()) {
            SUBSCRIPTION = current_column_index;
            i = current_column_index + 1;
            CALL_LOG_PROJECTION[current_column_index] = "subscription";
            current_column_index = i;
        } else {
            SUBSCRIPTION = current_column_index;
        }
        if (CompatUtils.isNCompatible()) {
            COLUMN_POST_DIAL_DIGITS = current_column_index;
            i = current_column_index + 1;
            CALL_LOG_PROJECTION[current_column_index] = "post_dial_digits";
            current_column_index = i;
            return;
        }
        COLUMN_POST_DIAL_DIGITS = 0;
    }

    private void dialNumber(int aSubId, Uri numberCallUri) {
        Intent intent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", numberCallUri);
        PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, 0);
        if (accountHandle != null) {
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
        }
        try {
            MSimSmsManagerEx.setSimIdToIntent(intent, aSubId);
        } catch (Exception e) {
            intent.putExtra("subscription", aSubId);
            e.printStackTrace();
        }
        startActivity(intent);
        StatisticalHelper.reportDialPortal(this, 2);
        if (HwLog.HWFLOW) {
            HwLog.i("CallDetail", "dialNumber DIAL_TYPE_CALL_LOG");
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(R.style.CallDetailActivityTheme);
        setContentView(R.layout.call_detail);
        if (icicle != null) {
            this.mCallLogIds = icicle.getLongArray("EXTRA_CALL_LOG_IDS");
            this.mNumber = icicle.getString("EXTRA_FROM_NOTIFICATION");
            this.mCallNumberCountryIso = icicle.getString("EXTRA_CALL_LOG_COUNTRY_ISO");
            this.mPresentation = icicle.getInt("EXTRA_CALL_LOG_PRESENTATION");
        } else {
            if (getIntent().getBooleanExtra("EXTRA_IGNORE_INCOMING_CALLLOG_IDS", false)) {
                getIntent().setData(null);
                getIntent().putExtra("EXTRA_CALL_LOG_IDS", (Long[]) null);
            } else {
                this.mCallLogIds = getCallLogEntryUris();
            }
            this.mNumber = getIntent().getStringExtra("EXTRA_CALL_LOG_NUMBER");
            this.mCallNumberCountryIso = getIntent().getStringExtra("EXTRA_CALL_LOG_COUNTRY_ISO");
            this.mPresentation = getIntent().getIntExtra("EXTRA_CALL_LOG_PRESENTATION", 1);
        }
        this.mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mResources = getResources();
        this.mCallTypeHelper = new CallTypeHelper(getResources());
        this.mPhoneNumberHelper = new PhoneNumberHelper(this.mResources);
        this.mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(getApplicationContext(), this.mCallTypeHelper, this.mPhoneNumberHelper);
        this.mHeaderTextView = (TextView) findViewById(R.id.header_name);
        this.mCallUnknownContactContainer = findViewById(R.id.call_unknown_contact_separator_container);
        this.mCallKnownContactContainer = findViewById(R.id.view_known_contact);
        this.mMainActionPushLayerView = (ImageButton) findViewById(R.id.main_action_push_layer);
        this.mContactBackgroundView = (ImageView) findViewById(R.id.contact_background);
        this.mNumberMakInfoView = (TextView) findViewById(R.id.numbermark_info);
        mIsDualSim = SimFactoryManager.isDualSim();
        this.mSimIPCall = (ImageView) findViewById(R.id.sim_call_ip);
        this.mSim2Call = (ImageView) findViewById(R.id.sim2_call);
        this.mSim1Call = (ImageView) findViewById(R.id.sim1_call);
        this.mDualSimSMS = (ImageView) findViewById(R.id.dualsim_sms);
        this.mSim1Call.setOnClickListener(this.mDualSimCallListener);
        this.mSim2Call.setOnClickListener(this.mDualSimCallListener);
        this.mSimIPCall.setOnClickListener(this.mDualSimCallListener);
        this.mDualSimSMS.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    CallDetailActivity.this.startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("sms", CommonUtilMethods.deleteIPHead(CallDetailActivity.this.mNumber), null)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CallDetailActivity.this, R.string.quickcontact_missing_app_Toast, 0).show();
                }
            }
        });
        this.mDefaultCountryIso = GeoUtil.getCurrentCountryIso(this);
        this.mContactPhotoManager = ContactPhotoManager.getInstance(this);
        this.mContactInfoHelper = new ContactInfoHelper(this, GeoUtil.getCurrentCountryIso(this));
        configureActionBar();
        ListView historyList = (ListView) findViewById(R.id.history);
        if (historyList != null) {
            historyList.setFastScrollEnabled(true);
            historyList.setOnCreateContextMenuListener(this);
        }
        if (getIntent().getBooleanExtra("EXTRA_FROM_NOTIFICATION", false)) {
            closeSystemDialogs();
        }
        this.mCallLogChangeObserver = new CallLogTableListener(this.mHandler);
        getContentResolver().registerContentObserver(QueryUtil.getCallsContentUri(), true, this.mCallLogChangeObserver);
    }

    public void onResume() {
        super.onResume();
        if (MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this)) {
            this.mPhoneCallDetailsHelper.resetTimeFormats();
        }
        updateData(this.mCallLogIds);
        this.mIsPaused = false;
        SimFactoryManager.listenPhoneState(this.mPhoneStateListener, 32);
        this.mDualSimBtnClick = Boolean.valueOf(false);
        SimFactoryManager.addSimStateListener(this);
        this.mIsFromDialer = getIntent().getBooleanExtra("INTENT_FROM_DIALER", false);
    }

    protected void onStart() {
        super.onStart();
        Bundle args = new Bundle();
        Intent intent = new Intent();
        intent.setPackage("com.android.phone");
        intent.setAction("com.android.phone.hap.service.PhoneServer");
        intent.putExtras(args);
        this.mIsPhoneConnectionBind = bindService(intent, this.mPhoneConnection, 1);
    }

    private boolean hasVoicemail() {
        return getVoicemailUri() != null;
    }

    private Uri getVoicemailUri() {
        return (Uri) getIntent().getParcelableExtra("EXTRA_VOICEMAIL_URI");
    }

    private long[] getCallLogEntryUris() {
        if (getIntent().getData() == null) {
            return getIntent().getLongArrayExtra("EXTRA_CALL_LOG_IDS");
        }
        return new long[]{Long.parseLong(getIntent().getData().getLastPathSegment())};
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 5:
                if (SimFactoryManager.getCallState() == 0) {
                    startActivity(CallUtil.getCallIntent(Uri.fromParts("tel", this.mNumber, null)));
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateData(long[] callUris) {
        this.mUpdateContactDetailsTask = this.mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateContactDetailsTask(callUris), new Void[0]);
    }

    private PhoneCallDetails getPhoneCallDetailsForUri(Cursor callCursor, ContactInfo info, boolean loadContactInfo) {
        if (callCursor == null) {
            throw new IllegalArgumentException("Cannot find content: ");
        }
        CharSequence formattedNumber;
        CharSequence nameText;
        int numberType;
        CharSequence numberLabel;
        Uri photoUri;
        Uri uri;
        String number = callCursor.getString(2);
        String postDialDigits = CompatUtils.isNCompatible() ? callCursor.getString(COLUMN_POST_DIAL_DIGITS) : "";
        int numPresentation = callCursor.getInt(6);
        long date = callCursor.getLong(0);
        long duration = callCursor.getLong(1);
        int callType = callCursor.getInt(3);
        String countryIso = callCursor.getString(4);
        int lSubID = callCursor.getInt(SUBSCRIPTION);
        long callFeature = callCursor.getLong(7);
        if (TextUtils.isEmpty(countryIso)) {
            countryIso = this.mDefaultCountryIso;
        }
        boolean isVoiceMailNum = PhoneNumberUtils.isVoiceMailNumber(number);
        if (loadContactInfo) {
            if (this.mPhoneNumberHelper.canPlaceCallsTo(number, numPresentation)) {
                info = this.mContactInfoHelper.lookupNumber(number, countryIso);
            } else {
                info = null;
            }
        }
        boolean lIsPrivate = false;
        if (info == null) {
            formattedNumber = this.mPhoneNumberHelper.getDisplayNumber(number, numPresentation, null, postDialDigits, isVoiceMailNum);
            nameText = "";
            numberType = 0;
            numberLabel = "";
            photoUri = null;
            uri = null;
        } else {
            Object formattedNumber2 = info.formattedNumber;
            Object nameText2 = info.name;
            numberType = info.type;
            Object numberLabel2 = info.label;
            photoUri = info.photoUri;
            uri = info.lookupUri;
            lIsPrivate = info.mIsPrivate;
        }
        String geocode = "";
        geocode = callCursor.getString(5);
        if (PhoneCapabilityTester.isGeoCodeFeatureEnabled(this) && !QueryUtil.checkGeoLocation(geocode, number)) {
            geocode = NumberLocationCache.getLocation(number);
            if (geocode == null) {
                geocode = NumberLocationLoader.getAndUpdateGeoNumLocation(this, number);
            }
        }
        if (TextUtils.isEmpty(geocode)) {
            String displayCountry = QueryUtil.getDefaultLocation(countryIso);
            if (!TextUtils.isEmpty(displayCountry)) {
                geocode = displayCountry;
            }
        }
        int ringTimes = 1;
        if (callCursor.getColumnIndex("ring_times") >= 0) {
            ringTimes = callCursor.getInt(RING_TIMES);
        }
        String numberMarkInfo = null;
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
            if (callCursor.getColumnIndex("is_cloud_mark") >= 0) {
                numberMarkInfo = NumberMarkUtil.getMarkLabel(getApplicationContext(), new NumberMarkInfo(number, callCursor.getString(MARK_CONTENT), callCursor.getString(MARK_TYPE), callCursor.getInt(MARK_COUNT), callCursor.getInt(IS_CLOUD_MARK) == 1));
            }
        }
        PhoneCallDetails lTempPhoneDetails = new PhoneCallDetails(number, formattedNumber, countryIso, geocode, new int[]{callType}, date, duration, nameText, numberType, numberLabel, uri, photoUri, lSubID, isVoiceMailNum, ringTimes, numberMarkInfo, null);
        lTempPhoneDetails.setIsPrivate(lIsPrivate);
        lTempPhoneDetails.setPresentation(numPresentation);
        if (loadContactInfo) {
            lTempPhoneDetails.contactInfo = info;
        }
        lTempPhoneDetails.setCallsTypeFeatures(callFeature);
        return lTempPhoneDetails;
    }

    private void loadContactPhotos(long photoId, Uri photoUri, boolean isPrivate, Uri contactUri) {
        int lFlags;
        if (isPrivate) {
            lFlags = 4;
        } else {
            lFlags = 0;
        }
        long contactId = -1;
        if (contactUri != null) {
            contactId = Long.parseLong(contactUri.getLastPathSegment());
        }
        if (photoId != 0 || photoUri == null) {
            this.mContactPhotoManager.loadThumbnail(this.mContactBackgroundView, photoId, false, new DefaultImageRequest(null, String.valueOf(contactId), true), contactId, lFlags);
            return;
        }
        this.mContactPhotoManager.loadPhoto(this.mContactBackgroundView, photoUri, this.mContactBackgroundView.getWidth(), false, lFlags, null);
    }

    private void disableCallAndSmsItem() {
        View view;
        if (2 == getResources().getConfiguration().orientation) {
            view = findViewById(R.id.call_and_sms_inner_container);
            if (view != null) {
                view.setVisibility(8);
            }
            view = findViewById(R.id.call_display_buttons);
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        view = findViewById(R.id.call_and_sms_container);
        if (view != null) {
            view.setVisibility(8);
        }
    }

    private void configureCallButton(ViewEntry entry) {
        View mainAction = findViewById(R.id.call_and_sms_main_action);
        mainAction.setVisibility(0);
        mainAction.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int i = 1;
                Uri numberCallUri = CallDetailActivity.this.mPhoneNumberHelper.getCallUri(CallDetailActivity.this.mNumber);
                if (CallDetailActivity.mIsDualSim) {
                    if (CallDetailActivity.this.mIsFirstSimEnabled && CallDetailActivity.this.mIsSecondSimEnabled) {
                        CommonUtilMethods.dialNumberFromcalllog(CallDetailActivity.this, numberCallUri, CallDetailActivity.this.getResources().getString(R.string.call_other), -1, false, true, CallDetailActivity.this.mNumber);
                        return;
                    }
                    CallDetailActivity callDetailActivity = CallDetailActivity.this;
                    if (!CallDetailActivity.this.mIsSecondSimEnabled) {
                        i = 0;
                    }
                    callDetailActivity.dialNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(i), numberCallUri);
                } else if (!CallDetailActivity.this.mDualSimBtnClick.booleanValue()) {
                    CallDetailActivity.this.dialNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(0), numberCallUri);
                    CallDetailActivity.this.mDualSimBtnClick = Boolean.valueOf(true);
                }
            }
        });
        ((TextView) mainAction.findViewById(R.id.call_and_sms_text)).setText(entry.text);
        TextView label = (TextView) mainAction.findViewById(R.id.call_and_sms_label);
        if (TextUtils.isEmpty(entry.label)) {
            label.setVisibility(8);
        } else {
            label.setText(entry.label);
            label.setVisibility(0);
        }
        registerForContextMenu(mainAction);
        View smsAction = findViewById(R.id.dualsim_sms_container);
        smsAction.setVisibility(0);
        smsAction.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    CallDetailActivity.this.startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("sms", CommonUtilMethods.deleteIPHead(CallDetailActivity.this.mNumber), null)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CallDetailActivity.this, R.string.quickcontact_missing_app_Toast, 0).show();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.call_details_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_remove_from_call_log).setVisible(true);
        MenuItem editNumberBeforeCallMenuItem = menu.findItem(R.id.menu_edit_number_before_call);
        MenuItem sendNumberMenuItem = menu.findItem(R.id.menu_send_number);
        MenuItem blacklistMenuItem = menu.findItem(R.id.contact_menu_add_to_blacklist);
        MenuItem trashMenuItem = menu.findItem(R.id.menu_trash);
        MenuItem ipCallMenuItem = menu.findItem(R.id.menu_ip_call);
        MenuItem numberMarkMenuItem = menu.findItem(R.id.menu_mark_as);
        if (ContactsUtils.isNumberDialable(this.mNumber, this.mPresentation)) {
            if (!(blacklistMenuItem == null || this.mNumber == null || TextUtils.isEmpty(this.mNumber))) {
                if (EmuiFeatureManager.isBlackListFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
                    int blacklistMenuString;
                    if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(IHarassmentInterceptionService.Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService")), this.mNumber)) {
                        blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
                    } else {
                        blacklistMenuString = R.string.contact_menu_add_to_blacklist;
                    }
                    blacklistMenuItem.setVisible(true);
                    blacklistMenuItem.setTitle(blacklistMenuString);
                } else {
                    blacklistMenuItem.setVisible(false);
                }
            }
            editNumberBeforeCallMenuItem.setVisible(this.mHasEditNumberBeforeCallOption);
            sendNumberMenuItem.setVisible(this.mHasEditNumberBeforeCallOption);
            trashMenuItem.setVisible(false);
            if (SimFactoryManager.isDualSim() || !DialpadFragment.getIsIpCallEnabled()) {
                ipCallMenuItem.setVisible(false);
            } else {
                ipCallMenuItem.setVisible(true);
            }
            if (!EmuiFeatureManager.isNumberMarkFeatureEnabled() || MultiUsersUtils.isCurrentUserGuest() || (!(numberMarkMenuItem == null || TextUtils.isEmpty(this.mName)) || PhoneNumberUtils.isVoiceMailNumber(this.mNumber))) {
                numberMarkMenuItem.setVisible(false);
            }
            return super.onPrepareOptionsMenu(menu);
        }
        editNumberBeforeCallMenuItem.setVisible(false);
        sendNumberMenuItem.setVisible(false);
        blacklistMenuItem.setVisible(false);
        trashMenuItem.setVisible(false);
        ipCallMenuItem.setVisible(false);
        numberMarkMenuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            case R.id.menu_remove_from_call_log:
                AlertDialogFragmet.show(getFragmentManager(), (int) R.string.str_delete_call_log_entry, getString(R.string.recentCalls_deleteFromRecentList_message), (int) R.string.recentCalls_deleteFromRecentList_message, true, new OnDialogOptionSelectListener() {
                    public void writeToParcel(Parcel dest, int flags) {
                    }

                    public int describeContents() {
                        return 0;
                    }

                    public void onDialogOptionSelected(int which, Context aContext) {
                        if (which == -1) {
                            CallDetailActivity.this.onMenuRemoveFromCallLog();
                        }
                    }
                }, 16843605, (int) R.string.menu_deleteContact, "dialog_from_menu");
                return true;
            case R.id.menu_edit_number_before_call:
                onMenuEditNumberBeforeCall(item);
                return true;
            case R.id.menu_send_number:
                onMenuSendNumber(item);
                return true;
            case R.id.menu_ip_call:
                onMenuIpCall(item);
                return true;
            case R.id.contact_menu_add_to_blacklist:
                IHarassmentInterceptionService mService = IHarassmentInterceptionService.Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
                if (item.getTitle().equals(getResources().getText(R.string.contact_menu_add_to_blacklist))) {
                    BlacklistCommonUtils.handleNumberBlockList(this, mService, this.mNumber, this.mName, 0, true);
                } else if (item.getTitle().equals(getResources().getText(R.string.contact_menu_remove_from_blacklist))) {
                    BlacklistCommonUtils.handleNumberBlockList(this, mService, this.mNumber, this.mName, 1, true);
                }
                invalidateOptionsMenu();
                return true;
            case R.id.menu_mark_as:
                try {
                    startActivityForResult(NumberMarkUtil.getIntentForMark(getApplicationContext(), this.mNumber), 100);
                } catch (ActivityNotFoundException e) {
                    HwLog.w("CallDetail", "Activity not found." + e);
                }
                return true;
            default:
                return true;
        }
    }

    public void onMenuSendNumber(MenuItem menuItem) {
        boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(this.mNumber, this.mPresentation);
        boolean isVoicemailNumber = this.mPhoneNumberHelper.isVoicemailNumber(this.mNumber);
        boolean isSipNumber = this.mPhoneNumberHelper.isSipNumber(this.mNumber);
        if (canPlaceCallsTo && !isVoicemailNumber && !isSipNumber) {
            try {
                Intent mainActionIntent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:"));
                mainActionIntent.putExtra("sms_body", this.mNameAndNumberSendByMMS);
                startActivity(mainActionIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.quickcontact_missing_app_Toast, 0).show();
                ex.printStackTrace();
            }
        }
    }

    public void onMenuRemoveFromCallLog() {
        final StringBuilder callIds = new StringBuilder();
        if (this.mCallLogIds != null) {
            for (long callUri : this.mCallLogIds) {
                if (callIds.length() != 0) {
                    callIds.append(",");
                }
                callIds.append(callUri);
            }
        }
        this.mAsyncTaskExecutor.submit(Tasks.REMOVE_FROM_CALL_LOG_AND_FINISH, new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... params) {
                CallDetailActivity.this.getContentResolver().delete(QueryUtil.getCallsContentUri(), "_id IN (" + callIds + ")", null);
                return null;
            }

            public void onPostExecute(Void result) {
                CallDetailActivity.this.finish();
            }
        }, new Void[0]);
    }

    public void onMenuEditNumberBeforeCall(MenuItem menuItem) {
        startActivity(new Intent("android.intent.action.DIAL", CallUtil.getCallUri(this.mNumber)));
    }

    public void onMenuIpCall(MenuItem menuItem) {
        String tmpPhoneNumber;
        String ipPrefix = null;
        try {
            if (this.mPhoneService != null) {
                ipPrefix = this.mPhoneService.getIpPrefix(0);
            }
        } catch (RemoteException e) {
            HwLog.e("CallDetail", "call remote Phone interface(getIpPrefix) error");
        }
        if (ipPrefix != null) {
            tmpPhoneNumber = ipPrefix + CommonUtilMethods.processNumber(this.mNumber);
        } else {
            tmpPhoneNumber = this.mNumber;
        }
        CommonUtilMethods.dialNumber((Context) this, Uri.fromParts("tel", tmpPhoneNumber, null), "", false, false);
    }

    private void configureActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onPause() {
        SimFactoryManager.listenPhoneState(this.mPhoneStateListener, 0);
        SimFactoryManager.removeSimStateListener(this);
        super.onPause();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) menuInfo;
        int viewId = v.getId();
        menu.setHeaderIcon(R.mipmap.ic_launcher_phone);
        boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(this.mNumber, this.mPresentation);
        if (this.mPhoneNumberHelper.isVoicemailNumber(this.mNumber)) {
            menu.setHeaderTitle(R.string.voicemail);
        } else if (canPlaceCallsTo) {
            menu.setHeaderTitle(this.mNameOrNumber);
        } else if (this.mPhoneNumberHelper.getDisplayNumber(this.mNumber, this.mPresentation, null, "") != null) {
            menu.setHeaderTitle(this.mPhoneNumberHelper.getDisplayNumber(this.mNumber, this.mPresentation, null, this.mPostDialDigits).toString());
        } else {
            menu.setHeaderTitle(this.mNameOrNumber);
        }
        if (viewId == R.id.call_and_sms_main_action) {
            menu.add(getString(R.string.copy_text)).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    CallDetailActivity.this.copyToClipboard();
                    return true;
                }
            });
        } else {
            menu.add(R.string.str_delete_call_log_entry).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (contextMenuInfo.position == 0) {
                        return false;
                    }
                    FragmentManager fragmentManager = CallDetailActivity.this.getFragmentManager();
                    String string = CallDetailActivity.this.getString(R.string.recentCalls_deleteFromRecentList_message);
                    final AdapterContextMenuInfo adapterContextMenuInfo = contextMenuInfo;
                    AlertDialogFragmet.show(fragmentManager, (int) R.string.str_delete_call_log_entry, string, (int) R.string.recentCalls_deleteFromRecentList_message, true, new OnDialogOptionSelectListener() {
                        public void writeToParcel(Parcel dest, int flags) {
                        }

                        public int describeContents() {
                            return 0;
                        }

                        public void onDialogOptionSelected(int which, Context aContext) {
                            if (which == -1) {
                                CallDetailActivity.this.deleteSingleCalllog(adapterContextMenuInfo.position - 1);
                            }
                        }
                    }, 16843605, (int) R.string.menu_deleteContact, "dialog_from_context_menu");
                    return true;
                }
            });
        }
    }

    private void copyToClipboard() {
        CharSequence textToCopy = this.mPhoneNumberToCopy;
        if (!TextUtils.isEmpty(textToCopy)) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(new ClipData(this.mPhoneNumberLabelToCopy, new String[]{"vnd.android.cursor.item/phone_v2"}, new Item(textToCopy)));
        }
    }

    private String[] getCallLogProjection() {
        return CALL_LOG_PROJECTION;
    }

    public void displayNumberMark(String markInfo) {
        if (!EmuiFeatureManager.isNumberMarkFeatureEnabled() || !MultiUsersUtils.isCurrentUserOwner() || !TextUtils.isEmpty(this.mName)) {
            this.mNumberMakInfoView.setVisibility(8);
            this.mNumberMakInfoView.setText("");
        } else if (TextUtils.isEmpty(markInfo)) {
            this.mNumberMakInfoView.setVisibility(8);
            this.mNumberMakInfoView.setText("");
        } else {
            this.mNumberMakInfoView.setVisibility(0);
            this.mNumberMakInfoView.setText(markInfo);
        }
    }

    @Deprecated
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case 257:
                String[] nameArray = args.getStringArray("item_name");
                final String[] pathArray = args.getStringArray("item_path");
                Builder builder = new Builder(this);
                builder.setTitle(getString(R.string.select_record)).setItems(nameArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CallDetailActivity.this.startRecordPlaybackSafely(CallDetailActivity.this, pathArray[which]);
                    }
                });
                Dialog dialog = builder.create();
                dialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        CallDetailActivity.this.removeDialog(257);
                    }
                });
                return dialog;
            default:
                return super.onCreateDialog(id, args);
        }
    }

    private void startRecordPlaybackSafely(Context context, String absolutePath) {
        Intent localIntent = new Intent();
        localIntent.addFlags(268468224);
        localIntent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.RecordListActivity");
        File file = new File(absolutePath);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", file.getAbsolutePath());
        bundle.putString("fileName", file.getName());
        bundle.putString("PlayUri", Uri.fromFile(file).toString());
        bundle.putBoolean("isCallfolder", true);
        localIntent.putExtras(bundle);
        try {
            context.startActivity(localIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeSystemDialogs() {
        sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    public void callButton() {
        this.mDualSimSMS.setVisibility(0);
        changeViewContainerVisibility(R.id.dualsim_sms_container, 0);
        this.mDualSimSMS.setClickable(true);
        this.mSim1Call.setVisibility(0);
        this.mSimIPCall.setVisibility(0);
        changeViewContainerVisibility(R.id.sim_call_ip_container, 0);
        if (SimFactoryManager.isDualSim()) {
            int simPresence;
            this.mIsFirstSimEnabled = SimFactoryManager.isSimEnabled(0);
            this.mIsSecondSimEnabled = SimFactoryManager.isSimEnabled(1);
            boolean isSecondSubIdle = true;
            boolean isFirstSubIdle = true;
            if (!CommonConstants.sRo_config_hw_dsda) {
                isSecondSubIdle = !SimFactoryManager.phoneIsOffhook(1);
                isFirstSubIdle = !SimFactoryManager.phoneIsOffhook(0);
            }
            if (!this.mIsFirstSimEnabled) {
                isSecondSubIdle = false;
            }
            this.mIsFirstSimEnabled = isSecondSubIdle;
            if (!this.mIsSecondSimEnabled) {
                isFirstSubIdle = false;
            }
            this.mIsSecondSimEnabled = isFirstSubIdle;
            if (SimFactoryManager.isSIM1CardPresent()) {
                simPresence = 1;
            } else {
                simPresence = 0;
            }
            if (SimFactoryManager.isSIM2CardPresent()) {
                simPresence |= 2;
            }
            this.mSim1Call.setEnabled(true);
            this.mSim2Call.setVisibility(8);
            this.mSim2Call.setEnabled(false);
            if (!(this.mIsFirstSimEnabled || this.mIsSecondSimEnabled)) {
                this.mSim1Call.setEnabled(true);
                this.mSim2Call.setEnabled(true);
            }
            switch (simPresence) {
                case 1:
                    this.mSim1Call.setVisibility(0);
                    this.mSim2Call.setVisibility(8);
                    break;
                case 2:
                    this.mSim1Call.setVisibility(0);
                    this.mSim2Call.setVisibility(8);
                    break;
                case 3:
                    this.mSim1Call.setVisibility(0);
                    this.mSim2Call.setVisibility(8);
                    if (!(this.mIsFirstSimEnabled || this.mIsSecondSimEnabled)) {
                        this.mSim2Call.setVisibility(8);
                        break;
                    }
                default:
                    this.mSim2Call.setVisibility(8);
                    break;
            }
            this.mSim1Call.setClickable(true);
            this.mSim2Call.setClickable(true);
        } else {
            this.mSim2Call.setVisibility(8);
        }
        if (DialpadFragment.getIsIpCallEnabled()) {
            this.mSimIPCall.setVisibility(0);
            changeViewContainerVisibility(R.id.sim_call_ip_container, 0);
            return;
        }
        this.mSimIPCall.setVisibility(8);
        changeViewContainerVisibility(R.id.sim_call_ip_container, 8);
    }

    public void simStateChanged(int aSubScription) {
        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (CallDetailActivity.this.mUpdateContactDetailsTask != null && CallDetailActivity.this.mUpdateContactDetailsTask.getStatus() == Status.FINISHED) {
                        CallDetailActivity.this.callButton();
                    }
                }
            });
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(this.mCallLogChangeObserver);
    }

    private long[] getCallLogIdsForNumber() {
        long[] jArr = null;
        String number = DatabaseUtils.sqlEscapeString(this.mNumber);
        number = number.substring(1, number.length() - 1);
        Cursor cursor = getContentResolver().query(QueryUtil.getCallsContentUri(), new String[]{"_id", "number", "countryiso", "name"}, "PHONE_NUMBERS_EQUAL(number, ?)", new String[]{number}, "date DESC");
        if (cursor != null) {
            try {
                int size = cursor.getCount();
                if (size > 0) {
                    ArrayList<Long> lcalllogids = new ArrayList(size);
                    while (cursor.moveToNext()) {
                        if (!CommonConstants.IS_HW_CUSTOM_NUMBER_MATCHING_ENABLED) {
                            lcalllogids.add(Long.valueOf(cursor.getLong(0)));
                        } else if (CommonUtilMethods.equalByNameOrNumber(this.mName, this.mNumber, cursor.getString(3), cursor.getString(1)) && CommonUtilMethods.compareNumsHw(this.mNumber, this.mCallNumberCountryIso, cursor.getString(1), cursor.getString(2))) {
                            lcalllogids.add(Long.valueOf(cursor.getLong(0)));
                        }
                    }
                    jArr = new long[lcalllogids.size()];
                    int index = 0;
                    for (Long id : lcalllogids) {
                        int index2 = index + 1;
                        jArr[index] = id.longValue();
                        index = index2;
                    }
                    this.mCallLogIds = jArr;
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (jArr == null) {
            return getCallLogEntryUris();
        }
        return jArr;
    }

    private long[] getCallLogIdsForUnknowNumber() {
        long[] jArr = null;
        Cursor cursor = getContentResolver().query(QueryUtil.getCallsContentUri(), new String[]{"_id", "presentation"}, "presentation=?", new String[]{String.valueOf(this.mPresentation)}, "date DESC");
        if (cursor != null) {
            try {
                int size = cursor.getCount();
                if (size > 0) {
                    ArrayList<Long> callLogIdsList = new ArrayList(size);
                    while (cursor.moveToNext()) {
                        callLogIdsList.add(Long.valueOf(cursor.getLong(0)));
                    }
                    jArr = new long[callLogIdsList.size()];
                    int index = 0;
                    for (Long id : callLogIdsList) {
                        int index2 = index + 1;
                        jArr[index] = id.longValue();
                        index = index2;
                    }
                    this.mCallLogIds = jArr;
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (jArr == null) {
            return getCallLogEntryUris();
        }
        return jArr;
    }

    protected void onStop() {
        super.onStop();
        this.mIsPaused = true;
        if (this.mOptionsMenu != null) {
            this.mOptionsMenu.close();
        }
        if (this.mIsPhoneConnectionBind) {
            try {
                unbindService(this.mPhoneConnection);
            } catch (IllegalArgumentException e) {
                HwLog.e("CallDetail", "IllegalArgumentException happens when unbindService.");
            }
            this.mIsPhoneConnectionBind = false;
        }
    }

    protected void onSaveInstanceState(Bundle aOutState) {
        super.onSaveInstanceState(aOutState);
        if (this.mIsFromDialer) {
            aOutState.putLongArray("EXTRA_CALL_LOG_IDS", getCallLogEntryUris());
        } else {
            aOutState.putLongArray("EXTRA_CALL_LOG_IDS", null);
        }
        aOutState.putString("EXTRA_FROM_NOTIFICATION", this.mNumber);
        aOutState.putString("EXTRA_CALL_LOG_COUNTRY_ISO", this.mCallNumberCountryIso);
        aOutState.putInt("EXTRA_CALL_LOG_PRESENTATION", this.mPresentation);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        if (this.mOptionsMenu == null || keyCode != 82 || duration >= ((long) ViewConfiguration.getLongPressTimeout())) {
            return super.onKeyUp(keyCode, event);
        }
        this.mOptionsMenu.performIdentifierAction(R.id.call_detail_overflow_menu, 0);
        return true;
    }

    private void changeViewContainerVisibility(int aViewId, int aVisibility) {
        View view = findViewById(aViewId);
        if (view != null) {
            view.setVisibility(aVisibility);
        }
    }

    private void deleteSingleCalllog(final int position) {
        final long[] ids = getCallLogEntryUris();
        int lposition = position;
        new AsyncTask<Void, Void, Void>() {
            private int isDeleted = 0;
            private boolean toBeFinished;

            public Void doInBackground(Void... params) {
                if (ids.length == 1) {
                    this.toBeFinished = true;
                }
                long id = ids[position];
                CallDetailActivity.this.deletedId = id;
                this.isDeleted = CallDetailActivity.this.getContentResolver().delete(QueryUtil.getCallsContentUri(), "_id ='" + id + "'", null);
                return null;
            }

            protected void onPostExecute(Void result) {
                if (this.toBeFinished) {
                    CallDetailActivity.this.finish();
                } else if (this.isDeleted > 0) {
                    long[] newIds = new long[(ids.length - 1)];
                    int index = 0;
                    int deletedIndex = -1;
                    for (int i = 0; i < ids.length; i++) {
                        if (ids[i] != CallDetailActivity.this.deletedId) {
                            newIds[index] = ids[i];
                            index++;
                        } else {
                            deletedIndex = i;
                        }
                    }
                    CallDetailActivity.this.getIntent().putExtra("EXTRA_CALL_LOG_IDS", newIds);
                    if (CallDetailActivity.this.mAdapter != null) {
                        CallDetailActivity.this.mAdapter.removeSingleCallDetail(deletedIndex);
                    }
                }
            }
        }.execute(new Void[0]);
    }

    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aResultCode == -1) {
            switch (aRequestCode) {
                case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                    if (aData.getIntExtra("MARK_TYPE", -2) != -1) {
                        String phoneMark = aData.getStringExtra("MARK_SUMMERY");
                        String margeMark = String.format(getString(R.string.marked), new Object[]{phoneMark});
                        this.mNumberMakInfoView.setVisibility(0);
                        this.mNumberMakInfoView.setText(margeMark);
                        break;
                    }
                    this.mNumberMakInfoView.setText("");
                    this.mNumberMakInfoView.setVisibility(8);
                    break;
            }
        }
    }
}
