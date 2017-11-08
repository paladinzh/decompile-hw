package com.android.contacts.hap.rcs.dialer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.GeoUtil;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.editor.Editor;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.rcs.RcsBitmapUtils;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.RoundOutlineProvider;
import com.android.contacts.hap.rcs.activities.RcsPreCallPreviewActivity;
import com.android.contacts.hap.rcs.dialer.RcsPhotoSelectionHandler.PhotoActionListener;
import com.android.contacts.model.ContactQuery;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ContactLoaderUtils;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.DialerHighlighter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RcsPreCallFragment extends Fragment implements OnCheckedChangeListener, OnScrollListener {
    private static final String[] PROJECTION = new String[]{"is_primary", "subject", "location_switch_state", "picture", "time"};
    private static final String[] PROJECTION_LOG = new String[]{"is_primary", "subject", "picture", "longitude", "latitude"};
    private String lDefaultCountryIso;
    private View mActionBarGradientView;
    private TextView mAddPictureHeadTextView;
    private LinearLayout mAddPictureLayout;
    private String mAddressString;
    private ImageView mBackButton;
    private final CancelListener mCancelListener = new CancelListener();
    private String mCompanyString;
    private ContactInfo mContactInfo;
    private ContactInfoHelper mContactInfoHelper;
    private Context mContext;
    private Uri mCurrentMapUri;
    private Uri mCurrentPictureUri;
    private ImageView mDeletePictureButton;
    private ImageView mDialerButton;
    private DialogListener mDialogListener = new DialogListener();
    private String mFormatNumber;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (RcsPreCallFragment.this.mIsQueryFromContacts) {
                        AsyncTaskExecutors.createThreadPoolExecutor().submit("GET_CONTACT_PHOTO", new RcsLoadPhotoTask(), new Context[0]);
                        RcsPreCallFragment.this.initCacheData(RcsPreCallFragment.this.mIsSavedInstanceState);
                    }
                    RcsPreCallFragment.this.showNameCompanyJob();
                    return;
                case 1:
                    AsyncTaskExecutors.createThreadPoolExecutor().submit("GET_CONTACT_PHOTO", new RcsLoadPhotoTask(), new Context[0]);
                    RcsPreCallFragment.this.initShowNameComJob();
                    RcsPreCallFragment.this.initCacheData(RcsPreCallFragment.this.mIsSavedInstanceState);
                    return;
                case 2:
                    RcsPreCallFragment.this.initScrollViewContent();
                    return;
                case 3:
                    RcsPreCallFragment.this.initPictureAndDeleteView(false);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasBigPhoto;
    private final BroadcastReceiver mHomeKeyEventBroadCastReceiver = new HomeKeyEventBroadCastReceiver();
    private String mId;
    private boolean mIsDialing = false;
    private boolean mIsHomeKeyBroadcastRegistered = false;
    private boolean mIsLoadMapTimeout;
    private boolean mIsMapSwtichChecked;
    private int mIsPrimary = -1;
    private boolean mIsPrioritySwitchChecked;
    private boolean mIsQueryFromContacts;
    private boolean mIsSavedInstanceState;
    private String mJobString;
    private double mLatitude = 360.0d;
    private double mLogitude = 360.0d;
    private LinearLayout mMapCheckContainer;
    private RelativeLayout mMapDiaplayContainer;
    private RelativeLayout mMapDiaplayView;
    private TextView mMapHeadTextView;
    private Switch mMapSwitch;
    private TextView mMapTextViw;
    private LinearLayout mMapTimeOutLayout;
    private TextView mMapTimeOutText;
    private String mNumber;
    private Bitmap mPhotoBitmap;
    private PhotoHandler mPhotoHandler;
    private String mPhotoUri;
    private Bitmap mPictureBitmap;
    private View mPictureProgressBar;
    private long mPictureSendTime = Long.MAX_VALUE;
    private ImageView mPictureView;
    private RelativeLayout mPicturelayout;
    private View mPostionProgressBar;
    private TextView mPreCallCompany;
    private TextView mPreCallJob;
    private ImageView mPreCallLittlePhoto;
    private TextView mPreCallName;
    private ImageView mPreCallPhoto;
    private TextView mPreCallPreviewButton;
    private LinearLayout mPriorityLayout;
    private Switch mPrioritySwitch;
    private TextView mPriorityText;
    private RcsPreCallFragmentHelper mRcsPreCallFragmentHelper;
    private Uri mRequestUri;
    private View mRootView;
    private TextView mStatusActionView;
    private RelativeLayout mStatusLayout;
    private TextView mStatusMessageView;
    private Dialog mSubjectDialog;
    private ImageView mSubjectDialogButton;
    private TextView mSubjectInputCountView;
    private String mSubjectString;
    private EditText mSubjectText;
    private View mTitleGradientView;
    private String mdisplayName;
    private int mdisplayNameSource;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }

        public void onCancel(DialogInterface dialog) {
            if (RcsPreCallFragment.this.mSubjectDialog != null && RcsPreCallFragment.this.mSubjectDialog.isShowing()) {
                RcsPreCallFragment.this.mSubjectDialog.dismiss();
            }
        }
    }

    private class DialogListener implements OnClickListener {
        private DialogListener() {
        }

        public void onClick(DialogInterface arg0, int arg1) {
            arg0.dismiss();
            Intent it = new Intent();
            it.setPackage("com.android.settings");
            it.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
            RcsPreCallFragment.this.startActivity(it);
        }
    }

    private class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
        private HomeKeyEventBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra("reason");
            if (reason != null && reason.equals("homekey")) {
                RcsPreCallFragment.this.insertOrUpdateCacheStatusAccordingNumber();
            }
        }
    }

    private final class PhotoHandler extends RcsPhotoSelectionHandler {
        private final PhotoEditorListener mPhotoEditorListener = new PhotoEditorListener();

        private final class PhotoEditorListener extends PhotoActionListener implements EditorListener {
            private PhotoEditorListener() {
                super();
            }

            public void onRequest(int request) {
                if (request == 1) {
                    PhotoHandler.this.onClick(RcsPreCallFragment.this.mAddPictureLayout);
                }
            }

            public void onPhotoSelected(Uri uri) throws FileNotFoundException {
                final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(PhotoHandler.this.mContext, uri);
                if (bitmap != null) {
                    if (RcsPreCallFragment.this.mCurrentPictureUri != null) {
                        PhotoHandler.this.mContext.getContentResolver().delete(RcsPreCallFragment.this.mCurrentPictureUri, null, null);
                    }
                    RcsPreCallFragment.this.mCurrentPictureUri = null;
                    RcsPreCallFragment.this.initPictureAndDeleteView(true);
                    HwLog.i("RcsPreCallFragment", " onPhotoSelected");
                    ContactsThreadPool.getInstance().execute(new Runnable() {
                        public void run() {
                            RcsPreCallFragment.this.processAndSendPicture(bitmap);
                        }
                    });
                }
            }

            public Uri getCurrentPhotoUri() {
                return RcsPreCallFragment.this.mCurrentPictureUri;
            }

            public void onPhotoSelectionDismissed() {
            }

            public void onDeleteRequested(Editor editor) {
            }
        }

        public PhotoHandler(Context context, ImageView photoView, int photoMode) {
            super(context, photoView, photoMode);
        }

        public PhotoActionListener getListener() {
            return this.mPhotoEditorListener;
        }

        public void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            HwLog.i("RcsPreCallFragment", " startPhotoActivity");
            RcsPreCallFragment.this.mCurrentPictureUri = photoUri;
            RcsPreCallFragment.this.startActivityForResult(intent, requestCode);
        }
    }

    public class RcsLoadPhotoTask extends AsyncTask<Context, Void, Bitmap> {
        protected Bitmap doInBackground(Context... params) {
            HwLog.i("RcsPreCallFragment", " RcsLoadPhotoTask doInBackground ");
            if (RcsPreCallFragment.this.mContactInfo == null || RcsPreCallFragment.this.mContactInfo.photoUri == null) {
                HwLog.i("RcsPreCallFragment", " RcsLoadPhotoTask ContactInfo or photo uri is null");
                RcsPreCallFragment.this.mHasBigPhoto = false;
                return null;
            }
            Bitmap mBitmap = RcsPreCallFragment.this.getBigPhoto();
            if (mBitmap != null) {
                RcsPreCallFragment.this.mHasBigPhoto = true;
                return mBitmap;
            }
            RcsPreCallFragment.this.mHasBigPhoto = false;
            return RcsPreCallFragment.this.getLittlePhoto();
        }

        protected void onPostExecute(Bitmap mBitmap) {
            super.onPostExecute(mBitmap);
            RcsPreCallFragment.this.mPhotoBitmap = mBitmap;
            HwLog.i("RcsPreCallFragment", " RcsLoadPhotoTask onPostExecute");
            RcsPreCallFragment.this.initGradientView(RcsPreCallFragment.this.mHasBigPhoto);
            if (RcsPreCallFragment.this.mHasBigPhoto) {
                RcsPreCallFragment.this.mPreCallPhoto.setImageDrawable(new BitmapDrawable(RcsPreCallFragment.this.mContext.getResources(), RcsPreCallFragment.this.mPhotoBitmap));
                RcsPreCallFragment.this.mPreCallLittlePhoto.setVisibility(8);
            } else if (RcsPreCallFragment.this.mPhotoBitmap != null) {
                RcsPreCallFragment.this.mPreCallLittlePhoto.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(RcsPreCallFragment.this.mContext.getResources(), RcsPreCallFragment.this.mPhotoBitmap)));
                RcsPreCallFragment.this.mPreCallPhoto.setImageDrawable(new ColorDrawable(RcsPreCallFragment.this.getResources().getColor(R.color.profile_simple_card_default_bg_color)));
            } else {
                RcsPreCallFragment.this.mPreCallLittlePhoto.setVisibility(8);
                RcsPreCallFragment.this.mPreCallPhoto.setImageDrawable(new ColorDrawable(ContactPhotoManager.pickColor(String.valueOf(RcsPreCallFragment.this.getContactId(RcsPreCallFragment.this.mContext, RcsPreCallFragment.this.mContactInfo)), RcsPreCallFragment.this.mContext.getResources())));
            }
        }
    }

    private class TimeOutListener implements View.OnClickListener {
        private TimeOutListener() {
        }

        public void onClick(View arg0) {
            RcsPreCallFragment.this.reStartLoadMap();
        }
    }

    private void loadCacheStatusAccordingNumber() {
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
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r4 = 1;
        r3 = 0;
        r5 = "RcsPreCallFragment";
        r6 = "loadCacheStatusAccordingNumber";
        com.android.contacts.util.HwLog.i(r5, r6);
        r0 = r10.queryPreCallCacheDB();
        if (r0 == 0) goto L_0x0092;
    L_0x0011:
        r5 = r0.moveToFirst();	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r5 == 0) goto L_0x0092;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0017:
        r5 = "RcsPreCallFragment";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r6 = "loadCacheStatusAccordingNumber cursor load";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        com.android.contacts.util.HwLog.i(r5, r6);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = "time";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r8 = r0.getLong(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r6 = r6 - r8;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r8 = 3600000; // 0x36ee80 float:5.044674E-39 double:1.7786363E-317;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r5 >= 0) goto L_0x0092;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0037:
        r5 = "is_primary";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getInt(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r5 != 0) goto L_0x0098;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0044:
        r5 = r3;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0045:
        r10.mIsPrioritySwitchChecked = r5;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = "subject";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getString(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r10.mSubjectString = r5;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = "location_switch_state";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r5 = r0.getInt(r5);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r5 != 0) goto L_0x009a;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0061:
        r10.mIsMapSwtichChecked = r3;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = "picture";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = r0.getColumnIndex(r3);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = r0.getString(r3);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = android.net.Uri.parse(r3);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r10.mCurrentPictureUri = r3;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = r10.mCurrentPictureUri;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r3 == 0) goto L_0x0092;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0078:
        r3 = r10.mContext;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r4 = r10.mCurrentPictureUri;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r4 = r4.getLastPathSegment();	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r2 = r10.pathForPictureOrMap(r3, r4);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = r10.mRcsPreCallFragmentHelper;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r3 = r10.mFormatNumber;	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        com.android.contacts.hap.rcs.dialer.RcsPreCallFragmentHelper.preCallSendImage(r2, r3);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r4 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r10.setPictureUrlAndTime(r4);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
    L_0x0092:
        if (r0 == 0) goto L_0x0097;
    L_0x0094:
        r0.close();
    L_0x0097:
        return;
    L_0x0098:
        r5 = r4;
        goto L_0x0045;
    L_0x009a:
        r3 = r4;
        goto L_0x0061;
    L_0x009c:
        r1 = move-exception;
        r3 = "RcsPreCallFragment";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        r4 = "load rcs cache failed!";	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        com.android.contacts.util.HwLog.e(r3, r4);	 Catch:{ Exception -> 0x009c, all -> 0x00ac }
        if (r0 == 0) goto L_0x0097;
    L_0x00a8:
        r0.close();
        goto L_0x0097;
    L_0x00ac:
        r3 = move-exception;
        if (r0 == 0) goto L_0x00b2;
    L_0x00af:
        r0.close();
    L_0x00b2:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.rcs.dialer.RcsPreCallFragment.loadCacheStatusAccordingNumber():void");
    }

    private void loadCallLogData() {
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
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r15 = this;
        r1 = r15.mContext;
        r0 = r1.getContentResolver();
        r6 = 0;
        r1 = com.android.contacts.compatibility.QueryUtil.getCallsContentUri();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = PROJECTION_LOG;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r3 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r3.<init>();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r4 = "_id = ";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r4 = r15.mId;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r3 = r3.toString();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r5 = "date DESC";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r4 = 0;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r6 == 0) goto L_0x008c;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x002d:
        r1 = r6.moveToFirst();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r1 == 0) goto L_0x008c;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0033:
        r1 = "is_primary";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getColumnIndex(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r14 = r6.getInt(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = 1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r14 != r1) goto L_0x0092;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0041:
        r1 = 1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0042:
        r15.mIsPrioritySwitchChecked = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = "subject";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getColumnIndex(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getString(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r15.mSubjectString = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = "picture";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getColumnIndex(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r12 = r6.getString(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r12 == 0) goto L_0x0064;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x005e:
        r1 = r12.isEmpty();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r1 == 0) goto L_0x0094;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0064:
        r1 = 0;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r15.mCurrentPictureUri = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0067:
        r1 = "longitude";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getColumnIndex(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r10 = r6.getDouble(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = "latitude";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r6.getColumnIndex(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r8 = r6.getDouble(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = 0;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = (r10 > r2 ? 1 : (r10 == r2 ? 0 : -1));	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r1 != 0) goto L_0x00d4;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0083:
        r2 = 0;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1));	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r1 != 0) goto L_0x00d4;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x0089:
        r1 = 0;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r15.mIsMapSwtichChecked = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x008c:
        if (r6 == 0) goto L_0x0091;
    L_0x008e:
        r6.close();
    L_0x0091:
        return;
    L_0x0092:
        r1 = 0;
        goto L_0x0042;
    L_0x0094:
        r1 = new java.io.File;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1.<init>(r12);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r1.exists();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r1 == 0) goto L_0x0064;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
    L_0x009f:
        r1 = r15.getPictureUri(r12);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r15.backupPicture(r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r15.mCurrentPictureUri = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r15.mContext;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = r15.mCurrentPictureUri;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = r2.getLastPathSegment();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r13 = r15.pathForPictureOrMap(r1, r2);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r15.mRcsPreCallFragmentHelper;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r1 = r15.mFormatNumber;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        com.android.contacts.hap.rcs.dialer.RcsPreCallFragmentHelper.preCallSendImage(r13, r1);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r15.setPictureUrlAndTime(r2);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        goto L_0x0067;
    L_0x00c4:
        r7 = move-exception;
        r1 = "RcsPreCallFragment";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        r2 = "loadCallLogData read cursor failed!";	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        com.android.contacts.util.HwLog.e(r1, r2);	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        if (r6 == 0) goto L_0x0091;
    L_0x00d0:
        r6.close();
        goto L_0x0091;
    L_0x00d4:
        r1 = 1;
        r15.mIsMapSwtichChecked = r1;	 Catch:{ IllegalArgumentException -> 0x00c4, all -> 0x00d8 }
        goto L_0x008c;
    L_0x00d8:
        r1 = move-exception;
        if (r6 == 0) goto L_0x00de;
    L_0x00db:
        r6.close();
    L_0x00de:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.rcs.dialer.RcsPreCallFragment.loadCallLogData():void");
    }

    public void onCreate(Bundle savedInstanceState) {
        Uri uri;
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        Intent intent = getActivity().getIntent();
        if (savedInstanceState != null) {
            this.mSubjectString = savedInstanceState.getString("currentSubjectString");
            this.mCurrentPictureUri = (Uri) savedInstanceState.getParcelable("currentPictureUri");
            this.mIsPrioritySwitchChecked = savedInstanceState.getBoolean("priority");
            this.mIsSavedInstanceState = true;
            this.mAddressString = savedInstanceState.getString("currentAddressString");
            this.mIsLoadMapTimeout = savedInstanceState.getBoolean("isLoadmapTimeout");
            this.mLatitude = savedInstanceState.getDouble("currentLatitude");
            this.mLogitude = savedInstanceState.getDouble("currentLongitude");
        } else {
            this.mIsPrimary = intent.getIntExtra("is_primary", -1);
            this.mId = intent.getStringExtra("_id");
        }
        this.mNumber = intent.getStringExtra("pre_call_number");
        this.mFormatNumber = DialerHighlighter.cleanNumber(this.mNumber, false);
        if (TextUtils.isEmpty(intent.getStringExtra("lookuri"))) {
            uri = null;
        } else {
            uri = Uri.parse(intent.getStringExtra("lookuri"));
        }
        this.mRequestUri = uri;
        updateStatusBarColor();
        this.lDefaultCountryIso = GeoUtil.getCurrentCountryIso(this.mContext);
        this.mRcsPreCallFragmentHelper = new RcsPreCallFragmentHelper(this);
        this.mRcsPreCallFragmentHelper.handleCustomizationsOnCreate();
        if (savedInstanceState == null) {
            this.mRcsPreCallFragmentHelper.preCallCreate(this.mNumber);
        }
        this.mRcsPreCallFragmentHelper.registerRcsCallBack();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(getActivity().isInMultiWindowMode() ? R.layout.pre_call_fragment_multiwindow : R.layout.pre_call_fragment, null);
        this.mStatusLayout = (RelativeLayout) this.mRootView.findViewById(R.id.precall_status_layout);
        this.mStatusMessageView = (TextView) this.mStatusLayout.findViewById(R.id.precall_status_message);
        this.mStatusActionView = (TextView) this.mStatusLayout.findViewById(R.id.precall_status_action);
        this.mRcsPreCallFragmentHelper.setRcsStatusView(this.mStatusLayout, this.mStatusMessageView, this.mStatusActionView);
        this.mActionBarGradientView = this.mRootView.findViewById(R.id.action_bar_gradient);
        this.mTitleGradientView = this.mRootView.findViewById(R.id.title_gradient);
        this.mBackButton = (ImageView) this.mRootView.findViewById(R.id.pre_call_back_button);
        if (CommonUtilMethods.isLayoutRTL()) {
            this.mBackButton.setRotationY(180.0f);
        }
        this.mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (RcsPreCallFragment.this.isAdded() && RcsPreCallFragment.this.getActivity() != null) {
                    RcsPreCallFragment.this.insertOrUpdateCacheStatusAccordingNumber();
                    RcsPreCallFragment.this.getActivity().finish();
                }
            }
        });
        this.mPreCallPhoto = (ImageView) this.mRootView.findViewById(R.id.pre_call_photo);
        this.mPreCallLittlePhoto = (ImageView) this.mRootView.findViewById(R.id.rcs_little_photo);
        this.mPreCallName = (TextView) this.mRootView.findViewById(R.id.pre_call_name);
        this.mPreCallCompany = (TextView) this.mRootView.findViewById(R.id.pre_call_company);
        this.mPreCallJob = (TextView) this.mRootView.findViewById(R.id.pre_call_job);
        this.mPrioritySwitch = (Switch) this.mRootView.findViewById(R.id.pre_call_priority_switch);
        this.mPrioritySwitch.setOnCheckedChangeListener(this);
        this.mPriorityText = (TextView) this.mRootView.findViewById(R.id.pre_call_priority_text);
        this.mPriorityLayout = (LinearLayout) this.mRootView.findViewById(R.id.pre_call_priority_layout);
        this.mPriorityLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                RcsPreCallFragment.this.mPrioritySwitch.setChecked(!RcsPreCallFragment.this.mIsPrioritySwitchChecked);
            }
        });
        this.mSubjectText = (EditText) this.mRootView.findViewById(R.id.pre_call_subject_text);
        this.mSubjectInputCountView = (TextView) this.mRootView.findViewById(R.id.pre_call_subject_input_count);
        this.mSubjectDialogButton = (ImageView) this.mRootView.findViewById(R.id.pre_call_subject_image);
        this.mSubjectDialogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RcsPreCallFragment.this.initAndShowSubjectDialog();
            }
        });
        this.mSubjectText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (TextUtils.isEmpty(c)) {
                    RcsPreCallFragment.this.mSubjectInputCountView.setVisibility(8);
                    RcsPreCallFragment.this.mSubjectString = null;
                    return;
                }
                RcsPreCallFragment.this.mSubjectString = c.toString();
                int lLength = c.length();
                if (lLength < 54) {
                    RcsPreCallFragment.this.mSubjectInputCountView.setVisibility(8);
                    return;
                }
                boolean longinSuccess = RcsPreCallFragment.this.mRcsPreCallFragmentHelper.isRcsLoginSuccess();
                if (longinSuccess && lLength < 60) {
                    RcsPreCallFragment.this.mSubjectInputCountView.setTextColor(RcsPreCallFragment.this.mContext.getResources().getColor(R.color.rcs_input_count_noraml_color));
                } else if (longinSuccess && lLength == 60) {
                    RcsPreCallFragment.this.mSubjectInputCountView.setTextColor(RcsPreCallFragment.this.mContext.getResources().getColor(R.color.rcs_input_count_tip_color));
                } else if (longinSuccess || lLength >= 60) {
                    RcsPreCallFragment.this.mSubjectInputCountView.setTextColor(RcsPreCallFragment.this.mContext.getResources().getColor(R.color.red_icon_disable_color));
                } else {
                    RcsPreCallFragment.this.mSubjectInputCountView.setTextColor(RcsPreCallFragment.this.mContext.getResources().getColor(R.color.rcs_text_color_grey));
                }
                RcsPreCallFragment.this.mSubjectInputCountView.setVisibility(0);
                RcsPreCallFragment.this.mSubjectInputCountView.setText(RcsPreCallFragment.this.mContext.getResources().getString(R.string.rcs_pre_call_subject_count, new Object[]{Integer.valueOf(lLength), Integer.valueOf(60)}));
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mMapDiaplayView = (RelativeLayout) this.mRootView.findViewById(R.id.pre_call_location_map_layout);
        this.mMapDiaplayView.setOutlineProvider(new RoundOutlineProvider());
        this.mMapDiaplayView.setClipToOutline(true);
        this.mMapCheckContainer = (LinearLayout) this.mRootView.findViewById(R.id.pre_call_location_check_container);
        this.mMapDiaplayContainer = (RelativeLayout) this.mRootView.findViewById(R.id.pre_call_location_display_container);
        this.mMapHeadTextView = (TextView) this.mRootView.findViewById(R.id.pre_call_location_textview);
        this.mMapSwitch = (Switch) this.mRootView.findViewById(R.id.pre_call_location_switch);
        this.mMapSwitch.setOnCheckedChangeListener(this);
        this.mMapTextViw = (TextView) this.mRootView.findViewById(R.id.pre_call_location_text);
        this.mPostionProgressBar = this.mRootView.findViewById(R.id.progressPositon);
        this.mPictureProgressBar = this.mRootView.findViewById(R.id.progressPicture);
        this.mMapTimeOutLayout = (LinearLayout) this.mRootView.findViewById(R.id.time_out_layout);
        this.mMapTimeOutText = (TextView) this.mRootView.findViewById(R.id.time_out_text);
        this.mMapTimeOutText.setOnClickListener(new TimeOutListener());
        this.mRcsPreCallFragmentHelper.setMapdisplayView(this.mMapDiaplayView, savedInstanceState);
        this.mMapCheckContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                RcsPreCallFragment.this.mMapSwitch.setChecked(!RcsPreCallFragment.this.mIsMapSwtichChecked);
            }
        });
        this.mAddPictureLayout = (LinearLayout) this.mRootView.findViewById(R.id.pre_call_add_picture_container);
        this.mAddPictureHeadTextView = (TextView) this.mRootView.findViewById(R.id.pre_call_add_picture_text);
        this.mPicturelayout = (RelativeLayout) this.mRootView.findViewById(R.id.pre_call_picture_container);
        this.mPictureView = (ImageView) this.mRootView.findViewById(R.id.pre_call_picture_view);
        this.mPictureView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                StatisticalHelper.report(1214);
                String picturePath = null;
                if (RcsPreCallFragment.this.mCurrentPictureUri != null) {
                    picturePath = RcsPreCallFragment.this.pathForPictureOrMap(RcsPreCallFragment.this.mContext, RcsPreCallFragment.this.mCurrentPictureUri.getLastPathSegment());
                }
                RcsContactsUtils.startPictureView(RcsPreCallFragment.this.mContext, RcsPreCallFragment.this.getActivity(), picturePath);
            }
        });
        this.mDeletePictureButton = (ImageView) this.mRootView.findViewById(R.id.pre_call_picture_deleteview);
        this.mDeletePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                RcsPreCallFragment.this.deleteButtonClick();
            }
        });
        bindPhotoHandler();
        this.mAddPictureLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((PhotoEditorListener) RcsPreCallFragment.this.mPhotoHandler.getListener()).onRequest(1);
            }
        });
        if (this.mIsSavedInstanceState) {
            initScrollViewContent();
        }
        this.mPreCallPreviewButton = (TextView) this.mRootView.findViewById(R.id.pre_call_preview);
        this.mPreCallPreviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                StatisticalHelper.report(1216);
                RcsPreCallFragment.this.startRcsCallPreview();
            }
        });
        this.mDialerButton = (ImageView) this.mRootView.findViewById(R.id.pre_call_rcscall_button);
        this.mDialerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                RcsPreCallFragment.this.dialerButtonClick();
            }
        });
        this.mContext.registerReceiver(this.mHomeKeyEventBroadCastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), "android.permission.INJECT_EVENTS", null);
        this.mIsHomeKeyBroadcastRegistered = true;
        initContactInfo();
        if (this.mRcsPreCallFragmentHelper.isRcsLoginSuccess()) {
            this.mStatusLayout.setVisibility(8);
            setAllEnabled(true);
        } else {
            this.mStatusLayout.setVisibility(0);
            setAllEnabled(false);
        }
        this.mRcsPreCallFragmentHelper.sendRcsQuestCapability(this.mNumber);
        return this.mRootView;
    }

    private void deleteButtonClick() {
        this.mContext.getContentResolver().delete(this.mCurrentPictureUri, null, null);
        this.mCurrentPictureUri = null;
        if (this.mPictureBitmap != null) {
            this.mPictureBitmap.recycle();
            this.mPictureBitmap = null;
        }
        RcsPreCallFragmentHelper rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
        RcsPreCallFragmentHelper.preCallSendImage(null, this.mFormatNumber);
        this.mPictureSendTime = Long.MAX_VALUE;
        this.mAddPictureLayout.setVisibility(0);
        this.mPicturelayout.setVisibility(8);
        StatisticalHelper.report(1213);
    }

    private void dialerButtonClick() {
        RcsPreCallFragmentHelper rcsPreCallFragmentHelper;
        int i;
        this.mIsDialing = true;
        String picturePath = null;
        if (this.mCurrentPictureUri != null) {
            picturePath = pathForPictureOrMap(this.mContext, this.mCurrentPictureUri.getLastPathSegment());
            if (System.currentTimeMillis() - this.mPictureSendTime >= 3600000) {
                HwLog.i("RcsPreCallFragment", "preCallSendImage");
                rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
                RcsPreCallFragmentHelper.preCallSendImage(picturePath, this.mFormatNumber);
            }
        } else {
            HwLog.i("RcsPreCallFragment", "preCallSendImage null");
            rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
            RcsPreCallFragmentHelper.preCallSendImage(null, this.mFormatNumber);
        }
        double lLat = 360.0d;
        double lLon = 360.0d;
        if (this.mIsMapSwtichChecked) {
            lLat = this.mLatitude;
            lLon = this.mLogitude;
        }
        rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
        String str = this.mSubjectString;
        if (this.mIsPrioritySwitchChecked) {
            i = 1;
        } else {
            i = 0;
        }
        Bundle bundle = RcsPreCallFragmentHelper.createComposerInfoBundle(str, i, lLat, lLon);
        rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
        RcsPreCallFragmentHelper.preCallSendCompserInfo(bundle, this.mFormatNumber);
        StatisticalHelper.report(1215);
        RcsContactsUtils.startRcsCall(getActivity(), this.mFormatNumber, this.mSubjectString, this.mIsPrioritySwitchChecked, lLon, lLat, picturePath, true);
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void reStartLoadMap() {
        HwLog.i("RcsPreCallFragment", "reStartLoadMap");
        if (RcsContactsUtils.isSettingsLocationOpen(this.mContext)) {
            this.mMapTextViw.setText(null);
            this.mIsLoadMapTimeout = false;
            this.mMapTimeOutLayout.setVisibility(8);
            this.mPostionProgressBar.setVisibility(0);
            this.mRcsPreCallFragmentHelper.startLoadMap(this.mLatitude, this.mLogitude);
            return;
        }
        gotoPositionDialog(this.mContext);
    }

    private void initContactInfo() {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (RcsPreCallFragment.this.mRequestUri == null) {
                    RcsPreCallFragment.this.mIsQueryFromContacts = false;
                    RcsPreCallFragment.this.mContactInfoHelper = new ContactInfoHelper(RcsPreCallFragment.this.mContext, RcsPreCallFragment.this.lDefaultCountryIso);
                    RcsPreCallFragment.this.mContactInfo = RcsPreCallFragment.this.mContactInfoHelper.lookupNumber(RcsPreCallFragment.this.mNumber, RcsPreCallFragment.this.lDefaultCountryIso);
                    RcsPreCallFragment.this.setDisplayData();
                    RcsPreCallFragment.this.mHandler.sendEmptyMessage(1);
                    return;
                }
                RcsPreCallFragment.this.mIsQueryFromContacts = true;
                RcsPreCallFragment.this.loadRcsPreCallFromContactsProvider(RcsPreCallFragment.this.mRequestUri);
                RcsPreCallFragment.this.mHandler.sendEmptyMessage(0);
            }
        });
    }

    private void insertOrUpdateCacheStatusAccordingNumber() {
        new Thread() {
            public void run() {
                int i;
                ContentResolver resolver = RcsPreCallFragment.this.mContext.getContentResolver();
                ContentValues values = new ContentValues();
                values.put("number", RcsPreCallFragment.this.mFormatNumber);
                String str = "is_primary";
                if (RcsPreCallFragment.this.mIsPrioritySwitchChecked) {
                    i = 1;
                } else {
                    i = 0;
                }
                values.put(str, Integer.valueOf(i));
                values.put("subject", RcsPreCallFragment.this.mSubjectString);
                values.put("picture", RcsPreCallFragment.this.mCurrentPictureUri == null ? null : RcsPreCallFragment.this.mCurrentPictureUri.toString());
                String str2 = "location_switch_state";
                if (RcsPreCallFragment.this.mIsMapSwtichChecked) {
                    i = 1;
                } else {
                    i = 0;
                }
                values.put(str2, Integer.valueOf(i));
                values.put("time", System.currentTimeMillis() + "");
                values.put("from_where", Integer.valueOf(0));
                if (RcsPreCallFragment.this.existInPreCallCacheDB()) {
                    resolver.update(RcsContactsUtils.RCS_PRE_CALL_CACHE_URI, values, "PHONE_NUMBERS_EQUAL(number, ?) AND from_where = 0", new String[]{RcsPreCallFragment.this.mFormatNumber});
                } else {
                    resolver.insert(RcsContactsUtils.RCS_PRE_CALL_CACHE_URI, values);
                }
                HwLog.i("RcsPreCallFragment", "rcs insert complete");
            }
        }.start();
    }

    private void initAndShowSubjectDialog() {
        final ArrayList<String> mSubjectArr = new ArrayList();
        String[] mPresetSubjectArray = getUsualMessages();
        for (Object add : mPresetSubjectArray) {
            mSubjectArr.add(add);
        }
        ArrayAdapter<String> mSubjectAdapter = new ArrayAdapter(this.mContext, R.layout.select_dialog_item, mSubjectArr);
        Builder builder = new Builder(this.mContext).setOnCancelListener(this.mCancelListener).setPositiveButton(this.mContext.getResources().getString(R.string.menu_doNotSave), this.mCancelListener);
        View view = getActivity().getLayoutInflater().inflate(R.layout.precall_dialog_layout, null);
        ListView mListView = (ListView) view.findViewById(R.id.suject_list);
        mListView.setAdapter(mSubjectAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                StatisticalHelper.report(1227);
                String mStringTemp = (String) mSubjectArr.get(position);
                RcsPreCallFragment.this.mSubjectDialog.dismiss();
                RcsPreCallFragment.this.mSubjectString = mStringTemp;
                RcsPreCallFragment.this.mSubjectText.setText(mStringTemp);
                RcsPreCallFragment.this.mSubjectText.setSelection(mStringTemp.length());
            }
        });
        mListView.setOnScrollListener(this);
        builder.setView(view);
        this.mSubjectDialog = builder.create();
        if (this.mSubjectDialog != null && !this.mSubjectDialog.isShowing()) {
            this.mSubjectDialog.show();
        }
    }

    private void updateStatusBarColor() {
        if (getActivity() != null && isAdded() && getActivity().getResources().getConfiguration().orientation != 2) {
            ObjectAnimator animation = ObjectAnimator.ofInt(getActivity().getWindow(), "statusBarColor", new int[]{getActivity().getWindow().getStatusBarColor(), -16777216});
            animation.setDuration(150);
            animation.setEvaluator(new ArgbEvaluator());
            animation.start();
        }
    }

    private void initCacheData(boolean isSavedInstanceState) {
        if (!isSavedInstanceState) {
            if (this.mId == null || this.mIsPrimary == -1) {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        HwLog.i("RcsPreCallFragment", "init savedInstanceState null and load");
                        RcsPreCallFragment.this.loadCacheStatusAccordingNumber();
                        RcsPreCallFragment.this.mHandler.sendEmptyMessage(2);
                    }
                });
            } else {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        HwLog.i("RcsPreCallFragment", "load call log data");
                        RcsPreCallFragment.this.loadCallLogData();
                        RcsPreCallFragment.this.mHandler.sendEmptyMessage(2);
                    }
                });
            }
        }
    }

    private void initScrollViewContent() {
        if (this.mIsPrioritySwitchChecked) {
            this.mPrioritySwitch.setChecked(this.mIsPrioritySwitchChecked);
        }
        if (!TextUtils.isEmpty(this.mSubjectString)) {
            this.mSubjectText.setText(this.mSubjectString);
            this.mSubjectText.setSelection(this.mSubjectString.length());
        }
        if (this.mIsMapSwtichChecked) {
            this.mMapSwitch.setChecked(this.mIsMapSwtichChecked);
        }
        initPictureAndDeleteView(false);
    }

    private void initShowNameComJob() {
        HwLog.i("RcsPreCallFragment", "initShowNameComJob");
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (RcsPreCallFragment.this.mContactInfo != null) {
                    Cursor cursor = null;
                    try {
                        cursor = RcsPreCallFragment.this.mContext.getContentResolver().query(Data.CONTENT_URI, new String[]{"data1", "data4"}, "lookup='" + RcsPreCallFragment.this.getLookupKey(RcsPreCallFragment.this.mContactInfo.lookupUri) + "'AND " + "mimetype" + "='" + "vnd.android.cursor.item/organization" + "'", null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            RcsPreCallFragment.this.mCompanyString = cursor.getString(cursor.getColumnIndex("data1"));
                            RcsPreCallFragment.this.mJobString = cursor.getString(cursor.getColumnIndex("data4"));
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Exception e) {
                        HwLog.e("RcsPreCallFragment", "get company job exception");
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                RcsPreCallFragment.this.mHandler.sendEmptyMessage(0);
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadRcsPreCallFromContactsProvider(Uri contactUri) {
        HwLog.i("RcsPreCallFragment", " loadContactHeaderData begin");
        ContentResolver resolver = this.mContext.getContentResolver();
        Cursor cursor = resolver.query(Uri.withAppendedPath(ContactLoaderUtils.ensureIsContactUri(resolver, contactUri), "entities"), ContactQuery.getColumns(), null, null, "raw_contact_id");
        if (cursor == null) {
            HwLog.e("RcsPreCallFragment", "No cursor returned in RcsPreCallFragment");
            return;
        }
        try {
            if (cursor.moveToFirst()) {
                loadContactHeaderData(cursor, contactUri);
            }
            cursor.close();
        } catch (Exception e) {
            HwLog.e("RcsPreCallFragment", "get data from cursor exception");
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private void loadContactHeaderData(Cursor cursor, Uri contactUri) {
        if (this.mContactInfo == null) {
            this.mContactInfo = new ContactInfo();
        }
        if (cursor != null) {
            this.mdisplayNameSource = cursor.getInt(1);
            this.mdisplayName = cursor.getString(3);
            this.mPhotoUri = cursor.getString(60);
            this.mContactInfo.lookupUri = this.mRequestUri;
            if (this.mPhotoUri != null) {
                this.mContactInfo.photoUri = Uri.parse(this.mPhotoUri);
            }
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                if (cursor.getString(50).equals("vnd.android.cursor.item/organization")) {
                    this.mCompanyString = cursor.getString(28);
                    this.mJobString = cursor.getString(31);
                    break;
                }
            }
            HwLog.i("RcsPreCallFragment", " loadContactHeaderData finish");
        }
    }

    private void showNameCompanyJob() {
        HwLog.i("RcsPreCallFragment", " showNameCompanyJob");
        String displayName = this.mdisplayName;
        if (this.mContactInfo == null) {
            this.mPreCallName.setText(this.mNumber);
            this.mPreCallCompany.setVisibility(8);
            this.mPreCallJob.setVisibility(8);
            return;
        }
        boolean isEmptyCompany = TextUtils.isEmpty(this.mCompanyString);
        boolean isEmptyProfession = TextUtils.isEmpty(this.mJobString);
        if (TextUtils.isEmpty(displayName)) {
            displayName = this.mNumber;
        } else if (!(isEmptyCompany && isEmptyProfession) && this.mdisplayNameSource == 30) {
            if (displayName.equals(this.mCompanyString)) {
                displayName = this.mCompanyString;
                this.mCompanyString = "";
            } else if (displayName.equals(this.mJobString)) {
                displayName = this.mJobString;
                this.mJobString = "";
            }
        }
        ContactDetailDisplayUtils.setDataOrHideIfNone(displayName, this.mPreCallName);
        ContactDetailDisplayUtils.setDataOrHideIfNone(this.mCompanyString, this.mPreCallCompany);
        ContactDetailDisplayUtils.setDataOrHideIfNone(this.mJobString, this.mPreCallJob);
    }

    private long getContactId(Context context, ContactInfo contactInfo) {
        long contactId = -1;
        if (contactInfo == null) {
            return -1;
        }
        Uri lookupUri = contactInfo.lookupUri;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Contacts.CONTENT_URI, new String[]{"_id"}, "lookup='" + getLookupKey(lookupUri) + "'", null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                contactId = cursor.getLong(cursor.getColumnIndex("_id"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("RcsPreCallFragment", "get contactid exception");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactId;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        hideSoftInputWindow();
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        hideSoftInputWindow();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        hideSoftInputWindow();
        int id = buttonView.getId();
        HwLog.i("RcsPreCallFragment", " onCheckedChanged");
        switch (id) {
            case R.id.pre_call_priority_switch:
                if (isChecked) {
                    this.mIsPrioritySwitchChecked = true;
                    StatisticalHelper.report(1208);
                } else {
                    this.mIsPrioritySwitchChecked = false;
                    StatisticalHelper.report(1209);
                }
                if (this.mRcsPreCallFragmentHelper.isRcsLoginSuccess()) {
                    this.mPriorityText.setTextColor(this.mContext.getResources().getColor(R.color.rcs_text_color_black));
                    return;
                } else {
                    this.mPriorityText.setTextColor(this.mContext.getResources().getColor(R.color.rcs_text_color_grey));
                    return;
                }
            case R.id.pre_call_location_switch:
                if (isChecked) {
                    StatisticalHelper.report(1210);
                    if (getActivity() != null && getActivity().checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
                        this.mMapSwitch.setChecked(false);
                        setMapSwitchCloseStatus();
                        requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 1);
                        return;
                    } else if (RcsContactsUtils.isSettingsLocationOpen(this.mContext)) {
                        this.mIsMapSwtichChecked = true;
                        HwLog.i("RcsPreCallFragment", "location swich open startLoadMap");
                        displayOrStartLoadMap();
                        return;
                    } else {
                        this.mMapSwitch.setChecked(false);
                        setMapSwitchCloseStatus();
                        gotoPositionDialog(this.mContext);
                        return;
                    }
                }
                setMapSwitchCloseStatus();
                return;
            default:
                return;
        }
    }

    private void displayOrStartLoadMap() {
        if (!TextUtils.isEmpty(this.mAddressString)) {
            setMapViewsContentDisplay(this.mAddressString, 8, 8, 0);
            this.mRcsPreCallFragmentHelper.displayMap(this.mLatitude, this.mLogitude);
        } else if (this.mIsLoadMapTimeout) {
            setMapViewsContentDisplay(null, 8, 0, 0);
        } else {
            setMapViewsContentDisplay(null, 0, 8, 0);
            this.mRcsPreCallFragmentHelper.startLoadMap(this.mLatitude, this.mLogitude);
        }
    }

    private void setMapViewsContentDisplay(String address, int progressBarVisible, int timeoutVisible, int containerVisible) {
        this.mMapTextViw.setText(address);
        this.mPostionProgressBar.setVisibility(progressBarVisible);
        this.mMapDiaplayContainer.setVisibility(containerVisible);
        this.mMapTimeOutLayout.setVisibility(timeoutVisible);
    }

    private void setMapSwitchCloseStatus() {
        StatisticalHelper.report(1211);
        this.mLogitude = 360.0d;
        this.mLatitude = 360.0d;
        this.mAddressString = null;
        this.mIsLoadMapTimeout = false;
        this.mIsMapSwtichChecked = false;
        this.mMapDiaplayContainer.setVisibility(8);
        if (this.mCurrentMapUri != null) {
            this.mContext.getContentResolver().delete(this.mCurrentMapUri, null, null);
        }
        this.mCurrentMapUri = null;
    }

    public void setLanLng(double lan, double log) {
        this.mLatitude = lan;
        this.mLogitude = log;
        HwLog.i("RcsPreCallFragment", "setLanLng");
    }

    public void setMapAddress(String address) {
        HwLog.i("RcsPreCallFragment", "setMapAddress");
        if (this.mIsMapSwtichChecked) {
            this.mAddressString = address;
        } else {
            this.mAddressString = null;
        }
        this.mIsLoadMapTimeout = false;
        this.mMapTextViw.setText(this.mAddressString);
        this.mPostionProgressBar.setVisibility(8);
        this.mMapTimeOutLayout.setVisibility(8);
        this.mRcsPreCallFragmentHelper.setStartLoadMap(false);
    }

    public void setMapTimeOutView() {
        this.mLogitude = 360.0d;
        this.mLatitude = 360.0d;
        this.mPostionProgressBar.setVisibility(8);
        this.mMapTimeOutLayout.setVisibility(0);
        HwLog.i("RcsPreCallFragment", "Timeout, map switch open ? " + this.mIsMapSwtichChecked);
        this.mIsLoadMapTimeout = this.mIsMapSwtichChecked;
        this.mAddressString = null;
    }

    public void snapshotMap(Bitmap bitmap) {
        HwLog.i("RcsPreCallFragment", "snapshotMap bitmap");
        if (bitmap != null) {
            this.mCurrentMapUri = generateRcsLocationUri(this.mContext);
            writeBitmapToUri(this.mContext, this.mCurrentMapUri, bitmap);
        }
    }

    private void initPictureAndDeleteView(boolean isLoadingPicture) {
        HwLog.i("RcsPreCallFragment", "initPictureAndDeleteView");
        if (this.mCurrentPictureUri != null) {
            try {
                this.mPictureBitmap = ContactPhotoUtils.getBitmapFromUri(this.mContext, this.mCurrentPictureUri);
            } catch (FileNotFoundException e) {
                HwLog.i("RcsPreCallFragment", " picture bitmap not found!");
            } catch (Exception e2) {
                HwLog.i("RcsPreCallFragment", " picture bitmap some error!");
            }
            if (this.mPictureBitmap != null) {
                int pictureViewWidth;
                this.mAddPictureLayout.setVisibility(8);
                this.mPictureProgressBar.setVisibility(8);
                this.mDeletePictureButton.setVisibility(0);
                this.mPicturelayout.setVisibility(0);
                int pictureViewHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_picture_height);
                if (this.mContext.getResources().getConfiguration().orientation == 1) {
                    pictureViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_picture_width);
                } else {
                    pictureViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_picture_width_land);
                }
                this.mPictureBitmap = ProfileUtils.cutBitmapAndScale(this.mPictureBitmap, pictureViewWidth, pictureViewHeight, true, false);
                this.mPictureView.setImageDrawable(new BitmapDrawable(this.mContext.getResources(), this.mPictureBitmap));
                StatisticalHelper.report(1212);
                return;
            }
            this.mPicturelayout.setVisibility(8);
            this.mAddPictureLayout.setVisibility(0);
        } else if (isLoadingPicture) {
            this.mPicturelayout.setVisibility(0);
            this.mPictureView.setVisibility(0);
            this.mPictureView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rcs_pre_call_default_picture));
            this.mPictureProgressBar.setVisibility(0);
            this.mDeletePictureButton.setVisibility(8);
            this.mAddPictureLayout.setVisibility(8);
        } else {
            this.mPicturelayout.setVisibility(8);
            this.mAddPictureLayout.setVisibility(0);
        }
    }

    private Uri generateRcsPictureUri(Context context) {
        return FileProvider.getUriForFile(context, "com.android.contacts.files", new File(getRootFilePath(context), generateRcsPictureName()));
    }

    private Uri generateRcsLocationUri(Context context) {
        return FileProvider.getUriForFile(context, "com.android.contacts.files", new File(getRootFilePath(context), generateRcsLocationName()));
    }

    private String generateRcsPictureName() {
        Date date = new Date(System.currentTimeMillis());
        return "RcsContactPicture-" + new SimpleDateFormat("'PICTURE'_yyyyMMdd_HHmmss", Locale.US).format(date) + ".jpg";
    }

    private String generateRcsLocationName() {
        Date date = new Date(System.currentTimeMillis());
        return "RcsContactLocation-" + new SimpleDateFormat("'LOCATION'_yyyyMMdd_HHmmss", Locale.US).format(date) + ".jpg";
    }

    private String pathForPictureOrMap(Context context, String fileName) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), "/Android/data/RCS");
        }
        try {
            dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(dir, fileName).getAbsolutePath();
    }

    private static void writeBitmapToUri(Context context, Uri uri, Bitmap bitmap) {
        IOException e;
        Throwable th;
        OutputStream outputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                bitmap.compress(CompressFormat.PNG, 100, baos);
                byte[] mImage = baos.toByteArray();
                if (outputStream != null) {
                    outputStream.write(mImage, 0, mImage.length);
                    HwLog.i("RcsPreCallFragment", "writeBitmapToUri success");
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        HwLog.i("RcsPreCallFragment", "IOException " + e2.getMessage());
                    }
                }
                if (baos != null) {
                    baos.close();
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                byteArrayOutputStream = baos;
            } catch (IOException e3) {
                e2 = e3;
                byteArrayOutputStream = baos;
                try {
                    HwLog.i("RcsPreCallFragment", "IOException " + e2.getMessage());
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e22) {
                            HwLog.i("RcsPreCallFragment", "IOException " + e22.getMessage());
                            if (bitmap == null) {
                                bitmap.recycle();
                            }
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (bitmap == null) {
                        bitmap.recycle();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e222) {
                            HwLog.i("RcsPreCallFragment", "IOException " + e222.getMessage());
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            throw th;
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = baos;
                if (outputStream != null) {
                    outputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                throw th;
            }
        } catch (IOException e4) {
            e222 = e4;
            HwLog.i("RcsPreCallFragment", "IOException " + e222.getMessage());
            if (outputStream != null) {
                outputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bitmap == null) {
                bitmap.recycle();
            }
        }
    }

    private void writeByteToUri(Context context, Uri uri, byte[] data) {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(data, 0, data.length);
                HwLog.i("RcsPreCallFragment", "writeByteToUri success");
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    HwLog.i("RcsPreCallFragment", "IOException " + e.getMessage());
                }
            }
        } catch (IOException e2) {
            HwLog.i("RcsPreCallFragment", "IOException " + e2.getMessage());
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e22) {
                    HwLog.i("RcsPreCallFragment", "IOException " + e22.getMessage());
                }
            }
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e222) {
                    HwLog.i("RcsPreCallFragment", "IOException " + e222.getMessage());
                }
            }
        }
    }

    private static File getRootFilePath(Context context) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), "/Android/data/RCS");
        }
        if (!(dir == null || dir.exists() || !dir.mkdirs())) {
            HwLog.d("RcsPreCallFragment", "dit make success");
        }
        return dir;
    }

    private void startRcsCallPreview() {
        HwLog.i("RcsPreCallFragment", "start preview picturePath");
        Intent mIntent = new Intent();
        String picturePath = null;
        String mapPath = null;
        if (this.mCurrentPictureUri != null) {
            picturePath = pathForPictureOrMap(this.mContext, this.mCurrentPictureUri.getLastPathSegment());
        }
        if (this.mCurrentMapUri != null && this.mIsMapSwtichChecked) {
            mapPath = pathForPictureOrMap(this.mContext, this.mCurrentMapUri.getLastPathSegment());
        }
        mIntent.putExtra("is_primary", this.mIsPrioritySwitchChecked);
        mIntent.putExtra("subject", this.mSubjectString);
        mIntent.putExtra("location_address", this.mAddressString);
        mIntent.putExtra("map_path", mapPath);
        mIntent.putExtra("picture", picturePath);
        if (this.mIsMapSwtichChecked) {
            mIntent.putExtra("longitude", this.mLogitude);
            mIntent.putExtra("latitude", this.mLatitude);
        } else {
            mIntent.putExtra("longitude", 360.0d);
            mIntent.putExtra("latitude", 360.0d);
        }
        mIntent.putExtra("picture_send_time", this.mPictureSendTime);
        mIntent.putExtra("pre_call_number", this.mFormatNumber);
        mIntent.setClass(this.mContext, RcsPreCallPreviewActivity.class);
        startActivityForResult(mIntent, 5000);
    }

    private String getLookupKey(Uri lookUpUri) {
        if (lookUpUri == null) {
            HwLog.w("RcsPreCallFragment", "lookUpUri is null");
            return null;
        }
        List<String> pathSegmentList = lookUpUri.getPathSegments();
        String str = null;
        if (pathSegmentList.size() == 4) {
            try {
                str = (String) pathSegmentList.get(2);
            } catch (NumberFormatException e) {
                str = null;
            }
        }
        return str;
    }

    private Bitmap getBigPhoto() {
        HwLog.i("RcsPreCallFragment", " getBigPhoto");
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            if ("photo".equals(this.mContactInfo.photoUri.getLastPathSegment())) {
                return null;
            }
            assetFileDescriptor = this.mContext.getContentResolver().openAssetFileDescriptor(this.mContactInfo.photoUri, "r");
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    try {
                        assetFileDescriptor.close();
                    } catch (IOException e) {
                        HwLog.i("RcsPreCallFragment", " IO exception!!!");
                    }
                }
                return null;
            }
            FileDescriptor fileDescriptor = assetFileDescriptor.getFileDescriptor();
            if (fileDescriptor != null) {
                Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
                if (assetFileDescriptor != null) {
                    try {
                        assetFileDescriptor.close();
                    } catch (IOException e2) {
                        HwLog.i("RcsPreCallFragment", " IO exception!!!");
                    }
                }
                return decodeFileDescriptor;
            }
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e3) {
                    HwLog.i("RcsPreCallFragment", " IO exception!!!");
                }
            }
            return null;
        } catch (FileNotFoundException e4) {
            HwLog.i("RcsPreCallFragment", " file not found!!!");
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e5) {
                    HwLog.i("RcsPreCallFragment", " IO exception!!!");
                }
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e6) {
                    HwLog.i("RcsPreCallFragment", " IO exception!!!");
                }
            }
        }
    }

    private Bitmap getLittlePhoto() {
        HwLog.i("RcsPreCallFragment", "getLittlePhoto");
        Cursor cursor = null;
        Bitmap bitmap = null;
        try {
            cursor = this.mContext.getContentResolver().query(Data.CONTENT_URI, new String[]{"data15"}, "lookup='" + getLookupKey(this.mContactInfo.lookupUri) + "'AND " + "mimetype" + "='" + "vnd.android.cursor.item/photo" + "'", null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                byte[] in = cursor.getBlob(cursor.getColumnIndex("data15"));
                bitmap = BitmapFactory.decodeByteArray(in, 0, in.length);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("RcsPreCallFragment", "getLittlePhoto Exception!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bitmap;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("currentSubjectString", this.mSubjectString);
        savedInstanceState.putParcelable("currentPictureUri", this.mCurrentPictureUri);
        savedInstanceState.putBoolean("priority", this.mIsPrioritySwitchChecked);
        savedInstanceState.putString("currentAddressString", this.mAddressString);
        savedInstanceState.putBoolean("isLoadmapTimeout", this.mIsLoadMapTimeout);
        savedInstanceState.putDouble("currentLatitude", this.mLatitude);
        savedInstanceState.putDouble("currentLongitude", this.mLogitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void initGradientView(boolean hasBigPhoto) {
        if (hasBigPhoto) {
            int[] gradientColors = new int[]{0, Integer.MIN_VALUE};
            int actionBarSize = ContactDpiAdapter.getActionbarHeight(this.mContext);
            this.mTitleGradientView.setBackground(new GradientDrawable(Orientation.TOP_BOTTOM, gradientColors));
            LayoutParams titleGradientLayoutParams = (LayoutParams) this.mTitleGradientView.getLayoutParams();
            titleGradientLayoutParams.height = actionBarSize * 2;
            this.mTitleGradientView.setLayoutParams(titleGradientLayoutParams);
            this.mTitleGradientView.setVisibility(0);
            if (this.mContext.getResources().getConfiguration().orientation == 1) {
                this.mActionBarGradientView.setBackground(new GradientDrawable(Orientation.BOTTOM_TOP, gradientColors));
                LayoutParams actionBarGradientLayoutParams = (LayoutParams) this.mActionBarGradientView.getLayoutParams();
                actionBarGradientLayoutParams.height = actionBarSize * 2;
                this.mActionBarGradientView.setLayoutParams(actionBarGradientLayoutParams);
                this.mActionBarGradientView.setVisibility(0);
                return;
            }
            this.mActionBarGradientView.setVisibility(8);
            return;
        }
        this.mActionBarGradientView.setVisibility(8);
        this.mTitleGradientView.setVisibility(8);
    }

    private void processAndSendPicture(Bitmap bitmap) {
        Uri lPictureUri = generateRcsPictureUri(this.mContext);
        byte[] pictureData = RcsBitmapUtils.compressBitmap(bitmap, 80);
        bitmap.recycle();
        writeByteToUri(this.mContext, lPictureUri, pictureData);
        this.mCurrentPictureUri = lPictureUri;
        String picturePath = pathForPictureOrMap(this.mContext, this.mCurrentPictureUri.getLastPathSegment());
        if (!this.mIsDialing) {
            RcsPreCallFragmentHelper rcsPreCallFragmentHelper = this.mRcsPreCallFragmentHelper;
            RcsPreCallFragmentHelper.preCallSendImage(picturePath, this.mFormatNumber);
        }
        setPictureUrlAndTime(System.currentTimeMillis());
        this.mHandler.sendEmptyMessage(2);
    }

    private void bindPhotoHandler() {
        this.mPhotoHandler = new PhotoHandler(this.mContext, this.mPictureView, 4);
    }

    public void setPictureUrlAndTime(long time) {
        this.mPictureSendTime = time;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (5000 == requestCode) {
            if (-1 == resultCode) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
            }
        } else if (this.mPhotoHandler != null && this.mPhotoHandler.handlePhotoActivityResult(requestCode, resultCode, data)) {
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        if (this.mSubjectDialog != null) {
            this.mSubjectDialog.dismiss();
        }
        if (this.mPhotoHandler != null) {
            this.mPhotoHandler.destroy();
        }
    }

    public void onDestroy() {
        HwLog.i("RcsPreCallFragment", " onDestroy");
        super.onDestroy();
        if (this.mSubjectDialog != null && this.mSubjectDialog.isShowing()) {
            this.mSubjectDialog.dismiss();
        }
        if (this.mPhotoHandler != null) {
            this.mPhotoHandler.destroy();
        }
        if (this.mCurrentMapUri != null) {
            this.mContext.getContentResolver().delete(this.mCurrentMapUri, null, null);
        }
        if (this.mPhotoBitmap != null) {
            this.mPhotoBitmap.recycle();
            this.mPhotoBitmap = null;
        }
        if (this.mPictureBitmap != null) {
            this.mPictureBitmap.recycle();
            this.mPictureBitmap = null;
        }
        this.mHandler.removeCallbacksAndMessages(null);
        this.mRcsPreCallFragmentHelper.handleCustomizationsOnDestroy(getActivity());
        if (this.mIsHomeKeyBroadcastRegistered && this.mHomeKeyEventBroadCastReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mHomeKeyEventBroadCastReceiver);
                this.mIsHomeKeyBroadcastRegistered = false;
            } catch (IllegalArgumentException e) {
                HwLog.e("RcsPreCallFragment", "mHomeKeyEventBroadCastReceiver is not registered: " + e);
            }
        }
    }

    public void onBackPressed() {
        HwLog.i("RcsPreCallFragment", "rcs back");
        insertOrUpdateCacheStatusAccordingNumber();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isAdded()) {
            switch (requestCode) {
                case 1:
                    if (permissions != null && permissions.length > 0) {
                        for (int i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e) {
                                    HwLog.e("RcsPreCallFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                    }
                    if (this.mMapSwitch != null) {
                        this.mMapSwitch.setChecked(true);
                        break;
                    }
                    break;
            }
        }
    }

    public void setAllEnabled(boolean enable) {
        this.mPriorityLayout.setEnabled(enable);
        this.mPrioritySwitch.setEnabled(enable);
        this.mSubjectText.setEnabled(enable);
        this.mSubjectText.setCursorVisible(enable);
        this.mSubjectDialogButton.setEnabled(enable);
        this.mMapCheckContainer.setEnabled(enable);
        this.mMapSwitch.setEnabled(enable);
        this.mAddPictureLayout.setEnabled(enable);
        this.mDeletePictureButton.setEnabled(enable);
        this.mPreCallPreviewButton.setEnabled(enable);
        this.mDialerButton.setEnabled(enable);
        this.mMapTimeOutText.setEnabled(enable);
        int blackColorId = this.mContext.getResources().getColor(R.color.rcs_text_color_black);
        int greyColorId = this.mContext.getResources().getColor(R.color.rcs_text_color_grey);
        if (enable) {
            this.mPriorityText.setTextColor(blackColorId);
            if (TextUtils.isEmpty(this.mSubjectString) || this.mSubjectString.length() < 60) {
                this.mSubjectInputCountView.setTextColor(this.mContext.getResources().getColor(R.color.rcs_input_count_noraml_color));
            } else {
                this.mSubjectInputCountView.setTextColor(this.mContext.getResources().getColor(R.color.rcs_input_count_tip_color));
            }
            this.mSubjectText.setTextColor(blackColorId);
            this.mMapHeadTextView.setTextColor(blackColorId);
            this.mAddPictureHeadTextView.setTextColor(blackColorId);
            return;
        }
        this.mPriorityText.setTextColor(greyColorId);
        this.mSubjectText.setTextColor(greyColorId);
        this.mSubjectInputCountView.setTextColor(greyColorId);
        if (TextUtils.isEmpty(this.mSubjectString) || this.mSubjectString.length() < 60) {
            this.mSubjectInputCountView.setTextColor(greyColorId);
        } else {
            this.mSubjectInputCountView.setTextColor(this.mContext.getResources().getColor(R.color.red_icon_disable_color));
        }
        this.mMapHeadTextView.setTextColor(greyColorId);
        this.mAddPictureHeadTextView.setTextColor(greyColorId);
    }

    private void hideSoftInputWindow() {
        Activity activity = getActivity();
        if (activity != null && activity.getWindow() != null) {
            View lCurrentViewWithFocus = activity.getWindow().getCurrentFocus();
            if (lCurrentViewWithFocus != null && lCurrentViewWithFocus.getWindowToken() != null) {
                InputMethodManager lManager = (InputMethodManager) activity.getSystemService("input_method");
                if (lManager != null) {
                    lManager.hideSoftInputFromWindow(lCurrentViewWithFocus.getWindowToken(), 0);
                }
            }
        }
    }

    private Cursor queryPreCallCacheDB() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(RcsContactsUtils.RCS_PRE_CALL_CACHE_URI, PROJECTION, "PHONE_NUMBERS_EQUAL(number, ?) AND from_where = 0", new String[]{this.mFormatNumber}, null);
        } catch (IllegalArgumentException e) {
            HwLog.e("RcsPreCallFragment", "queryPreCallCacheDB query failed");
        }
        return cursor;
    }

    private boolean existInPreCallCacheDB() {
        boolean isCached = false;
        Uri uri = null;
        Cursor cursor = null;
        try {
            cursor = queryPreCallCacheDB();
            if (cursor == null || !cursor.moveToFirst()) {
                isCached = false;
            } else {
                isCached = true;
                String strPath = cursor.getString(cursor.getColumnIndex("picture"));
                if (!(strPath == null || strPath.isEmpty())) {
                    uri = Uri.parse(strPath);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            HwLog.e("RcsPreCallFragment", "existInPreCallCacheDB query failed");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (!(!isCached || uri == null || this.mCurrentPictureUri == null || uri.equals(this.mCurrentPictureUri))) {
            this.mContext.getContentResolver().delete(uri, null, null);
        }
        return isCached;
    }

    private Uri getPictureUri(String picture) {
        return picture == null ? null : FileProvider.getUriForFile(this.mContext, "com.android.contacts.files", new File(picture));
    }

    private Uri backupPicture(Uri inUri) {
        Uri outputUri = generateRcsPictureUri(this.mContext);
        ContactPhotoUtils.savePhotoFromUriToUri(this.mContext, inUri, outputUri, false);
        return outputUri;
    }

    private void setDisplayData() {
        if (this.mContactInfo != null) {
            this.mdisplayName = this.mContactInfo.name;
        }
    }

    public static String[] getUsualMessages() {
        String[] messages = new String[]{"", "", "", "", ""};
        if (RcseProfile.getRcsService() != null) {
            try {
                messages = RcseProfile.getRcsService().getUsualMessages();
            } catch (RemoteException e) {
                HwLog.e("RcsPreCallFragment", "call getUsualMessages in RcseTransaction error, RemoteException: " + e);
            }
        }
        return messages;
    }

    private void gotoPositionDialog(Context context) {
        AlertDialog dialog = new Builder(context).setMessage(context.getResources().getString(R.string.rcs_openlocation_dialog_message)).setPositiveButton(R.string.CS_btn_ok, this.mDialogListener).setNegativeButton(R.string.CS_btn_cancel, null).setCancelable(false).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
